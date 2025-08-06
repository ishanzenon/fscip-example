package com.fscip.identity.service.impl;

import com.fscip.identity.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class MockEmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(MockEmailServiceImpl.class);

    // In-memory storage for demonstration purposes
    private final ConcurrentLinkedQueue<EmailRecord> sentEmails = new ConcurrentLinkedQueue<>();
    private final ConcurrentHashMap<String, String> lastOtpByEmail = new ConcurrentHashMap<>();

    @Override
    public boolean sendOtp(String email, String otp, int expirationMinutes) {
        try {
            // Simulate email sending delay
            Thread.sleep(100);

            // Log the OTP (in real implementation, this would be sent via email provider)
            logger.info("MOCK EMAIL SERVICE: Sending OTP to email: {} | OTP: {} | Expires in: {} minutes", 
                       email, otp, expirationMinutes);
            
            // Store in mock storage for testing purposes
            EmailRecord record = new EmailRecord(email, "OTP_VERIFICATION", 
                String.format("Your OTP code is: %s. It will expire in %d minutes.", otp, expirationMinutes));
            sentEmails.offer(record);
            lastOtpByEmail.put(email, otp);
            
            logger.info("MOCK EMAIL SERVICE: OTP successfully 'sent' to {}", email);
            return true;
            
        } catch (Exception e) {
            logger.error("MOCK EMAIL SERVICE: Failed to send OTP to {}: {}", email, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean sendWelcomeEmail(String email, String fullName) {
        try {
            // Simulate email sending delay
            Thread.sleep(100);

            logger.info("MOCK EMAIL SERVICE: Sending welcome email to: {} | Name: {}", email, fullName);
            
            // Store in mock storage
            EmailRecord record = new EmailRecord(email, "WELCOME", 
                String.format("Welcome to FSCIP, %s! Your account has been successfully activated.", fullName));
            sentEmails.offer(record);
            
            logger.info("MOCK EMAIL SERVICE: Welcome email successfully 'sent' to {}", email);
            return true;
            
        } catch (Exception e) {
            logger.error("MOCK EMAIL SERVICE: Failed to send welcome email to {}: {}", email, e.getMessage());
            return false;
        }
    }

    // Test utilities for verification in tests
    public String getLastOtpForEmail(String email) {
        return lastOtpByEmail.get(email);
    }

    public int getSentEmailCount() {
        return sentEmails.size();
    }

    public void clearHistory() {
        sentEmails.clear();
        lastOtpByEmail.clear();
    }

    // Inner class to represent email records
    public static class EmailRecord {
        private final String email;
        private final String type;
        private final String content;
        private final long timestamp;

        public EmailRecord(String email, String type, String content) {
            this.email = email;
            this.type = type;
            this.content = content;
            this.timestamp = System.currentTimeMillis();
        }

        public String getEmail() {
            return email;
        }

        public String getType() {
            return type;
        }

        public String getContent() {
            return content;
        }

        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return "EmailRecord{" +
                    "email='" + email + '\'' +
                    ", type='" + type + '\'' +
                    ", content='" + content + '\'' +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }
}