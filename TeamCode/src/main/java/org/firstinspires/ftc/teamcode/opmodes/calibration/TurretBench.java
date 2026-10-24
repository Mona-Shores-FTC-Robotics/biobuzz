package org.firstinspires.ftc.teamcode.opmodes.calibration;

import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.teamcode.hardware.ActiveConfig;
import org.firstinspires.ftc.teamcode.hardware.DeviceNames;
import org.firstinspires.ftc.teamcode.hardware.RobotIdentity;
import org.firstinspires.ftc.teamcode.subsystems.TurretEncoder;
import org.firstinspires.ftc.teamcode.subsystems.TurretServoControl;
import org.firstinspires.ftc.teamcode.util.LoopTimer;

import java.util.Locale;

/**
 * Bench test for the turret's drive and angle sensing, on a competition robot: a continuous
 * servo ({@code DeviceNames.TURRET}) turning the turret, and a Studica Cypher Max encoder read
 * through the encoder socket of Expansion Hub motor port 3 ({@code DeviceNames.TURRET_ENCODER_HOST}).
 * It settles the facts a {@code TurretSubsystem} needs, none of which may be guessed:
 *
 * <ol>
 *   <li><b>Does the encoder count?</b> Turn the turret by hand and watch {@code counts}.</li>
 *   <li><b>Encoder direction.</b> Press Cross (A) with the turret at home, turn it counter-clockwise
 *       (seen from above): {@code angle} should go up. If it goes down, set {@code direction} to
 *       -1 in Panels. Every angle in this codebase is counter-clockwise positive.</li>
 *   <li><b>Gear ratio.</b> One full turret turn by hand: {@code encoder turns} is
 *       {@code turnsPerTurretTurn}. Type it into Panels; {@code angle} then reads in turret
 *       degrees.</li>
 *   <li><b>Servo direction.</b> Push the left stick left: the turret should turn
 *       counter-clockwise. If it turns the other way, set {@code servoDirection} to -1.</li>
 *   <li><b>Closed loop.</b> Triangle (Y) holds the turret at {@code targetDeg}; D-pad up/down moves the
 *       target. Raise {@code kP} until it arrives briskly without hunting; if it stops a degree or
 *       two short, raise {@code minPower} until the last degrees close, and set
 *       {@code creepOnMs}/{@code creepOffMs} to pulse it so it steps onto the target instead of jumping past. If it runs away the servo
 *       direction is wrong; the hold cuts out past {@code runawayDeg} and says so.</li>
 * </ol>
 *
 * <p>Standalone diagnostics, like {@code ValidateHardware}: no {@code Robot}, no
 * {@code Subsystem}, the exception CLAUDE.md allows for rigs. It reads the feeder motor's encoder
 * socket and never touches that motor's power or mode; the feeder runs without its encoder.
 *
 * <p>Nothing moves in INIT: the servo gets power only after PLAY. Zeroing with Cross works in INIT, so
 * the turret can be put at home and zeroed before anything can turn it.
 */
@TeleOp(name = "Turret Bench", group = "Diagnostics")
@Configurable
public class TurretBench extends OpMode {

    // ------------------------------------------------------------ Panels tunables (live)

    /** Encoder turns per turret turn, from the gearing. 1 on the encoder's own shaft; 2.75 on the goBILDA kit. */
    public static double turnsPerTurretTurn = 1.0;

    /** +1 if the count rises as the turret turns counter-clockwise seen from above, -1 if it falls. */
    public static int direction = 1;

    /** +1 if positive servo power turns the turret counter-clockwise, -1 if clockwise. */
    public static int servoDirection = 1;

    /** Most servo power the stick or the hold may ask for, [0, 1]. 1.0 held fine on the bench (24 Oct 2026). */
    public static double maxPower = 1.0;

    /** Servo power while a bumper is held. */
    public static double nudgePower = 0.12;

    /** Hold gain: servo power per degree of error. 0.007 arrived without rocking on the bench (24 Oct 2026). */
    public static double kP = 0.007;

    /**
     * Damping: power per degree per second of turret rate, taken off the drive, so the servo
     * brakes into the target instead of being carried past it. 0 = none. Start at 0.002 and raise
     * it until the hump over the target disappears; too much and the approach gets sluggish.
     */
    public static double kD = 0.0;

    /** In the creep zone, faster than this the hold brakes rather than creeps, deg/s. */
    public static double creepMaxRateDps = 30.0;

    /** Where Triangle holds the turret, degrees from home, counter-clockwise positive. D-pad up/down moves it. */
    public static double targetDeg = 45.0;

    /** D-pad step for the target, degrees. */
    public static double targetStepDeg = 15.0;

    /** Inside this many degrees of the target the hold sends no power, so the servo does not buzz. */
    public static double holdToleranceDeg = 2.0;

    /**
     * Least power the hold sends while outside the tolerance, [0, 1]. A continuous servo does not
     * move below some power, so a small error times {@code kP} can ask for less than that and the
     * turret stops a degree or two short. 0: off. Raise it until the last few degrees close.
     */
    public static double minPower = 0.12;

    /**
     * Creep: when the hold is down to {@code minPower}, send it for {@code creepOnMs}, then 0 for
     * {@code creepOffMs}, and repeat. Punching a sticky servo with a steady power moves it several
     * degrees before the loop can stop it, and where it stops is luck: 0.5 deg one time, 5 the
     * next. A pulse moves it a fraction of a degree, so it steps onto the target. The servo reads
     * its signal every 20 ms, so a pulse shorter than two or three of those frames may never be
     * seen; a loop here is about 14 ms, which is why this is in milliseconds and not loops.
     * {@code creepOffMs} 0: no pulsing, minPower is continuous.
     */
    public static double creepOnMs = 30.0;
    public static double creepOffMs = 100.0;

    /**
     * Inside this many degrees of the target the hold creeps (pulses of {@code minPower}); outside
     * it, it drives continuously at {@code minPower + kP * error}, so a big error moves briskly
     * and only the last degrees are stepped.
     */
    public static double creepBelowDeg = 10.0;

    /**
     * The power at which this servo actually stands still, [-0.2, 0.2]. A continuous servo's
     * centre is rarely exactly 0: at 0 it drifts slowly one way, which shows as the angle
     * wandering off the target after the hold has arrived. Added to every power sent, including
     * the hold's 0, so "0" means stopped. Find it: stick centred, watch the angle drift, adjust
     * until it stops. Better still, set the servo's centre with the goBILDA programmer and leave
     * this at 0.
     */
    public static double servoStopPower = 0.0;

    /** A hold error past this means the servo is driving the wrong way: cut power and say so. */
    public static double runawayDeg = 180.0;

    static final double STICK_DEADBAND = 0.08;

    // ------------------------------------------------------------ hardware

    private DcMotorEx encoderHost;
    private CRServo turret;
    private String configWarning;
    private String encoderFault;
    private String servoFault;

    // ------------------------------------------------------------ state

    private TurretEncoder encoder;
    private String encoderProblem;
    private double encoderTurnsSetting = Double.NaN;
    private int directionSetting;

    private long counts;
    private long zeroCounts;
    private boolean zeroed;
    private double countsPerSecond;
    private double angleDeg = Double.NaN;
    private double rateDps = Double.NaN;

    private boolean holding;
    private String holdNote;
    private double errorDeg = Double.NaN;
    private double powerSent;

    private boolean aWasDown;
    private boolean yWasDown;
    private boolean bWasDown;
    private boolean upWasDown;
    private boolean downWasDown;

    private final LoopTimer loopTimer = new LoopTimer();
    private final TurretServoControl control = new TurretServoControl();
    private final TurretServoControl.Gains gains = new TurretServoControl.Gains(
            kP, kD, creepMaxRateDps, minPower, maxPower, creepBelowDeg, holdToleranceDeg, creepOnMs, creepOffMs);
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
                    + "\" (whose encoder socket carries the turret encoder) is not in the active configuration";
        }
        try {
            turret = hardwareMap.get(CRServo.class, DeviceNames.TURRET);
            turret.setPower(0.0);
        } catch (RuntimeException e) {
            turret = null;
            servoFault = "servo \"" + DeviceNames.TURRET + "\" is not in the active configuration";
        }
        try {
            panels = PanelsTelemetry.INSTANCE.getTelemetry();
            if (panels == null) panelsProblem = "PanelsTelemetry returned no telemetry manager";
        } catch (RuntimeException | LinkageError e) {
            panels = null;
            panelsProblem = "not available at init: " + e;
        }
        refreshEncoderSettings();
        if (encoderHost != null) {
            // Until A is pressed, angles are relative to wherever the turret sat at INIT.
            zeroCounts = encoderHost.getCurrentPosition();
        }
        telemetry.addLine("Turret Bench. Put the turret at home and press Cross. Nothing moves until PLAY.");
        report();
    }

    @Override
    public void init_loop() {
        step(false);
    }

    @Override
    public void loop() {
        step(true);
    }

    @Override
    public void stop() {
        if (turret != null) {
            turret.setPower(0.0);
        }
    }

    private void step(boolean mayDrive) {
        loopTimer.lap();
        refreshEncoderSettings();
        readEncoder();
        handleButtons(mayDrive);

        double requested = 0.0; // counter-clockwise positive, before the servo's own sign
        if (mayDrive && turret != null) {
            if (holding) {
                requested = holdPower();
            } else {
                requested = manualPower();
            }
        }
        powerSent = clamp(requested * servoSign(), unitRange(maxPower));
        if (turret != null) {
            turret.setPower(clamp(powerSent + stopOffset(), 1.0));
        }

        report();
        publish();
    }

    /** Rebuilds the converter only when a Panels value changes; a bad value is reported, never used. */
    private void refreshEncoderSettings() {
        if (encoderTurnsSetting == turnsPerTurretTurn && directionSetting == direction && encoder != null) {
            return;
        }
        encoderTurnsSetting = turnsPerTurretTurn;
        directionSetting = direction;
        try {
            encoder = new TurretEncoder(turnsPerTurretTurn, direction);
            encoderProblem = null;
        } catch (IllegalArgumentException e) {
            encoder = null;
            encoderProblem = e.getMessage();
        }
    }

    private void readEncoder() {
        if (encoderHost == null) {
            return;
        }
        try {
            counts = encoderHost.getCurrentPosition();
            countsPerSecond = encoderHost.getVelocity();
        } catch (RuntimeException e) {
            encoderFault = "encoder read failed: " + (e.getMessage() == null ? e.toString() : e.getMessage());
            return;
        }
        if (encoder != null) {
            angleDeg = encoder.degrees(counts, zeroCounts);
            rateDps = encoder.degreesPerSecond(countsPerSecond);
        } else {
            angleDeg = Double.NaN;
            rateDps = Double.NaN;
        }
    }

    private void handleButtons(boolean mayDrive) {
        boolean a = gamepad1.a;
        if (a && !aWasDown && encoderHost != null) {
            zeroCounts = counts;
            zeroed = true;
        }
        aWasDown = a;

        boolean y = gamepad1.y;
        if (y && !yWasDown && mayDrive) {
            holding = !holding;
            holdNote = null;
        }
        yWasDown = y;

        boolean b = gamepad1.b;
        if (b && !bWasDown) {
            holding = false;
            holdNote = null;
        }
        bWasDown = b;

        boolean up = gamepad1.dpad_up;
        if (up && !upWasDown) targetDeg += finite(targetStepDeg);
        upWasDown = up;

        boolean down = gamepad1.dpad_down;
        if (down && !downWasDown) targetDeg -= finite(targetStepDeg);
        downWasDown = down;
    }

    private double manualPower() {
        errorDeg = Double.NaN;
        // Stick left is negative x. Left means counter-clockwise, which is positive here.
        double stick = -deadband(gamepad1.left_stick_x) * unitRange(maxPower);
        if (gamepad1.left_bumper) stick += unitRange(nudgePower);
        if (gamepad1.right_bumper) stick -= unitRange(nudgePower);
        return stick;
    }

    private double holdPower() {
        if (Double.isNaN(angleDeg)) {
            holding = false;
            holdNote = "no angle to hold: fix the encoder settings first";
            return 0.0;
        }
        errorDeg = finite(targetDeg) - angleDeg;
        if (Math.abs(errorDeg) > Math.max(finite(runawayDeg), 1.0)) {
            holding = false;
            holdNote = String.format(Locale.US,
                    "RUNAWAY: %.0f deg from target. Positive power drove the turret away; flip servoDirection.",
                    errorDeg);
            return 0.0;
        }
        // The same law the robot's TurretSubsystem runs, so what is tuned here is what it gets.
        gains.set(kP, kD, creepMaxRateDps, minPower, maxPower, creepBelowDeg, holdToleranceDeg, creepOnMs, creepOffMs);
        return control.power(errorDeg, rateDps, gains, System.nanoTime());
    }

    /** The servo's centre correction, clamped so a typo cannot make "stopped" mean "spinning". */
    private static double stopOffset() {
        return clamp(servoStopPower, 0.2);
    }

    private int servoSign() {
        return servoDirection == -1 ? -1 : 1;
    }

    // ------------------------------------------------------------ display

    private void report() {
        if (configWarning != null) telemetry.addLine("CONFIG: " + configWarning);
        if (encoderFault != null) telemetry.addLine("ENCODER FAULT: " + encoderFault);
        if (servoFault != null) telemetry.addLine("SERVO FAULT: " + servoFault);
        if (encoderProblem != null) telemetry.addLine("SETTINGS: " + encoderProblem);
        if (holdNote != null) telemetry.addLine("HOLD: " + holdNote);
        telemetry.addLine("");
        telemetry.addLine(String.format(Locale.US, "counts %d  (zero %d%s)  encoder turns %.3f",
                counts, zeroCounts, zeroed ? "" : ", NOT ZEROED: relative to INIT",
                TurretEncoder.encoderTurns(counts, zeroCounts)));
        telemetry.addLine(String.format(Locale.US, "angle %s  (wrapped %s)  rate %s/s",
                deg(angleDeg), deg(TurretEncoder.wrap180(angleDeg)), deg(rateDps)));
        telemetry.addLine(String.format(Locale.US, "servo power %+.2f (stop offset %+.3f)  mode %s%s", powerSent, stopOffset(),
                holding ? "HOLD" : "MANUAL",
                holding ? String.format(Locale.US, "  target %s  error %s", deg(targetDeg), deg(errorDeg))
                        : String.format(Locale.US, "  (Triangle holds at %s)", deg(targetDeg))));
        telemetry.addLine(String.format(Locale.US,
                "settings: ratio %.3f  dir %+d  servoDir %+d  maxPower %.2f  kP %.4f  kD %.4f  minPower %.2f  creep %.0f/%.0f ms below %.1f  tol %.1f",
                turnsPerTurretTurn, direction, servoDirection, unitRange(maxPower), kP, kD, unitRange(minPower),
                creepOnMs, creepOffMs, creepBelowDeg, holdToleranceDeg));
        telemetry.addLine("");
        telemetry.addLine("Cross zero (turret at home) · left stick left/right = CCW/CW · L1/R1 nudge");
        telemetry.addLine("Triangle hold at target · D-pad up/down target ±step · Circle stop hold");
        telemetry.addLine("Check: hand-turn CCW, angle should rise (else direction -1); stick left, turret CCW (else servoDirection -1)");
        telemetry.addLine("");
        telemetry.addLine(loopTimer.summary());
        if (panelsProblem != null) telemetry.addLine("Panels: " + panelsProblem);
        telemetry.update();
    }

    private void publish() {
        if (panels == null) return;
        try {
            panels.addData("turret_counts", counts);
            panels.addData("turret_encoder_turns", TurretEncoder.encoderTurns(counts, zeroCounts));
            panels.addData("turret_angle_deg", angleDeg);
            panels.addData("turret_rate_dps", rateDps);
            panels.addData("turret_power", powerSent);
            panels.addData("turret_target_deg", targetDeg);
            panels.addData("turret_error_deg", errorDeg);
            panels.addData("turret_hold", holding ? 1.0 : 0.0);
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

    /** Clamps a Panels-editable power to [0, 1]; NaN is 0. A typo gives a slow turret, never a fast one. */
    static double unitRange(double value) {
        return value > 0.0 ? Math.min(value, 1.0) : 0.0;
    }

    static double clamp(double value, double limit) {
        if (Double.isNaN(value)) return 0.0;
        return Math.max(-limit, Math.min(limit, value));
    }

    private static double finite(double value) {
        return Double.isNaN(value) || Double.isInfinite(value) ? 0.0 : value;
    }

    private static double deadband(double value) {
        return Math.abs(value) < STICK_DEADBAND ? 0.0 : value;
    }
}
