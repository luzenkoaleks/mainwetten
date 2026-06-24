package de.mainwetten.legal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LegalController {

    private final LegalProperties legalProperties;

    public LegalController(
            LegalProperties legalProperties
    ) {
        this.legalProperties = legalProperties;
    }

    @GetMapping("/impressum")
    public String showImprint(Model model) {
        model.addAttribute(
                "legal",
                legalProperties
        );

        return "legal/impressum";
    }

    @GetMapping("/datenschutz")
    public String showPrivacyPolicy(Model model) {
        model.addAttribute(
                "legal",
                legalProperties
        );

        return "legal/datenschutz";
    }
}
