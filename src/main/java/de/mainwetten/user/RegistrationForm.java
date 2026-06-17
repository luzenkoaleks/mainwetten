package de.mainwetten.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegistrationForm {

    @NotBlank(message = "Bitte gib einen Benutzernamen ein.")
    @Size(min = 3, max = 50, message = "Der Benutzername muss zwischen 3 und 50 Zeichen lang sein.")
    @Pattern(
            regexp = "^[A-Za-z0-9._-]+$",
            message = "Der Benutzername darf nur Buchstaben, Zahlen, Punkt, Unterstrich und Bindestrich enthalten."
    )
    private String username;

    @NotBlank(message = "Bitte gib eine E-Mail-Adresse ein.")
    @Email(message = "Bitte gib eine gültige E-Mail-Adresse ein.")
    @Size(max = 255, message = "Die E-Mail-Adresse darf maximal 255 Zeichen lang sein.")
    private String email;

    @NotBlank(message = "Bitte gib ein Passwort ein.")
    @Size(min = 8, max = 72, message = "Das Passwort muss zwischen 8 und 72 Zeichen lang sein.")
    private String password;

    @NotBlank(message = "Bitte wiederhole dein Passwort.")
    @Size(min = 8, max = 72, message = "Die Passwort-Wiederholung muss zwischen 8 und 72 Zeichen lang sein.")
    private String confirmPassword;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username == null ? null : username.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim().toLowerCase();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}