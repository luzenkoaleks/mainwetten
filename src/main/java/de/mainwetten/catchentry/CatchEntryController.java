package de.mainwetten.catchentry;

import de.mainwetten.bet.Bet;
import de.mainwetten.bet.BetParticipant;
import de.mainwetten.bet.BetParticipantRepository;
import de.mainwetten.bet.ParticipantStatus;
import de.mainwetten.fish.FishCategory;
import de.mainwetten.fish.FishSpecies;
import de.mainwetten.fish.FishSpeciesRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

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
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    catchEntryWindowService.getCatchEntryNotice(participation.getBet())
            );
        }

        model.addAttribute("bet", participation.getBet());
        model.addAttribute("catchForm", new CatchForm());
        model.addAttribute("fishSpecies", getAllowedFishSpecies(participation.getBet()));

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
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    catchEntryWindowService.getCatchEntryNotice(bet)
            );
        }

        validateFishSpeciesForBet(form, bet, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("bet", bet);
            model.addAttribute("fishSpecies", getAllowedFishSpecies(bet));
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

    private List<FishSpecies> getAllowedFishSpecies(Bet bet) {
        if (bet.getFishCategory() == FishCategory.ALL) {
            return fishSpeciesRepository.findByActiveTrueOrderByCategoryAscNameAsc();
        }

        return fishSpeciesRepository.findByCategoryAndActiveTrueOrderByNameAsc(bet.getFishCategory());
    }

    private void validateFishSpeciesForBet(CatchForm form, Bet bet, BindingResult bindingResult) {
        if (form.getFishSpeciesId() == null) {
            return;
        }

        Optional<FishSpecies> selectedFishSpecies = fishSpeciesRepository.findById(form.getFishSpeciesId());

        if (selectedFishSpecies.isEmpty()) {
            bindingResult.rejectValue(
                    "fishSpeciesId",
                    "fishSpecies.notFound",
                    "Diese Fischart wurde nicht gefunden."
            );
            return;
        }

        FishSpecies fishSpecies = selectedFishSpecies.get();

        boolean allowed = bet.getFishCategory() == FishCategory.ALL
                || bet.getFishCategory() == fishSpecies.getCategory();

        if (!allowed) {
            bindingResult.rejectValue(
                    "fishSpeciesId",
                    "fishSpecies.notAllowed",
                    "Diese Fischart ist für diese Wette nicht erlaubt."
            );
        }
    }
}