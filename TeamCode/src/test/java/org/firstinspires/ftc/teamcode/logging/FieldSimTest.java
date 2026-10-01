package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.HiveGeometry;
import org.firstinspires.ftc.teamcode.vision.HiveState;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The game-piece and HIVE simulation behaves like the game: pieces fall, settle and stay on the
 * field, shots follow their arcs, the starting NECTAR does not tip a HIVE, enough scored pieces do,
 * and what is in the CELL that goes down spills out.
 */
public class FieldSimTest {

    private static final double LOOP = 0.02;

    private static FieldSim empty() {
        return new FieldSim(new ArrayList<>(), 1);
    }

    private static FieldSim.Piece drop(FieldSim sim, double x, double y, double z) {
        FieldSim.Piece p = new FieldSim.Piece(FieldSim.Kind.POLLEN, FieldSim.Where.FIELD, x, y, z);
        sim.pieces.add(p);
        return p;
    }

    private static void run(FieldSim sim, double seconds) {
        for (int i = 0; i < Math.round(seconds / LOOP); i++) sim.step(LOOP);
    }

    @Test
    public void aDroppedPieceBouncesAndComesToRestOnTheTiles() {
        FieldSim sim = empty();
        FieldSim.Piece p = drop(sim, 30, 30, 40);
        p.vx = 30;
        run(sim, 0.3);
        assertTrue("still falling or bouncing", p.z > FieldSim.POLLEN_RADIUS_IN + 1 || p.vz != 0);
        run(sim, 6);
        assertEquals(FieldSim.POLLEN_RADIUS_IN, p.z, 1e-9);
        assertEquals(0, Math.hypot(p.vx, p.vy), 1e-9);
        assertEquals(0, p.vz, 1e-9);
    }

    @Test
    public void nothingLeavesTheField() {
        FieldSim sim = empty();
        double[][] throwsAt = {{400, 0}, {-400, 0}, {0, 400}, {0, -400}, {300, 300}};
        List<FieldSim.Piece> pieces = new ArrayList<>();
        for (double[] v : throwsAt) {
            FieldSim.Piece p = drop(sim, 20, 20, 10);
            p.vx = v[0];
            p.vy = v[1];
            p.vz = 150;
            pieces.add(p);
        }
        for (int i = 0; i < 300; i++) {
            sim.step(LOOP);
            for (FieldSim.Piece p : pieces) {
                double r = p.kind.radius;
                assertTrue(p.x >= r - 1e-9 && p.x <= FieldSim.FIELD_SIZE_IN - r + 1e-9);
                assertTrue(p.y >= r - 1e-9 && p.y <= FieldSim.FIELD_SIZE_IN - r + 1e-9);
                assertTrue(p.z >= r - 1e-9);
            }
        }
    }

    @Test
    public void aLaunchFollowsItsBallisticArcToTheTarget() {
        FieldSim sim = empty();
        FieldSim.Piece p = new FieldSim.Piece(FieldSim.Kind.POLLEN, FieldSim.Where.ROBOT, 0, 0, 0);
        sim.pieces.add(p);
        sim.stored.add(p);
        sim.setRobot(30, 110, 0, 0, 0, 0, false);
        double[] target = {110, 110, 30};
        double[] from = sim.exitPoint();
        double[] v = sim.launchVelocity(from, target);
        assertNotNull(v);
        // The arc the planner draws passes through the target...
        boolean near = false;
        for (double[] q : FieldSim.arc(from, v, target[2], 400)) {
            near |= Math.hypot(Math.hypot(q[0] - target[0], q[1] - target[1]), q[2] - target[2]) < 0.5;
        }
        assertTrue(near);
        // ...and a simulated launch, spread and all, clears the robot and lands close to it.
        assertNotNull(sim.launch(target));
        double closest = Double.POSITIVE_INFINITY;
        for (int i = 0; i < 200 && !(p.vz < 0 && p.z < target[2] - 10); i++) {
            sim.step(LOOP);
            closest = Math.min(closest, Math.hypot(Math.hypot(p.x - target[0], p.y - target[1]), p.z - target[2]));
        }
        assertTrue("missed by " + closest + " in", closest < 4);
    }

    /** The CAD and the manual agree on where the raised CELL's opening is; so does the simulation. */
    @Test
    public void raisedOpeningIsWhereTheManualPutsIt() {
        FieldSim sim = empty();
        for (FieldSim.Rocker r : new FieldSim.Rocker[] {sim.red, sim.blue}) {
            int end = r.raisedEnd();
            double[] bottom = r.toWorld(0, end * FieldSim.CELL_OPENING_IN, FieldSim.CELL_FLOOR_IN);
            double[] top = r.toWorld(0, end * FieldSim.CELL_OPENING_IN, FieldSim.CELL_ROOF_IN);
            assertEquals(HiveGeometry.OPENING_BOTTOM_HEIGHT_IN, bottom[2], 1e-9);
            assertEquals(HiveGeometry.OPENING_TOP_HEIGHT_IN, top[2], 0.3);
        }
        assertEquals(HiveState.RIGHT_CELL_UP, sim.red.state());
        assertEquals(HiveState.RIGHT_CELL_UP, sim.blue.state());
        assertEquals(HiveCell.RED_AUDIENCE, sim.red.cell(sim.red.raisedEnd()));
        assertEquals(HiveCell.BLUE_SCORING, sim.blue.cell(sim.blue.raisedEnd()));
    }

    @Test
    public void theStartingFieldIsAtRestAndTheStartingNectarDoesNotTipAHive() throws IOException {
        FieldSim sim = new FieldSim(HiveAssets.committedStagedPieces(), 1);
        assertEquals(3, sim.count(HiveCell.RED_AUDIENCE));
        assertEquals(3, sim.count(HiveCell.BLUE_SCORING));
        double[] before = positions(sim);
        run(sim, 5);
        double[] after = positions(sim);
        for (int i = 0; i < before.length; i++) assertEquals(before[i], after[i], 0.5);
        assertEquals(HiveState.RIGHT_CELL_UP, sim.red.state());
        assertEquals(HiveState.RIGHT_CELL_UP, sim.blue.state());
        assertEquals(0, sim.red.tips + sim.blue.tips);
        assertTrue(sim.drainEvents().isEmpty());
    }

    @Test
    public void enoughScoredPiecesTipTheHiveAndTheLoweredCellSpills() throws IOException {
        FieldSim sim = new FieldSim(HiveAssets.committedStagedPieces(), 1);
        double[] aim = sim.red.aimPoint();
        double y = sim.red.openingCentre()[1] - SimDriver.SHOT_DISTANCE_IN;
        int shots = 0;
        while (sim.red.tips == 0 && shots < 8) {
            FieldSim.Piece p = new FieldSim.Piece(FieldSim.Kind.POLLEN, FieldSim.Where.ROBOT, 0, 0, 0);
            sim.pieces.add(p);
            sim.stored.add(p);
            sim.setRobot(sim.red.centreX, y, Math.PI / 2, 0, 0, 0, false);
            assertNotNull(sim.launch(aim));
            shots++;
            // Flight, settling, and a TIP that has started finishing: as in the calibration.
            run(sim, 0.6 + HiveCalibration.SETTLE_S);
            for (int i = 0; i < 500 && sim.red.state() == HiveState.TRANSITION; i++) sim.step(LOOP);
        }
        assertEquals("tipped after " + shots + " shots", 1, sim.red.tips);
        assertEquals("launched POLLEN tip it on the calibrated count, like dropped ones",
                HiveCalibration.current().pollenToTipFromMatchStart(), shots);
        assertEquals(HiveState.LEFT_CELL_UP, sim.red.state());
        run(sim, 2);
        assertEquals("the lowered CELL emptied", 0, sim.count(HiveCell.RED_AUDIENCE));
        boolean spilled = false;
        for (String e : sim.drainEvents()) spilled |= e.startsWith("spill: red NECTAR out of RED AUDIENCE");
        assertTrue(spilled);
        assertEquals("the blue HIVE is untouched", HiveState.RIGHT_CELL_UP, sim.blue.state());
    }

    /** Components are the rockers turned about their axle: the axle itself does not move. */
    @Test
    public void hiveComponentsTurnAboutTheAxle() {
        FieldSim sim = empty();
        double[] c = sim.hiveComponents();
        for (int i = 0; i < 2; i++) {
            assertEquals(0, c[7 * i], 1e-12);
            assertEquals(0, c[7 * i + 2], 1e-12);
            assertEquals(1, c[7 * i + 3], 1e-12);
        }
        sim.red.angle = -sim.red.builtAngle();
        c = sim.hiveComponents();
        double h = FieldSim.PIVOT_Z_IN * AdvantageScopeFrame.METERS_PER_INCH;
        // Apply the red pose to the axle point (0, 0, h): it must stay put.
        double w = c[3], qy = c[5];
        double angle = 2 * Math.atan2(qy, w);
        double ax = c[0] + h * Math.sin(angle), az = c[2] + h * Math.cos(angle);
        assertEquals(0, ax, 1e-12);
        assertEquals(h, az, 1e-12);
        assertEquals(Math.toRadians(2 * HiveGeometry.CELL_TILT_DEG), angle, 1e-12);
        assertEquals(1, c[7 + 3], 1e-12); // blue unmoved
        assertEquals(Alliance.RED, sim.rocker(Alliance.RED).alliance);
    }

    private static double[] positions(FieldSim sim) {
        double[] out = new double[3 * sim.pieces.size()];
        int i = 0;
        for (FieldSim.Piece p : sim.pieces) {
            out[i++] = p.x;
            out[i++] = p.y;
            out[i++] = p.z;
        }
        return out;
    }
}
