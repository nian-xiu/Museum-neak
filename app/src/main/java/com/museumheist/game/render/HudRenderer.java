package com.museumheist.game.render;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.museumheist.game.logic.StealthTracker;
import com.museumheist.game.world.Level;

public class HudRenderer {
    public interface TextPainter {
        void drawCenteredFittedText(
                Canvas canvas,
                String text,
                float x,
                float y,
                float width,
                float size,
                int color
        );

        void drawLeftFittedText(
                Canvas canvas,
                String text,
                float x,
                float y,
                float width,
                float size,
                int color
        );
    }

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF threatTrack = new RectF();
    private final RectF threatFill = new RectF();

    public void drawHud(
            Canvas canvas,
            RectF bounds,
            float scale,
            Level level,
            int levelNumber,
            int levelCount,
            String objective,
            int coins,
            float threat,
            StealthTracker.ThreatBand threatBand,
            int chain,
            float chainProgress,
            TextPainter text
    ) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(224, 12, 22, 29));
        canvas.drawRoundRect(bounds, 11f * scale, 11f * scale, paint);

        int bandColor = threatColor(threatBand);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.8f * scale);
        paint.setColor(bandColor);
        canvas.drawRoundRect(bounds, 11f * scale, 11f * scale, paint);

        text.drawLeftFittedText(
                canvas,
                "展厅 " + levelNumber + "/" + levelCount + " · " + level.getTitle(),
                bounds.left + 16f * scale,
                bounds.top + 25f * scale,
                bounds.width() * 0.58f,
                18f * scale,
                Color.WHITE
        );
        text.drawLeftFittedText(
                canvas,
                objective,
                bounds.left + 16f * scale,
                bounds.top + 51f * scale,
                bounds.width() * 0.70f,
                14.5f * scale,
                Color.rgb(222, 232, 233)
        );
        text.drawCenteredFittedText(
                canvas,
                "金币 " + coins,
                bounds.right - 70f * scale,
                bounds.top + 29f * scale,
                125f * scale,
                17f * scale,
                Color.rgb(242, 203, 92)
        );

        float labelWidth = 92f * scale;
        float barLeft = bounds.left + 16f * scale + labelWidth;
        float barRight = bounds.right - 112f * scale;
        float barTop = bounds.bottom - 20f * scale;
        float barBottom = bounds.bottom - 10f * scale;
        text.drawLeftFittedText(
                canvas,
                threatLabel(threatBand),
                bounds.left + 16f * scale,
                bounds.bottom - 9f * scale,
                labelWidth - 6f * scale,
                13f * scale,
                bandColor
        );

        threatTrack.set(barLeft, barTop, Math.max(barLeft + 18f * scale, barRight), barBottom);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(170, 34, 47, 54));
        canvas.drawRoundRect(threatTrack, 5f * scale, 5f * scale, paint);
        float progress = clamp(threat, 0f, 1f);
        threatFill.set(threatTrack.left, threatTrack.top, threatTrack.left + threatTrack.width() * progress, threatTrack.bottom);
        paint.setColor(bandColor);
        canvas.drawRoundRect(threatFill, 5f * scale, 5f * scale, paint);

        if (chain > 1) {
            int chainColor = Color.rgb(246, 210, 105);
            String chainText = "潜行连携 x" + chain;
            text.drawCenteredFittedText(
                    canvas,
                    chainText,
                    bounds.right - 57f * scale,
                    bounds.bottom - 8f * scale,
                    104f * scale,
                    13f * scale,
                    chainColor
            );
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(72, 246, 210, 105));
            float chainWidth = 98f * scale * clamp(chainProgress, 0f, 1f);
            canvas.drawRoundRect(
                    bounds.right - 106f * scale,
                    bounds.bottom - 4.5f * scale,
                    bounds.right - 106f * scale + chainWidth,
                    bounds.bottom - 2.5f * scale,
                    scale,
                    scale,
                    paint
            );
        }
    }

    public void drawTimerPanel(
            Canvas canvas,
            RectF bounds,
            float scale,
            float elapsed,
            TextPainter text
    ) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(210, 12, 22, 29));
        canvas.drawRoundRect(bounds, 9f * scale, 9f * scale, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.3f * scale);
        paint.setColor(Color.argb(150, 94, 205, 199));
        canvas.drawRoundRect(bounds, 9f * scale, 9f * scale, paint);

        int seconds = Math.max(0, (int) elapsed);
        String value = String.format(java.util.Locale.US, "%d:%02d", seconds / 60, seconds % 60);
        text.drawCenteredFittedText(
                canvas,
                value,
                bounds.centerX(),
                bounds.centerY() + 6f * scale,
                bounds.width() - 12f * scale,
                17f * scale,
                Color.WHITE
        );
    }

    private static int threatColor(StealthTracker.ThreatBand band) {
        if (band == StealthTracker.ThreatBand.DANGER) {
            return Color.rgb(240, 83, 73);
        }
        if (band == StealthTracker.ThreatBand.WATCHED) {
            return Color.rgb(241, 184, 83);
        }
        return Color.rgb(83, 211, 185);
    }

    private static String threatLabel(StealthTracker.ThreatBand band) {
        if (band == StealthTracker.ThreatBand.DANGER) {
            return "高危";
        }
        if (band == StealthTracker.ThreatBand.WATCHED) {
            return "被注视";
        }
        return "安全";
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}