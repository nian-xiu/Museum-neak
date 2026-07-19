package com.museumheist.game.logic;

import com.museumheist.game.GameConfig;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StealthTrackerTest {
    private static final float EPSILON = 0.0001f;

    @Test
    public void resetRestoresInitialState() {
        StealthTracker tracker = new StealthTracker();
        tracker.update(0.2f, 0.9f);
        tracker.registerPickup();

        tracker.reset();

        assertEquals(0f, tracker.getThreat(), EPSILON);
        assertEquals(0f, tracker.getDisplayedThreat(), EPSILON);
        assertEquals(0f, tracker.getPeakThreat(), EPSILON);
        assertEquals(0, tracker.getChain());
        assertEquals(0, tracker.getBestChain());
        assertEquals(StealthTracker.ThreatBand.CLEAR, tracker.getThreatBand());
        assertFalse(tracker.consumeDangerEntered());
    }

    @Test
    public void fourSafePickupsAwardFirstBonusTier() {
        StealthTracker tracker = new StealthTracker();
        tracker.update(0.016f, 0.05f);

        assertEquals(0, tracker.registerPickup());
        assertEquals(0, tracker.registerPickup());
        assertEquals(0, tracker.registerPickup());
        assertEquals(GameConfig.STEALTH_CHAIN_BONUS_STEP, tracker.registerPickup());
        assertEquals(GameConfig.STEALTH_CHAIN_STEP, tracker.getChain());
        assertEquals(GameConfig.STEALTH_CHAIN_STEP, tracker.getBestChain());
        assertEquals(1f, tracker.getChainProgress(), EPSILON);
    }

    @Test
    public void highThreatBreaksAndBlocksChain() {
        StealthTracker tracker = new StealthTracker();
        tracker.registerPickup();
        tracker.registerPickup();

        tracker.update(0.1f, GameConfig.STEALTH_CHAIN_BREAK_THREAT);

        assertEquals(0, tracker.getChain());
        assertEquals(0f, tracker.getChainProgress(), EPSILON);
        assertEquals(0, tracker.registerPickup());
        assertEquals(0, tracker.getChain());
        assertEquals(2, tracker.getBestChain());
    }

    @Test
    public void dangerEntryCanOnlyBeConsumedOncePerEntry() {
        StealthTracker tracker = new StealthTracker();
        tracker.update(0.1f, GameConfig.STEALTH_DANGER_THRESHOLD);

        assertTrue(tracker.consumeDangerEntered());
        assertFalse(tracker.consumeDangerEntered());

        tracker.update(0.1f, 0.9f);
        assertFalse(tracker.consumeDangerEntered());

        tracker.update(0.1f, 0f);
        tracker.update(0.1f, GameConfig.STEALTH_DANGER_THRESHOLD + 0.01f);
        assertTrue(tracker.consumeDangerEntered());
    }

    @Test
    public void peakThreatNeverFalls() {
        StealthTracker tracker = new StealthTracker();
        tracker.update(0.1f, 0.76f);
        tracker.update(0.1f, 0.24f);
        tracker.update(0.1f, 0f);

        assertEquals(0.76f, tracker.getPeakThreat(), EPSILON);
        assertEquals(0f, tracker.getThreat(), EPSILON);
    }

    @Test
    public void threatAndDeltaAreClamped() {
        StealthTracker tracker = new StealthTracker();
        tracker.update(-5f, 2f);

        assertEquals(1f, tracker.getThreat(), EPSILON);
        assertEquals(0f, tracker.getDisplayedThreat(), EPSILON);
        assertEquals(1f, tracker.getPeakThreat(), EPSILON);
        assertEquals(StealthTracker.ThreatBand.DANGER, tracker.getThreatBand());

        tracker.update(1f, -3f);
        assertEquals(0f, tracker.getThreat(), EPSILON);
        assertEquals(0f, tracker.getDisplayedThreat(), EPSILON);
        assertEquals(StealthTracker.ThreatBand.CLEAR, tracker.getThreatBand());
    }

    @Test
    public void chainExpiresAfterConfiguredWindow() {
        StealthTracker tracker = new StealthTracker();
        tracker.registerPickup();

        tracker.update(GameConfig.STEALTH_CHAIN_SECONDS + 0.1f, 0f);

        assertEquals(0, tracker.getChain());
        assertEquals(0f, tracker.getChainProgress(), EPSILON);
        assertEquals(1, tracker.getBestChain());
    }
}
