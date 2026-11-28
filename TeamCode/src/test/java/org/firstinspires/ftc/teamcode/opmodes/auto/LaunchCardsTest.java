package org.firstinspires.ftc.teamcode.opmodes.auto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.autokit.AutoRegistry;

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
    /** The launcher's forecast to at speed, seconds; NaN: the interface's default (0 or infinite). */
    private double toSpeedS;
    private boolean settled;
    /** The launcher's shot count; -1: a launcher with no detector. */
    private int shots;
    /** What the approach check ({@code PathEndSettle.readyOnApproach}) says. */
    private boolean readyOnApproach;
    private final List<String> notes = new ArrayList<>();
    private LaunchCards cards;

    @Before
    public void setUp() {
        Scheduler.reset();
        now = 0;
        spinning = false;
        atSpeed = false;
        settled = true;
        readyOnApproach = false;
        shots = -1;
        toSpeedS = Double.NaN;
        LaunchCards.gateLeadS = 0.0;
        LaunchCards.launchAllEndAfterShots = 4;
        LaunchCards.launchOnApproach = true;
        PathEndSettle.maxSettleWaitS = 1.0;
        LaunchCards.maxSpinUpWaitS = 2.5;
        LaunchCards.launchAllFeedS = 2.0;
        LaunchCards.firstLaunchAllFeedS = 2.0;
        LaunchCards.launchOneFeedS = 0.5;
        cards = new LaunchCards(
                new LaunchCards.Launcher() {
                    @Override public void spinUp() { spinning = true; log.add("spinUp"); }
                    @Override public boolean atSpeed() { return atSpeed; }
                    @Override public int shots() { return shots; }
                    @Override public double secondsToSpeed() {
                        if (atSpeed) return 0.0;
                        return Double.isNaN(toSpeedS) ? Double.POSITIVE_INFINITY : toSpeedS;
                    }
                },
                new LaunchCards.Feeder() {
                    @Override public void feed() { log.add(String.format(java.util.Locale.US, "feed@%.2f", now)); }
                    @Override public void idle() { log.add(String.format(java.util.Locale.US, "idle@%.2f", now)); }
                },
                () -> settled,
                (end, pose, remainingIn) -> readyOnApproach,
                notes::add,
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

    // ------------------------------------------------ LaunchAllAimed, from the park (19 Nov 2026)

    /** A turret for {@code LaunchAllAimed}: aims while told it can, says why not, and whether it is on target. */
    private final class FakeShot implements LaunchCards.AimedShot {
        String refusal;
        boolean canAim = true;
        boolean onTarget;
        int aims;
        boolean started, done;

        @Override public void start() { started = true; }
        @Override public boolean aim() { aims++; return canAim; }
        @Override public boolean onTarget() { return onTarget; }
        @Override public String refusal() { return refusal; }
        @Override public void done() { done = true; }
    }

    @Test
    public void anAimedVolleyFeedsOnlyOnTargetAndAtSpeed() {
        FakeShot shot = new FakeShot();
        Command card = cards.launchAllAimed(shot);
        card.schedule();
        atSpeed = true;
        step(0.5);
        assertTrue("aiming every loop", shot.aims > 10);
        assertEquals("not on target: no feed", null, first("feed"));
        shot.onTarget = true;
        finish(card, 5);
        assertEquals(0.5, timeOf(first("feed")), 0.05);
        assertTrue("the turret goes home at the end", shot.done);
        assertEquals("counts toward Empty", 1, cards.launchAllsCompleted());
    }

    @Test
    public void anAimedVolleyWithATurretThatCannotAimFiresFromHome() {
        // Locked forward or a dropped hold: home, and the gate does not wait for the turret.
        FakeShot shot = new FakeShot();
        shot.canAim = false;
        atSpeed = true;
        run(cards.launchAllAimed(shot), 5);
        assertEquals(0.0, timeOf(first("feed")), 0.05);
        assertTrue(notes.toString(), notes.stream().anyMatch(n -> n.contains("the turret at home")));
    }

    @Test
    public void anAimedVolleyRefusedAtTheStartFeedsNothing() {
        FakeShot shot = new FakeShot();
        shot.refusal = "the pose is not referenced on the field";
        atSpeed = true;
        shot.onTarget = true;
        run(cards.launchAllAimed(shot), 0.2);
        assertEquals(null, first("feed"));
        assertFalse("never started aiming", shot.started);
        assertTrue(notes.toString(), notes.get(0).contains("not firing, the pose is not referenced"));
        assertEquals(0, cards.launchAllsCompleted());
    }

    @Test
    public void anAimedVolleyStopsWaitingWhenTooLate() {
        FakeShot shot = new FakeShot();
        Command card = cards.launchAllAimed(shot);
        card.schedule();
        step(0.3);
        shot.refusal = "29.0 s on the match clock, past 28.8 s";
        finish(card, 0.2);
        assertEquals(null, first("feed"));
        assertTrue(shot.done);
        assertTrue(notes.toString(), notes.stream().anyMatch(n -> n.contains("the gate never opened")));
    }

    @Test
    public void anAimedVolleyFeedRunsOnOnceTheGateHasOpened() {
        FakeShot shot = new FakeShot();
        shot.onTarget = true;
        atSpeed = true;
        Command card = cards.launchAllAimed(shot);
        card.schedule();
        step(0.1);
        shot.refusal = "past the deadline";
        finish(card, 5);
        assertEquals(1, log.stream().filter(e -> e.startsWith("feed")).count());
        assertEquals(LaunchCards.launchAllFeedS, timeOf(first("idle")) - timeOf(first("feed")), 0.05);
    }

    // ------------------------------------------------ LaunchOneAimed, one piece on the turret (28 Nov 2026)

    @Test
    public void aOneBallAimedLaunchEndsAtItsCountedShot() {
        FakeShot shot = new FakeShot();
        shot.onTarget = true;
        atSpeed = true;
        Command card = cards.launchOneAimed(shot);
        card.schedule();
        step(0.8);
        assertTrue("fed, and still feeding: no shot counted yet", first("feed") != null && first("idle") == null);
        shots++;
        finish(card, 1);
        assertEquals("ended at the shot, not at the backstop", 0.8, timeOf(first("idle")) - timeOf(first("feed")), 0.1);
        assertTrue(shot.done);
        assertEquals("one ball does not empty the robot", 0, cards.launchAllsCompleted());
    }

    @Test
    public void awayVolleyNeverFeedsFromHomeEvenIfTheParkWouldAllowIt() {
        FakeShot shot = new FakeShot();
        shot.canAim = false;
        shot.onTarget = true;
        atSpeed = true;
        run(cards.launchAllAwayFromPark(shot), 0.3);
        assertEquals(null, first("feed"));
        assertEquals(0, cards.launchAllsCompleted());
    }

    @Test
    public void awayVolleyClosesItsGateOnTheLoopThatLosesTheCell() {
        FakeShot shot = new FakeShot();
        shot.onTarget = true;
        atSpeed = true;
        Command command = cards.launchAllAwayFromPark(shot);
        command.schedule();
        step(0.2);
        assertTrue(cards.feeding());
        shot.onTarget = false;
        double lostAt = now;
        finish(command, 0.1);
        assertEquals(lostAt, timeOf(first("idle")), LOOP_S);
        assertFalse(cards.feeding());
        assertEquals("an interrupted volley still holds its remaining load", 0, cards.launchAllsCompleted());
    }

    @Test
    public void aHealthyAwayVolleyCountsAsTheFirstOrdinaryVolley() {
        FakeShot shot = new FakeShot();
        shot.onTarget = true;
        atSpeed = true;
        run(cards.launchAllAwayFromPark(shot), 5);
        assertEquals(LaunchCards.firstLaunchAllFeedS, timeOf(first("idle")) - timeOf(first("feed")), 0.05);
        assertEquals(1, cards.launchAllsCompleted());
    }

    @Test
    public void aOneBallAimedLaunchWithNoShotCountedEndsAtItsBackstop() {
        FakeShot shot = new FakeShot();
        shot.onTarget = true;
        atSpeed = true;
        run(cards.launchOneAimed(shot), 5);
        assertEquals(LaunchCards.launchOneAimedFeedS, timeOf(first("idle")) - timeOf(first("feed")), 0.05);
        assertTrue("longer than LaunchOne's feed, which ends before the first ball",
                LaunchCards.launchOneAimedFeedS > LaunchCards.launchOneFeedS + 0.3);
    }

    @Test
    public void aOneBallAimedLaunchThatMayNotFireKeepsThePiece() {
        // A turret that cannot aim at a spot where home does not point at the CELL: the shot says so, nothing is fed.
        FakeShot shot = new FakeShot();
        shot.canAim = false;
        shot.refusal = "the turret cannot aim (locked forward, or it dropped its hold) and home points 84 deg off the CELL";
        atSpeed = true;
        run(cards.launchOneAimed(shot), 0.2);
        assertEquals(null, first("feed"));
        assertTrue(notes.toString(), notes.get(0).contains("the piece stays in the lane"));
    }

    @Test
    public void aOneBallAimedLaunchWaitsForTheTurretOnTarget() {
        FakeShot shot = new FakeShot();
        atSpeed = true;
        Command card = cards.launchOneAimed(shot);
        card.schedule();
        step(0.4);
        assertEquals("not on target: no feed", null, first("feed"));
        shot.onTarget = true;
        finish(card, 5);
        assertEquals(0.4, timeOf(first("feed")), 0.05);
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

    /** #193: a fire after a path waits for the robot to be still and aimed, not for Pedro's done. */
    /** 1 Nov 2026: a wheel that never reaches speed must not keep every ball in the robot. */
    @Test
    public void aLauncherThatNeverReachesSpeedFeedsAnywayAtTheMaxWaitAndTheLogSaysSo() {
        atSpeed = false;
        run(cards.launchAll(), 6.0);
        assertEquals("fed at maxSpinUpWaitS", 2.5, timeOf(first("feed")), LOOP_S + EPS);
        assertTrue("the log says why: " + notes, notes.stream().anyMatch(n -> n.contains("below launch speed")));
        assertEquals("it still counts as emptying", 1, cards.launchAllsCompleted());
    }

    /** Runs the scheduler until {@code at} seconds into the feed, then counts one more shot. */
    private void shotAt(double feedS, double at) {
        step(feedS + at - now);
        shots++;
    }

    @Test
    public void launchAllEndsItsFeedAtTheFourthCountedShot() {
        atSpeed = true;
        LaunchCards.launchAllFeedS = 3.5;
        LaunchCards.firstLaunchAllFeedS = 3.5;
        shots = 7;                               // shots from earlier volleys do not count
        Command launch = cards.launchAll();
        launch.schedule();
        Scheduler.execute();
        double fed = timeOf(first("feed"));
        // 23:41's first volley: the shots counted 1.09, 1.44, 1.94 and 2.36 s into the feed.
        shotAt(fed, 1.09);
        shotAt(fed, 1.44);
        shotAt(fed, 1.94);
        assertEquals("still feeding after three: " + log, null, first("idle"));
        shotAt(fed, 2.36);
        finish(launch, 0.1);
        assertEquals("ends at the fourth shot, not at 3.5 s", 2.36, timeOf(first("idle")) - fed, 2 * LOOP_S + EPS);
        assertEquals("it still empties the robot", 1, cards.launchAllsCompleted());
        assertTrue(notes.toString(), notes.get(notes.size() - 1).contains("ended at the last counted shot"));
        assertTrue(notes.toString(), notes.get(notes.size() - 1).endsWith(": 4 shots detected"));
    }

    @Test
    public void aShortCountFeedsForTheWholeSetTime() {
        atSpeed = true;
        LaunchCards.launchAllFeedS = 3.5;
        LaunchCards.firstLaunchAllFeedS = 3.5;
        shots = 0;
        Command launch = cards.launchAll();
        launch.schedule();
        Scheduler.execute();
        double fed = timeOf(first("feed"));
        shotAt(fed, 0.8);
        shotAt(fed, 1.2);
        shotAt(fed, 2.1);                        // one missed: three counted
        finish(launch, 2.0);
        assertEquals("a count too low costs nothing: the set time", 3.5, timeOf(first("idle")) - fed, LOOP_S + EPS);
        assertFalse(notes.toString(), notes.get(notes.size() - 1).contains("last counted shot"));
        assertTrue("a timer end says so, for the feeder sensor's case: " + notes,
                notes.get(notes.size() - 1).contains("ended at its 3.5 s timer"));
    }

    /** 22 Nov 2026: the first volley (the preloads, wheels from rest) feeds longer than the ones after it. */
    @Test
    public void theFirstVolleyFeedsForItsOwnTimeAndLaterOnesForTheShorterOne() {
        atSpeed = true;
        shots = -1;
        LaunchCards.firstLaunchAllFeedS = 2.8;
        LaunchCards.launchAllFeedS = 2.4;
        run(cards.launchAll(), 5.0);
        double firstFed = timeOf(first("feed"));
        double firstIdle = timeOf(first("idle"));
        assertEquals("the first volley: firstLaunchAllFeedS", 2.8, firstIdle - firstFed, LOOP_S + EPS);

        log.clear();
        run(cards.launchAll(), 5.0);
        assertEquals("a later volley: launchAllFeedS", 2.4, timeOf(first("idle")) - timeOf(first("feed")), LOOP_S + EPS);

        log.clear();
        run(cards.launchOne(), 5.0);
        assertEquals("LaunchOne keeps its own time and is not a volley", 0.5,
                timeOf(first("idle")) - timeOf(first("feed")), LOOP_S + EPS);
    }

    /** A gate the approach opens for the first volley is the first volley: it gets the first's time. */
    @Test
    public void aFirstVolleyOpenedOnTheApproachKeepsTheFirstTime() {
        atSpeed = true;
        shots = -1;
        readyOnApproach = true;
        LaunchCards.firstLaunchAllFeedS = 2.8;
        LaunchCards.launchAllFeedS = 2.4;
        cards.approach().started(new Pose(0, 0, 0));
        cards.approach().approaching(new Pose(0, 0, 0), 1.0);
        double fed = timeOf(first("feed"));
        run(cards.launchAll(), 5.0);
        assertEquals(2.8, timeOf(first("idle")) - fed, LOOP_S + EPS);
    }

    @Test
    public void noDetectorOrTheSwitchOffFeedsForTheSetTime() {
        atSpeed = true;
        shots = -1;
        run(cards.launchAll(), 5.0);
        assertEquals("no detector", 2.0, timeOf(first("idle")) - timeOf(first("feed")), LOOP_S + EPS);

        log.clear();
        LaunchCards.launchAllEndAfterShots = 0;
        shots = 0;
        Command launch = cards.launchAll();
        launch.schedule();
        Scheduler.execute();
        shots = 4;
        finish(launch, 5.0);
        assertEquals("switched off", 2.0, timeOf(first("idle")) - timeOf(first("feed")), LOOP_S + EPS);

        log.clear();
        LaunchCards.launchAllEndAfterShots = Double.NaN;
        shots = 0;
        launch = cards.launchAll();
        launch.schedule();
        Scheduler.execute();
        shots = 4;
        finish(launch, 5.0);
        assertEquals("NaN is off", 2.0, timeOf(first("idle")) - timeOf(first("feed")), LOOP_S + EPS);
    }

    @Test
    public void launchOneNeverEndsOnTheCount() {
        atSpeed = true;
        shots = 0;
        Command launch = cards.launchOne();
        launch.schedule();
        Scheduler.execute();
        shots = 4;
        finish(launch, 2.0);
        assertEquals(0.5, timeOf(first("idle")) - timeOf(first("feed")), LOOP_S + EPS);
    }

    @Test
    public void aLaunchAtSpeedWaitsForTheRobotToSettle() {
        atSpeed = true;
        settled = false;
        Command launch = cards.launchAll();
        launch.schedule();
        step(0.5);
        assertEquals("nothing fed while the robot is still moving: " + log, null, first("feed"));
        settled = true;
        finish(launch, 5.0);
        assertEquals("fed as soon as it settled", 0.5, timeOf(first("feed")), LOOP_S + EPS);
        assertTrue("a settled feed is not noted: " + notes, notes.isEmpty());
    }

    @Test
    public void aRobotThatNeverSettlesFiresAtTheMaxWaitAndTheLogSaysSo() {
        atSpeed = true;
        settled = false;
        run(cards.launchAll(), 5.0);
        assertEquals("fed at maxSettleWaitS", 1.0, timeOf(first("feed")), LOOP_S + EPS);
        assertEquals(notes.toString(), 1, notes.size());
        assertTrue(notes.get(0), notes.get(0).startsWith("LaunchAll fed before the robot settled"));
    }

    @Test
    public void aBadMaxSettleWaitNeverStallsTheCard() {
        atSpeed = true;
        settled = false;
        PathEndSettle.maxSettleWaitS = Double.NaN;
        run(cards.launchOne(), 2.0);
        assertEquals("NaN waits not at all", 0.0, timeOf(first("feed")), LOOP_S + EPS);
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
        LaunchCards.firstLaunchAllFeedS = Double.NaN;
        run(cards.launchAll(), 1.0);
        assertTrue(first("feed") != null && first("idle") != null);
        LaunchCards.launchAllFeedS = -3.0;
        LaunchCards.firstLaunchAllFeedS = -3.0;
        run(cards.launchAll(), 1.0);
        assertEquals(2, cards.launchAllsCompleted());
    }

    // --------------------------------------------------------- on the approach

    private static final Pose END = new Pose(100, 0, 0);

    /** The drive before a LaunchAll: {@code loops} loops of the approach, closing in from 12 in. */
    private void approach(AutoRegistry.Approach approach, int loops) {
        approach.started(END);
        for (int i = 0; i < loops; i++) {
            double remaining = Math.max(0, 12 - i);
            approach.approaching(new Pose(100 - remaining, 0, 0), remaining);
            now += LOOP_S;
        }
    }

    /**
     * Mentor, 4 Nov 2026: the gate opens on the approach once aimed and at speed, and the LaunchAll
     * after it waits for nothing, not even a robot that has not settled: it ends launchAllFeedS
     * after the gate opened.
     */
    @Test
    public void aGateOpenedOnTheApproachIsFinishedByTheLaunchAllAfterIt() {
        atSpeed = true;
        readyOnApproach = true;
        settled = false;
        approach(cards.approach(), 15);
        assertEquals("opened at the first ready loop: " + log, 0.0, timeOf(first("feed")), EPS);
        assertTrue("time limits are held from the gate opening", cards.feeding());
        assertTrue(notes.toString(), notes.get(0).startsWith("LaunchAll opened the gate on the approach"));

        run(cards.launchAll(), 5.0);
        assertEquals("one feed, the approach's: " + log, 1, log.stream().filter(e -> e.startsWith("feed")).count());
        assertEquals("ends launchAllFeedS after the gate opened", 2.0, timeOf(first("idle")), LOOP_S + EPS);
        assertFalse(cards.feeding());
        assertEquals(1, cards.launchAllsCompleted());
        assertFalse("fed on the approach, not 'before the robot settled': " + notes,
                notes.stream().anyMatch(n -> n.contains("before the robot settled")));
    }

    /** Shots fired on the approach, before the card starts, count toward the volley's four. */
    @Test
    public void shotsOnTheApproachCountTowardTheFour() {
        atSpeed = true;
        readyOnApproach = true;
        LaunchCards.launchAllFeedS = 3.5;
        LaunchCards.firstLaunchAllFeedS = 3.5;
        shots = 2;                               // from the volley before
        approach(cards.approach(), 15);          // gate opens at 0, the drive runs to 0.3 s
        shots += 2;                              // two leave before the path ends
        Command launch = cards.launchAll();
        launch.schedule();
        step(0.5);
        assertEquals("still feeding after two: " + log, null, first("idle"));
        shots += 2;
        finish(launch, 0.1);
        assertTrue("ended at the fourth, well before 3.5 s: " + log, timeOf(first("idle")) < 1.0);
        assertEquals(1, cards.launchAllsCompleted());
    }

    /** The gate waits for the launcher: below speed on the approach, the card feeds as it always has. */
    @Test
    public void theApproachNeverOpensTheGateBelowSpeed() {
        atSpeed = false;
        readyOnApproach = true;
        approach(cards.approach(), 15);
        assertEquals(null, first("feed"));
        assertFalse(cards.feeding());
    }

    /**
     * A late turn is never aimed on the approach, so nothing opens there and the LaunchAll behaves as
     * before: it waits for the settle.
     */
    @Test
    public void anApproachThatIsNeverReadyLeavesTheLaunchAllAsBefore() {
        atSpeed = true;
        readyOnApproach = false;
        approach(cards.approach(), 15);
        assertEquals(null, first("feed"));
        double cardStart = now;
        settled = false;
        Command launch = cards.launchAll();
        launch.schedule();
        step(0.3);
        assertEquals("waits for the settle: " + log, null, first("feed"));
        settled = true;
        finish(launch, 5.0);
        assertEquals(cardStart + 0.3, timeOf(first("feed")), LOOP_S + EPS);
    }

    /** The guard parks, or the path is cut, before the LaunchAll starts: the gate closes. */
    @Test
    public void aCancelledApproachClosesTheGate() {
        atSpeed = true;
        readyOnApproach = true;
        approach(cards.approach(), 5);
        assertTrue(cards.feeding());
        cards.approach().cancel();
        assertTrue("gate closed: " + log, first("idle") != null);
        assertFalse(cards.feeding());
        // The next LaunchAll starts from scratch: it waits for the settle and feeds its own feed.
        settled = true;
        run(cards.launchAll(), 5.0);
        assertEquals(2, log.stream().filter(e -> e.startsWith("feed")).count());
    }

    @Test
    public void theSwitchTurnsLaunchingOnTheApproachOff() {
        atSpeed = true;
        readyOnApproach = true;
        LaunchCards.launchOnApproach = false;
        approach(cards.approach(), 15);
        assertEquals(null, first("feed"));
    }

    // ------------------------------------------------------------ the early gate

    /** Off by default: a launcher due at speed in 0.1 s still waits for at speed, as before. */
    @Test
    public void withTheLeadOffTheGateWaitsForAtSpeedAsBefore() {
        toSpeedS = 0.1;
        Command launch = cards.launchAll();
        launch.schedule();
        step(0.5);
        assertEquals(null, first("feed"));
        atSpeed = true;
        finish(launch, 5.0);
        assertEquals(0.5, timeOf(first("feed")), LOOP_S + EPS);
        assertFalse(notes.toString(), notes.stream().anyMatch(n -> n.contains("ahead of launch speed")));
    }

    /** The preloads at PLAY: due at speed within the lead, the gate opens now and the log says so. */
    @Test
    public void withALeadTheGateOpensOnceTheLauncherIsDueWithinIt() {
        LaunchCards.gateLeadS = 0.4;
        toSpeedS = 0.9;
        Command launch = cards.launchAll();
        launch.schedule();
        step(0.2);
        assertEquals("0.9 s off is past a 0.4 s lead: " + log, null, first("feed"));
        toSpeedS = 0.3;
        step(0.1);
        assertEquals(0.2, timeOf(first("feed")), LOOP_S + EPS);
        assertTrue(notes.toString(), notes.stream().anyMatch(n -> n.contains("0.30 s ahead of launch speed")));
        finish(launch, 5.0);
        assertEquals("feeds its whole time from the gate", 2.0, timeOf(first("idle")) - timeOf(first("feed")), LOOP_S + EPS);
    }

    /** The lead is only the launcher's half: an unsettled robot still waits for the settle. */
    @Test
    public void theLeadDoesNotSkipTheSettle() {
        LaunchCards.gateLeadS = 0.4;
        toSpeedS = 0.2;
        settled = false;
        Command launch = cards.launchAll();
        launch.schedule();
        step(0.3);
        assertEquals(null, first("feed"));
        settled = true;
        finish(launch, 5.0);
        assertEquals(0.3, timeOf(first("feed")), LOOP_S + EPS);
    }

    /** A launcher that is not coming (infinite forecast) waits out maxSpinUpWaitS as before. */
    @Test
    public void aLauncherThatIsNotComingStillWaitsTheMaxSpinUpWait() {
        LaunchCards.gateLeadS = 0.4;
        run(cards.launchAll(), 6.0);
        assertEquals(2.5, timeOf(first("feed")), LOOP_S + EPS);
    }

    @Test
    public void theApproachOpensAheadOfSpeedWithALead() {
        LaunchCards.gateLeadS = 0.4;
        toSpeedS = 0.3;
        readyOnApproach = true;
        approach(cards.approach(), 5);
        assertEquals(0.0, timeOf(first("feed")), EPS);
        assertTrue(notes.toString(), notes.get(0).contains("ahead of launch speed"));
    }

    @Test
    public void aBadLeadIsOff() {
        toSpeedS = 0.1;
        readyOnApproach = true;
        for (double bad : new double[] {Double.NaN, -1.0}) {
            LaunchCards.gateLeadS = bad;
            approach(cards.approach(), 5);
            assertEquals("lead " + bad + ": " + log, null, first("feed"));
        }
    }

    @Test
    public void nonNegativeClampsTheTime() {
        assertEquals(1.5, LaunchCards.nonNegative(1.5), EPS);
        assertEquals(0.0, LaunchCards.nonNegative(-1.0), EPS);
        assertEquals(0.0, LaunchCards.nonNegative(Double.NaN), EPS);
    }
}
