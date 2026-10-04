package org.firstinspires.ftc.teamcode.localization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.HiveCellState;
import org.firstinspires.ftc.teamcode.vision.Vec3;
import org.junit.Test;

public class CellFixTest {

    private static final double EPS = 1e-9;

    /** The field point as a robot at (x, y, h) would see it, robot frame (+x forward, +y left). */
    private static Vec3 seenFrom(Vec3 field, double x, double y, double h) {
        double dx = field.x() - x, dy = field.y() - y;
        double c = Math.cos(h), s = Math.sin(h);
        return new Vec3(c * dx + s * dy, -s * dx + c * dy, field.z());
    }

    @Test
    public void facingTheCellHeadOnGivesThePositionBehindIt() {
        Vec3 cell = new Vec3(72, 100, 50);
        Pose fix = CellFix.position(cell, new Vec3(40, 0, 50), 0.0);

        assertEquals(32, fix.x(), EPS);
        assertEquals(100, fix.y(), EPS);
    }

    @Test
    public void theSightingIsRotatedIntoTheFieldByTheHeading() {
        Vec3 cell = new Vec3(72, 100, 50);
        for (double h : new double[]{0, 0.4, Math.PI / 2, -2.5, Math.PI}) {
            Pose fix = CellFix.position(cell, seenFrom(cell, 30, 45, h), h);
            assertEquals("heading " + h, 30, fix.x(), 1e-9);
            assertEquals("heading " + h, 45, fix.y(), 1e-9);
        }
    }

    @Test
    public void headingIsLeftToThePinpoint() {
        Pose fix = CellFix.position(new Vec3(0, 0, 0), new Vec3(1, 0, 0), 0.0);

        assertTrue(Double.isNaN(fix.heading()));
    }

    @Test
    public void anUnknownStateGivesNoFix() {
        assertNull(CellFix.position(HiveCell.RED_SCORING, HiveCellState.UNKNOWN, new Vec3(40, 0, 50), 0));
    }

    @Test
    public void anUnmeasuredPointGivesNoPoint() {
        double nan = Double.NaN;
        double[][][] unmeasured = new double[HiveCell.values().length][2][];
        for (double[][] cell : unmeasured) {
            cell[0] = new double[] {nan, nan, nan};
            cell[1] = new double[] {nan, nan, nan};
        }
        assertNull(HiveFieldPoints.rowCentre(unmeasured, HiveCell.RED_SCORING, HiveCellState.UP));
    }

    @Test
    public void todaysSightingPutsTheRobotWhereItWasTaped() {
        // 19429, 3 Oct 2026 (#156): centre 5 in from the audience wall, facing the HIVE (+y),
        // RED_AUDIENCE UP seen 53.8 in ahead and 0.57 in left.
        Pose fix = CellFix.position(HiveCell.RED_AUDIENCE, HiveCellState.UP,
                new Vec3(53.8, 0.57, 50.2), Math.PI / 2);

        assertEquals(5, fix.y(), 0.5);
        assertEquals(58.6, fix.x(), 0.1);
    }

    @Test
    public void varianceIsFiniteInEveryAxisBecausePedroInvertsIt() {
        Pose v = CellFix.variance(60, 1);

        assertTrue(v.x() > CellFix.variance(60, 3).x());
        assertTrue(CellFix.variance(120, 3).x() > CellFix.variance(30, 3).x());
        assertTrue(Double.isFinite(v.heading()));
    }
}
