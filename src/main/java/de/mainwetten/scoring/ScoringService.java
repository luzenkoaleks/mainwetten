package de.mainwetten.scoring;

import de.mainwetten.bet.BetParticipant;
import de.mainwetten.bet.BetParticipantRepository;
import de.mainwetten.bet.ParticipantStatus;
import de.mainwetten.bet.ScoringMode;
import de.mainwetten.catchentry.CatchEntry;
import de.mainwetten.catchentry.CatchEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScoringService {

    private final BetParticipantRepository betParticipantRepository;
    private final CatchEntryRepository catchEntryRepository;

    public ScoringService(
            BetParticipantRepository betParticipantRepository,
            CatchEntryRepository catchEntryRepository
    ) {
        this.betParticipantRepository = betParticipantRepository;
        this.catchEntryRepository = catchEntryRepository;
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntry> calculateLeaderboard(Long betId, ScoringMode scoringMode) {
        List<BetParticipant> participants = betParticipantRepository.findByBetIdOrderByUserUsernameAsc(betId);

        Map<Long, LeaderboardEntry> leaderboardByUserId = new LinkedHashMap<>();

        for (BetParticipant participant : participants) {
            if (participant.getStatus() == ParticipantStatus.ACCEPTED) {
                leaderboardByUserId.put(
                        participant.getUser().getId(),
                        new LeaderboardEntry(
                                participant.getUser().getId(),
                                participant.getUser().getUsername()
                        )
                );
            }
        }

        List<CatchEntry> catchEntries = catchEntryRepository.findByBetIdOrderByCaughtAtDescCreatedAtDesc(betId)
                .stream()
                .filter(entry -> leaderboardByUserId.containsKey(entry.getUser().getId()))
                .toList();

        Map<Long, Set<Long>> speciesIdsByUserId = new HashMap<>();
        Map<Long, Map<Long, BigDecimal>> bestLengthByUserIdAndSpeciesId = new HashMap<>();

        for (CatchEntry entry : catchEntries) {
            Long userId = entry.getUser().getId();
            Long speciesId = entry.getFishSpecies().getId();

            speciesIdsByUserId
                    .computeIfAbsent(userId, ignored -> new HashSet<>())
                    .add(speciesId);

            bestLengthByUserIdAndSpeciesId
                    .computeIfAbsent(userId, ignored -> new HashMap<>())
                    .merge(speciesId, entry.getLengthCm(), ScoringService::max);
        }

        awardCaughtSpeciesPoints(leaderboardByUserId, speciesIdsByUserId);

        Map<Long, List<CatchEntry>> entriesBySpeciesId = catchEntries.stream()
                .collect(Collectors.groupingBy(entry -> entry.getFishSpecies().getId()));

        for (List<CatchEntry> speciesEntries : entriesBySpeciesId.values()) {
            awardBiggestFishPoints(speciesEntries, leaderboardByUserId);

            if (scoringMode == ScoringMode.TOTAL_POINTS) {
                awardMostFishPoints(speciesEntries, leaderboardByUserId);
            }
        }

        awardDiversityPointOnlyForSingleWinner(leaderboardByUserId);

        calculateTieBreakerLengths(leaderboardByUserId, bestLengthByUserIdAndSpeciesId);

        List<LeaderboardEntry> leaderboard = new ArrayList<>(leaderboardByUserId.values());

        leaderboard.sort(
                Comparator.comparingInt(LeaderboardEntry::getPoints).reversed()
                        .thenComparing(LeaderboardEntry::getTieBreakerLength, Comparator.reverseOrder())
                        .thenComparing(LeaderboardEntry::getUsername)
        );

        assignRanks(leaderboard);

        return leaderboard;
    }

    private void awardCaughtSpeciesPoints(
            Map<Long, LeaderboardEntry> leaderboardByUserId,
            Map<Long, Set<Long>> speciesIdsByUserId
    ) {
        for (LeaderboardEntry entry : leaderboardByUserId.values()) {
            int speciesCount = speciesIdsByUserId
                    .getOrDefault(entry.getUserId(), Set.of())
                    .size();

            entry.setSpeciesCount(speciesCount);
            entry.addCaughtSpeciesPoints(speciesCount);
        }
    }

    private void awardBiggestFishPoints(
            List<CatchEntry> speciesEntries,
            Map<Long, LeaderboardEntry> leaderboardByUserId
    ) {
        BigDecimal maxLength = speciesEntries.stream()
                .map(CatchEntry::getLengthCm)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        Set<Long> usersWithMaxLength = speciesEntries.stream()
                .filter(entry -> entry.getLengthCm().compareTo(maxLength) == 0)
                .map(entry -> entry.getUser().getId())
                .collect(Collectors.toSet());

        for (Long userId : usersWithMaxLength) {
            leaderboardByUserId.get(userId).addBiggestFishPoint();
        }
    }

    private void awardMostFishPoints(
            List<CatchEntry> speciesEntries,
            Map<Long, LeaderboardEntry> leaderboardByUserId
    ) {
        Map<Long, Long> catchCountByUserId = speciesEntries.stream()
                .collect(Collectors.groupingBy(
                        entry -> entry.getUser().getId(),
                        Collectors.counting()
                ));

        long maxCount = catchCountByUserId.values()
                .stream()
                .max(Long::compareTo)
                .orElse(0L);

        for (Map.Entry<Long, Long> entry : catchCountByUserId.entrySet()) {
            if (entry.getValue() == maxCount) {
                leaderboardByUserId.get(entry.getKey()).addMostFishPoint();
            }
        }
    }

    private void awardDiversityPointOnlyForSingleWinner(Map<Long, LeaderboardEntry> leaderboardByUserId) {
        int maxSpeciesCount = leaderboardByUserId.values()
                .stream()
                .mapToInt(LeaderboardEntry::getSpeciesCount)
                .max()
                .orElse(0);

        if (maxSpeciesCount == 0) {
            return;
        }

        List<LeaderboardEntry> usersWithMaxSpeciesCount = leaderboardByUserId.values()
                .stream()
                .filter(entry -> entry.getSpeciesCount() == maxSpeciesCount)
                .toList();

        if (usersWithMaxSpeciesCount.size() == 1) {
            usersWithMaxSpeciesCount.getFirst().addDiversityPoint();
        }
    }

    private void calculateTieBreakerLengths(
            Map<Long, LeaderboardEntry> leaderboardByUserId,
            Map<Long, Map<Long, BigDecimal>> bestLengthByUserIdAndSpeciesId
    ) {
        Map<Integer, List<LeaderboardEntry>> entriesByPoints = leaderboardByUserId.values()
                .stream()
                .collect(Collectors.groupingBy(LeaderboardEntry::getPoints));

        for (List<LeaderboardEntry> tiedEntries : entriesByPoints.values()) {
            if (tiedEntries.size() == 1) {
                LeaderboardEntry entry = tiedEntries.getFirst();
                entry.setTieBreakerRelevant(false);
                entry.setTieBreakerLength(BigDecimal.ZERO);
                continue;
            }

            Set<Long> commonSpeciesIds = null;

            for (LeaderboardEntry entry : tiedEntries) {
                Set<Long> speciesIds = bestLengthByUserIdAndSpeciesId
                        .getOrDefault(entry.getUserId(), Map.of())
                        .keySet();

                if (commonSpeciesIds == null) {
                    commonSpeciesIds = new HashSet<>(speciesIds);
                } else {
                    commonSpeciesIds.retainAll(speciesIds);
                }
            }

            if (commonSpeciesIds == null) {
                commonSpeciesIds = Set.of();
            }

            for (LeaderboardEntry entry : tiedEntries) {
                Map<Long, BigDecimal> bestLengthsBySpeciesId = bestLengthByUserIdAndSpeciesId
                        .getOrDefault(entry.getUserId(), Map.of());

                BigDecimal sum = BigDecimal.ZERO;

                for (Long speciesId : commonSpeciesIds) {
                    sum = sum.add(bestLengthsBySpeciesId.getOrDefault(speciesId, BigDecimal.ZERO));
                }

                entry.setTieBreakerRelevant(true);
                entry.setTieBreakerLength(sum);
            }
        }
    }

    private BigDecimal sumAllBestLengths(Map<Long, BigDecimal> bestLengthsBySpeciesId) {
        BigDecimal sum = BigDecimal.ZERO;

        for (BigDecimal length : bestLengthsBySpeciesId.values()) {
            sum = sum.add(length);
        }

        return sum;
    }

    private void assignRanks(List<LeaderboardEntry> leaderboard) {
        LeaderboardEntry previous = null;

        for (int index = 0; index < leaderboard.size(); index++) {
            LeaderboardEntry current = leaderboard.get(index);

            if (previous == null) {
                current.setRank(1);
            } else if (
                    current.getPoints() == previous.getPoints()
                            && current.getTieBreakerLength().compareTo(previous.getTieBreakerLength()) == 0
            ) {
                current.setRank(previous.getRank());
            } else {
                current.setRank(index + 1);
            }

            previous = current;
        }
    }

    private static BigDecimal max(BigDecimal first, BigDecimal second) {
        return first.compareTo(second) >= 0 ? first : second;
    }
}