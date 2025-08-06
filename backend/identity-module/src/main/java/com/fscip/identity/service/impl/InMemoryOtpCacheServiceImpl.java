package com.fscip.identity.service.impl;

import com.fscip.identity.service.OtpCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class InMemoryOtpCacheServiceImpl implements OtpCacheService {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryOtpCacheServiceImpl.class);
    private static final int MAX_ATTEMPTS = 5;

    private final ConcurrentHashMap<UUID, OtpCacheEntry> otpCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newScheduledThreadPool(1);

    public InMemoryOtpCacheServiceImpl() {
        // Schedule cleanup task to run every minute
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredEntries, 1, 1, TimeUnit.MINUTES);
    }

    @Override
    public void storeOtp(UUID userId, String otp, long expirationSeconds) {
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expirationSeconds);
        OtpCacheEntry entry = new OtpCacheEntry(otp, expiresAt, MAX_ATTEMPTS);
        otpCache.put(userId, entry);
        
        logger.debug("OTP stored in cache for user: {} | Expires at: {}", userId, expiresAt);
    }

    @Override
    public String getOtp(UUID userId) {
        OtpCacheEntry entry = otpCache.get(userId);
        if (entry == null) {
            logger.debug("No OTP found in cache for user: {}", userId);
            return null;
        }

        if (entry.isExpired()) {
            otpCache.remove(userId);
            logger.debug("OTP expired and removed from cache for user: {}", userId);
            return null;
        }

        return entry.getOtp();
    }

    @Override
    public void removeOtp(UUID userId) {
        OtpCacheEntry removed = otpCache.remove(userId);
        if (removed != null) {
            logger.debug("OTP removed from cache for user: {}", userId);
        }
    }

    @Override
    public boolean hasActiveOtp(UUID userId) {
        OtpCacheEntry entry = otpCache.get(userId);
        if (entry == null) {
            return false;
        }

        if (entry.isExpired()) {
            otpCache.remove(userId);
            return false;
        }

        return true;
    }

    @Override
    public int getRemainingAttempts(UUID userId) {
        OtpCacheEntry entry = otpCache.get(userId);
        if (entry == null || entry.isExpired()) {
            return 0;
        }
        return entry.getRemainingAttempts();
    }

    @Override
    public int decrementAttempts(UUID userId) {
        OtpCacheEntry entry = otpCache.get(userId);
        if (entry == null || entry.isExpired()) {
            return 0;
        }

        int remaining = entry.decrementAttempts();
        logger.debug("Decremented OTP attempts for user: {} | Remaining: {}", userId, remaining);
        
        if (remaining <= 0) {
            otpCache.remove(userId);
            logger.debug("OTP locked and removed from cache for user: {}", userId);
        }
        
        return remaining;
    }

    @Override
    public void resetAttempts(UUID userId) {
        OtpCacheEntry entry = otpCache.get(userId);
        if (entry != null && !entry.isExpired()) {
            entry.resetAttempts(MAX_ATTEMPTS);
            logger.debug("OTP attempts reset for user: {}", userId);
        }
    }

    private void cleanupExpiredEntries() {
        int removedCount = 0;
        for (UUID userId : otpCache.keySet()) {
            OtpCacheEntry entry = otpCache.get(userId);
            if (entry != null && entry.isExpired()) {
                otpCache.remove(userId);
                removedCount++;
            }
        }
        
        if (removedCount > 0) {
            logger.debug("Cleaned up {} expired OTP entries from cache", removedCount);
        }
    }

    // Inner class to represent cache entry
    private static class OtpCacheEntry {
        private final String otp;
        private final LocalDateTime expiresAt;
        private int remainingAttempts;

        public OtpCacheEntry(String otp, LocalDateTime expiresAt, int maxAttempts) {
            this.otp = otp;
            this.expiresAt = expiresAt;
            this.remainingAttempts = maxAttempts;
        }

        public String getOtp() {
            return otp;
        }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiresAt);
        }

        public int getRemainingAttempts() {
            return remainingAttempts;
        }

        public int decrementAttempts() {
            if (remainingAttempts > 0) {
                remainingAttempts--;
            }
            return remainingAttempts;
        }

        public void resetAttempts(int maxAttempts) {
            this.remainingAttempts = maxAttempts;
        }

        @Override
        public String toString() {
            return "OtpCacheEntry{" +
                    "otp='[REDACTED]'" +
                    ", expiresAt=" + expiresAt +
                    ", remainingAttempts=" + remainingAttempts +
                    '}';
        }
    }
}