package de.mainwetten.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class EmailVerificationService {

    private static final int TOKEN_BYTE_LENGTH = 32;
    private static final Duration TOKEN_VALIDITY = Duration.ofHours(24);
    private static final Duration RESEND_COOLDOWN = Duration.ofMinutes(2);

    private final EmailVerificationTokenRepository tokenRepository;
    private final AppUserRepository appUserRepository;
    private final Clock clock;
    private final SecureRandom secureRandom;

    @Autowired
    public EmailVerificationService(
            EmailVerificationTokenRepository tokenRepository,
            AppUserRepository appUserRepository,
            Clock clock
    ) {
        this(
                tokenRepository,
                appUserRepository,
                clock,
                new SecureRandom()
        );
    }

    EmailVerificationService(
            EmailVerificationTokenRepository tokenRepository,
            AppUserRepository appUserRepository,
            Clock clock,
            SecureRandom secureRandom
    ) {
        this.tokenRepository = tokenRepository;
        this.appUserRepository = appUserRepository;
        this.clock = clock;
        this.secureRandom = secureRandom;
    }

    @Transactional
    public String createOrReplaceToken(AppUser user) {
        validateStoredUser(user);

        OffsetDateTime now = OffsetDateTime.now(clock);

        EmailVerificationToken verificationToken = tokenRepository
                .findByUserId(user.getId())
                .orElseGet(EmailVerificationToken::new);

        return issueToken(user, verificationToken, now);
    }

    @Transactional
    public Optional<String> createReplacementTokenIfAllowed(
            AppUser user
    ) {
        validateStoredUser(user);

        OffsetDateTime now = OffsetDateTime.now(clock);

        EmailVerificationToken verificationToken = tokenRepository
                .findByUserIdForUpdate(user.getId())
                .orElse(null);

        if (verificationToken != null
                && verificationToken.getLastSentAt()
                .plus(RESEND_COOLDOWN)
                .isAfter(now)) {
            return Optional.empty();
        }

        if (verificationToken == null) {
            verificationToken = new EmailVerificationToken();
        }

        return Optional.of(
                issueToken(user, verificationToken, now)
        );
    }

    @Transactional
    public EmailVerificationResult verifyToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return EmailVerificationResult.INVALID;
        }

        String tokenHash = hashToken(rawToken);

        EmailVerificationToken verificationToken = tokenRepository
                .findByTokenHash(tokenHash)
                .orElse(null);

        if (verificationToken == null) {
            return EmailVerificationResult.INVALID;
        }

        OffsetDateTime now = OffsetDateTime.now(clock);

        if (!verificationToken.getExpiresAt().isAfter(now)) {
            tokenRepository.delete(verificationToken);
            return EmailVerificationResult.EXPIRED;
        }

        AppUser user = verificationToken.getUser();
        user.setEmailVerified(true);
        appUserRepository.save(user);

        tokenRepository.delete(verificationToken);

        return EmailVerificationResult.VERIFIED;
    }

    private String issueToken(
            AppUser user,
            EmailVerificationToken verificationToken,
            OffsetDateTime now
    ) {
        String rawToken = generateRawToken();
        String tokenHash = hashToken(rawToken);

        verificationToken.setUser(user);
        verificationToken.setTokenHash(tokenHash);
        verificationToken.setExpiresAt(now.plus(TOKEN_VALIDITY));
        verificationToken.setLastSentAt(now);

        tokenRepository.save(verificationToken);

        return rawToken;
    }

    private void validateStoredUser(AppUser user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException(
                    "Für einen noch nicht gespeicherten Benutzer kann kein Verifikationstoken erstellt werden."
            );
        }
    }

    String hashToken(String rawToken) {
        try {
            MessageDigest messageDigest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = messageDigest.digest(
                    rawToken.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 ist auf diesem System nicht verfügbar.",
                    exception
            );
        }
    }

    private String generateRawToken() {
        byte[] tokenBytes = new byte[TOKEN_BYTE_LENGTH];
        secureRandom.nextBytes(tokenBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(tokenBytes);
    }
}
