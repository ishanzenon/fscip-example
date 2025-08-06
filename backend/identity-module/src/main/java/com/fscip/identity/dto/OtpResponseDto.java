package com.fscip.identity.dto;

public class OtpResponseDto {

    private boolean success;
    private String message;
    private String email;
    private Long expiresInSeconds;

    public OtpResponseDto() {
    }

    public OtpResponseDto(boolean success, String message, String email, Long expiresInSeconds) {
        this.success = success;
        this.message = message;
        this.email = email;
        this.expiresInSeconds = expiresInSeconds;
    }

    public static OtpResponseDto success(String email, Long expiresInSeconds) {
        return new OtpResponseDto(true, "OTP sent successfully", email, expiresInSeconds);
    }

    public static OtpResponseDto failure(String message) {
        return new OtpResponseDto(false, message, null, null);
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public void setExpiresInSeconds(Long expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
    }

    @Override
    public String toString() {
        return "OtpResponseDto{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", email='" + email + '\'' +
                ", expiresInSeconds=" + expiresInSeconds +
                '}';
    }
}