package org.firstinspires.ftc.teamcode.opmodes.calibration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.hardware.RobotIdentity;
import org.junit.Test;

/** {@link OctoQuadBench} runs wherever an OctoQuad is configured, and refuses nothing else. */
public class OctoQuadBenchTest {

    @Test
    public void theRobotsAndTheRigCarryTheOctoQuad() {
        assertTrue(OctoQuadBench.hasOctoQuad(RobotIdentity.TEAM_19429));
        assertTrue(OctoQuadBench.hasOctoQuad(RobotIdentity.TEAM_20245));
        assertTrue(OctoQuadBench.hasOctoQuad(RobotIdentity.OCTOQUAD_RIG));
        assertFalse(OctoQuadBench.hasOctoQuad(RobotIdentity.LAUNCHER_BENCH));
    }
}
