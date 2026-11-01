package org.firstinspires.ftc.teamcode.opmodes;

import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.qualcomm.robotcore.eventloop.opmode.Utility;

import org.firstinspires.ftc.teamcode.controls.Action;
import org.firstinspires.ftc.teamcode.controls.Controls;
import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.controls.Input;
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
 * <p><b>The buttons are Basic Drive's operator layout, on gamepad 1</b> (the mentor's call,
 * 1 Nov 2026), so muscle memory carries over: {@link Controls} is built from
 * {@link org.firstinspires.ftc.teamcode.controls.ControlScheme} with gamepad 1 standing in for
 * both pads, and this class never names a button of its own. With the default layout that is
 * Cross launcher on/off, Circle held fires, L1/R1 trim the launch speed, R2 taps the intake on and
 * off ({@link IntakeToggle}: the only change from the match, where it is held), L2 held unjams
 * (and the launcher backs up too, #323), Square toggles the FLOWER stick.
 *
 * <p><b>The setup layer</b> (gate and prefeed holds, gate nudges) sits behind a held modifier: the
 * operator's turret-autoaim button ({@link #SETUP_MODIFIER}, Triangle in the default layout), free
 * here because the turret is unplugged. While it is held, the FLOWER stick's button cycles the gate
 * hold, the launcher button cycles the prefeed hold, and the D-pad nudges the gate's two positions;
 * those two face buttons do nothing else while the modifier is down, so the setup tools cannot be
 * bumped in normal testing. A hold stays until it is cycled back to follow, and the page says so in
 * red while one is on. The D-pad does nothing without the modifier.
 *
 * <p><b>There is no interlock:</b> fire runs the feeder whether or not the launcher is at speed.
 * The trim uses {@code TeleOpConfig.launcherTrimStepTps}, the same Panels knob as Basic Drive, and
 * is this run's only: {@code LauncherSubsystem.targetTicksPerSecond} is not changed, so write a
 * speed that works back into that file.
 *
 * <p><b>Setting the gate's two positions</b> (both robots have them; this is for a rebuilt gate):
 * hold the modifier. The first D-pad press on an unmeasured end sets both ends to 0.50 and moves the
 * gate there, so be ready for that. Then hold the gate at either end and nudge that end until the
 * plate sits on its stop. Write the two numbers into {@code FeedGateSubsystem} with a comment saying
 * which robot. Panels edits are lost on restart.
 *
 * <p>Nothing moves before PLAY, as everywhere ({@code Subsystem}): the bindings are polled only
 * after PLAY and the sticks are read only in {@link #onLoop()}.
 */
@Utility(name = "Launch Test", description = "One person, one controller, the operator's buttons: drive, intake on/off, fire, launch speed; setup tools behind the turret button")
public class LaunchTest extends RobotOpMode {

    /** Stick signs as Basic Drive has them: push away is forward, Pedro's +strafe is left, +turn is CCW. */
    private static final double FORWARD_SIGN = -1.0;
    private static final double STRAFE_SIGN = -1.0;
    private static final double TURN_SIGN = -1.0;
    private static final double STICK_DEADBAND = 0.05;

    /** Stick multiplier. Gentle: this is for lining up a shot, not racing. */
    private static final double DRIVE_SPEED = 0.4;

    /** The operator action whose button is the setup layer's modifier: turret autoaim, idle here. */
    static final Action SETUP_MODIFIER = Action.TURRET_LOCK;
    /** With the modifier held, this action's button cycles the gate hold instead of its own job. */
    static final Action GATE_HOLD_BUTTON = Action.FLOWER_STICK;
    /** With the modifier held, this action's button cycles the prefeed hold instead of its own job. */
    static final Action PREFEED_HOLD_BUTTON = Action.LAUNCHER_TOGGLE;
    /** The operator actions the normal layer uses, all on gamepad 1 here. */
    static final Action[] NORMAL_LAYER = {
            Action.LAUNCHER_TOGGLE, Action.FIRE, Action.LAUNCHER_TRIM_DOWN, Action.LAUNCHER_TRIM_UP,
            Action.OPERATOR_INTAKE, Action.UNJAM, Action.FLOWER_STICK};

    /**
     * Gate position change per modifier + D-pad up / down press, servo range 0 to 1. A speed servo
     * covers 270°, so 0.02 is about 5°. Constants, not Panels knobs: a bench's tunables are not a
     * match's ({@code TunablesTest}).
     */
    private static final double COARSE_STEP = 0.02;

    /** Gate position change per modifier + D-pad right / left press. */
    private static final double FINE_STEP = 0.005;

    /** Where the first D-pad press puts an unmeasured gate position: mid-range, so it can go either way. */
    private static final double START = 0.5;

    private TelemetryManager panels;
    private Controls controls;
    private Input modifier;
    private final IntakeToggle intakeToggle = new IntakeToggle();
    /** Added to {@code LauncherSubsystem.targetTicksPerSecond} for this run, ticks per second. */
    private double trimTps;
    /** Modifier + gate button: 0 follows the feeder, 1 holds the gate BLOCKING, 2 holds it FEEDING. */
    private int gateHold;
    /** Modifier + launcher button: 0 follows the intake and feeder, then holds IN, OUT, IDLE. */
    private int prefeedHold;
    /** The page's one-line legend and the setup line, built once from the layout. */
    private String legend;
    private String setupLine;

    @Override
    protected void onInit() {
        try {
            panels = PanelsTelemetry.INSTANCE.getTelemetry();
        } catch (RuntimeException | LinkageError e) {
            panels = null; // Panels is a window onto the robot, not part of it.
        }
        // Gamepad 1 is both pads, and everything lists on the one Controls page.
        controls = new Controls(TeleOpConfig.flowerPickButton, gamepad1, gamepad1, driver, driver);
        modifier = controls.input(SETUP_MODIFIER);

        driver.note("Left stick / right stick X", "Drive, gentle fixed speed");
        onNormal(Action.LAUNCHER_TOGGLE).onPress(this::toggleLauncher);
        controls.note(Action.FIRE);
        controls.on(Action.LAUNCHER_TRIM_DOWN).onPress(() -> trim(-1));
        controls.on(Action.LAUNCHER_TRIM_UP).onPress(() -> trim(+1));
        driver.note(controls.input(Action.OPERATOR_INTAKE).label, "Intake on / off (tap)");
        controls.note(Action.UNJAM);
        onNormal(Action.FLOWER_STICK).onPress(robot.flowerStick::toggle);

        driver.note(modifier.label + " (hold)", "SETUP layer: the buttons below work only while it is held");
        onSetup(controls.input(GATE_HOLD_BUTTON), "Gate: follow feeder / hold BLOCKING / hold FEEDING")
                .onPress(this::cycleGate);
        onSetup(controls.input(PREFEED_HOLD_BUTTON), "Prefeed: follow / hold IN / hold OUT / hold IDLE")
                .onPress(this::cyclePrefeed);
        onSetup(Input.DPAD_UP, "Gate: shown position +" + COARSE_STEP + " (first press on an unmeasured end sets 0.50 and moves it)")
                .onPress(() -> nudge(COARSE_STEP));
        onSetup(Input.DPAD_DOWN, "Gate: shown position -" + COARSE_STEP).onPress(() -> nudge(-COARSE_STEP));
        onSetup(Input.DPAD_RIGHT, "Gate: shown position +" + FINE_STEP).onPress(() -> nudge(FINE_STEP));
        onSetup(Input.DPAD_LEFT, "Gate: shown position -" + FINE_STEP).onPress(() -> nudge(-FINE_STEP));

        legend = String.format(Locale.US, "<small>Sticks drive · %s launcher · %s fire (hold) · %s / %s launch speed · "
                        + "%s intake on/off (tap) · %s unjam (hold) · %s FLOWER stick · hold %s for setup · "
                        + "Share (Back): controls page</small>",
                label(Action.LAUNCHER_TOGGLE), label(Action.FIRE), label(Action.LAUNCHER_TRIM_DOWN),
                label(Action.LAUNCHER_TRIM_UP), label(Action.OPERATOR_INTAKE), label(Action.UNJAM),
                label(Action.FLOWER_STICK), modifier.label);
        setupLine = String.format(Locale.US, "%s gate hold · %s prefeed hold · D-pad nudges the gate end shown",
                label(GATE_HOLD_BUTTON), label(PREFEED_HOLD_BUTTON));
    }

    /** An operator action's button from the layout, on gamepad 1, doing its own job only while the modifier is up. */
    private org.firstinspires.ftc.teamcode.controls.Bindings.Trigger onNormal(Action action) {
        Input input = controls.input(action);
        return driver.when(input.label, action.description, () -> input.read(gamepad1) && !modifier.read(gamepad1));
    }

    /** A setup-layer button: {@code input} with the modifier held. */
    private org.firstinspires.ftc.teamcode.controls.Bindings.Trigger onSetup(Input input, String what) {
        return driver.when(modifier.label + " + " + input.label, what,
                () -> modifier.read(gamepad1) && input.read(gamepad1));
    }

    private String label(Action action) {
        return controls.input(action).label;
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
        boolean unjam = controls.held(Action.UNJAM);
        boolean fire = controls.held(Action.FIRE);
        // A tap toggles; the toggle is always enabled here (it is the point of a solo test).
        boolean intakeOn = intakeToggle.update(true, controls.held(Action.OPERATOR_INTAKE));
        // The subsystems update after this: the launcher, intake, prefeed and gate read these requests this loop.
        if (unjam) {
            robot.intake.eject();
            robot.feeder.reverse();
        } else {
            if (intakeOn) robot.intake.intake(); else robot.intake.idle();
            if (fire) robot.feeder.feed(); else robot.feeder.idle();
        }
        show(modifier.read(gamepad1));
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

    /** One trim press: the trim moves a step and a spinning launcher takes the new speed at once. */
    private void trim(int direction) {
        double step = trimStep(TeleOpConfig.launcherTrimStepTps, BasicDriveTeleOp.MAX_LAUNCHER_TRIM_STEP_TPS);
        trimTps = trimmed(trimTps, direction * step, LauncherSubsystem.targetTicksPerSecond);
        if (robot.launcher.isSpinning()) {
            robot.launcher.spinUp(launchSpeed());
        }
        log.event(String.format(Locale.US, "launcher: trim %+.0f, launch speed %.0f ticks/s", trimTps, launchSpeed()));
    }

    /** The launch speed in force: the configured speed plus this run's trim, never below zero. */
    private double launchSpeed() {
        return launchSpeedFor(LauncherSubsystem.targetTicksPerSecond, trimTps);
    }

    /** The Panels trim step, guarded as Basic Drive guards it: NaN or negative is no step, and never past {@code max}. Pure. */
    static double trimStep(double configured, double max) {
        return configured > 0.0 ? Math.min(configured, max) : 0.0;
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

    /** The one page: launcher, intake, transfer, FLOWER stick, battery, and the setup layer's state. */
    private void show(boolean setupLayer) {
        display.section("Launch Test");
        boolean spinning = robot.launcher.isSpinning();
        display.status("Launch speed", spinning && !robot.launcher.atSpeed() ? Display.Level.WARN : Display.Level.OK,
                String.format(Locale.US, "%.0f ticks/s (%.0f %+.0f) · %s / %s ±%.0f · %s",
                        launchSpeed(), LauncherSubsystem.targetTicksPerSecond, trimTps,
                        label(Action.LAUNCHER_TRIM_DOWN), label(Action.LAUNCHER_TRIM_UP),
                        trimStep(TeleOpConfig.launcherTrimStepTps, BasicDriveTeleOp.MAX_LAUNCHER_TRIM_STEP_TPS),
                        !spinning ? "launcher OFF" : robot.launcher.atSpeed() ? "AT SPEED" : "spinning up"));
        robot.launcher.describe(display);
        display.line("Intake toggle: " + (intakeToggle.on() ? "ON (tap to stop)" : "off (tap to start)"));
        robot.intake.describe(display);
        robot.prefeed.describe(display);
        robot.feedGate.describe(display);
        robot.feeder.describe(display);
        display.status("FLOWER stick", Display.Level.OK, (robot.flowerStick.extended() ? "out" : "in")
                + (robot.flowerStick.limp() ? ", servo limp" : ", servo holding"));
        double volts = batteryVolts();
        display.line(Double.isNaN(volts) ? "Battery: no voltage sensor" : String.format(Locale.US, "Battery %.2f V", volts));

        if (gateHold != 0 || prefeedHold != 0) {
            display.status("HOLD ACTIVE", Display.Level.FAULT,
                    (gateHold == 0 ? "" : "gate held " + (gateHold == 1 ? "BLOCKING" : "FEEDING") + " · ")
                            + (prefeedHold == 0 ? "" : "prefeed held " + prefeedName(prefeedHold) + " · ")
                            + "cycle back to follow with " + modifier.label + " + " + label(GATE_HOLD_BUTTON)
                            + " / " + label(PREFEED_HOLD_BUTTON));
        }
        if (setupLayer) {
            display.status("SETUP (" + modifier.label + " held)", Display.Level.WARN, setupLine);
        }
        display.line(String.format(Locale.US, "Gate positions: blocking %s · feeding %s",
                position(FeedGateSubsystem.blockingPosition), position(FeedGateSubsystem.feedingPosition)));
        display.status("Interlock", Display.Level.WARN, "none: fire runs whether or not the launcher is at speed");
        display.line(legend);
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
            panels.addData("launcher_reversing", robot.launcher.reversing() ? 1.0 : 0.0);
            panels.addData("shots", robot.launcher.shots());
            panels.addData("feeder_tps", robot.feeder.ticksPerSecond());
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
            panels.addData("flower_stick_out", robot.flowerStick.extended() ? 1.0 : 0.0);
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
