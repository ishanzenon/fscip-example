package com.fscip.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown for internal service errors
 */
public class ServiceException extends BaseException {

    private static final String ERROR_CODE = "SERVICE_ERROR";

    public ServiceException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, ERROR_CODE);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause, HttpStatus.INTERNAL_SERVER_ERROR, ERROR_CODE);
    }
}