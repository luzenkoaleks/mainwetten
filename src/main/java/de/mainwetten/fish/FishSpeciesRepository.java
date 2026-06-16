package de.mainwetten.fish;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FishSpeciesRepository extends JpaRepository<FishSpecies, Long> {

    List<FishSpecies> findByActiveTrueOrderByCategoryAscNameAsc();

    List<FishSpecies> findByCategoryAndActiveTrueOrderByNameAsc(FishCategory category);
}