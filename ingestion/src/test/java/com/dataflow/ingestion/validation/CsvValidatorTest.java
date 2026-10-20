package com.dataflow.ingestion.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class CsvValidatorTest {

    private CsvValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CsvValidator();
    }

    @Test
    void shouldValidateValidCsvSuccessfully(@TempDir Path tempDir) throws IOException {
        File csvFile = tempDir.resolve("valid.csv").toFile();
        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.write("restaurant_id,name,city,average_cost_for_two,aggregate_rating\n");
            writer.write("1001,Cafe Coffee,Bangalore,450.0,4.2\n");
            writer.write("1002,Bistro Garden,Delhi,850.0,3.9\n");
        }

        ValidationResult result = validator.validate(csvFile);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getEstimatedRowCount()).isEqualTo(2);
        assertThat(result.getHeaders()).contains("restaurant_id", "name", "city");
    }

    @Test
    void shouldFailWhenMandatoryColumnsAreMissing(@TempDir Path tempDir) throws IOException {
        File csvFile = tempDir.resolve("missing_cols.csv").toFile();
        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.write("restaurant_id,name,locality\n");
            writer.write("1001,Cafe Coffee,Indiranagar\n");
        }

        ValidationResult result = validator.validate(csvFile);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(err -> err.contains("Missing mandatory column: city"));
        assertThat(result.getErrors()).anyMatch(err -> err.contains("Missing mandatory column: aggregate_rating"));
    }

    @Test
    void shouldFailWhenFileIsEmpty(@TempDir Path tempDir) throws IOException {
        File emptyFile = tempDir.resolve("empty.csv").toFile();
        assertThat(emptyFile.createNewFile()).isTrue();

        ValidationResult result = validator.validate(emptyFile);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(err -> err.contains("empty"));
    }

    @Test
    void shouldParseQuotedCsvTokensProperly() {
        String line = "1001,\"The Royal, Kitchen\",Bangalore,\"1,200\",4.5";
        var tokens = CsvValidator.parseCsvLine(line);

        assertThat(tokens).hasSize(5);
        assertThat(tokens.get(0)).isEqualTo("1001");
        assertThat(tokens.get(1)).isEqualTo("The Royal, Kitchen");
        assertThat(tokens.get(2)).isEqualTo("Bangalore");
        assertThat(tokens.get(3)).isEqualTo("1,200");
        assertThat(tokens.get(4)).isEqualTo("4.5");
    }
}
