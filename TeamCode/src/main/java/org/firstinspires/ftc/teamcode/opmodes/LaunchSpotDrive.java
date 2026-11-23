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
 * The driver's "go to launch spot" button (Square), held: Pedro drives from the robot's pose to
 * the launch spot its tile row picks when the button is pressed ({@link LaunchSpots#choose}: the
 * nearest on the side wall from the row against the end wall, the nearest close one from the next
 * row in, none from the two rows beside the centre line; from our side, or up to
 * {@link #maxPastCentreIn} past the centre line), and arrives aimed at that half's CELL. Never a
 * spot chosen from what the camera believes about the HIVE: the field gets crowded and the driver
 * stays near where they are (mentor, 4 Nov 2026). Letting go, or
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
 * and once the robot is at the spot, aimed and settled (or, with {@code TeleOpConfig.squareGateOnApproach},
 * already on the approach: near the spot, aimed and at speed), it feeds for as long as the button is held, through the same feeder,
 * gate and prefeed path as the driver's Circle ({@link SpotVolley}: the Autos' {@code LaunchAll}
 * waits, then no set feed time). Letting go stops the drive and the feed at once. Nothing cuts into the volley.
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
 *
 * <p>A press that stalls short of its spot (mentor, 23 Nov 2026: blocked by a robot or a piece and
 * not moving), or reaches {@link #timeoutMs} still driving, fires from where it is with
 * {@link #fireOnStall} rather than giving up ({@link SpotStall}): Pedro lets go and the shot is
 * handed to the driver's Circle in AIM ({@link StallShot}, BasicDriveTeleOp), so the turret's aim,
 * the speed table by range, the arc turn, the at-speed, aim and NECTAR gates and the shot count are
 * Circle's own. Only with the pose referenced, our side of the centre line (a CELL is scored only
 * from its own end), the range inside the speed table's measured rows and the aim inside the firing
 * arc or turning into it; otherwise the press ends as before, the reason on the Match page and in
 * the log ("stalled, out of range (80 in)"). Letting go or a stick still ends it at once.
 */
@Configurable
public final class LaunchSpotDrive {

    /** The drive's speed cap, a fraction of the robot's top path speed (0 to 1]. A starting point. */
    public static double speed = 0.8;
    /**
     * The drive gives up after this long, ms, still driving: it fires from where it is if it may
     * ({@link #fireOnStall}), else hands the sticks back. NaN or negative gives up at once.
     */
    public static double timeoutMs = 6000;
    /**
     * A WALL-row spot is drawn touching its wall (the audience wall's or the scoring table's): the
     * drive stops this far out from it, inches, on top of {@code LaunchSpots.WALL_STAND_OFF_IN}.
     * NaN or negative is 0. 0.04 (drive team, 17 Nov 2026): at 0 a press against the audience wall
     * pushed into the wall, never settled and so never fired; 1 was too much, and 0.04 was right.
     * Tried on one robot; put in for both for now.
     */
    public static double wallGapIn = 0.04;
    /**
     * The button works with the robot's centre up to this far past the centre line, inches, on the
     * other alliance's side, and drives it home; further than that it refuses. 0, NaN or negative
     * asks for the robot on our side. 24: on 6 Nov 2026 the press was refused 10 times at 3 to 4 in
     * past (both robots' logs), and 19429 on blue spent much of a run up to 20 in past. A starting
     * point, not a measurement.
     */
    public static double maxPastCentreIn = 24.0;
    /**
     * From the tile row against the end wall, the middle wall spot ({@code RIGHT_START},
     * {@code LEFT_WALL_HIVE}) counts as this much nearer than it is, inches, so it wins more of the
     * row ({@link LaunchSpots#choose}). 12 (mentor, 17 Nov 2026, chosen on the "Square Launch Spots"
     * map); 0, NaN
     * or negative is plain nearest. Read at each press.
     */
    public static double wallCentreBiasIn = LaunchSpots.DEFAULT_WALL_CENTRE_BIAS_IN;
    /**
     * From the middle tile row, the middle close spot ({@code …_CLOSE_HIVE}) counts as this much
     * nearer than it is, inches, so it wins more of the row ({@link LaunchSpots#choose}). 10 (mentor,
     * 17 Nov 2026), chosen on the "Square Launch Spots" map; 0, NaN or negative is plain nearest.
     * Read at each press.
     */
    public static double closeCentreBiasIn = LaunchSpots.DEFAULT_CLOSE_CENTRE_BIAS_IN;
    /**
     * All six close spots are driven this far back from where they are drawn, straight toward
     * their end wall, inches; the four side ones re-aim from there. 3 (mentor, 17 Nov 2026; 2.5 at
     * first, then 4): close volleys were hitting the lip of the HIVE. 0, NaN or
     * negative: the spots as drawn. Which spot a press goes to does not change. Read at each press.
     */
    public static double closeBackIn = LaunchSpots.DEFAULT_CLOSE_BACK_IN;

    /**
     * A press that stalls short of its spot, or times out still driving, fires from where it is
     * through Circle's AIM shot when it may ({@link SpotStall#refusal}); off, it ends as before.
     * Only for a press that fires ({@code TeleOpConfig.goToSpotAlsoFires}).
     */
    public static boolean fireOnStall = true;
    /** Stalled this long on end, seconds, is a stall. NaN is 0.6; held to [0.2, 5]. */
    public static double stallS = 0.6;
    /** Slower than this over the ground, inches/s, counts toward a stall. NaN is 3; held to [0, 20]; 0 never stalls. */
    public static double stallSpeedInPerSec = 3.0;
    /** Turning slower than this, degrees/s, counts toward a stall. NaN is 20; held to [0, 180]; 0 never stalls. */
    public static double stallTurnDegPerSec = 20.0;
    /** Within this of the spot, inches, it is not a stall: Pedro is finishing the path. NaN is 2; held to [0, 24]. */
    public static double stallToleranceIn = 2.0;
    /**
     * Closing on the spot by more than this, inches, during the wait starts it again: a robot slow
     * but getting there is not stalled. NaN is 1; held to [0.1, 12].
     */
    public static double stallProgressIn = 1.0;

    /**
     * What the OpMode knows about a shot from here, Circle's in AIM: asked once, as a press stalls.
     * BasicDriveTeleOp answers it with its aimer, its range and the firing arc.
     */
    public interface StallShot {
        /** Null if Circle's AIM shot may fire from here now; else why not, a few words ({@link SpotStall#refusal}). */
        String refusal();

        /** The range the shot would fire from, inches; NaN unknown. */
        double rangeIn();
    }

    private enum Phase { IDLE, DRIVING, HOLDING, STALL_FIRING, REFUSED, ENDED }

    private final Robot robot;
    private final Consumer<String> event;
    /** The Autos' settle check (still, at the path's end heading), fed this drive's path. */
    private final PathEndSettle settle;
    private final SpotVolley volley = new SpotVolley();
    private final SpotStall stall = new SpotStall();
    /** Null: a stalled press ends, as before {@link #fireOnStall}. */
    private final StallShot stallShot;

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
    private int shotsAtStart;
    private Alliance alliance;
    /** This press's drift sample: camera minus odometry at the spot, inches; NaN until taken. */
    private double driftGapIn = Double.NaN;
    private String driftNote = "";
    /** This press's spot's range to its CELL, inches ({@link #spotRangeIn()}); NaN until a press starts. */
    private double spotRangeIn = Double.NaN;
    /** When this press stalled and handed its shot to Circle, s; and how far short, and the range. */
    private double stallAtS = Double.NaN;
    private double stallShortIn = Double.NaN;
    private double stallRangeIn = Double.NaN;

    public LaunchSpotDrive(Robot robot, Consumer<String> event) {
        this(robot, event, null);
    }

    public LaunchSpotDrive(Robot robot, Consumer<String> event, StallShot stallShot) {
        this.robot = robot;
        this.event = event;
        this.stallShot = stallShot;
        this.settle = PathEndSettle.forRobot(robot, event);
    }

    /** The press asks the launcher to spin: driving, holding or firing from a stall with {@code goToSpotAlsoFires}. */
    public boolean wantsLauncher() {
        return busy() && fires;
    }

    /**
     * The press stalled and is firing from where it is: Pedro has let go, and the OpMode fires
     * Circle's AIM shot (and its arc turn) for as long as the button is held.
     */
    public boolean firingFromStall() {
        return phase == Phase.STALL_FIRING;
    }

    /** The press owns the robot one way or another (driving, holding, or firing from a stall): no pick or gather. */
    public boolean busy() {
        return active() || firingFromStall();
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
     * @param held        the launch-spot button is held
     * @param alliance    ours ({@code setup.alliance()})
     * @param sticksMoved a drive stick is off centre: the driver wants the robot back
     */
    public void update(boolean held, Alliance alliance, boolean sticksMoved) {
        long now = System.nanoTime();
        feed = false;
        if (!held) {
            if (busy()) end(now, "let go");
            phase = Phase.IDLE;
            return;
        }
        if (phase == Phase.IDLE) {
            start(now, alliance);
            return;
        }
        if (!busy()) return; // REFUSED or ENDED: wait for the button to be let go
        if (sticksMoved) {
            end(now, "sticks moved");
            return;
        }
        if (!robot.drive.poseReferenced()) {
            end(now, "pose lost");
            return;
        }
        if (phase == Phase.STALL_FIRING) return; // Circle's shot, until let go or a stick
        if (phase == Phase.DRIVING) {
            if (robot.drive.pathDone()) {
                // Pedro's parametric end: the Follower now holds the spot until the button is let go.
                phase = Phase.HOLDING;
                arriveS = s(now);
                settle.pathEnded(path);
                event.accept(String.format(Locale.US, "launch spot: arrived at %s in %.2f s, %s, CELL %s",
                        choice.spot, arriveS, offBy(), cellRange(robot.drive.pose())));
            } else {
                boolean timedOut = !(s(now) * 1000.0 <= timeoutMs); // NaN or negative: give up at once
                // Not while the volley feeds on the approach: that shot is already going.
                boolean stalled = fireOnStall && fires && volley.phase() != SpotVolley.Phase.FEEDING
                        && stall.update(s(now), remainingIn(), robot.drive.speedInPerSec(),
                        Math.toDegrees(robot.drive.turnRadPerSec()), stallS(), stallTolerance(),
                        stallSpeed(), stallTurn(), stallProgress());
                if (stalled || timedOut) {
                    stalled(now, timedOut ? "timed out" : "stalled");
                    return;
                }
            }
        }
        settle.update();
        if (phase == Phase.HOLDING && Double.isNaN(driftGapIn) && settle.settled()) sampleDrift();
        if (!fires) return;
        boolean onApproach = TeleOpConfig.squareGateOnApproach && phase == Phase.DRIVING
                && settle.readyOnApproach(choice.target, robot.drive.pose(), remainingIn());
        feed = volley.update(s(now), phase == Phase.HOLDING, robot.launcher.atSpeed(), settle.settled(),
                onApproach);
        if (volley.fired() && !feedLogged) {
            feedLogged = true;
            String when = Double.isNaN(arriveS)
                    ? String.format(Locale.US, "before arriving, %.1f in out", remainingIn())
                    : String.format(Locale.US, "%.2f s after arriving", volley.feedStartS() - arriveS);
            event.accept(String.format(Locale.US, "launch spot: gate open %.2f s after the press (%s), %s",
                    volley.feedStartS(), when, volley.why()));
        }
    }

    /** The Match page's line: what it is doing, or how the last press ended; empty before the first. */
    public String line() {
        long now = System.nanoTime();
        switch (phase) {
            case DRIVING:
                return String.format(Locale.US, "Launch spot: driving to %s, %.0f in to go · %.1f s",
                        choice.spot, remainingIn(), s(now)) + volleyNote();
            case HOLDING:
                return String.format(Locale.US, "Launch spot: AT %s, holding (arrived in %.1f s)%s · let go to drive",
                        choice.spot, arriveS, volleyNote()) + driftNote;
            case STALL_FIRING:
                return String.format(Locale.US, "Launch spot: STALLED %.0f in short of %s · firing from %.0f in (Circle's shot) · let go to stop",
                        stallShortIn, choice.spot, stallRangeIn);
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
            default: return "";
        }
    }

    private void start(long now, Alliance alliance) {
        startNs = now;
        arriveS = Double.NaN;
        feedLogged = false;
        fires = false;
        driftGapIn = Double.NaN;
        driftNote = "";
        spotRangeIn = Double.NaN;
        stallAtS = Double.NaN;
        stallShortIn = Double.NaN;
        stallRangeIn = Double.NaN;
        stall.reset();
        this.alliance = alliance;
        Pose pose = robot.drive.pose();
        choice = LaunchSpots.choose(pose, robot.drive.poseReferenced(), alliance, wallGapIn,
                maxPastCentreIn, TurretSubsystem.turretForwardIn, TurretSubsystem.turretLeftIn,
                wallCentreBiasIn, closeCentreBiasIn, closeBackIn);
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
        if (fires) volley.start();
        shotsAtStart = robot.launcher.launched();
        spotRangeIn = rangeIn(to);
        event.accept(String.format(Locale.US, "launch spot: start %s → %s %s, %s, CELL %s from the spot%s",
                where(pose), choice.spot, where(to), choice, cellRange(to), fires ? ", fires on arrival" : ""));
    }

    /**
     * The press stalled ({@code why}: "stalled", or "timed out" still driving): with
     * {@link #fireOnStall} and a shot allowed from here, Pedro lets go and the shot is Circle's
     * ({@link #firingFromStall}); otherwise the press ends with the reason.
     */
    private void stalled(long now, String why) {
        if (!fireOnStall || !fires) {
            end(now, why); // as before: only a press that fires fires from a stall
            return;
        }
        String refusal = stallShot == null ? "no stall shot here" : stallShot.refusal();
        if (refusal != null) {
            end(now, why + ", " + refusal);
            return;
        }
        Pose pose = robot.drive.pose();
        stallAtS = s(now);
        stallShortIn = remainingIn();
        stallRangeIn = stallShot.rangeIn();
        robot.drive.drive(0.0, 0.0, 0.0); // Pedro lets go: the arc turn may turn the robot
        volley.stop(s(now));
        phase = Phase.STALL_FIRING;
        event.accept(String.format(Locale.US,
                "launch spot: %s at (%.1f, %.1f) after %.2f s, %.1f in short of %s, firing from range %.0f in (Circle's AIM shot)",
                why, pose == null ? Double.NaN : pose.x(), pose == null ? Double.NaN : pose.y(), stallAtS,
                stallShortIn, choice.spot, stallRangeIn));
    }

    /** Hands the sticks back this loop and logs how the press ended. */
    private void end(long now, String why) {
        robot.drive.drive(0.0, 0.0, 0.0);
        feed = false;
        boolean feeding = volley.phase() == SpotVolley.Phase.FEEDING;
        volley.stop(s(now));
        boolean early = phase == Phase.DRIVING || phase == Phase.STALL_FIRING;
        boolean fromStall = phase == Phase.STALL_FIRING;
        String line = fromStall
                ? String.format(Locale.US, "%s after %.2f s, stalled %.0f in short of %s at %.2f s and fired from %.0f in (Circle's AIM shot)",
                        why, s(now), stallShortIn, choice.spot, stallAtS, stallRangeIn)
                : early
                ? String.format(Locale.US, "%s early after %.2f s, %.0f in short of %s", why, s(now), remainingIn(), choice.spot)
                : String.format(Locale.US, "%s after arriving at %s in %.2f s (held %.2f s)", why, choice.spot, arriveS, s(now) - arriveS);
        // Who fired, the gate's open time, and the launcher's counted shots this press. The lane's
        // count is the tracker's guess, data only (it decides nothing).
        String firedBy = volley.fired() ? "button" : fromStall ? "Circle's AIM shot (stalled)" : "nobody";
        String gate = !volley.fired() ? "gate never opened by the button"
                : String.format(Locale.US, "gate open at %.2f s for %.2f s%s", volley.feedStartS(),
                        volley.feedEndS() - volley.feedStartS(), feeding ? " (until the press ended)" : "");
        // The spot's range and the speed in force (trim included): the speed-table row this press
        // measures, read off the Match page after each volley (LaunchSpeedTable, "Filling it in").
        line += String.format(Locale.US, "; CELL %s, launch %.0f ticks/s", cellRange(choice.target),
                robot.launcher.target());
        line += String.format(Locale.US, "; fired by %s, %s, %d shots counted, lane %d (tracker); drift %s",
                firedBy, fires ? gate : "button does not fire", robot.launcher.launched() - shotsAtStart,
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

    private static double stallS() {
        return SpotStall.guarded(stallS, 0.6, 0.2, 5.0);
    }

    private static double stallSpeed() {
        return SpotStall.guarded(stallSpeedInPerSec, 3.0, 0.0, 20.0);
    }

    private static double stallTurn() {
        return SpotStall.guarded(stallTurnDegPerSec, 20.0, 0.0, 180.0);
    }

    private static double stallTolerance() {
        return SpotStall.guarded(stallToleranceIn, 2.0, 0.0, 24.0);
    }

    private static double stallProgress() {
        return SpotStall.guarded(stallProgressIn, 1.0, 0.1, 12.0);
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
