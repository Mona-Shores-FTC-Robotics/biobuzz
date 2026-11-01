package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.hardware.DeviceNames;
import org.firstinspires.ftc.teamcode.util.Throttle;

import java.util.Locale;

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
 * no controller of ours to tune. If the hub's default loop turns out sluggish on the
 * bench, the knob is {@code setVelocityPIDFCoefficients}, not a controller here; the {@code
 * launcher2} rig has the hand-rolled alternative if it ever comes to that.
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
 * shot per dip: the wheels were at speed, fell at least {@link #shotDipTicksPerSecond} below target,
 * and came back within tolerance. Spin-up is not a dip (the wheels were never at speed), a wobble
 * inside the threshold is not a dip, and a target change re-arms the detector so a Panels nudge is
 * not a shot. The count and the last dip's depth go to the Robot page, Panels and the match log; a
 * student writes hit or miss next to each row afterwards. That is how #80's speed-vs-distance
 * table gets its rows. It is a measurement, never a gate: nothing here waits for it.
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
     * default for the TeleOp and the Launcher Bench, adjustable live in Panels (and by the bench's
     * D-pad and the TeleOp's trim). <b>A starting point, not a measured value.</b> A goBILDA 5203
     * counts 28 ticks per motor revolution, so a 6000 RPM bare motor tops out near 2800; 1525 is
     * where POLLEN and NECTAR both shoot. The value each leaves cleanly at is the first row of
     * #80's table.
     */
    public static double targetTicksPerSecond = 1525.0;

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
     * How far below its target a wheel must fall, from at speed, for the dip to count as a shot,
     * ticks per second. <b>A starting point, not measured:</b> read the real dip off the Panels
     * graph on the first shots and set this to about half of it.
     */
    public static double shotDipTicksPerSecond = 150.0;

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
     * Speed both wheels run backwards while the feeder reverses (an unjam), ticks per second. 300 is
     * about 640 motor RPM, a fifth of the launch speed: enough to roll a ball sitting on top of the
     * wheels back down onto the reversing feeder, not enough to fling it back down the lane. The
     * hub's velocity loop holds it, so it is the same on a fresh or a tired battery. 0 turns the
     * reverse off. <b>Not yet tuned:</b> set it in Panels at the lab and write it back here.
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
     * A wheel commanded for this long that still reads below {@link #encoderSilentTicksPerSecond}
     * is treated as having no encoder and is cut. Long enough for a real spin-up to show movement.
     */
    public static double encoderFaultAfterMs = 500.0;

    /** Below this measured speed, a commanded wheel is "not moving" for the encoder check. */
    public static double encoderSilentTicksPerSecond = 20.0;

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
    }

    private LauncherSubsystem(DcMotorEx pair, FeederSubsystem feeder) {
        layout = Layout.Y_CABLED;
        left = pair;
        right = null;
        this.feeder = feeder;
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
            if (leftTarget != lastLeftTarget || rightTarget != lastRightTarget) shots.reset();
            shots.update(deficit(leftTarget, leftTicksPerSecond, rightTarget, rightTicksPerSecond),
                    within, target(), shotDipTicksPerSecond);
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
                + (fault != null ? "; " + fault : "");
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
            return;
        }
        display.line(String.format(Locale.US, "Wheels L %.0f / R %.0f ticks/s%s · %s",
                leftTicksPerSecond, rightTicksPerSecond, rpmSuffix(),
                Double.isNaN(leftCurrentAmps) ? "current not read while idle"
                        : String.format(Locale.US, "L %.1f / R %.1f A", leftCurrentAmps, rightCurrentAmps)));
        display.line(shotsLine());
    }

    /** The shot count and the last dip, for the Robot page and the bench. */
    public String shotsLine() {
        int n = shots.shots();
        String last = n == 0 ? "no shots yet"
                : String.format(Locale.US, "last dip %.0f ticks/s below a %.0f target",
                        shots.lastDipTicksPerSecond(), shots.lastTargetTicksPerSecond());
        return String.format(Locale.US, "Shots %d · %s%s", n, last, shots.inDip() ? " · IN A DIP" : "");
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

    /** Clamps a Panels-editable speed to [0, ∞) and treats NaN as 0: a typo spins down, never backwards. */
    static double nonNegative(double value) {
        return value > 0.0 ? value : 0.0;
    }

    /**
     * Counts shots from speed dips (class comment): armed once the wheels are within tolerance,
     * a dip starts when the deficit reaches the threshold, and the shot is counted when the wheels
     * are back within tolerance, with the deepest point of the dip. A dip that never recovers (a
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
         * Feed this loop's reading. {@code deficit} is how far below target the slower wheel is,
         * {@code within} whether both wheels are inside the at-speed tolerance, {@code target} the
         * common target in force. Returns true in the loop a shot is counted.
         */
        boolean update(double deficit, boolean within, double target, double dipThreshold) {
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
            } else if (armed && deficit >= dipThreshold) {
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
