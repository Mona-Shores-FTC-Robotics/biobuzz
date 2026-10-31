package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RQualsAuto;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

import java.io.File;

/**
 * The simulator still runs master's Autos: one seed of R-Quals on the real robot, the way the
 * Visualizer's Save & Simulate runs a saved Auto ({@link SimRunTest}). It checks that the run starts,
 * finishes and moves the robot, not how well the Auto scores: points are for the simulated log to
 * show, never to fail a merge (mentor decision, 31 Oct 2026). The simulator's other tests are opt-in,
 * so master's CI stays fast.
 */
public class SimulatorSmokeTest {

    @Test
    public void rQualsRunsOnTheRealRobot() throws Exception {
        File log = new File(TeamCodeDir.simLogs(), "smoke-r-quals.wpilog");
        AutoSim.Result r = new AutoSim(RQualsAuto.class, Alliance.RED, 1)
                .design(AutoStudyTest.designs().get(RobotDesign.REAL)).write(log);
        assertTrue(r.toString(), log.length() > 0);
        assertFalse(r.toString(), r.robots.isEmpty());
        assertTrue(r.toString(), r.robots.get(0).leave);
    }
}
