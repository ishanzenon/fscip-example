package com.fscip.identity.repository;

import com.fscip.identity.entity.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {

    Optional<OtpCode> findByUserIdAndOtp(UUID userId, String otp);

    List<OtpCode> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<OtpCode> findTopByUserIdOrderByCreatedAtDesc(UUID userId);

    @Modifying
    @Query("DELETE FROM OtpCode o WHERE o.userId = :userId")
    void deleteByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("DELETE FROM OtpCode o WHERE o.expiresAt < :now")
    void deleteExpiredOtps(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE OtpCode o SET o.attempts = o.attempts + 1 WHERE o.otpId = :otpId")
    int incrementAttempts(@Param("otpId") Long otpId);

    @Query("SELECT COUNT(o) FROM OtpCode o WHERE o.userId = :userId AND o.createdAt > :since")
    long countOtpRequestsSince(@Param("userId") UUID userId, @Param("since") LocalDateTime since);
}