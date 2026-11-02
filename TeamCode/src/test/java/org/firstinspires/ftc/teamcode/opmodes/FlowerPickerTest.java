package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.localization.FlowerFieldPoints;

import org.junit.Test;

/** The pick's geometry: how far along the line into a FLOWER, how far off it, and the guards. */
public class FlowerPickerTest {

    private static final double EPS = 1e-9;

    /** Facing +y at the contact point (10, 20): a robot 5 in short is -5 along, 3 in beyond is +3. */
    @Test
    public void alongTrackIsNegativeBeforeTheContactPoint() {
        double h = Math.PI / 2;
        assertEquals(-5.0, FlowerPicker.alongTrackIn(10, 15, 10, 20, h), EPS);
        assertEquals(3.0, FlowerPicker.alongTrackIn(10, 23, 10, 20, h), EPS);
        assertEquals("sideways does not count as along", -5.0, FlowerPicker.alongTrackIn(14, 15, 10, 20, h), EPS);
    }

    /** Facing +y, the robot's left is -x: a robot at x 8 is 2 in left of the line through x 10. */
    @Test
    public void lateralIsPositiveToTheLeft() {
        double h = Math.PI / 2;
        assertEquals(2.0, FlowerPicker.lateralIn(8, 15, 10, 20, h), EPS);
        assertEquals(-2.0, FlowerPicker.lateralIn(12, 15, 10, 20, h), EPS);
        assertEquals(0.0, FlowerPicker.lateralIn(10, 5, 10, 20, h), EPS);
    }

    /** Facing -x (the red WALL FLOWER's way): +y is to the robot's right. */
    @Test
    public void lateralFollowsTheHeading() {
        assertEquals(-3.0, FlowerPicker.lateralIn(20, 50, 15, 47, Math.PI), EPS);
    }

    @Test
    public void wrapTakesTheShortWayRound() {
        assertEquals(-Math.PI / 2, FlowerPicker.wrapRad(3 * Math.PI / 2), EPS);
        assertEquals(Math.PI, FlowerPicker.wrapRad(-Math.PI), EPS);
        assertEquals(0.1, FlowerPicker.wrapRad(0.1 + 4 * Math.PI), 1e-9);
    }

    @Test
    public void clampGuardsPanelsTypos() {
        assertEquals(0.25, FlowerPicker.clamp(3.0, 0.25), EPS);
        assertEquals(-0.25, FlowerPicker.clamp(-3.0, 0.25), EPS);
        assertEquals(0.1, FlowerPicker.clamp(0.1, 0.25), EPS);
        assertEquals("NaN asks for nothing", 0.0, FlowerPicker.clamp(Double.NaN, 0.25), EPS);
        assertEquals("a bad limit asks for nothing", 0.0, FlowerPicker.clamp(0.1, Double.NaN), EPS);
        assertEquals(0.0, FlowerPicker.clamp(0.1, -1.0), EPS);
    }

    @Test
    public void negativeAndNanDistancesAndTimesReadAsZero() {
        assertEquals(0.0, FlowerPicker.nn(-4.0), EPS);
        assertEquals(0.0, FlowerPicker.nn(Double.NaN), EPS);
        assertEquals(12.0, FlowerPicker.nn(12.0), EPS);
    }

    /** An Auto's drive leaves the robot at the pick spot: close enough skips the approach path. */
    @Test
    public void atPoseNeedsBothPlaceAndHeading() {
        com.pedropathing.math.Pose spot = new com.pedropathing.math.Pose(26.71, 47.36, Math.PI);
        assertEquals(true, FlowerPicker.atPose(new com.pedropathing.math.Pose(27.5, 47.0, Math.PI - 0.05), spot, 1.5, 5.0));
        assertEquals("too far", false, FlowerPicker.atPose(new com.pedropathing.math.Pose(29.0, 47.36, Math.PI), spot, 1.5, 5.0));
        assertEquals("turned", false, FlowerPicker.atPose(new com.pedropathing.math.Pose(26.71, 47.36, Math.PI - 0.2), spot, 1.5, 5.0));
        assertEquals("the short way round", true, FlowerPicker.atPose(new com.pedropathing.math.Pose(26.71, 47.36, -Math.PI + 0.02), spot, 1.5, 5.0));
    }

    @Test
    public void speedFractionsStayBetweenSlowAndFull() {
        assertEquals(0.35, FlowerPicker.unitFraction(0.35), EPS);
        assertEquals(1.0, FlowerPicker.unitFraction(3.0), EPS);
        assertEquals("a typo drives slowly, it does not stop", 0.05, FlowerPicker.unitFraction(0.0), EPS);
        assertEquals(0.05, FlowerPicker.unitFraction(Double.NaN), EPS);
    }

    /** An Auto's pick squares up on the FLOWER's line at the distance its drive stopped, held to 18–24 in. */
    @Test
    public void squareUpLandsOnTheFlowersLineAtTheSameDistance() {
        FlowerFieldPoints.Flower wall = FlowerFieldPoints.RED_WALL; // (2.71, 47.36), facing -x
        // 19.5 in out, 4 in to one side and 10° turned: squares to 19.5 in out, on the line, facing it.
        Pose spot = new Pose(22.21, 51.36, Math.PI - Math.toRadians(10));
        assertNull(FlowerPicker.squareUpRefusal(wall, spot, 12, 30));
        Pose square = FlowerPicker.squareUpPose(wall, spot, 18, 24);
        assertEquals(22.21, square.x(), EPS);
        assertEquals(47.36, square.y(), EPS);
        assertEquals(Math.PI, square.heading(), EPS);
        // Too close is pulled back to 18 (the stick swings down clear there); too far comes in to 24.
        assertEquals(20.71, FlowerPicker.squareUpPose(wall, new Pose(14.71, 47.36, Math.PI), 18, 24).x(), EPS);
        assertEquals(26.71, FlowerPicker.squareUpPose(wall, new Pose(40.0, 47.36, Math.PI), 18, 24).x(), EPS);
    }

    /** A spot far off the line, turned away, or past the FLOWER is a drawing error: the card refuses. */
    @Test
    public void squareUpRefusesASpotThatIsNotRoughlyLinedUp() {
        FlowerFieldPoints.Flower wall = FlowerFieldPoints.RED_WALL;
        assertNotNull("13 in off the line", FlowerPicker.squareUpRefusal(wall, new Pose(22.21, 60.36, Math.PI), 12, 30));
        assertNotNull("35° turned", FlowerPicker.squareUpRefusal(wall,
                new Pose(22.21, 47.36, Math.PI - Math.toRadians(35)), 12, 30));
        assertNull("29° the short way round", FlowerPicker.squareUpRefusal(wall,
                new Pose(22.21, 47.36, -Math.PI + Math.toRadians(29)), 12, 30));
        assertNotNull("behind the FLOWER", FlowerPicker.squareUpRefusal(wall, new Pose(1.0, 47.36, Math.PI), 12, 30));
        // The far FLOWER faces +y: its line runs along x = 47.36.
        assertNull(FlowerPicker.squareUpRefusal(FlowerFieldPoints.RED_FAR, new Pose(47.36, 119.0, Math.PI / 2), 12, 30));
    }

    /** An Auto may not cross the centre line (x = 70.75), so it only grabs on its own half. */
    @Test
    public void ownHalfIsTheSameSideOfTheCentreLine() {
        assertEquals(true, FlowerPicker.ownHalf(55.0, 2.71));
        assertEquals("red's robot, blue's GARDEN", false, FlowerPicker.ownHalf(59.87, 132.0));
        assertEquals(true, FlowerPicker.ownHalf(100.0, 138.79));
        assertEquals(false, FlowerPicker.ownHalf(100.0, 47.36));
    }

    /** Letting go means done only once the pieces are being taken, and only on the button. */
    @Test
    public void lettingGoFinishesOnlyFromTheDwellOn() {
        for (GrabRoutine.Phase phase : GrabRoutine.Phase.values()) {
            boolean dwellOrBack = phase == GrabRoutine.Phase.DWELL || phase == GrabRoutine.Phase.BACK;
            assertEquals(phase.toString(), dwellOrBack, GrabRoutine.releaseFinishes(phase, true));
            assertEquals("an Auto's card cut short always stops: " + phase, false,
                    GrabRoutine.releaseFinishes(phase, false));
        }
    }

    /** The push waits for the robot to slow after the line-up; an unknown speed does not hold it. */
    @Test
    public void settledIsSlowerThanTheLimit() {
        assertEquals(true, GrabRoutine.settled(1.5, 2.0));
        assertEquals(false, GrabRoutine.settled(2.0, 2.0));
        assertEquals(false, GrabRoutine.settled(11.5, 2.0));
        assertEquals("no follower speed", true, GrabRoutine.settled(Double.NaN, 2.0));
        assertEquals("a Panels typo never waits forever", false, GrabRoutine.settled(0.5, -1.0));
    }
}
