package org.firstinspires.ftc.teamcode.controls;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

import java.util.function.BooleanSupplier;

/** #171: pairing a gamepad (Start + A, Start + B) must not read as a press of A or B. */
public class PairingGuardTest {

    private boolean start;
    private boolean button;
    private final BooleanSupplier guarded = new PairingGuard(() -> start).guard(() -> button);

    @Test
    public void aNormalPressReadsThrough() {
        assertArrayEquals(new boolean[] {false, true, true, false},
                run(new boolean[] {false, false, false, false}, new boolean[] {false, true, true, false}));
    }

    @Test
    public void startThenBIsNeverAPress() {
        // Start, then B with it (pairing), then both let go.
        assertArrayEquals(new boolean[] {false, false, false, false},
                run(new boolean[] {true, true, true, false}, new boolean[] {false, true, true, false}));
    }

    @Test
    public void startLetGoBeforeBStaysSwallowedUntilBIsReleased() {
        // The usual pairing slip: Start comes up a moment before B.
        assertArrayEquals(new boolean[] {false, false, false, false, true},
                run(new boolean[] {true, true, false, false, false}, new boolean[] {false, true, true, false, true}));
    }

    @Test
    public void aButtonHeldBeforeStartIsUnaffected() {
        assertArrayEquals(new boolean[] {true, true, true},
                run(new boolean[] {false, true, false}, new boolean[] {true, true, true}));
    }

    private boolean[] run(boolean[] starts, boolean[] buttons) {
        boolean[] out = new boolean[buttons.length];
        for (int i = 0; i < buttons.length; i++) {
            start = starts[i];
            button = buttons[i];
            out[i] = guarded.getAsBoolean();
        }
        return out;
    }
}
