package com.museumheist.game.entity;

import android.graphics.RectF;

public class PowerUp {
    public enum Type {
        CLOAK("光学斗篷", "隐"),
        PHASE("相位手套", "相"),
        SPEED("疾行针剂", "速"),
        JAMMER("电磁干扰器", "扰"),
        DECOY("全息诱饵", "诱");

        private final String label;
        private final String badge;

        Type(String label, String badge) {
            this.label = label;
            this.badge = badge;
        }

        public String getLabel() {
            return label;
        }

        public String getBadge() {
            return badge;
        }
    }

    private final Type type;
    private final RectF bounds;
    private float remainingSeconds;

    public PowerUp(Type type, float x, float y, float radius, float seconds) {
        this.type = type;
        bounds = new RectF(x - radius, y - radius, x + radius, y + radius);
        remainingSeconds = seconds;
    }

    public Type getType() {
        return type;
    }

    public RectF getBounds() {
        return bounds;
    }

    public float getRemainingSeconds() {
        return remainingSeconds;
    }

    public boolean update(float dt) {
        remainingSeconds = Math.max(0, remainingSeconds - dt);
        return remainingSeconds > 0;
    }
}