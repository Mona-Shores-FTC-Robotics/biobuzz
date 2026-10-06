package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** RobotInternalsLog's drawing against the transfer's numbers (doc/transfer.md on spike/164-transfer). */
public class RobotInternalsLogTest {

    private static final double P = FieldSim.POLLEN_RADIUS_IN, N = FieldSim.NECTAR_RADIUS_IN;

    @Test
    public void thePathRunsUnderTheRollerAlongTheLaneRoundTheJAndUpToTheExit() {
        for (double r : new double[] {P, N}) {
            RobotInternalsLog.Path path = new RobotInternalsLog.Path(r, -4, 12);
            double[] start = path.at(0);
            assertEquals(RobotInternalsLog.ENTRY_X, start[0], 1e-9);
            assertEquals("on the tiles", r, start[1], 1e-9);
            // The queue's front piece sits on the lane floor.
            double front = r == P ? FieldSim.LANE_FIRST_POLLEN_X : FieldSim.LANE_FIRST_NECTAR_X;
            double[] q = path.at(path.alongAtX(front));
            assertEquals(front, q[0], 1e-6);
            assertEquals(RobotInternalsLog.LANE_FLOOR_Z + r, q[1], 1e-6);
            double[] end = path.at(path.length);
            assertArrayEquals(new double[] {-4, 12}, end, 1e-9);
        }
        // Up the turret axis: NECTAR's centre at X -3.15 (on the axis), POLLEN's at -3.56 (the transfer's numbers).
        RobotInternalsLog.Path n = new RobotInternalsLog.Path(N, -4, 12), p = new RobotInternalsLog.Path(P, -4, 12);
        assertEquals(-3.16, n.at(n.length - 3)[0], 0.02);
        assertEquals(-3.56, p.at(p.length - 3)[0], 0.02);
    }

    @Test
    public void aNectarLiftsTheJArmAndAPollenBarely() {
        // Round the J, behind the wheel: the arc's midpoint for each piece.
        double a = Math.PI / 4;
        double[] nectar = {RobotInternalsLog.J_AXLE_X - (RobotInternalsLog.OUTER_J_RADIUS - N) * Math.sin(a),
                RobotInternalsLog.J_AXLE_Z - (RobotInternalsLog.OUTER_J_RADIUS - N) * Math.cos(a)};
        double[] pollen = {RobotInternalsLog.J_AXLE_X - (RobotInternalsLog.OUTER_J_RADIUS - P) * Math.sin(a),
                RobotInternalsLog.J_AXLE_Z - (RobotInternalsLog.OUTER_J_RADIUS - P) * Math.cos(a)};
        assertTrue("NECTAR lifts the arm", Math.toDegrees(RobotInternalsLog.jLift(nectar, N)) > 15);
        assertTrue("POLLEN is gripped", Math.toDegrees(RobotInternalsLog.jLift(pollen, P)) < 2);
        // Queued at the stop, neither lifts it: the queue rests against the stopped wheel.
        assertEquals(0, RobotInternalsLog.jLift(new double[] {FieldSim.LANE_FIRST_NECTAR_X, 0.9 + N}, N), 1e-9);
        assertEquals(0, RobotInternalsLog.jLift(new double[] {FieldSim.LANE_FIRST_POLLEN_X, 0.9 + P}, P), 1e-9);
    }

    @Test
    public void componentsAreIdentityAtRestAndTurnAboutTheirAxes() {
        double[] rest = RobotInternalsLog.components(1, 0, 0, 0);
        double[] identity = {0, 0, 0, 1, 0, 0, 0};
        for (int c = 0; c < RobotInternalsLog.COUNT; c++) {
            for (int i = 0; i < 7; i++) assertEquals("component " + c + "[" + i + "]", identity[i], rest[7 * c + i], 1e-9);
        }
        // The turret's axis stays where it is whatever the yaw: p = R p + t.
        double yaw = 1.0, px = RobotInternalsLog.TURRET_X * 0.0254;
        double[] turret = RobotInternalsLog.components(1, 0, yaw, 0);
        int t = 7 * RobotInternalsLog.TURRET;
        assertEquals(px, px * Math.cos(yaw) + turret[t], 1e-9);
        assertEquals(0, px * Math.sin(yaw) + turret[t + 1], 1e-9);
        // The roller floats straight up.
        assertEquals(1.3 * 0.0254, RobotInternalsLog.components(1, 1.3, 0, 0)[7 * RobotInternalsLog.ROLLER + 2], 1e-9);
        // The extractor's pose is the CAD's (AutoSim.cadComponents).
        double[] stowed = RobotInternalsLog.components(0, 0, 0, 0);
        for (int i = 0; i < 7; i++) assertEquals(AutoSim.cadComponents(0)[i], stowed[i], 1e-12);
    }
}
