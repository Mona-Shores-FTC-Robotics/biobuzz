package org.firstinspires.ftc.teamcode.opmodes.calibration;

import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.Utility;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.PwmControl;

import org.firstinspires.ftc.teamcode.RobotDifferences;
import org.firstinspires.ftc.teamcode.hardware.ActiveConfig;
import org.firstinspires.ftc.teamcode.hardware.DeviceNames;
import org.firstinspires.ftc.teamcode.hardware.RobotIdentity;
import org.firstinspires.ftc.teamcode.subsystems.TurretEncoder;
import org.firstinspires.ftc.teamcode.subsystems.TurretHomeSwitch;
import org.firstinspires.ftc.teamcode.subsystems.TurretServoControl;
import org.firstinspires.ftc.teamcode.subsystems.TurretSubsystem;
import org.firstinspires.ftc.teamcode.util.LoopTimer;

import java.util.Locale;

/**
 * Bench test for the turret on a competition robot: a goBILDA 25-3 Speed servo in continuous mode
 * ({@code DeviceNames.TURRET}) turning it, and the REV Through Bore Encoder read through the
 * encoder socket of the intake's motor, or of the back-right drive wheel's on a robot moved to it
 * ({@code TurretSubsystem.encoderHostName()}). It
 * measures the numbers {@code TurretSubsystem} needs, none of which may be guessed, in this order:
 *
 * <ol>
 *   <li><b>Encoder</b>, in INIT, nothing powered: Cross zeros it with the turret on its home
 *       mark. Turn the turret counter-clockwise (seen from above) by hand: {@code angle} should
 *       rise, else {@code TurretSubsystem.direction} is the other sign. One full turn reads
 *       {@code encoder turns} = {@code turnsPerTurretTurn} (2.75) and {@code angle} 360. Four times
 *       that, or a quarter: {@code countsPerEncoderTurn} is the other encoder's (8192 for a REV
 *       Through Bore, 2048 for a Cypher Max).</li>
 *   <li><b>The home switch</b> (either robot whose XML declares {@code DeviceNames.TURRET_HOME}, a REV Magnetic Limit
 *       Switch under a magnet on the back of the turret), in INIT, nothing powered. {@code home
 *       switch} reads MAGNET with the turret on its mark: if it never does, the switch is on the
 *       connector's other channel (the XML's {@code port}) or the magnet is too far from it. Zero
 *       on the mark (Cross), then turn the turret slowly by hand right across the mark, both ways,
 *       a few times: each pass shows the band's width and its middle from the zero. The middle,
 *       both ways' average, negated, is {@code TurretSubsystem.homeSwitchOffsetDeg}; the width,
 *       with a few degrees spare, is {@code homeSwitchMaxBandDeg}. A pass at power (step 5) shows
 *       whether a fast pass lands on the same middle.</li>
 *   <li><b>{@code servoStopPower}.</b> PLAY: the servo is sent power 0. If the turret creeps, step
 *       the power (D-pad, 0.005) until it stands still: that power is the value.</li>
 *   <li><b>{@code servoDirection}.</b> One bumper press to the right (+0.05 steps): {@code angle}
 *       should rise. If it falls, {@code servoDirection} is the other sign.</li>
 *   <li><b>{@code minPower}.</b> From stopped, step the power up until the turret turns steadily,
 *       each way: the larger distance from the stop power is the value.</li>
 *   <li><b>{@code topSpeedDps}.</b> Step to +1.0 and -1.0: {@code fastest} is the turret's top
 *       speed each way. Use the slower.</li>
 *   <li><b>The loop.</b> Triangle (stop), then Square: the turret is held on the target with
 *       {@code TurretSubsystem}'s own law and values. Push it off: it comes back. D-pad up/down
 *       moves the target 45° (from 180 it should cross to -135 the short way). Raise {@code kP}
 *       until it hunts, then back off; add {@code kD} if it swings past.</li>
 * </ol>
 *
 * <p>Standalone diagnostics, like {@code ValidateHardware}: no {@code Robot}, no
 * {@code Subsystem}, the exception CLAUDE.md allows for rigs. It reads the intake motor's encoder
 * socket and never touches that motor's power or mode.
 *
 * <p>Nothing moves in INIT: the servo gets its first pulse only after PLAY. Circle switches its
 * pulse off and on (limp, to turn the turret by hand). The turret has no stops, so a power left
 * on just keeps it turning: Triangle stops it.
 */
@Utility(name = "Bench: Turret", description = "The turret's continuous servo, encoder and home switch: signs, stop power, floor, top speed, the switch's band, and the hold")
@Configurable
public class TurretBench extends OpMode {

    /** D-pad step, servo power. */
    public static double fineStep = 0.005;

    /** Bumper step, servo power. */
    public static double coarseStep = 0.05;

    /** D-pad up/down step of the hold's target, degrees. */
    public static double targetStepDeg = 45.0;

    // ------------------------------------------------------------ hardware

    private DcMotorEx encoderHost;
    private CRServo servo;
    private PwmControl pwm;
    private String configWarning;
    private String encoderFault;
    private String servoFault;
    /** The home switch, or null on a robot without one ({@link #homeNote} says which). */
    private DigitalChannel homeInput;
    private String homeNote;

    // ------------------------------------------------------------ state

    private TurretEncoder encoder;
    private String encoderProblem;
    private final TurretHomeSwitch home = new TurretHomeSwitch();
    private boolean magnet;
    /** The band's middle from the last pass each way, hub counts; NaN before one. */
    private double ccwMiddleCounts = Double.NaN;
    private double cwMiddleCounts = Double.NaN;
    private double ccwBandDeg = Double.NaN;
    private double cwBandDeg = Double.NaN;
    private String homeRejected;
    private long counts;
    private long zeroCounts;
    private boolean zeroed;
    private double angleDeg = Double.NaN;
    private double rateDps = Double.NaN;
    private double fastestCcwDps;
    private double fastestCwDps;

    /** The raw power sent to the servo while not holding: no sign, no trim applied. */
    private double rawPower;
    private boolean holding;
    private double targetDeg;
    private double holdPower;
    private boolean limp = true;
    private boolean started;
    private final TurretServoControl.Gains gains = new TurretServoControl.Gains(0, 0, 0, 0, 0, 0, 0, 0);

    private boolean aWasDown;
    private boolean bWasDown;
    private boolean xWasDown;
    private boolean yWasDown;
    private boolean leftWasDown;
    private boolean rightWasDown;
    private boolean upWasDown;
    private boolean downWasDown;
    private boolean lbWasDown;
    private boolean rbWasDown;

    private final LoopTimer loopTimer = new LoopTimer();
    private TelemetryManager panels;
    private String panelsProblem;

    @Override
    public void init() {
        configWarning = checkActiveConfig();
        // This robot's values, as RobotOpMode applies them.
        try {
            RobotDifferences.applyForActiveRobot();
        } catch (RuntimeException e) {
            if (configWarning == null) configWarning = "robot differences not applied: " + e.getMessage();
        }
        try {
            encoderHost = hardwareMap.get(DcMotorEx.class, TurretSubsystem.encoderHostName());
        } catch (RuntimeException e) {
            encoderHost = null;
            encoderFault = "motor \"" + TurretSubsystem.encoderHostName()
                    + "\" (whose encoder socket is the turret encoder's) is not in the active configuration";
        }
        try {
            servo = hardwareMap.get(CRServo.class, DeviceNames.TURRET);
            pwm = servo instanceof PwmControl ? (PwmControl) servo : null;
        } catch (RuntimeException e) {
            servo = null;
            servoFault = "servo \"" + DeviceNames.TURRET
                    + "\" is not in the active configuration as a <ContinuousRotationServo>";
        }
        if (configHas(DeviceNames.TURRET_HOME)) {
            try {
                homeInput = hardwareMap.get(DigitalChannel.class, DeviceNames.TURRET_HOME);
                homeInput.setMode(DigitalChannel.Mode.INPUT);
            } catch (RuntimeException e) {
                homeInput = null;
                homeNote = "FAULT: \"" + DeviceNames.TURRET_HOME
                        + "\" is not in the active configuration as a <DigitalDevice>";
            }
        } else {
            homeNote = "none on this robot";
        }
        try {
            panels = PanelsTelemetry.INSTANCE.getTelemetry();
            if (panels == null) panelsProblem = "PanelsTelemetry returned no telemetry manager";
        } catch (RuntimeException | LinkageError e) {
            panels = null;
            panelsProblem = "not available at init: " + e;
        }
        if (encoderHost != null) {
            zeroCounts = TurretSubsystem.hostSign(encoderHost.getDirection()) * (long) encoderHost.getCurrentPosition();
        }
        telemetry.addLine("Bench: Turret. Nothing moves until PLAY; then the servo is sent power 0.");
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
        } else if (servo != null) {
            servo.setPower(0.0);
        }
    }

    private void step() {
        loopTimer.lap();
        readEncoder();
        handleButtons();
        if (started && servo != null) {
            if (limp) {
                if (pwm != null) {
                    pwm.setPwmDisable();
                } else {
                    servo.setPower(0.0);
                }
            } else {
                servo.setPower(holding ? holdOutput() : rawPower);
                if (pwm != null) pwm.setPwmEnable();
            }
        }
        report();
        publish();
    }

    /** The hold's servo power: {@code TurretSubsystem}'s law, sign and stop trim, with its live values. */
    private double holdOutput() {
        gains.set(TurretSubsystem.kP, TurretSubsystem.kD, TurretSubsystem.minPower, TurretSubsystem.maxPower,
                TurretSubsystem.topSpeedDps, TurretSubsystem.feedforward, TurretSubsystem.holdToleranceDeg,
                TurretSubsystem.targetStillDps);
        holdPower = TurretServoControl.power(TurretServoControl.shortestErrorDeg(targetDeg, angleDeg),
                rateDps, 0.0, gains);
        return servoPower(holdPower, TurretSubsystem.servoDirection, TurretSubsystem.servoStopPower);
    }

    /** A counter-clockwise power as the servo's: its sign, plus its stop trim (kept inside ±0.2), inside ±1. */
    static double servoPower(double ccwPower, int servoDirection, double stopPower) {
        double trim = Double.isNaN(stopPower) ? 0.0 : Math.max(-0.2, Math.min(0.2, stopPower));
        double out = (Double.isNaN(ccwPower) ? 0.0 : ccwPower) * (servoDirection == -1 ? -1 : 1) + trim;
        return Math.max(-1.0, Math.min(1.0, out));
    }

    private void readEncoder() {
        try {
            encoder = new TurretEncoder(TurretSubsystem.countsPerEncoderTurn, TurretSubsystem.turnsPerTurretTurn,
                    TurretSubsystem.direction);
            encoderProblem = null;
        } catch (IllegalArgumentException e) {
            encoder = null;
            encoderProblem = e.getMessage();
        }
        if (encoderHost == null) {
            return;
        }
        double countsPerSecond;
        try {
            int hostSign = TurretSubsystem.hostSign(encoderHost.getDirection());
            counts = hostSign * (long) encoderHost.getCurrentPosition();
            countsPerSecond = hostSign * encoderHost.getVelocity();
        } catch (RuntimeException e) {
            encoderFault = "encoder read failed: " + (e.getMessage() == null ? e.toString() : e.getMessage());
            return;
        }
        angleDeg = encoder == null ? Double.NaN : encoder.degrees(counts, zeroCounts);
        rateDps = encoder == null ? Double.NaN : encoder.degreesPerSecond(countsPerSecond);
        readHomeSwitch();
        if (started && !limp && !Double.isNaN(rateDps)) {
            fastestCcwDps = Math.max(fastestCcwDps, rateDps);
            fastestCwDps = Math.max(fastestCwDps, -rateDps);
        }
    }

    /**
     * The switch, and a pass across it: no speed limit and no offset, so a pass shows the band's own
     * middle. The limits on the band's width are the subsystem's, so a pass it would not take shows why.
     */
    private void readHomeSwitch() {
        if (homeInput == null) return;
        try {
            magnet = !homeInput.getState();   // active low: low with the magnet over it
        } catch (RuntimeException e) {
            homeNote = "read failed: " + (e.getMessage() == null ? e.toString() : e.getMessage());
            return;
        }
        TurretHomeSwitch.Result result = home.step(magnet, counts, encoder, rateDps, Double.NaN,
                TurretSubsystem.homeSwitchMinBandDeg, TurretSubsystem.homeSwitchMaxBandDeg, 0.0);
        if (result == TurretHomeSwitch.Result.PASS) {
            homeRejected = null;
            if (home.passDirection() > 0) {
                ccwMiddleCounts = home.markCounts();
                ccwBandDeg = home.bandDeg();
            } else {
                cwMiddleCounts = home.markCounts();
                cwBandDeg = home.bandDeg();
            }
        } else if (result == TurretHomeSwitch.Result.REJECTED) {
            homeRejected = home.rejectReason();
        }
    }

    /** A pass's middle as an angle from the current zero, wrapped; NaN before one. */
    private double middleDeg(double middleCounts) {
        if (encoder == null || Double.isNaN(middleCounts)) return Double.NaN;
        return TurretEncoder.wrap180(encoder.degrees(Math.round(middleCounts), zeroCounts));
    }

    private void handleButtons() {
        boolean a = gamepad1.a;
        if (a && !aWasDown && encoderHost != null) {
            zeroCounts = counts;
            zeroed = true;
        }
        aWasDown = a;

        boolean b = gamepad1.b;
        if (b && !bWasDown && started) {
            limp = !limp;
            holding = false;
            rawPower = 0.0;
        }
        bWasDown = b;

        boolean x = gamepad1.x;
        if (x && !xWasDown && started && !limp) {
            holding = !holding;
            rawPower = 0.0;
            targetDeg = Double.isNaN(angleDeg) ? 0.0 : Math.round(TurretEncoder.wrap180(angleDeg));
        }
        xWasDown = x;

        boolean y = gamepad1.y;
        if (y && !yWasDown) {
            holding = false;
            rawPower = 0.0;
            fastestCcwDps = 0.0;
            fastestCwDps = 0.0;
        }
        yWasDown = y;

        if (holding) {
            if (gamepad1.dpad_up && !upWasDown) targetDeg = TurretEncoder.wrap180(targetDeg + finite(targetStepDeg));
            if (gamepad1.dpad_down && !downWasDown) targetDeg = TurretEncoder.wrap180(targetDeg - finite(targetStepDeg));
        } else {
            rawPower = stepped(rawPower, gamepad1.dpad_right, rightWasDown, fineStep);
            rawPower = stepped(rawPower, gamepad1.dpad_left, leftWasDown, -fineStep);
            rawPower = stepped(rawPower, gamepad1.right_bumper, rbWasDown, coarseStep);
            rawPower = stepped(rawPower, gamepad1.left_bumper, lbWasDown, -coarseStep);
        }
        upWasDown = gamepad1.dpad_up;
        downWasDown = gamepad1.dpad_down;
        rightWasDown = gamepad1.dpad_right;
        leftWasDown = gamepad1.dpad_left;
        rbWasDown = gamepad1.right_bumper;
        lbWasDown = gamepad1.left_bumper;
    }

    /** One step of power on a press, kept inside ±1. A NaN or infinite step does nothing. */
    static double stepped(double power, boolean down, boolean wasDown, double step) {
        if (!down || wasDown || Double.isNaN(step) || Double.isInfinite(step)) {
            return power;
        }
        return Math.max(-1.0, Math.min(1.0, Math.round((power + step) * 1000.0) / 1000.0));
    }

    private static double finite(double value) {
        return Double.isNaN(value) || Double.isInfinite(value) ? 0.0 : value;
    }

    // ------------------------------------------------------------ display

    private void report() {
        if (configWarning != null) telemetry.addLine("CONFIG: " + configWarning);
        if (encoderFault != null) telemetry.addLine("ENCODER FAULT: " + encoderFault);
        if (servoFault != null) telemetry.addLine("SERVO FAULT: " + servoFault);
        if (encoderProblem != null) telemetry.addLine("SETTINGS: " + encoderProblem);
        telemetry.addLine("");
        telemetry.addLine(String.format(Locale.US, "encoder counts %d (zero %d%s)  turns %.3f  angle %s  rate %s",
                counts, zeroCounts, zeroed ? "" : ", not zeroed",
                encoder == null ? Double.NaN : encoder.encoderTurns(counts, zeroCounts), deg(angleDeg), dps(rateDps)));
        reportHomeSwitch();
        telemetry.addLine(String.format(Locale.US, "fastest: %.0f°/s counter-clockwise, %.0f°/s clockwise (Triangle clears)",
                fastestCcwDps, fastestCwDps));
        if (!started) {
            telemetry.addLine("servo: not sent until PLAY");
        } else if (limp) {
            telemetry.addLine("servo: LIMP (Circle)");
        } else if (holding) {
            telemetry.addLine(String.format(Locale.US, "HOLD %s → %.0f° (%+.1f)  power %+.3f  [kP %.3f kD %.4f floor %.2f]",
                    deg(TurretEncoder.wrap180(angleDeg)), targetDeg,
                    TurretServoControl.shortestErrorDeg(targetDeg, angleDeg), holdPower,
                    TurretSubsystem.kP, TurretSubsystem.kD, TurretSubsystem.minPower));
        } else {
            telemetry.addLine(String.format(Locale.US, "servo power %+.3f, as sent (in code: stop %+.3f, direction %+d)",
                    rawPower, TurretSubsystem.servoStopPower, TurretSubsystem.servoDirection));
        }
        telemetry.addLine("");
        telemetry.addLine(holding
                ? "D-pad up/down target ±" + targetStepDeg + "° · Square leaves the hold · Triangle stop · Circle limp"
                : "D-pad left/right ±" + fineStep + " · L1/R1 ±" + coarseStep
                        + " · Triangle stop · Square hold here · Circle limp on/off · Cross zero encoder");
        telemetry.addLine("");
        telemetry.addLine(loopTimer.summary());
        if (panelsProblem != null) telemetry.addLine("Panels: " + panelsProblem);
        telemetry.update();
    }

    private void reportHomeSwitch() {
        if (homeInput == null) {
            telemetry.addLine("home switch: " + homeNote);
            return;
        }
        telemetry.addLine("home switch: " + (magnet ? "MAGNET (on its mark)" : "no magnet")
                + (homeNote != null ? " · " + homeNote : ""));
        double ccw = middleDeg(ccwMiddleCounts);
        double cw = middleDeg(cwMiddleCounts);
        telemetry.addLine(String.format(Locale.US, "  pass counter-clockwise: %s · clockwise: %s",
                pass(ccwBandDeg, ccw), pass(cwBandDeg, cw)));
        if (!Double.isNaN(ccw) && !Double.isNaN(cw)) {
            double middle = (ccw + cw) / 2.0;
            telemetry.addLine(String.format(Locale.US,
                    "  middle both ways %+.1f°%s: homeSwitchOffsetDeg %+.1f if Cross zeroed on the mark (now %+.1f)",
                    middle, zeroed ? "" : " (not zeroed)", -middle, TurretSubsystem.homeSwitchOffsetDeg));
        } else {
            telemetry.addLine("  Cross on the mark, then turn it slowly by hand right across the mark, both ways");
        }
        if (homeRejected != null) telemetry.addLine("  last pass not taken: " + homeRejected);
    }

    private static String pass(double bandDeg, double middleDeg) {
        return Double.isNaN(middleDeg) ? "none yet"
                : String.format(Locale.US, "band %.1f° wide, middle %+.1f°", bandDeg, middleDeg);
    }

    private void publish() {
        if (panels == null) return;
        try {
            panels.addData("turret_power", holding ? holdPower : rawPower);
            panels.addData("turret_counts", counts);
            panels.addData("turret_angle_deg", angleDeg);
            panels.addData("turret_rate_dps", rateDps);
            panels.addData("turret_target_deg", holding ? targetDeg : Double.NaN);
            if (homeInput != null) panels.addData("turret_home_magnet", magnet ? 1 : 0);
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
        if (configHas(DeviceNames.TURRET)) return null;
        return "Active config is \"" + (active == null ? "none" : active)
                + "\", which has no turret. Driver Station: Configure Robot -> robot_20245 (or robot_19429) -> Activate.";
    }

    /** Whether the active configuration's robot has a device of this name; false when it cannot be read. */
    private static boolean configHas(String name) {
        RobotIdentity identity;
        try {
            identity = RobotIdentity.fromConfigName(ActiveConfig.name());
        } catch (RuntimeException e) {
            return false;
        }
        if (identity == null) return false;
        for (DeviceNames.Device device : identity.devices) {
            if (name.equals(device.name)) return true;
        }
        return false;
    }

    private static String deg(double value) {
        return Double.isNaN(value) ? "n/a" : String.format(Locale.US, "%.1f°", value);
    }

    private static String dps(double value) {
        return Double.isNaN(value) ? "n/a" : String.format(Locale.US, "%.0f°/s", value);
    }
}
