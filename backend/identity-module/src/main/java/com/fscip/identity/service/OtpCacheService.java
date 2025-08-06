package com.fscip.identity.service;

import java.util.UUID;

public interface OtpCacheService {

    /**
     * Store OTP in memory cache with expiration
     * 
     * @param userId user ID
     * @param otp the OTP code
     * @param expirationSeconds expiration time in seconds
     */
    void storeOtp(UUID userId, String otp, long expirationSeconds);

    /**
     * Retrieve OTP from memory cache
     * 
     * @param userId user ID
     * @return OTP code if exists and not expired, null otherwise
     */
    String getOtp(UUID userId);

    /**
     * Remove OTP from memory cache
     * 
     * @param userId user ID
     */
    void removeOtp(UUID userId);

    /**
     * Check if user has an active OTP in cache
     * 
     * @param userId user ID
     * @return true if active OTP exists, false otherwise
     */
    boolean hasActiveOtp(UUID userId);

    /**
     * Get remaining attempts for OTP verification
     * 
     * @param userId user ID
     * @return remaining attempts count
     */
    int getRemainingAttempts(UUID userId);

    /**
     * Decrement attempts for OTP verification
     * 
     * @param userId user ID
     * @return remaining attempts after decrement
     */
    int decrementAttempts(UUID userId);

    /**
     * Reset attempts for OTP verification
     * 
     * @param userId user ID
     */
    void resetAttempts(UUID userId);
}