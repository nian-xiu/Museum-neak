package com.museumheist.game.logic;

import com.museumheist.game.GameConfig;

/**
 * Tracks player-facing detection pressure and rewards uninterrupted, low-risk collection chains.
 * This class deliberately has no Android dependency so it can be tested on the JVM.
 */
public final class StealthTracker {
    public enum ThreatBand {
        CLEAR,
        WATCHED,
        DANGER
    }

    private float threat;
    private float displayedThreat;
    private float peakThreat;
    private float chainSeconds;
    private int chain;
    private int bestChain;
    private ThreatBand band = ThreatBand.CLEAR;
    private boolean dangerEntered;

    public void reset() {
        threat = 0f;
        displayedThreat = 0f;
        peakThreat = 0f;
        chainSeconds = 0f;
        chain = 0;
        bestChain = 0;
        band = ThreatBand.CLEAR;
        dangerEntered = false;
    }

    public void update(float deltaSeconds, float measuredThreat) {
        float dt = Math.max(0f, deltaSeconds);
        threat = clamp(measuredThreat, 0f, 1f);
        peakThreat = Math.max(peakThreat, threat);

        float response = threat > displayedThreat ? 10f : 4.5f;
        displayedThreat += (threat - displayedThreat) * clamp(dt * response, 0f, 1f);

        ThreatBand previous = band;
        if (threat >= GameConfig.STEALTH_DANGER_THRESHOLD) {
            band = ThreatBand.DANGER;
        } else if (threat >= 0.18f) {
            band = ThreatBand.WATCHED;
        } else {
            band = ThreatBand.CLEAR;
        }
        dangerEntered = previous != ThreatBand.DANGER && band == ThreatBand.DANGER;

        if (threat >= GameConfig.STEALTH_CHAIN_BREAK_THREAT) {
            clearChain();
            return;
        }
        if (chainSeconds > 0f) {
            chainSeconds = Math.max(0f, chainSeconds - dt);
            if (chainSeconds == 0f) {
                chain = 0;
            }
        }
    }

    /** Returns the immediate bonus earned by this safe pickup. */
    public int registerPickup() {
        if (threat >= GameConfig.STEALTH_CHAIN_BREAK_THREAT) {
            clearChain();
            return 0;
        }
        chain++;
        bestChain = Math.max(bestChain, chain);
        chainSeconds = GameConfig.STEALTH_CHAIN_SECONDS;
        if (chain % GameConfig.STEALTH_CHAIN_STEP != 0) {
            return 0;
        }
        int tier = chain / GameConfig.STEALTH_CHAIN_STEP;
        return Math.min(GameConfig.STEALTH_CHAIN_MAX_BONUS, tier * GameConfig.STEALTH_CHAIN_BONUS_STEP);
    }

    public void breakChain() {
        clearChain();
    }

    public boolean consumeDangerEntered() {
        boolean result = dangerEntered;
        dangerEntered = false;
        return result;
    }

    public float getThreat() {
        return threat;
    }

    public float getDisplayedThreat() {
        return displayedThreat;
    }

    public float getPeakThreat() {
        return peakThreat;
    }

    public float getChainProgress() {
        return chain <= 0 ? 0f : clamp(chainSeconds / GameConfig.STEALTH_CHAIN_SECONDS, 0f, 1f);
    }

    public int getChain() {
        return chain;
    }

    public int getBestChain() {
        return bestChain;
    }

    public ThreatBand getThreatBand() {
        return band;
    }

    private void clearChain() {
        chain = 0;
        chainSeconds = 0f;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
