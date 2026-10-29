package org.firstinspires.ftc.teamcode.localization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.vision.BiobuzzTags;
import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.junit.Before;
import org.junit.Test;

public class SettledStatsTest {

    private static final double ROW_Z = 49.76;
    private final TagGate gate = new TagGate(16);
    private final SettledStats stats = new SettledStats();

    @Before
    public void defaults() {
        LocalizationTuning.minFixTags = 2;
        LocalizationTuning.maxTagHeightErrorIn = 3.0;
        LocalizationTuning.maxTagRangeIn = 96.0;
        LocalizationTuning.tagOutlierIn = 3.0;
    }

    /** One frame of two tags as a robot really at (x, y), facing +y, would see them; solved at the pose's heading. */
    private void frame(double x, double y, long nowNs) {
        gate.clear();
        for (int id : new int[]{34, 35}) {
            double fx = 50 + (id - 34) * 3.75;
            double fy = 57.65;
            // Facing +y: robot forward = field +y, robot left = field -x.
            gate.add(id, fx, fy, ROW_Z, fy - y, -(fx - x), ROW_Z);
        }
        gate.solve(Math.PI / 2);
        stats.add(gate, 55.0, 9.7, Math.PI / 2, Math.PI / 2 + Math.toRadians(1.5), nowNs);
    }

    @Test
    public void averagesTheSettledPlacementAgainstThePose() {
        for (int i = 0; i < 10; i++) frame(56.0, 11.7, i * 50_000_000L);

        assertEquals(10, stats.frames());
        assertEquals(56.0, stats.x(), 1e-9);
        assertEquals(11.7, stats.y(), 1e-9);
        assertEquals(1.5, stats.headingDiffDeg(), 1e-9);
        assertEquals(0.45, stats.seconds(), 1e-9);
        HiveCell cell = BiobuzzTags.cellForTag(34);
        assertEquals(20, stats.tags(cell));
        assertEquals(1.0, stats.cellDx(cell), 1e-9);
        assertEquals(2.0, stats.cellDy(cell), 1e-9);
        assertTrue(stats.cellRangeIn(cell) > 40);
    }

    /** Lifted and put down somewhere else: only the frames after the jump count. */
    @Test
    public void aJumpStartsTheAverageAgain() {
        for (int i = 0; i < 5; i++) frame(60.0, 9.7, i * 50_000_000L);
        for (int i = 5; i < 8; i++) frame(55.0, 9.7, i * 50_000_000L);

        assertEquals(3, stats.frames());
        assertEquals(55.0, stats.x(), 1e-9);
        assertEquals(0.1, stats.seconds(), 1e-9);
    }

    @Test
    public void resetForgetsEverything() {
        frame(55.0, 9.7, 0);
        stats.reset();

        assertEquals(0, stats.frames());
        assertTrue(Double.isNaN(stats.x()));
        assertTrue(Double.isNaN(stats.headingDiffDeg()));
        assertEquals(0.0, stats.seconds(), 0.0);
    }
}
