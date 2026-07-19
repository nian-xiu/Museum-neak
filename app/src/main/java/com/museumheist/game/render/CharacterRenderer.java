package com.museumheist.game.render;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;

import com.museumheist.R;
import com.museumheist.game.character.CharacterConfig;

/**
 * Anime-styled full-body character art shared by the home and character-selection screens.
 * The renderer deliberately uses a tall, athletic silhouette and clean cel shading so the
 * characters remain attractive and readable at both hero-panel and card sizes.
 */
public final class CharacterRenderer {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Path path = new Path();
    private final RectF rect = new RectF();
    private final Rect bitmapSource = new Rect();
    private final Bitmap[] fullBodyArt;

    public CharacterRenderer() {
        this.fullBodyArt = null;
        this.paint.setFilterBitmap(true);
        this.paint.setDither(true);
    }

    public CharacterRenderer(Resources resources) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        this.fullBodyArt = new Bitmap[]{
                BitmapFactory.decodeResource(resources, R.drawable.character_city_explorer, options),
                BitmapFactory.decodeResource(resources, R.drawable.character_tech_scout, options),
                BitmapFactory.decodeResource(resources, R.drawable.character_museum_researcher, options),
                BitmapFactory.decodeResource(resources, R.drawable.character_street_player, options),
                BitmapFactory.decodeResource(resources, R.drawable.character_fantasy_guardian, options)
        };
        this.paint.setFilterBitmap(true);
        this.paint.setDither(true);
    }

    public void drawPortrait(Canvas canvas, CharacterConfig config, RectF bounds,
                             float time, boolean detailed) {
        drawFullBody(canvas, config, bounds, time, detailed);
    }

    public void drawFullBody(Canvas canvas, CharacterConfig config, RectF bounds,
                             float time, boolean detailed) {
        Bitmap illustration = illustrationFor(config);
        if (illustration != null && !illustration.isRecycled()) {
            drawIllustration(canvas, config, bounds, illustration, time, detailed);
            return;
        }

        canvas.save();
        canvas.clipRect(bounds);

        float figureH = Math.min(bounds.height() * 0.92f, bounds.width() * 2.18f);
        float cx = bounds.centerX();
        float top = bounds.bottom - figureH - bounds.height() * 0.018f;
        float breathe = (float) Math.sin(time * 1.35f + config.getVariant() * 0.72f) * figureH * 0.0028f;
        float sway = (float) Math.sin(time * 0.72f + config.getVariant() * 0.41f) * 0.45f;

        drawBackdrop(canvas, config, bounds, cx, top, figureH, detailed);

        canvas.save();
        canvas.rotate(sway, cx, top + figureH * 0.58f);
        canvas.translate(0f, breathe);
        drawFigure(canvas, config, cx, top, figureH, detailed);
        canvas.restore();
        canvas.restore();
    }

    private Bitmap illustrationFor(CharacterConfig config) {
        if (this.fullBodyArt == null || this.fullBodyArt.length == 0) {
            return null;
        }
        int variant = Math.max(0, Math.min(this.fullBodyArt.length - 1, config.getVariant()));
        return this.fullBodyArt[variant];
    }

    private void drawIllustration(Canvas canvas, CharacterConfig config, RectF bounds,
                                  Bitmap illustration, float time, boolean detailed) {
        canvas.save();
        canvas.clipRect(bounds);

        float cropLeft = illustration.getWidth() * 0.078f;
        float cropTop = illustration.getHeight() * 0.008f;
        float cropRight = illustration.getWidth() * 0.813f;
        float cropBottom = illustration.getHeight() * 0.978f;
        float sourceWidth = cropRight - cropLeft;
        float sourceHeight = cropBottom - cropTop;
        float aspect = sourceWidth / sourceHeight;
        float figureH = Math.min(bounds.height() * 0.975f, bounds.width() / aspect * 0.96f);
        float figureW = figureH * aspect;
        float cx = bounds.centerX();
        float top = bounds.bottom - figureH - bounds.height() * 0.010f;
        float breathe = (float) Math.sin(time * 1.28f + config.getVariant() * 0.68f)
                * figureH * 0.0022f;
        float sway = (float) Math.sin(time * 0.68f + config.getVariant() * 0.39f) * 0.28f;

        drawBackdrop(canvas, config, bounds, cx, top, figureH, detailed);
        this.bitmapSource.set(Math.round(cropLeft), Math.round(cropTop),
                Math.round(cropRight), Math.round(cropBottom));
        this.rect.set(cx - figureW * 0.5f, top, cx + figureW * 0.5f, top + figureH);

        canvas.save();
        canvas.rotate(sway, cx, top + figureH * 0.61f);
        canvas.translate(0f, breathe);
        this.paint.setStyle(Paint.Style.FILL);
        this.paint.setShader(null);
        this.paint.setAlpha(255);
        canvas.drawBitmap(illustration, this.bitmapSource, this.rect, this.paint);
        canvas.restore();
        canvas.restore();
    }

    /** Compatibility entry point retained for older callers. */
    public void drawBust(Canvas canvas, CharacterConfig config, float cx, float cy, float size,
                         float time, boolean detailed) {
        RectF bounds = new RectF(cx - size * 0.54f, cy - size * 1.36f,
                cx + size * 0.54f, cy + size * 0.18f);
        drawFullBody(canvas, config, bounds, time, detailed);
    }

    private void drawBackdrop(Canvas canvas, CharacterConfig config, RectF bounds,
                              float cx, float top, float h, boolean detailed) {
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new RadialGradient(
                cx, top + h * 0.39f, h * 0.42f,
                withAlpha(lighten(config.getAccentColor(), 0.16f), detailed ? 76 : 48),
                withAlpha(Color.rgb(3, 10, 16), 0), Shader.TileMode.CLAMP));
        canvas.drawCircle(cx, top + h * 0.40f, h * 0.42f, paint);
        paint.setShader(null);

        paint.setColor(Color.argb(detailed ? 82 : 58, 0, 0, 0));
        rect.set(cx - h * 0.15f, top + h * 0.952f,
                cx + h * 0.15f, top + h * 0.995f);
        canvas.drawOval(rect, paint);

        if (detailed) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Math.max(1.2f, h * 0.0032f));
            paint.setColor(withAlpha(config.getAccentColor(), 82));
            rect.set(cx - h * 0.205f, top + h * 0.035f,
                    cx + h * 0.205f, top + h * 0.91f);
            canvas.drawArc(rect, 205f, 130f, false, paint);
            paint.setStyle(Paint.Style.FILL);
        }
    }

    private void drawFigure(Canvas canvas, CharacterConfig config, float cx, float top,
                            float h, boolean detailed) {
        int outline = characterOutline(config);
        drawBackLayer(canvas, config, cx, top, h, outline);
        drawLegs(canvas, config, cx, top, h, outline);
        drawTorso(canvas, config, cx, top, h, outline);
        drawArms(canvas, config, cx, top, h, outline);
        drawHead(canvas, config, cx, top, h, outline, detailed);
    }

    private void drawBackLayer(Canvas canvas, CharacterConfig config, float cx, float top,
                               float h, int outline) {
        int variant = config.getVariant();
        if (variant == 2 || variant == 4) {
            path.reset();
            path.moveTo(cx - h * 0.105f, top + h * 0.285f);
            path.cubicTo(cx - h * 0.185f, top + h * 0.38f,
                    cx - h * 0.16f, top + h * 0.68f,
                    cx - h * 0.115f, top + h * 0.79f);
            path.lineTo(cx + h * 0.115f, top + h * 0.79f);
            path.cubicTo(cx + h * 0.16f, top + h * 0.68f,
                    cx + h * 0.185f, top + h * 0.38f,
                    cx + h * 0.105f, top + h * 0.285f);
            path.close();
            int back = variant == 4 ? Color.rgb(185, 170, 214) : darken(config.getPrimaryColor(), 0.23f);
            fillAndStroke(canvas, path, back, outline, h * 0.0052f);
            paint.setColor(withAlpha(lighten(back, 0.25f), 78));
            path.reset();
            path.moveTo(cx - h * 0.08f, top + h * 0.31f);
            path.cubicTo(cx - h * 0.12f, top + h * 0.47f,
                    cx - h * 0.105f, top + h * 0.66f,
                    cx - h * 0.08f, top + h * 0.75f);
            path.lineTo(cx - h * 0.025f, top + h * 0.75f);
            path.lineTo(cx - h * 0.025f, top + h * 0.34f);
            path.close();
            canvas.drawPath(path, paint);
        } else if (variant == 0) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(darken(config.getPrimaryColor(), 0.34f));
            rect.set(cx + h * 0.07f, top + h * 0.31f,
                    cx + h * 0.165f, top + h * 0.54f);
            canvas.drawRoundRect(rect, h * 0.028f, h * 0.028f, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(h * 0.007f);
            paint.setColor(config.getAccentColor());
            canvas.drawArc(cx + h * 0.085f, top + h * 0.315f,
                    cx + h * 0.155f, top + h * 0.47f,
                    275f, 170f, false, paint);
            paint.setStyle(Paint.Style.FILL);
        } else if (variant == 3) {
            paint.setColor(darken(config.getDarkColor(), 0.08f));
            rect.set(cx - h * 0.138f, top + h * 0.305f,
                    cx + h * 0.138f, top + h * 0.49f);
            canvas.drawRoundRect(rect, h * 0.07f, h * 0.07f, paint);
        }
    }

    private void drawLegs(Canvas canvas, CharacterConfig config, float cx, float top,
                          float h, int outline) {
        float hipY = top + h * 0.565f;
        float kneeY = top + h * 0.755f;
        float ankleY = top + h * 0.925f;
        float leftX = cx - h * 0.055f;
        float rightX = cx + h * 0.055f;
        int pants = config.getPantsColor();
        int pantsShadow = darken(pants, 0.28f);

        path.reset();
        path.moveTo(cx - h * 0.105f, hipY);
        path.lineTo(cx - h * 0.008f, hipY);
        path.lineTo(leftX + h * 0.028f, kneeY);
        path.lineTo(leftX + h * 0.025f, ankleY);
        path.lineTo(leftX - h * 0.035f, ankleY);
        path.lineTo(leftX - h * 0.045f, kneeY);
        path.close();
        fillAndStroke(canvas, path, pants, outline, h * 0.0052f);

        path.reset();
        path.moveTo(cx + h * 0.008f, hipY);
        path.lineTo(cx + h * 0.105f, hipY);
        path.lineTo(rightX + h * 0.045f, kneeY);
        path.lineTo(rightX + h * 0.035f, ankleY);
        path.lineTo(rightX - h * 0.025f, ankleY);
        path.lineTo(rightX - h * 0.028f, kneeY);
        path.close();
        fillAndStroke(canvas, path, pantsShadow, outline, h * 0.0052f);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(withAlpha(lighten(pants, 0.30f), 78));
        path.reset();
        path.moveTo(leftX - h * 0.016f, hipY + h * 0.025f);
        path.lineTo(leftX + h * 0.010f, hipY + h * 0.025f);
        path.lineTo(leftX - h * 0.005f, kneeY - h * 0.018f);
        path.lineTo(leftX - h * 0.025f, kneeY - h * 0.018f);
        path.close();
        canvas.drawPath(path, paint);

        drawShoe(canvas, leftX - h * 0.007f, ankleY, h, config.getShoeColor(), outline, false);
        drawShoe(canvas, rightX + h * 0.007f, ankleY, h, config.getShoeColor(), outline, true);
    }

    private void drawShoe(Canvas canvas, float x, float ankleY, float h, int color,
                          int outline, boolean right) {
        float direction = right ? 1f : -1f;
        path.reset();
        path.moveTo(x - h * 0.032f, ankleY - h * 0.005f);
        path.lineTo(x + h * 0.03f, ankleY - h * 0.005f);
        path.cubicTo(x + direction * h * 0.038f, ankleY + h * 0.014f,
                x + direction * h * 0.052f, ankleY + h * 0.034f,
                x + direction * h * 0.073f, ankleY + h * 0.042f);
        path.lineTo(x - direction * h * 0.043f, ankleY + h * 0.042f);
        path.cubicTo(x - direction * h * 0.052f, ankleY + h * 0.025f,
                x - direction * h * 0.048f, ankleY + h * 0.008f,
                x - h * 0.032f, ankleY - h * 0.005f);
        path.close();
        fillAndStroke(canvas, path, color, outline, h * 0.0052f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(h * 0.004f);
        paint.setColor(withAlpha(Color.WHITE, 125));
        canvas.drawLine(x - h * 0.035f, ankleY + h * 0.031f,
                x + direction * h * 0.058f, ankleY + h * 0.031f, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawTorso(Canvas canvas, CharacterConfig config, float cx, float top,
                           float h, int outline) {
        float shoulderY = top + h * 0.285f;
        float waistY = top + h * 0.485f;
        float hemY = top + h * (config.getVariant() == 2 ? 0.615f : 0.575f);
        float shoulder = h * 0.145f;
        float waist = h * 0.085f;
        float hem = h * (config.getVariant() == 3 ? 0.112f : 0.102f);

        path.reset();
        path.moveTo(cx - h * 0.055f, shoulderY - h * 0.025f);
        path.cubicTo(cx - shoulder, shoulderY,
                cx - shoulder, shoulderY + h * 0.055f,
                cx - waist, waistY);
        path.lineTo(cx - hem, hemY);
        path.lineTo(cx + hem, hemY);
        path.lineTo(cx + waist, waistY);
        path.cubicTo(cx + shoulder, shoulderY + h * 0.055f,
                cx + shoulder, shoulderY,
                cx + h * 0.055f, shoulderY - h * 0.025f);
        path.close();
        paint.setShader(new LinearGradient(cx - shoulder, shoulderY,
                cx + shoulder, hemY,
                lighten(config.getPrimaryColor(), 0.15f),
                darken(config.getPrimaryColor(), 0.27f), Shader.TileMode.CLAMP));
        fillAndStrokeCurrentShader(canvas, path, outline, h * 0.0056f);
        paint.setShader(null);

        // Slim side shadow reinforces the waist instead of creating a boxy torso.
        paint.setColor(withAlpha(config.getDarkColor(), 112));
        path.reset();
        path.moveTo(cx + shoulder * 0.62f, shoulderY + h * 0.018f);
        path.cubicTo(cx + shoulder * 0.82f, shoulderY + h * 0.075f,
                cx + waist * 0.72f, waistY,
                cx + hem * 0.58f, hemY - h * 0.012f);
        path.lineTo(cx + hem, hemY);
        path.lineTo(cx + waist, waistY);
        path.lineTo(cx + shoulder, shoulderY + h * 0.045f);
        path.close();
        canvas.drawPath(path, paint);

        drawOutfitDetails(canvas, config, cx, top, h, outline, shoulderY, hemY);
    }

    private void drawOutfitDetails(Canvas canvas, CharacterConfig config, float cx, float top,
                                   float h, int outline, float shoulderY, float hemY) {
        int variant = config.getVariant();
        paint.setStyle(Paint.Style.FILL);
        if (variant == 0) {
            // Layered tactical jacket with a compact shirt panel and crisp lapels.
            path.reset();
            path.moveTo(cx - h * 0.050f, shoulderY + h * 0.010f);
            path.lineTo(cx - h * 0.018f, top + h * 0.372f);
            path.lineTo(cx - h * 0.032f, hemY - h * 0.014f);
            path.lineTo(cx + h * 0.032f, hemY - h * 0.014f);
            path.lineTo(cx + h * 0.018f, top + h * 0.372f);
            path.lineTo(cx + h * 0.050f, shoulderY + h * 0.010f);
            path.lineTo(cx, top + h * 0.405f);
            path.close();
            fillAndStroke(canvas, path, mix(config.getSecondaryColor(), config.getPrimaryColor(), 0.46f),
                    outline, h * 0.0038f);
            paint.setColor(lighten(config.getSecondaryColor(), 0.06f));
            path.reset();
            path.moveTo(cx - h * 0.048f, shoulderY + h * 0.012f);
            path.lineTo(cx - h * 0.010f, top + h * 0.382f);
            path.lineTo(cx - h * 0.002f, top + h * 0.337f);
            path.close();
            canvas.drawPath(path, paint);
            path.reset();
            path.moveTo(cx + h * 0.048f, shoulderY + h * 0.012f);
            path.lineTo(cx + h * 0.010f, top + h * 0.382f);
            path.lineTo(cx + h * 0.002f, top + h * 0.337f);
            path.close();
            canvas.drawPath(path, paint);
            drawRoundedStroke(canvas, cx - h * 0.083f, shoulderY + h * 0.018f,
                    cx + h * 0.058f, hemY - h * 0.016f,
                    h * 0.010f, config.getAccentColor());
            paint.setColor(config.getAccentColor());
            canvas.drawRoundRect(cx + h * 0.050f, top + h * 0.435f,
                    cx + h * 0.088f, top + h * 0.478f,
                    h * 0.010f, h * 0.010f, paint);
        } else if (variant == 1) {
            paint.setColor(config.getSecondaryColor());
            path.reset();
            path.moveTo(cx - h * 0.048f, shoulderY + h * 0.015f);
            path.lineTo(cx - h * 0.010f, top + h * 0.405f);
            path.lineTo(cx - h * 0.016f, hemY - h * 0.016f);
            path.lineTo(cx - h * 0.072f, hemY - h * 0.016f);
            path.close();
            canvas.drawPath(path, paint);
            paint.setColor(withAlpha(config.getAccentColor(), 210));
            canvas.drawRoundRect(cx + h * 0.025f, top + h * 0.365f,
                    cx + h * 0.075f, top + h * 0.405f,
                    h * 0.006f, h * 0.006f, paint);
            paint.setColor(Color.rgb(115, 235, 241));
            canvas.drawCircle(cx + h * 0.059f, top + h * 0.385f, h * 0.006f, paint);
        } else if (variant == 2) {
            paint.setColor(config.getSecondaryColor());
            path.reset();
            path.moveTo(cx - h * 0.055f, shoulderY + h * 0.01f);
            path.lineTo(cx - h * 0.012f, top + h * 0.39f);
            path.lineTo(cx - h * 0.025f, hemY - h * 0.018f);
            path.lineTo(cx - h * 0.083f, hemY - h * 0.018f);
            path.close();
            canvas.drawPath(path, paint);
            path.reset();
            path.moveTo(cx + h * 0.055f, shoulderY + h * 0.01f);
            path.lineTo(cx + h * 0.012f, top + h * 0.39f);
            path.lineTo(cx + h * 0.025f, hemY - h * 0.018f);
            path.lineTo(cx + h * 0.083f, hemY - h * 0.018f);
            path.close();
            canvas.drawPath(path, paint);
            paint.setColor(config.getAccentColor());
            path.reset();
            path.moveTo(cx, top + h * 0.355f);
            path.lineTo(cx - h * 0.017f, top + h * 0.39f);
            path.lineTo(cx, top + h * 0.455f);
            path.lineTo(cx + h * 0.017f, top + h * 0.39f);
            path.close();
            canvas.drawPath(path, paint);
        } else if (variant == 3) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(h * 0.006f);
            paint.setColor(config.getAccentColor());
            canvas.drawLine(cx - h * 0.021f, top + h * 0.33f,
                    cx - h * 0.014f, top + h * 0.425f, paint);
            canvas.drawLine(cx + h * 0.021f, top + h * 0.33f,
                    cx + h * 0.014f, top + h * 0.425f, paint);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(cx - h * 0.014f, top + h * 0.43f, h * 0.009f, paint);
            canvas.drawCircle(cx + h * 0.014f, top + h * 0.43f, h * 0.009f, paint);
            paint.setColor(darken(config.getPrimaryColor(), 0.26f));
            canvas.drawRoundRect(cx - h * 0.067f, top + h * 0.485f,
                    cx + h * 0.067f, top + h * 0.535f,
                    h * 0.014f, h * 0.014f, paint);
        } else {
            paint.setColor(Color.rgb(246, 242, 224));
            path.reset();
            path.moveTo(cx - h * 0.048f, shoulderY);
            path.lineTo(cx, top + h * 0.405f);
            path.lineTo(cx + h * 0.048f, shoulderY);
            path.lineTo(cx + h * 0.04f, hemY - h * 0.012f);
            path.lineTo(cx - h * 0.04f, hemY - h * 0.012f);
            path.close();
            canvas.drawPath(path, paint);
            paint.setColor(config.getAccentColor());
            path.reset();
            path.moveTo(cx, top + h * 0.37f);
            path.lineTo(cx - h * 0.025f, top + h * 0.405f);
            path.lineTo(cx, top + h * 0.465f);
            path.lineTo(cx + h * 0.025f, top + h * 0.405f);
            path.close();
            canvas.drawPath(path, paint);
        }
    }

    private void drawArms(Canvas canvas, CharacterConfig config, float cx, float top,
                          float h, int outline) {
        float shoulderY = top + h * 0.302f;
        int sleeve = config.getPrimaryColor();
        int sleeveShadow = darken(sleeve, 0.25f);
        int skin = config.getSkinColor();

        if (config.getVariant() == 1) {
            drawArm(canvas, cx - h * 0.13f, shoulderY,
                    cx - h * 0.155f, top + h * 0.43f,
                    cx - h * 0.102f, top + h * 0.515f,
                    h, sleeve, skin, outline);
            drawArm(canvas, cx + h * 0.13f, shoulderY,
                    cx + h * 0.162f, top + h * 0.43f,
                    cx + h * 0.118f, top + h * 0.525f,
                    h, sleeveShadow, skin, outline);
            paint.setColor(config.getAccentColor());
            canvas.drawRoundRect(cx + h * 0.085f, top + h * 0.49f,
                    cx + h * 0.142f, top + h * 0.535f,
                    h * 0.008f, h * 0.008f, paint);
        } else {
            float leftWristX = cx - h * (config.getVariant() == 3 ? 0.115f : 0.145f);
            float rightWristX = cx + h * (config.getVariant() == 0 ? 0.108f : 0.145f);
            drawArm(canvas, cx - h * 0.13f, shoulderY,
                    cx - h * 0.155f, top + h * 0.435f,
                    leftWristX, top + h * 0.56f,
                    h, sleeve, skin, outline);
            drawArm(canvas, cx + h * 0.13f, shoulderY,
                    cx + h * 0.158f, top + h * 0.435f,
                    rightWristX, top + h * 0.56f,
                    h, sleeveShadow, skin, outline);
        }
    }

    private void drawArm(Canvas canvas, float sx, float sy, float ex, float ey,
                         float wx, float wy, float h, int sleeve, int skin, int outline) {
        path.reset();
        path.moveTo(sx, sy);
        path.quadTo(ex, ey, wx, wy);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(h * 0.042f);
        paint.setColor(outline);
        canvas.drawPath(path, paint);
        paint.setStrokeWidth(h * 0.032f);
        paint.setColor(sleeve);
        canvas.drawPath(path, paint);
        paint.setStrokeWidth(h * 0.020f);
        paint.setColor(skin);
        canvas.drawLine(wx, wy - h * 0.006f, wx, wy + h * 0.023f, paint);
        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawHead(Canvas canvas, CharacterConfig config, float cx, float top,
                          float h, int outline, boolean detailed) {
        float headCx = cx;
        float headCy = top + h * 0.155f;
        float faceHalf = h * 0.067f;
        float faceTop = top + h * 0.073f;
        float chinY = top + h * 0.248f;
        int skin = config.getSkinColor();
        int hair = hairColor(config);

        // Narrow neck keeps the silhouette youthful and avoids the old swollen look.
        paint.setShader(new LinearGradient(cx - h * 0.022f, top + h * 0.225f,
                cx + h * 0.028f, top + h * 0.292f,
                lighten(skin, 0.09f), darken(skin, 0.18f), Shader.TileMode.CLAMP));
        rect.set(cx - h * 0.024f, top + h * 0.22f,
                cx + h * 0.024f, top + h * 0.292f);
        canvas.drawRoundRect(rect, h * 0.016f, h * 0.016f, paint);
        paint.setShader(null);

        drawHairBack(canvas, config, headCx, top, h, hair, outline);

        paint.setColor(darken(skin, 0.05f));
        canvas.drawOval(headCx - faceHalf - h * 0.013f, headCy - h * 0.018f,
                headCx - faceHalf + h * 0.010f, headCy + h * 0.035f, paint);
        canvas.drawOval(headCx + faceHalf - h * 0.010f, headCy - h * 0.018f,
                headCx + faceHalf + h * 0.013f, headCy + h * 0.035f, paint);

        path.reset();
        path.moveTo(headCx, faceTop);
        path.cubicTo(headCx - faceHalf * 0.95f, faceTop,
                headCx - faceHalf * 1.05f, headCy + h * 0.015f,
                headCx - faceHalf * 0.74f, headCy + h * 0.055f);
        path.cubicTo(headCx - faceHalf * 0.54f, chinY - h * 0.018f,
                headCx - faceHalf * 0.22f, chinY,
                headCx, chinY + h * 0.003f);
        path.cubicTo(headCx + faceHalf * 0.22f, chinY,
                headCx + faceHalf * 0.54f, chinY - h * 0.018f,
                headCx + faceHalf * 0.74f, headCy + h * 0.055f);
        path.cubicTo(headCx + faceHalf * 1.05f, headCy + h * 0.015f,
                headCx + faceHalf * 0.95f, faceTop,
                headCx, faceTop);
        path.close();
        paint.setShader(new LinearGradient(headCx - faceHalf, faceTop,
                headCx + faceHalf, chinY,
                lighten(skin, 0.13f), darken(skin, 0.10f), Shader.TileMode.CLAMP));
        fillAndStrokeCurrentShader(canvas, path, outline, h * 0.0052f);
        paint.setShader(null);

        // Soft cheek and jaw cel shading.
        paint.setColor(withAlpha(darken(skin, 0.20f), 50));
        path.reset();
        path.moveTo(headCx + faceHalf * 0.55f, headCy + h * 0.01f);
        path.cubicTo(headCx + faceHalf * 0.75f, headCy + h * 0.055f,
                headCx + faceHalf * 0.42f, chinY - h * 0.016f,
                headCx + h * 0.006f, chinY - h * 0.004f);
        path.cubicTo(headCx + faceHalf * 0.32f, chinY - h * 0.045f,
                headCx + faceHalf * 0.36f, headCy + h * 0.03f,
                headCx + faceHalf * 0.55f, headCy + h * 0.01f);
        path.close();
        canvas.drawPath(path, paint);

        drawAnimeFace(canvas, config, headCx, headCy, top, h, outline, detailed);
        drawHairFront(canvas, config, headCx, top, h, hair, outline);
        drawHeadAccessory(canvas, config, headCx, top, h, outline);
    }

    private void drawAnimeFace(Canvas canvas, CharacterConfig config, float cx, float cy,
                               float top, float h, int outline, boolean detailed) {
        float eyeY = top + h * 0.156f;
        float eyeDx = h * 0.031f;
        float eyeW = h * 0.032f;
        float eyeH = h * (detailed ? 0.0115f : 0.0095f);
        int iris = config.getVariant() == 4 ? Color.rgb(104, 196, 222)
                : config.getVariant() == 3 ? Color.rgb(126, 91, 172)
                : Color.rgb(72, 104, 112);

        drawAnimeEye(canvas, cx - eyeDx, eyeY, eyeW, eyeH, iris, outline, false, h);
        drawAnimeEye(canvas, cx + eyeDx, eyeY, eyeW, eyeH, iris, outline, true, h);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(Math.max(1.2f, h * 0.0043f));
        paint.setColor(withAlpha(outline, 225));
        canvas.drawLine(cx - h * 0.052f, eyeY - h * 0.026f,
                cx - h * 0.014f, eyeY - h * 0.029f, paint);
        canvas.drawLine(cx + h * 0.014f, eyeY - h * 0.029f,
                cx + h * 0.052f, eyeY - h * 0.026f, paint);

        paint.setStrokeWidth(Math.max(1f, h * 0.0031f));
        paint.setColor(withAlpha(darken(config.getSkinColor(), 0.30f), 150));
        path.reset();
        path.moveTo(cx + h * 0.001f, eyeY + h * 0.018f);
        path.quadTo(cx - h * 0.009f, eyeY + h * 0.045f,
                cx + h * 0.005f, eyeY + h * 0.05f);
        canvas.drawPath(path, paint);

        paint.setStrokeWidth(Math.max(1.1f, h * 0.0035f));
        paint.setColor(withAlpha(Color.rgb(120, 56, 58), 205));
        path.reset();
        path.moveTo(cx - h * 0.022f, eyeY + h * 0.073f);
        path.quadTo(cx, eyeY + h * 0.081f,
                cx + h * 0.023f, eyeY + h * 0.071f);
        canvas.drawPath(path, paint);
        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setStyle(Paint.Style.FILL);

        if (detailed) {
            paint.setColor(Color.argb(40, 229, 98, 100));
            canvas.drawOval(cx - h * 0.060f, eyeY + h * 0.035f,
                    cx - h * 0.028f, eyeY + h * 0.050f, paint);
            canvas.drawOval(cx + h * 0.028f, eyeY + h * 0.035f,
                    cx + h * 0.060f, eyeY + h * 0.050f, paint);
        }
    }

    private void drawAnimeEye(Canvas canvas, float cx, float cy, float w, float eh,
                              int iris, int outline, boolean right, float h) {
        path.reset();
        path.moveTo(cx - w * 0.55f, cy);
        path.quadTo(cx, cy - eh, cx + w * 0.56f, cy);
        path.quadTo(cx, cy + eh * 0.75f, cx - w * 0.55f, cy);
        path.close();
        fillAndStroke(canvas, path, Color.rgb(247, 248, 244), outline, h * 0.0032f);
        paint.setColor(iris);
        canvas.drawCircle(cx + (right ? -h * 0.001f : h * 0.001f), cy, h * 0.0078f, paint);
        paint.setColor(Color.rgb(19, 25, 30));
        canvas.drawCircle(cx, cy + h * 0.001f, h * 0.0041f, paint);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(cx - h * 0.0028f, cy - h * 0.0035f, h * 0.0025f, paint);
    }

    private void drawHairBack(Canvas canvas, CharacterConfig config, float cx, float top,
                              float h, int hair, int outline) {
        int variant = config.getVariant();
        path.reset();
        if (variant == 4) {
            path.moveTo(cx, top + h * 0.045f);
            path.cubicTo(cx - h * 0.085f, top + h * 0.045f,
                    cx - h * 0.095f, top + h * 0.13f,
                    cx - h * 0.082f, top + h * 0.22f);
            path.lineTo(cx - h * 0.055f, top + h * 0.32f);
            path.lineTo(cx - h * 0.018f, top + h * 0.245f);
            path.lineTo(cx + h * 0.025f, top + h * 0.325f);
            path.lineTo(cx + h * 0.07f, top + h * 0.215f);
            path.cubicTo(cx + h * 0.095f, top + h * 0.13f,
                    cx + h * 0.085f, top + h * 0.055f,
                    cx, top + h * 0.045f);
        } else if (variant == 3) {
            path.moveTo(cx, top + h * 0.05f);
            path.cubicTo(cx - h * 0.083f, top + h * 0.05f,
                    cx - h * 0.092f, top + h * 0.13f,
                    cx - h * 0.078f, top + h * 0.235f);
            path.lineTo(cx - h * 0.045f, top + h * 0.205f);
            path.lineTo(cx + h * 0.048f, top + h * 0.215f);
            path.lineTo(cx + h * 0.078f, top + h * 0.22f);
            path.cubicTo(cx + h * 0.09f, top + h * 0.13f,
                    cx + h * 0.083f, top + h * 0.05f,
                    cx, top + h * 0.05f);
        } else if (variant == 0) {
            // Short layered cut: open jawline and clean sideburns for a mature hero silhouette.
            path.moveTo(cx, top + h * 0.048f);
            path.cubicTo(cx - h * 0.082f, top + h * 0.046f,
                    cx - h * 0.09f, top + h * 0.115f,
                    cx - h * 0.071f, top + h * 0.178f);
            path.lineTo(cx - h * 0.052f, top + h * 0.188f);
            path.lineTo(cx - h * 0.049f, top + h * 0.125f);
            path.cubicTo(cx - h * 0.025f, top + h * 0.078f,
                    cx + h * 0.032f, top + h * 0.074f,
                    cx + h * 0.059f, top + h * 0.105f);
            path.lineTo(cx + h * 0.055f, top + h * 0.184f);
            path.lineTo(cx + h * 0.073f, top + h * 0.176f);
            path.cubicTo(cx + h * 0.09f, top + h * 0.112f,
                    cx + h * 0.082f, top + h * 0.048f,
                    cx, top + h * 0.048f);
        } else {
            path.moveTo(cx, top + h * 0.05f);
            path.cubicTo(cx - h * 0.084f, top + h * 0.048f,
                    cx - h * 0.09f, top + h * 0.125f,
                    cx - h * 0.073f, top + h * 0.207f);
            path.cubicTo(cx - h * 0.035f, top + h * 0.238f,
                    cx + h * 0.037f, top + h * 0.238f,
                    cx + h * 0.073f, top + h * 0.207f);
            path.cubicTo(cx + h * 0.09f, top + h * 0.125f,
                    cx + h * 0.084f, top + h * 0.048f,
                    cx, top + h * 0.05f);
        }
        path.close();
        fillAndStroke(canvas, path, hair, outline, h * 0.0054f);
    }

    private void drawHairFront(Canvas canvas, CharacterConfig config, float cx, float top,
                               float h, int hair, int outline) {
        int variant = config.getVariant();
        path.reset();
        if (variant == 0) {
            path.moveTo(cx - h * 0.072f, top + h * 0.12f);
            path.cubicTo(cx - h * 0.055f, top + h * 0.07f,
                    cx - h * 0.012f, top + h * 0.062f,
                    cx + h * 0.025f, top + h * 0.073f);
            path.lineTo(cx + h * 0.008f, top + h * 0.129f);
            path.lineTo(cx - h * 0.014f, top + h * 0.103f);
            path.lineTo(cx - h * 0.035f, top + h * 0.135f);
            path.lineTo(cx - h * 0.052f, top + h * 0.109f);
            path.lineTo(cx - h * 0.072f, top + h * 0.15f);
        } else if (variant == 1) {
            path.moveTo(cx - h * 0.074f, top + h * 0.125f);
            path.cubicTo(cx - h * 0.052f, top + h * 0.065f,
                    cx + h * 0.035f, top + h * 0.055f,
                    cx + h * 0.073f, top + h * 0.10f);
            path.lineTo(cx + h * 0.04f, top + h * 0.118f);
            path.lineTo(cx + h * 0.015f, top + h * 0.102f);
            path.lineTo(cx - h * 0.012f, top + h * 0.128f);
            path.lineTo(cx - h * 0.042f, top + h * 0.105f);
            path.lineTo(cx - h * 0.074f, top + h * 0.15f);
        } else if (variant == 2) {
            path.moveTo(cx - h * 0.075f, top + h * 0.125f);
            path.cubicTo(cx - h * 0.04f, top + h * 0.06f,
                    cx + h * 0.035f, top + h * 0.058f,
                    cx + h * 0.071f, top + h * 0.11f);
            path.lineTo(cx + h * 0.022f, top + h * 0.123f);
            path.lineTo(cx - h * 0.012f, top + h * 0.103f);
            path.lineTo(cx - h * 0.038f, top + h * 0.135f);
            path.lineTo(cx - h * 0.075f, top + h * 0.15f);
        } else if (variant == 3) {
            path.moveTo(cx - h * 0.073f, top + h * 0.115f);
            path.cubicTo(cx - h * 0.045f, top + h * 0.055f,
                    cx + h * 0.04f, top + h * 0.055f,
                    cx + h * 0.073f, top + h * 0.113f);
            path.lineTo(cx + h * 0.046f, top + h * 0.13f);
            path.lineTo(cx + h * 0.012f, top + h * 0.104f);
            path.lineTo(cx - h * 0.014f, top + h * 0.135f);
            path.lineTo(cx - h * 0.046f, top + h * 0.11f);
            path.lineTo(cx - h * 0.073f, top + h * 0.15f);
        } else {
            path.moveTo(cx - h * 0.074f, top + h * 0.12f);
            path.cubicTo(cx - h * 0.055f, top + h * 0.052f,
                    cx + h * 0.04f, top + h * 0.048f,
                    cx + h * 0.075f, top + h * 0.112f);
            path.lineTo(cx + h * 0.038f, top + h * 0.14f);
            path.lineTo(cx + h * 0.008f, top + h * 0.10f);
            path.lineTo(cx - h * 0.018f, top + h * 0.145f);
            path.lineTo(cx - h * 0.045f, top + h * 0.107f);
            path.lineTo(cx - h * 0.074f, top + h * 0.15f);
        }
        path.close();
        fillAndStroke(canvas, path, hair, outline, h * 0.0048f);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(h * 0.0032f);
        paint.setColor(withAlpha(lighten(hair, 0.35f), 105));
        canvas.drawArc(cx - h * 0.048f, top + h * 0.073f,
                cx + h * 0.045f, top + h * 0.142f,
                205f, 92f, false, paint);
        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawHeadAccessory(Canvas canvas, CharacterConfig config, float cx, float top,
                                   float h, int outline) {
        int variant = config.getVariant();
        paint.setStyle(Paint.Style.FILL);
        if (variant == 1) {
            paint.setColor(withAlpha(config.getSecondaryColor(), 220));
            canvas.drawRoundRect(cx - h * 0.057f, top + h * 0.115f,
                    cx + h * 0.057f, top + h * 0.145f,
                    h * 0.012f, h * 0.012f, paint);
            paint.setColor(Color.argb(125, 153, 244, 248));
            canvas.drawRoundRect(cx - h * 0.047f, top + h * 0.12f,
                    cx + h * 0.01f, top + h * 0.137f,
                    h * 0.006f, h * 0.006f, paint);
        } else if (variant == 2) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(h * 0.0042f);
            paint.setColor(outline);
            canvas.drawRoundRect(cx - h * 0.059f, top + h * 0.142f,
                    cx - h * 0.008f, top + h * 0.172f,
                    h * 0.009f, h * 0.009f, paint);
            canvas.drawRoundRect(cx + h * 0.008f, top + h * 0.142f,
                    cx + h * 0.059f, top + h * 0.172f,
                    h * 0.009f, h * 0.009f, paint);
            canvas.drawLine(cx - h * 0.008f, top + h * 0.155f,
                    cx + h * 0.008f, top + h * 0.155f, paint);
            paint.setStyle(Paint.Style.FILL);
        } else if (variant == 3) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(h * 0.011f);
            paint.setColor(config.getAccentColor());
            canvas.drawArc(cx - h * 0.085f, top + h * 0.055f,
                    cx + h * 0.085f, top + h * 0.19f,
                    195f, 150f, false, paint);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(cx - h * 0.089f, top + h * 0.135f,
                    cx - h * 0.068f, top + h * 0.185f,
                    h * 0.008f, h * 0.008f, paint);
            canvas.drawRoundRect(cx + h * 0.068f, top + h * 0.135f,
                    cx + h * 0.089f, top + h * 0.185f,
                    h * 0.008f, h * 0.008f, paint);
            paint.setStrokeCap(Paint.Cap.BUTT);
        } else if (variant == 4) {
            paint.setColor(config.getAccentColor());
            path.reset();
            path.moveTo(cx - h * 0.052f, top + h * 0.072f);
            path.lineTo(cx - h * 0.025f, top + h * 0.028f);
            path.lineTo(cx - h * 0.004f, top + h * 0.075f);
            path.close();
            canvas.drawPath(path, paint);
            path.reset();
            path.moveTo(cx + h * 0.052f, top + h * 0.072f);
            path.lineTo(cx + h * 0.025f, top + h * 0.028f);
            path.lineTo(cx + h * 0.004f, top + h * 0.075f);
            path.close();
            canvas.drawPath(path, paint);
        }
    }

    private void fillAndStroke(Canvas canvas, Path shape, int fill, int stroke, float strokeWidth) {
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(fill);
        canvas.drawPath(shape, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(Math.max(1f, strokeWidth));
        paint.setColor(stroke);
        canvas.drawPath(shape, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void fillAndStrokeCurrentShader(Canvas canvas, Path shape, int stroke, float strokeWidth) {
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(shape, paint);
        Shader shader = paint.getShader();
        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(Math.max(1f, strokeWidth));
        paint.setColor(stroke);
        canvas.drawPath(shape, paint);
        paint.setShader(shader);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawRoundedStroke(Canvas canvas, float x1, float y1, float x2, float y2,
                                   float width, int color) {
        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(width);
        paint.setColor(color);
        canvas.drawLine(x1, y1, x2, y2, paint);
        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setStyle(Paint.Style.FILL);
    }

    private int hairColor(CharacterConfig config) {
        switch (config.getVariant()) {
            case 2:
                return Color.rgb(91, 58, 35);
            case 3:
                return Color.rgb(38, 27, 50);
            case 4:
                return Color.rgb(225, 216, 192);
            case 1:
                return Color.rgb(29, 38, 47);
            default:
                return Color.rgb(24, 29, 36);
        }
    }

    private int characterOutline(CharacterConfig config) {
        return mix(config.getDarkColor(), Color.rgb(7, 12, 18), 0.55f);
    }

    private int lighten(int color, float amount) {
        return mix(color, Color.WHITE, amount);
    }

    private int darken(int color, float amount) {
        return mix(color, Color.BLACK, amount);
    }

    private int mix(int from, int to, float amount) {
        float a = Math.max(0f, Math.min(1f, amount));
        return Color.rgb(
                Math.round(Color.red(from) + (Color.red(to) - Color.red(from)) * a),
                Math.round(Color.green(from) + (Color.green(to) - Color.green(from)) * a),
                Math.round(Color.blue(from) + (Color.blue(to) - Color.blue(from)) * a));
    }

    private int withAlpha(int color, int alpha) {
        return Color.argb(Math.max(0, Math.min(255, alpha)),
                Color.red(color), Color.green(color), Color.blue(color));
    }
}
