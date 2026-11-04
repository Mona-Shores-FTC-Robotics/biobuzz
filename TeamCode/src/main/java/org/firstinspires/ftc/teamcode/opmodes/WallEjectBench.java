package org.firstinspires.ftc.teamcode.opmodes;

import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Utility;

import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.controls.Input;

import java.util.Locale;

/**
 * Can the intake set the preloads down against a wall, gently enough that they stay there for a
 * robot to collect later? (Mentor's idea, 4 Nov 2026: an L robot has about 6 s with nothing to do
 * before our LEFT CELL rises; it could lay its four preloads at the wall, pick a FLOWER, and come
 * back for them.) This bench answers only the first question: what the four look like on the
 * tiles. Film each trial; the match log has the settings and the measured back-up beside it.
 *
 * <p><b>Setup.</b> Four POLLEN in the robot. The robot's front (the intake) against a field wall,
 * touching it or as close as the trial wants to start. Nothing moves before PLAY.
 *
 * <p><b>One trial (Cross).</b> The robot backs straight away from the wall, robot-centric, at a
 * low power until the Pinpoint says it has moved the set distance (or {@link #BACK_TIMEOUT_S}
 * passes); then the intake ejects at the set roller and star-wheel powers, with the feeder and
 * prefeed backing out too unless switched off, for the set time; then everything idles. Circle
 * stops a trial at once. Between trials the sticks drive gently, to put the robot back.
 *
 * <p><b>The knobs are this run's only</b> (a bench's numbers are not a match's): the D-pad, L1/R1
 * and Triangle change them, and nothing here writes {@code IntakeSubsystem.outPower} or any other
 * {@code @Configurable} field. A setting that works goes into whatever Auto command comes of this.
 * The prefeed and feeder back out at their own configured powers ({@code PrefeedSubsystem.outPower},
 * {@code FeederSubsystem.reversePower}); Square turns the feeder's part off to see whether the
 * roller and stars alone, pushing what reaches them, set the pieces down more gently.
 */
@Utility(name = "Bench: Wall Eject", description = "Back off a wall and set the preloads down against it gently; film each trial")
public class WallEjectBench extends RobotOpMode {

    private static final double FORWARD_SIGN = -1.0;
    private static final double STRAFE_SIGN = -1.0;
    private static final double TURN_SIGN = -1.0;
    private static final double STICK_DEADBAND = 0.05;
    /** Stick multiplier between trials: for putting the robot back on the wall, not driving anywhere. */
    private static final double DRIVE_SPEED = 0.3;

    /** Power while backing off the wall. Low, so the robot stops near the distance asked. */
    private static final double BACK_POWER = 0.2;
    /** Backing that has not reached its distance by now gives up and ejects where it is. */
    private static final double BACK_TIMEOUT_S = 2.0;

    private static final double BACK_STEP_IN = 0.25;
    private static final double BACK_MAX_IN = 6.0;
    private static final double ROLLER_STEP = 0.05;
    private static final double STARS_STEP = 0.1;
    private static final double EJECT_STEP_S = 0.5;
    private static final double EJECT_MAX_S = 5.0;

    private enum Phase { READY, BACKING, EJECTING }

    /** How far to back off the wall before ejecting, inches. 1.5: about one POLLEN's width. */
    private double backUpIn = 1.5;
    /** The roller's eject power, 0 to 1. Gentler than the match's unjam (0.6). */
    private double rollerPower = 0.3;
    /** The star wheels' eject power, 0 to 1. */
    private double starsPower = 0.3;
    /** How long the eject runs, seconds: long enough for four at a gentle power. */
    private double ejectS = 2.5;
    /** Whether the feeder (and so the prefeed) backs out too, pushing the lane toward the intake. */
    private boolean feederToo = true;

    private Phase phase = Phase.READY;
    private double phaseStartS;
    private Pose backFrom;
    private double backedIn;
    private int trial;
    private String lastResult = "no trial yet";

    @Override
    protected void onInit() {
        driver.note("Left stick / right stick X", "Drive gently between trials");
        on(Input.CROSS, "Run one trial: back off, then eject").onPress(this::startTrial);
        on(Input.CIRCLE, "Stop the trial now").onPress(() -> finish("stopped by Circle"));
        on(Input.DPAD_RIGHT, "Back-up distance +" + BACK_STEP_IN + " in").onPress(() -> backUpIn = clamp(backUpIn + BACK_STEP_IN, 0, BACK_MAX_IN));
        on(Input.DPAD_LEFT, "Back-up distance -" + BACK_STEP_IN + " in").onPress(() -> backUpIn = clamp(backUpIn - BACK_STEP_IN, 0, BACK_MAX_IN));
        on(Input.DPAD_UP, "Roller eject power +" + ROLLER_STEP).onPress(() -> rollerPower = clamp(rollerPower + ROLLER_STEP, ROLLER_STEP, 1));
        on(Input.DPAD_DOWN, "Roller eject power -" + ROLLER_STEP).onPress(() -> rollerPower = clamp(rollerPower - ROLLER_STEP, ROLLER_STEP, 1));
        on(Input.R1, "Star-wheel eject power +" + STARS_STEP).onPress(() -> starsPower = clamp(starsPower + STARS_STEP, 0, 1));
        on(Input.L1, "Star-wheel eject power -" + STARS_STEP).onPress(() -> starsPower = clamp(starsPower - STARS_STEP, 0, 1));
        on(Input.TRIANGLE, "Eject time +" + EJECT_STEP_S + " s (wraps to " + EJECT_STEP_S + ")")
                .onPress(() -> ejectS = ejectS + EJECT_STEP_S > EJECT_MAX_S ? EJECT_STEP_S : ejectS + EJECT_STEP_S);
        on(Input.SQUARE, "Feeder backs out too: on / off").onPress(() -> feederToo = !feederToo);
    }

    /** A gamepad 1 button, polled after PLAY only. */
    private org.firstinspires.ftc.teamcode.controls.Bindings.Trigger on(Input input, String what) {
        return driver.when(input.label, what, () -> input.read(gamepad1));
    }

    @Override
    protected void onInitLoop() {
        show();
    }

    @Override
    protected void onLoop() {
        double now = System.nanoTime() / 1e9;
        switch (phase) {
            case BACKING:
                Pose pose = robot.drive.pose();
                backedIn = pose == null || backFrom == null ? Double.NaN
                        : Math.hypot(pose.x() - backFrom.x(), pose.y() - backFrom.y());
                if (!(backedIn < backUpIn) || now - phaseStartS >= BACK_TIMEOUT_S) {
                    robot.drive.drive(0, 0, 0);
                    startEject(now);
                } else {
                    robot.drive.driveRobotCentric(-BACK_POWER, 0, 0);
                }
                break;
            case EJECTING:
                robot.drive.drive(0, 0, 0);
                robot.intake.eject(rollerPower, starsPower);
                if (feederToo) robot.feeder.reverse(); else robot.feeder.idle();
                if (now - phaseStartS >= ejectS) finish("done");
                break;
            default:
                robot.drive.drive(
                        deadband(gamepad1.left_stick_y) * FORWARD_SIGN * DRIVE_SPEED,
                        deadband(gamepad1.left_stick_x) * STRAFE_SIGN * DRIVE_SPEED,
                        deadband(gamepad1.right_stick_x) * TURN_SIGN * DRIVE_SPEED);
                robot.intake.idle();
                robot.feeder.idle();
        }
        show();
    }

    private void startTrial() {
        if (phase != Phase.READY) return;
        trial++;
        backFrom = robot.drive.pose();
        backedIn = 0;
        phaseStartS = System.nanoTime() / 1e9;
        log.event("wall eject " + trial + ": start, " + settings()
                + (backFrom == null ? " · no Pinpoint: ejecting where it stands" : ""));
        if (backFrom == null || backUpIn <= 0) {
            startEject(phaseStartS);
        } else {
            phase = Phase.BACKING;
        }
    }

    private void startEject(double now) {
        phase = Phase.EJECTING;
        phaseStartS = now;
        log.event(String.format(Locale.US, "wall eject %d: backed %s, ejecting", trial, inches(backedIn)));
    }

    private void finish(String how) {
        if (phase == Phase.READY) return;
        phase = Phase.READY;
        robot.drive.drive(0, 0, 0);
        robot.intake.idle();
        robot.feeder.idle();
        lastResult = String.format(Locale.US, "trial %d %s · backed %s · %s", trial, how, inches(backedIn), settings());
        log.event("wall eject " + lastResult);
    }

    private String settings() {
        return String.format(Locale.US, "back-up %.2f in, roller %.2f, stars %.1f, %.1f s, feeder %s",
                backUpIn, rollerPower, starsPower, ejectS, feederToo ? "backs out" : "idle");
    }

    private void show() {
        display.section("Bench: Wall Eject");
        display.status("Trial", phase == Phase.READY ? Display.Level.OK : Display.Level.WARN,
                phase == Phase.READY ? "ready: Cross runs one" : phase == Phase.BACKING
                        ? String.format(Locale.US, "backing off the wall: %s of %.2f in", inches(backedIn), backUpIn)
                        : "ejecting");
        display.line(String.format(Locale.US, "Back-up %.2f in (D-pad ◀ ▶) · roller %.2f (D-pad ▲ ▼) · stars %.1f (L1 / R1)",
                backUpIn, rollerPower, starsPower));
        display.line(String.format(Locale.US, "Eject %.1f s (Triangle) · feeder %s (Square) · Circle stops",
                ejectS, feederToo ? "backs out too" : "idle"));
        display.line("Last: " + lastResult);
        robot.intake.describe(display);
        if (robot.drive.pose() == null) {
            display.status("Pinpoint", Display.Level.WARN, "missing: a trial ejects where the robot stands");
        }
    }

    private static String inches(double value) {
        return Double.isNaN(value) ? "unknown" : String.format(Locale.US, "%.2f in", value);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double deadband(double value) {
        return Math.abs(value) < STICK_DEADBAND ? 0.0 : value;
    }
}
