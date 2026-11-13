package org.firstinspires.ftc.teamcode.opmodes.auto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.autokit.AutoOption;
import org.firstinspires.ftc.teamcode.controls.Display;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/** The D-pad's picks in INIT, as {@code BuiltAuto} reads them and the generated Auto gets them. */
public class InitOptionsTest {

    private final InitOptions options = new InitOptions(
            AutoOption.park("LEFT_PARK", "RIGHT_PARK"),
            AutoOption.matchSeconds("Wait for TIP or CELL up", 8.5));

    private void press(boolean left, boolean right, boolean up, boolean down) {
        press(options, left, right, up, down);
    }

    private static void press(InitOptions menu, boolean left, boolean right, boolean up, boolean down) {
        menu.update(left, right, up, down);
        menu.update(false, false, false, false);
    }

    private static void left(InitOptions menu) { press(menu, true, false, false, false); }
    private static void right(InitOptions menu) { press(menu, false, true, false, false); }
    private static void up(InitOptions menu) { press(menu, false, false, true, false); }
    private static void down(InitOptions menu) { press(menu, false, false, false, true); }

    @Test
    public void untouchedEverythingIsAsDrawn() {
        assertEquals(-1, options.choice(AutoOption.PARK));
        assertEquals(Double.NaN, options.seconds("Wait for TIP or CELL up"), 0);
        assertEquals("the first row is selected", AutoOption.PARK, options.selected());
    }

    @Test
    public void leftAndRightChangeTheSelectedRow() {
        right(options);
        assertEquals(1, options.choice(AutoOption.PARK));
        assertEquals("the other row is untouched", Double.NaN, options.seconds("Wait for TIP or CELL up"), 0);
        left(options);
        assertEquals("back to the drawing's", -1, options.choice(AutoOption.PARK));
    }

    @Test
    public void upAndDownSelectARowAndWrap() {
        down(options);
        assertEquals("Wait for TIP or CELL up", options.selected());
        down(options);
        assertEquals("past the last row is the first", AutoOption.PARK, options.selected());
        up(options);
        assertEquals("Wait for TIP or CELL up", options.selected());
        assertEquals("selecting changes nothing", -1, options.choice(AutoOption.PARK));
        assertEquals(Double.NaN, options.seconds("Wait for TIP or CELL up"), 0);
    }

    @Test
    public void holdingAKeyIsOnePress() {
        down(options);
        for (int i = 0; i < 10; i++) options.update(false, true, false, false);
        assertEquals(8.5 + InitOptions.STEP_S, options.seconds("Wait for TIP or CELL up"), 1e-9);
    }

    /**
     * The row is the second the wait gives up, and left / right move it: one number, because a
     * partner rarely knows when its last preload leaves (mentor, 12 Nov 2026).
     */
    @Test
    public void thePartnerWaitsRowIsTheSecondTheWaitGivesUp() {
        down(options);
        for (int i = 0; i < 3; i++) right(options);
        assertEquals(8.5 + 3 * InitOptions.STEP_S, options.seconds("Wait for TIP or CELL up"), 1e-9);
        for (int i = 0; i < 3; i++) left(options);
        assertEquals("back to the drawing's", Double.NaN, options.seconds("Wait for TIP or CELL up"), 0);
    }

    @Test
    public void theWaitStaysBetweenZeroAndItsLatest() {
        down(options);
        for (int i = 0; i < 40; i++) left(options);
        assertEquals("0 does not wait", 0, options.seconds("Wait for TIP or CELL up"), 1e-9);
        for (int i = 0; i < 60; i++) right(options);
        assertEquals(InitOptions.LATEST_WAIT_S, options.seconds("Wait for TIP or CELL up"), 1e-9);
    }

    @Test
    public void lockedAtPlay() {
        options.lock();
        right(options);
        down(options);
        right(options);
        assertEquals(-1, options.choice(AutoOption.PARK));
        assertEquals(Double.NaN, options.seconds("Wait for TIP or CELL up"), 0);
        assertEquals(AutoOption.PARK, options.selected());
    }

    @Test
    public void namesWithoutASideStepThroughTheList() {
        InitOptions three = new InitOptions(AutoOption.choice("Park", "A", "B", "C"));
        right(three);
        assertEquals(1, three.choice("Park"));
        left(three);
        left(three);
        assertEquals("wraps round", 2, three.choice("Park"));
    }

    /** A paired Auto's menu: its pickup, the shot trigger and its times, each a row. */
    private static InitOptions pair() {
        return new InitOptions(
                AutoOption.choice("Pickup", "WALL_FLOWER", "GARDEN"),
                AutoOption.shotTrigger(),
                AutoOption.time("T2 LEFT up by", 18),
                AutoOption.time("T3 RIGHT up by", 22.3),
                AutoOption.partner("L-Clockwise", "Pickup", "GARDEN", "WALL_FLOWER"));
    }

    @Test
    public void aTimeMovesHalfASecondAPressAndStaysInTheMatch() {
        InitOptions menu = pair();
        down(menu);
        down(menu);
        assertEquals("T2 LEFT up by", menu.selected());
        right(menu);
        assertEquals(18 + InitOptions.TIME_STEP_S, menu.seconds("T2 LEFT up by"), 1e-9);
        left(menu);
        assertEquals("back to the drawing's", Double.NaN, menu.seconds("T2 LEFT up by"), 0);
        for (int i = 0; i < 100; i++) right(menu);
        assertEquals(30, menu.seconds("T2 LEFT up by"), 1e-9);
        for (int i = 0; i < 100; i++) left(menu);
        assertEquals(0, menu.seconds("T2 LEFT up by"), 1e-9);
        assertEquals("only the selected time moved", Double.NaN, menu.seconds("T3 RIGHT up by"), 0);
    }

    @Test
    public void thePartnerNoteIsNotARow() {
        InitOptions menu = pair();
        for (int i = 0; i < 4; i++) down(menu);
        assertEquals("four rows, then the first again", "Pickup", menu.selected());
    }

    /** The two robots' settings must match: the page says what the partner's menu has to show. */
    @Test
    public void thePageSaysWhatItAssumesOfThePartner() {
        InitOptions menu = pair();
        List<String> lines = new ArrayList<>();
        menu.describe(new Display(lines::add));
        assertTrue(lines.toString(), lines.get(0).contains("▶ Pickup ◀▶: WALL_FLOWER"));
        assertTrue(lines.toString(), lines.get(0).contains("▲▼ row 1 of 4"));
        assertTrue(lines.toString(), lines.get(1).contains("Partner runs L-Clockwise: Pickup GARDEN · Shot trigger CAMERA"));
        assertTrue(lines.toString(), lines.get(2).contains("T2 LEFT up by 18 s"));

        right(menu);
        down(menu);
        right(menu);
        assertEquals(1, menu.choice(AutoOption.SHOT_TRIGGER));
        lines.clear();
        menu.describe(new Display(lines::add));
        assertTrue(lines.toString(), lines.get(0).contains("▶ Shot trigger ◀▶: TIMER"));
        assertTrue(lines.toString(), lines.get(1).contains("Partner runs L-Clockwise: Pickup WALL_FLOWER · Shot trigger TIMER"));
        assertTrue("amber once changed", lines.get(0).contains("#FFB300"));
        assertTrue(menu.summary(), menu.summary().contains("Pickup=GARDEN (drawn WALL_FLOWER)"));
        assertTrue(menu.summary(), menu.summary().contains("T2 LEFT up by=18 s (drawn 18 s)"));
        assertTrue(menu.summary(), menu.summary().contains("Partner runs L-Clockwise: Pickup WALL_FLOWER"));
    }

    @Test
    public void theSelectedRowThenEveryRowAmberOnceChanged() {
        List<String> lines = new ArrayList<>();
        Display display = new Display(lines::add);
        options.describe(display);
        assertEquals(2, lines.size());
        assertTrue(lines.toString(), lines.get(0).contains("▶ Park ◀▶: LEFT_PARK"));
        assertTrue(lines.toString(), lines.get(0).contains("#4CAF50"));
        assertTrue(lines.toString(), lines.get(1).contains("Park LEFT_PARK · partner's TIP until 8.5 s"));
        assertTrue("two rows of the screen, wrapped at most once", display.linesUsed() <= 3);

        lines.clear();
        display.header();
        right(options);
        down(options);
        right(options);
        options.describe(display);
        String line = lines.get(lines.size() - 2);
        assertTrue(line, line.contains("▶ waiting for partner's TIP ◀▶ until 9.5 s"));
        assertTrue(line, line.contains("#FFB300"));
        assertTrue(lines.toString(), lines.get(lines.size() - 1).contains("<b>Park RIGHT_PARK</b>"));
        assertTrue(options.summary(), options.summary().contains("Park=RIGHT_PARK (drawn LEFT_PARK)"));
    }

    /** A full page keeps its warnings: the menu shortens to its selected row, then gives way (the picks still hold). */
    @Test
    public void aFullPageShortensThenLeavesOutTheOptions() {
        down(options);
        List<String> lines = new ArrayList<>();
        Display display = new Display(lines::add);
        for (int i = 0; i < 15; i++) display.line("warning " + i);
        options.describe(display);
        assertEquals(16, lines.size());
        assertTrue(lines.toString(), lines.get(lines.size() - 1).contains("TIP ◀▶ until 8.5 s"));
        assertTrue(display.linesUsed() <= Display.SCREEN_LINES);

        lines.clear();
        Display full = new Display(lines::add);
        for (int i = 0; i < 16; i++) full.line("warning " + i);
        options.describe(full);
        assertEquals(16, lines.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void twoOptionsOfOneNameAreRefused() {
        new InitOptions(AutoOption.time("a", 5), AutoOption.time("a", 6));
    }
}
