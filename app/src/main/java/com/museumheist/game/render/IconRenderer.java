package com.museumheist.game.render;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.museumheist.game.entity.PowerUp;

/** Detailed vector icons used by collectibles, loadout cards and the HUD. */
public final class IconRenderer {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final RectF rect = new RectF();

    public void drawKey(Canvas canvas, float x, float y, float size, int color) {
        float stroke = Math.max(2f, size * 0.18f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(stroke + Math.max(1f, size * 0.08f));
        paint.setColor(Color.argb(80, 0, 0, 0));
        canvas.drawCircle(x - size * 0.34f + size * 0.04f, y + size * 0.05f, size * 0.30f, paint);
        canvas.drawLine(x, y + size * 0.05f, x + size * 0.72f, y + size * 0.05f, paint);

        paint.setStrokeWidth(stroke);
        paint.setColor(color);
        canvas.drawCircle(x - size * 0.34f, y, size * 0.30f, paint);
        canvas.drawCircle(x - size * 0.34f, y, size * 0.10f, paint);
        canvas.drawLine(x - size * 0.03f, y, x + size * 0.74f, y, paint);
        canvas.drawLine(x + size * 0.38f, y, x + size * 0.38f, y + size * 0.30f, paint);
        canvas.drawLine(x + size * 0.61f, y, x + size * 0.61f, y + size * 0.22f, paint);
        paint.setStrokeWidth(Math.max(1f, size * 0.055f));
        paint.setColor(Color.argb(180, 255, 255, 255));
        canvas.drawArc(x - size * 0.58f, y - size * 0.25f, x - size * 0.10f, y + size * 0.23f,
                208f, 104f, false, paint);
        canvas.drawLine(x + size * 0.02f, y - size * 0.04f, x + size * 0.56f, y - size * 0.04f, paint);
        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setStyle(Paint.Style.FILL);
    }

    public void drawCoin(Canvas canvas, float x, float y, float radius, float phase,
                         boolean premium, boolean shine) {
        int outer = premium ? Color.rgb(246, 205, 70) : Color.rgb(226, 174, 52);
        int inner = premium ? Color.rgb(255, 228, 116) : Color.rgb(247, 202, 84);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(62, 0, 0, 0));
        canvas.drawOval(x - radius * 0.82f, y + radius * 0.62f,
                x + radius * 0.82f, y + radius * 1.02f, paint);
        paint.setColor(Color.rgb(119, 79, 24));
        canvas.drawCircle(x + radius * 0.08f, y + radius * 0.10f, radius, paint);
        paint.setColor(outer);
        canvas.drawCircle(x, y, radius, paint);
        paint.setColor(inner);
        canvas.drawCircle(x, y, radius * 0.76f, paint);
        paint.setColor(Color.argb(52, 119, 79, 24));
        canvas.drawCircle(x, y, radius * 0.54f, paint);

        path.reset();
        path.moveTo(x, y - radius * 0.45f);
        path.lineTo(x + radius * 0.33f, y);
        path.lineTo(x, y + radius * 0.45f);
        path.lineTo(x - radius * 0.33f, y);
        path.close();
        paint.setColor(premium ? Color.rgb(255, 238, 156) : Color.rgb(255, 222, 119));
        canvas.drawPath(path, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1.4f, radius * 0.11f));
        paint.setColor(Color.argb(170, 255, 247, 204));
        canvas.drawArc(x - radius * 0.72f, y - radius * 0.72f,
                x + radius * 0.72f, y + radius * 0.72f, 202f, 118f, false, paint);
        if (shine) {
            float sparkle = 0.72f + 0.20f * (float) Math.sin(phase);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(Math.max(1.2f, radius * 0.08f));
            paint.setColor(Color.argb(210, 255, 255, 232));
            canvas.drawLine(x - radius * 0.56f, y - radius * sparkle,
                    x - radius * 0.56f, y - radius * 0.35f, paint);
            canvas.drawLine(x - radius * 0.78f, y - radius * 0.56f,
                    x - radius * 0.34f, y - radius * 0.56f, paint);
            paint.setStrokeCap(Paint.Cap.BUTT);
        }
        paint.setStyle(Paint.Style.FILL);
    }

    public void drawPowerBase(Canvas canvas, float x, float y, float radius, int color, float life) {
        float pulse = 1f + 0.03f * (float) Math.sin(life * 12f);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(withAlpha(color, 28));
        canvas.drawCircle(x, y, radius * 1.62f * pulse, paint);
        paint.setColor(Color.argb(72, 0, 0, 0));
        canvas.drawCircle(x + radius * 0.08f, y + radius * 0.12f, radius * 1.08f, paint);

        path.reset();
        for (int i = 0; i < 6; i++) {
            double angle = -Math.PI / 2d + i * Math.PI / 3d;
            float px = x + (float) Math.cos(angle) * radius;
            float py = y + (float) Math.sin(angle) * radius;
            if (i == 0) path.moveTo(px, py); else path.lineTo(px, py);
        }
        path.close();
        paint.setColor(color);
        canvas.drawPath(path, paint);
        paint.setColor(withAlpha(Color.WHITE, 38));
        path.reset();
        path.moveTo(x, y - radius * 0.86f);
        path.lineTo(x + radius * 0.72f, y - radius * 0.43f);
        path.lineTo(x, y - radius * 0.10f);
        path.lineTo(x - radius * 0.72f, y - radius * 0.43f);
        path.close();
        canvas.drawPath(path, paint);
        paint.setColor(Color.argb(115, 10, 16, 22));
        canvas.drawCircle(x, y, radius * 0.60f, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(Math.max(2f, radius * 0.10f));
        paint.setColor(withAlpha(Color.WHITE, 220));
        rect.set(x - radius * 1.27f, y - radius * 1.27f,
                x + radius * 1.27f, y + radius * 1.27f);
        canvas.drawArc(rect, -90f, 360f * life, false, paint);
        paint.setStrokeWidth(Math.max(1.2f, radius * 0.055f));
        paint.setColor(withAlpha(color, 205));
        for (int i = 0; i < 6; i++) {
            double angle = i * Math.PI / 3d;
            float ax = x + (float) Math.cos(angle) * radius * 1.34f;
            float ay = y + (float) Math.sin(angle) * radius * 1.34f;
            float bx = x + (float) Math.cos(angle) * radius * 1.49f;
            float by = y + (float) Math.sin(angle) * radius * 1.49f;
            canvas.drawLine(ax, ay, bx, by, paint);
        }
        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setStyle(Paint.Style.FILL);
    }


    /** Draws a distinct, allocation-free symbol for each usable gadget. */
    public void drawPowerSymbol(Canvas canvas, PowerUp.Type type, float x, float y,
                                float size, int color) {
        paint.setColor(color);
        paint.setStrokeCap(Paint.Cap.BUTT);
        switch (type) {
            case CLOAK:
                paint.setStyle(Paint.Style.FILL);
                path.reset();
                path.moveTo(x, y - size);
                path.quadTo(x - size * 0.92f, y - size * 0.18f,
                        x - size * 0.58f, y + size * 0.88f);
                path.lineTo(x - size * 0.12f, y + size * 0.62f);
                path.lineTo(x + size * 0.12f, y + size * 0.62f);
                path.lineTo(x + size * 0.58f, y + size * 0.88f);
                path.quadTo(x + size * 0.92f, y - size * 0.18f, x, y - size);
                path.close();
                canvas.drawPath(path, paint);
                paint.setColor(withAlpha(Color.WHITE, 138));
                canvas.drawCircle(x - size * 0.19f, y - size * 0.22f, size * 0.10f, paint);
                canvas.drawCircle(x + size * 0.19f, y - size * 0.22f, size * 0.10f, paint);
                break;
            case PHASE:
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeCap(Paint.Cap.ROUND);
                paint.setStrokeWidth(Math.max(2f, size * 0.15f));
                canvas.drawCircle(x - size * 0.29f, y + size * 0.25f, size * 0.40f, paint);
                canvas.drawCircle(x + size * 0.31f, y - size * 0.27f, size * 0.40f, paint);
                canvas.drawLine(x - size * 0.77f, y + size * 0.77f,
                        x + size * 0.77f, y - size * 0.77f, paint);
                paint.setColor(withAlpha(Color.WHITE, 130));
                paint.setStrokeWidth(Math.max(1f, size * 0.07f));
                canvas.drawLine(x - size * 0.58f, y + size * 0.23f,
                        x + size * 0.20f, y - size * 0.55f, paint);
                break;
            case SPEED:
                paint.setStyle(Paint.Style.FILL);
                rect.set(x - size * 0.45f, y - size * 0.82f,
                        x + size * 0.45f, y + size * 0.82f);
                canvas.drawRoundRect(rect, size * 0.34f, size * 0.34f, paint);
                paint.setColor(withAlpha(Color.WHITE, 112));
                rect.set(x - size * 0.30f, y - size * 0.66f,
                        x - size * 0.08f, y + size * 0.35f);
                canvas.drawRoundRect(rect, size * 0.10f, size * 0.10f, paint);
                paint.setColor(Color.rgb(16, 22, 24));
                path.reset();
                path.moveTo(x + size * 0.12f, y - size * 0.52f);
                path.lineTo(x - size * 0.25f, y + size * 0.05f);
                path.lineTo(x + size * 0.02f, y + size * 0.05f);
                path.lineTo(x - size * 0.10f, y + size * 0.52f);
                path.lineTo(x + size * 0.34f, y - size * 0.12f);
                path.lineTo(x + size * 0.08f, y - size * 0.12f);
                path.close();
                canvas.drawPath(path, paint);
                break;
            case JAMMER:
                drawJammerSymbol(canvas, x, y, size * 0.82f, color);
                break;
            case DECOY:
                paint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(x, y - size * 0.34f, size * 0.28f, paint);
                path.reset();
                path.moveTo(x - size * 0.65f, y + size * 0.68f);
                path.quadTo(x - size * 0.55f, y + size * 0.05f, x, y + size * 0.02f);
                path.quadTo(x + size * 0.55f, y + size * 0.05f, x + size * 0.65f, y + size * 0.68f);
                path.close();
                canvas.drawPath(path, paint);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeCap(Paint.Cap.ROUND);
                paint.setStrokeWidth(Math.max(1f, size * 0.08f));
                paint.setColor(withAlpha(Color.WHITE, 135));
                canvas.drawLine(x - size * 0.42f, y + size * 0.24f,
                        x + size * 0.42f, y + size * 0.24f, paint);
                canvas.drawLine(x - size * 0.30f, y + size * 0.46f,
                        x + size * 0.30f, y + size * 0.46f, paint);
                break;
        }
        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setStyle(Paint.Style.FILL);
    }

    /** Compact electromagnetic jammer icon shared by world props and inventory. */
    public void drawJammerSymbol(Canvas canvas, float x, float y, float size, int color) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        rect.set(x - size * 0.72f, y - size * 0.48f,
                x + size * 0.72f, y + size * 0.48f);
        canvas.drawRoundRect(rect, size * 0.20f, size * 0.20f, paint);
        paint.setColor(withAlpha(Color.WHITE, 58));
        rect.set(x - size * 0.55f, y - size * 0.32f,
                x + size * 0.10f, y - size * 0.12f);
        canvas.drawRoundRect(rect, size * 0.08f, size * 0.08f, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(2f, size * 0.14f));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(Color.rgb(14, 18, 22));
        canvas.drawLine(x - size * 0.34f, y, x + size * 0.34f, y, paint);
        rect.set(x - size * 1.10f, y - size * 1.10f,
                x + size * 1.10f, y + size * 1.10f);
        canvas.drawArc(rect, -35f, 70f, false, paint);
        canvas.drawArc(rect, 145f, 70f, false, paint);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(x, y, size * 0.10f, paint);
        paint.setStrokeCap(Paint.Cap.BUTT);
    }

    private static int withAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }
}
