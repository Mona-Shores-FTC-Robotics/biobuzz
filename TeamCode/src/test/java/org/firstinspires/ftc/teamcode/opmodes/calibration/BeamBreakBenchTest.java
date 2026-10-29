package org.firstinspires.ftc.teamcode.opmodes.calibration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.opmodes.calibration.BeamBreakBench.ChannelWatch;
import org.junit.Test;

public class BeamBreakBenchTest {

    private static final long MS = 1_000_000L;
    private static final long MERGE = 60 * MS;

    @Test
    public void theFirstReadingIsNotAChange() {
        ChannelWatch w = new ChannelWatch();
        assertFalse(w.update(false, 0, MERGE));
        assertFalse(w.high);
        assertFalse(w.seen());
        assertEquals(0, w.breaks);
    }

    @Test
    public void oneCleanPassIsOneBreakAndOnePiece() {
        ChannelWatch w = new ChannelWatch();
        w.update(true, 0, MERGE);
        assertTrue(w.update(false, 100 * MS, MERGE));
        assertTrue(w.update(true, 240 * MS, MERGE));
        assertEquals(1, w.breaks);
        assertEquals(1, w.pieces);
        assertEquals(140 * MS, w.lastBreakNanos);
        assertTrue(w.seen());
    }

    @Test
    public void aShortGapThroughAHoleIsStillOnePiece() {
        ChannelWatch w = new ChannelWatch();
        w.update(true, 0, MERGE);
        w.update(false, 100 * MS, MERGE);
        w.update(true, 150 * MS, MERGE);   // the beam shines through a hole
        w.update(false, 162 * MS, MERGE);
        w.update(true, 240 * MS, MERGE);
        assertEquals(2, w.breaks);
        assertEquals(1, w.pieces);
        assertEquals(12 * MS, w.shortestGapNanos);
    }

    @Test
    public void twoBallsALongGapApartAreTwoPieces() {
        ChannelWatch w = new ChannelWatch();
        w.update(true, 0, MERGE);
        w.update(false, 100 * MS, MERGE);
        w.update(true, 200 * MS, MERGE);
        w.update(false, 400 * MS, MERGE);
        w.update(true, 500 * MS, MERGE);
        assertEquals(2, w.breaks);
        assertEquals(2, w.pieces);
    }

    @Test
    public void resetClearsTheCountsButKeepsTheState() {
        ChannelWatch w = new ChannelWatch();
        w.update(true, 0, MERGE);
        w.update(false, 100 * MS, MERGE);
        w.reset();
        assertEquals(0, w.breaks);
        assertEquals(0, w.pieces);
        assertFalse(w.seen());
        assertFalse(w.high);
        w.update(true, 200 * MS, MERGE);
        w.update(false, 210 * MS, MERGE);
        assertEquals(1, w.pieces);   // nothing counted since the reset to merge into
    }

    @Test
    public void portLabelsMatchTheHubsPrinting() {
        assertEquals("0-1", BeamBreakBench.portLabel(0));
        assertEquals("0-1", BeamBreakBench.portLabel(1));
        assertEquals("6-7", BeamBreakBench.portLabel(7));
    }
}
