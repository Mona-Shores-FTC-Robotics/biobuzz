package org.firstinspires.ftc.teamcode.localization;

import static org.junit.Assert.assertEquals;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

public class StartCheckTest {

    private static final StartPosition RED = new StartPosition("Red test", Alliance.RED, new Pose(10, 20, 0));

    @Test
    public void anOpModeWithoutADeclaredStartIsNotChecked() {
        StartCheck.Result r = StartCheck.evaluate(null, 10, 0, 0);

        assertEquals(StartCheck.Status.NOT_DECLARED, r.status);
        assertEquals(Alliance.UNKNOWN, r.confirmedAlliance());
    }

    @Test
    public void tooFewFixesConfirmNothing() {
        StartCheck.Result r = StartCheck.evaluate(RED, StartCheck.FIXES_TO_CONFIRM - 1, 10, 20);

        assertEquals(StartCheck.Status.UNCONFIRMED, r.status);
        assertEquals(Alliance.UNKNOWN, r.confirmedAlliance());
    }

    @Test
    public void aConfirmedPlacementWithinTheMarginConfirmsTheAlliance() {
        StartCheck.Result r = StartCheck.evaluate(RED, StartCheck.FIXES_TO_CONFIRM, 11, 21);

        assertEquals(StartCheck.Status.IN_POSITION, r.status);
        assertEquals(Math.sqrt(2), r.errorIn, 1e-9);
        assertEquals(Alliance.RED, r.confirmedAlliance());
    }

    @Test
    public void aMisplacedRobotIsCalledOutAndConfirmsNoAlliance() {
        StartCheck.Result r = StartCheck.evaluate(RED, 20, 14, 20);

        assertEquals(StartCheck.Status.OUT_OF_POSITION, r.status);
        assertEquals(4.0, r.errorIn, 1e-9);
        assertEquals(Alliance.UNKNOWN, r.confirmedAlliance());
    }

    /** The red audience start faces +y (90°): the robot's forward is field +y, its left field -x. */
    @Test
    public void theMoveIsInTheRobotsOwnTermsAtItsStart() {
        StartPosition facingUpField = new StartPosition("Up field", Alliance.RED, new Pose(55, 9.7, Math.PI / 2));
        // The camera puts the robot 2 in further up the field and 1 in toward +x than the start.
        StartCheck.Result r = StartCheck.evaluate(facingUpField, 10, 56, 11.7);

        assertEquals(-2.0, r.moveForwardIn, 1e-9); // back 2
        assertEquals(1.0, r.moveLeftIn, 1e-9);     // +x is the robot's right, so move it left
        assertEquals("move it 2.0 in back, 1.0 in left", r.move());
    }

    @Test
    public void theRedAudienceStartIsDescribedInTiles() {
        assertEquals("tile C1, facing away from the audience", StartPositions.RED_AUDIENCE.where());
    }

    /** R-Quals, R-Simple and Sister-Right are drawn from (55, 9.7, 90°): they get its name and sentence. */
    @Test
    public void anAutoDrawnFromTheRedAudienceStartFindsIt() {
        assertEquals(StartPositions.RED_AUDIENCE, StartPositions.at(new Pose(55.0, 9.7, Math.toRadians(90))));
        assertEquals(null, StartPositions.at(new Pose(59.0, 133.69, Math.toRadians(270))));
        assertEquals(null, StartPositions.at(new Pose(55.0, 9.7, Math.toRadians(270))));
    }

    @Test
    public void aRobotOnTheMarkIsToldToLeaveIt() {
        assertEquals("leave it", StartCheck.evaluate(RED, 10, 10.1, 19.9).move());
    }
}
