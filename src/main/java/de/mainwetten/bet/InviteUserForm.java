package de.mainwetten.bet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class InviteUserForm {

    @NotBlank(message = "Bitte gib einen Benutzernamen ein.")
    @Size(max = 50, message = "Der Benutzername darf maximal 50 Zeichen lang sein.")
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
