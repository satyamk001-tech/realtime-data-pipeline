package com.dataflow.ingestion.service;

import com.dataflow.ingestion.model.IngestionJob;
import com.dataflow.ingestion.model.IngestionManifest;
import com.dataflow.ingestion.model.IngestionStatus;
import com.dataflow.ingestion.storage.StorageService;
import com.dataflow.ingestion.validation.CsvValidator;
import com.dataflow.ingestion.validation.ValidationResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

public class IngestionService {
    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    private final StorageService storageService;
    private final CsvValidator csvValidator;
    private final ObjectMapper objectMapper;
    private final String defaultBucket;

    public IngestionService(StorageService storageService, String defaultBucket) {
        this.storageService = storageService;
        this.csvValidator = new CsvValidator();
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        this.defaultBucket = defaultBucket;
    }

    public IngestionJob ingestFile(File file, String triggeredBy) {
        String jobId = "job-" + UUID.randomUUID().toString().substring(0, 8);
        String runId = "run-" + UUID.randomUUID().toString();
        Instant startTime = Instant.now();

        IngestionJob job = IngestionJob.builder()
                .jobId(jobId)
                .runId(runId)
                .sourceFilePath(file != null ? file.getAbsolutePath() : "null")
                .fileName(file != null ? file.getName() : "null")
                .fileSizeBytes(file != null && file.exists() ? file.length() : 0)
                .status(IngestionStatus.RUNNING)
                .startedAt(startTime)
                .build();

        log.info("Starting ingestion [{}] for file: {}", runId, job.getSourceFilePath());

        try {
            // Step 1: Pre-validation
            ValidationResult validation = csvValidator.validate(file);
            if (!validation.isValid()) {
                String errorMsg = "Validation failed: " + String.join("; ", validation.getErrors());
                log.error("Ingestion [{}] aborted: {}", runId, errorMsg);
                job.setStatus(IngestionStatus.FAILED);
                job.setErrorMessage(errorMsg);
                job.setCompletedAt(Instant.now());
                return job;
            }

            job.setRecordCount(validation.getEstimatedRowCount());

            // Step 2: Compute SHA-256 Checksum
            String sha256 = calculateSha256(file);
            job.setChecksumSha256(sha256);
            log.info("Ingestion [{}] SHA-256: {}", runId, sha256);

            // Step 3: Construct S3 partition path
            String ingestionDate = LocalDate.now(ZoneOffset.UTC).toString();
            String rawObjectKey = String.format("raw/restaurants/ingestion_date=%s/run_id=%s/%s",
                    ingestionDate, runId, file.getName());
            String manifestKey = String.format("raw/restaurants/ingestion_date=%s/run_id=%s/ingestion_manifest.json",
                    ingestionDate, runId);

            // Step 4: Upload raw source file to Object Storage
            String s3Uri = storageService.uploadFile(defaultBucket, rawObjectKey, file, "text/csv");
            job.setS3RawPath(s3Uri);

            // Step 5: Generate and upload Ingestion Manifest
            IngestionManifest manifest = IngestionManifest.builder()
                    .runId(runId)
                    .sourceFileName(file.getName())
                    .checksumSha256(sha256)
                    .fileSizeBytes(file.length())
                    .totalRecords(validation.getEstimatedRowCount())
                    .detectedHeaders(validation.getHeaders())
                    .rawStoragePath(s3Uri)
                    .bucketName(defaultBucket)
                    .status(IngestionStatus.SUCCEEDED)
                    .ingestionTimestamp(Instant.now())
                    .ingestedBy(triggeredBy != null ? triggeredBy : "system")
                    .notes("Ingestion completed with 0 pre-processing errors")
                    .build();

            byte[] manifestBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(manifest);
            try (InputStream manifestStream = new ByteArrayInputStream(manifestBytes)) {
                storageService.uploadStream(defaultBucket, manifestKey, manifestStream, manifestBytes.length, "application/json");
            }

            job.setStatus(IngestionStatus.SUCCEEDED);
            job.setCompletedAt(Instant.now());
            log.info("Ingestion [{}] completed successfully in {} ms. Target: {}",
                    runId, job.getCompletedAt().toEpochMilli() - startTime.toEpochMilli(), s3Uri);

        } catch (Exception e) {
            log.error("Ingestion [{}] failed with unexpected error", runId, e);
            job.setStatus(IngestionStatus.FAILED);
            job.setErrorMessage("Ingestion error: " + e.getMessage());
            job.setCompletedAt(Instant.now());
        }

        return job;
    }

    public static String calculateSha256(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream is = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
        }
        byte[] hash = digest.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
