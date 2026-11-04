package org.firstinspires.ftc.teamcode.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** The mean of the last few values: forgets the oldest, skips NaN. */
public class RecentMeanTest {

    private static final double EPS = 1e-9;

    @Test
    public void emptyIsNaN() {
        assertTrue(Double.isNaN(new RecentMean(5).mean()));
    }

    @Test
    public void theMeanIsOverTheLastFive() {
        RecentMean m = new RecentMean(5);
        m.add(100);
        m.add(200);
        assertEquals(2, m.count());
        assertEquals(150.0, m.mean(), EPS);
        for (int i = 0; i < 5; i++) m.add(1000);
        assertEquals(5, m.count());
        assertEquals("the two early ones are forgotten", 1000.0, m.mean(), EPS);
        m.add(500);
        assertEquals(900.0, m.mean(), EPS);
    }

    @Test
    public void nanIsSkippedAndResetForgets() {
        RecentMean m = new RecentMean(5);
        m.add(Double.NaN);
        assertEquals(0, m.count());
        m.add(4);
        m.reset();
        assertEquals(0, m.count());
        assertTrue(Double.isNaN(m.mean()));
    }
}
