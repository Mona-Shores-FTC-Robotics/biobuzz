package org.firstinspires.ftc.teamcode.localization;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.util.FieldFrame;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Where the four FLOWERs are, in Pedro's field frame (inches), and the poses a robot uses to drive
 * into one.
 *
 * <p>From the official field model, not a tape measure: the four Flower Assemblies of
 * AdvantageScope's 2026-27 FTC field (FIRST's field CAD), read by the simulator's
 * {@code HiveAssets} into {@code biobuzz-staged-pieces.csv} on {@code claude/simulator}. Each
 * FLOWER's staged POLLEN stack sits {@value #CENTRE_FROM_WALL_IN} in off a wall face and
 * {@value #ALONG_WALL_IN} in along it from a corner; the other two FLOWERs are those two turned
 * 180° about the field centre. The FLOWER stick's own study ({@code tools/robot-cad/flower.py},
 * spike/179) has the same 2.71 in from the centre to the wall. The committed Autos agree: R-Quals
 * drives to (47.36, 126.64) facing 90° for the far FLOWER, 12.15 in out from this one's centre.
 *
 * <p>A robot picks a FLOWER facing its wall: {@link Flower#facingRad} is the heading that points the
 * robot's front at the wall, and {@link #standoff} the pose a given distance back from the FLOWER's
 * centre along it. The names say which half of the field a FLOWER is in (red on the low-x side, as
 * {@link HiveFieldPoints} has the HIVEs) and use the Autos' own words for the two on each half; they
 * say nothing about who may use it.
 */
public final class FlowerFieldPoints {

    /** A FLOWER's centre from its wall's face, inches (field model). */
    public static final double CENTRE_FROM_WALL_IN = 2.71;

    /** A FLOWER's centre along its wall, from the field corner it is nearer, inches (field model). */
    public static final double ALONG_WALL_IN = 47.36;

    /** One FLOWER: its centre, and the field heading that faces a robot at its wall. */
    public static final class Flower {
        public final String name;
        public final double x;
        public final double y;
        public final double facingRad;

        Flower(String name, double x, double y, double facingRad) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.facingRad = facingRad;
        }

        @Override
        public String toString() {
            return String.format(java.util.Locale.US, "%s (%.1f, %.1f)", name, x, y);
        }
    }

    private static final double S = FieldFrame.FIELD_SIZE_INCHES;

    /** The red half's FLOWER on the low-x wall: the Autos' WALL_FLOWER. A robot faces -x. */
    public static final Flower RED_WALL = new Flower("red WALL FLOWER", CENTRE_FROM_WALL_IN, ALONG_WALL_IN, Math.PI);
    /** The red half's FLOWER on the high-y wall: the Autos' FAR_FLOWER. A robot faces +y. */
    public static final Flower RED_FAR = new Flower("red FAR FLOWER", ALONG_WALL_IN, S - CENTRE_FROM_WALL_IN, Math.PI / 2);
    /** {@link #RED_WALL} turned 180° about the centre. A robot faces +x. */
    public static final Flower BLUE_WALL = new Flower("blue WALL FLOWER", S - CENTRE_FROM_WALL_IN, S - ALONG_WALL_IN, 0.0);
    /** {@link #RED_FAR} turned 180° about the centre. A robot faces -y. */
    public static final Flower BLUE_FAR = new Flower("blue FAR FLOWER", S - ALONG_WALL_IN, CENTRE_FROM_WALL_IN, -Math.PI / 2);

    /** All four. */
    public static final List<Flower> ALL = Collections.unmodifiableList(
            Arrays.asList(RED_WALL, RED_FAR, BLUE_WALL, BLUE_FAR));

    private FlowerFieldPoints() {
    }

    /** The FLOWER whose centre is nearest (x, y) and within {@code maxRangeIn}, or null. */
    public static Flower nearest(double x, double y, double maxRangeIn) {
        Flower best = null;
        double bestRange = maxRangeIn;
        for (Flower f : ALL) {
            double range = Math.hypot(f.x - x, f.y - y);
            if (range <= bestRange) {
                best = f;
                bestRange = range;
            }
        }
        return best;
    }

    /**
     * Where the robot's tracked point is when it is {@code distanceIn} back from {@code flower}'s
     * centre, square to its wall and facing it.
     */
    public static Pose standoff(Flower flower, double distanceIn) {
        return new Pose(flower.x - distanceIn * Math.cos(flower.facingRad),
                flower.y - distanceIn * Math.sin(flower.facingRad),
                flower.facingRad);
    }
}
