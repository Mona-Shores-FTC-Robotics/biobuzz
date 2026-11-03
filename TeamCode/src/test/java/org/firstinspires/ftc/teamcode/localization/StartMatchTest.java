package org.firstinspires.ftc.teamcode.localization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

/**
 * The 31 Oct case: L-Simple, drawn for red from (62.3, 133.8, 270°), INITed with the robot on blue's
 * rotated start (79.2, 7.7, 90°). The camera must say blue, from the one HIVE a start sees. And the
 * 2 Nov ask: a robot on the red audience start with L-Simple picked must be named as on that start,
 * not left as "no alliance".
 *
 * <p>Each frame gives one position-only solve per candidate, with that candidate's heading. A robot
 * on a start solved with that start's heading lands on it; solved with a heading half a turn out it
 * lands about two camera ranges away, mirrored through the tags, modelled here as a point far off.
 */
public class StartMatchTest {

    private static final StartPosition RED_DRAWN = StartPositions.RED_FAR;
    private static final StartPosition BLUE_ROTATED = StartPositions.BLUE_FROM_RED_FAR;
    /** L-Simple's candidates: its two starts, then the other known ones (BuiltAuto's order). */
    private static final StartPosition[] L_SIMPLE = {RED_DRAWN, BLUE_ROTATED,
            StartPositions.RED_AUDIENCE, StartPositions.BLUE_FROM_RED_AUDIENCE};

    private static final Pose FAR_OFF = new Pose(-40.0, 200.0, 0.0);

    /** A frame of a robot on {@code candidates[on]}, a little off it: on with its heading, far off with the others. */
    private static Pose[] frame(StartPosition[] candidates, int on, double dx, double dy) {
        Pose[] fixes = new Pose[candidates.length];
        for (int i = 0; i < candidates.length; i++) {
            Pose c = candidates[i].pose;
            fixes[i] = i == on ? new Pose(c.x() + dx, c.y() + dy, c.heading()) : FAR_OFF;
        }
        return fixes;
    }

    private static int feed(StartMatch m, Pose[] fixes, StartPosition[] candidates, int frames) {
        int a = StartMatch.NONE;
        for (int i = 0; i < frames; i++) a = m.update(fixes, candidates);
        return a;
    }

    @Test
    public void aRobotOnTheRotatedStartIsBlue() {
        StartMatch m = new StartMatch();
        Pose[] onBlue = frame(L_SIMPLE, 1, 0.9, 0.9);
        assertEquals("not yet", StartMatch.NONE, feed(m, onBlue, L_SIMPLE, StartMatch.FRAMES_TO_AGREE - 1));
        assertEquals(1, m.update(onBlue, L_SIMPLE));
        assertEquals(Alliance.BLUE, L_SIMPLE[m.settled()].alliance);
    }

    @Test
    public void aRobotOnTheDrawnStartIsRed() {
        StartMatch m = new StartMatch();
        assertEquals(0, feed(m, frame(L_SIMPLE, 0, -0.8, -0.9), L_SIMPLE, StartMatch.FRAMES_TO_AGREE));
    }

    @Test
    public void theWrongAutosStartIsNamedNotGuessed() {
        // L-Simple picked, robot on the red audience start (R-Simple's): candidate 2, not an alliance.
        StartMatch m = new StartMatch();
        int on = feed(m, frame(L_SIMPLE, 2, 0.5, 0.0), L_SIMPLE, StartMatch.FRAMES_TO_AGREE);
        assertEquals(2, on);
        assertEquals(StartPositions.RED_AUDIENCE, L_SIMPLE[on]);
        assertTrue("not one of the Auto's own two", on >= 2);
    }

    @Test
    public void theTwoSameHeadingStartsCannotBothMatch() {
        // Red audience (55.6, 7.7) and blue's audience wall (79.2, 7.7) face the same way, 23.6 in
        // apart: a robot on one, solved with the other's heading, lands 23.6 in from the other.
        double apart = Math.hypot(StartPositions.RED_AUDIENCE.pose.x() - StartPositions.BLUE_FROM_RED_FAR.pose.x(),
                StartPositions.RED_AUDIENCE.pose.y() - StartPositions.BLUE_FROM_RED_FAR.pose.y());
        assertTrue(apart > 2 * StartMatch.MATCH_RADIUS_IN);
        StartMatch m = new StartMatch();
        Pose onRedAudience = new Pose(55.6, 7.7, Math.toRadians(90));
        Pose[] fixes = {FAR_OFF, onRedAudience, onRedAudience, FAR_OFF};
        assertEquals(2, feed(m, fixes, L_SIMPLE, StartMatch.FRAMES_TO_AGREE));
    }

    @Test
    public void aSolveThatFailedIsNoMatch() {
        // With the wrong heading the solve often lands off the field, and the gate returns no fix.
        StartMatch m = new StartMatch();
        Pose[] fixes = frame(L_SIMPLE, 1, 0, 0);
        fixes[0] = null;
        fixes[2] = null;
        fixes[3] = null;
        assertEquals(1, feed(m, fixes, L_SIMPLE, StartMatch.FRAMES_TO_AGREE));
        assertEquals(StartMatch.NONE, feed(new StartMatch(), new Pose[4], L_SIMPLE, 20));
    }

    @Test
    public void nowhereNearAStartIsNone() {
        StartMatch m = new StartMatch();
        Pose[] fixes = {FAR_OFF, FAR_OFF, FAR_OFF, FAR_OFF};
        assertEquals(StartMatch.NONE, feed(m, fixes, L_SIMPLE, 20));
    }

    @Test
    public void oneStrayFrameDoesNotFlipIt() {
        StartMatch m = new StartMatch();
        Pose[] onBlue = frame(L_SIMPLE, 1, 0.5, -0.5);
        feed(m, onBlue, L_SIMPLE, StartMatch.FRAMES_TO_AGREE);
        assertEquals(1, m.update(new Pose[4], L_SIMPLE));
        assertEquals(1, feed(m, onBlue, L_SIMPLE, 3));
    }

    @Test
    public void movedOffTheStartForLongEnoughClearsIt() {
        StartMatch m = new StartMatch();
        feed(m, frame(L_SIMPLE, 1, 0, 0), L_SIMPLE, StartMatch.FRAMES_TO_AGREE);
        Pose[] offAll = {FAR_OFF, new Pose(70.0, 40.0, Math.toRadians(90)), FAR_OFF, FAR_OFF};
        assertEquals(StartMatch.NONE, feed(m, offAll, L_SIMPLE, StartMatch.FRAMES_TO_AGREE));
    }

    @Test
    public void aFixSeveralInchesOffStillPicksTheStart() {
        // Placement scatter and a two-tag position: a few inches off. Still blue.
        StartMatch m = new StartMatch();
        assertEquals(1, feed(m, frame(L_SIMPLE, 1, 4.0, 3.0), L_SIMPLE, StartMatch.FRAMES_TO_AGREE));
    }

    @Test
    public void justOutsideTheRadiusIsNoMatch() {
        double r = StartMatch.MATCH_RADIUS_IN;
        Pose b = BLUE_ROTATED.pose;
        assertEquals(true, StartMatch.matches(new Pose(b.x() + r - 0.1, b.y(), b.heading()), BLUE_ROTATED));
        assertEquals(false, StartMatch.matches(new Pose(b.x() + r + 0.1, b.y(), b.heading()), BLUE_ROTATED));
        assertEquals(false, StartMatch.matches(null, BLUE_ROTATED));
    }

    @Test
    public void theKnownStartsAreTheTwoMeasuredAndTheirHalfTurns() {
        StartPosition[] known = StartPositions.known();
        assertEquals(4, known.length);
        assertEquals(79.2, StartPositions.BLUE_FROM_RED_FAR.pose.x(), 0.05);
        assertEquals(7.7, StartPositions.BLUE_FROM_RED_FAR.pose.y(), 0.05);
        assertEquals(90, Math.toDegrees(StartPositions.BLUE_FROM_RED_FAR.pose.heading()), 1e-9);
        assertEquals(Alliance.BLUE, StartPositions.BLUE_FROM_RED_FAR.alliance);
    }
}
