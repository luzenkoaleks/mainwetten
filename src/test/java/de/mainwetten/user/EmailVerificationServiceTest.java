package de.mainwetten.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailVerificationServiceTest {

    private static final Instant FIXED_INSTANT =
            Instant.parse("2026-06-21T10:00:00Z");

    private EmailVerificationTokenRepository tokenRepository;
    private AppUserRepository appUserRepository;
    private Clock clock;
    private EmailVerificationService service;

    @BeforeEach
    void setUp() {
        tokenRepository =
                mock(EmailVerificationTokenRepository.class);

        appUserRepository =
                mock(AppUserRepository.class);

        clock = Clock.fixed(
                FIXED_INSTANT,
                ZoneId.of("Europe/Berlin")
        );

        service = new EmailVerificationService(
                tokenRepository,
                appUserRepository,
                clock,
                new SecureRandom()
        );
    }

    @Test
    void createOrReplaceToken_createsNewTokenForUser() {
        AppUser user = createUser(1L, false);

        when(tokenRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        String rawToken = service.createOrReplaceToken(user);

        assertNotNull(rawToken);
        assertFalse(rawToken.isBlank());

        ArgumentCaptor<EmailVerificationToken> tokenCaptor =
                ArgumentCaptor.forClass(
                        EmailVerificationToken.class
                );

        verify(tokenRepository).save(tokenCaptor.capture());

        EmailVerificationToken savedToken =
                tokenCaptor.getValue();

        assertEquals(user, savedToken.getUser());
        assertNotEquals(rawToken, savedToken.getTokenHash());
        assertEquals(64, savedToken.getTokenHash().length());

        assertEquals(
                OffsetDateTime.now(clock).plusHours(24),
                savedToken.getExpiresAt()
        );

        assertEquals(
                OffsetDateTime.now(clock),
                savedToken.getLastSentAt()
        );

        assertEquals(
                service.hashToken(rawToken),
                savedToken.getTokenHash()
        );
    }

    @Test
    void createOrReplaceToken_updatesExistingToken() {
        AppUser user = createUser(1L, false);

        EmailVerificationToken existingToken =
                new EmailVerificationToken();

        existingToken.setUser(user);
        existingToken.setTokenHash("a".repeat(64));
        existingToken.setExpiresAt(
                OffsetDateTime.now(clock).plusHours(1)
        );
        existingToken.setLastSentAt(
                OffsetDateTime.now(clock).minusHours(1)
        );

        when(tokenRepository.findByUserId(1L))
                .thenReturn(Optional.of(existingToken));

        String rawToken = service.createOrReplaceToken(user);

        verify(tokenRepository).save(existingToken);

        assertEquals(
                service.hashToken(rawToken),
                existingToken.getTokenHash()
        );

        assertEquals(
                OffsetDateTime.now(clock).plusHours(24),
                existingToken.getExpiresAt()
        );

        assertEquals(
                OffsetDateTime.now(clock),
                existingToken.getLastSentAt()
        );
    }

    @Test
    void createOrReplaceToken_rejectsUnsavedUser() {
        AppUser user = createUser(null, false);

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> service.createOrReplaceToken(user)
        );

        verify(tokenRepository, never())
                .save(any(EmailVerificationToken.class));
    }

    @Test
    void verifyToken_verifiesUserAndDeletesToken() {
        AppUser user = createUser(1L, false);

        String rawToken = "test-verification-token";
        String tokenHash = service.hashToken(rawToken);

        EmailVerificationToken token =
                createToken(
                        user,
                        tokenHash,
                        OffsetDateTime.now(clock).plusMinutes(30)
                );

        when(tokenRepository.findByTokenHash(tokenHash))
                .thenReturn(Optional.of(token));

        EmailVerificationResult result =
                service.verifyToken(rawToken);

        assertEquals(
                EmailVerificationResult.VERIFIED,
                result
        );

        assertTrue(user.isEmailVerified());

        verify(appUserRepository).save(user);
        verify(tokenRepository).delete(token);
    }

    @Test
    void verifyToken_rejectsUnknownToken() {
        String rawToken = "unknown-token";
        String tokenHash = service.hashToken(rawToken);

        when(tokenRepository.findByTokenHash(tokenHash))
                .thenReturn(Optional.empty());

        EmailVerificationResult result =
                service.verifyToken(rawToken);

        assertEquals(
                EmailVerificationResult.INVALID,
                result
        );

        verify(appUserRepository, never())
                .save(any(AppUser.class));

        verify(tokenRepository, never())
                .delete(any(EmailVerificationToken.class));
    }

    @Test
    void verifyToken_rejectsBlankToken() {
        EmailVerificationResult result =
                service.verifyToken("   ");

        assertEquals(
                EmailVerificationResult.INVALID,
                result
        );

        verify(tokenRepository, never())
                .findByTokenHash(any(String.class));
    }

    @Test
    void verifyToken_rejectsExpiredTokenAndDeletesIt() {
        AppUser user = createUser(1L, false);

        String rawToken = "expired-token";
        String tokenHash = service.hashToken(rawToken);

        EmailVerificationToken token =
                createToken(
                        user,
                        tokenHash,
                        OffsetDateTime.now(clock).minusSeconds(1)
                );

        when(tokenRepository.findByTokenHash(tokenHash))
                .thenReturn(Optional.of(token));

        EmailVerificationResult result =
                service.verifyToken(rawToken);

        assertEquals(
                EmailVerificationResult.EXPIRED,
                result
        );

        assertFalse(user.isEmailVerified());

        verify(appUserRepository, never())
                .save(any(AppUser.class));

        verify(tokenRepository).delete(token);
    }

    @Test
    void verifyToken_treatsTokenExpiringNowAsExpired() {
        AppUser user = createUser(1L, false);

        String rawToken = "expires-now";
        String tokenHash = service.hashToken(rawToken);

        EmailVerificationToken token =
                createToken(
                        user,
                        tokenHash,
                        OffsetDateTime.now(clock)
                );

        when(tokenRepository.findByTokenHash(tokenHash))
                .thenReturn(Optional.of(token));

        EmailVerificationResult result =
                service.verifyToken(rawToken);

        assertEquals(
                EmailVerificationResult.EXPIRED,
                result
        );

        verify(tokenRepository).delete(token);
    }

    @Test
    void createReplacementTokenIfAllowed_rejectsRequestWithinCooldown() {
        AppUser user = createUser(1L, false);

        EmailVerificationToken existingToken =
                new EmailVerificationToken();

        existingToken.setUser(user);
        existingToken.setTokenHash("a".repeat(64));
        existingToken.setExpiresAt(
                OffsetDateTime.now(clock).plusHours(23)
        );
        existingToken.setLastSentAt(
                OffsetDateTime.now(clock).minusSeconds(30)
        );

        when(tokenRepository.findByUserIdForUpdate(1L))
                .thenReturn(Optional.of(existingToken));

        Optional<String> result =
                service.createReplacementTokenIfAllowed(user);

        assertTrue(result.isEmpty());

        verify(tokenRepository, never())
                .save(any(EmailVerificationToken.class));
    }

    @Test
    void createReplacementTokenIfAllowed_replacesTokenAfterCooldown() {
        AppUser user = createUser(1L, false);

        EmailVerificationToken existingToken =
                new EmailVerificationToken();

        existingToken.setUser(user);
        existingToken.setTokenHash("a".repeat(64));
        existingToken.setExpiresAt(
                OffsetDateTime.now(clock).plusHours(1)
        );
        existingToken.setLastSentAt(
                OffsetDateTime.now(clock).minusMinutes(3)
        );

        when(tokenRepository.findByUserIdForUpdate(1L))
                .thenReturn(Optional.of(existingToken));

        Optional<String> result =
                service.createReplacementTokenIfAllowed(user);

        assertTrue(result.isPresent());

        assertEquals(
                service.hashToken(result.orElseThrow()),
                existingToken.getTokenHash()
        );

        assertEquals(
                OffsetDateTime.now(clock),
                existingToken.getLastSentAt()
        );

        assertEquals(
                OffsetDateTime.now(clock).plusHours(24),
                existingToken.getExpiresAt()
        );

        verify(tokenRepository).save(existingToken);
    }

    private AppUser createUser(
            Long id,
            boolean emailVerified
    ) {
        AppUser user = new AppUser();

        if (id != null) {
            ReflectionTestUtils.setField(user, "id", id);
        }

        user.setUsername("Alice");
        user.setEmail("alice@example.test");
        user.setEmailVerified(emailVerified);

        return user;
    }

    private EmailVerificationToken createToken(
            AppUser user,
            String tokenHash,
            OffsetDateTime expiresAt
    ) {
        EmailVerificationToken token =
                new EmailVerificationToken();

        token.setUser(user);
        token.setTokenHash(tokenHash);
        token.setExpiresAt(expiresAt);

        return token;
    }
}
