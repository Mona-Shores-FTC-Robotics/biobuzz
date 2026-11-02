package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** The colour sensor's hue and saturation, the two numbers that do not depend on gain or distance. */
public class ColorSensorSubsystemTest {

    @Test
    public void primariesHaveTheirHues() {
        assertEquals(0.0, ColorSensorSubsystem.hueDegrees(1, 0, 0), 1e-9);
        assertEquals(120.0, ColorSensorSubsystem.hueDegrees(0, 1, 0), 1e-9);
        assertEquals(240.0, ColorSensorSubsystem.hueDegrees(0, 0, 1), 1e-9);
    }

    @Test
    public void inBetweenHues() {
        assertEquals(60.0, ColorSensorSubsystem.hueDegrees(1, 1, 0), 1e-9);   // yellow
        assertEquals(180.0, ColorSensorSubsystem.hueDegrees(0, 1, 1), 1e-9);  // cyan
        assertEquals(300.0, ColorSensorSubsystem.hueDegrees(1, 0, 1), 1e-9);  // magenta
        assertEquals(330.0, ColorSensorSubsystem.hueDegrees(1, 0, 0.5), 1e-9); // wraps, never negative
    }

    @Test
    public void hueIgnoresBrightness() {
        assertEquals(ColorSensorSubsystem.hueDegrees(0.8, 0.4, 0.1),
                ColorSensorSubsystem.hueDegrees(0.08, 0.04, 0.01), 1e-9);
    }

    @Test
    public void greyHasNoHue() {
        assertTrue(Double.isNaN(ColorSensorSubsystem.hueDegrees(0.3, 0.3, 0.3)));
        assertTrue(Double.isNaN(ColorSensorSubsystem.hueDegrees(0, 0, 0)));
    }

    @Test
    public void saturationRunsFromGreyToPure() {
        assertEquals(0.0, ColorSensorSubsystem.saturation(0.3, 0.3, 0.3), 1e-9);
        assertEquals(1.0, ColorSensorSubsystem.saturation(0.5, 0, 0), 1e-9);
        assertEquals(0.5, ColorSensorSubsystem.saturation(0.4, 0.2, 0.3), 1e-9);
        assertEquals(0.0, ColorSensorSubsystem.saturation(0, 0, 0), 1e-9);
    }
}
