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
 * The feeder: one motor that pushes a held piece into the launcher's wheels. The first rung,
 * written to the wiring spec ({@code feeder} on Expansion Hub motor port 3) before the transfer
 * is designed (#164): run forward, run backward, or idle, from a held button.
 *
 * <p>Same shape as {@link IntakeSubsystem}. What this does not do yet: feed exactly one piece
 * ({@code feedOne()}), know how many pieces it holds, or feed the Auto's {@code LaunchOne} and
 * {@code Empty} cards. Those need the transfer's geometry and a sensor, and come with #164.
 *
 * <p>Whether the feeder may run while the launcher is still spinning up is a TeleOp policy, not a
 * fact about this motor, so it lives in the OpMode; {@link #requestFor} is the pure form of that
 * policy so a test can check it without a gamepad.
 */
@Configurable
public class FeederSubsystem implements Subsystem {
    // On the competition robots this motor's encoder socket carries the turret's encoder
    // (DeviceNames.TURRET_ENCODER_HOST), so ticksPerSecond() and rpm() there are the turret
    // moving, not the feeder. They read the feeder itself only on the launcher bench rig.

    /** What the feeder is being asked to do. */
    public enum State { IDLE, FEED, REVERSE }

    /** Motor power while feeding into the launcher, 0 to 1. <b>Not yet tuned.</b> */
    public static double feedPower = 0.8;

    /** Motor power while backing a piece out of the launcher, 0 to 1. <b>Not yet tuned.</b> */
    public static double reversePower = 0.5;

    /**
     * Flips the motor so that positive power feeds toward the launcher. {@code true}: on the
     * launcher bench the default direction ran the feeder backwards (Square reversed, Triangle
     * fed). <b>Not yet checked on a robot:</b> if its feeder runs the wrong way, flip this in Panels.
     */
    public static boolean reversed = true;

    /**
     * Encoder ticks per revolution of the feeder motor's output shaft, for {@link #rpm()}. 384.5:
     * the feeder motors are goBILDA 5203 435 RPM, read off the label at the bench on 24 Oct 2026.
     */
    public static double ticksPerRev = 384.5;

    private final DcMotorEx motor;
    private State state = State.IDLE;
    private double power;
    private double ticksPerSecond;
    private double currentAmps = Double.NaN;

    /** Looks the motor up once. A missing motor fails init with the SDK's own message. */
    public FeederSubsystem(HardwareMap hardwareMap) {
        motor = hardwareMap.get(DcMotorEx.class, DeviceNames.FEEDER);
    }

    /** Ask for pieces toward the launcher. Takes effect in the next {@link #update()}. */
    public void feed() {
        state = State.FEED;
    }

    /** Ask for pieces back out of the launcher, to clear a jam. Takes effect in the next {@link #update()}. */
    public void reverse() {
        state = State.REVERSE;
    }

    /** Ask for the feeder to hold still. Not {@link #stop()}: the subsystem keeps running. */
    public void idle() {
        state = State.IDLE;
    }

    /** What the feeder has been asked to do. The request, not a measurement. */
    public State state() {
        return state;
    }

    /** The motor power written in the last {@link #update()}, signed, for the match log. */
    public double power() {
        return power;
    }

    /** Feeder motor speed from the last {@link #update()}, encoder ticks per second, signed. 0 without an encoder. */
    public double ticksPerSecond() {
        return ticksPerSecond;
    }

    /**
     * Feeder motor current from the last {@link #update()}, amps; NaN while idle. Read only while
     * running, since current is a separate hub transaction. A rising current with a falling speed is
     * a piece jamming.
     */
    public double currentAmps() {
        return currentAmps;
    }

    /** {@link #ticksPerSecond()} in output-shaft RPM, or NaN until {@link #ticksPerRev} is set. */
    public double rpm() {
        return IntakeSubsystem.rpm(ticksPerSecond, ticksPerRev);
    }

    /** Start idle, in plain power mode, braking so a held piece stays put. */
    @Override
    public void initialize() {
        state = State.IDLE;
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motor.setPower(0.0);
    }

    /**
     * One write per loop, the power the request asks for, clamped so Panels cannot overdrive it;
     * then one read of speed (from the bulk read) and, while running, one of current.
     */
    @Override
    public void update() {
        power = powerFor(state, feedPower, reversePower, reversed);
        motor.setPower(power);
        ticksPerSecond = motor.getVelocity();
        currentAmps = state == State.IDLE ? Double.NaN : motor.getCurrent(CurrentUnit.AMPS);
    }

    /** Cut power. Safe to call whether or not {@link #initialize()} ran. */
    @Override
    public void stop() {
        state = State.IDLE;
        power = 0.0;
        motor.setPower(0.0);
    }

    @Override
    public String health() {
        return "motor configured; presence unknown until it moves";
    }

    @Override
    public void describe(Display display) {
        display.status("Feeder", state == State.IDLE ? Display.Level.OK : Display.Level.WARN,
                state == State.FEED ? String.format(Locale.US, "FEEDING (%.2f)", power)
                        : state == State.REVERSE ? String.format(Locale.US, "REVERSE (%.2f)", power)
                        : "idle");
        double rpm = rpm();
        display.line(String.format(Locale.US, "Feeder %.0f ticks/s%s · %s", ticksPerSecond,
                Double.isNaN(rpm) ? "" : String.format(Locale.US, " (%.0f rpm)", rpm),
                Double.isNaN(currentAmps) ? "current not read while idle"
                        : String.format(Locale.US, "%.2f A", currentAmps)));
    }

    /**
     * The TeleOp policy for the feeder, as one pure function: reverse wins (clearing a jam is
     * always allowed); then feed if asked, where the launcher being at speed is a condition for
     * <b>starting</b> a feed, not for continuing one. A piece meeting the wheels slows them for a
     * moment, and a gate checked every loop would stop the feeder on that dip, recover, feed again,
     * and pulse. So once feeding ({@code alreadyFeeding}), a held button keeps feeding until it is
     * released. {@code requireAtSpeed} false drops the start condition for a bench.
     */
    public static State requestFor(boolean feedHeld, boolean reverseHeld, boolean launcherAtSpeed,
                                   boolean requireAtSpeed, boolean alreadyFeeding) {
        if (reverseHeld) return State.REVERSE;
        if (feedHeld && (alreadyFeeding || launcherAtSpeed || !requireAtSpeed)) return State.FEED;
        return State.IDLE;
    }

    /**
     * The motor power for a request: {@code +feed} for FEED, {@code -reverse} for REVERSE, 0 when
     * idle, each magnitude clamped to [0, 1] and negated when {@code reversed}. Pure, so a test can
     * check it.
     */
    static double powerFor(State state, double feed, double reverse, boolean reversed) {
        double p;
        switch (state) {
            case FEED:
                p = IntakeSubsystem.unitRange(feed);
                break;
            case REVERSE:
                p = -IntakeSubsystem.unitRange(reverse);
                break;
            default:
                p = 0.0;
        }
        return reversed ? -p : p;
    }
}
