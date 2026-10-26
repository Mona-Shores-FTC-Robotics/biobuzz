package org.firstinspires.ftc.teamcode.localization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.FieldFrame;
import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.HiveCellState;
import org.firstinspires.ftc.teamcode.vision.Vec3;
import org.junit.Test;

/** The eight CELL points: the CAD rocker model, the field check of it, and which points may fix the pose. */
public class HiveFieldPointsTest {

    /**
     * FTC 12303's reading of the field CAD (v26-27.2), red HIVE, relative to the axle: the tag row's
     * horizontal distance from the axle and height, raised and lowered, for both CELLs. Our two
     * rocker numbers were chosen to reproduce these; this pins them.
     */
    @Test
    public void theRockerModelReproducesTheCadRows() {
        assertEquals(13.10, HiveFieldPoints.UP_ROW_FROM_AXLE_IN, 0.05);
        assertEquals(49.80, HiveFieldPoints.UP_ROW_HEIGHT_IN, 0.05);
        assertEquals(11.58, HiveFieldPoints.DOWN_ROW_FROM_AXLE_IN, 0.05);
        assertEquals(35.48, HiveFieldPoints.DOWN_ROW_HEIGHT_IN, 0.05);
        // The other CELL in the same CAD: -13.08 / 49.84 up, -11.61 / 35.52 down.
        assertEquals(13.08, HiveFieldPoints.UP_ROW_FROM_AXLE_IN, 0.05);
        assertEquals(35.52, HiveFieldPoints.DOWN_ROW_HEIGHT_IN, 0.05);
    }

    /** 19429, 3 Oct 2026 (#156): the raised AUDIENCE rows by camera from the red audience start. */
    @Test
    public void theFieldMeasurementAgreesWithTheCadToUnderAnInch() {
        for (HiveCell cell : new HiveCell[] {HiveCell.RED_AUDIENCE, HiveCell.BLUE_AUDIENCE}) {
            Vec3 up = HiveFieldPoints.rowCentre(cell, HiveCellState.UP);
            assertEquals(cell.toString(), 58.4, up.y(), 1.0);
            assertEquals(cell.toString(), 50.2, up.z(), 1.0);
            assertTrue(HiveFieldPoints.checkedOnField(cell, HiveCellState.UP));
        }
        assertFalse(HiveFieldPoints.checkedOnField(HiveCell.RED_AUDIENCE, HiveCellState.DOWN));
        assertFalse(HiveFieldPoints.checkedOnField(HiveCell.RED_SCORING, HiveCellState.UP));
        assertFalse(HiveFieldPoints.checkedOnField(HiveCell.RED_SCORING, HiveCellState.UNKNOWN));
    }

    @Test
    public void theAxleIsOnTheCentreLineAndTheHivesSitAboutIt() {
        assertEquals(FieldFrame.FIELD_CENTRE_INCHES, HiveFieldPoints.AXLE_Y_IN, 1e-9);
        assertEquals(25.5, HiveFieldPoints.hiveX(Alliance.BLUE) - HiveFieldPoints.hiveX(Alliance.RED), 1e-9);
        assertEquals(FieldFrame.FIELD_CENTRE_INCHES,
                (HiveFieldPoints.hiveX(Alliance.BLUE) + HiveFieldPoints.hiveX(Alliance.RED)) / 2.0, 1e-9);
        assertTrue(HiveFieldPoints.hiveX(Alliance.RED) < HiveFieldPoints.hiveX(Alliance.BLUE));
    }

    /** Each HIVE's two rows mirror about the axle, and a lowered row is nearer the axle and lower than a raised one. */
    @Test
    public void theRowsMirrorAboutTheAxle() {
        for (Alliance alliance : new Alliance[] {Alliance.RED, Alliance.BLUE}) {
            HiveCell audience = HiveCell.cell(alliance, HiveCell.Side.AUDIENCE);
            HiveCell scoring = HiveCell.cell(alliance, HiveCell.Side.SCORING);
            for (HiveCellState state : new HiveCellState[] {HiveCellState.UP, HiveCellState.DOWN}) {
                Vec3 a = HiveFieldPoints.rowCentre(audience, state);
                Vec3 s = HiveFieldPoints.rowCentre(scoring, state);
                assertEquals(HiveFieldPoints.AXLE_Y_IN - a.y(), s.y() - HiveFieldPoints.AXLE_Y_IN, 1e-9);
                assertEquals(a.z(), s.z(), 1e-9);
                assertEquals(a.x(), s.x(), 1e-9);
                assertTrue(a.y() < HiveFieldPoints.AXLE_Y_IN);
                assertTrue(s.y() > HiveFieldPoints.AXLE_Y_IN);
            }
            Vec3 up = HiveFieldPoints.rowCentre(audience, HiveCellState.UP);
            Vec3 down = HiveFieldPoints.rowCentre(audience, HiveCellState.DOWN);
            assertTrue(down.y() > up.y());
            assertTrue(down.z() < up.z());
        }
    }

    @Test
    public void everySettledPointCanBeAimedAtAndCanFixThePose() {
        for (HiveCell cell : HiveCell.values()) {
            for (HiveCellState state : new HiveCellState[] {HiveCellState.UP, HiveCellState.DOWN}) {
                assertNotNull(cell + " " + state, HiveFieldPoints.rowCentre(cell, state));
                int tag = firstTagOf(cell);
                Vec3 fix = HiveFieldPoints.tagPositionForFix(tag, state);
                Vec3 aim = HiveFieldPoints.tagPosition(tag, state);
                assertNotNull(cell + " " + state, fix);
                assertEquals(aim.x(), fix.x(), 1e-9);
                assertEquals(aim.y(), fix.y(), 1e-9);
                assertEquals(aim.z(), fix.z(), 1e-9);
            }
            assertNull(HiveFieldPoints.rowCentre(cell, HiveCellState.UNKNOWN));
            assertNull(HiveFieldPoints.tagPositionForFix(firstTagOf(cell), HiveCellState.UNKNOWN));
        }
        assertNull(HiveFieldPoints.tagPositionForFix(999, HiveCellState.UP));
    }

    /** A tag's offset along its row stays along field x in both states; the row runs along the axle. */
    @Test
    public void aTagsOffsetAlongTheRowIsAlongFieldX() {
        int first = firstTagOf(HiveCell.RED_AUDIENCE);
        for (HiveCellState state : new HiveCellState[] {HiveCellState.UP, HiveCellState.DOWN}) {
            Vec3 row = HiveFieldPoints.rowCentre(HiveCell.RED_AUDIENCE, state);
            Vec3 tag0 = HiveFieldPoints.tagPosition(first, state);
            Vec3 tag3 = HiveFieldPoints.tagPosition(first + 3, state);
            assertEquals(row.y(), tag0.y(), 1e-9);
            assertEquals(row.z(), tag0.z(), 1e-9);
            assertEquals(13.0, Math.abs(tag3.x() - tag0.x()), 1e-9);
            assertEquals(row.x(), (tag0.x() + tag3.x()) / 2.0, 1e-9);
        }
    }

    private static int firstTagOf(HiveCell cell) {
        for (int id = 30; id <= 45; id++) {
            if (org.firstinspires.ftc.teamcode.vision.BiobuzzTags.cellForTag(id) == cell) return id;
        }
        throw new AssertionError("no tag for " + cell);
    }
}
