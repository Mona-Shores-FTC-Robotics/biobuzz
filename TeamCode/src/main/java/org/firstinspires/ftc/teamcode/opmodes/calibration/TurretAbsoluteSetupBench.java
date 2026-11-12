package org.firstinspires.ftc.teamcode.opmodes.calibration;

import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.Utility;
import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.teamcode.RobotDifferences;
import org.firstinspires.ftc.teamcode.hardware.ActiveConfig;
import org.firstinspires.ftc.teamcode.hardware.DeviceNames;
import org.firstinspires.ftc.teamcode.hardware.RobotIdentity;
import org.firstinspires.ftc.teamcode.subsystems.TurretAbsolute;
import org.firstinspires.ftc.teamcode.subsystems.TurretSubsystem;
import org.firstinspires.ftc.teamcode.util.LoopTimer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * The guided setup of the turret's two absolute encoders: Melonbotics Through Bore Encoders on the
 * 176T ring (A on the 64T gear, B on the 44T), their analog outputs on the hub's analog inputs
 * {@link DeviceNames#TURRET_ABS_A} and {@link DeviceNames#TURRET_ABS_B}. Nothing moves: the turret
 * is turned by hand.
 *
 * <p>The Driver Station shows one step at a time ({@link AbsoluteSetup}), each moving on by itself
 * when it has what it needs: both readings move; each encoder's voltage range; which way each
 * turns; the home readings; a check. At the end it shows the lines to paste into
 * {@code RobotDifferences} for this robot, puts them on Panels (copy them from the laptop's browser)
 * and in the Robot Controller log, and applies them until the app restarts, so
 * {@code Bench: Turret Absolute} can be tried straight away.
 *
 * <p>Cross (A) does what a step asks. Square (X) starts the step again. D-pad left goes back a step.
 *
 * <p>Standalone, like {@code ValidateHardware}: no {@code Robot}, no {@code Subsystem}. It applies
 * this robot's {@code RobotDifferences} first, as {@code RobotOpMode} would.
 */
@Utility(name = "Bench: Turret Absolute Setup", description = "Guided setup of the turret's two absolute encoders: step by step, turret turned by hand")
public class TurretAbsoluteSetupBench extends OpMode {

    private AnalogInput encoderA;
    private AnalogInput encoderB;
    private final LoopTimer loopTimer = new LoopTimer();
    private AbsoluteSetup setup;

    private String configWarning;
    private String fault;
    private String robotName = "TEAM_XXXXX";
    private final String date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    private double voltsA = Double.NaN;
    private double voltsB = Double.NaN;
    private TelemetryManager panels;
    private String panelsProblem;
    private boolean crossWas;
    private boolean squareWas;
    private boolean leftWas;
    private boolean logged;

    @Override
    public void init() {
        configWarning = checkActiveConfig();
        try {
            RobotDifferences.applyForActiveRobot();
        } catch (RuntimeException e) {
            if (configWarning == null) configWarning = "robot differences not applied: " + e.getMessage();
        }
        // After this robot's values are applied, so the ratios are the ones it runs with.
        setup = new AbsoluteSetup(TurretSubsystem.absRatioA, TurretSubsystem.absRatioB);
        try {
            encoderA = hardwareMap.get(AnalogInput.class, DeviceNames.TURRET_ABS_A);
            encoderB = hardwareMap.get(AnalogInput.class, DeviceNames.TURRET_ABS_B);
        } catch (RuntimeException e) {
            fault = "analog inputs \"" + DeviceNames.TURRET_ABS_A + "\" / \"" + DeviceNames.TURRET_ABS_B
                    + "\" are not in the active configuration";
        }
        try {
            panels = PanelsTelemetry.INSTANCE.getTelemetry();
            if (panels == null) panelsProblem = "PanelsTelemetry returned no telemetry manager";
        } catch (RuntimeException | LinkageError e) {
            panels = null;
            panelsProblem = "not available at init: " + e;
        }
        report();
    }

    @Override
    public void init_loop() {
        step();
    }

    @Override
    public void loop() {
        step();
    }

    private void step() {
        loopTimer.lap();
        boolean cross = gamepad1.a && !crossWas;
        boolean square = gamepad1.x && !squareWas;
        boolean left = gamepad1.dpad_left && !leftWas;
        crossWas = gamepad1.a;
        squareWas = gamepad1.x;
        leftWas = gamepad1.dpad_left;
        if (square) setup.redo();
        if (left) setup.back();

        if (fault == null) {
            voltsA = encoderA.getVoltage();
            voltsB = encoderB.getVoltage();
        }
        setup.update(fault == null, fault, voltsA, voltsB, cross);

        if (setup.measured()) {
            apply();
            double fa = TurretAbsolute.fractionOfRange(voltsA, TurretSubsystem.absVoltsMinA,
                    TurretSubsystem.absVoltsMaxA, AbsoluteSetup.SLACK_VOLTS);
            double fb = TurretAbsolute.fractionOfRange(voltsB, TurretSubsystem.absVoltsMinB,
                    TurretSubsystem.absVoltsMaxB, AbsoluteSetup.SLACK_VOLTS);
            double separation = TurretAbsolute.separationDeg(TurretSubsystem.absRatioA, TurretSubsystem.absRatioB);
            double maxResidual = Double.isNaN(TurretSubsystem.absMaxResidualDeg)
                    ? separation / 3.0 : TurretSubsystem.absMaxResidualDeg;
            setup.checkSolution(TurretAbsolute.solve(fa, fb, TurretSubsystem.absHomeA, TurretSubsystem.absHomeB,
                    TurretSubsystem.absRatioA, TurretSubsystem.absRatioB,
                    TurretSubsystem.absDirectionA, TurretSubsystem.absDirectionB, maxResidual, separation / 2.0));
        }
        if (setup.step() == AbsoluteSetup.Step.DONE && !logged) {
            logged = true;
            for (String line : setup.pasteLines(robotName, date)) RobotLog.ii("TurretAbsoluteSetup", line);
        } else if (setup.step() != AbsoluteSetup.Step.DONE) {
            logged = false;
        }
        report();
        publish();
    }

    /** This run's measured values into TurretSubsystem, until the app restarts. */
    private void apply() {
        TurretSubsystem.absVoltsMinA = setup.minA();
        TurretSubsystem.absVoltsMaxA = setup.maxA();
        TurretSubsystem.absVoltsMinB = setup.minB();
        TurretSubsystem.absVoltsMaxB = setup.maxB();
        TurretSubsystem.absDirectionA = setup.dirA();
        TurretSubsystem.absDirectionB = setup.dirB();
        TurretSubsystem.absHomeA = setup.homeA();
        TurretSubsystem.absHomeB = setup.homeB();
        if (setup.step() == AbsoluteSetup.Step.DONE) TurretSubsystem.useAbsoluteZero = true;
    }

    private void report() {
        if (configWarning != null) telemetry.addLine("CONFIG: " + configWarning);
        telemetry.addLine("== " + setup.title() + " ==");
        if (setup.problem() != null) telemetry.addLine("⚠ " + setup.problem());
        for (String line : setup.lines()) telemetry.addLine(line);
        if (setup.step() == AbsoluteSetup.Step.DONE) {
            telemetry.addLine("");
            for (String line : setup.pasteLines(robotName, date)) telemetry.addLine(line);
        }
        telemetry.addLine("");
        telemetry.addLine("Cross (A) do it · Square (X) start this step again · D-pad left back");
        telemetry.addLine(String.format(Locale.US, "A (64T) %.3f V · B (44T) %.3f V", voltsA, voltsB));
        telemetry.addLine(loopTimer.summary());
        if (panelsProblem != null) telemetry.addLine("Panels: " + panelsProblem);
        telemetry.update();
    }

    private void publish() {
        if (panels == null) return;
        try {
            panels.addData("tas_step", setup.step().ordinal() + 1);
            panels.addData("tas_a_volts", voltsA);
            panels.addData("tas_b_volts", voltsB);
            if (setup.step() == AbsoluteSetup.Step.DONE) {
                List<String> paste = setup.pasteLines(robotName, date);
                for (int i = 0; i < paste.size(); i++) {
                    panels.addData(String.format(Locale.US, "tas_paste_%02d", i + 1), paste.get(i));
                }
            }
            panels.addData("loop_ms", loopTimer.lastMs());
            panels.update();
        } catch (RuntimeException ignored) {
            // Telemetry never takes the bench down.
        }
    }

    /** Null if the active config carries the two encoders; otherwise what to activate. Sets the paste lines' robot. */
    private String checkActiveConfig() {
        String active;
        try {
            active = ActiveConfig.name();
        } catch (RuntimeException e) {
            return "Could not read the active configuration: " + e.getMessage();
        }
        RobotIdentity identity = RobotIdentity.fromConfigName(active);
        if (identity != null) robotName = identity.name();
        if (identity != null && hasTurretEncoders(identity)) return null;
        return "Active config is \"" + (active == null ? "none" : active)
                + "\", which has no turret absolute encoders. Driver Station: Configure Robot -> robot_19429"
                + " or robot_20245 -> Activate.";
    }

    static boolean hasTurretEncoders(RobotIdentity identity) {
        boolean a = false;
        boolean b = false;
        for (DeviceNames.Device device : identity.devices) {
            a |= DeviceNames.TURRET_ABS_A.equals(device.name);
            b |= DeviceNames.TURRET_ABS_B.equals(device.name);
        }
        return a && b;
    }
}
