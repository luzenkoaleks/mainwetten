package de.mainwetten.catchentry;

import java.util.List;

public class CatchSpeciesGroup {

    private final String fishSpeciesName;
    private final List<CatchUserGroup> userGroups;

    public CatchSpeciesGroup(String fishSpeciesName, List<CatchUserGroup> userGroups) {
        this.fishSpeciesName = fishSpeciesName;
        this.userGroups = userGroups;
    }

    public String getFishSpeciesName() {
        return fishSpeciesName;
    }

    public List<CatchUserGroup> getUserGroups() {
        return userGroups;
    }
}
