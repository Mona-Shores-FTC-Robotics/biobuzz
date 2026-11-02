package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.vision.HiveGeometry;
import org.firstinspires.ftc.teamcode.vision.HiveState;
import org.junit.Test;

/**
 * {@link HiveComponents} poses the HIVE asset's rockers the way the simulator's own rockers turn
 * ({@code FieldSim.Rocker}), so a match log and a simulated log draw a HIVE alike.
 */
public class HiveComponentsTest {

    private static final Alliance[] BOTH = {Alliance.RED, Alliance.BLUE};

    @Test
    public void theMatchStartIsAsBuilt() {
        double[] out = new double[7 * HiveComponents.COUNT];
        for (Alliance a : BOTH) {
            int c = HiveComponents.of(a);
            HiveComponents.pose(c, HiveComponents.fromBuiltRad(a, HiveState.RIGHT_CELL_UP), out);
            double[] mine = new double[7];
            System.arraycopy(out, 7 * c, mine, 0, 7);
            assertArrayEquals(a + " at the match start", new double[] {0, 0, 0, 1, 0, 0, 0}, mine, 1e-12);
        }
        assertEquals(-1, HiveComponents.of(Alliance.UNKNOWN));
    }

    @Test
    public void restingPositionsTurnAsTheSimulatorsRockersDo() {
        for (Alliance a : BOTH) {
            for (HiveState s : new HiveState[] {HiveState.RIGHT_CELL_UP, HiveState.LEFT_CELL_UP}) {
                FieldSim.Rocker r = new FieldSim.Rocker(a, 0);
                assertEquals(a + " " + s, r.angleFor(s) - r.builtAngle(), HiveComponents.fromBuiltRad(a, s), 1e-12);
            }
            double tilt = Math.toRadians(HiveGeometry.CELL_TILT_DEG);
            assertEquals("the other end stop is two tilts away", 2 * tilt,
                    Math.abs(HiveComponents.fromBuiltRad(a, HiveState.LEFT_CELL_UP)), 1e-12);
            assertTrue(Double.isNaN(HiveComponents.fromBuiltRad(a, HiveState.TRANSITION)));
            assertTrue(Double.isNaN(HiveComponents.fromBuiltRad(a, HiveState.UNSEEN)));
        }
    }

    @Test
    public void aRockerTurnsAboutTheAxle() {
        double pivot = HiveGeometry.PIVOT_AXIS_HEIGHT_IN * AdvantageScopeFrame.METERS_PER_INCH;
        double[] out = new double[7];
        for (double deg : new double[] {-60, -30, 25, 60}) {
            HiveComponents.pose(0, Math.toRadians(deg), out);
            double[] onAxle = apply(out, new double[] {0, -0.2, pivot});
            assertArrayEquals("a point on the axle (along y) stays put at " + deg, new double[] {0, -0.2, pivot}, onAxle, 1e-9);
            double[] below = apply(out, new double[] {0, 0, 0});
            assertEquals("a point under the axle swings round it at " + deg,
                    pivot, Math.hypot(below[0], below[2] - pivot), 1e-9);
        }
    }

    /** The pose's rotation then its translation, on a point. */
    private static double[] apply(double[] p, double[] v) {
        double w = p[3], x = p[4], y = p[5], z = p[6];
        double tx = 2 * (y * v[2] - z * v[1]), ty = 2 * (z * v[0] - x * v[2]), tz = 2 * (x * v[1] - y * v[0]);
        return new double[] {
                v[0] + w * tx + (y * tz - z * ty) + p[0],
                v[1] + w * ty + (z * tx - x * tz) + p[1],
                v[2] + w * tz + (x * ty - y * tx) + p[2]};
    }
}
