package com.museumheist.game.progress;

/** Campaign selection rules. Every gallery is available from the first launch. */
public final class ProgressionRules {
    private ProgressionRules() {
    }

    public static boolean isLevelUnlocked(int index, int[] stars) {
        return stars != null && index >= 0 && index < stars.length;
    }

    public static int highestUnlockedLevelIndex(int[] stars) {
        return stars == null || stars.length == 0 ? -1 : stars.length - 1;
    }

    public static int totalStars(int[] stars) {
        if (stars == null) return 0;
        int total = 0;
        for (int star : stars) {
            total += Math.max(0, Math.min(3, star));
        }
        return total;
    }
}
