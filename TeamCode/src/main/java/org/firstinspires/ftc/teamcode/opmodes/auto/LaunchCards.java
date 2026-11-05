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
 * <p>A launch card spins the launcher up, waits until it is at speed (at most
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
 * <p>There is no piece sensor yet, so the robot cannot know it is empty. The nearest honest fact is
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
     * dip per ball in the match logs; 3.5 keeps a margin and saves 3.5 s over the 7.0 tried first.
     * A step's time limit never cuts a feed that has started (AutoRegistry.holdTimeLimitsWhile).
     */
    public static double launchAllFeedS = 3.5;

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
    }

    /** What a launch card needs from the feeder. {@code FeederSubsystem} has both methods. */
    public interface Feeder {
        void feed();
        void idle();
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
    private final AutoRegistry.Approach launchAllApproach = new AutoRegistry.Approach() {
        @Override public void started(Pose end) {
            approachEnd = end;
            launcher.spinUpFor(end);
        }

        @Override public void approaching(Pose pose, double remainingIn) {
            if (!launchOnApproach || approachEnd == null || !Double.isNaN(approachFedAt)) return;
            if (!launcher.atSpeed() || !onApproach.ready(approachEnd, pose, remainingIn)) return;
            approachShots = launcher.shots();
            approachFedAt = secondsNow.getAsDouble();
            feeder.feed();
            feeding++;
            note.accept(String.format(Locale.US, "LaunchAll opened the gate on the approach, %.1f in from the path end",
                    remainingIn));
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
        };
        Feeder feeder = new Feeder() {
            @Override public void feed() { robot.feeder.feed(); }
            @Override public void idle() { robot.feeder.idle(); }
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
        return launch("LaunchAll", () -> launchAllFeedS, () -> launchAllEndAfterShots, true);
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

    /** {@code LaunchOne}: as {@link #launchAll()} with {@link #launchOneFeedS}; does not count toward Empty. */
    public Command launchOne() {
        return launch("LaunchOne", () -> launchOneFeedS, () -> 0.0, false);
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

    private Command launch(String name, DoubleSupplier feedSeconds, DoubleSupplier endAfterShots,
                           boolean countsAsEmptying) {
        final double[] fedAt = {Double.NaN};
        final boolean[] allShotsOut = new boolean[1];
        final double[] startedAt = new double[1];
        final int[] shotsAtFeed = new int[1];
        return new CommandBuilder()
                .setStart(() -> {
                    fedAt[0] = Double.NaN;
                    allShotsOut[0] = false;
                    startedAt[0] = secondsNow.getAsDouble();
                    launcher.spinUp();
                    // The drive in already opened the gate: this card only finishes that feed.
                    if (countsAsEmptying && !Double.isNaN(approachFedAt)) {
                        fedAt[0] = approachFedAt;
                        shotsAtFeed[0] = approachShots;
                        approachFedAt = Double.NaN;
                    }
                    approachEnd = null;
                })
                .setExecute(() -> {
                    if (!Double.isNaN(fedAt[0])) return;
                    double now = secondsNow.getAsDouble();
                    if (!launcher.atSpeed()) {
                        if (now - startedAt[0] < nonNegative(maxSpinUpWaitS)) return;
                        // Not at speed in time: a slightly slow shot beats driving off with every ball.
                        note.accept(String.format(Locale.US, "%s fed below launch speed: not at speed %.2f s after it started",
                                name, now - startedAt[0]));
                        shotsAtFeed[0] = launcher.shots();
                        feed(fedAt, now);
                        return;
                    }
                    if (settled.getAsBoolean()) {
                        shotsAtFeed[0] = launcher.shots();
                        feed(fedAt, now);
                    } else if (now - startedAt[0] >= PathEndSettle.maxWaitS()) {
                        note.accept(String.format(Locale.US, "%s fed before the robot settled: %.2f s since it started",
                                name, now - startedAt[0]));
                        shotsAtFeed[0] = launcher.shots();
                        feed(fedAt, now);
                    }
                })
                .setDone(() -> {
                    if (Double.isNaN(fedAt[0])) return false;
                    if (secondsNow.getAsDouble() - fedAt[0] >= nonNegative(feedSeconds.getAsDouble())) return true;
                    double enough = endAfterShots.getAsDouble();
                    int shots = launcher.shots();
                    allShotsOut[0] = enough >= 1 && shots >= 0 && shots - shotsAtFeed[0] >= enough;
                    return allShotsOut[0];
                })
                .setEnd(end -> {
                    feeder.idle();
                    if (!Double.isNaN(fedAt[0])) {
                        feeding--;
                        int shots = launcher.shots();
                        // Logged to judge the shot detector against what was seen (video).
                        if (shots >= 0) {
                            note.accept(String.format(Locale.US, "%s fed %.2f s (%s%s): %d shots detected",
                                    name, secondsNow.getAsDouble() - fedAt[0], end,
                                    allShotsOut[0] ? ", ended at the last counted shot" : "", shots - shotsAtFeed[0]));
                        }
                    }
                    if (countsAsEmptying && end == EndCondition.NATURALLY) {
                        launchAllsCompleted++;
                    }
                });
    }

    private void feed(double[] fedAt, double now) {
        feeder.feed();
        fedAt[0] = now;
        feeding++;
    }

    /** A Panels-editable time clamped to [0, ∞), NaN as 0: a typo makes a short feed, never a stuck card. */
    static double nonNegative(double seconds) {
        return seconds > 0.0 ? seconds : 0.0;
    }
}
