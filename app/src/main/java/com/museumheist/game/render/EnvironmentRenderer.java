package com.museumheist.game.render;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

/** Draws layered, non-collidable gallery materials and architectural lighting. */
public final class EnvironmentRenderer {
    private static final int[] ACCENTS = {
            Color.rgb(78, 205, 194),
            Color.rgb(224, 174, 82),
            Color.rgb(104, 152, 226),
            Color.rgb(194, 112, 185),
            Color.rgb(105, 191, 126),
            Color.rgb(225, 122, 90),
            Color.rgb(91, 188, 218),
            Color.rgb(179, 151, 231),
            Color.rgb(218, 188, 101),
            Color.rgb(115, 211, 184)
    };

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF scratch = new RectF();
    private final Path motif = new Path();

    public void draw(
            Canvas canvas,
            RectF bounds,
            int levelNumber,
            String levelTitle,
            float elapsed,
            boolean reduceMotion,
            HudRenderer.TextPainter text
    ) {
        int index = Math.floorMod(levelNumber - 1, ACCENTS.length);
        int accent = ACCENTS[index];
        drawTileField(canvas, bounds, index, accent);
        drawFloorInlays(canvas, bounds, index, accent);
        drawLightPools(canvas, bounds, index, accent);
        drawPerimeterLighting(canvas, bounds, index, accent);
        drawZoneMarkers(canvas, bounds, index, accent, text);
        drawGalleryMark(canvas, bounds, index, accent, levelNumber, levelTitle, text);
        drawLightDust(canvas, bounds, elapsed, reduceMotion, accent);
    }

    private void drawTileField(Canvas canvas, RectF bounds, int index, int accent) {
        float tile = 88f + (index % 3) * 12f;
        float inset = 28f;
        int row = 0;

        paint.setStyle(Paint.Style.FILL);
        for (float y = bounds.top + inset; y < bounds.bottom - inset; y += tile) {
            int column = 0;
            for (float x = bounds.left + inset; x < bounds.right - inset; x += tile) {
                float right = Math.min(x + tile, bounds.right - inset);
                float bottom = Math.min(y + tile, bounds.bottom - inset);
                int alpha = ((row + column) & 1) == 0 ? 13 : 7;
                paint.setColor(withAlpha(accent, alpha));
                scratch.set(x + 2f, y + 2f, right - 2f, bottom - 2f);
                canvas.drawRect(scratch, paint);
                column++;
            }
            row++;
        }

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.15f);
        paint.setColor(withAlpha(Color.WHITE, 18));
        for (float x = bounds.left + inset; x < bounds.right - inset; x += tile) {
            canvas.drawLine(x, bounds.top + inset, x, bounds.bottom - inset, paint);
        }
        for (float y = bounds.top + inset; y < bounds.bottom - inset; y += tile) {
            canvas.drawLine(bounds.left + inset, y, bounds.right - inset, y, paint);
        }

        paint.setStrokeWidth(0.8f);
        paint.setColor(withAlpha(Color.BLACK, 24));
        float half = tile * 0.5f;
        for (float x = bounds.left + inset + half; x < bounds.right - inset; x += tile) {
            canvas.drawLine(x, bounds.top + inset, x, bounds.bottom - inset, paint);
        }
        for (float y = bounds.top + inset + half; y < bounds.bottom - inset; y += tile) {
            canvas.drawLine(bounds.left + inset, y, bounds.right - inset, y, paint);
        }
    }

    private void drawFloorInlays(Canvas canvas, RectF bounds, int index, int accent) {
        float centerX = bounds.centerX();
        float centerY = bounds.centerY();
        float radius = Math.min(190f, Math.min(bounds.width(), bounds.height()) * 0.14f);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(25, 3, 7, 10));
        canvas.drawCircle(centerX + 8f, centerY + 10f, radius + 18f, paint);
        paint.setColor(withAlpha(accent, 18));
        canvas.drawCircle(centerX, centerY, radius + 12f, paint);
        paint.setColor(Color.argb(20, 234, 215, 158));
        canvas.drawCircle(centerX, centerY, radius * 0.72f, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4.5f);
        paint.setColor(Color.argb(82, 204, 176, 105));
        canvas.drawCircle(centerX, centerY, radius, paint);
        paint.setStrokeWidth(1.8f);
        paint.setColor(withAlpha(accent, 86));
        canvas.drawCircle(centerX, centerY, radius - 12f, paint);
        canvas.drawCircle(centerX, centerY, radius * 0.54f, paint);

        motif.reset();
        switch (index % 5) {
            case 0:
                for (int i = 0; i < 8; i++) {
                    float angle = (float) (Math.PI * 2d * i / 8d);
                    float inner = radius * 0.34f;
                    float outer = radius * 0.83f;
                    canvas.drawLine(
                            centerX + (float) Math.cos(angle) * inner,
                            centerY + (float) Math.sin(angle) * inner,
                            centerX + (float) Math.cos(angle) * outer,
                            centerY + (float) Math.sin(angle) * outer,
                            paint
                    );
                }
                break;
            case 1:
                scratch.set(centerX - radius * 0.70f, centerY - radius * 0.38f,
                        centerX + radius * 0.70f, centerY + radius * 0.38f);
                canvas.drawRoundRect(scratch, 22f, 22f, paint);
                break;
            case 2:
                motif.moveTo(centerX, centerY - radius * 0.76f);
                motif.lineTo(centerX + radius * 0.76f, centerY);
                motif.lineTo(centerX, centerY + radius * 0.76f);
                motif.lineTo(centerX - radius * 0.76f, centerY);
                motif.close();
                canvas.drawPath(motif, paint);
                break;
            case 3:
                for (int i = 0; i < 3; i++) {
                    float inset = radius * (0.30f + i * 0.18f);
                    scratch.set(centerX - inset, centerY - inset * 0.60f,
                            centerX + inset, centerY + inset * 0.60f);
                    canvas.drawRoundRect(scratch, 15f, 15f, paint);
                }
                break;
            default:
                canvas.drawLine(centerX - radius * 0.78f, centerY, centerX + radius * 0.78f, centerY, paint);
                canvas.drawLine(centerX, centerY - radius * 0.78f, centerX, centerY + radius * 0.78f, paint);
                break;
        }

        drawBrassInlay(canvas, bounds.left + bounds.width() * 0.20f, bounds.top + 54f,
                bounds.left + bounds.width() * 0.80f, bounds.top + 54f, accent);
        drawBrassInlay(canvas, bounds.left + bounds.width() * 0.20f, bounds.bottom - 54f,
                bounds.left + bounds.width() * 0.80f, bounds.bottom - 54f, accent);
    }

    private void drawBrassInlay(Canvas canvas, float startX, float startY, float endX, float endY, int accent) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5f);
        paint.setColor(Color.argb(34, 0, 0, 0));
        canvas.drawLine(startX + 3f, startY + 4f, endX + 3f, endY + 4f, paint);
        paint.setStrokeWidth(2.6f);
        paint.setColor(Color.argb(90, 204, 176, 105));
        canvas.drawLine(startX, startY, endX, endY, paint);
        paint.setStrokeWidth(1f);
        paint.setColor(withAlpha(accent, 70));
        canvas.drawLine(startX, startY - 2f, endX, endY - 2f, paint);
    }

    private void drawLightPools(Canvas canvas, RectF bounds, int index, int accent) {
        int columns = Math.max(3, Math.min(7, Math.round(bounds.width() / 520f)));
        int rows = Math.max(2, Math.min(4, Math.round(bounds.height() / 520f)));
        float usableWidth = bounds.width() - 220f;
        float usableHeight = bounds.height() - 210f;

        paint.setStyle(Paint.Style.FILL);
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                float x = bounds.left + 110f + usableWidth * (column + 0.5f) / columns;
                float y = bounds.top + 105f + usableHeight * (row + 0.5f) / rows;
                float width = 128f + ((column + index) % 3) * 18f;
                float height = 68f + ((row + index) % 2) * 10f;
                int lightColor = ((row + column + index) & 1) == 0 ? Color.rgb(248, 226, 174) : accent;
                for (int ring = 3; ring >= 1; ring--) {
                    float scale = 0.72f + ring * 0.22f;
                    paint.setColor(withAlpha(lightColor, 8 + (4 - ring) * 7));
                    scratch.set(x - width * scale, y - height * scale,
                            x + width * scale, y + height * scale);
                    canvas.drawOval(scratch, paint);
                }
                paint.setColor(withAlpha(Color.WHITE, 25));
                scratch.set(x - width * 0.30f, y - height * 0.20f,
                        x + width * 0.30f, y + height * 0.20f);
                canvas.drawOval(scratch, paint);
            }
        }
    }

    private void drawPerimeterLighting(Canvas canvas, RectF bounds, int index, int accent) {
        paint.setStyle(Paint.Style.FILL);
        float segment = 78f + (index % 2) * 10f;
        float gap = 34f;
        for (float x = bounds.left + 38f; x < bounds.right - 38f; x += segment + gap) {
            float right = Math.min(x + segment, bounds.right - 38f);
            paint.setColor(withAlpha(accent, 32));
            scratch.set(x - 4f, bounds.top + 9f, right + 4f, bounds.top + 23f);
            canvas.drawRoundRect(scratch, 7f, 7f, paint);
            scratch.set(x - 4f, bounds.bottom - 23f, right + 4f, bounds.bottom - 9f);
            canvas.drawRoundRect(scratch, 7f, 7f, paint);
            paint.setColor(withAlpha(accent, 118));
            scratch.set(x, bounds.top + 14f, right, bounds.top + 18f);
            canvas.drawRoundRect(scratch, 2f, 2f, paint);
            scratch.set(x, bounds.bottom - 18f, right, bounds.bottom - 14f);
            canvas.drawRoundRect(scratch, 2f, 2f, paint);
        }

        float verticalSegment = 62f;
        for (float y = bounds.top + 44f; y < bounds.bottom - 44f; y += verticalSegment + 42f) {
            float bottom = Math.min(y + verticalSegment, bounds.bottom - 44f);
            paint.setColor(withAlpha(accent, 84));
            scratch.set(bounds.left + 14f, y, bounds.left + 18f, bottom);
            canvas.drawRoundRect(scratch, 2f, 2f, paint);
            scratch.set(bounds.right - 18f, y, bounds.right - 14f, bottom);
            canvas.drawRoundRect(scratch, 2f, 2f, paint);
        }
    }

    private void drawZoneMarkers(
            Canvas canvas,
            RectF bounds,
            int index,
            int accent,
            HudRenderer.TextPainter text
    ) {
        String[] codes = {"A", "B", "C", "D"};
        float[] xs = {
                bounds.left + bounds.width() * 0.12f,
                bounds.right - bounds.width() * 0.12f,
                bounds.left + bounds.width() * 0.12f,
                bounds.right - bounds.width() * 0.12f
        };
        float[] ys = {
                bounds.top + bounds.height() * 0.18f,
                bounds.top + bounds.height() * 0.18f,
                bounds.bottom - bounds.height() * 0.18f,
                bounds.bottom - bounds.height() * 0.18f
        };

        for (int i = 0; i < codes.length; i++) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(42, 5, 10, 14));
            scratch.set(xs[i] - 34f + 4f, ys[i] - 22f + 5f, xs[i] + 34f + 4f, ys[i] + 22f + 5f);
            canvas.drawRoundRect(scratch, 8f, 8f, paint);
            paint.setColor(withAlpha(accent, 26));
            scratch.set(xs[i] - 34f, ys[i] - 22f, xs[i] + 34f, ys[i] + 22f);
            canvas.drawRoundRect(scratch, 8f, 8f, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1.5f);
            paint.setColor(withAlpha(accent, 92));
            canvas.drawRoundRect(scratch, 8f, 8f, paint);
            text.drawCenteredFittedText(
                    canvas,
                    codes[i] + (index + 1),
                    xs[i],
                    ys[i] + 6f,
                    52f,
                    16f,
                    withAlpha(Color.WHITE, 120)
            );
        }
    }

    private void drawGalleryMark(
            Canvas canvas,
            RectF bounds,
            int index,
            int accent,
            int levelNumber,
            String levelTitle,
            HudRenderer.TextPainter text
    ) {
        float markWidth = Math.min(520f, bounds.width() * 0.40f);
        float markY = bounds.bottom - 70f - (index % 2) * 10f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(82, 5, 10, 14));
        scratch.set(bounds.centerX() - markWidth * 0.5f + 6f, markY - 31f + 7f,
                bounds.centerX() + markWidth * 0.5f + 6f, markY + 21f + 7f);
        canvas.drawRoundRect(scratch, 9f, 9f, paint);
        paint.setColor(withAlpha(accent, 18));
        scratch.set(bounds.centerX() - markWidth * 0.5f, markY - 31f,
                bounds.centerX() + markWidth * 0.5f, markY + 21f);
        canvas.drawRoundRect(scratch, 9f, 9f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.8f);
        paint.setColor(withAlpha(accent, 88));
        canvas.drawRoundRect(scratch, 9f, 9f, paint);
        paint.setStrokeWidth(1f);
        paint.setColor(Color.argb(80, 204, 176, 105));
        scratch.inset(7f, 7f);
        canvas.drawRoundRect(scratch, 6f, 6f, paint);
        text.drawCenteredFittedText(
                canvas,
                String.format(java.util.Locale.CHINA, "第 %02d 展厅 · %s", levelNumber, levelTitle),
                bounds.centerX(),
                markY + 6f,
                markWidth - 36f,
                20f,
                withAlpha(Color.WHITE, 132)
        );
    }

    private void drawLightDust(Canvas canvas, RectF bounds, float elapsed, boolean reduceMotion, int accent) {
        paint.setStyle(Paint.Style.FILL);
        float time = reduceMotion ? 0f : elapsed;
        int count = Math.max(10, Math.min(20, Math.round(bounds.width() / 210f)));
        for (int i = 0; i < count; i++) {
            float seed = i * 1.37f;
            float xRatio = 0.08f + ((i * 29) % 83) / 100f;
            float yRatio = 0.10f + ((i * 41) % 79) / 100f;
            float x = bounds.left + bounds.width() * xRatio + (float) Math.sin(time * 0.25f + seed) * 18f;
            float y = bounds.top + bounds.height() * yRatio + (float) Math.cos(time * 0.20f + seed) * 13f;
            float pulse = reduceMotion ? 0.55f : 0.42f + 0.25f * (float) Math.sin(time * 0.75f + seed);
            paint.setColor(withAlpha(accent, (int) (18f + 24f * Math.max(0f, pulse))));
            canvas.drawCircle(x, y, 1.8f + (i % 4) * 0.8f, paint);
        }
    }

    private static int withAlpha(int color, int alpha) {
        return Color.argb(
                Math.max(0, Math.min(255, alpha)),
                Color.red(color),
                Color.green(color),
                Color.blue(color)
        );
    }
}
