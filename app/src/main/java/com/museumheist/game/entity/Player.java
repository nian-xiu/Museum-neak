package com.museumheist.game.entity;

/** Mutable player pose used by collision, camera and sprite animation systems. */
public final class Player {
    private static final float TWO_PI = (float) (Math.PI * 2d);

    private float x;
    private float y;
    private float facingX = 1f;
    private float facingY;
    private final float speed;
    private final float radius;
    private float movementBlend;
    private float runBlend;
    private float gaitPhase;
    private float bodyLean;

    public Player(float radius, float speed) {
        this.radius = radius;
        this.speed = speed;
    }

    public void reset(float x, float y) {
        this.x = x;
        this.y = y;
        facingX = 1f;
        facingY = 0f;
        movementBlend = 0f;
        runBlend = 0f;
        gaitPhase = 0f;
        bodyLean = 0f;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void updateMovementVisuals(float inputX, float inputY, float deltaSeconds,
                                      float multiplier, float movedDistance) {
        float dt = Math.max(0f, Math.min(0.05f, deltaSeconds));
        float inputLength = (float) Math.hypot(inputX, inputY);
        if (inputLength > 0.05f) {
            float targetFacingX = inputX / inputLength;
            float targetFacingY = inputY / inputLength;
            float turnResponse = 1f - (float) Math.exp(-dt * 22f);
            facingX += (targetFacingX - facingX) * turnResponse;
            facingY += (targetFacingY - facingY) * turnResponse;
            float facingLength = (float) Math.hypot(facingX, facingY);
            if (facingLength > 0.001f) {
                facingX /= facingLength;
                facingY /= facingLength;
            }
        }

        float actualDistance = Math.max(0f, movedDistance);
        float expectedDistance = speed * Math.max(0.1f, multiplier) * Math.max(0.001f, dt);
        float targetMovement = actualDistance > 0.01f
                ? Math.min(1f, actualDistance / expectedDistance)
                : 0f;
        float movementResponse = 1f - (float) Math.exp(-dt * (targetMovement > movementBlend ? 18f : 13f));
        movementBlend += (targetMovement - movementBlend) * movementResponse;
        if (movementBlend < 0.002f) {
            movementBlend = 0f;
        }

        float targetRun = actualDistance > 0.01f
                ? clamp((multiplier - 1.20f) / 0.30f)
                : 0f;
        float runResponse = 1f - (float) Math.exp(-dt * (targetRun > runBlend ? 10f : 8f));
        runBlend += (targetRun - runBlend) * runResponse;
        if (runBlend < 0.002f) {
            runBlend = 0f;
        }

        if (actualDistance > 0.001f) {
            float cycleDistance = 118f + runBlend * 46f;
            gaitPhase += actualDistance / cycleDistance * TWO_PI;
            gaitPhase %= TWO_PI;
        }

        float targetLean = movementBlend * runBlend * 0.10f;
        bodyLean += (targetLean - bodyLean) * Math.min(1f, dt * 9f);
    }

    private float clamp(float value) {
        return Math.max(0f, Math.min(1f, value));
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getRadius() { return radius; }
    public float getSpeed() { return speed; }
    public float getFacingX() { return facingX; }
    public float getFacingY() { return facingY; }
    public float getMovementBlend() { return movementBlend; }
    public float getRunBlend() { return runBlend; }
    public float getGaitPhase() { return gaitPhase; }
    public float getStride() { return (float) Math.sin(gaitPhase) * movementBlend; }
    public float getStepLift() { return Math.abs((float) Math.sin(gaitPhase)) * movementBlend; }
    public float getBodyLean() { return bodyLean; }
}
