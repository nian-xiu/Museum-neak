package com.museumheist.game.entity;

import com.museumheist.game.GameConfig;

/** Sweeping security camera with an independently decaying detection meter. */
public final class SecurityCamera {
    public enum AlertState { PATROL, SUSPICIOUS, ALERT, DISABLED }

    private final float x;
    private final float y;
    private final float baseAngle;
    private final float sweepAngle;
    private final float sweepSpeed;
    private final float viewDistance;
    private final float viewAngle;

    private float facingX;
    private float facingY;
    private float elapsed;
    private float suspicion;
    private boolean disabled;

    public SecurityCamera(float x, float y, float baseAngle, float sweepAngle, float sweepSpeed,
                          float viewDistance, float viewAngle) {
        this.x = x;
        this.y = y;
        this.baseAngle = baseAngle;
        this.sweepAngle = sweepAngle;
        this.sweepSpeed = sweepSpeed;
        this.viewDistance = viewDistance;
        this.viewAngle = viewAngle;
        reset();
    }

    public void reset() {
        elapsed = 0f;
        suspicion = 0f;
        disabled = false;
        updateFacing();
    }

    public void update(float deltaSeconds) {
        if (disabled) return;
        elapsed += Math.max(0f, deltaSeconds);
        updateFacing();
    }

    private void updateFacing() {
        float angle = baseAngle + (float) Math.sin(elapsed * sweepSpeed) * sweepAngle;
        facingX = (float) Math.cos(angle);
        facingY = (float) Math.sin(angle);
    }

    public boolean addSuspicion(float deltaSeconds) {
        suspicion = Math.min(
                GameConfig.CAMERA_SUSPICION_THRESHOLD_SECONDS * 1.1f,
                suspicion + Math.max(0f, deltaSeconds)
        );
        return suspicion >= GameConfig.CAMERA_SUSPICION_THRESHOLD_SECONDS;
    }

    public void decaySuspicion(float deltaSeconds, float decaySeconds) {
        float rate = GameConfig.CAMERA_SUSPICION_THRESHOLD_SECONDS / Math.max(0.1f, decaySeconds);
        suspicion = Math.max(0f, suspicion - Math.max(0f, deltaSeconds) * rate);
    }

    public float getCurrentAngleRadians() { return (float) Math.atan2(facingY, facingX); }
    public float getX() { return x; }
    public float getY() { return y; }
    public float getFacingX() { return facingX; }
    public float getFacingY() { return facingY; }
    public float getViewDistance() { return viewDistance; }
    public float getViewAngleRadians() { return viewAngle; }

    public float getSuspicionProgress() {
        return Math.min(1f, suspicion / GameConfig.CAMERA_SUSPICION_THRESHOLD_SECONDS);
    }

    public AlertState getAlertState() {
        if (disabled) return AlertState.DISABLED;
        if (suspicion >= GameConfig.CAMERA_SUSPICION_THRESHOLD_SECONDS) return AlertState.ALERT;
        return suspicion > 0f ? AlertState.SUSPICIOUS : AlertState.PATROL;
    }

    public boolean isDisabled() { return disabled; }

    public void setDisabled(boolean value) {
        disabled = value;
        if (value) suspicion = 0f;
    }
}
