package org.firstinspires.ftc.teamcode.opmodes;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.api.Paths;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.pedropathing.paths.interpolator.Interpolator;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.localization.LaunchSpots;
import org.firstinspires.ftc.teamcode.opmodes.auto.PathEndSettle;
import org.firstinspires.ftc.teamcode.pedro.Constants;
import org.firstinspires.ftc.teamcode.subsystems.TurretAim;
import org.firstinspires.ftc.teamcode.subsystems.TurretSubsystem;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.vision.Vec3;

import java.util.Locale;
import java.util.function.Consumer;

/**
 * The driver's "go to launch spot" button, held: Pedro drives from the robot's pose to the launch
 * spot nearest it when the button is pressed ({@link LaunchSpots#choose}: our two starts, stopped
 * {@link #startOffWallIn} off the wall, the Autos' four CATCH and OFFSET spots, and the two
 * CORNER long shots; from our side, or up to {@link #maxPastCentreIn} past the centre line), and arrives
 * aimed at that half's CELL. Never a spot chosen from what the camera believes about the HIVE: the
 * field gets crowded and the driver stays near where they are (mentor, 4 Nov 2026). Letting go, or
 * moving a drive stick, gives the sticks back that same loop, as the FLOWER pick does. At the spot
 * the Follower holds it until the button is let go.
 *
 * <p>Experimental, to try against chassis aim (Circle): it drives only with a referenced pose
 * ({@code robot.drive.poseReferenced()}) and refuses otherwise, with a Driver Station line and a log
 * event. The drive is one straight Pedro path, as the FLOWER pick's line-up is; it does not steer
 * round the HIVE's frame, so a drive from beside the HIVE is watched on the robot first
 * ({@code doc/robot-todo.md}).
 *
 * <p>With {@code TeleOpConfig.goToSpotAlsoFires} (on while testing), the press also launches: the
 * launcher spins up as the drive starts (the OpMode does it, at the operator's launch speed and trim),
 * and once the robot is at the spot, aimed and settled, it feeds for as long as the button is held, through the same feeder,
 * gate and prefeed path as the operator's FIRE ({@link SpotVolley}: the Autos' {@code LaunchAll}
 * waits, then no set feed time). Letting go stops the drive and the feed at once. The operator's FIRE wins.
 *
 * <p>Every press is match-log lines, for comparing practice logs with chassis-aim use: the start
 * (start pose, chosen spot, its distance, and the spot's range to its CELL, so presses are also
 * launcher speed-vs-distance data), the arrival, the gate opening, and the end (time to arrive, or
 * let go early and how far short; who fired; shots counted).
 *
 * <p>Each arrival is also a drift sample at a known spot: once settled there, facing the HIVE, the
 * first fresh camera position is compared with the odometry's, the same gap the match log keeps
 * as {@code /Drift/GapXIn}, {@code /Drift/GapYIn}, and logged. It is never applied (still and
 * continuous fixes stay off until measured); past {@code TeleOpConfig.spotDriftWarnIn} the Match
 * page suggests a corner reset. The presses build the evidence for whether TeleOp needs to
 * relocalize.
 */
@Configurable
public final class LaunchSpotDrive {

    /** The drive's speed cap, a fraction of the robot's top path speed (0 to 1]. A starting point. */
    public static double speed = 0.8;
    /** The drive gives up after this long, ms, and hands the sticks back. NaN or negative gives up at once. */
    public static double timeoutMs = 6000;
    /**
     * A start spot is on the wall, the robot's back against it: the drive stops this far out from it,
     * inches, along the start's facing, rather than into the wall. A starting point. NaN or negative is 0.
     */
    public static double startOffWallIn = 3.0;
    /**
     * The button works with the robot's centre up to this far past the centre line, inches, on the
     * other alliance's side, and drives it home; further than that it refuses. 0, NaN or negative
     * asks for the robot on our side. 24: on 6 Nov 2026 the press was refused 10 times at 3 to 4 in
     * past (both robots' logs), and 19429 on blue spent much of a run up to 20 in past. A starting
     * point, not a measurement.
     */
    public static double maxPastCentreIn = 24.0;

    private enum Phase { IDLE, DRIVING, HOLDING, REFUSED, ENDED }

    private final Robot robot;
    private final Consumer<String> event;
    /** The Autos' settle check (still, at the path's end heading), fed this drive's path. */
    private final PathEndSettle settle;
    /** The volley's feed held back while the robot is knocked off the spot's heading ({@code TeleOpConfig.pauseFeedOffAim}). */
    private final AimPause aimPause = new AimPause();
    private final SpotVolley volley = new SpotVolley();

    private Phase phase = Phase.IDLE;
    private LaunchSpots.Choice choice;
    private long startNs;
    private double arriveS = Double.NaN;
    private String result = "";
    private Path path;
    /** This press also fires ({@code TeleOpConfig.goToSpotAlsoFires}, read at the press). */
    private boolean fires;
    private boolean feed;
    private boolean feedLogged;
    private boolean operatorFired;
    private int shotsAtStart;
    private Alliance alliance;
    /** This press's drift sample: camera minus odometry at the spot, inches; NaN until taken. */
    private double driftGapIn = Double.NaN;
    private String driftNote = "";
    /** This press's spot's range to its CELL, inches ({@link #spotRangeIn()}); NaN until a press starts. */
    private double spotRangeIn = Double.NaN;

    public LaunchSpotDrive(Robot robot, Consumer<String> event) {
        this.robot = robot;
        this.event = event;
        this.settle = PathEndSettle.forRobot(robot, event);
    }

    /** The press asks the launcher to spin: driving or holding with {@code goToSpotAlsoFires}. */
    public boolean wantsLauncher() {
        return active() && fires;
    }

    /** The button's volley asks the feeder to feed this loop (the OpMode's fire decides with it). */
    public boolean feedRequested() {
        return feed;
    }

    /** This press was refused, and the button is still held: the Match page's alert. */
    public boolean refused() {
        return phase == Phase.REFUSED;
    }

    /** Pedro is driving or holding the robot: nothing else may drive it. */
    public boolean active() {
        return phase == Phase.DRIVING || phase == Phase.HOLDING;
    }

    /**
     * One step, every loop.
     *
     * @param held        the button is held
     * @param alliance    ours ({@code setup.alliance()})
     * @param sticksMoved a drive stick is off centre: the driver wants the robot back
     * @param operatorFiring the operator's FIRE is held: it wins over the button's volley
     */
    public void update(boolean held, Alliance alliance, boolean sticksMoved, boolean operatorFiring) {
        long now = System.nanoTime();
        feed = false;
        if (!held) {
            if (active()) end(now, "let go");
            phase = Phase.IDLE;
            return;
        }
        if (phase == Phase.IDLE) {
            start(now, alliance);
            return;
        }
        if (!active()) return; // REFUSED or ENDED: wait for the button to be let go
        if (sticksMoved) {
            end(now, "sticks moved");
            return;
        }
        if (!robot.drive.poseReferenced()) {
            end(now, "pose lost");
            return;
        }
        if (phase == Phase.DRIVING) {
            if (robot.drive.pathDone()) {
                // Pedro's parametric end: the Follower now holds the spot until the button is let go.
                phase = Phase.HOLDING;
                arriveS = s(now);
                settle.pathEnded(path);
                event.accept(String.format(Locale.US, "launch spot: arrived at %s in %.2f s, %s, CELL %s",
                        choice.spot, arriveS, offBy(), cellRange(robot.drive.pose())));
            } else if (!(s(now) * 1000.0 <= timeoutMs)) { // NaN or negative: give up at once
                end(now, "timed out");
                return;
            }
        }
        settle.update();
        if (phase == Phase.HOLDING && Double.isNaN(driftGapIn) && settle.settled()) sampleDrift();
        if (operatorFiring) operatorFired = true;
        if (!fires) return;
        // On the approach (TeleOpConfig.spotFeedsOnApproach): close, aimed and hardly turning, as an
        // Auto's LaunchAll opens its gate; the volley also wants the launcher at speed.
        boolean onApproach = TeleOpConfig.spotFeedsOnApproach && phase == Phase.DRIVING
                && settle.readyOnApproach(choice.target, robot.drive.pose(), remainingIn());
        feed = volley.update(s(now), phase == Phase.HOLDING, robot.launcher.atSpeed(), settle.settled(), operatorFiring,
                onApproach);
        // Knocked off the spot's heading while feeding (TeleOpConfig.pauseFeedOffAim): hold back
        // until the Follower has turned it back. The volley's own state is untouched.
        if (feed) {
            double offDeg = headingOffDeg();
            boolean was = aimPause.paused();
            if (aimPause.update(TeleOpConfig.pauseFeedOffAim, offDeg, TeleOpConfig.aimToleranceDeg, false)) feed = false;
            if (aimPause.paused() != was) {
                event.accept(String.format(Locale.US, "launch spot: feed %s, %+.1f° off the spot's heading",
                        aimPause.paused() ? "paused" : "resumed", offDeg));
            }
        }
        if (volley.fired() && !feedLogged) {
            feedLogged = true;
            event.accept(Double.isNaN(arriveS)
                    ? String.format(Locale.US, "launch spot: gate open %.2f s after the press (%.1f in before the spot), %s",
                            volley.feedStartS(), remainingIn(), volley.why())
                    : String.format(Locale.US, "launch spot: gate open %.2f s after the press (%.2f s after arriving), %s",
                            volley.feedStartS(), volley.feedStartS() - arriveS, volley.why()));
        }
    }

    /** The Match page's line: what it is doing, or how the last press ended; empty before the first. */
    public String line() {
        long now = System.nanoTime();
        switch (phase) {
            case DRIVING:
                return String.format(Locale.US, "Launch spot: driving to %s, %.0f in to go · %.1f s",
                        choice.spot, remainingIn(), s(now));
            case HOLDING:
                return String.format(Locale.US, "Launch spot: AT %s, holding (arrived in %.1f s)%s · let go to drive",
                        choice.spot, arriveS, volleyNote()) + driftNote;
            default:
                return result.isEmpty() ? "" : result + driftNote;
        }
    }

    /** On the Match page while holding: what the button's volley is doing. */
    private String volleyNote() {
        if (!fires) return "";
        switch (volley.phase()) {
            case WAITING: return " · volley: waiting for speed and settle";
            case FEEDING: return " · volley: FEEDING, let go to stop";
            case YIELDED: return " · volley: the operator's";
            default: return "";
        }
    }

    private void start(long now, Alliance alliance) {
        startNs = now;
        arriveS = Double.NaN;
        feedLogged = false;
        operatorFired = false;
        fires = false;
        driftGapIn = Double.NaN;
        driftNote = "";
        spotRangeIn = Double.NaN;
        this.alliance = alliance;
        Pose pose = robot.drive.pose();
        choice = LaunchSpots.choose(pose, robot.drive.poseReferenced(), alliance, startOffWallIn,
                maxPastCentreIn, TurretSubsystem.turretForwardIn, TurretSubsystem.turretLeftIn);
        if (choice.refused()) {
            // Not on the field: the drive says what fixes it (a lost Pinpoint is not "no reset yet").
            String why = pose != null && !robot.drive.poseReferenced() ? robot.drive.notReferencedWhy() : null;
            if (why == null) why = choice.refusal;
            phase = Phase.REFUSED;
            result = "Launch spot REFUSED: " + why;
            event.accept("launch spot: refused, " + why + ", from " + where(pose));
            return;
        }
        try {
            robot.drive.preparePathFollowing();
        } catch (RuntimeException e) {
            phase = Phase.REFUSED;
            result = "Launch spot REFUSED: no path following on this robot";
            event.accept("launch spot: refused, no path following on this robot: " + e.getMessage());
            return;
        }
        Pose to = choice.target;
        robot.drive.setAccelLimited(true);
        // The same call shape as the FLOWER pick's line-up and the generated Autos' paths.
        path = Paths.line(pose, to)
                .heading(Interpolator.piecewise().until(1, Interpolator.linear(pose, to)))
                .with(Constants.forActiveRobot().foresightConfig.maxPathSpeed.at(GrabRoutine.unitFraction(speed)));
        robot.drive.follow(path);
        settle.pathStarted(path);
        phase = Phase.DRIVING;
        fires = TeleOpConfig.goToSpotAlsoFires;
        aimPause.reset();
        if (fires) volley.start();
        shotsAtStart = robot.launcher.shots();
        spotRangeIn = rangeIn(to);
        event.accept(String.format(Locale.US, "launch spot: start %s → %s %s, %s, CELL %s from the spot%s",
                where(pose), choice.spot, where(to), choice, cellRange(to), fires ? ", fires on arrival" : ""));
    }

    /** Hands the sticks back this loop and logs how the press ended. */
    private void end(long now, String why) {
        robot.drive.drive(0.0, 0.0, 0.0);
        feed = false;
        boolean feeding = volley.phase() == SpotVolley.Phase.FEEDING;
        volley.stop(s(now));
        boolean early = phase == Phase.DRIVING;
        String line = early
                ? String.format(Locale.US, "%s early after %.2f s, %.0f in short of %s", why, s(now), remainingIn(), choice.spot)
                : String.format(Locale.US, "%s after arriving at %s in %.2f s (held %.2f s)", why, choice.spot, arriveS, s(now) - arriveS);
        // Who fired, the gate's open time, and the launcher's counted shots this press. The lane's
        // count is the tracker's guess, data only (it decides nothing).
        String firedBy = volley.fired() && operatorFired ? "button, then the operator"
                : volley.fired() ? "button" : operatorFired ? "operator" : "nobody";
        String gate = !volley.fired() ? "gate never opened by the button"
                : String.format(Locale.US, "gate open at %.2f s for %.2f s%s", volley.feedStartS(),
                        volley.feedEndS() - volley.feedStartS(), feeding ? " (until the press ended)" : "");
        // The spot's range and the speed in force (trim included): the speed-table row this press
        // measures, read off the Match page after each volley (LaunchSpeedTable, "Filling it in").
        line += String.format(Locale.US, "; CELL %s, launch %.0f ticks/s", cellRange(choice.target),
                robot.launcher.target());
        line += String.format(Locale.US, "; fired by %s, %s, %d shots counted, lane %d (tracker); drift %s",
                firedBy, fires ? gate : "button does not fire", robot.launcher.shots() - shotsAtStart,
                robot.colorSensor.lane().count(), early ? "not sampled (never arrived)"
                        : Double.isNaN(driftGapIn) ? "not sampled (no settled camera frame)"
                        : String.format(Locale.US, "%.1f in", driftGapIn));
        event.accept("launch spot: " + line);
        result = "Last launch spot: " + line;
        phase = Phase.ENDED;
    }

    /**
     * The drift sample, once per press: settled at the spot, the first fresh camera position against
     * the odometry's. Logged and shown only, never applied.
     */
    private void sampleDrift() {
        Pose camera = robot.drive.cameraPose();
        Pose pose = robot.drive.pose();
        if (camera == null || pose == null) return; // wait for a frame while held
        double dx = camera.x() - pose.x();
        double dy = camera.y() - pose.y();
        driftGapIn = Math.hypot(dx, dy);
        boolean warns = LaunchSpots.driftWarns(driftGapIn, TeleOpConfig.spotDriftWarnIn);
        event.accept(String.format(Locale.US,
                "launch spot: drift at %s, camera - odometry (%+.1f, %+.1f) = %.1f in, odometry %s (shadow only, not applied)%s",
                choice.spot, dx, dy, driftGapIn, where(pose), warns ? ": corner reset suggested" : ""));
        driftNote = warns ? String.format(Locale.US,
                " · POSE %.1f in off the camera here: corner reset suggested", driftGapIn) : "";
    }

    /**
     * The horizontal range from the turret at {@code at} to the raised row of the spot's CELL, as
     * the TeleOp's shot lines measure it (TurretAim, turret offsets), "48 in"; "unknown" without one.
     */
    private String cellRange(Pose at) {
        double range = rangeIn(at);
        return Double.isNaN(range) ? "range unknown" : String.format(Locale.US, "%.0f in", range);
    }

    /** {@link #cellRange} as a number, inches; NaN without a pose or a field point. */
    private double rangeIn(Pose at) {
        Vec3 row = choice.spot.cellRow(alliance);
        if (at == null || row == null) return Double.NaN;
        return TurretAim.rangeFromFieldIn(at.x(), at.y(), at.heading(),
                TurretSubsystem.turretForwardIn, TurretSubsystem.turretLeftIn, row.x(), row.y());
    }

    /**
     * While a press is driving or holding: the range from the spot it is going to, to that spot's
     * CELL, inches, so the launcher can spin up for where it will fire from rather than where it
     * is. NaN otherwise, or without a field point.
     */
    public double spotRangeIn() {
        return active() ? spotRangeIn : Double.NaN;
    }

    /** How far the robot is turned off the spot's heading, degrees, signed; NaN without a pose. */
    private double headingOffDeg() {
        Pose pose = robot.drive.pose();
        return pose == null ? Double.NaN
                : Math.toDegrees(Math.IEEEremainder(choice.target.heading() - pose.heading(), 2 * Math.PI));
    }

    private double remainingIn() {
        Pose pose = robot.drive.pose();
        return pose == null ? Double.NaN : Math.hypot(choice.target.x() - pose.x(), choice.target.y() - pose.y());
    }

    /** At arrival: how far from the spot and its heading. */
    private String offBy() {
        Pose pose = robot.drive.pose();
        if (pose == null) return "no pose";
        double headingDeg = Math.toDegrees(Math.IEEEremainder(choice.target.heading() - pose.heading(), 2 * Math.PI));
        return String.format(Locale.US, "%.1f in and %+.0f° off, %s", remainingIn(), headingDeg, where(pose));
    }

    private double s(long now) {
        return (now - startNs) / 1e9;
    }

    private static String where(Pose pose) {
        return pose == null ? "no pose" : String.format(Locale.US, "(%.1f, %.1f, %.0f°)",
                pose.x(), pose.y(), Math.toDegrees(pose.heading()));
    }
}
