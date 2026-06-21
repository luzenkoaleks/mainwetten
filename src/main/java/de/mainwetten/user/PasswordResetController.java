package de.mainwetten.user;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Controller
public class PasswordResetController {

    private final PasswordResetCompletionService completionService;

    public PasswordResetController(
            PasswordResetCompletionService completionService
    ) {
        this.completionService = completionService;
    }

    @GetMapping("/reset-password")
    public String showResetForm(
            @RequestParam(required = false) String token,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        PasswordResetTokenStatus status =
                completionService.inspectToken(token);

        if (status == PasswordResetTokenStatus.EXPIRED) {
            redirectAttributes.addFlashAttribute(
                    "passwordResetExpired",
                    "Der Link zum Zurücksetzen des Passworts ist abgelaufen."
            );

            return "redirect:/login";
        }

        if (status != PasswordResetTokenStatus.VALID) {
            redirectAttributes.addFlashAttribute(
                    "passwordResetInvalid",
                    "Der Link zum Zurücksetzen des Passworts ist ungültig oder wurde bereits verwendet."
            );

            return "redirect:/login";
        }

        if (!model.containsAttribute("passwordResetForm")) {
            PasswordResetForm form =
                    new PasswordResetForm();

            form.setToken(token);

            model.addAttribute(
                    "passwordResetForm",
                    form
            );
        }

        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(
            @Valid
            @ModelAttribute("passwordResetForm")
            PasswordResetForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
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
                        .getBytes(StandardCharsets.UTF_8)
                        .length > 72) {
            bindingResult.rejectValue(
                    "password",
                    "password.tooLongForBcrypt",
                    "Das Passwort ist aufgrund enthaltener Sonderzeichen technisch zu lang."
            );
        }

        if (bindingResult.hasErrors()) {
            return "reset-password";
        }

        PasswordResetResult result;

        try {
            result = completionService.resetPassword(
                    form.getToken(),
                    form.getPassword()
            );
        } catch (IllegalArgumentException exception) {
            bindingResult.reject(
                    "password.invalid",
                    exception.getMessage()
            );

            return "reset-password";
        }

        if (result == PasswordResetResult.EXPIRED) {
            redirectAttributes.addFlashAttribute(
                    "passwordResetExpired",
                    "Der Link zum Zurücksetzen des Passworts ist abgelaufen."
            );

            return "redirect:/login";
        }

        if (result != PasswordResetResult.RESET) {
            redirectAttributes.addFlashAttribute(
                    "passwordResetInvalid",
                    "Der Link zum Zurücksetzen des Passworts ist ungültig oder wurde bereits verwendet."
            );

            return "redirect:/login";
        }

        redirectAttributes.addFlashAttribute(
                "passwordResetSuccess",
                "Dein Passwort wurde erfolgreich geändert. Du kannst dich jetzt mit dem neuen Passwort einloggen."
        );

        return "redirect:/login";
    }
}
