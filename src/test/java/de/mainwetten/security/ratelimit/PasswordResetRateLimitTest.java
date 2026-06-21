package de.mainwetten.security.ratelimit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordResetRateLimitTest {

    @Test
    void allowsFiveRequestsAndRejectsSixth() {
        PublicFormRateLimiter limiter =
                new PublicFormRateLimiter();

        String clientKey = "192.0.2.10";

        for (int attempt = 0; attempt < 5; attempt++) {
            assertTrue(
                    limiter.tryConsumePasswordResetRequest(
                            clientKey
                    )
            );
        }

        assertFalse(
                limiter.tryConsumePasswordResetRequest(
                        clientKey
                )
        );
    }

    @Test
    void passwordResetUsesIndependentBucket() {
        PublicFormRateLimiter limiter =
                new PublicFormRateLimiter();

        String clientKey = "192.0.2.10";

        for (int attempt = 0; attempt < 5; attempt++) {
            assertTrue(
                    limiter.tryConsumeRegistration(clientKey)
            );
        }

        assertFalse(
                limiter.tryConsumeRegistration(clientKey)
        );

        assertTrue(
                limiter.tryConsumePasswordResetRequest(
                        clientKey
                )
        );
    }
}
