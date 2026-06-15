package de.mainwetten.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserRegistrationService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public UserRegistrationService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AppUser register(RegistrationForm form) {
        AppUser user = new AppUser();
        user.setUsername(form.getUsername().trim());
        user.setEmail(form.getEmail().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));

        return appUserRepository.save(user);
    }
}
