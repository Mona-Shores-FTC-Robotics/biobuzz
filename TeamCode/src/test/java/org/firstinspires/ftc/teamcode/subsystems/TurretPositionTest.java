package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** {@link TurretSubsystem#positionFor}: the servo position for a turret angle. */
public class TurretPositionTest {

    private static final double EPS = 1e-9;

    @Test
    public void homeIsHomeWithNoScaleMeasured() {
        assertEquals(0.5, TurretSubsystem.positionFor(0.0, 0.5, Double.NaN, 0.0, 1.0), EPS);
    }

    @Test
    public void anotherAngleNeedsTheScale() {
        assertTrue(Double.isNaN(TurretSubsystem.positionFor(30.0, 0.5, Double.NaN, 0.0, 1.0)));
        assertTrue(Double.isNaN(TurretSubsystem.positionFor(30.0, 0.5, 0.0, 0.0, 1.0)));
        assertTrue(Double.isNaN(TurretSubsystem.positionFor(30.0, 0.5, Double.POSITIVE_INFINITY, 0.0, 1.0)));
    }

    @Test
    public void theScaleAndItsSignSetThePosition() {
        assertEquals(0.6, TurretSubsystem.positionFor(30.0, 0.5, 300.0, 0.0, 1.0), EPS);
        assertEquals(0.4, TurretSubsystem.positionFor(30.0, 0.5, -300.0, 0.0, 1.0), EPS);
    }

    @Test
    public void farAnglesStopAtTheServosEnds() {
        assertEquals(1.0, TurretSubsystem.positionFor(500.0, 0.5, 300.0, 0.0, 1.0), EPS);
        assertEquals(0.0, TurretSubsystem.positionFor(-500.0, 0.5, 300.0, 0.0, 1.0), EPS);
    }

    @Test
    public void aBadHomeGoesLimpNotToAnEnd() {
        assertTrue(Double.isNaN(TurretSubsystem.positionFor(0.0, Double.NaN, 300.0, 0.0, 1.0)));
        assertTrue(Double.isNaN(TurretSubsystem.positionFor(0.0, 1.5, 300.0, 0.0, 1.0)));
        assertTrue(Double.isNaN(TurretSubsystem.positionFor(0.0, -0.1, 300.0, 0.0, 1.0)));
    }

    @Test
    public void theLimitsClampAnAimAndRefuseAHomeOutside() {
        assertEquals(0.75, TurretSubsystem.positionFor(-500.0, 0.514, -150.0, 0.25, 0.75), EPS);
        assertEquals(0.25, TurretSubsystem.positionFor(500.0, 0.514, -150.0, 0.25, 0.75), EPS);
        assertTrue(Double.isNaN(TurretSubsystem.positionFor(0.0, 0.2, -150.0, 0.25, 0.75)));
        assertTrue(Double.isNaN(TurretSubsystem.positionFor(0.0, 0.5, -150.0, 0.75, 0.25)));
    }
}
