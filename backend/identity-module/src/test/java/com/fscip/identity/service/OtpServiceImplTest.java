package com.fscip.identity.service;

import com.fscip.identity.dto.*;
import com.fscip.identity.entity.OtpCode;
import com.fscip.identity.entity.User;
import com.fscip.identity.entity.UserStatus;
import com.fscip.identity.exception.OtpException;
import com.fscip.identity.repository.OtpCodeRepository;
import com.fscip.identity.repository.UserRepository;
import com.fscip.identity.service.impl.OtpServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OtpCodeRepository otpCodeRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private OtpCacheService otpCacheService;

    private OtpServiceImpl otpService;

    private User testUser;
    private final String testEmail = "test@example.com";
    private final UUID testUserId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        otpService = new OtpServiceImpl(userRepository, otpCodeRepository, emailService, otpCacheService);
        
        // Set configuration properties
        ReflectionTestUtils.setField(otpService, "otpExpiryMinutes", 10);
        ReflectionTestUtils.setField(otpService, "rateLimitMinutes", 1);
        ReflectionTestUtils.setField(otpService, "maxRequestsPerHour", 5);

        // Create test user
        testUser = new User(testEmail, "hashedPassword", "Test User");
        testUser.setUserId(testUserId);
        testUser.setStatus(UserStatus.PENDING);
    }

    @Test
    void testRequestOtp_Success() {
        // Arrange
        OtpRequestDto request = new OtpRequestDto(testEmail);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(otpCodeRepository.countOtpRequestsSince(eq(testUserId), any())).thenReturn(0L);
        when(emailService.sendOtp(eq(testEmail), any(), eq(10))).thenReturn(true);
        when(otpCodeRepository.save(any(OtpCode.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OtpResponseDto response = otpService.requestOtp(request);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("OTP sent successfully", response.getMessage());
        assertEquals(testEmail, response.getEmail());
        assertEquals(600L, response.getExpiresInSeconds());

        verify(otpCodeRepository).deleteByUserId(testUserId);
        verify(otpCodeRepository).save(any(OtpCode.class));
        verify(otpCacheService).storeOtp(eq(testUserId), any(), eq(600L));
        verify(emailService).sendOtp(eq(testEmail), any(), eq(10));
    }

    @Test
    void testRequestOtp_UserNotFound() {
        // Arrange
        OtpRequestDto request = new OtpRequestDto(testEmail);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        // Act
        OtpResponseDto response = otpService.requestOtp(request);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("User not found", response.getMessage());

        verify(emailService, never()).sendOtp(any(), any(), anyInt());
        verify(otpCodeRepository, never()).save(any());
    }

    @Test
    void testRequestOtp_UserAlreadyActive() {
        // Arrange
        testUser.setStatus(UserStatus.ACTIVE);
        OtpRequestDto request = new OtpRequestDto(testEmail);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        // Act
        OtpResponseDto response = otpService.requestOtp(request);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Account is already active", response.getMessage());

        verify(emailService, never()).sendOtp(any(), any(), anyInt());
    }

    @Test
    void testRequestOtp_RateLimited() {
        // Arrange
        OtpRequestDto request = new OtpRequestDto(testEmail);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(otpCodeRepository.countOtpRequestsSince(eq(testUserId), any())).thenReturn(10L);

        // Act
        OtpResponseDto response = otpService.requestOtp(request);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Too many OTP requests. Please wait before requesting again", response.getMessage());

        verify(emailService, never()).sendOtp(any(), any(), anyInt());
    }

    @Test
    void testRequestOtp_EmailSendFailure() {
        // Arrange
        OtpRequestDto request = new OtpRequestDto(testEmail);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(otpCodeRepository.countOtpRequestsSince(eq(testUserId), any())).thenReturn(0L);
        when(emailService.sendOtp(eq(testEmail), any(), eq(10))).thenReturn(false);
        when(otpCodeRepository.save(any(OtpCode.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OtpResponseDto response = otpService.requestOtp(request);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Failed to send OTP email", response.getMessage());
    }

    @Test
    void testVerifyOtp_Success() {
        // Arrange
        String validOtp = "123456";
        OtpVerificationDto verification = new OtpVerificationDto(testEmail, validOtp);
        
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(otpCacheService.getRemainingAttempts(testUserId)).thenReturn(5);
        when(otpCacheService.getOtp(testUserId)).thenReturn(validOtp);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OtpVerificationResponseDto response = otpService.verifyOtp(verification);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("OTP verified successfully", response.getMessage());
        assertEquals(testUserId, response.getUserId());
        assertEquals("ACTIVE", response.getStatus());

        verify(otpCodeRepository).deleteByUserId(testUserId);
        verify(otpCacheService).removeOtp(testUserId);
        verify(emailService).sendWelcomeEmail(testEmail, testUser.getFullName());
        verify(userRepository).save(argThat(user -> user.getStatus() == UserStatus.ACTIVE));
    }

    @Test
    void testVerifyOtp_UserNotFound() {
        // Arrange
        OtpVerificationDto verification = new OtpVerificationDto(testEmail, "123456");
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        // Act
        OtpVerificationResponseDto response = otpService.verifyOtp(verification);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("User not found", response.getMessage());
        assertEquals(0, response.getRemainingAttempts());
    }

    @Test
    void testVerifyOtp_NoAttemptsRemaining() {
        // Arrange
        OtpVerificationDto verification = new OtpVerificationDto(testEmail, "123456");
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(otpCacheService.getRemainingAttempts(testUserId)).thenReturn(0);

        // Act
        OtpVerificationResponseDto response = otpService.verifyOtp(verification);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("OTP has been locked due to too many failed attempts", response.getMessage());
        assertEquals(0, response.getRemainingAttempts());
    }

    @Test
    void testVerifyOtp_InvalidOtp() {
        // Arrange
        String validOtp = "123456";
        String invalidOtp = "654321";
        OtpVerificationDto verification = new OtpVerificationDto(testEmail, invalidOtp);
        
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(otpCacheService.getRemainingAttempts(testUserId)).thenReturn(5);
        when(otpCacheService.getOtp(testUserId)).thenReturn(validOtp);
        when(otpCacheService.decrementAttempts(testUserId)).thenReturn(4);

        OtpCode otpEntity = new OtpCode(testUserId, validOtp, LocalDateTime.now().plusMinutes(10));
        when(otpCodeRepository.findTopByUserIdOrderByCreatedAtDesc(testUserId))
            .thenReturn(Optional.of(otpEntity));

        // Act
        OtpVerificationResponseDto response = otpService.verifyOtp(verification);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Invalid OTP code", response.getMessage());
        assertEquals(4, response.getRemainingAttempts());

        verify(otpCacheService).decrementAttempts(testUserId);
        verify(userRepository, never()).save(any());
    }

    @Test
    void testVerifyOtp_NoActiveOtp() {
        // Arrange
        OtpVerificationDto verification = new OtpVerificationDto(testEmail, "123456");
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(otpCacheService.getRemainingAttempts(testUserId)).thenReturn(5);
        when(otpCacheService.getOtp(testUserId)).thenReturn(null);
        when(otpCodeRepository.findTopByUserIdOrderByCreatedAtDesc(testUserId))
            .thenReturn(Optional.empty());

        // Act
        OtpVerificationResponseDto response = otpService.verifyOtp(verification);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("No active OTP found. Please request a new one", response.getMessage());
        assertEquals(0, response.getRemainingAttempts());
    }

    @Test
    void testVerifyOtp_ExpiredOtp() {
        // Arrange
        OtpVerificationDto verification = new OtpVerificationDto(testEmail, "123456");
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(otpCacheService.getRemainingAttempts(testUserId)).thenReturn(5);
        when(otpCacheService.getOtp(testUserId)).thenReturn(null);

        OtpCode expiredOtp = new OtpCode(testUserId, "123456", LocalDateTime.now().minusMinutes(1));
        when(otpCodeRepository.findTopByUserIdOrderByCreatedAtDesc(testUserId))
            .thenReturn(Optional.of(expiredOtp));

        // Act
        OtpVerificationResponseDto response = otpService.verifyOtp(verification);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("OTP has expired. Please request a new one", response.getMessage());
        assertEquals(0, response.getRemainingAttempts());
    }

    @Test
    void testRequestOtp_ThrowsException() {
        // Arrange
        OtpRequestDto request = new OtpRequestDto(testEmail);
        when(userRepository.findByEmail(testEmail)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(OtpException.class, () -> otpService.requestOtp(request));
    }

    @Test
    void testVerifyOtp_ThrowsException() {
        // Arrange
        OtpVerificationDto verification = new OtpVerificationDto(testEmail, "123456");
        when(userRepository.findByEmail(testEmail)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(OtpException.class, () -> otpService.verifyOtp(verification));
    }
}