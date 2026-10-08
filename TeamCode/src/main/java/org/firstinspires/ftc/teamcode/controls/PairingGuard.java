package org.firstinspires.ftc.teamcode.controls;

import java.util.function.BooleanSupplier;

/**
 * Hides the button presses a person makes while pairing a gamepad.
 *
 * <p>The Driver Station pairs a gamepad with Start + A (gamepad 1) or Start + B (gamepad 2). B is
 * also red alliance during INIT and a binding in some OpModes, so pairing used to change the
 * alliance or fire a binding without anyone meaning to (#171). A button pressed while its gamepad's
 * Start is held reads as released until it is let go. A button already held before Start is pressed
 * is not affected. Start itself is reserved for pairing: never bind it.
 *
 * <pre>{@code
 * PairingGuard pad1 = new PairingGuard(() -> gamepad1.start || gamepad1.options);
 * BooleanSupplier red = pad1.guard(() -> gamepad1.b);
 * }</pre>
 *
 * <p>Each guarded button keeps one bit of state, so make it once, in init, and read it every loop.
 */
public final class PairingGuard {

    private final BooleanSupplier start;

    /** {@code start}: whether this gamepad's Start (Options on a PS controller) is held. */
    public PairingGuard(BooleanSupplier start) {
        this.start = start;
    }

    /** {@code button}, reading as released from a press made with Start held until it is let go. */
    public BooleanSupplier guard(BooleanSupplier button) {
        return new Guarded(button, start);
    }

    private static final class Guarded implements BooleanSupplier {
        private final BooleanSupplier button;
        private final BooleanSupplier start;
        private boolean last;
        private boolean swallowed;

        Guarded(BooleanSupplier button, BooleanSupplier start) {
            this.button = button;
            this.start = start;
        }

        @Override
        public boolean getAsBoolean() {
            boolean down = button.getAsBoolean();
            if (down && !last && start.getAsBoolean()) {
                swallowed = true;
            }
            if (!down) {
                swallowed = false;
            }
            last = down;
            return down && !swallowed;
        }
    }
}
