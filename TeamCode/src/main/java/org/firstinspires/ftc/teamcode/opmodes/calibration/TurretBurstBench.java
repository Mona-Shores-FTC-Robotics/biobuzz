package org.firstinspires.ftc.teamcode.opmodes.calibration;

import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.Utility;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.PwmControl;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.hardware.ActiveConfig;
import org.firstinspires.ftc.teamcode.hardware.DeviceNames;
import org.firstinspires.ftc.teamcode.hardware.RobotIdentity;
import org.firstinspires.ftc.teamcode.subsystems.TurretEncoder;
import org.firstinspires.ftc.teamcode.subsystems.TurretServoControl;

import java.util.Locale;

/**
 * Standalone 19429 experiment: SWYFT flashed to continuous rotation, Cypher Max on the 64T
 * drive gear. Restores the October pulse controller without changing the match subsystem.
 *
 * <p>The XML remains Servo so 20245 and the position-mode implementation stay intact. Here
 * Servo is ONLY a pulse transport: position 0.5 is 1500 us, and signed power p becomes
 * 0.5 + p/2 over 500..2500 us. It is never a request for a physical angle. No XML edit needed.
 * Nothing writes PWM before PLAY; STOP disables the pulse. Only this utility may be run on
 * 19429 while its servo is flashed continuous: the ordinary bench and match OpModes still
 * send position commands. The feeder motor is never powered or reset here.
 */
@Utility(name = "Bench: Turret Burst", description = "19429 only, SWYFT CONTINUOUS mode: encoder pulse hold; hold R2 to drive")
@Configurable
public class TurretBurstBench extends OpMode {
    public static double turnsPerTurretTurn = 2.75;
    public static int direction = -1;
    /** Starting sign from the earlier bench; verify with a short manual nudge. */
    public static int servoDirection = -1;
    public static double servoStopPower = 0.0;
    /** Experimental cap, not a measured tuning value for SWYFT. */
    public static double maxPower = 0.12;
    public static double nudgePower = 0.12;
    public static double kP = 0.004;
    public static double kD = 0.002;
    public static double minPower = 0.07;
    public static double creepPower = 0.12;
    public static double creepOnMs = 25.0;
    public static double creepOffMs = 100.0;
    public static double creepBelowDeg = 10.0;
    public static double creepMaxRateDps = 30.0;
    public static double holdToleranceDeg = 1.0;
    public static double targetDeg = 5.0;
    public static double targetStepDeg = 5.0;
    /** Test window only: check physical clearance first; reduce if cables require it. */
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
    private TelemetryManager panels;
    private final TurretServoControl control = new TurretServoControl();
    private final TurretServoControl.Gains gains = new TurretServoControl.Gains(
            kP, kD, creepMaxRateDps, minPower, creepPower, maxPower, creepBelowDeg,
            holdToleranceDeg, creepOnMs, creepOffMs);

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
        report();
    }

    @Override
    public void init_loop() {
        step();
    }

    @Override
    public void start() {
        // Match the programmer's 500 / 1500 / 2500 us signals. No PWM changes during INIT.
        pwm.setPwmRange(new PwmControl.PwmRange(500, 2500));
        servo.setPosition(0.5);
        pwm.setPwmEnable();
        started = true;
    }

    @Override
    public void loop() {
        try {
            step();
        } catch (RuntimeException e) {
            pwm.setPwmDisable();
            throw e;
        }
    }

    @Override
    public void stop() {
        started = false;
        if (pwm != null) pwm.setPwmDisable();
    }

    private void step() {
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
        if (limit > 0 && limit <= 180) targetDeg = clamp(finite(targetDeg), limit);
        gains.set(kP, kD, creepMaxRateDps, minPower, creepPower, maxPower, creepBelowDeg,
                holdToleranceDeg, creepOnMs, creepOffMs);
        double requested = 0.0;
        if (gamepad1.left_bumper || gamepad1.right_bumper) {
            holding = false;
            requested = (gamepad1.left_bumper ? 1 : 0) - (gamepad1.right_bumper ? 1 : 0);
            requested *= Math.min(Math.max(finite(nudgePower), 0), gains.maxPower);
        } else if (holding && deadman) {
            requested = control.power(targetDeg - angleDeg, rateDps, gains, System.nanoTime());
        }
        requested = allowedPower(requested, angleDeg, zeroed, started && deadman,
                gamepad1.b || fault != null, limit);
        powerSent = clamp(requested * (servoDirection == -1 ? -1 : 1), gains.maxPower);
        if (started) {
            // R2 released or Circle held means exact neutral, independent of the trim setting.
            double trim = deadman && !gamepad1.b && zeroed && fault == null
                    && Double.isFinite(angleDeg) && limit > 0 && limit <= 180 && Math.abs(angleDeg) < limit
                    ? clamp(finite(servoStopPower), 0.2) : 0.0;
            servo.setPosition(0.5 + clamp(powerSent + trim, gains.maxPower) / 2.0);
        }
        report();
    }

    /** Reject invalid feedback and outward movement at the test window, including manual nudges. */
    static double allowedPower(double requested, double angle, boolean zeroed, boolean enabled,
                               boolean stopped, double limit) {
        if (!zeroed || !enabled || stopped || !Double.isFinite(angle)
                || !Double.isFinite(requested) || !Double.isFinite(limit) || limit <= 0 || limit > 180) return 0;
        if ((angle >= limit && requested > 0) || (angle <= -limit && requested < 0)) return 0;
        return requested;
    }

    private void report() {
        telemetry.addLine("19429 SWYFT CONTINUOUS ONLY. Do not run Basic Drive / ordinary Turret bench.");
        telemetry.addLine("Cross/A: zero at forward with R2 released. R2 held: enable movement.");
        telemetry.addLine("L1/R1: CCW/CW nudge. Triangle/Y: toggle hold. D-pad up/down: target +/- step.");
        telemetry.addLine("Circle/B: neutral and cancel hold. Release R2: neutral. STOP: PWM off.");
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

    private static double clamp(double value, double limit) {
        return Math.max(-limit, Math.min(limit, value));
    }
}
