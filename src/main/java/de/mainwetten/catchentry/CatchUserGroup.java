package de.mainwetten.catchentry;

import java.util.List;

public class CatchUserGroup {

    private final String username;
    private final CatchRecord bestEntry;
    private final List<CatchRecord> entries;
    private final boolean overallBestForSpecies;

    public CatchUserGroup(
            String username,
            CatchRecord bestEntry,
            List<CatchRecord> entries,
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

    public CatchRecord getBestEntry() {
        return bestEntry;
    }

    public List<CatchRecord> getEntries() {
        return entries;
    }

    public int getCatchCount() {
        return entries.size();
    }

    public boolean isOverallBestForSpecies() {
        return overallBestForSpecies;
    }
}