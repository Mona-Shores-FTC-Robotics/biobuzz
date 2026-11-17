package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PwmControl;

import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.hardware.DeviceNames;
import org.firstinspires.ftc.teamcode.util.Throttle;

import java.util.Locale;

/**
 * The turret: a goBILDA 25-3 Speed servo in continuous mode ({@link DeviceNames#TURRET}) turning
 * it, and a REV Through Bore Encoder on its drive gear, read as a relative quadrature count
 * through the encoder socket of {@link DeviceNames#TURRET_ENCODER_HOST}. The turret has no stops
 * and no cables: it turns for ever either way, an angle is a direction, and it always goes the
 * shortest way round. A continuous servo has no feedback of its own, so the loop is closed here,
 * on the encoder ({@link TurretServoControl}).
 *
 * <h2>What the OpMode asks for</h2>
 *
 * <ul>
 *   <li>{@link #zeroHere()}: the turret is physically at home (its mark lined up, pointing along
 *       the robot's nose). The count is relative, so the turret sits on its mark before PLAY, and
 *       nothing here can tell if it did not: {@link #zeroed()} only says whether a zero was taken.</li>
 *   <li>{@link #aim(double, double)}: hold a chassis-frame angle, degrees counter-clockwise from
 *       the nose, that is moving at the given rate. The rate is how the turret cancels the robot's
 *       turning: a target fixed on the field moves across the chassis at minus the robot's turn
 *       rate, and the servo is told that speed before an error builds. The aim math is
 *       {@link TurretAim}; which target is the OpMode's.</li>
 *   <li>{@link #home()}: hold 0. {@link #idle()}: pulse off, limp, hold nothing.</li>
 * </ul>
 *
 * <p>{@link #update()} reads the encoder once and, from PLAY, writes the servo one power. Nothing
 * blocks. While {@link #lockedForward} it holds 0 and nothing else.
 *
 * <h2>Hardware it shares</h2>
 *
 * <p>The encoder host is another mechanism's motor. This class reads its position and velocity and
 * <b>never</b> sets its power, mode or direction, and never resets its count; it undoes the sign
 * the host's own direction puts on the count ({@link #hostSign}). Zero is a software offset, so
 * the intake (today's host) is untouched.
 *
 * <p>The values below are both robots'. One that a robot measures differently is a line in
 * {@link org.firstinspires.ftc.teamcode.RobotDifferences}, not a second field here.
 */
@Configurable
public class TurretSubsystem implements Subsystem {

    // ------------------------------------------------------------ the encoder

    /**
     * Hub counts per turn of the turret's encoder: 8192, a REV Through Bore Encoder (17 Nov 2026).
     * A Studica Cypher Max counts 2048. Wrong, every angle is off by the ratio of the two: on
     * {@code Bench: Turret} one turret turn by hand must read 360 (20245, 17 Nov 2026: 359.9 and
     * 358.3).
     */
    public static double countsPerEncoderTurn = TurretEncoder.REV_THROUGH_BORE_COUNTS;

    /**
     * Encoder turns per turret turn: the encoder rides the goBILDA turret's 64T drive gear
     * against its 176T ring, 2.75. One turret turn by hand read 2.749 and 2.737 encoder turns on
     * {@code Bench: Turret} (20245, 17 Nov 2026).
     */
    public static double turnsPerTurretTurn = 2.75;

    /** +1 if the count rises as the turret turns counter-clockwise seen from above. -1 on 20245: it falls (robot, 24 Oct 2026; again with the REV Through Bore, 17 Nov 2026). */
    public static int direction = -1;

    // ------------------------------------------------------------ the servo

    /**
     * +1 if positive servo power turns the turret counter-clockwise. +1 on 20245 with the 25-3
     * Speed (Bench: Turret, 17 Nov 2026; its earlier servo was -1). Wrong, the turret drives away
     * from its target and the hold is dropped ({@link TurretDriveWatch}).
     */
    public static int servoDirection = 1;

    /** The power at which the servo stands still, [-0.2, 0.2]; see {@code Bench: Turret}. 0 on 20245: it stands still (17 Nov 2026). */
    public static double servoStopPower = 0.0;

    /**
     * Turret degrees per second at full power, which scales the feedforward. 180 on 20245, both
     * ways (Bench: Turret, 17 Nov 2026): the steady rate at power 1, under a peak reading of 194.
     * The data sheet's no-load speed through 2.75:1 is 196.
     */
    public static double topSpeedDps = 180.0;

    /**
     * The least power that turns the turret steadily. 0.09 on 20245 (Bench: Turret, 17 Nov 2026):
     * at 0.06 it buzzes without moving, and from 0.065 to 0.08 it moves in spurts, 10 to 20 deg/s
     * at 0.08.
     */
    public static double minPower = 0.09;
    public static double maxPower = 1.0;

    // ------------------------------------------------------------ the loop
    // kP, kD and the tolerance are 20245's, tuned in Basic Drive with the turret holding straight
    // ahead (17 Nov 2026): pushed off by hand it comes back cleanly; a hard shove zigzags briefly.
    // Bench: Turret liked 0.035 and 0.006, and those rocked back and forth in Basic Drive, whose
    // loop is slower: tune the loop in the OpMode that runs it. The floor could not be lowered to
    // slow the approach, since under 0.09 the turret sticks and buzzes. Aiming with the robot
    // turning, any damping made the turret twitch, and with none it swung past after a big move:
    // so the law damps only onto a still target, which has not yet run on a robot.

    /** Power per degree of error. */
    public static double kP = 0.02;
    /**
     * Damping: power per deg/s of the turret's rate, taken off the correction while the target
     * stands still (a hold, or an aim with the robot not turning). Not applied while the target
     * moves.
     */
    public static double kD = 0.003;
    /** The share of the target's rate fed forward to the servo: 1 cancels all of the robot's turning, 0 none. */
    public static double feedforward = 1.0;
    /** A target rate slower than this is treated as still, deg/s: the gyro's noise is not fed to the servo. */
    public static double targetStillDps = 5.0;
    /**
     * Inside this many degrees of the target, {@link #onTarget()}, and no correction is made. 2:
     * the slowest the servo turns the turret is 10 to 20 deg/s (at the floor), so a narrower band
     * is crossed before the stop takes effect and the turret hunts.
     */
    public static double holdToleranceDeg = 2.0;

    /** Driven one way and turning the other for this long, the hold is dropped, ms ({@link TurretDriveWatch}). */
    public static double watchWrongWayMs = 400.0;
    /** Driven hard and not turning for this long, the hold is dropped, ms. */
    public static double watchStallMs = 700.0;

    /**
     * Where the turret's axis sits from the robot's tracked point, inches, robot frame (+forward,
     * +left). <b>From the CAD, not yet taped:</b> 84.5 mm behind the midpoint of the four wheels
     * (Onshape, 24 Oct 2026; 3.31 in again on 16 Nov) and 2.68 in to the right of it. The sideways
     * number was 1.87 in (47.6 mm, against three wheel axles, 24 Oct) until 16 Nov 2026, when the
     * mentor measured the turret bearing 4.94 in from the right edge of the 15.236 in chassis
     * (7.62 - 4.94 = 2.68), which is what the model built from the CAD has for the bearing's axis
     * ({@code cad/advantagescope}, 2.67). Assumes the Pinpoint tracks that midpoint. Tape it on a
     * robot ({@code doc/robot-todo.md}), and re-measure if the ring moves in the CAD.
     */
    public static double turretForwardIn = -3.33;
    public static double turretLeftIn = -2.68;

    /**
     * The turret is held straight ahead and never aimed: {@link #angleDegrees()} is 0 and every
     * request is answered by holding 0, from PLAY, on the encoder. The mentor's call on 1 Nov 2026:
     * both robots play locked. Set false on a robot whose 25-3 Speed servo is fitted and whose
     * signs, stop power, floor and top speed are checked on {@code Bench: Turret}
     * ({@code doc/robot-todo.md}).
     */
    public static boolean lockedForward = true;

    // ------------------------------------------------------------ state

    private enum Mode { IDLE, HOLD }

    /** Where the zero came from, for the Robot page and the log. */
    public enum ZeroSource {
        NONE("not zeroed yet"),
        HAND("zeroed by hand"),
        PLAY("zeroed at PLAY, assumed at home"),
        AUTO("zero carried from the Auto");

        public final String text;

        ZeroSource(String text) {
            this.text = text;
        }
    }

    private final DcMotorEx encoderHost;
    private final CRServo servo;
    private final PwmControl pwm;   // null if this servo cannot switch its pulse off
    private final TurretServoControl.Gains gains = new TurretServoControl.Gains(
            kP, kD, minPower, maxPower, topSpeedDps, feedforward, holdToleranceDeg, targetStillDps);
    private final TurretDriveWatch watch = new TurretDriveWatch();

    private TurretEncoder encoder;
    private double encoderTurnsSetting = Double.NaN;
    private double encoderCountsSetting = Double.NaN;
    private int directionSetting;
    private String settingsProblem;

    private Mode mode = Mode.IDLE;
    private boolean started;
    private double targetDeg;
    private double targetRateDps;

    private long counts;
    private long zeroCounts;
    private boolean zeroed;
    private ZeroSource zeroSource = ZeroSource.NONE;
    private double angleDeg = Double.NaN;
    private double rateDps = Double.NaN;
    private double errorDeg = Double.NaN;
    /** The power asked of the servo this loop, counter-clockwise positive; NaN while limp. */
    private double power = Double.NaN;
    /**
     * What the servo was last told, to the PWM's own step; NaN before the first and while limp. An
     * unchanged write waits for {@link #resend}, which keeps the repeat that recovers a servo a hub
     * has reset.
     */
    private double servoOut = Double.NaN;
    private boolean limp = true;
    private final Throttle resend = new Throttle(250, 0);
    /** Why the hold was dropped, or null. */
    private String faultNote;
    /** The encoder's own angle and rate, whether or not the turret is locked; NaN without one. */
    private double encoderAngleDeg = Double.NaN;
    private double encoderRateDps = Double.NaN;

    /** A power this big is "driven" for the wrong-way watch, and this big "driven hard" for the stall watch. */
    private static final double WATCH_DRIVE_POWER = 0.15;
    private static final double WATCH_HARD_POWER = 0.4;
    /** The encoder turning at least this fast is "turning", deg/s. */
    private static final double WATCH_MOVING_DPS = 10.0;

    /** Looks both devices up once. A missing one fails init with the SDK's own message. */
    public TurretSubsystem(HardwareMap hardwareMap) {
        encoderHost = hardwareMap.get(DcMotorEx.class, DeviceNames.TURRET_ENCODER_HOST);
        servo = hardwareMap.get(CRServo.class, DeviceNames.TURRET);
        pwm = servo instanceof PwmControl ? (PwmControl) servo : null;
    }

    // ------------------------------------------------------------ requests

    /** The turret is at home right now: angles are measured from here on. A person's request, and it wins. */
    public void zeroHere() {
        zeroCounts = counts;
        zeroed = true;
        zeroSource = ZeroSource.HAND;
    }

    /** At PLAY: the turret is assumed at home, so zero here unless a person or the handoff already did. */
    public void zeroAtPlay() {
        if (zeroed) {
            return;
        }
        zeroCounts = counts;
        zeroed = true;
        zeroSource = ZeroSource.PLAY;
    }

    /**
     * Take the zero an Autonomous left behind. The count is the hub's and was never reset, so the
     * same offset still gives the same angle, wherever the Auto turned the turret to.
     */
    public void restoreZero(long autoZeroCounts) {
        zeroCounts = autoZeroCounts;
        zeroed = true;
        zeroSource = ZeroSource.AUTO;
    }

    /** The offset that makes an angle: the encoder count at home. For the handoff to carry. */
    public long zeroCounts() {
        return zeroCounts;
    }

    /**
     * The drive team discarded the handoff ("start fresh"): drop a zero carried from the last
     * OpMode, so the turret zeroes at PLAY as on any fresh start. A zero set by hand stays.
     */
    public void forgetCarriedZero() {
        if (zeroSource == ZeroSource.AUTO) {
            zeroed = false;
            zeroSource = ZeroSource.NONE;
        }
    }

    /** Where the current zero came from. */
    public ZeroSource zeroSource() {
        return zeroSource;
    }

    /**
     * Hold {@code deg} from home, counter-clockwise positive, chassis frame: a direction, reached
     * the shortest way round. {@code rateDps} is how fast that angle is itself moving, degrees per
     * second counter-clockwise (for a target fixed on the field, minus the robot's turn rate); 0
     * or NaN for a target that stands still on the chassis. A request after a dropped hold tries
     * again.
     */
    public void aim(double deg, double rateDps) {
        if (Double.isNaN(deg)) {
            return;
        }
        if (mode != Mode.HOLD) {
            faultNote = null;
            watch.reset();
        }
        targetDeg = deg;
        targetRateDps = Double.isNaN(rateDps) ? 0.0 : rateDps;
        mode = Mode.HOLD;
    }

    /** Hold {@code deg} from home, a target that stands still on the chassis. */
    public void aim(double deg) {
        aim(deg, 0.0);
    }

    /** Hold straight ahead. */
    public void home() {
        aim(0.0, 0.0);
    }

    /**
     * Pulse off: the servo goes limp and the turret stays wherever it is pushed. While
     * {@link #lockedForward} the turret holds 0 all the same.
     */
    public void idle() {
        mode = Mode.IDLE;
    }

    // ------------------------------------------------------------ reads

    /**
     * Degrees from home, counter-clockwise positive, not wrapped: it grows past 180 as the turret
     * winds round, so wrap a difference from it ({@link TurretEncoder#wrap180}). NaN until the
     * settings are valid.
     */
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

    /** Target minus angle the shortest way round, (-180, 180], or NaN when not holding. */
    public double errorDegrees() {
        return errorDeg;
    }

    /** Holding, and within the tolerance of the target. */
    public boolean onTarget() {
        return mode == Mode.HOLD && !Double.isNaN(errorDeg) && Math.abs(errorDeg) <= gains.toleranceDeg;
    }

    /** Whether a zero has been taken (by hand, at PLAY or from the Auto) since this OpMode started. */
    public boolean zeroed() {
        return zeroed;
    }

    /** Whether the turret is holding an angle. */
    public boolean holding() {
        return mode == Mode.HOLD;
    }

    // ------------------------------------------------------------ lifecycle

    /**
     * Idle, angle relative to wherever it sits until a zero is taken. The servo is not written
     * until PLAY: unwritten it is limp, since nothing may move or be held by a servo before PLAY
     * (G304.H, G403; see {@link Subsystem}). So the drive team sets the turret at home by its mark
     * in INIT, it is zeroed there at PLAY, and from then the loop holds it.
     */
    @Override
    public void initialize() {
        mode = Mode.IDLE;
        started = false;
        refreshSettings();
        counts = hostSign(encoderHost.getDirection()) * (long) encoderHost.getCurrentPosition();
        zeroCounts = counts;
        zeroed = false;
        zeroSource = ZeroSource.NONE;
        faultNote = null;
        watch.reset();
    }

    /** PLAY: the servo may be written from now on. */
    @Override
    public void start() {
        started = true;
    }

    /** One encoder read, and from PLAY one servo write. */
    @Override
    public void update() {
        refreshSettings();
        // The host motor's own code may reverse it, and the SDK then flips the count with the power.
        int hostSign = hostSign(encoderHost.getDirection());
        counts = hostSign * (long) encoderHost.getCurrentPosition();
        double countsPerSecond = hostSign * encoderHost.getVelocity();
        if (encoder != null) {
            encoderAngleDeg = encoder.degrees(counts, zeroCounts);
            encoderRateDps = encoder.degreesPerSecond(countsPerSecond);
        } else {
            encoderAngleDeg = Double.NaN;
            encoderRateDps = Double.NaN;
        }
        if (lockedForward) {
            angleDeg = 0.0;
            rateDps = 0.0;
        } else {
            angleDeg = encoderAngleDeg;
            rateDps = encoderRateDps;
        }
        errorDeg = !lockedForward && mode == Mode.HOLD
                ? TurretServoControl.shortestErrorDeg(targetDeg, angleDeg) : Double.NaN;

        // Locked, the turret holds 0 whatever was asked; the loop is the same one.
        boolean holding = (lockedForward || mode == Mode.HOLD) && faultNote == null && zeroed && encoder != null;
        power = Double.NaN;   // NaN: limp
        if (holding) {
            double want = lockedForward ? 0.0 : targetDeg;
            double wantRate = lockedForward ? 0.0 : targetRateDps;
            power = TurretServoControl.power(TurretServoControl.shortestErrorDeg(want, encoderAngleDeg),
                    encoderRateDps, wantRate, gains);
        }
        if (!started) {
            return;
        }
        if (Double.isNaN(power)) {
            goLimp();
            return;
        }
        long now = System.nanoTime();
        TurretDriveWatch.Fault fault = watch.step(power, encoderRateDps, WATCH_DRIVE_POWER, WATCH_HARD_POWER,
                WATCH_MOVING_DPS, watchWrongWayMs, watchStallMs, now);
        if (fault != TurretDriveWatch.Fault.NONE) {
            faultNote = fault == TurretDriveWatch.Fault.WRONG_WAY
                    ? "DROPPED: the turret turned against its servo. Flip servoDirection (it ran from the"
                            + " target) or direction (the angle read the other way) on Bench: Turret."
                    : "DROPPED: driven and the encoder did not move. Encoder cable in the intake's socket?"
                            + " Servo turning? Turret jammed?";
            mode = Mode.IDLE;
            power = Double.NaN;
            goLimp();
            return;
        }
        double out = pwmStep(clamp(power * (servoDirection == -1 ? -1 : 1) + clamp(servoStopPower, 0.2), 1.0));
        if (limp || out != servoOut || resend.due(now)) {
            servo.setPower(out);
            servoOut = out;
            if (limp && pwm != null) {
                pwm.setPwmEnable();
            }
            limp = false;
        }
    }

    /**
     * +1 or -1 to turn the host motor's reading back into the hub's own count. The encoder is read
     * through another mechanism's motor ({@link DeviceNames#TURRET_ENCODER_HOST}), and that
     * mechanism may set the motor's direction: the SDK then flips the position and the velocity it
     * reports along with the power. The turret's count must not change sign because the intake was
     * reversed, so the flip is undone here (20245, 17 Nov 2026: the hold worked on the bench, which
     * leaves the motor forward, and ran away in Basic Drive, where the intake reverses it).
     */
    public static int hostSign(DcMotorSimple.Direction hostDirection) {
        return hostDirection == DcMotorSimple.Direction.REVERSE ? -1 : 1;
    }

    private void goLimp() {
        if (!limp) {
            if (pwm != null) {
                pwm.setPwmDisable();
            } else {
                servo.setPower(clamp(servoStopPower, 0.2));
            }
        }
        limp = true;
        servoOut = Double.NaN;
        watch.reset();
    }

    /**
     * A servo power to the PWM's own step: power ±1 spans about 1000 µs of pulse either side of
     * the centre, so 0.001 is about one microsecond. Finer changes only cost writes.
     */
    static double pwmStep(double power) {
        return Math.round(power * 1000.0) / 1000.0;
    }

    /** Final teardown: pulse off, limp. */
    @Override
    public void stop() {
        if (pwm != null) {
            pwm.setPwmDisable();
        } else {
            servo.setPower(clamp(servoStopPower, 0.2));
        }
        limp = true;
    }

    @Override
    public String health() {
        return (lockedForward ? "locked straight ahead (lockedForward): from PLAY holds 0° on the encoder; " : "")
                + "continuous servo, presence unknown (a servo reports nothing back); encoder read through "
                + DeviceNames.TURRET_ENCODER_HOST + "; zero: " + zeroSource.text
                + (settingsProblem != null ? "; settings: " + settingsProblem : "");
    }

    @Override
    public void describe(Display display) {
        if (settingsProblem != null) {
            display.status("Turret", Display.Level.FAULT, settingsProblem + ": limp");
            return;
        }
        if (faultNote != null) {
            display.status("Turret", Display.Level.FAULT, faultNote);
            return;
        }
        String zero = zeroed ? zeroSource.text : "not zeroed yet: it zeroes where it stands at PLAY, or zero it now";
        if (lockedForward) {
            display.status("Turret", Display.Level.OK, String.format(Locale.US,
                    "locked straight ahead: %s 0° (encoder %.1f°) · %s",
                    started ? "holding" : "holds from PLAY at", TurretEncoder.wrap180(encoderAngleDeg), zero));
            return;
        }
        String what;
        Display.Level level;
        switch (mode) {
            case HOLD:
                what = String.format(Locale.US, "%.1f° → %.1f° (%+.1f)", TurretEncoder.wrap180(angleDeg),
                        TurretEncoder.wrap180(targetDeg), errorDeg);
                level = onTarget() ? Display.Level.OK : Display.Level.WARN;
                break;
            default:
                what = String.format(Locale.US, "%.1f° · idle", TurretEncoder.wrap180(angleDeg));
                level = Display.Level.OK;
                break;
        }
        display.status("Turret", level, what + " · " + zero);
    }

    /** Why the last hold was dropped, or null. Shown by the OpMode too. */
    public String faultNote() {
        return faultNote;
    }

    /** The power asked of the servo this loop, counter-clockwise positive; NaN while limp. For the log. */
    public double power() {
        return power;
    }

    // ------------------------------------------------------------ helpers

    /** Pulls the Panels values in; rebuilds the encoder's converter only when its two inputs change. */
    private void refreshSettings() {
        gains.set(kP, kD, minPower, maxPower, topSpeedDps, feedforward, holdToleranceDeg, targetStillDps);
        if (encoderTurnsSetting == turnsPerTurretTurn && directionSetting == direction
                && encoderCountsSetting == countsPerEncoderTurn && encoder != null) {
            return;
        }
        encoderTurnsSetting = turnsPerTurretTurn;
        encoderCountsSetting = countsPerEncoderTurn;
        directionSetting = direction;
        try {
            encoder = new TurretEncoder(countsPerEncoderTurn, turnsPerTurretTurn, direction);
            settingsProblem = null;
        } catch (IllegalArgumentException e) {
            encoder = null;
            settingsProblem = "bad settings: " + e.getMessage();
        }
    }

    private static double clamp(double value, double limit) {
        if (Double.isNaN(value)) {
            return 0.0;
        }
        return Math.max(-limit, Math.min(limit, value));
    }
}
