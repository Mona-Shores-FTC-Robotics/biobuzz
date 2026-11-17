package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.localization.FlowerFieldPoints;
import org.firstinspires.ftc.teamcode.subsystems.LaunchSpeedTable;
import org.firstinspires.ftc.teamcode.util.Alliance;

import org.junit.Test;

/** The FLOWER 8-shot: where it can be fired from, when the gate opens, and when the robot leaves. */
public class FlowerVolleyTest {

    private static Pose contact(FlowerFieldPoints.Flower flower) {
        return FlowerFieldPoints.standoff(flower, FlowerPicker.contactStandoffIn);
    }

    /**
     * Pressed against the SCORING FLOWER the turret is on the speed table's lowest measured row and
     * well inside the angle a CELL takes a shot from; the same for blue, half a turn round.
     */
    @Test
    public void theScoringFlowerIsALaunchSpot() {
        double red = FlowerVolley.rangeIn(contact(FlowerFieldPoints.RED_SCORING), Alliance.RED);
        double blue = FlowerVolley.rangeIn(contact(FlowerFieldPoints.BLUE_AUDIENCE), Alliance.BLUE);
        assertEquals(43.3, red, 0.5);
        assertEquals("blue is red turned half a turn", red, blue, 1e-6);
        assertTrue("on or above the lowest measured row", red >= LaunchSpeedTable.row1RangeIn);
        double off = FlowerVolley.offSquareDeg(contact(FlowerFieldPoints.RED_SCORING), Alliance.RED);
        assertEquals(10.6, off, 1.0);
        assertEquals(off, FlowerVolley.offSquareDeg(contact(FlowerFieldPoints.BLUE_AUDIENCE), Alliance.BLUE), 1e-6);
        assertTrue(off <= FlowerVolley.maxOffSquareDeg);
    }

    /** Back at the pick's line-up the robot is nearer the HIVE than any measured row: the load is not fired on the way in. */
    @Test
    public void theLineUpIsTooCloseToFireFrom() {
        Pose lineUp = FlowerFieldPoints.standoff(FlowerFieldPoints.RED_SCORING, FlowerPicker.approachStandoffIn);
        assertTrue(FlowerVolley.rangeIn(lineUp, Alliance.RED) < LaunchSpeedTable.row1RangeIn - 8.0);
    }

    /** From the WALL FLOWER the near CELL opens almost side-on: no volley from there. */
    @Test
    public void theWallFlowerIsNot() {
        double off = FlowerVolley.offSquareDeg(contact(FlowerFieldPoints.RED_WALL), Alliance.RED);
        assertTrue("was " + off, off > 60.0);
        assertTrue(off > FlowerVolley.maxOffSquareDeg);
    }

    @Test
    public void noAllianceIsNoRange() {
        assertTrue(Double.isNaN(FlowerVolley.rangeIn(contact(FlowerFieldPoints.RED_SCORING), Alliance.UNKNOWN)));
        assertTrue(Double.isNaN(FlowerVolley.offSquareDeg(null, Alliance.RED)));
    }

    @Test
    public void theGateWaitsForSpeedAndAimButNotForever() {
        assertTrue(FlowerVolley.gateMayOpen(true, true, 0, 1500));
        assertFalse(FlowerVolley.gateMayOpen(false, true, 1499, 1500));
        assertFalse(FlowerVolley.gateMayOpen(true, false, 1499, 1500));
        assertTrue("it opens anyway", FlowerVolley.gateMayOpen(false, false, 1500, 1500));
    }

    @Test
    public void theRobotStaysUntilTheGateHasOpened() {
        assertNull(FlowerVolley.over(false, 0, 0.0, 9999, 9999, 8, 6.0, 1500));
    }

    @Test
    public void eightCountedShotsEndIt() {
        assertNull(FlowerVolley.over(true, 7, 3.0, 100, 2000, 8, 6.0, 1500));
        assertEquals("8 shots counted", FlowerVolley.over(true, 8, 3.0, 0, 2000, 8, 6.0, 1500));
        assertNull("the count can be switched off", FlowerVolley.over(true, 9, 3.0, 0, 2000, 0, 6.0, 1500));
    }

    @Test
    public void theCapEndsAFeedTheCountMissed() {
        assertEquals("the feed's cap", FlowerVolley.over(true, 5, 6.0, 100, 4000, 8, 6.0, 0));
    }

    /** A FLOWER that gave fewer than four: once the shots stop, leave; but not before the pieces have had their time to come in. */
    @Test
    public void quietEndsItOnlyOnceTheRobotHasBeenAtTheFlower() {
        assertNull("the load's last shot was a while ago, but the robot has only just arrived",
                FlowerVolley.over(true, 4, 2.5, 1600, 400, 8, 6.0, 1500));
        assertNull(FlowerVolley.over(true, 6, 3.5, 900, 2500, 8, 6.0, 1500));
        assertEquals("no more shots", FlowerVolley.over(true, 6, 4.2, 1500, 3000, 8, 6.0, 1500));
        assertNull("off", FlowerVolley.over(true, 6, 4.2, 5000, 5000, 8, 6.0, 0));
    }

    /** Fire the load, keep the FLOWER's: four counted shots shut the gate, and the robot then stays for the pieces. */
    @Test
    public void theLoadOnlyEndsAtFourAndThenPicksOn() {
        assertNull(FlowerVolley.over(true, 3, 1.5, 100, 800, FlowerVolley.loadShots, FlowerVolley.loadFeedCapS, 1500));
        assertEquals("4 shots counted",
                FlowerVolley.over(true, 4, 1.9, 0, 900, FlowerVolley.loadShots, FlowerVolley.loadFeedCapS, 1500));
        assertEquals("a count that missed a ball: the load's own cap, not the eight's",
                "the feed's cap", FlowerVolley.over(true, 3, 3.5, 300, 2500, FlowerVolley.loadShots, FlowerVolley.loadFeedCapS, 0));
        assertNull("the gate has just shut: stay for the FLOWER's pieces", FlowerVolley.pickOver(false, 200, 1500));
        assertEquals("the lane full", FlowerVolley.pickOver(true, 200, 1500));
        assertEquals("the pick's time", FlowerVolley.pickOver(false, 1500, 1500));
    }
}
