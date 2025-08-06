package com.fscip.identity.dto;

import java.util.UUID;

public class OtpVerificationResponseDto {

    private boolean success;
    private String message;
    private UUID userId;
    private String status;
    private int remainingAttempts;

    public OtpVerificationResponseDto() {
    }

    public OtpVerificationResponseDto(boolean success, String message, UUID userId, String status, int remainingAttempts) {
        this.success = success;
        this.message = message;
        this.userId = userId;
        this.status = status;
        this.remainingAttempts = remainingAttempts;
    }

    public static OtpVerificationResponseDto success(UUID userId, String status) {
        return new OtpVerificationResponseDto(true, "OTP verified successfully", userId, status, 0);
    }

    public static OtpVerificationResponseDto failure(String message, int remainingAttempts) {
        return new OtpVerificationResponseDto(false, message, null, null, remainingAttempts);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getRemainingAttempts() {
        return remainingAttempts;
    }

    public void setRemainingAttempts(int remainingAttempts) {
        this.remainingAttempts = remainingAttempts;
    }

    @Override
    public String toString() {
        return "OtpVerificationResponseDto{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", userId=" + userId +
                ", status='" + status + '\'' +
                ", remainingAttempts=" + remainingAttempts +
                '}';
    }
}