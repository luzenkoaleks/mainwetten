package de.mainwetten.security.ratelimit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PublicFormRateLimiterTest {

    @Test
    void registrationAllowsFiveAttemptsAndRejectsSixth() {
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
    }

    @Test
    void registrationUsesSeparateBucketForEachClient() {
        PublicFormRateLimiter limiter =
                new PublicFormRateLimiter();

        String firstClient = "192.0.2.10";
        String secondClient = "192.0.2.20";

        for (int attempt = 0; attempt < 5; attempt++) {
            assertTrue(
                    limiter.tryConsumeRegistration(firstClient)
            );
        }

        assertFalse(
                limiter.tryConsumeRegistration(firstClient)
        );

        assertTrue(
                limiter.tryConsumeRegistration(secondClient)
        );
    }

    @Test
    void verificationResendAllowsTenAttemptsAndRejectsEleventh() {
        PublicFormRateLimiter limiter =
                new PublicFormRateLimiter();

        String clientKey = "192.0.2.10";

        for (int attempt = 0; attempt < 10; attempt++) {
            assertTrue(
                    limiter.tryConsumeVerificationResend(clientKey)
            );
        }

        assertFalse(
                limiter.tryConsumeVerificationResend(clientKey)
        );
    }

    @Test
    void registrationAndResendUseIndependentBuckets() {
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
                limiter.tryConsumeVerificationResend(clientKey)
        );
    }

    @Test
    void missingClientAddressUsesSharedFallbackBucket() {
        PublicFormRateLimiter limiter =
                new PublicFormRateLimiter();

        for (int attempt = 0; attempt < 5; attempt++) {
            assertTrue(
                    limiter.tryConsumeRegistration(null)
            );
        }

        assertFalse(
                limiter.tryConsumeRegistration(" ")
        );
    }
}
