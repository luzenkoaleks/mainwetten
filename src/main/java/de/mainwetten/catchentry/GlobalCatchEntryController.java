package de.mainwetten.catchentry;

import de.mainwetten.fish.FishCategory;
import de.mainwetten.fish.FishSpeciesRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/catches")
public class GlobalCatchEntryController {

    private final GlobalCatchEntryService globalCatchEntryService;
    private final FishSpeciesRepository fishSpeciesRepository;

    public GlobalCatchEntryController(
            GlobalCatchEntryService globalCatchEntryService,
            FishSpeciesRepository fishSpeciesRepository
    ) {
        this.globalCatchEntryService = globalCatchEntryService;
        this.fishSpeciesRepository = fishSpeciesRepository;
    }

    @GetMapping("/new")
    public String showCreateForm(
            Authentication authentication,
            Model model
    ) {
        if (!model.containsAttribute("globalCatchForm")) {
            model.addAttribute("globalCatchForm", new GlobalCatchForm());
        }

        addFormAttributes(model, authentication.getName());

        return "catches/global-new";
    }

    @PostMapping
    public String createCatchEntry(
            @Valid @ModelAttribute("globalCatchForm") GlobalCatchForm form,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            addFormAttributes(model, authentication.getName());
            return "catches/global-new";
        }

        try {
            int assignmentCount = globalCatchEntryService.createGlobalCatchEntry(
                    authentication.getName(),
                    form
            );

            redirectAttributes.addFlashAttribute(
                    "catchSuccess",
                    "Fang wurde in " + assignmentCount + " Wette(n) eingetragen."
            );

            return "redirect:/dashboard";
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("globalCatchError", exception.getMessage());
            addFormAttributes(model, authentication.getName());
            return "catches/global-new";
        }
    }

    private void addFormAttributes(Model model, String username) {
        model.addAttribute(
                "freshwaterFishSpecies",
                fishSpeciesRepository.findByCategoryAndActiveTrueOrderByNameAsc(FishCategory.FRESHWATER)
        );

        model.addAttribute(
                "saltwaterFishSpecies",
                fishSpeciesRepository.findByCategoryAndActiveTrueOrderByNameAsc(FishCategory.SALTWATER)
        );

        model.addAttribute(
                "eligibleBets",
                globalCatchEntryService.getEligibleBetOptions(username)
        );
    }
}
