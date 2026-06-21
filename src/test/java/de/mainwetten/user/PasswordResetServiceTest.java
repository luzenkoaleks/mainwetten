package de.mainwetten.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PasswordResetServiceTest {

    private static final Instant FIXED_INSTANT =
            Instant.parse("2026-06-21T10:00:00Z");

    private AppUserRepository appUserRepository;
    private PasswordResetTokenRepository tokenRepository;
    private Clock clock;
    private PasswordResetService service;

    @BeforeEach
    void setUp() {
        appUserRepository = mock(AppUserRepository.class);
        tokenRepository =
                mock(PasswordResetTokenRepository.class);

        clock = Clock.fixed(
                FIXED_INSTANT,
                ZoneId.of("Europe/Berlin")
        );

        service = new PasswordResetService(
                appUserRepository,
                tokenRepository,
                clock
        );
    }

    @Test
    void createOrReplaceTokenIfAllowedCreatesNewToken() {
        AppUser user = createUser(1L);

        when(appUserRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(user));

        when(tokenRepository.findByUserIdForUpdate(1L))
                .thenReturn(Optional.empty());

        Optional<String> result =
                service.createOrReplaceTokenIfAllowed(user);

        assertTrue(result.isPresent());

        String rawToken = result.orElseThrow();

        ArgumentCaptor<PasswordResetToken> tokenCaptor =
                ArgumentCaptor.forClass(
                        PasswordResetToken.class
                );

        verify(tokenRepository).save(tokenCaptor.capture());

        PasswordResetToken savedToken =
                tokenCaptor.getValue();

        assertEquals(user, savedToken.getUser());
        assertNotEquals(rawToken, savedToken.getTokenHash());
        assertEquals(64, savedToken.getTokenHash().length());

        assertEquals(
                service.hashToken(rawToken),
                savedToken.getTokenHash()
        );

        assertEquals(
                OffsetDateTime.now(clock).plusHours(1),
                savedToken.getExpiresAt()
        );

        assertEquals(
                OffsetDateTime.now(clock),
                savedToken.getLastSentAt()
        );
    }

    @Test
    void createOrReplaceTokenIfAllowedRejectsDuringCooldown() {
        AppUser user = createUser(1L);

        PasswordResetToken existingToken =
                createToken(
                        user,
                        OffsetDateTime.now(clock)
                                .minusSeconds(30)
                );

        when(appUserRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(user));

        when(tokenRepository.findByUserIdForUpdate(1L))
                .thenReturn(Optional.of(existingToken));

        Optional<String> result =
                service.createOrReplaceTokenIfAllowed(user);

        assertTrue(result.isEmpty());

        verify(tokenRepository, never())
                .save(any(PasswordResetToken.class));
    }

    @Test
    void createOrReplaceTokenIfAllowedReplacesAfterCooldown() {
        AppUser user = createUser(1L);

        PasswordResetToken existingToken =
                createToken(
                        user,
                        OffsetDateTime.now(clock)
                                .minusMinutes(3)
                );

        String oldHash = existingToken.getTokenHash();

        when(appUserRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(user));

        when(tokenRepository.findByUserIdForUpdate(1L))
                .thenReturn(Optional.of(existingToken));

        Optional<String> result =
                service.createOrReplaceTokenIfAllowed(user);

        assertTrue(result.isPresent());
        assertNotEquals(
                oldHash,
                existingToken.getTokenHash()
        );

        assertEquals(
                service.hashToken(result.orElseThrow()),
                existingToken.getTokenHash()
        );

        assertEquals(
                OffsetDateTime.now(clock).plusHours(1),
                existingToken.getExpiresAt()
        );

        assertEquals(
                OffsetDateTime.now(clock),
                existingToken.getLastSentAt()
        );

        verify(tokenRepository).save(existingToken);
    }

    @Test
    void createOrReplaceTokenIfAllowedRejectsUnsavedUser() {
        AppUser user = createUser(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.createOrReplaceTokenIfAllowed(user)
        );

        verify(appUserRepository, never())
                .findByIdForUpdate(any());

        verify(tokenRepository, never())
                .save(any(PasswordResetToken.class));
    }

    @Test
    void createOrReplaceTokenIfAllowedRejectsDeletedUser() {
        AppUser user = createUser(1L);

        when(appUserRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.createOrReplaceTokenIfAllowed(user)
        );

        verify(tokenRepository, never())
                .findByUserIdForUpdate(any());

        verify(tokenRepository, never())
                .save(any(PasswordResetToken.class));
    }

    private AppUser createUser(Long id) {
        AppUser user = new AppUser();

        if (id != null) {
            ReflectionTestUtils.setField(user, "id", id);
        }

        user.setUsername("Alice");
        user.setEmail("alice@example.test");
        user.setEmailVerified(true);

        return user;
    }

    private PasswordResetToken createToken(
            AppUser user,
            OffsetDateTime lastSentAt
    ) {
        PasswordResetToken token =
                new PasswordResetToken();

        token.setUser(user);
        token.setTokenHash("a".repeat(64));
        token.setExpiresAt(
                OffsetDateTime.now(clock).plusMinutes(30)
        );
        token.setLastSentAt(lastSentAt);

        return token;
    }
}
