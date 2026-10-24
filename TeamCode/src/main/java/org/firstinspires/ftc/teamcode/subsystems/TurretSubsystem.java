package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.hardware.DeviceNames;

import java.util.Locale;

/**
 * The turret: a continuous servo ({@link DeviceNames#TURRET}) turning it, and a Studica Cypher Max
 * encoder on its drive, read as a relative quadrature count through the encoder socket of
 * {@link DeviceNames#TURRET_ENCODER_HOST}. Built from the {@code Turret Bench} session of 24 Oct
 * 2026 on 20245; every number below was measured there.
 *
 * <h2>What the OpMode asks for</h2>
 *
 * <ul>
 *   <li>{@link #zeroHere()}: the turret is physically at home (its mark lined up, pointing along
 *       the robot's nose). The count is relative, so a person does this before INIT ends, and
 *       nothing here can tell if they forgot: {@link #zeroed()} only says whether it was asked.</li>
 *   <li>{@link #aim(double)}: hold a chassis-frame angle, degrees counter-clockwise from the
 *       nose. The aim math that produces it is {@link TurretAim}; which target is the OpMode's.</li>
 *   <li>{@link #home()}: hold 0. {@link #idle()}: power off, hold nothing.</li>
 *   <li>{@link #manual(double)}: a power, counter-clockwise positive, for a bench or a stick.</li>
 * </ul>
 *
 * <p>{@link #update()} reads the encoder once, works out the power with
 * {@link TurretServoControl} and writes the servo, once per loop. Nothing blocks.
 *
 * <h2>Hardware it shares</h2>
 *
 * <p>The encoder host is another mechanism's motor. This class reads its position and velocity and
 * <b>never</b> sets its power or mode, and never resets its count: zero is a software offset, so
 * the feeder (today's host) is untouched.
 *
 * <p>Anything unmeasured on a given robot is a Panels tunable here rather than a per-robot
 * constant, because only 20245 has been on the bench. Once 19429 is measured, the two robots'
 * values move to a per-robot table keyed by {@code ActiveConfig.requireIdentity()}.
 */
@Configurable
public class TurretSubsystem implements Subsystem {

    // ------------------------------------------------------------ measured on the bench (20245)

    /** Encoder turns per turret turn: 2038 counts per flag turn on the bench, so 1 (24 Oct 2026). */
    public static double turnsPerTurretTurn = 1.0;

    /** +1 if the count rises as the turret turns counter-clockwise seen from above (bench, 24 Oct 2026). */
    public static int direction = 1;

    /** +1 if positive servo power turns the turret counter-clockwise (bench, 24 Oct 2026). */
    public static int servoDirection = 1;

    /** The power at which the servo stands still, [-0.2, 0.2]; see {@code Turret Bench}. 0 until measured. */
    public static double servoStopPower = 0.0;

    // The hold, as it held on the bench.
    public static double kP = 0.007;
    public static double minPower = 0.12;
    public static double maxPower = 1.0;
    public static double creepBelowDeg = 10.0;
    public static double holdToleranceDeg = 1.0;
    public static double creepOnMs = 30.0;
    public static double creepOffMs = 100.0;

    /**
     * Where the turret's axis sits from the robot's tracked point, inches, robot frame (+forward,
     * +left). <b>Not measured:</b> 0, 0 aims from the robot centre, which is within a couple of
     * degrees at HIVE range for an offset of a few inches. Tape it and write it here.
     */
    public static double turretForwardIn = 0.0;
    public static double turretLeftIn = 0.0;

    /** How far the turret may be asked to turn each way from home, degrees. Stops are the wires'; NaN = no clamp yet. */
    public static double travelEachWayDeg = Double.NaN;

    /**
     * A hold error past this means the servo is driving the wrong way, or the encoder is reading
     * the wrong way: the hold is dropped and the turret goes idle rather than spinning. The Robot
     * page says so. Flip {@link #servoDirection} (if the turret ran away from the target) or
     * {@link #direction} (if it ran but the angle went the other way), then aim again.
     */
    public static double runawayDeg = 180.0;

    // ------------------------------------------------------------ state

    private enum Mode { IDLE, HOLD, MANUAL }

    private final DcMotorEx encoderHost;
    private final CRServo servo;
    private final TurretServoControl control = new TurretServoControl();
    private final TurretServoControl.Gains gains = new TurretServoControl.Gains(
            kP, minPower, maxPower, creepBelowDeg, holdToleranceDeg, creepOnMs, creepOffMs);

    private TurretEncoder encoder;
    private double encoderTurnsSetting = Double.NaN;
    private int directionSetting;
    private String settingsProblem;

    private Mode mode = Mode.IDLE;
    private double targetDeg;
    private double manualPower;

    private long counts;
    private long zeroCounts;
    private boolean zeroed;
    private double angleDeg = Double.NaN;
    private double rateDps = Double.NaN;
    private double errorDeg = Double.NaN;
    private double powerSent;
    private String runawayNote;

    /** Looks both devices up once. A missing one fails init with the SDK's own message. */
    public TurretSubsystem(HardwareMap hardwareMap) {
        encoderHost = hardwareMap.get(DcMotorEx.class, DeviceNames.TURRET_ENCODER_HOST);
        servo = hardwareMap.get(CRServo.class, DeviceNames.TURRET);
    }

    // ------------------------------------------------------------ requests

    /** The turret is at home right now: angles are measured from here on. */
    public void zeroHere() {
        zeroCounts = counts;
        zeroed = true;
    }

    /** Hold {@code deg} from home, counter-clockwise positive, chassis frame. Clamped into the travel if known. */
    public void aim(double deg) {
        if (Double.isNaN(deg)) {
            return;
        }
        if (mode != Mode.HOLD) {
            runawayNote = null;
        }
        double limit = travelEachWayDeg;
        targetDeg = Double.isNaN(limit) || limit <= 0 ? deg : Math.max(-limit, Math.min(limit, deg));
        mode = Mode.HOLD;
    }

    /** Hold straight ahead. */
    public void home() {
        aim(0.0);
    }

    /** Power off; the turret stays wherever it is. */
    public void idle() {
        mode = Mode.IDLE;
    }

    /** Drive at {@code ccwPower} in [-1, 1], counter-clockwise positive, until the next request. */
    public void manual(double ccwPower) {
        manualPower = Double.isNaN(ccwPower) ? 0.0 : Math.max(-1.0, Math.min(1.0, ccwPower));
        mode = Mode.MANUAL;
    }

    // ------------------------------------------------------------ reads

    /** Degrees from home, counter-clockwise positive, not wrapped; NaN until the settings are valid. */
    public double angleDegrees() {
        return angleDeg;
    }

    /** Degrees per second, counter-clockwise positive. */
    public double rateDegreesPerSecond() {
        return rateDps;
    }

    /** The angle being held, or NaN when not holding. */
    public double targetDegrees() {
        return mode == Mode.HOLD ? targetDeg : Double.NaN;
    }

    /** Target minus angle, or NaN when not holding. */
    public double errorDegrees() {
        return errorDeg;
    }

    /** Holding, and within the tolerance of the target. */
    public boolean onTarget() {
        return mode == Mode.HOLD && !Double.isNaN(errorDeg) && Math.abs(errorDeg) <= gains.toleranceDeg;
    }

    /** Whether {@link #zeroHere()} has been called since this OpMode started. */
    public boolean zeroed() {
        return zeroed;
    }

    /** Whether the turret is holding an angle. */
    public boolean holding() {
        return mode == Mode.HOLD;
    }

    // ------------------------------------------------------------ lifecycle

    /** Power off, angle relative to wherever it sits until {@link #zeroHere()}. */
    @Override
    public void initialize() {
        mode = Mode.IDLE;
        servo.setPower(0.0);
        refreshSettings();
        counts = encoderHost.getCurrentPosition();
        zeroCounts = counts;
        zeroed = false;
    }

    /** One encoder read, one servo write. */
    @Override
    public void update() {
        refreshSettings();
        counts = encoderHost.getCurrentPosition();
        double countsPerSecond = encoderHost.getVelocity();
        if (encoder != null) {
            angleDeg = encoder.degrees(counts, zeroCounts);
            rateDps = encoder.degreesPerSecond(countsPerSecond);
        } else {
            angleDeg = Double.NaN;
            rateDps = Double.NaN;
        }

        double requested;
        switch (mode) {
            case HOLD:
                errorDeg = Double.isNaN(angleDeg) ? Double.NaN : targetDeg - angleDeg;
                if (Math.abs(errorDeg) > Math.max(finite(runawayDeg), 1.0)) {
                    runawayNote = String.format(Locale.US,
                            "RUNAWAY: %.0f° from target; hold dropped. Flip servoDirection (or direction) and aim again.",
                            errorDeg);
                    mode = Mode.IDLE;
                    requested = 0.0;
                    break;
                }
                requested = control.power(errorDeg, gains, System.nanoTime());
                break;
            case MANUAL:
                errorDeg = Double.NaN;
                requested = manualPower * gains.maxPower;
                break;
            default:
                errorDeg = Double.NaN;
                requested = 0.0;
                break;
        }
        powerSent = requested * (servoDirection == -1 ? -1 : 1);
        servo.setPower(clamp(powerSent + clamp(servoStopPower, 0.2), 1.0));
    }

    /** Final teardown: power off. */
    @Override
    public void stop() {
        servo.setPower(0.0);
    }

    @Override
    public void describe(Display display) {
        if (settingsProblem != null) {
            display.status("Turret", Display.Level.FAULT, settingsProblem);
            return;
        }
        String what;
        Display.Level level;
        switch (mode) {
            case HOLD:
                what = String.format(Locale.US, "%.1f° → %.1f° (%+.1f)", angleDeg, targetDeg, errorDeg);
                level = onTarget() ? Display.Level.OK : Display.Level.WARN;
                break;
            case MANUAL:
                what = String.format(Locale.US, "%.1f° · manual %+.2f", angleDeg, manualPower);
                level = Display.Level.OK;
                break;
            default:
                what = String.format(Locale.US, "%.1f° · idle", angleDeg);
                level = Display.Level.OK;
                break;
        }
        display.status("Turret", level, what + (zeroed ? "" : " · <b>NOT ZEROED</b>: put it at home, zero it"));
        if (runawayNote != null) {
            display.status("Turret", Display.Level.FAULT, runawayNote);
        }
    }

    /** Why the last hold was dropped, or null. Shown by the OpMode too. */
    public String runawayNote() {
        return runawayNote;
    }

    // ------------------------------------------------------------ helpers

    /** Pulls the Panels values in; rebuilds the converter only when its two inputs change. */
    private void refreshSettings() {
        gains.set(kP, minPower, maxPower, creepBelowDeg, holdToleranceDeg, creepOnMs, creepOffMs);
        if (encoderTurnsSetting == turnsPerTurretTurn && directionSetting == direction && encoder != null) {
            return;
        }
        encoderTurnsSetting = turnsPerTurretTurn;
        directionSetting = direction;
        try {
            encoder = new TurretEncoder(turnsPerTurretTurn, direction);
            settingsProblem = null;
        } catch (IllegalArgumentException e) {
            encoder = null;
            settingsProblem = "bad settings: " + e.getMessage();
        }
    }

    private static double finite(double value) {
        return Double.isNaN(value) || Double.isInfinite(value) ? 0.0 : value;
    }

    private static double clamp(double value, double limit) {
        if (Double.isNaN(value)) return 0.0;
        return Math.max(-limit, Math.min(limit, value));
    }
}
