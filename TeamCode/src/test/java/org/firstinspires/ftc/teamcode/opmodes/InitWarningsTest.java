package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.localization.StartCheck;
import org.firstinspires.ftc.teamcode.localization.StartPosition;
import org.firstinspires.ftc.teamcode.opmodes.InitWarnings.Kind;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

/** Each warning the mentor listed (2 Nov 2026), its action, and nothing when all is well. */
public class InitWarningsTest {

    private static final StartPosition RED = new StartPosition("Red test", Alliance.RED, new Pose(50, 10, Math.toRadians(90)));
    private static final StartCheck.Result ON_MARK = StartCheck.evaluate(RED, StartCheck.FIXES_TO_CONFIRM, 50.5, 10);
    private static final StartCheck.Result OFF_MARK = StartCheck.evaluate(RED, StartCheck.FIXES_TO_CONFIRM, 62, 10);

    private final InitWarnings w = new InitWarnings();

    /** All well, but for what each test changes. */
    private void evaluate(String pinpoint, Alliance alliance, Alliance camera, StartCheck.Result start,
                          String cameraProblem, long frames, long ms, double volts) {
        w.evaluate(pinpoint, alliance, camera, start, cameraProblem, frames, ms, volts, 12.0);
    }

    @Test
    public void allWellIsNoWarning() {
        evaluate(null, Alliance.RED, Alliance.RED, ON_MARK, null, 40, 20_000, 13.1);
        assertFalse(w.any());
        assertEquals("none", w.summary());
    }

    @Test
    public void aPinpointFaultSaysReseatTheCables() {
        evaluate("FAULT_Y_POD_NOT_DETECTED", Alliance.RED, Alliance.RED, ON_MARK, null, 40, 20_000, 13.1);
        assertTrue(w.has(Kind.PINPOINT));
        assertTrue(w.action(Kind.PINPOINT).contains("reseat the Pinpoint cables"));
    }

    @Test
    public void noAllianceSaysPressXOrB() {
        evaluate(null, Alliance.UNKNOWN, Alliance.UNKNOWN, null, null, 40, 20_000, 13.1);
        assertTrue(w.has(Kind.ALLIANCE));
        assertEquals("press X for blue, B for red", w.action(Kind.ALLIANCE));
    }

    @Test
    public void anAllianceTheCameraDisagreesWithSaysWhichWay() {
        evaluate(null, Alliance.BLUE, Alliance.RED, null, null, 40, 20_000, 13.1);
        assertEquals("BLUE, but camera sees RED start", w.what(Kind.ALLIANCE));
        assertTrue(w.action(Kind.ALLIANCE).startsWith("press B for red"));
    }

    @Test
    public void aCameraThatSeesNoStartIsNoDisagreement() {
        evaluate(null, Alliance.BLUE, Alliance.UNKNOWN, null, null, 40, 20_000, 13.1);
        assertFalse(w.any());
    }

    @Test
    public void offTheMarkSaysHowFarAndWhere() {
        evaluate(null, Alliance.RED, Alliance.RED, OFF_MARK, null, 40, 20_000, 13.1);
        assertEquals("12 in off the start mark", w.what(Kind.OFF_MARK));
        assertEquals("move robot to start position", w.action(Kind.OFF_MARK));
    }

    @Test
    public void anUnconfirmedStartIsNoWarning() {
        evaluate(null, Alliance.RED, Alliance.RED, StartCheck.evaluate(RED, 0, Double.NaN, Double.NaN),
                null, 40, 20_000, 13.1);
        assertFalse(w.any());
    }

    @Test
    public void aCameraNotAnsweringSaysReplug() {
        evaluate(null, Alliance.RED, Alliance.UNKNOWN, null, "not answering", 0, 1_000, 13.1);
        assertTrue(w.has(Kind.CAMERA_NOT_ANSWERING));
        assertEquals("unplug and replug the Limelight, wait 30 s", w.action(Kind.CAMERA_NOT_ANSWERING));
        assertFalse("one camera warning at a time", w.has(Kind.CAMERA_NO_FRAMES));
    }

    @Test
    public void noFramesWarnsOnlyAfterFifteenSeconds() {
        evaluate(null, Alliance.RED, Alliance.UNKNOWN, null, null, 0, InitWarnings.FIRST_FRAME_GRACE_MS - 1, 13.1);
        assertFalse(w.any());
        evaluate(null, Alliance.RED, Alliance.UNKNOWN, null, null, 0, InitWarnings.FIRST_FRAME_GRACE_MS, 13.1);
        assertTrue(w.has(Kind.CAMERA_NO_FRAMES));
    }

    @Test
    public void aLowBatterySaysSwapIt() {
        evaluate(null, Alliance.RED, Alliance.RED, ON_MARK, null, 40, 20_000, 11.8);
        assertEquals("Battery 11.8 V", w.what(Kind.BATTERY_LOW));
        assertEquals("swap to a charged battery", w.action(Kind.BATTERY_LOW));
        evaluate(null, Alliance.RED, Alliance.RED, ON_MARK, null, 40, 20_000, Double.NaN);
        assertFalse("no voltage sensor is no warning", w.any());
    }

    @Test
    public void severalAreAllListedMostSeriousFirst() {
        evaluate("missing", Alliance.UNKNOWN, Alliance.UNKNOWN, null, "not answering", 0, 20_000, 11.5);
        assertEquals(4, w.count());
        String summary = w.summary();
        assertTrue(summary.indexOf("Pinpoint") < summary.indexOf("No alliance"));
        assertTrue(summary.indexOf("No alliance") < summary.indexOf("Camera"));
        assertTrue(summary.indexOf("Camera") < summary.indexOf("Battery"));
    }

    @Test
    public void aSilentColorSensorSaysWhatChangesForTheDriveTeam() {
        w.evaluate(null, Alliance.RED, Alliance.RED, ON_MARK, null, 40, 20_000, 13.1, 12.0, true);
        assertTrue(w.has(Kind.COLOR_SENSOR));
        assertEquals("no full light or roller stop: watch the intake", w.action(Kind.COLOR_SENSOR));
        w.evaluate(null, Alliance.RED, Alliance.RED, ON_MARK, null, 40, 20_000, 13.1, 12.0, false);
        assertFalse(w.any());
    }

    @Test
    public void eachEvaluationStartsClean() {
        evaluate("missing", Alliance.UNKNOWN, Alliance.UNKNOWN, null, null, 40, 20_000, 11.5);
        evaluate(null, Alliance.RED, Alliance.RED, ON_MARK, null, 40, 20_000, 13.1);
        assertFalse(w.any());
        assertNull(w.what(Kind.PINPOINT));
    }
}
