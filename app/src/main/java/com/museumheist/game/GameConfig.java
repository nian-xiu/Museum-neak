package com.museumheist.game;

/** Central gameplay tuning values. Keep this class Android-free so logic remains unit-testable. */
public final class GameConfig {
    public static final float PLAYER_SPEED = 305f;
    public static final float BASE_PLAYER_SPEED = PLAYER_SPEED;
    public static final float PLAYER_RADIUS = 20f;
    public static final float JOYSTICK_RADIUS_SCREEN_FRACTION = 0.09f;
    public static final float JOYSTICK_DEAD_ZONE = 0.12f;

    public static final float BOOST_MULTIPLIER = 1.5f;
    public static final float BOOST_CHARGE_SECONDS = 10f;
    public static final float BOOST_FULL_USE_SECONDS = 5f;

    public static final float POWER_UP_LIFETIME_SECONDS = 8f;
    public static final float POWER_UP_SPAWN_SECONDS = 6.5f;
    public static final float POWER_UP_EFFECT_SECONDS = 3f;
    public static final int POWER_UP_SLOT_COUNT = 2;
    public static final int POWER_UP_FIELD_TARGET = 3;

    public static final float DECOY_DURATION_SECONDS = 5.5f;
    public static final float SPEED_POTION_SECONDS = 5f;
    public static final float SPEED_POTION_MULTIPLIER = 2f;
    public static final float DISRUPTOR_DURATION_SECONDS = 8f;
    public static final float DISRUPTOR_RADIUS = 260f;
    public static final float BASE_DISRUPTOR_RADIUS = DISRUPTOR_RADIUS;

    public static final float TREASURE_BOOST_REWARD = 0.22f;
    public static final float TREASURE_CLAIM_SECONDS = 4f;
    public static final float BASE_TREASURE_CLAIM_SECONDS = TREASURE_CLAIM_SECONDS;
    public static final float LEVEL_TIME_LIMIT_SECONDS = 360f;
    public static final float EXIT_AUTO_CONFIRM_SECONDS = 5f;
    public static final int HOTBAR_SLOT_COUNT = 5;
    public static final float DOOR_INTERACT_RADIUS = 88f;

    public static final int STARTING_COINS = 120;
    public static final int COIN_FIELD_BASE_COUNT = 22;
    public static final int COIN_FIELD_LEVEL_BONUS = 6;
    public static final int COIN_VALUE_MIN = 8;
    public static final int COIN_VALUE_MAX = 24;
    public static final int TREASURE_COIN_REWARD = 60;

    public static final float SUSPICION_THRESHOLD_SECONDS = 1.05f;
    public static final float STAFF_SUSPICION_THRESHOLD_SECONDS = 1.35f;
    public static final float CAMERA_SUSPICION_THRESHOLD_SECONDS = 1.20f;
    public static final float SUSPICION_DECAY_SECONDS = 0.9f;
    public static final float INVESTIGATE_WAIT_SECONDS = 1.6f;

    public static final float STEALTH_CHAIN_SECONDS = 6.5f;
    public static final float STEALTH_CHAIN_BREAK_THREAT = 0.58f;
    public static final float STEALTH_DANGER_THRESHOLD = 0.72f;
    public static final int STEALTH_CHAIN_STEP = 4;
    public static final int STEALTH_CHAIN_BONUS_STEP = 5;
    public static final int STEALTH_CHAIN_MAX_BONUS = 25;

    public static final float THREE_STAR_TIME_SECONDS = 220f;
    public static final int CLEAR_COIN_REWARD = 40;
    public static final int STAR_COIN_REWARD = 35;
    public static final int PERFECT_COIN_REWARD = 55;
    public static final int UPGRADE_MAX_LEVEL = 3;

    private GameConfig() {
    }
}

