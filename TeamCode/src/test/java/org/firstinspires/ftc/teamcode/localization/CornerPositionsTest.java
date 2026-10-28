package org.firstinspires.ftc.teamcode.localization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

public class CornerPositionsTest {

    private final Pose near = new Pose(10, 10, 0);
    private final Pose far = new Pose(130, 10, Math.PI);
    private final Pose[] two = {near, far};

    @Test
    public void noCornersMeansNoSnap() {
        assertNull(CornerPositions.choose(new Pose[0], 1, true, 0, 0));
        assertNull(CornerPositions.choose(new Pose[0], 2, false, 0, 0));
    }

    @Test
    public void oneInUseIsTheFirstWhereverTheRobotThinksItIs() {
        assertSame(near, CornerPositions.choose(two, 1, false, 120, 10));
        assertSame(near, CornerPositions.choose(new Pose[]{near}, 2, false, 120, 10));
    }

    @Test
    public void twoInUsePicksTheNearerButOnlyWithAPoseOnTheField() {
        assertSame(far, CornerPositions.choose(two, 2, true, 120, 12));
        assertSame(near, CornerPositions.choose(two, 2, true, 20, 12));
        assertNull(CornerPositions.choose(two, 2, false, 120, 12));
    }

    @Test
    public void aBadCountIsReadAsOneOrTwo() {
        assertSame(near, CornerPositions.choose(two, 0, false, 120, 10));
        assertSame(far, CornerPositions.choose(two, 7, true, 120, 10));
    }

    @Test
    public void nothingIsMeasuredYet() {
        assertEquals(0, CornerPositions.forAlliance(Alliance.BLUE).length);
        assertEquals(0, CornerPositions.forAlliance(Alliance.RED).length);
        assertEquals(0, CornerPositions.forAlliance(Alliance.UNKNOWN).length);
    }
}
