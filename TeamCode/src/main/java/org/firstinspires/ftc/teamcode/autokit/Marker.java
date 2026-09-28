package org.firstinspires.ftc.teamcode.autokit;

/** An action to start once the robot is a given fraction of the way along a path. */
public final class Marker {

    final double fraction;
    final String action;

    Marker(double fraction, String action) {
        if (!(fraction >= 0 && fraction <= 1)) {
            throw new IllegalArgumentException("Event " + action + " is at " + fraction + ", outside 0 to 1");
        }
        this.fraction = fraction;
        this.action = action;
    }
}
