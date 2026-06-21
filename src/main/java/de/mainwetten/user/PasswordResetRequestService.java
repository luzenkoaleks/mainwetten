package de.mainwetten.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetRequestService {

    private final AppUserRepository appUserRepository;
    private final PasswordResetService passwordResetService;
    private final PasswordResetMailService passwordResetMailService;

    public PasswordResetRequestService(
            AppUserRepository appUserRepository,
            PasswordResetService passwordResetService,
            PasswordResetMailService passwordResetMailService
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordResetService = passwordResetService;
        this.passwordResetMailService = passwordResetMailService;
    }

    @Transactional
    public void requestResetIfEligible(String email) {
        if (email == null || email.isBlank()) {
            return;
        }

        appUserRepository.findByEmailIgnoreCase(email.trim())
                .filter(AppUser::isEmailVerified)
                .ifPresent(user ->
                        passwordResetService
                                .createOrReplaceTokenIfAllowed(user)
                                .ifPresent(rawToken ->
                                        passwordResetMailService
                                                .sendPasswordResetEmail(
                                                        user,
                                                        rawToken
                                                )
                                )
                );
    }
}
