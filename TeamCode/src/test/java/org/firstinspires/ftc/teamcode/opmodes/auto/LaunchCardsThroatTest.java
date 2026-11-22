package org.firstinspires.ftc.teamcode.opmodes.auto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.subsystems.BeamBreakSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.FeedWatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

/**
 * {@link LaunchCards} with the throat beam fitted: a volley ends on the beam's balls, the thin
 * feed timers become a backstop, and a stuck feed is backed out and fed again, once. Through Ivy's
 * real scheduler with a fake launcher, feeder and beam on a made-up clock, as {@link LaunchCardsTest}.
 */
public class LaunchCardsThroatTest {

    private static final double LOOP_S = 0.02;

    private final List<String> log = new ArrayList<>();
    private final List<String> notes = new ArrayList<>();
    /** Balls the beam will have seen by each time: the fake beam's pass count. */
    private final TreeMap<Double, Integer> ballsAt = new TreeMap<>();
    private double now;
    private boolean atSpeed;
    private double blockedS;
    private FeedWatch.Lane lane;
    private boolean reversing;
    private LaunchCards cards;

    private double quietBefore, firstBefore, jamBefore, gapBefore, backstopBefore, reverseBefore;
    private int triesBefore;

    @Before
    public void setUp() {
        Scheduler.reset();
        now = 0;
        atSpeed = true;
        blockedS = 0;
        lane = FeedWatch.Lane.EMPTY;
        quietBefore = BeamBreakSubsystem.quietS;
        firstBefore = BeamBreakSubsystem.firstBallWithinS;
        jamBefore = BeamBreakSubsystem.jamBlockedS;
        gapBefore = BeamBreakSubsystem.stuckGapS;
        backstopBefore = LaunchCards.throatBackstopS;
        reverseBefore = LaunchCards.unjamReverseS;
        triesBefore = LaunchCards.unjamTries;
        BeamBreakSubsystem.quietS = 0.9;
        BeamBreakSubsystem.firstBallWithinS = 1.5;
        BeamBreakSubsystem.jamBlockedS = 0.5;
        BeamBreakSubsystem.stuckGapS = 1.2;
        LaunchCards.throatBackstopS = 3.5;
        LaunchCards.unjamReverseS = 0.3;
        LaunchCards.unjamTries = 1;
        LaunchCards.gateLeadS = 0.0;
        LaunchCards.launchAllEndAfterShots = 4;
        LaunchCards.launchOnApproach = true;
        LaunchCards.maxSpinUpWaitS = 2.5;
        // The thin timers: with the beam they must not cut a volley.
        LaunchCards.launchAllFeedS = 2.0;
        LaunchCards.firstLaunchAllFeedS = 2.0;
        PathEndSettle.maxSettleWaitS = 1.0;
        cards = new LaunchCards(
                new LaunchCards.Launcher() {
                    @Override public void spinUp() { }
                    @Override public boolean atSpeed() { return atSpeed && !reversing; }
                    @Override public int shots() { return 0; }
                    @Override public int throatBalls() {
                        java.util.Map.Entry<Double, Integer> e = ballsAt.floorEntry(now + 1e-9);
                        return e == null ? 0 : e.getValue();
                    }
                    @Override public double throatBlockedS() { return blockedS; }
                    @Override public FeedWatch.Lane lane() { return lane; }
                },
                new LaunchCards.Feeder() {
                    @Override public void feed() { reversing = false; log.add(at("feed")); }
                    @Override public void idle() { reversing = false; log.add(at("idle")); }
                    @Override public void reverse() { reversing = true; log.add(at("reverse")); }
                },
                () -> true,
                (end, pose, remainingIn) -> true,
                notes::add,
                () -> now);
    }

    @After
    public void restore() {
        BeamBreakSubsystem.quietS = quietBefore;
        BeamBreakSubsystem.firstBallWithinS = firstBefore;
        BeamBreakSubsystem.jamBlockedS = jamBefore;
        BeamBreakSubsystem.stuckGapS = gapBefore;
        LaunchCards.throatBackstopS = backstopBefore;
        LaunchCards.unjamReverseS = reverseBefore;
        LaunchCards.unjamTries = triesBefore;
    }

    private String at(String what) {
        return String.format(Locale.US, "%s@%.2f", what, now);
    }

    /** The beam sees one more ball at each of these times (seconds on the test clock). */
    private void balls(double... times) {
        int n = 0;
        for (double t : times) ballsAt.put(t, ++n);
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
    public void theFourthBallEndsTheVolleyTheMomentItPasses() {
        balls(0.50, 0.95, 1.40, 1.85);
        double ended = runToEnd(cards.launchAll(), 5);
        assertEquals("ends in the loop the 4th ball passed", 1.86, ended, 0.03);
        assertNotNull(notes.toString(), note("ended: ball 4 through the throat"));
        assertNotNull(note("4 balls through the throat"));
    }

    @Test
    public void theThinTimerNoLongerCutsAVolleyThatIsStillGoing() {
        // A slow volley: the 4th ball at 2.6 s, past the 2.0 s thin timer.
        balls(0.6, 1.3, 1.95, 2.6);
        double ended = runToEnd(cards.launchAll(), 5);
        assertEquals(2.6, ended, 0.03);
        assertNotNull(note("ball 4 through the throat"));
    }

    @Test
    public void aShortVolleyEndsWhenTheThroatGoesQuietWithTheLaneEmpty() {
        balls(0.5, 0.95);
        double ended = runToEnd(cards.launchAll(), 5);
        assertEquals("0.9 s after the 2nd ball", 1.85, ended, 0.03);
        assertNotNull(notes.toString(), note("throat quiet"));
        assertEquals(0, count("reverse"));
    }

    @Test
    public void noBallAtAllWithAnEmptyLaneEndsSoon() {
        double ended = runToEnd(cards.launchAll(), 5);
        assertEquals(1.5, ended, 0.03);
        assertNotNull(note("no ball through the throat"));
        assertEquals("an empty lane is not unjammed", 0, count("reverse"));
    }

    @Test
    public void aGapWithTheLaneHoldingIsUnjammedAndFedAgain() {
        lane = FeedWatch.Lane.HOLDS;
        // Two balls, then a hang; after the unjam (gap 1.2 s at 2.15, back out 0.3 s, at speed at once)
        // the last two go.
        balls(0.5, 0.95, 2.9, 3.35);
        double ended = runToEnd(cards.launchAll(), 8);
        assertEquals(1, count("reverse"));
        String reverse = log.get(log.indexOf(firstStarting("reverse")));
        assertEquals("stuck 1.2 s after the 2nd ball", 2.15, Double.parseDouble(reverse.substring(8)), 0.03);
        assertNotNull(note("fed again after its unjam"));
        assertEquals(3.35, ended, 0.03);
        assertNotNull(note("ball 4 through the throat"));
    }

    @Test
    public void aBallHeldInTheThroatIsBackedOutOnceThenTheCardGivesUp() {
        lane = FeedWatch.Lane.HOLDS;
        balls(0.5);
        Command card = cards.launchAll();
        card.schedule();
        double end = 8;
        while (now < end && card.isScheduled()) {
            // From 1.0 s a ball sits in the beam, and stays there.
            blockedS = now >= 1.0 ? now - 1.0 : 0;
            Scheduler.execute();
            now += LOOP_S;
        }
        assertFalse("ended", card.isScheduled());
        assertEquals("one unjam, then it gives up", 1, count("reverse"));
        assertNotNull(notes.toString(), note("gave up: a ball held in the throat"));
    }

    @Test
    public void theBackstopStillEndsAFeedNothingElseEnds() {
        BeamBreakSubsystem.quietS = 0;
        BeamBreakSubsystem.firstBallWithinS = 0;
        BeamBreakSubsystem.jamBlockedS = 0;
        BeamBreakSubsystem.stuckGapS = 0;
        balls(0.5);
        double ended = runToEnd(cards.launchAll(), 6);
        assertEquals(3.5, ended, 0.03);
        assertNotNull(note("ended at its 3.5 s backstop"));
    }

    @Test
    public void anApproachFeedEndsOnTheBeamToo() {
        cards.approach().started(new Pose(10, 10, 0));
        cards.approach().approaching(new Pose(9, 10, 0), 1.0);
        assertTrue("the approach opened the gate", count("feed") == 1);
        balls(0.5, 0.95, 1.40, 1.85);
        double ended = runToEnd(cards.launchAll(), 5);
        assertEquals(1.86, ended, 0.03);
        assertEquals("the card took the approach's feed over, no second feed", 1, count("feed"));
    }

    @Test
    public void unjamTimeDoesNotCountAgainstTheBackstop() {
        lane = FeedWatch.Lane.HOLDS;
        BeamBreakSubsystem.stuckGapS = 1.2;
        LaunchCards.throatBackstopS = 2.5;
        // Stuck at 1.7 (gap 1.2 after 0.5), out 0.3, then the wheels take 0.5 s to come back.
        balls(0.5, 2.8, 3.0, 3.2);
        Command card = cards.launchAll();
        card.schedule();
        double end = 8;
        while (now < end && card.isScheduled()) {
            if (count("reverse") == 1 && count("idle") == 1) atSpeed = now >= 2.5;
            Scheduler.execute();
            now += LOOP_S;
        }
        assertEquals("ended at ball 4, past 2.5 s from the gate", 3.2, now - LOOP_S, 0.05);
        assertNotNull(notes.toString(), note("ball 4 through the throat"));
    }

    private String firstStarting(String prefix) {
        for (String e : log) if (e.startsWith(prefix)) return e;
        return null;
    }
}
