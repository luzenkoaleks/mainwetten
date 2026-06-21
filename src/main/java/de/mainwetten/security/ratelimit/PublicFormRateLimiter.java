package de.mainwetten.security.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class PublicFormRateLimiter {

    private static final long REGISTRATION_CAPACITY = 5;
    private static final Duration REGISTRATION_WINDOW =
            Duration.ofMinutes(15);

    private static final long VERIFICATION_RESEND_CAPACITY = 10;
    private static final Duration VERIFICATION_RESEND_WINDOW =
            Duration.ofMinutes(15);

    private static final long LOGIN_CAPACITY = 10;
    private static final Duration LOGIN_WINDOW =
            Duration.ofMinutes(15);

    private static final long MAX_TRACKED_CLIENTS = 10_000;
    private static final Duration CACHE_EXPIRY =
            Duration.ofHours(1);

    private final Cache<String, Bucket> registrationBuckets;
    private final Cache<String, Bucket> verificationResendBuckets;
    private final Cache<String, Bucket> loginBuckets;

    public PublicFormRateLimiter() {
        registrationBuckets = createCache();
        verificationResendBuckets = createCache();
        loginBuckets = createCache();
    }

    public boolean tryConsumeRegistration(String clientKey) {
        return tryConsume(
                registrationBuckets,
                normalizeClientKey(clientKey),
                REGISTRATION_CAPACITY,
                REGISTRATION_WINDOW
        );
    }

    public boolean tryConsumeVerificationResend(String clientKey) {
        return tryConsume(
                verificationResendBuckets,
                normalizeClientKey(clientKey),
                VERIFICATION_RESEND_CAPACITY,
                VERIFICATION_RESEND_WINDOW
        );
    }

    public boolean tryConsumeLoginAttempt(String clientKey) {
        return tryConsume(
                loginBuckets,
                normalizeClientKey(clientKey),
                LOGIN_CAPACITY,
                LOGIN_WINDOW
        );
    }

    public void clearLoginAttempts(String clientKey) {
        loginBuckets.invalidate(
                normalizeClientKey(clientKey)
        );
    }

    private boolean tryConsume(
            Cache<String, Bucket> buckets,
            String clientKey,
            long capacity,
            Duration refillWindow
    ) {
        Bucket bucket = buckets.get(
                clientKey,
                ignored -> createBucket(
                        capacity,
                        refillWindow
                )
        );

        return bucket.tryConsume(1);
    }

    private Bucket createBucket(
            long capacity,
            Duration refillWindow
    ) {
        return Bucket.builder()
                .addLimit(limit -> limit
                        .capacity(capacity)
                        .refillIntervally(
                                capacity,
                                refillWindow
                        )
                )
                .build();
    }

    private Cache<String, Bucket> createCache() {
        return Caffeine.<String, Bucket>newBuilder()
                .maximumSize(MAX_TRACKED_CLIENTS)
                .expireAfterAccess(CACHE_EXPIRY)
                .build();
    }

    private String normalizeClientKey(String clientKey) {
        if (clientKey == null || clientKey.isBlank()) {
            return "unknown-client";
        }

        return clientKey.trim();
    }
}
