package de.mainwetten.bet;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

public interface BetParticipantRepository extends JpaRepository<BetParticipant, Long> {

    List<BetParticipant> findByUserUsernameAndStatusOrderByBetEndDateAsc(
            String username,
            ParticipantStatus status
    );

    Optional<BetParticipant> findByBetIdAndUserUsernameAndStatus(
            Long betId,
            String username,
            ParticipantStatus status
    );

    Optional<BetParticipant> findByIdAndUserUsernameIgnoreCaseAndStatus(
            Long id,
            String username,
            ParticipantStatus status
    );

    List<BetParticipant> findByBetIdOrderByUserUsernameAsc(Long betId);

    Optional<BetParticipant> findByBetIdAndUserId(Long betId, Long userId);

    boolean existsByBetIdAndStatusAndUserIdNot(
            Long betId,
            ParticipantStatus status,
            Long userId
    );

    long deleteByInvitedByIdAndStatusIn(
            Long invitedById,
            Collection<ParticipantStatus> statuses
    );
}