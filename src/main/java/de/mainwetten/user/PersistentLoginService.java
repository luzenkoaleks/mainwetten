package de.mainwetten.user;

import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Service;

@Service
public class PersistentLoginService {

    private final PersistentTokenRepository tokenRepository;

    public PersistentLoginService(
            PersistentTokenRepository tokenRepository
    ) {
        this.tokenRepository = tokenRepository;
    }

    public void invalidateForUser(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "Der Benutzername darf nicht leer sein."
            );
        }

        tokenRepository.removeUserTokens(username);
    }
}
