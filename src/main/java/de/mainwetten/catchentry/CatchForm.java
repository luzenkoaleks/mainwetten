package de.mainwetten.catchentry;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CatchForm {

    @NotNull(message = "Bitte wähle eine Fischart aus.")
    private Long fishSpeciesId;

    @NotNull(message = "Bitte gib die Länge an.")
    @DecimalMin(value = "1.0", message = "Die Länge muss mindestens 1 cm betragen.")
    @Digits(integer = 3, fraction = 1, message = "Bitte gib maximal eine Nachkommastelle ein.")
    private BigDecimal lengthCm;

    public Long getFishSpeciesId() {
        return fishSpeciesId;
    }

    public void setFishSpeciesId(Long fishSpeciesId) {
        this.fishSpeciesId = fishSpeciesId;
    }

    public BigDecimal getLengthCm() {
        return lengthCm;
    }

    public void setLengthCm(BigDecimal lengthCm) {
        this.lengthCm = lengthCm;
    }
}