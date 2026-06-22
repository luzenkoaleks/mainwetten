package de.mainwetten.user;

import de.mainwetten.bet.Bet;
import de.mainwetten.bet.BetParticipantRepository;
import de.mainwetten.bet.BetRepository;
import de.mainwetten.bet.ParticipantStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountDeletionServiceTest {

    private AppUserRepository appUserRepository;
    private BetRepository betRepository;
    private BetParticipantRepository
            betParticipantRepository;
    private PasswordEncoder passwordEncoder;
    private PersistentLoginService
            persistentLoginService;
    private AccountDeletionService service;

    @BeforeEach
    void setUp() {
        appUserRepository =
                mock(AppUserRepository.class);

        betRepository =
                mock(BetRepository.class);

        betParticipantRepository =
                mock(BetParticipantRepository.class);

        passwordEncoder =
                mock(PasswordEncoder.class);

        persistentLoginService =
                mock(PersistentLoginService.class);

        service = new AccountDeletionService(
                appUserRepository,
                betRepository,
                betParticipantRepository,
                passwordEncoder,
                persistentLoginService
        );
    }

    @Test
    void deleteAccountKeepsSharedBetAndDeletesSoloBet() {
        AppUser user = createUser();

        Bet sharedBet = createBet(10L, user);
        Bet soloBet = createBet(20L, user);

        when(appUserRepository
                .findByUsernameIgnoreCase("Alice"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "correct-password",
                "password-hash"
        )).thenReturn(true);

        when(betRepository.findByCreatedById(1L))
                .thenReturn(
                        List.of(sharedBet, soloBet)
                );

        when(betParticipantRepository
                .existsByBetIdAndStatusAndUserIdNot(
                        10L,
                        ParticipantStatus.ACCEPTED,
                        1L
                )).thenReturn(true);

        when(betParticipantRepository
                .existsByBetIdAndStatusAndUserIdNot(
                        20L,
                        ParticipantStatus.ACCEPTED,
                        1L
                )).thenReturn(false);

        AccountDeletionResult result =
                service.deleteAccount(
                        "Alice",
                        "correct-password",
                        "LÖSCHEN"
                );

        assertEquals(
                AccountDeletionResult.DELETED,
                result
        );

        assertNull(sharedBet.getCreatedBy());

        verify(betRepository).save(sharedBet);
        verify(betRepository).delete(soloBet);
        verify(betRepository).flush();

        verify(betParticipantRepository)
                .deleteByInvitedByIdAndStatusIn(
                        1L,
                        List.of(
                                ParticipantStatus.INVITED,
                                ParticipantStatus.DECLINED
                        )
                );

        verify(persistentLoginService)
                .invalidateForUser("Alice");

        verify(appUserRepository).delete(user);
        verify(appUserRepository).flush();
    }

    @Test
    void deleteAccountRejectsIncorrectPassword() {
        AppUser user = createUser();

        when(appUserRepository
                .findByUsernameIgnoreCase("Alice"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "wrong-password",
                "password-hash"
        )).thenReturn(false);

        AccountDeletionResult result =
                service.deleteAccount(
                        "Alice",
                        "wrong-password",
                        "LÖSCHEN"
                );

        assertEquals(
                AccountDeletionResult
                        .CURRENT_PASSWORD_INVALID,
                result
        );

        verify(betRepository, never())
                .findByCreatedById(1L);

        verify(persistentLoginService, never())
                .invalidateForUser("Alice");

        verify(appUserRepository, never())
                .delete(user);
    }

    @Test
    void deleteAccountRejectsIncorrectConfirmation() {
        AccountDeletionResult result =
                service.deleteAccount(
                        "Alice",
                        "correct-password",
                        "löschen"
                );

        assertEquals(
                AccountDeletionResult
                        .CONFIRMATION_INVALID,
                result
        );

        verify(appUserRepository, never())
                .findByUsernameIgnoreCase("Alice");

        verify(passwordEncoder, never())
                .matches(
                        "correct-password",
                        "password-hash"
                );
    }

    private AppUser createUser() {
        AppUser user = new AppUser();

        ReflectionTestUtils.setField(
                user,
                "id",
                1L
        );

        user.setUsername("Alice");
        user.setEmail("alice@example.test");
        user.setPasswordHash("password-hash");
        user.setEmailVerified(true);

        return user;
    }

    private Bet createBet(
            Long id,
            AppUser creator
    ) {
        Bet bet = new Bet();

        ReflectionTestUtils.setField(
                bet,
                "id",
                id
        );

        bet.setTitle("Testwette " + id);
        bet.setCreatedBy(creator);

        return bet;
    }
}
