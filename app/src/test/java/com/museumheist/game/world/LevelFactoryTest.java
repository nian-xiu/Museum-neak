package com.museumheist.game.world;

import com.museumheist.game.GameConfig;

import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LevelFactoryTest {
    @Test
    public void createsTenDistinctOpenScaleGalleries() {
        List<Level> levels = LevelFactory.createLevels();
        assertEquals(10, levels.size());

        Set<String> titles = new HashSet<>();
        float previousWidth = 0f;
        float previousHeight = 0f;
        for (int index = 0; index < levels.size(); index++) {
            Level level = levels.get(index);
            assertTrue(titles.add(level.getTitle()));
            assertEquals(2400f + index * 90f, level.getWorldWidth(), 0.001f);
            assertEquals(1350f + index * 50f, level.getWorldHeight(), 0.001f);
            assertTrue(level.getWorldWidth() > previousWidth);
            assertTrue(level.getWorldHeight() > previousHeight);
            assertTrue(level.getPlayerStartX() > 0f && level.getPlayerStartX() < level.getWorldWidth());
            assertTrue(level.getPlayerStartY() > 0f && level.getPlayerStartY() < level.getWorldHeight());
            assertTrue(level.getPlayerStartX() < level.getWorldWidth() * 0.15f);
            assertTrue(level.getPlayerStartY() > level.getWorldHeight() * 0.85f);
            assertTrue(level.getTreasureCount() >= 4);
            assertTrue(level.getGuards().size() >= 3);
            assertTrue(level.getCameras().size() >= 1);
            assertTrue(level.getLasers().size() >= 1);
            assertTrue(level.getPowerUpSpawnPoints().size() >= 1);
            previousWidth = level.getWorldWidth();
            previousHeight = level.getWorldHeight();
        }
    }

    @Test
    public void firstGalleryStartsWithMeaningfulSecurity() {
        Level first = LevelFactory.createLevels().get(0);
        assertEquals(3, first.getGuards().size());
        assertEquals(1, first.getCameras().size());
        assertEquals(1, first.getLasers().size());
        assertTrue(first.getWalls().size() >= 12);
    }

    @Test
    public void mapsExpandFromTwoPointTwoFiveToAboutFourTimesOriginalArea() {
        List<Level> levels = LevelFactory.createLevels();
        float originalArea = 1600f * 900f;
        float firstRatio = levels.get(0).getWorldWidth() * levels.get(0).getWorldHeight() / originalArea;
        Level last = levels.get(levels.size() - 1);
        float lastRatio = last.getWorldWidth() * last.getWorldHeight() / originalArea;
        assertEquals(2.25f, firstRatio, 0.001f);
        assertTrue(lastRatio >= 4f && lastRatio < 4.1f);
    }

    @Test
    public void runLimitIsSixMinutes() {
        assertEquals(360f, GameConfig.LEVEL_TIME_LIMIT_SECONDS, 0.001f);
    }
}
