package de.mainwetten.catchentry;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CatchAssignmentRepository extends JpaRepository<CatchAssignment, Long> {

    @Query("""
            select assignment
            from CatchAssignment assignment
            join fetch assignment.catchRecord record
            join fetch record.user
            join fetch record.fishSpecies
            where assignment.bet.id = :betId
            order by record.caughtAt desc, record.createdAt desc
            """)
    List<CatchAssignment> findByBetIdWithDetailsOrderByCaughtAtDesc(@Param("betId") Long betId);
}
