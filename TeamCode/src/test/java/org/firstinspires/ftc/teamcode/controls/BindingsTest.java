package org.firstinspires.ftc.teamcode.controls;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.ivy.commands.Commands;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

public class BindingsTest {

    private final Bindings bindings = new Bindings("DRIVER");
    private boolean pressed;

    @Before
    @After
    public void resetScheduler() {
        Scheduler.reset();
    }

    @Test
    public void labelsListEveryBindingAndNoteInOrder() {
        bindings.note("Left stick", "Drive");
        bindings.when("Y", "Reset heading", () -> false);

        assertEquals(Arrays.asList("Left stick — Drive", "Y — Reset heading"), bindings.labels());
    }

    @Test
    public void onPressFiresOncePerPressNotWhileHeld() {
        int[] count = {0};
        bindings.when("Y", "Count", () -> pressed).onPress(() -> count[0]++);

        poll(false, true, true, true, false, true);

        assertEquals(2, count[0]);
    }

    @Test
    public void whileHeldRunsTheCommandOnlyWhileHeld() {
        Command command = Commands.infinite(() -> { });
        bindings.when("A", "Hold", () -> pressed).whileHeld(command);

        poll(true);
        assertTrue(command.isScheduled());

        poll(false);
        assertFalse(command.isScheduled());
    }

    @Test
    public void aButtonHeldThroughPlayIsNotAFreshPress() {
        int[] count = {0};
        bindings.when("B", "Count", () -> pressed).onPress(() -> count[0]++);

        pressed = true;
        bindings.prime();
        poll(true, true);
        assertEquals(0, count[0]);

        poll(false, true);
        assertEquals(1, count[0]);
    }

    @Test
    public void aPressMadeWhilePairingFiresNothing() {
        boolean[] start = {false};
        Bindings guarded = new Bindings("OPERATOR", () -> start[0]);
        int[] count = {0};
        guarded.when("B", "Count", () -> pressed).onPress(() -> count[0]++);

        start[0] = true;
        for (boolean state : new boolean[] {false, true, true}) {
            pressed = state;
            guarded.update();
        }
        start[0] = false;
        pressed = false;
        guarded.update();
        assertEquals(0, count[0]);

        pressed = true;
        guarded.update();
        assertEquals(1, count[0]);
    }

    @Test
    public void nothingFiresWithoutAPoll() {
        int[] count = {0};
        bindings.when("Y", "Count", () -> true).onPress(() -> count[0]++);

        assertEquals(0, count[0]);
    }

    private void poll(boolean... states) {
        for (boolean state : states) {
            pressed = state;
            bindings.update();
            Scheduler.execute();
        }
    }
}
