package org.firstinspires.ftc.teamcode.localization;

/**
 * Finds, from what the drive was told and what the odometry measured, the moments that may knock
 * the pose off: a <b>push</b> (the drive told to go, the robot not moving: against a wall, the
 * FLOWER or another robot) and a <b>jolt</b> (the measured speed changing faster than the drive
 * can, as in a collision, or a pod leaving the floor). The match log gets one event for each,
 * so the camera's gap to the odometry ({@code /Drift/...}) can be checked before and after.
 *
 * <p>It says what happened, not what the pose did: whether pushes or jolts make the pose drift is
 * the question the log is for. The thresholds are starting points; the raw signals are logged
 * beside the events, so the analysis can try others.
 *
 * <p>Plain Java with no SDK types, so it is unit tested. The caller passes the time.
 */
public final class ImpactWatch {

    /** A command at least this large (0 to 1, translation) counts as told to go. */
    static final double PUSH_COMMAND = 0.4;
    /** Slower than this, inches/s, the robot is not going. */
    static final double PUSH_SPEED_IN_PER_SEC = 3.0;
    /** A push is reported once it has lasted this long, ms. */
    static final double PUSH_MIN_MS = 300;
    /** A speed change faster than this, inches/s², is a jolt. */
    static final double JOLT_IN_PER_SEC2 = 250;
    /** After a jolt, the next is not reported for this long, ms: one knock, one event. */
    static final double JOLT_HOLDOFF_MS = 500;

    /** What one step found. */
    public enum Event { NONE, PUSH_ENDED, JOLT }

    private long pushStartMs = -1;
    private double pushPeakCommand;
    private double lastPushMs;
    private double lastPushPeakCommand;
    private double lastSpeed = Double.NaN;
    private long lastMs;
    private long lastJoltMs = Long.MIN_VALUE / 2;
    private double lastJoltInPerSec2;

    /**
     * One loop.
     *
     * @param command translation command magnitude, 0 to 1; NaN when a path drives (no push then)
     * @param speedInPerSec the odometry's speed over the ground; NaN without it
     */
    public Event update(long nowMs, double command, double speedInPerSec) {
        Event event = Event.NONE;
        if (!Double.isNaN(speedInPerSec) && !Double.isNaN(lastSpeed) && nowMs > lastMs) {
            double accel = Math.abs(speedInPerSec - lastSpeed) / ((nowMs - lastMs) / 1000.0);
            if (accel > JOLT_IN_PER_SEC2 && nowMs - lastJoltMs >= JOLT_HOLDOFF_MS) {
                lastJoltMs = nowMs;
                lastJoltInPerSec2 = accel;
                event = Event.JOLT;
            }
        }
        lastSpeed = speedInPerSec;
        lastMs = nowMs;

        boolean pushing = command >= PUSH_COMMAND && speedInPerSec < PUSH_SPEED_IN_PER_SEC;
        if (pushing) {
            if (pushStartMs < 0) {
                pushStartMs = nowMs;
                pushPeakCommand = 0.0;
            }
            pushPeakCommand = Math.max(pushPeakCommand, command);
        } else if (pushStartMs >= 0) {
            double lasted = nowMs - pushStartMs;
            pushStartMs = -1;
            if (lasted >= PUSH_MIN_MS && event == Event.NONE) {
                lastPushMs = lasted;
                lastPushPeakCommand = pushPeakCommand;
                event = Event.PUSH_ENDED;
            }
        }
        return event;
    }

    /** Whether a push is under way now (counted from its start, reported at its end). */
    public boolean pushing() {
        return pushStartMs >= 0;
    }

    /** The last reported push: how long, ms, and its largest command. */
    public double lastPushMs() { return lastPushMs; }
    public double lastPushPeakCommand() { return lastPushPeakCommand; }

    /** The last jolt's speed change, inches/s². */
    public double lastJoltInPerSec2() { return lastJoltInPerSec2; }
}
