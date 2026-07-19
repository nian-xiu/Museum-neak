package com.museumheist.game;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.view.HapticFeedbackConstants;
import android.view.SoundEffectConstants;
import android.view.View;

/** Small fault-tolerant audio/haptic feedback facade. */
public final class GameFeedback {
    private ToneGenerator tone;
    private boolean soundEnabled = true;
    private boolean hapticEnabled = true;

    public void setSoundEnabled(boolean value) { soundEnabled = value; }
    public void setHapticEnabled(boolean value) { hapticEnabled = value; }

    public void click(View view) {
        if (soundEnabled) view.post(() -> view.playSoundEffect(SoundEffectConstants.CLICK));
        haptic(view, HapticFeedbackConstants.VIRTUAL_KEY);
    }

    public void collect(View view) {
        play(ToneGenerator.TONE_PROP_ACK, 80);
        haptic(view, HapticFeedbackConstants.KEYBOARD_TAP);
    }

    public void warning(View view) {
        play(ToneGenerator.TONE_PROP_BEEP2, 95);
        haptic(view, Build.VERSION.SDK_INT >= 30
                ? HapticFeedbackConstants.GESTURE_START
                : HapticFeedbackConstants.CLOCK_TICK);
    }

    public void clear(View view) {
        play(ToneGenerator.TONE_PROP_PROMPT, 180);
        haptic(view, Build.VERSION.SDK_INT >= 30
                ? HapticFeedbackConstants.CONFIRM
                : HapticFeedbackConstants.LONG_PRESS);
    }

    public void fail(View view) {
        play(ToneGenerator.TONE_PROP_NACK, 150);
        haptic(view, Build.VERSION.SDK_INT >= 30
                ? HapticFeedbackConstants.REJECT
                : HapticFeedbackConstants.LONG_PRESS);
    }

    public synchronized void release() {
        if (tone != null) {
            tone.release();
            tone = null;
        }
    }

    private synchronized void play(int type, int durationMs) {
        if (!soundEnabled) return;
        try {
            if (tone == null) tone = new ToneGenerator(AudioManager.STREAM_MUSIC, 42);
            tone.startTone(type, durationMs);
        } catch (RuntimeException ignored) {
            if (tone != null) tone.release();
            tone = null;
        }
    }

    private void haptic(View view, int constant) {
        if (hapticEnabled) view.post(() -> view.performHapticFeedback(constant));
    }
}
