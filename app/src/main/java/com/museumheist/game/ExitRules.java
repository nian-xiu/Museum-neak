package com.museumheist.game;

/** Rules for activating a gallery exit. */
public final class ExitRules {
    private ExitRules() {
    }

    public static boolean canExit(int collectedTreasureCount) {
        return collectedTreasureCount >= 1;
    }

    public static boolean canExit(GameState state) {
        return state != null && canExit(state.getCollectedTreasureCount());
    }
}
