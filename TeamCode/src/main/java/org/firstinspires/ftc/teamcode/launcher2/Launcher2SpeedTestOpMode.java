package org.firstinspires.ftc.teamcode.launcher2;

import com.bylazar.configurables.PanelsConfigurables;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.hardware.ActiveConfig;
import org.firstinspires.ftc.teamcode.hardware.RobotIdentity;
import org.firstinspires.ftc.teamcode.launcher2.config.Launcher2Config;

import java.util.Locale;

/**
 * Speed test for the two-wheel pinch launcher rig on Control Hub FTC-EoM3.
 *
 * <p>Same controls and the same RPM steps as the three-wheel
 * <b>Flywheel Speed Test</b>, so a number found on one rig means the same thing
 * on the other. Everything with a number is in Panels under {@code Launcher2}.
 *
 * <h2>Gamepad 1</h2>
 * <table>
 *   <tr><td>A</td><td>spin up</td></tr>
 *   <tr><td>B</td><td>stop (wheels coast down)</td></tr>
 *   <tr><td>Dpad up / down</td><td>target RPM by the fine step (25 default)</td></tr>
 *   <tr><td>Dpad right / left</td><td>target RPM by the coarse step (100 default)</td></tr>
 * </table>
 *
 * <h2>Before the first spin</h2>
 * <ol>
 *   <li>On the rig's Driver Station activate {@code robot_launcher_rig}. This
 *       OpMode says so at init if another configuration is active.</li>
 *   <li>Spin up at the default 1500 RPM and <b>watch the wheels</b>. Both must
 *       push the game piece forward. If both push it backward, tick
 *       {@code motor.reversed}. If they turn the same way as each other, that
 *       is the wiring, not the code.</li>
 *   <li>Then tune as on the three-wheel rig: kV with kP at 0, then kS, then a
 *       little kP.</li>
 * </ol>
 *
 * <p>The RPM readout is an absolute value, so it cannot show direction; see
 * {@link Launcher2}.
 */
@TeleOp(name = "Launcher2 Speed Test", group = "Shooter")
public class Launcher2SpeedTestOpMode extends OpMode {

    /** Bump when behaviour changes; if the rig shows an older one, redeploy. */
    private static final String BUILD = "launcher2 spike v1 (26 Sep)";

    private Launcher2 launcher;
    private TelemetryManager panels;

    /** Null when the active config is the rig's; otherwise what to tell the operator. */
    private String configWarning;

    private double loopMs = 0.0;
    private long lastLoopStartNs = 0L;

    private boolean prevA = false;
    private boolean prevB = false;
    private boolean prevDpadUp = false;
    private boolean prevDpadDown = false;
    private boolean prevDpadRight = false;
    private boolean prevDpadLeft = false;
    private boolean prevAtSpeed = false;

    @Override
    public void init() {
        for (LynxModule hub : hardwareMap.getAll(LynxModule.class)) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }

        configWarning = checkActiveConfig();

        launcher = new Launcher2(hardwareMap);
        launcher.initialize();

        try {
            panels = PanelsTelemetry.INSTANCE.getTelemetry();
        } catch (Exception ignored) {
            panels = null;
        }
        showInitScreen();
    }

    @Override
    public void init_loop() {
        launcher.periodic();
        showInitScreen();
    }

    @Override
    public void loop() {
        long now = System.nanoTime();
        if (lastLoopStartNs != 0L) {
            loopMs = (now - lastLoopStartNs) / 1e6;
        }
        lastLoopStartNs = now;

        handleGamepad();
        launcher.periodic();
        publishDriverStation();
        publishPanels();
    }

    @Override
    public void stop() {
        if (launcher != null) {
            launcher.stop();
            launcher.periodic();
        }
    }

    /**
     * An OpMode cannot choose the active configuration — that is a setting on
     * the Robot Controller. The next best thing is to say so, by name, when it
     * is the wrong one.
     */
    private static String checkActiveConfig() {
        String wanted = RobotIdentity.LAUNCHER_RIG.configName;
        String active;
        try {
            active = ActiveConfig.name();
        } catch (RuntimeException e) {
            return "Could not read the active configuration: " + e.getMessage();
        }
        if (wanted.equals(active)) {
            return null;
        }
        return "Active config is \"" + (active == null ? "none" : active) + "\", not \"" + wanted
                + "\". Driver Station: Configure Robot -> " + wanted + " -> Activate.";
    }

    private void showInitScreen() {
        telemetry.addLine("Launcher2 Speed Test ready.  Build: " + BUILD);
        telemetry.addLine("A = spin up, B = stop, dpad = target RPM.");
        telemetry.addLine("Watch the wheels on the first spin: the RPM cannot show direction.");
        reportProblems();
        telemetry.update();
    }

    private void handleGamepad() {
        Launcher2Config.Target target = Launcher2.config.target;

        if (gamepad1.a && !prevA) {
            launcher.spinUp();
        }
        if (gamepad1.b && !prevB) {
            launcher.stop();
        }
        if (gamepad1.dpad_up && !prevDpadUp) {
            nudgeTarget(target.fineStepRpm);
        }
        if (gamepad1.dpad_down && !prevDpadDown) {
            nudgeTarget(-target.fineStepRpm);
        }
        if (gamepad1.dpad_right && !prevDpadRight) {
            nudgeTarget(target.coarseStepRpm);
        }
        if (gamepad1.dpad_left && !prevDpadLeft) {
            nudgeTarget(-target.coarseStepRpm);
        }

        prevA = gamepad1.a;
        prevB = gamepad1.b;
        prevDpadUp = gamepad1.dpad_up;
        prevDpadDown = gamepad1.dpad_down;
        prevDpadRight = gamepad1.dpad_right;
        prevDpadLeft = gamepad1.dpad_left;
    }

    private void nudgeTarget(double deltaRpm) {
        Launcher2Config.Target target = Launcher2.config.target;
        target.targetRpm = TargetSteps.step(target.targetRpm, deltaRpm, target.maxRpm);
        // Panels only re-sends values on connect or when told to; without this
        // the page keeps showing the old target after a d-pad press.
        try {
            PanelsConfigurables.refreshClass(Launcher2.class);
        } catch (RuntimeException ignored) {
            // A dashboard problem must not stop the rig.
        }
    }

    private void reportProblems() {
        if (configWarning != null) {
            telemetry.addData("WRONG CONFIG", configWarning);
        }
        if (!launcher.isConnected()) {
            telemetry.addData("NOT IN ROBOT CONFIG", "\"%s\"", launcher.getMotorName());
        }
    }

    private void publishDriverStation() {
        Launcher2Config.Target target = Launcher2.config.target;

        telemetry.addData("Build", BUILD);
        telemetry.addLine(launcher.isSpinning() ? ">>> SPINNING  (B to stop)" : "--- STOPPED   (A to spin up)");
        telemetry.addData("Target RPM", "%.0f   dpad U/D %.0f, R/L %.0f",
                target.targetRpm, target.fineStepRpm, target.coarseStepRpm);

        double voltage = launcher.getBatteryVoltage();
        telemetry.addData("Battery", Double.isNaN(voltage)
                ? "no sensor"
                : String.format(Locale.US, "%.2f V  (power x%.2f)", voltage, launcher.getVoltageMultiplier()));

        telemetry.addLine();
        telemetry.addData("State", stateLabel());
        telemetry.addData("RPM", "%.0f  (error %.0f)", launcher.getMeasuredRpm(), launcher.getErrorRpm());
        telemetry.addData("Power", "%.2f  (ff %.2f + fb %.2f)",
                launcher.getAppliedPower(), launcher.getFeedforwardPower(), launcher.getFeedbackPower());
        double spinUpMs = launcher.getLastSpinUpMs();
        telemetry.addData("Spin-up", Double.isNaN(spinUpMs) ? "--" : String.format(Locale.US, "%.0f ms", spinUpMs));
        telemetry.addData("Direction", launcher.isReversed() ? "REV" : "FWD");

        boolean atSpeed = launcher.isAtSpeed();
        if (atSpeed && !prevAtSpeed) {
            gamepad1.rumble(200);
        }
        prevAtSpeed = atSpeed;

        reportProblems();
    }

    private String stateLabel() {
        if (!launcher.isConnected()) {
            return "[ n/c ]";
        }
        if (launcher.getCommandedRpm() <= 0.0) {
            return "[stop ]";
        }
        return launcher.isAtSpeed() ? "[READY]" : "[spin ]";
    }

    /** Numeric series via addData so Panels can graph them. */
    private void publishPanels() {
        if (panels == null) {
            return;
        }
        try {
            panels.addData("rpm", launcher.getMeasuredRpm());
            panels.addData("target", launcher.getCommandedRpm());
            panels.addData("error", launcher.getErrorRpm());
            panels.addData("power", launcher.getAppliedPower());
            panels.addData("ff", launcher.getFeedforwardPower());
            panels.addData("fb", launcher.getFeedbackPower());
            panels.addData("at_speed", launcher.isAtSpeed() ? 1.0 : 0.0);
            panels.addData("raw_tps", launcher.getRawTicksPerSec());
            panels.addData("spin_up_ms", launcher.getLastSpinUpMs());
            panels.addData("reversed", launcher.isReversed() ? 1.0 : 0.0);

            double amps = launcher.getCurrentAmps();
            double volts = launcher.getBatteryVoltage();
            panels.addData("amps", amps);
            if (Double.isFinite(amps) && Double.isFinite(volts)) {
                panels.addData("watts", amps * volts);
            }
            panels.addData("spinning", launcher.isSpinning() ? 1.0 : 0.0);
            panels.addData("battery_volts", volts);
            panels.addData("voltage_multiplier", launcher.getVoltageMultiplier());
            panels.addData("loop_ms", loopMs);
            panels.debug(launcher.isSpinning() ? "SPINNING" : "STOPPED");
            panels.debug(stateLabel());
            panels.update();
        } catch (Exception ignored) {
            // A Panels hiccup must never take the rig down mid-spin.
        }
    }
}
