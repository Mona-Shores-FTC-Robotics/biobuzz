package org.firstinspires.ftc.teamcode.controls;

/**
 * Everything a person can ask the robot to do in TeleOp, by name. The OpMode asks
 * "is {@link #FIRE} held?", never "is R2 held?"; which input that is lives in one place,
 * {@link ControlScheme}, and so does the text the drivers read for it, on the Driver Station's
 * Controls page and on the printed cheat sheet.
 */
public enum Action {
    // Driver
    DRIVE(Pad.DRIVER, "Drive"),
    TURN(Pad.DRIVER, "Turn"),
    INTAKE(Pad.DRIVER, "Intake (hold)"),
    DRIVER_UNJAM(Pad.DRIVER, "Unjam (hold)"),
    SLOW(Pad.DRIVER, "Slow (hold)"),
    TURBO(Pad.DRIVER, "Turbo (hold)"),
    FLOWER_PICK(Pad.DRIVER, "Pick FLOWER (hold; a stick cancels)"),
    FLOWER_STICK(Pad.DRIVER, "FLOWER stick out / in"),
    CHASSIS_AIM(Pad.DRIVER, "Aim at CELL (hold)"),
    RESET_HEADING(Pad.DRIVER, "Reset field-centric forward (hold 1 s)"),
    CAMERA_RELOCALIZE(Pad.DRIVER, "Camera relocalize (also in INIT)"),
    RESET_TO_SPOT(Pad.DRIVER, "Fixed relocalize (left Loading Zone corner)"),
    AUTO_PARK(Pad.DRIVER, "Auto-park (not built yet)"),

    // Operator
    OPERATOR_INTAKE(Pad.OPERATOR, "Intake (hold)"),
    UNJAM(Pad.OPERATOR, "Unjam (hold)"),
    LAUNCHER_TOGGLE(Pad.OPERATOR, "Launcher Spin Up / Idle (Toggle)"),
    FIRE(Pad.OPERATOR, "Fire (hold)"),
    TURRET_LOCK(Pad.OPERATOR, "Turret Autoaim (Toggle)"),
    TRIM_LEFT(Pad.OPERATOR, "Trim aim left"),
    TRIM_RIGHT(Pad.OPERATOR, "Trim aim right"),
    TRIM_RESET(Pad.OPERATOR, "Trim reset"),
    TURRET_ZERO(Pad.OPERATOR, "Zero turret at home mark (also in INIT)");

    /** Which person, and so which gamepad. */
    public enum Pad { DRIVER, OPERATOR }

    public final Pad pad;
    public final String description;

    Action(Pad pad, String description) {
        this.pad = pad;
        this.description = description;
    }
}
