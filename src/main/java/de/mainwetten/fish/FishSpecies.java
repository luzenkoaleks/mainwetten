package de.mainwetten.fish;

import jakarta.persistence.*;

@Entity
@Table(name = "fish_species")
public class FishSpecies {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FishCategory category;

    @Column(nullable = false)
    private Boolean active;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public FishCategory getCategory() {
        return category;
    }

    public Boolean getActive() {
        return active;
    }
}