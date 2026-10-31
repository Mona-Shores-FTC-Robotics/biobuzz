package org.firstinspires.ftc.teamcode.opmodes;

import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.qualcomm.robotcore.eventloop.opmode.Utility;

import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.subsystems.FeedGateSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.PrefeedSubsystem;

import java.util.Locale;

/**
 * The full path on the real robot, one piece at a time: intake, prefeed, feed gate, feeder,
 * launcher. For the first session with the transfer built, to find each direction and the gate's two
 * positions, then to watch the whole path move a piece.
 *
 * <p>It runs the same {@code Robot} and the same subsystems as the match TeleOp, so what is tuned
 * here is what ships. The prefeed and the gate follow the intake and the feeder exactly as they do
 * in a match; the bench only lets a person hold either one still ({@code PrefeedSubsystem.override},
 * {@code FeedGateSubsystem.hold}) to test it alone. <b>There is no interlock:</b> the feeder runs
 * whenever R1 is held, launcher at speed or not, and the gate follows it. Nothing drives.
 *
 * <p><b>Setting the gate's two positions.</b> They are NaN until measured, and the gate does not
 * move while they are. The first D-pad press sets both to 0.50 and moves the gate there: be ready
 * for that. After it, Square holds the gate BLOCKING or FEEDING, and the D-pad nudges whichever end
 * is shown until the plate sits on its stop. Read the two numbers off the Driver Station or Panels,
 * write them into {@code FeedGateSubsystem} with a comment saying which robot, and Sloth Load.
 * Panels edits are lost on restart.
 *
 * <p>Nothing moves before PLAY, as everywhere ({@code Subsystem}).
 */
@Utility(name = "Full Path Bench", description = "Intake, prefeed, feed gate, feeder and launcher on one screen: directions and the gate's two positions")
public class FullPathBench extends RobotOpMode {

    /** How far a trigger must be pressed to count as held. */
    private static final double TRIGGER = 0.5;

    /**
     * Gate position change per D-pad up / down press, servo range 0 to 1. A speed servo covers 270°, so
     * 0.02 is about 5°. Constants, not Panels knobs: a bench's tunables are not a match's
     * ({@code TunablesTest}).
     */
    private static final double COARSE_STEP = 0.02;

    /** Gate position change per D-pad right / left press. */
    private static final double FINE_STEP = 0.005;

    /** Where the first D-pad press puts an unmeasured gate position: mid-range, so it can go either way. */
    private static final double START = 0.5;

    private TelemetryManager panels;
    /** Square: 0 follows the feeder, 1 holds the gate BLOCKING, 2 holds it FEEDING. */
    private int gateHold;
    /** Triangle: 0 follows the intake and feeder, then holds IN, OUT, IDLE. */
    private int prefeedHold;

    @Override
    protected void onInit() {
        panels = PanelsTelemetry.INSTANCE.getTelemetry();
        driver.note("R2 (hold)", "Intake in (prefeed follows)");
        driver.note("L2 (hold)", "Unjam: intake out, feeder reverse, prefeed back (wins)");
        driver.note("R1 (hold)", "Feed: feeder runs, prefeed carries, gate opens. NO interlock");
        driver.when("Circle (B)", "Launcher: spin up / spin down", () -> gamepad1.b)
                .onPress(this::toggleLauncher);
        driver.when("Square (X)", "Gate: follow feeder / hold BLOCKING / hold FEEDING", () -> gamepad1.x)
                .onPress(this::cycleGate);
        driver.when("Triangle (Y)", "Prefeed: follow / hold IN / hold OUT / hold IDLE", () -> gamepad1.y)
                .onPress(this::cyclePrefeed);
        driver.when("D-pad up", "Gate: shown position +" + COARSE_STEP + " (first press sets 0.50 and moves it)",
                () -> gamepad1.dpad_up).onPress(() -> nudge(COARSE_STEP));
        driver.when("D-pad down", "Gate: shown position -" + COARSE_STEP, () -> gamepad1.dpad_down)
                .onPress(() -> nudge(-COARSE_STEP));
        driver.when("D-pad right", "Gate: shown position +" + FINE_STEP, () -> gamepad1.dpad_right)
                .onPress(() -> nudge(FINE_STEP));
        driver.when("D-pad left", "Gate: shown position -" + FINE_STEP, () -> gamepad1.dpad_left)
                .onPress(() -> nudge(-FINE_STEP));
    }

    @Override
    protected void onInitLoop() {
        show();
    }

    @Override
    protected void onLoop() {
        boolean in = gamepad1.right_trigger > TRIGGER;
        boolean unjam = gamepad1.left_trigger > TRIGGER;
        boolean feed = gamepad1.right_bumper;
        // The subsystems update after this: the prefeed and the gate read these requests this loop.
        if (unjam) {
            robot.intake.eject();
            robot.feeder.reverse();
        } else {
            if (in) robot.intake.intake(); else robot.intake.idle();
            if (feed) robot.feeder.feed(); else robot.feeder.idle();
        }
        show();
    }

    private void toggleLauncher() {
        if (robot.launcher.isSpinning()) {
            robot.launcher.spinDown();
        } else {
            robot.launcher.spinUp();
        }
    }

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

    private void show() {
        display.section("Full path");
        robot.intake.describe(display);
        robot.prefeed.describe(display);
        robot.feedGate.describe(display);
        robot.feeder.describe(display);
        robot.launcher.describe(display);
        display.line(String.format(Locale.US, "Gate positions: blocking %s · feeding %s",
                position(FeedGateSubsystem.blockingPosition), position(FeedGateSubsystem.feedingPosition)));
        display.status("Interlock", Display.Level.WARN, "none: R1 feeds whether or not the launcher is at speed");
        display.line("<small>R2 intake · L2 unjam · R1 feed · Circle launcher · Square gate hold · "
                + "Triangle prefeed hold · D-pad nudge the gate · Share (Back): controls page</small>");
        publish();
    }

    private static String position(double value) {
        return Double.isNaN(value) ? "NaN (not set)" : String.format(Locale.US, "%.3f", value);
    }

    /** The transfer's numbers to Panels, flat keys, one update per loop. Never takes the robot down. */
    private void publish() {
        if (panels == null) return;
        try {
            panels.addData("intake_tps", robot.intake.ticksPerSecond());
            panels.addData("intake_amps", robot.intake.currentAmps());
            panels.addData("prefeed_power", robot.prefeed.power());
            panels.addData("gate_feeding", robot.feedGate.state() == FeedGateSubsystem.State.FEEDING ? 1.0 : 0.0);
            panels.addData("gate_blocking_position", FeedGateSubsystem.blockingPosition);
            panels.addData("gate_feeding_position", FeedGateSubsystem.feedingPosition);
            panels.addData("feeder_power", robot.feeder.power());
            panels.addData("feeder_amps", robot.feeder.currentAmps());
            panels.addData("left_tps", robot.launcher.leftTicksPerSecond());
            panels.addData("right_tps", robot.launcher.rightTicksPerSecond());
            panels.addData("at_speed", robot.launcher.atSpeed() ? 1.0 : 0.0);
            panels.addData("loop_ms", loopTimer.lastMs());
            panels.update();
        } catch (RuntimeException ignored) {
            // A Panels hiccup must never take the robot down.
        }
    }
}
