package de.mainwetten.dashboard;

import de.mainwetten.bet.BetParticipantRepository;
import de.mainwetten.bet.ParticipantStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final BetParticipantRepository betParticipantRepository;

    public DashboardController(BetParticipantRepository betParticipantRepository) {
        this.betParticipantRepository = betParticipantRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());

        model.addAttribute(
                "participations",
                betParticipantRepository.findByUserUsernameAndStatusOrderByBetEndDateAsc(
                        authentication.getName(),
                        ParticipantStatus.ACCEPTED
                )
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