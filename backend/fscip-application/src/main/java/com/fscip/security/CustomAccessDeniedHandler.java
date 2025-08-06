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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * Custom Access Denied Handler for FSCIP application
 * Handles authorization failures with structured error responses and security logging
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);
    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY");
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, 
                      AccessDeniedException accessDeniedException) throws IOException, ServletException {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "anonymous";
        String userAgent = request.getHeader("User-Agent");
        String clientIp = getClientIpAddress(request);

        // Security audit logging
        securityLogger.warn("Access denied for user '{}' attempting to access '{}' from IP '{}' with User-Agent '{}'", 
            username, request.getRequestURI(), clientIp, userAgent);

        logger.warn("Access denied for request {}: {}", 
            request.getRequestURI(), accessDeniedException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.FORBIDDEN.value(),
            HttpStatus.FORBIDDEN.getReasonPhrase(),
            "Access denied - insufficient privileges",
            request.getServletPath(),
            "ACCESS_DENIED",
            request.getHeader("X-Request-ID")
        );

        // Add security headers
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-XSS-Protection", "1; mode=block");

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }

    /**
     * Extract client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * Check if running in development environment
     */
    private boolean isDevelopmentEnvironment() {
        String profiles = System.getProperty("spring.profiles.active", "");
        return profiles.contains("dev") || profiles.contains("local");
    }
}