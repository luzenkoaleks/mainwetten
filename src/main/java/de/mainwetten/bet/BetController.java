package de.mainwetten.bet;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/bets")
public class BetController {

    private final BetService betService;
    private final BetParticipantRepository betParticipantRepository;

    public BetController(
            BetService betService,
            BetParticipantRepository betParticipantRepository
    ) {
        this.betService = betService;
        this.betParticipantRepository = betParticipantRepository;
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

        model.addAttribute("bet", currentUserParticipation.getBet());
        model.addAttribute("participants", betParticipantRepository.findByBetIdOrderByUserUsernameAsc(id));

        return "bets/detail";
    }
}