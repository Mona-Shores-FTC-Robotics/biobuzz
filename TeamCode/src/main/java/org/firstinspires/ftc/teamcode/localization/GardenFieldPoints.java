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
 * <p><b>Red's gather pose is from 20245's size (30 Oct 2026):</b> facing the audience wall, the
 * robot's centre is 8 in behind its front and the nose 1.5 in beyond that, so y 9.5 puts the nose
 * at the wall; x 8.5 leaves the side 0.4 in off the red-side wall (8.1 in from centre to side). The
 * line-up stays 9.6 in behind the gather pose. L-Quals and Sister-Right draw the GARDEN with the
 * team spots ({@code TeamCode/autos/spots.json}) GARDEN, this gather pose, and GARDEN_IN at
 * (11.5, 22.1), their own way in, not this line-up. Blue's is red's turned half a turn about the field centre, as the field is (manual
 * Figure 9-2).
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
            new Pose(8.5, 9.5, Math.toRadians(270)), new Pose(8.5, 19.1, Math.toRadians(270)));
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
