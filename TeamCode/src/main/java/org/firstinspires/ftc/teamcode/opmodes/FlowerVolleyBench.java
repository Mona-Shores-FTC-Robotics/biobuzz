package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Utility;

import org.firstinspires.ftc.teamcode.controls.Bindings;
import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.controls.Input;
import org.firstinspires.ftc.teamcode.localization.FlowerFieldPoints;
import org.firstinspires.ftc.teamcode.localization.StartPosition;
import org.firstinspires.ftc.teamcode.subsystems.TurretSubsystem;
import org.firstinspires.ftc.teamcode.util.Alliance;

import java.util.Locale;

/**
 * Can the robot fire eight in a row from the SCORING FLOWER: the four it arrives with, and the
 * FLOWER's four as the stick and the intake bring them in ({@link FlowerVolley}; mentor's idea,
 * 17 Nov 2026)? If it can, an Auto's second pick and its volley become one step with no drive
 * between them. This bench answers that before anything is drawn. Film every trial.
 *
 * <p><b>Setup.</b> A robot whose turret aims (20245; a locked turret cannot face the CELL from
 * here, and the bench refuses). Four POLLEN in the robot, four in the SCORING FLOWER (blue: its
 * AUDIENCE FLOWER, the same place turned half a turn). The robot square to the FLOWER and centred
 * on it, its centre {@link #START_STANDOFF_IN} in from the FLOWER's centre, which puts its front
 * face about 18.6 in off the wall; the turret on its mark. The pose starts there, so an inch of
 * placement is about 1.4° of aim: the D-pad trims it, and the match log keeps the trim.
 *
 * <p><b>One trial (Cross).</b> The launcher spins up for the shot from the FLOWER, the stick goes
 * down, the robot pushes in, the gate opens ({@code FlowerVolley.feedStart}: as the push starts, or
 * at contact; Triangle), the robot stays against the FLOWER until eight shots are counted, the
 * shots stop or the feed's cap runs out, and backs out to where it started. Circle stops it at
 * once. Reload the robot and the FLOWER and press Cross again: the pose carries on from the
 * odometry. Between trials the sticks drive gently, to put the robot back.
 *
 * <p><b>What to write down per trial</b> (the match log has the rest, in one "Last FLOWER volley"
 * line): pieces that went in the CELL, pieces left in the robot, in the FLOWER and on the floor,
 * and, from the film, whether a FLOWER piece was inside the robot before the first ball left it
 * (G407: four at most).
 */
@Utility(name = "Drive: FLOWER 8-Shot", description = "Fire four from the SCORING FLOWER while picking its four; turret robots only; film each trial")
public class FlowerVolleyBench extends RobotOpMode {

    /** Where the robot's centre starts, back from the FLOWER's centre, inches: the pick's own line-up. */
    static final double START_STANDOFF_IN = 24.0;

    private static final double FORWARD_SIGN = -1.0;
    private static final double STRAFE_SIGN = -1.0;
    private static final double TURN_SIGN = -1.0;
    private static final double STICK_DEADBAND = 0.05;
    /** Stick multiplier between trials: for putting the robot back, not driving anywhere. */
    private static final double DRIVE_SPEED = 0.3;
    private static final double MAX_AIM_TRIM_STEP_DEG = 5.0;

    private final StartPosition redStart = new StartPosition("In front of the SCORING FLOWER", Alliance.RED,
            FlowerFieldPoints.standoff(FlowerFieldPoints.RED_SCORING, START_STANDOFF_IN));
    private final StartPosition blueStart = new StartPosition("In front of the AUDIENCE FLOWER", Alliance.BLUE,
            FlowerFieldPoints.standoff(FlowerFieldPoints.BLUE_AUDIENCE, START_STANDOFF_IN));

    private TurretAimer aimer;
    private FlowerVolley volley;
    private double speedTrimTps;
    private int trial;
    private String refusal = "";

    @Override
    protected StartPosition startPosition() {
        return setup.alliance() == Alliance.BLUE ? blueStart : redStart;
    }

    @Override
    protected void onInit() {
        aimer = new TurretAimer(robot);
        volley = new FlowerVolley(robot, line -> log.event(line));
        driver.note("Left stick / right stick X", "Drive gently between trials");
        on(Input.CROSS, "Run one trial").onPress(this::startTrial);
        on(Input.CIRCLE, "Stop the trial now").onPress(this::stopTrial);
        on(Input.TRIANGLE, "Gate opens: as the push starts / at contact").onPress(() -> {
            FlowerVolley.feedStart = FlowerVolley.feedStart == FlowerVolley.FeedStart.CONTACT
                    ? FlowerVolley.FeedStart.PUSH : FlowerVolley.FeedStart.CONTACT;
            log.event("FLOWER volley: gate opens at " + FlowerVolley.feedStart);
        });
        on(Input.DPAD_LEFT, "Aim trim left").onPress(() -> aimTrim(+1));
        on(Input.DPAD_RIGHT, "Aim trim right").onPress(() -> aimTrim(-1));
        on(Input.DPAD_UP, "Launch speed trim up").onPress(() -> speedTrim(+1));
        on(Input.DPAD_DOWN, "Launch speed trim down").onPress(() -> speedTrim(-1));
    }

    /** A gamepad 1 button, polled after PLAY only. */
    private Bindings.Trigger on(Input input, String what) {
        return driver.when(input.label, what, () -> input.read(gamepad1));
    }

    @Override
    protected void onInitLoop() {
        show();
    }

    @Override
    protected void onStart() {
        aimer.start();
    }

    @Override
    protected void onLoop() {
        // The turret tracks our CELL the whole time, as Basic Drive's AIM mode does.
        if (!TurretSubsystem.lockedForward) aimer.update(setup.alliance(), TurretAimer.SWITCH_BAND_IN);
        if (volley.active()) {
            volley.update();
            if (!volley.active()) finishTrial();
        } else {
            robot.drive.drive(
                    deadband(gamepad1.left_stick_y) * FORWARD_SIGN * DRIVE_SPEED,
                    deadband(gamepad1.left_stick_x) * STRAFE_SIGN * DRIVE_SPEED,
                    deadband(gamepad1.right_stick_x) * TURN_SIGN * DRIVE_SPEED);
        }
        show();
    }

    private void startTrial() {
        if (volley.active()) return;
        if (TurretSubsystem.lockedForward) {
            refusal = "this robot's turret is locked forward: it cannot face the CELL from the FLOWER";
            log.event("FLOWER volley: refused, " + refusal);
            return;
        }
        refusal = volley.start(setup.alliance(), speedTrimTps);
        if (refusal == null) {
            refusal = "";
            trial++;
            log.event(String.format(Locale.US, "FLOWER volley: trial %d, aim trim %+.1f°, speed trim %+.0f ticks/s",
                    trial, aimer.trimDeg(), speedTrimTps));
        } else {
            log.event("FLOWER volley: refused, " + refusal);
        }
    }

    private void stopTrial() {
        if (!volley.active()) return;
        volley.cancel();
        finishTrial();
    }

    /** After a trial, however it ended: the launcher and the intake rest until the next. */
    private void finishTrial() {
        robot.launcher.spinDown();
        robot.intake.idle();
        robot.feeder.idle();
    }

    private void aimTrim(int direction) {
        double step = TeleOpConfig.trimStepDeg > 0.0 ? Math.min(TeleOpConfig.trimStepDeg, MAX_AIM_TRIM_STEP_DEG) : 0.0;
        if (direction > 0) aimer.trimLeft(step); else aimer.trimRight(step);
        log.event(String.format(Locale.US, "trim %+.1f°", aimer.trimDeg()));
    }

    private void speedTrim(int direction) {
        double step = TeleOpConfig.launcherTrimStepTps > 0.0
                ? Math.min(TeleOpConfig.launcherTrimStepTps, BasicDriveTeleOp.MAX_LAUNCHER_TRIM_STEP_TPS) : 0.0;
        speedTrimTps += direction * step;
        log.event(String.format(Locale.US, "launch speed trim %+.0f ticks/s (the next trial's)", speedTrimTps));
    }

    private void show() {
        display.section("Drive: FLOWER 8-Shot");
        if (TurretSubsystem.lockedForward) {
            display.status("Turret", Display.Level.FAULT, "locked forward on this robot: this bench needs one that aims");
        }
        display.status("Trial", volley.active() ? Display.Level.WARN : Display.Level.OK,
                volley.active() ? "running: Circle stops it" : "ready: Cross runs one (4 in the robot, 4 in the FLOWER)");
        if (!refusal.isEmpty()) display.status("Refused", Display.Level.FAULT, refusal);
        display.line(String.format(Locale.US, "Gate opens %s (Triangle) · aim trim %+.1f° (D-pad ◀ ▶) · speed trim %+.0f (D-pad ▲ ▼)",
                FlowerVolley.feedStart == FlowerVolley.FeedStart.CONTACT ? "at contact" : "as the push starts",
                aimer.trimDeg(), speedTrimTps));
        display.line(aimer.line());
        String line = volley.line();
        if (!line.isEmpty()) display.line(line);
    }

    private static double deadband(double value) {
        return Math.abs(value) < STICK_DEADBAND ? 0.0 : value;
    }
}
