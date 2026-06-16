package de.mainwetten.catchentry;

import de.mainwetten.bet.Bet;
import de.mainwetten.bet.BetParticipant;
import de.mainwetten.bet.BetParticipantRepository;
import de.mainwetten.bet.ParticipantStatus;
import de.mainwetten.fish.FishCategory;
import de.mainwetten.fish.FishSpecies;
import de.mainwetten.fish.FishSpeciesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class CatchEntryService {

    private final CatchRecordRepository catchRecordRepository;
    private final CatchAssignmentRepository catchAssignmentRepository;
    private final BetParticipantRepository betParticipantRepository;
    private final FishSpeciesRepository fishSpeciesRepository;

    public CatchEntryService(
            CatchRecordRepository catchRecordRepository,
            CatchAssignmentRepository catchAssignmentRepository,
            BetParticipantRepository betParticipantRepository,
            FishSpeciesRepository fishSpeciesRepository
    ) {
        this.catchRecordRepository = catchRecordRepository;
        this.catchAssignmentRepository = catchAssignmentRepository;
        this.betParticipantRepository = betParticipantRepository;
        this.fishSpeciesRepository = fishSpeciesRepository;
    }

    @Transactional
    public CatchRecord createCatchEntry(Long betId, String username, CatchForm form) {
        BetParticipant participation = betParticipantRepository
                .findByBetIdAndUserUsernameAndStatus(
                        betId,
                        username,
                        ParticipantStatus.ACCEPTED
                )
                .orElseThrow(() -> new IllegalArgumentException("Wette nicht gefunden oder kein Zugriff."));

        FishSpecies fishSpecies = fishSpeciesRepository.findById(form.getFishSpeciesId())
                .orElseThrow(() -> new IllegalArgumentException("Fischart nicht gefunden."));

        Bet bet = participation.getBet();

        if (!isFishAllowedForBet(bet, fishSpecies)) {
            throw new IllegalArgumentException("Diese Fischart ist für die ausgewählte Wette nicht erlaubt.");
        }

        CatchRecord catchRecord = new CatchRecord();
        catchRecord.setUser(participation.getUser());
        catchRecord.setFishSpecies(fishSpecies);
        catchRecord.setLengthCm(form.getLengthCm());
        catchRecord.setCaughtAt(OffsetDateTime.now());

        CatchRecord savedCatchRecord = catchRecordRepository.save(catchRecord);

        CatchAssignment catchAssignment = new CatchAssignment();
        catchAssignment.setCatchRecord(savedCatchRecord);
        catchAssignment.setBet(bet);

        catchAssignmentRepository.save(catchAssignment);

        return savedCatchRecord;
    }

    private boolean isFishAllowedForBet(Bet bet, FishSpecies fishSpecies) {
        return bet.getFishCategory() == FishCategory.ALL
                || bet.getFishCategory() == fishSpecies.getCategory();
    }
}