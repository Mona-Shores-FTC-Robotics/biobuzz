package org.firstinspires.ftc.teamcode.opmodes;

import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.qualcomm.robotcore.eventloop.opmode.Utility;

import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.controls.IntakeToggle;
import org.firstinspires.ftc.teamcode.subsystems.FeedGateSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.LauncherSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.PrefeedSubsystem;

import java.util.Locale;

/**
 * The launch test for one person with one controller: drive gently, tap the intake on, spin the
 * launcher up, hold fire, and watch the shots count. The whole path runs on the real {@code Robot}
 * with the same subsystems as the match TeleOp, so what is tuned here is what ships.
 *
 * <p>It replaces the Full Path Bench (31 Oct 2026). That one needed R2 held while working the
 * launcher and fire, too much for one tester, and its gate and prefeed hold buttons were bumped by
 * accident and froze the prefeed. So:
 *
 * <ul>
 *   <li><b>Normal layer</b>, all gamepad 1: sticks drive at a gentle fixed speed; R2 <em>taps</em>
 *       the intake on and off ({@link IntakeToggle}); R1 held fires, which since #320 runs the feeder,
 *       prefeed, gate, stars and roller together; L2 held unjams and wins; Circle spins the launcher
 *       up and down; D-pad up and down trim the launch speed by {@link #SPEED_STEP_TPS}.</li>
 *   <li><b>Setup layer</b>, only while L1 is held: Square cycles the gate between following the
 *       feeder and holding BLOCKING or FEEDING, Triangle cycles the prefeed's holds, and the D-pad
 *       nudges the gate's two positions. A hold stays until it is cycled back to follow, and the page
 *       says so in red while one is on.</li>
 * </ul>
 *
 * <p><b>There is no interlock:</b> R1 fires whether or not the launcher is at speed. The trim is
 * this run's only: {@code LauncherSubsystem.targetTicksPerSecond} is not changed, so write a speed
 * that works back into that file.
 *
 * <p><b>Setting the gate's two positions</b> (both robots have them measured now; this is for a
 * rebuilt gate): hold L1. The first D-pad press on an unmeasured end sets both ends to 0.50 and moves
 * the gate there, so be ready for that. Then L1 + Square holds the gate at either end and L1 + D-pad
 * nudges that end until the plate sits on its stop. Write the two numbers into
 * {@code FeedGateSubsystem} with a comment saying which robot. Panels edits are lost on restart.
 *
 * <p>Nothing moves before PLAY, as everywhere ({@code Subsystem}): the bindings are polled only
 * after PLAY and the sticks are read only in {@link #onLoop()}.
 */
@Utility(name = "Launch Test", description = "One person, one controller: drive, intake on/off, fire, launch speed on the D-pad; setup tools behind L1")
public class LaunchTest extends RobotOpMode {

    /** How far a trigger must be pressed to count as held. */
    private static final double TRIGGER = 0.5;

    /** Stick signs as Basic Drive has them: push away is forward, Pedro's +strafe is left, +turn is CCW. */
    private static final double FORWARD_SIGN = -1.0;
    private static final double STRAFE_SIGN = -1.0;
    private static final double TURN_SIGN = -1.0;
    private static final double STICK_DEADBAND = 0.05;

    /** Stick multiplier. Gentle: this is for lining up a shot, not racing. */
    private static final double DRIVE_SPEED = 0.4;

    /** Launch speed change per D-pad up or down press, ticks per second. */
    static final double SPEED_STEP_TPS = 50.0;

    /**
     * Gate position change per L1 + D-pad up / down press, servo range 0 to 1. A speed servo covers
     * 270°, so 0.02 is about 5°. Constants, not Panels knobs: a bench's tunables are not a match's
     * ({@code TunablesTest}).
     */
    private static final double COARSE_STEP = 0.02;

    /** Gate position change per L1 + D-pad right / left press. */
    private static final double FINE_STEP = 0.005;

    /** Where the first D-pad press puts an unmeasured gate position: mid-range, so it can go either way. */
    private static final double START = 0.5;

    private TelemetryManager panels;
    private final IntakeToggle intakeToggle = new IntakeToggle();
    /** Added to {@code LauncherSubsystem.targetTicksPerSecond} for this run, ticks per second. */
    private double trimTps;
    /** L1 + Square: 0 follows the feeder, 1 holds the gate BLOCKING, 2 holds it FEEDING. */
    private int gateHold;
    /** L1 + Triangle: 0 follows the intake and feeder, then holds IN, OUT, IDLE. */
    private int prefeedHold;

    @Override
    protected void onInit() {
        try {
            panels = PanelsTelemetry.INSTANCE.getTelemetry();
        } catch (RuntimeException | LinkageError e) {
            panels = null; // Panels is a window onto the robot, not part of it.
        }
        driver.note("Left stick / right stick X", "Drive, gentle fixed speed");
        driver.note("R2 (tap)", "Intake on / off");
        driver.note("R1 (hold)", "Fire: feeder, prefeed, gate, stars and roller. NO interlock");
        driver.note("L2 (hold)", "Unjam: everything backs out (wins)");
        driver.when("Circle (B)", "Launcher: spin up / spin down", () -> gamepad1.b)
                .onPress(this::toggleLauncher);
        driver.when("D-pad up", "Launch speed +" + (int) SPEED_STEP_TPS + " ticks/s",
                () -> !gamepad1.left_bumper && gamepad1.dpad_up).onPress(() -> trim(+1));
        driver.when("D-pad down", "Launch speed -" + (int) SPEED_STEP_TPS + " ticks/s",
                () -> !gamepad1.left_bumper && gamepad1.dpad_down).onPress(() -> trim(-1));

        driver.note("L1 (hold)", "SETUP layer: the buttons below work only while L1 is held");
        driver.when("L1 + Square (X)", "Gate: follow feeder / hold BLOCKING / hold FEEDING",
                () -> gamepad1.left_bumper && gamepad1.x).onPress(this::cycleGate);
        driver.when("L1 + Triangle (Y)", "Prefeed: follow / hold IN / hold OUT / hold IDLE",
                () -> gamepad1.left_bumper && gamepad1.y).onPress(this::cyclePrefeed);
        driver.when("L1 + D-pad up", "Gate: shown position +" + COARSE_STEP + " (first press on an unmeasured end sets 0.50 and moves it)",
                () -> gamepad1.left_bumper && gamepad1.dpad_up).onPress(() -> nudge(COARSE_STEP));
        driver.when("L1 + D-pad down", "Gate: shown position -" + COARSE_STEP,
                () -> gamepad1.left_bumper && gamepad1.dpad_down).onPress(() -> nudge(-COARSE_STEP));
        driver.when("L1 + D-pad right", "Gate: shown position +" + FINE_STEP,
                () -> gamepad1.left_bumper && gamepad1.dpad_right).onPress(() -> nudge(FINE_STEP));
        driver.when("L1 + D-pad left", "Gate: shown position -" + FINE_STEP,
                () -> gamepad1.left_bumper && gamepad1.dpad_left).onPress(() -> nudge(-FINE_STEP));
    }

    @Override
    protected void onInitLoop() {
        show(false);
    }

    @Override
    protected void onLoop() {
        robot.drive.drive(
                deadband(gamepad1.left_stick_y) * FORWARD_SIGN * DRIVE_SPEED,
                deadband(gamepad1.left_stick_x) * STRAFE_SIGN * DRIVE_SPEED,
                deadband(gamepad1.right_stick_x) * TURN_SIGN * DRIVE_SPEED);
        boolean unjam = gamepad1.left_trigger > TRIGGER;
        boolean fire = gamepad1.right_bumper;
        // A tap of R2 toggles; the toggle is always enabled here (it is the point of a solo test).
        boolean intakeOn = intakeToggle.update(true, gamepad1.right_trigger > TRIGGER);
        // The subsystems update after this: the intake, prefeed and gate read these requests this loop.
        if (unjam) {
            robot.intake.eject();
            robot.feeder.reverse();
        } else {
            if (intakeOn) robot.intake.intake(); else robot.intake.idle();
            if (fire) robot.feeder.feed(); else robot.feeder.idle();
        }
        show(gamepad1.left_bumper);
    }

    // ------------------------------------------------------------ launcher

    private void toggleLauncher() {
        if (robot.launcher.isSpinning()) {
            robot.launcher.spinDown();
            log.event("launcher: off");
        } else {
            robot.launcher.spinUp(launchSpeed());
            log.event(String.format(Locale.US, "launcher: on at %.0f ticks/s", launchSpeed()));
        }
    }

    /** One D-pad press: the trim moves a step and a spinning launcher takes the new speed at once. */
    private void trim(int direction) {
        trimTps = trimmed(trimTps, direction * SPEED_STEP_TPS, LauncherSubsystem.targetTicksPerSecond);
        if (robot.launcher.isSpinning()) {
            robot.launcher.spinUp(launchSpeed());
        }
        log.event(String.format(Locale.US, "launcher: trim %+.0f, launch speed %.0f ticks/s", trimTps, launchSpeed()));
    }

    /** The launch speed in force: the configured speed plus this run's trim, never below zero. */
    private double launchSpeed() {
        return launchSpeedFor(LauncherSubsystem.targetTicksPerSecond, trimTps);
    }

    /**
     * The trim after a step of {@code delta}, clamped so the launch speed ({@code base + trim}) never
     * goes below zero. A base that is NaN or negative counts as zero. Pure, so a test can check it.
     */
    static double trimmed(double trim, double delta, double base) {
        double b = base > 0.0 ? base : 0.0;
        return Math.max(-b, trim + delta);
    }

    /** {@code base + trim}, never below zero; a NaN or negative base counts as zero. Pure. */
    static double launchSpeedFor(double base, double trim) {
        double b = base > 0.0 ? base : 0.0;
        return Math.max(0.0, b + trim);
    }

    // ------------------------------------------------------------ setup layer

    private void cycleGate() {
        gateHold = (gateHold + 1) % 3;
        if (gateHold == 0) {
            robot.feedGate.follow();
        } else {
            robot.feedGate.hold(gateHold == 1 ? FeedGateSubsystem.State.BLOCKING : FeedGateSubsystem.State.FEEDING);
        }
    }

    private void cyclePrefeed() {
        prefeedHold = (prefeedHold + 1) % 4;
        switch (prefeedHold) {
            case 1:
                robot.prefeed.override(PrefeedSubsystem.State.IN);
                break;
            case 2:
                robot.prefeed.override(PrefeedSubsystem.State.OUT);
                break;
            case 3:
                robot.prefeed.override(PrefeedSubsystem.State.IDLE);
                break;
            default:
                robot.prefeed.follow();
        }
    }

    /**
     * Move the position of the end the gate is at now (BLOCKING or FEEDING, held or following the
     * feeder). An unmeasured end starts at {@link #START}, and so does the other one if it is also
     * unmeasured, because the gate does not move until both are set.
     */
    private void nudge(double delta) {
        boolean blocking = robot.feedGate.state() == FeedGateSubsystem.State.BLOCKING;
        double now = blocking ? FeedGateSubsystem.blockingPosition : FeedGateSubsystem.feedingPosition;
        double next = nudged(now, delta);
        if (blocking) {
            FeedGateSubsystem.blockingPosition = next;
            if (Double.isNaN(FeedGateSubsystem.feedingPosition)) FeedGateSubsystem.feedingPosition = START;
        } else {
            FeedGateSubsystem.feedingPosition = next;
            if (Double.isNaN(FeedGateSubsystem.blockingPosition)) FeedGateSubsystem.blockingPosition = START;
        }
    }

    /** A position moved by {@code delta} and kept in the servo's 0 to 1; unmeasured (NaN) starts at {@link #START}. Pure. */
    static double nudged(double position, double delta) {
        if (Double.isNaN(position)) return START;
        return Math.max(0.0, Math.min(1.0, position + delta));
    }

    // ------------------------------------------------------------ Match page and Panels

    /** The one page: launcher, intake, transfer, battery, and the setup layer's state. */
    private void show(boolean setupLayer) {
        display.section("Launch Test");
        boolean spinning = robot.launcher.isSpinning();
        display.status("Launch speed", spinning && !robot.launcher.atSpeed() ? Display.Level.WARN : Display.Level.OK,
                String.format(Locale.US, "%.0f ticks/s (%.0f %+.0f) · D-pad up/down ±%.0f · %s",
                        launchSpeed(), LauncherSubsystem.targetTicksPerSecond, trimTps, SPEED_STEP_TPS,
                        !spinning ? "launcher OFF (Circle)" : robot.launcher.atSpeed() ? "AT SPEED" : "spinning up"));
        robot.launcher.describe(display);
        display.line("Intake toggle: " + (intakeToggle.on() ? "ON (R2 to stop)" : "off (R2 to start)"));
        robot.intake.describe(display);
        robot.prefeed.describe(display);
        robot.feedGate.describe(display);
        robot.feeder.describe(display);
        double volts = batteryVolts();
        display.line(Double.isNaN(volts) ? "Battery: no voltage sensor" : String.format(Locale.US, "Battery %.2f V", volts));

        if (gateHold != 0 || prefeedHold != 0) {
            display.status("HOLD ACTIVE", Display.Level.FAULT,
                    (gateHold == 0 ? "" : "gate held " + (gateHold == 1 ? "BLOCKING" : "FEEDING") + " · ")
                            + (prefeedHold == 0 ? "" : "prefeed held " + prefeedName(prefeedHold) + " · ")
                            + "cycle back to follow with L1 + Square / L1 + Triangle");
        }
        if (setupLayer) {
            display.status("SETUP (L1 held)", Display.Level.WARN,
                    "Square gate hold · Triangle prefeed hold · D-pad nudges the gate end shown");
        }
        display.line(String.format(Locale.US, "Gate positions: blocking %s · feeding %s",
                position(FeedGateSubsystem.blockingPosition), position(FeedGateSubsystem.feedingPosition)));
        display.status("Interlock", Display.Level.WARN, "none: R1 fires whether or not the launcher is at speed");
        display.line("<small>Sticks drive · R2 tap intake on/off · R1 fire · L2 unjam · Circle launcher · "
                + "D-pad launch speed · hold L1 for setup · Share (Back): controls page</small>");
        publish(setupLayer);
    }

    private static String prefeedName(int hold) {
        return hold == 1 ? "IN" : hold == 2 ? "OUT" : "IDLE";
    }

    private static String position(double value) {
        return Double.isNaN(value) ? "NaN (not set)" : String.format(Locale.US, "%.3f", value);
    }

    /** The path's numbers to Panels, flat keys, one update per loop. Never takes the robot down. */
    private void publish(boolean setupLayer) {
        if (panels == null) return;
        try {
            panels.addData("launch_target_tps", launchSpeed());
            panels.addData("launch_trim_tps", trimTps);
            panels.addData("left_tps", robot.launcher.leftTicksPerSecond());
            panels.addData("right_tps", robot.launcher.rightTicksPerSecond());
            panels.addData("at_speed", robot.launcher.atSpeed() ? 1.0 : 0.0);
            panels.addData("shots", robot.launcher.shots());
            panels.addData("intake_toggle", intakeToggle.on() ? 1.0 : 0.0);
            panels.addData("intake_tps", robot.intake.ticksPerSecond());
            panels.addData("intake_amps", robot.intake.currentAmps());
            panels.addData("intake_feed_assist", robot.intake.feedAssist() ? 1.0 : 0.0);
            panels.addData("prefeed_power", robot.prefeed.power());
            panels.addData("gate_feeding", robot.feedGate.state() == FeedGateSubsystem.State.FEEDING ? 1.0 : 0.0);
            panels.addData("gate_blocking_position", FeedGateSubsystem.blockingPosition);
            panels.addData("gate_feeding_position", FeedGateSubsystem.feedingPosition);
            panels.addData("feeder_power", robot.feeder.power());
            panels.addData("feeder_amps", robot.feeder.currentAmps());
            panels.addData("battery_volts", batteryVolts());
            panels.addData("setup_layer", setupLayer ? 1.0 : 0.0);
            panels.addData("loop_ms", loopTimer.lastMs());
            panels.update();
        } catch (RuntimeException ignored) {
            // A Panels hiccup must never take the robot down.
        }
    }

    private static double deadband(double value) {
        return Math.abs(value) < STICK_DEADBAND ? 0.0 : value;
    }
}
