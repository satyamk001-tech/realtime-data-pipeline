package com.dataflow.ingestion;

import com.dataflow.ingestion.model.IngestionJob;
import com.dataflow.ingestion.service.IngestionService;
import com.dataflow.ingestion.storage.LocalStorageService;
import com.dataflow.ingestion.storage.MinioStorageService;
import com.dataflow.ingestion.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class IngestionApplication {
    private static final Logger log = LoggerFactory.getLogger(IngestionApplication.class);

    public static void main(String[] args) {
        log.info("=================================================================");
        log.info("           DataFlow Java Ingestion Service Initializing          ");
        log.info("=================================================================");

        String filePath = "data/samples/zomato_restaurants.csv";
        String bucket = System.getenv().getOrDefault("MINIO_BUCKET_NAME", "dataflow-lake");
        String endpoint = System.getenv().getOrDefault("MINIO_ENDPOINT", "http://localhost:9000");
        String accessKey = System.getenv().getOrDefault("MINIO_ACCESS_KEY", "minioadmin");
        String secretKey = System.getenv().getOrDefault("MINIO_SECRET_KEY", "minioadmin123");
        boolean useLocalLake = Boolean.parseBoolean(System.getProperty("useLocalLake", "false"));

        for (int i = 0; i < args.length; i++) {
            if ("--file".equals(args[i]) && i + 1 < args.length) {
                filePath = args[++i];
            } else if ("--bucket".equals(args[i]) && i + 1 < args.length) {
                bucket = args[++i];
            } else if ("--local".equals(args[i])) {
                useLocalLake = true;
            }
        }

        File file = new File(filePath);
        if (!file.exists()) {
            log.error("Input file not found at: {}", file.getAbsolutePath());
            System.exit(1);
        }

        StorageService storageService;
        if (useLocalLake) {
            log.info("Using local filesystem storage under target/lake directory");
            storageService = new LocalStorageService("target/lake");
        } else {
            try {
                log.info("Connecting to MinIO S3 endpoint: {}", endpoint);
                storageService = new MinioStorageService(endpoint, accessKey, secretKey);
                storageService.ensureBucketExists(bucket);
            } catch (Exception e) {
                log.warn("Could not connect to MinIO at {}. Falling back to LocalStorageService...", endpoint);
                storageService = new LocalStorageService("target/lake");
            }
        }

        IngestionService ingestionService = new IngestionService(storageService, bucket);
        IngestionJob job = ingestionService.ingestFile(file, "CLI_USER");

        log.info("=================================================================");
        log.info("                      INGESTION RESULT SUMMARY                   ");
        log.info("=================================================================");
        log.info("Job ID:       {}", job.getJobId());
        log.info("Run ID:       {}", job.getRunId());
        log.info("File:         {} ({} bytes)", job.getFileName(), job.getFileSizeBytes());
        log.info("Records:      {}", job.getRecordCount());
        log.info("SHA-256:      {}", job.getChecksumSha256());
        log.info("Status:       {}", job.getStatus());
        log.info("Target URI:   {}", job.getS3RawPath());
        if (job.getErrorMessage() != null) {
            log.error("Error:        {}", job.getErrorMessage());
        }
        log.info("=================================================================");

        if (job.getStatus() != com.dataflow.ingestion.model.IngestionStatus.SUCCEEDED) {
            System.exit(2);
        }
    }
}
