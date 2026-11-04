package org.firstinspires.ftc.teamcode.opmodes;

import com.bylazar.configurables.annotations.Configurable;

import org.firstinspires.ftc.teamcode.controls.ControlScheme;
import org.firstinspires.ftc.teamcode.controls.Input;

/**
 * The TeleOp's driver-facing knobs, in one Panels class: the FLOWER pick button, speeds, aim trim,
 * the fire gates, chassis aim and the launch-spot button. A mechanism's own constants (powers,
 * launcher speed, the FLOWER pick's distances) stay on the class that owns the mechanism.
 *
 * <p>Edit in Panels while someone drives, then write the values the drivers like back here:
 * Panels edits are lost on restart. Every value is guarded where it is read, so a typo gives a
 * timid robot, never a runaway one. All of these are written to the match log at PLAY
 * ({@code Tunables}).
 */
@Configurable
public final class TeleOpConfig {

    private TeleOpConfig() {
    }

    /**
     * The driver's FLOWER pick button: any face button or D-pad direction. Whatever was there
     * swaps onto Cross. Anything else is ignored and the pick stays on Cross. Read at INIT.
     */
    public static Input flowerPickButton = ControlScheme.DEFAULT_FLOWER_PICK;

    /**
     * Stick multiplier for everyday driving. 1.0 would be full power. 0.8 since 29 Oct, when at 0.6
     * the driver held the stick at its rim more than half the time and almost never used turbo.
     */
    public static double normalSpeed = 0.8;

    /** Stick multiplier while turbo is held. */
    public static double turboSpeed = 1.0;

    /**
     * Stick multiplier while slow is held. Slow wins if both are held. 0.45 since the everyday speed
     * went to 0.8, keeping slow a little over half of it.
     */
    public static double slowSpeed = 0.45;

    /** Degrees one trim press moves the aim. Clamped to [0, 5]. */
    public static double trimStepDeg = 1.5;

    /**
     * The launcher's speed when toggled to idle, ticks per second. 0 stops it. The launch speed is
     * {@code LauncherSubsystem.targetTicksPerSecond}.
     */
    public static double launcherIdleTicksPerSecond = 0.0;

    /**
     * Ticks per second one launcher trim press moves the launch speed. Clamped to [0, 200]. The
     * trim lasts the OpMode; the launch speed it trims is {@code LauncherSubsystem.targetTicksPerSecond}.
     */
    public static double launcherTrimStepTps = 25.0;

    /**
     * The operator's R2 toggles the intake on and off instead of running it only while held. On by
     * default since 4 Nov 2026 (the drive team's call): the roller slows to hold the last piece and
     * the prefeed stops by themselves when the lane is full, and both start again when a shot makes
     * room, so the intake can be left on. The driver's R2 runs it while held and either L2 reverses it while held, over the
     * toggle; letting go returns to it. Firing does not turn it off. Off: hold-to-run on both pads,
     * where letting go of R2 stops the intake.
     */
    public static boolean intakeToggle = true;

    /**
     * Enforce the at-speed gate: a feed starts only once the launcher is at launch speed (it never
     * cuts a feed already running). On since 1 Nov 2026, the mentor's call from that day's match
     * logs on 20245: in shadow mode, fire opened the gate with the launcher idle (eleven presses in
     * one run) or still recovering from an unjam (820 of 1575 ticks/s), and those shots fell short.
     */
    public static boolean enforceAtSpeedGate = true;

    /** Enforce the aim gate: a feed starts only with the aim within {@link #aimToleranceDeg}. */
    public static boolean enforceAimGate = false;

    /** How far off our CELL a shot may be aimed and still pass the aim gate, degrees. */
    public static double aimToleranceDeg = 3.0;

    /**
     * The driver's go-to-launch-spot button ({@link LaunchSpotDrive}, held): on while the drivers
     * try it against chassis aim; off leaves it out of the layout, the Controls page and the
     * button's reading. Read at INIT.
     */
    public static boolean launchSpot = true;

    /** Chassis aim: turn power per radian of heading error. A starting point, not a measurement. */
    public static double chassisAimKp = 1.0;

    /** Chassis aim: the most turn power it may ask for. Clamped to [0, 1]. */
    public static double chassisAimMaxTurn = 0.5;
}
