package de.mainwetten.bet;

import de.mainwetten.security.usage.UsageLimitExceededException;
import de.mainwetten.security.usage.UsageLimitProperties;
import de.mainwetten.user.AppUser;
import de.mainwetten.user.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Service
public class BetService {

    private static final ZoneId APPLICATION_ZONE =
            ZoneId.of("Europe/Berlin");

    private final BetRepository betRepository;
    private final BetParticipantRepository betParticipantRepository;
    private final AppUserRepository appUserRepository;
    private final UsageLimitProperties usageLimitProperties;

    public BetService(
            BetRepository betRepository,
            BetParticipantRepository betParticipantRepository,
            AppUserRepository appUserRepository,
            UsageLimitProperties usageLimitProperties
    ) {
        this.betRepository = betRepository;
        this.betParticipantRepository =
                betParticipantRepository;
        this.appUserRepository = appUserRepository;
        this.usageLimitProperties = usageLimitProperties;
    }

    @Transactional
    public Bet createBet(
            BetForm form,
            String username
    ) {
        AppUser creator = appUserRepository
                .findByUsernameIgnoreCase(username)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User nicht gefunden"
                        )
                );

        AppUser lockedCreator = appUserRepository
                .findByIdForUpdate(creator.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User nicht gefunden"
                        )
                );

        checkUsageLimits(lockedCreator);

        Bet bet = new Bet();
        bet.setTitle(form.getTitle().trim());
        bet.setDescription(
                form.getDescription() == null
                        ? null
                        : form.getDescription().trim()
        );
        bet.setStartDate(form.getStartDate());
        bet.setEndDate(form.getEndDate());
        bet.setScoringMode(form.getScoringMode());
        bet.setFishCategory(form.getFishCategory());
        bet.setCreatedBy(lockedCreator);

        Bet savedBet = betRepository.save(bet);

        BetParticipant participant =
                new BetParticipant();

        participant.setBet(savedBet);
        participant.setUser(lockedCreator);
        participant.setStatus(
                ParticipantStatus.ACCEPTED
        );

        betParticipantRepository.save(participant);

        return savedBet;
    }

    private void checkUsageLimits(AppUser creator) {
        OffsetDateTime createdAfter =
                OffsetDateTime
                        .now(APPLICATION_ZONE)
                        .minusHours(24);

        long recentBetCount =
                betRepository.countCreatedByUserSince(
                        creator.getId(),
                        createdAfter
                );

        int dailyLimit =
                usageLimitProperties
                        .getBetCreationsPer24Hours();

        if (recentBetCount >= dailyLimit) {
            throw new UsageLimitExceededException(
                    "Du kannst innerhalb von 24 Stunden "
                            + "maximal "
                            + dailyLimit
                            + " "
                            + betLabel(dailyLimit)
                            + " erstellen."
            );
        }

        LocalDate today =
                LocalDate.now(APPLICATION_ZONE);

        long activeBetCount =
                betRepository
                        .countActiveOrUpcomingCreatedByUser(
                                creator.getId(),
                                today
                        );

        int activeLimit =
                usageLimitProperties
                        .getMaxActiveCreatedBets();

        if (activeBetCount >= activeLimit) {
            throw new UsageLimitExceededException(
                    "Du kannst maximal "
                            + activeLimit
                            + " eigene aktive oder kommende "
                            + betLabel(activeLimit)
                            + " gleichzeitig haben."
            );
        }
    }
    private String betLabel(int number) {
        return number == 1
                ? "Wette"
                : "Wetten";
    }
}
