package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.CommandBuilder;
import com.pedropathing.ivy.behaviors.EndCondition;
import com.pedropathing.ivy.commands.Commands;
import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.autokit.AutoRegistry;
import org.firstinspires.ftc.teamcode.localization.HiveFieldPoints;
import org.firstinspires.ftc.teamcode.subsystems.BeamBreakSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.FeedWatch;
import org.firstinspires.ftc.teamcode.subsystems.IntakeSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.LaunchSpeedTable;
import org.firstinspires.ftc.teamcode.subsystems.LauncherSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.TurretAim;
import org.firstinspires.ftc.teamcode.subsystems.TurretSubsystem;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.vision.CellSighting;
import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.HiveCellState;
import org.firstinspires.ftc.teamcode.vision.Vec3;

import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

/**
 * The launcher's cards for the Auto Builder: {@code SpinUp}, {@code LaunchAll}, {@code LaunchOne}
 * and the {@code Empty} trigger, built on {@code robot.launcher} and {@code robot.feeder}.
 *
 * <p>A launch card spins the launcher up, waits until it is at speed (or, with {@link #gateLeadS} on, due at speed within it; at most
 * {@link #maxSpinUpWaitS}, then it feeds anyway and logs it) <em>and the robot has
 * settled</em> ({@link PathEndSettle}: aimed at the last path's end heading, and slowing), runs the
 * feeder for a set time ({@code LaunchAll}: or until the launcher counts its last shot, if sooner),
 * then idles the feeder. A fire step after a path therefore waits for the
 * robot, not for Pedro's "done" (#193). If the robot has not settled within
 * {@link PathEndSettle#maxSettleWaitS} of the card starting, it feeds anyway and says so in the
 * match log, so a robot that never quite settles still fires. The launcher is left spinning: the Autos call {@code SpinUp} once
 * and fire several times, and spinning down between shots would cost a spin-up each time. The
 * OpMode's stop cuts the wheels at the end.
 *
 * <h2>Launching on the approach</h2>
 *
 * <p>A good TeleOp driver opens the gate before the robot has stopped at a launch spot. So does a
 * {@code LaunchAll} right after a path card: its {@link #approach()} runs while that path drives
 * ({@link AutoRegistry.Approach}), and opens the gate once the robot is within
 * {@link PathEndSettle#launchWithinIn} of the path's end, within {@link PathEndSettle#aimToleranceDeg}
 * of its end heading, turning slower than {@link PathEndSettle#launchTurnDegPerSec}, and the launcher
 * is at speed ({@link PathEndSettle#readyOnApproach}). The card then only finishes the feed: it ends
 * {@link #launchAllFeedS} after the gate opened, on the approach. An approach that never meets the
 * condition (one that turns late, onto the HIVE only at the last instant) opens nothing, and the
 * card waits for the settle as before. {@link #launchOnApproach} turns it off.
 *
 * <p>Why (match logs 3 and 4 Nov 2026, R-Solo and R-Solo-Staging on 20245): from arriving at
 * LEFT_LAUNCH_OFFSET to the first ball took 1.9 to 2.0 s, of which path end to gate open was 0.98 to
 * 1.12 s (the settle waited 0.38 to 0.57 s for under 2 in/s; a launcher overshoot after a dip kept it
 * off speed about 0.7 s more). At the path end the robot was already within about 1.2° and 0.35 in
 * of its end pose, moving about 10 in/s. The distance and turn knobs are <b>not measured</b>: the
 * hit rate firing on the approach against firing still is a robot check ({@code doc/robot-todo.md}).
 *
 * <h2>What "Empty" means today</h2>
 *
 * <p>A throat beam behind the gate ({@code BeamBreakSubsystem.throatBehindGate}) counts nothing: the
 * volley keeps its shot counter and set time, and also ends once that beam has stayed clear with the
 * lane empty ({@link Launcher#gateClearS()}). Everything below is a beam past the gate.
 *
 * <p>With the throat beam fitted ({@code BeamBreakSubsystem}), a volley ends on its balls: the
 * moment the 4th passes the throat, or once the throat goes quiet with the lane empty, and a stuck
 * feed is backed out and fed again ({@link #throatBackstopS}, {@link #unjamTries}; the private
 * {@code Volley} class). Without it, everything below holds.
 *
 * <p>Without a beam the robot cannot know it is empty. The nearest honest fact is
 * that a {@code LaunchAll} has run its whole feed since the wait began: the Autos use
 * {@code Empty} to end the step that fires, so this moves them on as soon as the shots are out
 * instead of at the step's time limit. A sensor (#164, #181) replaces this with the real thing.
 * {@code LaunchOne} does not count: one shot does not empty the robot.
 *
 * <p>The feed times are {@code @Configurable} and <b>not measured</b>: how long the feeder has to
 * run to push four pieces, or one, through the wheels is a number from the bench, set in Panels
 * and written back here. A card that reaches the wheels' speed but never fed a piece shows up as
 * a {@code LaunchAll} that "finished" with nothing scored, which is what the time limit on the
 * firing step and {@code AutoKit}'s command timeout already catch.
 *
 * <p>The cards hold no requirements, per CLAUDE.md: a card that required the launcher or feeder
 * would interrupt that subsystem's {@code periodic()}. They call the subsystems' request methods
 * and the subsystems' own {@code update()} does the work.
 *
 * <p>Pure apart from the clock: the launcher and feeder are two small interfaces, so a test drives
 * the cards through Ivy's scheduler with fakes and a made-up clock, the way {@code AutoKitTest}
 * does.
 */
@Configurable
public final class LaunchCards {

    /**
     * How long the feeder runs for {@code LaunchAll}, seconds. 3.5: in six R Simple runs on 20245
     * (1 Nov 2026) the last ball left 2.4 to 3.0 s after the gate opened, read from the launcher's
     * dip per ball in the match logs; 3.5 kept a margin and saved 3.5 s over the 7.0 tried first.
     * Kept at 3.5 on 6 Nov 2026 after a look at every Auto LaunchAll on 5 and 6 Nov (52 feeds): the
     * counted shots all came by 3.01 s, but the shot count misses balls (most feeds counted fewer than
     * four, and some show ball-sized wheel dips the count did not take), so a shorter feed is not
     * shown to be safe. Video of a few volleys decides it. Square's volley in TeleOp does not use
     * this: it feeds while held. A step's time limit never cuts a feed that has started
     * (AutoRegistry.holdTimeLimitsWhile).
     *
     * <p><b>2.4 since 22 Nov 2026</b>, for every {@code LaunchAll} after an Auto's first
     * ({@link #firstLaunchAllFeedS} is the first's), deliberately thin: the mentor's call, to win the
     * time back until a sensor at the feeder ends the feed as the last ball passes. Read from every Auto
     * LaunchAll on both robots, 12 to 21 Nov plus the Belding and Fruitport events (311 feeds, balls
     * timed from the gate opening by the wheels' dips and the shot counter): balls leave on a steady
     * rhythm about 0.45 s apart, the 4th a median 2.05 s after the gate and 2.32 s in 90% of volleys.
     * At 2.4 a later volley leaves a ball behind about 9% of the time (13 of 142), almost all of them
     * a ball that hung up for over a second mid-volley. A ball left behind is not lost: it stays in
     * the lane for the next volley, though it takes one of the next pick's four places. Each saves
     * 1.1 s over 3.5 whenever the 4th shot is not counted (44% of volleys, 21 Nov).
     */
    public static double launchAllFeedS = 2.4;

    /**
     * {@link #launchAllFeedS} for the first {@code LaunchAll} these cards feed, an Auto's preloads:
     * 2.8 since 22 Nov 2026. That volley starts with the wheels coming up from rest and its balls
     * run about 0.3 to 0.5 s later than a later volley's; at 2.8 it leaves a ball behind about 4.5%
     * of the time (6 of 134, same logs). Thin on purpose, as {@link #launchAllFeedS}.
     */
    public static double firstLaunchAllFeedS = 2.8;

    /**
     * With the throat beam fitted ({@code BeamBreakSubsystem.throatFitted}), every volley's set feed
     * time, in place of {@link #launchAllFeedS} and {@link #firstLaunchAllFeedS}: only a backstop. The
     * beam ends a volley the moment its {@link #launchAllEndAfterShots}th ball leaves (median 2.05 s
     * after the gate, 12 to 21 Nov 2026), or 0.9 s after a short volley's last ball with the lane
     * empty ({@code BeamBreakSubsystem.quietS}), so the thin timers that leave a hung ball behind
     * are not needed: 3.5, the timer before 22 Nov, lets a ball that hung over a second still go.
     */
    public static double throatBackstopS = 3.5;

    /**
     * How long a stuck feed is backed out, seconds (throat beam only): the feeder reverses and the
     * wheels back up gently ({@code LauncherSubsystem.reverseTicksPerSecond}), then the card waits for
     * launch speed and feeds again. 0.3: the TeleOp's unjam backs out a ball in a tap. <b>Not measured.</b>
     */
    public static double unjamReverseS = 0.3;

    /** Unjams a stuck volley gets before it gives up and the card ends (throat beam only). 1; 0: none. */
    public static int unjamTries = 1;

    /**
     * A {@code LaunchAll} ends its feed once the launcher has counted this many shots since the
     * gate opened, rather than at {@link #launchAllFeedS}, which stays the backstop. 4: a volley
     * holds four. 0 or NaN: off, every feed runs its set time.
     *
     * <p>A count too low costs nothing (the feed runs its set time, as before); only a count too high
     * would end a feed with a ball still in the robot. On 4 Nov 2026 (20245), replayed with the shot
     * detector of 5 Nov, every R-Solo volley that counted 4 had its four dips evenly spaced, 0.36 to
     * 0.62 s apart, as the feeder delivers balls, and R-Solo 23:41's first volley matches its video
     * (four balls, four counts). The fourth count came 2.0 to 2.6 s after the gate opened, so a full
     * volley ends about 1 to 1.5 s sooner. Still to prove on video: {@code doc/robot-todo.md}.
     */
    public static double launchAllEndAfterShots = 4;

    /**
     * The longest a launch card waits for the launcher to reach speed, seconds; after that it feeds
     * at whatever speed the wheels have reached and says so in the match log. A slightly slow shot
     * beats none: on 1 Nov 2026 (20245) R Quals' first LaunchAll never fed, its right wheel stuck at
     * 1520 of 1625 on a sagging battery, until the step timed out and the robot drove off with all
     * four balls. Spin-up takes about 1.2 s on a good battery. NaN or below 0 counts as 0.
     */
    public static double maxSpinUpWaitS = 2.5;

    /** How long the feeder runs for {@code LaunchOne}, seconds. <b>Not measured.</b> */
    public static double launchOneFeedS = 0.5;

    /**
     * Whether a {@code LaunchAll} right after a path opens the gate on the approach (see the class
     * comment) rather than only once the robot has settled. On until a robot check shows the hit
     * rate on the approach is worse than firing still; the condition's knobs are
     * {@link PathEndSettle}'s and <b>not measured</b>.
     */
    public static boolean launchOnApproach = true;

    /**
     * How far ahead of launch speed a launch card may open the gate, seconds: it opens once the
     * launcher forecasts it will be at speed within this long ({@code LauncherSubsystem.secondsToSpeed}),
     * because the first ball still has to travel to the wheels. <b>0: off</b>, the gate waits for
     * {@code atSpeed()} exactly as before; NaN or below 0 is off too. Everything else about the gate
     * (aim, settle, the approach's checks, {@link #maxSpinUpWaitS}) is unchanged.
     *
     * <p>Why (Belding Qualifier logs, 7 Nov 2026, 17 Autos): in all nine that fired their preloads at
     * PLAY the gate was waiting on speed, at-speed coming 0.01 to 0.05 s before the gate opened, 0.42
     * to 0.70 s after PLAY on 19429 and 0.52 to 1.35 s on 20245. The first ball then took 0.58 to
     * 0.93 s from the gate to the wheels. Several later volleys waited on speed too, the wheels coming
     * down from the preloads' speed to the next spot's. So up to about 0.5 s is there to win, on
     * the preloads most.
     *
     * <p>The risks, and why it is off until a robot check: a ball already sitting against the wheels
     * leaves at once (three later volleys at Belding showed a first ball 0.07 to 0.14 s after the
     * gate), so it goes out short of speed; a wheel that overshoots after the gate opened (20245's
     * left, 120 over for a second) sends the first ball fast, where today the gate waits it out. Keep
     * it under the fastest ball's trip, 0.58 s: 0.3 to 0.4 is the place to start. The check is in
     * {@code doc/robot-todo.md}.
     */
    public static double gateLeadS = 0.0;

    /** What a launch card needs from the launcher. {@code LauncherSubsystem} has both methods. */
    public interface Launcher {
        void spinUp();
        /**
         * The path a {@code LaunchAll} approach runs for ends at {@code launchPose}: spin up for a
         * launch from there. Nothing by default; the robot's launcher uses it for the speed table.
         */
        default void spinUpFor(Pose launchPose) { }
        boolean atSpeed();
        /**
         * Shots the launcher's detector has counted, or -1 if it has none. A {@code LaunchAll} ends
         * its feed at {@link #launchAllEndAfterShots} of them; -1 leaves every feed its set time.
         */
        default int shots() { return -1; }
        /**
         * How long until {@link #atSpeed()} is due, seconds ({@code LauncherSubsystem.secondsToSpeed}):
         * 0 at speed, infinite when it is not coming. The default knows only {@link #atSpeed()}.
         */
        default double secondsToSpeed() { return atSpeed() ? 0.0 : Double.POSITIVE_INFINITY; }
        /**
         * Balls the throat beam has seen pass since the OpMode started, or -1 with none fitted. With a
         * beam, a volley ends on it ({@code FeedWatch}) and the shot count is only logged.
         */
        default int throatBalls() { return -1; }
        /** How long the throat beam has been broken now, seconds; 0 while clear or with none fitted. */
        default double throatBlockedS() { return 0.0; }
        /** What the lane holds apart from the throat ({@code Robot.laneHolds}): it tells an empty lane from a stuck feed. */
        default FeedWatch.Lane lane() { return FeedWatch.Lane.UNKNOWN; }
        /**
         * How long the beam behind the gate ({@code BeamBreakSubsystem.throatBehindGate}) has been clear,
         * seconds, 0 while a ball waits in it; -1 with no such beam. Such a beam counts nothing, so
         * {@link #throatBalls()} is -1 beside it and the feed keeps its shot counter and set time, and
         * also ends once this has been clear {@code gateClearEmptyS} with the lane empty.
         */
        default double gateClearS() { return -1.0; }
    }

    /** What a launch card needs from the feeder. {@code FeederSubsystem} has both methods. */
    public interface Feeder {
        void feed();
        void idle();
        /** Back a stuck ball out: the robot's feeder reverses (the gate open, the wheels backing up). */
        default void reverse() { idle(); }
    }

    /**
     * What {@code LaunchAllAimed} needs from the turret: aim it at the CELL at the robot's end from
     * where the robot stands, fall back to its home angle when it cannot, and say when a feed may
     * start. {@code AimedVolley} is the robot's.
     */
    public interface AimedShot {
        /** Begin aiming (the card has started). */
        void start();
        /**
         * One loop of aiming the turret. True while it aims, so the gate waits for {@link #onTarget()};
         * false when it cannot (locked forward, a dropped hold): it holds its home angle, where a
         * {@code LaunchAll} fires, and the gate does not wait for it.
         */
        boolean aim();
        /**
         * Why no feed may start now, or null: no referenced pose, no alliance, or too late for the last
         * piece to be out before AUTO ends. Asked at the start and every loop until the gate opens; a
         * reason ends the card without a feed.
         */
        String refusal();
        /** The turret is on its aim and the aim is inside the firing arc: a feed may start. */
        boolean onTarget();
        /** The card has ended: the turret back to its home angle. */
        void done();
    }

    /** Whether the robot may open the gate on the approach to a path's end: {@link PathEndSettle#readyOnApproach}. */
    public interface ApproachCheck {
        boolean ready(Pose end, Pose pose, double remainingIn);
    }

    private static final ApproachCheck NEVER_ON_APPROACH = (end, pose, remainingIn) -> false;

    private final Launcher launcher;
    private final Feeder feeder;
    private final BooleanSupplier settled;
    private final Consumer<String> note;
    private final DoubleSupplier secondsNow;
    private final ApproachCheck onApproach;
    private int launchAllsCompleted;
    private int feeding;
    /** Where the path a {@code LaunchAll} approach is running for ends; null outside one. */
    private Pose approachEnd;
    /** When the approach opened the gate, seconds; NaN until it has, and once a card takes it over. */
    private double approachFedAt = Double.NaN;
    private int approachShots;
    private int approachBalls;
    /** Whether the approach's open feed is the first volley's ({@link #firstLaunchAllFeedS}). */
    private boolean approachFirst;
    /** {@code LaunchAll} and {@code LaunchAllAimed} feeds started so far: the first gets {@link #firstLaunchAllFeedS}. */
    private int volleyFeedsStarted;
    private final AutoRegistry.Approach launchAllApproach = new AutoRegistry.Approach() {
        @Override public void started(Pose end) {
            approachEnd = end;
            launcher.spinUpFor(end);
        }

        @Override public void approaching(Pose pose, double remainingIn) {
            if (!launchOnApproach || approachEnd == null || !Double.isNaN(approachFedAt)) return;
            if (!speedReady() || !onApproach.ready(approachEnd, pose, remainingIn)) return;
            approachShots = launcher.shots();
            approachBalls = launcher.throatBalls();
            approachFedAt = secondsNow.getAsDouble();
            approachFirst = claimFirstVolley();
            feeder.feed();
            feeding++;
            note.accept(String.format(Locale.US, "LaunchAll opened the gate on the approach, %.1f in from the path end%s",
                    remainingIn, aheadOfSpeed()));
        }

        @Override public boolean committed() {
            return !Double.isNaN(approachFedAt);
        }

        @Override public void cancel() {
            approachEnd = null;
            if (Double.isNaN(approachFedAt)) return;
            approachFedAt = Double.NaN;
            feeder.idle();
            feeding--;
            note.accept("LaunchAll did not follow its approach: gate closed");
        }
    };

    /** {@link #LaunchCards(Launcher, Feeder, BooleanSupplier, ApproachCheck, Consumer, DoubleSupplier)} that never launches on the approach. */
    public LaunchCards(Launcher launcher, Feeder feeder, BooleanSupplier settled, Consumer<String> note,
                       DoubleSupplier secondsNow) {
        this(launcher, feeder, settled, NEVER_ON_APPROACH, note, secondsNow);
    }

    /**
     * @param settled    true once the robot is aimed and slowing ({@link PathEndSettle#settled})
     * @param onApproach whether the gate may open on the approach ({@link PathEndSettle#readyOnApproach})
     * @param note       where "fed before the robot settled" goes (the match log)
     */
    public LaunchCards(Launcher launcher, Feeder feeder, BooleanSupplier settled, ApproachCheck onApproach,
                       Consumer<String> note, DoubleSupplier secondsNow) {
        if (launcher == null || feeder == null || settled == null || onApproach == null || secondsNow == null) {
            throw new IllegalArgumentException("LaunchCards needs a launcher, a feeder, a settle check and a clock");
        }
        this.launcher = launcher;
        this.feeder = feeder;
        this.settled = settled;
        this.onApproach = onApproach;
        this.note = note == null ? line -> { } : note;
        this.secondsNow = secondsNow;
    }

    /**
     * The cards for a real robot, on the system clock, gated on {@code settle}. With
     * {@link LaunchSpeedTable#enabled} on, each spin-up asks for the table's speed for the range to
     * the CELL on the launch pose's half ({@link #rangeToHalfCellIn}), and logs it: a
     * {@code LaunchAll}'s approach spins up for its path's end as the path starts, so the wheels are
     * there on arrival; the {@code SpinUp} card and a card's own spin-up use where the robot is,
     * by the camera's range when it sees one of our CELLs ({@link #cameraRangeIn}), else the pose's.
     * Held within half the launcher's tolerance ({@link LaunchSpeedTable#held}), so the card's
     * spin-up at the path end, an inch from the approach's pose, never moves the target mid-feed
     * (a new target re-arms at-speed and the shot detector). Off, or with no range, it is the
     * configured speed, exactly as before the table.
     *
     * @param alliance ours, read at each spin-up; null or UNKNOWN gives the configured speed
     */
    public static LaunchCards forRobot(Robot robot, PathEndSettle settle, Consumer<String> note,
                                       Supplier<Alliance> alliance) {
        Consumer<String> log = note == null ? line -> { } : note;
        Launcher launcher = new Launcher() {
            /** The table speed last asked for, ticks/s; NaN before the first. */
            private double heldTps = Double.NaN;

            @Override public void spinUp() {
                if (!LaunchSpeedTable.enabled) {
                    robot.launcher.spinUp();
                    return;
                }
                // Where the robot is: the camera's range when it sees one of our CELLs (what the
                // table was measured with, and right even when the pose is not), else the pose's.
                Alliance ours = alliance == null ? null : alliance.get();
                double camera = cameraRangeIn(robot.vision.sightings(), ours, SIGHTING_FRESH_MS);
                Pose at = robot.drive.poseReferenced() ? robot.drive.pose() : null;
                double pose = rangeToHalfCellIn(at, ours);
                if (!Double.isNaN(camera)) {
                    spinUpAtRange(camera, Double.isNaN(pose) ? "here, camera"
                            : String.format(Locale.US, "here, camera; pose says %.1f in", pose));
                    return;
                }
                spinUpAtRange(pose, "here, pose");
            }

            @Override public void spinUpFor(Pose launchPose) {
                if (LaunchSpeedTable.enabled) {
                    spinUpAtRange(rangeToHalfCellIn(launchPose, alliance == null ? null : alliance.get()),
                            "at the path end");
                }
            }

            private void spinUpAtRange(double range, String where) {
                double answer = LaunchSpeedTable.launchSpeed(range, LauncherSubsystem.targetTicksPerSecond);
                double tps = LaunchSpeedTable.held(heldTps, answer, LauncherSubsystem.toleranceTicksPerSecond / 2.0);
                boolean changed = Double.compare(tps, heldTps) != 0;
                heldTps = tps;
                robot.launcher.spinUp(tps);
                if (!changed) return;
                if (Double.isNaN(range)) {
                    log.accept(String.format(Locale.US, "launcher: speed table on, no range: configured %.0f ticks/s", tps));
                } else if (Double.isNaN(LaunchSpeedTable.ticksPerSecondFor(range))) {
                    log.accept(String.format(Locale.US,
                            "launcher: %.0f in (%s) is past the speed table, not believed: configured %.0f ticks/s",
                            range, where, tps));
                } else {
                    log.accept(String.format(Locale.US, "launcher: speed table %.0f ticks/s for %.0f in (%s)", tps, range, where));
                }
            }
            @Override public boolean atSpeed() { return robot.launcher.atSpeed(); }
            @Override public int shots() { return robot.launcher.shots(); }
            @Override public double secondsToSpeed() { return robot.launcher.secondsToSpeed(); }
            @Override public int throatBalls() {
                return robot.beams.countingThroat() == null ? -1 : robot.beams.countingThroat().passes();
            }
            @Override public double throatBlockedS() {
                return robot.beams.countingThroat() == null ? 0.0 : robot.beams.countingThroat().blockedForMs() / 1000.0;
            }
            @Override public FeedWatch.Lane lane() { return robot.laneHolds(); }
            @Override public double gateClearS() { return robot.beams.gateClearS(); }
        };
        Feeder feeder = new Feeder() {
            @Override public void feed() { robot.feeder.feed(); }
            @Override public void idle() { robot.feeder.idle(); }
            @Override public void reverse() { robot.feeder.reverse(); }
        };
        return new LaunchCards(launcher, feeder, settle::settled, settle::readyOnApproach, note,
                () -> System.nanoTime() / 1e9);
    }

    /** A camera sighting older than this, ms, is not a range: as the TeleOp's FIRE takes it. */
    static final long SIGHTING_FRESH_MS = 500;

    /**
     * The camera's ground range from the turret axis to the nearest fresh sighting of one of our
     * CELLs, inches; NaN with none, or with no alliance. From the turret axis, as the pose's range
     * ({@link #rangeToHalfCellIn}) and the speed table's rows are, so the two sources agree; the
     * TeleOp's FIRE and Square take it the same way.
     */
    static double cameraRangeIn(List<CellSighting> sightings, Alliance alliance, long freshMs) {
        if (sightings == null || alliance == null || alliance == Alliance.UNKNOWN) return Double.NaN;
        double best = Double.NaN;
        for (CellSighting sighting : sightings) {
            if (sighting == null || sighting.ageMs() > freshMs) continue;
            if (sighting.cell() == null || !sighting.cell().belongsTo(alliance)) continue;
            double range = sighting.groundRangeFromIn(TurretSubsystem.turretForwardIn, TurretSubsystem.turretLeftIn);
            if (Double.isNaN(range)) continue;
            if (Double.isNaN(best) || range < best) best = range;
        }
        return best;
    }

    /**
     * The horizontal range from the turret at {@code pose} to the raised row of our CELL on that
     * pose's half of the field, inches: the CELL an Auto fires at, as the launch spots choose it
     * ({@code LaunchSpots}: each CELL is scored from its own half). NaN without a pose, an alliance
     * or a field point.
     */
    static double rangeToHalfCellIn(Pose pose, Alliance alliance) {
        if (pose == null || alliance == null || alliance == Alliance.UNKNOWN) return Double.NaN;
        HiveCell cell = TurretAim.nearEndCell(alliance, pose.y(), null, HiveFieldPoints.AXLE_Y_IN, 0.0);
        Vec3 row = cell == null ? null : HiveFieldPoints.rowCentre(cell, HiveCellState.UP);
        if (row == null) return Double.NaN;
        return TurretAim.rangeFromFieldIn(pose.x(), pose.y(), pose.heading(),
                TurretSubsystem.turretForwardIn, TurretSubsystem.turretLeftIn, row.x(), row.y());
    }

    /**
     * The launcher's half of opening the gate: at speed, or with {@link #gateLeadS} on, due at speed
     * within it.
     */
    private boolean speedReady() {
        if (launcher.atSpeed()) return true;
        double lead = gateLeadS;
        return lead > 0.0 && launcher.secondsToSpeed() <= lead;
    }

    /** For the match log: ", 0.25 s ahead of launch speed" when the gate opens before it, else empty. */
    private String aheadOfSpeed() {
        if (launcher.atSpeed()) return "";
        return String.format(Locale.US, ", %.2f s ahead of launch speed (gateLeadS %.2f)",
                launcher.secondsToSpeed(), gateLeadS);
    }

    /** {@code SpinUp}: ask the launcher for speed and finish at once. The wheels come up while the robot drives. */
    public Command spinUp() {
        return Commands.instant(launcher::spinUp);
    }

    /**
     * {@code LaunchAll}: spin up, wait for speed and a settled robot, feed until the launcher counts
     * {@link #launchAllEndAfterShots} shots or for {@link #launchAllFeedS}, whichever comes first,
     * idle the feeder. If its {@link #approach()} opened the gate on the drive in, it waits for
     * nothing and ends {@link #launchAllFeedS} after the gate opened.
     */
    public Command launchAll() {
        return launch("LaunchAll", true);
    }

    /**
     * {@code LaunchAll}'s approach, registered with it ({@code AutoRegistry.command(..., approach)}):
     * the path card right before a {@code LaunchAll} card runs it, and it opens the gate once
     * {@link PathEndSettle#readyOnApproach} and the launcher agree (see the class comment). One
     * object, shared by every {@code LaunchAll} card of these cards.
     */
    public AutoRegistry.Approach approach() {
        return launchAllApproach;
    }

    /**
     * {@code LaunchAllAimed}: a volley from where the robot stands, parked, with the turret aimed at
     * the CELL at the robot's end ({@code AimedVolley}, the TeleOp's {@code TurretAimer}). It spins
     * up, aims every loop, and opens the gate only once the launcher is at speed and, while the turret
     * aims, {@code shot} is on target inside the firing arc (a turret that cannot aim, locked forward
     * or with a dropped hold, holds home and fires from there, the park spot facing the CELL); then it
     * feeds as {@link #launchAll()} does, until the last
     * counted shot or {@link #launchAllFeedS}. Until the gate opens, a {@link AimedShot#refusal()}
     * ends it with nothing fed, and it says why: the robot stays where it parked. No settle, no
     * approach: the robot is not driving. Counts toward {@code Empty} like a {@code LaunchAll}.
     */
    public Command launchAllAimed(AimedShot shot) {
        if (shot == null) throw new IllegalArgumentException("LaunchAllAimed needs a turret to aim");
        final Volley volley = new Volley("LaunchAllAimed", true);
        final boolean[] refused = new boolean[1];
        final double[] startedAt = new double[1];
        return new CommandBuilder()
                .setStart(() -> {
                    volley.reset();
                    startedAt[0] = secondsNow.getAsDouble();
                    String why = shot.refusal();
                    refused[0] = why != null;
                    if (refused[0]) {
                        note.accept("LaunchAllAimed: not firing, " + why + "; staying parked");
                        return;
                    }
                    launcher.spinUp();
                    shot.start();
                })
                .setExecute(() -> {
                    if (refused[0]) return;
                    boolean aiming = shot.aim();
                    if (volley.fed()) return;
                    String why = shot.refusal();
                    if (why != null) {
                        refused[0] = true;
                        note.accept(String.format(Locale.US, "LaunchAllAimed: not firing, %s (%.2f s in, the gate never opened); staying parked",
                                why, secondsNow.getAsDouble() - startedAt[0]));
                        return;
                    }
                    if (!speedReady() || (aiming && !shot.onTarget())) return;
                    note.accept(String.format(Locale.US, "LaunchAllAimed opened the gate, %s, %.2f s after it started%s",
                            aiming ? "the turret on target" : "the turret at home",
                            secondsNow.getAsDouble() - startedAt[0], aheadOfSpeed()));
                    volley.begin(secondsNow.getAsDouble(), claimFirstVolley());
                })
                .setDone(() -> refused[0] || volley.done(secondsNow.getAsDouble()))
                .setEnd(end -> {
                    shot.done();
                    if (!volley.fed()) {
                        if (!refused[0]) note.accept("LaunchAllAimed ended before the gate opened (" + end + "): nothing fed");
                        return;
                    }
                    volley.end(end);
                    if (end == EndCondition.NATURALLY) launchAllsCompleted++;
                });
    }

    /** {@code LaunchOne}: as {@link #launchAll()} with {@link #launchOneFeedS}; does not count toward Empty. */
    public Command launchOne() {
        return launch("LaunchOne", false);
    }

    /**
     * Whether a launch card is feeding now: from the feed's start to the card's end. The Autos hold
     * their steps' time limits while it is ({@code AutoRegistry.holdTimeLimitsWhile}), so a feed
     * always runs to its end: its set time, or a {@code LaunchAll}'s last counted shot.
     */
    public boolean feeding() {
        return feeding > 0;
    }

    /** How many {@code LaunchAll} cards have run their whole feed since this object was made. */
    public int launchAllsCompleted() {
        return launchAllsCompleted;
    }

    /**
     * The {@code Empty} check for one wait: true once a {@code LaunchAll} has finished its feed
     * after the wait started. Registered with {@code triggerSince}, so each wait gets its own.
     */
    public BooleanSupplier emptySince() {
        final int before = launchAllsCompleted;
        return () -> launchAllsCompleted > before;
    }

    private Command launch(String name, boolean countsAsEmptying) {
        final Volley volley = new Volley(name, countsAsEmptying);
        final double[] startedAt = new double[1];
        return new CommandBuilder()
                .setStart(() -> {
                    volley.reset();
                    startedAt[0] = secondsNow.getAsDouble();
                    launcher.spinUp();
                    // The drive in already opened the gate: this card only finishes that feed.
                    if (countsAsEmptying && !Double.isNaN(approachFedAt)) {
                        volley.adopt(approachFedAt, approachFirst, approachShots, approachBalls);
                        approachFedAt = Double.NaN;
                    }
                    approachEnd = null;
                })
                .setExecute(() -> {
                    if (volley.fed()) return;
                    double now = secondsNow.getAsDouble();
                    if (!speedReady()) {
                        if (now - startedAt[0] < nonNegative(maxSpinUpWaitS)) return;
                        // Not at speed in time: a slightly slow shot beats driving off with every ball.
                        note.accept(String.format(Locale.US, "%s fed below launch speed: not at speed %.2f s after it started",
                                name, now - startedAt[0]));
                        volley.begin(now, countsAsEmptying && claimFirstVolley());
                        return;
                    }
                    if (settled.getAsBoolean()) {
                        String ahead = aheadOfSpeed();
                        if (!ahead.isEmpty()) note.accept(name + " opened the gate" + ahead);
                        volley.begin(now, countsAsEmptying && claimFirstVolley());
                    } else if (now - startedAt[0] >= PathEndSettle.maxWaitS()) {
                        note.accept(String.format(Locale.US, "%s fed before the robot settled: %.2f s since it started",
                                name, now - startedAt[0]));
                        volley.begin(now, countsAsEmptying && claimFirstVolley());
                    }
                })
                .setDone(() -> volley.done(secondsNow.getAsDouble()))
                .setEnd(end -> {
                    if (volley.fed()) {
                        volley.end(end);
                    } else {
                        feeder.idle();
                    }
                    if (countsAsEmptying && end == EndCondition.NATURALLY) {
                        launchAllsCompleted++;
                    }
                });
    }

    /** True for the first volley feed these cards start ({@link #firstLaunchAllFeedS}), and counts it. */
    private boolean claimFirstVolley() {
        return volleyFeedsStarted++ == 0;
    }

    /** A volley's set feed time: {@link #firstLaunchAllFeedS} for the first, else {@link #launchAllFeedS}. */
    private static double volleyFeedS(boolean first) {
        return first ? firstLaunchAllFeedS : launchAllFeedS;
    }

    /**
     * One card's feed, from the gate opening to the card's end: when it ends (the throat beam's
     * verdict with one fitted, else the shot counter or the set time) and its recovery from a stuck
     * feed. One per card, reset each time the card starts.
     *
     * <p>With the throat beam ({@link Launcher#throatBalls()} not -1) the set time is only a backstop,
     * {@link #throatBackstopS}: a {@code LaunchAll} ends the moment its {@link #launchAllEndAfterShots}th
     * ball passes the throat, or once the throat has gone quiet with the lane empty
     * ({@link FeedWatch}). A ball held in the throat beam is backed out ({@link #unjamReverseS}: the
     * feeder and the wheels reverse, the gate open), then fed again once the wheels are back at speed,
     * up to {@link #unjamTries} times; after that the card ends, the ball left for the next volley. A
     * jam in the lane (no ball coming, the lane holding, the throat clear) is the lane entry's
     * ({@code IntakeSubsystem.clearFeedJam}, {@code FeedJamClear}, which stands down while a ball is
     * held in the throat): the card notes it and feeds on, to its backstop. With that switch off, the
     * feeder backs a lane jam out here too. The unjam's time does not count against
     * the backstop. Without the beam, nothing here changes: the shot counter and the timer, as before.
     */
    private final class Volley {
        private final String name;
        private final boolean volleyCard;
        private final FeedWatch watch = new FeedWatch();
        private double fedAt = Double.NaN;
        private boolean first;
        private int shotsAtFeed;
        private boolean allShotsOut;
        private String endWhy = "";
        /** 0 feeding, 1 backing out an unjam, 2 waiting for speed after it. */
        private int phase;
        private double phaseAt;
        private double pausedS;
        private int unjams;
        /** This feed's lane jam has been noted as the lane entry's to clear. */
        private boolean leftToLane;

        Volley(String name, boolean volleyCard) {
            this.name = name;
            this.volleyCard = volleyCard;
        }

        void reset() {
            fedAt = Double.NaN;
            first = false;
            allShotsOut = false;
            endWhy = "";
            phase = 0;
            pausedS = 0;
            unjams = 0;
            leftToLane = false;
            watch.stop();
        }

        boolean fed() {
            return !Double.isNaN(fedAt);
        }

        /** The gate opens now: the feed starts. */
        void begin(double now, boolean firstVolley) {
            shotsAtFeed = launcher.shots();
            first = firstVolley;
            int balls = launcher.throatBalls();
            if (balls >= 0 || launcher.gateClearS() >= 0) watch.start(now, Math.max(balls, 0));
            feed(now);
        }

        /** The approach already opened the gate at {@code at}: carry its feed on. */
        void adopt(double at, boolean firstVolley, int shots, int balls) {
            fedAt = at;
            first = firstVolley;
            shotsAtFeed = shots;
            if (balls >= 0 || launcher.gateClearS() >= 0) watch.start(at, Math.max(balls, 0));
        }

        private void feed(double now) {
            feeder.feed();
            fedAt = now;
            feeding++;
        }

        private double limitS() {
            if (!volleyCard) return nonNegative(launchOneFeedS);
            return launcher.throatBalls() >= 0 ? nonNegative(throatBackstopS) : nonNegative(volleyFeedS(first));
        }

        private double endAfter() {
            return volleyCard ? launchAllEndAfterShots : 1;
        }

        boolean done(double now) {
            if (!fed()) return false;
            int balls = launcher.throatBalls();
            if (phase == 1) {
                if (now - phaseAt < nonNegative(unjamReverseS)) return false;
                feeder.idle();
                phase = 2;
                phaseAt = now;
                return false;
            }
            if (phase == 2) {
                if (!speedReady() && now - phaseAt < nonNegative(maxSpinUpWaitS)) return false;
                pausedS += now - phaseAt + nonNegative(unjamReverseS);
                feeder.feed();
                watch.restart(now);
                phase = 0;
                note.accept(String.format(Locale.US, "%s fed again after its unjam, %d ball%s out so far",
                        name, watch.balls(), watch.balls() == 1 ? "" : "s"));
                return false;
            }
            if (now - fedAt - pausedS >= limitS()) {
                endWhy = balls >= 0
                        ? String.format(Locale.US, ", ended at its %.1f s backstop", limitS())
                        : volleyCard ? String.format(Locale.US, ", ended at its %.1f s timer", limitS()) : "";
                return true;
            }
            if (balls < 0) {
                double enough = endAfter();
                int shots = launcher.shots();
                allShotsOut = volleyCard && enough >= 1 && shots >= 0 && shots - shotsAtFeed >= enough;
                if (allShotsOut) endWhy = ", ended at the last counted shot";
                if (allShotsOut || !volleyCard) return allShotsOut;
                // A beam behind the gate: the volley's last ball has passed the gate once it stays clear
                // with the lane empty. It sees no jam at the wheels; a lane jam is the lane entry's.
                double clearS = launcher.gateClearS();
                if (clearS < 0) return false;
                FeedWatch.Verdict verdict = watch.stepBehindGate(now, clearS, launcher.lane(),
                        BeamBreakSubsystem.gateClearEmptyS, BeamBreakSubsystem.stuckGapS);
                if (verdict == FeedWatch.Verdict.EMPTY) {
                    endWhy = ", ended: " + watch.why();
                    return true;
                }
                if (verdict == FeedWatch.Verdict.STUCK && !leftToLane) {
                    leftToLane = true;
                    note.accept(String.format(Locale.US, "%s feed stuck (%s): %s", name, watch.why(),
                            IntakeSubsystem.clearFeedJam ? "left to the lane's jam clear" : "feeding on to its set time"));
                }
                return false;
            }
            FeedWatch.Verdict verdict = watch.step(now, balls, launcher.throatBlockedS(), launcher.lane(), endAfter(),
                    BeamBreakSubsystem.quietS, BeamBreakSubsystem.firstBallWithinS, BeamBreakSubsystem.jamBlockedS,
                    BeamBreakSubsystem.stuckGapS);
            switch (verdict) {
                case ALL_OUT:
                case EMPTY:
                    endWhy = ", ended: " + watch.why();
                    return true;
                case STUCK:
                    // A jam in the lane, the throat clear: the lane entry's jam clear backs it out
                    // (IntakeSubsystem.clearFeedJam, FeedJamClear) while this feed goes on. One jam,
                    // one handler; with that off, the feeder backs it out here instead.
                    if (IntakeSubsystem.clearFeedJam) {
                        if (!leftToLane) {
                            leftToLane = true;
                            note.accept(String.format(Locale.US, "%s feed stuck (%s): left to the lane's jam clear",
                                    name, watch.why()));
                        }
                        return false;
                    }
                    // fall through
                case HELD:
                    if (unjams < unjamTries) {
                        unjams++;
                        note.accept(String.format(Locale.US, "%s feed stuck (%s): backing out, unjam %d of %d",
                                name, watch.why(), unjams, unjamTries));
                        feeder.reverse();
                        phase = 1;
                        phaseAt = now;
                        return false;
                    }
                    endWhy = String.format(Locale.US, ", gave up: %s after %d unjam%s", watch.why(), unjams,
                            unjams == 1 ? "" : "s");
                    return true;
                default:
                    return false;
            }
        }

        void end(EndCondition end) {
            feeder.idle();
            feeding--;
            double now = secondsNow.getAsDouble();
            int balls = launcher.throatBalls();
            if (balls >= 0) {
                // Logged against the dip count, to judge the beam (and the dips) by video.
                note.accept(String.format(Locale.US, "%s fed %.2f s (%s%s): %d balls through the throat, %d shots detected",
                        name, now - fedAt, end, endWhy, watch.balls(), launcher.shots() - shotsAtFeed));
            } else if (launcher.gateClearS() >= 0) {
                // Logged against the dip count, to judge the gate beam's end (and the dips) by video.
                note.accept(String.format(Locale.US, "%s fed %.2f s (%s%s): gate beam behind the gate, %d shots detected",
                        name, now - fedAt, end, endWhy, launcher.shots() - shotsAtFeed));
            } else if (launcher.shots() >= 0) {
                // Logged to judge the shot detector against what was seen (video).
                note.accept(String.format(Locale.US, "%s fed %.2f s (%s%s): %d shots detected",
                        name, now - fedAt, end, endWhy, launcher.shots() - shotsAtFeed));
            }
            watch.stop();
            fedAt = Double.NaN;
        }
    }

    /** A Panels-editable time clamped to [0, ∞), NaN as 0: a typo makes a short feed, never a stuck card. */
    static double nonNegative(double seconds) {
        return seconds > 0.0 ? seconds : 0.0;
    }
}
