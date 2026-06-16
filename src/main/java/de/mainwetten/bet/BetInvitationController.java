package de.mainwetten.bet;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/invitations")
public class BetInvitationController {

    private final BetInvitationService betInvitationService;

    public BetInvitationController(BetInvitationService betInvitationService) {
        this.betInvitationService = betInvitationService;
    }

    @PostMapping("/{participantId}/accept")
    public String acceptInvitation(
            @PathVariable Long participantId,
            Authentication authentication
    ) {
        betInvitationService.acceptInvitation(participantId, authentication.getName());
        return "redirect:/dashboard";
    }

    @PostMapping("/{participantId}/decline")
    public String declineInvitation(
            @PathVariable Long participantId,
            Authentication authentication
    ) {
        betInvitationService.declineInvitation(participantId, authentication.getName());
        return "redirect:/dashboard";
    }
}
