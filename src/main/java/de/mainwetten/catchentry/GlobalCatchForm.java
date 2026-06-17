package de.mainwetten.catchentry;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.DecimalMax;

public class GlobalCatchForm {

    @NotNull(message = "Bitte wähle eine Fischart aus.")
    private Long fishSpeciesId;

    @NotNull(message = "Bitte gib die Länge ein.")
    @DecimalMin(value = "0.1", message = "Die Länge muss größer als 0 sein.")
    @DecimalMax(value = "999.9", message = "Die Länge darf maximal 999,9 cm betragen.")
    @Digits(integer = 3, fraction = 1, message = "Bitte gib maximal drei Vorkommastellen und eine Nachkommastelle ein")
    private BigDecimal lengthCm;

    @NotEmpty(message = "Bitte wähle mindestens eine Wette aus.")
    private List<Long> betIds = new ArrayList<>();

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

    public List<Long> getBetIds() {
        return betIds;
    }

    public void setBetIds(List<Long> betIds) {
        this.betIds = betIds;
    }
}
