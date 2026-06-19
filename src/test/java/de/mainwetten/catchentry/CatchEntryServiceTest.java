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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatchEntryServiceTest {

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

    @InjectMocks
    private CatchEntryService catchEntryService;

    @Test
    void createCatchEntry_rejectsUserWithoutAcceptedParticipation() {
        CatchForm form = createCatchForm(5L, "42.5");

        when(betParticipantRepository
                .findByBetIdAndUserUsernameAndStatus(
                        10L,
                        "Alice",
                        ParticipantStatus.ACCEPTED
                ))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> catchEntryService.createCatchEntry(
                        10L,
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
    void createCatchEntry_rejectsCatchOutsideEntryWindow() {
        AppUser user = createUser(1L, "Alice");
        Bet bet = createBet(10L, FishCategory.FRESHWATER);
        BetParticipant participation = createParticipation(bet, user);

        CatchForm form = createCatchForm(5L, "42.5");

        when(betParticipantRepository
                .findByBetIdAndUserUsernameAndStatus(
                        10L,
                        "Alice",
                        ParticipantStatus.ACCEPTED
                ))
                .thenReturn(Optional.of(participation));

        when(catchEntryWindowService.canEnterCatch(bet))
                .thenReturn(false);

        when(catchEntryWindowService.getCatchEntryNotice(bet))
                .thenReturn("Für diese Wette können keine Fänge eingetragen werden.");

        assertThrows(
                IllegalArgumentException.class,
                () -> catchEntryService.createCatchEntry(
                        10L,
                        "Alice",
                        form
                )
        );

        verify(fishSpeciesRepository, never())
                .findByIdAndActiveTrue(5L);

        verify(catchRecordRepository, never())
                .save(any(CatchRecord.class));
    }

    @Test
    void createCatchEntry_rejectsInactiveOrUnknownFishSpecies() {
        AppUser user = createUser(1L, "Alice");
        Bet bet = createBet(10L, FishCategory.FRESHWATER);
        BetParticipant participation = createParticipation(bet, user);

        CatchForm form = createCatchForm(5L, "42.5");

        when(betParticipantRepository
                .findByBetIdAndUserUsernameAndStatus(
                        10L,
                        "Alice",
                        ParticipantStatus.ACCEPTED
                ))
                .thenReturn(Optional.of(participation));

        when(catchEntryWindowService.canEnterCatch(bet))
                .thenReturn(true);

        when(fishSpeciesRepository.findByIdAndActiveTrue(5L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> catchEntryService.createCatchEntry(
                        10L,
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
    void createCatchEntry_rejectsFishSpeciesFromWrongCategory() {
        AppUser user = createUser(1L, "Alice");
        Bet bet = createBet(10L, FishCategory.FRESHWATER);
        BetParticipant participation = createParticipation(bet, user);

        FishSpecies saltwaterFish = createFishSpecies(
                5L,
                "Dorsch",
                FishCategory.SALTWATER,
                true
        );

        CatchForm form = createCatchForm(5L, "42.5");

        when(betParticipantRepository
                .findByBetIdAndUserUsernameAndStatus(
                        10L,
                        "Alice",
                        ParticipantStatus.ACCEPTED
                ))
                .thenReturn(Optional.of(participation));

        when(catchEntryWindowService.canEnterCatch(bet))
                .thenReturn(true);

        when(fishSpeciesRepository.findByIdAndActiveTrue(5L))
                .thenReturn(Optional.of(saltwaterFish));

        assertThrows(
                IllegalArgumentException.class,
                () -> catchEntryService.createCatchEntry(
                        10L,
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
    void createCatchEntry_allowsFishSpeciesForAllCategoryBet() {
        AppUser user = createUser(1L, "Alice");
        Bet bet = createBet(10L, FishCategory.ALL);
        BetParticipant participation = createParticipation(bet, user);

        FishSpecies saltwaterFish = createFishSpecies(
                5L,
                "Dorsch",
                FishCategory.SALTWATER,
                true
        );

        CatchForm form = createCatchForm(5L, "42.5");

        when(betParticipantRepository
                .findByBetIdAndUserUsernameAndStatus(
                        10L,
                        "Alice",
                        ParticipantStatus.ACCEPTED
                ))
                .thenReturn(Optional.of(participation));

        when(catchEntryWindowService.canEnterCatch(bet))
                .thenReturn(true);

        when(fishSpeciesRepository.findByIdAndActiveTrue(5L))
                .thenReturn(Optional.of(saltwaterFish));

        when(catchRecordRepository.save(any(CatchRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        catchEntryService.createCatchEntry(
                10L,
                "Alice",
                form
        );

        verify(catchRecordRepository)
                .save(any(CatchRecord.class));

        verify(catchAssignmentRepository)
                .save(any(CatchAssignment.class));
    }

    @Test
    void createCatchEntry_savesCatchForAuthenticatedParticipantAndAssignsBet() {
        AppUser user = createUser(1L, "Alice");
        Bet bet = createBet(10L, FishCategory.FRESHWATER);
        BetParticipant participation = createParticipation(bet, user);

        FishSpecies fishSpecies = createFishSpecies(
                5L,
                "Hecht",
                FishCategory.FRESHWATER,
                true
        );

        CatchForm form = createCatchForm(5L, "87.4");

        when(betParticipantRepository
                .findByBetIdAndUserUsernameAndStatus(
                        10L,
                        "Alice",
                        ParticipantStatus.ACCEPTED
                ))
                .thenReturn(Optional.of(participation));

        when(catchEntryWindowService.canEnterCatch(bet))
                .thenReturn(true);

        when(fishSpeciesRepository.findByIdAndActiveTrue(5L))
                .thenReturn(Optional.of(fishSpecies));

        when(catchRecordRepository.save(any(CatchRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CatchRecord result = catchEntryService.createCatchEntry(
                10L,
                "Alice",
                form
        );

        ArgumentCaptor<CatchRecord> catchRecordCaptor =
                ArgumentCaptor.forClass(CatchRecord.class);

        verify(catchRecordRepository)
                .save(catchRecordCaptor.capture());

        CatchRecord savedCatch = catchRecordCaptor.getValue();

        assertSame(user, savedCatch.getUser());
        assertSame(fishSpecies, savedCatch.getFishSpecies());
        assertEquals(
                new BigDecimal("87.4"),
                savedCatch.getLengthCm()
        );
        assertNotNull(savedCatch.getCaughtAt());
        assertSame(savedCatch, result);

        ArgumentCaptor<CatchAssignment> assignmentCaptor =
                ArgumentCaptor.forClass(CatchAssignment.class);

        verify(catchAssignmentRepository)
                .save(assignmentCaptor.capture());

        CatchAssignment savedAssignment = assignmentCaptor.getValue();

        assertSame(savedCatch, savedAssignment.getCatchRecord());
        assertSame(bet, savedAssignment.getBet());
    }

    private CatchForm createCatchForm(Long fishSpeciesId, String lengthCm) {
        CatchForm form = new CatchForm();
        form.setFishSpeciesId(fishSpeciesId);
        form.setLengthCm(new BigDecimal(lengthCm));
        return form;
    }

    private AppUser createUser(Long id, String username) {
        AppUser user = new AppUser();
        ReflectionTestUtils.setField(user, "id", id);
        user.setUsername(username);
        return user;
    }

    private Bet createBet(Long id, FishCategory fishCategory) {
        Bet bet = new Bet();
        ReflectionTestUtils.setField(bet, "id", id);
        bet.setFishCategory(fishCategory);
        return bet;
    }

    private BetParticipant createParticipation(Bet bet, AppUser user) {
        BetParticipant participation = new BetParticipant();
        participation.setBet(bet);
        participation.setUser(user);
        participation.setStatus(ParticipantStatus.ACCEPTED);
        return participation;
    }

    private FishSpecies createFishSpecies(
            Long id,
            String name,
            FishCategory category,
            boolean active
    ) {
        FishSpecies fishSpecies = new FishSpecies();

        ReflectionTestUtils.setField(fishSpecies, "id", id);
        ReflectionTestUtils.setField(fishSpecies, "name", name);
        ReflectionTestUtils.setField(fishSpecies, "category", category);
        ReflectionTestUtils.setField(fishSpecies, "active", active);

        return fishSpecies;
    }
}
