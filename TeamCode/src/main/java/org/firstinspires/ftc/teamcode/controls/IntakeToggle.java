package org.firstinspires.ftc.teamcode.controls;

/**
 * The TeleOp's intake buttons as a toggle instead of a hold ({@code TeleOpConfig.intakeToggle},
 * on by default): a press of either intake button turns the intake on, the next press turns it off,
 * one latch for both pads. Left on, the intake stops its roller and the prefeed by itself when the
 * lane is full and starts them again when a shot makes room.
 *
 * <p>Pure and allocation-free, called once a loop: it sees the buttons as held or not and works out
 * the presses itself. Switching the toggle off in Panels drops the latch, so the intake never stays
 * on by itself once hold-to-run is back.
 */
public final class IntakeToggle {

    private boolean on;
    private boolean lastPressed;

    /**
     * One loop. {@code enabled} is {@code TeleOpConfig.intakeToggle}; {@code pressed} is either
     * intake button held. Returns whether the intake should run in.
     */
    public boolean update(boolean enabled, boolean pressed) {
        boolean press = pressed && !lastPressed;
        lastPressed = pressed;
        if (!enabled) {
            on = false;
            return false;
        }
        if (press) on = !on;
        return on;
    }

    /** Whether the toggle has the intake on. */
    public boolean on() {
        return on;
    }
}
