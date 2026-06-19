package de.mainwetten.bet;

import de.mainwetten.user.AppUser;
import de.mainwetten.user.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BetInvitationService {

    private final BetParticipantRepository betParticipantRepository;
    private final AppUserRepository appUserRepository;

    public BetInvitationService(
            BetParticipantRepository betParticipantRepository,
            AppUserRepository appUserRepository
    ) {
        this.betParticipantRepository = betParticipantRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public void inviteUser(Long betId, String inviterUsername, String invitedUsername) {
        BetParticipant inviterParticipation = betParticipantRepository
                .findByBetIdAndUserUsernameAndStatus(
                        betId,
                        inviterUsername,
                        ParticipantStatus.ACCEPTED
                )
                .orElseThrow(() -> new IllegalArgumentException("Wette nicht gefunden oder kein Zugriff."));

        AppUser invitedUser = appUserRepository.findByUsernameIgnoreCase(invitedUsername.trim())
                .orElseThrow(() -> new IllegalArgumentException("Benutzer wurde nicht gefunden."));

        if (invitedUser.getId().equals(inviterParticipation.getUser().getId())) {
            throw new IllegalArgumentException("Du kannst dich nicht selbst einladen.");
        }

        var existingParticipant = betParticipantRepository.findByBetIdAndUserId(betId, invitedUser.getId());

        if (existingParticipant.isPresent()) {
            BetParticipant participant = existingParticipant.get();

            if (participant.getStatus() == ParticipantStatus.ACCEPTED) {
                throw new IllegalArgumentException("Dieser Benutzer ist bereits Teilnehmer.");
            }

            if (participant.getStatus() == ParticipantStatus.INVITED) {
                throw new IllegalArgumentException("Dieser Benutzer wurde bereits eingeladen.");
            }

            if (participant.getStatus() == ParticipantStatus.DECLINED) {
                participant.setStatus(ParticipantStatus.INVITED);
                return;
            }
        }

        BetParticipant participant = new BetParticipant();
        participant.setBet(inviterParticipation.getBet());
        participant.setUser(invitedUser);
        participant.setStatus(ParticipantStatus.INVITED);

        betParticipantRepository.save(participant);
    }

    @Transactional
    public void acceptInvitation(Long participantId, String username) {
        BetParticipant participant = getOwnInvitation(participantId, username);
        participant.setStatus(ParticipantStatus.ACCEPTED);
    }

    @Transactional
    public void declineInvitation(Long participantId, String username) {
        BetParticipant participant = getOwnInvitation(participantId, username);
        participant.setStatus(ParticipantStatus.DECLINED);
    }

    private BetParticipant getOwnInvitation(Long participantId, String username) {
        return betParticipantRepository
                .findByIdAndUserUsernameIgnoreCaseAndStatus(
                        participantId,
                        username,
                        ParticipantStatus.INVITED
                )
                .orElseThrow(() -> new IllegalArgumentException(
                        "Einladung nicht gefunden oder nicht mehr verfügbar."
                ));
    }
}
