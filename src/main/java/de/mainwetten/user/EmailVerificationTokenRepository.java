package de.mainwetten.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.Modifying;
import java.time.OffsetDateTime;

public interface EmailVerificationTokenRepository
        extends JpaRepository<EmailVerificationToken, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select token
        from EmailVerificationToken token
        where token.user.id = :userId
        """)
    Optional<EmailVerificationToken> findByUserIdForUpdate(
            @Param("userId") Long userId
    );

    @Modifying
    @Query("""
        delete from EmailVerificationToken token
        where token.expiresAt <= :now
        """)
    int deleteExpiredTokens(
            @Param("now") OffsetDateTime now
    );

    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

    Optional<EmailVerificationToken> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
