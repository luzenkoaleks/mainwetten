package de.mainwetten.bet;

import de.mainwetten.fish.FishCategory;
import de.mainwetten.security.usage.UsageLimitExceededException;
import de.mainwetten.security.usage.UsageLimitProperties;
import de.mainwetten.user.AppUser;
import de.mainwetten.user.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BetServiceTest {

    private BetRepository betRepository;
    private BetParticipantRepository betParticipantRepository;
    private AppUserRepository appUserRepository;
    private UsageLimitProperties usageLimitProperties;
    private BetService betService;
    private AppUser creator;

    @BeforeEach
    void setUp() {
        betRepository = mock(BetRepository.class);
        betParticipantRepository =
                mock(BetParticipantRepository.class);
        appUserRepository =
                mock(AppUserRepository.class);
        usageLimitProperties =
                mock(UsageLimitProperties.class);

        betService = new BetService(
                betRepository,
                betParticipantRepository,
                appUserRepository,
                usageLimitProperties
        );

        creator = mock(AppUser.class);

        when(creator.getId())
                .thenReturn(1L);

        when(appUserRepository
                .findByUsernameIgnoreCase("alice"))
                .thenReturn(Optional.of(creator));

        when(appUserRepository
                .findByIdForUpdate(1L))
                .thenReturn(Optional.of(creator));

        when(usageLimitProperties
                .getBetCreationsPer24Hours())
                .thenReturn(10);

        when(usageLimitProperties
                .getMaxActiveCreatedBets())
                .thenReturn(25);

        when(betRepository.save(any(Bet.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );
    }

    @Test
    void createsBetWhenUsageLimitsAreNotReached() {
        when(betRepository.countCreatedByUserSince(
                any(Long.class),
                any(OffsetDateTime.class)
        )).thenReturn(9L);

        when(betRepository
                .countActiveOrUpcomingCreatedByUser(
                        any(Long.class),
                        any(LocalDate.class)
                ))
                .thenReturn(24L);

        Bet result = betService.createBet(
                createValidForm(),
                "alice"
        );

        assertNotNull(result);

        verify(betRepository)
                .save(any(Bet.class));

        verify(betParticipantRepository)
                .save(any(BetParticipant.class));
    }

    @Test
    void blocksBetWhenDailyCreationLimitIsReached() {
        when(betRepository.countCreatedByUserSince(
                any(Long.class),
                any(OffsetDateTime.class)
        )).thenReturn(10L);

        UsageLimitExceededException exception =
                assertThrows(
                        UsageLimitExceededException.class,
                        () -> betService.createBet(
                                createValidForm(),
                                "alice"
                        )
                );

        assertEquals(
                "Du kannst innerhalb von 24 Stunden "
                        + "maximal 10 Wetten erstellen.",
                exception.getMessage()
        );

        verify(betRepository, never())
                .save(any(Bet.class));

        verify(betParticipantRepository, never())
                .save(any(BetParticipant.class));
    }

    @Test
    void blocksBetWhenActiveCreationLimitIsReached() {
        when(betRepository.countCreatedByUserSince(
                any(Long.class),
                any(OffsetDateTime.class)
        )).thenReturn(9L);

        when(betRepository
                .countActiveOrUpcomingCreatedByUser(
                        any(Long.class),
                        any(LocalDate.class)
                ))
                .thenReturn(25L);

        UsageLimitExceededException exception =
                assertThrows(
                        UsageLimitExceededException.class,
                        () -> betService.createBet(
                                createValidForm(),
                                "alice"
                        )
                );

        assertEquals(
                "Du kannst maximal 25 eigene aktive oder "
                        + "kommende Wetten gleichzeitig haben.",
                exception.getMessage()
        );

        verify(betRepository, never())
                .save(any(Bet.class));

        verify(betParticipantRepository, never())
                .save(any(BetParticipant.class));
    }

    @Test
    void usesSingularBetLabelForLimitOfOne() {
        when(usageLimitProperties
                .getBetCreationsPer24Hours())
                .thenReturn(1);

        when(betRepository.countCreatedByUserSince(
                any(Long.class),
                any(OffsetDateTime.class)
        )).thenReturn(1L);

        UsageLimitExceededException exception =
                assertThrows(
                        UsageLimitExceededException.class,
                        () -> betService.createBet(
                                createValidForm(),
                                "alice"
                        )
                );

        assertEquals(
                "Du kannst innerhalb von 24 Stunden "
                        + "maximal 1 Wette erstellen.",
                exception.getMessage()
        );
    }

    private BetForm createValidForm() {
        BetForm form = new BetForm();

        form.setTitle("Testwette");
        form.setDescription("Beschreibung");
        form.setStartDate(
                LocalDate.now().plusDays(1)
        );
        form.setEndDate(
                LocalDate.now().plusDays(2)
        );
        form.setScoringMode(
                ScoringMode.values()[0]
        );
        form.setFishCategory(
                FishCategory.values()[0]
        );

        return form;
    }
}
