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
 *   <tr><td>After PLAY, the lane is full (the colour sensor's {@code LaneFull})</td><td>green</td>
 *       <td>stops intaking</td></tr>
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
    public static double BLUE = 0.611;
    public static double WHITE = 1.0;
    /** A flashing signal is on this long, then off as long, ms. 250 is 2 Hz, well under the 5 Hz the manual frowns on. */
    public static double FLASH_MS = 250;

    /** What the light says. */
    public enum Signal {
        OFF, CHECK_DRIVER_STATION, FULL, ALLIANCE_RED, ALLIANCE_BLUE
    }

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
        if (init && warnings) return Signal.CHECK_DRIVER_STATION;
        if (!init) return full ? Signal.FULL : Signal.OFF;
        if (alliance == Alliance.RED) return Signal.ALLIANCE_RED;
        if (alliance == Alliance.BLUE) return Signal.ALLIANCE_BLUE;
        return Signal.OFF;
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
        switch (signal) {
            case CHECK_DRIVER_STATION: return flashOn(nowMs) ? WHITE : OFF;
            case FULL: return GREEN;
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
        double position = Math.max(0.0, Math.min(1.0, position(signal, nowNs / 1_000_000L)));
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
