package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link CycleReadout}: the practice readout times pick-ups and cycles as {@code read_logs.py cycles}
 * does, from pieces in, the lane reading full, the fire press, the feed gate and the launch count.
 */
public class CycleReadoutTest {

    private static final double DT = 0.02;
    private static final double EPS = DT + 1e-9;

    /** A TeleOp loop at 50 Hz: events are scheduled at times, then {@link #runTo} steps through them. */
    private static final class Rig {
        final List<String> log = new ArrayList<>();
        final CycleReadout r = new CycleReadout(log::add);
        double t;
        int launched;
        int lane;
        boolean gate;
        final List<double[]> ins = new ArrayList<>();
        final List<Double> presses = new ArrayList<>();
        final List<double[]> gates = new ArrayList<>();
        final List<Double> shots = new ArrayList<>();

        Rig() {
            r.start(0.0);
        }

        Rig in(double... ts) {
            for (double x : ts) ins.add(new double[]{x});
            return this;
        }

        Rig press(double x) {
            presses.add(x);
            return this;
        }

        /** The gate open from a to b, launching {@code balls} evenly inside it. */
        Rig feed(double a, double b, int balls) {
            gates.add(new double[]{a, b});
            for (int i = 0; i < balls; i++) shots.add(a + (b - a) * (i + 0.5) / balls);
            return this;
        }

        void runTo(double end) {
            while (t < end - 1e-9) {
                t += DT;
                boolean pieceIn = false;
                for (double[] x : ins) if (x[0] > t - DT && x[0] <= t) pieceIn = true;
                if (pieceIn) lane++;
                boolean press = false;
                for (double x : presses) if (x > t - DT && x <= t) press = true;
                gate = false;
                for (double[] g : gates) if (t >= g[0] && t < g[1]) gate = true;
                for (double x : shots) {
                    if (x > t - DT && x <= t) {
                        launched++;
                        lane = Math.max(0, lane - 1);
                    }
                }
                r.update(t, pieceIn, lane >= 4, press, gate, launched);
            }
        }
    }

    /**
     * The after-practice report's own fixture (tools/pull-logs/test_read_logs.py, {@code Cycles}):
     * the same events give the same split, so the Driver Station and {@code read_logs.py cycles} agree.
     */
    @Test
    public void theReportsFixtureGivesTheReportsSplit() {
        Rig g = new Rig().in(5.0, 8.0, 30.0, 33.0, 44.0).press(19.6).press(49.5)
                .feed(20.0, 21.5, 2).feed(40.0, 40.3, 0).feed(50.0, 52.0, 4);
        g.runTo(23.0);
        assertEquals(1, g.r.cycles());
        assertEquals(2, g.r.lastBalls());
        assertEquals(5.0, g.r.toPiecesS(), EPS);
        assertEquals(3.0, g.r.pickUpPhaseS(), EPS);
        assertEquals(11.6, g.r.driveLoadedS(), EPS);
        assertEquals(0.4, g.r.spinUpAimS(), EPS);
        assertEquals(1.5, g.r.feedS(), EPS);
        g.runTo(60.0);
        assertEquals(2, g.r.cycles());
        assertEquals(4, g.r.lastBalls());
        assertEquals(1, g.r.lastEmptyFeeds());
        assertEquals(52.0 - 21.5, g.r.lastCycleS(), EPS);
        assertEquals(30.0 - 21.5, g.r.toPiecesS(), EPS);
        assertEquals(14.0, g.r.pickUpPhaseS(), EPS);
        assertEquals(5.5, g.r.driveLoadedS(), EPS);
        assertEquals(0.5, g.r.spinUpAimS(), EPS);
        assertEquals(2.0, g.r.feedS(), EPS);
    }

    @Test
    public void aFullLoadIsAPickUpToGreenAndACycleToTheFeedsEnd() {
        Rig g = new Rig().in(2.0, 2.8, 3.6, 4.4).press(6.0).feed(6.4, 7.7, 4);
        g.runTo(4.5);
        // The lane went green on the 4th piece: the pick-up is shown at once.
        assertEquals(2.4, g.r.lastPickUpS(), EPS);
        assertEquals(4, g.r.lastPickUpPieces());
        assertTrue(g.r.lastPickUpFull());
        assertEquals("practice: pick-up 2.4 s, 4 in, lane full", g.log.get(0));
        assertEquals(0, g.r.cycles());
        g.runTo(10.0);
        assertEquals(1, g.r.cycles());
        assertEquals(4, g.r.lastBalls());
        assertEquals(7.7, g.r.lastCycleS(), EPS);
        assertEquals(2.0, g.r.toPiecesS(), EPS);
        assertEquals(2.4, g.r.pickUpPhaseS(), EPS);
        assertEquals(1.6, g.r.driveLoadedS(), EPS);
        assertEquals(0.4, g.r.spinUpAimS(), EPS);
        assertEquals(1.3, g.r.feedS(), EPS);
        assertEquals(0, g.r.lastEmptyFeeds());
        assertTrue(g.log.get(1).startsWith("practice: cycle 1 7.7 s, 4 balls: to pieces 2.0, pick up 2.4"));
    }

    @Test
    public void theNextCycleStartsAtTheFeedsEndAndTheSessionKeepsBestAndMedian() {
        Rig g = new Rig()
                .in(2.0, 2.5, 3.0, 3.5).press(4.0).feed(4.3, 5.5, 4)          // cycle 1: 5.5 s
                .in(9.0, 9.5, 10.0, 10.5).press(11.0).feed(11.3, 12.5, 4)     // cycle 2: 7.0 s
                .in(15.0, 15.5, 16.0, 16.5).press(18.0).feed(18.3, 19.5, 4);  // cycle 3: 7.0 s
        g.runTo(22.0);
        assertEquals(3, g.r.cycles());
        assertEquals(7.0, g.r.lastCycleS(), EPS);
        assertEquals(15.0 - 12.5, g.r.toPiecesS(), EPS);
        assertEquals(5.5, g.r.bestCycleS(), EPS);
        assertEquals(7.0, g.r.medianCycleS(), EPS);
        assertEquals(1.5, g.r.bestPickUpS(), EPS);
    }

    @Test
    public void aShortFireEndsThePickUpAtTheLastPieceIn() {
        Rig g = new Rig().in(2.0, 3.0, 3.8).press(5.0).feed(5.4, 6.4, 3);
        g.runTo(5.3);
        assertTrue(Double.isNaN(g.r.lastPickUpS()));
        g.runTo(5.5);
        // The gate opened on 3: the pick-up is first piece to last, marked short.
        assertEquals(1.8, g.r.lastPickUpS(), EPS);
        assertEquals(3, g.r.lastPickUpPieces());
        assertFalse(g.r.lastPickUpFull());
        assertEquals("practice: pick-up 1.8 s, 3 in, fired short", g.log.get(0));
        g.runTo(8.0);
        assertEquals(3, g.r.lastBalls());
        assertEquals(1.2, g.r.driveLoadedS(), EPS);
    }

    @Test
    public void aFiredShortPickUpNeverSetsTheBest() {
        Rig g = new Rig()
                .in(2.0, 2.5, 3.0, 3.5).press(4.0).feed(4.3, 5.5, 4)   // full: pick-up 1.5 s
                .in(8.0).press(8.5).feed(8.8, 9.2, 1);                  // one ball: pick-up 0 s, short
        g.runTo(11.0);
        assertEquals(0.0, g.r.lastPickUpS(), EPS);
        assertFalse(g.r.lastPickUpFull());
        assertEquals(1.5, g.r.bestPickUpS(), EPS);
        assertEquals(1.5, g.r.medianPickUpS(), EPS);
    }

    @Test
    public void aFiredShortPickUpAloneLeavesNoBest() {
        Rig g = new Rig().in(2.0).press(3.0).feed(3.3, 3.7, 1);
        g.runTo(5.0);
        assertEquals(0.0, g.r.lastPickUpS(), EPS);
        assertTrue(Double.isNaN(g.r.bestPickUpS()));
    }

    @Test
    public void anEmptyFireIsNoVolleyAndItsPressIsAnEarlyPress() {
        // Circle at an empty lane: the gate opens, nothing launches. Then the pickup, and a real volley.
        Rig g = new Rig().press(1.0).feed(1.3, 2.0, 0)
                .in(4.0, 4.5, 5.0, 5.5).press(7.0).feed(7.3, 8.5, 4);
        g.runTo(4.0);
        assertEquals(0, g.r.cycles());
        assertTrue(g.log.isEmpty());
        g.runTo(10.0);
        assertEquals(1, g.r.cycles());
        assertEquals(1, g.r.lastEmptyFeeds());
        assertEquals(1, g.r.lastEarlyPresses());
        // Spin-up and aim is timed from the press after the pickup, not the early one.
        assertEquals(0.3, g.r.spinUpAimS(), EPS);
        assertEquals(1.5, g.r.driveLoadedS(), EPS);
        assertEquals(8.5, g.r.lastCycleS(), EPS);
        assertEquals(4.0, g.r.toPiecesS(), EPS);
    }

    @Test
    public void aJamFeedsNothingThenTheUnjammedVolleyCarriesTheCycle() {
        // Full lane, fire: the gate opens and nothing comes out (the jam). Unjam, fire again.
        Rig g = new Rig().in(2.0, 2.5, 3.0, 3.5).press(4.0).feed(4.3, 5.3, 0)
                .press(8.0).feed(8.3, 9.5, 4);
        g.runTo(7.0);
        assertEquals(0, g.r.cycles());
        assertEquals(1, g.log.size()); // only the pick-up
        g.runTo(12.0);
        assertEquals(1, g.r.cycles());
        assertEquals(4, g.r.lastBalls());
        assertEquals(1, g.r.lastEmptyFeeds());
        assertEquals(9.5, g.r.lastCycleS(), EPS);
        // The first press after the last piece in is the jammed one: spin-up and aim includes the unjam.
        assertEquals(0.5, g.r.driveLoadedS(), EPS);
        assertEquals(4.3, g.r.spinUpAimS(), EPS);
        assertTrue(g.log.get(1).endsWith("1 empty feeds, 0 early presses"));
    }

    @Test
    public void aGateLetGoAndPressedAgainIsOneFeed() {
        Rig g = new Rig().in(2.0, 2.5, 3.0, 3.5).press(4.0).feed(4.3, 5.0, 2).press(5.5).feed(5.5, 6.2, 2);
        g.runTo(9.0);
        assertEquals(1, g.r.cycles());
        assertEquals(4, g.r.lastBalls());
        assertEquals(6.2 - 4.3, g.r.feedS(), EPS);
    }

    @Test
    public void piecesInDuringTheFeedBelongToNeitherCycle() {
        Rig g = new Rig().in(2.0, 2.5, 3.0, 3.5).press(4.0).feed(4.3, 5.5, 4)
                .in(5.0) // in while the gate is open
                .in(8.0, 8.5, 9.0, 9.5).press(10.0).feed(10.3, 11.5, 4);
        g.runTo(13.0);
        assertEquals(2, g.r.cycles());
        assertEquals(8.0 - 5.5, g.r.toPiecesS(), EPS);
    }

    @Test
    public void theMatchPageShowsNothingButAPromptUntilTheFirstVolley() {
        List<String> lines = new ArrayList<>();
        org.firstinspires.ftc.teamcode.controls.Display d = new org.firstinspires.ftc.teamcode.controls.Display(lines::add);
        Rig g = new Rig();
        g.r.show(d);
        assertTrue(String.join("\n", lines).contains("fire a volley to time a cycle"));
        g.in(2.0, 2.5, 3.0, 3.5).press(4.0).feed(4.3, 5.5, 4).runTo(8.0);
        lines.clear();
        d = new org.firstinspires.ftc.teamcode.controls.Display(lines::add);
        g.r.show(d);
        String page = String.join("\n", lines);
        assertTrue(page, page.contains("PICK-UP 1.5 s (4 in)"));
        assertTrue(page, page.contains("CYCLE 5.5 s · 4 balls"));
        assertTrue(page, page.contains("cycle best 5.5 · median 5.5 s (1)"));
    }
}
