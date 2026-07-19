package com.museumheist.game.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import java.util.ArrayList;
import java.util.List;

public class FloatingText {
    private static final float LIFE_SECONDS = 1.2f;

    private static final class Entry {
        private final String text;
        private final int color;
        private float x;
        private float y;
        private float remainingSeconds = LIFE_SECONDS;

        private Entry(String text, float x, float y, int color) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.color = color;
        }
    }

    private final List<Entry> items = new ArrayList<>();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public FloatingText() {
        paint.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
    }

    public void add(String text, float x, float y, int color) {
        items.add(new Entry(text, x, y, color));
    }

    public void update(float deltaSeconds) {
        float safeDelta = Math.max(0f, deltaSeconds);
        for (int i = items.size() - 1; i >= 0; i--) {
            Entry entry = items.get(i);
            entry.remainingSeconds -= safeDelta;
            entry.y -= 24f * safeDelta;
            if (entry.remainingSeconds <= 0f) {
                items.remove(i);
            }
        }
    }

    public void draw(Canvas canvas) {
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(18f);
        for (Entry entry : items) {
            float life = Math.max(0f, Math.min(1f, entry.remainingSeconds / LIFE_SECONDS));
            int alpha = Math.round(255f * Math.min(1f, life * 1.7f));
            paint.setColor(Color.argb(
                    alpha,
                    Color.red(entry.color),
                    Color.green(entry.color),
                    Color.blue(entry.color)
            ));
            canvas.drawText(entry.text, entry.x, entry.y, paint);
        }
        paint.setTextAlign(Paint.Align.LEFT);
    }

    public void clear() {
        items.clear();
    }
}