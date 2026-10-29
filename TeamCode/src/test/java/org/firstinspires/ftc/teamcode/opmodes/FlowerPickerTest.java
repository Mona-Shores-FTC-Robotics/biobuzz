package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** The pick's geometry: how far along the line into a FLOWER, how far off it, and the guards. */
public class FlowerPickerTest {

    private static final double EPS = 1e-9;

    /** Facing +y at the contact point (10, 20): a robot 5 in short is -5 along, 3 in beyond is +3. */
    @Test
    public void alongTrackIsNegativeBeforeTheContactPoint() {
        double h = Math.PI / 2;
        assertEquals(-5.0, FlowerPicker.alongTrackIn(10, 15, 10, 20, h), EPS);
        assertEquals(3.0, FlowerPicker.alongTrackIn(10, 23, 10, 20, h), EPS);
        assertEquals("sideways does not count as along", -5.0, FlowerPicker.alongTrackIn(14, 15, 10, 20, h), EPS);
    }

    /** Facing +y, the robot's left is -x: a robot at x 8 is 2 in left of the line through x 10. */
    @Test
    public void lateralIsPositiveToTheLeft() {
        double h = Math.PI / 2;
        assertEquals(2.0, FlowerPicker.lateralIn(8, 15, 10, 20, h), EPS);
        assertEquals(-2.0, FlowerPicker.lateralIn(12, 15, 10, 20, h), EPS);
        assertEquals(0.0, FlowerPicker.lateralIn(10, 5, 10, 20, h), EPS);
    }

    /** Facing -x (the red WALL FLOWER's way): +y is to the robot's right. */
    @Test
    public void lateralFollowsTheHeading() {
        assertEquals(-3.0, FlowerPicker.lateralIn(20, 50, 15, 47, Math.PI), EPS);
    }

    @Test
    public void wrapTakesTheShortWayRound() {
        assertEquals(-Math.PI / 2, FlowerPicker.wrapRad(3 * Math.PI / 2), EPS);
        assertEquals(Math.PI, FlowerPicker.wrapRad(-Math.PI), EPS);
        assertEquals(0.1, FlowerPicker.wrapRad(0.1 + 4 * Math.PI), 1e-9);
    }

    @Test
    public void clampGuardsPanelsTypos() {
        assertEquals(0.25, FlowerPicker.clamp(3.0, 0.25), EPS);
        assertEquals(-0.25, FlowerPicker.clamp(-3.0, 0.25), EPS);
        assertEquals(0.1, FlowerPicker.clamp(0.1, 0.25), EPS);
        assertEquals("NaN asks for nothing", 0.0, FlowerPicker.clamp(Double.NaN, 0.25), EPS);
        assertEquals("a bad limit asks for nothing", 0.0, FlowerPicker.clamp(0.1, Double.NaN), EPS);
        assertEquals(0.0, FlowerPicker.clamp(0.1, -1.0), EPS);
    }

    @Test
    public void negativeAndNanDistancesAndTimesReadAsZero() {
        assertEquals(0.0, FlowerPicker.nn(-4.0), EPS);
        assertEquals(0.0, FlowerPicker.nn(Double.NaN), EPS);
        assertEquals(12.0, FlowerPicker.nn(12.0), EPS);
    }
}
