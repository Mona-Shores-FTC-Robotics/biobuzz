package org.firstinspires.ftc.teamcode.shooter;

/**
 * The three flywheel lanes, left to right as seen from behind the robot.
 *
 * <p>Ported from DECODE's {@code util/LauncherLane}, trimmed to what the speed
 * test rig needs — the burst-order and index helpers belong with the shot queue,
 * which this rig does not have.
 */
public enum FlywheelLane {
    LEFT("L"),
    CENTER("C"),
    RIGHT("R");

    /** One-character tag used to keep the telemetry table narrow. */
    public final String tag;

    FlywheelLane(String tag) {
        this.tag = tag;
    }
}
