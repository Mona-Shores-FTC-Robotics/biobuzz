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
     * <p>The absolute heights are not published. The Competition Manual gives the
     * pivot height, the tilt and the CELL spacing, but locates the AprilTag cluster
     * only by reference-hole alignment — §9.9 says those holes "can be used to
     * measure the location of the AprilTag Cluster relative to the rest of the
     * FIELD", i.e. FIRST expects teams to measure it. See {@link HiveGeometry}.
     *
     * <p>What <em>is</em> known from the manual is the <b>difference</b> between the
     * two: {@link HiveGeometry#STATE_HEIGHT_DELTA_IN}, about nine inches. That is
     * what makes this classifier viable before anything has been measured, and it
     * is what sets the default tolerance below.
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
         * <p>Two bands of this width have to fit inside the roughly nine-inch swing
         * between states with room to spare, or UP and DOWN stop being
         * distinguishable — {@code HiveGeometryTest} asserts that they do. 2.5"
         * leaves a dead band of about 4.4" that reads as UNKNOWN, which is where a
         * cell mid-tip lands.
         *
         * <p>Erring small is the safe direction: a tolerance that is too tight costs
         * availability, where one that is too loose misclassifies and feeds a wrong
         * pose into localization. Revisit once the real heights are measured and the
         * true separation is known.
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

        // Distrust immediately: any observation that disagrees with the settled state
        // drops it. That covers UNKNOWN — a cell we cannot classify is a cell we must
        // not localize against, even if it was settled a moment ago — and equally the
        // opposite state, which is what a tip looks like when its mid-transition
        // frames were missed. Re-establishing a state always goes back through the
        // sample and dwell requirements below, so this is the "quick to distrust"
        // half of the asymmetry and nothing short-circuits the "slow to trust" half.
        if (observed != settled) {
            settled = HiveCellState.UNKNOWN;
        }

        if (observed == HiveCellState.UNKNOWN) {
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
