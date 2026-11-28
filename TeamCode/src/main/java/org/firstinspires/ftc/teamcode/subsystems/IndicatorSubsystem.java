package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.hardware.DeviceNames;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.Throttle;

/**
 * The indicator lights: goBILDA RGB Indicator Lights on one servo port (more than one light on a
 * Y cable, so one device, all the same colour), whose colour is the servo position.
 *
 * <p><b>The rule</b> (mentor, 2 Nov 2026): the light shows only what the drive team acts on. Anything
 * else (a routine driving, the launcher at speed, a piece or two in the lane) goes to the match log,
 * not the light.
 *
 * <table>
 *   <caption>What it shows, the first that holds</caption>
 *   <tr><th>When</th><th>Light</th><th>The drive team</th></tr>
 *   <tr><td>An Autonomous's INIT, with any warning ({@code InitWarnings})</td><td>white, flashing</td>
 *       <td>reads the Driver Station, which says what is wrong and what to do</td></tr>
 *   <tr><td>INIT otherwise</td><td>the alliance's colour, steady; off with none</td>
 *       <td>checks it against the field</td></tr>
 *   <tr><td>After PLAY, a fire held with the shot behind the turret's firing arc</td><td>orange,
 *       flashing</td><td>turns the robot (19429 does not shoot there; a robot with the arc not enforced
 *       shoots into itself)</td></tr>
 *   <tr><td>After PLAY, Circle's arc turn has just started ({@code ArcTurn})</td><td>orange, one solid
 *       pulse of {@link #ARC_TURN_PULSE_MS}</td><td>lets the robot turn itself</td></tr>
 *   <tr><td>After PLAY, an Auto calls for the human player's NECTAR ({@code CallNectar}, as the robot
 *       starts turning its intake to the LOADING ZONE, until its {@code CatchNectar} hold ends)</td><td>the
 *       alliance's colour, flashing at 2 Hz, then 4 Hz from {@link #NECTAR_FAST_AFTER_MS} into the hold</td><td>the
 *       human player rolls the NECTAR now; faster means the robot is about to give up and leave (mentor,
 *       28 Nov 2026)</td></tr>
 *   <tr><td>After PLAY, the lane is full (the colour sensor's {@code LaneFull})</td><td>green</td>
 *       <td>stops intaking</td></tr>
 *   <tr><td>After PLAY, the shot is behind the firing arc ({@code ArcLight})</td><td>orange</td>
 *       <td>turns the robot before pressing Circle (mentor, 20 Nov 2026)</td></tr>
 *   <tr><td>After PLAY otherwise</td><td>off</td><td>nothing, until the team decides otherwise</td></tr>
 * </table>
 *
 * <p>{@code RobotOpMode} decides what to show each loop ({@link #choose}) and hands it over with
 * {@link #show}; {@link #update()} writes the light only when its colour changes (and once a
 * second), so it costs no hub round trip in a steady loop.
 */
@Configurable
public class IndicatorSubsystem implements Subsystem {

    // Servo positions for each colour, from goBILDA's chart for the light (3118-0808-0002):
    // checked against the light itself at the next session (doc/robot-session-4.md).
    public static double OFF = 0.0;
    public static double RED = 0.279;
    /** From the same goBILDA chart as the rest; not yet checked against the light. */
    public static double GREEN = 0.5;
    /** From the same goBILDA chart (orange, 0.333); not yet checked against the light. */
    public static double ORANGE = 0.333;
    public static double BLUE = 0.611;
    public static double WHITE = 1.0;
    /** A flashing signal is on this long, then off as long, ms. 250 is 2 Hz, well under the 5 Hz the manual frowns on. */
    public static double FLASH_MS = 250;

    /** What the light says. */
    public enum Signal {
        OFF, CHECK_DRIVER_STATION, FULL, ALLIANCE_RED, ALLIANCE_BLUE,
        /** The shot is behind the turret's firing arc: orange. */
        OUTSIDE_ARC,
        /** A fire held with the shot behind the firing arc: orange, flashing. */
        FIRING_OUTSIDE_ARC,
        /** Circle's arc turn has just started ({@code ArcTurn}): solid orange for {@link #ARC_TURN_PULSE_MS}. */
        ARC_TURN,
        /** An Auto waits for the human player's NECTAR ({@code CatchNectar}): red, flashing, faster near the end. */
        NECTAR_CUE_RED,
        /** The same on blue. */
        NECTAR_CUE_BLUE
    }

    /**
     * How long into the NECTAR hold ({@code CatchNectar}) the light flashes faster, ms: the hold
     * lasts at most about 2.5 s (the RightUnderHive NECTAR hold), so from 1.5 s in the human player
     * sees the robot is about to leave. A starting point, <b>not seen on the robot</b>.
     */
    public static double NECTAR_FAST_AFTER_MS = 1500;
    /**
     * The NECTAR cue goes out by itself this long after it came on, ms, in case the Auto skipped the
     * hold that would have ended it (a late TIP: the turn and the volley, then no hold).
     */
    public static double NECTAR_CUE_MAX_MS = 8000;
    /** The faster flash's half period, ms: 125 is 4 Hz, still under the 5 Hz the manual frowns on. */
    public static double NECTAR_FAST_FLASH_MS = 125;

    /** When the NECTAR cue came on ({@link #nectarCue}), ms on the nanoTime clock; negative: off. */
    private long nectarCueSinceMs = -1;
    /** When the NECTAR hold began ({@link #nectarHold}), the same clock; negative: not holding yet. */
    private long nectarHoldSinceMs = -1;

    /**
     * How long the arc turn's orange pulse lasts, ms: one solid orange as Circle starts turning the
     * robot to bring the CELL into the firing arc, so the driver knows the robot is turning itself
     * (mentor, 20 Nov 2026). Then the light goes back to what it would show (green if full, else off).
     * 400 is a starting point, <b>not seen on the robot</b>. 0 or less: no pulse.
     */
    public static double ARC_TURN_PULSE_MS = 400;

    private final Servo light;
    private Signal signal = Signal.OFF;
    private double written = Double.NaN;
    private final Throttle resend = new Throttle(1000, 0);

    /** Looks the light up once. Configured on every competition robot, so a missing one fails init. */
    public IndicatorSubsystem(HardwareMap hardwareMap) {
        light = hardwareMap.get(Servo.class, DeviceNames.INDICATOR);
    }

    /**
     * The signal for the robot's state, the first that holds (see the class comment). Pure, so a
     * test can check it.
     *
     * @param init     before PLAY
     * @param warnings an Autonomous's INIT has something for the drive team to fix; {@code RobotOpMode}
     *                 passes false in any other OpMode
     * @param full     the lane is full
     * @param alliance shown in INIT only
     */
    public static Signal choose(boolean init, boolean warnings, boolean full, Alliance alliance) {
        return choose(init, warnings, full, alliance, false, false);
    }

    /**
     * The same, with the firing arc ({@code ArcLight}) after PLAY: a fire held outside it outranks a
     * full lane, which outranks the shot merely being outside it.
     *
     * @param outsideArc  the shot is behind the turret's firing arc
     * @param firingThere a fire is held while it is
     */
    public static Signal choose(boolean init, boolean warnings, boolean full, Alliance alliance,
                                boolean outsideArc, boolean firingThere) {
        return choose(init, warnings, full, alliance, outsideArc, firingThere, false);
    }

    /**
     * The same, with the arc turn's pulse after PLAY: below a fire held outside the arc, above a full
     * lane (the pulse is short, and the green comes back after it).
     *
     * @param arcTurnPulse Circle's arc turn has just started ({@link Signal#ARC_TURN})
     */
    public static Signal choose(boolean init, boolean warnings, boolean full, Alliance alliance,
                                boolean outsideArc, boolean firingThere, boolean arcTurnPulse) {
        if (init && warnings) return Signal.CHECK_DRIVER_STATION;
        if (!init) {
            if (outsideArc && firingThere) return Signal.FIRING_OUTSIDE_ARC;
            if (arcTurnPulse) return Signal.ARC_TURN;
            if (full) return Signal.FULL;
            return outsideArc ? Signal.OUTSIDE_ARC : Signal.OFF;
        }
        if (alliance == Alliance.RED) return Signal.ALLIANCE_RED;
        if (alliance == Alliance.BLUE) return Signal.ALLIANCE_BLUE;
        return Signal.OFF;
    }

    /**
     * The NECTAR cue: on from {@code CallNectar} (the robot starts turning its intake to the LOADING
     * ZONE), or from the hold if nothing called it; off as the hold ends, or after
     * {@link #NECTAR_CUE_MAX_MS}. {@code RobotOpMode} reads {@link #nectarCueOn()} into {@link #choose}.
     */
    public void nectarCue(boolean on) {
        if (!on) {
            nectarCueSinceMs = -1;
            nectarHoldSinceMs = -1;
        } else if (nectarCueSinceMs < 0) {
            nectarCueSinceMs = System.nanoTime() / 1_000_000L;
        }
    }

    /** {@code CatchNectar} has begun: the cue on, and its faster flash counted from now. */
    public void nectarHold() {
        nectarCue(true);
        if (nectarHoldSinceMs < 0) nectarHoldSinceMs = System.nanoTime() / 1_000_000L;
    }

    public boolean nectarCueOn() {
        if (nectarCueSinceMs < 0) return false;
        if (System.nanoTime() / 1_000_000L - nectarCueSinceMs < NECTAR_CUE_MAX_MS) return true;
        nectarCue(false);
        return false;
    }

    /**
     * The same, with the NECTAR cue after PLAY: below the firing arc's flash and pulse (the TeleOp's),
     * above a full lane (the robot waiting for the NECTAR has fired its load).
     *
     * @param nectarCue an Auto waits for the human player's NECTAR ({@link #nectarCue})
     */
    public static Signal choose(boolean init, boolean warnings, boolean full, Alliance alliance,
                                boolean outsideArc, boolean firingThere, boolean arcTurnPulse,
                                boolean nectarCue) {
        if (!init && nectarCue && !(outsideArc && firingThere) && !arcTurnPulse) {
            if (alliance == Alliance.RED) return Signal.NECTAR_CUE_RED;
            if (alliance == Alliance.BLUE) return Signal.NECTAR_CUE_BLUE;
        }
        return choose(init, warnings, full, alliance, outsideArc, firingThere, arcTurnPulse);
    }

    /** What the light shows from the next {@link #update()}. */
    public void show(Signal signal) {
        this.signal = signal == null ? Signal.OFF : signal;
    }

    public Signal signal() {
        return signal;
    }

    /** The servo position for {@code signal} at {@code nowMs}: a flashing one is off half the time. */
    static double position(Signal signal, long nowMs) {
        return position(signal, nowMs, 0);
    }

    /**
     * The same, {@code cueMs} into the NECTAR hold: its flash is {@link #FLASH_MS} until
     * {@link #NECTAR_FAST_AFTER_MS}, then {@link #NECTAR_FAST_FLASH_MS}.
     */
    static double position(Signal signal, long nowMs, long cueMs) {
        switch (signal) {
            case NECTAR_CUE_RED:
            case NECTAR_CUE_BLUE: {
                double half = cueMs >= NECTAR_FAST_AFTER_MS ? NECTAR_FAST_FLASH_MS : FLASH_MS;
                boolean on = (nowMs / (long) Math.max(50.0, half)) % 2 == 0;
                return on ? (signal == Signal.NECTAR_CUE_RED ? RED : BLUE) : OFF;
            }
            case CHECK_DRIVER_STATION: return flashOn(nowMs) ? WHITE : OFF;
            case FULL: return GREEN;
            case OUTSIDE_ARC: return ORANGE;
            case ARC_TURN: return ORANGE;
            case FIRING_OUTSIDE_ARC: return flashOn(nowMs) ? ORANGE : OFF;
            case ALLIANCE_RED: return RED;
            case ALLIANCE_BLUE: return BLUE;
            default: return OFF;
        }
    }

    private static boolean flashOn(long nowMs) {
        long half = (long) Math.max(50.0, FLASH_MS);
        return (nowMs / half) % 2 == 0;
    }

    @Override
    public void initialize() {
        signal = Signal.OFF;
        nectarCueSinceMs = -1;
        nectarHoldSinceMs = -1;
        written = Double.NaN;
    }

    /** Nothing to do: the light moves nothing, so it lights during INIT too. */
    @Override
    public void start() {
    }

    /** A write only when the colour changes, and once a second to be sure the light has it. */
    @Override
    public void update() {
        long nowNs = System.nanoTime();
        long nowMs = nowNs / 1_000_000L;
        long cueMs = nectarHoldSinceMs < 0 ? 0 : nowMs - nectarHoldSinceMs;
        double position = Math.max(0.0, Math.min(1.0, position(signal, nowMs, cueMs)));
        if (position != written || resend.due(nowNs)) {
            light.setPosition(position);
            written = position;
        }
    }

    /** Off, at the end. */
    @Override
    public void stop() {
        light.setPosition(OFF);
    }

    @Override
    public String health() {
        return "lights configured (Y-cabled, one device); presence unknown (a servo port reports nothing back)";
    }
}
