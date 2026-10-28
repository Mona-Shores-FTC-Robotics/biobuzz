package org.firstinspires.ftc.teamcode.localization;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.util.Alliance;

/**
 * Where the robot sits when the driver squares it into a field corner, for the driver's corner
 * snap: a pose reset that needs no camera. Pedro field frame, inches, radians.
 *
 * <p><b>Empty until measured.</b> Like {@link StartPositions}, each pose comes from putting the
 * robot there and reading it off the field, never from a drawing: the robot squared into the
 * corner (two sides against the two walls), its centre measured from each wall, and the way it
 * faces. Each alliance lists its corners in the order the snap should prefer them; with
 * {@code TeleOpConfig.cornerCount} at 1 only the first is used.
 */
public final class CornerPositions {

    private CornerPositions() {
    }

    private static final Pose[] BLUE = {};
    private static final Pose[] RED = {};

    /** {@code alliance}'s measured corners, best first. Empty for an unknown alliance or none measured. */
    public static Pose[] forAlliance(Alliance alliance) {
        if (alliance == Alliance.BLUE) return BLUE;
        if (alliance == Alliance.RED) return RED;
        return new Pose[0];
    }

    /**
     * The corner to snap to, or null. With one corner in use, that one. With two, the nearer to
     * the current pose, which needs the pose on the field ({@code referenced}); otherwise it is a
     * guess between two corners, so null.
     */
    public static Pose choose(Pose[] corners, int count, boolean referenced, double x, double y) {
        int usable = Math.min(corners.length, Math.max(1, Math.min(2, count)));
        if (usable == 0) return null;
        if (usable == 1) return corners[0];
        if (!referenced) return null;
        Pose best = null;
        double bestDist = Double.POSITIVE_INFINITY;
        for (int i = 0; i < usable; i++) {
            double d = Math.hypot(corners[i].x() - x, corners[i].y() - y);
            if (d < bestDist) {
                bestDist = d;
                best = corners[i];
            }
        }
        return best;
    }
}
