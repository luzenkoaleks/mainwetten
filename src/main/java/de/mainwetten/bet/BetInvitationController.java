package de.mainwetten.bet;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            betInvitationService.acceptInvitation(
                    participantId,
                    authentication.getName()
            );
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute(
                    "invitationError",
                    "Die Einladung wurde nicht gefunden oder ist nicht mehr verfügbar."
            );
        }

        return "redirect:/dashboard";
    }

    @PostMapping("/{participantId}/decline")
    public String declineInvitation(
            @PathVariable Long participantId,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            betInvitationService.declineInvitation(
                    participantId,
                    authentication.getName()
            );
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute(
                    "invitationError",
                    "Die Einladung wurde nicht gefunden oder ist nicht mehr verfügbar."
            );
        }

        return "redirect:/dashboard";
    }
}