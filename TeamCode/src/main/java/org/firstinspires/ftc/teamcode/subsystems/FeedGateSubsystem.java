package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.hardware.DeviceNames;

import java.util.Locale;

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
 * <p><b>The two positions are NaN until measured on a robot</b> ({@link #blockingPosition},
 * {@link #feedingPosition}), never guessed: a wrong guess drives the plate into something. While
 * either is NaN the gate is not written at all and the Robot page says so. Set them in Panels with
 * the servo moving, until the plate sits on each stop, then write the numbers back here with a
 * comment saying which servo and robot they were measured on. Panels edits are lost on restart.
 *
 * <p>The servo is written only when its position changes, not every loop: each write is its own
 * round trip to the hub, and a held servo needs no repeat.
 *
 * <p>Nothing is written before PLAY, so the gate does not move at INIT (G304.H, G403; see
 * {@link Subsystem}), and it goes to BLOCKING at PLAY. If the gate has to block before PLAY, to
 * keep pieces in at the start, that is a mechanical stop or latch (R103.A), not this servo: the
 * TeleOp's INIT runs between AUTO and TELEOP too, where a plate swinging in is a G403 foul.
 *
 * <p>The four pre-load POLLEN are placed behind the plate by hand during field setup (the mentor's
 * decision, 1 Nov 2026: pre-loads are the field's and are handed over there), with the servo
 * unwritten and the plate limp; nothing runs at INIT to take them in or to hold them. So the plate
 * has to stay closed on its own with four balls resting against it until PLAY drives it. If it does
 * not, that is a detent on the plate, never a write before PLAY.
 */
@Configurable
public class FeedGateSubsystem implements Subsystem {

    /** Where the gate plate is. */
    public enum State { BLOCKING, FEEDING }

    /**
     * Servo position with the plate blocking, 0 to 1. <b>NaN until measured:</b> the gate is not
     * written while this or {@link #feedingPosition} is NaN.
     */
    public static double blockingPosition = Double.NaN;

    /** Servo position with the plate open to the feeder, 0 to 1. <b>NaN until measured</b>, with {@link #blockingPosition}. */
    public static double feedingPosition = Double.NaN;

    private final Servo servo;
    private final FeederSubsystem feeder;
    private State state = State.BLOCKING;
    private State hold;
    private double lastWritten = Double.NaN;
    private boolean started;

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

    /** Start BLOCKING and unwritten: the servo is not touched until PLAY. */
    @Override
    public void initialize() {
        state = State.BLOCKING;
        hold = null;
        lastWritten = Double.NaN;
        started = false;
    }

    /** PLAY: the servo may be written from now on, and the gate goes to its request. */
    @Override
    public void start() {
        started = true;
    }

    /**
     * Decide the state from the feeder (or the bench's hold), then, from PLAY and with both positions
     * set, write the servo if the position changed. Nothing is written before PLAY.
     */
    @Override
    public void update() {
        state = hold != null ? hold : requestFor(feeder.state());
        if (!started) {
            return;
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
                        + (hold != null ? " · held by the bench" : ""));
    }

    /**
     * The policy, as one pure function: open while the feeder feeds (a piece goes through) or
     * reverses (a jam clears backward), BLOCKING otherwise.
     */
    public static State requestFor(FeederSubsystem.State feeder) {
        return feeder == FeederSubsystem.State.IDLE ? State.BLOCKING : State.FEEDING;
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
