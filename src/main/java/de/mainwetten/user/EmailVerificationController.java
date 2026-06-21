package de.mainwetten.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    public EmailVerificationController(
            EmailVerificationService emailVerificationService
    ) {
        this.emailVerificationService = emailVerificationService;
    }

    @GetMapping("/verify-email")
    public String verifyEmail(
            @RequestParam(required = false) String token,
            RedirectAttributes redirectAttributes
    ) {
        EmailVerificationResult result =
                emailVerificationService.verifyToken(token);

        switch (result) {
            case VERIFIED -> redirectAttributes.addFlashAttribute(
                    "emailVerified",
                    "Deine E-Mail-Adresse wurde erfolgreich bestätigt. Du kannst dich jetzt einloggen."
            );

            case EXPIRED -> redirectAttributes.addFlashAttribute(
                    "verificationExpired",
                    "Der Bestätigungslink ist abgelaufen."
            );

            case INVALID -> redirectAttributes.addFlashAttribute(
                    "verificationInvalid",
                    "Der Bestätigungslink ist ungültig oder wurde bereits verwendet."
            );
        }

        return "redirect:/login";
    }
}
