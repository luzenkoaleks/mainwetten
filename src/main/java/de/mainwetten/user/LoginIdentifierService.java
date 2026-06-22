package de.mainwetten.user;

import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
public class LoginIdentifierService {

    private static final String UNKNOWN_USER =
            "unknown-user";

    private final AppUserRepository appUserRepository;

    public LoginIdentifierService(
            AppUserRepository appUserRepository
    ) {
        this.appUserRepository = appUserRepository;
    }

    public Optional<AppUser> findUser(
            String loginIdentifier
    ) {
        String normalizedIdentifier =
                normalize(loginIdentifier);

        if (normalizedIdentifier.isBlank()) {
            return Optional.empty();
        }

        return appUserRepository
                .findByUsernameIgnoreCase(
                        normalizedIdentifier
                )
                .or(() -> appUserRepository
                        .findByEmailIgnoreCase(
                                normalizedIdentifier
                        ));
    }

    public String normalizeForRateLimit(
            String loginIdentifier
    ) {
        String normalizedIdentifier =
                normalize(loginIdentifier);

        if (normalizedIdentifier.isBlank()) {
            return UNKNOWN_USER;
        }

        return findUser(normalizedIdentifier)
                .map(AppUser::getUsername)
                .map(this::normalizeLowerCase)
                .orElseGet(() ->
                        normalizeLowerCase(
                                normalizedIdentifier
                        )
                );
    }

    private String normalize(String value) {
        return value == null
                ? ""
                : value.trim();
    }

    private String normalizeLowerCase(String value) {
        return normalize(value)
                .toLowerCase(Locale.ROOT);
    }
}
