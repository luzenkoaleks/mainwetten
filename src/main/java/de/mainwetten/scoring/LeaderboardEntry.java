package de.mainwetten.scoring;

import java.math.BigDecimal;

public class LeaderboardEntry {

    private final Long userId;
    private final String username;

    private int rank;
    private int points;
    private int caughtSpeciesPoints;
    private int biggestFishPoints;
    private int mostFishPoints;
    private int diversityPoints;
    private int speciesCount;

    private boolean tieBreakerRelevant;
    private BigDecimal tieBreakerLength = BigDecimal.ZERO;

    public LeaderboardEntry(Long userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public void addCaughtSpeciesPoints(int amount) {
        caughtSpeciesPoints += amount;
        points += amount;
    }

    public void addBiggestFishPoint() {
        biggestFishPoints++;
        points++;
    }

    public void addMostFishPoint() {
        mostFishPoints++;
        points++;
    }

    public void addDiversityPoint() {
        diversityPoints++;
        points++;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getPoints() {
        return points;
    }

    public int getCaughtSpeciesPoints() {
        return caughtSpeciesPoints;
    }

    public int getBiggestFishPoints() {
        return biggestFishPoints;
    }

    public int getMostFishPoints() {
        return mostFishPoints;
    }

    public int getDiversityPoints() {
        return diversityPoints;
    }

    public int getSpeciesCount() {
        return speciesCount;
    }

    public void setSpeciesCount(int speciesCount) {
        this.speciesCount = speciesCount;
    }

    public boolean isTieBreakerRelevant() {
        return tieBreakerRelevant;
    }

    public void setTieBreakerRelevant(boolean tieBreakerRelevant) {
        this.tieBreakerRelevant = tieBreakerRelevant;
    }

    public BigDecimal getTieBreakerLength() {
        return tieBreakerLength;
    }

    public void setTieBreakerLength(BigDecimal tieBreakerLength) {
        this.tieBreakerLength = tieBreakerLength;
    }
}