package de.mainwetten.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegistrationForm {

    @NotBlank(message = "Der Benutzername darf nicht leer sein.")
    @Size(min = 3, max = 50, message = "Der Benutzername muss zwischen 3 und 50 Zeichen lang sein.")
    private String username;

    @NotBlank(message = "Die E-Mail-Adresse darf nicht leer sein.")
    @Email(message = "Bitte gib eine gültige E-Mail-Adresse ein.")
    private String email;

    @NotBlank(message = "Das Passwort darf nicht leer sein.")
    @Size(min = 8, message = "Das Passwort muss mindestens 8 Zeichen lang sein.")
    private String password;

    @NotBlank(message = "Bitte wiederhole das Passwort.")
    private String confirmPassword;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
