package de.mainwetten.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Locale;

public class PasswordResetRequestForm {

    @NotBlank(message = "Bitte gib deine E-Mail-Adresse ein.")
    @Email(message = "Bitte gib eine gültige E-Mail-Adresse ein.")
    @Size(
            max = 255,
            message = "Die E-Mail-Adresse darf maximal 255 Zeichen lang sein."
    )
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email == null
                ? null
                : email.trim().toLowerCase(Locale.ROOT);
    }
}
