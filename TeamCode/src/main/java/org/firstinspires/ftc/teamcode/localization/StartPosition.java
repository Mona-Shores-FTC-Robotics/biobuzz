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
     * How a drive team puts the robot there, in one sentence, in the manual's tile and seam names
     * (§9.4); null for a start nobody has written one for, which then reads as {@link #where}.
     */
    public final String placement;

    public StartPosition(String name, Alliance alliance, Pose pose) {
        this(name, alliance, pose, null);
    }

    public StartPosition(String name, Alliance alliance, Pose pose, String placement) {
        this.name = name;
        this.alliance = alliance;
        this.pose = pose;
        this.placement = placement;
    }

    /** The placement sentence, or the tile and facing when there is none. */
    public String howToPlace() {
        return placement != null ? placement : where();
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
