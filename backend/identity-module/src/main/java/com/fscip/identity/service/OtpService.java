package com.fscip.identity.service;

import com.fscip.identity.dto.OtpRequestDto;
import com.fscip.identity.dto.OtpResponseDto;
import com.fscip.identity.dto.OtpVerificationDto;
import com.fscip.identity.dto.OtpVerificationResponseDto;

public interface OtpService {

    /**
     * Generate and send OTP to user's email
     * 
     * @param request OTP request containing email
     * @return OTP response with success status and message
     */
    OtpResponseDto requestOtp(OtpRequestDto request);

    /**
     * Verify OTP and activate user account if valid
     * 
     * @param verification OTP verification request containing email and OTP
     * @return verification response with success status and user details
     */
    OtpVerificationResponseDto verifyOtp(OtpVerificationDto verification);
}