package org.firstinspires.ftc.teamcode.vision;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Exercises the multi-tag aggregation in {@link CellSighting}.
 *
 * <p>The interesting property is that the row centre comes out the same whether
 * the camera sees four member tags or two, because the lateral offsets are known.
 * That is what stops the aim point jumping as members drop in and out of view —
 * the failure mode of just picking the single "best" tag each frame.
 */
public class CellSightingTest {

    private static final double EPS = 1e-9;
    private static final HiveCell CELL = HiveCell.RED_SCORING;

    /**
     * Places member tags of {@link #CELL} in the robot frame as if the cell were
     * dead ahead at {@code distance} inches, its row of tags running along the
     * robot's Y axis, at height {@code height}.
     */
    private static CellSighting.Member memberAhead(int tagId, double distance, double height) {
        double lateral = BiobuzzTags.memberOffset(tagId).x();
        return new CellSighting.Member(tagId, new Vec3(distance, lateral, height), 1.0);
    }

    private static List<CellSighting.Member> ahead(double distance, double height, int... tagIds) {
        List<CellSighting.Member> members = new ArrayList<>();
        for (int tagId : tagIds) members.add(memberAhead(tagId, distance, height));
        return members;
    }

    @Test
    public void allFourTagsGiveTheRowCentre() {
        CellSighting sighting = CellSighting.fromMembers(CELL, ahead(100, 0, 30, 31, 32, 33), 0L);

        assertEquals(4, sighting.tagCount());
        assertTrue(sighting.lateralCorrectionApplied());
        assertEquals(100.0, sighting.rowCentreRobot().x(), EPS);
        assertEquals(0.0, sighting.rowCentreRobot().y(), EPS);
        assertEquals(100.0, sighting.groundRangeIn(), EPS);
        assertEquals(0.0, sighting.bearingRad(), EPS);
    }

    /**
     * The whole point of using the published offsets: two tags off to one side
     * still resolve to the same centre as all four.
     */
    @Test
    public void twoTagsRecoverTheSameCentreAsFour() {
        CellSighting all = CellSighting.fromMembers(CELL, ahead(100, 0, 30, 31, 32, 33), 0L);

        for (int[] pair : new int[][] {{30, 31}, {32, 33}, {30, 33}, {31, 32}, {30, 32}}) {
            CellSighting some = CellSighting.fromMembers(CELL, ahead(100, 0, pair), 0L);
            assertTrue(Arrays.toString(pair), some.lateralCorrectionApplied());
            assertEquals(Arrays.toString(pair) + " x",
                    all.rowCentreRobot().x(), some.rowCentreRobot().x(), 1e-9);
            assertEquals(Arrays.toString(pair) + " y",
                    all.rowCentreRobot().y(), some.rowCentreRobot().y(), 1e-9);
            assertEquals(Arrays.toString(pair) + " bearing",
                    all.bearingRad(), some.bearingRad(), 1e-9);
        }
    }

    /** One tag leaves the lateral position unknowable, and the sighting says so. */
    @Test
    public void oneTagIsFlaggedAsUncorrected() {
        CellSighting sighting = CellSighting.fromMembers(CELL, ahead(100, 0, 30), 0L);

        assertEquals(1, sighting.tagCount());
        assertFalse(sighting.lateralCorrectionApplied());
        // Sits at the tag itself, 6.5in off the row centre along the row.
        assertEquals(-6.5, sighting.rowCentreRobot().y(), EPS);
    }

    /** Works with the row running along any direction, not just the Y axis. */
    @Test
    public void recoversCentreWhenTheCellIsOffToTheSide() {
        // Cell 45 degrees to the robot's left, row running perpendicular to the
        // line of sight, i.e. along the (-1, 1)/sqrt(2) direction in robot XY.
        double d = 100;
        double k = Math.sqrt(0.5);
        List<CellSighting.Member> members = new ArrayList<>();
        for (int tagId : new int[] {30, 33}) {
            double lateral = BiobuzzTags.memberOffset(tagId).x();
            members.add(new CellSighting.Member(
                    tagId,
                    new Vec3(d * k - lateral * k, d * k + lateral * k, 0),
                    1.0));
        }

        CellSighting sighting = CellSighting.fromMembers(CELL, members, 0L);
        assertTrue(sighting.lateralCorrectionApplied());
        assertEquals(d * k, sighting.rowCentreRobot().x(), 1e-9);
        assertEquals(d * k, sighting.rowCentreRobot().y(), 1e-9);
        assertEquals(Math.toRadians(45), sighting.bearingRad(), 1e-9);
        assertEquals(d, sighting.groundRangeIn(), 1e-9);
    }

    @Test
    public void bearingIsPositiveToTheLeft() {
        List<CellSighting.Member> left = new ArrayList<>();
        left.add(new CellSighting.Member(30, new Vec3(100, 100, 0), 1.0));
        assertEquals(Math.toRadians(45), CellSighting.fromMembers(CELL, left, 0L).bearingRad(), EPS);

        List<CellSighting.Member> right = new ArrayList<>();
        right.add(new CellSighting.Member(30, new Vec3(100, -100, 0), 1.0));
        assertEquals(Math.toRadians(-45), CellSighting.fromMembers(CELL, right, 0L).bearingRad(), EPS);
    }

    /** The HIVE is overhead, so elevation is the angle the shooter has to clear. */
    @Test
    public void elevationMeasuresHeightAboveHorizontal() {
        CellSighting sighting = CellSighting.fromMembers(CELL, ahead(100, 100, 30, 31, 32, 33), 0L);
        assertEquals(Math.toRadians(45), sighting.elevationRad(), 1e-9);
        assertEquals(100.0, sighting.groundRangeIn(), 1e-9);
        assertEquals(Math.hypot(100, 100), sighting.slantRangeIn(), 1e-9);
    }

    @Test
    public void areaIsSummedAcrossContributingTags() {
        List<CellSighting.Member> members = new ArrayList<>();
        members.add(new CellSighting.Member(30, new Vec3(100, -6.5, 0), 0.25));
        members.add(new CellSighting.Member(31, new Vec3(100, -2.75, 0), 0.75));
        assertEquals(1.0, CellSighting.fromMembers(CELL, members, 0L).totalAreaPercent(), EPS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsTagsFromAnotherCell() {
        List<CellSighting.Member> members = new ArrayList<>();
        members.add(new CellSighting.Member(30, new Vec3(100, 0, 0), 1.0)); // RED SCORING
        members.add(new CellSighting.Member(42, new Vec3(100, 0, 0), 1.0)); // BLUE SCORING
        CellSighting.fromMembers(CELL, members, 0L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsEmptyMemberList() {
        CellSighting.fromMembers(CELL, new ArrayList<CellSighting.Member>(), 0L);
    }
}
