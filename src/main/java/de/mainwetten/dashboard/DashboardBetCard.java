package de.mainwetten.dashboard;

import de.mainwetten.bet.Bet;

import java.time.LocalDate;

public class DashboardBetCard {

    private final Bet bet;
    private final String statusLabel;

    public DashboardBetCard(Bet bet, LocalDate today) {
        this.bet = bet;

        if (bet.getStartDate().isAfter(today)) {
            this.statusLabel = "Kommend";
        } else if (bet.getEndDate().isBefore(today)) {
            this.statusLabel = "Abgelaufen";
        } else {
            this.statusLabel = "Aktiv";
        }
    }

    public Bet getBet() {
        return bet;
    }

    public String getStatusLabel() {
        return statusLabel;
    }
}
