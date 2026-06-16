package de.mainwetten.bet;

import de.mainwetten.catchentry.CatchEntryWindowService;
import de.mainwetten.catchentry.CatchOverviewService;
import de.mainwetten.fish.FishCategory;
import de.mainwetten.scoring.LeaderboardEntry;
import de.mainwetten.scoring.ScoringService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/bets")
public class BetController {

    private final BetService betService;
    private final BetParticipantRepository betParticipantRepository;
    private final ScoringService scoringService;
    private final CatchOverviewService catchOverviewService;
    private final BetInvitationService betInvitationService;
    private final CatchEntryWindowService catchEntryWindowService;

    public BetController(
            BetService betService,
            BetParticipantRepository betParticipantRepository,
            ScoringService scoringService,
            CatchOverviewService catchOverviewService,
            BetInvitationService betInvitationService,
            CatchEntryWindowService catchEntryWindowService
    ) {
        this.betService = betService;
        this.betParticipantRepository = betParticipantRepository;
        this.scoringService = scoringService;
        this.catchOverviewService = catchOverviewService;
        this.betInvitationService = betInvitationService;
        this.catchEntryWindowService = catchEntryWindowService;
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("betForm", new BetForm());
        model.addAttribute("scoringModes", ScoringMode.values());
        model.addAttribute("fishCategories", FishCategory.values());
        return "bets/new";
    }

    @PostMapping
    public String createBet(
            @Valid @ModelAttribute("betForm") BetForm form,
            BindingResult bindingResult,
            Authentication authentication,
            Model model
    ) {
        if (form.getStartDate() != null
                && form.getEndDate() != null
                && form.getEndDate().isBefore(form.getStartDate())) {
            bindingResult.rejectValue(
                    "endDate",
                    "endDate.beforeStartDate",
                    "Das Enddatum darf nicht vor dem Startdatum liegen."
            );
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("scoringModes", ScoringMode.values());
            model.addAttribute("fishCategories", FishCategory.values());
            return "bets/new";
        }

        betService.createBet(form, authentication.getName());
        return "redirect:/dashboard";
    }

    @GetMapping("/{id}")
    public String showBet(
            @PathVariable Long id,
            Authentication authentication,
            Model model
    ) {
        BetParticipant currentUserParticipation = betParticipantRepository
                .findByBetIdAndUserUsernameAndStatus(
                        id,
                        authentication.getName(),
                        ParticipantStatus.ACCEPTED
                )
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Bet bet = currentUserParticipation.getBet();

        List<LeaderboardEntry> leaderboard = scoringService.calculateLeaderboard(id, bet.getScoringMode());

        model.addAttribute("bet", bet);
        model.addAttribute("catchEntryAllowed", catchEntryWindowService.canEnterCatch(bet));
        model.addAttribute("catchEntryNotice", catchEntryWindowService.getCatchEntryNotice(bet));
        model.addAttribute("participants", betParticipantRepository.findByBetIdOrderByUserUsernameAsc(id));
        model.addAttribute("catchGroups", catchOverviewService.getGroupedCatches(id));
        model.addAttribute("inviteUserForm", new InviteUserForm());
        model.addAttribute("leaderboard", leaderboard);
        model.addAttribute(
                "showTieBreaker",
                leaderboard.stream().anyMatch(LeaderboardEntry::isTieBreakerRelevant)
        );

        return "bets/detail";
    }

    @PostMapping("/{id}/participants")
    public String inviteUser(
            @PathVariable Long id,
            @ModelAttribute InviteUserForm inviteUserForm,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            betInvitationService.inviteUser(
                    id,
                    authentication.getName(),
                    inviteUserForm.getUsername()
            );

            redirectAttributes.addFlashAttribute("inviteSuccess", "Benutzer wurde eingeladen.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("inviteError", exception.getMessage());
        }

        return "redirect:/bets/" + id;
    }
}