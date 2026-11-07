package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.opmodes.ChassisAimTarget.Source;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.junit.Test;

import java.util.EnumMap;
import java.util.Map;

/** Circle's camera-first aim: the camera, then its heading on the gyro, then the odometry. */
public class ChassisAimTargetTest {

    private static final double EPS = 1e-9;
    private static final HiveCell LEFT = HiveCell.leftCell(Alliance.RED);
    private static final HiveCell RIGHT = HiveCell.rightCell(Alliance.RED);

    /** What the camera sees this loop: bearing and range per CELL; absent is not in view. */
    private final Map<HiveCell, Double> bearing = new EnumMap<>(HiveCell.class);
    private final Map<HiveCell, Double> range = new EnumMap<>(HiveCell.class);
    private final ChassisAimTarget target = new ChassisAimTarget(
            c -> bearing.containsKey(c) ? bearing.get(c) : Double.NaN,
            c -> range.containsKey(c) ? range.get(c) : Double.NaN);

    private void sees(HiveCell cell, double bearingDeg, double rangeIn) {
        bearing.put(cell, bearingDeg);
        range.put(cell, rangeIn);
    }

    private void seesNothing() {
        bearing.clear();
        range.clear();
    }

    @Test
    public void theCameraBeatsAWrongPose() {
        sees(RIGHT, 10.0, 50.0);
        // The odometry, 5 in off, says -2°: the camera's 10° is what the robot turns to.
        assertTrue(target.update(true, Alliance.RED, RIGHT, 0.0, -2.0, 0.0));
        assertEquals(Source.CAMERA, target.source());
        assertEquals(10.0, target.aimDeg(), EPS);
        assertEquals(RIGHT, target.locked());
    }

    @Test
    public void onlyTheUpCellIsAimedAtWhenTheHiveIsKnown() {
        sees(LEFT, 5.0, 40.0); // in view but down
        assertTrue(target.update(true, Alliance.RED, RIGHT, 0.0, 3.0, 0.0));
        assertEquals(Source.ODOMETRY, target.source());
        assertNull(target.locked());
        assertEquals(3.0, target.aimDeg(), EPS);
    }

    @Test
    public void withTheHiveUnknownItTakesTheNearestOfOursInView() {
        sees(LEFT, 5.0, 60.0);
        sees(RIGHT, -8.0, 45.0);
        assertTrue(target.update(true, Alliance.RED, null, 0.0, Double.NaN, 0.0));
        assertEquals(RIGHT, target.locked());
        assertEquals(-8.0, target.aimDeg(), EPS);
    }

    @Test
    public void theOtherAlliancesCellIsNeverAimedAt() {
        sees(HiveCell.rightCell(Alliance.BLUE), 4.0, 40.0);
        assertFalse(target.update(true, Alliance.RED, null, 0.0, Double.NaN, 0.0));
        assertEquals(Source.NONE, target.source());
        assertTrue(Double.isNaN(target.aimDeg()));
    }

    @Test
    public void aPoseAimTurnsIntoACameraAimWhenTheCellComesIntoView() {
        assertTrue(target.update(true, Alliance.RED, RIGHT, 0.0, 30.0, 0.0));
        assertEquals(Source.ODOMETRY, target.source());
        // The turn brings it into view: the camera takes over the next loop.
        sees(RIGHT, 6.0, 50.0);
        assertTrue(target.update(true, Alliance.RED, RIGHT, Math.toRadians(24.0), 5.0, 0.0));
        assertEquals(Source.CAMERA, target.source());
        assertEquals(6.0, target.aimDeg(), EPS);
        assertTrue(target.lockedThisLoop());
        assertTrue(target.update(true, Alliance.RED, RIGHT, Math.toRadians(24.0), 5.0, 0.0));
        assertFalse("the lock is logged once", target.lockedThisLoop());
    }

    @Test
    public void losingTheCameraHoldsItsHeadingOnTheGyroNotTheOdometry() {
        sees(RIGHT, 20.0, 50.0);
        target.update(true, Alliance.RED, RIGHT, Math.toRadians(90.0), 0.0, 0.0);
        // Blocked: the odometry says 0° more to go, but the camera put the CELL at 110° on the gyro.
        seesNothing();
        assertTrue(target.update(true, Alliance.RED, RIGHT, Math.toRadians(100.0), 0.0, 0.0));
        assertEquals(Source.GYRO_HOLD, target.source());
        assertEquals(10.0, target.aimDeg(), 1e-6);
        // Back in view: the camera again.
        sees(RIGHT, 1.0, 50.0);
        assertTrue(target.update(true, Alliance.RED, RIGHT, Math.toRadians(109.0), 0.0, 0.0));
        assertEquals(Source.CAMERA, target.source());
    }

    @Test
    public void theHeldHeadingWrapsAcrossHalfATurn() {
        sees(RIGHT, 10.0, 50.0);
        target.update(true, Alliance.RED, RIGHT, Math.toRadians(175.0), Double.NaN, 0.0);
        seesNothing();
        target.update(true, Alliance.RED, RIGHT, Math.toRadians(-178.0), Double.NaN, 0.0);
        assertEquals(3.0, target.aimDeg(), 1e-6); // 185° is -175°: 3° more to the left
    }

    @Test
    public void theLockedCellStaysForThePressAsTagsComeAndGo() {
        sees(RIGHT, 2.0, 50.0);
        target.update(true, Alliance.RED, null, 0.0, Double.NaN, 0.0);
        bearing.remove(RIGHT);
        range.remove(RIGHT);
        sees(LEFT, -30.0, 40.0); // nearer, but the press has its CELL
        target.update(true, Alliance.RED, null, 0.0, Double.NaN, 0.0);
        assertEquals(RIGHT, target.locked());
        assertEquals(Source.GYRO_HOLD, target.source());
    }

    @Test
    public void aTipUnderThePressDropsTheLockedCell() {
        sees(RIGHT, 2.0, 50.0);
        sees(LEFT, -12.0, 55.0);
        target.update(true, Alliance.RED, RIGHT, 0.0, Double.NaN, 0.0);
        assertEquals(RIGHT, target.locked());
        target.update(true, Alliance.RED, LEFT, 0.0, Double.NaN, 0.0);
        assertEquals(LEFT, target.locked());
        assertEquals(-12.0, target.aimDeg(), EPS);
    }

    @Test
    public void lettingGoEndsThePress() {
        sees(RIGHT, 2.0, 50.0);
        target.update(true, Alliance.RED, RIGHT, 0.0, Double.NaN, 0.0);
        assertFalse(target.update(false, Alliance.RED, RIGHT, 0.0, 1.0, 0.0));
        assertNull(target.locked());
        assertEquals(Source.NONE, target.source());
        // A new press with nothing in view: no held heading from the last one.
        seesNothing();
        assertFalse(target.update(true, Alliance.RED, RIGHT, 0.0, Double.NaN, 0.0));
        assertFalse(target.cameraThisPress());
    }

    @Test
    public void trimIsAddedToTheCameraAndTheHeldAim() {
        sees(RIGHT, 4.0, 50.0);
        target.update(true, Alliance.RED, RIGHT, 0.0, Double.NaN, 1.5);
        assertEquals(5.5, target.aimDeg(), EPS);
        seesNothing();
        target.update(true, Alliance.RED, RIGHT, 0.0, Double.NaN, 1.5);
        assertEquals(5.5, target.aimDeg(), 1e-6);
    }

    @Test
    public void noAllianceNoAim() {
        sees(RIGHT, 4.0, 50.0);
        assertFalse(target.update(true, Alliance.UNKNOWN, null, 0.0, 1.0, 0.0));
    }

    @Test
    public void cellsAreNamedAsTheDriversSeeThem() {
        assertEquals("LEFT", ChassisAimTarget.driverName(LEFT));
        assertEquals("RIGHT", ChassisAimTarget.driverName(HiveCell.rightCell(Alliance.BLUE)));
    }
}
