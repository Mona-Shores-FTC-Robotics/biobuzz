package org.firstinspires.ftc.teamcode.controls;

/**
 * An intake button as a toggle instead of a hold: a press turns the intake on, the next press turns
 * it off. Only the bench uses it now ({@code LaunchTest}). Basic Drive dropped its toggle on 7 Nov
 * 2026: its intake runs all match ({@code TeleOpConfig.intakeAlwaysOn}), and 19429's operator
 * turned it off by accident 12 times in one run on 6 Nov.
 *
 * <p>Pure and allocation-free, called once a loop: it sees the buttons as held or not and works out
 * the presses itself. Switching the toggle off in Panels drops the latch, so the intake never stays
 * on by itself once hold-to-run is back.
 */
public final class IntakeToggle {

    private boolean on;
    private boolean lastPressed;

    /** Starts off. */
    public IntakeToggle() {
        this(false);
    }

    /** Starts {@code on} or off; switching the toggle off in Panels still drops it. */
    public IntakeToggle(boolean on) {
        this.on = on;
    }

    /**
     * One loop. {@code enabled}: the toggle is in use; {@code pressed} is either
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
