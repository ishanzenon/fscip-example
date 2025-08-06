package com.fscip.identity.repository;

import com.fscip.identity.entity.User;
import com.fscip.identity.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByMobile(String mobile);

    @Modifying
    @Query("UPDATE User u SET u.status = :status, u.updatedAt = :updatedAt WHERE u.userId = :userId")
    int updateUserStatus(@Param("userId") UUID userId, 
                         @Param("status") UserStatus status, 
                         @Param("updatedAt") LocalDateTime updatedAt);

    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :lastLogin, u.updatedAt = :updatedAt WHERE u.userId = :userId")
    int updateLastLogin(@Param("userId") UUID userId, 
                        @Param("lastLogin") LocalDateTime lastLogin, 
                        @Param("updatedAt") LocalDateTime updatedAt);
}