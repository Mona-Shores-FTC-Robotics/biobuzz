package org.firstinspires.ftc.teamcode.localization;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.util.Alliance;

/**
 * A legal starting position: where an Autonomous expects the robot to be placed. An Autonomous
 * declares one by overriding {@code RobotOpMode.startPosition()}; the Pedro paths start from its
 * pose, and the start check compares the camera's view against it during INIT.
 *
 * <p>Declare them in {@link StartPositions}, measured from the field — never guessed.
 */
public final class StartPosition {

    public final String name;
    public final Alliance alliance;
    /** Pedro field frame: inches, radians. */
    public final Pose pose;

    public StartPosition(String name, Alliance alliance, Pose pose) {
        this.name = name;
        this.alliance = alliance;
        this.pose = pose;
    }

    @Override
    public String toString() {
        return name;
    }
}
