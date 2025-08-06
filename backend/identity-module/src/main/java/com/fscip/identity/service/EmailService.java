package com.fscip.identity.service;

public interface EmailService {

    /**
     * Sends an OTP via email to the specified recipient
     * 
     * @param email recipient email address
     * @param otp the 6-digit OTP code
     * @param expirationMinutes expiration time in minutes
     * @return true if email was sent successfully, false otherwise
     */
    boolean sendOtp(String email, String otp, int expirationMinutes);

    /**
     * Sends a welcome email after successful account activation
     * 
     * @param email recipient email address
     * @param fullName recipient's full name
     * @return true if email was sent successfully, false otherwise
     */
    boolean sendWelcomeEmail(String email, String fullName);
}