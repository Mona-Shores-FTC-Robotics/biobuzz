package org.firstinspires.ftc.teamcode.localization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.localization.LaunchSpots.Choice;
import org.firstinspires.ftc.teamcode.localization.LaunchSpots.Column;
import org.firstinspires.ftc.teamcode.localization.LaunchSpots.Row;
import org.firstinspires.ftc.teamcode.localization.LaunchSpots.Spot;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.FieldFrame;
import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.HiveCellState;
import org.firstinspires.ftc.teamcode.vision.Vec3;
import org.junit.Test;

/**
 * {@link LaunchSpots}: pose, alliance and the button's row in, the spot in line with the robot (or a
 * refusal) out; and the twelve spots' geometry, for a turret locked forward.
 */
public class LaunchSpotsTest {

    private static final double C = FieldFrame.FIELD_CENTRE_INCHES;
    private static final double SIZE = FieldFrame.FIELD_SIZE_INCHES;
    private static final double EPS = 1e-9;
    /** The turret's place on the robot ({@code TurretSubsystem}): 3.33 in behind, 1.87 in right. */
    private static final double FWD = -3.33, LEFT = -1.87;
    /** Past centre far enough for every FAR spot: the TeleOp's reach. */
    private static final double REACH = 24.0;

    private static Choice red(Row row, double x, double y) {
        return LaunchSpots.choose(new Pose(x, y, 0), true, Alliance.RED, row, 0.0, REACH, FWD, LEFT);
    }

    private static Pose target(Spot spot) {
        return spot.target(Alliance.RED, 0.0, FWD, LEFT);
    }

    private static double wrap(double rad) {
        return Math.IEEEremainder(rad, 2 * Math.PI);
    }

    /** The frame's four corners at {@code p}, a square of {@link LaunchSpots#FRAME_HALF_IN}. */
    private static double[][] corners(Pose p) {
        double h = LaunchSpots.FRAME_HALF_IN, c = Math.cos(p.heading()), s = Math.sin(p.heading());
        double[][] local = {{h, h}, {h, -h}, {-h, -h}, {-h, h}};
        double[][] out = new double[4][2];
        for (int i = 0; i < 4; i++) {
            out[i][0] = p.x() + local[i][0] * c - local[i][1] * s;
            out[i][1] = p.y() + local[i][0] * s + local[i][1] * c;
        }
        return out;
    }

    /** Whether two frames overlap: the separating-axis test for two rectangles. */
    private static boolean overlap(Pose a, Pose b) {
        double[][] pa = corners(a), pb = corners(b);
        for (double[][] poly : new double[][][] {pa, pb}) {
            for (int i = 0; i < 4; i++) {
                double ax = poly[(i + 1) % 4][1] - poly[i][1], ay = poly[i][0] - poly[(i + 1) % 4][0];
                double minA = Double.POSITIVE_INFINITY, maxA = Double.NEGATIVE_INFINITY;
                double minB = Double.POSITIVE_INFINITY, maxB = Double.NEGATIVE_INFINITY;
                for (double[] q : pa) {
                    double d = q[0] * ax + q[1] * ay;
                    minA = Math.min(minA, d);
                    maxA = Math.max(maxA, d);
                }
                for (double[] q : pb) {
                    double d = q[0] * ax + q[1] * ay;
                    minB = Math.min(minB, d);
                    maxB = Math.max(maxB, d);
                }
                if (maxA <= minB || maxB <= minA) return false;
            }
        }
        return true;
    }

    @Test
    public void theRightWallHiveSpotIsTheMeasuredStart() {
        Pose start = StartPositions.RED_AUDIENCE.pose;
        // As drawn it is the start; Square stops its stand-off out from it, squared as the start is.
        assertEquals(start.x(), Spot.RIGHT_START.red.x(), EPS);
        assertEquals(start.y(), Spot.RIGHT_START.red.y(), EPS);
        Pose t = target(Spot.RIGHT_START);
        assertEquals(start.x(), t.x(), EPS);
        assertEquals(start.y() + LaunchSpots.RIGHT_WALL_STAND_OFF_IN, t.y(), EPS);
        assertEquals(0, wrap(start.heading() - t.heading()), EPS);
        assertEquals(Spot.RIGHT_START, Spot.of(false, Row.WALL, Column.HIVE));
        // The LEFT start is placed on the centre-line seam, so the LEFT spot is its own.
        assertTrue(Math.abs(StartPositions.RED_FAR.pose.x() - target(Spot.LEFT_WALL_HIVE).x()) > 2.0);
    }

    @Test
    public void thereAreTwelveOnePerHalfRowAndColumn() {
        assertEquals(12, Spot.values().length);
        for (boolean left : new boolean[] {false, true}) {
            for (Row row : Row.values()) {
                for (Column column : Column.values()) {
                    Spot spot = Spot.of(left, row, column);
                    assertEquals(left, spot.left);
                    assertEquals(row, spot.row);
                    assertEquals(column, spot.column);
                    // The RIGHT wall's HIVE spot is the measured start, under the start's name.
                    String name = spot == Spot.RIGHT_START ? "RIGHT_START"
                            : (left ? "LEFT_" : "RIGHT_") + row + "_" + column;
                    assertEquals(name, spot.name());
                }
            }
        }
    }

    @Test
    public void theButtonsRowAndTheRobotsPlaceAlongTheWallPickTheSpot() {
        // RIGHT half (below the axle line for red), from anywhere between the wall and the HIVE.
        assertEquals(Spot.RIGHT_WALL_NEAR, red(Row.WALL, 30, 30).spot);
        assertEquals(Spot.RIGHT_START, red(Row.WALL, 56, 45).spot);
        assertEquals(Spot.RIGHT_WALL_FAR, red(Row.WALL, 72, 20).spot);
        assertEquals(Spot.RIGHT_CLOSE_NEAR, red(Row.CLOSE, 30, 30).spot);
        assertEquals(Spot.RIGHT_CLOSE_HIVE, red(Row.CLOSE, 56, 45).spot);
        assertEquals(Spot.RIGHT_CLOSE_FAR, red(Row.CLOSE, 72, 20).spot);
        // LEFT half.
        assertEquals(Spot.LEFT_WALL_NEAR, red(Row.WALL, 30, 110).spot);
        assertEquals(Spot.LEFT_WALL_HIVE, red(Row.WALL, 60, 100).spot);
        assertEquals(Spot.LEFT_WALL_FAR, red(Row.WALL, 75, 120).spot);
        assertEquals(Spot.LEFT_CLOSE_HIVE, red(Row.CLOSE, 60, 100).spot);
        // No row asked for is the wall row.
        assertEquals(Spot.RIGHT_START, red(null, 56, 45).spot);
    }

    @Test
    public void theColumnIsTheNearestWallSpotAlongTheWallHoweverFarFromIt() {
        for (boolean left : new boolean[] {false, true}) {
            double near = Spot.of(left, Row.WALL, Column.NEAR).red.x();
            double hive = Spot.of(left, Row.WALL, Column.HIVE).red.x();
            double far = Spot.of(left, Row.WALL, Column.FAR).red.x();
            assertTrue(near < hive && hive < far);
            assertEquals(Column.NEAR, LaunchSpots.columnFor(left, 0));
            assertEquals(Column.NEAR, LaunchSpots.columnFor(left, (near + hive) / 2 - 0.1));
            assertEquals(Column.HIVE, LaunchSpots.columnFor(left, (near + hive) / 2 + 0.1));
            assertEquals(Column.HIVE, LaunchSpots.columnFor(left, (hive + far) / 2 - 0.1));
            assertEquals(Column.FAR, LaunchSpots.columnFor(left, (hive + far) / 2 + 0.1));
            assertEquals(Column.FAR, LaunchSpots.columnFor(left, SIZE));
        }
        // The same column for both buttons, and for every distance from the wall on that half.
        for (double y : new double[] {5, 30, 60}) {
            assertEquals(Column.NEAR, red(Row.WALL, 40, y).spot.column);
            assertEquals(Column.NEAR, red(Row.CLOSE, 40, y).spot.column);
        }
    }

    @Test
    public void theChoiceSaysHowFarTheDriveIs() {
        Choice c = red(Row.WALL, 45, 40);
        assertEquals(Math.hypot(c.target.x() - 45, c.target.y() - 40), c.distanceIn, EPS);
        assertTrue(c.toString(), c.toString().contains("in line"));
    }

    @Test
    public void aHiveColumnSpotIsSquaredToTheWallWithTheTurretInLineWithTheRow() {
        for (Spot spot : Spot.values()) {
            if (spot.column != Column.HIVE) continue;
            assertFalse(spot.name(), spot.aimed);
            Pose t = target(spot);
            assertEquals(spot + " faces away from its wall", spot.left ? 270.0 : 90.0,
                    (Math.toDegrees(t.heading()) + 360) % 360, 1e-6);
            Vec3 row = spot.cellRow(Alliance.RED);
            double ax = t.x() + FWD * Math.cos(t.heading()) - LEFT * Math.sin(t.heading());
            // RIGHT_START is where the robot is placed (frame on the seam): half an inch off.
            assertEquals(spot + ": the turret's axis is in line with the row's centre", row.x(), ax, 0.6);
        }
    }

    @Test
    public void everyOtherSpotPointsTheTurretAtItsCellsRow() {
        for (Alliance alliance : new Alliance[] {Alliance.RED, Alliance.BLUE}) {
            for (Spot spot : Spot.values()) {
                if (spot.column == Column.HIVE) continue;
                assertTrue(spot.name(), spot.aimed);
                Pose t = spot.target(alliance, 0.0, FWD, LEFT);
                Vec3 row = spot.cellRow(alliance);
                double ax = t.x() + FWD * Math.cos(t.heading()) - LEFT * Math.sin(t.heading());
                double ay = t.y() + FWD * Math.sin(t.heading()) + LEFT * Math.cos(t.heading());
                assertEquals(spot + " " + alliance, 0, wrap(Math.atan2(row.y() - ay, row.x() - ax) - t.heading()), 1e-6);
            }
        }
        // The drawn heading is what that comes to with today's turret offsets, from where it is drawn.
        for (Spot spot : Spot.values()) {
            double h = spot.aimed ? LaunchSpots.aimTurret(spot.red.x(), spot.red.y(), spot.cellRow(Alliance.RED).x(),
                    spot.cellRow(Alliance.RED).y(), FWD, LEFT) : spot.red.heading();
            assertEquals(spot.name(), 0, Math.toDegrees(wrap(h - spot.red.heading())), 0.3);
        }
    }

    @Test
    public void noShotIsMoreThan26DegreesOffSquareAndTheRowsShareARange() {
        for (Spot spot : Spot.values()) {
            Pose t = target(spot);
            Vec3 row = spot.cellRow(Alliance.RED);
            double ax = t.x() + FWD * Math.cos(t.heading()) - LEFT * Math.sin(t.heading());
            double ay = t.y() + FWD * Math.sin(t.heading()) + LEFT * Math.cos(t.heading());
            // The row runs along field x, so square to it is along field y.
            double offSquareDeg = Math.toDegrees(Math.atan2(Math.abs(row.x() - ax), Math.abs(row.y() - ay)));
            assertTrue(spot + " comes in " + offSquareDeg + " deg off square", offSquareDeg <= 26.0);
            double range = Math.hypot(row.x() - ax, row.y() - ay);
            if (spot.row == Row.WALL) {
                assertTrue(spot + " range " + range, range > 51.5 && range < 56);
            } else {
                assertTrue(spot + " range " + range, range > 34 && range < 37);
            }
        }
    }

    @Test
    public void aWallSpotTouchesItsWallAndNoSpotLeavesTheField() {
        for (Spot spot : Spot.values()) {
            double low = Double.POSITIVE_INFINITY, high = Double.NEGATIVE_INFINITY;
            for (double[] corner : corners(target(spot))) {
                assertTrue(spot + " x " + corner[0], corner[0] > 0 && corner[0] < SIZE);
                low = Math.min(low, corner[1]);
                high = Math.max(high, corner[1]);
            }
            // The LEFT half's wall is the high-y one. Touching: within a quarter inch, never through it.
            // The measured start has its back on the wall at 7.7 in, where the corner resets measured
            // 8.1 from wall to centre (FRAME_HALF_IN): 0.4 in "through" here is that difference.
            // Less its stand-off: where the spot is drawn.
            double gap = (spot.left ? SIZE - high : low) - spot.standOffIn;
            assertTrue(spot + " is " + (-gap) + " in through its wall", gap > (spot == Spot.RIGHT_START ? -0.45 : -0.15));
            if (spot.row == Row.WALL) {
                assertTrue(spot + " is " + gap + " in off its wall", gap < 0.25);
            } else {
                assertTrue(spot + " is a close spot, off the wall", gap > 15);
            }
        }
    }

    @Test
    public void theRightWallRowStandsAnInchOutAndNothingElseDoes() {
        for (Spot spot : Spot.values()) {
            boolean rightWall = spot.row == Row.WALL && !spot.left;
            assertEquals(spot.name(), rightWall ? 1.0 : 0.0, spot.standOffIn, EPS);
            Pose t = target(spot);
            assertEquals(spot.name(), spot.red.x(), t.x(), EPS);
            assertEquals(spot.name(), spot.red.y() + spot.standOffIn, t.y(), EPS); // RIGHT: out is +y
        }
    }

    @Test
    public void aWallGapMovesOnlyTheWallRowOutFromTheWall() {
        for (Spot spot : Spot.values()) {
            Pose touching = spot.target(Alliance.RED, 0.0, FWD, LEFT);
            Pose off = spot.target(Alliance.RED, 2.0, FWD, LEFT);
            assertEquals(spot.name(), touching.x(), off.x(), EPS);
            double moved = spot.left ? touching.y() - off.y() : off.y() - touching.y();
            assertEquals(spot.name(), spot.row == Row.WALL ? 2.0 : 0.0, moved, EPS);
            // NaN or negative is touching.
            assertEquals(touching.y(), spot.target(Alliance.RED, Double.NaN, FWD, LEFT).y(), EPS);
            assertEquals(touching.y(), spot.target(Alliance.RED, -3, FWD, LEFT).y(), EPS);
        }
    }

    @Test
    public void spotsThatMayBeUsedTogetherDoNotOverlap() {
        for (Spot a : Spot.values()) {
            for (Spot b : Spot.values()) {
                if (a.ordinal() >= b.ordinal() || a.left != b.left) continue;
                // Inside the close row the side spots overlap the middle one, by design: they are
                // alternatives, so only NEAR with FAR must clear each other there.
                boolean closePair = a.row == Row.CLOSE && b.row == Row.CLOSE;
                if (closePair && (a.column == Column.HIVE || b.column == Column.HIVE)) continue;
                assertFalse(a + " overlaps " + b, overlap(target(a), target(b)));
            }
        }
        assertTrue("the close fan overlaps its middle",
                overlap(target(Spot.RIGHT_CLOSE_NEAR), target(Spot.RIGHT_CLOSE_HIVE)));
    }

    @Test
    public void theLeftWallSpotsStraddleTheScoringFlowerWithoutEnteringIt() {
        // The SCORING FLOWER, as the Visualizer draws it: x 43.4 to 50.1, 3.8 in out from the wall.
        double x0 = 43.4, x1 = 50.1, y0 = SIZE - 3.8;
        for (Spot spot : new Spot[] {Spot.LEFT_WALL_NEAR, Spot.LEFT_WALL_HIVE}) {
            double[][] c = corners(target(spot));
            for (int i = 0; i < 4; i++) {
                for (int k = 0; k <= 100; k++) {
                    double x = c[i][0] + (c[(i + 1) % 4][0] - c[i][0]) * k / 100.0;
                    double y = c[i][1] + (c[(i + 1) % 4][1] - c[i][1]) * k / 100.0;
                    assertFalse(spot + " is in the FLOWER at (" + x + ", " + y + ")",
                            x > x0 + 0.1 && x < x1 - 0.1 && y > y0 + 0.1);
                }
            }
        }
        assertTrue("NEAR is on the FLOWER's far side", target(Spot.LEFT_WALL_NEAR).x() < x0);
        assertTrue("HIVE is on its near side", target(Spot.LEFT_WALL_HIVE).x() > x1);
    }

    @Test
    public void blueIsRedTurnedHalfATurn() {
        double[][] where = {{30, 30}, {60, 22}, {56, 6}, {65, 135}, {60, 120}, {30, 110}, {45, 60}, {75, 12}, {80, 129}};
        for (Row row : Row.values()) {
            for (double[] w : where) {
                Pose p = new Pose(w[0], w[1], 0);
                Choice r = LaunchSpots.choose(p, true, Alliance.RED, row, 0.0, REACH, FWD, LEFT);
                Choice b = LaunchSpots.choose(LaunchSpots.turned(p), true, Alliance.BLUE, row, 0.0, REACH, FWD, LEFT);
                assertNotNull(p.toString(), r.spot);
                assertEquals(p.toString(), r.spot, b.spot);
                Pose expected = LaunchSpots.turned(r.target);
                assertEquals(expected.x(), b.target.x(), EPS);
                assertEquals(expected.y(), b.target.y(), EPS);
                assertEquals(0, wrap(expected.heading() - b.target.heading()), EPS);
            }
        }
    }

    @Test
    public void eachSpotIsOnItsCellsHalfAndOnlyTheFarColumnIsPastTheCentreLine() {
        for (Spot spot : Spot.values()) {
            Pose red = target(spot);
            Pose blue = spot.target(Alliance.BLUE, 0.0, FWD, LEFT);
            assertEquals(spot + " red half", spot.left, LaunchSpots.leftHalf(Alliance.RED, red.y()));
            assertEquals(spot + " blue half", spot.left, LaunchSpots.leftHalf(Alliance.BLUE, blue.y()));
            assertEquals(spot.name(), spot.name().startsWith("LEFT_"), spot.left);
            boolean past = red.x() > C;
            if (spot.column != Column.FAR) assertFalse(spot + " is past the centre line", past);
            // A spot past the line is still within the reach the button drives back from.
            assertTrue(spot + " within reach", LaunchSpots.withinReach(Alliance.RED, red.x(), REACH));
        }
        assertTrue("the wall row's far spots are past the line", target(Spot.RIGHT_WALL_FAR).x() > C
                && target(Spot.LEFT_WALL_FAR).x() > C);
    }

    @Test
    public void eachSpotServesItsOwnHalfsCell() {
        for (Alliance alliance : new Alliance[]{Alliance.RED, Alliance.BLUE}) {
            for (Spot spot : Spot.values()) {
                HiveCell cell = spot.left ? HiveCell.leftCell(alliance) : HiveCell.rightCell(alliance);
                Vec3 row = spot.cellRow(alliance);
                Vec3 expected = HiveFieldPoints.rowCentre(cell, HiveCellState.UP);
                assertEquals(expected.x(), row.x(), EPS);
                assertEquals(expected.y(), row.y(), EPS);
                // The CELL is on the spot's side of the axle line.
                Pose t = spot.target(alliance, 0.0);
                assertEquals(spot + " " + alliance, t.y() > HiveFieldPoints.AXLE_Y_IN, row.y() > HiveFieldPoints.AXLE_Y_IN);
            }
        }
        assertNull(Spot.RIGHT_START.cellRow(Alliance.UNKNOWN));
    }

    @Test
    public void driftWarnsOnlyPastAPositiveThreshold() {
        assertFalse(LaunchSpots.driftWarns(2.9, 3.0));
        assertFalse(LaunchSpots.driftWarns(3.0, 3.0));
        assertTrue(LaunchSpots.driftWarns(3.1, 3.0));
        assertFalse("no camera frame never warns", LaunchSpots.driftWarns(Double.NaN, 3.0));
        assertFalse("a NaN threshold never warns", LaunchSpots.driftWarns(50, Double.NaN));
        assertFalse("0 turns it off", LaunchSpots.driftWarns(50, 0));
    }

    @Test
    public void anUnreferencedPoseIsRefusedAndNothingIsChosen() {
        Choice c = LaunchSpots.choose(new Pose(40, 30, 0), false, Alliance.RED, Row.WALL, 0.0, 0.0);
        assertTrue(c.refused());
        assertNull(c.spot);
        assertNull(c.target);
        assertTrue(c.refusal, c.refusal.contains("not on the field"));
    }

    @Test
    public void noPoseNoAllianceOrTheOtherSideIsRefused() {
        assertTrue(LaunchSpots.choose(null, true, Alliance.RED, Row.WALL, 0.0, 0.0).refused());
        assertTrue(LaunchSpots.choose(new Pose(40, 30, 0), true, Alliance.UNKNOWN, Row.WALL, 0.0, 0.0).refused());
        assertTrue(LaunchSpots.choose(new Pose(40, 30, 0), true, null, Row.WALL, 0.0, 0.0).refused());
        // With no reach: red past the centre line or on it, blue short of it.
        Choice past = LaunchSpots.choose(new Pose(80, 30, 0), true, Alliance.RED, Row.WALL, 0.0, 0.0);
        assertTrue(past.refused());
        assertTrue(past.refusal, past.refusal.contains("centre line"));
        assertTrue(LaunchSpots.choose(new Pose(C, 30, 0), true, Alliance.RED, Row.CLOSE, 0.0, 0.0).refused());
        assertTrue(LaunchSpots.choose(new Pose(60, 110, 0), true, Alliance.BLUE, Row.WALL, 0.0, 0.0).refused());
    }

    @Test
    public void aReachPastTheCentreLineWorksFromWithinIt() {
        // 6 Nov 2026: refused at 3 to 4 in past the line (red at 73.9, blue at 67.4).
        Choice red = LaunchSpots.choose(new Pose(73.9, 125.3, 0), true, Alliance.RED, Row.WALL, 0.0, 24.0);
        assertFalse(red.refusal, red.refused());
        Choice blue = LaunchSpots.choose(new Pose(67.4, 110, 0), true, Alliance.BLUE, Row.WALL, 0.0, 24.0);
        assertFalse(blue.refusal, blue.refused());
        // Past the reach, refused, and the refusal says by how much.
        Choice far = LaunchSpots.choose(new Pose(C + 30, 30, 0), true, Alliance.RED, Row.WALL, 0.0, 24.0);
        assertTrue(far.refused());
        assertTrue(far.refusal, far.refusal.contains("30 in past the centre line"));
        assertTrue(LaunchSpots.choose(new Pose(C - 30, 30, 0), true, Alliance.BLUE, Row.WALL, 0.0, 24.0).refused());
        // NaN or negative reach is 0: strictly our side.
        assertTrue(LaunchSpots.choose(new Pose(C + 1, 30, 0), true, Alliance.RED, Row.WALL, 0.0, Double.NaN).refused());
        assertTrue(LaunchSpots.choose(new Pose(C + 1, 30, 0), true, Alliance.RED, Row.WALL, 0.0, -5).refused());
    }
}
