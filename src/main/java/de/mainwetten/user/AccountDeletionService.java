package de.mainwetten.user;

import de.mainwetten.bet.Bet;
import de.mainwetten.bet.BetParticipantRepository;
import de.mainwetten.bet.BetRepository;
import de.mainwetten.bet.ParticipantStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AccountDeletionService {

    private static final String REQUIRED_CONFIRMATION =
            "LÖSCHEN";

    private final AppUserRepository appUserRepository;
    private final BetRepository betRepository;
    private final BetParticipantRepository
            betParticipantRepository;
    private final PasswordEncoder passwordEncoder;
    private final PersistentLoginService
            persistentLoginService;

    public AccountDeletionService(
            AppUserRepository appUserRepository,
            BetRepository betRepository,
            BetParticipantRepository
                    betParticipantRepository,
            PasswordEncoder passwordEncoder,
            PersistentLoginService persistentLoginService
    ) {
        this.appUserRepository = appUserRepository;
        this.betRepository = betRepository;
        this.betParticipantRepository =
                betParticipantRepository;
        this.passwordEncoder = passwordEncoder;
        this.persistentLoginService =
                persistentLoginService;
    }

    @Transactional
    public AccountDeletionResult deleteAccount(
            String username,
            String currentPassword,
            String confirmation
    ) {
        validateInput(
                username,
                currentPassword,
                confirmation
        );

        if (!REQUIRED_CONFIRMATION.equals(
                confirmation.trim()
        )) {
            return AccountDeletionResult
                    .CONFIRMATION_INVALID;
        }

        AppUser user = appUserRepository
                .findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new IllegalStateException(
                        "Der angemeldete Benutzer wurde nicht gefunden."
                ));

        if (!passwordEncoder.matches(
                currentPassword,
                user.getPasswordHash()
        )) {
            return AccountDeletionResult
                    .CURRENT_PASSWORD_INVALID;
        }

        handleCreatedBets(user);

        /*
         * Entfernt noch offene oder abgelehnte
         * Einladungen, die dieser Nutzer verschickt hat.
         * Akzeptierte Teilnehmer bleiben erhalten.
         */
        betParticipantRepository
                .deleteByInvitedByIdAndStatusIn(
                        user.getId(),
                        List.of(
                                ParticipantStatus.INVITED,
                                ParticipantStatus.DECLINED
                        )
                );

        /*
         * persistent_logins besitzt keinen
         * Fremdschlüssel zu app_user.
         */
        persistentLoginService.invalidateForUser(
                user.getUsername()
        );

        /*
         * Die Datenbank entfernt durch ON DELETE CASCADE:
         *
         * - eigene Teilnahmen
         * - empfangene Einladungen
         * - eigene Fänge
         * - Fangzuordnungen dieser Fänge
         * - Verifikations- und Reset-Tokens
         */
        appUserRepository.delete(user);
        appUserRepository.flush();

        return AccountDeletionResult.DELETED;
    }

    private void handleCreatedBets(AppUser user) {
        List<Bet> createdBets =
                betRepository.findByCreatedById(
                        user.getId()
                );

        for (Bet bet : createdBets) {
            boolean hasOtherAcceptedParticipant =
                    betParticipantRepository
                            .existsByBetIdAndStatusAndUserIdNot(
                                    bet.getId(),
                                    ParticipantStatus.ACCEPTED,
                                    user.getId()
                            );

            if (hasOtherAcceptedParticipant) {
                /*
                 * Die Wette bleibt für die übrigen
                 * Teilnehmer bestehen.
                 */
                bet.setCreatedBy(null);
                betRepository.save(bet);
            } else {
                /*
                 * Niemand außer dem Ersteller hat
                 * die Wette angenommen.
                 */
                betRepository.delete(bet);
            }
        }

        betRepository.flush();
    }

    private void validateInput(
            String username,
            String currentPassword,
            String confirmation
    ) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "Der Benutzername darf nicht leer sein."
            );
        }

        if (currentPassword == null
                || currentPassword.isBlank()) {
            throw new IllegalArgumentException(
                    "Das aktuelle Passwort darf nicht leer sein."
            );
        }

        if (confirmation == null
                || confirmation.isBlank()) {
            throw new IllegalArgumentException(
                    "Die Löschbestätigung darf nicht leer sein."
            );
        }
    }
}
