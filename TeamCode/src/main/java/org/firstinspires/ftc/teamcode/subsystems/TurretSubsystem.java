package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PwmControl;

import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.hardware.DeviceNames;
import org.firstinspires.ftc.teamcode.util.Throttle;

import java.util.Locale;
import java.util.function.LongSupplier;

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
 * <h2>The home switch</h2>
 *
 * <p>A robot with {@link #homeSwitch} (19429) has a REV Magnetic Limit Switch
 * ({@link DeviceNames#TURRET_HOME}) under a magnet on the back of the turret, over it when the
 * turret is on its mark. Each pass of the turret across it puts the zero on the mark
 * ({@link TurretHomeSwitch}, {@link TurretZeroPolicy#atSwitchPass}), in INIT too, so a turret turned
 * by hand across its mark is zeroed, and a zero that drifted is put right the next time the turret
 * swings through straight ahead. Reading it moves nothing. A zero set by hand wins. An
 * Autonomous's INIT, and only that, also turns the turret up to a full turn to find the switch, then
 * past it and back across it slowly both ways, and zeroes on the average of the two slow passes
 * ({@link #homeSweep}, {@link TurretHomeSweep}); then it holds its home angle.
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
    // kP, kD and the tolerance are 20245's, tuned in Basic Drive (17 Nov 2026), holding straight
    // ahead and then aiming: kP 0.02 with no damping. Bench: Turret liked 0.035 and 0.006, and
    // those rocked back and forth in Basic Drive, whose loop is slower: tune the loop in the OpMode
    // that runs it. Aiming, any damping made the turret twitch through a turn; with none it swings
    // a little past after a big move and comes back, which the team accepted. The law has since
    // been changed to damp only onto a still target, so a kD may now help a big move without the
    // twitch: not yet tried on a robot. The floor could not be lowered to slow the approach, since
    // under 0.09 the turret sticks and buzzes.

    /** Power per degree of error. */
    public static double kP = 0.02;
    /**
     * Damping: power per deg/s of the turret's rate, taken off the correction while the target
     * stands still (a hold, or an aim with the robot not turning). Not applied while the target
     * moves. 0 as run on 20245; 0.003 held well on a still target before the law changed.
     */
    public static double kD = 0.0;
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
     * Where the shots go straight ahead, measured from the home mark, degrees counter-clockwise
     * seen from above. The drive team still sets the turret on its mark before PLAY, and that is
     * still the zero; every angle the turret holds or reports is then counted from this many
     * degrees round from the mark. So at PLAY it turns here and holds, and an aim lands the shots
     * on the target, with the operator's AIM TRIM at 0. 0 for a turret whose shots go where its
     * mark points; a robot whose shots sit to one side gets its line in
     * {@link org.firstinspires.ftc.teamcode.RobotDifferences}. Read every loop: a change in Panels
     * moves the turret at once. NaN is 0; kept inside ±30. 20245 ran with 4.5 on 17 Nov 2026 and
     * the operator then trimmed 4.5 back to shoot straight, so it is 0 there too: the first
     * reading was not a fixed offset.
     */
    public static double aimOffsetDeg = 0.0;

    /**
     * The turret is held straight ahead and never aimed: {@link #angleDegrees()} is 0 and every
     * request is answered by holding 0, from PLAY, on the encoder. The mentor's call on 1 Nov 2026:
     * both robots play locked. False for a robot whose 25-3 Speed servo is fitted and whose signs,
     * stop power, floor and top speed are checked on {@code Bench: Turret}: its line in
     * {@link org.firstinspires.ftc.teamcode.RobotDifferences} (20245 since 17 Nov 2026, 19429 since
     * 18 Nov 2026).
     */
    public static boolean lockedForward = true;

    /**
     * The firing arc ({@link TurretArc}): how far left of the arc's centre (0, or in the TeleOp the aim trim) the turret may point and still fire
     * clear of the robot, degrees. Behind the arc it holds at the edge and a fire button does not
     * feed. 90 on both robots is the mentor's judgement (18 Nov 2026: the turret is good across
     * ±90°), not a hand measurement; measuring it is optional (doc/robot-todo.md, the turret arc),
     * and a robot whose arc is seen to differ gets its line in
     * {@link org.firstinspires.ftc.teamcode.RobotDifferences}. NaN: no arc, the turret goes all
     * the way round. Read every loop.
     */
    public static double arcLeftDeg = 90.0;

    /** The same to the right of 0, degrees, a positive number. */
    public static double arcRightDeg = 90.0;

    // ------------------------------------------------------------ the home switch

    /**
     * This turret has a home switch, {@link DeviceNames#TURRET_HOME}: a REV Magnetic Limit Switch on
     * a digital port, and a magnet on the back of the turret over it on the mark. False shared; 19429
     * has one (its line in {@link org.firstinspires.ftc.teamcode.RobotDifferences}).
     */
    public static boolean homeSwitch = false;

    /**
     * Where the home mark is from the middle of the switch's band, degrees counter-clockwise: the
     * band's middle as {@code Bench: Turret} reads it with the encoder zeroed on the mark, negated.
     * <b>0 until measured</b> (doc/robot-todo.md): the mark is then the band's middle.
     */
    public static double homeSwitchOffsetDeg = 0.0;

    /**
     * A pass whose edge is crossed faster than this is not taken, deg/s: an edge is placed to half a
     * loop's travel, about 1° at 90°/s and a 20 ms loop.
     */
    public static double homeSwitchMaxPassDps = 90.0;

    /** A band narrower than this is a flicker, not the magnet, degrees. */
    public static double homeSwitchMinBandDeg = 0.5;

    /** A band wider than this is the switch stuck on, degrees. To be narrowed once Bench: Turret has measured the band. */
    public static double homeSwitchMaxBandDeg = 30.0;

    /**
     * A pass that puts the mark further than this from the zero is logged and not taken, degrees:
     * a turret that far out was not on its mark, or the pass was not the magnet. NaN takes any.
     */
    public static double homeSwitchMaxCorrectionDeg = 30.0;

    /**
     * An Autonomous's INIT sweeps the turret to find its mark ({@link TurretHomeSweep}), on a turret
     * with a {@link #homeSwitch}: R103.B lets an Auto's INIT move it (mentor, 21 Nov 2026). Never a
     * TeleOp's INIT. Off, an Auto's INIT zeroes where the turret stands, as without a switch.
     */
    public static boolean homeSweep = true;

    /** How far the sweep's search turns the turret, counter-clockwise, degrees: a full turn and a band. */
    public static double homeSweepRangeDeg = 370.0;

    /** The search's speed, deg/s: under {@link #homeSwitchMaxPassDps}. A full turn is about 6 s. */
    public static double homeSweepSearchDps = 60.0;

    /** The slow passes' speed, deg/s: over the servo's floor (19429 turns steadily from 10 to 13). */
    public static double homeSweepFineDps = 15.0;

    /** How far past the switch's band the sweep goes before it turns back, degrees. */
    public static double homeSweepOvershootDeg = 10.0;

    /** The sweep gives up after this long, seconds; the turret then holds where it stood. */
    public static double homeSweepTimeoutS = 20.0;

    // ------------------------------------------------------------ state

    private enum Mode { IDLE, HOLD }

    /** Where the zero came from, for the Robot page and the log. */
    public enum ZeroSource {
        NONE("not zeroed yet"),
        HAND("zeroed by hand"),
        PLAY("zeroed at PLAY, assumed at home"),
        INIT("zeroed at the Auto's INIT, assumed at home"),
        AUTO("zero carried from the Auto"),
        SWITCH("zeroed by the home switch");

        public final String text;

        ZeroSource(String text) {
            this.text = text;
        }
    }

    private final DcMotorEx encoderHost;
    private final CRServo servo;
    private final PwmControl pwm;   // null if this servo cannot switch its pulse off
    /** The home switch, or null on a robot without one ({@link #homeSwitch}). */
    private final DigitalChannel homeInput;
    private final TurretHomeSwitch home = new TurretHomeSwitch();
    private final TurretHomeSweep sweep = new TurretHomeSweep();
    private final TurretHomeSweep.Limits sweepLimits = new TurretHomeSweep.Limits();
    /** The clock, nanoseconds; a test's own. */
    private LongSupplier clock = System::nanoTime;
    /** The switch sees the magnet, this loop. */
    private boolean magnet;
    /** The switch saw the magnet when an Autonomous's INIT zeroed the turret where it stood. */
    private boolean magnetAtInitZero;
    /** How far the last pass moved the zero, degrees; NaN before one, or when it was the first zero. */
    private double lastSwitchCorrectionDeg = Double.NaN;
    /** A line for the match log about the last pass, until {@link #takeSwitchEvent()} takes it. */
    private String switchEvent;
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
    /** An Autonomous's INIT: zeroed there and held at home before PLAY ({@link #holdAtInit()}). */
    private boolean initHold;
    private double targetDeg;
    private double targetRateDps;
    /** The aim asked for before the arc, and the arc edge held instead of it (NaN: inside the arc). */
    private double askedDeg = Double.NaN;
    private double arcEdgeDeg = Double.NaN;
    /** The firing arc's centre, degrees in this turret's frame: the operator's aim trim ({@link TurretArc}). */
    private double arcCentreDeg = 0.0;

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

    /**
     * Looks the devices up once: the encoder host, the servo, and on a robot with {@link #homeSwitch}
     * the switch. A missing one fails init with the SDK's own message.
     */
    public TurretSubsystem(HardwareMap hardwareMap) {
        encoderHost = hardwareMap.get(DcMotorEx.class, DeviceNames.TURRET_ENCODER_HOST);
        servo = hardwareMap.get(CRServo.class, DeviceNames.TURRET);
        pwm = servo instanceof PwmControl ? (PwmControl) servo : null;
        homeInput = homeSwitch ? hardwareMap.get(DigitalChannel.class, DeviceNames.TURRET_HOME) : null;
        if (homeInput != null) {
            homeInput.setMode(DigitalChannel.Mode.INPUT);
        }
    }

    // ------------------------------------------------------------ requests

    /** The turret is at home right now: angles are measured from here on. A person's request, and it wins. */
    public void zeroHere() {
        zeroCounts = counts;
        zeroed = true;
        zeroSource = ZeroSource.HAND;
    }

    /**
     * An Autonomous's INIT (R103.B, through {@code Robot.holdForAutoInit()} only): the turret is
     * assumed on its home mark, so it zeroes here ({@link TurretZeroPolicy#atInit}) and holds 0,
     * {@link #aimOffsetDeg} round from the mark, from now until PLAY and on. The drive team then sees
     * at INIT that it sits right; a wrong angle means it was not on its mark: re-seat it and INIT
     * again, which zeroes again. A turret {@link #lockedForward} holds 0 the same way. A person's
     * zero button still wins, and PLAY does not zero on top of this one.
     */
    public void holdAtInit() {
        magnetAtInitZero = magnet;
        if (TurretZeroPolicy.atInit(true, false) == TurretZeroPolicy.Action.ZERO_HERE) {
            zeroCounts = counts;
            zeroed = true;
            zeroSource = ZeroSource.INIT;
        }
        home();
        initHold = true;
        if (homeInput != null && homeSweep) {
            // From the mark it was assumed on, in the hold's frame (counted from the aim offset).
            sweep.start(clock.getAsLong(), -aimOffset(), sweepLimits());
            note("turret: sweeping for the home switch");
        }
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
        // Behind the firing arc the turret holds at the edge, still, and does not chase round the back.
        askedDeg = deg;
        double hold = TurretArc.hold(deg, arcEdgeDeg, arcCentreDeg);
        arcEdgeDeg = hold == deg ? Double.NaN : hold;
        targetDeg = hold;
        targetRateDps = hold != deg || Double.isNaN(rateDps) ? 0.0 : rateDps;
        mode = Mode.HOLD;
    }

    /** Hold {@code deg} from home, a target that stands still on the chassis. */
    public void aim(double deg) {
        aim(deg, 0.0);
    }

    /**
     * Centre the firing arc on {@code deg}, this turret's frame: the TeleOp's aim trim, which is where
     * the robot's real forward reads when the zero is off ({@link TurretArc}). NaN is 0. It applies
     * from the next {@link #aim}; an Auto never sets it.
     */
    public void arcCentre(double deg) {
        arcCentreDeg = Double.isNaN(deg) ? 0.0 : deg;
    }

    /** The firing arc's centre, degrees in this turret's frame. */
    public double arcCentreDegrees() {
        return arcCentreDeg;
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

    /** Target minus angle the way the turret turns (inside the firing arc, else the shortest), or NaN when not holding. */
    public double errorDegrees() {
        return errorDeg;
    }

    /** Holding, and within the tolerance of the target. */
    public boolean onTarget() {
        return mode == Mode.HOLD && !Double.isNaN(errorDeg) && Math.abs(errorDeg) <= gains.toleranceDeg;
    }

    /** Holding at an edge of the firing arc, because the aim asked for is behind it. */
    public boolean atArcEdge() {
        return mode == Mode.HOLD && !Double.isNaN(arcEdgeDeg);
    }

    /** The aim last asked of {@link #aim}, before the firing arc; NaN before the first. */
    public double askedDegrees() {
        return askedDeg;
    }

    /** Whether a zero has been taken (by hand, at an Auto's INIT, at PLAY or from the Auto) since this OpMode started. */
    public boolean zeroed() {
        return zeroed;
    }

    /** Whether the turret is holding an angle. */
    public boolean holding() {
        return mode == Mode.HOLD;
    }

    /** Whether this robot has a home switch ({@link #homeSwitch}). */
    public boolean homeSwitchFitted() {
        return homeInput != null;
    }

    /** The home switch sees the magnet now: the turret is within the switch's band of its mark. False without one. */
    public boolean onMark() {
        return magnet;
    }

    /** Whether the switch saw the magnet when an Autonomous's INIT zeroed the turret where it stood. */
    public boolean magnetAtInitZero() {
        return magnetAtInitZero;
    }

    /** Where an Autonomous's INIT sweep for the mark stands ({@link TurretHomeSweep}). */
    public TurretHomeSweep.State sweepState() {
        return sweep.state();
    }

    /**
     * What is wrong with the mark an Autonomous's INIT zeroed the turret on, for the INIT warning, or
     * null: the sweep did not find the magnet, or with no sweep the switch saw none at the zero.
     */
    public String autoInitMarkProblem() {
        if (homeInput == null || zeroSource != ZeroSource.INIT) return null;
        if (sweep.state() == TurretHomeSweep.State.NOT_FOUND) return sweep.state().text;
        if (sweep.sweeping()) return null;
        if (sweep.state() == TurretHomeSweep.State.IDLE && !magnetAtInitZero) return "zeroed with no magnet over the switch";
        return null;
    }

    /** A test's clock, nanoseconds. */
    void useClock(LongSupplier clock) {
        this.clock = clock;
    }

    /** How far the home switch's last pass moved the zero, degrees; NaN before one. */
    public double lastSwitchCorrectionDeg() {
        return lastSwitchCorrectionDeg;
    }

    /** A line for the match log about the switch's last pass, once; null when there is nothing new. */
    public String takeSwitchEvent() {
        String event = switchEvent;
        switchEvent = null;
        return event;
    }

    // ------------------------------------------------------------ lifecycle

    /**
     * Idle, angle relative to wherever it sits until a zero is taken. The servo is not written
     * until PLAY: unwritten it is limp, since nothing may move or be held by a servo before PLAY
     * (G304.H, G403; see {@link Subsystem}). So the drive team sets the turret at home by its mark
     * in INIT, it is zeroed there at PLAY, and from then the loop holds it. An Autonomous's INIT is
     * the exception: {@link #holdAtInit()} zeroes it and holds it from INIT.
     */
    @Override
    public void initialize() {
        mode = Mode.IDLE;
        started = false;
        initHold = false;
        refreshSettings();
        counts = hostSign(encoderHost.getDirection()) * (long) encoderHost.getCurrentPosition();
        zeroCounts = counts;
        zeroed = false;
        zeroSource = ZeroSource.NONE;
        faultNote = null;
        watch.reset();
        home.reset();
        sweep.reset();
        magnet = readMagnet();
        magnetAtInitZero = false;
        lastSwitchCorrectionDeg = Double.NaN;
        switchEvent = null;
    }

    /** PLAY: the servo may be written from now on. A sweep still looking stops, the zero where the turret stood. */
    @Override
    public void start() {
        started = true;
        if (sweep.stop()) {
            note("turret: PLAY before the sweep found the mark: zeroed where it stood");
        }
    }

    /** One encoder read, and from PLAY one servo write. */
    @Override
    public void update() {
        refreshSettings();
        // The host motor's own code may reverse it, and the SDK then flips the count with the power.
        int hostSign = hostSign(encoderHost.getDirection());
        counts = hostSign * (long) encoderHost.getCurrentPosition();
        double countsPerSecond = hostSign * encoderHost.getVelocity();
        if (homeInput != null) {
            // Before the angle: this loop's angle uses a zero this loop's pass put right.
            magnet = readMagnet();
            sweepStep(homeSwitchStep(countsPerSecond));
        }
        if (encoder != null) {
            // Less the aim offset: "0" is where the shots go straight ahead, not the home mark.
            encoderAngleDeg = encoder.degrees(counts, zeroCounts) - aimOffset();
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
                ? TurretArc.errorDeg(targetDeg, angleDeg, arcCentreDeg) : Double.NaN;

        // Locked, the turret holds 0 whatever was asked; the loop is the same one.
        boolean holding = (lockedForward || mode == Mode.HOLD) && faultNote == null && zeroed && encoder != null;
        power = Double.NaN;   // NaN: limp
        if (holding && sweep.sweeping()) {
            // The sweep's moving target, locked or not; it turns well inside the firing arc.
            power = TurretServoControl.power(TurretServoControl.shortestErrorDeg(sweep.targetDeg(), encoderAngleDeg),
                    encoderRateDps, sweep.rateDps(), gains);
        } else if (holding) {
            double want = lockedForward ? 0.0 : targetDeg;
            double wantRate = lockedForward ? 0.0 : targetRateDps;
            // Inside the firing arc the way there never crosses the back (TurretArc.errorDeg).
            power = TurretServoControl.power(lockedForward
                            ? TurretServoControl.shortestErrorDeg(want, encoderAngleDeg)
                            : TurretArc.errorDeg(want, encoderAngleDeg, arcCentreDeg),
                    encoderRateDps, wantRate, gains);
        }
        // Before PLAY the servo is not written, unless an Autonomous's INIT holds it (holdAtInit).
        if (!started && !initHold) {
            return;
        }
        if (Double.isNaN(power)) {
            goLimp();
            return;
        }
        long now = clock.getAsLong();
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
            if (sweep.stop()) note("turret: sweep stopped, the hold dropped");
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

    /** The switch reads low with the magnet over it (a REV Magnetic Limit Switch is active low). */
    private boolean readMagnet() {
        return homeInput != null && !homeInput.getState();
    }

    /** One step of the home switch: a pass puts the zero on the mark, as {@link TurretZeroPolicy#atSwitchPass} says. */
    private TurretHomeSwitch.Result homeSwitchStep(double countsPerSecond) {
        double rate = encoder == null ? Double.NaN : encoder.degreesPerSecond(countsPerSecond);
        TurretHomeSwitch.Result result = home.step(magnet, counts, encoder, rate, homeSwitchMaxPassDps,
                homeSwitchMinBandDeg, homeSwitchMaxBandDeg, homeSwitchOffsetDeg);
        if (result == TurretHomeSwitch.Result.REJECTED) {
            note("turret: home switch pass not taken: " + home.rejectReason());
            return result;
        }
        if (result != TurretHomeSwitch.Result.PASS) {
            return result;
        }
        long mark = Math.round(home.markCounts());
        // Wrapped: a turret wound a turn round is still on its mark at 360.
        double disagree = TurretEncoder.wrap180(encoder.degrees(mark, zeroCounts));
        String pass = String.format(Locale.US, "turret: home switch pass %s, band %.1f°",
                home.passDirection() > 0 ? "counter-clockwise" : "clockwise", home.bandDeg());
        if (sweep.sweeping()) {
            // The sweep takes its passes itself and moves the zero once, at its end.
            note(pass + String.format(Locale.US, ", the mark %+.2f° from where it stood", disagree));
            return result;
        }
        switch (TurretZeroPolicy.atSwitchPass(zeroSource == ZeroSource.HAND, zeroed, disagree,
                homeSwitchMaxCorrectionDeg)) {
            case ZERO_AT_SWITCH:
                if (zeroed) {
                    zeroCounts += Math.round(disagree / (encoder.direction * encoder.degreesPerCount()));
                    lastSwitchCorrectionDeg = disagree;
                    note(pass + String.format(Locale.US, ": zero moved %+.1f° (was %s)", disagree,
                            zeroSource.text));
                } else {
                    zeroCounts = mark;
                    note(pass + ": first zero");
                }
                zeroed = true;
                zeroSource = ZeroSource.SWITCH;
                break;
            case SWITCH_DISAGREES:
                note(pass + String.format(Locale.US,
                        ": puts the mark %+.1f° from the zero, over the %.0f° limit: zero kept", disagree,
                        homeSwitchMaxCorrectionDeg));
                break;
            default:
                note(pass + String.format(Locale.US, ": %+.1f° from the zero set by hand, which is kept",
                        disagree));
                break;
        }
        return result;
    }

    /** One step of an Autonomous's INIT sweep: it ends on the switch's zero, a zero set by hand, or its range. */
    private void sweepStep(TurretHomeSwitch.Result pass) {
        if (!sweep.sweeping()) return;
        if (zeroSource == ZeroSource.HAND) {
            sweep.stop();
            note("turret: sweep stopped, zeroed by hand");
            return;
        }
        double angle = encoder == null ? Double.NaN : encoder.degrees(counts, zeroCounts) - aimOffset();
        if (!sweep.step(clock.getAsLong(), angle, magnet, pass, home.passDirection(), home.markCounts(),
                sweepLimits())) {
            return;
        }
        TurretHomeSweep.State state = sweep.state();
        if (state == TurretHomeSweep.State.FOUND || state == TurretHomeSweep.State.ROUGH) {
            long mark = Math.round(sweep.markCounts());
            lastSwitchCorrectionDeg = TurretEncoder.wrap180(encoder.degrees(mark, zeroCounts));
            // The mark the turret just crossed: the angle reads from it, unwound.
            zeroCounts = mark;
            zeroed = true;
            zeroSource = ZeroSource.SWITCH;
            note(String.format(Locale.US, "turret: %s, %+.2f° from where it stood", state.text,
                    lastSwitchCorrectionDeg));
        } else if (state == TurretHomeSweep.State.NOT_FOUND) {
            note(String.format(Locale.US, "turret: the sweep did not find the magnet in %.0f°: zeroed where it stood",
                    homeSweepRangeDeg));
        }
    }

    /** The sweep's settings, from the {@code @Configurable} values. */
    private TurretHomeSweep.Limits sweepLimits() {
        sweepLimits.searchDps = homeSweepSearchDps;
        sweepLimits.fineDps = homeSweepFineDps;
        sweepLimits.searchRangeDeg = homeSweepRangeDeg;
        sweepLimits.overshootDeg = homeSweepOvershootDeg;
        sweepLimits.maxBandDeg = homeSwitchMaxBandDeg;
        sweepLimits.timeoutS = homeSweepTimeoutS;
        return sweepLimits;
    }

    /** A line for the match log; two in one loop are joined. */
    private void note(String text) {
        switchEvent = switchEvent == null ? text : switchEvent + "; " + text;
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

    /** {@link #aimOffsetDeg}, guarded: NaN is 0, and it stays inside ±30 degrees. */
    public static double aimOffset() {
        double offset = aimOffsetDeg;
        if (Double.isNaN(offset)) return 0.0;
        return Math.max(-30.0, Math.min(30.0, offset));
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
                + DeviceNames.TURRET_ENCODER_HOST
                + (homeInput != null ? "; home switch " + DeviceNames.TURRET_HOME + ": "
                        + (magnet ? "magnet (on its mark)" : "no magnet") : "")
                + "; zero: " + zeroSource.text
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
        if (aimOffset() != 0.0) {
            zero += String.format(Locale.US, " · aim offset %+.1f° from the mark", aimOffset());
        }
        if (homeInput != null) {
            zero += " · switch: " + (magnet ? "magnet" : "no magnet")
                    + (sweep.state() != TurretHomeSweep.State.IDLE ? ", " + sweep.state().text : "")
                    + (Double.isNaN(lastSwitchCorrectionDeg) ? ""
                    : String.format(Locale.US, ", last pass moved the zero %+.1f°", lastSwitchCorrectionDeg));
        }
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
                        TurretEncoder.wrap180(targetDeg), errorDeg)
                        + (atArcEdge() ? String.format(Locale.US, " · at the arc's edge: aim %.0f° is behind it",
                        TurretEncoder.wrap180(askedDeg)) : "");
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
