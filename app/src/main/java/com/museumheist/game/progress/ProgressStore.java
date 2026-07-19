package com.museumheist.game.progress;

import android.content.Context;
import android.content.SharedPreferences;

import com.museumheist.game.GameConfig;
import com.museumheist.game.entity.PowerUp;

public final class ProgressStore {
    private static final String NAME = "museum_heist_progress";
    private static final String STARS = "level_stars_";
    private static final String COINS = "coins";
    private static final String STASH = "stash_";
    private static final String UPGRADE = "upgrade_";
    private static final String LOADOUT = "loadout_";
    private static final String ROUTES = "setting_show_patrol_routes";
    private static final String CONTRAST = "setting_high_contrast_vision";
    private static final String SOUND = "setting_sound_feedback";
    private static final String HAPTIC = "setting_haptic_feedback";
    private static final String REDUCE_MOTION = "setting_reduce_motion";
    private static final String LARGE_TEXT = "setting_large_text";

    private final SharedPreferences preferences;
    private final int levelCount;

    public ProgressStore(Context context, int levelCount) {
        preferences = context.getApplicationContext().getSharedPreferences(NAME, Context.MODE_PRIVATE);
        this.levelCount = levelCount;
    }

    public void loadBestStars(int[] target) {
        int count = Math.min(levelCount, target.length);
        for (int i = 0; i < count; i++) {
            target[i] = clamp(preferences.getInt(STARS + i, 0), 0, 3);
        }
        for (int i = count; i < target.length; i++) {
            target[i] = 0;
        }
    }

    public int loadCoins() {
        return Math.max(0, preferences.getInt(COINS, GameConfig.STARTING_COINS));
    }

    public void saveCoins(int coins) {
        preferences.edit().putInt(COINS, Math.max(0, coins)).apply();
    }

    public void loadShopStock(int[] target) {
        for (PowerUp.Type type : PowerUp.Type.values()) {
            if (type.ordinal() < target.length) {
                target[type.ordinal()] = clamp(preferences.getInt(STASH + type.name(), 0), 0, 99);
            }
        }
    }

    public void loadUpgradeLevels(int[] target) {
        for (UpgradeType type : UpgradeType.values()) {
            if (type.ordinal() < target.length) {
                target[type.ordinal()] = clamp(
                        preferences.getInt(UPGRADE + type.name(), 0),
                        0,
                        GameConfig.UPGRADE_MAX_LEVEL
                );
            }
        }
    }

    public void loadLoadout(PowerUp.Type[] target) {
        for (int i = 0; i < target.length; i++) {
            String value = preferences.getString(LOADOUT + i, "");
            try {
                target[i] = value == null || value.isEmpty() ? null : PowerUp.Type.valueOf(value);
            } catch (IllegalArgumentException exception) {
                target[i] = null;
            }
        }
    }

    public void saveLoadout(PowerUp.Type[] loadout) {
        SharedPreferences.Editor editor = preferences.edit();
        for (int i = 0; i < GameConfig.POWER_UP_SLOT_COUNT; i++) {
            PowerUp.Type type = i < loadout.length ? loadout[i] : null;
            if (type == null) {
                editor.remove(LOADOUT + i);
            } else {
                editor.putString(LOADOUT + i, type.name());
            }
        }
        editor.apply();
    }

    public boolean loadShowPatrolRoutes() {
        return preferences.getBoolean(ROUTES, true);
    }

    public boolean loadHighContrastVision() {
        return preferences.getBoolean(CONTRAST, false);
    }

    public boolean loadSoundFeedbackEnabled() {
        return preferences.getBoolean(SOUND, true);
    }

    public boolean loadHapticFeedbackEnabled() {
        return preferences.getBoolean(HAPTIC, true);
    }

    public boolean loadReduceMotion() {
        return preferences.getBoolean(REDUCE_MOTION, false);
    }

    public boolean loadLargeText() {
        return preferences.getBoolean(LARGE_TEXT, false);
    }

    public void saveSettings(
            boolean showRoutes,
            boolean highContrast,
            boolean sound,
            boolean haptic,
            boolean reduceMotion,
            boolean largeText
    ) {
        preferences.edit()
                .putBoolean(ROUTES, showRoutes)
                .putBoolean(CONTRAST, highContrast)
                .putBoolean(SOUND, sound)
                .putBoolean(HAPTIC, haptic)
                .putBoolean(REDUCE_MOTION, reduceMotion)
                .putBoolean(LARGE_TEXT, largeText)
                .apply();
    }

    public void saveShopStock(PowerUp.Type type, int count) {
        preferences.edit().putInt(STASH + type.name(), clamp(count, 0, 99)).apply();
    }

    public void saveShopStock(int[] stock) {
        SharedPreferences.Editor editor = preferences.edit();
        for (PowerUp.Type type : PowerUp.Type.values()) {
            if (type.ordinal() < stock.length) {
                editor.putInt(STASH + type.name(), clamp(stock[type.ordinal()], 0, 99));
            }
        }
        editor.apply();
    }

    public void commitPowerUpPurchase(PowerUp.Type type, int coins, int stock) {
        preferences.edit()
                .putInt(COINS, Math.max(0, coins))
                .putInt(STASH + type.name(), clamp(stock, 0, 99))
                .apply();
    }

    public void commitUpgradePurchase(UpgradeType type, int coins, int level) {
        preferences.edit()
                .putInt(COINS, Math.max(0, coins))
                .putInt(UPGRADE + type.name(), clamp(level, 0, GameConfig.UPGRADE_MAX_LEVEL))
                .apply();
    }

    public void saveUpgradeLevel(UpgradeType type, int level) {
        preferences.edit()
                .putInt(UPGRADE + type.name(), clamp(level, 0, GameConfig.UPGRADE_MAX_LEVEL))
                .apply();
    }

    public void saveLevelStars(int index, int stars) {
        preferences.edit().putInt(STARS + index, clamp(stars, 0, 3)).apply();
    }

    public void resetLevelStars(int[] target) {
        SharedPreferences.Editor editor = preferences.edit();
        for (int i = 0; i < Math.min(levelCount, target.length); i++) {
            target[i] = 0;
            editor.remove(STARS + i);
        }
        editor.apply();
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}