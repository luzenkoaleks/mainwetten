package de.mainwetten.bet;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

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

    List<BetParticipant> findByBetIdOrderByUserUsernameAsc(Long betId);

    Optional<BetParticipant> findByBetIdAndUserId(Long betId, Long userId);
}