package de.mainwetten.scoring;

import de.mainwetten.bet.Bet;
import de.mainwetten.bet.BetParticipant;
import de.mainwetten.bet.BetParticipantRepository;
import de.mainwetten.bet.ParticipantStatus;
import de.mainwetten.bet.ScoringMode;
import de.mainwetten.catchentry.CatchAssignment;
import de.mainwetten.catchentry.CatchAssignmentRepository;
import de.mainwetten.catchentry.CatchRecord;
import de.mainwetten.fish.FishCategory;
import de.mainwetten.fish.FishSpecies;
import de.mainwetten.user.AppUser;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ScoringServiceTest {

    private final BetParticipantRepository betParticipantRepository = mock(BetParticipantRepository.class);
    private final CatchAssignmentRepository catchAssignmentRepository = mock(CatchAssignmentRepository.class);

    private final ScoringService scoringService = new ScoringService(
            betParticipantRepository,
            catchAssignmentRepository
    );

    @Test
    void totalPointsAwardsSpeciesBiggestMostAndSingleDiversityWinner() {
        Bet bet = new Bet();

        AppUser hans = user(1L, "hans");
        AppUser testuser = user(2L, "testuser");

        FishSpecies aal = fish(1L, "Aal");
        FishSpecies barsch = fish(2L, "Barsch");
        FishSpecies zander = fish(3L, "Zander");
        FishSpecies wels = fish(4L, "Wels");

        when(betParticipantRepository.findByBetIdOrderByUserUsernameAsc(1L))
                .thenReturn(List.of(
                        acceptedParticipant(bet, hans),
                        acceptedParticipant(bet, testuser)
                ));

        when(catchAssignmentRepository.findByBetIdWithDetailsOrderByCaughtAtDesc(1L))
                .thenReturn(List.of(
                        assignment(bet, catchRecord(hans, wels, "85.0")),
                        assignment(bet, catchRecord(hans, aal, "100.0")),
                        assignment(bet, catchRecord(hans, zander, "55.0")),
                        assignment(bet, catchRecord(hans, barsch, "47.0")),

                        assignment(bet, catchRecord(testuser, aal, "50.0")),
                        assignment(bet, catchRecord(testuser, aal, "65.0")),
                        assignment(bet, catchRecord(testuser, zander, "65.0")),
                        assignment(bet, catchRecord(testuser, barsch, "46.0"))
                ));

        List<LeaderboardEntry> leaderboard = scoringService.calculateLeaderboard(
                1L,
                ScoringMode.TOTAL_POINTS
        );

        LeaderboardEntry hansEntry = entryFor(leaderboard, "hans");
        LeaderboardEntry testuserEntry = entryFor(leaderboard, "testuser");

        assertThat(hansEntry.getPoints()).isEqualTo(11);
        assertThat(hansEntry.getCaughtSpeciesPoints()).isEqualTo(4);
        assertThat(hansEntry.getBiggestFishPoints()).isEqualTo(3);
        assertThat(hansEntry.getMostFishPoints()).isEqualTo(3);
        assertThat(hansEntry.getDiversityPoints()).isEqualTo(1);

        assertThat(testuserEntry.getPoints()).isEqualTo(7);
        assertThat(testuserEntry.getCaughtSpeciesPoints()).isEqualTo(3);
        assertThat(testuserEntry.getBiggestFishPoints()).isEqualTo(1);
        assertThat(testuserEntry.getMostFishPoints()).isEqualTo(3);
        assertThat(testuserEntry.getDiversityPoints()).isEqualTo(0);

        assertThat(leaderboard.getFirst().getUsername()).isEqualTo("hans");
    }

    @Test
    void bestPerSpeciesDoesNotAwardMostFishPoints() {
        Bet bet = new Bet();

        AppUser hans = user(1L, "hans");
        AppUser testuser = user(2L, "testuser");

        FishSpecies aal = fish(1L, "Aal");
        FishSpecies barsch = fish(2L, "Barsch");
        FishSpecies zander = fish(3L, "Zander");
        FishSpecies wels = fish(4L, "Wels");

        when(betParticipantRepository.findByBetIdOrderByUserUsernameAsc(1L))
                .thenReturn(List.of(
                        acceptedParticipant(bet, hans),
                        acceptedParticipant(bet, testuser)
                ));

        when(catchAssignmentRepository.findByBetIdWithDetailsOrderByCaughtAtDesc(1L))
                .thenReturn(List.of(
                        assignment(bet, catchRecord(hans, wels, "85.0")),
                        assignment(bet, catchRecord(hans, aal, "100.0")),
                        assignment(bet, catchRecord(hans, zander, "55.0")),
                        assignment(bet, catchRecord(hans, barsch, "47.0")),

                        assignment(bet, catchRecord(testuser, aal, "50.0")),
                        assignment(bet, catchRecord(testuser, aal, "65.0")),
                        assignment(bet, catchRecord(testuser, zander, "65.0")),
                        assignment(bet, catchRecord(testuser, barsch, "46.0"))
                ));

        List<LeaderboardEntry> leaderboard = scoringService.calculateLeaderboard(
                1L,
                ScoringMode.BEST_PER_SPECIES
        );

        LeaderboardEntry hansEntry = entryFor(leaderboard, "hans");
        LeaderboardEntry testuserEntry = entryFor(leaderboard, "testuser");

        assertThat(hansEntry.getPoints()).isEqualTo(8);
        assertThat(hansEntry.getCaughtSpeciesPoints()).isEqualTo(4);
        assertThat(hansEntry.getBiggestFishPoints()).isEqualTo(3);
        assertThat(hansEntry.getMostFishPoints()).isEqualTo(0);
        assertThat(hansEntry.getDiversityPoints()).isEqualTo(1);

        assertThat(testuserEntry.getPoints()).isEqualTo(4);
        assertThat(testuserEntry.getCaughtSpeciesPoints()).isEqualTo(3);
        assertThat(testuserEntry.getBiggestFishPoints()).isEqualTo(1);
        assertThat(testuserEntry.getMostFishPoints()).isEqualTo(0);
        assertThat(testuserEntry.getDiversityPoints()).isEqualTo(0);
    }

    @Test
    void diversityBonusIsNotAwardedWhenSpeciesCountIsTied() {
        Bet bet = new Bet();

        AppUser hans = user(1L, "hans");
        AppUser testuser = user(2L, "testuser");

        FishSpecies aal = fish(1L, "Aal");
        FishSpecies barsch = fish(2L, "Barsch");

        when(betParticipantRepository.findByBetIdOrderByUserUsernameAsc(1L))
                .thenReturn(List.of(
                        acceptedParticipant(bet, hans),
                        acceptedParticipant(bet, testuser)
                ));

        when(catchAssignmentRepository.findByBetIdWithDetailsOrderByCaughtAtDesc(1L))
                .thenReturn(List.of(
                        assignment(bet, catchRecord(hans, aal, "100.0")),
                        assignment(bet, catchRecord(hans, barsch, "50.0")),

                        assignment(bet, catchRecord(testuser, aal, "90.0")),
                        assignment(bet, catchRecord(testuser, barsch, "60.0"))
                ));

        List<LeaderboardEntry> leaderboard = scoringService.calculateLeaderboard(
                1L,
                ScoringMode.TOTAL_POINTS
        );

        LeaderboardEntry hansEntry = entryFor(leaderboard, "hans");
        LeaderboardEntry testuserEntry = entryFor(leaderboard, "testuser");

        assertThat(hansEntry.getSpeciesCount()).isEqualTo(2);
        assertThat(testuserEntry.getSpeciesCount()).isEqualTo(2);

        assertThat(hansEntry.getDiversityPoints()).isEqualTo(0);
        assertThat(testuserEntry.getDiversityPoints()).isEqualTo(0);
    }

    @Test
    void tieBreakerUsesOnlySpeciesCaughtByAllTiedUsers() {
        Bet bet = new Bet();

        AppUser hans = user(1L, "hans");
        AppUser testuser = user(2L, "testuser");

        FishSpecies aal = fish(1L, "Aal");
        FishSpecies barsch = fish(2L, "Barsch");
        FishSpecies wels = fish(3L, "Wels");
        FishSpecies hecht = fish(4L, "Hecht");

        when(betParticipantRepository.findByBetIdOrderByUserUsernameAsc(1L))
                .thenReturn(List.of(
                        acceptedParticipant(bet, hans),
                        acceptedParticipant(bet, testuser)
                ));

        when(catchAssignmentRepository.findByBetIdWithDetailsOrderByCaughtAtDesc(1L))
                .thenReturn(List.of(
                        assignment(bet, catchRecord(hans, aal, "100.0")),
                        assignment(bet, catchRecord(hans, barsch, "50.0")),
                        assignment(bet, catchRecord(hans, wels, "80.0")),

                        assignment(bet, catchRecord(testuser, aal, "90.0")),
                        assignment(bet, catchRecord(testuser, barsch, "60.0")),
                        assignment(bet, catchRecord(testuser, hecht, "70.0"))
                ));

        List<LeaderboardEntry> leaderboard = scoringService.calculateLeaderboard(
                1L,
                ScoringMode.BEST_PER_SPECIES
        );

        LeaderboardEntry hansEntry = entryFor(leaderboard, "hans");
        LeaderboardEntry testuserEntry = entryFor(leaderboard, "testuser");

        assertThat(hansEntry.getPoints()).isEqualTo(5);
        assertThat(testuserEntry.getPoints()).isEqualTo(5);

        assertThat(hansEntry.isTieBreakerRelevant()).isTrue();
        assertThat(testuserEntry.isTieBreakerRelevant()).isTrue();

        assertThat(hansEntry.getTieBreakerLength()).isEqualByComparingTo("150.0");
        assertThat(testuserEntry.getTieBreakerLength()).isEqualByComparingTo("150.0");
    }

    private static AppUser user(Long id, String username) {
        AppUser user = new AppUser();
        ReflectionTestUtils.setField(user, "id", id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPasswordHash("irrelevant-for-test");
        return user;
    }

    private static FishSpecies fish(Long id, String name) {
        FishSpecies fishSpecies = new FishSpecies();
        ReflectionTestUtils.setField(fishSpecies, "id", id);
        ReflectionTestUtils.setField(fishSpecies, "name", name);
        ReflectionTestUtils.setField(fishSpecies, "category", FishCategory.FRESHWATER);
        ReflectionTestUtils.setField(fishSpecies, "active", true);
        return fishSpecies;
    }

    private static BetParticipant acceptedParticipant(Bet bet, AppUser user) {
        BetParticipant participant = new BetParticipant();
        participant.setBet(bet);
        participant.setUser(user);
        participant.setStatus(ParticipantStatus.ACCEPTED);
        return participant;
    }

    private static CatchRecord catchRecord(AppUser user, FishSpecies fishSpecies, String lengthCm) {
        CatchRecord catchRecord = new CatchRecord();
        catchRecord.setUser(user);
        catchRecord.setFishSpecies(fishSpecies);
        catchRecord.setLengthCm(new BigDecimal(lengthCm));
        catchRecord.setCaughtAt(OffsetDateTime.now());
        return catchRecord;
    }

    private static CatchAssignment assignment(Bet bet, CatchRecord catchRecord) {
        CatchAssignment assignment = new CatchAssignment();
        assignment.setBet(bet);
        assignment.setCatchRecord(catchRecord);
        return assignment;
    }

    private static LeaderboardEntry entryFor(List<LeaderboardEntry> leaderboard, String username) {
        return leaderboard.stream()
                .filter(entry -> entry.getUsername().equals(username))
                .findFirst()
                .orElseThrow();
    }
}