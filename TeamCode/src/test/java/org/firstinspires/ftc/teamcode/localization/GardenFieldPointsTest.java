package org.firstinspires.ftc.teamcode.localization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class GardenFieldPointsTest {

    private static final double EPS = 1e-9;

    /** Red's GARDEN: nose at the audience wall, side just off the red wall, lined up 9.6 in back. */
    @Test
    public void redGathersWithTheNoseAtTheAudienceWall() {
        assertEquals(8.5, GardenFieldPoints.RED.gather.x(), EPS);
        assertEquals(9.5, GardenFieldPoints.RED.gather.y(), EPS);
        assertEquals(8.5, GardenFieldPoints.RED.lineUp.x(), EPS);
        assertEquals(19.1, GardenFieldPoints.RED.lineUp.y(), EPS);
        assertEquals(Math.toRadians(270), GardenFieldPoints.RED.gather.heading(), EPS);
    }

    /** The field is turned, not mirrored: blue's GARDEN is red's half a turn about the centre. */
    @Test
    public void blueIsRedTurnedHalfATurn() {
        assertEquals(141.5 - 8.5, GardenFieldPoints.BLUE.gather.x(), EPS);
        assertEquals(141.5 - 9.5, GardenFieldPoints.BLUE.gather.y(), EPS);
        assertEquals(141.5 - 19.1, GardenFieldPoints.BLUE.lineUp.y(), EPS);
        assertEquals(Math.cos(Math.toRadians(90)), Math.cos(GardenFieldPoints.BLUE.gather.heading()), EPS);
        assertEquals(Math.sin(Math.toRadians(90)), Math.sin(GardenFieldPoints.BLUE.gather.heading()), EPS);
    }

    @Test
    public void nearestIsWithinRange() {
        assertSame(GardenFieldPoints.RED, GardenFieldPoints.nearest(40, 40, 100));
        assertSame(GardenFieldPoints.BLUE, GardenFieldPoints.nearest(100, 100, 100));
        assertNull(GardenFieldPoints.nearest(70, 70, 30));
    }
}
