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

    private final NormalizedColorSensor sensor;
    /** The same device, as its proximity side: the V3 is both. */
    private final DistanceSensor proximity;

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

    /** @param hardwareMap the sensor is {@link DeviceNames#COLOR_SENSOR} */
    public ColorSensorSubsystem(HardwareMap hardwareMap) {
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
