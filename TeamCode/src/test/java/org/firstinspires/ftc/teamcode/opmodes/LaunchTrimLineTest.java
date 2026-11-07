package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.controls.Display;
import org.junit.Test;

/** Basic Drive's first Match-page line: the operator's launch trim, huge, amber while trimmed. */
public class LaunchTrimLineTest {

    @Test
    public void noTrimIsShownAsZeroHugeAndNotAmber() {
        String line = BasicDriveTeleOp.launchTrimLine(0.0);
        assertEquals(Display.huge("TRIM 0", false), line);
        assertTrue(line, line.startsWith("<big><big><big><b>"));
        assertFalse(line, line.contains("<font"));
    }

    @Test
    public void aTrimIsSignedAndAmber() {
        assertEquals(Display.huge("TRIM +50", true), BasicDriveTeleOp.launchTrimLine(50));
        assertEquals(Display.huge("TRIM -25", true), BasicDriveTeleOp.launchTrimLine(-25));
    }

    @Test
    public void itFitsOneHugeRow() {
        // Huge text: one row is HUGE_SCALE ordinary rows; the line must not wrap onto a second.
        assertEquals(Display.HUGE_SCALE, Display.rows(BasicDriveTeleOp.launchTrimLine(-200)), 1e-9);
    }

    @Test
    public void theSpeedLineUnderItIsOneOrdinaryRow() {
        String line = BasicDriveTeleOp.launchSpeedLine(1650);
        assertEquals("launch speed 1650 t/s", line);
        assertEquals(1.0, Display.rows(line), 1e-9);
    }
}
