package com.fscip.identity.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "otp_codes")
public class OtpCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "otp_id")
    private Long otpId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "otp", nullable = false, length = 6)
    private String otp;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "attempts")
    private Integer attempts = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    public OtpCode() {
    }

    public OtpCode(UUID userId, String otp, LocalDateTime expiresAt) {
        this.userId = userId;
        this.otp = otp;
        this.expiresAt = expiresAt;
    }

    public Long getOtpId() {
        return otpId;
    }

    public void setOtpId(Long otpId) {
        this.otpId = otpId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Integer getAttempts() {
        return attempts;
    }

    public void setAttempts(Integer attempts) {
        this.attempts = attempts;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void incrementAttempts() {
        this.attempts = (this.attempts == null ? 0 : this.attempts) + 1;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isLocked() {
        return attempts != null && attempts >= 5;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OtpCode otpCode = (OtpCode) o;
        return Objects.equals(otpId, otpCode.otpId) &&
                Objects.equals(userId, otpCode.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(otpId, userId);
    }

    @Override
    public String toString() {
        return "OtpCode{" +
                "otpId=" + otpId +
                ", userId=" + userId +
                ", otp='[REDACTED]'" +
                ", expiresAt=" + expiresAt +
                ", attempts=" + attempts +
                ", createdAt=" + createdAt +
                '}';
    }
}