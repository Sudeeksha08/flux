package com.brightloop.auth.repository;

import com.brightloop.auth.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import com.brightloop.auth.entity.User;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    void deleteAllByUser(User user);

    @Modifying
    @Query("delete from PasswordResetToken t where t.expiresAt < :now or t.usedAt is not null")
    int deleteExpiredOrUsed(@Param("now") Instant now);
}
