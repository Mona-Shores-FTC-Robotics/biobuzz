package org.firstinspires.ftc.teamcode.localization;

import static org.junit.Assert.assertEquals;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

/**
 * The 31 Oct case: L-Simple, drawn for red from (59, 133.69, 270°), INITed with the robot on blue's
 * rotated start (82.5, 7.81, 90°). The camera must say blue, from the one HIVE a start sees.
 *
 * <p>Each frame gives two position-only solves, one per start's heading. A robot on blue's start
 * solved with blue's heading lands on it; solved with red's heading (half a turn out) it lands about
 * two camera ranges away, mirrored through the tags, which here is modelled as a point far off.
 */
public class StartMatchTest {

    private static final StartPosition RED_DRAWN =
            new StartPosition("RED start (as drawn)", Alliance.RED, new Pose(59.0, 133.69, Math.toRadians(270)));
    private static final StartPosition BLUE_ROTATED =
            new StartPosition("BLUE start (drawing rotated)", Alliance.BLUE, new Pose(82.5, 7.81, Math.toRadians(90)));

    /** The frame of a robot on blue's start: on it with blue's heading, far off with red's. */
    private static final Pose ON_BLUE_AS_BLUE = new Pose(83.4, 8.6, Math.toRadians(90));
    private static final Pose ON_BLUE_AS_RED = new Pose(30.0, 120.0, Math.toRadians(270));

    private static Alliance feed(StartMatch m, Pose atDrawn, Pose atRotated, int frames) {
        Alliance a = Alliance.UNKNOWN;
        for (int i = 0; i < frames; i++) a = m.update(atDrawn, atRotated, RED_DRAWN, BLUE_ROTATED);
        return a;
    }

    @Test
    public void aRobotOnTheRotatedStartIsBlue() {
        StartMatch m = new StartMatch();
        assertEquals("not yet", Alliance.UNKNOWN, feed(m, ON_BLUE_AS_RED, ON_BLUE_AS_BLUE, StartMatch.FRAMES_TO_AGREE - 1));
        assertEquals(Alliance.BLUE, m.update(ON_BLUE_AS_RED, ON_BLUE_AS_BLUE, RED_DRAWN, BLUE_ROTATED));
    }

    @Test
    public void aRobotOnTheDrawnStartIsRed() {
        StartMatch m = new StartMatch();
        Pose asRed = new Pose(58.2, 132.9, Math.toRadians(270));
        Pose asBlue = new Pose(110.0, 20.0, Math.toRadians(90));
        assertEquals(Alliance.RED, feed(m, asRed, asBlue, StartMatch.FRAMES_TO_AGREE));
    }

    @Test
    public void aSolveThatFailedWithOneHeadingStillMatchesTheOther() {
        // With the wrong heading the solve often lands off the field, and the gate returns no fix.
        StartMatch m = new StartMatch();
        assertEquals(Alliance.BLUE, feed(m, null, ON_BLUE_AS_BLUE, StartMatch.FRAMES_TO_AGREE));
    }

    @Test
    public void nowhereNearEitherStartIsNoAlliance() {
        StartMatch m = new StartMatch();
        assertEquals(Alliance.UNKNOWN, feed(m, new Pose(70.0, 70.0, Math.toRadians(270)),
                new Pose(72.0, 71.0, Math.toRadians(90)), 20));
        assertEquals(Alliance.UNKNOWN, feed(m, null, null, 20));
    }

    @Test
    public void oneStrayFrameDoesNotFlipIt() {
        StartMatch m = new StartMatch();
        feed(m, ON_BLUE_AS_RED, ON_BLUE_AS_BLUE, StartMatch.FRAMES_TO_AGREE);
        assertEquals(Alliance.BLUE, m.update(null, null, RED_DRAWN, BLUE_ROTATED));
        assertEquals(Alliance.BLUE, feed(m, ON_BLUE_AS_RED, ON_BLUE_AS_BLUE, 3));
    }

    @Test
    public void movedOffTheStartForLongEnoughClearsIt() {
        StartMatch m = new StartMatch();
        feed(m, ON_BLUE_AS_RED, ON_BLUE_AS_BLUE, StartMatch.FRAMES_TO_AGREE);
        assertEquals(Alliance.UNKNOWN, feed(m, new Pose(60.0, 60.0, Math.toRadians(270)),
                new Pose(70.0, 40.0, Math.toRadians(90)), StartMatch.FRAMES_TO_AGREE));
    }

    @Test
    public void aFixSeveralInchesOffStillPicksTheStart() {
        // Placement scatter and a 2-tag position: several inches off. Still blue.
        StartMatch m = new StartMatch();
        assertEquals(Alliance.BLUE, feed(m, ON_BLUE_AS_RED, new Pose(89.0, 12.0, Math.toRadians(90)), StartMatch.FRAMES_TO_AGREE));
    }

    @Test
    public void justOutsideTheRadiusIsNoMatch() {
        double r = StartMatch.MATCH_RADIUS_IN;
        assertEquals(true, StartMatch.matches(new Pose(82.5 + r - 0.1, 7.81, Math.toRadians(90)), BLUE_ROTATED));
        assertEquals(false, StartMatch.matches(new Pose(82.5 + r + 0.1, 7.81, Math.toRadians(90)), BLUE_ROTATED));
        assertEquals(false, StartMatch.matches(null, BLUE_ROTATED));
    }
}
