package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.hardware.DeviceNames;

import java.util.Locale;

/**
 * The REV Color Sensor V3 in the transfer: what colour is in front of it, and how close. Its
 * numbers go to the match log ({@code /ColorSensor/*}) and the Robot page. One thing is decided from
 * it: whether the lane is full (below); its colours are data only.
 *
 * <p>It is an I2C device, so every read is a round trip to the hub that the bulk cache does not
 * cover (the colour block, then the proximity). It reads every {@link #readEveryMs}, not every loop,
 * the way {@code RobotOpMode} reads the battery; {@code /Loop/Subsystem/ColorSensorSubsystemMs}
 * shows what a read costs. It moves nothing, so it reads in INIT too: the preloads are in front of
 * it before PLAY.
 *
 * <p>It says whether the lane is <b>full</b> ({@link #full()}, {@link LaneFull}): a piece held under
 * it for {@link #fullAfterMs} while the intake takes in. The indicator's green, the intake's roller
 * stop and the Autos' {@code IntakeFull} all read that one signal.
 *
 * <p>It also keeps {@link LaneTracker}: what it thinks is in the lane, slot by slot, from pieces
 * arriving under it, shots, and ejects. Data only, logged as {@code /Lane/*}: a count drifts on any
 * missed event, so nothing decides anything from it (mentor, 2 Nov 2026). Its thresholds
 * ({@link #presentAbove}, {@link #leaveBelow}, the hue ranges) come from one bench reading of each
 * piece on robot 20245, 2 Nov 2026, with the sensor at slot 3; logs will refine them, and moving the
 * sensor (to slot 4, the entrance) means measuring them again.
 *
 * <p>The colour is the SDK's normalised reading at the sensor's default gain, unchanged, plus its
 * hue and saturation. Hue and saturation do not depend on the gain or on how far the ball is; the
 * raw numbers and the distance do. No threshold or classification here until a log says where one
 * belongs.
 */
@Configurable
public class ColorSensorSubsystem implements Subsystem {

    /**
     * How often to read the sensor, ms. 50 (20 a second) sees a ball that passes in a tenth of a
     * second at least once and costs a fraction of reading every loop. 0 reads every loop.
     */
    public static int readEveryMs = 50;

    /**
     * A piece is under the sensor above this brightness (the clear channel, {@link #alpha()}). NaN
     * turns arrivals off. Brightness, not distance: the proximity reading is IR reflectance, and a
     * POLLEN reflects so little that it read 44 mm against an empty lane's 54 (NECTAR 18). Measured
     * on 20245, 2 Nov 2026, as mounted at the entrance: empty 0.014 to 0.020 (steady), POLLEN at
     * rest 0.155 to 0.45, NECTAR 0.29 to 0.97; a piece pushed right up to it reads up to 0.99. 0.10
     * keeps clear of the dimmest POLLEN; replaying that session, 0.15 split one hold into two.
     */
    public static double presentAbove = 0.10;
    /** And has gone below this brightness (hysteresis). NaN: the same as {@link #presentAbove}. */
    public static double leaveBelow = 0.05;
    /**
     * POLLEN's hue range, degrees (min above max wraps through 0). Bench, 2 Nov 2026: 73° to 94° at
     * rest, saturation about 0.7 (it falls to 0.35 when pressed against the sensor; the hue holds).
     * The low end sits midway to red NECTAR's highest reading.
     */
    public static double pollenHueMin = 48;
    public static double pollenHueMax = 130;
    /**
     * A piece held under the sensor this long while the intake takes in means the lane is full, ms
     * ({@link LaneFull}). Negative or NaN: never full.
     *
     * <p>From 20245's ten TeleOp logs of 1 Nov 2026 (sensor at slot 3, before the dead-zone fix),
     * replaying the brightness against the roller's speed: a piece passing with the roller running
     * was under the sensor for 0.30 to 0.98 s of intaking (12 pieces), not the bench's 0.1 to
     * 0.2 s; a piece that parked with the roller running passed 1.5 s of intaking 1.5 to 1.6 s
     * after it arrived (6 of 6). 1500 leaves half again the slowest passing piece and calls full
     * about 1.5 s after the lane fills. Measure it again once the sensor moves to slot 4 and the dead
     * zone is fixed: the match log has every piece's dwell ({@code /Lane/DwellMs}).
     */
    public static double fullAfterMs = 1500;

    /** Red NECTAR's hue range, degrees; it wraps through 0. Bench, 2 Nov 2026: 9° to 22°, saturation 0.76. */
    public static double redNectarHueMin = 330;
    public static double redNectarHueMax = 47;
    /** Blue NECTAR's hue range, degrees. Bench, 2 Nov 2026: 212° to 232°, saturation 0.62 to 0.88. */
    public static double blueNectarHueMin = 180;
    public static double blueNectarHueMax = 270;

    private final NormalizedColorSensor sensor;
    /** The same device, as its proximity side: the V3 is both. */
    private final DistanceSensor proximity;
    private final LauncherSubsystem launcher;
    private final IntakeSubsystem intake;
    private final PrefeedSubsystem prefeed;
    private final LaneTracker lane = new LaneTracker();
    private final LaneFull laneFull = new LaneFull();

    private long lastReadNs;
    private boolean everRead;
    private boolean fresh;
    private float red;
    private float green;
    private float blue;
    private float alpha;
    private double distanceMm = Double.NaN;
    private double hueDeg = Double.NaN;
    private double saturation = Double.NaN;
    private String atInit;

    /**
     * @param hardwareMap the sensor is {@link DeviceNames#COLOR_SENSOR}
     * @param launcher    read for its shots; updated before this subsystem
     * @param intake      read for running outward; updated before this subsystem
     * @param prefeed     read for running outward; updated before this subsystem
     */
    public ColorSensorSubsystem(HardwareMap hardwareMap, LauncherSubsystem launcher,
                                IntakeSubsystem intake, PrefeedSubsystem prefeed) {
        this.launcher = launcher;
        this.intake = intake;
        this.prefeed = prefeed;
        sensor = hardwareMap.get(NormalizedColorSensor.class, DeviceNames.COLOR_SENSOR);
        proximity = hardwareMap.get(DistanceSensor.class, DeviceNames.COLOR_SENSOR);
    }

    /** One read, so {@link #health()} can say what the sensor answered. Not in the loop. */
    @Override
    public void initialize() {
        read(System.nanoTime());
        atInit = allZero()
                ? "reads all zero: unplugged, or not on the bus robot_<name>.xml gives it"
                : String.format(Locale.US, "answering: %s, gain %.1f", summary(), sensor.getGain());
    }

    @Override
    public void start() {
    }

    @Override
    public void update() {
        long now = System.nanoTime();
        fresh = !everRead || now - lastReadNs >= Math.max(0, readEveryMs) * 1_000_000L;
        if (fresh) {
            read(now);
            laneFull.step(now / 1_000_000L, alpha, intake.requested() == IntakeSubsystem.State.IN,
                    presentAbove, leaveBelow, fullAfterMs);
        }
        // The lane runs outward when the stars do, whatever the roller does.
        boolean outward = intake.starsState() == IntakeSubsystem.State.OUT
                || prefeed.state() == PrefeedSubsystem.State.OUT;
        lane.step(fresh, alpha,
                LaneTracker.classify(hueDeg, pollenHueMin, pollenHueMax,
                        redNectarHueMin, redNectarHueMax, blueNectarHueMin, blueNectarHueMax),
                outward, launcher.shotJustDetected(), presentAbove, leaveBelow);
    }

    /** The lane is full: a piece held under the sensor ({@link LaneFull}). As of the last reading. */
    public boolean full() {
        return laneFull.full();
    }

    /** The full detector itself, for the match log's dwell of each piece. */
    public LaneFull laneFull() {
        return laneFull;
    }

    /** What it thinks is in the lane. Data only: never decide anything from it. */
    public LaneTracker lane() {
        return lane;
    }

    private void read(long now) {
        NormalizedRGBA c = sensor.getNormalizedColors();
        red = c.red;
        green = c.green;
        blue = c.blue;
        alpha = c.alpha;
        distanceMm = proximity.getDistance(DistanceUnit.MM);
        hueDeg = hueDegrees(red, green, blue);
        saturation = saturation(red, green, blue);
        lastReadNs = now;
        everRead = true;
    }

    @Override
    public void stop() {
    }

    /** Whether the latest {@link #update()} read the sensor; the log writes only fresh readings. */
    public boolean fresh() {
        return fresh;
    }

    public float red() {
        return red;
    }

    public float green() {
        return green;
    }

    public float blue() {
        return blue;
    }

    /** The clear channel: total light, colour or not. */
    public float alpha() {
        return alpha;
    }

    /** Distance to what is in front of it, mm, from the proximity sensor. Only meaningful within a few cm. */
    public double distanceMm() {
        return distanceMm;
    }

    /** Hue, 0 to 360°: 0 red, 120 green, 240 blue. NaN for no colour at all (grey or dark). */
    public double hueDegrees() {
        return hueDeg;
    }

    /** Saturation, 0 (grey) to 1 (pure colour). */
    public double saturation() {
        return saturation;
    }

    @Override
    public String health() {
        return atInit;
    }

    @Override
    public void describe(Display display) {
        display.status("Color sensor", allZero() ? Display.Level.WARN : Display.Level.OK,
                allZero() ? "reads all zero: unplugged?" : summary());
        display.status("Lane", Display.Level.OK,
                laneFull.full() ? "FULL (a piece held under the sensor)"
                        : laneFull.present() ? String.format(Locale.US, "piece under the sensor, %.0f ms",
                                laneFull.heldWhileIntakingMs())
                        : "room");
        display.status("Lane (count, log only)", Double.isNaN(presentAbove) ? Display.Level.WARN : Display.Level.OK,
                lane.lettersFrontToBack() + " front to back, " + lane.count() + " in"
                        + (Double.isNaN(presentAbove) ? "; presentAbove not set: counts shots only" : ""));
    }

    private boolean allZero() {
        return red == 0f && green == 0f && blue == 0f && alpha == 0f;
    }

    private String summary() {
        return String.format(Locale.US, "R %.3f G %.3f B %.3f A %.3f, hue %.0f°, sat %.2f, %.0f mm",
                red, green, blue, alpha, hueDeg, saturation, distanceMm);
    }

    /**
     * The hue of an RGB colour, 0 to 360°, the standard HSV formula. Pure, so a test can check it.
     * NaN when the three are equal: grey has no hue.
     */
    static double hueDegrees(double r, double g, double b) {
        double max = Math.max(r, Math.max(g, b));
        double min = Math.min(r, Math.min(g, b));
        double span = max - min;
        if (!(span > 0)) return Double.NaN;
        double h;
        if (max == r) {
            h = (g - b) / span;
        } else if (max == g) {
            h = 2.0 + (b - r) / span;
        } else {
            h = 4.0 + (r - g) / span;
        }
        h *= 60.0;
        return h < 0 ? h + 360.0 : h;
    }

    /** HSV saturation, 0 to 1. 0 for black. Pure, so a test can check it. */
    static double saturation(double r, double g, double b) {
        double max = Math.max(r, Math.max(g, b));
        double min = Math.min(r, Math.min(g, b));
        return max > 0 ? (max - min) / max : 0.0;
    }
}
