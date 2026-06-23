package de.mainwetten.bet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public interface BetRepository
        extends JpaRepository<Bet, Long> {

    List<Bet> findByCreatedById(Long userId);

    @Query("""
            select count(bet)
            from Bet bet
            where bet.createdBy.id = :userId
              and bet.createdAt >= :createdAfter
            """)
    long countCreatedByUserSince(
            @Param("userId") Long userId,
            @Param("createdAfter")
            OffsetDateTime createdAfter
    );

    @Query("""
            select count(bet)
            from Bet bet
            where bet.createdBy.id = :userId
              and bet.endDate >= :today
            """)
    long countActiveOrUpcomingCreatedByUser(
            @Param("userId") Long userId,
            @Param("today") LocalDate today
    );
}