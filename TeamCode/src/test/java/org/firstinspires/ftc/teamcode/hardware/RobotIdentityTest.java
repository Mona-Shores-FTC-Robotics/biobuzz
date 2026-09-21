package org.firstinspires.ftc.teamcode.hardware;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class RobotIdentityTest {

    @Test
    public void bundledConfigNamesResolve() {
        assertEquals(RobotIdentity.TEAM_19429, RobotIdentity.fromConfigName("robot_19429"));
        assertEquals(RobotIdentity.TEAM_20245, RobotIdentity.fromConfigName("robot_20245"));
    }

    /** The event workflow: edit the read-only bundled config on the DS, "Save As" a new name. */
    @Test
    public void aDriverStationCopyIsStillTheSameRobot() {
        assertEquals(RobotIdentity.TEAM_19429, RobotIdentity.fromConfigName("robot_19429 port fix"));
        assertEquals(RobotIdentity.TEAM_20245, RobotIdentity.fromConfigName("robot_20245_copy"));
    }

    @Test
    public void unknownConfigsResolveToNothingRatherThanADefault() {
        assertNull(RobotIdentity.fromConfigName(null));
        assertNull(RobotIdentity.fromConfigName(""));
        assertNull(RobotIdentity.fromConfigName("DECODE_Robot_Config19429"));
        assertNull(RobotIdentity.fromConfigName("my config"));
    }

    @Test
    public void controlHubNamesResolveByTeamNumber() {
        assertEquals(RobotIdentity.TEAM_19429, RobotIdentity.fromDeviceName("19429-RC"));
        assertEquals(RobotIdentity.TEAM_20245, RobotIdentity.fromDeviceName("FTC-20245-C"));
    }

    @Test
    public void ambiguousOrUnknownControlHubNamesResolveToNothing() {
        assertNull(RobotIdentity.fromDeviceName(null));
        assertNull(RobotIdentity.fromDeviceName("FTC-RC"));
        assertNull(RobotIdentity.fromDeviceName("19429-20245"));
    }
}
