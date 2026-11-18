package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.localization.PinpointTrust;
import org.junit.Test;

/** Basic Drive's huge Match-page lines: the operator's launch trim (amber while trimmed) and the lane's pieces. */
public class LaunchTrimLineTest {

    @Test
    public void noTrimIsShownAsZeroHugeAndNotAmber() {
        String line = BasicDriveTeleOp.launchTrimLine(0.0);
        assertEquals(Display.huge("LAUNCH TRIM 0", false), line);
        assertTrue(line, line.startsWith("<big><big><big><b>"));
        assertFalse(line, line.contains("<font"));
    }

    @Test
    public void aTrimIsSignedGreenUpAndRedDown() {
        assertEquals(Display.huge("LAUNCH TRIM +50", Display.Level.OK), BasicDriveTeleOp.launchTrimLine(50));
        assertEquals(Display.huge("LAUNCH TRIM -25", Display.Level.FAULT), BasicDriveTeleOp.launchTrimLine(-25));
    }

    @Test
    public void aFractionalTrimShowsToATenth() {
        assertEquals(Display.huge("LAUNCH TRIM +12.5", Display.Level.OK), BasicDriveTeleOp.launchTrimLine(12.5));
        assertEquals(Display.huge("LAUNCH TRIM -15", Display.Level.FAULT), BasicDriveTeleOp.launchTrimLine(-15));
    }

    @Test
    public void holdingCircleMakesBothTrimsStepFiner() {
        assertEquals(25.0, BasicDriveTeleOp.trimStep(false, 25.0, 10.0, 200.0), 0.0);
        assertEquals(10.0, BasicDriveTeleOp.trimStep(true, 25.0, 10.0, 200.0), 0.0);
        assertEquals(1.5, BasicDriveTeleOp.trimStep(false, 1.5, 0.5, 5.0), 0.0);
        assertEquals(0.5, BasicDriveTeleOp.trimStep(true, 1.5, 0.5, 5.0), 0.0);
        // Guarded as the normal step: NaN or negative is no step, and never past the cap.
        assertEquals(0.0, BasicDriveTeleOp.trimStep(true, 25.0, Double.NaN, 200.0), 0.0);
        assertEquals(0.0, BasicDriveTeleOp.trimStep(true, 1.5, -1.0, 5.0), 0.0);
        assertEquals(5.0, BasicDriveTeleOp.trimStep(true, 1.5, 9.0, 5.0), 0.0);
    }

    @Test
    public void theLoopTimeIsWholeMilliseconds() {
        assertEquals(Display.huge("LOOP 21 ms", false), BasicDriveTeleOp.loopLine(20.6));
        assertEquals(Display.huge("LOOP -- ms", false), BasicDriveTeleOp.loopLine(Double.NaN));
    }

    @Test
    public void thePinpointLineSaysWhetherItIsWhyTheRobotDrivesOddly() {
        assertEquals(Display.huge("PINPOINT OK", Display.Level.OK),
                BasicDriveTeleOp.pinpointLine(true, PinpointTrust.State.TRUSTED, 0));
        assertEquals(Display.huge("PINPOINT 2 FAULTS", Display.Level.WARN),
                BasicDriveTeleOp.pinpointLine(true, PinpointTrust.State.TRUSTED, 2));
        assertEquals(Display.huge("PINPOINT LOST", Display.Level.FAULT),
                BasicDriveTeleOp.pinpointLine(true, PinpointTrust.State.FAULTED, 1));
        assertEquals(Display.huge("PINPOINT NONE", Display.Level.FAULT),
                BasicDriveTeleOp.pinpointLine(false, PinpointTrust.State.TRUSTED, 0));
        for (PinpointTrust.State state : PinpointTrust.State.values()) {
            assertEquals("one huge row", Display.HUGE_SCALE, Display.rows(BasicDriveTeleOp.pinpointLine(true, state, 12)), 1e-9);
        }
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

    @Test
    public void thePiecesLineIsTheCountHugeGreenWhileTheSensorSeesFull() {
        assertEquals(Display.huge("3 PIECES", (Display.Level) null), BasicDriveTeleOp.piecesLine(3, false));
        assertEquals(Display.huge("1 PIECE", (Display.Level) null), BasicDriveTeleOp.piecesLine(1, false));
        assertEquals(Display.huge("4 PIECES", Display.Level.OK), BasicDriveTeleOp.piecesLine(4, true));
        // A count the sensor disagrees with shows as it is, green: that is how the count gets better.
        assertEquals(Display.huge("3 PIECES", Display.Level.OK), BasicDriveTeleOp.piecesLine(3, true));
        assertEquals(Display.HUGE_SCALE, Display.rows(BasicDriveTeleOp.piecesLine(4, true)), 1e-9);
    }

    @Test
    public void theAimTrimIsNamedApartAndSaysWhichWay() {
        assertEquals(Display.huge("AIM TRIM 0", false), BasicDriveTeleOp.aimTrimLine(0.0));
        assertEquals(Display.huge("AIM TRIM L 3.0\u00b0", Display.Level.WARN), BasicDriveTeleOp.aimTrimLine(3.0));
        assertEquals(Display.huge("AIM TRIM R 1.5\u00b0", Display.Level.WARN), BasicDriveTeleOp.aimTrimLine(-1.5));
    }

    @Test
    public void anAimTrimPastFiveDegreesIsRed() {
        assertEquals(Display.huge("AIM TRIM L 5.0\u00b0", Display.Level.WARN), BasicDriveTeleOp.aimTrimLine(5.0));
        assertEquals(Display.huge("AIM TRIM L 6.0\u00b0", Display.Level.FAULT), BasicDriveTeleOp.aimTrimLine(6.0));
        assertEquals(Display.huge("AIM TRIM R 15.0\u00b0", Display.Level.FAULT), BasicDriveTeleOp.aimTrimLine(-15.0));
    }

    @Test
    public void trimsStopAtTheirCap() {
        // 18 Nov 2026: 20245's aim trim ran to +92.5°. Now a press past the cap stops at it.
        assertEquals(15.0, BasicDriveTeleOp.clampTrim(16.5, 15.0), 1e-9);
        assertEquals(-15.0, BasicDriveTeleOp.clampTrim(-92.5, 15.0), 1e-9);
        assertEquals(13.5, BasicDriveTeleOp.clampTrim(13.5, 15.0), 1e-9);
        assertEquals(150.0, BasicDriveTeleOp.clampTrim(175.0, 150.0), 1e-9);
        assertEquals(-150.0, BasicDriveTeleOp.clampTrim(-160.0, 150.0), 1e-9);
        assertEquals(0.0, BasicDriveTeleOp.clampTrim(1.5, 0.0), 1e-9);
    }

    @Test
    public void aTrimCapIsGuarded() {
        assertEquals(15.0, BasicDriveTeleOp.trimCap(15.0, 15.0, 45.0), 1e-9);
        assertEquals(15.0, BasicDriveTeleOp.trimCap(Double.NaN, 15.0, 45.0), 1e-9);
        assertEquals(15.0, BasicDriveTeleOp.trimCap(-3.0, 15.0, 45.0), 1e-9);
        assertEquals(45.0, BasicDriveTeleOp.trimCap(400.0, 15.0, 45.0), 1e-9);
        assertEquals(0.0, BasicDriveTeleOp.trimCap(0.0, 15.0, 45.0), 1e-9);
        assertEquals(150.0, BasicDriveTeleOp.trimCap(TeleOpConfig.maxLauncherTrimTps, 150.0, 500.0), 1e-9);
        assertEquals(180.0, BasicDriveTeleOp.trimCap(TeleOpConfig.maxAimTrimDeg, 180.0, 180.0), 1e-9);
    }

    @Test
    public void aRedAimTrimSaysTheZeroMayBeOff() {
        assertNull(BasicDriveTeleOp.aimTrimHint(0.0));
        assertNull(BasicDriveTeleOp.aimTrimHint(-5.0));
        assertNull(BasicDriveTeleOp.aimTrimHint(Double.NaN));
        assertEquals(Display.statusLine(Display.Level.FAULT, "turret zero off? hold D-pad up at home to re-zero"),
                BasicDriveTeleOp.aimTrimHint(60.0));
        assertEquals(BasicDriveTeleOp.aimTrimHint(60.0), BasicDriveTeleOp.aimTrimHint(-5.5));
    }

    @Test
    public void theInitPageSaysTheTurretWillTurnAtPlay() {
        assertEquals(null, RobotOpMode.turretOffsetNote(0.0));
        assertEquals("set it on its mark; at PLAY it turns 4.5\u00b0 left and holds (aim offset)",
                RobotOpMode.turretOffsetNote(4.5));
        assertEquals("set it on its mark; at PLAY it turns 2.0\u00b0 right and holds (aim offset)",
                RobotOpMode.turretOffsetNote(-2.0));
    }
}
