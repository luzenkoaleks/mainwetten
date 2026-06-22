package de.mainwetten.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Controller
public class AccountSecurityController {

    private final AuthenticatedPasswordChangeService passwordChangeService;

    public AccountSecurityController(
            AuthenticatedPasswordChangeService passwordChangeService
    ) {
        this.passwordChangeService = passwordChangeService;
    }

    @GetMapping("/account/security")
    public String showSecurityPage(Model model) {
        if (!model.containsAttribute("passwordChangeForm")) {
            model.addAttribute(
                    "passwordChangeForm",
                    new PasswordChangeForm()
            );
        }

        return "account-security";
    }

    @PostMapping("/account/change-password")
    public String changePassword(
            @Valid
            @ModelAttribute("passwordChangeForm")
            PasswordChangeForm form,
            BindingResult bindingResult,
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (!Objects.equals(
                form.getNewPassword(),
                form.getConfirmPassword()
        )) {
            bindingResult.rejectValue(
                    "confirmPassword",
                    "password.mismatch",
                    "Die Passwörter stimmen nicht überein."
            );
        }

        if (!bindingResult.hasFieldErrors("newPassword")
                && form.getNewPassword() != null
                && form.getNewPassword()
                        .getBytes(StandardCharsets.UTF_8)
                        .length > 72) {
            bindingResult.rejectValue(
                    "newPassword",
                    "password.tooLongForBcrypt",
                    "Das Passwort ist aufgrund enthaltener Sonderzeichen technisch zu lang."
            );
        }

        if (bindingResult.hasErrors()) {
            return "account-security";
        }

        PasswordChangeResult result;

        try {
            result = passwordChangeService.changePassword(
                    authentication.getName(),
                    form.getCurrentPassword(),
                    form.getNewPassword()
            );
        } catch (IllegalArgumentException exception) {
            bindingResult.reject(
                    "password.invalid",
                    exception.getMessage()
            );

            return "account-security";
        }

        if (result
                == PasswordChangeResult.CURRENT_PASSWORD_INVALID) {
            bindingResult.rejectValue(
                    "currentPassword",
                    "password.currentInvalid",
                    "Das aktuelle Passwort ist nicht korrekt."
            );

            return "account-security";
        }

        if (result
                == PasswordChangeResult.NEW_PASSWORD_UNCHANGED) {
            bindingResult.rejectValue(
                    "newPassword",
                    "password.unchanged",
                    "Das neue Passwort muss sich vom bisherigen Passwort unterscheiden."
            );

            return "account-security";
        }

        new SecurityContextLogoutHandler().logout(
                request,
                response,
                authentication
        );

        return "redirect:/login?passwordChanged=true";
    }
}
