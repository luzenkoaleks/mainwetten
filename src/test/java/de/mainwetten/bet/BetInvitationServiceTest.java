package de.mainwetten.bet;

import de.mainwetten.user.AppUser;
import de.mainwetten.user.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
class BetInvitationServiceTest {

    @Mock
    private BetParticipantRepository betParticipantRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private BetInvitationService betInvitationService;

    @Test
    void acceptInvitation_acceptsOwnOpenInvitation() {
        AppUser invitedUser = createUser(2L, "Bob");
        BetParticipant invitation = createParticipation(
                new Bet(),
                invitedUser,
                ParticipantStatus.INVITED
        );

        when(betParticipantRepository
                .findByIdAndUserUsernameIgnoreCaseAndStatus(
                        50L,
                        "Bob",
                        ParticipantStatus.INVITED
                ))
                .thenReturn(Optional.of(invitation));

        betInvitationService.acceptInvitation(50L, "Bob");

        assertEquals(
                ParticipantStatus.ACCEPTED,
                invitation.getStatus()
        );
    }

    @Test
    void declineInvitation_declinesOwnOpenInvitation() {
        AppUser invitedUser = createUser(2L, "Bob");
        BetParticipant invitation = createParticipation(
                new Bet(),
                invitedUser,
                ParticipantStatus.INVITED
        );

        when(betParticipantRepository
                .findByIdAndUserUsernameIgnoreCaseAndStatus(
                        50L,
                        "Bob",
                        ParticipantStatus.INVITED
                ))
                .thenReturn(Optional.of(invitation));

        betInvitationService.declineInvitation(50L, "Bob");

        assertEquals(
                ParticipantStatus.DECLINED,
                invitation.getStatus()
        );
    }

    @Test
    void acceptInvitation_rejectsInvitationThatIsNotOpenForCurrentUser() {
        when(betParticipantRepository
                .findByIdAndUserUsernameIgnoreCaseAndStatus(
                        50L,
                        "Charlie",
                        ParticipantStatus.INVITED
                ))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> betInvitationService.acceptInvitation(50L, "Charlie")
        );
    }

    @Test
    void declineInvitation_rejectsInvitationThatIsNotOpenForCurrentUser() {
        when(betParticipantRepository
                .findByIdAndUserUsernameIgnoreCaseAndStatus(
                        50L,
                        "Charlie",
                        ParticipantStatus.INVITED
                ))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> betInvitationService.declineInvitation(50L, "Charlie")
        );
    }

    @Test
    void inviteUser_rejectsSelfInvitation() {
        Bet bet = new Bet();
        AppUser inviter = createUser(1L, "Alice");

        BetParticipant inviterParticipation = createParticipation(
                bet,
                inviter,
                ParticipantStatus.ACCEPTED
        );

        when(betParticipantRepository
                .findByBetIdAndUserUsernameAndStatus(
                        10L,
                        "Alice",
                        ParticipantStatus.ACCEPTED
                ))
                .thenReturn(Optional.of(inviterParticipation));

        when(appUserRepository.findByUsernameIgnoreCase("Alice"))
                .thenReturn(Optional.of(inviter));

        assertThrows(
                IllegalArgumentException.class,
                () -> betInvitationService.inviteUser(
                        10L,
                        "Alice",
                        "Alice"
                )
        );

        verify(
                betParticipantRepository,
                never()
        ).findByBetIdAndUserId(anyLong(), anyLong());

        verify(
                betParticipantRepository,
                never()
        ).save(any(BetParticipant.class));
    }

    @Test
    void inviteUser_reactivatesDeclinedParticipation() {
        Bet bet = new Bet();

        AppUser inviter = createUser(1L, "Alice");
        AppUser invitedUser = createUser(2L, "Bob");

        BetParticipant inviterParticipation = createParticipation(
                bet,
                inviter,
                ParticipantStatus.ACCEPTED
        );

        BetParticipant declinedParticipation = createParticipation(
                bet,
                invitedUser,
                ParticipantStatus.DECLINED
        );

        when(betParticipantRepository
                .findByBetIdAndUserUsernameAndStatus(
                        10L,
                        "Alice",
                        ParticipantStatus.ACCEPTED
                ))
                .thenReturn(Optional.of(inviterParticipation));

        when(appUserRepository.findByUsernameIgnoreCase("Bob"))
                .thenReturn(Optional.of(invitedUser));

        when(betParticipantRepository.findByBetIdAndUserId(10L, 2L))
                .thenReturn(Optional.of(declinedParticipation));

        betInvitationService.inviteUser(
                10L,
                "Alice",
                "Bob"
        );

        assertEquals(
                ParticipantStatus.INVITED,
                declinedParticipation.getStatus()
        );
    }

    @Test
    void inviteUser_rejectsAlreadyAcceptedParticipant() {
        Bet bet = new Bet();

        AppUser inviter = createUser(1L, "Alice");
        AppUser invitedUser = createUser(2L, "Bob");

        BetParticipant inviterParticipation = createParticipation(
                bet,
                inviter,
                ParticipantStatus.ACCEPTED
        );

        BetParticipant acceptedParticipation = createParticipation(
                bet,
                invitedUser,
                ParticipantStatus.ACCEPTED
        );

        when(betParticipantRepository
                .findByBetIdAndUserUsernameAndStatus(
                        10L,
                        "Alice",
                        ParticipantStatus.ACCEPTED
                ))
                .thenReturn(Optional.of(inviterParticipation));

        when(appUserRepository.findByUsernameIgnoreCase("Bob"))
                .thenReturn(Optional.of(invitedUser));

        when(betParticipantRepository.findByBetIdAndUserId(10L, 2L))
                .thenReturn(Optional.of(acceptedParticipation));

        assertThrows(
                IllegalArgumentException.class,
                () -> betInvitationService.inviteUser(
                        10L,
                        "Alice",
                        "Bob"
                )
        );
    }

    @Test
    void inviteUser_createsNewInvitation() {
        Bet bet = new Bet();

        AppUser inviter = createUser(1L, "Alice");
        AppUser invitedUser = createUser(2L, "Bob");

        BetParticipant inviterParticipation = createParticipation(
                bet,
                inviter,
                ParticipantStatus.ACCEPTED
        );

        when(betParticipantRepository
                .findByBetIdAndUserUsernameAndStatus(
                        10L,
                        "Alice",
                        ParticipantStatus.ACCEPTED
                ))
                .thenReturn(Optional.of(inviterParticipation));

        when(appUserRepository.findByUsernameIgnoreCase("Bob"))
                .thenReturn(Optional.of(invitedUser));

        when(betParticipantRepository.findByBetIdAndUserId(10L, 2L))
                .thenReturn(Optional.empty());

        betInvitationService.inviteUser(
                10L,
                "Alice",
                "Bob"
        );

        ArgumentCaptor<BetParticipant> participantCaptor =
                ArgumentCaptor.forClass(BetParticipant.class);

        verify(betParticipantRepository).save(participantCaptor.capture());

        BetParticipant savedParticipant = participantCaptor.getValue();

        assertSame(bet, savedParticipant.getBet());
        assertSame(invitedUser, savedParticipant.getUser());
        assertEquals(
                ParticipantStatus.INVITED,
                savedParticipant.getStatus()
        );
    }

    private AppUser createUser(Long id, String username) {
        AppUser user = new AppUser();
        ReflectionTestUtils.setField(user, "id", id);
        user.setUsername(username);
        return user;
    }

    private BetParticipant createParticipation(
            Bet bet,
            AppUser user,
            ParticipantStatus status
    ) {
        BetParticipant participant = new BetParticipant();
        participant.setBet(bet);
        participant.setUser(user);
        participant.setStatus(status);
        return participant;
    }
}
