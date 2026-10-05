package org.firstinspires.ftc.teamcode.localization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.vision.CameraMount;
import org.firstinspires.ftc.teamcode.vision.CellSighting;
import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.HiveCellState;
import org.firstinspires.ftc.teamcode.vision.Vec3;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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

    @Test
    public void aRowOfTagsGivesTheWholePoseHeadingIncluded() {
        Vec3 row = new Vec3(72, 100, 50);
        for (double alongX : new double[]{+1, -1}) {
            for (double h : new double[]{0, 0.4, Math.PI / 2, -2.5, Math.PI}) {
                // Tag 0 and tag 3 of the row, as a robot at (30, 45, h) would see them.
                Vec3 first = seenFrom(new Vec3(row.x() + alongX * MEMBER_X[0], row.y(), row.z()), 30, 45, h);
                Vec3 last = seenFrom(new Vec3(row.x() + alongX * MEMBER_X[3], row.y(), row.z()), 30, 45, h);
                Vec3 lateral = last.minus(first).times(1.0 / (MEMBER_X[3] - MEMBER_X[0]));
                Pose pose = CellFix.pose(row, alongX, seenFrom(row, 30, 45, h), lateral);

                String what = "along " + alongX + ", heading " + h;
                assertEquals(what, 30, pose.x(), 1e-9);
                assertEquals(what, 45, pose.y(), 1e-9);
                assertEquals(what, 0, Math.IEEEremainder(pose.heading() - h, 2 * Math.PI), 1e-9);
            }
        }
    }

    @Test
    public void noRowDirectionNoPose() {
        assertNull(CellFix.pose(HiveCell.RED_AUDIENCE, HiveCellState.UP, new Vec3(50, 0, 50), null));
        // Not yet seen on the robot which way this row runs.
        assertNull(CellFix.pose(HiveCell.BLUE_SCORING, HiveCellState.UP, new Vec3(50, 0, 50),
                new Vec3(0, -1, 0)));
    }

    @Test
    public void todaysTagsSeedTheRobotWhereItWasTaped() {
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
            List<CellSighting.Member> members = new ArrayList<>();
            for (double[] tag : seen) {
                members.add(new CellSighting.Member((int) tag[0],
                        CameraMount.toRobotFrame(new Vec3(tag[1], tag[2], tag[3])), 0.003));
            }
            CellSighting sighting = CellSighting.fromMembers(HiveCell.RED_AUDIENCE, members, 0L);
            Pose pose = CellFix.pose(HiveCell.RED_AUDIENCE, HiveCellState.UP,
                    sighting.rowCentreRobot(), sighting.lateralAxisRobot());

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
