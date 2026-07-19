package com.museumheist.game;

public class LevelResult {
    private int stars;
    private int rewardCoins;
    private int collectedCoins;
    private int collectedTreasures;
    private int totalTreasures;
    private int alerts;
    private int usedPowerUps;
    private int bestStealthChain;
    private float elapsedSeconds;
    private float peakThreat;
    private boolean newRecord;

    public void reset() {
        stars = 0;
        rewardCoins = 0;
        collectedCoins = 0;
        collectedTreasures = 0;
        totalTreasures = 0;
        alerts = 0;
        usedPowerUps = 0;
        bestStealthChain = 0;
        elapsedSeconds = 0f;
        peakThreat = 0f;
        newRecord = false;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int value) {
        stars = value;
    }

    public int getRewardCoins() {
        return rewardCoins;
    }

    public void setRewardCoins(int value) {
        rewardCoins = value;
    }

    public int getCollectedCoins() {
        return collectedCoins;
    }

    public void addCollectedCoins(int value) {
        collectedCoins += Math.max(0, value);
    }

    public int getCollectedTreasures() {
        return collectedTreasures;
    }

    public void setCollectedTreasures(int value) {
        collectedTreasures = value;
    }

    public int getTotalTreasures() {
        return totalTreasures;
    }

    public void setTotalTreasures(int value) {
        totalTreasures = value;
    }

    public int getAlerts() {
        return alerts;
    }

    public void addAlert() {
        alerts++;
    }

    public int getUsedPowerUps() {
        return usedPowerUps;
    }

    public void addUsedPowerUp() {
        usedPowerUps++;
    }

    public int getBestStealthChain() {
        return bestStealthChain;
    }

    public void setBestStealthChain(int value) {
        bestStealthChain = Math.max(0, value);
    }

    public float getElapsedSeconds() {
        return elapsedSeconds;
    }

    public void setElapsedSeconds(float value) {
        elapsedSeconds = Math.max(0f, value);
    }

    public float getPeakThreat() {
        return peakThreat;
    }

    public void setPeakThreat(float value) {
        peakThreat = Math.max(0f, Math.min(1f, value));
    }

    public boolean isNewRecord() {
        return newRecord;
    }

    public void setNewRecord(boolean value) {
        newRecord = value;
    }
}