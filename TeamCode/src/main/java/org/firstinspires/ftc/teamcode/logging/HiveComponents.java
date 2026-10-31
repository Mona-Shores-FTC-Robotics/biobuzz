package org.firstinspires.ftc.teamcode.logging;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.vision.HiveGeometry;
import org.firstinspires.ftc.teamcode.vision.HiveState;
import org.firstinspires.ftc.teamcode.vision.HiveTracker;

/**
 * The two HIVE rockers as AdvantageScope component poses, for the {@code BIOBUZZ HIVE} asset
 * ({@code HiveAssets}: the frame as the base model, the red rocker as component 0, the blue one as
 * component 1) placed at the field origin by {@link #STRUCTURE_KEY}.
 *
 * <p>A match log draws what the robot <em>believes</em> ({@code HiveTracker}), so a match video
 * beside the 3D view shows where the robot was wrong; the simulator draws its own HIVE through the
 * same keys, so one layout plays both. Each rocker is turned about the axle by how far its angle is
 * from the as-built one, which is the match-start position {@link HiveState#RIGHT_CELL_UP}.
 *
 * <p>The axle runs along Pedro's x, which is AdvantageScope's Center/Rotated y, at
 * {@link HiveGeometry#PIVOT_AXIS_HEIGHT_IN} over the field centre: a rotation about y through
 * {@code (0, 0, pivot)}.
 */
public final class HiveComponents {

    /** The rockers' poses, a {@code Pose3d[]} of {@link #COUNT}. */
    public static final String KEY = "/Hive/Components";
    /** The HIVE "robot", at the field origin for the whole log. */
    public static final String STRUCTURE_KEY = "/Hive/Structure";

    public static final int RED = 0;
    public static final int BLUE = 1;
    public static final int COUNT = 2;

    /** A settled CELL's tilt, and so the rocker's end stops. */
    static final double TILT_RAD = Math.toRadians(HiveGeometry.CELL_TILT_DEG);
    private static final double PIVOT_Z_M = HiveGeometry.PIVOT_AXIS_HEIGHT_IN * AdvantageScopeFrame.METERS_PER_INCH;

    private HiveComponents() {
    }

    /** The component that draws {@code alliance}'s HIVE; -1 for UNKNOWN. */
    public static int of(Alliance alliance) {
        if (alliance == Alliance.RED) return RED;
        if (alliance == Alliance.BLUE) return BLUE;
        return -1;
    }

    /**
     * How far {@code alliance}'s rocker is turned from as built when it rests at {@code state}:
     * 0 at the match start, ±2 tilts at the other end stop. NaN for a state that is not a resting
     * position.
     */
    public static double fromBuiltRad(Alliance alliance, HiveState state) {
        if (!state.settled()) return Double.NaN;
        return angleRad(alliance, state) - angleRad(alliance, HiveState.RIGHT_CELL_UP);
    }

    /**
     * What the robot believes of {@code alliance}'s HIVE, as {@link #fromBuiltRad}: where it last
     * rested, held while out of view, and swung toward the other end stop over
     * {@code HiveTracker.Tuning.tipSeconds} while a TIP is under way.
     */
    public static double believedRad(Alliance alliance, HiveTracker hive, long nowMs) {
        HiveState at = hive.position();
        double from = fromBuiltRad(alliance, at);
        if (!hive.tipping()) return from;
        HiveState to = at == HiveState.RIGHT_CELL_UP ? HiveState.LEFT_CELL_UP : HiveState.RIGHT_CELL_UP;
        return from + (fromBuiltRad(alliance, to) - from) * hive.tipProgress(nowMs);
    }

    /** The rocker's angle at a resting position: + when the scoring side is up (the simulator's sign). */
    private static double angleRad(Alliance alliance, HiveState state) {
        boolean scoringUp = (alliance == Alliance.BLUE) == (state == HiveState.RIGHT_CELL_UP);
        return scoringUp ? TILT_RAD : -TILT_RAD;
    }

    /**
     * Component {@code c} turned {@code fromBuiltRad} from as built, as seven numbers
     * ({@code x, y, z, qw, qx, qy, qz}) at {@code out[7c]}. Allocates nothing.
     */
    public static void pose(int c, double fromBuiltRad, double[] out) {
        double d = fromBuiltRad;
        int k = 7 * c;
        out[k] = -PIVOT_Z_M * Math.sin(d);
        out[k + 1] = 0;
        out[k + 2] = PIVOT_Z_M * (1 - Math.cos(d));
        out[k + 3] = Math.cos(d / 2);
        out[k + 4] = 0;
        out[k + 5] = Math.sin(d / 2);
        out[k + 6] = 0;
    }
}
