package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** RobotInternalsLog's drawing against the transfer's numbers (v2, the CAD chat's 12d34bb). */
public class RobotInternalsLogTest {

    private static final double P = FieldSim.POLLEN_RADIUS_IN, N = FieldSim.NECTAR_RADIUS_IN;

    @Test
    public void thePathRunsUnderTheRollerAlongTheLaneToTheHoldAndUpTheAxis() {
        for (double r : new double[] {P, N}) {
            RobotInternalsLog.Path path = new RobotInternalsLog.Path(r, -4, 12);
            assertArrayEquals("on the tiles", new double[] {RobotInternalsLog.ENTRY_X, r}, path.at(0), 1e-9);
            assertArrayEquals("up the axis to the exit's height", new double[] {RobotInternalsLog.HOLD_X, 12}, path.at(path.length), 1e-9);
            assertTrue(path.hold < path.length);
        }
        // Held on the flat lane at the CAD's heights: POLLEN 2.70, NECTAR 3.11 (to the radius's rounding).
        assertArrayEquals(new double[] {-2.845, 2.70}, new RobotInternalsLog.Path(P, -4, 12).holdPoint(), 0.01);
        assertArrayEquals(new double[] {-2.845, 3.11}, new RobotInternalsLog.Path(N, -4, 12).holdPoint(), 0.01);
        assertEquals(RobotInternalsLog.TURRET_X, RobotInternalsLog.HOLD_X, 0.01);
    }

    @Test
    public void theQueueSitsNoseToTailFromTheHoldAsTheCadPlacesIt() {
        // The CAD chat's queue (146f130): POLLEN at X -2.845, -0.045, 2.755, 5.555; NECTAR at -2.845, 0.775, 4.395.
        double[][] expected = {{-2.845, -0.045, 2.755, 5.555}, {-2.845, 0.775, 4.395}};
        double[] radius = {P, N};
        for (int k = 0; k < 2; k++) {
            RobotInternalsLog.Path path = new RobotInternalsLog.Path(radius[k], -4, 12);
            for (int q = 0; q < expected[k].length; q++) {
                // Each further piece one diameter back along the path from the hold. The CAD's NECTAR is 3.62 in across,
                // the simulator's 3.60, so the third NECTAR sits 0.04 in nearer.
                assertEquals("slot " + q, expected[k][q], path.at(path.hold - q * 2 * radius[k])[0], 0.05);
            }
        }
    }

    @Test
    public void componentsAreIdentityAtRestAndTurnAboutTheirAxes() {
        double[] rest = RobotInternalsLog.components(1, 0, 0);
        double[] identity = {0, 0, 0, 1, 0, 0, 0};
        for (int c = 0; c < RobotInternalsLog.COUNT; c++) {
            for (int i = 0; i < 7; i++) assertEquals("component " + c + "[" + i + "]", identity[i], rest[7 * c + i], 1e-9);
        }
        // The turret's axis stays where it is whatever the yaw: p = R p + t.
        double yaw = 1.0, px = RobotInternalsLog.TURRET_X * 0.0254, py = RobotInternalsLog.TURRET_Y * 0.0254;
        double[] turret = RobotInternalsLog.components(1, 0, yaw);
        int t = 7 * RobotInternalsLog.TURRET;
        assertEquals(px, px * Math.cos(yaw) - py * Math.sin(yaw) + turret[t], 1e-9);
        assertEquals(py, px * Math.sin(yaw) + py * Math.cos(yaw) + turret[t + 1], 1e-9);
        // The roller's carriage floats straight up.
        assertEquals(1.3 * 0.0254, RobotInternalsLog.components(1, 1.3, 0)[7 * RobotInternalsLog.ROLLER + 2], 1e-9);
        // The extractor's pose is the CAD's (AutoSim.cadComponents).
        double[] stowed = RobotInternalsLog.components(0, 0, 0);
        for (int i = 0; i < 7; i++) assertEquals(AutoSim.cadComponents(0, 0)[i], stowed[i], 1e-12);
    }

    @Test
    public void theIntakeRollerSpinsAboutItsAxleAndRisesWithTheCarriage() {
        double spin = 1.2, rise = 0.8;
        double[] c = RobotInternalsLog.components(1, rise, 0, spin, 0, 0);
        RobotInternalsLog.Spinner w = RobotInternalsLog.INTAKE_ROLLER_SPIN;
        double[] moved = apply(c, w.component, w.pointM);
        assertArrayEquals(new double[] {w.pointM[0], w.pointM[1], w.pointM[2] + rise * 0.0254}, moved, 1e-9);
    }

    @Test
    public void eachFlywheelAndFeederSpinsAboutItsOwnAxle() {
        double[] c = RobotInternalsLog.components(1, 0, 0, 0, 0.7, 0.9);
        for (RobotInternalsLog.Spinner[] set : new RobotInternalsLog.Spinner[][] {RobotInternalsLog.FLYWHEELS, RobotInternalsLog.FEEDERS}) {
            for (RobotInternalsLog.Spinner w : set) {
                assertArrayEquals("component " + w.component, w.pointM, apply(c, w.component, w.pointM), 1e-9);
                // A point off the axle moves.
                double[] off = {w.pointM[0] + 0.01, w.pointM[1] + 0.01, w.pointM[2] + 0.01};
                double[] moved = apply(c, w.component, off);
                assertTrue(Math.abs(moved[0] - off[0]) + Math.abs(moved[1] - off[1]) + Math.abs(moved[2] - off[2]) > 1e-4);
            }
        }
    }

    /** Component {@code k}'s pose applied to {@code p}: R p + t. */
    private static double[] apply(double[] c, int k, double[] p) {
        int i = 7 * k;
        double w = c[i + 3], x = c[i + 4], y = c[i + 5], z = c[i + 6];
        // q v q* for a unit quaternion.
        double[] u = {x, y, z};
        double[] uv = cross(u, p), uuv = cross(u, uv);
        return new double[] {p[0] + 2 * (w * uv[0] + uuv[0]) + c[i], p[1] + 2 * (w * uv[1] + uuv[1]) + c[i + 1],
                p[2] + 2 * (w * uv[2] + uuv[2]) + c[i + 2]};
    }

    private static double[] cross(double[] a, double[] b) {
        return new double[] {a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]};
    }
}
