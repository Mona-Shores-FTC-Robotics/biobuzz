package org.firstinspires.ftc.teamcode.opmodes.auto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;

import org.firstinspires.ftc.teamcode.subsystems.BeamBreakSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.FeedWatch;
import org.firstinspires.ftc.teamcode.subsystems.IntakeSubsystem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * {@link LaunchCards} with the throat beam <b>behind the gate</b> ({@code BeamBreakSubsystem.throatBehindGate}):
 * it counts nothing, so the volley keeps its shot counter and set time, and also ends once the beam
 * has stayed clear with the lane empty. A ball waiting in it is never a jam. Through Ivy's real
 * scheduler with a fake launcher, feeder and beam, as {@link LaunchCardsThroatTest}.
 */
public class LaunchCardsGateBeamTest {

    private static final double LOOP_S = 0.02;

    private final List<String> log = new ArrayList<>();
    private final List<String> notes = new ArrayList<>();
    private double now;
    /** The beam is broken until this time (the volley's balls passing, touching), clear after it. */
    private double blockedUntil;
    /** Shots the dip detector has counted, from this time on: 4 (NaN: never). */
    private double fourShotsAt = Double.NaN;
    private FeedWatch.Lane lane;
    private LaunchCards cards;

    private double gapBefore, emptyBefore, feedBefore, firstFeedBefore;
    private boolean laneClearBefore;

    @Before
    public void setUp() {
        Scheduler.reset();
        now = 0;
        blockedUntil = 0;
        lane = FeedWatch.Lane.EMPTY;
        gapBefore = BeamBreakSubsystem.stuckGapS;
        emptyBefore = BeamBreakSubsystem.gateClearEmptyS;
        feedBefore = LaunchCards.launchAllFeedS;
        firstFeedBefore = LaunchCards.firstLaunchAllFeedS;
        laneClearBefore = IntakeSubsystem.clearFeedJam;
        IntakeSubsystem.clearFeedJam = true;
        BeamBreakSubsystem.stuckGapS = 1.2;
        BeamBreakSubsystem.gateClearEmptyS = 0.5;
        LaunchCards.gateLeadS = 0.0;
        LaunchCards.launchAllEndAfterShots = 4;
        LaunchCards.launchOnApproach = true;
        LaunchCards.maxSpinUpWaitS = 2.5;
        // The set time stays the volley's backstop with this beam.
        LaunchCards.launchAllFeedS = 3.0;
        LaunchCards.firstLaunchAllFeedS = 3.0;
        PathEndSettle.maxSettleWaitS = 1.0;
        cards = new LaunchCards(
                new LaunchCards.Launcher() {
                    @Override public void spinUp() { }
                    @Override public boolean atSpeed() { return true; }
                    @Override public int shots() { return !Double.isNaN(fourShotsAt) && now >= fourShotsAt - 1e-9 ? 4 : 0; }
                    // No throatBalls(): a beam behind the gate counts nothing (-1, the default).
                    @Override public FeedWatch.Lane lane() { return lane; }
                    @Override public double gateClearS() { return now < blockedUntil ? 0.0 : now - blockedUntil; }
                },
                new LaunchCards.Feeder() {
                    @Override public void feed() { log.add(at("feed")); }
                    @Override public void idle() { log.add(at("idle")); }
                    @Override public void reverse() { log.add(at("reverse")); }
                },
                () -> true,
                (end, pose, remainingIn) -> true,
                notes::add,
                () -> now);
    }

    @After
    public void restore() {
        BeamBreakSubsystem.stuckGapS = gapBefore;
        BeamBreakSubsystem.gateClearEmptyS = emptyBefore;
        LaunchCards.launchAllFeedS = feedBefore;
        LaunchCards.firstLaunchAllFeedS = firstFeedBefore;
        IntakeSubsystem.clearFeedJam = laneClearBefore;
    }

    private String at(String what) {
        return String.format(Locale.US, "%s@%.2f", what, now);
    }

    private double runToEnd(Command command, double maxSeconds) {
        command.schedule();
        double end = now + maxSeconds;
        while (now < end) {
            Scheduler.execute();
            if (!command.isScheduled()) return now;
            now += LOOP_S;
        }
        fail("Still running after " + maxSeconds + " s. Log: " + log + " notes " + notes);
        return Double.NaN;
    }

    private String note(String containing) {
        for (String n : notes) if (n.contains(containing)) return n;
        return null;
    }

    private int count(String prefix) {
        int n = 0;
        for (String e : log) if (e.startsWith(prefix)) n++;
        return n;
    }

    @Test
    public void theVolleyEndsOnceTheGateHasStayedClearWithTheLaneEmpty() {
        blockedUntil = 1.6;            // four balls through, touching; the last one clears the beam at 1.6 s
        double ended = runToEnd(cards.launchAll(), 5);
        assertEquals("0.5 s after the last ball cleared the gate, before the 3.0 s set time", 2.1, ended, 0.03);
        assertNotNull(notes.toString(), note("ended: nothing at the gate"));
        assertNotNull(notes.toString(), note("gate beam behind the gate"));
        assertEquals(0, count("reverse"));
    }

    @Test
    public void theFourthCountedShotStillEndsItFirst() {
        blockedUntil = 1.6;
        fourShotsAt = 1.9;
        double ended = runToEnd(cards.launchAll(), 5);
        assertEquals(1.9, ended, 0.03);
        assertNotNull(notes.toString(), note("ended at the last counted shot"));
    }

    @Test
    public void nothingWaitingAndTheLaneEmptyEndsSoon() {
        double ended = runToEnd(cards.launchAll(), 5);
        assertEquals(0.5, ended, 0.03);
        assertNotNull(note("nothing at the gate"));
    }

    @Test
    public void aBallSittingInTheBeamIsNeverAJam() {
        blockedUntil = 99;             // a ball that never leaves: no unjam, the set time ends the card
        double ended = runToEnd(cards.launchAll(), 5);
        assertEquals(3.0, ended, 0.03);
        assertEquals("no HELD verdict behind the gate", 0, count("reverse"));
        assertNotNull(notes.toString(), note("ended at its 3.0 s timer"));
    }

    @Test
    public void aClearGateWithTheLaneHoldingIsTheLanesJamAndTheFeedGoesOn() {
        lane = FeedWatch.Lane.HOLDS;
        double ended = runToEnd(cards.launchAll(), 5);
        assertEquals("not empty: the set time ends it", 3.0, ended, 0.03);
        assertEquals("one jam, one handler: the feeder does not back out", 0, count("reverse"));
        assertNotNull(notes.toString(), note("left to the lane's jam clear"));
    }
}
