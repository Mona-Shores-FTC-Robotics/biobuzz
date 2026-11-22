package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.hardware.DeviceNames;

import java.util.Locale;
import java.util.function.BooleanSupplier;

/**
 * The feed gate: a plate on a goBILDA Speed servo that turns between two positions. BLOCKING holds
 * pieces back so the robot cannot carry more than four; FEEDING lets them through to the feeder.
 *
 * <p>The gate is BLOCKING unless the feeder is asked to move a piece: it follows the feeder's
 * request every {@link #update()} ({@link #requestFor}), so the TeleOp, every Autonomous and the
 * bench get the same behaviour. It is updated after the feeder, so it follows this loop's request.
 * Reversing opens it too, so a jam can clear backward. {@link #hold} replaces that for a bench;
 * {@link #follow} gives it back.
 *
 * <p>It stays shut to a feed while a wrong NECTAR may be in the lane ({@link #shutWhile}, from
 * {@code ColorSensorSubsystem.wrongNectarMayBeIn()}): launching the other alliance's NECTAR is a
 * penalty, and in Fruitport Q-11 20245 launched one the reject bursts had not cleared. TeleOp and
 * every Auto alike, since every feed opens this gate; a reverse still opens it, so an unjam works.
 *
 * <p><b>The two positions are measured on a robot, never guessed</b> ({@link #blockingPosition},
 * {@link #feedingPosition}): a wrong guess drives the plate into something. While
 * either is NaN the gate is not written at all and the Robot page says so. Set them in Panels with
 * the servo moving, until the plate sits on each stop, then write the numbers back here with a
 * comment saying which servo and robot they were measured on. Panels edits are lost on restart.
 *
 * <p>The servo is written only when its position changes, not every loop: each write is its own
 * round trip to the hub, and a held servo needs no repeat.
 *
 * <p>In a TeleOp's INIT nothing is written, so the gate does not move (G403; see {@link Subsystem}):
 * that INIT runs between AUTO and TELEOP, where a plate swinging in is a foul. An Autonomous's INIT
 * closes it and holds it BLOCKING until PLAY ({@link #holdAtInit()}, R103.B), so the four pre-load
 * POLLEN, placed behind the plate by hand during field setup (the mentor's decision, 1 Nov 2026),
 * stay put; nothing runs at INIT to take them in. It is BLOCKING at PLAY either way.
 */
@Configurable
public class FeedGateSubsystem implements Subsystem {

    /** Where the gate plate is. */
    public enum State { BLOCKING, FEEDING }

    /**
     * Servo position with the plate blocking, 0 to 1. Measured on robot 20245 on 1 Nov 2026 in Launch
     * Test's setup layer, after the builders flipped the gate servo: the plate sits closed on its stop
     * at 0.82. (Before the flip it was 1.0 blocking, 0.775 feeding.) 19429's gate has not been
     * measured; it uses these until it is. The gate is not written while this or
     * {@link #feedingPosition} is NaN.
     */
    public static double blockingPosition = 0.82;

    /**
     * Servo position with the plate open to the feeder, 0 to 1. Measured on robot 20245 on 1 Nov 2026,
     * with {@link #blockingPosition}, after the gate servo was flipped: fully open at 1.0, the servo's
     * end of range.
     */
    public static double feedingPosition = 1.0;

    private final Servo servo;
    private final FeederSubsystem feeder;
    private State state = State.BLOCKING;
    private State hold;
    private double lastWritten = Double.NaN;
    private boolean started;
    /** An Autonomous's INIT: hold the gate closed before PLAY ({@link #holdAtInit()}). */
    private boolean holdAtInit;
    /** Whether a feed must not open the gate; never until {@link #shutWhile} is called. */
    private BooleanSupplier shut = () -> false;
    private boolean shutToFeed;

    /**
     * Looks the servo up once. A missing servo fails init with the SDK's own message.
     *
     * @param feeder read for its request; updated before this subsystem
     */
    public FeedGateSubsystem(HardwareMap hardwareMap, FeederSubsystem feeder) {
        servo = hardwareMap.get(Servo.class, DeviceNames.FEED_GATE);
        this.feeder = feeder;
    }

    /** Hold the gate in {@code state} whatever the feeder asks, for a bench. */
    public void hold(State state) {
        hold = state;
    }

    /**
     * Keep the gate shut to a feed while {@code shut} says so: a wrong NECTAR may be in the lane.
     * Wired once by {@code Robot}. The colour sensor updates after this, so it is last loop's answer.
     */
    public void shutWhile(BooleanSupplier shut) {
        this.shut = shut;
    }

    /** Whether the last {@link #update()} kept the gate shut to a feed ({@link #shutWhile}). */
    public boolean shutToFeed() {
        return shutToFeed;
    }

    /** Follow the feeder again ({@link #requestFor}). The default. */
    public void follow() {
        hold = null;
    }

    /** Whether a bench is holding the gate instead of the feeder. */
    public boolean held() {
        return hold != null;
    }

    /** Where the gate has been asked to be as of the last {@link #update()}. The request: nothing measures the plate. */
    public State state() {
        return state;
    }

    /** Whether both positions are set, so the gate can be written. */
    public boolean calibrated() {
        return !Double.isNaN(blockingPosition) && !Double.isNaN(feedingPosition);
    }

    /** The servo position last written, or NaN if none yet. For the match log. */
    public double position() {
        return lastWritten;
    }

    /** Start BLOCKING and unwritten: the servo is not touched until PLAY, or {@link #holdAtInit()}. */
    @Override
    public void initialize() {
        state = State.BLOCKING;
        hold = null;
        lastWritten = Double.NaN;
        started = false;
        holdAtInit = false;
    }

    /**
     * An Autonomous's INIT only: from the next {@link #update()} the gate is closed (BLOCKING) and
     * held there until PLAY, whatever the feeder asks (R103.B). Never from a TeleOp: its INIT runs
     * between AUTO and TELEOP, where any movement is a G403 foul. Nothing is written while the
     * positions are NaN. Called after {@link #initialize()}, which clears it.
     */
    public void holdAtInit() {
        holdAtInit = true;
    }

    /** PLAY: the servo may be written from now on, and the gate goes to its request. */
    @Override
    public void start() {
        started = true;
    }

    /**
     * Decide the state from the feeder (or the bench's hold), then, from PLAY and with both positions
     * set, write the servo if the position changed. Before PLAY nothing is written, unless an
     * Autonomous asked for the gate held closed ({@link #holdAtInit()}).
     */
    @Override
    public void update() {
        shutToFeed = hold == null && feeder.state() == FeederSubsystem.State.FEED && shut.getAsBoolean();
        state = hold != null ? hold : requestFor(feeder.state(), shutToFeed);
        if (!started) {
            if (!holdAtInit) {
                return;
            }
            state = State.BLOCKING;
        }
        double target = positionFor(state, blockingPosition, feedingPosition);
        if (!Double.isNaN(target) && target != lastWritten) {
            servo.setPosition(target);
            lastWritten = target;
        }
    }

    /** Nothing to cut: the SDK drops every servo's pulse when an OpMode ends, and the gate is left as it stands. */
    @Override
    public void stop() {
    }

    @Override
    public String health() {
        return "servo configured; presence unknown until it moves; positions "
                + (calibrated() ? "set" : "NOT SET (NaN until measured)");
    }

    @Override
    public void describe(Display display) {
        if (!calibrated()) {
            display.status("Feed gate", Display.Level.WARN,
                    "positions not set: the gate will not move (blockingPosition / feedingPosition in Panels)");
            return;
        }
        display.status("Feed gate", state == State.BLOCKING ? Display.Level.OK : Display.Level.WARN,
                (state == State.BLOCKING ? "BLOCKING" : "FEEDING")
                        + (Double.isNaN(lastWritten) ? "" : String.format(Locale.US, " (%.2f)", lastWritten))
                        + (hold != null ? " · held by the bench" : "")
                        + (shutToFeed ? " · shut to the feed: a wrong NECTAR may be in the lane" : ""));
    }

    /**
     * The policy, as one pure function: open while the feeder feeds (a piece goes through) or
     * reverses (a jam clears backward), BLOCKING otherwise.
     */
    public static State requestFor(FeederSubsystem.State feeder) {
        return requestFor(feeder, false);
    }

    /** {@link #requestFor(FeederSubsystem.State)}, but a feed with {@code shutToFeed} stays BLOCKING. Pure. */
    public static State requestFor(FeederSubsystem.State feeder, boolean shutToFeed) {
        if (feeder == FeederSubsystem.State.IDLE) return State.BLOCKING;
        if (feeder == FeederSubsystem.State.FEED && shutToFeed) return State.BLOCKING;
        return State.FEEDING;
    }

    /**
     * The servo position for a state, clamped to [0, 1], or NaN if that state's position (or the
     * other's) is not set: NaN means "do not write". Pure, so a test can check it.
     */
    static double positionFor(State state, double blocking, double feeding) {
        if (Double.isNaN(blocking) || Double.isNaN(feeding)) {
            return Double.NaN;
        }
        double position = state == State.BLOCKING ? blocking : feeding;
        return Math.max(0.0, Math.min(1.0, position));
    }
}
