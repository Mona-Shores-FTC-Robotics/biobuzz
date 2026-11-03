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

    /**
     * "Our last preload leaves at 8 s": the partner's last shot plus the TIP lag is when the wait gives up
     * (mentor, 3 Nov 2026).
     */
    @Test
    public void upAndDownMoveThePartnersLastShotAndTheWaitFollows() {
        double drawnShot = 8.5 - AutoRegistration.PARTNER_TIP_LAG_S;
        int presses = (int) Math.round((8 - drawnShot) / InitOptions.STEP_S);
        for (int i = 0; i < presses; i++) press(false, false, true, false);
        double shot = drawnShot + presses * InitOptions.STEP_S;
        assertEquals(shot + AutoRegistration.PARTNER_TIP_LAG_S, options.seconds("Wait for TIP or CELL up"), 1e-9);
        for (int i = 0; i < presses; i++) press(false, false, false, true);
        assertEquals("back to the drawing's", Double.NaN, options.seconds("Wait for TIP or CELL up"), 0);
    }

    @Test
    public void theLastShotNeverGoesBeforeTheMatchStarts() {
        for (int i = 0; i < 40; i++) press(false, false, false, true);
        assertEquals(AutoRegistration.PARTNER_TIP_LAG_S, options.seconds("Wait for TIP or CELL up"), 1e-9);
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
        // Both numbers, never only the sum: the drawn 8.5 s limit is a last shot at 4.5 s plus the 4 s lag.
        assertTrue(lines.toString(), lines.get(0).contains("partner's last shot ▲▼ 4.5 s, waiting until 8.5 s"));
        assertTrue(lines.toString(), lines.get(0).contains("#4CAF50"));
        assertTrue("one line, wrapped at most once", display.linesUsed() <= 2);

        lines.clear();
        display.header();
        press(false, true, true, false);
        options.describe(display);
        String line = lines.get(lines.size() - 1);
        assertTrue(line, line.contains("Park ◀▶: RIGHT_PARK"));
        assertTrue(line, line.contains("5.5 s, waiting until 9.5 s"));
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
        assertTrue(lines.toString(), lines.get(lines.size() - 1).contains("shot ▲▼ 4.5+4=8.5 s"));
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
