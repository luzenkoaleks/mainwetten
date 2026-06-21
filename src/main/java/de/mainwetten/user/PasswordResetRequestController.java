package de.mainwetten.user;

import de.mainwetten.security.ratelimit.PublicFormRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PasswordResetRequestController {

    private static final String NEUTRAL_SUCCESS_MESSAGE =
            "Falls für diese E-Mail-Adresse ein bestätigtes Konto existiert, wurde ein Link zum Zurücksetzen des Passworts versendet.";

    private final PasswordResetRequestService requestService;
    private final PublicFormRateLimiter rateLimiter;

    public PasswordResetRequestController(
            PasswordResetRequestService requestService,
            PublicFormRateLimiter rateLimiter
    ) {
        this.requestService = requestService;
        this.rateLimiter = rateLimiter;
    }

    @GetMapping("/forgot-password")
    public String showRequestForm(Model model) {
        if (!model.containsAttribute(
                "passwordResetRequestForm"
        )) {
            model.addAttribute(
                    "passwordResetRequestForm",
                    new PasswordResetRequestForm()
            );
        }

        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String requestPasswordReset(
            @Valid
            @ModelAttribute("passwordResetRequestForm")
            PasswordResetRequestForm form,
            BindingResult bindingResult,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "forgot-password";
        }

        if (!rateLimiter.tryConsumePasswordResetRequest(
                request.getRemoteAddr()
        )) {
            redirectAttributes.addFlashAttribute(
                    "requestSuccess",
                    NEUTRAL_SUCCESS_MESSAGE
            );

            return "redirect:/forgot-password";
        }

        try {
            requestService.requestResetIfEligible(
                    form.getEmail()
            );
        } catch (MailException exception) {
            bindingResult.reject(
                    "passwordReset.mailError",
                    "Die Anfrage konnte momentan nicht verarbeitet werden. Bitte versuche es später erneut."
            );

            return "forgot-password";
        }

        redirectAttributes.addFlashAttribute(
                "requestSuccess",
                NEUTRAL_SUCCESS_MESSAGE
        );

        return "redirect:/forgot-password";
    }
}
