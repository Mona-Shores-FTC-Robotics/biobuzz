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
    REVERSE_INTAKE(Pad.DRIVER, "Reverse the intake (hold)"),
    SLOW(Pad.DRIVER, "Slow (hold)"),
    TURBO(Pad.DRIVER, "Turbo (hold): full power, no acceleration limit"),
    FLOWER_PICK(Pad.DRIVER, "Pick the nearest FLOWER (hold; let go or move a stick to stop)"),
    FLOWER_STICK(Pad.DRIVER, "FLOWER stick out / in"),
    CHASSIS_AIM(Pad.DRIVER, "Point the robot at our CELL (hold; left stick still drives, right stick cancels)"),
    CORNER_SNAP(Pad.DRIVER, "Pose: snap to the nearest measured corner (robot squared into it)"),
    RESET_HEADING(Pad.DRIVER, "Reset field-centric forward (hold 1 s; a tap does nothing)"),
    AUTO_PARK(Pad.DRIVER, "Auto-park (hold): not built yet"),

    // Operator
    LAUNCHER_TOGGLE(Pad.OPERATOR, "Launcher: idle / launch speed"),
    FIRE(Pad.OPERATOR, "Fire (hold): feeds now, never waits"),
    UNJAM(Pad.OPERATOR, "Unjam (hold): feeder and intake in reverse"),
    OPERATOR_INTAKE(Pad.OPERATOR, "Intake (hold); the driver's intake wins"),
    TRIM_LEFT(Pad.OPERATOR, "Aim trim left, one step"),
    TRIM_RIGHT(Pad.OPERATOR, "Aim trim right, one step"),
    TRIM_RESET(Pad.OPERATOR, "Aim trim back to zero"),
    TURRET_LOCK(Pad.OPERATOR, "Turret: LOCKED forward / AIM at our CELL"),
    TURRET_ZERO(Pad.OPERATOR, "Turret: zero here, at its home mark (also in INIT)"),
    RELOCALIZE(Pad.OPERATOR, "Pose: relocalize from the camera (hold still, 2+ tags in view)");

    /** Which person, and so which gamepad. */
    public enum Pad { DRIVER, OPERATOR }

    public final Pad pad;
    public final String description;

    Action(Pad pad, String description) {
        this.pad = pad;
        this.description = description;
    }
}
