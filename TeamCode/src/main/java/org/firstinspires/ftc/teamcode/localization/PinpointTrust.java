package org.firstinspires.ftc.teamcode.localization;

/**
 * Whether the Pinpoint's pose is believed after PLAY. Pure, so a test can drive it.
 *
 * <p>{@link State#TRUSTED} until a fault holds {@link #LOST_AFTER_MS}; then {@link State#FAULTED}
 * while it holds; once the Pinpoint reads again, {@link State#CONFIRMING} until something confirms
 * the pose, and then trusted again. A fault that never clears (4 Nov 2026, R-Solo 21:26 and the
 * run after it: y frozen at 223 in for the whole run) never leaves FAULTED, so the pose is never
 * believed in that run, as before.
 *
 * <p><b>Why a fault that clears is believed again.</b> Belding, 7 Nov 2026, 19429: Q13 faulted at
 * 226.3 s and read again at 233.4 s; the lower final faulted in the Auto at 42.5 s and read again
 * at 105.9 s. 19 of the 22 Square and FLOWER pick refusals in those runs came after it read again.
 * Against the camera's range to our CELL, the pose after recovery was as good as in a normal match
 * (median gap 0.2 in in Q13's first 7 s, 1.7 in, 90% under 4.2 in, over the final's 70 s; normal
 * matches 1.6 in, 90% under 6.7 in): the Pinpoint keeps integrating on its own board while the
 * hub's reads fail, so its pose is right when they resume. That was range only, so a sideways error
 * would not have shown; hence a confirmation rather than trusting the first good read.
 *
 * <p><b>What confirms</b> (only while the Pinpoint reads):
 * <ul>
 *   <li>a pose set on purpose ({@link #poseSet}): a corner reset, a camera reset, a heading fix
 *       from the camera. One made during the fault counts if the Pinpoint reads again within
 *       {@link #RESET_CLEARS_WITHIN_MS} of it: writing an on-field pose is what clears the SDK
 *       driver's jump rejection ({@code isPositionCorrupt}: a read more than 5000 mm from its stored
 *       value is refused), so the press that cleared the fault set the pose too.</li>
 *   <li>the camera agreeing ({@link #cameraGap}): {@link #AGREE_FRAMES} gated camera frames in a row,
 *       spanning at least {@link #AGREE_SPAN_MS}, each with its x, y (solved with the Pinpoint's
 *       heading) within {@link #AGREE_IN} of the pose. That is full position, not range, and a wrong
 *       heading shows in it too (13 in per 15° at 50 in).</li>
 * </ul>
 *
 * <p>Before PLAY nothing is decided here: an Autonomous's INIT already warns
 * ({@code PinpointInitFault}), and a restart then is the fix.
 */
public final class PinpointTrust {

    /** Where the pose stands. */
    public enum State {
        /** Believed: no fault, or only blips shorter than {@link #LOST_AFTER_MS}. */
        TRUSTED,
        /** A fault has held {@link #LOST_AFTER_MS} and still holds: the pose is frozen. */
        FAULTED,
        /** Reading again after a fault; not believed until a reset or the camera confirms it. */
        CONFIRMING
    }

    /** A fault after PLAY must hold this long to count: longer than every blip in the logs (220 ms). */
    public static final long LOST_AFTER_MS = 500;

    /**
     * The camera agrees within this, inches. Normal matches' camera-vs-odometry range gap: median
     * 1.6 in, 90% under 6.7 in; after recovery 90% under 4.2 in; while frozen median 5.6 in, 90% under
     * 22.6 in. 6 in passes most good frames; a run of {@link #AGREE_FRAMES} in a row does not pass by
     * chance from a pose that is really off by more than that.
     */
    public static final double AGREE_IN = 6.0;

    /** Agreeing frames in a row; one frame that disagrees starts the count again. */
    public static final int AGREE_FRAMES = 10;

    /** The agreeing frames span at least this, so a burst of near-identical frames is not enough. */
    public static final long AGREE_SPAN_MS = 500;

    /** A reset made during the fault confirms if the Pinpoint reads again within this of it. */
    public static final long RESET_CLEARS_WITHIN_MS = 1000;

    /** How the pose was last confirmed, for the log. */
    public static final String BY_RESET = "a reset";
    public static final String BY_CAMERA = "the camera";

    private State state = State.TRUSTED;
    private long faultSinceMs = -1;
    private String faultStatus;
    /** When a reset was made during the current fault; -1 for none. */
    private long resetDuringFaultMs = -1;
    private int agreeing;
    private long agreeSinceMs;
    private int faults;
    private String confirmedBy;

    /**
     * One loop.
     *
     * @param started whether PLAY has been pressed
     * @param status  the Pinpoint's status as of its last read ({@code READY}, {@code FAULT_…}), or null
     * @param nowMs   a millisecond clock
     */
    public void observe(boolean started, String status, long nowMs) {
        boolean fault = started && status != null && status.startsWith("FAULT");
        if (!fault) {
            faultSinceMs = -1;
            if (state == State.FAULTED) {
                if (resetDuringFaultMs >= 0 && nowMs - resetDuringFaultMs <= RESET_CLEARS_WITHIN_MS) {
                    trust(BY_RESET);
                } else {
                    state = State.CONFIRMING;
                    agreeing = 0;
                }
            }
            return;
        }
        agreeing = 0; // a fault, even a blip, breaks a run of agreeing frames
        if (state == State.FAULTED) return;
        if (faultSinceMs < 0) faultSinceMs = nowMs;
        if (nowMs - faultSinceMs >= LOST_AFTER_MS) {
            state = State.FAULTED;
            faultStatus = status;
            resetDuringFaultMs = -1;
            confirmedBy = null;
            faults++;
        }
    }

    /** The pose was set on purpose (a corner reset, a camera reset, a heading fix from the camera). */
    public void poseSet(long nowMs) {
        if (state == State.CONFIRMING) {
            trust(BY_RESET);
        } else if (state == State.FAULTED) {
            resetDuringFaultMs = nowMs;
        }
    }

    /**
     * A gated camera frame: its position minus the pose's, inches (NaN counts as disagreeing).
     * Only counts while {@link State#CONFIRMING}.
     */
    public void cameraGap(double gapIn, long nowMs) {
        if (state != State.CONFIRMING) return;
        if (!(gapIn <= AGREE_IN)) {
            agreeing = 0;
            return;
        }
        if (agreeing == 0) agreeSinceMs = nowMs;
        agreeing++;
        if (agreeing >= AGREE_FRAMES && nowMs - agreeSinceMs >= AGREE_SPAN_MS) trust(BY_CAMERA);
    }

    private void trust(String by) {
        state = State.TRUSTED;
        confirmedBy = by;
        agreeing = 0;
        faultSinceMs = -1;
    }

    public State state() {
        return state;
    }

    /** Whether the pose is believed: not faulted, and confirmed since the last fault. */
    public boolean trusted() {
        return state == State.TRUSTED;
    }

    /** The status of the last fault that held ({@code FAULT_BAD_READ}), or null if none has. */
    public String faultStatus() {
        return faultStatus;
    }

    /** Faults that held {@link #LOST_AFTER_MS} since init. */
    public int faults() {
        return faults;
    }

    /** What confirmed the pose after the last fault ({@link #BY_RESET}, {@link #BY_CAMERA}), or null. */
    public String confirmedBy() {
        return confirmedBy;
    }

    /** Camera frames in a row agreeing so far, while confirming. */
    public int agreeingFrames() {
        return agreeing;
    }
}
