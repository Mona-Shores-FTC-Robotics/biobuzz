package org.firstinspires.ftc.teamcode.opmodes;

import com.bylazar.configurables.annotations.Configurable;

import org.firstinspires.ftc.teamcode.controls.ControlScheme;
import org.firstinspires.ftc.teamcode.controls.Input;

/**
 * The TeleOp's driver-facing knobs, in one Panels class: the control scheme, speeds, aim trim,
 * the fire gates and chassis aim. A mechanism's own constants (powers, launcher speed, the FLOWER
 * pick's distances) stay on the class that owns the mechanism.
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

    /** Which {@link ControlScheme}. Read once at INIT: change it here, then press INIT. */
    public static ControlScheme scheme = ControlScheme.A;

    /**
     * The driver's FLOWER pick button: any face button or D-pad direction. Whatever was there
     * swaps onto Cross. Anything else is ignored and the pick stays on Cross. Read at INIT.
     */
    public static Input flowerPickButton = ControlScheme.DEFAULT_FLOWER_PICK;

    /** Stick multiplier for everyday driving. 1.0 would be full power. */
    public static double normalSpeed = 0.6;

    /** Stick multiplier while turbo is held. */
    public static double turboSpeed = 1.0;

    /** Stick multiplier while slow is held. Slow wins if both are held. */
    public static double slowSpeed = 0.35;

    /** Degrees one trim press moves the aim. Clamped to [0, 5]. */
    public static double trimStepDeg = 1.5;

    /**
     * The launcher's speed when toggled to idle, ticks per second. 0 stops it. The launch speed is
     * {@code LauncherSubsystem.targetTicksPerSecond}.
     */
    public static double launcherIdleTicksPerSecond = 0.0;

    /** Enforce the at-speed gate: a feed starts only once the launcher is at launch speed. */
    public static boolean enforceAtSpeedGate = false;

    /** Enforce the aim gate: a feed starts only with the aim within {@link #aimToleranceDeg}. */
    public static boolean enforceAimGate = false;

    /** How far off our CELL a shot may be aimed and still pass the aim gate, degrees. */
    public static double aimToleranceDeg = 3.0;

    /** Chassis aim: turn power per radian of heading error. A starting point, not a measurement. */
    public static double chassisAimKp = 1.0;

    /** Chassis aim: the most turn power it may ask for. Clamped to [0, 1]. */
    public static double chassisAimMaxTurn = 0.5;
}
