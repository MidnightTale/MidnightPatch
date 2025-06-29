package fun.mntale.midnightPatch.module.entity.player.indicator;

import org.joml.Matrix4f;

import fun.mntale.midnightPatch.MidnightPatch;

import org.bukkit.entity.TextDisplay;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;

public class DisplayAnimator {
    // Animation phase percentages and delays
    private static final double POP_UP_PERCENT = 0.05;
    private static final double BOUNCE_PERCENT = 0.10;
    private static final double STABILIZE_PERCENT = 0.35;
    private static final double FADE_PERCENT = 0.50;
    private static final int INITIAL_DELAY = 2;
    // Scale multipliers
    private static final float POP_SCALE = 1.5f;
    private static final float BOUNCE_SCALE = 1.2f;
    private static final float FADE_SCALE = 0.1f;
    private static final double POP_Y_OFFSET = 0.3;
    private static final double BOUNCE_Y_OFFSET = 0.2;
    private static final double FADE_Y_OFFSET = 3.6;
    private final TextDisplay display;
    private final int duration;
    private final float targetScale;
    private final double targetYOffset;
    public DisplayAnimator(TextDisplay display, int duration, float targetScale, double targetYOffset) {
        this.display = display;
        this.duration = duration;
        this.targetScale = targetScale;
        this.targetYOffset = targetYOffset;
    }
    public void start() {
        // Initial state
        display.setTransformationMatrix(new Matrix4f().scale(0.0f).translate(0, 0, 0));
        // Calculate phase timings
        int[] durations = calculateDurations();
        int[] delays = calculateDelays(durations);
        // Schedule animation sequence
        FoliaScheduler.getEntityScheduler().runDelayed(display, MidnightPatch.instance, (task2) -> {
            animate(targetScale * POP_SCALE, targetYOffset * POP_Y_OFFSET, durations[0], delays[0]);
            animate(targetScale * BOUNCE_SCALE, targetYOffset * BOUNCE_Y_OFFSET, durations[1], delays[1]);
            animate(targetScale, targetYOffset * POP_Y_OFFSET, durations[2], delays[2]);
            animate(FADE_SCALE, targetYOffset + FADE_Y_OFFSET, durations[3], delays[3]);
        }, null, INITIAL_DELAY);
        // Schedule removal
        FoliaScheduler.getEntityScheduler().runDelayed(display, MidnightPatch.instance, (task) -> display.remove(), null, duration);
    }
    private int[] calculateDurations() {
        return new int[] {
            (int)(duration * POP_UP_PERCENT),
            (int)(duration * BOUNCE_PERCENT),
            (int)(duration * STABILIZE_PERCENT),
            (int)(duration * FADE_PERCENT)
        };
    }
    private int[] calculateDelays(int[] durations) {
        return new int[] {
            INITIAL_DELAY,
            INITIAL_DELAY + durations[0],
            INITIAL_DELAY + durations[0] + durations[1],
            INITIAL_DELAY + durations[0] + durations[1] + durations[2]
        };
    }
    private void animate(float scale, double yOffset, int duration, int delay) {
        FoliaScheduler.getEntityScheduler().runDelayed(display, MidnightPatch.instance, (task) -> {
            display.setInterpolationDelay(0);
            display.setInterpolationDuration(duration);
            Matrix4f matrix = new Matrix4f()
                .scale(scale)
                .translate(
                    0,
                    (float)yOffset,
                    0
                );
            display.setTransformationMatrix(matrix);
        }, null, delay);
    }
} 