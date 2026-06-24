package de.mainwetten.catchentry;

import de.mainwetten.bet.Bet;
import de.mainwetten.bet.BetParticipant;
import de.mainwetten.bet.BetParticipantRepository;
import de.mainwetten.bet.ParticipantStatus;
import de.mainwetten.fish.FishCategory;
import de.mainwetten.fish.FishSpecies;
import de.mainwetten.fish.FishSpeciesRepository;
import de.mainwetten.user.AppUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import de.mainwetten.security.usage.CatchUsageLimitService;

@Service
public class GlobalCatchEntryService {

    private final CatchRecordRepository catchRecordRepository;
    private final CatchAssignmentRepository catchAssignmentRepository;
    private final BetParticipantRepository betParticipantRepository;
    private final FishSpeciesRepository fishSpeciesRepository;
    private final CatchEntryWindowService catchEntryWindowService;
    private final CatchUsageLimitService catchUsageLimitService;


    public GlobalCatchEntryService(
            CatchRecordRepository catchRecordRepository,
            CatchAssignmentRepository catchAssignmentRepository,
            BetParticipantRepository betParticipantRepository,
            FishSpeciesRepository fishSpeciesRepository,
            CatchEntryWindowService catchEntryWindowService,
            CatchUsageLimitService catchUsageLimitService
    ) {
        this.catchRecordRepository = catchRecordRepository;
        this.catchAssignmentRepository = catchAssignmentRepository;
        this.betParticipantRepository = betParticipantRepository;
        this.fishSpeciesRepository = fishSpeciesRepository;
        this.catchEntryWindowService = catchEntryWindowService;
        this.catchUsageLimitService = catchUsageLimitService;
    }

    @Transactional(readOnly = true)
    public List<EligibleBetOption> getEligibleBetOptions(String username) {
        return betParticipantRepository
                .findByUserUsernameAndStatusOrderByBetEndDateAsc(username, ParticipantStatus.ACCEPTED)
                .stream()
                .map(BetParticipant::getBet)
                .filter(catchEntryWindowService::canEnterCatch)
                .map(bet -> new EligibleBetOption(
                        bet.getId(),
                        bet.getTitle(),
                        bet.getStartDate(),
                        bet.getEndDate(),
                        bet.getFishCategory()
                ))
                .toList();
    }

    @Transactional
    public GlobalCatchEntryResult createGlobalCatchEntry(
            String username,
            GlobalCatchForm form
    ) {
        if (form.getBetIds() == null || form.getBetIds().isEmpty()) {
            throw new IllegalArgumentException("Bitte wähle mindestens eine Wette aus.");
        }

        FishSpecies fishSpecies = fishSpeciesRepository
                .findByIdAndActiveTrue(form.getFishSpeciesId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Fischart nicht gefunden oder nicht mehr verfügbar."
                ));

        List<Long> distinctBetIds = form.getBetIds()
                .stream()
                .distinct()
                .toList();

        List<Bet> selectedBets = new ArrayList<>();
        AppUser user = null;

        for (Long betId : distinctBetIds) {
            BetParticipant participation = betParticipantRepository
                    .findByBetIdAndUserUsernameAndStatus(
                            betId,
                            username,
                            ParticipantStatus.ACCEPTED
                    )
                    .orElseThrow(() -> new IllegalArgumentException("Eine ausgewählte Wette wurde nicht gefunden oder du hast keinen Zugriff."));

            Bet bet = participation.getBet();

            if (!catchEntryWindowService.canEnterCatch(bet)) {
                throw new IllegalArgumentException("Für eine ausgewählte Wette können aktuell keine Fänge eingetragen werden.");
            }

            if (!isFishAllowedForBet(bet, fishSpecies)) {
                throw new IllegalArgumentException("Die gewählte Fischart passt nicht zu allen ausgewählten Wetten.");
            }

            if (user == null) {
                user = participation.getUser();
            }

            selectedBets.add(bet);
        }

        AppUser lockedUser =
                catchUsageLimitService
                        .lockUserAndCheckLimit(
                                user.getId()
                        );

        CatchRecord catchRecord = new CatchRecord();
        catchRecord.setUser(lockedUser);
        catchRecord.setFishSpecies(fishSpecies);
        catchRecord.setLengthCm(form.getLengthCm());
        catchRecord.setCaughtAt(OffsetDateTime.now());

        CatchRecord savedCatchRecord = catchRecordRepository.save(catchRecord);

        for (Bet bet : selectedBets) {
            CatchAssignment assignment = new CatchAssignment();
            assignment.setCatchRecord(savedCatchRecord);
            assignment.setBet(bet);
            catchAssignmentRepository.save(assignment);
        }

        return new GlobalCatchEntryResult(
                fishSpecies.getName(),
                savedCatchRecord.getLengthCm(),
                selectedBets.stream()
                        .map(Bet::getTitle)
                        .toList()
        );
    }

    private boolean isFishAllowedForBet(Bet bet, FishSpecies fishSpecies) {
        return bet.getFishCategory() == FishCategory.ALL
                || bet.getFishCategory() == fishSpecies.getCategory();
    }
}
