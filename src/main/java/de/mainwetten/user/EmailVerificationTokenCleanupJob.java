package de.mainwetten.user;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EmailVerificationTokenCleanupJob {

    private final EmailVerificationTokenCleanupService cleanupService;

    public EmailVerificationTokenCleanupJob(
            EmailVerificationTokenCleanupService cleanupService
    ) {
        this.cleanupService = cleanupService;
    }

    @Scheduled(
            cron = "0 30 3 * * *",
            zone = "Europe/Berlin"
    )
    public void cleanupExpiredTokens() {
        cleanupService.deleteExpiredTokens();
    }
}
