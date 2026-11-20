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
     * Degrees one aim trim press moves the aim while the operator holds the fine-trim button
     * (Circle). Clamped to [0, 5], as {@link #trimStepDeg}.
     */
    public static double trimFineStepDeg = 0.5;

    /**
     * The most aim trim the D-pad can set, degrees either way. 180 (mentor, 18 Nov 2026): the trim
     * also corrects a turret zeroed off its mark, of any size, and the firing arc is centred on it
     * ({@code TurretArc}). A press past it stops at it. Clamped to [0, 180]; NaN or negative is
     * the default, 180.
     */
    public static double maxAimTrimDeg = 180.0;

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
     *
     * <p>1495 since 20 Nov 2026, the mentor's call: the speed table's lowest row
     * ({@code LaunchSpeedTable.row1TicksPerSecond}), so from the close end the launcher is already
     * at launch speed and from further out it only climbs to 1590–1795. Never past the launch speed
     * ({@code applyLauncher} caps it). Was 1000; try it on the robot and watch the battery.
     */
    public static double launcherIdleTicksPerSecond = 1495.0;

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
     * Ticks per second one launcher trim press moves the launch speed while the operator holds the
     * fine-trim button (Circle). Clamped to [0, 200], as {@link #launcherTrimStepTps}.
     */
    public static double launcherTrimFineStepTps = 10.0;

    /**
     * The most launch speed trim the D-pad can set, ticks per second either way (mentor, 18 Nov
     * 2026). A press past it stops at it. Clamped to [0, 500]; NaN or negative is the default, 150.
     */
    public static double maxLauncherTrimTps = 150.0;

    /**
     * The speed table follows the range during a feed too, so a volley fired while the robot moves
     * goes at the speed for where each ball leaves, not for where the feed started. Off (the rule
     * since the table came in): the launch speed is held from the moment the gate opens until the
     * feed ends. In the 19 Nov 2026 logs on 19429 a Circle pressed at 48 in held 1507 ticks/s while
     * the driver drove to 52–55 in, where the table gives 1530–1550.
     *
     * <p><b>A test switch, not measured.</b> With it on, each change the table makes mid-feed (a
     * step of at least half the launcher's tolerance) reseeds the shot detector's level, so a shot
     * taken while it reseeds can go uncounted, and the lane's count with it. Each mid-feed change
     * is logged ("speed table … during the feed"); compare the shot count with the video and the
     * hits with the off runs. Read every loop. TeleOp only: an Auto's {@code LaunchAll} picks its
     * speed once, from where it stops.
     */
    public static boolean speedTableDuringFeed = false;

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

    /**
     * Enforce the aim gate: a feed starts only with the aim within {@link #aimToleranceDeg}, for
     * every Circle press (and always for one that turns the robot, {@code CirclePress}). The aim is the camera's when it saw our
     * CELL in the last {@code TurretAimer.SIGHTING_FRESH_MS}, else odometry's; with neither it is
     * unknown and never blocks. On since 18 Nov 2026 (mentor), with Circle firing while the turret
     * aims.
     */
    public static boolean enforceAimGate = true;

    /**
     * Enforce the turret's firing arc on the TeleOp's feeds ({@code TurretArc#allowsFeed}): no fire
     * button feeds, and a feed already going stops, while the turret, or in AIM its aim, is behind
     * the arc. Off, the TeleOp feeds wherever the turret points and leaves the call to the driver;
     * the turret still holds at the arc's edge rather than turn through the robot's back, and an
     * Auto always enforces the arc. Either way, the robot's indicator light is orange while the turret
     * or its aim is outside the arc, and flashes orange while a fire is held there ({@code ArcLight}). On by default; a robot whose drivers fire past it gets a line in
     * {@code RobotDifferences}.
     */
    public static boolean enforceFiringArc = true;

    /**
     * Basic Drive starts at PLAY with the turret aiming (AIM) rather than LOCKED straight ahead, on
     * a robot whose turret aims ({@code TurretSubsystem.lockedForward} off: both robots). The
     * driver's R1 still toggles it. On since 18 Nov 2026 (mentor).
     */
    public static boolean startTurretAiming = true;

    /**
     * Enforce the aim gate while the driver's chassis aim (Circle) is turning the robot, whatever
     * {@link #enforceAimGate} says: the feed starts only once the robot is within
     * {@link #aimToleranceDeg} of the CELL, so an early shot waits rather than goes wide. On since 7 Nov 2026 (mentor), with the chassis aim
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
     * The go-to-launch-spot button also launches: the launcher spins up as the drive starts, and
     * once the robot is at the spot, aimed and settled, it feeds while the button is held
     * ({@link SpotVolley}). On for testing in Basic Drive (mentor, 4 Nov 2026); off, Square only
     * drives and the driver's Circle fires. The operator's launcher toggle keeps working either way.
     * Read at each press.
     */
    public static boolean goToSpotAlsoFires = true;

    /**
     * With {@link #goToSpotAlsoFires}: the launch-spot button's volley opens the gate on the
     * approach, before the robot arrives, as an Auto's {@code LaunchAll} after a path does
     * ({@code LaunchCards.launchOnApproach}). It opens once the robot is within
     * {@code PathEndSettle.launchWithinIn} (6 in) of the spot, within
     * {@code PathEndSettle.aimToleranceDeg} of its heading and turning slower than
     * {@code PathEndSettle.launchTurnDegPerSec}, with the launcher at speed; those knobs are the
     * Autos' too. Off: the gate waits for arrival and the settle, as before. On since 13 Nov 2026
     * (mentor), <b>not yet driven</b>: at Belding the gate opened a median 0.30 s after arriving,
     * plus 0.8 to 0.9 s from gate open to first ball. Turn it off if the volleys miss more than
     * they did (the shot count and video, {@code doc/robot-todo.md}). Read every loop.
     */
    public static boolean squareGateOnApproach = true;

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

    /**
     * Chassis aim: the least turn power it asks for outside half the aim tolerance, so it keeps
     * turning onto the CELL instead of stalling a few degrees short ({@code ChassisAimTurn}). On
     * 19429 (19 Nov 2026 logs) 0.14 or less did not rotate the robot and 0.28 always did; 0.2 is a
     * starting point between them, to be tuned in Panels: raise it until the robot creeps onto the
     * CELL, lower it if it swings past. Clamped to [0, chassisAimMaxTurn].
     */
    public static double chassisAimMinTurn = 0.2;

    /** Chassis aim: the most turn power it may ask for. Clamped to [0, 1]. */
    public static double chassisAimMaxTurn = 0.5;
}
