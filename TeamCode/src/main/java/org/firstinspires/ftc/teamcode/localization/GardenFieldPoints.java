package org.firstinspires.ftc.teamcode.localization;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.util.FieldFrame;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Where a robot gathers from each GARDEN, in Pedro's field frame (inches, radians), and where it
 * lines up for it.
 *
 * <p><b>From the Autos' drawings, not a tape measure:</b> L-Quals and Sister-Right drive red's
 * GARDEN as GARDEN_IN (9.5, 20.56) to GARDEN (9.5, 10.96), facing the audience wall. Blue's is red's
 * turned half a turn about the field centre, as the field is (manual Figure 9-2). Measure both on
 * the field before trusting an inch of them.
 */
public final class GardenFieldPoints {

    /** One GARDEN: where the robot's centre is while it gathers, and where it lines up first. */
    public static final class Garden {
        public final String name;
        public final Pose gather;
        public final Pose lineUp;

        Garden(String name, Pose gather, Pose lineUp) {
            this.name = name;
            this.gather = gather;
            this.lineUp = lineUp;
        }

        @Override
        public String toString() {
            return String.format(Locale.US, "%s (%.1f, %.1f)", name, gather.x(), gather.y());
        }
    }

    private static final double C = FieldFrame.FIELD_CENTRE_INCHES;

    /** Red's GARDEN, by the audience wall on the red side. */
    public static final Garden RED = new Garden("red GARDEN",
            new Pose(9.5, 10.96, Math.toRadians(270)), new Pose(9.5, 20.56, Math.toRadians(270)));
    /** {@link #RED} turned 180° about the centre. */
    public static final Garden BLUE = new Garden("blue GARDEN", turned(RED.gather), turned(RED.lineUp));

    /** Both. */
    public static final List<Garden> ALL = Collections.unmodifiableList(Arrays.asList(RED, BLUE));

    private GardenFieldPoints() {
    }

    /** The GARDEN whose gather pose is nearest (x, y) and within {@code maxRangeIn}, or null. */
    public static Garden nearest(double x, double y, double maxRangeIn) {
        Garden best = null;
        double bestRange = maxRangeIn;
        for (Garden g : ALL) {
            double range = Math.hypot(g.gather.x() - x, g.gather.y() - y);
            if (range <= bestRange) {
                best = g;
                bestRange = range;
            }
        }
        return best;
    }

    private static Pose turned(Pose p) {
        return new Pose(2 * C - p.x(), 2 * C - p.y(), p.heading() + Math.PI);
    }
}
