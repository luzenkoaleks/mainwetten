package de.mainwetten.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.HexFormat;

@Service
public class PasswordResetCompletionService {

    private static final int MINIMUM_PASSWORD_LENGTH = 8;
    private static final int MAXIMUM_BCRYPT_BYTES = 72;

    private final PasswordResetTokenRepository tokenRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private final PersistentLoginService persistentLoginService;
    private final ActiveSessionService activeSessionService;

    public PasswordResetCompletionService(
            PasswordResetTokenRepository tokenRepository,
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            Clock clock,
            PersistentLoginService persistentLoginService,
            ActiveSessionService activeSessionService
    ) {
        this.tokenRepository = tokenRepository;
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
        this.persistentLoginService = persistentLoginService;
        this.activeSessionService = activeSessionService;
    }

    @Transactional(readOnly = true)
    public PasswordResetTokenStatus inspectToken(
            String rawToken
    ) {
        if (rawToken == null || rawToken.isBlank()) {
            return PasswordResetTokenStatus.INVALID;
        }

        PasswordResetToken token = tokenRepository
                .findByTokenHash(hashToken(rawToken))
                .orElse(null);

        if (token == null) {
            return PasswordResetTokenStatus.INVALID;
        }

        if (!token.getExpiresAt().isAfter(
                OffsetDateTime.now(clock)
        )) {
            return PasswordResetTokenStatus.EXPIRED;
        }

        return PasswordResetTokenStatus.VALID;
    }

    @Transactional
    public PasswordResetResult resetPassword(
            String rawToken,
            String newPassword
    ) {
        validatePassword(newPassword);

        if (rawToken == null || rawToken.isBlank()) {
            return PasswordResetResult.INVALID;
        }

        PasswordResetToken token = tokenRepository
                .findByTokenHashForUpdate(
                        hashToken(rawToken)
                )
                .orElse(null);

        if (token == null) {
            return PasswordResetResult.INVALID;
        }

        OffsetDateTime now = OffsetDateTime.now(clock);

        if (!token.getExpiresAt().isAfter(now)) {
            tokenRepository.delete(token);
            return PasswordResetResult.EXPIRED;
        }

        AppUser user = token.getUser();
        user.setPasswordHash(
                passwordEncoder.encode(newPassword)
        );

        appUserRepository.save(user);

        persistentLoginService.invalidateForUser(
                user.getUsername()
        );

        activeSessionService.expireSessionsForUser(
                user.getUsername()
        );

        tokenRepository.delete(token);

        return PasswordResetResult.RESET;
    }

    private void validatePassword(String password) {
        if (password == null
                || password.length()
                        < MINIMUM_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "Das neue Passwort muss mindestens 8 Zeichen lang sein."
            );
        }

        if (password.getBytes(StandardCharsets.UTF_8).length
                > MAXIMUM_BCRYPT_BYTES) {
            throw new IllegalArgumentException(
                    "Das neue Passwort ist für BCrypt technisch zu lang."
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
}
