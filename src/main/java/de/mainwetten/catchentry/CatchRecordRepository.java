package de.mainwetten.catchentry;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;

public interface CatchRecordRepository
        extends JpaRepository<CatchRecord, Long> {

    @Query("""
            select count(catchRecord)
            from CatchRecord catchRecord
            where catchRecord.user.id = :userId
              and catchRecord.caughtAt >= :caughtAfter
            """)
    long countCreatedByUserSince(
            @Param("userId") Long userId,
            @Param("caughtAfter")
            OffsetDateTime caughtAfter
    );
}