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
 * line-up stays 9.6 in behind the gather pose. An Auto drives to the team spot
 * ({@code TeamCode/autos/spots.json}) GARDEN_STAGING, (11.5, 22.1), and its Garden gather card
 * drives in from there; the gather pose is this class's, not a spot. Blue's is red's turned half a turn about the field centre, as the field is (manual
 * Figure 9-2).
 *
 * <p><b>The corner ways in ({@link #CORNERS}).</b> The TeleOp's gather button may gather in any of the
 * four field corners, facing either of its two walls (mentor, 20 Nov 2026: driven by hand, a gather
 * facing whichever wall the robot already nearly faced worked). Each is red's gather pose and line-up
 * moved to that corner and that wall: reflected across the corner's diagonal for the other wall, and
 * mirrored or turned for the other corners. Red's own (facing the audience wall) and blue's are
 * {@link #RED} and {@link #BLUE}; the rest are that geometry, not measured.
 */
public final class GardenFieldPoints {

    /** One GARDEN: where the robot's centre is while it gathers, and where it lines up first. */
    public static final class Garden {
        public final String name;
        public final Pose gather;
        public final Pose lineUp;
        /**
         * Whether the wall the robot runs along is on its right as it faces the gather heading
         * ({@link #RED}'s and {@link #BLUE}'s are: the side wall); false for a way in facing the side
         * wall, along the audience wall, and its mirrors. A gather's "toward the wall" and "into the
         * corner" turn to that side.
         */
        public final boolean wallOnRight;

        Garden(String name, Pose gather, Pose lineUp, boolean wallOnRight) {
            this.name = name;
            this.gather = gather;
            this.lineUp = lineUp;
            this.wallOnRight = wallOnRight;
        }

        /** +1 with the wall on the robot's right, -1 on its left. */
        public double wallSide() {
            return wallOnRight ? 1.0 : -1.0;
        }

        @Override
        public String toString() {
            return String.format(Locale.US, "%s (%.1f, %.1f)", name, gather.x(), gather.y());
        }
    }

    private static final double C = FieldFrame.FIELD_CENTRE_INCHES;

    /** Red's GARDEN, by the audience wall on the red side. */
    public static final Garden RED = new Garden("red GARDEN",
            new Pose(8.5, 9.5, Math.toRadians(270)), new Pose(8.5, 19.1, Math.toRadians(270)), true);
    /** {@link #RED} turned 180° about the centre. */
    public static final Garden BLUE = new Garden("blue GARDEN", turned(RED.gather), turned(RED.lineUp), true);

    /** Both. */
    public static final List<Garden> ALL = Collections.unmodifiableList(Arrays.asList(RED, BLUE));

    /** Red's corner, facing the red-side wall (180°), along the audience wall: {@link #RED} reflected across the corner's diagonal. */
    private static final Garden RED_ALONG_AUDIENCE = new Garden("red GARDEN corner, facing the side wall",
            diagonal(RED.gather), diagonal(RED.lineUp), false);

    /**
     * Every corner way in, two per corner (facing one wall, then the other), in corner order: red's
     * GARDEN corner, blue's, the audience wall's blue-side corner, the far wall's red-side corner. A
     * pair is entries 2k and 2k + 1.
     */
    public static final List<Garden> CORNERS = Collections.unmodifiableList(Arrays.asList(
            RED,
            RED_ALONG_AUDIENCE,
            BLUE,
            new Garden("blue GARDEN corner, facing the side wall",
                    turned(RED_ALONG_AUDIENCE.gather), turned(RED_ALONG_AUDIENCE.lineUp), false),
            new Garden("audience blue-side corner, facing the audience wall",
                    mirrored(RED.gather), mirrored(RED.lineUp), false),
            new Garden("audience blue-side corner, facing the side wall",
                    mirrored(RED_ALONG_AUDIENCE.gather), mirrored(RED_ALONG_AUDIENCE.lineUp), true),
            new Garden("far red-side corner, facing the far wall",
                    turned(mirrored(RED.gather)), turned(mirrored(RED.lineUp)), false),
            new Garden("far red-side corner, facing the side wall",
                    turned(mirrored(RED_ALONG_AUDIENCE.gather)), turned(mirrored(RED_ALONG_AUDIENCE.lineUp)), true)));

    /**
     * The button's corner way in: of the corner whose ways in are nearest ({@code x}, {@code y}),
     * within {@code maxRangeIn}, the one whose heading is closest to {@code headingRad}, so the robot
     * turns at most 45° to line up. Null when no corner is in range.
     */
    public static Garden nearestCorner(double x, double y, double headingRad, double maxRangeIn) {
        int bestPair = -1;
        double bestRange = maxRangeIn;
        for (int i = 0; i < CORNERS.size(); i++) {
            Pose g = CORNERS.get(i).gather;
            double range = Math.hypot(g.x() - x, g.y() - y);
            if (range <= bestRange) {
                bestPair = i / 2;
                bestRange = range;
            }
        }
        if (bestPair < 0) return null;
        Garden a = CORNERS.get(2 * bestPair), b = CORNERS.get(2 * bestPair + 1);
        return turnTo(a, headingRad) <= turnTo(b, headingRad) ? a : b;
    }

    /**
     * {@code g}'s other way in: along the audience wall (blue's: the back wall) facing the side wall,
     * the wall on the robot's left, so the intake's left edge runs down the GARDEN's row of POLLEN.
     * {@link #RED} gives {@code (9.5, 8.5, 180°)}; {@link #BLUE} that turned half a turn. An Auto's
     * {@code GardenGatherAlongWall} card gathers this way. Null for anything but RED or BLUE.
     */
    public static Garden alongAudienceWall(Garden g) {
        if (g == RED) return CORNERS.get(1);
        if (g == BLUE) return CORNERS.get(3);
        return null;
    }

    /** How far, radians, a robot at {@code headingRad} turns to face {@code g}'s gather heading. */
    private static double turnTo(Garden g, double headingRad) {
        return Math.abs(Math.IEEEremainder(g.gather.heading() - headingRad, 2 * Math.PI));
    }

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

    /** Reflected across the line x = y, red's corner's diagonal: a heading of 270° becomes 180°. */
    private static Pose diagonal(Pose p) {
        return new Pose(p.y(), p.x(), Math.PI / 2 - p.heading());
    }

    /** Mirrored across the centre line x = the field's centre: red's corner to the audience wall's other corner. */
    private static Pose mirrored(Pose p) {
        return new Pose(2 * C - p.x(), p.y(), Math.PI - p.heading());
    }
}
