package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.Vec3;
import org.junit.Test;

public class TurretAimTest {

    private static final double EPS = 1e-9;

    @Test
    public void aTargetAheadOfTheRobotIsZeroAndLeftIsPositive() {
        assertEquals(0.0, TurretAim.fromRobotFrameDeg(new Vec3(50, 0, 40), 0, 0), EPS);
        assertEquals(90.0, TurretAim.fromRobotFrameDeg(new Vec3(0, 50, 40), 0, 0), EPS);
        assertEquals(-90.0, TurretAim.fromRobotFrameDeg(new Vec3(0, -50, 40), 0, 0), EPS);
        assertEquals(45.0, TurretAim.fromRobotFrameDeg(new Vec3(30, 30, 40), 0, 0), EPS);
    }

    @Test
    public void theTurretOffsetMovesTheEye() {
        // Turret 2 in behind the centre: a target 10 in to the left is seen from (-2, 0).
        double expected = Math.toDegrees(Math.atan2(10, 2));
        assertEquals(expected, TurretAim.fromRobotFrameDeg(new Vec3(0, 10, 0), -2, 0), EPS);
        // Turret 3 in to the left: a target 10 in left and 10 in ahead is 7 in left of it.
        assertEquals(Math.toDegrees(Math.atan2(7, 10)), TurretAim.fromRobotFrameDeg(new Vec3(10, 10, 0), 0, 3), EPS);
    }

    @Test
    public void fieldAimSubtractsTheHeading() {
        // Facing +x, target up-right: 45 deg left of the nose.
        assertEquals(45.0, TurretAim.fromFieldDeg(0, 0, 0, 0, 0, 10, 10), EPS);
        // Facing +y (90 deg), target straight up the field: dead ahead.
        assertEquals(0.0, TurretAim.fromFieldDeg(0, 0, Math.PI / 2, 0, 0, 0, 10), EPS);
        // Facing +y, target along +x: 90 deg to the right.
        assertEquals(-90.0, TurretAim.fromFieldDeg(0, 0, Math.PI / 2, 0, 0, 10, 0), EPS);
        // Facing +x, target behind: +180, never -180.
        assertEquals(180.0, TurretAim.fromFieldDeg(0, 0, 0, 0, 0, -10, 0), EPS);
    }

    @Test
    public void fieldAimRotatesTheTurretOffsetWithTheRobot() {
        // Robot at (10, 10) facing +y; turret 2 in forward of centre is at (10, 12).
        // Target at (10, 22) is straight ahead of it; target at (12, 12) is 90 deg right.
        assertEquals(0.0, TurretAim.fromFieldDeg(10, 10, Math.PI / 2, 2, 0, 10, 22), EPS);
        assertEquals(-90.0, TurretAim.fromFieldDeg(10, 10, Math.PI / 2, 2, 0, 12, 12), EPS);
        assertEquals(10.0, TurretAim.rangeFromFieldIn(10, 10, Math.PI / 2, 2, 0, 10, 22), EPS);
    }

    @Test
    public void cameraAndFieldAgreeOnTheSameGeometry() {
        // Robot at (20, 30) facing 30 deg; a point 40 in ahead and 10 in left of the centre.
        double h = Math.toRadians(30);
        double px = 20 + 40 * Math.cos(h) - 10 * Math.sin(h);
        double py = 30 + 40 * Math.sin(h) + 10 * Math.cos(h);
        double fromCamera = TurretAim.fromRobotFrameDeg(new Vec3(40, 10, 0), 1.5, -0.5);
        double fromField = TurretAim.fromFieldDeg(20, 30, h, 1.5, -0.5, px, py);
        assertEquals(fromCamera, fromField, 1e-9);
    }

    // ---------------------------------------------------------------- which CELL

    private static final double AXLE = 72.29;
    private static final double BAND = 19.75;

    @Test
    public void theCellAtTheRobotsEndOfTheField() {
        assertEquals(HiveCell.RED_AUDIENCE, TurretAim.nearEndCell(Alliance.RED, 10, null, AXLE, BAND));
        assertEquals(HiveCell.RED_SCORING, TurretAim.nearEndCell(Alliance.RED, 120, null, AXLE, BAND));
        assertEquals(HiveCell.BLUE_AUDIENCE, TurretAim.nearEndCell(Alliance.BLUE, 10, null, AXLE, BAND));
        assertEquals(HiveCell.BLUE_SCORING, TurretAim.nearEndCell(Alliance.BLUE, 120, null, AXLE, BAND));
    }

    @Test
    public void insideTheBandThePreviousChoiceStands() {
        // Driving up past the HIVE from the audience end: stays on AUDIENCE until clear of the band.
        assertEquals(HiveCell.RED_AUDIENCE, TurretAim.nearEndCell(Alliance.RED, 60, HiveCell.RED_AUDIENCE, AXLE, BAND));
        assertEquals(HiveCell.RED_AUDIENCE, TurretAim.nearEndCell(Alliance.RED, 85, HiveCell.RED_AUDIENCE, AXLE, BAND));
        assertEquals(HiveCell.RED_SCORING, TurretAim.nearEndCell(Alliance.RED, 93, HiveCell.RED_AUDIENCE, AXLE, BAND));
        // And back down: stays on SCORING until clear the other way.
        assertEquals(HiveCell.RED_SCORING, TurretAim.nearEndCell(Alliance.RED, 60, HiveCell.RED_SCORING, AXLE, BAND));
        assertEquals(HiveCell.RED_AUDIENCE, TurretAim.nearEndCell(Alliance.RED, 50, HiveCell.RED_SCORING, AXLE, BAND));
    }

    @Test
    public void insideTheBandWithNoPreviousChoiceTheNearerSideWins() {
        assertEquals(HiveCell.RED_AUDIENCE, TurretAim.nearEndCell(Alliance.RED, 70, null, AXLE, BAND));
        assertEquals(HiveCell.RED_SCORING, TurretAim.nearEndCell(Alliance.RED, 75, null, AXLE, BAND));
    }

    @Test
    public void noAllianceOrNoPoseGivesNoCell() {
        assertNull(TurretAim.nearEndCell(Alliance.UNKNOWN, 10, null, AXLE, BAND));
        assertNull(TurretAim.nearEndCell(Alliance.RED, Double.NaN, HiveCell.RED_AUDIENCE, AXLE, BAND));
    }
}
