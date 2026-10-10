package org.firstinspires.ftc.teamcode.vision;

import org.firstinspires.ftc.teamcode.util.Alliance;

/**
 * The four BIOBUZZ HIVE CELLs, each carrying a 4-tag AprilTag cluster on its
 * underside.
 *
 * <p>The {@link #clusterName()} strings are the names the FTC SDK's own BIOBUZZ
 * tag library uses, verbatim. {@link BiobuzzTags} matches on them, and
 * {@code BiobuzzTagsTest} asserts they still match the SDK — so if FIRST renames
 * a cluster in a future SDK release, the unit test fails rather than the robot
 * silently failing to recognise a cell.
 */
public enum HiveCell {

    RED_SCORING("RED SCORING", Alliance.RED, Side.SCORING),
    RED_AUDIENCE("RED AUDIENCE", Alliance.RED, Side.AUDIENCE),
    BLUE_AUDIENCE("BLUE AUDIENCE", Alliance.BLUE, Side.AUDIENCE),
    BLUE_SCORING("BLUE SCORING", Alliance.BLUE, Side.SCORING);

    /**
     * Which end of the field a cell sits at. AUDIENCE: the audience side. SCORING: the opposite side,
     * where the scoring table is (the manual: "on the side of the FIELD opposite of the audience").
     * The names are FIRST's, from the SDK's cluster names.
     */
    public enum Side { SCORING, AUDIENCE }

    private final String clusterName;
    private final Alliance alliance;
    private final Side side;

    HiveCell(String clusterName, Alliance alliance, Side side) {
        this.clusterName = clusterName;
        this.alliance = alliance;
        this.side = side;
    }

    /** The cluster name as it appears in {@code AprilTagGameDatabase.getBioBuzzTagLibrary()}. */
    public String clusterName() { return clusterName; }

    public Alliance alliance() { return alliance; }

    public Side side() { return side; }

    /** True if this cell belongs to {@code alliance}. {@code UNKNOWN} matches nothing. */
    public boolean belongsTo(Alliance alliance) {
        return alliance != null && alliance != Alliance.UNKNOWN && this.alliance == alliance;
    }

    /**
     * The alliance's LEFT CELL, as its drivers see it from their alliance area: the one that starts
     * the match DOWN. The red one is at the rear (the SDK's "RED SCORING" cluster), the blue one at
     * the audience end. Null for UNKNOWN.
     */
    public static HiveCell leftCell(Alliance alliance) {
        if (alliance == Alliance.RED) return RED_SCORING;
        if (alliance == Alliance.BLUE) return BLUE_AUDIENCE;
        return null;
    }

    /** The alliance's RIGHT CELL: the one that starts the match UP. Null for UNKNOWN. */
    public static HiveCell rightCell(Alliance alliance) {
        if (alliance == Alliance.RED) return RED_AUDIENCE;
        if (alliance == Alliance.BLUE) return BLUE_SCORING;
        return null;
    }

    /** Looks up a cell by its SDK cluster name, or null if no cell matches. */
    public static HiveCell forClusterName(String name) {
        if (name == null) return null;
        for (HiveCell cell : values()) {
            if (cell.clusterName.equals(name)) return cell;
        }
        return null;
    }
}
