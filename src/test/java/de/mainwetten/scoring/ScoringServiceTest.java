package de.mainwetten.scoring;

import de.mainwetten.bet.Bet;
import de.mainwetten.bet.BetParticipant;
import de.mainwetten.bet.BetParticipantRepository;
import de.mainwetten.bet.ParticipantStatus;
import de.mainwetten.bet.ScoringMode;
import de.mainwetten.catchentry.CatchEntry;
import de.mainwetten.catchentry.CatchEntryRepository;
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
    private final CatchEntryRepository catchEntryRepository = mock(CatchEntryRepository.class);

    private final ScoringService scoringService = new ScoringService(
            betParticipantRepository,
            catchEntryRepository
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

        when(catchEntryRepository.findByBetIdOrderByCaughtAtDescCreatedAtDesc(1L))
                .thenReturn(List.of(
                        catchEntry(hans, wels, "85.0"),
                        catchEntry(hans, aal, "100.0"),
                        catchEntry(hans, zander, "55.0"),
                        catchEntry(hans, barsch, "47.0"),

                        catchEntry(testuser, aal, "50.0"),
                        catchEntry(testuser, aal, "65.0"),
                        catchEntry(testuser, zander, "65.0"),
                        catchEntry(testuser, barsch, "46.0")
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

        when(catchEntryRepository.findByBetIdOrderByCaughtAtDescCreatedAtDesc(1L))
                .thenReturn(List.of(
                        catchEntry(hans, wels, "85.0"),
                        catchEntry(hans, aal, "100.0"),
                        catchEntry(hans, zander, "55.0"),
                        catchEntry(hans, barsch, "47.0"),

                        catchEntry(testuser, aal, "50.0"),
                        catchEntry(testuser, aal, "65.0"),
                        catchEntry(testuser, zander, "65.0"),
                        catchEntry(testuser, barsch, "46.0")
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

        when(catchEntryRepository.findByBetIdOrderByCaughtAtDescCreatedAtDesc(1L))
                .thenReturn(List.of(
                        catchEntry(hans, aal, "100.0"),
                        catchEntry(hans, barsch, "50.0"),

                        catchEntry(testuser, aal, "90.0"),
                        catchEntry(testuser, barsch, "60.0")
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

        when(catchEntryRepository.findByBetIdOrderByCaughtAtDescCreatedAtDesc(1L))
                .thenReturn(List.of(
                        catchEntry(hans, aal, "100.0"),
                        catchEntry(hans, barsch, "50.0"),
                        catchEntry(hans, wels, "80.0"),

                        catchEntry(testuser, aal, "90.0"),
                        catchEntry(testuser, barsch, "60.0"),
                        catchEntry(testuser, hecht, "70.0")
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
        ReflectionTestUtils.setField(fishSpecies, "basePoints", 0);
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

    private static CatchEntry catchEntry(AppUser user, FishSpecies fishSpecies, String lengthCm) {
        CatchEntry catchEntry = new CatchEntry();
        catchEntry.setUser(user);
        catchEntry.setFishSpecies(fishSpecies);
        catchEntry.setLengthCm(new BigDecimal(lengthCm));
        catchEntry.setCaughtAt(OffsetDateTime.now());
        return catchEntry;
    }

    private static LeaderboardEntry entryFor(List<LeaderboardEntry> leaderboard, String username) {
        return leaderboard.stream()
                .filter(entry -> entry.getUsername().equals(username))
                .findFirst()
                .orElseThrow();
    }
}
