package org.firstinspires.ftc.teamcode.localization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.localization.LaunchSpots.Choice;
import org.firstinspires.ftc.teamcode.localization.LaunchSpots.Spot;
import org.firstinspires.ftc.teamcode.localization.LaunchSpots.Why;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.FieldFrame;
import org.firstinspires.ftc.teamcode.vision.HiveState;
import org.junit.Test;

/** {@link LaunchSpots#choose}: pose, alliance and CELL state in, a spot (or a refusal) out. */
public class LaunchSpotsTest {

    private static final double C = FieldFrame.FIELD_CENTRE_INCHES;

    /** A red pose turned half a turn for blue, as the field is. */
    private static Pose blue(Pose red) {
        return new Pose(2 * C - red.x(), 2 * C - red.y(), red.heading() + Math.PI);
    }

    private static Choice red(double x, double y, HiveState hive) {
        return LaunchSpots.choose(new Pose(x, y, 0), true, Alliance.RED, hive);
    }

    @Test
    public void theUpCellsHalfWinsWhereverTheRobotIs() {
        // Red at the audience end, its LEFT (scoring-end) CELL up: across the axle to a LEFT spot.
        Choice c = red(40, 30, HiveState.LEFT_CELL_UP);
        assertFalse(c.refused());
        assertTrue(c.spot.left);
        assertEquals(Why.CELL_UP, c.why);
        Choice r = red(40, 110, HiveState.RIGHT_CELL_UP);
        assertFalse(r.spot.left);
        assertEquals(Why.CELL_UP, r.why);
    }

    @Test
    public void unknownCellStateGoesToTheNearerHalf() {
        for (HiveState unknown : new HiveState[]{HiveState.UNSEEN, HiveState.TRANSITION, null}) {
            Choice low = red(45, 40, unknown);
            assertFalse(unknown + ": audience end is red's RIGHT CELL", low.spot.left);
            assertEquals(Why.NEAREST, low.why);
            Choice high = red(45, 100, unknown);
            assertTrue(unknown + ": scoring end is red's LEFT CELL", high.spot.left);
        }
    }

    @Test
    public void onTheHalfTheNearerOfCatchAndOffset() {
        // The OFFSETs are at x 41, the CATCHes at 57.5.
        assertEquals(Spot.RIGHT_LAUNCH_OFFSET, red(30, 30, HiveState.RIGHT_CELL_UP).spot);
        assertEquals(Spot.RIGHT_LAUNCH_CATCH, red(65, 30, HiveState.RIGHT_CELL_UP).spot);
        assertEquals(Spot.LEFT_LAUNCH_OFFSET, red(30, 110, HiveState.LEFT_CELL_UP).spot);
        assertEquals(Spot.LEFT_LAUNCH_CATCH, red(65, 110, HiveState.LEFT_CELL_UP).spot);
    }

    @Test
    public void theTargetIsTheSpotAsDrawnForRed() {
        Choice c = red(65, 30, HiveState.RIGHT_CELL_UP);
        assertEquals(57.5, c.target.x(), 1e-9);
        assertEquals(18, c.target.y(), 1e-9);
        assertEquals(Math.toRadians(91.9), c.target.heading(), 1e-9);
    }

    @Test
    public void blueIsRedTurnedHalfATurn() {
        Pose[] poses = {new Pose(30, 30, 0), new Pose(65, 30, 0), new Pose(30, 110, 0), new Pose(65, 110, 0)};
        HiveState[] states = {HiveState.RIGHT_CELL_UP, HiveState.LEFT_CELL_UP, HiveState.UNSEEN};
        for (Pose p : poses) {
            for (HiveState s : states) {
                Choice r = LaunchSpots.choose(p, true, Alliance.RED, s);
                Choice b = LaunchSpots.choose(blue(p), true, Alliance.BLUE, s);
                assertEquals(p + " " + s, r.spot, b.spot);
                assertEquals(r.why, b.why);
                Pose expected = blue(r.target);
                assertEquals(expected.x(), b.target.x(), 1e-9);
                assertEquals(expected.y(), b.target.y(), 1e-9);
                assertEquals(0, Math.IEEEremainder(expected.heading() - b.target.heading(), 2 * Math.PI), 1e-9);
            }
        }
        // Blue's RIGHT CELL is at the scoring end: its RIGHT spots are past the axle.
        assertTrue(Spot.RIGHT_LAUNCH_CATCH.pose(Alliance.BLUE).y() > C);
        assertTrue(Spot.RIGHT_LAUNCH_CATCH.pose(Alliance.BLUE).x() > C);
    }

    @Test
    public void everySpotIsOnOurSideOfTheCentreLine() {
        for (Spot spot : Spot.values()) {
            assertTrue(spot + " red", spot.pose(Alliance.RED).x() < C);
            assertTrue(spot + " blue", spot.pose(Alliance.BLUE).x() > C);
            assertEquals(spot + " is on its CELL's half", spot.left,
                    LaunchSpots.leftHalf(Alliance.RED, spot.red.y()));
        }
    }

    @Test
    public void anUnreferencedPoseIsRefusedAndNothingIsChosen() {
        Choice c = LaunchSpots.choose(new Pose(40, 30, 0), false, Alliance.RED, HiveState.RIGHT_CELL_UP);
        assertTrue(c.refused());
        assertNull(c.spot);
        assertNull(c.target);
        assertTrue(c.refusal, c.refusal.contains("not on the field"));
    }

    @Test
    public void noPoseNoAllianceOrTheOtherSideIsRefused() {
        assertTrue(LaunchSpots.choose(null, true, Alliance.RED, HiveState.RIGHT_CELL_UP).refused());
        assertTrue(LaunchSpots.choose(new Pose(40, 30, 0), true, Alliance.UNKNOWN, HiveState.RIGHT_CELL_UP).refused());
        assertTrue(LaunchSpots.choose(new Pose(40, 30, 0), true, null, HiveState.RIGHT_CELL_UP).refused());
        // Never across the centre line: red past it, blue short of it, or on it.
        Choice past = red(80, 30, HiveState.RIGHT_CELL_UP);
        assertTrue(past.refused());
        assertTrue(past.refusal, past.refusal.contains("centre line"));
        assertTrue(red(C, 30, HiveState.RIGHT_CELL_UP).refused());
        assertTrue(LaunchSpots.choose(new Pose(60, 110, 0), true, Alliance.BLUE, HiveState.RIGHT_CELL_UP).refused());
    }
}
