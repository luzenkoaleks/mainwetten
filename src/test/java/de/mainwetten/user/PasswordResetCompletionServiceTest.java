package de.mainwetten.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PasswordResetCompletionServiceTest {

    private PasswordResetTokenRepository tokenRepository;
    private AppUserRepository appUserRepository;
    private PasswordEncoder passwordEncoder;
    private Clock clock;
    private PasswordResetCompletionService service;
    private PersistentLoginService persistentLoginService;
    private ActiveSessionService activeSessionService;

    @BeforeEach
    void setUp() {
        tokenRepository =
                mock(PasswordResetTokenRepository.class);

        appUserRepository =
                mock(AppUserRepository.class);

        passwordEncoder =
                mock(PasswordEncoder.class);

        persistentLoginService =
                mock(PersistentLoginService.class);

        activeSessionService =
                mock(ActiveSessionService.class);

        clock = Clock.fixed(
                Instant.parse("2026-06-21T10:00:00Z"),
                ZoneId.of("Europe/Berlin")
        );

        service = new PasswordResetCompletionService(
                tokenRepository,
                appUserRepository,
                passwordEncoder,
                clock,
                persistentLoginService,
                activeSessionService
        );
    }

    @Test
    void inspectTokenReturnsValidForActiveToken() {
        String rawToken = "valid-token";

        PasswordResetToken token =
                createToken(
                        OffsetDateTime.now(clock)
                                .plusMinutes(30)
                );

        when(tokenRepository.findByTokenHash(
                service.hashToken(rawToken)
        )).thenReturn(Optional.of(token));

        assertEquals(
                PasswordResetTokenStatus.VALID,
                service.inspectToken(rawToken)
        );
    }

    @Test
    void inspectTokenReturnsExpiredForExpiredToken() {
        String rawToken = "expired-token";

        PasswordResetToken token =
                createToken(
                        OffsetDateTime.now(clock)
                                .minusSeconds(1)
                );

        when(tokenRepository.findByTokenHash(
                service.hashToken(rawToken)
        )).thenReturn(Optional.of(token));

        assertEquals(
                PasswordResetTokenStatus.EXPIRED,
                service.inspectToken(rawToken)
        );

        verify(activeSessionService, never())
                .expireSessionsForUser(
                        org.mockito.ArgumentMatchers.anyString()
                );
    }

    @Test
    void resetPasswordChangesPasswordAndDeletesToken() {
        String rawToken = "valid-token";
        AppUser user = new AppUser();
        user.setUsername("Alice");

        PasswordResetToken token =
                createToken(
                        OffsetDateTime.now(clock)
                                .plusMinutes(30)
                );

        token.setUser(user);

        when(tokenRepository.findByTokenHashForUpdate(
                service.hashToken(rawToken)
        )).thenReturn(Optional.of(token));

        when(passwordEncoder.encode("new-password"))
                .thenReturn("encoded-password");

        PasswordResetResult result =
                service.resetPassword(
                        rawToken,
                        "new-password"
                );

        assertEquals(
                PasswordResetResult.RESET,
                result
        );

        assertEquals(
                "encoded-password",
                user.getPasswordHash()
        );

        verify(appUserRepository).save(user);
        verify(persistentLoginService)
                .invalidateForUser("Alice");
        verify(tokenRepository).delete(token);
        verify(activeSessionService)
                .expireSessionsForUser("Alice");
    }

    @Test
    void resetPasswordRejectsExpiredToken() {
        String rawToken = "expired-token";

        PasswordResetToken token =
                createToken(
                        OffsetDateTime.now(clock)
                                .minusSeconds(1)
                );

        when(tokenRepository.findByTokenHashForUpdate(
                service.hashToken(rawToken)
        )).thenReturn(Optional.of(token));

        PasswordResetResult result =
                service.resetPassword(
                        rawToken,
                        "new-password"
                );

        assertEquals(
                PasswordResetResult.EXPIRED,
                result
        );

        verify(tokenRepository).delete(token);

        verify(persistentLoginService, never())
                .invalidateForUser(
                        org.mockito.ArgumentMatchers.anyString()
                );

        verify(appUserRepository, never())
                .save(
                        org.mockito.ArgumentMatchers.any()
                );

        verify(activeSessionService, never())
                .expireSessionsForUser(
                        org.mockito.ArgumentMatchers.anyString()
                );
    }

    @Test
    void resetPasswordRejectsUnknownToken() {
        String rawToken = "unknown-token";

        when(tokenRepository.findByTokenHashForUpdate(
                service.hashToken(rawToken)
        )).thenReturn(Optional.empty());

        PasswordResetResult result =
                service.resetPassword(
                        rawToken,
                        "new-password"
                );

        assertEquals(
                PasswordResetResult.INVALID,
                result
        );

        verify(appUserRepository, never())
                .save(
                        org.mockito.ArgumentMatchers.any()
                );
        verify(persistentLoginService, never())
                .invalidateForUser(
                        org.mockito.ArgumentMatchers.anyString()
                );
    }

    @Test
    void resetPasswordRejectsShortPassword() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.resetPassword(
                        "token",
                        "short"
                )
        );

        verify(tokenRepository, never())
                .findByTokenHashForUpdate(
                        org.mockito.ArgumentMatchers.anyString()
                );
    }

    private PasswordResetToken createToken(
            OffsetDateTime expiresAt
    ) {
        PasswordResetToken token =
                new PasswordResetToken();

        token.setTokenHash("a".repeat(64));
        token.setExpiresAt(expiresAt);
        token.setLastSentAt(
                OffsetDateTime.now(clock)
        );

        return token;
    }
}
