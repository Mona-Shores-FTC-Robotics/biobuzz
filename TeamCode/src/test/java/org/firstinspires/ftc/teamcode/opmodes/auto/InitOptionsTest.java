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
        options.update(left, right, up, down);
        options.update(false, false, false, false);
    }

    @Test
    public void untouchedEverythingIsAsDrawn() {
        assertEquals(-1, options.choice(AutoOption.PARK));
        assertEquals(Double.NaN, options.seconds("Wait for TIP or CELL up"), 0);
    }

    @Test
    public void leftAndRightPickThePark() {
        press(false, true, false, false);
        assertEquals(1, options.choice(AutoOption.PARK));
        press(true, false, false, false);
        assertEquals("back to the drawing's", -1, options.choice(AutoOption.PARK));
    }

    @Test
    public void holdingAKeyIsOnePress() {
        for (int i = 0; i < 10; i++) options.update(false, false, true, false);
        assertEquals(8.5 + InitOptions.STEP_S, options.seconds("Wait for TIP or CELL up"), 1e-9);
    }

    /** Up and down move the second the wait gives up, a second a press; back at the drawing's it is "as drawn". */
    @Test
    public void upAndDownMoveWhenTheWaitGivesUp() {
        for (int i = 0; i < 3; i++) press(false, false, true, false);
        assertEquals(8.5 + 3 * InitOptions.STEP_S, options.seconds("Wait for TIP or CELL up"), 1e-9);
        for (int i = 0; i < 3; i++) press(false, false, false, true);
        assertEquals("back to the drawing's", Double.NaN, options.seconds("Wait for TIP or CELL up"), 0);
    }

    /** "We don't shoot preloads": all the way down is 0 s, which does not wait at all. */
    @Test
    public void theWaitNeverGoesBeforeTheMatchStartsOrPastItsLatest() {
        for (int i = 0; i < 40; i++) press(false, false, false, true);
        assertEquals(0, options.seconds("Wait for TIP or CELL up"), 1e-9);
        for (int i = 0; i < 60; i++) press(false, false, true, false);
        assertEquals(InitOptions.LATEST_WAIT_S, options.seconds("Wait for TIP or CELL up"), 1e-9);
    }

    @Test
    public void lockedAtPlay() {
        options.lock();
        press(false, true, true, false);
        assertEquals(-1, options.choice(AutoOption.PARK));
        assertEquals(Double.NaN, options.seconds("Wait for TIP or CELL up"), 0);
    }

    @Test
    public void namesWithoutASideStepThroughTheList() {
        InitOptions three = new InitOptions(AutoOption.choice("Park", "A", "B", "C"));
        three.update(false, true, false, false);
        three.update(false, false, false, false);
        assertEquals(1, three.choice("Park"));
        three.update(true, false, false, false);
        three.update(false, false, false, false);
        three.update(true, false, false, false);
        assertEquals("wraps round", 2, three.choice("Park"));
    }

    @Test
    public void theOptionsAreOneLineAmberOnceChanged() {
        List<String> lines = new ArrayList<>();
        Display display = new Display(lines::add);
        options.describe(display);
        assertEquals(1, lines.size());
        assertTrue(lines.toString(), lines.get(0).contains("Park ◀▶: LEFT_PARK"));
        // One number a student can say: when the wait gives up.
        assertTrue(lines.toString(), lines.get(0).contains("waiting for partner's TIP ▲▼ until 8.5 s"));
        assertTrue(lines.toString(), lines.get(0).contains("#4CAF50"));
        assertTrue("one line, wrapped at most once", display.linesUsed() <= 2);

        lines.clear();
        display.header();
        press(false, true, true, false);
        options.describe(display);
        String line = lines.get(lines.size() - 1);
        assertTrue(line, line.contains("Park ◀▶: RIGHT_PARK"));
        assertTrue(line, line.contains("▲▼ until 9.5 s"));
        assertTrue(line, line.contains("#FFB300"));
        assertTrue(options.summary(), options.summary().contains("Park=RIGHT_PARK (drawn LEFT_PARK)"));
    }

    /** A full page keeps its warnings: the options shorten, then give way (the picks still hold). */
    @Test
    public void aFullPageShortensThenLeavesOutTheOptions() {
        List<String> lines = new ArrayList<>();
        Display display = new Display(lines::add);
        for (int i = 0; i < 15; i++) display.line("warning " + i);
        options.describe(display);
        assertTrue(lines.toString(), lines.get(lines.size() - 1).contains("TIP ▲▼ until 8.5 s"));
        assertTrue(display.linesUsed() <= Display.SCREEN_LINES);

        lines.clear();
        Display full = new Display(lines::add);
        for (int i = 0; i < 16; i++) full.line("warning " + i);
        options.describe(full);
        assertEquals(16, lines.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void twoAdjustableWaitsAreRefused() {
        new InitOptions(AutoOption.matchSeconds("a", 5), AutoOption.matchSeconds("b", 6));
    }
}
