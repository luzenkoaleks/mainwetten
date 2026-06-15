package de.mainwetten.catchentry;

import de.mainwetten.bet.Bet;
import de.mainwetten.fish.FishSpecies;
import de.mainwetten.user.AppUser;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "catch_entry")
public class CatchEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bet_id", nullable = false)
    private Bet bet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fish_species_id", nullable = false)
    private FishSpecies fishSpecies;

    @Column(name = "length_cm", nullable = false, precision = 5, scale = 1)
    private BigDecimal lengthCm;

    @Column(name = "caught_at", nullable = false)
    private OffsetDateTime caughtAt;

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

    public FishSpecies getFishSpecies() {
        return fishSpecies;
    }

    public void setFishSpecies(FishSpecies fishSpecies) {
        this.fishSpecies = fishSpecies;
    }

    public BigDecimal getLengthCm() {
        return lengthCm;
    }

    public void setLengthCm(BigDecimal lengthCm) {
        this.lengthCm = lengthCm;
    }

    public OffsetDateTime getCaughtAt() {
        return caughtAt;
    }

    public void setCaughtAt(OffsetDateTime caughtAt) {
        this.caughtAt = caughtAt;
    }

    public LocalDate getCaughtDate() {
        return caughtAt.toLocalDate();
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
