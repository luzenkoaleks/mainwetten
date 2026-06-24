package de.mainwetten.user;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ActiveSessionServiceTest {

    @Test
    void expireSessionsForUserExpiresAllMatchingSessions() {
        SessionRegistry sessionRegistry =
                mock(SessionRegistry.class);

        SessionInformation firstSession =
                mock(SessionInformation.class);

        SessionInformation secondSession =
                mock(SessionInformation.class);

        UserDetails principal = User
                .withUsername("Alice")
                .password("password")
                .roles("USER")
                .build();

        when(sessionRegistry.getAllPrincipals())
                .thenReturn(List.of(principal));

        when(sessionRegistry.getAllSessions(
                principal,
                false
        )).thenReturn(
                List.of(firstSession, secondSession)
        );

        ActiveSessionService service =
                new ActiveSessionService(sessionRegistry);

        int expiredSessions =
                service.expireSessionsForUser("alice");

        assertEquals(2, expiredSessions);

        verify(firstSession).expireNow();
        verify(secondSession).expireNow();
    }

    @Test
    void expireSessionsForUserIgnoresOtherUsers() {
        SessionRegistry sessionRegistry =
                mock(SessionRegistry.class);

        UserDetails principal = User
                .withUsername("Bob")
                .password("password")
                .roles("USER")
                .build();

        when(sessionRegistry.getAllPrincipals())
                .thenReturn(List.of(principal));

        ActiveSessionService service =
                new ActiveSessionService(sessionRegistry);

        int expiredSessions =
                service.expireSessionsForUser("Alice");

        assertEquals(0, expiredSessions);

        verify(sessionRegistry, never())
                .getAllSessions(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.anyBoolean()
                );
    }

    @Test
    void expireSessionsForUserRejectsBlankUsername() {
        SessionRegistry sessionRegistry =
                mock(SessionRegistry.class);

        ActiveSessionService service =
                new ActiveSessionService(sessionRegistry);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.expireSessionsForUser(" ")
        );
    }
}
