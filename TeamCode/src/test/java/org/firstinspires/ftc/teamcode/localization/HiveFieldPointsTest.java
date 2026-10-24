package org.firstinspires.ftc.teamcode.localization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.HiveCellState;
import org.firstinspires.ftc.teamcode.vision.Vec3;
import org.junit.Test;

/** Which field points may move the pose, and the axle line the aim rule splits the field on. */
public class HiveFieldPointsTest {

    @Test
    public void onlyTheRaisedAudienceCellsAreMeasured() {
        assertTrue(HiveFieldPoints.measured(HiveCell.RED_AUDIENCE, HiveCellState.UP));
        assertTrue(HiveFieldPoints.measured(HiveCell.BLUE_AUDIENCE, HiveCellState.UP));
        assertFalse(HiveFieldPoints.measured(HiveCell.RED_AUDIENCE, HiveCellState.DOWN));
        assertFalse(HiveFieldPoints.measured(HiveCell.RED_SCORING, HiveCellState.UP));
        assertFalse(HiveFieldPoints.measured(HiveCell.BLUE_SCORING, HiveCellState.DOWN));
        assertFalse(HiveFieldPoints.measured(HiveCell.RED_SCORING, HiveCellState.UNKNOWN));
    }

    @Test
    public void everyPointCanBeAimedAtButOnlyMeasuredOnesCanFixThePose() {
        for (HiveCell cell : HiveCell.values()) {
            for (HiveCellState state : new HiveCellState[] {HiveCellState.UP, HiveCellState.DOWN}) {
                assertNotNull(cell + " " + state, HiveFieldPoints.rowCentre(cell, state));
            }
        }
        // Tag 34 is on RED_AUDIENCE, tag 30 on RED_SCORING (BiobuzzTags).
        int redAudienceTag = firstTagOf(HiveCell.RED_AUDIENCE);
        int redScoringTag = firstTagOf(HiveCell.RED_SCORING);
        assertNotNull(HiveFieldPoints.tagPositionForFix(redAudienceTag, HiveCellState.UP));
        assertNull(HiveFieldPoints.tagPositionForFix(redAudienceTag, HiveCellState.DOWN));
        assertNull(HiveFieldPoints.tagPositionForFix(redScoringTag, HiveCellState.UP));
        assertNull(HiveFieldPoints.tagPositionForFix(999, HiveCellState.UP));
    }

    @Test
    public void aFixPointIsTheSamePointAsTheAimPoint() {
        int tag = firstTagOf(HiveCell.BLUE_AUDIENCE);
        Vec3 fix = HiveFieldPoints.tagPositionForFix(tag, HiveCellState.UP);
        Vec3 aim = HiveFieldPoints.tagPosition(tag, HiveCellState.UP);
        assertEquals(aim.x(), fix.x(), 1e-9);
        assertEquals(aim.y(), fix.y(), 1e-9);
        assertEquals(aim.z(), fix.z(), 1e-9);
    }

    @Test
    public void theAxleLineLiesBetweenTheTwoRowsOfEachHive() {
        for (HiveCell cell : HiveCell.values()) {
            Vec3 up = HiveFieldPoints.rowCentre(cell, HiveCellState.UP);
            boolean audience = cell.side() == HiveCell.Side.AUDIENCE;
            assertTrue(cell.toString(), audience ? up.y() < HiveFieldPoints.AXLE_Y_IN : up.y() > HiveFieldPoints.AXLE_Y_IN);
        }
    }

    private static int firstTagOf(HiveCell cell) {
        for (int id = 30; id <= 45; id++) {
            if (org.firstinspires.ftc.teamcode.vision.BiobuzzTags.cellForTag(id) == cell) return id;
        }
        throw new AssertionError("no tag for " + cell);
    }
}
