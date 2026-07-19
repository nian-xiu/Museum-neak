package com.museumheist.game.entity;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PlayerMovementVisualsTest {
    @Test
    public void gaitDoesNotAdvanceWithoutActualMovement() {
        Player player = new Player(20f, 305f);
        player.reset(100f, 100f);
        player.updateMovementVisuals(1f, 0f, 1f / 60f, 1f, 0f);
        assertEquals(0f, player.getGaitPhase(), 0.0001f);
    }

    @Test
    public void gaitAdvancesFromTravelDistance() {
        Player player = new Player(20f, 305f);
        player.reset(100f, 100f);
        player.updateMovementVisuals(1f, 0f, 1f / 60f, 1f, 6f);
        assertTrue(player.getGaitPhase() > 0f);
        assertTrue(player.getMovementBlend() > 0f);
    }

    @Test
    public void boostedMovementBlendsIntoRun() {
        Player player = new Player(20f, 305f);
        player.reset(100f, 100f);
        for (int i = 0; i < 30; i++) {
            player.updateMovementVisuals(1f, 0f, 1f / 60f, 1.5f, 7.6f);
        }
        assertTrue(player.getRunBlend() > 0.5f);
    }
}
