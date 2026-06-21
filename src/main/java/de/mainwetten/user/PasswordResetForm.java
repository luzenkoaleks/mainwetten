package de.mainwetten.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PasswordResetForm {

    @NotBlank
    private String token;

    @NotBlank(message = "Bitte gib ein neues Passwort ein.")
    @Size(
            min = 8,
            max = 72,
            message = "Das Passwort muss zwischen 8 und 72 Zeichen lang sein."
    )
    private String password;

    @NotBlank(message = "Bitte bestätige das neue Passwort.")
    private String confirmPassword;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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

    public void setConfirmPassword(
            String confirmPassword
    ) {
        this.confirmPassword = confirmPassword;
    }
}
