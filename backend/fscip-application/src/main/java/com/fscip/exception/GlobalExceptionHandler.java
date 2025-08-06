package com.fscip.exception;

import com.fscip.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.stream.Collectors;

/**
 * Global exception handler for FSCIP application
 * Handles all exceptions and maps them to standardized error responses
 * Follows coding standards requirements
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY");

    @Value("${messages.validation.invalid-input:Invalid input provided}")
    private String invalidInputMessage;

    @Value("${messages.auth.invalid-credentials:Invalid username or password}")
    private String invalidCredentialsMessage;

    @Value("${messages.auth.token-expired:Authentication token has expired}")
    private String tokenExpiredMessage;

    /**
     * Handle BaseException and its subclasses
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex, HttpServletRequest request) {
        logger.warn("Application exception occurred: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = new ErrorResponse(
            ex.getHttpStatus().value(),
            ex.getHttpStatus().getReasonPhrase(),
            ex.getMessage(),
            request.getServletPath(),
            ex.getErrorCode(),
            getRequestId(request)
        );

        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
    }

    /**
     * Handle ResourceNotFoundException specifically
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        logger.debug("Resource not found: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            ex.getMessage(),
            request.getServletPath(),
            ex.getErrorCode(),
            getRequestId(request)
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handle ValidationException specifically
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex, HttpServletRequest request) {
        logger.warn("Validation error: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            ex.getMessage(),
            request.getServletPath(),
            ex.getErrorCode(),
            getRequestId(request)
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle method argument validation errors
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ErrorResponse> handleValidationErrors(BindException ex, HttpServletRequest request) {
        String errorMessage = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));

        if (errorMessage.isEmpty()) {
            errorMessage = invalidInputMessage;
        }

        logger.warn("Input validation failed: {}", errorMessage);

        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            errorMessage,
            request.getServletPath(),
            "VALIDATION_ERROR",
            getRequestId(request)
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle Spring Security authentication exceptions
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        securityLogger.warn("Authentication failed for request {}: {}", request.getRequestURI(), ex.getMessage());

        String message = ex instanceof BadCredentialsException ? 
            invalidCredentialsMessage : tokenExpiredMessage;

        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            HttpStatus.UNAUTHORIZED.getReasonPhrase(),
            message,
            request.getServletPath(),
            "AUTHENTICATION_ERROR",
            getRequestId(request)
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handle Spring Security access denied exceptions
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        securityLogger.warn("Access denied for request {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.FORBIDDEN.value(),
            HttpStatus.FORBIDDEN.getReasonPhrase(),
            "Access denied - insufficient privileges",
            request.getServletPath(),
            "ACCESS_DENIED",
            getRequestId(request)
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Handle IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        logger.warn("Illegal argument: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            ex.getMessage(),
            request.getServletPath(),
            "ILLEGAL_ARGUMENT",
            getRequestId(request)
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle all other unexpected exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        logger.error("Unexpected error occurred", ex);

        // Don't expose internal error details in production
        String message = isDevelopmentEnvironment() ? 
            ex.getMessage() : "An internal server error occurred";

        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            message,
            request.getServletPath(),
            "INTERNAL_SERVER_ERROR",
            getRequestId(request)
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Get request ID from current request
     */
    private String getRequestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-ID");
        if (requestId == null) {
            // Generate a simple request ID if not provided
            requestId = String.valueOf(System.currentTimeMillis());
        }
        return requestId;
    }

    /**
     * Check if running in development environment
     */
    private boolean isDevelopmentEnvironment() {
        String profiles = System.getProperty("spring.profiles.active", "");
        return profiles.contains("dev") || profiles.contains("local");
    }
}