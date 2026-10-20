package com.dataflow.ingestion.validation;

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {
    private boolean valid;
    private List<String> errors;
    private List<String> warnings;
    private List<String> headers;
    private long estimatedRowCount;

    public ValidationResult() {
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.headers = new ArrayList<>();
        this.valid = true;
    }

    public ValidationResult(boolean valid, List<String> errors, List<String> warnings, List<String> headers, long estimatedRowCount) {
        this.valid = valid;
        this.errors = errors != null ? errors : new ArrayList<>();
        this.warnings = warnings != null ? warnings : new ArrayList<>();
        this.headers = headers != null ? headers : new ArrayList<>();
        this.estimatedRowCount = estimatedRowCount;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean valid = true;
        private List<String> errors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();
        private List<String> headers = new ArrayList<>();
        private long estimatedRowCount;

        public Builder valid(boolean valid) { this.valid = valid; return this; }
        public Builder errors(List<String> errors) { this.errors = errors; return this; }
        public Builder warnings(List<String> warnings) { this.warnings = warnings; return this; }
        public Builder headers(List<String> headers) { this.headers = headers; return this; }
        public Builder estimatedRowCount(long estimatedRowCount) { this.estimatedRowCount = estimatedRowCount; return this; }

        public ValidationResult build() {
            return new ValidationResult(valid, errors, warnings, headers, estimatedRowCount);
        }
    }

    public void addError(String error) {
        this.errors.add(error);
        this.valid = false;
    }

    public void addWarning(String warning) {
        this.warnings.add(warning);
    }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }
    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }
    public List<String> getHeaders() { return headers; }
    public void setHeaders(List<String> headers) { this.headers = headers; }
    public long getEstimatedRowCount() { return estimatedRowCount; }
    public void setEstimatedRowCount(long estimatedRowCount) { this.estimatedRowCount = estimatedRowCount; }
}
