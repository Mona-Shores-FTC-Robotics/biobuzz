package org.firstinspires.ftc.teamcode.localization;

import static org.junit.Assert.assertEquals;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

/**
 * The 31 Oct case: L-Simple, drawn for red from (59, 133.69, 270°), INITed with the robot on blue's
 * rotated start (82.5, 7.81, 90°). The camera's whole-pose fits must say blue.
 */
public class StartMatchTest {

    private static final StartPosition RED_DRAWN =
            new StartPosition("RED start (as drawn)", Alliance.RED, new Pose(59.0, 133.69, Math.toRadians(270)));
    private static final StartPosition BLUE_ROTATED =
            new StartPosition("BLUE start (drawing rotated)", Alliance.BLUE, new Pose(82.5, 7.81, Math.toRadians(90)));

    private static Alliance feed(StartMatch m, Pose fit, int frames) {
        Alliance a = Alliance.UNKNOWN;
        for (int i = 0; i < frames; i++) a = m.update(fit, RED_DRAWN, BLUE_ROTATED);
        return a;
    }

    @Test
    public void aRobotOnTheRotatedStartIsBlue() {
        StartMatch m = new StartMatch();
        Pose fit = new Pose(83.4, 8.6, Math.toRadians(92));
        assertEquals("not yet", Alliance.UNKNOWN, feed(m, fit, StartMatch.FRAMES_TO_AGREE - 1));
        assertEquals(Alliance.BLUE, m.update(fit, RED_DRAWN, BLUE_ROTATED));
    }

    @Test
    public void aRobotOnTheDrawnStartIsRed() {
        StartMatch m = new StartMatch();
        assertEquals(Alliance.RED, feed(m, new Pose(58.2, 132.9, Math.toRadians(268)), StartMatch.FRAMES_TO_AGREE));
    }

    @Test
    public void nowhereNearEitherStartIsNoAlliance() {
        StartMatch m = new StartMatch();
        assertEquals(Alliance.UNKNOWN, feed(m, new Pose(70.0, 70.0, 0.0), 20));
    }

    @Test
    public void theRightSpotFacingTheWrongWayIsNoMatch() {
        StartMatch m = new StartMatch();
        assertEquals(Alliance.UNKNOWN, feed(m, new Pose(82.5, 7.81, Math.toRadians(270)), 20));
    }

    @Test
    public void oneStrayFrameDoesNotFlipIt() {
        StartMatch m = new StartMatch();
        Pose blue = new Pose(82.0, 8.0, Math.toRadians(90));
        feed(m, blue, StartMatch.FRAMES_TO_AGREE);
        assertEquals(Alliance.BLUE, m.update(new Pose(30.0, 30.0, 0.0), RED_DRAWN, BLUE_ROTATED));
        assertEquals(Alliance.BLUE, feed(m, blue, 3));
    }

    @Test
    public void movedOffTheStartForLongEnoughClearsIt() {
        StartMatch m = new StartMatch();
        feed(m, new Pose(82.0, 8.0, Math.toRadians(90)), StartMatch.FRAMES_TO_AGREE);
        assertEquals(Alliance.UNKNOWN, feed(m, new Pose(70.0, 40.0, Math.toRadians(90)), StartMatch.FRAMES_TO_AGREE));
    }

    @Test
    public void justOutsideTheMarginIsNoMatch() {
        double margin = LocalizationTuning.startMarginIn;
        assertEquals(true, StartMatch.matches(new Pose(82.5 + margin - 0.1, 7.81, Math.toRadians(90)), BLUE_ROTATED));
        assertEquals(false, StartMatch.matches(new Pose(82.5 + margin + 0.1, 7.81, Math.toRadians(90)), BLUE_ROTATED));
        assertEquals(false, StartMatch.matches(new Pose(82.5, 7.81, Math.toRadians(90 + 25)), BLUE_ROTATED));
    }
}
