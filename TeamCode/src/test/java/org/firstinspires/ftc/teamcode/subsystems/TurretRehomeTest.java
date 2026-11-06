package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** {@link TurretRehome}: a twirled position-servo turret is brought back to its mark by the encoder. */
public class TurretRehomeTest {

    private static final double EPS = 1e-9;
    private static final double HOME = 0.514;
    private static final double DPU = -105.0;   // higher position turns it clockwise
    private static final long MS = 1_000_000L;

    /** A turret whose servo is {@code phaseDeg} out of step: at a position it sits that far off. */
    private static final class Turret {
        final double phaseDeg;
        double angleDeg;

        Turret(double phaseDeg) {
            this.phaseDeg = phaseDeg;
        }

        void settleAt(double position) {
            angleDeg = (position - HOME) * DPU + phaseDeg;
        }
    }

    /** Runs {@code loops} loops of 20 ms with the turret settling at once to what it is sent. */
    private static double run(TurretRehome r, Turret t, double lo, double hi, int loops) {
        double sent = HOME;
        t.settleAt(sent);
        for (int i = 0; i < loops; i++) {
            double before = t.angleDeg;
            sent = r.step(HOME, 0.0, t.angleDeg, 0.0, DPU, lo, hi, 5.0, 250.0, 1.5, 80.0, i * 20 * MS);
            t.settleAt(sent);
            if (t.angleDeg != before) {
                // It moved: one loop of motion, so the still clock restarts.
                r.step(HOME, 0.0, t.angleDeg, 100.0, DPU, lo, hi, 5.0, 250.0, 1.5, 80.0, i * 20 * MS + 1);
            }
        }
        return sent;
    }

    @Test
    public void aTwirledTurretComesBackToItsMark() {
        TurretRehome r = new TurretRehome();
        Turret t = new Turret(32.0);   // three turret turns: a quarter servo turn out of step
        run(r, t, 0.0, 1.0, 200);
        assertEquals(0.0, t.angleDeg, 1.5);
        assertFalse(r.outOfReach());
        assertTrue(r.corrections() >= 1);
    }

    @Test
    public void nothingChangesUntilTheTurretHasBeenStill() {
        TurretRehome r = new TurretRehome();
        double sent = r.step(HOME, 0.0, 30.0, 0.0, DPU, 0.0, 1.0, 5.0, 250.0, 1.5, 80.0, 0);
        assertEquals(HOME, sent, EPS);
        sent = r.step(HOME, 0.0, 30.0, 0.0, DPU, 0.0, 1.0, 5.0, 250.0, 1.5, 80.0, 100 * MS);
        assertEquals("still only 100 ms", HOME, sent, EPS);
        sent = r.step(HOME, 0.0, 30.0, 50.0, DPU, 0.0, 1.0, 5.0, 250.0, 1.5, 80.0, 300 * MS);
        assertEquals("moving: no correction", HOME, sent, EPS);
    }

    @Test
    public void aSmallErrorIsLeftAlone() {
        TurretRehome r = new TurretRehome();
        r.step(HOME, 0.0, 1.0, 0.0, DPU, 0.0, 1.0, 5.0, 250.0, 1.5, 80.0, 0);
        double sent = r.step(HOME, 0.0, 1.0, 0.0, DPU, 0.0, 1.0, 5.0, 250.0, 1.5, 80.0, 300 * MS);
        assertEquals(HOME, sent, EPS);
        assertEquals(0, r.corrections());
    }

    @Test
    public void tooFarOutOfStepStopsAtALimitAndSaysSo() {
        TurretRehome r = new TurretRehome();
        Turret t = new Turret(65.0);   // two turret turns: half a servo turn out of step
        double sent = run(r, t, 0.0, 1.0, 200);
        assertEquals(1.0, sent, EPS);
        assertTrue(r.outOfReach());
    }

    @Test
    public void noScaleOrNoAngleMeansNoCorrection() {
        TurretRehome r = new TurretRehome();
        r.step(HOME, 0.0, 30.0, 0.0, Double.NaN, 0.0, 1.0, 5.0, 250.0, 1.5, 80.0, 0);
        assertEquals(HOME, r.step(HOME, 0.0, 30.0, 0.0, Double.NaN, 0.0, 1.0, 5.0, 250.0, 1.5, 80.0, 300 * MS), EPS);
        assertEquals(HOME, r.step(HOME, 0.0, Double.NaN, 0.0, DPU, 0.0, 1.0, 5.0, 250.0, 1.5, 80.0, 600 * MS), EPS);
        assertEquals(0, r.corrections());
    }

    @Test
    public void resetForgetsTheOffset() {
        TurretRehome r = new TurretRehome();
        Turret t = new Turret(20.0);
        run(r, t, 0.0, 1.0, 100);
        assertTrue(r.offset() != 0.0);
        r.reset();
        assertEquals(0.0, r.offset(), EPS);
    }

    @Test
    public void anImpossibleErrorIsNotBelieved() {
        TurretRehome r = new TurretRehome();
        // The Expansion Hub reset: the count went to 0 and the zero now reads -2000°.
        r.step(HOME, 0.0, -2000.0, 0.0, DPU, 0.0, 1.0, 5.0, 250.0, 1.5, 80.0, 0);
        double sent = r.step(HOME, 0.0, -2000.0, 0.0, DPU, 0.0, 1.0, 5.0, 250.0, 1.5, 80.0, 300 * MS);
        assertEquals(HOME, sent, EPS);
        assertTrue(r.disbelieved());
        assertEquals(0, r.corrections());
    }
}
