package com.museumheist.game.entity;

import android.graphics.PointF;

import com.museumheist.game.GameConfig;
import com.museumheist.game.logic.PatrolPath;
import com.museumheist.game.world.Level;

/** Runtime model for guards, staff members and patrol robots. */
public final class Guard {
    public enum Kind { GUARD, ROBOT, STAFF }
    public enum AlertState { PATROL, SUSPICIOUS, ALERT, DISABLED }

    private static final float TWO_PI = (float) (Math.PI * 2d);

    private final Kind kind;
    private final PatrolPath path;
    private final float speed;
    private final float viewDistance;
    private final float viewAngle;

    private float x;
    private float y;
    private float facingX = 1f;
    private float facingY;
    private int pathIndex;
    private float suspicion;
    private boolean disabled;
    private float investigateSeconds;
    private float investigateX;
    private float investigateY;
    private float movementBlend;
    private float runBlend;
    private float gaitPhase;

    public Guard(Kind kind, PatrolPath path, float speed, float viewDistance, float viewAngle) {
        this.kind = kind;
        this.path = path;
        this.speed = speed;
        this.viewDistance = viewDistance;
        this.viewAngle = viewAngle;
        reset();
    }

    public void reset() {
        PointF start = path.get(0);
        x = start.x;
        y = start.y;
        pathIndex = path.size() > 1 ? 1 : 0;
        facingX = 1f;
        facingY = 0f;
        suspicion = 0f;
        disabled = false;
        investigateSeconds = 0f;
        investigateX = x;
        investigateY = y;
        movementBlend = 0f;
        runBlend = 0f;
        gaitPhase = 0f;
    }

    public void update(float deltaSeconds, Level level) {
        float dt = Math.max(0f, Math.min(0.05f, deltaSeconds));
        if (disabled || dt <= 0f) {
            settlePose(dt);
            return;
        }

        boolean investigating = investigateSeconds > 0f;
        if (investigating) {
            investigateSeconds = Math.max(0f, investigateSeconds - dt);
        }
        PointF patrolTarget = path.get(pathIndex);
        float targetX = investigating ? investigateX : patrolTarget.x;
        float targetY = investigating ? investigateY : patrolTarget.y;
        float dx = targetX - x;
        float dy = targetY - y;
        float distance = (float) Math.hypot(dx, dy);
        if (distance < 4f) {
            if (!investigating && path.size() > 0) {
                pathIndex = (pathIndex + 1) % path.size();
            }
            settlePose(dt);
            return;
        }

        float targetFacingX = dx / distance;
        float targetFacingY = dy / distance;
        float turnResponse = 1f - (float) Math.exp(-dt * (kind == Kind.ROBOT ? 15f : 11f));
        facingX += (targetFacingX - facingX) * turnResponse;
        facingY += (targetFacingY - facingY) * turnResponse;
        float facingLength = (float) Math.hypot(facingX, facingY);
        if (facingLength > 0.001f) {
            facingX /= facingLength;
            facingY /= facingLength;
        }

        float behaviorMultiplier = investigating ? 1.24f : 1f;
        float movedDistance = Math.min(distance, speed * behaviorMultiplier * dt);
        x += targetFacingX * movedDistance;
        y += targetFacingY * movedDistance;

        float targetMovement = movedDistance > 0.01f ? 1f : 0f;
        movementBlend += (targetMovement - movementBlend)
                * (1f - (float) Math.exp(-dt * 12f));
        float targetRun = investigating ? 0.88f : 0f;
        runBlend += (targetRun - runBlend)
                * (1f - (float) Math.exp(-dt * (investigating ? 8f : 6f)));

        if (movedDistance > 0.001f) {
            float walkCycle = kind == Kind.ROBOT ? 92f : kind == Kind.STAFF ? 104f : 110f;
            float cycleDistance = walkCycle + runBlend * (kind == Kind.ROBOT ? 30f : 42f);
            gaitPhase += movedDistance / cycleDistance * TWO_PI;
            gaitPhase %= TWO_PI;
        }
    }

    private void settlePose(float dt) {
        movementBlend += (0f - movementBlend) * Math.min(1f, dt * 11f);
        runBlend += (0f - runBlend) * Math.min(1f, dt * 8f);
        if (movementBlend < 0.002f) movementBlend = 0f;
        if (runBlend < 0.002f) runBlend = 0f;
    }

    public boolean addSuspicion(float amount, float playerX, float playerY) {
        suspicion = Math.min(threshold() * 1.1f, suspicion + Math.max(0f, amount));
        investigateX = playerX;
        investigateY = playerY;
        return suspicion >= threshold();
    }

    public void decaySuspicion(float deltaSeconds, float decaySeconds) {
        float rate = threshold() / Math.max(0.1f, decaySeconds);
        suspicion = Math.max(0f, suspicion - Math.max(0f, deltaSeconds) * rate);
    }

    public void investigate(float targetX, float targetY, float seconds) {
        investigateX = targetX;
        investigateY = targetY;
        investigateSeconds = Math.max(investigateSeconds, Math.max(0f, seconds));
    }

    private float threshold() {
        return kind == Kind.STAFF
                ? GameConfig.STAFF_SUSPICION_THRESHOLD_SECONDS
                : GameConfig.SUSPICION_THRESHOLD_SECONDS;
    }

    public Kind getKind() { return kind; }
    public PatrolPath getPatrolPath() { return path; }
    public float getX() { return x; }
    public float getY() { return y; }
    public float getFacingX() { return facingX; }
    public float getFacingY() { return facingY; }
    public float getViewDistance() { return viewDistance; }
    public float getViewAngleRadians() { return viewAngle; }
    public float getSuspicionProgress() { return Math.min(1f, suspicion / threshold()); }
    public boolean isInvestigating() { return investigateSeconds > 0f; }
    public float getMovementBlend() { return movementBlend; }
    public float getRunBlend() { return runBlend; }
    public float getGaitPhase() { return gaitPhase; }
    public float getStride() { return (float) Math.sin(gaitPhase) * movementBlend; }

    public AlertState getAlertState() {
        if (disabled) return AlertState.DISABLED;
        if (suspicion >= threshold()) return AlertState.ALERT;
        return suspicion > 0f || isInvestigating() ? AlertState.SUSPICIOUS : AlertState.PATROL;
    }

    public boolean isDisabled() { return disabled; }

    public void setDisabled(boolean value) {
        disabled = value;
        if (value) {
            suspicion = 0f;
        }
    }
}
