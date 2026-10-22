package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.hardware.DeviceNames;

/**
 * The intake roller: one motor, run in, run out, or idle. The first edition, enough to spin the
 * roller from a gamepad so the build team can test the mechanism (#160).
 *
 * <p>Same shape as {@link FlowerStickSubsystem}: the OpMode says what it wants ({@link #intake()},
 * {@link #eject()}, {@link #idle()}), {@link #update()} does it, once per loop. Which button does
 * what belongs in the OpMode's bindings, not here.
 *
 * <p>What this does not do yet: count pieces, stop at four, or feed the Auto's {@code IntakeFull}
 * trigger. That is #181, and it layers on top of this class.
 *
 * <p>The powers and the direction are {@code @Configurable}. Nobody can tell from a desk which way
 * the roller is wired: if it spits pieces out on "intake", flip {@link #reversed} in Panels and
 * write {@code true} back here with a note saying which robot it was checked on.
 */
@Configurable
public class IntakeSubsystem implements Subsystem {

    /** What the roller is being asked to do. */
    public enum State { IDLE, IN, OUT }

    /** Motor power while taking pieces in, 0 to 1. <b>Not yet tuned.</b> */
    public static double inPower = 1.0;

    /** Motor power while pushing pieces back out, 0 to 1. <b>Not yet tuned.</b> */
    public static double outPower = 0.6;

    /**
     * Flips the motor so that positive power takes pieces in. <b>Not yet checked on a robot:</b>
     * {@code false} is the SDK's default direction, not a fact about the wiring.
     */
    public static boolean reversed = false;

    private final DcMotorEx motor;
    private State state = State.IDLE;

    /** Looks the motor up once. A missing motor fails init with the SDK's own message. */
    public IntakeSubsystem(HardwareMap hardwareMap) {
        motor = hardwareMap.get(DcMotorEx.class, DeviceNames.INTAKE);
    }

    /** Ask for pieces in. Takes effect in the next {@link #update()}. */
    public void intake() {
        state = State.IN;
    }

    /** Ask for pieces out. Takes effect in the next {@link #update()}. */
    public void eject() {
        state = State.OUT;
    }

    /** Ask for the roller to stop. Not {@link #stop()}: the subsystem keeps running. */
    public void idle() {
        state = State.IDLE;
    }

    /** What the roller has been asked to do. There is no sensor, so this is the request. */
    public State state() {
        return state;
    }

    /** Start idle, in plain power mode. */
    @Override
    public void initialize() {
        state = State.IDLE;
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motor.setPower(0.0);
    }

    /** One write per loop: the power the request asks for, clamped so Panels cannot overdrive it. */
    @Override
    public void update() {
        motor.setPower(powerFor(state, inPower, outPower, reversed));
    }

    /** Cut power. Safe to call whether or not {@link #initialize()} ran. */
    @Override
    public void stop() {
        state = State.IDLE;
        motor.setPower(0.0);
    }

    @Override
    public void describe(Display display) {
        display.status("Intake", state == State.IDLE ? Display.Level.OK : Display.Level.WARN,
                state == State.IN ? "IN" : state == State.OUT ? "OUT" : "idle");
    }

    /**
     * The motor power for a request: {@code +in} for IN, {@code -out} for OUT, 0 when idle, each
     * magnitude clamped to [0, 1] and negated when {@code reversed}. Pure, so a test can check it.
     */
    static double powerFor(State state, double in, double out, boolean reversed) {
        double power;
        switch (state) {
            case IN:
                power = unitRange(in);
                break;
            case OUT:
                power = -unitRange(out);
                break;
            default:
                power = 0.0;
        }
        return reversed ? -power : power;
    }

    /**
     * Clamps a Panels-editable power to [0, 1], and treats NaN as 0. A typo in Panels should give
     * a roller that is too slow, never one commanded past full power or sent NaN.
     */
    static double unitRange(double value) {
        return value > 0.0 ? Math.min(value, 1.0) : 0.0;
    }
}
