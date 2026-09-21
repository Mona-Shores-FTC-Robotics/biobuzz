package org.firstinspires.ftc.teamcode.vision;

import com.bylazar.configurables.annotations.Configurable;

/**
 * Decides whether a {@link HiveCell} is settled UP or DOWN, and refuses to answer
 * while it is tipping.
 *
 * <h2>Classifying on height, not orientation</h2>
 *
 * The obvious discriminator is the tag plane's orientation, which differs a lot
 * between the two states. The cheaper one is the <em>height</em> of the tag row
 * above the floor, which falls straight out of {@link CellSighting#rowCentreRobot()}
 * and depends only on the camera's axis convention — not on the Euler convention
 * the Limelight reports yaw/pitch/roll in, which is the one thing about the camera
 * that nothing in the SDK pins down. Fewer unverified assumptions in the path to a
 * decision that gates localization.
 *
 * <p>Orientation is still worth adding as a confirming second opinion once the
 * Euler convention is established on the robot. Height first, because it works now.
 *
 * <h2>Quick to distrust, slow to trust</h2>
 *
 * Establishing a state needs {@link #requiredSamples} consecutive agreeing
 * observations spanning at least {@link #requiredDwellMs}. Losing one takes a
 * single contrary observation. The asymmetry is deliberate: this gates whether a
 * sighting is allowed to move the robot's pose estimate, and briefly reporting
 * UNKNOWN during a genuine tip costs almost nothing, where confidently reporting a
 * stale UP for a cell that has already dropped injects a badly wrong pose.
 */
public class CellStateTracker {

    /**
     * Measured geometry of the two resting positions.
     *
     * <p>Both heights default to NaN, which classifies everything as
     * {@link HiveCellState#UNKNOWN}. That is intentional — an unmeasured field is
     * not a reason to guess. Read the real numbers off <b>Vision: Sighting
     * Diagnostics</b>, which displays the measured row height for exactly this
     * purpose.
     *
     * <p>The absolute heights are not in the Competition Manual, which locates the
     * AprilTag cluster only by reference-hole alignment (§9.9). The field CAD does
     * give them, transcribed in {@link HiveCellPoses}: sticker centres at
     * <b>49.67 in</b> raised and <b>35.65 in</b> lowered, a rise of
     * {@link HiveCellPoses#STICKER_STATE_RISE_IN} ≈ 14.02 in. The tag row can sit up
     * to ±0.31 in off those heights, because its position on the sticker isn't in
     * the CAD.
     *
     * <p><b>Correction, 2026-09-21:</b> this previously said the rise was about nine
     * inches ({@link HiveGeometry#STATE_HEIGHT_DELTA_IN}). That figure is for a point
     * half a CELL spacing from the pivot; the stickers sit further out.
     *
     * <p><b>Status, 2026-09-21: still unmeasured, on purpose.</b> Nobody has had a
     * robot in front of a real HIVE yet, and the CAD cannot stand in for it: these
     * are heights <em>as the camera measures them</em>, so they absorb the camera
     * axis convention and mount calibration along with the field itself. Copying
     * them from CAD would hide exactly the upstream errors that comparing a reading
     * against the CAD is meant to catch. Procedure: run <b>Vision: Sighting
     * Diagnostics</b>, settle a cell, read the labelled {@code ROW HEIGHT}, tip it,
     * read again. The readings should land near 49.7 / 35.7 in and about 14 in
     * apart. If they don't, chase the axis convention and mount geometry before
     * recording anything.
     */
    @Configurable
    public static class Geometry {
        /** Height of the tag row above the floor with the cell raised, inches. */
        public static double upRowHeightIn = Double.NaN;

        /** Height of the tag row above the floor with the cell lowered, inches. */
        public static double downRowHeightIn = Double.NaN;

        /**
         * How far from a nominal height still counts as that state, inches.
         *
         * <p>Two bands of this width have to fit inside the swing between states with
         * room to spare, or UP and DOWN stop being distinguishable —
         * {@code HiveGeometryTest} asserts that they do. Against the CAD's ~14 in
         * swing, 2.5" leaves a dead band of about 9" that reads as UNKNOWN, which is
         * where a cell mid-tip lands. (It was sized against a ~9.4 in swing that
         * turned out to be the wrong number; see above.)
         *
         * <p>Erring small is the safe direction: a tolerance that is too tight costs
         * availability, where one that is too loose misclassifies and feeds a wrong
         * pose into localization. Left at 2.5" on 2026-09-21: the wider real swing
         * would allow more, but frame-to-frame and tip-to-tip spread are unmeasured,
         * and those should set it rather than the swing.
         */
        public static double heightToleranceIn = 2.5;
    }

    private final int requiredSamples;
    private final long requiredDwellMs;

    private HiveCellState candidate = HiveCellState.UNKNOWN;
    private int candidateSamples = 0;
    private long candidateSinceNs = 0L;
    private HiveCellState settled = HiveCellState.UNKNOWN;

    public CellStateTracker() {
        this(3, 250L);
    }

    public CellStateTracker(int requiredSamples, long requiredDwellMs) {
        this.requiredSamples = Math.max(1, requiredSamples);
        this.requiredDwellMs = Math.max(0L, requiredDwellMs);
    }

    /**
     * Classifies a single height reading against the configured geometry.
     *
     * <p>Returns {@link HiveCellState#UNKNOWN} when the height matches neither
     * nominal — which is the clean signal that a cell is mid-tip, and is why this
     * does not need to detect motion directly. Also returns UNKNOWN when the two
     * nominals are too close to tell apart at the configured tolerance, rather than
     * silently picking whichever is nearer.
     *
     * @param rowHeightIn measured height of the tag row above the floor, inches
     */
    public static HiveCellState classifyByHeight(
            double rowHeightIn, double upHeightIn, double downHeightIn, double toleranceIn) {

        if (Double.isNaN(rowHeightIn) || Double.isNaN(upHeightIn) || Double.isNaN(downHeightIn)) {
            return HiveCellState.UNKNOWN;
        }
        if (toleranceIn <= 0.0 || Double.isNaN(toleranceIn)) {
            return HiveCellState.UNKNOWN;
        }
        // Overlapping acceptance bands cannot distinguish the states at all.
        if (Math.abs(upHeightIn - downHeightIn) < 2.0 * toleranceIn) {
            return HiveCellState.UNKNOWN;
        }

        boolean nearUp = Math.abs(rowHeightIn - upHeightIn) <= toleranceIn;
        boolean nearDown = Math.abs(rowHeightIn - downHeightIn) <= toleranceIn;

        if (nearUp && !nearDown) return HiveCellState.UP;
        if (nearDown && !nearUp) return HiveCellState.DOWN;
        return HiveCellState.UNKNOWN;
    }

    /** Classifies against the live {@link Geometry} configuration. */
    public static HiveCellState classifyByHeight(double rowHeightIn) {
        return classifyByHeight(rowHeightIn,
                Geometry.upRowHeightIn, Geometry.downRowHeightIn, Geometry.heightToleranceIn);
    }

    /**
     * Feeds one observation and returns the currently settled state.
     *
     * @param observed   this frame's classification
     * @param timestampNs when the underlying frame was captured
     */
    public HiveCellState update(HiveCellState observed, long timestampNs) {
        if (observed == null) observed = HiveCellState.UNKNOWN;

        if (observed != candidate) {
            candidate = observed;
            candidateSamples = 1;
            candidateSinceNs = timestampNs;
        } else {
            candidateSamples++;
        }

        if (observed == HiveCellState.UNKNOWN) {
            // Distrust immediately: a cell we cannot classify is a cell we must not
            // localize against, even if it was settled a moment ago.
            settled = HiveCellState.UNKNOWN;
            return settled;
        }

        boolean enoughSamples = candidateSamples >= requiredSamples;
        boolean enoughDwell = (timestampNs - candidateSinceNs) >= requiredDwellMs * 1_000_000L;
        if (enoughSamples && enoughDwell) settled = candidate;

        return settled;
    }

    /** The settled state, or UNKNOWN if the cell is not currently trustworthy. */
    public HiveCellState settledState() { return settled; }

    /** True once a state has been confirmed and not since contradicted. */
    public boolean isSettled() { return settled != HiveCellState.UNKNOWN; }

    /** What the most recent observation suggested, before dwell filtering. */
    public HiveCellState candidateState() { return candidate; }

    /** Consecutive observations backing the current candidate. */
    public int candidateSamples() { return candidateSamples; }

    public void reset() {
        candidate = HiveCellState.UNKNOWN;
        candidateSamples = 0;
        candidateSinceNs = 0L;
        settled = HiveCellState.UNKNOWN;
    }
}
