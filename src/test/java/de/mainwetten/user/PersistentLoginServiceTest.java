package de.mainwetten.user;

import org.junit.jupiter.api.Test;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class PersistentLoginServiceTest {

    @Test
    void invalidateForUserRemovesAllPersistentTokens() {
        PersistentTokenRepository tokenRepository =
                mock(PersistentTokenRepository.class);

        PersistentLoginService service =
                new PersistentLoginService(tokenRepository);

        service.invalidateForUser("Alice");

        verify(tokenRepository).removeUserTokens("Alice");
    }

    @Test
    void invalidateForUserRejectsBlankUsername() {
        PersistentTokenRepository tokenRepository =
                mock(PersistentTokenRepository.class);

        PersistentLoginService service =
                new PersistentLoginService(tokenRepository);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.invalidateForUser(" ")
        );

        verify(tokenRepository, never())
                .removeUserTokens(
                        org.mockito.ArgumentMatchers.anyString()
                );
    }
}
