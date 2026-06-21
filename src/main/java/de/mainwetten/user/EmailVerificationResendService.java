package de.mainwetten.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailVerificationResendService {

    private final AppUserRepository appUserRepository;
    private final EmailVerificationService emailVerificationService;
    private final EmailVerificationMailService emailVerificationMailService;

    public EmailVerificationResendService(
            AppUserRepository appUserRepository,
            EmailVerificationService emailVerificationService,
            EmailVerificationMailService emailVerificationMailService
    ) {
        this.appUserRepository = appUserRepository;
        this.emailVerificationService = emailVerificationService;
        this.emailVerificationMailService = emailVerificationMailService;
    }

    @Transactional
    public void resendIfEligible(String email) {
        if (email == null || email.isBlank()) {
            return;
        }

        appUserRepository.findByEmailIgnoreCase(email.trim())
                .filter(user -> !user.isEmailVerified())
                .ifPresent(user ->
                        emailVerificationService
                                .createReplacementTokenIfAllowed(user)
                                .ifPresent(rawToken ->
                                        emailVerificationMailService
                                                .sendVerificationEmail(
                                                        user,
                                                        rawToken
                                                )
                                )
                );
    }
}
