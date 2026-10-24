package org.firstinspires.ftc.teamcode.opmodes.auto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * {@link LaunchCards} through Ivy's real scheduler, with a fake launcher and feeder and a
 * made-up clock: when the feeder runs, for how long, and what {@code Empty} means.
 */
public class LaunchCardsTest {

    private static final double LOOP_S = 0.02;
    private static final double EPS = 1e-9;

    private final List<String> log = new ArrayList<>();
    private double now;
    private boolean spinning;
    private boolean atSpeed;
    private LaunchCards cards;

    @Before
    public void setUp() {
        Scheduler.reset();
        now = 0;
        spinning = false;
        atSpeed = false;
        LaunchCards.launchAllFeedS = 2.0;
        LaunchCards.launchOneFeedS = 0.5;
        cards = new LaunchCards(
                new LaunchCards.Launcher() {
                    @Override public void spinUp() { spinning = true; log.add("spinUp"); }
                    @Override public boolean atSpeed() { return atSpeed; }
                },
                new LaunchCards.Feeder() {
                    @Override public void feed() { log.add(String.format(java.util.Locale.US, "feed@%.2f", now)); }
                    @Override public void idle() { log.add(String.format(java.util.Locale.US, "idle@%.2f", now)); }
                },
                () -> now);
    }

    /** Schedules the command and runs the scheduler until it finishes, or fails after {@code maxSeconds}. */
    private void run(Command command, double maxSeconds) {
        command.schedule();
        finish(command, maxSeconds);
    }

    /** Runs the scheduler until an already-scheduled command finishes, or fails after {@code maxSeconds}. */
    private void finish(Command command, double maxSeconds) {
        double end = now + maxSeconds;
        while (now < end) {
            Scheduler.execute();
            if (!command.isScheduled()) return;
            now += LOOP_S;
        }
        fail("Still running after " + maxSeconds + " s. Log: " + log);
    }

    /** Runs the scheduler for {@code seconds} whether or not the command finishes. */
    private void step(double seconds) {
        double end = now + seconds;
        while (now < end) {
            Scheduler.execute();
            now += LOOP_S;
        }
    }

    private static double timeOf(String entry) {
        return Double.parseDouble(entry.substring(entry.indexOf('@') + 1));
    }

    private String first(String prefix) {
        for (String entry : log) if (entry.startsWith(prefix)) return entry;
        return null;
    }

    @Test
    public void spinUpAsksTheLauncherAndFinishesAtOnce() {
        run(cards.spinUp(), 0.1);
        assertTrue(spinning);
        assertEquals("[spinUp]", log.toString());
    }

    @Test
    public void launchAllWaitsForSpeedThenFeedsForTheSetTime() {
        Command launch = cards.launchAll();
        launch.schedule();
        step(1.0);
        assertTrue(spinning);
        assertEquals("nothing fed while the wheels are below speed: " + log, null, first("feed"));

        atSpeed = true;
        finish(launch, 5.0);

        String fed = first("feed");
        String idled = first("idle");
        assertTrue("fed once at speed: " + log, fed != null);
        assertTrue("idled at the end: " + log, idled != null);
        assertEquals("feeds for launchAllFeedS", 2.0, timeOf(idled) - timeOf(fed), LOOP_S + EPS);
        assertEquals(1, cards.launchAllsCompleted());
    }

    @Test
    public void launchOneFeedsForItsOwnTimeAndDoesNotCountAsEmptying() {
        atSpeed = true;
        run(cards.launchOne(), 5.0);
        assertEquals(0.5, timeOf(first("idle")) - timeOf(first("feed")), LOOP_S + EPS);
        assertEquals(0, cards.launchAllsCompleted());
    }

    @Test
    public void anInterruptedLaunchIdlesTheFeederAndDoesNotCount() {
        atSpeed = true;
        Command launch = cards.launchAll();
        launch.schedule();
        step(0.5);
        assertTrue("feeding by now: " + log, first("feed") != null);
        launch.cancel();
        Scheduler.execute();
        assertTrue("feeder idled on interrupt: " + log, first("idle") != null);
        assertEquals(0, cards.launchAllsCompleted());
    }

    @Test
    public void emptyFiresOnlyForALaunchAllThatFinishesAfterTheWatchBegan() {
        atSpeed = true;
        BooleanSupplier empty = cards.emptySince();
        assertFalse(empty.getAsBoolean());

        run(cards.launchAll(), 5.0);
        assertTrue("a finished LaunchAll empties the robot", empty.getAsBoolean());

        BooleanSupplier later = cards.emptySince();
        assertFalse("a watch started after the launch does not see it", later.getAsBoolean());

        run(cards.launchOne(), 5.0);
        assertFalse("LaunchOne never says Empty", later.getAsBoolean());
    }

    @Test
    public void aBadFeedTimeMakesAShortFeedNotAStuckCard() {
        atSpeed = true;
        LaunchCards.launchAllFeedS = Double.NaN;
        run(cards.launchAll(), 1.0);
        assertTrue(first("feed") != null && first("idle") != null);
        LaunchCards.launchAllFeedS = -3.0;
        run(cards.launchAll(), 1.0);
        assertEquals(2, cards.launchAllsCompleted());
    }

    @Test
    public void nonNegativeClampsTheTime() {
        assertEquals(1.5, LaunchCards.nonNegative(1.5), EPS);
        assertEquals(0.0, LaunchCards.nonNegative(-1.0), EPS);
        assertEquals(0.0, LaunchCards.nonNegative(Double.NaN), EPS);
    }
}
