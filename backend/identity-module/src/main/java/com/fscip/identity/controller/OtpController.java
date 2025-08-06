package com.fscip.identity.controller;

import com.fscip.identity.dto.OtpRequestDto;
import com.fscip.identity.dto.OtpResponseDto;
import com.fscip.identity.dto.OtpVerificationDto;
import com.fscip.identity.dto.OtpVerificationResponseDto;
import com.fscip.identity.service.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/otp")
@Tag(name = "OTP Authentication", description = "OTP-based email authentication endpoints")
@CrossOrigin(origins = "*")
public class OtpController {

    private static final Logger logger = LoggerFactory.getLogger(OtpController.class);

    private final OtpService otpService;

    @Autowired
    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }

    @Operation(
        summary = "Request OTP",
        description = "Generate and send a 6-digit OTP to the user's email address for account verification"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OTP sent successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "429", description = "Rate limit exceeded"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/request")
    public ResponseEntity<OtpResponseDto> requestOtp(@Valid @RequestBody OtpRequestDto request) {
        logger.info("Received OTP request for email: {}", request.getEmail());
        
        try {
            OtpResponseDto response = otpService.requestOtp(request);
            
            if (response.isSuccess()) {
                logger.info("OTP request successful for email: {}", request.getEmail());
                return ResponseEntity.ok(response);
            } else {
                logger.warn("OTP request failed for email: {} | Reason: {}", 
                           request.getEmail(), response.getMessage());
                
                // Return appropriate HTTP status based on error message
                HttpStatus status = determineErrorStatus(response.getMessage());
                return ResponseEntity.status(status).body(response);
            }
        } catch (Exception e) {
            logger.error("Error processing OTP request for email: {}", request.getEmail(), e);
            OtpResponseDto errorResponse = OtpResponseDto.failure("Internal server error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(
        summary = "Verify OTP",
        description = "Verify the 6-digit OTP and activate the user account if valid"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OTP verified successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid OTP or request data"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "423", description = "OTP locked due to too many attempts"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/verify")
    public ResponseEntity<OtpVerificationResponseDto> verifyOtp(@Valid @RequestBody OtpVerificationDto verification) {
        logger.info("Received OTP verification for email: {}", verification.getEmail());
        
        try {
            OtpVerificationResponseDto response = otpService.verifyOtp(verification);
            
            if (response.isSuccess()) {
                logger.info("OTP verification successful for email: {} | User ID: {}", 
                           verification.getEmail(), response.getUserId());
                return ResponseEntity.ok(response);
            } else {
                logger.warn("OTP verification failed for email: {} | Reason: {} | Remaining attempts: {}", 
                           verification.getEmail(), response.getMessage(), response.getRemainingAttempts());
                
                // Return appropriate HTTP status based on error message
                HttpStatus status = determineVerificationErrorStatus(response.getMessage(), response.getRemainingAttempts());
                return ResponseEntity.status(status).body(response);
            }
        } catch (Exception e) {
            logger.error("Error processing OTP verification for email: {}", verification.getEmail(), e);
            OtpVerificationResponseDto errorResponse = OtpVerificationResponseDto.failure("Internal server error occurred", 0);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private HttpStatus determineErrorStatus(String errorMessage) {
        if (errorMessage.contains("Too many OTP requests")) {
            return HttpStatus.TOO_MANY_REQUESTS;
        } else if (errorMessage.contains("User not found")) {
            return HttpStatus.NOT_FOUND;
        } else if (errorMessage.contains("already active")) {
            return HttpStatus.CONFLICT;
        }
        return HttpStatus.BAD_REQUEST;
    }

    private HttpStatus determineVerificationErrorStatus(String errorMessage, int remainingAttempts) {
        if (errorMessage.contains("locked")) {
            return HttpStatus.LOCKED;
        } else if (errorMessage.contains("User not found")) {
            return HttpStatus.NOT_FOUND;
        } else if (errorMessage.contains("No active OTP") || errorMessage.contains("expired")) {
            return HttpStatus.GONE;
        }
        return HttpStatus.BAD_REQUEST;
    }
}