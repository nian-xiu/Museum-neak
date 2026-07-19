package com.museumheist.game.progress;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ProgressionRulesTest {
    @Test public void everyValidGalleryIsAvailableImmediately() {
        int[] stars = new int[]{0, 0, 0, 0};
        for (int index = 0; index < stars.length; index++) {
            assertTrue(ProgressionRules.isLevelUnlocked(index, stars));
        }
    }

    @Test public void invalidGalleryIndicesStayRejected() {
        assertFalse(ProgressionRules.isLevelUnlocked(-1, new int[]{0}));
        assertFalse(ProgressionRules.isLevelUnlocked(1, new int[]{0}));
        assertFalse(ProgressionRules.isLevelUnlocked(0, null));
    }

    @Test public void highestUnlockedIsLastGallery() {
        assertEquals(3, ProgressionRules.highestUnlockedLevelIndex(new int[]{0, 0, 0, 0}));
        assertEquals(-1, ProgressionRules.highestUnlockedLevelIndex(new int[]{}));
    }

    @Test public void totalStarsClampsValues() {
        assertEquals(6, ProgressionRules.totalStars(new int[]{-2, 2, 9, 1}));
    }
}
