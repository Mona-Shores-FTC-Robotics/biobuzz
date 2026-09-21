package org.firstinspires.ftc.teamcode.vision;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

/**
 * Guards the transcribed CAD poses in {@link HiveCellPoses}.
 *
 * <p>There is no SDK copy to cross-check against, as {@code BiobuzzTagsTest} does.
 * So each check here holds the table to something independent of it: the manual's
 * published geometry in {@link HiveGeometry}, rigid-body motion about the pivot, or
 * the field's two-fold symmetry. A mistyped digit in any one of the eight entries
 * breaks at least one of them.
 */
public class HiveCellPosesTest {

    private static final double POS_EPS = 0.002; // CAD values are transcribed to 0.001 in
    private static final double DIR_EPS = 1e-6;

    private static HiveCellPoses.Pose pose(HiveCell cell, HiveCellState state) {
        return HiveCellPoses.of(cell, state);
    }

    private static void assertVec(String what, Vec3 expected, Vec3 actual, double eps) {
        assertEquals(what + " x", expected.x(), actual.x(), eps);
        assertEquals(what + " y", expected.y(), actual.y(), eps);
        assertEquals(what + " z", expected.z(), actual.z(), eps);
    }

    private static double dot(Vec3 a, Vec3 b) {
        return a.x() * b.x() + a.y() * b.y() + a.z() * b.z();
    }

    /** Rotates about the pivot axis (parallel to X through Y = 0, Z = pivot). */
    private static Vec3 aboutPivot(Vec3 p, double deg) {
        double t = Math.toRadians(deg), c = Math.cos(t), s = Math.sin(t);
        double y = p.y(), z = p.z() - HiveCellPoses.PIVOT_Z_IN;
        return new Vec3(p.x(), c * y - s * z, s * y + c * z + HiveCellPoses.PIVOT_Z_IN);
    }

    private static Vec3 turn(Vec3 d, double deg) {
        double t = Math.toRadians(deg), c = Math.cos(t), s = Math.sin(t);
        return new Vec3(d.x(), c * d.y() - s * d.z(), s * d.y() + c * d.z());
    }

    @Test
    public void everyCellHasBothPosesAndUnknownHasNone() {
        for (HiveCell cell : HiveCell.values()) {
            assertTrue(pose(cell, HiveCellState.UP) != null);
            assertTrue(pose(cell, HiveCellState.DOWN) != null);
            assertThrows(IllegalArgumentException.class,
                    () -> HiveCellPoses.of(cell, HiveCellState.UNKNOWN));
        }
    }

    @Test
    public void pivotMatchesTheManual() {
        assertEquals(HiveGeometry.PIVOT_AXIS_HEIGHT_IN, HiveCellPoses.PIVOT_Z_IN, 1e-9);
    }

    /** Figure 9-10: HIVE centres 25.5 in apart, structure centred on the field. */
    @Test
    public void stickersSitOnTheirHiveCentreline() {
        double half = HiveGeometry.HIVE_CENTER_TO_CENTER_IN / 2.0;
        for (HiveCell cell : HiveCell.values()) {
            double expectedX = cell.alliance() == Alliance.BLUE
                    ? half : -half;
            for (HiveCellState state : new HiveCellState[] {HiveCellState.UP, HiveCellState.DOWN}) {
                assertEquals(cell + " " + state + " lateral position",
                        expectedX, pose(cell, state).stickerCentre().x(), 0.01);
            }
        }
    }

    /** The frame's +Y is defined as toward the SCORING cells. */
    @Test
    public void scoringCellsAreAtPositiveY() {
        for (HiveCell cell : HiveCell.values()) {
            double y = pose(cell, HiveCellState.UP).stickerCentre().y();
            assertEquals(cell.toString(), cell.side() == HiveCell.Side.SCORING, y > 0);
        }
    }

    @Test
    public void directionsAreOrthonormal() {
        for (HiveCell cell : HiveCell.values()) {
            for (HiveCellState state : new HiveCellState[] {HiveCellState.UP, HiveCellState.DOWN}) {
                HiveCellPoses.Pose p = pose(cell, state);
                assertEquals(1.0, p.outwardNormal().norm(), DIR_EPS);
                assertEquals(1.0, p.openingFacing().norm(), DIR_EPS);
                assertEquals(0.0, dot(p.outwardNormal(), p.openingFacing()), DIR_EPS);
                assertEquals(1.0, p.rowAxis().norm(), DIR_EPS);
                // The tag row runs along the pivot axis, i.e. along X.
                assertEquals(1.0, Math.abs(p.rowAxis().x()), DIR_EPS);
            }
        }
    }

    /**
     * Figure 9-10's 30° tilt, and the answer to "can the camera see a lowered cell":
     * in both states the tags face downward, 30° off vertical.
     */
    @Test
    public void tagsFaceDownThirtyDegreesOffVerticalInBothStates() {
        double cosTilt = Math.cos(Math.toRadians(HiveGeometry.CELL_TILT_DEG));
        for (HiveCell cell : HiveCell.values()) {
            for (HiveCellState state : new HiveCellState[] {HiveCellState.UP, HiveCellState.DOWN}) {
                assertEquals(cell + " " + state, -cosTilt,
                        pose(cell, state).outwardNormal().z(), DIR_EPS);
            }
        }
    }

    /** Raised openings tilt up, lowered ones down, and both face away from the pivot. */
    @Test
    public void openingsFaceOutwardAndTiltWithTheState() {
        for (HiveCell cell : HiveCell.values()) {
            HiveCellPoses.Pose up = pose(cell, HiveCellState.UP);
            HiveCellPoses.Pose down = pose(cell, HiveCellState.DOWN);
            assertTrue(cell + " UP opening tilts up", up.openingFacing().z() > 0);
            assertTrue(cell + " DOWN opening tilts down", down.openingFacing().z() < 0);
            double side = Math.signum(up.stickerCentre().y());
            assertTrue(cell + " UP faces outward", up.openingFacing().y() * side > 0);
            assertTrue(cell + " DOWN faces outward", down.openingFacing().y() * side > 0);
        }
    }

    /**
     * The rocker is rigid, so each cell's two poses must be one 60° rotation about the
     * pivot apart — the swing between the two 30° stops. This is what backs the four
     * poses that were derived rather than modelled.
     */
    @Test
    public void eachCellsTwoPosesAreOneRigidSixtyDegreeSwingApart() {
        for (HiveCell cell : HiveCell.values()) {
            HiveCellPoses.Pose up = pose(cell, HiveCellState.UP);
            HiveCellPoses.Pose down = pose(cell, HiveCellState.DOWN);
            // Raising a +Y cell turns the rocker by +60° about +X; a -Y cell by -60°.
            double deg = 2 * HiveGeometry.CELL_TILT_DEG * Math.signum(up.stickerCentre().y());
            assertVec(cell + " centre", up.stickerCentre(), aboutPivot(down.stickerCentre(), deg), POS_EPS);
            assertVec(cell + " normal", up.outwardNormal(), turn(down.outwardNormal(), deg), 1e-6);
            assertVec(cell + " opening", up.openingFacing(), turn(down.openingFacing(), deg), 1e-6);
        }
    }

    /**
     * The two HIVEs are the same assembly turned 180° about the vertical through the
     * field centre, so red and blue cells on opposite sides mirror each other exactly.
     * These pairs are both modelled or both derived; see the next test for the check
     * that ties the derived poses to the CAD.
     */
    @Test
    public void hivesAreTwoFoldSymmetricAboutTheFieldCentre() {
        HiveCell[][] pairs = {
                {HiveCell.RED_SCORING, HiveCell.BLUE_AUDIENCE},
                {HiveCell.RED_AUDIENCE, HiveCell.BLUE_SCORING}};
        for (HiveCell[] pair : pairs) {
            for (HiveCellState state : new HiveCellState[] {HiveCellState.UP, HiveCellState.DOWN}) {
                HiveCellPoses.Pose a = pose(pair[0], state), b = pose(pair[1], state);
                Vec3 ca = a.stickerCentre(), cb = b.stickerCentre();
                // The CAD's HIVE pair sits 0.008 in off-centre in X; allow for it.
                assertEquals(pair[0] + "/" + pair[1] + " " + state + " x", -ca.x(), cb.x(), 0.02);
                assertEquals(-ca.y(), cb.y(), POS_EPS);
                assertEquals(ca.z(), cb.z(), POS_EPS);
                assertEquals(-a.outwardNormal().y(), b.outwardNormal().y(), DIR_EPS);
                assertEquals(a.outwardNormal().z(), b.outwardNormal().z(), DIR_EPS);
                assertEquals(-a.openingFacing().y(), b.openingFacing().y(), DIR_EPS);
                assertEquals(a.openingFacing().z(), b.openingFacing().z(), DIR_EPS);
            }
        }
    }

    /**
     * The same CELL on the two HIVEs differs only in X. In the CAD snapshot the two
     * HIVEs are in opposite states, so every pair here is one modelled pose against
     * one derived pose. This is what ties the four rotated poses to independently
     * modelled geometry rather than only to the rotation that produced them.
     */
    @Test
    public void derivedPosesMatchTheOtherHivesModelledCell() {
        HiveCell[][] pairs = {
                {HiveCell.RED_SCORING, HiveCell.BLUE_SCORING},
                {HiveCell.RED_AUDIENCE, HiveCell.BLUE_AUDIENCE}};
        for (HiveCell[] pair : pairs) {
            for (HiveCellState state : new HiveCellState[] {HiveCellState.UP, HiveCellState.DOWN}) {
                HiveCellPoses.Pose red = pose(pair[0], state), blue = pose(pair[1], state);
                String what = pair[0] + "/" + pair[1] + " " + state;
                assertEquals(what + " x", -red.stickerCentre().x(), blue.stickerCentre().x(), 0.02);
                assertEquals(what + " y", red.stickerCentre().y(), blue.stickerCentre().y(), POS_EPS);
                assertEquals(what + " z", red.stickerCentre().z(), blue.stickerCentre().z(), POS_EPS);
                assertVec(what + " normal", red.outwardNormal(), blue.outwardNormal(), DIR_EPS);
                assertVec(what + " opening", red.openingFacing(), blue.openingFacing(), DIR_EPS);
            }
        }
    }

    /**
     * The separation a tag-row height classifier actually sees, about 14 in. It is
     * <em>not</em> the ~9.42 in of {@link HiveGeometry#STATE_HEIGHT_DELTA_IN}, which
     * applies to a point half a CELL spacing from the pivot rather than to the tags.
     */
    @Test
    public void stickerRiseIsAboutFourteenInches() {
        assertEquals(14.019, HiveCellPoses.STICKER_STATE_RISE_IN, POS_EPS);
        for (HiveCell cell : HiveCell.values()) {
            assertEquals(cell.toString(), HiveCellPoses.STICKER_STATE_RISE_IN,
                    pose(cell, HiveCellState.UP).stickerCentre().z()
                            - pose(cell, HiveCellState.DOWN).stickerCentre().z(), POS_EPS);
        }
        assertNotEquals(HiveGeometry.STATE_HEIGHT_DELTA_IN, HiveCellPoses.STICKER_STATE_RISE_IN, 1.0);

        // 2 · dx · sin(tilt) still holds; dx is the sticker's level-rocker distance.
        Vec3 up = pose(HiveCell.BLUE_SCORING, HiveCellState.UP).stickerCentre();
        Vec3 level = aboutPivot(up, -HiveGeometry.CELL_TILT_DEG);
        double dx = level.y();
        assertEquals(2 * dx * Math.sin(Math.toRadians(HiveGeometry.CELL_TILT_DEG)),
                HiveCellPoses.STICKER_STATE_RISE_IN, POS_EPS);
    }

    /** Unmeasured stays unmeasured: no tag-row position until the offset is known. */
    @Test
    public void tagRowIsNaNUntilTheOffsetIsMeasured() {
        assertTrue(Double.isNaN(HiveCellPoses.TAG_ROW_OFFSET_TOWARD_OPENING_IN));
        Vec3 row = pose(HiveCell.RED_SCORING, HiveCellState.UP).tagRowCentre();
        assertTrue(Double.isNaN(row.x()) && Double.isNaN(row.y()) && Double.isNaN(row.z()));
        assertEquals(0.625, HiveCellPoses.TAG_ROW_OFFSET_BOUND_IN, 1e-9);
    }

    /** Figure 9-10's raised-opening heights bracket the raised sticker, which sits under the CELL. */
    @Test
    public void raisedStickerSitsBelowTheRaisedOpening() {
        for (HiveCell cell : HiveCell.values()) {
            double z = pose(cell, HiveCellState.UP).stickerCentre().z();
            assertTrue(z > HiveGeometry.PIVOT_AXIS_HEIGHT_IN);
            assertTrue(z < HiveGeometry.OPENING_BOTTOM_HEIGHT_IN);
        }
    }
}
