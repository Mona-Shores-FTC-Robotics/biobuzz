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

/** Circle's camera-first aim: the camera, then where the camera last saw the CELL, then the odometry. */
public class ChassisAimTargetTest {

    private static final double EPS = 1e-9;
    private static final HiveCell LEFT = HiveCell.leftCell(Alliance.RED);
    private static final HiveCell RIGHT = HiveCell.rightCell(Alliance.RED);

    /** What the camera sees this loop: bearing and range per CELL; absent is not in view. */
    private final Map<HiveCell, Double> bearing = new EnumMap<>(HiveCell.class);
    private final Map<HiveCell, Double> range = new EnumMap<>(HiveCell.class);
    /** The age of the camera's frames, ms. */
    private double frameAgeMs = 30;
    /** The robot's pose as the Pinpoint reports it: inches, degrees. */
    private double x = 50;
    private double y = 50;
    private double headingDeg;
    private boolean poseLive = true;
    private int poseSets;

    private final ChassisAimTarget target = new ChassisAimTarget(
            c -> bearing.containsKey(c) ? bearing.get(c) : Double.NaN,
            c -> range.containsKey(c) ? range.get(c) : Double.NaN,
            c -> bearing.containsKey(c) ? frameAgeMs : Double.NaN,
            () -> 0.0, () -> 0.0);

    private void sees(HiveCell cell, double bearingDeg, double rangeIn) {
        bearing.put(cell, bearingDeg);
        range.put(cell, rangeIn);
    }

    private void seesNothing() {
        bearing.clear();
        range.clear();
    }

    private void at(double x, double y, double headingDeg) {
        this.x = x;
        this.y = y;
        this.headingDeg = headingDeg;
    }

    /** One loop at the current pose. */
    private boolean loop(boolean held, HiveCell upCell, double odometryAimDeg, double trimDeg) {
        return target.update(held, Alliance.RED, upCell, x, y, Math.toRadians(headingDeg), poseLive, poseSets,
                odometryAimDeg, trimDeg);
    }

    private boolean press(HiveCell upCell) {
        return loop(true, upCell, Double.NaN, 0.0);
    }

    @Test
    public void theCameraBeatsAWrongPose() {
        sees(RIGHT, 10.0, 50.0);
        // The odometry, 5 in off, says -2°: the camera's 10° is what the robot turns to.
        assertTrue(loop(true, RIGHT, -2.0, 0.0));
        assertEquals(Source.CAMERA, target.source());
        assertEquals(10.0, target.aimDeg(), EPS);
        assertEquals(50.0, target.rangeIn(), EPS);
        assertEquals(RIGHT, target.locked());
    }

    @Test
    public void onlyTheUpCellIsAimedAtWhenTheHiveIsKnown() {
        sees(LEFT, 5.0, 40.0); // in view but down
        assertTrue(loop(true, RIGHT, 3.0, 0.0));
        assertEquals(Source.ODOMETRY, target.source());
        assertNull(target.locked());
        assertEquals(3.0, target.aimDeg(), EPS);
        assertTrue("the odometry's aim brings no range of its own", Double.isNaN(target.rangeIn()));
    }

    @Test
    public void withTheHiveUnknownItTakesTheNearestOfOursInView() {
        sees(LEFT, 5.0, 60.0);
        sees(RIGHT, -8.0, 45.0);
        assertTrue(press(null));
        assertEquals(RIGHT, target.locked());
        assertEquals(-8.0, target.aimDeg(), EPS);
    }

    @Test
    public void theOtherAlliancesCellIsNeverAimedAt() {
        sees(HiveCell.rightCell(Alliance.BLUE), 4.0, 40.0);
        assertFalse(press(null));
        assertEquals(Source.NONE, target.source());
        assertTrue(Double.isNaN(target.aimDeg()));
        assertNull(target.anchored());
    }

    @Test
    public void aPoseAimTurnsIntoACameraAimWhenTheCellComesIntoView() {
        assertTrue(loop(true, RIGHT, 30.0, 0.0));
        assertEquals(Source.ODOMETRY, target.source());
        // The turn brings it into view: the camera takes over the next loop.
        at(50, 50, 24);
        sees(RIGHT, 6.0, 50.0);
        assertTrue(loop(true, RIGHT, 5.0, 0.0));
        assertEquals(Source.CAMERA, target.source());
        assertEquals(6.0, target.aimDeg(), EPS);
        assertTrue(target.lockedThisLoop());
        assertTrue(loop(true, RIGHT, 5.0, 0.0));
        assertFalse("the lock is logged once", target.lockedThisLoop());
    }

    @Test
    public void losingTheCameraAimsWhereItLastSawTheCellNotWhereTheOdometrySays() {
        at(50, 50, 90);
        sees(RIGHT, 20.0, 50.0); // the CELL is 50 in away at 110° in the Pinpoint's frame
        press(RIGHT);
        // Blocked: the odometry says 0° more to go, the anchor says 10°.
        seesNothing();
        at(50, 50, 100);
        assertTrue(loop(true, RIGHT, 0.0, 0.0));
        assertEquals(Source.ANCHOR, target.source());
        assertEquals(10.0, target.aimDeg(), 1e-6);
        assertEquals(50.0, target.rangeIn(), 1e-6);
        // Back in view: the camera again.
        sees(RIGHT, 1.0, 50.0);
        at(50, 50, 109);
        assertTrue(loop(true, RIGHT, 0.0, 0.0));
        assertEquals(Source.CAMERA, target.source());
    }

    /** The old gyro hold kept a heading and ignored the driving: the anchor follows it. */
    @Test
    public void theAnchorFollowsTheRobotAsItDrivesBlind() {
        at(50, 50, 90);
        sees(RIGHT, 0.0, 40.0); // straight ahead: the CELL is at (50, 90)
        press(RIGHT);
        seesNothing();
        at(80, 50, 90); // 30 in to the side, same heading
        assertTrue(press(RIGHT));
        assertEquals(Source.ANCHOR, target.source());
        assertEquals(Math.toDegrees(Math.atan2(40, -30)) - 90, target.aimDeg(), 1e-6);
        assertEquals(50.0, target.rangeIn(), 1e-6);
    }

    /**
     * A pose that is off cancels: the anchor was stored with the same wrong pose. The robot is
     * really at (50, 50) facing 90° with the CELL at (50, 90), then really at (80, 50); the Pinpoint
     * reports both turned 12° about the origin and shifted (7, -4).
     */
    @Test
    public void aWrongPoseCancelsOutOfTheAnchor() {
        double turn = Math.toRadians(12);
        double c = Math.cos(turn);
        double s = Math.sin(turn);
        at(50 * c - 50 * s + 7, 50 * s + 50 * c - 4, 90 + 12);
        sees(RIGHT, 0.0, 40.0);
        press(RIGHT);
        seesNothing();
        at(80 * c - 50 * s + 7, 80 * s + 50 * c - 4, 90 + 12);
        assertTrue(press(RIGHT));
        assertEquals(Math.toDegrees(Math.atan2(40, -30)) - 90, target.aimDeg(), 1e-6);
        assertEquals(50.0, target.rangeIn(), 1e-6);
    }

    @Test
    public void theAnchorOutlivesThePressAndStartsTheNextOne() {
        at(50, 50, 90);
        sees(RIGHT, 0.0, 40.0);
        press(RIGHT);
        assertFalse(loop(false, RIGHT, 1.0, 0.0));
        assertNull(target.locked());
        assertEquals(Source.NONE, target.source());
        assertEquals(RIGHT, target.anchored());
        // A new press facing away, nothing in view: it turns to the anchor.
        seesNothing();
        at(60, 40, 0);
        assertTrue(press(RIGHT));
        assertEquals(Source.ANCHOR, target.source());
        assertEquals(RIGHT, target.locked());
        assertFalse(target.cameraThisPress());
        assertEquals(Math.toDegrees(Math.atan2(50, -10)), target.aimDeg(), 1e-6);
        // The turn brings the CELL into view: the camera takes over, logged once.
        sees(RIGHT, 3.0, 51.0);
        assertTrue(press(RIGHT));
        assertEquals(Source.CAMERA, target.source());
        assertTrue(target.cameraThisPress());
        assertTrue(target.lockedThisLoop());
    }

    @Test
    public void aSightingBetweenPressesStoresTheAnchorToo() {
        at(50, 50, 90);
        sees(RIGHT, 0.0, 40.0);
        loop(false, RIGHT, Double.NaN, 0.0); // no press yet
        assertEquals(RIGHT, target.anchored());
        seesNothing();
        at(50, 50, 60);
        assertTrue(press(RIGHT));
        assertEquals(Source.ANCHOR, target.source());
        assertEquals(30.0, target.aimDeg(), 1e-6);
    }

    @Test
    public void aPoseSetOnPurposeDropsTheAnchor() {
        sees(RIGHT, 0.0, 40.0);
        press(RIGHT);
        loop(false, RIGHT, Double.NaN, 0.0);
        seesNothing();
        poseSets++; // a corner reset: the frame moved
        assertFalse(press(RIGHT));
        assertEquals(Source.NONE, target.source());
        assertNull(target.anchored());
    }

    @Test
    public void theHiveSayingAnotherCellIsUpDropsTheAnchor() {
        sees(RIGHT, 0.0, 40.0);
        press(RIGHT);
        seesNothing();
        assertFalse(press(LEFT));
        assertNull(target.anchored());
        assertNull(target.locked());
    }

    /** Belding, 19429: the Pinpoint's reads failed for 7.6 s and 64 s, the pose frozen meanwhile. */
    @Test
    public void whileThePinpointIsNotReadingTheAnchorIsNotUsedAndItComesBackAfter() {
        at(50, 50, 90);
        sees(RIGHT, 0.0, 40.0);
        press(RIGHT);
        seesNothing();
        poseLive = false; // frozen at (50, 50, 90°) while the robot drives
        assertFalse(press(RIGHT));
        assertEquals(Source.NONE, target.source());
        assertTrue(Double.isNaN(target.rangeIn()));
        // The camera still aims through the fault, and does not move the anchor with a frozen pose.
        sees(RIGHT, -4.0, 45.0);
        assertTrue(press(RIGHT));
        assertEquals(Source.CAMERA, target.source());
        seesNothing();
        // Reads resume: the Pinpoint kept counting, so the point stored before the fault is right.
        poseLive = true;
        at(80, 50, 90);
        assertTrue(press(RIGHT));
        assertEquals(Source.ANCHOR, target.source());
        assertEquals(50.0, target.rangeIn(), 1e-6);
    }

    @Test
    public void anOldFrameDoesNotStoreAnAnchor() {
        frameAgeMs = 300; // still fresh enough to aim by, too old to place with this loop's pose
        sees(RIGHT, 5.0, 40.0);
        assertTrue(press(RIGHT));
        assertEquals(Source.CAMERA, target.source());
        assertNull(target.anchored());
    }

    @Test
    public void theLockedCellStaysForThePressAsTagsComeAndGo() {
        sees(RIGHT, 2.0, 50.0);
        press(null);
        bearing.remove(RIGHT);
        range.remove(RIGHT);
        sees(LEFT, -30.0, 40.0); // nearer, but the press has its CELL
        press(null);
        assertEquals(RIGHT, target.locked());
        assertEquals(Source.ANCHOR, target.source());
    }

    @Test
    public void aTipUnderThePressDropsTheLockedCell() {
        sees(RIGHT, 2.0, 50.0);
        sees(LEFT, -12.0, 55.0);
        press(RIGHT);
        assertEquals(RIGHT, target.locked());
        press(LEFT);
        assertEquals(LEFT, target.locked());
        assertEquals(-12.0, target.aimDeg(), EPS);
    }

    @Test
    public void trimIsAddedToTheCameraAndTheAnchorAim() {
        sees(RIGHT, 4.0, 50.0);
        loop(true, RIGHT, Double.NaN, 1.5);
        assertEquals(5.5, target.aimDeg(), EPS);
        seesNothing();
        loop(true, RIGHT, Double.NaN, 1.5);
        assertEquals(5.5, target.aimDeg(), 1e-6);
    }

    @Test
    public void noAllianceNoAim() {
        sees(RIGHT, 4.0, 50.0);
        assertFalse(target.update(true, Alliance.UNKNOWN, null, x, y, 0.0, true, 0, 1.0, 0.0));
        assertNull(target.anchored());
    }

    @Test
    public void cellsAreNamedAsTheDriversSeeThem() {
        assertEquals("LEFT", ChassisAimTarget.driverName(LEFT));
        assertEquals("RIGHT", ChassisAimTarget.driverName(HiveCell.rightCell(Alliance.BLUE)));
    }
}
