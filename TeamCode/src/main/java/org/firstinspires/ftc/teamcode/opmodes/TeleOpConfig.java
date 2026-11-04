package org.firstinspires.ftc.teamcode.opmodes;

import com.bylazar.configurables.annotations.Configurable;

import org.firstinspires.ftc.teamcode.controls.ControlScheme;
import org.firstinspires.ftc.teamcode.controls.Input;

/**
 * The TeleOp's driver-facing knobs, in one Panels class: the FLOWER pick button, speeds, aim trim,
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

    /**
     * The driver's FLOWER pick button: any face button or D-pad direction. Whatever was there
     * swaps onto Cross. Anything else is ignored and the pick stays on Cross. Read at INIT.
     */
    public static Input flowerPickButton = ControlScheme.DEFAULT_FLOWER_PICK;

    /**
     * Everyday driving: the sticks are multiplied by this, and no wheel gets more than this
     * ({@code WheelPowerCap}), so it is this fraction of turbo in every direction and while turning.
     * 1.0 would be full power. Clamped to [0, 1], NaN stops.
     *
     * <p>0.7 since 4 Nov 2026, from the 2 and 3 Nov Basic Drive logs on 20245, when L1 and R1 did
     * not feel like they changed much. Two reasons, both in the logs (steady segments, the command
     * held over 0.5 s):
     * <ul>
     *   <li>Before the wheel cap, 0.8 with the stick at its rim was normalized to full power about
     *       half the time (a diagonal to the robot or a turn), 0.94 of full power on average, so turbo
     *       gave 6% more power: wheels 1660 against 1740 ticks/s, 36 against 39 in/s.</li>
     *   <li>The wheels stop gaining speed near the top: about 2000 ticks/s per unit of power up to
     *       0.8 (1040 at 0.5, 1600 at 0.8), then only about 1700 at full. A normal speed above about
     *       0.8 is turbo with the acceleration limit on.</li>
     * </ul>
     * At 0.7 the wheels should run about 1400 ticks/s against turbo's 1700 to 1800, so turbo is
     * about a quarter faster and the difference is in top speed, not only in getting there. History:
     * 0.6 until 29 Oct (the driver held the stick at its rim more than half the time and almost
     * never used turbo), then 0.8, which with the saturation was nearly turbo. Drivers judge the new
     * value in Basic Drive ({@code doc/robot-todo.md}).
     */
    public static double normalSpeed = 0.7;

    /**
     * Stick multiplier and wheel cap while turbo is held, with the acceleration limit off. Full
     * power: the motors give no more. Clamped to [0, 1].
     */
    public static double turboSpeed = 1.0;

    /**
     * Stick multiplier and wheel cap while slow is held, for lining up. Slow wins if both are held.
     * Clamped to [0, 1].
     *
     * <p>0.25 since 4 Nov 2026, about a third of {@link #normalSpeed}. At 0.45 it was 57% of normal
     * at the wheels (940 against 1660 ticks/s) and 75% of its speed over the floor (27 against
     * 36 in/s, in the 2 and 3 Nov logs), too close to normal to feel like a precision mode. At 0.25
     * the wheels should run about 500 ticks/s, roughly 15 in/s. The cap holds the chassis aim's turn
     * to it too.
     */
    public static double slowSpeed = 0.25;

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

    /** Chassis aim: turn power per radian of heading error. A starting point, not a measurement. */
    public static double chassisAimKp = 1.0;

    /** Chassis aim: the most turn power it may ask for. Clamped to [0, 1]. */
    public static double chassisAimMaxTurn = 0.5;
}
