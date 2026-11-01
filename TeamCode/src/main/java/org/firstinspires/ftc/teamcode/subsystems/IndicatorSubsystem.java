package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.hardware.DeviceNames;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.Throttle;

/**
 * The indicator lights: goBILDA RGB Indicator Lights on one servo port (more than one light on a
 * Y cable, so one device, all the same colour), whose colour is the servo position. It says, from across the field, the one thing the drive team most needs to know now;
 * the Driver Station's page says the rest.
 *
 * <p>{@code RobotOpMode} decides what to show each loop ({@link #choose}) and hands it over with
 * {@link #show}; {@link #update()} writes the light only when its colour changes (and once a
 * second), so it costs no hub round trip in a steady loop.
 *
 * <table>
 *   <caption>What it shows, the first that holds</caption>
 *   <tr><th>When</th><th>Light</th></tr>
 *   <tr><td>INIT, battery below {@code RobotOpMode.LOW_BATTERY_VOLTS}</td><td>red, flashing</td></tr>
 *   <tr><td>The camera is missing or not answering</td><td>orange</td></tr>
 *   <tr><td>INIT, the camera has sent no frame yet: wait before PLAY</td><td>yellow, flashing</td></tr>
 *   <tr><td>A FLOWER pick or GARDEN gather is driving</td><td>violet</td></tr>
 *   <tr><td>The pose is not on the field (no handoff, no camera fix)</td><td>yellow</td></tr>
 *   <tr><td>All good</td><td>the alliance's colour; white with none</td></tr>
 * </table>
 */
@Configurable
public class IndicatorSubsystem implements Subsystem {

    // Servo positions for each colour, from goBILDA's chart for the light (3118-0808-0002):
    // checked against the light itself at the next session (doc/robot-session-4.md).
    public static double OFF = 0.0;
    public static double RED = 0.279;
    public static double ORANGE = 0.333;
    public static double YELLOW = 0.388;
    public static double BLUE = 0.611;
    public static double VIOLET = 0.722;
    public static double WHITE = 1.0;
    /** A flashing signal is on this long, then off as long, ms. */
    public static double FLASH_MS = 250;

    /** What the light says. */
    public enum Signal {
        OFF, BATTERY_LOW, CAMERA_FAULT, CAMERA_NO_FRAMES, BUSY, POSE_UNREFERENCED,
        ALLIANCE_RED, ALLIANCE_BLUE, NO_ALLIANCE
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
     * The signal for the robot's state, the first that holds (see the class comment).
     *
     * @param init           before PLAY
     * @param batteryLow     the battery read low at INIT
     * @param cameraFault    the camera is missing or not answering
     * @param cameraNoFrames the camera answers but has sent no frame yet
     * @param busy           a pick or a gather is driving the robot
     * @param poseOnField    the pose is referenced to the field
     */
    public static Signal choose(boolean init, boolean batteryLow, boolean cameraFault, boolean cameraNoFrames,
                                boolean busy, boolean poseOnField, Alliance alliance) {
        if (init && batteryLow) return Signal.BATTERY_LOW;
        if (cameraFault) return Signal.CAMERA_FAULT;
        if (init && cameraNoFrames) return Signal.CAMERA_NO_FRAMES;
        if (busy) return Signal.BUSY;
        if (!poseOnField) return Signal.POSE_UNREFERENCED;
        if (alliance == Alliance.RED) return Signal.ALLIANCE_RED;
        if (alliance == Alliance.BLUE) return Signal.ALLIANCE_BLUE;
        return Signal.NO_ALLIANCE;
    }

    /** What the light shows from the next {@link #update()}. */
    public void show(Signal signal) {
        this.signal = signal == null ? Signal.OFF : signal;
    }

    public Signal signal() {
        return signal;
    }

    /** The servo position for {@code signal} at {@code nowMs}: flashing ones are off half the time. */
    static double position(Signal signal, long nowMs) {
        switch (signal) {
            case BATTERY_LOW: return flashOn(nowMs) ? RED : OFF;
            case CAMERA_FAULT: return ORANGE;
            case CAMERA_NO_FRAMES: return flashOn(nowMs) ? YELLOW : OFF;
            case BUSY: return VIOLET;
            case POSE_UNREFERENCED: return YELLOW;
            case ALLIANCE_RED: return RED;
            case ALLIANCE_BLUE: return BLUE;
            case NO_ALLIANCE: return WHITE;
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
