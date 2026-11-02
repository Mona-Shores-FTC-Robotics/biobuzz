package org.firstinspires.ftc.teamcode.localization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class HeadingFixTest {

    @Test
    public void firesOnlyPastTheThresholdWithEnoughFrames() {
        int n = LocalizationTuning.headingFixFrames;
        double d = LocalizationTuning.headingFixDeg;
        assertFalse("fit noise", HeadingFix.due(d - 0.5, n));
        assertFalse("too few frames", HeadingFix.due(d + 10, n - 1));
        assertFalse("no heading", HeadingFix.due(Double.NaN, n));
        assertTrue(HeadingFix.due(d + 0.5, n));
        assertTrue("either sign", HeadingFix.due(-(d + 0.5), n));
    }

    @Test
    public void correctsTowardTheCamera() {
        // Pinpoint says 90 deg, the camera's fit says 102: the corrected heading is 102.
        assertEquals(Math.toRadians(102), HeadingFix.corrected(Math.toRadians(90), 12.0), 1e-12);
        assertEquals(Math.toRadians(78), HeadingFix.corrected(Math.toRadians(90), -12.0), 1e-12);
    }
}
