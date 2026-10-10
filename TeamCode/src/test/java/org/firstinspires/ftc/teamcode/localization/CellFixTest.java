package org.firstinspires.ftc.teamcode.localization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.vision.CameraMount;
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

    private static final double[] MEMBER_X = {-6.5, -2.75, 2.75, 6.5};

    /** Tags at {@code field} as a robot at (x, y, h) sees them, fitted back. */
    private static Pose fitFrom(Vec3[] field, double x, double y, double h) {
        int n = field.length;
        double[] fx = new double[n], fy = new double[n], rx = new double[n], ry = new double[n];
        for (int i = 0; i < n; i++) {
            Vec3 seen = seenFrom(field[i], x, y, h);
            fx[i] = field[i].x();
            fy[i] = field[i].y();
            rx[i] = seen.x();
            ry[i] = seen.y();
        }
        return CellFix.fit(fx, fy, rx, ry, n, 1.5);
    }

    @Test
    public void anyTwoOrMoreTagsGiveTheWholePose() {
        Vec3[][] layouts = {
                // A whole row, two neighbours, and one tag on each of two CELLs.
                {new Vec3(65.5, 100, 50), new Vec3(69.25, 100, 50), new Vec3(74.75, 100, 50), new Vec3(78.5, 100, 50)},
                {new Vec3(69.25, 100, 50), new Vec3(74.75, 100, 50)},
                {new Vec3(58, 58.4, 50), new Vec3(83.5, 84.7, 35)}};
        for (Vec3[] tags : layouts) {
            for (double h : new double[]{0, 0.4, Math.PI / 2, -2.5, Math.PI}) {
                Pose pose = fitFrom(tags, 30, 45, h);
                String what = tags.length + " tags, heading " + h;
                assertEquals(what, 30, pose.x(), 1e-9);
                assertEquals(what, 45, pose.y(), 1e-9);
                assertEquals(what, 0, Math.IEEEremainder(pose.heading() - h, 2 * Math.PI), 1e-9);
            }
        }
    }

    @Test
    public void oneTagOrTagsOnTopOfEachOtherGiveNoHeading() {
        assertNull(fitFrom(new Vec3[]{new Vec3(70, 100, 50)}, 30, 45, 0));
        assertNull(fitFrom(new Vec3[]{new Vec3(70, 100, 50), new Vec3(70.5, 100, 50)}, 30, 45, 0));
    }

    @Test
    public void oneTagWithTheHeadingGivesThePositionExactly() {
        Vec3 tag = new Vec3(78.5, 100, 50);
        Vec3 seen = seenFrom(tag, 30, 45, 0.7);
        Pose pose = CellFix.position(new double[]{tag.x()}, new double[]{tag.y()},
                new double[]{seen.x()}, new double[]{seen.y()}, 1, 0.7);

        assertEquals(30, pose.x(), 1e-9);
        assertEquals(45, pose.y(), 1e-9);
        assertTrue(Double.isNaN(pose.heading()));
    }

    @Test
    public void eachTagKnowsWhereItSitsAlongItsRow() {
        // RED_AUDIENCE runs along +x (measured); its tag 37 is cluster +6.5 in.
        assertEquals(58.0 + 6.5, HiveFieldPoints.tagPosition(37, HiveCellState.UP).x(), 1e-9);
        assertEquals(58.0 - 6.5, HiveFieldPoints.tagPosition(34, HiveCellState.UP).x(), 1e-9);
        // SCORING CELLs face the other way, so their tag 3 is at -6.5 along field x.
        assertEquals(58.0 - 6.5, HiveFieldPoints.tagPosition(33, HiveCellState.UP).x(), 1e-9);
        assertNull(HiveFieldPoints.tagPosition(37, HiveCellState.UNKNOWN));
        assertNull(HiveFieldPoints.tagPosition(7, HiveCellState.UP));
    }

    @Test
    public void todaysTagsPutTheRobotWhereItWasTaped() {
        // 19429, 3 Oct 2026 (#156), Vision: Raw Tag Dump at the red audience start: robot centre
        // 5 in from the audience wall, square to it, facing the HIVE (+y). Camera inches.
        double[][] seen = {
                {34, -7.12, 9.60, 60.69}, {35, -3.37, 9.59, 60.87},
                {36, 2.16, 9.71, 61.70}, {37, 5.94, 9.62, 61.24}};
        double[] mount = {CameraMount.mountForwardIn, CameraMount.mountLeftIn, CameraMount.mountUpIn,
                CameraMount.pitchDeg, CameraMount.yawDeg};
        try {
            CameraMount.mountForwardIn = 4;
            CameraMount.mountLeftIn = 0;
            CameraMount.mountUpIn = 14;
            CameraMount.pitchDeg = 45;
            CameraMount.yawDeg = 0;
            double[] fx = new double[4], fy = new double[4], rx = new double[4], ry = new double[4];
            for (int i = 0; i < 4; i++) {
                Vec3 field = HiveFieldPoints.tagPosition((int) seen[i][0], HiveCellState.UP);
                Vec3 robot = CameraMount.toRobotFrame(new Vec3(seen[i][1], seen[i][2], seen[i][3]));
                fx[i] = field.x();
                fy[i] = field.y();
                rx[i] = robot.x();
                ry[i] = robot.y();
            }
            Pose pose = CellFix.fit(fx, fy, rx, ry, 4, 1.5);

            assertEquals(90, Math.toDegrees(pose.heading()), 3);
            assertEquals(5, pose.y(), 1.5);
        } finally {
            CameraMount.mountForwardIn = mount[0];
            CameraMount.mountLeftIn = mount[1];
            CameraMount.mountUpIn = mount[2];
            CameraMount.pitchDeg = mount[3];
            CameraMount.yawDeg = mount[4];
        }
    }

    @Test
    public void varianceIsFiniteInEveryAxisBecausePedroInvertsIt() {
        Pose v = CellFix.variance(60, 1);

        assertTrue(v.x() > CellFix.variance(60, 3).x());
        assertTrue(CellFix.variance(120, 3).x() > CellFix.variance(30, 3).x());
        assertTrue(Double.isFinite(v.heading()));
    }
}
