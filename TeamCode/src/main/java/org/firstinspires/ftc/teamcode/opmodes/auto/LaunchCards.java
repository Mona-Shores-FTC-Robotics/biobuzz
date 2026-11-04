package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.CommandBuilder;
import com.pedropathing.ivy.behaviors.EndCondition;
import com.pedropathing.ivy.commands.Commands;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.localization.HiveFieldPoints;
import org.firstinspires.ftc.teamcode.subsystems.LaunchSpeedTable;
import org.firstinspires.ftc.teamcode.subsystems.LauncherSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.TurretAim;
import org.firstinspires.ftc.teamcode.subsystems.TurretSubsystem;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.HiveCellState;
import org.firstinspires.ftc.teamcode.vision.Vec3;

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
 * settled</em> ({@link PathEndSettle}: still, and at the last path's end heading), runs the feeder
 * for a set time, then idles the feeder. A fire step after a path therefore waits for the robot,
 * not for Pedro's "done" (#193). If the robot has not settled within
 * {@link PathEndSettle#maxSettleWaitS} of the card starting, it feeds anyway and says so in the
 * match log, so a robot that never quite settles still fires. The launcher is left spinning: the Autos call {@code SpinUp} once
 * and fire several times, and spinning down between shots would cost a spin-up each time. The
 * OpMode's stop cuts the wheels at the end.
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
     * The longest a launch card waits for the launcher to reach speed, seconds; after that it feeds
     * at whatever speed the wheels have reached and says so in the match log. A slightly slow shot
     * beats none: on 1 Nov 2026 (20245) R Quals' first LaunchAll never fed, its right wheel stuck at
     * 1520 of 1625 on a sagging battery, until the step timed out and the robot drove off with all
     * four balls. Spin-up takes about 1.2 s on a good battery. NaN or below 0 counts as 0.
     */
    public static double maxSpinUpWaitS = 2.5;

    /** How long the feeder runs for {@code LaunchOne}, seconds. <b>Not measured.</b> */
    public static double launchOneFeedS = 0.5;

    /** What a launch card needs from the launcher. {@code LauncherSubsystem} has both methods. */
    public interface Launcher {
        void spinUp();
        boolean atSpeed();
        /** Shots the launcher's detector has counted, or -1 if it has none. Logged only, never acted on. */
        default int shots() { return -1; }
    }

    /** What a launch card needs from the feeder. {@code FeederSubsystem} has both methods. */
    public interface Feeder {
        void feed();
        void idle();
    }

    private final Launcher launcher;
    private final Feeder feeder;
    private final BooleanSupplier settled;
    private final Consumer<String> note;
    private final DoubleSupplier secondsNow;
    private int launchAllsCompleted;
    private int feeding;

    /**
     * @param settled true once the robot is still and aimed ({@link PathEndSettle#settled})
     * @param note    where "fed before the robot settled" goes (the match log)
     */
    public LaunchCards(Launcher launcher, Feeder feeder, BooleanSupplier settled, Consumer<String> note,
                       DoubleSupplier secondsNow) {
        if (launcher == null || feeder == null || settled == null || secondsNow == null) {
            throw new IllegalArgumentException("LaunchCards needs a launcher, a feeder, a settle check and a clock");
        }
        this.launcher = launcher;
        this.feeder = feeder;
        this.settled = settled;
        this.note = note == null ? line -> { } : note;
        this.secondsNow = secondsNow;
    }

    /**
     * The cards for a real robot, on the system clock, gated on {@code settle}. With
     * {@link LaunchSpeedTable#enabled} on, each spin-up (the {@code SpinUp} card, and every
     * {@code LaunchAll} and {@code LaunchOne} at its launch pose) asks for the table's speed for the
     * range from where the robot is to its half's CELL ({@link #rangeToHalfCellIn}), and logs it; the
     * wheels settle on it while the card waits for at-speed and the settle. Off, or with no range,
     * it is the configured speed, as before.
     *
     * @param alliance ours, read at each spin-up; null or UNKNOWN gives the configured speed
     */
    public static LaunchCards forRobot(Robot robot, PathEndSettle settle, Consumer<String> note,
                                       Supplier<Alliance> alliance) {
        Consumer<String> log = note == null ? line -> { } : note;
        Launcher launcher = new Launcher() {
            @Override public void spinUp() {
                if (!LaunchSpeedTable.enabled) {
                    robot.launcher.spinUp();
                    return;
                }
                double range = rangeToHalfCellIn(robot, alliance == null ? null : alliance.get());
                double tps = LaunchSpeedTable.launchSpeed(range, LauncherSubsystem.targetTicksPerSecond);
                robot.launcher.spinUp(tps);
                log.accept(Double.isNaN(range)
                        ? String.format(Locale.US, "launcher: speed table on, no range: configured %.0f ticks/s", tps)
                        : String.format(Locale.US, "launcher: speed table %.0f ticks/s at %.0f in", tps, range));
            }
            @Override public boolean atSpeed() { return robot.launcher.atSpeed(); }
            @Override public int shots() { return robot.launcher.shots(); }
        };
        Feeder feeder = new Feeder() {
            @Override public void feed() { robot.feeder.feed(); }
            @Override public void idle() { robot.feeder.idle(); }
        };
        return new LaunchCards(launcher, feeder, settle::settled, note, () -> System.nanoTime() / 1e9);
    }

    /**
     * The horizontal range from the turret at the robot's pose to the raised row of our CELL on the
     * robot's half of the field, inches: the CELL an Auto fires at, as the launch spots choose it
     * ({@code LaunchSpots}: each CELL is scored from its own half). NaN without a referenced pose,
     * an alliance or a field point.
     */
    static double rangeToHalfCellIn(Robot robot, Alliance alliance) {
        if (alliance == null || alliance == Alliance.UNKNOWN || !robot.drive.poseReferenced()) return Double.NaN;
        Pose pose = robot.drive.pose();
        if (pose == null) return Double.NaN;
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

    /** {@code LaunchAll}: spin up, wait for speed and a settled robot, feed for {@link #launchAllFeedS}, idle the feeder. */
    public Command launchAll() {
        return launch("LaunchAll", () -> launchAllFeedS, true);
    }

    /** {@code LaunchOne}: as {@link #launchAll()} with {@link #launchOneFeedS}; does not count toward Empty. */
    public Command launchOne() {
        return launch("LaunchOne", () -> launchOneFeedS, false);
    }

    /**
     * Whether a launch card is feeding now: from the feed's start to the card's end. The Autos hold
     * their steps' time limits while it is ({@code AutoRegistry.holdTimeLimitsWhile}), so a feed
     * always runs its set time.
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

    private Command launch(String name, DoubleSupplier feedSeconds, boolean countsAsEmptying) {
        final double[] fedAt = {Double.NaN};
        final double[] startedAt = new double[1];
        final int[] shotsAtFeed = new int[1];
        return new CommandBuilder()
                .setStart(() -> {
                    fedAt[0] = Double.NaN;
                    startedAt[0] = secondsNow.getAsDouble();
                    launcher.spinUp();
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
                .setDone(() -> !Double.isNaN(fedAt[0])
                        && secondsNow.getAsDouble() - fedAt[0] >= nonNegative(feedSeconds.getAsDouble()))
                .setEnd(end -> {
                    feeder.idle();
                    if (!Double.isNaN(fedAt[0])) {
                        feeding--;
                        int shots = launcher.shots();
                        // Logged to judge the shot detector against what was seen; never acted on
                        // (mentor, 1 Nov 2026: a feed runs its set time until the detector is proven).
                        if (shots >= 0) {
                            note.accept(String.format(Locale.US, "%s fed %.2f s (%s): %d shots detected",
                                    name, secondsNow.getAsDouble() - fedAt[0], end, shots - shotsAtFeed[0]));
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
