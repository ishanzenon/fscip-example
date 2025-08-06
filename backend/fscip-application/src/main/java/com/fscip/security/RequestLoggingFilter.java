package com.fscip.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Request logging filter for FSCIP application
 * Adds request correlation ID and logs incoming requests
 * Follows observability requirements from architecture design
 */
@Component
@Order(1)
public class RequestLoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final Logger accessLogger = LoggerFactory.getLogger("ACCESS");
    
    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";
    private static final String USER_ID_MDC_KEY = "userId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Generate or extract request ID
        String requestId = getOrGenerateRequestId(httpRequest);
        
        // Set correlation ID in MDC for structured logging
        MDC.put(CORRELATION_ID_MDC_KEY, requestId);
        
        // Add request ID to response headers
        httpResponse.setHeader(REQUEST_ID_HEADER, requestId);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Log incoming request
            logIncomingRequest(httpRequest, requestId);
            
            // Continue with the request
            chain.doFilter(request, response);
            
        } finally {
            // Log outgoing response
            long duration = System.currentTimeMillis() - startTime;
            logOutgoingResponse(httpRequest, httpResponse, requestId, duration);
            
            // Clean up MDC
            MDC.clear();
        }
    }

    private String getOrGenerateRequestId(HttpServletRequest request) {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.trim().isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }
        return requestId;
    }

    private void logIncomingRequest(HttpServletRequest request, String requestId) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String userAgent = request.getHeader("User-Agent");
        String clientIp = getClientIpAddress(request);
        
        StringBuilder logMessage = new StringBuilder()
            .append("Incoming request: ")
            .append(method).append(" ")
            .append(uri);
            
        if (queryString != null) {
            logMessage.append("?").append(queryString);
        }
        
        // Log at INFO level for access logging
        accessLogger.info("REQ {} {} from {} - UserAgent: {}", 
            method, uri, clientIp, userAgent);
            
        // Log at DEBUG level for detailed request info
        logger.debug("{} - IP: {}, UserAgent: {}", 
            logMessage.toString(), clientIp, userAgent);
    }

    private void logOutgoingResponse(HttpServletRequest request, HttpServletResponse response, 
                                   String requestId, long duration) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        int status = response.getStatus();
        String clientIp = getClientIpAddress(request);
        
        // Log at INFO level for access logging
        accessLogger.info("RES {} {} {} {}ms from {}", 
            method, uri, status, duration, clientIp);
            
        // Log at DEBUG level for detailed response info
        logger.debug("Response: {} {} - Status: {}, Duration: {}ms", 
            method, uri, status, duration);
            
        // Log slow requests at WARN level
        if (duration > 1000) { // Requests taking more than 1 second
            logger.warn("Slow request detected: {} {} - Duration: {}ms", 
                method, uri, duration);
        }
    }

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

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("RequestLoggingFilter initialized");
    }

    @Override
    public void destroy() {
        logger.info("RequestLoggingFilter destroyed");
    }
}