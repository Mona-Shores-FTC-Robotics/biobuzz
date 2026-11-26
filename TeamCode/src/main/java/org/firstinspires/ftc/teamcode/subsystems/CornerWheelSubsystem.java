package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.hardware.DeviceNames;

import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;

/**
 * The corner wheels ({@code doc/corner-wheels.md}): a 3.5 in SWYFT Intake Wheel at each front corner, upright,
 * its 1/2 in hex axle pointing forward and its servo behind it, the wheel's bottom edge sweeping inward, so a
 * POLLEN at the robot's corner, or pinned on a wall there, is raked into the intake mouth. Each is spun by a
 * continuous-rotation servo. <b>Both robots have them</b> (mentor, 26 Nov 2026, after robot testing: "worked
 * very, very well"), on Expansion Hub servo ports 1 ({@code cornerWheelLeft}) and 2 ({@code cornerWheelRight}),
 * so {@link #fitted} is on in the shared value. The GARDEN gather relies on them, with the FLOWER stick up
 * ({@code GardenGather.Stick#useStick}).
 *
 * <p>With {@link #fitted} on, the constructor asks for each servo and the OpMode refuses to start if the XML
 * lacks one. A robot without them (a new chassis) gets a {@code RobotDifferences} line,
 * {@code () -> CornerWheelSubsystem.fitted = false}; one with a single servo (one corner, or both wheels on one
 * belt) adds {@code rightServo = false} and wires that servo as {@code cornerWheelLeft}. Each wheel's direction
 * is set on <b>Bench: Corner wheels</b> ({@link #leftReversed}, {@link #rightReversed}), shared until the bench
 * shows the robots differ.
 *
 * <h2>When they spin</h2>
 *
 * <p>Inward, at {@link #power}, only while every one of these holds ({@link #stopFor}, the whole policy, pure,
 * so a test can check it):
 *
 * <ul>
 *   <li><b>PLAY has started</b>: nothing moves before PLAY (G304.H, G403), and an Autonomous's INIT holds
 *       nothing here.</li>
 *   <li><b>The intake is taking pieces in</b> because the OpMode asked: its own request, the roller turning in,
 *       not a feed's assist, an eject, a reject's burst or hold-off, a wedge or a feed-jam clear.</li>
 *   <li><b>The robot holds fewer than 4</b>: the lane does not read full and the lane tracker counts fewer than
 *       4. A raking wheel herds a piece, which is CONTROL, so a 5th raked in is CONTROL of 5 (G407). The driver's
 *       R2 past a full lane runs the intake, never these.</li>
 *   <li><b>No wrong NECTAR may be in the lane</b> ({@code ColorSensorSubsystem.wrongNectarMayBeIn()}) and none is
 *       being spat out: a wheel raking the opponent's NECTAR CONTROLs it (G408).</li>
 *   <li><b>The FLOWER stick is up.</b> A pick lowers it, and a spinning wheel on a FLOWER's side could pull
 *       POLLEN out through it (G418). With the stick down, its arm lies between each wheel and the mouth
 *       anyway.</li>
 * </ul>
 *
 * <p>Otherwise each servo is written 0 once a loop from PLAY. One write per servo per loop, nothing read.
 */
@Configurable
public class CornerWheelSubsystem implements Subsystem {

    /**
     * The corner wheels are fitted on this robot. On for both robots (mentor, 26 Nov 2026); a robot without them
     * turns it off with a {@code RobotDifferences} line.
     */
    public static boolean fitted = true;

    /**
     * The right wheel has its own servo, {@link DeviceNames#CORNER_WHEEL_RIGHT}. Off: the robot has one servo
     * only, {@link DeviceNames#CORNER_WHEEL_LEFT} (one corner, or both wheels on one belt); a
     * {@code RobotDifferences} line beside {@link #fitted}. Read once, when the subsystem is built.
     */
    public static boolean rightServo = true;

    /** Servo power while raking, 0 to 1. Full: the TPU flaps should throw a ball, not nudge it. Not tuned. */
    public static double power = 1.0;

    /**
     * Flips the left servo so positive power turns the left wheel's front inward (clockwise seen from above).
     * Not seen on a robot: <b>Bench: Corner wheels</b> shows it.
     */
    public static boolean leftReversed = false;

    /**
     * Flips the right servo. The right wheel turns the other way (counter-clockwise seen from above) and the two
     * servos hang the same way up, so true by default. Not seen on a robot.
     */
    public static boolean rightReversed = true;

    /** Why the wheels are stopped, or {@link #RAKING}: the first rule of {@link #stopFor} that holds. */
    public enum Why {
        NOT_FITTED("not fitted"),
        BEFORE_PLAY("before PLAY"),
        INTAKE_NOT_TAKING_IN("intake not taking in"),
        HOLDS_FOUR("holds 4 (G407)"),
        WRONG_NECTAR("wrong NECTAR (G408)"),
        STICK_DOWN("FLOWER stick down (G418)"),
        RAKING("raking");

        public final String label;

        Why(String label) {
            this.label = label;
        }
    }

    private final CRServo left;
    private final CRServo right;
    private final String atInit;

    private BooleanSupplier takingIn = () -> false;
    private BooleanSupplier laneFull = () -> true;
    private IntSupplier held = () -> 0;
    private BooleanSupplier wrongNectar = () -> false;
    private BooleanSupplier stickDown = () -> false;
    private boolean started;
    private Why why = Why.NOT_FITTED;

    /**
     * Looks up each servo when {@link #fitted} is on, and only then. A switch on with no such device in the
     * active configuration fails here with what to add, rather than running without it.
     */
    public CornerWheelSubsystem(HardwareMap hardwareMap) {
        left = fitted ? lookUp(hardwareMap, DeviceNames.CORNER_WHEEL_LEFT) : null;
        right = fitted && rightServo ? lookUp(hardwareMap, DeviceNames.CORNER_WHEEL_RIGHT) : null;
        atInit = left == null ? "corner wheels not fitted"
                : "corner wheels: " + (right == null ? "one servo" : "two servos") + ", presence unknown";
    }

    private static CRServo lookUp(HardwareMap hardwareMap, String name) {
        CRServo servo = hardwareMap.tryGet(CRServo.class, name);
        if (servo == null) {
            throw new IllegalStateException("CornerWheelSubsystem.fitted is on, but the active configuration has no"
                    + " ContinuousRotationServo \"" + name + "\". Add <ContinuousRotationServo name=\"" + name
                    + "\" port=\"N\" /> to the robot's XML (full TeamCode install, Activate), or turn the switch"
                    + " off in RobotDifferences.");
        }
        return servo;
    }

    /** Whether the corner-wheel servo named {@code name} is asked for on this robot, for Check: Hardware. */
    public static boolean fitted(String name) {
        if (DeviceNames.CORNER_WHEEL_LEFT.equals(name)) return fitted;
        if (DeviceNames.CORNER_WHEEL_RIGHT.equals(name)) return fitted && rightServo;
        return false;
    }

    /** The switch for the servo named {@code name}, for Check: Hardware. */
    public static String switchName(String name) {
        if (DeviceNames.CORNER_WHEEL_RIGHT.equals(name)) return "CornerWheelSubsystem.fitted and rightServo";
        return "CornerWheelSubsystem.fitted";
    }

    /** Whether the name is one of the corner wheels' servos. */
    public static boolean isCornerWheel(String name) {
        return DeviceNames.CORNER_WHEEL_LEFT.equals(name) || DeviceNames.CORNER_WHEEL_RIGHT.equals(name);
    }

    /**
     * What the stop rules read, wired once by {@code Robot}: whether the intake is taking in at the OpMode's
     * request (nothing backing out or holding off), whether the lane reads full, how many pieces the lane
     * tracker counts, whether a wrong NECTAR may be in or is being spat out, and whether the FLOWER stick is
     * asked down. Until then the lane reads full, so nothing spins.
     */
    public void watch(BooleanSupplier takingIn, BooleanSupplier laneFull, IntSupplier held,
                      BooleanSupplier wrongNectar, BooleanSupplier stickDown) {
        this.takingIn = takingIn;
        this.laneFull = laneFull;
        this.held = held;
        this.wrongNectar = wrongNectar;
        this.stickDown = stickDown;
    }

    @Override
    public void initialize() {
        started = false;
    }

    @Override
    public void start() {
        started = true;
    }

    @Override
    public void update() {
        if (left == null) {
            why = Why.NOT_FITTED;
            return;
        }
        why = stopFor(started, takingIn.getAsBoolean(), laneFull.getAsBoolean(), held.getAsInt(),
                wrongNectar.getAsBoolean(), stickDown.getAsBoolean());
        if (!started) return;
        boolean on = why == Why.RAKING;
        left.setPower(powerFor(on, power, leftReversed));
        if (right != null) right.setPower(powerFor(on, power, rightReversed));
    }

    @Override
    public void stop() {
        started = false;
        if (left != null) left.setPower(0.0);
        if (right != null) right.setPower(0.0);
    }

    /** Whether the wheels are raking as of the last {@link #update()}. */
    public boolean raking() {
        return why == Why.RAKING;
    }

    /** Why the wheels are stopped, or {@link Why#RAKING}, as of the last {@link #update()}. */
    public Why why() {
        return why;
    }

    /** Whether this robot has the corner wheels ({@link #fitted}, as when the subsystem was built). */
    public boolean present() {
        return left != null;
    }

    @Override
    public String health() {
        return atInit;
    }

    @Override
    public void describe(Display display) {
        if (left == null) {
            display.status("Corner wheels", Display.Level.OK, "not fitted (CornerWheelSubsystem.fitted)");
            return;
        }
        display.status("Corner wheels", Display.Level.OK, why.label);
    }

    /**
     * The stop rules, in order; the first that holds is why the wheels are stopped, else {@link Why#RAKING}.
     * {@code held} is the lane tracker's count. Pure, so a test can check it.
     */
    static Why stopFor(boolean started, boolean takingIn, boolean laneFull, int held, boolean wrongNectar,
                       boolean stickDown) {
        if (!started) return Why.BEFORE_PLAY;
        if (!takingIn) return Why.INTAKE_NOT_TAKING_IN;
        if (laneFull || held >= 4) return Why.HOLDS_FOUR;
        if (wrongNectar) return Why.WRONG_NECTAR;
        if (stickDown) return Why.STICK_DOWN;
        return Why.RAKING;
    }

    /**
     * A servo's power: {@code +power} raking, 0 stopped, negated when {@code reversed}; stopped is always a plain
     * 0, never -0. The power is clamped to [0, 1], NaN as 0. Pure; the bench uses it too.
     */
    public static double powerFor(boolean on, double power, boolean reversed) {
        double p = on && power > 0.0 ? Math.min(power, 1.0) : 0.0;
        if (p == 0.0) return 0.0;
        return reversed ? -p : p;
    }
}
