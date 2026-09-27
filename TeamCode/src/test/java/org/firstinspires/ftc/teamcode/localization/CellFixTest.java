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
    public void anUnknownStateOrUnmeasuredPointGivesNoFix() {
        assertNull(CellFix.position(HiveCell.RED_SCORING, HiveCellState.UNKNOWN, new Vec3(40, 0, 50), 0));
        // Every point is unmeasured until the CAD numbers go in.
        assertNull(CellFix.position(HiveCell.RED_SCORING, HiveCellState.UP, new Vec3(40, 0, 50), 0));
    }

    @Test
    public void varianceIsFiniteInEveryAxisBecausePedroInvertsIt() {
        Pose v = CellFix.variance(60, 1);

        assertTrue(v.x() > CellFix.variance(60, 3).x());
        assertTrue(CellFix.variance(120, 3).x() > CellFix.variance(30, 3).x());
        assertTrue(Double.isFinite(v.heading()));
    }
}
