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
 * The REV Color Sensor V3 in the transfer: what colour is in front of it, and how close. Data only,
 * for now. Nothing reads it to decide anything; its numbers go to the match log
 * ({@code /ColorSensor/*}) and the Robot page, so a few runs can show whether it tells POLLEN from
 * NECTAR, and a ball from no ball, before any code relies on it.
 *
 * <p>It is an I2C device, so every read is a round trip to the hub that the bulk cache does not
 * cover (the colour block, then the proximity). It reads every {@link #readEveryMs}, not every loop,
 * the way {@code RobotOpMode} reads the battery; {@code /Loop/Subsystem/ColorSensorSubsystemMs}
 * shows what a read costs. It moves nothing, so it reads in INIT too: the preloads are in front of
 * it before PLAY.
 *
 * <p>It also keeps {@link LaneTracker}: what it thinks is in the lane, slot by slot, from pieces
 * arriving under it, shots, and ejects. Also data only, logged as {@code /Lane/*}. Its thresholds
 * ({@link #presentAbove}, {@link #leaveBelow}, the hue ranges) come from one bench reading of each
 * piece on robot 20245, 2 Nov 2026; logs will refine them.
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
     * 37 to 117 keeps a wide gap to NECTAR.
     */
    public static double pollenHueMin = 37;
    public static double pollenHueMax = 117;
    /** NECTAR's hue range, degrees. Bench, 2 Nov 2026: 212° to 232°, saturation 0.62 to 0.88. */
    public static double nectarHueMin = 189;
    public static double nectarHueMax = 269;

    private final NormalizedColorSensor sensor;
    /** The same device, as its proximity side: the V3 is both. */
    private final DistanceSensor proximity;
    private final LauncherSubsystem launcher;
    private final IntakeSubsystem intake;
    private final PrefeedSubsystem prefeed;
    private final LaneTracker lane = new LaneTracker();

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
        }
        boolean outward = intake.state() == IntakeSubsystem.State.OUT
                || prefeed.state() == PrefeedSubsystem.State.OUT;
        lane.step(fresh, alpha,
                LaneTracker.classify(hueDeg, pollenHueMin, pollenHueMax, nectarHueMin, nectarHueMax),
                outward, launcher.shotJustDetected(), presentAbove, leaveBelow);
    }

    /** What it thinks is in the lane. Data only. */
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
        display.status("Lane (guess)", Double.isNaN(presentAbove) ? Display.Level.WARN : Display.Level.OK,
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
