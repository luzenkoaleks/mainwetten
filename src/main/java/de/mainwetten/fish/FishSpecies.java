package de.mainwetten.fish;

import jakarta.persistence.*;

@Entity
@Table(name = "fish_species")
public class FishSpecies {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "base_points", nullable = false)
    private Integer basePoints;

    @Column(nullable = false)
    private Boolean active;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getBasePoints() {
        return basePoints;
    }

    public Boolean getActive() {
        return active;
    }
}
