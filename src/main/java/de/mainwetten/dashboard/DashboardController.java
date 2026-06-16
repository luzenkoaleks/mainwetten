package de.mainwetten.dashboard;

import de.mainwetten.bet.BetParticipant;
import de.mainwetten.bet.BetParticipantRepository;
import de.mainwetten.bet.ParticipantStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
public class DashboardController {

    private final BetParticipantRepository betParticipantRepository;

    public DashboardController(BetParticipantRepository betParticipantRepository) {
        this.betParticipantRepository = betParticipantRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        LocalDate today = LocalDate.now();

        List<DashboardBetCard> betCards = betParticipantRepository
                .findByUserUsernameAndStatusOrderByBetEndDateAsc(
                        authentication.getName(),
                        ParticipantStatus.ACCEPTED
                )
                .stream()
                .map(participation -> new DashboardBetCard(participation.getBet(), today))
                .toList();

        model.addAttribute("username", authentication.getName());

        model.addAttribute(
                "activeBets",
                betCards.stream()
                        .filter(card -> "Aktiv".equals(card.getStatusLabel()))
                        .toList()
        );

        model.addAttribute(
                "upcomingBets",
                betCards.stream()
                        .filter(card -> "Kommend".equals(card.getStatusLabel()))
                        .toList()
        );

        model.addAttribute(
                "expiredBets",
                betCards.stream()
                        .filter(card -> "Abgelaufen".equals(card.getStatusLabel()))
                        .toList()
        );

        model.addAttribute(
                "invitations",
                betParticipantRepository.findByUserUsernameAndStatusOrderByBetEndDateAsc(
                        authentication.getName(),
                        ParticipantStatus.INVITED
                )
        );

        return "dashboard";
    }
}