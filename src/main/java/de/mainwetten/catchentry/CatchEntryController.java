package de.mainwetten.catchentry;

import de.mainwetten.bet.Bet;
import de.mainwetten.bet.BetParticipant;
import de.mainwetten.bet.BetParticipantRepository;
import de.mainwetten.bet.ParticipantStatus;
import de.mainwetten.fish.FishSpeciesRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/bets/{betId}/catches")
public class CatchEntryController {

    private final CatchEntryService catchEntryService;
    private final BetParticipantRepository betParticipantRepository;
    private final FishSpeciesRepository fishSpeciesRepository;
    private final CatchEntryWindowService catchEntryWindowService;

    public CatchEntryController(
            CatchEntryService catchEntryService,
            BetParticipantRepository betParticipantRepository,
            FishSpeciesRepository fishSpeciesRepository,
            CatchEntryWindowService catchEntryWindowService
    ) {
        this.catchEntryService = catchEntryService;
        this.betParticipantRepository = betParticipantRepository;
        this.fishSpeciesRepository = fishSpeciesRepository;
        this.catchEntryWindowService = catchEntryWindowService;
    }

    @GetMapping("/new")
    public String showCreateForm(
            @PathVariable Long betId,
            Authentication authentication,
            Model model
    ) {
        BetParticipant participation = getCurrentUserParticipationOr404(betId, authentication.getName());

        if (!catchEntryWindowService.canEnterCatch(participation.getBet())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, catchEntryWindowService.getCatchEntryNotice(participation.getBet()));
        }

        model.addAttribute("bet", participation.getBet());
        model.addAttribute("catchForm", new CatchForm());
        model.addAttribute("fishSpecies", fishSpeciesRepository.findByActiveTrueOrderByNameAsc());

        return "catches/new";
    }

    @PostMapping
    public String createCatchEntry(
            @PathVariable Long betId,
            @Valid @ModelAttribute("catchForm") CatchForm form,
            BindingResult bindingResult,
            Authentication authentication,
            Model model
    ) {
        BetParticipant participation = getCurrentUserParticipationOr404(betId, authentication.getName());
        Bet bet = participation.getBet();

        if (!catchEntryWindowService.canEnterCatch(bet)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, catchEntryWindowService.getCatchEntryNotice(bet));
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("bet", bet);
            model.addAttribute("fishSpecies", fishSpeciesRepository.findByActiveTrueOrderByNameAsc());
            return "catches/new";
        }

        catchEntryService.createCatchEntry(betId, authentication.getName(), form);
        return "redirect:/bets/" + betId;
    }

    private BetParticipant getCurrentUserParticipationOr404(Long betId, String username) {
        return betParticipantRepository
                .findByBetIdAndUserUsernameAndStatus(
                        betId,
                        username,
                        ParticipantStatus.ACCEPTED
                )
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
