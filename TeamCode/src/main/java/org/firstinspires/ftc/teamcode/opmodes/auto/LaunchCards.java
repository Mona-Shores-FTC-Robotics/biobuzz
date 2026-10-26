package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.CommandBuilder;
import com.pedropathing.ivy.behaviors.EndCondition;
import com.pedropathing.ivy.commands.Commands;

import org.firstinspires.ftc.teamcode.Robot;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

/**
 * The launcher's cards for the Auto Builder: {@code SpinUp}, {@code LaunchAll}, {@code LaunchOne}
 * and the {@code Empty} trigger, built on {@code robot.launcher} and {@code robot.feeder}.
 *
 * <p>A launch card spins the launcher up, waits until it is at speed, runs the feeder for a set
 * time, then idles the feeder. The launcher is left spinning: the Autos call {@code SpinUp} once
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

    /** How long the feeder runs for {@code LaunchAll}, seconds. <b>Not measured.</b> */
    public static double launchAllFeedS = 2.0;

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
    private final DoubleSupplier secondsNow;
    private int launchAllsCompleted;

    public LaunchCards(Launcher launcher, Feeder feeder, DoubleSupplier secondsNow) {
        if (launcher == null || feeder == null || secondsNow == null) {
            throw new IllegalArgumentException("LaunchCards needs a launcher, a feeder and a clock");
        }
        this.launcher = launcher;
        this.feeder = feeder;
        this.secondsNow = secondsNow;
    }

    /** The cards for a real robot, on the system clock. */
    public static LaunchCards forRobot(Robot robot) {
        Launcher launcher = new Launcher() {
            @Override public void spinUp() { robot.launcher.spinUp(); }
            @Override public boolean atSpeed() { return robot.launcher.atSpeed(); }
        };
        Feeder feeder = new Feeder() {
            @Override public void feed() { robot.feeder.feed(); }
            @Override public void idle() { robot.feeder.idle(); }
        };
        return new LaunchCards(launcher, feeder, () -> System.nanoTime() / 1e9);
    }

    /** {@code SpinUp}: ask the launcher for speed and finish at once. The wheels come up while the robot drives. */
    public Command spinUp() {
        return Commands.instant(launcher::spinUp);
    }

    /** {@code LaunchAll}: spin up, wait for speed, feed for {@link #launchAllFeedS}, idle the feeder. */
    public Command launchAll() {
        return launch(() -> launchAllFeedS, true);
    }

    /** {@code LaunchOne}: as {@link #launchAll()} with {@link #launchOneFeedS}; does not count toward Empty. */
    public Command launchOne() {
        return launch(() -> launchOneFeedS, false);
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

    private Command launch(DoubleSupplier feedSeconds, boolean countsAsEmptying) {
        final double[] fedAt = {Double.NaN};
        return new CommandBuilder()
                .setStart(() -> {
                    fedAt[0] = Double.NaN;
                    launcher.spinUp();
                })
                .setExecute(() -> {
                    if (Double.isNaN(fedAt[0]) && launcher.atSpeed()) {
                        feeder.feed();
                        fedAt[0] = secondsNow.getAsDouble();
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

    /** A Panels-editable time clamped to [0, ∞), NaN as 0: a typo makes a short feed, never a stuck card. */
    static double nonNegative(double seconds) {
        return seconds > 0.0 ? seconds : 0.0;
    }
}
