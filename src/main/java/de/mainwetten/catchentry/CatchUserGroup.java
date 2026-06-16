package de.mainwetten.catchentry;

import java.util.List;

public class CatchUserGroup {

    private final String username;
    private final CatchEntry bestEntry;
    private final List<CatchEntry> entries;
    private final boolean overallBestForSpecies;

    public CatchUserGroup(
            String username,
            CatchEntry bestEntry,
            List<CatchEntry> entries,
            boolean overallBestForSpecies
    ) {
        this.username = username;
        this.bestEntry = bestEntry;
        this.entries = entries;
        this.overallBestForSpecies = overallBestForSpecies;
    }

    public String getUsername() {
        return username;
    }

    public CatchEntry getBestEntry() {
        return bestEntry;
    }

    public List<CatchEntry> getEntries() {
        return entries;
    }

    public int getCatchCount() {
        return entries.size();
    }

    public boolean isOverallBestForSpecies() {
        return overallBestForSpecies;
    }
}