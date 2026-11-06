package org.firstinspires.ftc.teamcode.opmodes.calibration;

import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.Utility;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.PwmControl;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.hardware.DeviceNames;
import org.firstinspires.ftc.teamcode.subsystems.TurretBurstControl;
import org.firstinspires.ftc.teamcode.subsystems.TurretEncoder;
import org.firstinspires.ftc.teamcode.util.LoopTimer;

import java.util.Locale;

/**
 * A 360° turret on a servo that has only full speed and off: an experiment, on the bench, before
 * any match code depends on it. For 19429's SWYFT Balance servo flashed to SWYFT's continuous mode
 * (SWYFT Servo Programmer V2: Continuous rotation <b>on</b>, so it reads Range 255/255). In that
 * mode a pulse far below 1500 µs spins it full speed one way, far above the other way, and a
 * disabled pulse stops it; there is no partial speed. The Cypher Max on the turret's drive gear
 * (2.75 turns per turret turn) counts every turn, so the turret may turn without limit, and
 * {@link TurretBurstControl} aims it with full-speed runs and short bursts.
 *
 * <p>The match code is not touched: Basic Drive and the Autos keep the position-mode turret.
 * Flash the servo back (continuous off, range 320) before running them.
 *
 * <h2>The checks, in order (someone on Stop)</h2>
 *
 * <ol>
 *   <li><b>INIT</b>: nothing is written. Turret on its mark, Cross: the encoder reads 0 there.</li>
 *   <li><b>Direction.</b> PLAY, push the left stick left and hold: the turret spins
 *       counter-clockwise seen from above, and {@code angle} rises. Clockwise → flip
 *       {@code ccwIsLowPulse}. Angle falls while it turns counter-clockwise → flip
 *       {@code direction}. Let go: it stops (pulse off).</li>
 *   <li><b>Coast.</b> Spin and let go a few times: {@code last coast} shows how far it ran on after
 *       the pulse was cut, and the {@code leadMs} that predicts it. Set {@code leadMs} to it.</li>
 *   <li><b>Aim.</b> Triangle holds the target: D-pad left/right ±{@code stepDeg}, up +180° (behind),
 *       down back to 0, bumpers ±5°. Watch {@code error}: it should settle inside
 *       {@code toleranceDeg} without hunting. Hunting → raise {@code leadMs} or {@code burstOffMs},
 *       or lower {@code burstOnMs}; slow to arrive → raise {@code burstBelowDeg} less, or
 *       {@code burstOnMs} more. If it is too fast to stop at all, lower Max Power in the SWYFT
 *       programmer (it caps the duty cycle) and measure the coast again.</li>
 *   <li><b>Push it</b> off target by hand while holding: it comes back.</li>
 * </ol>
 *
 * <p>Guards: it stops and says why if the turret turns the wrong way, if the encoder sees no motion
 * while spinning (servo not in continuous mode, or encoder unplugged), or after {@code maxSpinMs}
 * of continuous full speed. Circle stops everything and clears a fault.
 *
 * <p>Standalone, like {@code TurretBench}: no {@code Robot}, the exception CLAUDE.md allows for
 * rigs. It reads the feeder motor's encoder socket and never touches that motor's power or mode.
 */
@Utility(name = "Bench: Turret 360", description = "Experiment: 360° turret on a full-speed-only servo, steered by the encoder")
@Configurable
public class TurretBurstBench extends OpMode {

    // ------------------------------------------------------------ Panels tunables

    /** Encoder turns per turret turn: the Cypher Max on the 64T drive gear of the 2.75:1 turret (19429). */
    public static double turnsPerTurretTurn = 2.75;
    /** +1 if the count rises as the turret turns counter-clockwise; -1 on 19429 (6 Nov 2026). */
    public static int direction = -1;
    /** True if the low pulse spins the turret counter-clockwise. Check 2 says. */
    public static boolean ccwIsLowPulse = true;
    /** The spin pulses, µs: far from 1500 so the servo never reaches its target and spins. */
    public static double lowPulseUs = 500.0;
    public static double highPulseUs = 2500.0;

    public static double leadMs = 0.0;
    public static double burstBelowDeg = 20.0;
    public static double burstOnMs = 20.0;
    public static double burstOffMs = 80.0;
    public static double toleranceDeg = 2.0;
    public static boolean shortestPath = true;

    /** D-pad left/right target step, degrees. */
    public static double stepDeg = 45.0;
    /** Full speed for longer than this without arriving stops it, ms. */
    public static double maxSpinMs = 3000.0;

    static final double STICK = 0.5;
    /** Below this the turret counts as still, deg/s. */
    static final double STILL_DPS = 3.0;

    // ------------------------------------------------------------ hardware and state

    private DcMotorEx encoderHost;
    private Servo servo;
    private PwmControl pwm;
    private String fault;
    private String setupProblem;

    private TurretEncoder encoder;
    private double encoderTurnsSetting = Double.NaN;
    private int directionSetting;
    private long counts;
    private long zeroCounts;
    private double angleDeg = Double.NaN;
    private double rateDps = Double.NaN;

    private final TurretBurstControl control = new TurretBurstControl();
    private final TurretBurstControl.Settings settings = new TurretBurstControl.Settings();
    private boolean started;
    private boolean holding;
    private double targetDeg;
    private TurretBurstControl.Command sent = TurretBurstControl.Command.OFF;
    private long spinSinceNs;
    private boolean spinning;

    private boolean coastMeasuring;
    private double coastFromDeg;
    private double coastFromRate;
    private String coastNote = "none yet";
    private double peakRateDps;

    private boolean aWas, bWas, yWas, upWas, downWas, leftWas, rightWas, lbWas, rbWas;

    private final LoopTimer loopTimer = new LoopTimer();
    private TelemetryManager panels;

    @Override
    public void init() {
        try {
            encoderHost = hardwareMap.get(DcMotorEx.class, DeviceNames.TURRET_ENCODER_HOST);
        } catch (RuntimeException e) {
            setupProblem = "motor \"" + DeviceNames.TURRET_ENCODER_HOST + "\" (the encoder's socket) is not in the active configuration";
        }
        try {
            servo = hardwareMap.get(Servo.class, DeviceNames.TURRET);
            pwm = servo instanceof PwmControl ? (PwmControl) servo : null;
        } catch (RuntimeException e) {
            setupProblem = "servo \"" + DeviceNames.TURRET + "\" is not in the active configuration as a <Servo>";
        }
        if (servo != null && pwm == null) {
            setupProblem = "this servo cannot switch its pulse off, which is how it stops: not safe to run";
        }
        try {
            panels = PanelsTelemetry.INSTANCE.getTelemetry();
        } catch (RuntimeException | LinkageError e) {
            panels = null;
        }
        if (encoderHost != null) {
            zeroCounts = encoderHost.getCurrentPosition();
        }
        report();
    }

    @Override
    public void init_loop() {
        step();
    }

    /** PLAY: widen the pulse range so 0 and 1 are 500 and 2500 µs; the servo may be written from now on. */
    @Override
    public void start() {
        if (pwm != null) {
            pwm.setPwmRange(new PwmControl.PwmRange(500, 2500));
            pwm.setPwmDisable();
        }
        started = true;
    }

    @Override
    public void loop() {
        step();
    }

    @Override
    public void stop() {
        if (pwm != null) {
            pwm.setPwmDisable();
        }
    }

    private void step() {
        loopTimer.lap();
        refreshEncoder();
        read();
        buttons();
        TurretBurstControl.Command want = decide();
        guard(want);
        if (fault != null) {
            want = TurretBurstControl.Command.OFF;
        }
        send(want);
        coast();
        report();
        publish();
    }

    private void refreshEncoder() {
        if (encoderTurnsSetting == turnsPerTurretTurn && directionSetting == direction && encoder != null) {
            return;
        }
        encoderTurnsSetting = turnsPerTurretTurn;
        directionSetting = direction;
        try {
            encoder = new TurretEncoder(turnsPerTurretTurn, direction);
        } catch (IllegalArgumentException e) {
            encoder = null;
            fault = "bad encoder settings: " + e.getMessage();
        }
    }

    private void read() {
        if (encoderHost == null || encoder == null) {
            angleDeg = Double.NaN;
            rateDps = Double.NaN;
            return;
        }
        counts = encoderHost.getCurrentPosition();
        angleDeg = encoder.degrees(counts, zeroCounts);
        rateDps = encoder.degreesPerSecond(encoderHost.getVelocity());
        if (Math.abs(rateDps) > peakRateDps) {
            peakRateDps = Math.abs(rateDps);
        }
    }

    private void buttons() {
        if (gamepad1.a && !aWas && encoderHost != null) {
            zeroCounts = counts;
            targetDeg = 0.0;
        }
        aWas = gamepad1.a;
        if (gamepad1.b && !bWas) {
            holding = false;
            fault = null;
        }
        bWas = gamepad1.b;
        if (gamepad1.y && !yWas && started) {
            holding = !holding;
            fault = null;
        }
        yWas = gamepad1.y;
        if (gamepad1.dpad_left && !leftWas) targetDeg += stepDeg;
        leftWas = gamepad1.dpad_left;
        if (gamepad1.dpad_right && !rightWas) targetDeg -= stepDeg;
        rightWas = gamepad1.dpad_right;
        if (gamepad1.dpad_up && !upWas) targetDeg += 180.0;
        upWas = gamepad1.dpad_up;
        if (gamepad1.dpad_down && !downWas) targetDeg = 0.0;
        downWas = gamepad1.dpad_down;
        if (gamepad1.left_bumper && !lbWas) targetDeg += 5.0;
        lbWas = gamepad1.left_bumper;
        if (gamepad1.right_bumper && !rbWas) targetDeg -= 5.0;
        rbWas = gamepad1.right_bumper;
    }

    private TurretBurstControl.Command decide() {
        if (!started || setupProblem != null) {
            return TurretBurstControl.Command.OFF;
        }
        double stick = gamepad1.left_stick_x;
        if (Math.abs(stick) > STICK) {
            holding = false;
            // Stick left is negative x; left means counter-clockwise.
            return stick < 0 ? TurretBurstControl.Command.CCW : TurretBurstControl.Command.CW;
        }
        if (!holding) {
            return TurretBurstControl.Command.OFF;
        }
        settings.leadMs = leadMs;
        settings.burstBelowDeg = burstBelowDeg;
        settings.burstOnMs = burstOnMs;
        settings.burstOffMs = burstOffMs;
        settings.toleranceDeg = toleranceDeg;
        settings.shortestPath = shortestPath;
        return control.step(targetDeg, angleDeg, rateDps, settings, System.nanoTime());
    }

    /** Stops on the three ways an experiment like this goes wrong. */
    private void guard(TurretBurstControl.Command want) {
        long now = System.nanoTime();
        if (want == TurretBurstControl.Command.OFF) {
            spinning = false;
            return;
        }
        if (!spinning || want != sent) {
            spinning = true;
            spinSinceNs = now;
        }
        double ms = (now - spinSinceNs) / 1e6;
        if (ms > 250 && !Double.isNaN(rateDps) && Math.abs(rateDps) > 30 && Math.signum(rateDps) != want.sign) {
            fault = "turned the wrong way: flip ccwIsLowPulse (or direction, if the angle reads backwards). Circle clears";
        } else if (ms > 400 && (Double.isNaN(rateDps) || Math.abs(rateDps) < STILL_DPS)) {
            fault = "no encoder motion while spinning: is the servo in SWYFT continuous mode (Range 255/255), and the encoder plugged in? Circle clears";
        } else if (ms > maxSpinMs) {
            fault = String.format(Locale.US, "full speed for %.1f s without arriving: stopped. Circle clears", ms / 1000.0);
        }
        if (fault != null) {
            holding = false;
        }
    }

    private void send(TurretBurstControl.Command want) {
        if (!started || servo == null || pwm == null) {
            return;
        }
        if (want == sent) {
            return;
        }
        if (want == TurretBurstControl.Command.OFF) {
            pwm.setPwmDisable();
        } else {
            boolean low = (want == TurretBurstControl.Command.CCW) == ccwIsLowPulse;
            double us = low ? lowPulseUs : highPulseUs;
            servo.setPosition(Math.max(0.0, Math.min(1.0, (us - 500.0) / 2000.0)));
            pwm.setPwmEnable();
        }
        if (sent != TurretBurstControl.Command.OFF && want == TurretBurstControl.Command.OFF
                && !Double.isNaN(rateDps) && Math.abs(rateDps) >= 30) {
            coastMeasuring = true;
            coastFromDeg = angleDeg;
            coastFromRate = rateDps;
        } else if (want != TurretBurstControl.Command.OFF) {
            coastMeasuring = false;
        }
        sent = want;
    }

    /** After a cut from speed: how far it ran on, and the lead that predicts it. */
    private void coast() {
        if (!coastMeasuring || Double.isNaN(rateDps) || Math.abs(rateDps) >= STILL_DPS) {
            return;
        }
        coastMeasuring = false;
        double ran = angleDeg - coastFromDeg;
        double lead = coastFromRate == 0 ? Double.NaN : ran / coastFromRate * 1000.0;
        coastNote = String.format(Locale.US, "%.1f° from %.0f°/s → leadMs %.0f", ran, coastFromRate, lead);
    }

    private void report() {
        if (setupProblem != null) telemetry.addLine("SETUP: " + setupProblem);
        if (fault != null) telemetry.addLine("STOPPED: " + fault);
        telemetry.addLine("Servo must be in SWYFT continuous mode (Range 255/255). Flash it back before Basic Drive.");
        telemetry.addLine("");
        telemetry.addLine(String.format(Locale.US, "angle %s  rate %s/s  peak %.0f°/s  counts %d",
                deg(angleDeg), deg(rateDps), peakRateDps, counts));
        telemetry.addLine(String.format(Locale.US, "%s  target %s  aiming at %s  error %s  servo %s",
                holding ? "HOLD" : "free", deg(targetDeg), deg(control.effectiveTargetDegrees()),
                deg(control.lastErrorDegrees()), sent));
        telemetry.addLine("last coast: " + coastNote);
        telemetry.addLine(String.format(Locale.US,
                "settings: lead %.0f ms · bursts below %.0f° %.0f/%.0f ms · tol %.1f° · ccwIsLow %s · ratio %.2f dir %+d",
                leadMs, burstBelowDeg, burstOnMs, burstOffMs, toleranceDeg, ccwIsLowPulse, turnsPerTurretTurn, direction));
        telemetry.addLine("");
        telemetry.addLine("Cross zero (turret on mark) · stick left/right spin CCW/CW · Triangle hold");
        telemetry.addLine("D-pad L/R ±" + stepDeg + "° · up +180° · down 0° · L1/R1 ±5° · Circle stop");
        telemetry.addLine(loopTimer.summary());
        telemetry.update();
    }

    private void publish() {
        if (panels == null) return;
        try {
            panels.addData("turret_angle_deg", angleDeg);
            panels.addData("turret_rate_dps", rateDps);
            panels.addData("turret_target_deg", control.effectiveTargetDegrees());
            panels.addData("turret_error_deg", control.lastErrorDegrees());
            panels.addData("turret_cmd", sent.sign);
            panels.addData("loop_ms", loopTimer.lastMs());
            panels.update();
        } catch (RuntimeException ignored) {
            // Telemetry never takes the bench down.
        }
    }

    private static String deg(double value) {
        return Double.isNaN(value) ? "n/a" : String.format(Locale.US, "%.1f°", value);
    }
}
