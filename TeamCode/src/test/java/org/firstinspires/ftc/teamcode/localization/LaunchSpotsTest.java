package org.firstinspires.ftc.teamcode.localization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.localization.LaunchSpots.Choice;
import org.firstinspires.ftc.teamcode.localization.LaunchSpots.Spot;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.FieldFrame;
import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.HiveCellState;
import org.firstinspires.ftc.teamcode.vision.Vec3;
import org.junit.Test;

/** {@link LaunchSpots#choose}: pose and alliance in, the nearest of the six spots (or a refusal) out. */
public class LaunchSpotsTest {

    private static final double C = FieldFrame.FIELD_CENTRE_INCHES;
    private static final double OFF_WALL = 3.0;
    private static final double EPS = 1e-9;

    private static Choice red(double x, double y) {
        return LaunchSpots.choose(new Pose(x, y, 0), true, Alliance.RED, OFF_WALL);
    }

    private static double wrap(double rad) {
        return Math.IEEEremainder(rad, 2 * Math.PI);
    }

    @Test
    public void theNearestOfTheSixWins() {
        assertEquals(Spot.RIGHT_LAUNCH_OFFSET, red(30, 30).spot);
        assertEquals(Spot.RIGHT_LAUNCH_CATCH, red(60, 22).spot);
        assertEquals(Spot.RIGHT_START, red(56, 6).spot);
        assertEquals(Spot.LEFT_START, red(65, 135).spot);
        assertEquals(Spot.LEFT_LAUNCH_CATCH, red(60, 120).spot);
        assertEquals(Spot.LEFT_LAUNCH_OFFSET, red(30, 110).spot);
    }

    @Test
    public void itIsTheNearestWhateverTheHiveIsDoing() {
        // No HIVE state goes in at all: a robot near the audience end never crosses to the far one.
        Choice c = red(45, 40);
        assertFalse(c.spot.left);
        assertEquals(Math.hypot(c.target.x() - 45, c.target.y() - 40), c.distanceIn, EPS);
        for (Spot spot : Spot.values()) {
            Pose t = spot.target(Alliance.RED, OFF_WALL);
            assertTrue(spot + " is nearer than the choice", Math.hypot(t.x() - 45, t.y() - 40) >= c.distanceIn - EPS);
        }
    }

    @Test
    public void aLaunchSpotIsDrivenToAsDrawn() {
        Pose t = Spot.RIGHT_LAUNCH_CATCH.target(Alliance.RED, OFF_WALL);
        assertEquals(57.5, t.x(), EPS);
        assertEquals(18, t.y(), EPS);
        assertEquals(Math.toRadians(91.9), t.heading(), EPS);
    }

    @Test
    public void aStartStopsOffTheWallAimedAtItsHalfsCell() {
        Pose start = StartPositions.RED_AUDIENCE.pose;
        Pose t = Spot.RIGHT_START.target(Alliance.RED, OFF_WALL);
        assertEquals(start.x(), t.x(), EPS);
        assertEquals(start.y() + OFF_WALL, t.y(), EPS); // faces 90°: out from the audience wall
        Vec3 row = HiveFieldPoints.rowCentre(HiveCell.rightCell(Alliance.RED), HiveCellState.UP);
        assertEquals(0, wrap(Math.atan2(row.y() - t.y(), row.x() - t.x()) - t.heading()), EPS);
        assertEquals("near its start's facing", 0, Math.toDegrees(wrap(t.heading() - start.heading())), 6);

        Pose far = StartPositions.RED_FAR.pose;
        Pose l = Spot.LEFT_START.target(Alliance.RED, OFF_WALL);
        assertEquals(far.y() - OFF_WALL, l.y(), EPS); // faces 270°: out from the far wall
        Vec3 leftRow = HiveFieldPoints.rowCentre(HiveCell.leftCell(Alliance.RED), HiveCellState.UP);
        assertEquals(0, wrap(Math.atan2(leftRow.y() - l.y(), leftRow.x() - l.x()) - l.heading()), EPS);

        // A NaN or negative distance off the wall is the start itself.
        assertEquals(start.y(), Spot.RIGHT_START.target(Alliance.RED, Double.NaN).y(), EPS);
        assertEquals(start.y(), Spot.RIGHT_START.target(Alliance.RED, -4).y(), EPS);
    }

    @Test
    public void blueIsRedTurnedHalfATurn() {
        double[][] where = {{30, 30}, {60, 22}, {56, 6}, {65, 135}, {60, 120}, {30, 110}, {45, 70}};
        for (double[] w : where) {
            Pose p = new Pose(w[0], w[1], 0);
            Choice r = LaunchSpots.choose(p, true, Alliance.RED, OFF_WALL);
            Choice b = LaunchSpots.choose(LaunchSpots.turned(p), true, Alliance.BLUE, OFF_WALL);
            assertEquals(p.toString(), r.spot, b.spot);
            Pose expected = LaunchSpots.turned(r.target);
            assertEquals(expected.x(), b.target.x(), EPS);
            assertEquals(expected.y(), b.target.y(), EPS);
            assertEquals(0, wrap(expected.heading() - b.target.heading()), EPS);
        }
    }

    @Test
    public void everySpotIsOnOurSideAndOnItsCellsHalf() {
        for (Spot spot : Spot.values()) {
            Pose red = spot.target(Alliance.RED, OFF_WALL);
            Pose blue = spot.target(Alliance.BLUE, OFF_WALL);
            assertTrue(spot + " red", red.x() < C);
            assertTrue(spot + " blue", blue.x() > C);
            assertEquals(spot + " red half", spot.left, LaunchSpots.leftHalf(Alliance.RED, red.y()));
            assertEquals(spot + " blue half", spot.left, LaunchSpots.leftHalf(Alliance.BLUE, blue.y()));
            assertEquals(spot.name(), spot.name().startsWith("LEFT_"), spot.left);
        }
    }

    @Test
    public void eachSpotServesItsOwnHalfsCell() {
        for (Alliance alliance : new Alliance[]{Alliance.RED, Alliance.BLUE}) {
            for (Spot spot : Spot.values()) {
                HiveCell cell = spot.left ? HiveCell.leftCell(alliance) : HiveCell.rightCell(alliance);
                Vec3 row = spot.cellRow(alliance);
                Vec3 expected = HiveFieldPoints.rowCentre(cell, HiveCellState.UP);
                assertEquals(expected.x(), row.x(), EPS);
                assertEquals(expected.y(), row.y(), EPS);
                // The CELL is on the spot's side of the axle line.
                Pose t = spot.target(alliance, OFF_WALL);
                assertEquals(spot + " " + alliance, t.y() > HiveFieldPoints.AXLE_Y_IN, row.y() > HiveFieldPoints.AXLE_Y_IN);
            }
        }
        assertNull(Spot.RIGHT_START.cellRow(Alliance.UNKNOWN));
    }

    @Test
    public void driftWarnsOnlyPastAPositiveThreshold() {
        assertFalse(LaunchSpots.driftWarns(2.9, 3.0));
        assertFalse(LaunchSpots.driftWarns(3.0, 3.0));
        assertTrue(LaunchSpots.driftWarns(3.1, 3.0));
        assertFalse("no camera frame never warns", LaunchSpots.driftWarns(Double.NaN, 3.0));
        assertFalse("a NaN threshold never warns", LaunchSpots.driftWarns(50, Double.NaN));
        assertFalse("0 turns it off", LaunchSpots.driftWarns(50, 0));
    }

    @Test
    public void anUnreferencedPoseIsRefusedAndNothingIsChosen() {
        Choice c = LaunchSpots.choose(new Pose(40, 30, 0), false, Alliance.RED, OFF_WALL);
        assertTrue(c.refused());
        assertNull(c.spot);
        assertNull(c.target);
        assertTrue(c.refusal, c.refusal.contains("not on the field"));
    }

    @Test
    public void noPoseNoAllianceOrTheOtherSideIsRefused() {
        assertTrue(LaunchSpots.choose(null, true, Alliance.RED, OFF_WALL).refused());
        assertTrue(LaunchSpots.choose(new Pose(40, 30, 0), true, Alliance.UNKNOWN, OFF_WALL).refused());
        assertTrue(LaunchSpots.choose(new Pose(40, 30, 0), true, null, OFF_WALL).refused());
        // Never across the centre line: red past it or on it, blue short of it.
        Choice past = red(80, 30);
        assertTrue(past.refused());
        assertTrue(past.refusal, past.refusal.contains("centre line"));
        assertTrue(red(C, 30).refused());
        assertTrue(LaunchSpots.choose(new Pose(60, 110, 0), true, Alliance.BLUE, OFF_WALL).refused());
    }
}
