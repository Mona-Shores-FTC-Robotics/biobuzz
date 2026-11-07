package org.firstinspires.ftc.teamcode.controls;

/**
 * Everything a person can ask the robot to do in TeleOp, by name. The OpMode asks
 * "is {@link #FIRE} held?", never "is R2 held?"; which input that is lives in one place,
 * {@link ControlScheme}, and so does the text the drivers read for it, on the Driver Station's
 * Controls page and on the printed cheat sheet. A description starting with {@code *} is work in
 * progress: the sheet footnotes it.
 */
public enum Action {
    // Driver
    DRIVE(Pad.DRIVER, "Drive"),
    TURN(Pad.DRIVER, "Turn"),
    INTAKE(Pad.DRIVER, "Intake (hold, over the toggle)"),
    DRIVER_UNJAM(Pad.DRIVER, "Unjam (hold)"),
    SLOW(Pad.DRIVER, "Slow (hold)"),
    FLOWER_PICK(Pad.DRIVER, "Pick FLOWER (hold)"),
    GARDEN_GATHER(Pad.DRIVER, "*Gather GARDEN (hold)"),
    CHASSIS_AIM(Pad.DRIVER, "Aim Chassis (hold)"),
    LAUNCH_SPOT(Pad.DRIVER, "*Go to Launch Spot (hold)"),
    RESET_HEADING(Pad.DRIVER, "Gyro Reset (hold 0.5\u00a0s)"),
    RESET_LEFT_CORNER(Pad.DRIVER, "Left Corner + Gyro Reset (hold 0.5\u00a0s; intake toward opponents)"),
    RESET_RIGHT_CORNER(Pad.DRIVER, "Right Corner + Gyro Reset (hold 0.5\u00a0s; intake toward opponents)"),
    CAMERA_RELOCALIZE(Pad.DRIVER, "Camera Reset (hold 0.5\u00a0s, robot still)"),
    AUTO_PARK(Pad.DRIVER, "*Auto-Park (hold)"),

    // Operator
    OPERATOR_INTAKE(Pad.OPERATOR, "Intake On / Off (Toggle)"),
    UNJAM(Pad.OPERATOR, "Unjam (hold)"),
    LAUNCHER_TOGGLE(Pad.OPERATOR, "Launcher Spin Up / Idle (Toggle)"),
    LAUNCHER_TRIM_DOWN(Pad.OPERATOR, "Launcher Trim Down"),
    LAUNCHER_TRIM_UP(Pad.OPERATOR, "Launcher Trim Up"),
    LAUNCHER_TRIM_RESET(Pad.OPERATOR, "Launcher Trim Reset to 0"),
    FIRE(Pad.OPERATOR, "Fire (hold; spins up first if idle)"),
    FLOWER_STICK(Pad.OPERATOR, "FLOWER Stick Deploy / Retract (Toggle)"),
    TURRET_LOCK(Pad.OPERATOR, "Turret Autoaim (Toggle)"),
    TRIM_LEFT(Pad.OPERATOR, "Trim aim left"),
    TRIM_RIGHT(Pad.OPERATOR, "Trim aim right"),
    TRIM_RESET(Pad.OPERATOR, "Trim reset (aim and launcher)"),
    TURRET_ZERO(Pad.OPERATOR, "Zero Turret (hold 0.5\u00a0s)");

    /** Which person, and so which gamepad. */
    public enum Pad { DRIVER, OPERATOR }

    public final Pad pad;
    public final String description;

    Action(Pad pad, String description) {
        this.pad = pad;
        this.description = description;
    }
}
