package org.firstinspires.ftc.teamcode.opmodes.calibration;

import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.Utility;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.PwmControl;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.VoltageSensor;

import org.firstinspires.ftc.teamcode.hardware.ActiveConfig;
import org.firstinspires.ftc.teamcode.hardware.DeviceNames;
import org.firstinspires.ftc.teamcode.hardware.RobotIdentity;
import org.firstinspires.ftc.teamcode.subsystems.TurretEncoder;
import org.firstinspires.ftc.teamcode.logging.BuildStamp;
import org.firstinspires.ftc.teamcode.logging.MatchLog;
import org.firstinspires.ftc.teamcode.logging.MatchLogFiles;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.teamcode.subsystems.SwyftBurstControl;
import org.firstinspires.ftc.teamcode.subsystems.SwyftSingleBurst;

import java.util.Locale;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Standalone 19429 experiment: programmable SWYFT flashed to continuous rotation; Cypher Max
 * on the 64T drive gear. SWYFT's V2 programmer guide says partial speed is unavailable,
 * 1500 us RETURNS HOME, and stopping requires PWM disable. Never send a centre pulse here.
 *
 * <p>XML remains Servo; this utility sends only directional endpoints, 500 or 2500 us,
 * while a burst is active. Every pause, trigger release, stop request and STOP disables PWM
 * on this servo's port. Short full-speed bursts control average motion, not pulse amplitude.
 * Source: https://github.com/SWYFT-Robotics/swyft-servo-programmer-v2/blob/main/src/GuideWindow.xaml
 *
 * <p>No PWM writes before PLAY. Only this utility may be run on 19429 while its servo is
 * flashed continuous: ordinary bench/match OpModes still send positional commands. The feeder
 * motor is never powered or reset here. The software window needs a working encoder and
 * correct direction and cannot prevent coasting.
 */
@Utility(name = "Bench: Turret Burst", description = "19429 SWYFT continuous: FULL-SPEED bursts, PWM OFF to stop; hold R2")
@Configurable
public class TurretBurstBench extends OpMode {
    public static double turnsPerTurretTurn = 2.75;
    public static int direction = -1;
    /** Starting sign from the earlier bench; verify with a short manual nudge. */
    public static int servoDirection = -1;
    /** Full-speed ON time, clamped by SwyftBurstControl to at most 100 ms. */
    public static double burstOnMs = 25.0;
    /** Manual diagnostic: one longer burst per press; release R2/bumpers to rearm. */
    public static boolean manualSingleBurst = true;
    /** Diagnostic ON time, capped at 500 ms; hold bursts retain their separate 100 ms cap. */
    public static double manualBurstMs = 250.0;
    /** PWM-disabled pause, at least 100 ms. Start with a slow repetition rate. */
    public static double burstOffMs = 250.0;
    public static double holdToleranceDeg = 1.0;
    public static double targetDeg = 5.0;
    public static double targetStepDeg = 5.0;
    /** Test window only: check physical clearance first; reduce if mechanical clearance requires it. */
    public static double travelEachWayDeg = 30.0;

    private Servo servo;
    private PwmControl pwm;
    private DcMotorEx encoderHost;
    private long counts;
    private long zeroCounts;
    private boolean zeroed;
    private boolean holding;
    private boolean started;
    private boolean aWasDown;
    private boolean yWasDown;
    private boolean upWasDown;
    private boolean downWasDown;
    private double angleDeg;
    private double rateDps;
    private double powerSent;
    private String fault;
    private MatchLog log = MatchLog.disabled("not initialized");
    private VoltageSensor battery;
    private double batteryVolts = Double.NaN;
    private long batteryReadNs;
    private long previousLoopNs;
    private double loopMs;
    private double rawRequest;
    private double allowedRequest;
    private double burstStartAngle;
    private double lastBurstDelta;
    private boolean previousZeroed;
    private long previousZeroCounts;
    private boolean pwmReportedEnabled;
    private boolean wasDriving;
    private long driveStartedNs;
    private long burstsSent;
    private double lastBurstMs;
    private double lastPulseUs;
    private TelemetryManager panels;
    private final SwyftBurstControl control = new SwyftBurstControl();
    private final SwyftSingleBurst singleBurst = new SwyftSingleBurst();

    @Override
    public void init() {
        if (RobotIdentity.fromConfigName(ActiveConfig.name()) != RobotIdentity.TEAM_19429) {
            throw new IllegalStateException("Bench: Turret Burst requires active robot_19429 configuration");
        }
        servo = hardwareMap.get(Servo.class, DeviceNames.TURRET);
        if (!(servo instanceof PwmControl)) {
            throw new IllegalStateException("Turret servo must support per-port PWM control");
        }
        pwm = (PwmControl) servo;
        encoderHost = hardwareMap.get(DcMotorEx.class, DeviceNames.TURRET_ENCODER_HOST);
        counts = encoderHost.getCurrentPosition();
        zeroCounts = counts;
        zeroed = false;
        holding = false;
        started = false;
        try {
            panels = PanelsTelemetry.INSTANCE.getTelemetry();
        } catch (RuntimeException | LinkageError ignored) {
            // Driver Station telemetry remains available.
        }
        for (VoltageSensor sensor : hardwareMap.voltageSensor) {
            battery = sensor;
            break;
        }
        long originNs = System.nanoTime();
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("OpMode", "Bench: Turret Burst");
        metadata.put("RobotConfig", ActiveConfig.name());
        metadata.put("Build", BuildStamp.describe());
        metadata.put("GitCommit", BuildStamp.commit());
        metadata.put("GitBranch", BuildStamp.branch());
        metadata.put("BenchVersion", "SWYFT v4 recorded diagnostic");
        File file = MatchLogFiles.next(new File(AppUtil.FIRST_FOLDER, MatchLogFiles.LOGS_FOLDER),
                MatchLogFiles.matchId(System.currentTimeMillis()), "Bench_Turret_Burst");
        log = MatchLog.toFile(file, "SWYFT turret bench", metadata,
                () -> (System.nanoTime() - originNs) / 1000L);
        log.event("Turret burst INIT; no motion before PLAY; recording inputs, commands and encoder");
        recordLog();
        report();
    }

    @Override
    public void init_loop() {
        step();
    }

    @Override
    public void start() {
        // SWYFT centre is HOME, not stop. PLAY starts with no signal.
        pwm.setPwmDisable();
        pwm.setPwmRange(new PwmControl.PwmRange(500, 2500));
        control.reset();
        started = true;
        log.event("Turret burst PLAY; PWM disabled until a drive request");
    }

    @Override
    public void loop() {
        try {
            step();
        } catch (RuntimeException e) {
            pwm.setPwmDisable();
            powerSent = 0;
            pwmReportedEnabled = false;
            log.event("Turret burst FAULT: " + e);
            recordLog();
            log.close("Turret burst fault; PWM disabled", 500);
            throw e;
        }
    }

    @Override
    public void stop() {
        started = false;
        try {
            if (pwm != null) pwm.setPwmDisable();
        } finally {
            powerSent = 0;
            pwmReportedEnabled = false;
            recordLog();
            log.close("Turret burst STOP; final angle is last sample, settling may be incomplete", 500);
        }
    }

    private void step() {
        long loopNs = System.nanoTime();
        loopMs = previousLoopNs == 0 ? 0 : (loopNs - previousLoopNs) / 1_000_000.0;
        previousLoopNs = loopNs;
        if (battery != null && loopNs - batteryReadNs >= 500_000_000L) {
            batteryVolts = battery.getVoltage();
            batteryReadNs = loopNs;
        }
        fault = null;
        try {
            TurretEncoder encoder = new TurretEncoder(turnsPerTurretTurn, direction);
            counts = encoderHost.getCurrentPosition();
            angleDeg = encoder.degrees(counts, zeroCounts);
            rateDps = encoder.degreesPerSecond(encoderHost.getVelocity());
        } catch (IllegalArgumentException e) {
            angleDeg = Double.NaN;
            rateDps = Double.NaN;
            fault = e.getMessage();
        }
        boolean deadman = gamepad1.right_trigger > 0.5;
        // Zero is a deliberate, unpowered action, and resets an active hold.
        if (gamepad1.a && !aWasDown && !deadman && fault == null) {
            zeroCounts = counts;
            angleDeg = 0.0;
            zeroed = true;
            holding = false;
        }
        aWasDown = gamepad1.a;
        if (gamepad1.y && !yWasDown && started && zeroed && fault == null) holding = !holding;
        yWasDown = gamepad1.y;
        if (gamepad1.b) holding = false;
        if (gamepad1.dpad_up && !upWasDown) targetDeg += finite(targetStepDeg);
        if (gamepad1.dpad_down && !downWasDown) targetDeg -= finite(targetStepDeg);
        upWasDown = gamepad1.dpad_up;
        downWasDown = gamepad1.dpad_down;
        double limit = finite(travelEachWayDeg);
        if (limit > 0 && limit <= 180) targetDeg = Math.max(-limit, Math.min(limit, finite(targetDeg)));
        long nowNs = System.nanoTime();
        boolean manual = gamepad1.left_bumper || gamepad1.right_bumper;
        double manualRequest = (gamepad1.left_bumper ? 1 : 0) - (gamepad1.right_bumper ? 1 : 0);
        double diagnostic = singleBurst.power(started && deadman && !gamepad1.b && zeroed
                && fault == null && manualSingleBurst ? manualRequest : 0, manualBurstMs, nowNs);
        double requested = 0.0;
        if (manual) {
            holding = false;
            requested = manualSingleBurst ? diagnostic : manualRequest;
        } else if (holding) {
            double tolerance = finite(holdToleranceDeg);
            double error = targetDeg - angleDeg;
            if (tolerance >= 0 && Math.abs(error) > tolerance) requested = Math.signum(error);
        }
        rawRequest = requested;
        requested = allowedPower(requested, angleDeg, zeroed, started && deadman,
                gamepad1.b || fault != null, limit);
        allowedRequest = requested;
        double burst;
        if (manual && manualSingleBurst) {
            control.reset();
            burst = requested;
        } else {
            burst = control.power(requested, burstOnMs, burstOffMs, nowNs);
        }
        powerSent = burst * (servoDirection == -1 ? -1 : 1);
        if (started) {
            writeBurst(servo, pwm, powerSent);
            pwmReportedEnabled = pwm.isPwmEnabled();
            long writtenNs = System.nanoTime();
            boolean driving = powerSent != 0;
            if (driving && !wasDriving) {
                driveStartedNs = writtenNs;
                burstsSent++;
                burstStartAngle = angleDeg;
                log.event(String.format(Locale.US, "Turret burst #%d START angle %.3f deg, signal %.0f us, manual %.1f ms",
                        burstsSent, angleDeg, powerSent > 0 ? 2500.0 : 500.0, manualBurstMs));
            }
            if (driving) lastPulseUs = powerSent > 0 ? 2500 : 500;
            if (!driving && wasDriving) {
                lastBurstMs = (writtenNs - driveStartedNs) / 1_000_000.0;
                lastBurstDelta = angleDeg - burstStartAngle;
                log.event(String.format(Locale.US, "Turret burst #%d END %.1f ms, angle %.3f -> %.3f, delta %+.3f deg, R2 %.2f L1 %s R1 %s",
                        burstsSent, lastBurstMs, burstStartAngle, angleDeg, lastBurstDelta,
                        gamepad1.right_trigger, gamepad1.left_bumper, gamepad1.right_bumper));
            }
            wasDriving = driving;
        }
        recordLog();
        report();
    }

    /** The normal asynchronous match logger: no disk writes or waits in the control loop. */
    private void recordLog() {
        if (zeroed != previousZeroed || zeroCounts != previousZeroCounts) {
            log.event("Turret zero reference: zeroed=" + zeroed + ", counts=" + zeroCounts);
            previousZeroed = zeroed;
            previousZeroCounts = zeroCounts;
        }
        log.match(started ? MatchLog.Mode.TELEOP : MatchLog.Mode.DISABLED, 0);
        log.loopMs(loopMs);
        log.put("/TurretBurst/Counts", counts);
        log.put("/TurretBurst/ZeroCounts", zeroCounts);
        log.put("/TurretBurst/Zeroed", zeroed ? 1 : 0);
        log.put("/TurretBurst/AngleDeg", angleDeg);
        log.put("/TurretBurst/RateDps", rateDps);
        log.put("/TurretBurst/TargetDeg", targetDeg);
        log.put("/TurretBurst/ErrorDeg", targetDeg - angleDeg);
        log.put("/TurretBurst/R2", gamepad1.right_trigger);
        log.put("/TurretBurst/L1", gamepad1.left_bumper ? 1 : 0);
        log.put("/TurretBurst/R1", gamepad1.right_bumper ? 1 : 0);
        log.put("/TurretBurst/Cross", gamepad1.a ? 1 : 0);
        log.put("/TurretBurst/Triangle", gamepad1.y ? 1 : 0);
        log.put("/TurretBurst/Circle", gamepad1.b ? 1 : 0);
        log.put("/TurretBurst/Holding", holding ? 1 : 0);
        log.put("/TurretBurst/RawRequest", rawRequest);
        log.put("/TurretBurst/AllowedRequest", allowedRequest);
        log.put("/TurretBurst/Power", powerSent);
        // Zero here means no signal, NOT a 0-us pulse, and never a centre/home command.
        log.put("/TurretBurst/PulseUs", powerSent == 0 ? 0 : powerSent > 0 ? 2500 : 500);
        log.put("/TurretBurst/PwmEnabledSdk", pwmReportedEnabled ? 1 : 0);
        log.put("/TurretBurst/BurstCount", burstsSent);
        log.put("/TurretBurst/LastOnMs", lastBurstMs);
        log.put("/TurretBurst/LastDriveDeltaDeg", lastBurstDelta);
        log.put("/TurretBurst/ManualSingleBurst", manualSingleBurst ? 1 : 0);
        log.put("/TurretBurst/ManualBurstMs", manualBurstMs);
        log.put("/TurretBurst/BurstOnMs", burstOnMs);
        log.put("/TurretBurst/BurstOffMs", burstOffMs);
        log.put("/TurretBurst/ToleranceDeg", holdToleranceDeg);
        log.put("/TurretBurst/TravelEachWayDeg", travelEachWayDeg);
        log.put("/TurretBurst/EncoderRatio", turnsPerTurretTurn);
        log.put("/TurretBurst/EncoderDirection", direction);
        log.put("/TurretBurst/ServoDirection", servoDirection);
        log.put("/TurretBurst/BatteryVolts", batteryVolts);
        log.put("/TurretBurst/SettingsFault", fault == null ? 0 : 1);
        log.commit();
    }

    /** Reject invalid feedback and outward movement at the test window, including manual nudges. */
    static double allowedPower(double requested, double angle, boolean zeroed, boolean enabled,
                               boolean stopped, double limit) {
        if (!zeroed || !enabled || stopped || !Double.isFinite(angle)
                || !Double.isFinite(requested) || !Double.isFinite(limit) || limit <= 0 || limit > 180) return 0;
        if ((angle >= limit && requested > 0) || (angle <= -limit && requested < 0)) return 0;
        return requested;
    }

    /** Servo is a PWM transport: endpoints drive, every zero disables the port; never 0.5. */
    static void writeBurst(Servo servo, PwmControl pwm, double power) {
        if (!Double.isFinite(power) || power == 0.0) {
            pwm.setPwmDisable();
        } else {
            servo.setPosition(power > 0 ? 1.0 : 0.0);
            pwm.setPwmEnable();
        }
    }

    private void report() {
        telemetry.addLine("19429 SWYFT CONTINUOUS ONLY. Do not run Basic Drive / ordinary Turret bench.");
        telemetry.addLine("Cross/A: zero at forward with R2 released. R2 held: enable FULL-SPEED bursts.");
        telemetry.addLine("L1/R1: one CCW/CW diagnostic burst; release to rearm. Triangle/Y: hold. D-pad: target.");
        telemetry.addLine("Circle/B, R2 released, and pauses: PWM OFF. STOP: PWM OFF.");
        telemetry.addData("Build", "SWYFT v4: RECORDED bursts / PWM OFF");
        telemetry.addData("Burst on/off ms", burstOnMs + " / " + burstOffMs);
        telemetry.addData("Manual test", manualSingleBurst ? "ONE burst, " + manualBurstMs + " ms (max 500)" : "repeating bursts");
        telemetry.addData("Inputs R2 / L1 / R1", String.format(Locale.US, "%.2f / %s / %s",
                gamepad1.right_trigger, gamepad1.left_bumper, gamepad1.right_bumper));
        telemetry.addData("PWM enabled (SDK)", pwmReportedEnabled);
        telemetry.addData("Bursts / last signal / last on time", String.format(Locale.US,
                "%d / %.0f us / %.1f ms", burstsSent, lastPulseUs, lastBurstMs));
        telemetry.addData("Last drive delta", String.format(Locale.US, "%+.3f deg", lastBurstDelta));
        telemetry.addData("Recording", log.isLogging() ? log.name() : "FAULT: " + log.failure());
        telemetry.addData("Log dropped loops", log.droppedLoops());
        telemetry.addData("Zeroed", zeroed);
        telemetry.addData("Counts", counts);
        telemetry.addData("Angle / rate", String.format(Locale.US, "%.2f deg / %.2f deg/s", angleDeg, rateDps));
        telemetry.addData("Target / window", String.format(Locale.US, "%.1f / +/-%.1f deg", targetDeg, travelEachWayDeg));
        telemetry.addData("Hold / signed servo power", holding + " / " + powerSent);
        if (fault != null) telemetry.addData("FAULT", fault);
        telemetry.update();
        if (panels != null) {
            panels.addData("turret_burst_angle_deg", angleDeg);
            panels.addData("turret_burst_target_deg", targetDeg);
            panels.addData("turret_burst_rate_dps", rateDps);
            panels.addData("turret_burst_power", powerSent);
            panels.update();
        }
    }

    private static double finite(double value) {
        return Double.isFinite(value) ? value : 0;
    }

}
