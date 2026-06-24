package de.mainwetten.bet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class InviteUserForm {

    @NotBlank(message = "Bitte gib einen Benutzernamen ein.")
    @Size(
            min = 3,
            max = 50,
            message = "Der Benutzername muss zwischen "
                    + "3 und 50 Zeichen lang sein."
    )
    @Pattern(
            regexp = "^[A-Za-z0-9._-]+$",
            message = "Der Benutzername darf nur Buchstaben, "
                    + "Zahlen, Punkt, Unterstrich und "
                    + "Bindestrich enthalten."
    )
    private String invitedUsername;

    public String getInvitedUsername() {
        return invitedUsername;
    }

    public void setInvitedUsername(
            String invitedUsername
    ) {
        this.invitedUsername =
                invitedUsername == null
                        ? null
                        : invitedUsername.trim();
    }
}