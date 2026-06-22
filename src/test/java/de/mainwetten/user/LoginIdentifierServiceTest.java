package de.mainwetten.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoginIdentifierServiceTest {

    private AppUserRepository appUserRepository;
    private LoginIdentifierService service;

    @BeforeEach
    void setUp() {
        appUserRepository =
                mock(AppUserRepository.class);

        service = new LoginIdentifierService(
                appUserRepository
        );
    }

    @Test
    void findUserFindsUserByUsername() {
        AppUser user = createUser();

        when(appUserRepository
                .findByUsernameIgnoreCase("Alice"))
                .thenReturn(Optional.of(user));

        Optional<AppUser> result =
                service.findUser("  Alice  ");

        assertTrue(result.isPresent());
        assertSame(user, result.get());

        verify(appUserRepository)
                .findByUsernameIgnoreCase("Alice");

        verify(appUserRepository, never())
                .findByEmailIgnoreCase(
                        org.mockito.ArgumentMatchers
                                .anyString()
                );
    }

    @Test
    void findUserFallsBackToEmail() {
        AppUser user = createUser();

        when(appUserRepository
                .findByUsernameIgnoreCase(
                        "Alice@Example.Test"
                ))
                .thenReturn(Optional.empty());

        when(appUserRepository
                .findByEmailIgnoreCase(
                        "Alice@Example.Test"
                ))
                .thenReturn(Optional.of(user));

        Optional<AppUser> result =
                service.findUser(
                        "Alice@Example.Test"
                );

        assertTrue(result.isPresent());
        assertSame(user, result.get());

        verify(appUserRepository)
                .findByUsernameIgnoreCase(
                        "Alice@Example.Test"
                );

        verify(appUserRepository)
                .findByEmailIgnoreCase(
                        "Alice@Example.Test"
                );

    }


    @Test
    void normalizeForRateLimitMapsEmailToUsername() {
        AppUser user = createUser();

        when(appUserRepository
                .findByUsernameIgnoreCase(
                        "Alice@Example.Test"
                ))
                .thenReturn(Optional.empty());

        when(appUserRepository
                .findByEmailIgnoreCase(
                        "Alice@Example.Test"
                ))
                .thenReturn(Optional.of(user));

        assertEquals(
                "alice",
                service.normalizeForRateLimit(
                        "Alice@Example.Test"
                )
        );

    }


    @Test
    void normalizeForRateLimitKeepsUnknownIdentifier() {
        when(appUserRepository
                .findByUsernameIgnoreCase("Unknown"))
                .thenReturn(Optional.empty());

        when(appUserRepository
                .findByEmailIgnoreCase("Unknown"))
                .thenReturn(Optional.empty());

        assertEquals(
                "unknown",
                service.normalizeForRateLimit(
                        " Unknown "
                )
        );
    }

    @Test
    void findUserRejectsBlankIdentifier() {
        assertTrue(
                service.findUser(" ").isEmpty()
        );

        verify(appUserRepository, never())
                .findByUsernameIgnoreCase(
                        org.mockito.ArgumentMatchers
                                .anyString()
                );

        verify(appUserRepository, never())
                .findByEmailIgnoreCase(
                        org.mockito.ArgumentMatchers
                                .anyString()
                );
    }

    private AppUser createUser() {
        AppUser user = new AppUser();

        user.setUsername("Alice");
        user.setEmail("alice@example.test");
        user.setPasswordHash("password-hash");
        user.setEmailVerified(true);

        return user;
    }
}
