package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LaunchSpeedTableTest {

    private static final double EPS = 1e-9;
    private static final double NaN = Double.NaN;

    private static double at(double[] ranges, double[] speeds, double rangeIn) {
        return LaunchSpeedTable.interpolate(ranges, speeds, rangeIn, 0);
    }

    @Test
    public void betweenTwoRowsIsAStraightLine() {
        double[] r = {40, 60};
        double[] s = {1400, 1600};
        assertEquals(1500, at(r, s, 50), EPS);
        assertEquals(1450, at(r, s, 45), EPS);
    }

    @Test
    public void onARowIsThatRow() {
        double[] r = {40, 60, 80};
        double[] s = {1400, 1600, 1900};
        assertEquals(1600, at(r, s, 60), EPS);
        assertEquals(1400, at(r, s, 40), EPS);
    }

    @Test
    public void withNoExtensionPastEitherEndIsTheEndRow() {
        double[] r = {40, 60, 80};
        double[] s = {1400, 1600, 1900};
        assertEquals(1400, LaunchSpeedTable.interpolate(r, s, 10, 0), EPS);
        assertEquals(1900, LaunchSpeedTable.interpolate(r, s, 200, 0), EPS);
        assertEquals(1900, LaunchSpeedTable.interpolate(r, s, 200, NaN), EPS);
    }

    @Test
    public void slightlyPastAnEndFollowsThatEndsSlope() {
        // Close end: 10 ticks/s per inch (40→60); far end: 15 per inch (60→80).
        double[] r = {40, 60, 80};
        double[] s = {1400, 1600, 1900};
        assertEquals(1370, LaunchSpeedTable.interpolate(r, s, 37, 5), EPS);
        assertEquals(1945, LaunchSpeedTable.interpolate(r, s, 83, 5), EPS);
    }

    @Test
    public void farPastAnEndHoldsWhereTheExtensionStops() {
        double[] r = {40, 60, 80};
        double[] s = {1400, 1600, 1900};
        assertEquals(1350, LaunchSpeedTable.interpolate(r, s, 10, 5), EPS);
        assertEquals(1975, LaunchSpeedTable.interpolate(r, s, 200, 5), EPS);
    }

    @Test
    public void theEndSlopeUsesTheTwoOutermostRowsWhateverTheirOrder() {
        double[] r = {80, 40, 60};
        double[] s = {1900, 1400, 1600};
        assertEquals(1370, LaunchSpeedTable.interpolate(r, s, 37, 5), EPS);
        assertEquals(1945, LaunchSpeedTable.interpolate(r, s, 83, 5), EPS);
    }

    @Test
    public void aRangeFarPastTheRowsIsNotBelieved() {
        double[] r = {39.0, 42.3, 50.5, 55.4};
        double[] s = {1520, 1560, 1570, 1635};
        assertTrue(LaunchSpeedTable.trusted(r, s, 60.4, 5, 10));
        assertTrue(LaunchSpeedTable.trusted(r, s, 70.4, 5, 10));
        assertFalse("4 Nov's bad pose", LaunchSpeedTable.trusted(r, s, 136, 5, 10));
        assertTrue(LaunchSpeedTable.trusted(r, s, 24, 5, 10));
        assertFalse(LaunchSpeedTable.trusted(r, s, 23, 5, 10));
        assertFalse(LaunchSpeedTable.trusted(r, s, NaN, 5, 10));
        assertFalse("no rows", LaunchSpeedTable.trusted(new double[] {NaN}, new double[] {NaN}, 50, 5, 10));
        assertTrue("NaN margins are 0: on a row", LaunchSpeedTable.trusted(r, s, 55.4, NaN, NaN));
        assertFalse("NaN margins are 0: past it", LaunchSpeedTable.trusted(r, s, 55.5, NaN, NaN));
    }

    @Test
    public void untrustedRangeFallsBackToTheConfiguredSpeed() {
        LaunchSpeedTable.enabled = true;
        LaunchSpeedTable.row1RangeIn = 40;
        LaunchSpeedTable.row1TicksPerSecond = 1400;
        LaunchSpeedTable.row2RangeIn = 60;
        LaunchSpeedTable.row2TicksPerSecond = 1600;
        LaunchSpeedTable.extendIn = 5;
        LaunchSpeedTable.trustBeyondIn = 10;
        assertEquals(1650, LaunchSpeedTable.launchSpeed(70, 1595), EPS);
        assertEquals(1595, LaunchSpeedTable.launchSpeed(136, 1595), EPS);
        assertTrue(Double.isNaN(LaunchSpeedTable.ticksPerSecondFor(136)));
    }

    @Test
    public void the4NovRowsAnswerAcrossTheSpotsAndALittleBeyond() {
        double[] r = {39.0, 42.3, 50.5, 55.4};
        double[] s = {1520, 1560, 1570, 1635};
        assertEquals(1560, LaunchSpeedTable.interpolate(r, s, 42.3, 5), EPS);
        assertEquals(1665, LaunchSpeedTable.interpolate(r, s, 57.66, 5), 1.0);
        assertEquals(1460, LaunchSpeedTable.interpolate(r, s, 30, 5), 1.0);
        assertEquals(1701, LaunchSpeedTable.interpolate(r, s, 70, 5), 1.0);
    }

    @Test
    public void rowsNeedNotBeInOrder() {
        double[] r = {80, 40, 60};
        double[] s = {1900, 1400, 1600};
        assertEquals(1750, at(r, s, 70), EPS);
        assertEquals(1500, at(r, s, 50), EPS);
    }

    @Test
    public void aNanOrBadRowIsNoRow() {
        // Row 2's NaN speed, row 3's negative range and row 4's zero speed are skipped: 40 to 80.
        double[] r = {40, 60, -5, 70, 80};
        double[] s = {1400, NaN, 1700, 0, 1800};
        assertEquals(1600, at(r, s, 60), EPS);
        assertEquals(1800, at(r, s, 100), EPS);
        assertFalse(LaunchSpeedTable.usable(NaN, 1500));
        assertFalse(LaunchSpeedTable.usable(50, Double.POSITIVE_INFINITY));
        assertTrue(LaunchSpeedTable.usable(0, 1500));
    }

    @Test
    public void oneRowAnswersForEveryRange() {
        double[] r = {NaN, 55, NaN};
        double[] s = {NaN, 1595, NaN};
        assertEquals(1595, at(r, s, 20), EPS);
        assertEquals(1595, at(r, s, 120), EPS);
    }

    @Test
    public void noRowOrNoRangeIsNanSoTheCallerKeepsItsSpeed() {
        double[] r = {NaN, NaN};
        double[] s = {NaN, NaN};
        assertTrue(Double.isNaN(at(r, s, 50)));
        assertTrue(Double.isNaN(at(new double[] {40}, new double[] {1400}, NaN)));
    }

    @Test
    public void twoRowsAtOneRangeTheFirstCounts() {
        double[] r = {50, 50, 70};
        double[] s = {1500, 1550, 1700};
        assertEquals(1500, at(r, s, 50), EPS);
        assertEquals(1600, at(r, s, 60), EPS);
    }

    @Test
    public void theConfiguredRowsAreTheTable() {
        LaunchSpeedTable.row1RangeIn = 60;
        LaunchSpeedTable.row1TicksPerSecond = 1600;
        LaunchSpeedTable.row4RangeIn = 40;
        LaunchSpeedTable.row4TicksPerSecond = 1400;
        assertEquals(2, LaunchSpeedTable.usableRows());
        assertEquals(1500, LaunchSpeedTable.ticksPerSecondFor(50), EPS);
    }

    @Test
    public void aSmallChangeHoldsTheSpeedInForceABigOneMovesIt() {
        assertEquals(1500, LaunchSpeedTable.held(1500, 1520, 30), EPS);
        assertEquals(1500, LaunchSpeedTable.held(1500, 1470, 30), EPS);
        assertEquals(1540, LaunchSpeedTable.held(1500, 1540, 30), EPS);
        assertEquals(1460, LaunchSpeedTable.held(1500, 1460, 30), EPS);
    }

    @Test
    public void noAnswerKeepsTheSpeedInForceAndTheFirstAnswerIsTaken() {
        assertEquals(1500, LaunchSpeedTable.held(1500, NaN, 30), EPS);
        assertTrue(Double.isNaN(LaunchSpeedTable.held(NaN, NaN, 30)));
        assertEquals(1520, LaunchSpeedTable.held(NaN, 1520, 30), EPS);
    }

    @Test
    public void aBadBandIsNoBand() {
        assertEquals(1501, LaunchSpeedTable.held(1500, 1501, NaN), EPS);
        assertEquals(1501, LaunchSpeedTable.held(1500, 1501, -10), EPS);
    }

    @Test
    public void offIsTheConfiguredSpeedWhateverTheRows() {
        LaunchSpeedTable.enabled = false;
        LaunchSpeedTable.row1RangeIn = 40;
        LaunchSpeedTable.row1TicksPerSecond = 1400;
        assertEquals(1595, LaunchSpeedTable.launchSpeed(40, 1595), EPS);
    }

    @Test
    public void onUsesTheTableAndFallsBackWhereItHasNoAnswer() {
        LaunchSpeedTable.enabled = true;
        assertEquals(1595, LaunchSpeedTable.launchSpeed(40, 1595), EPS);
        LaunchSpeedTable.row1RangeIn = 40;
        LaunchSpeedTable.row1TicksPerSecond = 1400;
        LaunchSpeedTable.row2RangeIn = 60;
        LaunchSpeedTable.row2TicksPerSecond = 1600;
        assertEquals(1500, LaunchSpeedTable.launchSpeed(50, 1595), EPS);
        assertEquals(1595, LaunchSpeedTable.launchSpeed(Double.NaN, 1595), EPS);
    }

    /** The configured rows, saved and cleared before each test and put back after, whatever they are. */
    private final java.util.Map<java.lang.reflect.Field, Double> saved = new java.util.HashMap<>();
    private boolean enabledBefore;

    @Before
    public void clearRows() throws Exception {
        enabledBefore = LaunchSpeedTable.enabled;
        LaunchSpeedTable.enabled = false;
        for (java.lang.reflect.Field f : LaunchSpeedTable.class.getFields()) {
            if (f.getType() != double.class) continue;
            saved.put(f, f.getDouble(null));
            f.setDouble(null, NaN);
        }
    }

    @After
    public void restoreRows() throws Exception {
        LaunchSpeedTable.enabled = enabledBefore;
        for (java.util.Map.Entry<java.lang.reflect.Field, Double> e : saved.entrySet()) {
            e.getKey().setDouble(null, e.getValue());
        }
    }
}
