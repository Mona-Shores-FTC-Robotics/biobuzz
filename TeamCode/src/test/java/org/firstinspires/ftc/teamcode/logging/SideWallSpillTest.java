package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Do the side walls ({@code RobotAssets}' side-wall sketch) stop a TIP's spill from scattering?
 *
 * <p>The spill is {@link SpillLandingTest}'s: the red rocker loaded as at the start of a match,
 * POLLEN placed until it tips. Before the TIP a robot parks between the landing and its alliance
 * wall, facing the HIVE, at {@code x} = the landings' median. Its walls are out (24 in long) the
 * whole time, standing in for "slide them out just as the pieces land". Three versions over the
 * same TIPs: no robot, a plain 18 in robot, and the robot with walls out.
 *
 * <p>Counted 3 s after the TIP starts, per spilled piece:
 * <ul>
 *   <li><b>between the walls</b>: ahead of the robot's front and between the walls (for the other
 *       two versions, the same patch of floor);</li>
 *   <li><b>within reach</b>: within 6 in of the robot's 24 × 18 in outline with walls out;</li>
 *   <li><b>G409</b>: touched the robot before the tiles. Any of these is a foul to design out.</li>
 * </ul>
 * And per TIP, the most pieces between the walls at once, against G407's 4.
 *
 * <p>The robot and the walls are placeholders ({@code FieldSim}'s robot restitution, a 0.25 in wall),
 * so read the comparison between versions, not the counts, as the finding.
 */
public class SideWallSpillTest {

    static final int SEEDS = 20;
    /** Robot centre from the alliance wall, inches: its walls' front edges are 15 in further out. */
    static final double[] CENTRE_FROM_WALL_IN = {14, 20, 25};
    static final double ROBOT_X_IN = 60;
    static final double REACH_IN = 6;

    enum Version { NONE, PLAIN, WALLS }

    static final class Tally {
        int pieces, between, reach, g409, runs, overFour, maxBetween;
    }

    @Test
    public void wallsAgainstNoWalls() {
        FieldSim.Physics physics = HiveCalibration.current().fit();
        List<String> rows = new ArrayList<>();
        int g409 = 0;
        for (double d : CENTRE_FROM_WALL_IN) {
            for (Version v : Version.values()) {
                Tally t = new Tally();
                for (long seed = 1; seed <= SEEDS; seed++) run(physics, seed, d, v, t);
                if (v == Version.WALLS) g409 += t.g409;
                rows.add(String.format(Locale.ROOT,
                        "SIDEWALLS centre %2.0f in from wall, %-5s: %3d pieces; between walls %3.0f%%;"
                                + " within %.0f in %3.0f%%; G409 touches %d; most between at once %d (TIPs over 4: %d/%d)",
                        d, v, t.pieces, 100.0 * t.between / t.pieces, REACH_IN, 100.0 * t.reach / t.pieces,
                        t.g409, t.maxBetween, t.overFour, t.runs));
                assertTrue("no TIP happened", t.runs > 0);
            }
        }
        for (String r : rows) System.out.println(r);
        System.out.println("SIDEWALLS G409 touches with walls out, all positions: " + g409);
    }

    /**
     * The walls do what the sketch says, rolling one piece at a time at a robot facing +x: POLLEN
     * from outside rolls in under a flap, POLLEN inside cannot roll out, NECTAR cannot get in.
     */
    @Test
    public void flapsLetPollenInNotOutAndKeepNectarOut() {
        assertTrue("POLLEN rolls in", rollAtLeftWall("Pollen", 14, -40) < 9);
        assertTrue("POLLEN stays in", rollAtLeftWall("Pollen", 4, 40) < 9);
        assertTrue("NECTAR stays out", rollAtLeftWall("Red Nectar", 14, -40) > 9);
    }

    /**
     * Rolls a piece across the robot's left wall, ahead of its front, starting {@code startLeftIn}
     * to the robot's left at {@code vy} in/s (negative = toward the robot). Returns where it ends, in
     * inches to the robot's left.
     */
    private static double rollAtLeftWall(String kind, double startLeftIn, double vy) {
        double rx = 70, ry = 25, ahead = 12, r = kind.equals("Pollen") ? FieldSim.POLLEN_RADIUS_IN : FieldSim.NECTAR_RADIUS_IN;
        List<HiveAssets.StagedPiece> one = new ArrayList<>();
        one.add(new HiveAssets.StagedPiece(kind, "floor", rx + ahead, ry + startLeftIn, r));
        FieldSim sim = new FieldSim(one, 1);
        addSideWalls(sim.main);
        sim.setRobot(rx, ry, 0, 0, 0, 0, false);
        FieldSim.Piece p = sim.pieces.get(sim.pieces.size() - 1);
        p.vy = vy;
        for (int i = 0; i < 200; i++) sim.step(0.01);
        return p.y - ry;
    }

    /** One TIP with the robot as {@code v}, centred {@code d} from the wall the spill falls toward. */
    static void run(FieldSim.Physics physics, long seed, double d, Version v, Tally t) {
        FieldSim sim = new FieldSim(new ArrayList<>(), seed, physics);
        sim.red.locked = true;
        for (int i = 0; i < HiveCalibration.NECTAR_AT_MATCH_START; i++) {
            sim.placeInRaisedCell(sim.red, FieldSim.Kind.RED_NECTAR);
            HiveCalibration.settle(sim);
        }
        sim.red.locked = false;
        // The raised CELL goes down: the spill falls toward the wall at its end.
        boolean towardHighY = sim.red.raisedEnd() > 0;
        double wallY = towardHighY ? 2 * FieldSim.CENTRE_IN : 0;
        double out = towardHighY ? -1 : 1;
        double rx = ROBOT_X_IN, ry = wallY + out * d, heading = out * Math.PI / 2;
        if (v != Version.NONE) {
            if (v == Version.WALLS) addSideWalls(sim.main);
            sim.setRobot(rx, ry, heading, 0, 0, 0, false);
        }

        for (int k = 0; k < 12 && sim.red.tipsStarted == 0; k++) {
            sim.placeInRaisedCell(sim.red, FieldSim.Kind.POLLEN);
            for (int i = 0; i < 75 && sim.red.tipsStarted == 0; i++) sim.step(HiveCalibration.LOOP_S);
        }
        if (sim.red.tipsStarted == 0) return;
        List<FieldSim.Piece> spill = new ArrayList<>();
        for (FieldSim.Piece p : sim.pieces) {
            if (p.where == FieldSim.Where.FIELD && p.cell != null && p.cell.alliance() == sim.red.alliance) spill.add(p);
        }
        int most = 0;
        for (int i = 0; i < 300; i++) {
            sim.step(0.01);
            int now = 0;
            for (FieldSim.Piece p : spill) if (p.cell == null && betweenWalls(p, rx, ry, heading)) now++;
            most = Math.max(most, now);
        }
        t.runs++;
        t.maxBetween = Math.max(t.maxBetween, most);
        if (most > 4) t.overFour++;
        for (FieldSim.Piece p : spill) {
            if (p.cell != null) continue; // stayed in the CELL
            t.pieces++;
            if (betweenWalls(p, rx, ry, heading)) t.between++;
            if (withinReach(p, rx, ry, heading)) t.reach++;
            if (p.robotBeforeTile) t.g409++;
        }
    }

    /** The robot's walls, slid all the way out ({@link RobotDesign#sideWallsSlideIn}). */
    static void addSideWalls(FieldSim.Bot bot) {
        bot.design = bot.design.copy(bot.design.name + ", side walls");
        bot.design.sideWallsSlideIn = RobotAssets.WALL_SLIDE_IN;
        bot.wallsOut = 1;
    }

    /** {x, y} of a piece in the robot's frame. */
    private static double[] local(FieldSim.Piece p, double rx, double ry, double heading) {
        double c = Math.cos(heading), s = Math.sin(heading);
        return new double[] {(p.x - rx) * c + (p.y - ry) * s, -(p.x - rx) * s + (p.y - ry) * c};
    }

    /** Ahead of the robot's front and between where the walls are when out. */
    static boolean betweenWalls(FieldSim.Piece p, double rx, double ry, double heading) {
        double[] l = local(p, rx, ry, heading);
        double half = RobotAssets.CHASSIS_SIZE_IN / 2;
        return l[0] > half && l[0] < half + RobotAssets.WALL_SLIDE_IN
                && Math.abs(l[1]) < half - RobotAssets.WALL_THICKNESS_IN;
    }

    /** Within {@link #REACH_IN} of the robot's outline with walls out. */
    static boolean withinReach(FieldSim.Piece p, double rx, double ry, double heading) {
        double[] l = local(p, rx, ry, heading);
        double half = RobotAssets.CHASSIS_SIZE_IN / 2;
        return l[0] > -half - REACH_IN && l[0] < half + RobotAssets.WALL_SLIDE_IN + REACH_IN
                && Math.abs(l[1]) < half + REACH_IN;
    }
}
