package de.mainwetten.bet;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

import de.mainwetten.fish.FishCategory;

public class BetForm {

    @NotBlank(message = "Der Titel darf nicht leer sein.")
    @Size(max = 100, message = "Der Titel darf maximal 100 Zeichen lang sein.")
    private String title;

    @Size(max = 2000, message = "Die Beschreibung darf maximal 2000 Zeichen lang sein.")
    private String description;

    @NotNull(message = "Bitte gib ein Startdatum an.")
    @FutureOrPresent(message = "Das Startdatum darf nicht in der Vergangenheit liegen.")
    private LocalDate startDate;

    @NotNull(message = "Bitte gib ein Enddatum an.")
    @FutureOrPresent(message = "Das Enddatum darf nicht in der Vergangenheit liegen.")
    private LocalDate endDate;

    @NotNull(message = "Bitte wähle einen Bewertungsmodus aus.")
    private ScoringMode scoringMode;

    @NotNull(message = "Bitte wähle aus, welche Fischarten in der Wette zählen sollen.")
    private FishCategory fishCategory;

    public BetForm() {
        this.startDate = LocalDate.now();
        this.endDate = LocalDate.now().plusWeeks(1);
        this.scoringMode = ScoringMode.TOTAL_POINTS;
        this.fishCategory = FishCategory.ALL;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public ScoringMode getScoringMode() {
        return scoringMode;
    }

    public void setScoringMode(ScoringMode scoringMode) {
        this.scoringMode = scoringMode;
    }

    public FishCategory getFishCategory() {
        return fishCategory;
    }

    public void setFishCategory(FishCategory fishCategory) {
        this.fishCategory = fishCategory;
    }
}
