package de.mainwetten.fish;

public enum FishCategory {
    ALL("Alle Fischarten"),
    FRESHWATER("Süßwasser"),
    SALTWATER("Salzwasser");

    private final String displayName;

    FishCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
