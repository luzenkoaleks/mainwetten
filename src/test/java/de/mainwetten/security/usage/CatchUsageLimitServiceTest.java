package de.mainwetten.security.usage;

import de.mainwetten.catchentry.CatchRecordRepository;
import de.mainwetten.user.AppUser;
import de.mainwetten.user.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CatchUsageLimitServiceTest {

    private CatchRecordRepository catchRecordRepository;
    private AppUserRepository appUserRepository;
    private UsageLimitProperties usageLimitProperties;
    private CatchUsageLimitService catchUsageLimitService;
    private AppUser user;

    @BeforeEach
    void setUp() {
        catchRecordRepository =
                mock(CatchRecordRepository.class);

        appUserRepository =
                mock(AppUserRepository.class);

        usageLimitProperties =
                mock(UsageLimitProperties.class);

        catchUsageLimitService =
                new CatchUsageLimitService(
                        catchRecordRepository,
                        appUserRepository,
                        usageLimitProperties
                );

        user = mock(AppUser.class);

        when(appUserRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(user));

        when(usageLimitProperties
                .getCatchCreationsPer24Hours())
                .thenReturn(100);
    }

    @Test
    void returnsLockedUserWhenLimitIsNotReached() {
        when(catchRecordRepository
                .countCreatedByUserSince(
                        any(Long.class),
                        any(OffsetDateTime.class)
                ))
                .thenReturn(99L);

        AppUser result =
                catchUsageLimitService
                        .lockUserAndCheckLimit(1L);

        assertSame(user, result);

        verify(appUserRepository)
                .findByIdForUpdate(1L);
    }

    @Test
    void blocksCatchWhenCreationLimitIsReached() {
        when(catchRecordRepository
                .countCreatedByUserSince(
                        any(Long.class),
                        any(OffsetDateTime.class)
                ))
                .thenReturn(100L);

        UsageLimitExceededException exception =
                assertThrows(
                        UsageLimitExceededException.class,
                        () -> catchUsageLimitService
                                .lockUserAndCheckLimit(1L)
                );

        assertEquals(
                "Du kannst innerhalb von 24 Stunden "
                        + "maximal 100 Fänge eintragen.",
                exception.getMessage()
        );
    }

    @Test
    void usesSingularCatchLabelForLimitOfOne() {
        when(usageLimitProperties
                .getCatchCreationsPer24Hours())
                .thenReturn(1);

        when(catchRecordRepository
                .countCreatedByUserSince(
                        any(Long.class),
                        any(OffsetDateTime.class)
                ))
                .thenReturn(1L);

        UsageLimitExceededException exception =
                assertThrows(
                        UsageLimitExceededException.class,
                        () -> catchUsageLimitService
                                .lockUserAndCheckLimit(1L)
                );

        assertEquals(
                "Du kannst innerhalb von 24 Stunden "
                        + "maximal 1 Fang eintragen.",
                exception.getMessage()
        );
    }
}
