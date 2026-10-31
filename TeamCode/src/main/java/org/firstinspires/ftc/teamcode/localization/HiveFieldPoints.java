package org.firstinspires.ftc.teamcode.localization;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.FieldFrame;
import org.firstinspires.ftc.teamcode.vision.BiobuzzTags;
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
 * <h2>Where these come from</h2>
 *
 * <p>All eight from one rocker model whose numbers are the official field CAD, as two other teams
 * read it (FTC 12303's {@code FieldConstants}, CAD v26-27.2; FTC 16751's {@code FieldAim}), checked
 * against what 19429 measured on 3 Oct 2026 (#156):
 *
 * <ul>
 *   <li><b>The axle</b> runs along field x on the field's centre line ({@link #AXLE_Y_IN}), at
 *       {@link HiveGeometry#PIVOT_AXIS_HEIGHT_IN}. The two HIVEs sit
 *       {@link HiveGeometry#HIVE_CENTER_TO_CENTER_IN} apart about the field centre, red on the
 *       low-x side.</li>
 *   <li><b>The row</b> is a point rigidly on the rocker, {@link #ROW_OUT_IN} out from the axle along
 *       the CELL and {@link #ROW_DROP_IN} below the axle line, turned {@link HiveGeometry#CELL_TILT_DEG}
 *       either way. Raised, that puts the row 13.10 in from the axle and 49.76 in up; lowered, 11.58 in
 *       and 35.51 in. Those are 12303's four CAD numbers to within 0.05 in, which is how the two
 *       rocker numbers were found.</li>
 *   <li><b>The check.</b> From the red audience start (robot centre about 5 in from the audience
 *       wall, facing the HIVE), Sighting Diagnostics put the raised AUDIENCE rows 58.4 in from the
 *       wall and 50.2 in up; the CAD model says 57.65 and 49.76. Camera and CAD agree to under an inch
 *       at the one spot we have measured. The derivation that preceded this one, from that measurement
 *       alone, put the axle at 72.29 and the row 15.2 in out, which moved the SCORING rows 2.3 in
 *       toward the scoring wall; the 6 in disagreement seen from behind the HIVE on 24 Oct was on
 *       those points.</li>
 * </ul>
 *
 * <p>Every point may fix the pose ({@link #tagPositionForFix}) and be aimed at ({@link #rowCentre}).
 * {@link #checkedOnField} records which rows a field measurement has confirmed; the rest rest on
 * the CAD. A tape-measured pose that a fix disputes by more than an inch or two is a reason to
 * measure that row, not to move the model.
 */
public final class HiveFieldPoints {

    private HiveFieldPoints() {
    }

    /**
     * Field y of the HIVEs' axle, the line the CELLs rock about: the field's centre line (CAD). The
     * AUDIENCE CELLs are on the low-y side of it, the SCORING CELLs on the high-y side.
     */
    public static final double AXLE_Y_IN = FieldFrame.FIELD_CENTRE_INCHES;

    /** How far the row centre sits out from the axle along the CELL, in the rocker's own frame, inches (CAD). */
    public static final double ROW_OUT_IN = 14.25;

    /** How far the row centre sits below the axle line, in the rocker's own frame, inches (CAD). */
    public static final double ROW_DROP_IN = 1.52;

    private static final double TILT_RAD = Math.toRadians(HiveGeometry.CELL_TILT_DEG);
    private static final double COS_TILT = Math.cos(TILT_RAD);
    private static final double SIN_TILT = Math.sin(TILT_RAD);

    /** Horizontal distance from the axle to a raised CELL's row centre, inches: 13.10. */
    public static final double UP_ROW_FROM_AXLE_IN = ROW_OUT_IN * COS_TILT + ROW_DROP_IN * SIN_TILT;

    /** Horizontal distance from the axle to a lowered CELL's row centre, inches: 11.58. */
    public static final double DOWN_ROW_FROM_AXLE_IN = ROW_OUT_IN * COS_TILT - ROW_DROP_IN * SIN_TILT;

    /** Height of a raised CELL's row centre above the tiles, inches: 49.76. */
    public static final double UP_ROW_HEIGHT_IN =
            HiveGeometry.PIVOT_AXIS_HEIGHT_IN + ROW_OUT_IN * SIN_TILT - ROW_DROP_IN * COS_TILT;

    /** Height of a lowered CELL's row centre above the tiles, inches: 35.51. */
    public static final double DOWN_ROW_HEIGHT_IN =
            HiveGeometry.PIVOT_AXIS_HEIGHT_IN - ROW_OUT_IN * SIN_TILT - ROW_DROP_IN * COS_TILT;

    /**
     * Which rows a field measurement has confirmed, as against resting on the CAD alone: index as
     * {@link #ROW_CENTRE}. The two raised AUDIENCE rows, by camera from the red audience start on
     * 3 Oct 2026 (#156), to under an inch. Information, not a gate: every point may fix the pose.
     */
    private static final boolean[][] CHECKED_ON_FIELD = {
            /* RED_SCORING   */ {false, false},
            /* RED_AUDIENCE  */ {true, false},
            /* BLUE_AUDIENCE */ {true, false},
            /* BLUE_SCORING  */ {false, false},
    };

    // Row centre, Pedro frame, inches: {x, y, z}. Index: HiveCell ordinal, then UP = 0, DOWN = 1.
    private static final double[][][] ROW_CENTRE = rowCentresFromCad();

    private static double[][][] rowCentresFromCad() {
        double[][][] table = new double[HiveCell.values().length][2][];
        for (HiveCell cell : HiveCell.values()) {
            double x = hiveX(cell.alliance());
            // An AUDIENCE CELL's row lies on the low-y side of the axle, a SCORING CELL's on the high-y side.
            double side = cell.side() == HiveCell.Side.AUDIENCE ? -1.0 : 1.0;
            table[cell.ordinal()][0] = new double[] {x, AXLE_Y_IN + side * UP_ROW_FROM_AXLE_IN, UP_ROW_HEIGHT_IN};
            table[cell.ordinal()][1] = new double[] {x, AXLE_Y_IN + side * DOWN_ROW_FROM_AXLE_IN, DOWN_ROW_HEIGHT_IN};
        }
        return table;
    }

    /** Field x of an alliance's HIVE: the two sit about the field centre, red on the low-x side (CAD). */
    public static double hiveX(Alliance alliance) {
        double half = HiveGeometry.HIVE_CENTER_TO_CENTER_IN / 2.0;
        return alliance == Alliance.RED ? FieldFrame.FIELD_CENTRE_INCHES - half
                : FieldFrame.FIELD_CENTRE_INCHES + half;
    }

    /**
     * Which way each CELL's tag row runs along field x: +1 if its cluster +x (tag 0 toward tag 3)
     * points along field +x, -1 if along -x. Index: HiveCell ordinal.
     *
     * <p><b>Derived from the SDK's cluster layout, confirmed on RED_AUDIENCE.</b> The SDK puts each
     * CELL's opening at +7.19 in cluster y and -5.62 in cluster z from its tag row. The opening faces
     * away from the axle and the tags face down out of the CELL's underside, so for a right-handed
     * cluster frame x = y × z: an AUDIENCE CELL (opening toward low field y) gives
     * (0,-1,0) × (0,0,-1) = +x, a SCORING CELL (toward high y) gives -x. On 19429 (#156), from the
     * red audience start facing +y, RED_AUDIENCE's tag 37 (cluster +6.5) sat on the robot's right,
     * which is field +x, as predicted. To check another CELL: face it in Vision: HIVE & Pose (the Tags
     * section lists ids); its highest id should sit on the robot's right when facing +y for an AUDIENCE CELL, left for SCORING.
     */
    private static final double[] CLUSTER_X_ALONG_FIELD_X = {
            /* RED_SCORING   */ -1,
            /* RED_AUDIENCE  */ +1,
            /* BLUE_AUDIENCE */ +1,
            /* BLUE_SCORING  */ -1,
    };

    /** +1 or -1: which way {@code cell}'s row runs along field x. */
    public static double clusterXAlongFieldX(HiveCell cell) {
        return CLUSTER_X_ALONG_FIELD_X[cell.ordinal()];
    }

    /**
     * Where one HIVE tag is on the field, with its CELL in {@code state}: the row centre plus the
     * tag's own offset along the row (the row runs along the axle, so a TIP leaves the offset along
     * field x). Null for a non-HIVE tag or an unknown state.
     */
    public static Vec3 tagPosition(int tagId, HiveCellState state) {
        HiveCell cell = BiobuzzTags.cellForTag(tagId);
        Vec3 row = cell == null ? null : rowCentre(cell, state);
        if (row == null) return null;
        double along = clusterXAlongFieldX(cell) * BiobuzzTags.memberOffset(tagId).x();
        return new Vec3(row.x() + along, row.y(), row.z());
    }

    /** The row centre of {@code cell} in {@code state}, or null if the state is not a settled one. */
    public static Vec3 rowCentre(HiveCell cell, HiveCellState state) {
        return rowCentre(ROW_CENTRE, cell, state);
    }

    /** Whether a field measurement has confirmed {@code cell}'s row in {@code state} (class comment). */
    public static boolean checkedOnField(HiveCell cell, HiveCellState state) {
        if (state == HiveCellState.UP) return CHECKED_ON_FIELD[cell.ordinal()][0];
        if (state == HiveCellState.DOWN) return CHECKED_ON_FIELD[cell.ordinal()][1];
        return false;
    }

    /**
     * {@link #tagPosition} for a camera fix. The same point: every settled row may move the pose,
     * since each comes from the same CAD model and the one we could check agreed with it. Kept as
     * its own name so the fix path reads as a decision, and so a point can be withdrawn here if a
     * field ever disputes it.
     */
    public static Vec3 tagPositionForFix(int tagId, HiveCellState state) {
        return tagPosition(tagId, state);
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
