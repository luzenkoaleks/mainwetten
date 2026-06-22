package de.mainwetten.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PasswordChangeForm {

    @NotBlank(message = "Bitte gib dein aktuelles Passwort ein.")
    private String currentPassword;

    @NotBlank(message = "Bitte gib ein neues Passwort ein.")
    @Size(
            min = 8,
            max = 72,
            message = "Das Passwort muss zwischen 8 und 72 Zeichen lang sein."
    )
    private String newPassword;

    @NotBlank(message = "Bitte bestätige das neue Passwort.")
    private String confirmPassword;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
