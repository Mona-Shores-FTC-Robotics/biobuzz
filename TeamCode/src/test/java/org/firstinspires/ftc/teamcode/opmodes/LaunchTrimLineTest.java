package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.controls.Display;
import org.junit.Test;

/** Basic Drive's first Match-page line: the operator's launch trim, big, amber while trimmed. */
public class LaunchTrimLineTest {

    @Test
    public void noTrimIsShownAsZeroAndNotAmber() {
        String line = BasicDriveTeleOp.launchTrimLine(0.0, 1600);
        assertTrue(line, line.contains("LAUNCH TRIM 0 · 1600 t/s"));
        assertTrue(line, line.startsWith("<big><b>"));
        assertFalse(line, line.contains("<font"));
    }

    @Test
    public void aTrimIsSignedAmberAndShowsTheSpeedItGives() {
        String up = BasicDriveTeleOp.launchTrimLine(50, 1650);
        assertEquals(Display.alertWhat("LAUNCH TRIM +50 · 1650 t/s"), up);
        String down = BasicDriveTeleOp.launchTrimLine(-25, 1575);
        assertTrue(down, down.contains("LAUNCH TRIM -25 · 1575 t/s"));
    }

    @Test
    public void itFitsOneRow() {
        // Big text: one row is 1.25 ordinary rows; the line must not wrap onto a second.
        assertEquals(Display.BIG_SCALE, Display.rows(BasicDriveTeleOp.launchTrimLine(-200, 1450)), 1e-9);
    }
}
