package de.mainwetten.user;

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
public class PasswordResetService {

    private static final int TOKEN_BYTE_LENGTH = 32;
    private static final Duration TOKEN_VALIDITY =
            Duration.ofHours(1);
    private static final Duration REQUEST_COOLDOWN =
            Duration.ofMinutes(2);

    private final AppUserRepository appUserRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetService(
            AppUserRepository appUserRepository,
            PasswordResetTokenRepository tokenRepository,
            Clock clock
    ) {
        this.appUserRepository = appUserRepository;
        this.tokenRepository = tokenRepository;
        this.clock = clock;
    }

    @Transactional
    public Optional<String> createOrReplaceTokenIfAllowed(
            AppUser user
    ) {
        validateStoredUser(user);

        AppUser lockedUser = appUserRepository
                .findByIdForUpdate(user.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Der Benutzer existiert nicht mehr."
                ));

        OffsetDateTime now = OffsetDateTime.now(clock);

        PasswordResetToken resetToken = tokenRepository
                .findByUserIdForUpdate(lockedUser.getId())
                .orElse(null);

        if (resetToken != null
                && resetToken.getLastSentAt()
                        .plus(REQUEST_COOLDOWN)
                        .isAfter(now)) {
            return Optional.empty();
        }

        if (resetToken == null) {
            resetToken = new PasswordResetToken();
        }

        String rawToken = generateRawToken();

        resetToken.setUser(lockedUser);
        resetToken.setTokenHash(hashToken(rawToken));
        resetToken.setExpiresAt(now.plus(TOKEN_VALIDITY));
        resetToken.setLastSentAt(now);

        tokenRepository.save(resetToken);

        return Optional.of(rawToken);
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

    private void validateStoredUser(AppUser user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException(
                    "Für einen nicht gespeicherten Benutzer kann kein Passwort-Reset-Token erstellt werden."
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
