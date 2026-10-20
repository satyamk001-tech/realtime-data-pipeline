package com.dataflow.ingestion.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CsvValidator {
    private static final Logger log = LoggerFactory.getLogger(CsvValidator.class);

    public static final Set<String> MANDATORY_COLUMNS = Set.of(
            "restaurant_id",
            "name",
            "city",
            "average_cost_for_two",
            "aggregate_rating"
    );

    public ValidationResult validate(File file) {
        ValidationResult result = ValidationResult.builder().valid(true).build();

        if (file == null || !file.exists()) {
            result.addError("File does not exist or is null");
            return result;
        }

        if (!file.canRead()) {
            result.addError("File cannot be read (insufficient file permissions)");
            return result;
        }

        if (file.length() == 0) {
            result.addError("Source file is empty (0 bytes)");
            return result;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.trim().isEmpty()) {
                result.addError("CSV file contains no header row");
                return result;
            }

            List<String> headers = parseCsvLine(headerLine).stream()
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());

            result.setHeaders(headers);

            for (String mandatory : MANDATORY_COLUMNS) {
                if (!headers.contains(mandatory)) {
                    result.addError("Missing mandatory column: " + mandatory);
                }
            }

            long rowCount = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    rowCount++;
                }
            }

            result.setEstimatedRowCount(rowCount);

            if (rowCount == 0) {
                result.addError("CSV contains headers but zero data records");
            } else {
                log.info("CSV validation passed for '{}' with {} headers and {} records",
                        file.getName(), headers.size(), rowCount);
            }

        } catch (IOException e) {
            log.error("Failed to read CSV file: {}", file.getAbsolutePath(), e);
            result.addError("IO error while reading CSV file: " + e.getMessage());
        }

        return result;
    }

    public static List<String> parseCsvLine(String line) {
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
