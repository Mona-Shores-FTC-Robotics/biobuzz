package org.firstinspires.ftc.teamcode.localization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FixAveragerTest {

    private int stillFixFrames;
    private final FixAverager averager = new FixAverager();

    @Before
    public void keepTuning() {
        stillFixFrames = LocalizationTuning.stillFixFrames;
        LocalizationTuning.stillFixFrames = 4;
    }

    @After
    public void restoreTuning() {
        LocalizationTuning.stillFixFrames = stillFixFrames;
    }

    @Test
    public void readyOnceEnoughFramesAreIn() {
        for (int i = 0; i < 3; i++) averager.add(50, 40, 4, 60, i);
        assertFalse(averager.ready());
        averager.add(50, 40, 4, 60, 3);
        assertTrue(averager.ready());
    }

    @Test
    public void jitterAveragesOut() {
        averager.add(51, 40, 4, 60, 1);
        averager.add(49, 40, 3, 55, 2);
        averager.add(50, 41, 4, 70, 3);
        averager.add(50, 39, 4, 60, 4);

        assertEquals(50, averager.x(), 1e-9);
        assertEquals(40, averager.y(), 1e-9);
        assertEquals(1.0, averager.spreadIn(), 1e-9);
        assertEquals(3, averager.minTags());
        assertEquals(55, averager.nearestRangeIn(), 1e-9);
        assertEquals(4, averager.newestCaptureNs());
    }

    @Test
    public void resetStartsAgain() {
        averager.add(51, 40, 4, 60, 1);
        averager.reset();

        assertEquals(0, averager.frames());
        assertTrue(Double.isNaN(averager.x()));
        assertTrue(Double.isNaN(averager.spreadIn()));
    }
}
