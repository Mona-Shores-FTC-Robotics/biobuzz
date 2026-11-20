package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** The speed table follows the range during a feed only with TeleOpConfig.speedTableDuringFeed on (20 Nov 2026). */
public class SpeedTableDuringFeedTest {

    @Test
    public void betweenFeedsTheTableAlwaysFollows() {
        assertTrue(BasicDriveTeleOp.tableFollowsRange(false, false));
        assertTrue(BasicDriveTeleOp.tableFollowsRange(false, true));
    }

    @Test
    public void duringAFeedItHoldsUnlessSwitchedOn() {
        assertFalse(BasicDriveTeleOp.tableFollowsRange(true, false));
        assertTrue(BasicDriveTeleOp.tableFollowsRange(true, true));
    }

    @Test
    public void offByDefault() {
        assertFalse(TeleOpConfig.speedTableDuringFeed);
    }
}
