package de.mainwetten.user;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetTokenCleanupJob {

    private final PasswordResetTokenCleanupService cleanupService;

    public PasswordResetTokenCleanupJob(
            PasswordResetTokenCleanupService cleanupService
    ) {
        this.cleanupService = cleanupService;
    }

    @Scheduled(
            cron = "0 35 3 * * *",
            zone = "Europe/Berlin"
    )
    public void cleanupExpiredTokens() {
        cleanupService.deleteExpiredTokens();
    }
}
