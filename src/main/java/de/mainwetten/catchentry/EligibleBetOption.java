package de.mainwetten.catchentry;

import de.mainwetten.fish.FishCategory;

import java.time.LocalDate;

public class EligibleBetOption {

    private final Long id;
    private final String title;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final FishCategory fishCategory;

    public EligibleBetOption(
            Long id,
            String title,
            LocalDate startDate,
            LocalDate endDate,
            FishCategory fishCategory
    ) {
        this.id = id;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.fishCategory = fishCategory;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public FishCategory getFishCategory() {
        return fishCategory;
    }
}
