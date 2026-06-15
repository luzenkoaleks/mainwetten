package de.mainwetten.catchentry;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CatchEntryRepository extends JpaRepository<CatchEntry, Long> {

    List<CatchEntry> findByBetIdOrderByCaughtAtDescCreatedAtDesc(Long betId);
}
