package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.autokit.AutoOption;
import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.controls.MatchSetup;
import org.firstinspires.ftc.teamcode.localization.StartCheck;
import org.firstinspires.ftc.teamcode.localization.StartPositions;
import org.firstinspires.ftc.teamcode.opmodes.InitWarnings.Kind;
import org.firstinspires.ftc.teamcode.opmodes.auto.InitOptions;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * An Auto's INIT page fits one Driver Hub screen ({@link Display#SCREEN_LINES}) in its worst
 * cases: nobody at the field scrolls (mentor, 2 Nov 2026). The page is written in the order
 * {@code RobotOpMode} writes it: header, top line and warnings, the alliance line unless a warning
 * covers it, the start, the Auto's INIT options ({@code BuiltAuto}: L-Partner's two, the most any
 * Auto has), and the map only if it helps and fits.
 */
public class InitPageFitsTest {

    private final List<String> lines = new ArrayList<>();
    private final Display display = new Display(lines::add);
    private final InitWarnings warnings = new InitWarnings();
    private final MatchSetup setup = new MatchSetup();
    private final InitOptions options = new InitOptions(
            AutoOption.park("LEFT_PARK", "RIGHT_PARK"),
            AutoOption.matchSeconds("Wait for TIP or CELL up", 8));

    private static final StartCheck.Result ON_MARK =
            StartCheck.evaluate(StartPositions.RED_AUDIENCE, StartCheck.FIXES_TO_CONFIRM, 55.9, 7.7);
    private static final StartCheck.Result OFF_MARK =
            StartCheck.evaluate(StartPositions.RED_AUDIENCE, StartCheck.FIXES_TO_CONFIRM, 67.6, 7.7);
    private static final StartCheck.Result UNCONFIRMED =
            StartCheck.evaluate(StartPositions.RED_AUDIENCE, 0, Double.NaN, Double.NaN);

    /** As RobotOpMode's INIT loop writes an Auto's Match page. */
    private void writePage(StartCheck.Result start) {
        display.header();
        warnings.describeTop(display, setup.alliance(), true);
        if (!warnings.has(Kind.ALLIANCE)) setup.describe(display);
        start.describe(display);
        options.describe(display);
        if (start.mapHelps(setup.alliance()) && display.room(StartCheck.Result.MAP_LINES)) {
            start.describeMap(display, setup.alliance());
        }
    }

    private void assertFits() {
        assertTrue(display.linesUsed() + " rows on a " + Display.SCREEN_LINES + "-row screen:\n"
                        + String.join("\n", lines),
                display.linesUsed() <= Display.SCREEN_LINES);
    }

    @Test
    public void fourWarningsWithTheCameraWorkingFit() {
        setup.offerVision(Alliance.RED, "the camera puts the robot on the RED start");
        setup.chooseManually(Alliance.BLUE);
        warnings.evaluate("y pod not detected", setup.alliance(), setup.visionAlliance(), OFF_MARK,
                null, 40, 20_000, 11.8, 12.0);
        assertEquals(4, warnings.count());
        writePage(OFF_MARK);
        assertFits();
    }

    @Test
    public void fourWarningsWithTheCameraDownFit() {
        warnings.evaluate("not found", setup.alliance(), setup.visionAlliance(), UNCONFIRMED,
                "not answering", 0, 20_000, 11.8, 12.0);
        assertEquals(4, warnings.count());
        writePage(UNCONFIRMED);
        assertFits();
    }

    /**
     * Five at once: the camera stops answering after its fixes put the robot off the mark (the
     * fixes outlive the camera), with a stale alliance disagreement too (review on #353).
     */
    @Test
    public void fiveWarningsFitWithTheStartStillShowing() {
        setup.offerVision(Alliance.RED, "the camera puts the robot on the RED start");
        setup.chooseManually(Alliance.BLUE);
        warnings.evaluate("y pod not detected", setup.alliance(), setup.visionAlliance(), OFF_MARK,
                "not answering", 0, 20_000, 11.8, 12.0);
        assertEquals(5, warnings.count());
        writePage(OFF_MARK);
        assertFits();
        assertTrue("every warning named", String.join("\n", lines).contains("Battery 11.8 V"));
        assertTrue("the start line still shows", String.join("\n", lines).contains("Start: "));
    }

    /** Six, with the colour sensor silent too: still one screen, the start still showing. */
    @Test
    public void sixWarningsFitWithTheStartStillShowing() {
        setup.offerVision(Alliance.RED, "the camera puts the robot on the RED start");
        setup.chooseManually(Alliance.BLUE);
        warnings.evaluate("y pod not detected", setup.alliance(), setup.visionAlliance(), OFF_MARK,
                "not answering", 0, 20_000, 11.8, 12.0, true);
        assertEquals(6, warnings.count());
        writePage(OFF_MARK);
        assertFits();
        assertTrue("the count says six", String.join("\n", lines).contains("6 TO FIX"));
        assertTrue("the start line still shows", String.join("\n", lines).contains("Start: "));
    }

    @Test
    public void readyAndUnconfirmedShowsTheMap() {
        setup.offerVision(Alliance.RED, "the camera puts the robot on the RED start");
        warnings.evaluate(null, setup.alliance(), setup.visionAlliance(), UNCONFIRMED,
                null, 40, 20_000, 13.0, 12.0);
        assertFalse(warnings.any());
        writePage(UNCONFIRMED);
        assertFits();
        assertTrue("the map is there", String.join("\n", lines).contains("your wall (red)"));
    }

    @Test
    public void confirmedDropsTheMap() {
        setup.offerVision(Alliance.RED, "the camera puts the robot on the RED start");
        warnings.evaluate(null, setup.alliance(), setup.visionAlliance(), ON_MARK,
                null, 40, 20_000, 13.0, 12.0);
        writePage(ON_MARK);
        assertFalse(String.join("\n", lines).contains("<tt>"));
    }

    @Test
    public void noAllianceShowsNoMap() {
        warnings.evaluate(null, setup.alliance(), setup.visionAlliance(), UNCONFIRMED,
                null, 40, 20_000, 13.0, 12.0);
        writePage(UNCONFIRMED);
        assertFalse("never red by default", String.join("\n", lines).contains("<tt>"));
    }

    @Test
    public void bigTextIsTallerAndWrapsSooner() {
        assertEquals(1.0, Display.rows("short"), 1e-9);
        assertEquals(Display.BIG_SCALE, Display.rows("<big>short</big>"), 1e-9);
        StringBuilder long60 = new StringBuilder();
        for (int i = 0; i < 60; i++) long60.append('x');
        assertEquals(2.0, Display.rows(long60.toString()), 1e-9);
        assertEquals(2 * Display.BIG_SCALE, Display.rows("<big>" + long60 + "</big>"), 1e-9);
    }

    /** The worst case and an unassigned gamepad: seven warnings, still one screen (7 Nov 2026). */
    @Test
    public void sixWarningsAndAnUnassignedGamepadFit() {
        setup.offerVision(Alliance.RED, "the camera puts the robot on the RED start");
        setup.chooseManually(Alliance.BLUE);
        warnings.evaluate("y pod not detected", setup.alliance(), setup.visionAlliance(), OFF_MARK,
                "not answering", 0, 20_000, 11.8, 12.0, true);
        warnings.gamepads(false, false);
        assertEquals(7, warnings.count());
        writePage(OFF_MARK);
        assertFits();
        assertTrue("the gamepads are named", String.join("\n", lines).contains("Gamepads 1 and 2 not assigned"));
    }
}
