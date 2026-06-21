package de.mainwetten.user;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PasswordResetTokenCleanupJobTest {

    @Test
    void cleanupJobDelegatesToCleanupService() {
        PasswordResetTokenCleanupService cleanupService =
                mock(PasswordResetTokenCleanupService.class);

        PasswordResetTokenCleanupJob cleanupJob =
                new PasswordResetTokenCleanupJob(
                        cleanupService
                );

        cleanupJob.cleanupExpiredTokens();

        verify(cleanupService).deleteExpiredTokens();
    }
}
