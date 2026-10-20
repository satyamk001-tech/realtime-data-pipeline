package com.dataflow.backend.service;

import com.dataflow.backend.dto.PipelineJobDto;
import com.dataflow.backend.dto.PipelineRunRequest;
import com.dataflow.backend.entity.PipelineJobAttemptEntity;
import com.dataflow.backend.entity.PipelineJobEntity;
import com.dataflow.backend.entity.RestaurantEntity;
import com.dataflow.backend.exception.ResourceNotFoundException;
import com.dataflow.backend.mapper.PipelineJobMapper;
import com.dataflow.backend.repository.PipelineJobRepository;
import com.dataflow.backend.repository.RestaurantRepository;
import com.dataflow.ingestion.model.IngestionJob;
import com.dataflow.ingestion.model.IngestionStatus;
import com.dataflow.ingestion.service.IngestionService;
import com.dataflow.ingestion.storage.LocalStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PipelineJobService {
    private static final Logger log = LoggerFactory.getLogger(PipelineJobService.class);

    private final PipelineJobRepository pipelineJobRepository;
    private final RestaurantRepository restaurantRepository;

    public PipelineJobService(PipelineJobRepository pipelineJobRepository,
                              RestaurantRepository restaurantRepository) {
        this.pipelineJobRepository = pipelineJobRepository;
        this.restaurantRepository = restaurantRepository;
    }

    @Transactional(readOnly = true)
    public List<PipelineJobDto> getAllJobs() {
        return pipelineJobRepository.findAllByOrderByStartedAtDesc().stream()
                .map(PipelineJobMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PipelineJobDto getJobById(String id) {
        PipelineJobEntity entity = pipelineJobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pipeline job not found with id: " + id));
        return PipelineJobMapper.toDto(entity);
    }

    @Transactional
    public PipelineJobDto triggerPipeline(PipelineRunRequest request) {
        String jobId = "job-" + UUID.randomUUID().toString().substring(0, 8);
        String runId = "run-" + UUID.randomUUID().toString();
        String sourcePath = (request.getSourcePath() != null && !request.getSourcePath().isEmpty())
                ? request.getSourcePath()
                : "data/samples/zomato_restaurants.csv";

        PipelineJobEntity entity = new PipelineJobEntity();
        entity.setId(jobId);
        entity.setRunId(runId);
        entity.setPipelineType(request.getPipelineType() != null ? request.getPipelineType() : "BATCH_ETL");
        entity.setStatus("PENDING");
        entity.setSourcePath(sourcePath);
        entity.setTriggeredBy(request.getTriggeredBy() != null ? request.getTriggeredBy() : "REST_API");
        entity.setStartedAt(Instant.now());

        PipelineJobAttemptEntity attempt = new PipelineJobAttemptEntity();
        attempt.setJob(entity);
        attempt.setAttemptNumber(1);
        attempt.setStatus("PENDING");
        attempt.setStartedAt(Instant.now());
        entity.getAttempts().add(attempt);

        PipelineJobEntity saved = pipelineJobRepository.save(entity);
        log.info("Created pipeline job ticket [{}] with status PENDING", jobId);

        // Execute asynchronously
        executeAsyncPipeline(jobId, runId, sourcePath);

        return PipelineJobMapper.toDto(saved);
    }

    @Async("pipelineTaskExecutor")
    public void executeAsyncPipeline(String jobId, String runId, String sourcePath) {
        log.info("Worker picked up async pipeline run [{}] for job [{}]", runId, jobId);
        long start = System.currentTimeMillis();

        PipelineJobEntity job = pipelineJobRepository.findById(jobId).orElse(null);
        if (job == null) return;

        job.setStatus("RUNNING");
        pipelineJobRepository.save(job);

        try {
            File sourceFile = new File(sourcePath);
            if (!sourceFile.exists()) {
                throw new RuntimeException("Source data file not found at: " + sourceFile.getAbsolutePath());
            }

            // Ingest file using Java Ingestion Service
            LocalStorageService localStorage = new LocalStorageService("target/lake");
            IngestionService ingestionService = new IngestionService(localStorage, "dataflow-lake");
            IngestionJob ingestionJob = ingestionService.ingestFile(sourceFile, job.getTriggeredBy());

            if (ingestionJob.getStatus() != IngestionStatus.SUCCEEDED) {
                throw new RuntimeException("Ingestion phase failed: " + ingestionJob.getErrorMessage());
            }

            // Process records and synchronize to operational store
            long validCount = 0;
            long quarantinedCount = 0;
            List<RestaurantEntity> batchToPersist = new ArrayList<>();

            try (BufferedReader reader = new BufferedReader(new FileReader(sourceFile, StandardCharsets.UTF_8))) {
                String header = reader.readLine();
                String line;
                while ((line = reader.readLine()) != null) {
                    var tokens = parseCsvLine(line);
                    if (tokens.size() >= 13) {
                        try {
                            Long rId = Long.parseLong(tokens.get(0).trim());
                            String name = tokens.get(1).trim();
                            String city = tokens.get(2).trim();
                            String costStr = tokens.get(6).replace(",", "").replace("\"", "").trim();
                            double cost = Double.parseDouble(costStr);
                            double rating = Double.parseDouble(tokens.get(11).trim());

                            if (name.isEmpty() || city.isEmpty() || cost <= 0 || rating < 1.0 || rating > 5.0) {
                                quarantinedCount++;
                                continue;
                            }

                            RestaurantEntity entity = new RestaurantEntity();
                            entity.setId(rId);
                            entity.setName(name);
                            entity.setCity(city);
                            entity.setAddress(tokens.size() > 3 ? tokens.get(3).trim() : "");
                            entity.setLocality(tokens.size() > 4 ? tokens.get(4).trim() : "");
                            entity.setCuisines(tokens.size() > 5 ? tokens.get(5).trim() : "");
                            entity.setAverageCostForTwo(BigDecimal.valueOf(cost));
                            entity.setCurrency(tokens.size() > 7 ? tokens.get(7).trim() : "INR");
                            entity.setHasOnlineDelivery("Yes".equalsIgnoreCase(tokens.get(8).trim()));
                            entity.setHasTableBooking("Yes".equalsIgnoreCase(tokens.get(9).trim()));
                            entity.setPriceRange(tokens.size() > 10 ? Integer.parseInt(tokens.get(10).trim()) : 1);
                            entity.setAggregateRating(BigDecimal.valueOf(rating));
                            entity.setRatingText(tokens.size() > 12 ? tokens.get(12).trim() : "Good");
                            entity.setVotes(tokens.size() > 13 ? Long.parseLong(tokens.get(13).trim()) : 0L);
                            entity.setPipelineRunId(runId);

                            batchToPersist.add(entity);
                            validCount++;
                        } catch (Exception parseEx) {
                            quarantinedCount++;
                        }
                    } else {
                        quarantinedCount++;
                    }
                }
            }

            restaurantRepository.saveAll(batchToPersist);

            long duration = System.currentTimeMillis() - start;
            job.setStatus("SUCCEEDED");
            job.setInputRecords(validCount + quarantinedCount);
            job.setValidRecords(validCount);
            job.setQuarantinedRecords(quarantinedCount);
            job.setTargetPath(ingestionJob.getS3RawPath());
            job.setDurationMs(duration);
            job.setCompletedAt(Instant.now());
            pipelineJobRepository.save(job);

            log.info("Pipeline job [{}] completed in {} ms. Valid={}, Quarantined={}",
                    jobId, duration, validCount, quarantinedCount);

        } catch (Exception e) {
            log.error("Pipeline job [{}] failed", jobId, e);
            job.setStatus("FAILED");
            job.setErrorMessage(e.getMessage());
            job.setDurationMs(System.currentTimeMillis() - start);
            job.setCompletedAt(Instant.now());
            pipelineJobRepository.save(job);
        }
    }

    private static List<String> parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString().trim());
        return tokens;
    }
}
