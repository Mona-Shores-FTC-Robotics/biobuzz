package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** The chassis aim keeps turning onto the CELL: a floor outside the stop band, nothing inside it. */
public class ChassisAimTurnTest {

    private static final double EPS = 1e-9;

    @Test
    public void aSmallErrorStillTurnsAtTheFloor() {
        // 5° at kP 1.0 is 0.087, which did not rotate 19429: the floor lifts it.
        assertEquals(0.2, ChassisAimTurn.power(5.0, 1.0, 0.2, 0.5, 1.5), EPS);
        assertEquals("the other way too", -0.2, ChassisAimTurn.power(-5.0, 1.0, 0.2, 0.5, 1.5), EPS);
    }

    @Test
    public void insideTheStopBandItAsksForNothing() {
        assertEquals(0.0, ChassisAimTurn.power(1.0, 1.0, 0.2, 0.5, 1.5), EPS);
        assertEquals(0.0, ChassisAimTurn.power(Double.NaN, 1.0, 0.2, 0.5, 1.5), EPS);
    }

    @Test
    public void aBigErrorIsProportionalThenCapped() {
        assertEquals(Math.toRadians(20.0), ChassisAimTurn.power(20.0, 1.0, 0.2, 0.5, 1.5), EPS);
        assertEquals(0.5, ChassisAimTurn.power(60.0, 1.0, 0.2, 0.5, 1.5), EPS);
        assertEquals("the floor never exceeds the cap", 0.3, ChassisAimTurn.power(5.0, 1.0, 0.4, 0.3, 1.5), EPS);
    }

    @Test
    public void noFloorIsTheOldLaw() {
        assertEquals(Math.toRadians(5.0), ChassisAimTurn.power(5.0, 1.0, 0.0, 0.5, 0.0), EPS);
    }
}
