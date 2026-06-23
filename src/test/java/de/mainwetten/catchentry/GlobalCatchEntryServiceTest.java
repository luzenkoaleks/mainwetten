package de.mainwetten.catchentry;

import de.mainwetten.bet.Bet;
import de.mainwetten.bet.BetParticipant;
import de.mainwetten.bet.BetParticipantRepository;
import de.mainwetten.bet.ParticipantStatus;
import de.mainwetten.fish.FishCategory;
import de.mainwetten.fish.FishSpecies;
import de.mainwetten.fish.FishSpeciesRepository;
import de.mainwetten.user.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.mainwetten.security.usage.CatchUsageLimitService;

@ExtendWith(MockitoExtension.class)
class GlobalCatchEntryServiceTest {

    @Mock
    private CatchRecordRepository catchRecordRepository;

    @Mock
    private CatchAssignmentRepository catchAssignmentRepository;

    @Mock
    private BetParticipantRepository betParticipantRepository;

    @Mock
    private FishSpeciesRepository fishSpeciesRepository;

    @Mock
    private CatchEntryWindowService catchEntryWindowService;

    @Mock
    private CatchUsageLimitService catchUsageLimitService;

    @InjectMocks
    private GlobalCatchEntryService globalCatchEntryService;

    @Test
    void createGlobalCatchEntry_rejectsEmptyBetSelection() {
        GlobalCatchForm form = createForm(
                5L,
                "42.5",
                List.of()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> globalCatchEntryService.createGlobalCatchEntry(
                        "Alice",
                        form
                )
        );

        verify(fishSpeciesRepository, never())
                .findByIdAndActiveTrue(5L);

        verify(catchRecordRepository, never())
                .save(any(CatchRecord.class));

        verify(catchAssignmentRepository, never())
                .save(any(CatchAssignment.class));
    }

    @Test
    void createGlobalCatchEntry_rejectsInactiveOrUnknownFishSpecies() {
        GlobalCatchForm form = createForm(
                5L,
                "42.5",
                List.of(10L)
        );

        when(fishSpeciesRepository.findByIdAndActiveTrue(5L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> globalCatchEntryService.createGlobalCatchEntry(
                        "Alice",
                        form
                )
        );

        verify(
                betParticipantRepository,
                never()
        ).findByBetIdAndUserUsernameAndStatus(
                10L,
                "Alice",
                ParticipantStatus.ACCEPTED
        );

        verify(catchRecordRepository, never())
                .save(any(CatchRecord.class));
    }

    @Test
    void createGlobalCatchEntry_rejectsBetWithoutAcceptedParticipation() {
        FishSpecies fishSpecies = createFishSpecies(
                5L,
                "Hecht",
                FishCategory.FRESHWATER
        );

        GlobalCatchForm form = createForm(
                5L,
                "42.5",
                List.of(10L)
        );

        when(fishSpeciesRepository.findByIdAndActiveTrue(5L))
                .thenReturn(Optional.of(fishSpecies));

        when(betParticipantRepository
                .findByBetIdAndUserUsernameAndStatus(
                        10L,
                        "Alice",
                        ParticipantStatus.ACCEPTED
                ))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> globalCatchEntryService.createGlobalCatchEntry(
                        "Alice",
                        form
                )
        );

        verify(catchRecordRepository, never())
                .save(any(CatchRecord.class));

        verify(catchAssignmentRepository, never())
                .save(any(CatchAssignment.class));
    }

    @Test
    void createGlobalCatchEntry_rejectsBetOutsideEntryWindow() {
        AppUser user = createUser(1L, "Alice");
        Bet bet = createBet(10L, FishCategory.FRESHWATER);

        BetParticipant participation =
                createParticipation(bet, user);

        FishSpecies fishSpecies = createFishSpecies(
                5L,
                "Hecht",
                FishCategory.FRESHWATER
        );

        GlobalCatchForm form = createForm(
                5L,
                "42.5",
                List.of(10L)
        );

        when(fishSpeciesRepository.findByIdAndActiveTrue(5L))
                .thenReturn(Optional.of(fishSpecies));

        when(betParticipantRepository
                .findByBetIdAndUserUsernameAndStatus(
                        10L,
                        "Alice",
                        ParticipantStatus.ACCEPTED
                ))
                .thenReturn(Optional.of(participation));

        when(catchEntryWindowService.canEnterCatch(bet))
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> globalCatchEntryService.createGlobalCatchEntry(
                        "Alice",
                        form
                )
        );

        verify(catchRecordRepository, never())
                .save(any(CatchRecord.class));

        verify(catchAssignmentRepository, never())
                .save(any(CatchAssignment.class));
    }

    @Test
    void createGlobalCatchEntry_rejectsFishSpeciesThatDoesNotMatchEveryBet() {
        AppUser user = createUser(1L, "Alice");
        Bet freshwaterBet = createBet(
                10L,
                FishCategory.FRESHWATER
        );

        BetParticipant participation =
                createParticipation(freshwaterBet, user);

        FishSpecies saltwaterFish = createFishSpecies(
                5L,
                "Dorsch",
                FishCategory.SALTWATER
        );

        GlobalCatchForm form = createForm(
                5L,
                "42.5",
                List.of(10L)
        );

        when(fishSpeciesRepository.findByIdAndActiveTrue(5L))
                .thenReturn(Optional.of(saltwaterFish));

        when(betParticipantRepository
                .findByBetIdAndUserUsernameAndStatus(
                        10L,
                        "Alice",
                        ParticipantStatus.ACCEPTED
                ))
                .thenReturn(Optional.of(participation));

        when(catchEntryWindowService.canEnterCatch(freshwaterBet))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> globalCatchEntryService.createGlobalCatchEntry(
                        "Alice",
                        form
                )
        );

        verify(catchRecordRepository, never())
                .save(any(CatchRecord.class));

        verify(catchAssignmentRepository, never())
                .save(any(CatchAssignment.class));
    }

    @Test
    void createGlobalCatchEntry_validatesAllBetsBeforeSavingCatch() {
        AppUser user = createUser(1L, "Alice");

        Bet allowedBet = createBet(
                10L,
                FishCategory.FRESHWATER
        );

        BetParticipant allowedParticipation =
                createParticipation(allowedBet, user);

        FishSpecies fishSpecies = createFishSpecies(
                5L,
                "Hecht",
                FishCategory.FRESHWATER
        );

        GlobalCatchForm form = createForm(
                5L,
                "42.5",
                List.of(10L, 20L)
        );

        when(fishSpeciesRepository.findByIdAndActiveTrue(5L))
                .thenReturn(Optional.of(fishSpecies));

        when(betParticipantRepository
                .findByBetIdAndUserUsernameAndStatus(
                        10L,
                        "Alice",
                        ParticipantStatus.ACCEPTED
                ))
                .thenReturn(Optional.of(allowedParticipation));

        when(catchEntryWindowService.canEnterCatch(allowedBet))
                .thenReturn(true);

        when(betParticipantRepository
                .findByBetIdAndUserUsernameAndStatus(
                        20L,
                        "Alice",
                        ParticipantStatus.ACCEPTED
                ))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> globalCatchEntryService.createGlobalCatchEntry(
                        "Alice",
                        form
                )
        );

        verify(catchRecordRepository, never())
                .save(any(CatchRecord.class));

        verify(catchAssignmentRepository, never())
                .save(any(CatchAssignment.class));
    }

    @Test
    void createGlobalCatchEntry_ignoresDuplicateBetIds() {
        AppUser user = createUser(1L, "Alice");
        Bet bet = createBet(10L, FishCategory.ALL);

        BetParticipant participation =
                createParticipation(bet, user);

        when(catchUsageLimitService
                .lockUserAndCheckLimit(1L))
                .thenReturn(user);

        FishSpecies fishSpecies = createFishSpecies(
                5L,
                "Dorsch",
                FishCategory.SALTWATER
        );

        GlobalCatchForm form = createForm(
                5L,
                "42.5",
                List.of(10L, 10L, 10L)
        );

        when(fishSpeciesRepository.findByIdAndActiveTrue(5L))
                .thenReturn(Optional.of(fishSpecies));

        when(betParticipantRepository
                .findByBetIdAndUserUsernameAndStatus(
                        10L,
                        "Alice",
                        ParticipantStatus.ACCEPTED
                ))
                .thenReturn(Optional.of(participation));

        when(catchEntryWindowService.canEnterCatch(bet))
                .thenReturn(true);

        when(catchRecordRepository.save(any(CatchRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GlobalCatchEntryResult result =
                globalCatchEntryService.createGlobalCatchEntry(
                        "Alice",
                        form
                );

        assertEquals(1, result.assignmentCount());
        assertEquals("Dorsch", result.fishSpeciesName());
        assertEquals(new BigDecimal("42.5"), result.lengthCm());
        assertEquals(List.of("Wette 10"), result.betTitles());

        verify(betParticipantRepository, times(1))
                .findByBetIdAndUserUsernameAndStatus(
                        10L,
                        "Alice",
                        ParticipantStatus.ACCEPTED
                );

        verify(catchAssignmentRepository, times(1))
                .save(any(CatchAssignment.class));
    }

    @Test
    void createGlobalCatchEntry_savesOneCatchAndAssignsAllSelectedBets() {
        AppUser user = createUser(1L, "Alice");

        Bet freshwaterBet = createBet(
                10L,
                FishCategory.FRESHWATER
        );

        Bet allSpeciesBet = createBet(
                20L,
                FishCategory.ALL
        );

        BetParticipant freshwaterParticipation =
                createParticipation(freshwaterBet, user);

        BetParticipant allSpeciesParticipation =
                createParticipation(allSpeciesBet, user);

        when(catchUsageLimitService
                .lockUserAndCheckLimit(1L))
                .thenReturn(user);

        FishSpecies fishSpecies = createFishSpecies(
                5L,
                "Hecht",
                FishCategory.FRESHWATER
        );

        GlobalCatchForm form = createForm(
                5L,
                "87.4",
                List.of(10L, 20L)
        );

        when(fishSpeciesRepository.findByIdAndActiveTrue(5L))
                .thenReturn(Optional.of(fishSpecies));

        when(betParticipantRepository
                .findByBetIdAndUserUsernameAndStatus(
                        10L,
                        "Alice",
                        ParticipantStatus.ACCEPTED
                ))
                .thenReturn(Optional.of(freshwaterParticipation));

        when(betParticipantRepository
                .findByBetIdAndUserUsernameAndStatus(
                        20L,
                        "Alice",
                        ParticipantStatus.ACCEPTED
                ))
                .thenReturn(Optional.of(allSpeciesParticipation));

        when(catchEntryWindowService.canEnterCatch(freshwaterBet))
                .thenReturn(true);

        when(catchEntryWindowService.canEnterCatch(allSpeciesBet))
                .thenReturn(true);

        when(catchRecordRepository.save(any(CatchRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GlobalCatchEntryResult result =
                globalCatchEntryService.createGlobalCatchEntry(
                        "Alice",
                        form
                );

        assertEquals(2, result.assignmentCount());
        assertEquals("Hecht", result.fishSpeciesName());
        assertEquals(new BigDecimal("87.4"), result.lengthCm());
        assertEquals(
                List.of("Wette 10", "Wette 20"),
                result.betTitles()
        );

        ArgumentCaptor<CatchRecord> catchCaptor =
                ArgumentCaptor.forClass(CatchRecord.class);

        verify(catchRecordRepository, times(1))
                .save(catchCaptor.capture());

        CatchRecord savedCatch = catchCaptor.getValue();

        assertSame(user, savedCatch.getUser());
        assertSame(fishSpecies, savedCatch.getFishSpecies());
        assertEquals(
                new BigDecimal("87.4"),
                savedCatch.getLengthCm()
        );
        assertNotNull(savedCatch.getCaughtAt());

        ArgumentCaptor<CatchAssignment> assignmentCaptor =
                ArgumentCaptor.forClass(CatchAssignment.class);

        verify(catchAssignmentRepository, times(2))
                .save(assignmentCaptor.capture());

        List<CatchAssignment> assignments =
                assignmentCaptor.getAllValues();

        assertSame(
                savedCatch,
                assignments.get(0).getCatchRecord()
        );

        assertSame(
                savedCatch,
                assignments.get(1).getCatchRecord()
        );

        assertEquals(
                List.of(freshwaterBet, allSpeciesBet),
                assignments.stream()
                        .map(CatchAssignment::getBet)
                        .toList()
        );
    }

    private GlobalCatchForm createForm(
            Long fishSpeciesId,
            String lengthCm,
            List<Long> betIds
    ) {
        GlobalCatchForm form = new GlobalCatchForm();
        form.setFishSpeciesId(fishSpeciesId);
        form.setLengthCm(new BigDecimal(lengthCm));
        form.setBetIds(betIds);
        return form;
    }

    private AppUser createUser(Long id, String username) {
        AppUser user = new AppUser();
        ReflectionTestUtils.setField(user, "id", id);
        user.setUsername(username);
        return user;
    }

    private Bet createBet(
            Long id,
            FishCategory fishCategory
    ) {
        Bet bet = new Bet();
        ReflectionTestUtils.setField(bet, "id", id);
        bet.setTitle("Wette " + id);
        bet.setFishCategory(fishCategory);
        return bet;
    }

    private BetParticipant createParticipation(
            Bet bet,
            AppUser user
    ) {
        BetParticipant participation = new BetParticipant();
        participation.setBet(bet);
        participation.setUser(user);
        participation.setStatus(ParticipantStatus.ACCEPTED);
        return participation;
    }

    private FishSpecies createFishSpecies(
            Long id,
            String name,
            FishCategory category
    ) {
        FishSpecies fishSpecies = new FishSpecies();

        ReflectionTestUtils.setField(fishSpecies, "id", id);
        ReflectionTestUtils.setField(fishSpecies, "name", name);
        ReflectionTestUtils.setField(fishSpecies, "category", category);
        ReflectionTestUtils.setField(fishSpecies, "active", true);

        return fishSpecies;
    }
}
