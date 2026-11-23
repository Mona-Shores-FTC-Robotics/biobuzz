package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** {@link FeedWatch}: when a feed is all out, run dry, or stuck, from the throat beam's count. */
public class FeedWatchTest {

    private final FeedWatch watch = new FeedWatch();

    private FeedWatch.Verdict at(double now, int passes, double blockedS, FeedWatch.Lane lane) {
        return watch.step(now, passes, blockedS, lane, 4, 0.9, 1.5, 0.5, 1.2);
    }

    @Test
    public void allOutAtTheFourthBall() {
        watch.start(10.0, 7);
        assertEquals(FeedWatch.Verdict.FEEDING, at(10.5, 8, 0, FeedWatch.Lane.HOLDS));
        assertEquals(FeedWatch.Verdict.FEEDING, at(11.4, 10, 0, FeedWatch.Lane.HOLDS));
        assertEquals(FeedWatch.Verdict.ALL_OUT, at(11.85, 11, 0, FeedWatch.Lane.EMPTY));
        assertEquals(4, watch.balls());
        assertEquals("ball 4 through the throat", watch.why());
    }

    @Test
    public void quietWithTheLaneEmptyIsEmptyButNotWithItHolding() {
        watch.start(0, 0);
        at(0.5, 1, 0, FeedWatch.Lane.EMPTY);
        assertEquals(FeedWatch.Verdict.FEEDING, at(1.3, 1, 0, FeedWatch.Lane.EMPTY));
        assertEquals(FeedWatch.Verdict.EMPTY, at(1.45, 1, 0, FeedWatch.Lane.EMPTY));
        assertEquals(FeedWatch.Verdict.EMPTY, at(1.45, 1, 0, FeedWatch.Lane.UNKNOWN));
        assertEquals("holding: wait longer", FeedWatch.Verdict.FEEDING, at(1.45, 1, 0, FeedWatch.Lane.HOLDS));
        assertEquals(FeedWatch.Verdict.STUCK, at(1.7, 1, 0, FeedWatch.Lane.HOLDS));
    }

    @Test
    public void noBallAtAllIsEmptyOrStuckByTheLane() {
        watch.start(0, 3);
        assertEquals(FeedWatch.Verdict.FEEDING, at(1.4, 3, 0, FeedWatch.Lane.HOLDS));
        assertEquals(FeedWatch.Verdict.STUCK, at(1.5, 3, 0, FeedWatch.Lane.HOLDS));
        assertEquals(FeedWatch.Verdict.EMPTY, at(1.5, 3, 0, FeedWatch.Lane.EMPTY));
    }

    @Test
    public void aBallHeldInTheBeamIsStuckWhateverTheLaneSays() {
        watch.start(0, 0);
        assertEquals(FeedWatch.Verdict.FEEDING, at(0.8, 0, 0.45, FeedWatch.Lane.EMPTY));
        assertEquals("held in the throat: the throat's jam, not the lane's",
                FeedWatch.Verdict.HELD, at(0.85, 0, 0.5, FeedWatch.Lane.EMPTY));
        assertTrue(watch.why(), watch.why().startsWith("a ball held in the throat"));
    }

    @Test
    public void aRestartKeepsTheBallsAndStartsTheClocksOver() {
        watch.start(0, 0);
        at(0.5, 1, 0, FeedWatch.Lane.HOLDS);
        at(0.95, 2, 0, FeedWatch.Lane.HOLDS);
        assertEquals(FeedWatch.Verdict.STUCK, at(2.2, 2, 0, FeedWatch.Lane.HOLDS));
        watch.restart(2.7);
        assertEquals(FeedWatch.Verdict.FEEDING, at(3.5, 2, 0, FeedWatch.Lane.HOLDS));
        assertEquals(FeedWatch.Verdict.ALL_OUT, at(3.6, 4, 0, FeedWatch.Lane.HOLDS));
    }

    @Test
    public void offKnobsNeverEndAFeed() {
        watch.start(0, 0);
        assertEquals(FeedWatch.Verdict.FEEDING,
                watch.step(30, 0, 10, FeedWatch.Lane.HOLDS, Double.NaN, 0, 0, 0, 0));
        assertEquals("unstarted: feeding", FeedWatch.Verdict.FEEDING, new FeedWatch().step(1, 5, 0,
                FeedWatch.Lane.EMPTY, 4, 0.9, 1.5, 0.5, 1.2));
    }

    // ---- a beam behind the gate (BeamBreakSubsystem.throatBehindGate): judged by its clear time only ----

    private FeedWatch.Verdict gate(double now, double clearS, FeedWatch.Lane lane) {
        return watch.stepBehindGate(now, clearS, lane, 0.5, 1.2);
    }

    @Test
    public void behindTheGateAVolleyEndsOnceTheBeamStaysClearWithTheLaneEmpty() {
        watch.start(10.0, 0);
        assertEquals("balls coming through touching", FeedWatch.Verdict.FEEDING, gate(11.6, 0, FeedWatch.Lane.HOLDS));
        assertEquals(FeedWatch.Verdict.FEEDING, gate(12.0, 0.4, FeedWatch.Lane.EMPTY));
        assertEquals(FeedWatch.Verdict.EMPTY, gate(12.1, 0.5, FeedWatch.Lane.EMPTY));
        assertEquals(FeedWatch.Verdict.EMPTY, gate(12.1, 0.5, FeedWatch.Lane.UNKNOWN));
        assertEquals("nothing at the gate 0.50 s", watch.why());
        assertEquals("it counts nothing", 0, watch.balls());
    }

    @Test
    public void behindTheGateABallWaitingIsNeverHeldOrAllOut() {
        watch.start(0, 0);
        for (double t = 0; t <= 30; t += 0.5) {
            assertEquals(FeedWatch.Verdict.FEEDING, gate(t, 0, FeedWatch.Lane.EMPTY));
            assertEquals(FeedWatch.Verdict.FEEDING, gate(t, 0, FeedWatch.Lane.HOLDS));
        }
    }

    @Test
    public void behindTheGateNothingArrivingWithTheLaneHoldingIsALaneJam() {
        watch.start(0, 0);
        assertEquals("holding: not empty", FeedWatch.Verdict.FEEDING, gate(0.8, 0.8, FeedWatch.Lane.HOLDS));
        assertEquals(FeedWatch.Verdict.STUCK, gate(1.2, 1.2, FeedWatch.Lane.HOLDS));
        assertEquals("nothing at the gate 1.20 s, the lane holding", watch.why());
    }

    @Test
    public void behindTheGateOnlyTheFeedsOwnClearTimeCounts() {
        // Clear for 5 s before the gate opened (an empty lane waiting): the feed's clock starts at the gate.
        watch.start(20.0, 0);
        assertEquals(FeedWatch.Verdict.FEEDING, gate(20.3, 5.3, FeedWatch.Lane.EMPTY));
        assertEquals(FeedWatch.Verdict.EMPTY, gate(20.5, 5.5, FeedWatch.Lane.EMPTY));
        watch.restart(21.0);
        assertEquals("a restart starts it over", FeedWatch.Verdict.FEEDING, gate(21.2, 6.2, FeedWatch.Lane.EMPTY));
    }

    @Test
    public void behindTheGateOffKnobsAndAnUnstartedWatchNeverEndAFeed() {
        watch.start(0, 0);
        assertEquals(FeedWatch.Verdict.FEEDING, watch.stepBehindGate(30, 30, FeedWatch.Lane.EMPTY, Double.NaN, 0));
        assertEquals(FeedWatch.Verdict.FEEDING, watch.stepBehindGate(30, 30, FeedWatch.Lane.HOLDS, 0, Double.NaN));
        assertEquals(FeedWatch.Verdict.FEEDING, new FeedWatch().stepBehindGate(1, 1, FeedWatch.Lane.EMPTY, 0.5, 1.2));
    }
}
