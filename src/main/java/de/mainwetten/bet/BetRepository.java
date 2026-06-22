package de.mainwetten.bet;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BetRepository
        extends JpaRepository<Bet, Long> {

    List<Bet> findByCreatedById(Long userId);
}