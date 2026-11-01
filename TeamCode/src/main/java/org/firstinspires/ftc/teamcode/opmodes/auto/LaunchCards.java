package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.CommandBuilder;
import com.pedropathing.ivy.behaviors.EndCondition;
import com.pedropathing.ivy.commands.Commands;

import org.firstinspires.ftc.teamcode.Robot;

import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

/**
 * The launcher's cards for the Auto Builder: {@code SpinUp}, {@code LaunchAll}, {@code LaunchOne}
 * and the {@code Empty} trigger, built on {@code robot.launcher} and {@code robot.feeder}.
 *
 * <p>A launch card spins the launcher up, waits until it is at speed <em>and the robot has
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
     * How long the feeder runs for {@code LaunchAll}, seconds. 7.0: set by the mentor on robot 20245
     * on 1 Nov 2026, after 2.0 cut the feed off with pieces still in. A firing step's "at most" in
     * the Auto Builder has to leave room for spin-up plus this, or the step cuts the feed short.
     */
    public static double launchAllFeedS = 7.0;

    /** How long the feeder runs for {@code LaunchOne}, seconds. <b>Not measured.</b> */
    public static double launchOneFeedS = 0.5;

    /** What a launch card needs from the launcher. {@code LauncherSubsystem} has both methods. */
    public interface Launcher {
        void spinUp();
        boolean atSpeed();
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

    /** The cards for a real robot, on the system clock, gated on {@code settle}. */
    public static LaunchCards forRobot(Robot robot, PathEndSettle settle, Consumer<String> note) {
        Launcher launcher = new Launcher() {
            @Override public void spinUp() { robot.launcher.spinUp(); }
            @Override public boolean atSpeed() { return robot.launcher.atSpeed(); }
        };
        Feeder feeder = new Feeder() {
            @Override public void feed() { robot.feeder.feed(); }
            @Override public void idle() { robot.feeder.idle(); }
        };
        return new LaunchCards(launcher, feeder, settle::settled, note, () -> System.nanoTime() / 1e9);
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
        return new CommandBuilder()
                .setStart(() -> {
                    fedAt[0] = Double.NaN;
                    startedAt[0] = secondsNow.getAsDouble();
                    launcher.spinUp();
                })
                .setExecute(() -> {
                    if (!Double.isNaN(fedAt[0]) || !launcher.atSpeed()) return;
                    double now = secondsNow.getAsDouble();
                    if (settled.getAsBoolean()) {
                        feed(fedAt, now);
                    } else if (now - startedAt[0] >= PathEndSettle.maxWaitS()) {
                        note.accept(String.format(Locale.US, "%s fed before the robot settled: %.2f s since it started",
                                name, now - startedAt[0]));
                        feed(fedAt, now);
                    }
                })
                .setDone(() -> !Double.isNaN(fedAt[0])
                        && secondsNow.getAsDouble() - fedAt[0] >= nonNegative(feedSeconds.getAsDouble()))
                .setEnd(end -> {
                    feeder.idle();
                    if (countsAsEmptying && end == EndCondition.NATURALLY) {
                        launchAllsCompleted++;
                    }
                });
    }

    private void feed(double[] fedAt, double now) {
        feeder.feed();
        fedAt[0] = now;
    }

    /** A Panels-editable time clamped to [0, ∞), NaN as 0: a typo makes a short feed, never a stuck card. */
    static double nonNegative(double seconds) {
        return seconds > 0.0 ? seconds : 0.0;
    }
}
