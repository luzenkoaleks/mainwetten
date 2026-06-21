package de.mainwetten.user;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select token
            from PasswordResetToken token
            where token.user.id = :userId
            """)
    Optional<PasswordResetToken> findByUserIdForUpdate(
            @Param("userId") Long userId
    );

    Optional<PasswordResetToken> findByTokenHash(
            String tokenHash
    );

    Optional<PasswordResetToken> findByUserId(
            Long userId
    );

    @Modifying
    @Query("""
            delete from PasswordResetToken token
            where token.expiresAt <= :now
            """)
    int deleteExpiredTokens(
            @Param("now") OffsetDateTime now
    );

    void deleteByUserId(Long userId);
}
