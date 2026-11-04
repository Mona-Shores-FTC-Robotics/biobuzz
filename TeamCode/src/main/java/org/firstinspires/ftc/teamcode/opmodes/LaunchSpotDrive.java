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
import org.firstinspires.ftc.teamcode.util.Alliance;

import java.util.Locale;
import java.util.function.Consumer;

/**
 * The driver's "go to launch spot" button, held: Pedro drives from the robot's pose to the launch
 * spot nearest it when the button is pressed ({@link LaunchSpots#choose}: our two starts, stopped
 * {@link #startOffWallIn} off the wall, and the Autos' four CATCH and OFFSET spots), and arrives
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
 * and once the robot is at the spot, aimed and settled, one volley feeds through the same feeder,
 * gate and prefeed path as the operator's FIRE ({@link SpotVolley}: the Autos' {@code LaunchAll}
 * waits and feed time). Letting go stops the drive and the feed at once. The operator's FIRE wins.
 *
 * <p>Every press is match-log lines, for comparing practice logs with chassis-aim use: the start
 * (start pose, chosen spot, its distance), the arrival, the gate opening, and the end (time to
 * arrive, or let go early and how far short; who fired; shots counted).
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

    private enum Phase { IDLE, DRIVING, HOLDING, REFUSED, ENDED }

    private final Robot robot;
    private final Consumer<String> event;
    /** The Autos' settle check (still, at the path's end heading), fed this drive's path. */
    private final PathEndSettle settle;
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
                event.accept(String.format(Locale.US, "launch spot: arrived at %s in %.2f s, %s",
                        choice.spot, arriveS, offBy()));
            } else if (!(s(now) * 1000.0 <= timeoutMs)) { // NaN or negative: give up at once
                end(now, "timed out");
                return;
            }
        }
        settle.update();
        if (operatorFiring) operatorFired = true;
        if (!fires) return;
        feed = volley.update(s(now), phase == Phase.HOLDING, robot.launcher.atSpeed(), settle.settled(), operatorFiring);
        if (volley.fired() && !feedLogged) {
            feedLogged = true;
            event.accept(String.format(Locale.US, "launch spot: gate open %.2f s after the press (%.2f s after arriving), %s",
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
                        choice.spot, arriveS, volleyNote());
            default:
                return result;
        }
    }

    /** On the Match page while holding: what the button's volley is doing. */
    private String volleyNote() {
        if (!fires) return "";
        switch (volley.phase()) {
            case WAITING: return " · volley: waiting for speed and settle";
            case FEEDING: return " · volley: FEEDING";
            case DONE: return " · volley: fed";
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
        Pose pose = robot.drive.pose();
        choice = LaunchSpots.choose(pose, robot.drive.poseReferenced(), alliance, startOffWallIn);
        if (choice.refused()) {
            phase = Phase.REFUSED;
            result = "Launch spot REFUSED: " + choice.refusal;
            event.accept("launch spot: refused, " + choice.refusal + ", from " + where(pose));
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
        if (fires) volley.start();
        shotsAtStart = robot.launcher.shots();
        event.accept(String.format(Locale.US, "launch spot: start %s → %s %s, %s%s",
                where(pose), choice.spot, where(to), choice, fires ? ", fires on arrival" : ""));
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
                        volley.feedEndS() - volley.feedStartS(), feeding ? " (cut by the end of the press)" : "");
        line += String.format(Locale.US, "; fired by %s, %s, %d shots counted, lane %d (tracker)",
                firedBy, fires ? gate : "button does not fire", robot.launcher.shots() - shotsAtStart,
                robot.colorSensor.lane().count());
        event.accept("launch spot: " + line);
        result = "Last launch spot: " + line;
        phase = Phase.ENDED;
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
