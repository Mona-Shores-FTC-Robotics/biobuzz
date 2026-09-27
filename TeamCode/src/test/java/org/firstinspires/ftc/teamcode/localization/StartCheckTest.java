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
}
