package org.firstinspires.ftc.teamcode.localization;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.FieldFrame;

/**
 * A legal starting position: where an Autonomous expects the robot to be placed. An Autonomous
 * declares one by overriding {@code RobotOpMode.startPosition()}; the Pedro paths start from its
 * pose, and the start check compares the camera's view against it during INIT.
 *
 * <p>Declare them in {@link StartPositions}, measured from the field — never guessed.
 */
public final class StartPosition {

    public final String name;
    public final Alliance alliance;
    /** Pedro field frame: inches, radians. */
    public final Pose pose;

    /**
     * How far the robot's left side may be from its centre for {@link #placement} to name the seam
     * it is on, inches: about half the robot's width.
     */
    static final double LEFT_WHEELS_FROM_CENTRE_MAX_IN = 10.0;

    public StartPosition(String name, Alliance alliance, Pose pose) {
        this.name = name;
        this.alliance = alliance;
        this.pose = pose;
    }

    /**
     * How to put the robot there, as short as it can be: "Left side on B-C Seam", the seam on
     * the robot's left as it faces at the start (columns for a robot facing along the field, rows
     * for one facing across it). Worked out from the pose, so a start turned half a turn for the
     * other alliance gets its own seam. Falls back to {@link #where} when no seam is close enough
     * to the robot's left to be where its side is.
     */
    public String placement() {
        String seam = leftSeam();
        return seam == null ? where() : "Left side on " + seam + " Seam";
    }

    /** The seam just to the robot's left, "B-C" or "2-3", or null if none is within reach. */
    String leftSeam() {
        double tile = FieldFrame.TILE_INCHES;
        String facing = FieldFrame.facing(pose.heading());
        double along;     // the coordinate across the robot (x or y)
        int k;            // the seam's index: it lies at k tiles
        boolean columns;  // a seam between columns (x) or between rows (y)
        if (facing.equals("away from the audience")) {        // left is -x
            along = pose.x(); k = (int) Math.floor(along / tile); columns = true;
        } else if (facing.equals("toward the audience")) {    // left is +x
            along = pose.x(); k = (int) Math.ceil(along / tile); columns = true;
        } else if (facing.equals("toward the blue wall")) {   // left is +y
            along = pose.y(); k = (int) Math.ceil(along / tile); columns = false;
        } else {                                               // toward the red wall: left is -y
            along = pose.y(); k = (int) Math.floor(along / tile); columns = false;
        }
        if (k < 1 || k > 5 || Math.abs(along - k * tile) > LEFT_WHEELS_FROM_CENTRE_MAX_IN) return null;
        return columns ? (char) ('A' + k - 1) + "-" + (char) ('A' + k) : k + "-" + (k + 1);
    }

    /** The field with this start's robot on it, for the Driver Station: see {@link FieldMap}. */
    public String[] map() {
        return FieldMap.lines(this);
    }

    /** The way the robot faces on {@link #map}: ^ away from the audience, &gt; toward blue. */
    public String arrow() {
        String facing = FieldFrame.facing(pose.heading());
        if (facing.startsWith("away")) return "^";
        if (facing.endsWith("blue wall")) return "&gt;";
        if (facing.endsWith("red wall")) return "&lt;";
        return "v";
    }

    /** Where it is in a drive team's terms: "tile C1, facing away from the audience". */
    public String where() {
        String tile = FieldFrame.tileName(pose.x(), pose.y());
        return (tile == null ? "off the field" : "tile " + tile) + ", facing "
                + FieldFrame.facing(pose.heading());
    }

    @Override
    public String toString() {
        return name;
    }
}
