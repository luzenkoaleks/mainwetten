package de.mainwetten.user;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.springframework.mail.MailException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.mainwetten.security.ratelimit.PublicFormRateLimiter;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/register")
public class RegistrationController {

    private final AppUserRepository appUserRepository;
    private final UserRegistrationService userRegistrationService;
    private final EmailVerificationResendService emailVerificationResendService;
    private final PublicFormRateLimiter publicFormRateLimiter;

    public RegistrationController(
            AppUserRepository appUserRepository,
            UserRegistrationService userRegistrationService,
            EmailVerificationResendService emailVerificationResendService,
            PublicFormRateLimiter publicFormRateLimiter
    ) {
        this.appUserRepository = appUserRepository;
        this.userRegistrationService = userRegistrationService;
        this.emailVerificationResendService = emailVerificationResendService;
        this.publicFormRateLimiter = publicFormRateLimiter;
    }

    @GetMapping
    public String showRegistrationForm(Model model) {
        model.addAttribute("registrationForm", new RegistrationForm());
        return "register";
    }

    @GetMapping("/check-email")
    public String showCheckEmailPage() {
        return "registration-check-email";
    }

    @PostMapping
    public String register(
            @Valid @ModelAttribute("registrationForm") RegistrationForm form,
            BindingResult bindingResult,
            HttpServletRequest request
    ) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        if (!Objects.equals(
                form.getPassword(),
                form.getConfirmPassword()
        )) {
            bindingResult.rejectValue(
                    "confirmPassword",
                    "password.mismatch",
                    "Die Passwörter stimmen nicht überein."
            );
        }

        if (!bindingResult.hasFieldErrors("password")
                && form.getPassword() != null
                && form.getPassword()
                .getBytes(StandardCharsets.UTF_8).length > 72) {
            bindingResult.rejectValue(
                    "password",
                    "password.tooLongForBcrypt",
                    "Das Passwort ist aufgrund enthaltener Sonderzeichen technisch zu lang."
            );
        }

        if (bindingResult.hasErrors()) {
            return "register";
        }

        if (!publicFormRateLimiter.tryConsumeRegistration(
                request.getRemoteAddr()
        )) {
            bindingResult.reject(
                    "registration.rateLimit",
                    "Zu viele Registrierungsversuche. Bitte warte einige Minuten und versuche es anschließend erneut."
            );

            return "register";
        }

        if (appUserRepository.existsByUsernameIgnoreCase(
                form.getUsername()
        )) {
            bindingResult.rejectValue(
                    "username",
                    "username.exists",
                    "Dieser Benutzername ist bereits vergeben."
            );
        }

        if (appUserRepository.existsByEmailIgnoreCase(
                form.getEmail()
        )) {
            bindingResult.rejectValue(
                    "email",
                    "email.exists",
                    "Diese E-Mail-Adresse ist bereits registriert."
            );
        }

        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            userRegistrationService.register(form);
        } catch (DataIntegrityViolationException exception) {
            bindingResult.reject(
                    "registration.conflict",
                    "Der Benutzername oder die E-Mail-Adresse wurde inzwischen bereits registriert."
            );

            return "register";
        } catch (MailException exception) {
            bindingResult.reject(
                    "registration.mailError",
                    "Die Verifikations-E-Mail konnte momentan nicht versendet werden. Bitte versuche es später erneut."
            );

            return "register";
        }

        return "redirect:/register/check-email";
    }

    @GetMapping("/resend-verification")
    public String showResendVerificationForm(Model model) {
        if (!model.containsAttribute("resendVerificationForm")) {
            model.addAttribute(
                    "resendVerificationForm",
                    new ResendVerificationForm()
            );
        }

        return "registration-resend-verification";
    }

    @PostMapping("/resend-verification")
    public String resendVerificationEmail(
            @Valid @ModelAttribute("resendVerificationForm")
            ResendVerificationForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {
        if (bindingResult.hasErrors()) {
            return "registration-resend-verification";
        }

        if (!publicFormRateLimiter.tryConsumeVerificationResend(
                request.getRemoteAddr()
        )) {
            redirectAttributes.addFlashAttribute(
                    "resendSuccess",
                    "Falls für diese E-Mail-Adresse ein noch nicht bestätigtes Konto existiert, wurde ein neuer Bestätigungslink versendet."
            );

            return "redirect:/register/resend-verification";
        }

        try {
            emailVerificationResendService.resendIfEligible(
                    form.getEmail()
            );
        } catch (MailException exception) {
            bindingResult.reject(
                    "resend.mailError",
                    "Die E-Mail konnte momentan nicht versendet werden. Bitte versuche es später erneut."
            );

            return "registration-resend-verification";
        }

        redirectAttributes.addFlashAttribute(
                "resendSuccess",
                "Falls für diese E-Mail-Adresse ein noch nicht bestätigtes Konto existiert, wurde ein neuer Bestätigungslink versendet."
        );

        return "redirect:/register/resend-verification";
    }
}
