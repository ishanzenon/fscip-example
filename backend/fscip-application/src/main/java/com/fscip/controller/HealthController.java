package com.fscip.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller for FSCIP application
 * Provides basic health and status information
 */
@RestController
@RequestMapping("/health")
@Tag(name = "Health", description = "Application health and status endpoints")
public class HealthController {

    @Operation(
        summary = "Health check endpoint",
        description = "Returns basic health status of the application"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Application is healthy"),
        @ApiResponse(responseCode = "503", description = "Application is unhealthy")
    })
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", Instant.now());
        response.put("service", "fscip-backend");
        response.put("version", "1.0.0");
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Simple ping endpoint",
        description = "Returns a simple pong response for connectivity testing"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Service is reachable")
    })
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "pong");
        response.put("timestamp", Instant.now());
        
        return ResponseEntity.ok(response);
    }
}