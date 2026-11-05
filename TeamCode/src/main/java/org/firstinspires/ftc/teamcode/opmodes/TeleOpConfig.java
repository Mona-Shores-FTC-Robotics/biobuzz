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
     * Everyday driving: the sticks are multiplied by this, and no wheel gets more than this
     * ({@code WheelPowerCap}). Full power, with the acceleration limit on. Clamped to [0, 1], NaN
     * stops.
     *
     * <p>1.0 since 5 Nov 2026, the drive team's call: turbo (R1, full power) is gone and its speed
     * is now normal, and the old normal (0.76) is now {@link #slowSpeed}. History: 0.6 until 29 Oct,
     * then 0.8 (which saturated to nearly full power on diagonals before the wheel cap), 0.7 on
     * 4 Nov, 0.76 on 5 Nov, with turbo at 1.0 on R1.
     */
    public static double normalSpeed = 1.0;

    /**
     * Stick multiplier and wheel cap while slow (L1) is held, for lining up: this fraction of full
     * power in every direction and while turning. Clamped to [0, 1].
     *
     * <p>0.76 since 5 Nov 2026: the old normal speed, the drive team's call when turbo became
     * normal. Before that slow was 0.38 (0.25 on 4 Nov did not strafe on 20245: a mecanum strafe
     * needs more wheel power than a straight drive to overcome the rollers).
     */
    public static double slowSpeed = 0.76;

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
     * room, so the intake is left on: it starts on at PLAY (5 Nov 2026), as in an Auto, and a FLOWER
     * pick or GARDEN gather ending hands it back to the toggle. The driver's R2 runs it while held and either L2 reverses it while held, over the
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

    /**
     * The go-to-launch-spot button also launches: the launcher spins up as the drive starts, and
     * once the robot is at the spot, aimed and settled, one volley feeds as the operator's FIRE
     * would ({@link SpotVolley}). On for testing in Basic Drive (mentor, 4 Nov 2026): a testing
     * choice the team may turn off to give the shot back to the operator. The operator's FIRE and
     * launcher toggle keep working either way and win. Read at each press.
     */
    public static boolean goToSpotAlsoFires = true;

    /**
     * At a launch spot, settled and facing the HIVE, a camera-vs-odometry gap past this many inches
     * puts "corner reset suggested" on the Match page ({@link LaunchSpotDrive}). The gap is only
     * logged and shown, never applied: still and continuous fixes stay off until measured. 3 in is
     * a starting point, <b>not measured</b>; the presses' drift lines are the measurement. 0 or NaN
     * never warns.
     */
    public static double spotDriftWarnIn = 3.0;

    /** Chassis aim: turn power per radian of heading error. A starting point, not a measurement. */
    public static double chassisAimKp = 1.0;

    /** Chassis aim: the most turn power it may ask for. Clamped to [0, 1]. */
    public static double chassisAimMaxTurn = 0.5;
}
