package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.hardware.DeviceNames;
import org.firstinspires.ftc.teamcode.util.Throttle;
import org.firstinspires.ftc.teamcode.util.VoltageCompensation;
import org.firstinspires.ftc.teamcode.util.WheelSpeed;

import java.util.Locale;
import java.util.function.BooleanSupplier;

/**
 * The launcher: two flywheels, left and right, held at one commanded speed. The first edition,
 * written to the wiring spec ({@code launcher_left} and {@code launcher_right} on Expansion Hub
 * motor ports 1 and 2) before the mechanism was mounted, so the first bench test has code to run.
 *
 * <p>Same shape as {@link IntakeSubsystem}: the OpMode says what it wants ({@link #spinUp()},
 * {@link #spinDown()}), {@link #update()} does it once per loop, and {@link #atSpeed()} is the one
 * fact the feeder and the Autos need from it. Which button does what belongs in the bindings.
 *
 * <h2>How the speed is held</h2>
 *
 * <p>The hub's own velocity loop: each motor runs in {@code RUN_USING_ENCODER} and is given a
 * target in encoder ticks per second with {@code setVelocity}. That is one write per wheel when
 * the target changes, and again every {@link #RESEND_MS} (each is a 2–3 ms round trip to the hub,
 * and the SDK does not skip a repeated velocity; the resend recovers a motor a hub has zeroed), and
 * no controller of ours to run.
 *
 * <h2>Tuning the hub's loop</h2>
 *
 * <p>The hub's velocity loop has four gains, P, I, D and F, set with
 * {@code setVelocityPIDFCoefficients}. Match logs of 3 and 4 Nov 2026 (20245) showed what its
 * defaults do with no tuned feedforward: after each ball's dip the wheels overshot to about 1680
 * of 1595 and at-speed stayed false for about 0.7 s, and a spin-up from rest took 0.9 to 2.1 s,
 * slowest on a sagging battery. So the four gains are Panels knobs here ({@link #velocityP},
 * {@link #velocityI}, {@link #velocityD}, {@link #velocityF}): NaN, the default, leaves that gain
 * at what the hub had when this OpMode's {@link #initialize()} read it ({@link #hubDefaults()},
 * shown on the Robot page and in the match log's Metadata), so nothing changes until someone sets
 * one. The gains go to the hub only when one changes, never every loop: each write is a hub round
 * trip per wheel. Optionally F follows the battery ({@link #compensateF}): F × {@link #nominalVolts}
 * / battery, from the reading {@code RobotOpMode} already takes once a second
 * ({@link #batteryVolts(double)}), written again only when it moves by more than
 * {@link #F_COMPENSATION_STEP}. The procedure is in {@code doc/robot-todo.md}, "Launcher tuning".
 *
 * <p>The setpoint is in ticks per second, not RPM, because ticks per second is what the hardware
 * measures and commands. RPM is shown as well once {@link #ticksPerRev} is read off the motor
 * label; until then it is NaN rather than a guess. The speed-vs-distance table (#80) is written
 * in whatever unit the bench measures and replaces {@link #targetTicksPerSecond} as the source of
 * the setpoint.
 *
 * <h2>Why there is an encoder check</h2>
 *
 * <p>A velocity loop with no encoder feedback drives the motor to full power forever, trying to
 * reach a speed it never sees. The intake's encoder cable was unplugged at its first bench test,
 * so this is not hypothetical. A wheel that is commanded and still reads no speed after
 * {@link #encoderFaultAfterMs} is cut and reported on the Match page; {@link #spinDown()} clears
 * the fault so a fixed cable can be tried again without restarting.
 *
 * <h2>Shots are counted from the speed dip</h2>
 *
 * <p>A piece meeting the wheels pulls their speed down and the loop pulls it back. That dip is the
 * one thing the launcher can see of a shot without a sensor, so {@link ShotDetector} counts one
 * shot per dip: the wheels were at speed, both fell at least {@link #shotDipTicksPerSecond} below
 * target while the feed gate was open, and came back within tolerance. Spin-up is not a dip (the wheels were
 * never at speed), a wobble inside the threshold is not a dip, and a target change re-arms the
 * detector so a Panels nudge is not a shot.
 *
 * <p>A dip that starts with the gate shut is not a shot: no piece can reach the wheels. The match
 * log of 4 Nov 2026 (20245, 00:02 Basic Drive) is why: 17 of 88 counted shots started with the gate
 * shut and the feeder unloaded (about 1.6 A against 6 A and more for a fed shot), most of them while
 * the intake took in a NECTAR and its speed collapsed from about 2600 to under 600 ticks/s. Both
 * wheels sagged 80–100 ticks/s over about 0.3 s, a load on the robot (most likely the battery under
 * the intake's stall), not a piece; at 207.38 s and 207.91 s the left wheel alone dipped. Only the start is judged: a dip that
 * began with the gate open and recovers after it shuts still counts.
 *
 * <p>Both wheels must dip, because a piece taken by the wheels pulls both down together: in the same
 * log, 31 of the 34 shots deeper than 120 dipped the two wheels within 40 of each other, bottoming
 * in the same sample, and the most uneven still took both past 140. One wheel dipping alone is the
 * wheels wobbling, each on its own hub velocity loop, while the feeder presses a ball against them
 * before they take it (feeder current climbing to 6 A and more): at 134.46 s the left fell to 1520
 * with the right at 1620, then at 134.81–134.98 s the right fell to 1480 with the left at 1580, and
 * each counted as a shot before the real one at 135.52 s, both wheels 280 down. Requiring both
 * wheels drops 25 such gate-open dips in that log and keeps every shot deeper than 120. The
 * recorded depth is still the slower wheel's ({@link #deficit}).
 *
 * <p>The gate is the one {@link #countShotsWhile} names (the robot's {@code FeedGateSubsystem}), or, for
 * a launcher built without one, the feeder feeding. The count and the last dip's depth go to the Robot page, Panels and the match log; a
 * student writes hit or miss next to each row afterwards. That is how #80's speed-vs-distance
 * table gets its rows. It is a measurement, never a gate: nothing here waits for it.
 *
 * <p>Each spin-up and each shot's dip is also timed ({@link LauncherResponse}: rise, overshoot,
 * settle; dip depth, recovery, overshoot after it), for tuning the velocity PIDF from numbers.
 *
 * <p>Nothing here is measured yet. The directions, the starting setpoint and the tolerance are
 * {@code @Configurable} so the bench sets them in Panels; the values that work get written back
 * here with the robot they were checked on.
 *
 * <h2>Backing up for an unjam</h2>
 *
 * <p>Two balls can meet at the wheels and sit on top of them (20245, 1 Nov 2026); the only way out
 * was to turn the launcher off so they dropped, then reverse the feeder. So while the feeder
 * reverses, the wheels run backwards at {@link #reverseTicksPerSecond}, gently, and push the balls
 * back down onto the reversing feeder. The launcher follows the feeder's request every
 * {@link #update()}, as the intake and the prefeed do ({@link #reverseFor}): an explicit state,
 * not a negative target, which {@link #commandFor} still refuses. What the OpMode asked for is
 * untouched, so on release a spinning launcher spins back up to its launch speed and an idle one
 * stays idle; at-speed and the shot detector sit out the reverse. Only after PLAY.
 */
@Configurable
public class LauncherSubsystem implements Subsystem {

    /** What the launcher is being asked to do. */
    public enum State { IDLE, SPINNING }

    /**
     * How the two flywheel motors are wired. {@link #TWO_MOTORS} is the robot: each motor on its own
     * port with its own encoder, so each wheel has its own speed loop and its own trim.
     * {@link #Y_CABLED} is the bench rig: both motors on one port through a Y cable, so software sees
     * one motor with one encoder, both wheels get the same signal, and the trims do nothing. The
     * wheels turning opposite ways then comes from how the motors are mounted and wired, not code.
     */
    public enum Layout { TWO_MOTORS, Y_CABLED }

    /**
     * The speed both wheels are asked to hold while spinning, encoder ticks per second: the
     * default for the TeleOp and the Bench: Launcher, adjustable live in Panels (and by the bench's
     * D-pad and the TeleOp's trim). <b>A starting point, not a measured value.</b> A goBILDA 5203
     * counts 28 ticks per motor revolution, so a 6000 RPM bare motor tops out near 2800; 1525 is
     * where POLLEN and NECTAR both shoot. The value each leaves cleanly at is the first row of
     * #80's table. 1595 since 1 Nov 2026, the mentor's pick on 20245 (locked turret, POLLEN only),
     * from R Simple at the red right start: 1575 landed a little short (3 and 2 of 4), 1625 was
     * too much (1 of 4); the logs showed every ball leaving at the full target either way.
     */
    public static double targetTicksPerSecond = 1595.0;

    /**
     * Added to {@link #targetTicksPerSecond} for the left wheel only, ticks per second. Zero means
     * both wheels run the same speed. A bench sets it to run the wheels apart (to find whether the
     * pinch wants spin, or to match two motors that differ); a measured value gets written back.
     */
    public static double leftTrimTicksPerSecond = 0.0;

    /** As {@link #leftTrimTicksPerSecond}, for the right wheel. */
    public static double rightTrimTicksPerSecond = 0.0;

    /** How close each wheel must be to its own target, ticks per second, to count as at speed. */
    public static double toleranceTicksPerSecond = 60.0;

    /** How long both wheels must stay within tolerance before {@link #atSpeed()} says so, ms. */
    public static double atSpeedHoldMs = 150.0;

    /**
     * How far below its target each wheel must fall, from at speed, for the dip to count as a shot,
     * ticks per second (both wheels: class comment). Measured on 20245, 1 to 2 Nov 2026, target 1595: every POLLEN shot dipped
     * the slower wheel 95 (nine shots over three runs; one 135), and the wheels hover within about
     * 25 at speed. 70 sits between the two. It was 150, a starting guess, and counted no shot at
     * all, which left the lane tracker's count stuck after firing. Below
     * {@link #toleranceTicksPerSecond} (60) it would make no difference: a dip only starts once both
     * wheels leave the at-speed band.
     */
    public static double shotDipTicksPerSecond = 70.0;

    /**
     * Flips the left wheel so that a positive command throws a piece out. Checked on robot 20245 on
     * 1 Nov 2026 with the Full Path Bench: the left wheel (the robot's left, seen from behind) needs
     * flipping and the right does not, as expected for two mirror-image modules. The speed readout
     * is an absolute value, so it cannot show a wheel spinning backwards: check by eye. 19429 is
     * not checked yet.
     */
    public static boolean leftReversed = true;

    /** As {@link #leftReversed}, for the right wheel: not flipped, checked on 20245 on 1 Nov 2026. */
    public static boolean rightReversed = false;

    /**
     * Speed both wheels run backwards while the feeder reverses (an unjam), ticks per second, to
     * drive a ball sitting on top of the wheels back down onto the reversing feeder. 300, about a
     * fifth of the launch speed: the mentor's call on 1 Nov 2026 once the robot went POLLEN only.
     * 1500 had been set that afternoon to clear NECTAR, but the match logs showed its cost: the
     * wheels took about a second to swing from -1500 back to launch speed, and a fire straight
     * after an unjam went out at 820 ticks/s. The hub's velocity loop holds it, so it is the same
     * on a fresh or a tired battery. 0 turns the reverse off.
     */
    public static double reverseTicksPerSecond = 300.0;

    /**
     * Encoder ticks per revolution of each flywheel <b>motor</b>, for the RPM display. 28: the
     * motors are believed to be goBILDA 5203 6000 RPM (1:1) at the bench on 24 Oct 2026; confirm on
     * the label. The encoder is on the motor, so the RPM shown is motor RPM; the wheel turns at that
     * times the module's belt ratio, which is a CAD number not written here yet.
     */
    public static double ticksPerRev = 28.0;

    /**
     * Flywheel revolutions per motor revolution (the module's belt), for the surface speed. NaN
     * until measured (count teeth, or mark the wheel and turn the motor by hand): the surface speed
     * shows as unknown rather than a guess.
     */
    public static double beltRatio = Double.NaN;

    /** Flywheel diameter, inches, for the surface speed. NaN until measured with calipers on the robot. */
    public static double wheelDiameterInches = Double.NaN;

    /**
     * A wheel commanded for this long that still reads below {@link #encoderSilentTicksPerSecond}
     * is treated as having no encoder and is cut. Long enough for a real spin-up to show movement.
     */
    public static double encoderFaultAfterMs = 500.0;

    /** Below this measured speed, a commanded wheel is "not moving" for the encoder check. */
    public static double encoderSilentTicksPerSecond = 20.0;

    /**
     * The hub velocity loop's proportional gain for both wheels. NaN (the default): the hub's own,
     * as read at init ({@link #hubDefaults()}). Not measured yet: see "Tuning the hub's loop" in
     * the class comment. A negative, infinite or absurd value (above {@link #MAX_GAIN}) counts as
     * NaN, so a Panels typo leaves the hub's gain in place.
     */
    public static double velocityP = Double.NaN;

    /** As {@link #velocityP}, the integral gain. Keep it small: it is what winds up into overshoot. */
    public static double velocityI = Double.NaN;

    /** As {@link #velocityP}, the derivative gain. Usually 0 for a flywheel. */
    public static double velocityD = Double.NaN;

    /**
     * As {@link #velocityP}, the feedforward gain: the hub's power per tick per second of target,
     * scaled so that 32767 is full power. A first guess is 32767 / (the speed one wheel reaches at
     * full power, ticks per second), at {@link #nominalVolts}.
     */
    public static double velocityF = Double.NaN;

    /**
     * Scale F by the battery: F × {@link #nominalVolts} / battery, so the feedforward pushes as hard
     * on a tired battery as on the one it was tuned on. Off until a robot run shows it helps.
     * Applies to F as it stands, set here or the hub's own.
     */
    public static boolean compensateF = false;

    /** The battery voltage F is tuned at, volts, for {@link #compensateF}. NaN, zero or negative: no compensation. */
    public static double nominalVolts = 12.0;

    /** Above this a gain from Panels is treated as a typo and the hub's own is kept. */
    static final double MAX_GAIN = 1000.0;

    /** A compensated F is written again only when its scale moves by more than this fraction (1%, about 0.1 V). */
    static final double F_COMPENSATION_STEP = 0.01;

    private final Layout layout;
    private final DcMotorEx left;
    /** The right wheel's motor, or null when {@link Layout#Y_CABLED}: the left motor object drives both. */
    private final DcMotorEx right;

    private State state = State.IDLE;
    private final FeederSubsystem feeder;
    /** PLAY has happened: the wheels may run backwards for an unjam. */
    private boolean started;
    /** The wheels are running backwards for an unjam, as of the last update. */
    private boolean reversing;
    /** A speed this OpMode asked for with {@link #spinUp(double)}, ticks per second; NaN for {@link #targetTicksPerSecond}. */
    private double heldTicksPerSecond = Double.NaN;
    private double leftTicksPerSecond;
    private double rightTicksPerSecond;
    private double leftCurrentAmps = Double.NaN;
    private double rightCurrentAmps = Double.NaN;
    private long spinUpStartedNs = -1;
    private boolean leftFault;
    private boolean rightFault;
    private final AtSpeedTimer readiness = new AtSpeedTimer();
    private boolean atSpeed;
    private final ShotDetector shots = new ShotDetector();
    /** Whether the feed gate is open, so a dip may be a shot ({@link #countShotsWhile}). */
    private BooleanSupplier gateOpen;
    private final LauncherResponse response = new LauncherResponse();
    /** The last update ran the SPINNING branch (not idle, not reversing): a new one starts a spin-up measurement. */
    private boolean measuring;
    private double lastLeftTarget = Double.NaN;
    private double lastRightTarget = Double.NaN;
    /** The velocity last sent to each wheel; NaN when none is standing, so the next one goes. */
    private double leftSent = Double.NaN;
    private double rightSent = Double.NaN;
    /** An unchanged velocity is sent again this often, ms. */
    static final double RESEND_MS = 250;
    private final Throttle resend = new Throttle(RESEND_MS, 0);
    /** Current is not in the bulk read; ten reads a second, as {@code IntakeSubsystem}'s. */
    private final Throttle currentRead = new Throttle(100, 66);

    /** Each wheel's velocity PIDF as read at init; null when the hub did not answer with one. */
    private PIDFCoefficients leftDefaults;
    private PIDFCoefficients rightDefaults;
    /** The Panels gains and switch last acted on, so a write happens only when one changes. NaN-aware. */
    private double seenP = Double.NaN;
    private double seenI = Double.NaN;
    private double seenD = Double.NaN;
    private double seenF = Double.NaN;
    private boolean seenCompensate;
    private double seenNominal = Double.NaN;
    /** The battery, volts, as {@code RobotOpMode} last passed it in; NaN until then. */
    private double batteryVolts = Double.NaN;
    /** The scale on F in force (1 when not compensating). */
    private double fScale = 1.0;
    /** The battery's scale on F is looked at once a second at most. */
    private final Throttle fScaleCheck = new Throttle(1000, 0);
    /** The left wheel's gains as last written (the right's differ only if its defaults do); NaN before the first write. */
    private double appliedP = Double.NaN;
    private double appliedI = Double.NaN;
    private double appliedD = Double.NaN;
    private double appliedF = Double.NaN;
    private int pidfWrites;

    /**
     * The robot's launcher: looks both motors up once. A missing motor fails init with the SDK's own message.
     *
     * @param feeder read for its request: the wheels back up while it reverses
     */
    public LauncherSubsystem(HardwareMap hardwareMap, FeederSubsystem feeder) {
        layout = Layout.TWO_MOTORS;
        left = hardwareMap.get(DcMotorEx.class, DeviceNames.LAUNCHER_LEFT);
        right = hardwareMap.get(DcMotorEx.class, DeviceNames.LAUNCHER_RIGHT);
        this.feeder = feeder;
        gateOpen = this::feederFeeding;
    }

    private LauncherSubsystem(DcMotorEx pair, FeederSubsystem feeder) {
        layout = Layout.Y_CABLED;
        left = pair;
        right = null;
        this.feeder = feeder;
        gateOpen = this::feederFeeding;
    }

    private boolean feederFeeding() {
        return feeder.state() == FeederSubsystem.State.FEED;
    }

    /**
     * Count a speed dip as a shot only if it starts while {@code gateOpen} says the feed gate is open
     * (class comment). Wired once by {@code Robot} to its feed gate, so a bench holding the gate open
     * counts too; until then, and on a launcher built without a gate, it is the feeder feeding.
     */
    public void countShotsWhile(BooleanSupplier gateOpen) {
        this.gateOpen = gateOpen;
    }

    /**
     * The bench rig's launcher: both motors on one port through a Y cable, one device named
     * {@link DeviceNames#LAUNCHER}. An explicit decision, not a missing device being tolerated.
     */
    public static LauncherSubsystem yCabled(HardwareMap hardwareMap, FeederSubsystem feeder) {
        return new LauncherSubsystem(hardwareMap.get(DcMotorEx.class, DeviceNames.LAUNCHER), feeder);
    }

    /** How the motors are wired; see {@link Layout}. */
    public Layout layout() {
        return layout;
    }

    /** Ask both wheels to hold {@link #targetTicksPerSecond}. Takes effect in the next {@link #update()}. */
    public void spinUp() {
        heldTicksPerSecond = Double.NaN;
        beginSpinning();
    }

    /**
     * Ask both wheels to hold a given speed, ticks per second: a trimmed launch speed or an idle
     * speed. For this OpMode only: {@link #targetTicksPerSecond}, the configured speed, is left as
     * it is, since a static changed here would outlive the OpMode and every run would start from
     * the last one's trim. {@link #spinUp()} goes back to the configured speed.
     */
    public void spinUp(double ticksPerSecond) {
        heldTicksPerSecond = ticksPerSecond;
        beginSpinning();
    }

    private void beginSpinning() {
        if (state != State.SPINNING) {
            spinUpStartedNs = System.nanoTime();
            readiness.reset();
            shots.reset();
        }
        state = State.SPINNING;
    }

    /** Let both wheels coast to a stop. Not {@link #stop()}: the subsystem keeps running. Clears any encoder fault. */
    public void spinDown() {
        state = State.IDLE;
        spinUpStartedNs = -1;
        leftFault = false;
        rightFault = false;
        readiness.reset();
        shots.reset();
        atSpeed = false;
        response.cancel();
        measuring = false;
    }

    /** What the launcher has been asked to do. The request, not a measurement. */
    public State state() {
        return state;
    }

    /** True while spinning has been asked for, whether or not the wheels are there yet. */
    public boolean isSpinning() {
        return state == State.SPINNING;
    }

    /** True while the wheels run backwards for an unjam (the feeder is reversing). For the match log. */
    public boolean reversing() {
        return reversing;
    }

    /**
     * Both wheels have held the target within {@link #toleranceTicksPerSecond} for
     * {@link #atSpeedHoldMs}. The feeder's green light. False while idle or faulted.
     */
    public boolean atSpeed() {
        return atSpeed;
    }

    /** Shots counted from speed dips since this OpMode started (class comment). Not cleared by {@link #spinDown()}. */
    public int shots() {
        return shots.shots();
    }

    /** True in the loop after a shot was counted, for a once-per-shot log line. */
    public boolean shotJustDetected() {
        return shots.justCounted();
    }

    /** How far below target the slower wheel fell on the last counted shot, ticks per second; NaN before the first. */
    public double lastShotDipTicksPerSecond() {
        return shots.lastDipTicksPerSecond();
    }

    /** The common target in force on the last counted shot, ticks per second; NaN before the first. */
    public double lastShotTargetTicksPerSecond() {
        return shots.lastTargetTicksPerSecond();
    }

    /** Spin-up and dip timings (class comment), for tuning the velocity PIDF. */
    public LauncherResponse response() {
        return response;
    }

    /** True while the wheels are down in a dip that has not yet recovered: a piece in the wheels, or a jam. */
    public boolean inDip() {
        return shots.inDip();
    }

    /**
     * The common speed in force, ticks per second: the one {@link #spinUp(double)} asked for, else
     * {@link #targetTicksPerSecond}, clamped the way the wheels get it.
     */
    public double target() {
        return nonNegative(common());
    }

    private double common() {
        return Double.isNaN(heldTicksPerSecond) ? targetTicksPerSecond : heldTicksPerSecond;
    }

    /** The left wheel's own target: the common target plus its trim, never below zero. */
    public double leftTarget() {
        return wheelTarget(common(), leftTrimTicksPerSecond);
    }

    /**
     * The right wheel's own target: the common target plus its trim, never below zero. Y-cabled,
     * there is one signal, so this is {@link #leftTarget()}.
     */
    public double rightTarget() {
        return layout == Layout.Y_CABLED ? leftTarget()
                : wheelTarget(common(), rightTrimTicksPerSecond);
    }

    /** Left wheel speed from the last {@link #update()}, ticks per second, direction removed. */
    public double leftTicksPerSecond() {
        return leftTicksPerSecond;
    }

    /** Right wheel speed from the last {@link #update()}, ticks per second, direction removed. */
    public double rightTicksPerSecond() {
        return rightTicksPerSecond;
    }

    /** {@link #leftTicksPerSecond()} in output-shaft RPM, or NaN until {@link #ticksPerRev} is set. */
    public double leftRpm() {
        return IntakeSubsystem.rpm(leftTicksPerSecond, ticksPerRev);
    }

    /**
     * The flywheels' surface speed, inches per second, the mean of both wheels: comparable with the
     * feeder's ({@code FeederSubsystem.surfaceInchesPerSecond()}), as ticks per second are not. NaN
     * until {@link #beltRatio} and {@link #wheelDiameterInches} are measured.
     */
    public double surfaceInchesPerSecond() {
        return WheelSpeed.surfaceInchesPerSecond((leftTicksPerSecond + rightTicksPerSecond) / 2.0,
                ticksPerRev, beltRatio, wheelDiameterInches);
    }

    /** {@link #rightTicksPerSecond()} in output-shaft RPM, or NaN until {@link #ticksPerRev} is set. */
    public double rightRpm() {
        return IntakeSubsystem.rpm(rightTicksPerSecond, ticksPerRev);
    }

    /** Left motor current from the last {@link #update()}, amps; NaN while idle (read only while spinning). */
    public double leftCurrentAmps() {
        return leftCurrentAmps;
    }

    /** Right motor current from the last {@link #update()}, amps; NaN while idle. */
    public double rightCurrentAmps() {
        return rightCurrentAmps;
    }

    /**
     * The battery's latest reading, volts, from {@code RobotOpMode}'s once-a-second read (no hub
     * read here). Used only for {@link #compensateF}; NaN means no compensation.
     */
    public void batteryVolts(double volts) {
        batteryVolts = volts;
    }

    /** The left wheel's velocity PIDF as read at init, or null when the hub gave none. */
    public PIDFCoefficients hubDefaults() {
        return leftDefaults;
    }

    /** The P written to the hub last, or NaN while the hub's own gains stand untouched. Likewise I, D and F below. */
    public double appliedP() {
        return appliedP;
    }

    public double appliedI() {
        return appliedI;
    }

    public double appliedD() {
        return appliedD;
    }

    /** The F written last, battery scale included. */
    public double appliedF() {
        return appliedF;
    }

    /** The scale on F in force: 1 unless {@link #compensateF} is on and a battery reading came in. */
    public double fScale() {
        return fScale;
    }

    /** How many times the gains were written to the hub this OpMode, so the log can mark each write. */
    public int pidfWrites() {
        return pidfWrites;
    }

    /** A one-line description of what is wrong, or null when nothing is. */
    public String fault() {
        if (layout == Layout.Y_CABLED && leftFault) return "launcher reads no speed while commanded: encoder cable?";
        if (leftFault && rightFault) return "both wheels read no speed while commanded: encoder cables?";
        if (leftFault) return "left wheel reads no speed while commanded: encoder cable?";
        if (rightFault) return "right wheel reads no speed while commanded: encoder cable?";
        return null;
    }

    /** Start idle, coasting, with the hub's velocity loop selected. */
    @Override
    public void initialize() {
        spinDown();
        started = false;
        reversing = false;
        for (DcMotorEx motor : new DcMotorEx[] {left, right}) {
            if (motor == null) continue;
            motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
            motor.setPower(0.0);
        }
        // What the hub has now, so NaN can mean "these" and the tuner starts from known numbers.
        leftDefaults = left.getPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER);
        rightDefaults = right == null ? null : right.getPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER);
        seenP = seenI = seenD = seenF = seenNominal = Double.NaN;
        seenCompensate = false;
        fScale = 1.0;
        fScaleCheck.reset();
        appliedP = appliedI = appliedD = appliedF = Double.NaN;
        pidfWrites = 0;
    }

    /**
     * PLAY: from now on an unjam may run the wheels backwards. Spinning up already needed PLAY,
     * since only an OpMode's loop asks for it.
     */
    @Override
    public void start() {
        started = true;
    }

    /**
     * While the feeder reverses, both wheels run backwards at {@link #reverseTicksPerSecond}
     * ({@link #reverseFor}) and nothing else is judged. Otherwise a velocity write per wheel when
     * its target changes (and every {@link #RESEND_MS}), one read of each speed (from the bulk read)
     * and, while running, one of each current every 100 ms. A wheel that has been commanded for
     * {@link #encoderFaultAfterMs} and still reads nothing is cut until {@link #spinDown()}.
     */
    @Override
    public void update() {
        long now = System.nanoTime();
        applyGains(now);
        double leftTarget = leftTarget();
        double rightTarget = rightTarget();

        // Y-cabled: one motor object, one encoder, so the right side mirrors the left.
        leftTicksPerSecond = Math.abs(left.getVelocity());
        rightTicksPerSecond = right == null ? leftTicksPerSecond : Math.abs(right.getVelocity());

        boolean reverse = reverseFor(feeder.state(), started);
        if (reverse != reversing) {
            // Into or out of the unjam: neither counts as a shot, and at-speed starts over. Out of
            // it while spinning, the spin-up starts over too, so the wheels passing through zero on
            // their way back up are not taken for a silent encoder.
            reversing = reverse;
            readiness.reset();
            shots.reset();
            atSpeed = false;
            if (!reverse && state == State.SPINNING) spinUpStartedNs = now;
        }
        if (measuring && (reverse || state != State.SPINNING)) {
            // Not spinning toward a target any more: a timing in progress means nothing.
            response.cancel();
            measuring = false;
        }
        if (reverse) {
            boolean force = resend.due(now);
            leftSent = command(left, leftFault ? 0.0 : reverseCommandFor(reverseTicksPerSecond, leftReversed),
                    leftSent, force);
            if (right != null) {
                rightSent = command(right, rightFault ? 0.0 : reverseCommandFor(reverseTicksPerSecond, rightReversed),
                        rightSent, force);
            }
            if (currentRead.due(now)) {
                leftCurrentAmps = left.getCurrent(CurrentUnit.AMPS);
                rightCurrentAmps = right == null ? Double.NaN : right.getCurrent(CurrentUnit.AMPS);
            }
        } else if (state == State.SPINNING) {
            double elapsedMs = (now - spinUpStartedNs) / 1e6;
            if (!leftFault && encoderSilent(leftTarget, leftTicksPerSecond, elapsedMs,
                    encoderFaultAfterMs, encoderSilentTicksPerSecond)) {
                leftFault = true;
            }
            if (right != null && !rightFault && encoderSilent(rightTarget, rightTicksPerSecond, elapsedMs,
                    encoderFaultAfterMs, encoderSilentTicksPerSecond)) {
                rightFault = true;
            }
            boolean force = resend.due(now);
            leftSent = command(left, leftFault ? 0.0 : commandFor(leftTarget, leftReversed), leftSent, force);
            if (right != null) {
                rightSent = command(right, rightFault ? 0.0 : commandFor(rightTarget, rightReversed),
                        rightSent, force);
            }
            if (currentRead.due(now)) {
                leftCurrentAmps = left.getCurrent(CurrentUnit.AMPS);
                rightCurrentAmps = right == null ? Double.NaN : right.getCurrent(CurrentUnit.AMPS);
            }
            boolean within = !leftFault && !rightFault && (leftTarget > 0.0 || rightTarget > 0.0)
                    && Math.abs(leftTicksPerSecond - leftTarget) <= toleranceTicksPerSecond
                    && Math.abs(rightTicksPerSecond - rightTarget) <= toleranceTicksPerSecond;
            atSpeed = readiness.update(within, now, atSpeedHoldMs);
            // A target change (Panels nudge, a table lookup) re-arms the detector: not a shot.
            boolean newTarget = leftTarget != lastLeftTarget || rightTarget != lastRightTarget;
            if (newTarget) shots.reset();
            shots.update(deficit(leftTarget, leftTicksPerSecond, rightTarget, rightTicksPerSecond),
                    bothDeficit(leftTarget, leftTicksPerSecond, rightTarget, rightTicksPerSecond), within, gateOpen.getAsBoolean(), target(), shotDipTicksPerSecond);
            // A step starts here: from idle, out of an unjam, or to a new target.
            if (!measuring || newTarget) {
                response.command(now, Math.min(leftTicksPerSecond, rightTicksPerSecond));
                measuring = true;
            }
            if (leftFault || rightFault) {
                response.cancel();
            } else {
                response.update(now, within, atSpeed,
                        excess(leftTarget, leftTicksPerSecond, rightTarget, rightTicksPerSecond),
                        shots.inDip(), shots.justCounted(), shots.lastDipTicksPerSecond());
            }
        } else {
            left.setPower(0.0);
            if (right != null) right.setPower(0.0);
            leftSent = Double.NaN;
            rightSent = Double.NaN;
            resend.reset();
            currentRead.reset();
            leftCurrentAmps = Double.NaN;
            rightCurrentAmps = Double.NaN;
            atSpeed = false;
        }
        lastLeftTarget = leftTarget;
        lastRightTarget = rightTarget;
    }

    /**
     * Writes the velocity PIDF when a Panels gain or the switch changed, or the battery's scale on
     * F moved by more than {@link #F_COMPENSATION_STEP} (looked at once a second). Nothing is
     * written while every gain is NaN, F is unscaled and nothing was written before: the hub's own
     * gains stand. A gain set back to NaN writes what was read at init. Gains are not motion, so
     * this runs from INIT on.
     */
    private void applyGains(long now) {
        boolean changed = !same(velocityP, seenP) || !same(velocityI, seenI) || !same(velocityD, seenD)
                || !same(velocityF, seenF) || compensateF != seenCompensate || !same(nominalVolts, seenNominal);
        if (changed) {
            seenP = velocityP;
            seenI = velocityI;
            seenD = velocityD;
            seenF = velocityF;
            seenCompensate = compensateF;
            seenNominal = nominalVolts;
        }
        double scale = VoltageCompensation.scale(compensateF, nominalVolts, batteryVolts);
        if (scale != fScale && (changed
                || (VoltageCompensation.worthApplying(fScale, scale, F_COMPENSATION_STEP) && fScaleCheck.due(now)))) {
            fScale = scale;
            changed = true;
        }
        if (!changed) return;
        boolean nothingSet = !validGain(velocityP) && !validGain(velocityI) && !validGain(velocityD)
                && !validGain(velocityF) && fScale == 1.0;
        if (nothingSet && Double.isNaN(appliedP)) return;
        if (writeGains(left, leftDefaults, true) && right != null) {
            writeGains(right, rightDefaults, false);
        }
    }

    /**
     * One wheel's gains: each Panels gain or, where it is unset, that wheel's own read at init; F
     * times the battery scale. Not written when any gain is still unknown (the hub gave no
     * defaults and Panels has not set it). Returns whether it wrote.
     */
    private boolean writeGains(DcMotorEx motor, PIDFCoefficients defaults, boolean record) {
        double p = gain(velocityP, defaults == null ? Double.NaN : defaults.p);
        double i = gain(velocityI, defaults == null ? Double.NaN : defaults.i);
        double d = gain(velocityD, defaults == null ? Double.NaN : defaults.d);
        double f = gain(velocityF, defaults == null ? Double.NaN : defaults.f) * fScale;
        if (Double.isNaN(p) || Double.isNaN(i) || Double.isNaN(d) || Double.isNaN(f)) return false;
        motor.setVelocityPIDFCoefficients(p, i, d, f);
        if (record) {
            appliedP = p;
            appliedI = i;
            appliedD = d;
            appliedF = f;
            pidfWrites++;
        }
        return true;
    }

    /**
     * A gain to write: the Panels value when it is a real one (finite, 0 to {@link #MAX_GAIN}),
     * otherwise {@code hubDefault}. So NaN, a negative or an absurd typo keeps the hub's gain. Pure.
     */
    static double gain(double configured, double hubDefault) {
        return validGain(configured) ? configured : hubDefault;
    }

    private static boolean validGain(double value) {
        return value >= 0.0 && value <= MAX_GAIN;
    }

    /** Equal, counting NaN equal to NaN: a Panels field left at NaN is not a change. */
    private static boolean same(double a, double b) {
        return Double.compare(a, b) == 0;
    }

    /** Cut power. Safe to call whether or not {@link #initialize()} ran. */
    @Override
    public void stop() {
        state = State.IDLE;
        left.setPower(0.0);
        if (right != null) right.setPower(0.0);
    }

    @Override
    public String health() {
        String fault = fault();
        return "motors configured (" + layout + "); presence unknown until they spin"
                + "; hub velocity PIDF at init " + pidfText(leftDefaults)
                + (right != null && !samePidf(leftDefaults, rightDefaults) ? " (right " + pidfText(rightDefaults) + ")" : "")
                + (fault != null ? "; " + fault : "");
    }

    /** The velocity PIDF line for the Robot page and the bench: what the hub had, what is written, the battery's scale. */
    public String pidfLine() {
        String applied = Double.isNaN(appliedP) ? "hub's own (nothing set in Panels)"
                : gainsText(appliedP, appliedI, appliedD, appliedF);
        String comp = !compensateF ? ""
                : Double.isNaN(batteryVolts) ? " · F compensation on, no battery reading"
                : String.format(Locale.US, " · F × %.3f at %.2f V", fScale, batteryVolts);
        return "Velocity PIDF " + applied + " · at init " + pidfText(leftDefaults) + comp;
    }

    private static String pidfText(PIDFCoefficients c) {
        return c == null ? "unread" : gainsText(c.p, c.i, c.d, c.f);
    }

    /** Four gains as "P 10 I 3 D 0 F 13.5": four significant figures, no trailing zeros. */
    public static String gainsText(double p, double i, double d, double f) {
        return "P " + gainText(p) + " I " + gainText(i) + " D " + gainText(d) + " F " + gainText(f);
    }

    static String gainText(double value) {
        if (Double.isNaN(value)) return "NaN";
        String text = String.format(Locale.US, "%.4g", value);
        if (text.contains("e") || !text.contains(".")) return text;
        text = text.replaceAll("0+$", "");
        return text.endsWith(".") ? text.substring(0, text.length() - 1) : text;
    }

    private static boolean samePidf(PIDFCoefficients a, PIDFCoefficients b) {
        if (a == null || b == null) return a == b;
        return a.p == b.p && a.i == b.i && a.d == b.d && a.f == b.f;
    }

    @Override
    public void describe(Display display) {
        String fault = fault();
        if (fault != null) {
            display.status("Launcher", Display.Level.FAULT, fault);
        } else if (reversing) {
            display.status("Launcher", Display.Level.WARN, String.format(Locale.US,
                    "REVERSING for the unjam at %.0f ticks/s · back to %s on release",
                    nonNegative(reverseTicksPerSecond), state == State.SPINNING ? "launch speed" : "idle"));
        } else if (state == State.IDLE) {
            display.status("Launcher", Display.Level.OK, "idle");
        } else {
            double l = leftTarget();
            double r = rightTarget();
            display.status("Launcher", atSpeed ? Display.Level.OK : Display.Level.WARN,
                    String.format(Locale.US, "%s · target %s ticks/s", atSpeed ? "AT SPEED" : "spinning up",
                            l == r ? String.format(Locale.US, "%.0f", l)
                                    : String.format(Locale.US, "L %.0f / R %.0f", l, r)));
        }
        if (layout == Layout.Y_CABLED) {
            double rpm = leftRpm();
            display.line(String.format(Locale.US, "Y-cabled pair %.0f ticks/s%s · %s · one encoder, trims unused",
                    leftTicksPerSecond,
                    Double.isNaN(rpm) ? "" : String.format(Locale.US, " (%.0f rpm)", rpm),
                    Double.isNaN(leftCurrentAmps) ? "current not read while idle"
                            : String.format(Locale.US, "%.1f A (both motors)", leftCurrentAmps)));
            display.line(shotsLine());
            display.line(responseLine());
            display.line(pidfLine());
            return;
        }
        display.line(String.format(Locale.US, "Wheels L %.0f / R %.0f ticks/s%s · surface %s · %s",
                leftTicksPerSecond, rightTicksPerSecond, rpmSuffix(), surfaceText(surfaceInchesPerSecond()),
                Double.isNaN(leftCurrentAmps) ? "current not read while idle"
                        : String.format(Locale.US, "L %.1f / R %.1f A", leftCurrentAmps, rightCurrentAmps)));
        display.line(shotsLine());
        display.line(responseLine());
        display.line(pidfLine());
    }

    /** The shot count and the last dip, for the Robot page and the bench. */
    public String shotsLine() {
        int n = shots.shots();
        String last = n == 0 ? "no shots yet"
                : String.format(Locale.US, "last dip %.0f ticks/s below a %.0f target",
                        shots.lastDipTicksPerSecond(), shots.lastTargetTicksPerSecond());
        return String.format(Locale.US, "Shots %d · %s%s", n, last, shots.inDip() ? " · IN A DIP" : "");
    }

    /** The last spin-up's and the last dip's timings (LauncherResponse), for the Robot page and the bench. */
    public String responseLine() {
        String spin = response.spinUps() == 0 ? "no spin-up timed yet"
                : String.format(Locale.US, "spin-up rise %.0f ms, over %.0f, at speed %.0f ms",
                        response.riseMs(), response.spinOvershootTps(), response.settleMs());
        String dip = response.dips() == 0 ? "no dip timed yet"
                : String.format(Locale.US, "dip %.0f, back %.0f ms, over %.0f, at speed %s, gap %s",
                        response.dipDepthTps(), response.recoverMs(), response.dipOvershootTps(),
                        Double.isNaN(response.resettleMs()) ? "never"
                                : String.format(Locale.US, "%.0f ms", response.resettleMs()),
                        Double.isNaN(response.shotGapMs()) ? "first"
                                : String.format(Locale.US, "%.0f ms", response.shotGapMs()));
        return spin + " · " + dip;
    }

    /** A surface speed for a page: "123 in/s", or "unknown (not measured)" while it is NaN. */
    public static String surfaceText(double inchesPerSecond) {
        return Double.isNaN(inchesPerSecond) ? "unknown (not measured)"
                : String.format(Locale.US, "%.0f in/s", inchesPerSecond);
    }

    private String rpmSuffix() {
        double l = leftRpm();
        double r = rightRpm();
        return Double.isNaN(l) || Double.isNaN(r) ? "" : String.format(Locale.US, " (%.0f / %.0f rpm)", l, r);
    }

    /**
     * Sends {@code ticksPerSecond} unless it is what was last sent and {@code force} is false.
     * Returns what now stands.
     */
    private static double command(DcMotorEx motor, double ticksPerSecond, double lastSent, boolean force) {
        if (!force && ticksPerSecond == lastSent) return lastSent;
        if (ticksPerSecond == 0.0) {
            motor.setPower(0.0);
        } else {
            motor.setVelocity(ticksPerSecond);
        }
        return ticksPerSecond;
    }

    /**
     * The velocity command for one wheel: the target, negated when that wheel is reversed. Pure,
     * so a test can check it. A negative or NaN target commands nothing.
     */
    static double commandFor(double target, boolean reversed) {
        double t = nonNegative(target);
        return reversed ? -t : t;
    }

    /** Whether the wheels run backwards: the feeder is reversing (an unjam) and PLAY has happened. Pure. */
    static boolean reverseFor(FeederSubsystem.State feeder, boolean started) {
        return started && feeder == FeederSubsystem.State.REVERSE;
    }

    /**
     * The velocity command for one wheel while reversing: {@code speed} backwards in that wheel's
     * own sense ({@link #commandFor} negated, so the wiring flip still applies). A zero, negative
     * or NaN speed commands nothing. Pure, so a test can check it.
     */
    static double reverseCommandFor(double speed, boolean reversed) {
        double forward = commandFor(speed, reversed);
        return forward == 0.0 ? 0.0 : -forward;
    }

    /**
     * True when a wheel has been commanded to {@code target} for {@code elapsedMs} or longer and
     * still measures below {@code silentFloor}: the signature of a motor with no encoder feedback.
     * Never true for a zero target, in the first {@code faultAfterMs}, or once the wheel moves.
     */
    static boolean encoderSilent(double target, double measured, double elapsedMs,
                                 double faultAfterMs, double silentFloor) {
        return target > 0.0 && elapsedMs >= faultAfterMs && Math.abs(measured) < silentFloor;
    }

    /**
     * One wheel's target: the common target plus that wheel's trim, clamped to [0, ∞). A NaN trim
     * counts as zero, so a Panels typo in a trim leaves the wheel at the common speed.
     */
    static double wheelTarget(double common, double trim) {
        return nonNegative(nonNegative(common) + (Double.isNaN(trim) ? 0.0 : trim));
    }

    /**
     * How far below target the slower wheel is, ticks per second: the larger of the two wheels'
     * shortfalls, so one wheel dragged down by a piece shows even if the other holds. Negative when
     * both are over target. Pure, so a test can check it.
     */
    static double deficit(double leftTarget, double leftMeasured, double rightTarget, double rightMeasured) {
        return Math.max(leftTarget - leftMeasured, rightTarget - rightMeasured);
    }

    /**
     * How far below target both wheels are, ticks per second: the smaller of the two shortfalls, so
     * it passes a threshold only when both wheels do (a shot's dip: class comment). Pure.
     */
    static double bothDeficit(double leftTarget, double leftMeasured, double rightTarget, double rightMeasured) {
        return Math.min(leftTarget - leftMeasured, rightTarget - rightMeasured);
    }

    /**
     * How far over its target the faster wheel is, ticks per second: the larger of the two wheels'
     * excesses, negative when both are under. The mirror of {@link #deficit}. Pure.
     */
    static double excess(double leftTarget, double leftMeasured, double rightTarget, double rightMeasured) {
        return Math.max(leftMeasured - leftTarget, rightMeasured - rightTarget);
    }

    /** Clamps a Panels-editable speed to [0, ∞) and treats NaN as 0: a typo spins down, never backwards. */
    static double nonNegative(double value) {
        return value > 0.0 ? value : 0.0;
    }

    /**
     * Counts shots from speed dips (class comment): armed once the wheels are within tolerance,
     * a dip starts when both wheels' shortfalls reach the threshold with the feed gate open, and the
     * shot is counted when the wheels are back within tolerance, with the deepest point of the dip. A dip that never recovers (a
     * jam, or a spin-down) is never counted. Pure, so a test can drive it with made-up readings.
     */
    static final class ShotDetector {
        private boolean armed;
        private boolean inDip;
        private double deepest;
        private double dipTarget;
        private int shots;
        private double lastDip = Double.NaN;
        private double lastTarget = Double.NaN;
        private boolean justCounted;

        /**
         * Feed this loop's reading. {@code deficit} is how far below target the slower wheel is (the
         * depth recorded), {@code bothDeficit} how far below target both wheels are (a dip starts only
         * once it reaches the threshold), {@code within} whether both wheels are inside the at-speed tolerance, {@code gateOpen}
         * whether a piece can reach the wheels (a dip only starts while it is), {@code target} the
         * common target in force. Returns true in the loop a shot is counted.
         */
        boolean update(double deficit, double bothDeficit, boolean within, boolean gateOpen, double target,
                       double dipThreshold) {
            justCounted = false;
            if (within) {
                if (inDip) {
                    shots++;
                    lastDip = deepest;
                    lastTarget = dipTarget;
                    inDip = false;
                    justCounted = true;
                }
                armed = true;
                return justCounted;
            }
            if (inDip) {
                if (deficit > deepest) deepest = deficit;
            } else if (armed && gateOpen && bothDeficit >= dipThreshold) {
                inDip = true;
                deepest = deficit;
                dipTarget = target;
            }
            return false;
        }

        /** Forget the arming and any dip in progress; the count and the last shot stay. */
        void reset() {
            armed = false;
            inDip = false;
            deepest = 0.0;
            justCounted = false;
        }

        int shots() {
            return shots;
        }

        boolean justCounted() {
            return justCounted;
        }

        boolean inDip() {
            return inDip;
        }

        double lastDipTicksPerSecond() {
            return lastDip;
        }

        double lastTargetTicksPerSecond() {
            return lastTarget;
        }
    }

    /**
     * Says "at speed" only after the wheels have been within tolerance continuously for a hold
     * time, so a wheel passing through the band on its way up does not fire the feeder. Pure
     * timing, no hardware, so a test can drive it with made-up clocks.
     */
    static final class AtSpeedTimer {
        private long withinSinceNs = -1;

        /** Feed the latest reading; returns whether the hold has been satisfied. */
        boolean update(boolean withinTolerance, long nowNs, double holdMs) {
            if (!withinTolerance) {
                withinSinceNs = -1;
                return false;
            }
            if (withinSinceNs < 0) {
                withinSinceNs = nowNs;
            }
            return (nowNs - withinSinceNs) >= holdMs * 1e6;
        }

        void reset() {
            withinSinceNs = -1;
        }
    }
}
