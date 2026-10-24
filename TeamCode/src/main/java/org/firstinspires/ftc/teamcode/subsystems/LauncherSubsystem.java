package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.hardware.DeviceNames;

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
 * target in encoder ticks per second with {@code setVelocity}. That is one write per wheel per
 * loop and no controller of ours to tune. If the hub's default loop turns out sluggish on the
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
 * <p>Nothing here is measured yet. The directions, the starting setpoint and the tolerance are
 * {@code @Configurable} so the bench sets them in Panels; the values that work get written back
 * here with the robot they were checked on.
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
     * The speed both wheels are asked to hold while spinning, encoder ticks per second. <b>A
     * starting point for the bench, not a measured value.</b> A goBILDA 5203 counts 28 ticks per
     * motor revolution, so a 6000 RPM bare motor tops out near 2800; 1000 is a gentle first spin.
     * The value a POLLEN and a NECTAR each leave cleanly at is the first row of #80's table.
     */
    public static double targetTicksPerSecond = 1000.0;

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
     * Flips the left wheel so that a positive command throws a piece out. <b>Not yet checked on a
     * robot:</b> {@code false} is the SDK's default direction, not a fact about the wiring. The two
     * modules are mirror images, so expect exactly one of the pair to need flipping.
     */
    public static boolean leftReversed = false;

    /** As {@link #leftReversed}, for the right wheel. */
    public static boolean rightReversed = false;

    /**
     * Encoder ticks per revolution of each flywheel motor's output shaft, for the RPM display.
     * <b>NaN until read off the motor's label</b> (see {@link IntakeSubsystem#ticksPerRev} for the
     * goBILDA 5203 table). Speed is shown in ticks/s regardless; RPM only once this is set.
     */
    public static double ticksPerRev = Double.NaN;

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
    private double leftTicksPerSecond;
    private double rightTicksPerSecond;
    private double leftCurrentAmps = Double.NaN;
    private double rightCurrentAmps = Double.NaN;
    private long spinUpStartedNs = -1;
    private boolean leftFault;
    private boolean rightFault;
    private final AtSpeedTimer readiness = new AtSpeedTimer();
    private boolean atSpeed;

    /** The robot's launcher: looks both motors up once. A missing motor fails init with the SDK's own message. */
    public LauncherSubsystem(HardwareMap hardwareMap) {
        layout = Layout.TWO_MOTORS;
        left = hardwareMap.get(DcMotorEx.class, DeviceNames.LAUNCHER_LEFT);
        right = hardwareMap.get(DcMotorEx.class, DeviceNames.LAUNCHER_RIGHT);
    }

    private LauncherSubsystem(DcMotorEx pair) {
        layout = Layout.Y_CABLED;
        left = pair;
        right = null;
    }

    /**
     * The bench rig's launcher: both motors on one port through a Y cable, one device named
     * {@link DeviceNames#LAUNCHER}. An explicit decision, not a missing device being tolerated.
     */
    public static LauncherSubsystem yCabled(HardwareMap hardwareMap) {
        return new LauncherSubsystem(hardwareMap.get(DcMotorEx.class, DeviceNames.LAUNCHER));
    }

    /** How the motors are wired; see {@link Layout}. */
    public Layout layout() {
        return layout;
    }

    /** Ask both wheels to hold {@link #targetTicksPerSecond}. Takes effect in the next {@link #update()}. */
    public void spinUp() {
        if (state != State.SPINNING) {
            spinUpStartedNs = System.nanoTime();
            readiness.reset();
        }
        state = State.SPINNING;
    }

    /**
     * Ask both wheels to hold a given speed, ticks per second. Sets {@link #targetTicksPerSecond},
     * so Panels shows the number in force wherever it came from; an Auto or an aiming table calls
     * this, a gamepad calls {@link #spinUp()}.
     */
    public void spinUp(double ticksPerSecond) {
        targetTicksPerSecond = ticksPerSecond;
        spinUp();
    }

    /** Let both wheels coast to a stop. Not {@link #stop()}: the subsystem keeps running. Clears any encoder fault. */
    public void spinDown() {
        state = State.IDLE;
        spinUpStartedNs = -1;
        leftFault = false;
        rightFault = false;
        readiness.reset();
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

    /**
     * Both wheels have held the target within {@link #toleranceTicksPerSecond} for
     * {@link #atSpeedHoldMs}. The feeder's green light. False while idle or faulted.
     */
    public boolean atSpeed() {
        return atSpeed;
    }

    /** The common speed in force, ticks per second: {@link #targetTicksPerSecond} clamped the way the wheels get it. */
    public double target() {
        return nonNegative(targetTicksPerSecond);
    }

    /** The left wheel's own target: the common target plus its trim, never below zero. */
    public double leftTarget() {
        return wheelTarget(targetTicksPerSecond, leftTrimTicksPerSecond);
    }

    /**
     * The right wheel's own target: the common target plus its trim, never below zero. Y-cabled,
     * there is one signal, so this is {@link #leftTarget()}.
     */
    public double rightTarget() {
        return layout == Layout.Y_CABLED ? leftTarget()
                : wheelTarget(targetTicksPerSecond, rightTrimTicksPerSecond);
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
        for (DcMotorEx motor : new DcMotorEx[] {left, right}) {
            if (motor == null) continue;
            motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
            motor.setPower(0.0);
        }
    }

    /**
     * One velocity write per wheel, then one read of each speed (from the bulk read) and, while
     * spinning, one of each current. A wheel that has been commanded for {@link #encoderFaultAfterMs}
     * and still reads nothing is cut until {@link #spinDown()}.
     */
    @Override
    public void update() {
        long now = System.nanoTime();
        double leftTarget = leftTarget();
        double rightTarget = rightTarget();

        // Y-cabled: one motor object, one encoder, so the right side mirrors the left.
        leftTicksPerSecond = Math.abs(left.getVelocity());
        rightTicksPerSecond = right == null ? leftTicksPerSecond : Math.abs(right.getVelocity());

        if (state == State.SPINNING) {
            double elapsedMs = (now - spinUpStartedNs) / 1e6;
            if (!leftFault && encoderSilent(leftTarget, leftTicksPerSecond, elapsedMs,
                    encoderFaultAfterMs, encoderSilentTicksPerSecond)) {
                leftFault = true;
            }
            if (right != null && !rightFault && encoderSilent(rightTarget, rightTicksPerSecond, elapsedMs,
                    encoderFaultAfterMs, encoderSilentTicksPerSecond)) {
                rightFault = true;
            }
            command(left, leftFault ? 0.0 : commandFor(leftTarget, leftReversed));
            if (right != null) command(right, rightFault ? 0.0 : commandFor(rightTarget, rightReversed));
            leftCurrentAmps = left.getCurrent(CurrentUnit.AMPS);
            rightCurrentAmps = right == null ? Double.NaN : right.getCurrent(CurrentUnit.AMPS);
            boolean within = !leftFault && !rightFault && (leftTarget > 0.0 || rightTarget > 0.0)
                    && Math.abs(leftTicksPerSecond - leftTarget) <= toleranceTicksPerSecond
                    && Math.abs(rightTicksPerSecond - rightTarget) <= toleranceTicksPerSecond;
            atSpeed = readiness.update(within, now, atSpeedHoldMs);
        } else {
            left.setPower(0.0);
            if (right != null) right.setPower(0.0);
            leftCurrentAmps = Double.NaN;
            rightCurrentAmps = Double.NaN;
            atSpeed = false;
        }
    }

    /** Cut power. Safe to call whether or not {@link #initialize()} ran. */
    @Override
    public void stop() {
        state = State.IDLE;
        left.setPower(0.0);
        if (right != null) right.setPower(0.0);
    }

    @Override
    public void describe(Display display) {
        String fault = fault();
        if (fault != null) {
            display.status("Launcher", Display.Level.FAULT, fault);
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
            return;
        }
        display.line(String.format(Locale.US, "Wheels L %.0f / R %.0f ticks/s%s · %s",
                leftTicksPerSecond, rightTicksPerSecond, rpmSuffix(),
                Double.isNaN(leftCurrentAmps) ? "current not read while idle"
                        : String.format(Locale.US, "L %.1f / R %.1f A", leftCurrentAmps, rightCurrentAmps)));
    }

    private String rpmSuffix() {
        double l = leftRpm();
        double r = rightRpm();
        return Double.isNaN(l) || Double.isNaN(r) ? "" : String.format(Locale.US, " (%.0f / %.0f rpm)", l, r);
    }

    private static void command(DcMotorEx motor, double ticksPerSecond) {
        if (ticksPerSecond == 0.0) {
            motor.setPower(0.0);
        } else {
            motor.setVelocity(ticksPerSecond);
        }
    }

    /**
     * The velocity command for one wheel: the target, negated when that wheel is reversed. Pure,
     * so a test can check it. A negative or NaN target commands nothing.
     */
    static double commandFor(double target, boolean reversed) {
        double t = nonNegative(target);
        return reversed ? -t : t;
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

    /** Clamps a Panels-editable speed to [0, ∞) and treats NaN as 0: a typo spins down, never backwards. */
    static double nonNegative(double value) {
        return value > 0.0 ? value : 0.0;
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
