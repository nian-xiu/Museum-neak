package com.museumheist.game;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExitRulesTest {
    @Test
    public void requiresAtLeastOneTreasure() {
        assertFalse(ExitRules.canExit(0));
        assertFalse(ExitRules.canExit(-1));
        assertTrue(ExitRules.canExit(1));
        assertTrue(ExitRules.canExit(4));
    }

    @Test
    public void gameStateBecomesEligibleAfterFirstTreasure() {
        GameState state = new GameState();
        state.reset(4);
        assertFalse(ExitRules.canExit(state));
        assertTrue(state.collectTreasure(2));
        assertTrue(ExitRules.canExit(state));
    }

    @Test
    public void nullStateCannotExit() {
        assertFalse(ExitRules.canExit((GameState) null));
    }
}
