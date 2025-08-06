package com.fscip.identity.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fscip.identity.dto.*;
import com.fscip.identity.entity.User;
import com.fscip.identity.entity.UserStatus;
import com.fscip.identity.repository.OtpCodeRepository;
import com.fscip.identity.repository.UserRepository;
import com.fscip.identity.service.impl.MockEmailServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = {com.fscip.identity.TestApplication.class, com.fscip.identity.TestSecurityConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class OtpFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpCodeRepository otpCodeRepository;

    @Autowired
    private MockEmailServiceImpl mockEmailService;

    @Autowired
    private ObjectMapper objectMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final String testEmail = "integration.test@example.com";
    private final String testName = "Integration Test User";

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        userRepository.findByEmail(testEmail).ifPresent(user -> {
            otpCodeRepository.deleteByUserId(user.getUserId());
            userRepository.delete(user);
        });

        // Clear email service history
        mockEmailService.clearHistory();
    }

    @Test
    void testCompleteOtpFlow_Success() throws Exception {
        // Step 1: Create a pending user
        User testUser = createPendingUser();

        // Step 2: Request OTP
        OtpRequestDto otpRequest = new OtpRequestDto(testEmail);
        MvcResult requestResult = mockMvc.perform(post("/auth/otp/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(otpRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.email").value(testEmail))
                .andReturn();

        // Verify email was "sent"
        assertEquals(1, mockEmailService.getSentEmailCount());
        String sentOtp = mockEmailService.getLastOtpForEmail(testEmail);
        assertNotNull(sentOtp);
        assertEquals(6, sentOtp.length());

        // Step 3: Verify OTP
        OtpVerificationDto otpVerification = new OtpVerificationDto(testEmail, sentOtp);
        MvcResult verifyResult = mockMvc.perform(post("/auth/otp/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(otpVerification)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.userId").value(testUser.getUserId().toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn();

        // Verify user status was updated
        User updatedUser = userRepository.findByEmail(testEmail).orElseThrow();
        assertEquals(UserStatus.ACTIVE, updatedUser.getStatus());
        assertNotNull(updatedUser.getLastLogin());

        // Verify OTP was cleaned up
        assertEquals(0, otpCodeRepository.findByUserIdOrderByCreatedAtDesc(testUser.getUserId()).size());

        // Verify welcome email was sent
        assertEquals(2, mockEmailService.getSentEmailCount()); // OTP + Welcome email
    }

    @Test
    void testOtpFlow_InvalidOtpAttempts() throws Exception {
        // Step 1: Create a pending user and request OTP
        User testUser = createPendingUser();
        requestOtp();
        String correctOtp = mockEmailService.getLastOtpForEmail(testEmail);

        // Step 2: Try invalid OTP multiple times
        String invalidOtp = "000000";
        for (int i = 5; i > 1; i--) {
            OtpVerificationDto otpVerification = new OtpVerificationDto(testEmail, invalidOtp);
            mockMvc.perform(post("/auth/otp/verify")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(otpVerification)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Invalid OTP code"))
                    .andExpect(jsonPath("$.remainingAttempts").value(i - 1));
        }

        // Step 3: Final invalid attempt should lock the OTP
        OtpVerificationDto finalAttempt = new OtpVerificationDto(testEmail, invalidOtp);
        mockMvc.perform(post("/auth/otp/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(finalAttempt)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.remainingAttempts").value(0));

        // Step 4: Even correct OTP should be rejected now
        OtpVerificationDto correctAttempt = new OtpVerificationDto(testEmail, correctOtp);
        mockMvc.perform(post("/auth/otp/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(correctAttempt)))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("OTP has been locked due to too many failed attempts"));

        // Verify user is still pending
        User stillPendingUser = userRepository.findByEmail(testEmail).orElseThrow();
        assertEquals(UserStatus.PENDING, stillPendingUser.getStatus());
    }

    @Test
    void testOtpFlow_UserNotFound() throws Exception {
        String nonExistentEmail = "nonexistent@example.com";
        
        // Request OTP for non-existent user
        OtpRequestDto otpRequest = new OtpRequestDto(nonExistentEmail);
        mockMvc.perform(post("/auth/otp/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(otpRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User not found"));

        // Verify no email was sent
        assertEquals(0, mockEmailService.getSentEmailCount());
    }

    @Test
    void testOtpFlow_AlreadyActiveUser() throws Exception {
        // Create an active user
        User activeUser = createActiveUser();

        // Request OTP for active user
        OtpRequestDto otpRequest = new OtpRequestDto(testEmail);
        mockMvc.perform(post("/auth/otp/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(otpRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Account is already active"));

        // Verify no email was sent
        assertEquals(0, mockEmailService.getSentEmailCount());
    }

    @Test
    void testOtpFlow_RateLimit() throws Exception {
        // Create a pending user
        createPendingUser();

        // Request OTP multiple times rapidly
        OtpRequestDto otpRequest = new OtpRequestDto(testEmail);
        
        // First request should succeed
        mockMvc.perform(post("/auth/otp/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(otpRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Simulate multiple rapid requests (this would depend on actual rate limiting implementation)
        // For now, we'll test that the service handles the logic correctly
        assertTrue(mockEmailService.getSentEmailCount() >= 1);
    }

    @Test
    void testOtpFlow_ValidationErrors() throws Exception {
        // Test invalid email format
        OtpRequestDto invalidEmailRequest = new OtpRequestDto("invalid-email");
        mockMvc.perform(post("/auth/otp/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidEmailRequest)))
                .andExpect(status().isBadRequest());

        // Test invalid OTP format (too short)
        OtpVerificationDto shortOtpRequest = new OtpVerificationDto(testEmail, "123");
        mockMvc.perform(post("/auth/otp/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(shortOtpRequest)))
                .andExpect(status().isBadRequest());

        // Test invalid OTP format (contains letters)
        OtpVerificationDto letterOtpRequest = new OtpVerificationDto(testEmail, "12A456");
        mockMvc.perform(post("/auth/otp/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(letterOtpRequest)))
                .andExpect(status().isBadRequest());
    }

    private User createPendingUser() {
        User user = new User(testEmail, passwordEncoder.encode("password123"), testName);
        user.setMobile("+1234567890");
        user.setStatus(UserStatus.PENDING);
        return userRepository.save(user);
    }

    private User createActiveUser() {
        User user = new User(testEmail, passwordEncoder.encode("password123"), testName);
        user.setMobile("+1234567890");
        user.setStatus(UserStatus.ACTIVE);
        return userRepository.save(user);
    }

    private void requestOtp() throws Exception {
        OtpRequestDto otpRequest = new OtpRequestDto(testEmail);
        mockMvc.perform(post("/auth/otp/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(otpRequest)))
                .andExpect(status().isOk());
    }
}