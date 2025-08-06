package com.fscip.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown for authentication-related errors
 */
public class AuthException extends BaseException {

    private static final String ERROR_CODE = "AUTHENTICATION_ERROR";

    public AuthException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, ERROR_CODE);
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause, HttpStatus.UNAUTHORIZED, ERROR_CODE);
    }
}