package org.firstinspires.ftc.teamcode.controls;

/**
 * The TeleOp's intake buttons as a toggle instead of a hold, for one person testing alone
 * ({@code TeleOpConfig.intakeToggle}): a press of either intake button turns the intake on, the
 * next press turns it off. Off by default, so a match keeps hold-to-run, where letting go stops it.
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
