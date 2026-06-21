package de.mainwetten.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailVerificationResendServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private EmailVerificationService emailVerificationService;

    @Mock
    private EmailVerificationMailService emailVerificationMailService;

    @InjectMocks
    private EmailVerificationResendService resendService;

    @Test
    void resendIfEligible_sendsNewTokenForUnverifiedUser() {
        AppUser user = createUser(false);

        when(appUserRepository.findByEmailIgnoreCase(
                "alice@example.test"
        )).thenReturn(Optional.of(user));

        when(emailVerificationService
                .createReplacementTokenIfAllowed(user))
                .thenReturn(Optional.of("new-token"));

        resendService.resendIfEligible("alice@example.test");

        verify(emailVerificationService)
                .createReplacementTokenIfAllowed(user);

        verify(emailVerificationMailService)
                .sendVerificationEmail(user, "new-token");
    }

    @Test
    void resendIfEligible_doesNothingForVerifiedUser() {
        AppUser user = createUser(true);

        when(appUserRepository.findByEmailIgnoreCase(
                "alice@example.test"
        )).thenReturn(Optional.of(user));

        resendService.resendIfEligible("alice@example.test");

        verify(emailVerificationService, never())
                .createOrReplaceToken(user);

        verify(emailVerificationMailService, never())
                .sendVerificationEmail(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.anyString()
                );
    }

    @Test
    void resendIfEligible_doesNothingForUnknownEmail() {
        when(appUserRepository.findByEmailIgnoreCase(
                "unknown@example.test"
        )).thenReturn(Optional.empty());

        resendService.resendIfEligible("unknown@example.test");

        verify(emailVerificationService, never())
                .createOrReplaceToken(
                        org.mockito.ArgumentMatchers.any()
                );

        verify(emailVerificationMailService, never())
                .sendVerificationEmail(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.anyString()
                );
    }

    @Test
    void resendIfEligible_doesNotSendMailDuringCooldown() {
        AppUser user = createUser(false);

        when(appUserRepository.findByEmailIgnoreCase(
                "alice@example.test"
        )).thenReturn(Optional.of(user));

        when(emailVerificationService
                .createReplacementTokenIfAllowed(user))
                .thenReturn(Optional.empty());

        resendService.resendIfEligible("alice@example.test");

        verify(emailVerificationMailService, never())
                .sendVerificationEmail(
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
