package de.mainwetten.user;

import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class ActiveSessionService {

    private final SessionRegistry sessionRegistry;

    public ActiveSessionService(
            SessionRegistry sessionRegistry
    ) {
        this.sessionRegistry = sessionRegistry;
    }

    public int expireSessionsForUser(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "Der Benutzername darf nicht leer sein."
            );
        }

        int expiredSessionCount = 0;

        for (Object principal
                : sessionRegistry.getAllPrincipals()) {

            String principalUsername =
                    extractUsername(principal);

            if (principalUsername == null
                    || !principalUsername.equalsIgnoreCase(
                            username.trim()
                    )) {
                continue;
            }

            for (SessionInformation sessionInformation
                    : sessionRegistry.getAllSessions(
                            principal,
                            false
                    )) {
                sessionInformation.expireNow();
                expiredSessionCount++;
            }
        }

        return expiredSessionCount;
    }

    private String extractUsername(Object principal) {
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }

        if (principal instanceof String username) {
            return username;
        }

        return null;
    }
}
