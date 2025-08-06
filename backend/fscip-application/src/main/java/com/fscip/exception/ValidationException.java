package com.fscip.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when input validation fails
 */
public class ValidationException extends BaseException {

    private static final String ERROR_CODE = "VALIDATION_ERROR";

    public ValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST, ERROR_CODE);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause, HttpStatus.BAD_REQUEST, ERROR_CODE);
    }
}