package com.fscip.identity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fscip.identity.dto.*;
import com.fscip.identity.service.OtpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OtpController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
class OtpControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OtpService otpService;

    @Autowired
    private ObjectMapper objectMapper;

    private final String testEmail = "test@example.com";
    private final UUID testUserId = UUID.randomUUID();

    @Test
    void testRequestOtp_Success() throws Exception {
        // Arrange
        OtpRequestDto request = new OtpRequestDto(testEmail);
        OtpResponseDto response = OtpResponseDto.success(testEmail, 600L);
        
        when(otpService.requestOtp(any(OtpRequestDto.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/auth/otp/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OTP sent successfully"))
                .andExpect(jsonPath("$.email").value(testEmail))
                .andExpect(jsonPath("$.expiresInSeconds").value(600));
    }

    @Test
    void testRequestOtp_ValidationError_EmptyEmail() throws Exception {
        // Arrange
        OtpRequestDto request = new OtpRequestDto("");

        // Act & Assert
        mockMvc.perform(post("/auth/otp/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRequestOtp_ValidationError_InvalidEmail() throws Exception {
        // Arrange
        OtpRequestDto request = new OtpRequestDto("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/auth/otp/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRequestOtp_UserNotFound() throws Exception {
        // Arrange
        OtpRequestDto request = new OtpRequestDto(testEmail);
        OtpResponseDto response = OtpResponseDto.failure("User not found");
        
        when(otpService.requestOtp(any(OtpRequestDto.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/auth/otp/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void testRequestOtp_RateLimited() throws Exception {
        // Arrange
        OtpRequestDto request = new OtpRequestDto(testEmail);
        OtpResponseDto response = OtpResponseDto.failure("Too many OTP requests. Please wait before requesting again");
        
        when(otpService.requestOtp(any(OtpRequestDto.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/auth/otp/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Too many OTP requests. Please wait before requesting again"));
    }

    @Test
    void testRequestOtp_AccountAlreadyActive() throws Exception {
        // Arrange
        OtpRequestDto request = new OtpRequestDto(testEmail);
        OtpResponseDto response = OtpResponseDto.failure("Account is already active");
        
        when(otpService.requestOtp(any(OtpRequestDto.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/auth/otp/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Account is already active"));
    }

    @Test
    void testRequestOtp_InternalServerError() throws Exception {
        // Arrange
        OtpRequestDto request = new OtpRequestDto(testEmail);
        when(otpService.requestOtp(any(OtpRequestDto.class))).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(post("/auth/otp/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Internal server error occurred"));
    }

    @Test
    void testVerifyOtp_Success() throws Exception {
        // Arrange
        OtpVerificationDto request = new OtpVerificationDto(testEmail, "123456");
        OtpVerificationResponseDto response = OtpVerificationResponseDto.success(testUserId, "ACTIVE");
        
        when(otpService.verifyOtp(any(OtpVerificationDto.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/auth/otp/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OTP verified successfully"))
                .andExpect(jsonPath("$.userId").value(testUserId.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.remainingAttempts").value(0));
    }

    @Test
    void testVerifyOtp_ValidationError_EmptyEmail() throws Exception {
        // Arrange
        OtpVerificationDto request = new OtpVerificationDto("", "123456");

        // Act & Assert
        mockMvc.perform(post("/auth/otp/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testVerifyOtp_ValidationError_InvalidOtp() throws Exception {
        // Arrange
        OtpVerificationDto request = new OtpVerificationDto(testEmail, "12345"); // Only 5 digits

        // Act & Assert
        mockMvc.perform(post("/auth/otp/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testVerifyOtp_ValidationError_OtpWithLetters() throws Exception {
        // Arrange
        OtpVerificationDto request = new OtpVerificationDto(testEmail, "12A456");

        // Act & Assert
        mockMvc.perform(post("/auth/otp/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testVerifyOtp_InvalidOtpCode() throws Exception {
        // Arrange
        OtpVerificationDto request = new OtpVerificationDto(testEmail, "123456");
        OtpVerificationResponseDto response = OtpVerificationResponseDto.failure("Invalid OTP code", 4);
        
        when(otpService.verifyOtp(any(OtpVerificationDto.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/auth/otp/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid OTP code"))
                .andExpect(jsonPath("$.remainingAttempts").value(4));
    }

    @Test
    void testVerifyOtp_OtpLocked() throws Exception {
        // Arrange
        OtpVerificationDto request = new OtpVerificationDto(testEmail, "123456");
        OtpVerificationResponseDto response = OtpVerificationResponseDto.failure("OTP has been locked due to too many failed attempts", 0);
        
        when(otpService.verifyOtp(any(OtpVerificationDto.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/auth/otp/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("OTP has been locked due to too many failed attempts"))
                .andExpect(jsonPath("$.remainingAttempts").value(0));
    }

    @Test
    void testVerifyOtp_UserNotFound() throws Exception {
        // Arrange
        OtpVerificationDto request = new OtpVerificationDto(testEmail, "123456");
        OtpVerificationResponseDto response = OtpVerificationResponseDto.failure("User not found", 0);
        
        when(otpService.verifyOtp(any(OtpVerificationDto.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/auth/otp/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User not found"))
                .andExpect(jsonPath("$.remainingAttempts").value(0));
    }

    @Test
    void testVerifyOtp_ExpiredOtp() throws Exception {
        // Arrange
        OtpVerificationDto request = new OtpVerificationDto(testEmail, "123456");
        OtpVerificationResponseDto response = OtpVerificationResponseDto.failure("OTP has expired. Please request a new one", 0);
        
        when(otpService.verifyOtp(any(OtpVerificationDto.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/auth/otp/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("OTP has expired. Please request a new one"))
                .andExpect(jsonPath("$.remainingAttempts").value(0));
    }

    @Test
    void testVerifyOtp_InternalServerError() throws Exception {
        // Arrange
        OtpVerificationDto request = new OtpVerificationDto(testEmail, "123456");
        when(otpService.verifyOtp(any(OtpVerificationDto.class))).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(post("/auth/otp/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Internal server error occurred"));
    }
}