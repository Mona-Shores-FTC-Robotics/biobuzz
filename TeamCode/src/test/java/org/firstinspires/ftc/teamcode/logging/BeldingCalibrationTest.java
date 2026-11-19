package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Random;
import org.firstinspires.ftc.teamcode.RobotDifferences;
import org.firstinspires.ftc.teamcode.hardware.RobotIdentity;
import org.junit.Test;

/** Per-robot loads in a pair: ours and the partner's are chosen apart, and each falls back to the run's. */
public class BeldingCalibrationTest {

    @Test
    public void oursAndPartnerAreChosenApart() {
        assertEquals("20245", BeldingCalibration.robotFor(0, "20245", "19429", "both"));
        assertEquals("19429", BeldingCalibration.robotFor(1, "20245", "19429", "both"));
        assertEquals("19429", BeldingCalibration.robotFor(2, "20245", "19429", "both"));
    }

    @Test
    public void unsetFallsBackToTheRunsRobot() {
        assertEquals("full", BeldingCalibration.robotFor(0, null, "19429", "full"));
        assertEquals("both", BeldingCalibration.robotFor(1, "20245", " ", "both"));
    }

    @Test
    public void eachRobotDrawsFromItsOwnLoads() {
        Random luck = new Random(1);
        for (int i = 0; i < 200; i++) {
            assertEquals(4, BeldingCalibration.load("20245", false, luck));
            assertEquals(4, BeldingCalibration.load("full", true, luck));
        }
        boolean shortSeen = false;
        for (int i = 0; i < 200; i++) shortSeen |= BeldingCalibration.load("19429", false, luck) < 4;
        assertTrue("19429's WALL FLOWER loads come up short", shortSeen);
    }

    /** The simulator's roller stick follows master's robot differences: 19429 has it, 20245 does not. */
    @Test
    public void rollerStickIsTheRobotDifferencesOne() {
        for (RobotIdentity robot : new RobotIdentity[] {RobotIdentity.TEAM_19429, RobotIdentity.TEAM_20245}) {
            boolean listed = false;
            for (String why : RobotDifferences.of(robot)) listed |= why.contains("FLOWER stick with a roller");
            String team = robot == RobotIdentity.TEAM_19429 ? "19429" : "20245";
            assertEquals(team + ": the roller stick, in RobotDifferences and in the simulator", listed,
                    BeldingCalibration.rollerStick(team));
        }
        assertTrue(BeldingCalibration.rollerStick("19429"));
        assertFalse(BeldingCalibration.rollerStick("20245"));
        assertFalse(BeldingCalibration.rollerStick("both"));
    }
}
