package de.mainwetten.user;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PasswordResetTokenCleanupServiceTest {

    @Test
    void deleteExpiredTokensUsesCurrentApplicationTime() {
        PasswordResetTokenRepository tokenRepository =
                mock(PasswordResetTokenRepository.class);

        Clock clock = Clock.fixed(
                Instant.parse("2026-06-21T10:00:00Z"),
                ZoneId.of("Europe/Berlin")
        );

        OffsetDateTime expectedTime =
                OffsetDateTime.now(clock);

        when(tokenRepository.deleteExpiredTokens(expectedTime))
                .thenReturn(4);

        PasswordResetTokenCleanupService service =
                new PasswordResetTokenCleanupService(
                        tokenRepository,
                        clock
                );

        int deletedTokens = service.deleteExpiredTokens();

        assertEquals(4, deletedTokens);

        verify(tokenRepository)
                .deleteExpiredTokens(expectedTime);
    }
}
