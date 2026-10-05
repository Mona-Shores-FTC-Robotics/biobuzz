package org.firstinspires.ftc.teamcode.localization;

import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.HiveCellState;
import org.firstinspires.ftc.teamcode.vision.HiveGeometry;
import org.firstinspires.ftc.teamcode.vision.Vec3;

/**
 * Where each CELL's tag row sits on the field, in each of its two resting states: eight points, in
 * Pedro's field frame (inches; see {@code util/FieldFrame}). The only field data localization needs.
 *
 * <p>The point is the centre of the four-tag row — the same point {@code CellSighting} measures
 * relative to the robot — so a sighting plus the robot's heading gives the robot's position directly,
 * with no tag orientations and no Limelight field map.
 *
 * <h2>Where these come from (19429, 3 Oct 2026, #156)</h2>
 *
 * <ul>
 *   <li><b>y (along the field, audience wall = 0)</b>: measured. From the red audience start
 *       position, robot centre 5 in from the audience wall and facing the HIVE, Sighting
 *       Diagnostics put RED_AUDIENCE's UP row 53.8 in ahead and BLUE_AUDIENCE's 52.9 in ahead
 *       (tape: about 60 in from the wall). Their mean, 58.35 in, is the AUDIENCE CELLs' UP y.</li>
 *   <li><b>z</b>: measured. UP 50.2 in (camera, both HIVEs), DOWN 35.0 in (tape: a lowered CELL's
 *       tags face away from the start position).</li>
 *   <li><b>The other three per CELL</b> follow from the rocker: the row is 15.2 in out from the
 *       axle and 1.56 in below it (what the two heights and {@link HiveGeometry}'s 43.95 in pivot
 *       and 30° tilt imply; the CAD's CELL floor is 1.34 in below the axle), turned ±30°. That
 *       puts the axle at y = 72.29 and each SCORING point at the AUDIENCE one's mirror about it.</li>
 *   <li><b>x</b>: the manual, not measured. HIVEs 25.5 in apart about the field centre (the two
 *       sightings put the rows 25.1 in apart). The red HIVE is on the low-x side (CAD).</li>
 * </ul>
 *
 * <p>Unverified on a second position. Check before trusting a fix: from another tape-measured
 * pose, {@code CellFix} on a settled CELL should land within an inch or two of it.
 */
public final class HiveFieldPoints {

    private HiveFieldPoints() {
    }

    // Row centre, Pedro frame, inches: {x, y, z}. Index: HiveCell ordinal, then UP = 0, DOWN = 1.
    private static final double[][][] ROW_CENTRE = {
            /* RED_SCORING   */ {{58.0, 86.2, 50.2}, {58.0, 84.7, 35.0}},
            /* RED_AUDIENCE  */ {{58.0, 58.4, 50.2}, {58.0, 59.9, 35.0}},
            /* BLUE_AUDIENCE */ {{83.5, 58.4, 50.2}, {83.5, 59.9, 35.0}},
            /* BLUE_SCORING  */ {{83.5, 86.2, 50.2}, {83.5, 84.7, 35.0}},
    };

    /**
     * Which way each CELL's tag row runs along field x: +1 if its cluster +x (tag 0 toward tag 3)
     * points along field +x, -1 if along -x, NaN until seen. Index: HiveCell ordinal.
     *
     * <p>RED_AUDIENCE measured (#156): from the red audience start facing +y, tag 37 (cluster +6.5)
     * was on the robot's right, which is field +x. The others: run Vision: Raw Tag Dump facing the
     * CELL and see whether its highest id sits to the robot's right (+1 facing +y) or left.
     */
    private static final double[] CLUSTER_X_ALONG_FIELD_X = {
            /* RED_SCORING   */ Double.NaN,
            /* RED_AUDIENCE  */ +1,
            /* BLUE_AUDIENCE */ Double.NaN,
            /* BLUE_SCORING  */ Double.NaN,
    };

    /** +1 or -1: which way {@code cell}'s row runs along field x; NaN until seen on the robot. */
    public static double clusterXAlongFieldX(HiveCell cell) {
        return CLUSTER_X_ALONG_FIELD_X[cell.ordinal()];
    }

    /** The row centre of {@code cell} in {@code state}, or null if unmeasured or the state is unknown. */
    public static Vec3 rowCentre(HiveCell cell, HiveCellState state) {
        return rowCentre(ROW_CENTRE, cell, state);
    }

    /** Whether any of the eight points has been filled in. */
    public static boolean anyMeasured() {
        for (HiveCell cell : HiveCell.values()) {
            if (rowCentre(cell, HiveCellState.UP) != null || rowCentre(cell, HiveCellState.DOWN) != null) {
                return true;
            }
        }
        return false;
    }

    static Vec3 rowCentre(double[][][] table, HiveCell cell, HiveCellState state) {
        int s;
        if (state == HiveCellState.UP) s = 0;
        else if (state == HiveCellState.DOWN) s = 1;
        else return null;
        double[] p = table[cell.ordinal()][s];
        if (Double.isNaN(p[0]) || Double.isNaN(p[1]) || Double.isNaN(p[2])) {
            return null;
        }
        return new Vec3(p[0], p[1], p[2]);
    }
}
