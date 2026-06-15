package de.mainwetten.bet;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import de.mainwetten.catchentry.CatchEntryRepository;
import de.mainwetten.scoring.ScoringService;

@Controller
@RequestMapping("/bets")
public class BetController {

    private final BetService betService;
    private final BetParticipantRepository betParticipantRepository;
    private final CatchEntryRepository catchEntryRepository;
    private final ScoringService scoringService;

    public BetController(
            BetService betService,
            BetParticipantRepository betParticipantRepository,
            CatchEntryRepository catchEntryRepository,
            ScoringService scoringService
    ) {
        this.betService = betService;
        this.betParticipantRepository = betParticipantRepository;
        this.catchEntryRepository = catchEntryRepository;
        this.scoringService = scoringService;
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("betForm", new BetForm());
        model.addAttribute("scoringModes", ScoringMode.values());
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

        model.addAttribute("bet", bet);
        model.addAttribute("participants", betParticipantRepository.findByBetIdOrderByUserUsernameAsc(id));
        model.addAttribute("catchEntries", catchEntryRepository.findByBetIdOrderByCaughtAtDescCreatedAtDesc(id));
        model.addAttribute("leaderboard", scoringService.calculateLeaderboard(id, bet.getScoringMode()));

        return "bets/detail";
    }
}