package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class TurretEncoderTest {

    private static final double EPS = 1e-9;

    @Test
    public void oneEncoderTurnAtOneToOneIsOneTurretTurn() {
        TurretEncoder e = new TurretEncoder(1.0, 1);
        assertEquals(360.0, e.degrees(2048, 0), EPS);
        assertEquals(90.0, e.degrees(512, 0), EPS);
        assertEquals(360.0 / 2048.0, e.degreesPerCount(), EPS);
    }

    @Test
    public void theGearRatioScalesTheAngle() {
        // goBILDA turret kit: 64T drive gear on a 176T ring, 2.75 encoder turns per turret turn.
        TurretEncoder e = new TurretEncoder(2.75, 1);
        assertEquals(2048 * 2.75, e.countsPerTurretTurn(), EPS);
        assertEquals(360.0, e.degrees(5632, 0), EPS);
        assertEquals(360.0 / 5632.0, e.degreesPerCount(), EPS);
    }

    @Test
    public void theAngleIsMeasuredFromTheZeroCount() {
        TurretEncoder e = new TurretEncoder(1.0, 1);
        assertEquals(0.0, e.degrees(1000, 1000), EPS);
        assertEquals(90.0, e.degrees(1512, 1000), EPS);
        assertEquals(-90.0, e.degrees(488, 1000), EPS);
    }

    @Test
    public void aReversedEncoderStillReadsCounterClockwisePositive() {
        TurretEncoder e = new TurretEncoder(1.0, -1);
        assertEquals(-90.0, e.degrees(512, 0), EPS);
        assertEquals(90.0, e.degrees(-512, 0), EPS);
        assertEquals(-45.0, e.degreesPerSecond(256), EPS);
    }

    @Test
    public void theAngleIsNotWrappedSoWindingIsVisible() {
        TurretEncoder e = new TurretEncoder(1.0, 1);
        assertEquals(540.0, e.degrees(3072, 0), EPS);
        assertEquals(180.0, TurretEncoder.wrap180(e.degrees(3072, 0)), EPS);
    }

    @Test
    public void velocityConvertsTheSameWayAsPosition() {
        TurretEncoder e = new TurretEncoder(2.0, 1);
        // 4096 counts/s is one turret turn per second at 2:1.
        assertEquals(360.0, e.degreesPerSecond(4096), EPS);
    }

    @Test
    public void encoderTurnsAreReadStraightOffTheCount() {
        assertEquals(1.0, TurretEncoder.encoderTurns(2048, 0), EPS);
        assertEquals(-0.5, TurretEncoder.encoderTurns(0, 1024), EPS);
    }

    @Test
    public void wrapKeepsEveryAngleInMinus180To180() {
        assertEquals(180.0, TurretEncoder.wrap180(180.0), EPS);
        assertEquals(180.0, TurretEncoder.wrap180(-180.0), EPS);
        assertEquals(-90.0, TurretEncoder.wrap180(270.0), EPS);
        assertEquals(10.0, TurretEncoder.wrap180(730.0), EPS);
        assertEquals(-10.0, TurretEncoder.wrap180(-730.0), EPS);
        assertTrue(Double.isNaN(TurretEncoder.wrap180(Double.NaN)));
    }

    @Test
    public void badMeasuredValuesAreRefusedNotGuessed() {
        for (double ratio : new double[] {0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY}) {
            try {
                new TurretEncoder(ratio, 1);
                fail("ratio " + ratio + " should be refused");
            } catch (IllegalArgumentException expected) {
                // good
            }
        }
        for (int direction : new int[] {0, 2, -2}) {
            try {
                new TurretEncoder(1.0, direction);
                fail("direction " + direction + " should be refused");
            } catch (IllegalArgumentException expected) {
                // good
            }
        }
    }
}
