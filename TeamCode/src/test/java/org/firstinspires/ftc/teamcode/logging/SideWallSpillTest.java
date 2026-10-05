package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

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
     * Which shape inside R105's 18 × 24 in catches a TIP's spill best (mentor, 5 Oct 2026): the long U
     * (walls slide 6 in forward, 18 in mouth), the wide U (6 in wings at the front corners swing 3 in
     * out each side, 24 in mouth, nothing forward), or no walls; each standing still, or creeping 8 in
     * forward once the spill is on the tiles (G409 lets the robot touch pieces then). Walls go out as
     * the TIP starts.
     */
    enum Shape {
        NONE(0, 0, 18), LONG_U(RobotAssets.WALL_SLIDE_IN, 0, 18), WIDE_U(0, 3, 6);
        final double slide, out, length;
        Shape(double slide, double out, double length) {
            this.slide = slide;
            this.out = out;
            this.length = length;
        }
    }

    /** Every shape parks with its front-most point this far from the wall, short of the landing. */
    static final double NOSE_FROM_WALL_IN = 35;
    static final double CREEP_IN = 8, CREEP_IN_PER_S = 12;
    /**
     * "Gathered", 3 s after the TIP starts: in a 24 in wide zone from 15 in behind the robot's
     * front-most point to 8 in past it, the same patch of floor for every shape.
     */
    static final double GATHER_AHEAD_IN = 8, GATHER_BEHIND_NOSE_IN = 15, GATHER_HALF_WIDTH_IN = 12;
    static final int SHAPE_SEEDS = 40;

    @Test
    public void whichShapeGathersTheSpill() {
        FieldSim.Physics physics = HiveCalibration.current().fit();
        List<String> rows = new ArrayList<>();
        for (Shape shape : Shape.values()) {
            for (boolean creep : new boolean[] {false, true}) {
                int pieces = 0, gathered = 0, g409 = 0, overFour = 0, between = 0;
                for (long seed = 1; seed <= SHAPE_SEEDS; seed++) {
                    int[] r = shapeRun(physics, seed, shape, creep);
                    pieces += r[0];
                    gathered += r[1];
                    g409 += r[2];
                    between = Math.max(between, r[3]);
                    if (r[3] > 4) overFour++;
                }
                rows.add(String.format(Locale.ROOT,
                        "SHAPES %-6s %-11s: %3d pieces; gathered %3.0f%% (%.2f a TIP); G409 touches %d; most inside the U %d (TIPs over 4: %d)",
                        shape, creep ? "creep 8 in" : "stand", pieces, 100.0 * gathered / pieces,
                        (double) gathered / SHAPE_SEEDS, g409, between, overFour));
            }
        }
        for (String r : rows) System.out.println(r);
    }

    /**
     * How far forward to park (mentor, 5 Oct 2026: "the front face at the landing, the arms past
     * it"): the long U and no walls, standing, at each nose distance from the wall. Where the spill
     * lands, falling pieces hit the robot or its arms before the tiles (G409).
     */
    @Test
    public void howFarForwardToPark() {
        FieldSim.Physics physics = HiveCalibration.current().fit();
        for (Shape shape : new Shape[] {Shape.NONE, Shape.LONG_U}) {
            for (double nose : new double[] {35, 38, 41, 44, 47, 50, 53}) {
                int pieces = 0, gathered = 0, g409 = 0, runsTouched = 0, overFour = 0;
                for (long seed = 1; seed <= SHAPE_SEEDS; seed++) {
                    int[] r = shapeRun(physics, seed, shape, false, nose);
                    pieces += r[0];
                    gathered += r[1];
                    g409 += r[2];
                    if (r[2] > 0) runsTouched++;
                    if (r[3] > 4) overFour++;
                }
                System.out.printf(Locale.ROOT,
                        "PARK %-6s nose %2.0f in (front face %2.0f): gathered %3.0f%% (%.2f a TIP); G409 %.2f a TIP, in %d of %d; over 4 inside: %d%n",
                        shape, nose, nose - shape.slide, 100.0 * gathered / pieces, (double) gathered / SHAPE_SEEDS,
                        (double) g409 / SHAPE_SEEDS, runsTouched, SHAPE_SEEDS, overFour);
            }
        }
    }

    /**
     * One TIP for a shape: {spilled pieces, gathered, G409 touches, most pieces between the arms at
     * once}.
     */
    static int[] shapeRun(FieldSim.Physics physics, long seed, Shape shape, boolean creep) {
        return shapeRun(physics, seed, shape, creep, NOSE_FROM_WALL_IN);
    }

    /** As above, with the robot's front-most point {@code nose} in from the wall. */
    static int[] shapeRun(FieldSim.Physics physics, long seed, Shape shape, boolean creep, double nose) {
        FieldSim sim = new FieldSim(new ArrayList<>(), seed, physics);
        sim.red.locked = true;
        for (int i = 0; i < HiveCalibration.NECTAR_AT_MATCH_START; i++) {
            sim.placeInRaisedCell(sim.red, FieldSim.Kind.RED_NECTAR);
            HiveCalibration.settle(sim);
        }
        sim.red.locked = false;
        boolean towardHighY = sim.red.raisedEnd() > 0;
        double wallY = towardHighY ? 2 * FieldSim.CENTRE_IN : 0;
        double out = towardHighY ? -1 : 1;
        double half = RobotAssets.CHASSIS_SIZE_IN / 2;
        double d = nose - half - shape.slide;
        double rx = ROBOT_X_IN, ry = wallY + out * d, heading = out * Math.PI / 2;
        FieldSim.Bot bot = sim.main;
        if (shape != Shape.NONE) {
            bot.design = bot.design.copy(bot.design.name + ", " + shape);
            bot.design.sideWallsSlideIn = shape.slide;
            bot.design.sideWallsOutIn = shape.out;
            bot.design.sideWallsLengthIn = shape.length;
            bot.design.checked();
            bot.wallsOut = 0;
        }
        sim.setRobot(rx, ry, heading, 0, 0, 0, false);
        for (int k = 0; k < 12 && sim.red.tipsStarted == 0; k++) {
            sim.placeInRaisedCell(sim.red, FieldSim.Kind.POLLEN);
            for (int i = 0; i < 75 && sim.red.tipsStarted == 0; i++) sim.step(HiveCalibration.LOOP_S);
        }
        if (sim.red.tipsStarted == 0) return new int[4];
        List<FieldSim.Piece> spill = new ArrayList<>();
        for (FieldSim.Piece p : sim.pieces) {
            if (p.where == FieldSim.Where.FIELD && p.cell != null && p.cell.alliance() == sim.red.alliance) spill.add(p);
        }
        double dt = 0.01, moved = 0;
        int most = 0;
        for (int i = 0; i < 300; i++) {
            double t = i * dt;
            if (shape != Shape.NONE) bot.wallsOut = Math.min(1, bot.wallsOut + dt / bot.design.sideWallsTravelS);
            double v = creep && t >= 1.5 && moved < CREEP_IN ? CREEP_IN_PER_S : 0;
            moved += v * dt;
            sim.setRobot(rx + Math.cos(heading) * moved, ry + Math.sin(heading) * moved, heading,
                    v * Math.cos(heading), v * Math.sin(heading), 0, false);
            sim.step(dt);
            int inside = 0;
            for (FieldSim.Piece p : spill) {
                if (p.cell != null || shape == Shape.NONE) continue;
                double[] l = local(p, rx + Math.cos(heading) * moved, ry + Math.sin(heading) * moved, heading);
                double reach = half + shape.slide, across = half + shape.out;
                if (l[0] > half - shape.length && l[0] < reach && Math.abs(l[1]) < across) {
                    if (l[0] > half || Math.abs(l[1]) > half) inside++;
                }
            }
            most = Math.max(most, inside);
        }
        double fx = rx + Math.cos(heading) * moved, fy = ry + Math.sin(heading) * moved;
        int n = 0, gathered = 0, g409 = 0;
        for (FieldSim.Piece p : spill) {
            if (p.cell != null) continue;
            n++;
            double[] l = local(p, fx, fy, heading);
            double fromNose = l[0] - (half + shape.slide);  // the same zone for every shape: from its nose
            if (fromNose > -GATHER_BEHIND_NOSE_IN && fromNose < GATHER_AHEAD_IN && Math.abs(l[1]) < GATHER_HALF_WIDTH_IN) gathered++;
            if (p.robotBeforeTile) g409++;
        }
        return new int[] {n, gathered, g409, most};
    }

    /** Where the demo robot parks: centred this far from the wall, where the walls help most. */
    static final double DEMO_CENTRE_FROM_WALL_IN = 20;

    /**
     * Two logs of the same TIP to watch in AdvantageScope, one with side walls and one without, in
     * {@code build/sim-logs}: {@code side-walls-demo-walls.wpilog} and {@code side-walls-demo-plain.wpilog}.
     * The robot parks {@value #DEMO_CENTRE_FROM_WALL_IN} in from the wall facing the HIVE, short of
     * where the spill lands; its walls slide out as the TIP starts. The TIP is the seed of 20 where the walls keep the most more
     * pieces within reach than no walls do. Open with {@code sim-review/advantagescope-layout-walls.json}.
     */
    @Test
    public void writesWallDemoLogs() throws IOException {
        FieldSim.Physics physics = HiveCalibration.current().fit();
        long best = 1;
        int bestGain = Integer.MIN_VALUE;
        for (long seed = 1; seed <= 20; seed++) {
            int gain = demo(physics, seed, Version.WALLS, null) - demo(physics, seed, Version.PLAIN, null);
            if (gain > bestGain) {
                bestGain = gain;
                best = seed;
            }
        }
        File dir = TeamCodeDir.simLogs();
        int walls = demo(physics, best, Version.WALLS, new File(dir, "side-walls-demo-walls.wpilog"));
        int plain = demo(physics, best, Version.PLAIN, new File(dir, "side-walls-demo-plain.wpilog"));
        System.out.printf(Locale.ROOT, "SIDEWALLS demo seed %d: within %.0f in of the robot 3 s after the TIP, walls %d, plain %d%n",
                best, REACH_IN, walls, plain);
        assertTrue("the walls should keep more of the spill within reach", walls >= plain);
    }

    /**
     * One TIP with the robot parked as {@code v}, its walls run as on the robot. Logged to
     * {@code file} unless it is null. Returns the spilled pieces within reach 3 s after the TIP starts.
     */
    static int demo(FieldSim.Physics physics, long seed, Version v, File file) throws IOException {
        FieldSim sim = new FieldSim(new ArrayList<>(), seed, physics);
        sim.red.locked = true;
        for (int i = 0; i < HiveCalibration.NECTAR_AT_MATCH_START; i++) {
            sim.placeInRaisedCell(sim.red, FieldSim.Kind.RED_NECTAR);
            HiveCalibration.settle(sim);
        }
        sim.red.locked = false;
        boolean towardHighY = sim.red.raisedEnd() > 0;
        double wallY = towardHighY ? 2 * FieldSim.CENTRE_IN : 0;
        double out = towardHighY ? -1 : 1;
        double rx = ROBOT_X_IN, ry = wallY + out * DEMO_CENTRE_FROM_WALL_IN, heading = out * Math.PI / 2;
        if (v == Version.WALLS) {
            addSideWalls(sim.main);
            sim.main.wallsOut = 0;
            // Out as soon as the TIP starts: the spill scatters the moment it lands, so walls that wait
            // for it to land (RobotDesign#sideWallsDeployS 1.5 s) catch no more than no walls. Parked
            // short of the landing, they stop pieces that have hit the tiles and touch none in the air.
            sim.main.design.sideWallsDeployS = 0;
        }
        sim.setRobot(rx, ry, heading, 0, 0, 0, false);

        WpiLog log = file == null ? null : open(file, v);
        Demo d = new Demo(sim, log, v, rx, ry, heading);
        // The same steps as run(): POLLEN one at a time until the TIP, then 3 s at 0.01 s.
        for (int k = 0; k < 12 && sim.red.tipsStarted == 0; k++) {
            sim.placeInRaisedCell(sim.red, FieldSim.Kind.POLLEN);
            for (int i = 0; i < 75 && sim.red.tipsStarted == 0; i++) d.step(HiveCalibration.LOOP_S);
        }
        List<FieldSim.Piece> spill = new ArrayList<>();
        if (sim.red.tipsStarted == 0) return 0;
        for (FieldSim.Piece p : sim.pieces) {
            if (p.where == FieldSim.Where.FIELD && p.cell != null && p.cell.alliance() == sim.red.alliance) spill.add(p);
        }
        d.event("our CELL starts to TIP");
        double tipAt = d.t;
        for (int i = 0; i < 300; i++) {
            if (v == Version.WALLS && d.t - tipAt >= sim.main.design.sideWallsDeployS) {
                if (sim.main.wallsOut == 0) d.event("side walls out: our CELL started to TIP");
                sim.main.wallsOut = Math.min(1, sim.main.wallsOut + 0.01 / sim.main.design.sideWallsTravelS);
            }
            d.step(0.01);
        }
        double t = d.t;
        int near = 0;
        for (FieldSim.Piece p : spill) if (p.cell == null && withinReach(p, rx, ry, heading)) near++;
        if (log != null) {
            log.putEvent(String.format(Locale.ROOT, "3 s after the TIP: %d of %d spilled pieces within %.0f in of the robot",
                    near, spill.size(), REACH_IN), us(t));
            log.close();
        }
        return near;
    }

    /** Steps a demo and logs what AdvantageScope draws, about 50 times a second. */
    private static final class Demo {
        final FieldSim sim;
        final WpiLog log;
        final Version v;
        final double rx, ry, heading;
        final FieldSimLog field = new FieldSimLog();
        double t, lastLogged = -1;

        Demo(FieldSim sim, WpiLog log, Version v, double rx, double ry, double heading) {
            this.sim = sim;
            this.log = log;
            this.v = v;
            this.rx = rx;
            this.ry = ry;
            this.heading = heading;
        }

        void step(double dt) throws IOException {
            sim.step(dt);
            t += dt;
            if (log == null || t - lastLogged < 0.019) return;
            lastLogged = t;
            FieldRobot.slot(0).putPose(log, rx, ry, heading, us(t));
            double out = v == Version.WALLS ? sim.main.wallsOut : 0;
            double x = out * RobotAssets.WALL_SLIDE_IN * AdvantageScopeFrame.METERS_PER_INCH;
            log.put("/SideWalls/Out", out, us(t));
            log.putPose3dArray("/SideWalls/Components", new double[] {x, 0, 0, 1, 0, 0, 0, x, 0, 0, 1, 0, 0, 0}, us(t));
            field.write(log, sim, us(t));
            for (String e : sim.drainEvents()) log.putEvent("sim: " + e, us(t));
        }

        void event(String text) throws IOException {
            if (log != null) log.putEvent(text, us(t));
        }
    }

    private static long us(double t) {
        return Math.round(t * 1e6);
    }

    private static WpiLog open(File file, Version v) throws IOException {
        file.getParentFile().mkdirs();
        WpiLog log = new WpiLog(new WpiLogWriter(new java.io.BufferedOutputStream(new java.io.FileOutputStream(file), 1 << 16),
                "BIOBUZZ side walls demo"));
        log.putMetadata("Generator", "SideWallSpillTest (TeamCode test sources)");
        log.putMetadata("PoseFrame", AdvantageScopeFrame.DESCRIPTION);
        log.putMetadata("Note", v == Version.WALLS
                ? "Robot parked facing the HIVE, side walls out once the spill has landed"
                : "Robot parked facing the HIVE, no side walls (compare side-walls-demo-walls)");
        FieldSimLog.putMetadata(log, HiveCalibration.current());
        FieldSimLog.putHiveStructure(log);
        log.put(AdvantageScopeKeys.ALLIANCE_STATION, AdvantageScopeKeys.allianceStation(true, 1), 0);
        return log;
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
