package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Random;
import org.firstinspires.ftc.teamcode.localization.TeamSpot;
import org.firstinspires.ftc.teamcode.localization.generated.TeamSpots;
import org.firstinspires.ftc.teamcode.opmodes.FlowerPicker;
import org.firstinspires.ftc.teamcode.subsystems.TurretAim;
import org.firstinspires.ftc.teamcode.subsystems.TurretSubsystem;
import org.junit.After;
import org.junit.Test;

/**
 * The Fruitport calibration ({@link FruitportCalibration}): it puts Fruitport's numbers into the calibration engine,
 * keeps Belding's where it has none, and puts Belding's back afterwards.
 */
public class FruitportCalibrationTest {

    @After
    public void beldingsNumbersAreBack() {
        FruitportCalibration.restore();
    }

    /** Using it turns the engine on with Fruitport's rows and loads; restoring leaves Belding's exactly as they were. */
    @Test
    public void useAndRestoreSwapTheNumbers() {
        assumeNotSelectedByEnvironment();
        boolean onBefore = BeldingCalibration.on;
        double[][] rows = BeldingCalibration.STAY_IN;
        int[] irWall = BeldingCalibration.IR_WALL;
        double dwell = BeldingCalibration.PICK_DWELL_S;
        FruitportCalibration.use();
        assertTrue(FruitportCalibration.on);
        assertTrue("the engine runs Fruitport's numbers", BeldingCalibration.on);
        assertEquals(0.94, BeldingCalibration.stayInFraction(FruitportCalibration.AUTO_HIVE_RANGE_IN), 1e-9);
        assertArrayEquals(FruitportCalibration.IR_WALL, BeldingCalibration.IR_WALL);
        assertTrue(BeldingCalibration.shortVolleysRunTheFeed);
        assertEquals(4.0 / 13, BeldingCalibration.wholeFeedChance, 1e-9);
        FruitportCalibration.restore();
        assertFalse(FruitportCalibration.on);
        assertEquals(onBefore, BeldingCalibration.on);
        assertTrue(rows == BeldingCalibration.STAY_IN);
        assertTrue(irWall == BeldingCalibration.IR_WALL);
        assertEquals(dwell, BeldingCalibration.PICK_DWELL_S, 0);
        assertFalse(BeldingCalibration.shortVolleysRunTheFeed);
        assertEquals(0, BeldingCalibration.wholeFeedChance, 0);
        assertTrue(BeldingCalibration.rollerFillsLane);
        assertEquals("Belding's CLOSE row is back", 0.70, BeldingCalibration.stayInFraction(36.0), 1e-9);
    }

    /**
     * The measured row sits at the range of both spots Fruitport's Autos fired from, as the team spots are today:
     * move a spot and the row no longer describes it.
     */
    @Test
    public void theMeasuredRowIsBothAutoHiveSpotsRange() {
        assertEquals("RIGHT_AUTO_HIVE's range to the RIGHT CELL", FruitportCalibration.AUTO_HIVE_RANGE_IN,
                range(TeamSpots.RIGHT_AUTO_HIVE), 0.6);
        assertEquals("LEFT_AUTO_HIVE's range to the LEFT CELL", FruitportCalibration.AUTO_HIVE_RANGE_IN,
                range(TeamSpots.LEFT_AUTO_HIVE), 0.6);
    }

    /** Beyond the furthest spot Fruitport fired from, the share is Belding's RIGHT_START row, unchanged. */
    @Test
    public void beyondFruitportsSpotsTheRowIsBeldings() {
        assumeNotSelectedByEnvironment();
        double beldings = BeldingCalibration.stayInFraction(53.9);
        FruitportCalibration.use();
        assertEquals(beldings, BeldingCalibration.stayInFraction(53.9), 1e-9);
        assertEquals(beldings, BeldingCalibration.stayInFraction(70), 1e-9);
        double between = BeldingCalibration.stayInFraction(51.2);  // RIGHT_AUTO_OFFSET
        assertTrue("between the rows the share is between them", between < 0.94 && between > beldings);
        assertEquals("nearer than Fruitport fired from, Belding's CLOSE row", 0.70, BeldingCalibration.stayInFraction(36.0), 1e-9);
    }

    /**
     * Both robots' loads are four in 9 of 10: 20245's ten logged volleys, and 19429 given the same (mentor, 28 Nov
     * 2026). 19429's own logged volleys, four in 4 of 10, are kept beside them.
     */
    @Test
    public void bothRobotsLoadsAreFullNineTimesInTen() {
        assumeNotSelectedByEnvironment();
        org.junit.Assume.assumeTrue(System.getenv("BIOBUZZ_FRUITPORT_19429_LOADS") == null);
        FruitportCalibration.use();
        for (String robot : new String[] {"19429", "20245", "both"}) {
            Random luck = new Random(1);
            int full = 0, n = 4000;
            for (int i = 0; i < n; i++) {
                assertEquals(robot + " at the WALL FLOWER", 4, BeldingCalibration.load(robot, false, luck));
                if (BeldingCalibration.load(robot, true, luck) == 4) full++;
            }
            assertEquals(robot + ": the SCORING FLOWER load is four 3 times in 4", 0.75, full / (double) n, 0.03);
        }
        int[] wall = FruitportCalibration.UV_WALL, scoring = FruitportCalibration.UV_SCORING;
        int fours = 0;
        for (int v : wall) if (v == 4) fours++;
        for (int v : scoring) if (v == 4) fours++;
        assertEquals("four in 9 of the 10 volleys", 9, fours);
        assertEquals(10, wall.length + scoring.length);
        int logged = 0;
        for (int v : FruitportCalibration.IR_WALL_LOGGED) if (v == 4) logged++;
        for (int v : FruitportCalibration.IR_SCORING_LOGGED) if (v == 4) logged++;
        assertEquals("19429's logged volleys: four in 4 of 10", 4, logged);
    }

    /** 19429's roller stick does not fill the lane here: its measured volleys are drawn instead. */
    @Test
    public void theRollerStickDrawsItsMeasuredLoads() {
        assumeNotSelectedByEnvironment();
        assertTrue(BeldingCalibration.rollerStick("19429"));
        FruitportCalibration.use();
        assertFalse(BeldingCalibration.rollerFillsLane);
    }

    /**
     * The pick's dwell floor is the whole of master's dwell: Fruitport's picks stayed their whole dwell (3.0 to 4.0 s
     * card to card), the lane-full signal never ending one early.
     */
    @Test
    public void aPickStaysItsWholeDwell() {
        assertEquals(FlowerPicker.dwell.dwellMs / 1000.0, FruitportCalibration.PICK_DWELL_S, 1e-9);
    }

    /** A volley's whole-feed chance is drawn per volley, never in the best case. */
    @Test
    public void theWholeFeedIsDrawnPerVolley() {
        assumeNotSelectedByEnvironment();
        FruitportCalibration.use();
        Random luck = new Random(7);
        int whole = 0;
        for (int i = 0; i < 4000; i++) if (BeldingCalibration.wholeFeed(luck)) whole++;
        assertEquals(4.0 / 13, whole / 4000.0, 0.03);
        try {
            BeldingCalibration.bestCase = true;
            assertFalse(BeldingCalibration.wholeFeed(luck));
        } finally {
            BeldingCalibration.bestCase = false;
        }
    }

    /** A study run with BIOBUZZ_AUTO_CALIBRATION=fruitport has it on from the start: these swaps are not for it. */
    private static void assumeNotSelectedByEnvironment() {
        org.junit.Assume.assumeTrue(System.getenv("BIOBUZZ_AUTO_CALIBRATION") == null
                && System.getenv("BIOBUZZ_BELDING_WHOLE_FEED") == null && System.getenv("BIOBUZZ_BELDING_PICK_DWELL") == null);
    }

    /**
     * A team spot's range from the turret axis to the raised CELL at its end of the field, as the simulator's launch
     * computes it. The RIGHT CELL is the one up at the start; a spot at the LEFT end is measured as its mirror image
     * at the RIGHT end (the HIVE is the same either way up).
     */
    private static double range(TeamSpot spot) {
        FieldSim probe = new FieldSim(new java.util.ArrayList<>(), 1, HiveCalibration.current().fit());
        double[] aim = probe.red.aimPointFor(-1);
        boolean leftEnd = spot.y > FieldSim.CENTRE_IN;
        double y = leftEnd ? 2 * FieldSim.CENTRE_IN - spot.y : spot.y;
        double heading = Math.toRadians(leftEnd ? -spot.headingDeg : spot.headingDeg);
        // The turret sits left of the robot's centre line, so the mirror image carries it on the other side.
        double turretLeft = leftEnd ? -TurretSubsystem.turretLeftIn : TurretSubsystem.turretLeftIn;
        return TurretAim.rangeFromFieldIn(spot.x, y, heading, TurretSubsystem.turretForwardIn, turretLeft, aim[0], aim[1]);
    }
}
