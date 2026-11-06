package org.firstinspires.ftc.teamcode.opmodes.calibration;

import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.Utility;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.PwmControl;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.RobotDifferences;
import org.firstinspires.ftc.teamcode.hardware.ActiveConfig;
import org.firstinspires.ftc.teamcode.hardware.DeviceNames;
import org.firstinspires.ftc.teamcode.hardware.RobotIdentity;
import org.firstinspires.ftc.teamcode.subsystems.TurretEncoder;
import org.firstinspires.ftc.teamcode.subsystems.TurretSubsystem;
import org.firstinspires.ftc.teamcode.util.LoopTimer;

import java.util.Locale;

/**
 * Bench test for the turret on a competition robot: a goBILDA servo in standard (position) mode
 * ({@code DeviceNames.TURRET}) turning it, and, once fitted, a Studica Cypher Max encoder read
 * through the encoder socket of Expansion Hub motor port 3 ({@code DeviceNames.TURRET_ENCODER_HOST}).
 * It measures the numbers {@code TurretSubsystem} needs, none of which may be guessed:
 *
 * <ol>
 *   <li><b>{@code homePosition}.</b> Press PLAY: the servo goes to {@code TurretSubsystem.homePosition}.
 *       Step it with the D-pad (fine) and the bumpers (coarse) until the turret's mark lines up
 *       straight ahead along the nose. The {@code position} shown is the value for
 *       {@code TurretSubsystem.homePosition}.</li>
 *   <li><b>{@code degreesPerUnit}.</b> At home, press Square to mark. Step the turret to a known
 *       angle (a 90° mark is easiest), counter-clockwise seen from above. {@code degreesPerUnit} is
 *       that angle over the {@code moved} shown, signed: negative if the position went down.</li>
 *   <li><b>Encoder</b> (once the Cypher Max is fitted): Cross zeros it with the turret at home; turn
 *       counter-clockwise and {@code angle} should rise (else {@code TurretSubsystem.direction} -1);
 *       one full turn by hand gives {@code encoder turns} = {@code turnsPerTurretTurn}.</li>
 * </ol>
 *
 * <p>Standalone diagnostics, like {@code ValidateHardware}: no {@code Robot}, no
 * {@code Subsystem}, the exception CLAUDE.md allows for rigs. It reads the feeder motor's encoder
 * socket and never touches that motor's power or mode.
 *
 * <p>Nothing moves in INIT: the servo gets its first position only after PLAY. Circle switches its
 * pulse off and on (limp, to turn the turret by hand).
 */
@Utility(name = "Bench: Turret", description = "The turret's position servo: its home position and degrees per unit; the encoder once fitted")
@Configurable
public class TurretBench extends OpMode {

    /** D-pad step, servo position units. */
    public static double fineStep = 0.002;

    /** Bumper step, servo position units. */
    public static double coarseStep = 0.02;

    // ------------------------------------------------------------ hardware

    private DcMotorEx encoderHost;
    private Servo servo;
    private PwmControl pwm;
    private String configWarning;
    private String encoderFault;
    private String servoFault;

    // ------------------------------------------------------------ state

    private TurretEncoder encoder;
    private String encoderProblem;
    private long counts;
    private long zeroCounts;
    private boolean zeroed;
    private double angleDeg = Double.NaN;

    private double position = Double.NaN;
    private double markPosition = Double.NaN;
    private boolean limp = true;
    private boolean started;

    private boolean aWasDown;
    private boolean bWasDown;
    private boolean xWasDown;
    private boolean yWasDown;
    private boolean leftWasDown;
    private boolean rightWasDown;
    private boolean lbWasDown;
    private boolean rbWasDown;

    private final LoopTimer loopTimer = new LoopTimer();
    private TelemetryManager panels;
    private String panelsProblem;

    @Override
    public void init() {
        configWarning = checkActiveConfig();
        try {
            encoderHost = hardwareMap.get(DcMotorEx.class, DeviceNames.TURRET_ENCODER_HOST);
        } catch (RuntimeException e) {
            encoderHost = null;
            encoderFault = "motor \"" + DeviceNames.TURRET_ENCODER_HOST
                    + "\" (whose encoder socket is the turret encoder's) is not in the active configuration";
        }
        try {
            servo = hardwareMap.get(Servo.class, DeviceNames.TURRET);
            pwm = servo instanceof PwmControl ? (PwmControl) servo : null;
        } catch (RuntimeException e) {
            servo = null;
            servoFault = "servo \"" + DeviceNames.TURRET + "\" is not in the active configuration as a <Servo>";
        }
        try {
            panels = PanelsTelemetry.INSTANCE.getTelemetry();
            if (panels == null) panelsProblem = "PanelsTelemetry returned no telemetry manager";
        } catch (RuntimeException | LinkageError e) {
            panels = null;
            panelsProblem = "not available at init: " + e;
        }
        try {
            encoder = new TurretEncoder(TurretSubsystem.turnsPerTurretTurn, TurretSubsystem.direction);
        } catch (IllegalArgumentException e) {
            encoderProblem = e.getMessage();
        }
        if (encoderHost != null) {
            zeroCounts = encoderHost.getCurrentPosition();
        }
        // This robot's values (19429's homePosition is its own), as RobotOpMode applies them.
        try {
            RobotDifferences.applyForActiveRobot();
        } catch (RuntimeException e) {
            if (configWarning == null) configWarning = "robot differences not applied: " + e.getMessage();
        }
        double home = TurretSubsystem.homePosition;
        position = home >= 0.0 && home <= 1.0 ? home : 0.5;
        telemetry.addLine("Bench: Turret. Nothing moves until PLAY; then the servo goes to homePosition.");
        report();
    }

    @Override
    public void init_loop() {
        step();
    }

    @Override
    public void start() {
        started = true;
        limp = false;
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
        readEncoder();
        handleButtons();
        if (started && servo != null) {
            if (limp) {
                if (pwm != null) pwm.setPwmDisable();
            } else {
                servo.setPosition(position);
                if (pwm != null) pwm.setPwmEnable();
            }
        }
        report();
        publish();
    }

    private void readEncoder() {
        if (encoderHost == null) {
            return;
        }
        try {
            counts = encoderHost.getCurrentPosition();
        } catch (RuntimeException e) {
            encoderFault = "encoder read failed: " + (e.getMessage() == null ? e.toString() : e.getMessage());
            return;
        }
        angleDeg = encoder == null ? Double.NaN : encoder.degrees(counts, zeroCounts);
    }

    private void handleButtons() {
        boolean a = gamepad1.a;
        if (a && !aWasDown && encoderHost != null) {
            zeroCounts = counts;
            zeroed = true;
        }
        aWasDown = a;

        boolean b = gamepad1.b;
        if (b && !bWasDown && started) limp = !limp;
        bWasDown = b;

        boolean x = gamepad1.x;
        if (x && !xWasDown) markPosition = position;
        xWasDown = x;

        boolean y = gamepad1.y;
        if (y && !yWasDown) {
            double home = TurretSubsystem.homePosition;
            if (home >= 0.0 && home <= 1.0) position = home;
        }
        yWasDown = y;

        position = stepped(position, gamepad1.dpad_right, rightWasDown, fineStep);
        rightWasDown = gamepad1.dpad_right;
        position = stepped(position, gamepad1.dpad_left, leftWasDown, -fineStep);
        leftWasDown = gamepad1.dpad_left;
        position = stepped(position, gamepad1.right_bumper, rbWasDown, coarseStep);
        rbWasDown = gamepad1.right_bumper;
        position = stepped(position, gamepad1.left_bumper, lbWasDown, -coarseStep);
        lbWasDown = gamepad1.left_bumper;
    }

    /** One step on a press, kept in [0, 1]; a NaN or infinite step does nothing. */
    static double stepped(double position, boolean down, boolean wasDown, double step) {
        if (!down || wasDown || Double.isNaN(step) || Double.isInfinite(step)) {
            return position;
        }
        return Math.round(Math.max(0.0, Math.min(1.0, position + step)) * 1000.0) / 1000.0;
    }

    // ------------------------------------------------------------ display

    private void report() {
        if (configWarning != null) telemetry.addLine("CONFIG: " + configWarning);
        if (encoderFault != null) telemetry.addLine("ENCODER FAULT: " + encoderFault);
        if (servoFault != null) telemetry.addLine("SERVO FAULT: " + servoFault);
        if (encoderProblem != null) telemetry.addLine("SETTINGS: " + encoderProblem);
        telemetry.addLine("");
        telemetry.addLine(String.format(Locale.US, "position %.3f  %s  (homePosition in code %.3f)",
                position, !started ? "not sent until PLAY" : limp ? "LIMP" : "held",
                TurretSubsystem.homePosition));
        telemetry.addLine(Double.isNaN(markPosition) ? "no mark (Square marks the position here)"
                : String.format(Locale.US, "mark %.3f  moved %+.3f  → degreesPerUnit = degrees turned / %+.3f",
                        markPosition, position - markPosition, position - markPosition));
        telemetry.addLine(String.format(Locale.US, "encoder counts %d (zero %d%s)  turns %.3f  angle %s",
                counts, zeroCounts, zeroed ? "" : ", not zeroed",
                TurretEncoder.encoderTurns(counts, zeroCounts), deg(angleDeg)));
        telemetry.addLine("");
        telemetry.addLine("D-pad left/right ±" + fineStep + " · L1/R1 ±" + coarseStep
                + " · Triangle back to homePosition · Square mark · Circle limp on/off · Cross zero encoder");
        telemetry.addLine("");
        telemetry.addLine(loopTimer.summary());
        if (panelsProblem != null) telemetry.addLine("Panels: " + panelsProblem);
        telemetry.update();
    }

    private void publish() {
        if (panels == null) return;
        try {
            panels.addData("turret_position", position);
            panels.addData("turret_counts", counts);
            panels.addData("turret_angle_deg", angleDeg);
            panels.addData("loop_ms", loopTimer.lastMs());
            panels.update();
        } catch (RuntimeException ignored) {
            // Telemetry never takes the bench down.
        }
    }

    // ------------------------------------------------------------ helpers

    /** Null if the active configuration is one that declares the turret; otherwise what to do. */
    private static String checkActiveConfig() {
        String active;
        try {
            active = ActiveConfig.name();
        } catch (RuntimeException e) {
            return "Could not read the active configuration: " + e.getMessage();
        }
        RobotIdentity identity = RobotIdentity.fromConfigName(active);
        if (identity != null) {
            for (DeviceNames.Device device : identity.devices) {
                if (DeviceNames.TURRET.equals(device.name)) return null;
            }
        }
        return "Active config is \"" + (active == null ? "none" : active)
                + "\", which has no turret. Driver Station: Configure Robot -> robot_20245 (or robot_19429) -> Activate.";
    }

    private static String deg(double value) {
        return Double.isNaN(value) ? "n/a" : String.format(Locale.US, "%.1f°", value);
    }
}
