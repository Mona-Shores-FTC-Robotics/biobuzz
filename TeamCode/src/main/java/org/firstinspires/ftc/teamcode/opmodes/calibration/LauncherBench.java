package org.firstinspires.ftc.teamcode.opmodes.calibration;

import com.bylazar.configurables.PanelsConfigurables;
import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.hardware.ActiveConfig;
import org.firstinspires.ftc.teamcode.hardware.RobotIdentity;
import org.firstinspires.ftc.teamcode.subsystems.FeederSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.LauncherSubsystem;
import org.firstinspires.ftc.teamcode.util.LoopTimer;

import java.util.Locale;

/**
 * The launcher on a bench: the real {@link LauncherSubsystem} and {@link FeederSubsystem}, driven
 * from one gamepad, on a spare Control Hub with {@code robot_launcher_bench} active. No
 * {@code Robot}, because a bench has no drivetrain to build; the same two classes the robot runs,
 * so every number found here is a number for the robot.
 *
 * <p>The rig is {@link LauncherSubsystem.Layout#Y_CABLED}: both flywheel motors on one port through
 * a Y cable, and both feeder motors on another. Software sees one launcher motor and one feeder
 * motor; both wheels get the same signal, so there is no per-wheel trim here and the trim buttons
 * are shown as unused. The wheels turning opposite ways comes from mounting and wiring, not code:
 * if both turn the same way, that is a wiring fix.
 *
 * <h2>Gamepad 1, PlayStation names (Xbox in brackets)</h2>
 *
 * <ul>
 *   <li>Cross (A): spin up both wheels. Circle (B): spin down, wheels coast.</li>
 *   <li>D-pad up / down: launcher target +100 / −100 ticks per second.</li>
 *   <li>D-pad right / left: feeder power +0.1 / −0.1 (0 to 1).</li>
 *   <li>L1 / L2: left wheel trim −100 / +100. R1 / R2: right wheel trim −100 / +100.</li>
 *   <li>Square (X): feeder on; press again for off. Circle also turns it off. Triangle (Y) held:
 *       reverse the feeder, and cancels the toggle.</li>
 *   <li>Share (Back): zero both trims.</li>
 * </ul>
 *
 * <p><b>No interlock on the bench.</b> Basic Drive stops the feeder from pushing a piece into wheels
 * that are still spinning up; here the person holding the gamepad is that check, and the point of a
 * bench is to run each motor on its own. {@link #feedOnlyAtSpeed} is off by default and in Panels
 * for anyone who wants the robot's behaviour back.
 *
 * <p>Standalone diagnostics, like {@code OctoQuadBench}: the exception CLAUDE.md allows for rigs.
 */
@TeleOp(name = "Launcher Bench", group = "Diagnostics")
@Configurable
public class LauncherBench extends OpMode {

    /** Off: Square feeds whenever held. On: a feed starts only once the launcher is at speed, as on the robot. */
    public static boolean feedOnlyAtSpeed = false;

    private static final double STEP_TICKS_PER_SECOND = 100.0;
    private static final double STEP_FEED_POWER = 0.1;
    private static final double TRIGGER_PRESSED = 0.5;

    private LauncherSubsystem launcher;
    private FeederSubsystem feeder;
    private Display display;
    private final LoopTimer loopTimer = new LoopTimer();
    private TelemetryManager panels;
    private String panelsProblem;
    private String configWarning;
    private String fault;

    private boolean aWas, bWas, xWas, upWas, downWas, leftWas, rightWas, l1Was, l2Was, r1Was, r2Was, backWas;
    /** Square's toggle: the feeder has been asked to run until Square, Circle or Triangle says otherwise. */
    private boolean feedToggled;

    @Override
    public void init() {
        for (LynxModule hub : hardwareMap.getAll(LynxModule.class)) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }
        configWarning = checkActiveConfig();
        display = new Display(telemetry);
        try {
            launcher = LauncherSubsystem.yCabled(hardwareMap);
            feeder = new FeederSubsystem(hardwareMap);
            launcher.initialize();
            feeder.initialize();
        } catch (RuntimeException e) {
            fault = "Hardware: " + (e.getMessage() == null ? e.toString() : e.getMessage());
        }
        try {
            panels = PanelsTelemetry.INSTANCE.getTelemetry();
            if (panels == null) panelsProblem = "PanelsTelemetry returned no telemetry manager";
        } catch (RuntimeException | LinkageError e) {
            panels = null;
            panelsProblem = "not available at init: " + e;
        }
        show();
    }

    @Override
    public void init_loop() {
        // Read-only before PLAY: the wheels stay still, the screen shows the config and the numbers.
        show();
    }

    @Override
    public void loop() {
        loopTimer.lap();
        if (fault == null) {
            readGamepad();
            launcher.update();
            feeder.update();
        }
        show();
        publish();
    }

    @Override
    public void stop() {
        if (launcher != null) launcher.stop();
        if (feeder != null) feeder.stop();
    }

    private void readGamepad() {
        boolean a = gamepad1.a, b = gamepad1.b, x = gamepad1.x, up = gamepad1.dpad_up, down = gamepad1.dpad_down;
        boolean dleft = gamepad1.dpad_left, dright = gamepad1.dpad_right;
        boolean l1 = gamepad1.left_bumper, l2 = gamepad1.left_trigger > TRIGGER_PRESSED;
        boolean r1 = gamepad1.right_bumper, r2 = gamepad1.right_trigger > TRIGGER_PRESSED;
        boolean back = gamepad1.back;

        if (a && !aWas) launcher.spinUp();
        if (b && !bWas) {
            launcher.spinDown();
            feedToggled = false;
        }
        if (x && !xWas) feedToggled = !feedToggled;
        if (gamepad1.y) feedToggled = false;
        if (up && !upWas) nudge(STEP_TICKS_PER_SECOND, 0, 0);
        if (down && !downWas) nudge(-STEP_TICKS_PER_SECOND, 0, 0);
        if (dright && !rightWas) nudgeFeed(STEP_FEED_POWER);
        if (dleft && !leftWas) nudgeFeed(-STEP_FEED_POWER);
        boolean trims = launcher.layout() == LauncherSubsystem.Layout.TWO_MOTORS;
        if (trims && l1 && !l1Was) nudge(0, -STEP_TICKS_PER_SECOND, 0);
        if (trims && l2 && !l2Was) nudge(0, STEP_TICKS_PER_SECOND, 0);
        if (trims && r1 && !r1Was) nudge(0, 0, -STEP_TICKS_PER_SECOND);
        if (trims && r2 && !r2Was) nudge(0, 0, STEP_TICKS_PER_SECOND);
        if (back && !backWas) {
            LauncherSubsystem.leftTrimTicksPerSecond = 0.0;
            LauncherSubsystem.rightTrimTicksPerSecond = 0.0;
            refreshPanels();
        }
        aWas = a; bWas = b; xWas = x; upWas = up; downWas = down; leftWas = dleft; rightWas = dright;
        l1Was = l1; l2Was = l2; r1Was = r1; r2Was = r2; backWas = back;

        // Decided every loop: Square's toggle stands in for a held feed button; Triangle is held.
        // No gate unless feedOnlyAtSpeed is switched on in Panels.
        FeederSubsystem.State request = FeederSubsystem.requestFor(feedToggled, gamepad1.y, launcher.atSpeed(),
                feedOnlyAtSpeed, feeder.state() == FeederSubsystem.State.FEED);
        switch (request) {
            case FEED: feeder.feed(); break;
            case REVERSE: feeder.reverse(); break;
            default: feeder.idle();
        }
    }

    private void nudge(double common, double leftTrim, double rightTrim) {
        LauncherSubsystem.targetTicksPerSecond = Math.max(0.0, LauncherSubsystem.targetTicksPerSecond + common);
        LauncherSubsystem.leftTrimTicksPerSecond += leftTrim;
        LauncherSubsystem.rightTrimTicksPerSecond += rightTrim;
        refreshPanels();
    }

    private void nudgeFeed(double delta) {
        double p = FeederSubsystem.feedPower + delta;
        FeederSubsystem.feedPower = Math.round(Math.max(0.0, Math.min(1.0, p)) * 100.0) / 100.0;
        refreshPanels();
    }

    /** Panels re-sends a class's values only when told to; without this the page shows stale numbers after a nudge. */
    private void refreshPanels() {
        try {
            PanelsConfigurables.refreshClass(LauncherSubsystem.class);
            PanelsConfigurables.refreshClass(FeederSubsystem.class);
        } catch (RuntimeException ignored) {
            // A dashboard problem must not stop the bench.
        }
    }

    private void show() {
        boolean yCabled = launcher != null && launcher.layout() == LauncherSubsystem.Layout.Y_CABLED;
        telemetry.addLine("<b>Launcher Bench</b>  <small>Cross spin up · Circle spin down · D-pad up/down launcher speed · "
                + "D-pad right/left feeder power · "
                + (yCabled ? "Y-cabled: trims unused" : "L1/L2 left trim · R1/R2 right trim · Share zero trims")
                + " · Square feeder on/off" + (feedOnlyAtSpeed ? " (starts once at speed)" : "")
                + " · Triangle reverse</small>");
        if (configWarning != null) display.status("Config", Display.Level.WARN, configWarning);
        if (fault != null) {
            display.status("Bench", Display.Level.FAULT, fault);
        } else {
            launcher.describe(display);
            feeder.describe(display);
            display.line(yCabled
                    ? String.format(Locale.US, "Launcher target %.0f ticks/s (one signal to both wheels) · feeder power %.2f",
                            launcher.target(), FeederSubsystem.feedPower)
                    : String.format(Locale.US, "Targets: common %.0f · left trim %+.0f · right trim %+.0f ticks/s",
                            launcher.target(), LauncherSubsystem.leftTrimTicksPerSecond,
                            LauncherSubsystem.rightTrimTicksPerSecond));
        }
        display.line(loopTimer.summary());
        if (panelsProblem != null) display.line("Panels: " + panelsProblem);
        telemetry.update();
    }

    private void publish() {
        if (panels == null || fault != null) return;
        try {
            if (launcher.layout() == LauncherSubsystem.Layout.Y_CABLED) {
                // One motor to software: one target, one speed, one current (both motors together).
                panels.addData("launcher_target_tps", launcher.leftTarget());
                panels.addData("launcher_tps", launcher.leftTicksPerSecond());
                panels.addData("launcher_amps", launcher.leftCurrentAmps());
                panels.addData("launcher_rpm", launcher.leftRpm()); // NaN until ticksPerRev is set
            } else {
                panels.addData("left_target_tps", launcher.leftTarget());
                panels.addData("right_target_tps", launcher.rightTarget());
                panels.addData("left_tps", launcher.leftTicksPerSecond());
                panels.addData("right_tps", launcher.rightTicksPerSecond());
                panels.addData("left_amps", launcher.leftCurrentAmps());
                panels.addData("right_amps", launcher.rightCurrentAmps());
            }
            panels.addData("at_speed", launcher.atSpeed() ? 1.0 : 0.0);
            panels.addData("feeder_power", feeder.power());
            panels.addData("feeder_tps", feeder.ticksPerSecond());
            panels.addData("feeder_amps", feeder.currentAmps());
            panels.addData("feeder_rpm", feeder.rpm());
            panels.addData("loop_ms", loopTimer.lastMs());
            panels.update();
        } catch (RuntimeException ignored) {
            // Telemetry never takes the bench down.
        }
    }

    private static String checkActiveConfig() {
        String wanted = RobotIdentity.LAUNCHER_BENCH.configName;
        String active;
        try {
            active = ActiveConfig.name();
        } catch (RuntimeException e) {
            return "Could not read the active configuration: " + e.getMessage();
        }
        if (wanted.equals(active)) return null;
        return "Active config is \"" + (active == null ? "none" : active) + "\", not \"" + wanted
                + "\". Driver Station: Configure Robot -> " + wanted + " -> Activate.";
    }
}
