package org.firstinspires.ftc.teamcode.util;

/**
 * Keeps every mecanum wheel's power at or under a cap, by scaling the whole command down
 * together, so the robot keeps going the way it was told, only slower. This is how a TeleOp speed
 * mode is a fraction of full power in every direction.
 *
 * <p>Why it exists: Pedro's {@code Mecanum} divides all four wheels by the largest one only when
 * that one is over 1. A mode that scales the sticks by 0.8 therefore gets 0.8 only driving
 * straight along a robot axis. Driving at 45° to one (field-centric, so any stick direction once
 * the robot has turned), or turning while driving, the wheel sum goes past 1, Pedro normalizes it
 * to 1, and the mode is full power, the same as turbo. Measured in the 2 and 3 Nov 2026 Basic Drive
 * logs on 20245: with the stick held at its rim in normal mode (0.8), the wheels were already
 * normalized in about half the samples and got 0.94 of full power on average, so turbo added
 * about 6% power and 5% speed (wheels 1660 against 1740 ticks/s, 36 against 39 in/s).
 *
 * <p>Capping at the mode's scale after the field-centric turn gives exactly turbo's command times
 * the scale, in every direction and with any turn: for a command {@code c} and scale {@code k},
 * {@code k c / max(1, |k c|/k) = k (c / max(1, |c|))}, where {@code |c|} is the largest wheel.
 * A cap of 1 changes nothing: it is Pedro's own normalization.
 *
 * <p>Plain Java with no SDK types, so it is unit tested.
 */
public final class WheelPowerCap {

    private WheelPowerCap() {
    }

    /**
     * The largest wheel power a mecanum drive gives this robot-frame command, before any
     * normalization: {@code |forward| + |strafe| + |turn|}, since the four wheels between them have
     * every sign combination of the three ({@code f - s - t}, {@code f + s + t}, {@code f + s - t},
     * {@code f - s + t}, as Pedro mixes them).
     */
    public static double largestWheel(double forward, double strafe, double turn) {
        return Math.abs(forward) + Math.abs(strafe) + Math.abs(turn);
    }

    /**
     * What to multiply the robot-frame command by so no wheel goes over {@code cap}: 1 when none
     * would, else {@code cap / largestWheel}. The cap is clamped to [0, 1], and NaN counts as 0, so
     * a bad value typed into Panels gives a robot that stops, never one past full power. A NaN
     * command is passed through untouched (factor 1) for the drivetrain to deal with as before.
     */
    public static double factor(double forward, double strafe, double turn, double cap) {
        double limit = cap > 0.0 ? Math.min(cap, 1.0) : 0.0;
        double largest = largestWheel(forward, strafe, turn);
        return largest > limit ? limit / largest : 1.0;
    }
}
