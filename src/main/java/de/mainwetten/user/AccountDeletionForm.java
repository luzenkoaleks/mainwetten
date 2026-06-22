package de.mainwetten.user;

import jakarta.validation.constraints.NotBlank;

public class AccountDeletionForm {

    @NotBlank(message = "Bitte gib dein aktuelles Passwort ein.")
    private String currentPassword;

    @NotBlank(message = "Bitte gib LÖSCHEN zur Bestätigung ein.")
    private String confirmation;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getConfirmation() {
        return confirmation;
    }

    public void setConfirmation(String confirmation) {
        this.confirmation = confirmation;
    }
}
