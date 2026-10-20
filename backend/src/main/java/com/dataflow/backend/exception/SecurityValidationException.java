package com.dataflow.backend.exception;

public class SecurityValidationException extends RuntimeException {
    public SecurityValidationException(String message) {
        super(message);
    }
}
