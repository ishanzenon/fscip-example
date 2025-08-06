package com.fscip.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fscip.dto.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Custom Authentication Entry Point for FSCIP application
 * Handles authentication failures with structured error responses
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationEntryPoint.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, 
                        AuthenticationException authException) throws IOException, ServletException {
        
        logger.warn("Authentication failed for request {}: {}", 
            request.getRequestURI(), authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String message;
        String errorCode;

        if (authException instanceof InvalidBearerTokenException) {
            InvalidBearerTokenException tokenException = (InvalidBearerTokenException) authException;
            message = "Invalid or expired authentication token";
            errorCode = "INVALID_TOKEN";
            
            // Log token validation issues
            logger.warn("Invalid bearer token for request {}: {}", 
                request.getRequestURI(), tokenException.getMessage());
        } else {
            message = "Authentication required to access this resource";
            errorCode = "AUTHENTICATION_REQUIRED";
        }

        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            HttpStatus.UNAUTHORIZED.getReasonPhrase(),
            message,
            request.getServletPath(),
            errorCode,
            request.getHeader("X-Request-ID")
        );

        // Add security headers
        response.setHeader("WWW-Authenticate", "Bearer realm=\"FSCIP\"");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-XSS-Protection", "1; mode=block");

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}