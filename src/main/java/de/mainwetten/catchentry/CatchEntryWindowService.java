package de.mainwetten.catchentry;

import de.mainwetten.bet.Bet;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class CatchEntryWindowService {

    private static final int GRACE_PERIOD_DAYS = 2;

    public boolean canEnterCatch(Bet bet) {
        LocalDate today = LocalDate.now();

        boolean hasStarted = !today.isBefore(bet.getStartDate());
        boolean isWithinGracePeriod = !today.isAfter(bet.getEndDate().plusDays(GRACE_PERIOD_DAYS));

        return hasStarted && isWithinGracePeriod;
    }

    public String getCatchEntryNotice(Bet bet) {
        LocalDate today = LocalDate.now();

        if (today.isBefore(bet.getStartDate())) {
            return "Fänge können erst ab dem Startdatum der Wette eingetragen werden.";
        }

        if (today.isAfter(bet.getEndDate().plusDays(GRACE_PERIOD_DAYS))) {
            return "Die Nachtragefrist ist abgelaufen. Fänge konnten bis 2 Tage nach Wettende eingetragen werden.";
        }

        if (today.isAfter(bet.getEndDate())) {
            return "Die Wette ist beendet. Fänge können noch innerhalb der Nachtragefrist eingetragen werden.";
        }

        return "";
    }
}
