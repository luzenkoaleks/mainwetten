package de.mainwetten.user;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class EmailVerificationTokenCleanupJobTest {

    @Test
    void cleanupJobDelegatesToCleanupService() {
        EmailVerificationTokenCleanupService cleanupService =
                mock(EmailVerificationTokenCleanupService.class);

        EmailVerificationTokenCleanupJob cleanupJob =
                new EmailVerificationTokenCleanupJob(
                        cleanupService
                );

        cleanupJob.cleanupExpiredTokens();

        verify(cleanupService).deleteExpiredTokens();
    }
}
