package com.museumheist.game.render;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;

import com.museumheist.R;
import com.museumheist.game.character.CharacterConfig;
import com.museumheist.game.entity.Guard;
import com.museumheist.game.entity.Player;
import com.museumheist.game.entity.SecurityCamera;

/** Draws grounded eight-direction sprite animation for every moving actor. */
public final class ActorRenderer {
    private static final int FRAME_WIDTH = 96;
    private static final int FRAME_HEIGHT = 128;
    private static final int FRAME_COUNT = 8;
    private static final int DIRECTION_COUNT = 8;
    private static final float TWO_PI = (float) (Math.PI * 2d);

    private final Resources resources;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Paint spritePaint = new Paint(
            Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
    private final Path path = new Path();
    private final Rect source = new Rect();
    private final RectF target = new RectF();
    private final RectF rect = new RectF();
    private final Bitmap guardSheet;
    private final Bitmap staffSheet;
    private final Bitmap robotSheet;

    private Bitmap playerSheet;
    private int playerVariant = -1;

    public ActorRenderer(Resources resources) {
        this.resources = resources;
        guardSheet = decode(R.drawable.actor_guard_sprites);
        staffSheet = decode(R.drawable.actor_staff_sprites);
        robotSheet = decode(R.drawable.actor_robot_sprites);
        spritePaint.setFilterBitmap(true);
    }

    public void drawPlayer(Canvas canvas, Player player, CharacterConfig config, boolean invisible,
                           boolean phase, boolean boost, boolean speed, int treasures, float time) {
        float x = player.getX();
        float y = player.getY();
        float r = player.getRadius();
        float movement = player.getMovementBlend();
        int alpha = invisible ? 112 : 255;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(invisible ? Color.argb(25, 0, 0, 0) : Color.argb(78, 0, 0, 0));
        rect.set(x - r * (0.82f + movement * 0.08f), y + r * 0.69f,
                x + r * (0.82f + movement * 0.08f), y + r * 1.08f);
        canvas.drawOval(rect, paint);

        if (boost || speed) {
            drawMotionWake(canvas, player, boost, movement);
        }
        if (phase) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Math.max(2f, r * 0.09f));
            paint.setColor(Color.argb(150, 118, 207, 224));
            float pulse = 1f + (float) Math.sin(time * 5f) * 0.035f;
            rect.set(x - r * 1.56f * pulse, y - r * 2.53f * pulse,
                    x + r * 1.56f * pulse, y + r * 1.18f * pulse);
            canvas.drawOval(rect, paint);
            paint.setStyle(Paint.Style.FILL);
        }

        if (treasures > 0) {
            drawTreasurePack(canvas, player, config, treasures, alpha);
        }

        Bitmap sheet = playerSheet(config.getVariant());
        int action = chooseAction(movement, player.getRunBlend());
        int direction = directionIndex(player.getFacingX(), player.getFacingY());
        int frame = animationFrame(action, player.getGaitPhase(), time, config.getVariant() * 0.19f);
        float height = r * 4.50f;
        float width = height * FRAME_WIDTH / FRAME_HEIGHT;
        float bottom = y + r * 1.36f;
        drawSprite(canvas, sheet, action, direction, frame,
                x - width * 0.5f, bottom - height, x + width * 0.5f, bottom, alpha);
    }

    private void drawMotionWake(Canvas canvas, Player player, boolean boost, float movement) {
        float x = player.getX();
        float y = player.getY();
        float r = player.getRadius();
        float backX = -player.getFacingX();
        float backY = -player.getFacingY();
        int trailColor = boost ? Color.rgb(238, 128, 54) : Color.rgb(104, 196, 202);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        for (int i = 0; i < 3; i++) {
            paint.setStrokeWidth(Math.max(1.5f, r * (0.11f - i * 0.02f)));
            paint.setColor(withAlpha(trailColor, 82 - i * 19));
            float side = (i - 1) * r * 0.34f;
            float sideX = -player.getFacingY() * side;
            float sideY = player.getFacingX() * side;
            canvas.drawLine(x + sideX + backX * r * 0.74f, y + sideY + backY * r * 0.74f,
                    x + sideX + backX * r * (1.62f + movement * 0.48f),
                    y + sideY + backY * r * (1.62f + movement * 0.48f), paint);
        }
        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawTreasurePack(Canvas canvas, Player player, CharacterConfig config,
                                  int treasures, int alpha) {
        float r = player.getRadius();
        float x = player.getX() - player.getFacingX() * r * 0.34f;
        float y = player.getY() - r * 0.45f - player.getFacingY() * r * 0.18f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(withAlpha(darken(config.getDarkColor(), 0.74f), alpha));
        rect.set(x - r * 0.35f, y - r * 0.42f, x + r * 0.35f, y + r * 0.37f);
        canvas.drawRoundRect(rect, r * 0.12f, r * 0.12f, paint);
        paint.setColor(withAlpha(config.getAccentColor(), alpha));
        canvas.drawRoundRect(x - r * 0.22f, y - r * 0.31f,
                x + r * 0.22f, y - r * 0.18f, r * 0.05f, r * 0.05f, paint);
        if (treasures > 1) {
            paint.setColor(withAlpha(Color.rgb(226, 194, 75), alpha));
            canvas.drawCircle(x + r * 0.34f, y - r * 0.34f, r * 0.20f, paint);
            paint.setColor(withAlpha(Color.rgb(29, 31, 34), alpha));
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setFakeBoldText(true);
            paint.setTextSize(r * 0.27f);
            canvas.drawText(String.valueOf(Math.min(9, treasures)),
                    x + r * 0.34f, y - r * 0.25f, paint);
            paint.setFakeBoldText(false);
            paint.setTextAlign(Paint.Align.LEFT);
        }
    }

    public void drawGuard(Canvas canvas, Guard guard, float time) {
        float x = guard.getX();
        float y = guard.getY();
        boolean robot = guard.getKind() == Guard.Kind.ROBOT;
        boolean staff = guard.getKind() == Guard.Kind.STAFF;
        float height = robot ? 84f : staff ? 87f : 90f;
        float width = height * FRAME_WIDTH / FRAME_HEIGHT;
        float footOffset = robot ? 25f : 28f;
        int alpha = guard.isDisabled() ? 132 : 255;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(guard.isDisabled() ? 38 : 72, 0, 0, 0));
        rect.set(x - width * 0.27f, y + 15f, x + width * 0.27f, y + 24f);
        canvas.drawOval(rect, paint);

        Bitmap sheet = robot ? robotSheet : staff ? staffSheet : guardSheet;
        int action = guard.isDisabled() ? 0 : chooseAction(guard.getMovementBlend(), guard.getRunBlend());
        int direction = directionIndex(guard.getFacingX(), guard.getFacingY());
        int frame = animationFrame(action, guard.getGaitPhase(), time,
                ((int) x * 0.013f + (int) y * 0.007f));
        drawSprite(canvas, sheet, action, direction, frame,
                x - width * 0.5f, y + footOffset - height,
                x + width * 0.5f, y + footOffset, alpha);

        if (guard.isDisabled()) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2.2f);
            paint.setColor(Color.argb(150, 104, 196, 202));
            canvas.drawCircle(x, y - 8f, 24f + (float) Math.sin(time * 4f) * 2f, paint);
            paint.setStyle(Paint.Style.FILL);
        }
        drawAlertBadge(canvas, guard, height);
    }

    private int chooseAction(float movementBlend, float runBlend) {
        if (movementBlend < 0.12f) {
            return 0;
        }
        return runBlend >= 0.42f ? 2 : 1;
    }

    private int animationFrame(int action, float gaitPhase, float time, float offset) {
        if (action == 0) {
            return positiveModulo((int) Math.floor((time + offset) * 3.2f), FRAME_COUNT);
        }
        float normalized = positiveModulo(gaitPhase, TWO_PI) / TWO_PI;
        return positiveModulo((int) Math.floor(normalized * FRAME_COUNT), FRAME_COUNT);
    }

    private int directionIndex(float facingX, float facingY) {
        if (Math.abs(facingX) + Math.abs(facingY) < 0.001f) {
            return 0;
        }
        int octant = Math.round((float) Math.atan2(facingY, facingX) / ((float) Math.PI / 4f));
        return positiveModulo(octant, DIRECTION_COUNT);
    }

    private void drawSprite(Canvas canvas, Bitmap sheet, int action, int direction, int frame,
                            float left, float top, float right, float bottom, int alpha) {
        int row = action * DIRECTION_COUNT + direction;
        int sourceLeft = frame * FRAME_WIDTH;
        int sourceTop = row * FRAME_HEIGHT;
        source.set(sourceLeft, sourceTop, sourceLeft + FRAME_WIDTH, sourceTop + FRAME_HEIGHT);
        target.set(left, top, right, bottom);
        spritePaint.setAlpha(Math.max(0, Math.min(255, alpha)));
        canvas.drawBitmap(sheet, source, target, spritePaint);
        spritePaint.setAlpha(255);
    }

    private Bitmap playerSheet(int variant) {
        int normalized = Math.max(0, Math.min(4, variant));
        if (playerSheet == null || playerVariant != normalized) {
            if (playerSheet != null && !playerSheet.isRecycled()) {
                playerSheet.recycle();
            }
            playerSheet = decode(playerResource(normalized));
            playerVariant = normalized;
        }
        return playerSheet;
    }

    private int playerResource(int variant) {
        switch (variant) {
            case 1:
                return R.drawable.actor_tech_scout_sprites;
            case 2:
                return R.drawable.actor_museum_researcher_sprites;
            case 3:
                return R.drawable.actor_street_player_sprites;
            case 4:
                return R.drawable.actor_fantasy_guardian_sprites;
            default:
                return R.drawable.actor_city_explorer_sprites;
        }
    }

    private Bitmap decode(int resourceId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        return BitmapFactory.decodeResource(resources, resourceId, options);
    }

    private void drawAlertBadge(Canvas canvas, Guard guard, float spriteHeight) {
        if (guard.getAlertState() == Guard.AlertState.PATROL || guard.isDisabled()) {
            return;
        }
        float x = guard.getX();
        float y = guard.getY() - spriteHeight * 0.68f;
        float progress = guard.getSuspicionProgress();
        int color = guard.getAlertState() == Guard.AlertState.ALERT
                ? Color.rgb(235, 88, 80) : Color.rgb(226, 194, 75);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(205, 12, 18, 24));
        canvas.drawCircle(x, y, 11f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);
        paint.setColor(color);
        canvas.drawArc(x - 9f, y - 9f, x + 9f, y + 9f,
                -90f, 360f * progress, false, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);
        paint.setTextSize(15f);
        canvas.drawText(guard.getAlertState() == Guard.AlertState.ALERT ? "!" : "?",
                x, y + 5f, paint);
        paint.setFakeBoldText(false);
        paint.setTextAlign(Paint.Align.LEFT);
    }

    public void drawCamera(Canvas canvas, SecurityCamera camera, boolean disrupted, float time) {
        float x = camera.getX();
        float y = camera.getY();
        float angle = (float) Math.toDegrees(camera.getCurrentAngleRadians());
        int body = disrupted ? Color.rgb(62, 76, 80) : Color.rgb(52, 75, 101);
        int rim = disrupted ? Color.rgb(96, 116, 120) : Color.rgb(127, 166, 207);
        int lens = disrupted ? Color.rgb(104, 196, 202)
                : camera.getSuspicionProgress() > 0f ? Color.rgb(235, 88, 80) : Color.rgb(100, 205, 233);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(70, 0, 0, 0));
        canvas.drawCircle(x + 5f, y + 7f, 23f, paint);
        paint.setColor(Color.rgb(27, 34, 42));
        canvas.drawCircle(x, y, 21f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4f);
        paint.setColor(rim);
        canvas.drawArc(x - 18f, y - 18f, x + 18f, y + 18f, 198f, 150f, false, paint);
        paint.setStyle(Paint.Style.FILL);

        canvas.save();
        canvas.translate(x, y);
        canvas.rotate(angle);
        paint.setColor(body);
        path.reset();
        path.moveTo(-8f, -13f);
        path.lineTo(26f, -9f);
        path.quadTo(34f, 0f, 26f, 9f);
        path.lineTo(-8f, 13f);
        path.close();
        canvas.drawPath(path, paint);
        paint.setColor(rim);
        canvas.drawRoundRect(12f, -8f, 29f, 8f, 5f, 5f, paint);
        paint.setColor(Color.rgb(18, 26, 34));
        canvas.drawCircle(27f, 0f, 6.5f, paint);
        float pulse = disrupted ? 0.5f : 0.72f + (float) Math.sin(time * 5.4f) * 0.28f;
        paint.setColor(withAlpha(lens, (int) (255f * pulse)));
        canvas.drawCircle(27f, 0f, 3.4f, paint);
        canvas.restore();

        if (camera.getSuspicionProgress() > 0f && !disrupted) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3f);
            paint.setColor(lens);
            canvas.drawArc(x - 15f, y - 15f, x + 15f, y + 15f,
                    -90f, 360f * camera.getSuspicionProgress(), false, paint);
            paint.setStyle(Paint.Style.FILL);
        }
    }

    private int positiveModulo(int value, int modulus) {
        int result = value % modulus;
        return result < 0 ? result + modulus : result;
    }

    private float positiveModulo(float value, float modulus) {
        float result = value % modulus;
        return result < 0f ? result + modulus : result;
    }

    private int darken(int color, float factor) {
        return Color.rgb(
                Math.max(0, Math.min(255, Math.round(Color.red(color) * factor))),
                Math.max(0, Math.min(255, Math.round(Color.green(color) * factor))),
                Math.max(0, Math.min(255, Math.round(Color.blue(color) * factor)))
        );
    }

    private int withAlpha(int color, int alpha) {
        return Color.argb(Math.max(0, Math.min(255, alpha)),
                Color.red(color), Color.green(color), Color.blue(color));
    }
}
