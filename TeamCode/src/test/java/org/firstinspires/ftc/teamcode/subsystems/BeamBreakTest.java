package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** {@link BeamBreak}: a piece is a break of at least two readings that then clears; a flicker is not. */
public class BeamBreakTest {

    private final BeamBreak beam = new BeamBreak();
    private long t;

    /** {@code reads} readings 20 ms apart, all broken or all clear. */
    private void read(boolean broken, int reads) {
        for (int i = 0; i < reads; i++) {
            beam.step(t, broken, 2);
            t += 20;
        }
    }

    @Test
    public void aPieceCountsOnceAsItClears() {
        read(false, 3);
        read(true, 8);
        assertEquals("not yet: still in the beam", 0, beam.passes());
        assertTrue(beam.blocked());
        assertEquals(140, beam.blockedForMs(), 1e-9);
        beam.step(t, false, 2);
        assertTrue(beam.passedThisStep());
        assertEquals(1, beam.passes());
        assertEquals(160, beam.lastPassBlockedMs());
        t += 20;
        beam.step(t, false, 2);
        assertFalse("one step only", beam.passedThisStep());
        assertEquals(20, beam.clearForMs(), 1e-9);
    }

    @Test
    public void aOneReadingFlickerIsNotAPiece() {
        read(false, 2);
        read(true, 1);
        read(false, 2);
        assertEquals(0, beam.passes());
        read(true, 2);
        read(false, 1);
        assertEquals("two readings is a piece", 1, beam.passes());
    }

    @Test
    public void aPieceAlreadyInTheBeamAtTheFirstReadingCountsWhenItLeaves() {
        read(true, 3);
        read(false, 1);
        assertEquals(1, beam.passes());
    }

    @Test
    public void fourPiecesFourPasses() {
        for (int i = 0; i < 4; i++) {
            read(true, 7);
            read(false, 15);
        }
        assertEquals(4, beam.passes());
        assertEquals(0, beam.blockedForMs(), 1e-9);
    }
}
