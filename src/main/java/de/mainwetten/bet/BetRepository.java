package de.mainwetten.bet;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BetRepository extends JpaRepository<Bet, Long> {
}
