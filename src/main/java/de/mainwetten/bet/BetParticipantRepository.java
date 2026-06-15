package de.mainwetten.bet;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BetParticipantRepository extends JpaRepository<BetParticipant, Long> {

    List<BetParticipant> findByUserUsernameAndStatusOrderByBetEndDateAsc(
            String username,
            ParticipantStatus status
    );
}
