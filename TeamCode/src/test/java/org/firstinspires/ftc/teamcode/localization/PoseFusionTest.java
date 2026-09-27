package org.firstinspires.ftc.teamcode.localization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PoseFusionTest {

    private static final long MS = 1_000_000L;
    private static final double EPS = 1e-6;

    private PoseFusion fusion() {
        // drift 0.01 in²/in, 3-sigma gate, 3 rad/s turn limit, trusted under 3 in, inflate x4 after 3 outliers
        return new PoseFusion(0.01, 3.0, 3.0, 3.0, 3, 4.0);
    }

    /** A field point at (fx, fy) as the robot at (x, y, h) would see it, robot frame. */
    private static double[] seen(double fx, double fy, double x, double y, double h) {
        double dx = fx - x, dy = fy - y;
        double c = Math.cos(h), s = Math.sin(h);
        return new double[]{c * dx + s * dy, -s * dx + c * dy};
    }

    @Test
    public void fixesAreIgnoredUntilTheHeadingIsFieldReferenced() {
        PoseFusion f = fusion();
        f.recordOdometry(0, 0, 0, 0);

        assertEquals(PoseFusion.Verdict.HEADING_NOT_REFERENCED, f.addFix(0, 100, 0, 100, 0, 1));
        assertFalse(f.isTrusted());
    }

    @Test
    public void aGoodFixPullsTheDriftedPoseTowardTheTruth() {
        PoseFusion f = fusion();
        f.reset(6.0, true);
        // Odometry says (10, 20); the robot is really at (13, 20).
        f.recordOdometry(0, 10, 20, 0);
        double[] v = seen(100, 20, 13, 20, 0);

        for (int i = 0; i < 20; i++) {
            assertEquals(PoseFusion.Verdict.ACCEPTED, f.addFix(0, 100, 20, v[0], v[1], 1.0));
        }
        assertEquals(3.0, f.offsetX(), 0.05);
        assertEquals(0.0, f.offsetY(), 0.05);
        assertTrue(f.isTrusted());
    }

    @Test
    public void theSightingIsRotatedByTheHeading() {
        PoseFusion f = fusion();
        f.reset(6.0, true);
        double h = Math.PI / 2; // facing +y
        f.recordOdometry(0, 50, 50, h);
        double[] v = seen(50, 100, 52, 49, h); // truly at (52, 49)

        for (int i = 0; i < 30; i++) f.addFix(0, 50, 100, v[0], v[1], 0.5);

        assertEquals(2.0, f.offsetX(), 0.05);
        assertEquals(-1.0, f.offsetY(), 0.05);
    }

    @Test
    public void aDelayedFixIsComparedWithWhereTheRobotWasWhenTheFrameWasTaken() {
        PoseFusion f = fusion();
        f.reset(6.0, true);
        // Driving +x at 20 in/s with no drift, sampled every 20 ms.
        for (int i = 0; i <= 10; i++) f.recordOdometry(i * 20 * MS, i * 0.4, 0, 0);
        // A frame from t = 100 ms (robot at x = 2.0) arrives now, at t = 200 ms (x = 4.0).
        double[] v = seen(100, 0, 2.0, 0, 0);

        assertEquals(PoseFusion.Verdict.ACCEPTED, f.addFix(100 * MS, 100, 0, v[0], v[1], 1.0));
        assertEquals(0.0, f.offsetX(), EPS); // no correction: odometry was right
    }

    @Test
    public void aFrameFromOutsideTheBufferIsTooOld() {
        PoseFusion f = fusion();
        f.reset(6.0, true);
        f.recordOdometry(100 * MS, 0, 0, 0);
        f.recordOdometry(120 * MS, 0, 0, 0);

        assertEquals(PoseFusion.Verdict.TOO_OLD, f.addFix(50 * MS, 10, 0, 10, 0, 1));
        assertEquals(PoseFusion.Verdict.TOO_OLD, f.addFix(200 * MS, 10, 0, 10, 0, 1));
    }

    @Test
    public void aFrameTakenWhileSpinningIsRejected() {
        PoseFusion f = fusion();
        f.reset(6.0, true);
        f.recordOdometry(0, 0, 0, 0);
        f.recordOdometry(20 * MS, 0, 0, 0.2); // 10 rad/s

        assertEquals(PoseFusion.Verdict.TURNING, f.addFix(10 * MS, 10, 0, 10, 0, 1));
    }

    @Test
    public void oneWildReadingCannotMoveACertainPose() {
        PoseFusion f = fusion();
        f.reset(1.0, true);
        f.recordOdometry(0, 0, 0, 0);

        assertEquals(PoseFusion.Verdict.OUTLIER, f.addFix(0, 100, 0, 60, 0, 1.0)); // says x = 40
        assertEquals(0.0, f.offsetX(), EPS);
    }

    @Test
    public void aRunOfConsistentReadingsWinsBackAPoseAfterAJolt() {
        PoseFusion f = fusion();
        f.reset(1.0, true);
        f.recordOdometry(0, 0, 0, 0);
        // The robot was knocked 20 in; every camera fix agrees.
        int accepted = 0;
        for (int i = 0; i < 40 && accepted == 0; i++) {
            if (f.addFix(0, 100, 0, 80, 0, 1.0) == PoseFusion.Verdict.ACCEPTED) accepted++;
        }
        assertTrue("consistent fixes were never accepted", accepted > 0);
        for (int i = 0; i < 20; i++) f.addFix(0, 100, 0, 80, 0, 1.0);
        assertEquals(20.0, f.offsetX(), 0.5);
    }

    @Test
    public void uncertaintyGrowsWithDistanceDrivenNotTime() {
        PoseFusion f = fusion();
        f.reset(1.0, true);
        for (int i = 0; i < 100; i++) f.recordOdometry(i * 20 * MS, 0, 0, 0); // parked 2 s
        assertEquals(1.0, f.sigmaIn(), EPS);

        for (int i = 0; i < 1000; i++) f.recordOdometry((100 + i) * 20 * MS, i, 0, 0); // 999 in
        assertEquals(Math.sqrt(1.0 + 0.01 * 999), f.sigmaIn(), 1e-3);
        assertFalse(f.isTrusted());
    }

    @Test
    public void resetForgetsHistoryFromTheOldFrame() {
        PoseFusion f = fusion();
        f.reset(6.0, true);
        f.recordOdometry(0, 0, 0, 0);
        f.reset(6.0, true);

        assertEquals(PoseFusion.Verdict.TOO_OLD, f.addFix(0, 10, 0, 10, 0, 1));
    }
}
