package de.mainwetten.catchentry;

import java.math.BigDecimal;
import java.util.List;

public record GlobalCatchEntryResult(
        String fishSpeciesName,
        BigDecimal lengthCm,
        List<String> betTitles
) {

    public int assignmentCount() {
        return betTitles.size();
    }
}