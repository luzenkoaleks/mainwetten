package de.mainwetten.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthenticatedPasswordChangeServiceTest {

    private AppUserRepository appUserRepository;
    private PasswordResetTokenRepository resetTokenRepository;
    private PasswordEncoder passwordEncoder;
    private PersistentLoginService persistentLoginService;
    private ActiveSessionService activeSessionService;
    private AuthenticatedPasswordChangeService service;

    @BeforeEach
    void setUp() {
        appUserRepository =
                mock(AppUserRepository.class);

        resetTokenRepository =
                mock(PasswordResetTokenRepository.class);

        passwordEncoder =
                mock(PasswordEncoder.class);

        persistentLoginService =
                mock(PersistentLoginService.class);

        activeSessionService =
                mock(ActiveSessionService.class);

        service = new AuthenticatedPasswordChangeService(
                appUserRepository,
                resetTokenRepository,
                passwordEncoder,
                persistentLoginService,
                activeSessionService
        );
    }

    @Test
    void changePasswordUpdatesPasswordAndInvalidatesAccess() {
        AppUser user = createUser();

        when(appUserRepository.findByUsernameIgnoreCase("Alice"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "old-password",
                "old-hash"
        )).thenReturn(true);

        when(passwordEncoder.matches(
                "new-password",
                "old-hash"
        )).thenReturn(false);

        when(passwordEncoder.encode("new-password"))
                .thenReturn("new-hash");

        PasswordChangeResult result =
                service.changePassword(
                        "Alice",
                        "old-password",
                        "new-password"
                );

        assertEquals(
                PasswordChangeResult.CHANGED,
                result
        );

        assertEquals(
                "new-hash",
                user.getPasswordHash()
        );

        verify(appUserRepository).save(user);
        verify(resetTokenRepository).deleteByUserId(1L);

        verify(persistentLoginService)
                .invalidateForUser("Alice");

        verify(activeSessionService)
                .expireSessionsForUser("Alice");
    }

    @Test
    void changePasswordRejectsIncorrectCurrentPassword() {
        AppUser user = createUser();

        when(appUserRepository.findByUsernameIgnoreCase("Alice"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "wrong-password",
                "old-hash"
        )).thenReturn(false);

        PasswordChangeResult result =
                service.changePassword(
                        "Alice",
                        "wrong-password",
                        "new-password"
                );

        assertEquals(
                PasswordChangeResult.CURRENT_PASSWORD_INVALID,
                result
        );

        verify(appUserRepository, never()).save(user);
        verify(resetTokenRepository, never())
                .deleteByUserId(1L);

        verify(persistentLoginService, never())
                .invalidateForUser("Alice");

        verify(activeSessionService, never())
                .expireSessionsForUser("Alice");
    }

    @Test
    void changePasswordRejectsUnchangedPassword() {
        AppUser user = createUser();

        when(appUserRepository.findByUsernameIgnoreCase("Alice"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "old-password",
                "old-hash"
        )).thenReturn(true);

        when(passwordEncoder.matches(
                "same-password",
                "old-hash"
        )).thenReturn(true);

        PasswordChangeResult result =
                service.changePassword(
                        "Alice",
                        "old-password",
                        "same-password"
                );

        assertEquals(
                PasswordChangeResult.NEW_PASSWORD_UNCHANGED,
                result
        );

        verify(appUserRepository, never()).save(user);
    }

    private AppUser createUser() {
        AppUser user = new AppUser();

        ReflectionTestUtils.setField(user, "id", 1L);

        user.setUsername("Alice");
        user.setEmail("alice@example.test");
        user.setPasswordHash("old-hash");
        user.setEmailVerified(true);

        return user;
    }
}
