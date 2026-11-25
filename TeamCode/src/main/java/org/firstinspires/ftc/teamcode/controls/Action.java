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
    INTAKE(Pad.DRIVER, "Intake (hold, runs past full)"),
    DRIVER_UNJAM(Pad.DRIVER, "Unjam (hold)"),
    SLOW(Pad.DRIVER, "Slow (hold)"),
    FLOWER_PICK(Pad.DRIVER, "Pick FLOWER (hold)"),
    GARDEN_GATHER(Pad.DRIVER, "*Corner Gather (hold)"),
    CHASSIS_AIM(Pad.DRIVER, "Fire (hold): turret AIM, fires; LOCKED, turns onto the CELL, then fires"),
    LAUNCH_SPOT(Pad.DRIVER, "*Go to Launch Spot (hold)"),
    RESET_HEADING(Pad.DRIVER, "Gyro Reset (hold 0.5\u00a0s)"),
    RESET_LEFT_CORNER(Pad.DRIVER, "Left Corner + Gyro Reset (hold 0.5\u00a0s; intake toward opponents)"),
    RESET_RIGHT_CORNER(Pad.DRIVER, "Right Corner + Gyro Reset (hold 0.5\u00a0s; intake toward opponents)"),
    HEADING_LOCK(Pad.DRIVER, "Heading Lock (hold): turns to the nearest 90\u00b0 and holds it; left stick drives dead straight near an axis"),
    CAMERA_RELOCALIZE(Pad.DRIVER, "Camera Reset (hold 0.5\u00a0s, robot still)"),
    AUTO_PARK(Pad.DRIVER, "*Auto-Park (hold)"),

    // Operator
    OPERATOR_INTAKE(Pad.OPERATOR, "Intake (hold)"),
    UNJAM(Pad.OPERATOR, "Unjam (hold)"),
    LAUNCHER_TOGGLE(Pad.OPERATOR, "Launcher Spin Up / Idle (Toggle; pre-spin for the driver's Circle)"),
    LAUNCHER_TRIM_DOWN(Pad.OPERATOR, "Launcher Trim Down"),
    LAUNCHER_TRIM_UP(Pad.OPERATOR, "Launcher Trim Up"),
    LAUNCHER_TRIM_RESET(Pad.OPERATOR, "Launcher Trim Reset to 0"),
    FINE_TRIM(Pad.OPERATOR, "Fine trim (hold with L1/R1 or D-pad)"),
    /** The bench's fire (Bench: Launch Test); never in the match layout, where the driver's Circle fires. */
    FIRE(Pad.OPERATOR, "Fire (hold; spins up first if idle)"),
    FLOWER_STICK(Pad.OPERATOR, "FLOWER Stick Deploy / Retract (Toggle)"),
    TRIM_LEFT(Pad.OPERATOR, "Trim aim left"),
    TRIM_RIGHT(Pad.OPERATOR, "Trim aim right"),
    TRIM_RESET(Pad.OPERATOR, "Trim reset (aim and launcher)"),
    TURRET_ZERO(Pad.OPERATOR, "Zero Turret (hold 0.5\u00a0s)"),
    TURRET_LOCK(Pad.OPERATOR, "Turret AIM / LOCKED (hold 0.5\u00a0s; sets what the driver's Circle does: 1 buzz AIM, 2 LOCKED)"),
    /** The bench's setup layer (Bench: Launch Test), held: never in the match layout. */
    BENCH_SETUP(Pad.OPERATOR, "Setup layer (hold)");

    /** Which person, and so which gamepad. */
    public enum Pad { DRIVER, OPERATOR }

    public final Pad pad;
    public final String description;

    Action(Pad pad, String description) {
        this.pad = pad;
        this.description = description;
    }
}
