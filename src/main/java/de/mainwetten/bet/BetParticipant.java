package de.mainwetten.bet;

import de.mainwetten.user.AppUser;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "bet_participant")
public class BetParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bet_id", nullable = false)
    private Bet bet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ParticipantStatus status;

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

    public Bet getBet() {
        return bet;
    }

    public void setBet(Bet bet) {
        this.bet = bet;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public ParticipantStatus getStatus() {
        return status;
    }

    public void setStatus(ParticipantStatus status) {
        this.status = status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
