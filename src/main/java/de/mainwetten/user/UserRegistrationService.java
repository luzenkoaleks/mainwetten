package de.mainwetten.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserRegistrationService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;
    private final EmailVerificationMailService emailVerificationMailService;

    public UserRegistrationService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            EmailVerificationService emailVerificationService,
            EmailVerificationMailService emailVerificationMailService
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailVerificationService = emailVerificationService;
        this.emailVerificationMailService = emailVerificationMailService;
    }

    @Transactional
    public AppUser register(RegistrationForm form) {
        AppUser user = new AppUser();
        user.setUsername(form.getUsername());
        user.setEmail(form.getEmail());
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        user.setEmailVerified(false);

        AppUser savedUser = appUserRepository.saveAndFlush(user);

        String rawToken =
                emailVerificationService.createOrReplaceToken(savedUser);

        emailVerificationMailService.sendVerificationEmail(
                savedUser,
                rawToken
        );

        return savedUser;
    }
}
