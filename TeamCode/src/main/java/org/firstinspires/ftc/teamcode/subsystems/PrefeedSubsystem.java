package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.hardware.DeviceNames;

import java.util.Locale;
import java.util.function.BooleanSupplier;

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
 * <p>Once the lane is full ({@link #stopWhenFull}) taking in stops running it, as the intake's
 * roller drops to its hold power, so nothing more is pushed up the lane while the stars hold the last piece; a
 * feed still runs it, and the moment a shot moves the piece off the sensor, taking in runs it again.
 *
 * <p>No battery compensation, unlike the feeder ({@code FeederSubsystem.compensateVoltage}): a
 * continuous servo runs from the hub's regulated servo supply, not the battery, so its speed does not
 * sag with it. If a log ever shows the prefeed slowing as the battery falls, that supply is browning
 * out, which compensation could not fix anyway.
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

    /** Servo power carrying pieces toward the gate, 0 to 1. 1.0: set by the mentor on robot 20245 from launch testing, 1 Nov 2026. */
    public static double inPower = 1.0;

    /**
     * Servo power carrying pieces back toward the intake, while ejecting or clearing a jam, 0 to 1.
     * 0.8: set by the mentor on robot 20245 from launch testing, 1 Nov 2026. 0 leaves the prefeed
     * still then.
     */
    public static double outPower = 0.8;

    /**
     * Flips the servo so that positive power carries pieces toward the gate. <b>Not yet checked on a
     * robot:</b> {@code false} is the SDK's default direction, not a fact about the wiring.
     */
    public static boolean reversed = false;

    /**
     * At the start of each feed, how long the prefeed gives the feed gate to swing open before it
     * pushes, in seconds. 0 turns this off: the prefeed pushes as soon as the feeder feeds. A NECTAR
     * pressed against the closed gate by the prefeed jammed the plate as it opened; 0.5 s at full
     * reverse was tried on 20245 on 1 Nov 2026. Off (0) since the robot went POLLEN only the same day:
     * the mentor's call, so a feed starts pushing at once.
     */
    public static double gateClearSeconds = 0.0;

    /**
     * Prefeed power during {@link #gateClearSeconds}, 0 to 1, away from the gate. 0 holds the prefeed
     * still (lets the gate open with the piece resting on it); more than 0 backs it off at that power
     * (pulls the piece away so the gate has room). 0 (off) with {@link #gateClearSeconds}, since 20245
     * went POLLEN only on 1 Nov 2026.
     */
    public static double gateClearPower = 0.0;

    /**
     * Whether the prefeed stops when the lane is full and only the intake was asking for it, the
     * stars and a slow roller holding the last piece in ({@code IntakeSubsystem.holdWhenFull}; the mentor's call, 4 Nov 2026). A feed
     * still runs it and an unjam still backs it out. Off if a false full sticks: with the prefeed
     * stopped, a piece that only paused under the sensor stays there until a shot or an unjam
     * moves it ({@code ColorSensorSubsystem.fullHoldS}). Read every loop, so a Panels edit applies at once.
     */
    public static boolean stopWhenFull = true;

    private final CRServo servo;
    private final IntakeSubsystem intake;
    private final FeederSubsystem feeder;
    private State state = State.IDLE;
    private State override;
    private double power;
    private boolean started;
    private boolean wasFeeding;
    private long feedStartNs;
    private boolean clearingGate;
    private boolean stoppedForFull;
    /** Whether the lane is full; never until {@link #laneFull} is called. */
    private BooleanSupplier laneFull = () -> false;

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

    /**
     * Where to read whether the lane is full, for {@link #stopWhenFull}. Wired once by
     * {@code Robot} to the colour sensor, as the intake's full hold is.
     */
    public void laneFull(BooleanSupplier full) {
        laneFull = full;
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

    /** Whether the last {@link #update()} was holding back to let the gate open ({@link #gateClearSeconds}). */
    public boolean clearingGate() {
        return clearingGate;
    }

    /** Whether the last {@link #update()} stopped the prefeed because the lane is full ({@link #stopWhenFull}). */
    public boolean stoppedForFull() {
        return stoppedForFull;
    }

    /** Start idle and unwritten: the servo is not touched until PLAY. */
    @Override
    public void initialize() {
        state = State.IDLE;
        override = null;
        power = 0.0;
        started = false;
        wasFeeding = false;
        clearingGate = false;
        stoppedForFull = false;
    }

    /** PLAY: the servo may be written from now on. */
    @Override
    public void start() {
        started = true;
    }

    /**
     * Decide the state from the intake and feeder (or the bench's override), then, from PLAY, one
     * write to the servo, clamped so Panels cannot overdrive it. Nothing is written before PLAY. For
     * the first {@link #gateClearSeconds} of each feed the prefeed holds or backs off instead of
     * pushing, so the gate can open ({@link #clearingGate(double, double)}).
     */
    @Override
    public void update() {
        if (override != null) {
            state = override;
            stoppedForFull = false;
        } else {
            boolean full = stopWhenFull && laneFull.getAsBoolean();
            state = requestFor(intake.starsState(), feeder.state(), full);
            stoppedForFull = full && requestFor(intake.starsState(), feeder.state()) != state;
        }
        boolean feeding = override == null && feeder.state() == FeederSubsystem.State.FEED;
        if (feeding && !wasFeeding) {
            feedStartNs = System.nanoTime();
        }
        wasFeeding = feeding;
        clearingGate = feeding && state == State.IN
                && clearingGate((System.nanoTime() - feedStartNs) / 1e9, gateClearSeconds);
        power = clearingGate
                ? powerFor(State.OUT, 0.0, gateClearPower, reversed)
                : powerFor(state, inPower, outPower, reversed);
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
                (clearingGate ? String.format(Locale.US, "clearing the gate (%.2f)", power)
                        : state == State.IN ? String.format(Locale.US, "IN (%.2f)", power)
                        : state == State.OUT ? String.format(Locale.US, "OUT (%.2f)", power)
                        : stoppedForFull ? "idle (lane full)"
                        : "idle") + (override != null ? " · held by the bench" : ""));
    }

    /**
     * The policy, as one pure function: the prefeed carries pieces toward the gate while the intake
     * takes them in or the feeder feeds, and back while the intake ejects or the feeder reverses.
     * Backing out wins, because clearing a jam is always allowed.
     */
    public static State requestFor(IntakeSubsystem.State intake, FeederSubsystem.State feeder) {
        return requestFor(intake, feeder, false);
    }

    /**
     * As {@link #requestFor(IntakeSubsystem.State, FeederSubsystem.State)}, and with {@code stopForFull}
     * (the lane is full and {@link #stopWhenFull} is on) taking in no longer runs the prefeed: only a
     * feed carries pieces toward the gate then. Backing out still wins, and a feed still pushes.
     */
    public static State requestFor(IntakeSubsystem.State intake, FeederSubsystem.State feeder, boolean stopForFull) {
        if (intake == IntakeSubsystem.State.OUT || feeder == FeederSubsystem.State.REVERSE) {
            return State.OUT;
        }
        if (feeder == FeederSubsystem.State.FEED) {
            return State.IN;
        }
        if (intake == IntakeSubsystem.State.IN && !stopForFull) {
            return State.IN;
        }
        return State.IDLE;
    }

    /**
     * Whether a feed that started {@code secondsSinceFeed} ago is still in its gate-clearing time:
     * {@code clearSeconds} more than 0 and not yet passed. NaN counts as off. Pure, so a test can check it.
     */
    static boolean clearingGate(double secondsSinceFeed, double clearSeconds) {
        return clearSeconds > 0.0 && secondsSinceFeed < clearSeconds;
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
