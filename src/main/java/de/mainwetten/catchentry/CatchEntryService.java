package de.mainwetten.catchentry;

import de.mainwetten.bet.Bet;
import de.mainwetten.bet.BetParticipant;
import de.mainwetten.bet.BetParticipantRepository;
import de.mainwetten.bet.ParticipantStatus;
import de.mainwetten.fish.FishSpecies;
import de.mainwetten.fish.FishSpeciesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;

@Service
public class CatchEntryService {

    private final CatchEntryRepository catchEntryRepository;
    private final BetParticipantRepository betParticipantRepository;
    private final FishSpeciesRepository fishSpeciesRepository;

    public CatchEntryService(
            CatchEntryRepository catchEntryRepository,
            BetParticipantRepository betParticipantRepository,
            FishSpeciesRepository fishSpeciesRepository
    ) {
        this.catchEntryRepository = catchEntryRepository;
        this.betParticipantRepository = betParticipantRepository;
        this.fishSpeciesRepository = fishSpeciesRepository;
    }

    @Transactional
    public CatchEntry createCatchEntry(Long betId, String username, CatchForm form) {
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

        CatchEntry catchEntry = new CatchEntry();
        catchEntry.setBet(bet);
        catchEntry.setUser(participation.getUser());
        catchEntry.setFishSpecies(fishSpecies);
        catchEntry.setLengthCm(form.getLengthCm());
        catchEntry.setCaughtAt(form.getCaughtDate().atStartOfDay().atOffset(ZoneOffset.UTC));

        return catchEntryRepository.save(catchEntry);
    }
}
