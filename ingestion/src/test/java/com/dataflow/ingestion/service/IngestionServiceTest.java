package com.dataflow.ingestion.service;

import com.dataflow.ingestion.model.IngestionJob;
import com.dataflow.ingestion.model.IngestionStatus;
import com.dataflow.ingestion.storage.LocalStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class IngestionServiceTest {

    private IngestionService ingestionService;
    private LocalStorageService storageService;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        storageService = new LocalStorageService(tempDir.resolve("lake").toString());
        ingestionService = new IngestionService(storageService, "test-lake");
    }

    @Test
    void shouldIngestValidFileAndCreateManifest(@TempDir Path tempDir) throws IOException {
        File csvFile = tempDir.resolve("valid_restaurants.csv").toFile();
        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.write("restaurant_id,name,city,average_cost_for_two,aggregate_rating\n");
            writer.write("2001,Spice House,Mumbai,600.0,4.1\n");
            writer.write("2002,Coastal Treats,Goa,1100.0,4.6\n");
        }

        IngestionJob job = ingestionService.ingestFile(csvFile, "TEST_USER");

        assertThat(job.getStatus()).isEqualTo(IngestionStatus.SUCCEEDED);
        assertThat(job.getRecordCount()).isEqualTo(2);
        assertThat(job.getChecksumSha256()).isNotEmpty();
        assertThat(job.getRunId()).startsWith("run-");
        assertThat(job.getS3RawPath()).isNotEmpty();
    }

    @Test
    void shouldMarkJobFailedWhenValidationFails(@TempDir Path tempDir) throws IOException {
        File invalidFile = tempDir.resolve("bad_restaurants.csv").toFile();
        try (FileWriter writer = new FileWriter(invalidFile)) {
            writer.write("restaurant_id,notes\n");
            writer.write("2001,Incomplete row\n");
        }

        IngestionJob job = ingestionService.ingestFile(invalidFile, "TEST_USER");

        assertThat(job.getStatus()).isEqualTo(IngestionStatus.FAILED);
        assertThat(job.getErrorMessage()).contains("Validation failed");
    }
}
