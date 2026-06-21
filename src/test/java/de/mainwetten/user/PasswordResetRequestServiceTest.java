package de.mainwetten.user;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PasswordResetRequestServiceTest {

    @Test
    void sendsResetMailForVerifiedUser() {
        AppUserRepository userRepository =
                mock(AppUserRepository.class);

        PasswordResetService resetService =
                mock(PasswordResetService.class);

        PasswordResetMailService mailService =
                mock(PasswordResetMailService.class);

        PasswordResetRequestService requestService =
                new PasswordResetRequestService(
                        userRepository,
                        resetService,
                        mailService
                );

        AppUser user = createUser(true);

        when(userRepository.findByEmailIgnoreCase(
                "alice@example.test"
        )).thenReturn(Optional.of(user));

        when(resetService.createOrReplaceTokenIfAllowed(user))
                .thenReturn(Optional.of("reset-token"));

        requestService.requestResetIfEligible(
                "alice@example.test"
        );

        verify(mailService).sendPasswordResetEmail(
                user,
                "reset-token"
        );
    }

    @Test
    void doesNothingForUnknownEmail() {
        AppUserRepository userRepository =
                mock(AppUserRepository.class);

        PasswordResetService resetService =
                mock(PasswordResetService.class);

        PasswordResetMailService mailService =
                mock(PasswordResetMailService.class);

        PasswordResetRequestService requestService =
                new PasswordResetRequestService(
                        userRepository,
                        resetService,
                        mailService
                );

        when(userRepository.findByEmailIgnoreCase(
                "unknown@example.test"
        )).thenReturn(Optional.empty());

        requestService.requestResetIfEligible(
                "unknown@example.test"
        );

        verify(resetService, never())
                .createOrReplaceTokenIfAllowed(
                        org.mockito.ArgumentMatchers.any()
                );

        verify(mailService, never())
                .sendPasswordResetEmail(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.anyString()
                );
    }

    @Test
    void doesNothingForUnverifiedUser() {
        AppUserRepository userRepository =
                mock(AppUserRepository.class);

        PasswordResetService resetService =
                mock(PasswordResetService.class);

        PasswordResetMailService mailService =
                mock(PasswordResetMailService.class);

        PasswordResetRequestService requestService =
                new PasswordResetRequestService(
                        userRepository,
                        resetService,
                        mailService
                );

        AppUser user = createUser(false);

        when(userRepository.findByEmailIgnoreCase(
                "alice@example.test"
        )).thenReturn(Optional.of(user));

        requestService.requestResetIfEligible(
                "alice@example.test"
        );

        verify(resetService, never())
                .createOrReplaceTokenIfAllowed(user);

        verify(mailService, never())
                .sendPasswordResetEmail(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.anyString()
                );
    }

    @Test
    void doesNotSendMailDuringCooldown() {
        AppUserRepository userRepository =
                mock(AppUserRepository.class);

        PasswordResetService resetService =
                mock(PasswordResetService.class);

        PasswordResetMailService mailService =
                mock(PasswordResetMailService.class);

        PasswordResetRequestService requestService =
                new PasswordResetRequestService(
                        userRepository,
                        resetService,
                        mailService
                );

        AppUser user = createUser(true);

        when(userRepository.findByEmailIgnoreCase(
                "alice@example.test"
        )).thenReturn(Optional.of(user));

        when(resetService.createOrReplaceTokenIfAllowed(user))
                .thenReturn(Optional.empty());

        requestService.requestResetIfEligible(
                "alice@example.test"
        );

        verify(mailService, never())
                .sendPasswordResetEmail(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.anyString()
                );
    }

    private AppUser createUser(boolean verified) {
        AppUser user = new AppUser();
        user.setUsername("Alice");
        user.setEmail("alice@example.test");
        user.setEmailVerified(verified);
        return user;
    }
}
