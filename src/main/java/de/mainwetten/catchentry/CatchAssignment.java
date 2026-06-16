package de.mainwetten.catchentry;

import de.mainwetten.bet.Bet;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "catch_assignment")
public class CatchAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "catch_record_id", nullable = false)
    private CatchRecord catchRecord;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bet_id", nullable = false)
    private Bet bet;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public CatchRecord getCatchRecord() {
        return catchRecord;
    }

    public void setCatchRecord(CatchRecord catchRecord) {
        this.catchRecord = catchRecord;
    }

    public Bet getBet() {
        return bet;
    }

    public void setBet(Bet bet) {
        this.bet = bet;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
