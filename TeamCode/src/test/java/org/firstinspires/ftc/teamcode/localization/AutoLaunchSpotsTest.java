package org.firstinspires.ftc.teamcode.localization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.localization.AutoLaunchSpots.Spot;
import org.firstinspires.ftc.teamcode.subsystems.TurretSubsystem;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.FieldFrame;
import org.firstinspires.ftc.teamcode.vision.Vec3;
import org.junit.Test;

/**
 * The Autos' launch spots ({@link AutoLaunchSpots}, the team spots of the same names, which
 * {@code TeamSpotsTest} checks) are where they are for the reasons given: aimed, two robots to an
 * end, short of the centre line. A spot moved in the Visualizer is checked against these too.
 */
public class AutoLaunchSpotsTest {

    private static final double HALF = LaunchSpots.FRAME_HALF_IN;
    private static final double SIZE = FieldFrame.FIELD_SIZE_INCHES;

    /**
     * Every Auto launch spot, the five placed by hand and any made in the Visualizer since (a team
     * spot named …_AUTO_…): aimed at its CELL, which the Visualizer keeps as the spot is moved,
     * inside the field, and short of the centre line.
     */
    @Test
    public void everyLaunchSpotIsAimedOnTheFieldAndOurSide() {
        assertTrue("the five placed by hand are launch spots", AutoLaunchSpots.all().size() >= Spot.values().length);
        for (TeamSpot spot : AutoLaunchSpots.all()) {
            assertTrue(spot + " has a heading", spot.hasHeading());
            Pose p = spot.pose();
            Vec3 row = AutoLaunchSpots.cellRowFor(spot.y, Alliance.RED);
            assertNotNull(row);
            double aimed = LaunchSpots.aimTurret(p.x(), p.y(), row.x(), row.y(),
                    TurretSubsystem.turretForwardIn, TurretSubsystem.turretLeftIn);
            assertEquals(spot + " faces " + spot.headingDeg + " deg; the turret aims from there at "
                            + String.format(java.util.Locale.ROOT, "%.1f", (Math.toDegrees(aimed) % 360 + 360) % 360)
                            + " deg: move it in the Visualizer, which aims it", 0,
                    Math.toDegrees(Math.IEEEremainder(aimed - p.heading(), 2 * Math.PI)), 0.3);
            for (double[] c : corners(p)) {
                assertTrue(spot + " reaches x " + c[0] + ", past the centre line less a quarter inch",
                        c[0] <= FieldFrame.FIELD_CENTRE_INCHES - 0.25);
                assertTrue(spot + " leaves the field at (" + c[0] + ", " + c[1] + ")",
                        c[0] >= 0 && c[1] >= 0 && c[1] <= SIZE);
            }
        }
    }

    @Test
    public void eachPointsTheTurretAtItsCellsRaisedRow() {
        for (Spot spot : Spot.values()) {
            Vec3 row = spot.cellRow(Alliance.RED);
            assertNotNull(row);
            double aimed = LaunchSpots.aimTurret(spot.red.x(), spot.red.y(), row.x(), row.y(),
                    TurretSubsystem.turretForwardIn, TurretSubsystem.turretLeftIn);
            assertEquals(spot + " is drawn " + Math.toDegrees(spot.red.heading()) + " deg; the turret aims from there at "
                            + Math.toDegrees(aimed) + " deg", 0,
                    Math.toDegrees(Math.IEEEremainder(aimed - spot.red.heading(), 2 * Math.PI)), 0.3);
        }
    }

    @Test
    public void hiveIsFortyInchesOutAndOffsetFurtherBack() {
        for (Spot spot : Spot.values()) {
            double range = rangeIn(spot);
            if (spot == Spot.RIGHT_AUTO_FIX_TIP) assertEquals(spot + " range", 35.3, range, 0.5);
            else if (spot.hive) assertEquals(spot + " range", 40.0, range, 0.5);
            else assertEquals(spot + " range", spot.left ? 44.0 : 52.0, range, 0.5);
        }
    }

    @Test
    public void twoRobotsHoldAnEndTogetherAndOffsetClearsTheStart() {
        for (boolean left : new boolean[] {true, false}) {
            Spot hive = left ? Spot.LEFT_AUTO_HIVE : Spot.RIGHT_AUTO_HIVE;
            Spot offset = left ? Spot.LEFT_AUTO_OFFSET : Spot.RIGHT_AUTO_OFFSET;
            Pose start = (left ? StartPositions.RED_FAR : StartPositions.RED_AUDIENCE).pose;
            assertTrue(hive + " and " + offset + " are " + gap(hive.red, offset.red) + " in apart",
                    gap(hive.red, offset.red) >= 1.5);
            assertTrue(offset + " is " + gap(offset.red, start) + " in from a robot on its end's start",
                    gap(offset.red, start) >= 2.0);
            // HIVE is for a robot that knows the start behind it is empty: it stands in front of it.
            assertTrue(hive + " overlaps a robot on its end's start", gap(hive.red, start) < 0);
        }
    }

    @Test
    public void theFixTipSpotStandsEighteenInchesOffTheWallSquareToIt() {
        Pose p = Spot.RIGHT_AUTO_FIX_TIP.red, start = StartPositions.RED_AUDIENCE.pose;
        assertEquals("square to the wall", 90.0, Math.toDegrees(p.heading()), 1e-9);
        // RIGHT_START is placed with the frame's back on the wall, so its y is the frame's back.
        assertEquals("back edge to the audience wall", 18.0, p.y() - start.y(), 0.05);
        assertTrue("clear of a robot on RIGHT_START: " + gap(p, start), gap(p, start) >= 1.5);
    }

    @Test
    public void noneCrossesTheCentreLineOrAWall() {
        for (Spot spot : Spot.values()) {
            for (double[] c : corners(spot.red)) {
                assertTrue(spot + " reaches x " + c[0] + ", past the centre line less a quarter inch",
                        c[0] <= FieldFrame.FIELD_CENTRE_INCHES - 0.25);
                assertTrue(spot + " leaves the field at (" + c[0] + ", " + c[1] + ")",
                        c[0] >= 0 && c[1] >= 0 && c[1] <= SIZE);
            }
        }
    }

    @Test
    public void blueIsRedTurnedHalfATurn() {
        Pose red = Spot.LEFT_AUTO_HIVE.red, blue = Spot.LEFT_AUTO_HIVE.pose(Alliance.BLUE);
        assertEquals(SIZE - red.x(), blue.x(), 1e-9);
        assertEquals(SIZE - red.y(), blue.y(), 1e-9);
        assertEquals(Math.PI, Math.abs(Math.IEEEremainder(blue.heading() - red.heading(), 2 * Math.PI)), 1e-9);
    }

    /** From the turret's axis to the raised row, along the floor. */
    private static double rangeIn(Spot spot) {
        Pose p = spot.red;
        Vec3 row = spot.cellRow(Alliance.RED);
        double f = TurretSubsystem.turretForwardIn, l = TurretSubsystem.turretLeftIn;
        double ax = p.x() + f * Math.cos(p.heading()) - l * Math.sin(p.heading());
        double ay = p.y() + f * Math.sin(p.heading()) + l * Math.cos(p.heading());
        return Math.hypot(row.x() - ax, row.y() - ay);
    }

    /** The frame's corners at {@code p}: a square {@link LaunchSpots#FRAME_HALF_IN} to each side. */
    private static double[][] corners(Pose p) {
        double c = Math.cos(p.heading()), s = Math.sin(p.heading());
        double[][] local = {{HALF, HALF}, {HALF, -HALF}, {-HALF, -HALF}, {-HALF, HALF}};
        double[][] out = new double[4][2];
        for (int i = 0; i < 4; i++) {
            out[i][0] = p.x() + local[i][0] * c - local[i][1] * s;
            out[i][1] = p.y() + local[i][0] * s + local[i][1] * c;
        }
        return out;
    }

    /** Between two frames, inches: how far apart along the best separating edge; negative, how far they overlap. */
    private static double gap(Pose a, Pose b) {
        double[][] A = corners(a), B = corners(b);
        double best = Double.NEGATIVE_INFINITY;
        for (double[][] poly : new double[][][] {A, B}) {
            for (int i = 0; i < 4; i++) {
                double nx = poly[(i + 1) % 4][1] - poly[i][1], ny = poly[i][0] - poly[(i + 1) % 4][0];
                double n = Math.hypot(nx, ny);
                nx /= n;
                ny /= n;
                double aLo = Double.POSITIVE_INFINITY, aHi = Double.NEGATIVE_INFINITY;
                double bLo = Double.POSITIVE_INFINITY, bHi = Double.NEGATIVE_INFINITY;
                for (double[] p : A) {
                    aLo = Math.min(aLo, p[0] * nx + p[1] * ny);
                    aHi = Math.max(aHi, p[0] * nx + p[1] * ny);
                }
                for (double[] p : B) {
                    bLo = Math.min(bLo, p[0] * nx + p[1] * ny);
                    bHi = Math.max(bHi, p[0] * nx + p[1] * ny);
                }
                best = Math.max(best, Math.max(bLo - aHi, aLo - bHi));
            }
        }
        return best;
    }
}
