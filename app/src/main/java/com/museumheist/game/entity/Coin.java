package com.museumheist.game.entity;

import android.graphics.RectF;

/** Immutable coin placement plus its runtime collection state. */
public final class Coin {
    private final RectF bounds;
    private final int value;
    private boolean collected;

    public Coin(float x, float y, float radius, int value) {
        bounds = new RectF(x - radius, y - radius, x + radius, y + radius);
        this.value = Math.max(0, value);
    }

    public RectF getBounds() {
        return bounds;
    }

    public int getValue() {
        return value;
    }

    public boolean isCollected() {
        return collected;
    }

    public void collect() {
        collected = true;
    }
}
