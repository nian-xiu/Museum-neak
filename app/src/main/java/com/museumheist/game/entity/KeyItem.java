package com.museumheist.game.entity;

import android.graphics.RectF;

/** Collectible access key that can be dropped and restored to its spawn point. */
public final class KeyItem {
    private final String keyCode;
    private final String label;
    private final int color;
    private final float startX;
    private final float startY;
    private final float radius;
    private final RectF bounds = new RectF();
    private boolean collected;

    public KeyItem(String code, String label, int color, float x, float y, float radius) {
        keyCode = code;
        this.label = label;
        this.color = color;
        startX = x;
        startY = y;
        this.radius = radius;
        setPosition(x, y);
    }

    private void setPosition(float x, float y) {
        bounds.set(x - radius, y - radius, x + radius, y + radius);
    }

    public RectF getBounds() {
        return bounds;
    }

    public String getKeyCode() {
        return keyCode;
    }

    public String getLabel() {
        return label;
    }

    public int getColor() {
        return color;
    }

    public boolean isCollected() {
        return collected;
    }

    public void collect() {
        collected = true;
    }

    public void dropAt(float x, float y) {
        setPosition(x, y);
        collected = false;
    }

    public void reset() {
        setPosition(startX, startY);
        collected = false;
    }
}
