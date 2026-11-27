package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.localization.GardenFieldPoints;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * The simulator's corner wheels ({@link FieldSim#rake}, {@code doc/corner-wheels.md}): the real robot driven by hand
 * into red's GARDEN, both ways in, with the staged row as the field model has it. No Auto runs here, only the
 * field and one robot, so it takes a fraction of a second and runs with the unit tests.
 */
public class CornerWheelSimTest {

    private static final double LOOP_S = 0.02;

    private static FieldSim field(boolean wheels) throws Exception {
        FieldSim sim = new FieldSim(HiveAssets.committedStagedPieces(), 1);
        RobotDesign design = RobotDesign.real().copy("corner wheel test");
        design.cornerWheels = wheels;
        sim.main.design = design;
        return sim;
    }

    /** Drives the robot in a straight line from {@code from} to {@code to} at {@code inPerS}, then stands {@code standS}. */
    private static void drive(FieldSim sim, Pose from, Pose to, double inPerS, double standS, boolean intake) {
        double length = Math.hypot(to.x() - from.x(), to.y() - from.y());
        double vx = length == 0 ? 0 : (to.x() - from.x()) / length * inPerS;
        double vy = length == 0 ? 0 : (to.y() - from.y()) / length * inPerS;
        int steps = (int) Math.ceil(length / (inPerS * LOOP_S));
        for (int k = 0; k <= steps; k++) {
            double f = steps == 0 ? 1 : (double) k / steps;
            sim.main.set(from.x() + (to.x() - from.x()) * f, from.y() + (to.y() - from.y()) * f, to.heading(),
                    k < steps ? vx : 0, k < steps ? vy : 0, 0, intake);
            sim.step(LOOP_S);
        }
        stand(sim, to, standS, intake);
    }

    private static void stand(FieldSim sim, Pose at, double seconds, boolean intake) {
        for (int k = 0; k < Math.round(seconds / LOOP_S); k++) {
            sim.main.set(at.x(), at.y(), at.heading(), 0, 0, 0, intake);
            sim.step(LOOP_S);
        }
    }

    private static List<FieldSim.Piece> redGarden(FieldSim sim) {
        List<FieldSim.Piece> row = new ArrayList<>();
        for (FieldSim.Piece p : sim.pieces) {
            if (p.where == FieldSim.Where.FIELD && FieldSim.garden(p.x, p.y) == Alliance.RED) row.add(p);
        }
        return row;
    }

    /** An Auto's GardenGatherAlongWall: from WALL_GARDEN_STAGING (28, 9, 180°) along the audience wall into the corner. */
    private static FieldSim alongWall(boolean wheels, boolean stickDown) throws Exception {
        FieldSim sim = field(wheels);
        assertEquals("the staged row", 4, redGarden(sim).size());
        Pose in = GardenFieldPoints.alongAudienceWall(GardenFieldPoints.RED).gather;
        sim.main.gardenGather = true;
        sim.main.extractorDown = stickDown ? 1 : 0;
        // The four come off the wheel one at a time, about 0.33 s each: 1.5 s covers it (the gather has about 1.6).
        drive(sim, new Pose(28.0, in.y(), in.heading()), in, 40.0, 1.5, true);
        return sim;
    }

    @Test
    public void alongTheWallTheLeftWheelTakesTheWholeRow() throws Exception {
        FieldSim sim = alongWall(true, false);
        assertEquals("the row lies outside the mouth, down the left corner strip: 4 POLLEN", 4, sim.main.stored.size());
        assertEquals("every one off the wheel", 4, sim.main.rakedIn);
        assertEquals("none left in the GARDEN", 0, redGarden(sim).size());
        assertEquals(0, sim.main.onWheels());
    }

    @Test
    public void alongTheWallWithoutWheelsTheRowIsPushedAway() throws Exception {
        FieldSim sim = alongWall(false, false);
        assertEquals(0, sim.main.rakedIn);
        assertTrue("the flat front drives the row into the side wall: fewer than 4, took " + sim.main.stored.size(),
                sim.main.stored.size() < 4);
    }

    /** Master's rule (CornerWheelSubsystem.stopFor): the wheels do not spin with the FLOWER stick down. */
    @Test
    public void theWheelsStopWithTheStickDown() throws Exception {
        FieldSim sim = alongWall(true, true);
        assertEquals(0, sim.main.rakedIn);
        assertTrue(sim.main.stored.size() < 4);
    }

    /** An Auto's GardenGather: from GARDEN_STAGING (11.5, 22.1, 270°) into the corner, facing the audience wall. */
    @Test
    public void facingTheAudienceWallTheRightWheelBringsInTheCornerPiece() throws Exception {
        FieldSim sim = field(true);
        Pose in = GardenFieldPoints.RED.gather;
        sim.main.gardenGather = true;
        drive(sim, new Pose(11.5, 22.1, in.heading()), in, 25.0, 0.6, true);
        assertEquals("3 by the mouth's touch and the corner piece off the right wheel", 4, sim.main.stored.size());
        assertEquals(1, sim.main.rakedIn);

        FieldSim without = field(false);
        without.main.gardenGather = true;
        drive(without, new Pose(11.5, 22.1, in.heading()), in, 25.0, 0.6, true);
        assertEquals("without the wheels the corner piece stays", 3, without.main.stored.size());
    }

    /** One loose POLLEN put on the left corner strip of a standing robot, {@code asideIn} from its centre line. */
    private static FieldSim.Piece onTheLeftCorner(FieldSim sim, Pose robot, double asideIn) {
        FieldSim.Piece p = redGarden(sim).get(0);
        double ahead = sim.main.design.frameIn / 2 + p.kind.radius;
        p.x = robot.x() + ahead * Math.cos(robot.heading()) - asideIn * Math.sin(robot.heading());
        p.y = robot.y() + ahead * Math.sin(robot.heading()) + asideIn * Math.cos(robot.heading());
        p.z = p.kind.radius;
        p.vx = p.vy = p.vz = 0;
        return p;
    }

    /** The pace: a piece 1.65 in outside the mouth's edge reaches it at 6.3 in/s, about 0.27 s, and is taken then. */
    @Test
    public void aPieceIsRakedInAtTheWheelsPace() throws Exception {
        FieldSim sim = field(true);
        Pose robot = new Pose(40.0, 30.0, 0.0);
        stand(sim, robot, LOOP_S, true);
        double aside = sim.main.design.intakeWidthIn / 2 + 1.65;
        FieldSim.Piece p = onTheLeftCorner(sim, robot, aside);
        double expected = (1.65 + FieldSim.CORNER_WHEEL_INTO_MOUTH_IN) / sim.main.design.cornerWheelRakeInPerS;
        stand(sim, robot, expected - 0.06, true);
        assertNotNull("on the wheel", p.rakedBy);
        assertEquals("not at the mouth yet", 0, sim.main.stored.size());
        assertTrue("moving inward", Math.abs(p.y - robot.y()) < aside);
        stand(sim, robot, 0.12, true);
        assertEquals("taken as it reaches the mouth", 1, sim.main.stored.size());
        assertEquals(1, sim.main.rakedIn);
    }

    @Test
    public void aPieceBeyondTheWheelIsNotRaked() throws Exception {
        FieldSim sim = field(true);
        Pose robot = new Pose(40.0, 30.0, 0.0);
        stand(sim, robot, LOOP_S, true);
        FieldSim.Piece p = onTheLeftCorner(sim, robot, sim.main.design.intakeWidthIn / 2 + FieldSim.CORNER_WHEEL_IN + 0.5);
        stand(sim, robot, 1.0, true);
        assertNull(p.rakedBy);
        assertEquals(0, sim.main.stored.size());
    }

    /** Master's rules: nothing is raked with the intake off, and a wheel that stops lets its piece go where it is. */
    @Test
    public void theWheelsStopWithTheIntakeAndLetGo() throws Exception {
        FieldSim sim = field(true);
        Pose robot = new Pose(40.0, 30.0, 0.0);
        stand(sim, robot, LOOP_S, false);
        FieldSim.Piece p = onTheLeftCorner(sim, robot, sim.main.design.intakeWidthIn / 2 + 2.5);
        stand(sim, robot, 1.0, false);
        assertNull("intake off: not raked", p.rakedBy);

        p = onTheLeftCorner(sim, robot, sim.main.design.intakeWidthIn / 2 + 2.5);
        stand(sim, robot, 0.1, true);
        assertNotNull("intake on: on the wheel", p.rakedBy);
        stand(sim, robot, 0.1, false);
        assertNull("intake off again: let go", p.rakedBy);
        assertEquals(FieldSim.Where.FIELD, p.where);
        assertEquals(0, sim.main.stored.size());
        assertEquals(0, sim.main.onWheels());
    }

    /** G407: with 4 held the wheels do not spin, so a 5th piece at the corner stays on the tiles. */
    @Test
    public void aFullRobotRakesNothing() throws Exception {
        FieldSim sim = alongWall(true, false);
        assertEquals(4, sim.main.stored.size());
        Pose robot = new Pose(40.0, 30.0, 0.0);
        stand(sim, robot, LOOP_S, true);
        FieldSim.Piece fifth = null;
        for (FieldSim.Piece q : sim.pieces) {
            if (q.where == FieldSim.Where.FIELD && FieldSim.garden(q.x, q.y) == Alliance.BLUE) fifth = q;
        }
        assertNotNull(fifth);
        double ahead = sim.main.design.frameIn / 2 + fifth.kind.radius, aside = sim.main.design.intakeWidthIn / 2 + 2.0;
        fifth.x = robot.x() + ahead;
        fifth.y = robot.y() + aside;
        stand(sim, robot, 1.0, true);
        assertNull(fifth.rakedBy);
        assertEquals(4, sim.main.stored.size());
    }
}
