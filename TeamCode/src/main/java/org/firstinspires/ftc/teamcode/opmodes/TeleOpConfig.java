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
     * The launcher's speed when it idles (toggled off, or a Square press's keep-spinning time is
     * up), ticks per second. 0 stops it. Never above the launch speed
     * ({@code LauncherSubsystem.targetTicksPerSecond}, or the speed table's). It applies from the
     * first idle on: at PLAY the launcher is stopped until something spins it up.
     *
     * <p>1000 since 13 Nov 2026, the mentor's call after trying it on the robot: the wheels only
     * have to climb from 1000 to the launch speed, so the gate opens sooner after a press (at
     * Belding, with 0, the launcher was not yet at speed on arrival in 42 of 72 Square volleys).
     * The cost is the battery the wheels draw while idling, not measured. Was 0.
     */
    public static double launcherIdleTicksPerSecond = 1000.0;

    /**
     * After a Square press that fires ends, the launcher stays at launch speed this many seconds
     * before it idles ({@link LauncherOwner}), so a Square pressed again or the operator's Fire
     * finds it at speed: the first ball then leaves in about 0.8 s, not 1.5 s (#497, Belding). 3 s
     * is a starting point, <b>not measured</b>; the launcher at speed draws about 3 A. Clamped to
     * [0, 10]; 0 or NaN idles it the moment Square is let go (the rule of 7 Nov 2026).
     */
    public static double launcherKeepSpinningAfterSquareS = 3.0;

    /**
     * Ticks per second one launcher trim press moves the launch speed. Clamped to [0, 200]. The
     * trim lasts the OpMode; the launch speed it trims is {@code LauncherSubsystem.targetTicksPerSecond}.
     */
    public static double launcherTrimStepTps = 25.0;

    /**
     * The intake runs all match unless something else asks: from PLAY, as in an Auto. The roller
     * slows to hold the last piece and the prefeed stops by themselves when the lane is full, and
     * both start again when a shot makes room; either L2 reverses it while held; a FLOWER pick or
     * GARDEN gather runs it itself. R2 on either pad runs it while held, which with this on changes
     * nothing (mentor, 7 Nov 2026: until then the operator's R2 was a toggle, and 19429's operator
     * turned the intake off by accident 12 times in one 104 s run on 6 Nov). Off, to save battery:
     * the intake runs only while someone holds R2.
     */
    public static boolean intakeAlwaysOn = true;

    /**
     * Enforce the at-speed gate: a feed starts only once the launcher is at launch speed (it never
     * cuts a feed already running). On since 1 Nov 2026, the mentor's call from that day's match
     * logs on 20245: in shadow mode, fire opened the gate with the launcher idle (eleven presses in
     * one run) or still recovering from an unjam (820 of 1575 ticks/s), and those shots fell short.
     */
    public static boolean enforceAtSpeedGate = true;

    /** Enforce the aim gate: a feed starts only with the aim within {@link #aimToleranceDeg}. */
    public static boolean enforceAimGate = false;

    /**
     * Enforce the aim gate while the driver's chassis aim (Circle) is turning the robot, whatever
     * {@link #enforceAimGate} says: the driver points, the operator's FIRE chooses the moment, and
     * the feed starts only once the robot is within {@link #aimToleranceDeg} of the CELL, so an
     * early press waits rather than goes wide. On since 7 Nov 2026 (mentor), with the chassis aim
     * camera first ({@code ChassisAimTarget}). An unknown aim never blocks.
     */
    public static boolean chassisAimGate = true;

    /** How far off our CELL a shot may be aimed and still pass the aim gate, degrees. */
    public static double aimToleranceDeg = 3.0;

    /**
     * The driver's go-to-launch-spot button ({@link LaunchSpotDrive}, held): on while the drivers
     * try it against chassis aim; off leaves it out of the layout, the Controls page and the
     * button's reading. Read at INIT.
     */
    public static boolean launchSpot = true;

    /**
     * The close row's button (Triangle: the launch spot about 36 in from the CELL, in front of the
     * wall one). On, so the close spots can be driven to and their launcher speed measured
     * (mentor, 12 Nov 2026): {@code LaunchSpeedTable}'s rows start at 42.4 in and its trend stops at
     * 37.4, so until a row is measured at 36 in a close volley fires at the held end speed
     * ({@code doc/robot-todo.md}). Off leaves it out of the layout, the Controls page and the
     * button's reading; {@link #launchSpot} off takes it out too. Read at INIT.
     */
    public static boolean launchSpotClose = ControlScheme.LAUNCH_SPOT_CLOSE_DEFAULT;

    /**
     * The go-to-launch-spot button also launches: the launcher spins up as the drive starts, and
     * once the robot is at the spot, aimed and settled, it feeds while the button is held, as the
     * operator's FIRE does ({@link SpotVolley}). On for testing in Basic Drive (mentor, 4 Nov 2026): a testing
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

    /**
     * A drive stick already off centre when Square, the FLOWER pick or the GARDEN gather is pressed
     * does not cancel the press until it has been centred once, or has stayed off centre this many
     * seconds after the press ({@link FreshSticks}); a stick moved after the press cancels at once,
     * as ever. 0.6 s is a starting point, <b>not measured</b>: at Belding the stick was moving at
     * 50 of the 51 Square presses cancelled within 0.1 s (#495). Clamped to [0, 2]; 0 or NaN is
     * the old rule, any stick cancels at once.
     */
    public static double staleStickCancelsAfterS = 0.6;

    /** Chassis aim: turn power per radian of heading error. A starting point, not a measurement. */
    public static double chassisAimKp = 1.0;

    /** Chassis aim: the most turn power it may ask for. Clamped to [0, 1]. */
    public static double chassisAimMaxTurn = 0.5;
}
