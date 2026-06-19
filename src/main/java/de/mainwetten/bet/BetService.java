package de.mainwetten.bet;

import de.mainwetten.user.AppUser;
import de.mainwetten.user.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BetService {

    private final BetRepository betRepository;
    private final BetParticipantRepository betParticipantRepository;
    private final AppUserRepository appUserRepository;

    public BetService(
            BetRepository betRepository,
            BetParticipantRepository betParticipantRepository,
            AppUserRepository appUserRepository
    ) {
        this.betRepository = betRepository;
        this.betParticipantRepository = betParticipantRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public Bet createBet(BetForm form, String username) {
        AppUser creator = appUserRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new IllegalArgumentException("User nicht gefunden"));

        Bet bet = new Bet();
        bet.setTitle(form.getTitle().trim());
        bet.setDescription(form.getDescription() == null ? null : form.getDescription().trim());
        bet.setStartDate(form.getStartDate());
        bet.setEndDate(form.getEndDate());
        bet.setScoringMode(form.getScoringMode());
        bet.setFishCategory(form.getFishCategory());
        bet.setCreatedBy(creator);

        Bet savedBet = betRepository.save(bet);

        BetParticipant participant = new BetParticipant();
        participant.setBet(savedBet);
        participant.setUser(creator);
        participant.setStatus(ParticipantStatus.ACCEPTED);

        betParticipantRepository.save(participant);

        return savedBet;
    }
}
