package de.mainwetten.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;

@Service
public class PasswordResetTokenCleanupService {

    private final PasswordResetTokenRepository tokenRepository;
    private final Clock clock;

    public PasswordResetTokenCleanupService(
            PasswordResetTokenRepository tokenRepository,
            Clock clock
    ) {
        this.tokenRepository = tokenRepository;
        this.clock = clock;
    }

    @Transactional
    public int deleteExpiredTokens() {
        return tokenRepository.deleteExpiredTokens(
                OffsetDateTime.now(clock)
        );
    }
}
