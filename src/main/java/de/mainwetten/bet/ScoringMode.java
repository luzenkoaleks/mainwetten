package de.mainwetten.bet;

public enum ScoringMode {
    TOTAL_POINTS("Alle Fänge zählen"),
    BEST_PER_SPECIES("Bester Fang pro Fischart zählt");

    private final String displayName;

    ScoringMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
