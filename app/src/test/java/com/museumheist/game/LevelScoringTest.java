package com.museumheist.game;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LevelScoringTest {
    @Test
    public void zero() {
        assertEquals(0, LevelScoring.calculateStars(false, false, 20f, 0));
    }

    @Test
    public void one() {
        assertEquals(1, LevelScoring.calculateStars(true, false, 20f, 0));
    }

    @Test
    public void two() {
        assertEquals(2, LevelScoring.calculateStars(
                true,
                true,
                GameConfig.THREE_STAR_TIME_SECONDS + 1f,
                0
        ));
        assertEquals(2, LevelScoring.calculateStars(true, true, 100f, 1));
    }

    @Test
    public void three() {
        assertEquals(3, LevelScoring.calculateStars(
                true,
                true,
                GameConfig.THREE_STAR_TIME_SECONDS,
                0
        ));
    }

    @Test
    public void perfectReward() {
        assertEquals(
                GameConfig.CLEAR_COIN_REWARD
                        + 3 * GameConfig.STAR_COIN_REWARD
                        + GameConfig.PERFECT_COIN_REWARD,
                LevelScoring.calculateRewardCoins(3, 0)
        );
    }

    @Test
    public void run() {
        assertEquals(
                LevelScoring.calculateRewardCoins(2, 0) + 75,
                LevelScoring.calculateRewardCoins(2, 75)
        );
        assertEquals(
                LevelScoring.calculateRewardCoins(2, 0),
                LevelScoring.calculateRewardCoins(2, -1)
        );
    }

    @Test
    public void clamp() {
        assertEquals(
                LevelScoring.calculateRewardCoins(3, 0),
                LevelScoring.calculateRewardCoins(99, 0)
        );
        assertEquals(
                GameConfig.CLEAR_COIN_REWARD,
                LevelScoring.calculateRewardCoins(-5, 0)
        );
    }
}
