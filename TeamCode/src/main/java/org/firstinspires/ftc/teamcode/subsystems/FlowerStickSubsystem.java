package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PwmControl;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.hardware.DeviceNames;

/**
 * The FLOWER stick: one servo swinging a stick, the FLOWER extractor's first edition (#177).
 *
 * <p>The smallest real subsystem in the repo, and the pattern every mechanism follows: the OpMode
 * says what it wants ({@link #extend()}, {@link #retract()}, {@link #toggle()}), {@link #update()}
 * does it, once per loop. Which button triggers it belongs in the OpMode's bindings, not here.
 *
 * <p>The servo only swings the stick. Once the stick is down, its wheels sit on the tile and the
 * push from the FLOWER goes into the tile through the arm, not into the servo, as long as nothing
 * is holding the arm somewhere else. A positional servo cannot "rest" on the tile: a position a
 * hair above the tile carries the stick and takes the push, a hair below stalls against the tile.
 * So after the swing the pulse is switched off ({@link PwmControl#setPwmDisable()}): the servo goes
 * limp and the stick rests on the tile by its own weight. Stowed, the servo keeps holding, because
 * the stowed hard stop may not exist yet; {@link #LIMP_STOWED} releases it there too once it does.
 *
 * <p>Nothing is written before PLAY, so the stick does not move at INIT (G304.H, G403; see
 * {@link Subsystem}). The drive team stows it by hand before the match; at PLAY the servo takes
 * it and holds it in. If it ever has to be held in to fit the 18 in starting cube, that is a
 * mechanical stop or latch (R103.A), not the servo at INIT: the TeleOp's INIT runs between AUTO and
 * TELEOP too, where a stick swinging in after an Auto is a G403 foul.
 *
 * <p>The two positions are {@code @Configurable}: drag them in Panels with the servo moving until
 * the stick sits on each stop, then write the numbers back here with a comment saying which servo
 * and stick they were measured on. Panels edits are lost on restart.
 */
@Configurable
public class FlowerStickSubsystem implements Subsystem {

    /**
     * Servo position with the stick in, 0 to 1. Measured on 20245 on 26 Oct 2026 with the goBILDA
     * Torque servo on Expansion Hub servo port 1; 19429 not yet checked.
     */
    public static double RETRACTED = 0.6;

    /** Servo position with the stick out, 0 to 1. Measured with {@link #RETRACTED}. */
    public static double EXTENDED = 0.0;

    /**
     * Milliseconds the servo is given to finish a swing before the pulse is cut. A goBILDA Torque
     * servo does 60 degrees in 0.25 s unloaded, so the 162 degree swing takes about 0.7 s; the stick
     * on it, more. If the stick is still moving when the servo goes limp it just drops the rest of
     * the way onto the tile.
     */
    public static int SETTLE_MS = 1500;

    /**
     * Degrees the servo turns over its whole 0 to 1 range: the goBILDA Torque servo's 270, which
     * makes {@link #RETRACTED} to {@link #EXTENDED} the 162 degree swing ({@link #SETTLE_MS}).
     */
    public static final double SERVO_RANGE_DEG = 270.0;

    /** Cut the pulse once the stick is out and settled, so the stick rests on the tile. */
    public static boolean LIMP_EXTENDED = true;

    /** Cut the pulse once the stick is in and settled. Only once a stowed hard stop exists. */
    public static boolean LIMP_STOWED = false;

    private final Servo servo;
    private final PwmControl pwm;   // null if this servo cannot switch its pulse off
    private boolean out = false;
    private boolean lastOut = false;
    private boolean limp = false;
    private boolean started;
    private long movedAtMs = 0;
    private final DriveSubsystem drive;
    // A pending retractWhenClear: the stick comes in once the robot is clearIn from (clearX, clearY).
    private boolean waitingToClear = false;
    private double clearX;
    private double clearY;
    private double clearIn;

    /**
     * Looks the servo up once. A missing servo fails init with the SDK's own message.
     *
     * @param drive read for the pose by {@link #retractWhenClear}; updated before this subsystem
     */
    public FlowerStickSubsystem(HardwareMap hardwareMap, DriveSubsystem drive) {
        servo = hardwareMap.get(Servo.class, DeviceNames.FLOWER_STICK);
        pwm = servo instanceof PwmControl ? (PwmControl) servo : null;
        this.drive = drive;
    }

    /** Ask for the stick out. Takes effect in the next {@link #update()}. */
    public void extend() {
        waitingToClear = false;
        out = true;
    }

    /** Ask for the stick in. Takes effect in the next {@link #update()}. */
    public void retract() {
        waitingToClear = false;
        out = false;
    }

    /**
     * Ask for the stick in, but only once the robot's centre is at least {@code inches} from the
     * field point ({@code x}, {@code y}): a stick swung up while it is still in a FLOWER pulls on the
     * FLOWER. The stick stays down until then, checked every {@link #update()}, whatever ended the
     * grab and whoever drives the robot away. At once if the robot is already that far, if the stick
     * is in, or if there is no pose to check. Any other request ({@link #extend}, {@link #retract},
     * {@link #toggle}) replaces it, so the operator's button always wins.
     */
    public void retractWhenClear(double x, double y, double inches) {
        if (!out) return;
        waitingToClear = true;
        clearX = x;
        clearY = y;
        clearIn = inches;
    }

    /** Whether the stick is out only until the robot is clear ({@link #retractWhenClear}). */
    public boolean waitingToClear() {
        return waitingToClear;
    }

    /**
     * Ask for the other end: in if the stick is out, out if it is in. One button on a gamepad does
     * both; a press mid-swing turns the stick round, which the servo handles as a new target.
     */
    public void toggle() {
        waitingToClear = false;
        out = !out;
    }

    /** Whether the stick has been asked out. The servo has no sensor, so this is the request. */
    public boolean extended() {
        return out;
    }

    /** Whether the servo's pulse is off and the stick is resting on its own. */
    public boolean limp() {
        return limp;
    }

    /** The swing from in to out, degrees: the two positions' distance over the servo's range. */
    public static double swingDegrees() {
        return Math.abs(unitRange(RETRACTED) - unitRange(EXTENDED)) * SERVO_RANGE_DEG;
    }

    /**
     * Degrees swung out from stowed as commanded: 0 in, {@link #swingDegrees()} out. What the
     * servo was asked for, not a measurement (nothing measures the stick); it jumps at the request
     * while the stick takes about a second to follow. For the robot model in the match log.
     */
    public double commandedDegrees() {
        return out ? swingDegrees() : 0.0;
    }

    /**
     * Asked in, and nothing written: the servo stays limp until PLAY (the SDK cuts every servo's
     * pulse when an OpMode ends, and a servo port sends none until it is written).
     */
    @Override
    public void initialize() {
        waitingToClear = false;
        out = false;
        lastOut = false;
        limp = pwm != null;
        started = false;
    }

    /** PLAY: the servo takes the stick and holds it in, unless something already asked it out. */
    @Override
    public void start() {
        started = true;
        movedAtMs = System.currentTimeMillis();
    }

    /**
     * Nothing before PLAY. Then one write per loop while the servo is working: the position the
     * request asks for, clamped so Panels cannot overdrive it. A change of request powers the servo
     * and starts the swing; once the swing has had {@link #SETTLE_MS} the pulse is cut if that end
     * asks for it. The position goes out before the pulse is switched back on, so a limp servo
     * never wakes up driving to an old one.
     */
    @Override
    public void update() {
        if (!started) {
            return;
        }
        long now = System.currentTimeMillis();
        if (waitingToClear && clearOf(drive.pose(), clearX, clearY, clearIn)) {
            waitingToClear = false;
            out = false;
        }
        if (out != lastOut) {
            lastOut = out;
            movedAtMs = now;
        }
        boolean wantLimp = shouldGoLimp(now - movedAtMs, SETTLE_MS, out ? LIMP_EXTENDED : LIMP_STOWED);
        if (wantLimp) {
            power(false);
        } else {
            servo.setPosition(unitRange(out ? EXTENDED : RETRACTED));
            power(true);
        }
    }

    /** A servo holds where it is; there is nothing to cut. */
    @Override
    public void stop() {
    }

    @Override
    public String health() {
        if (pwm == null) {
            return "servo configured; it cannot switch its pulse off, so it holds the stick down";
        }
        return "servo configured; presence unknown (a servo reports nothing back)";
    }

    /** Switches the servo's pulse on or off, if it can, and remembers which. */
    private void power(boolean on) {
        if (pwm == null) {
            limp = false;
            return;
        }
        if (on && limp) {
            pwm.setPwmEnable();
        } else if (!on && !limp) {
            pwm.setPwmDisable();
        }
        limp = !on;
    }

    /**
     * Whether the servo should be limp: the swing has had its settle time and this end asks for it.
     * Pure, so the test can pin it down.
     */
    static boolean shouldGoLimp(long sinceMoveMs, int settleMs, boolean limpWanted) {
        return limpWanted && sinceMoveMs >= settleMs;
    }

    /**
     * Whether {@code pose} is at least {@code inches} from (x, y). No pose counts as clear: without
     * one the stick could never come in. Pure, so the test can pin it down.
     */
    static boolean clearOf(Pose pose, double x, double y, double inches) {
        return pose == null || Math.hypot(pose.x() - x, pose.y() - y) >= inches;
    }

    /**
     * Clamps a Panels-editable position to [0, 1], and treats NaN as 0. A typo in Panels should
     * leave the stick in, never command the servo past its travel or send NaN.
     */
    static double unitRange(double value) {
        return value > 0.0 ? Math.min(value, 1.0) : 0.0;
    }
}
