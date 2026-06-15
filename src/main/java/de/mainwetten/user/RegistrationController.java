package de.mainwetten.user;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/register")
public class RegistrationController {

    private final AppUserRepository appUserRepository;
    private final UserRegistrationService userRegistrationService;

    public RegistrationController(
            AppUserRepository appUserRepository,
            UserRegistrationService userRegistrationService
    ) {
        this.appUserRepository = appUserRepository;
        this.userRegistrationService = userRegistrationService;
    }

    @GetMapping
    public String showRegistrationForm(Model model) {
        model.addAttribute("registrationForm", new RegistrationForm());
        return "register";
    }

    @PostMapping
    public String register(
            @Valid @ModelAttribute("registrationForm") RegistrationForm form,
            BindingResult bindingResult
    ) {
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue(
                    "confirmPassword",
                    "password.mismatch",
                    "Die Passwörter stimmen nicht überein."
            );
        }

        if (appUserRepository.existsByUsername(form.getUsername().trim())) {
            bindingResult.rejectValue(
                    "username",
                    "username.exists",
                    "Dieser Benutzername ist bereits vergeben."
            );
        }

        if (appUserRepository.existsByEmail(form.getEmail().trim().toLowerCase())) {
            bindingResult.rejectValue(
                    "email",
                    "email.exists",
                    "Diese E-Mail-Adresse ist bereits registriert."
            );
        }

        if (bindingResult.hasErrors()) {
            return "register";
        }

        userRegistrationService.register(form);
        return "redirect:/login";
    }
}
