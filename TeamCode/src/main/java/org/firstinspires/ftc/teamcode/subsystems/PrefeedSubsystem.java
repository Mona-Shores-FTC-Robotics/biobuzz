package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.hardware.DeviceNames;

import java.util.Locale;

/**
 * The prefeed wheel: the big grey wheel at the start of the transfer, on a goBILDA Torque servo in
 * continuous mode. It runs whenever the intake takes pieces in and whenever the robot is
 * launching, so a piece that has come through the intake is carried up to the feed gate and, with
 * the gate open, on into the feed wheels (the feeder's four blue wheels).
 *
 * <p>Nothing has to ask for it. The prefeed reads the intake's and the feeder's requests every
 * {@link #update()} and does what {@link #requestFor} says, so the TeleOp, every Autonomous and the
 * bench all get the same behaviour, and "the prefeed runs whenever the intake runs" lives in one
 * pure function a test can check. It is updated after the intake and the feeder, so it follows this
 * loop's request, not last loop's. {@link #override} replaces that for a bench; {@link #follow}
 * gives it back.
 *
 * <p><b>Not yet checked on a robot:</b> the powers, the direction ({@link #reversed}), and whether
 * the prefeed should back out with the intake at all ({@link #outPower}, 0 turns that off).
 *
 * <p>The servo is not written before PLAY: a continuous servo told "stop" creeps if its stop is not
 * quite centred, and nothing may move after INIT (G304.H, G403; see {@link Subsystem}).
 */
@Configurable
public class PrefeedSubsystem implements Subsystem {

    /** What the prefeed is doing. */
    public enum State { IDLE, IN, OUT }

    /** Servo power carrying pieces toward the gate, 0 to 1. <b>Not yet tuned.</b> */
    public static double inPower = 0.8;

    /**
     * Servo power carrying pieces back toward the intake, while ejecting or clearing a jam, 0 to 1.
     * <b>Not yet tuned.</b> 0 leaves the prefeed still then.
     */
    public static double outPower = 0.6;

    /**
     * Flips the servo so that positive power carries pieces toward the gate. <b>Not yet checked on a
     * robot:</b> {@code false} is the SDK's default direction, not a fact about the wiring.
     */
    public static boolean reversed = false;

    private final CRServo servo;
    private final IntakeSubsystem intake;
    private final FeederSubsystem feeder;
    private State state = State.IDLE;
    private State override;
    private double power;
    private boolean started;

    /**
     * Looks the servo up once. A missing servo fails init with the SDK's own message.
     *
     * @param intake read for its request; updated before this subsystem
     * @param feeder read for its request; updated before this subsystem
     */
    public PrefeedSubsystem(HardwareMap hardwareMap, IntakeSubsystem intake, FeederSubsystem feeder) {
        servo = hardwareMap.get(CRServo.class, DeviceNames.PREFEED);
        this.intake = intake;
        this.feeder = feeder;
    }

    /** Hold the prefeed in {@code state} whatever the intake and feeder ask, for a bench. */
    public void override(State state) {
        override = state;
    }

    /** Follow the intake and the feeder again ({@link #requestFor}). The default. */
    public void follow() {
        override = null;
    }

    /** Whether a bench is holding the prefeed instead of the intake and feeder. */
    public boolean overridden() {
        return override != null;
    }

    /** What the prefeed is doing as of the last {@link #update()}. The request, not a measurement. */
    public State state() {
        return state;
    }

    /** The servo power written in the last {@link #update()}, signed, for the match log. */
    public double power() {
        return power;
    }

    /** Start idle and unwritten: the servo is not touched until PLAY. */
    @Override
    public void initialize() {
        state = State.IDLE;
        override = null;
        power = 0.0;
        started = false;
    }

    /** PLAY: the servo may be written from now on. */
    @Override
    public void start() {
        started = true;
    }

    /**
     * Decide the state from the intake and feeder (or the bench's override), then, from PLAY, one
     * write to the servo, clamped so Panels cannot overdrive it. Nothing is written before PLAY.
     */
    @Override
    public void update() {
        state = override != null ? override : requestFor(intake.state(), feeder.state());
        power = powerFor(state, inPower, outPower, reversed);
        if (started) {
            servo.setPower(power);
        }
    }

    /** Cut the servo. Safe whether or not {@link #initialize()} ran; a servo never written is already limp. */
    @Override
    public void stop() {
        state = State.IDLE;
        power = 0.0;
        if (started) {
            servo.setPower(0.0);
        }
    }

    @Override
    public String health() {
        return "servo configured; presence unknown until it moves";
    }

    @Override
    public void describe(Display display) {
        display.status("Prefeed", state == State.IDLE ? Display.Level.OK : Display.Level.WARN,
                (state == State.IN ? String.format(Locale.US, "IN (%.2f)", power)
                        : state == State.OUT ? String.format(Locale.US, "OUT (%.2f)", power)
                        : "idle") + (override != null ? " · held by the bench" : ""));
    }

    /**
     * The policy, as one pure function: the prefeed carries pieces toward the gate while the intake
     * takes them in or the feeder feeds, and back while the intake ejects or the feeder reverses.
     * Backing out wins, because clearing a jam is always allowed.
     */
    public static State requestFor(IntakeSubsystem.State intake, FeederSubsystem.State feeder) {
        if (intake == IntakeSubsystem.State.OUT || feeder == FeederSubsystem.State.REVERSE) {
            return State.OUT;
        }
        if (intake == IntakeSubsystem.State.IN || feeder == FeederSubsystem.State.FEED) {
            return State.IN;
        }
        return State.IDLE;
    }

    /**
     * The servo power for a state: {@code +in} for IN, {@code -out} for OUT, 0 when idle, each
     * magnitude clamped to [0, 1] (NaN counts as 0) and negated when {@code reversed}. Pure, so a
     * test can check it.
     */
    static double powerFor(State state, double in, double out, boolean reversed) {
        double p;
        switch (state) {
            case IN:
                p = IntakeSubsystem.unitRange(in);
                break;
            case OUT:
                p = -IntakeSubsystem.unitRange(out);
                break;
            default:
                p = 0.0;
        }
        return reversed ? -p : p;
    }
}
