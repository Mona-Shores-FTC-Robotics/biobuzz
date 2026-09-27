package org.firstinspires.ftc.teamcode.localization;

import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.HiveCellState;
import org.firstinspires.ftc.teamcode.vision.Vec3;

/**
 * Where each CELL's tag row sits on the field, in each of its two resting states: eight points, in
 * Pedro's field frame (inches; see {@code util/FieldFrame}). The only field data localization needs.
 *
 * <p>The point is the centre of the four-tag row — the same point {@code CellSighting} measures
 * relative to the robot — so a sighting plus the robot's heading gives the robot's position directly,
 * with no tag orientations and no Limelight field map.
 *
 * <h2>Not measured yet</h2>
 *
 * <p>Every value is NaN until it comes from the field CAD, checked on a real field. SDK 12.0
 * publishes no BIOBUZZ tag positions, and Limelight publishes no BIOBUZZ map, so these are ours to
 * measure and nothing here guesses. While a point is NaN, {@link #rowCentre} returns null and that
 * CELL contributes no position fixes — the robot runs on the Pinpoint alone and says so.
 *
 * <p>Record the source (CAD model and version) beside the numbers when they go in. If the CAD has
 * only the reset state, the other state is the reset point rotated about the rocker's pivot axis by
 * the rocker's travel; {@code HiveGeometry} has the pivot height and the cell offsets to check it
 * against.
 */
public final class HiveFieldPoints {

    private HiveFieldPoints() {
    }

    private static final double NOT_MEASURED = Double.NaN;

    // Row centre, Pedro frame, inches: {x, y, z}. Index: HiveCell ordinal, then UP = 0, DOWN = 1.
    private static final double[][][] ROW_CENTRE = {
            /* RED_SCORING   */ {{NOT_MEASURED, NOT_MEASURED, NOT_MEASURED}, {NOT_MEASURED, NOT_MEASURED, NOT_MEASURED}},
            /* RED_AUDIENCE  */ {{NOT_MEASURED, NOT_MEASURED, NOT_MEASURED}, {NOT_MEASURED, NOT_MEASURED, NOT_MEASURED}},
            /* BLUE_AUDIENCE */ {{NOT_MEASURED, NOT_MEASURED, NOT_MEASURED}, {NOT_MEASURED, NOT_MEASURED, NOT_MEASURED}},
            /* BLUE_SCORING  */ {{NOT_MEASURED, NOT_MEASURED, NOT_MEASURED}, {NOT_MEASURED, NOT_MEASURED, NOT_MEASURED}},
    };

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
