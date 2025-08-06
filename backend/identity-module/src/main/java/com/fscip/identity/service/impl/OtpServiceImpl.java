package com.fscip.identity.service.impl;

import com.fscip.identity.dto.*;
import com.fscip.identity.entity.OtpCode;
import com.fscip.identity.entity.User;
import com.fscip.identity.entity.UserStatus;
import com.fscip.identity.exception.OtpException;
import com.fscip.identity.repository.OtpCodeRepository;
import com.fscip.identity.repository.UserRepository;
import com.fscip.identity.service.EmailService;
import com.fscip.identity.service.OtpCacheService;
import com.fscip.identity.service.OtpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@Transactional
public class OtpServiceImpl implements OtpService {

    private static final Logger logger = LoggerFactory.getLogger(OtpServiceImpl.class);
    private static final int OTP_LENGTH = 6;
    private static final int MAX_ATTEMPTS = 5;

    @Value("${app.otp.expiry-minutes:10}")
    private int otpExpiryMinutes;

    @Value("${app.otp.rate-limit-minutes:1}")
    private int rateLimitMinutes;

    @Value("${app.otp.max-requests-per-hour:5}")
    private int maxRequestsPerHour;

    private final UserRepository userRepository;
    private final OtpCodeRepository otpCodeRepository;
    private final EmailService emailService;
    private final OtpCacheService otpCacheService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    public OtpServiceImpl(UserRepository userRepository,
                         OtpCodeRepository otpCodeRepository,
                         EmailService emailService,
                         OtpCacheService otpCacheService) {
        this.userRepository = userRepository;
        this.otpCodeRepository = otpCodeRepository;
        this.emailService = emailService;
        this.otpCacheService = otpCacheService;
    }

    @Override
    public OtpResponseDto requestOtp(OtpRequestDto request) {
        try {
            logger.info("Processing OTP request for email: {}", request.getEmail());

            // Find user by email
            Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
            if (userOpt.isEmpty()) {
                logger.warn("OTP request for non-existent email: {}", request.getEmail());
                return OtpResponseDto.failure("User not found");
            }

            User user = userOpt.get();
            
            // Check if user is already active
            if (user.getStatus() == UserStatus.ACTIVE) {
                logger.warn("OTP request for already active user: {}", request.getEmail());
                return OtpResponseDto.failure("Account is already active");
            }

            // Check rate limiting
            if (isRateLimited(user.getUserId())) {
                logger.warn("Rate limit exceeded for user: {}", request.getEmail());
                return OtpResponseDto.failure("Too many OTP requests. Please wait before requesting again");
            }

            // Generate new OTP
            String otpCode = generateOtp();
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(otpExpiryMinutes);

            // Clean up existing OTPs for this user
            otpCodeRepository.deleteByUserId(user.getUserId());

            // Save OTP to database
            OtpCode otpEntity = new OtpCode(user.getUserId(), otpCode, expiresAt);
            otpCodeRepository.save(otpEntity);

            // Store in cache for quick access
            otpCacheService.storeOtp(user.getUserId(), otpCode, otpExpiryMinutes * 60L);

            // Send OTP via email
            boolean emailSent = emailService.sendOtp(request.getEmail(), otpCode, otpExpiryMinutes);
            if (!emailSent) {
                logger.error("Failed to send OTP email to: {}", request.getEmail());
                return OtpResponseDto.failure("Failed to send OTP email");
            }

            logger.info("OTP successfully generated and sent for email: {}", request.getEmail());
            return OtpResponseDto.success(request.getEmail(), (long) otpExpiryMinutes * 60);

        } catch (Exception e) {
            logger.error("Error processing OTP request for email: {}", request.getEmail(), e);
            throw new OtpException("Failed to process OTP request", e);
        }
    }

    @Override
    public OtpVerificationResponseDto verifyOtp(OtpVerificationDto verification) {
        try {
            logger.info("Processing OTP verification for email: {}", verification.getEmail());

            // Find user by email
            Optional<User> userOpt = userRepository.findByEmail(verification.getEmail());
            if (userOpt.isEmpty()) {
                logger.warn("OTP verification for non-existent email: {}", verification.getEmail());
                return OtpVerificationResponseDto.failure("User not found", 0);
            }

            User user = userOpt.get();

            // Check remaining attempts from cache first
            int remainingAttempts = otpCacheService.getRemainingAttempts(user.getUserId());
            if (remainingAttempts <= 0) {
                logger.warn("OTP verification blocked - no attempts remaining for user: {}", verification.getEmail());
                return OtpVerificationResponseDto.failure("OTP has been locked due to too many failed attempts", 0);
            }

            // Get OTP from cache
            String cachedOtp = otpCacheService.getOtp(user.getUserId());
            if (cachedOtp == null) {
                // Fallback to database
                Optional<OtpCode> otpOpt = otpCodeRepository.findTopByUserIdOrderByCreatedAtDesc(user.getUserId());
                if (otpOpt.isEmpty()) {
                    logger.warn("No OTP found for user: {}", verification.getEmail());
                    return OtpVerificationResponseDto.failure("No active OTP found. Please request a new one", 0);
                }

                OtpCode otpEntity = otpOpt.get();
                if (otpEntity.isExpired()) {
                    logger.warn("Expired OTP verification attempt for user: {}", verification.getEmail());
                    return OtpVerificationResponseDto.failure("OTP has expired. Please request a new one", 0);
                }

                if (otpEntity.isLocked()) {
                    logger.warn("Locked OTP verification attempt for user: {}", verification.getEmail());
                    return OtpVerificationResponseDto.failure("OTP has been locked due to too many failed attempts", 0);
                }

                cachedOtp = otpEntity.getOtp();
            }

            // Verify OTP
            if (!cachedOtp.equals(verification.getOtp())) {
                // Decrement attempts
                remainingAttempts = otpCacheService.decrementAttempts(user.getUserId());
                
                // Also update database
                otpCodeRepository.findTopByUserIdOrderByCreatedAtDesc(user.getUserId())
                    .ifPresent(otp -> {
                        otp.incrementAttempts();
                        otpCodeRepository.save(otp);
                    });

                logger.warn("Invalid OTP verification attempt for user: {} | Remaining attempts: {}", 
                           verification.getEmail(), remainingAttempts);
                return OtpVerificationResponseDto.failure("Invalid OTP code", remainingAttempts);
            }

            // OTP is valid - activate user
            user.setStatus(UserStatus.ACTIVE);
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            // Clean up OTPs
            otpCodeRepository.deleteByUserId(user.getUserId());
            otpCacheService.removeOtp(user.getUserId());

            // Send welcome email
            emailService.sendWelcomeEmail(user.getEmail(), user.getFullName());

            logger.info("OTP successfully verified and user activated: {}", verification.getEmail());
            return OtpVerificationResponseDto.success(user.getUserId(), user.getStatus().toString());

        } catch (Exception e) {
            logger.error("Error processing OTP verification for email: {}", verification.getEmail(), e);
            throw new OtpException("Failed to process OTP verification", e);
        }
    }

    private String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(secureRandom.nextInt(10));
        }
        return otp.toString();
    }

    private boolean isRateLimited(java.util.UUID userId) {
        LocalDateTime since = LocalDateTime.now().minus(rateLimitMinutes, ChronoUnit.MINUTES);
        long recentRequests = otpCodeRepository.countOtpRequestsSince(userId, since);
        return recentRequests >= maxRequestsPerHour;
    }
}