package de.mainwetten.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

@Service
public class AuthenticatedPasswordChangeService {

    private static final int MINIMUM_PASSWORD_LENGTH = 8;
    private static final int MAXIMUM_BCRYPT_BYTES = 72;

    private final AppUserRepository appUserRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final PersistentLoginService persistentLoginService;
    private final ActiveSessionService activeSessionService;

    public AuthenticatedPasswordChangeService(
            AppUserRepository appUserRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            PersistentLoginService persistentLoginService,
            ActiveSessionService activeSessionService
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordResetTokenRepository =
                passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.persistentLoginService = persistentLoginService;
        this.activeSessionService = activeSessionService;
    }

    @Transactional
    public PasswordChangeResult changePassword(
            String username,
            String currentPassword,
            String newPassword
    ) {
        validateInput(username, currentPassword, newPassword);

        AppUser user = appUserRepository
                .findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new IllegalStateException(
                        "Der angemeldete Benutzer wurde nicht gefunden."
                ));

        if (!passwordEncoder.matches(
                currentPassword,
                user.getPasswordHash()
        )) {
            return PasswordChangeResult.CURRENT_PASSWORD_INVALID;
        }

        if (passwordEncoder.matches(
                newPassword,
                user.getPasswordHash()
        )) {
            return PasswordChangeResult.NEW_PASSWORD_UNCHANGED;
        }

        user.setPasswordHash(
                passwordEncoder.encode(newPassword)
        );

        appUserRepository.save(user);

        passwordResetTokenRepository.deleteByUserId(
                user.getId()
        );

        persistentLoginService.invalidateForUser(
                user.getUsername()
        );

        activeSessionService.expireSessionsForUser(
                user.getUsername()
        );

        return PasswordChangeResult.CHANGED;
    }

    private void validateInput(
            String username,
            String currentPassword,
            String newPassword
    ) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "Der Benutzername darf nicht leer sein."
            );
        }

        if (currentPassword == null
                || currentPassword.isBlank()) {
            throw new IllegalArgumentException(
                    "Das aktuelle Passwort darf nicht leer sein."
            );
        }

        if (newPassword == null
                || newPassword.length()
                        < MINIMUM_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "Das neue Passwort muss mindestens 8 Zeichen lang sein."
            );
        }

        if (newPassword
                .getBytes(StandardCharsets.UTF_8).length
                > MAXIMUM_BCRYPT_BYTES) {
            throw new IllegalArgumentException(
                    "Das neue Passwort ist für BCrypt technisch zu lang."
            );
        }
    }
}
