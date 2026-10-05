package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Does a smaller body with passive flaps catch a TIP's spill better than the 18 in robot (mentor,
 * 5 Oct 2026)? Each shape parks as in {@link SideWallSpillTest#howCloseCanTheLongUPark}: centred on
 * the red CELL's axis, facing the HIVE, its front-most point at each distance from the wall, over
 * the same 200 TIPs from 8 POLLEN and from the match-start load.
 *
 * <p>Per position: <b>kept</b>, the share of the spill in the same patch of floor in front of the
 * robot 3 s after the TIP starts ({@link SideWallSpillTest#GATHER_BEHIND_NOSE_IN} behind its front-most
 * point to {@link SideWallSpillTest#GATHER_AHEAD_IN} past it, 24 in wide, as the baselines); the TIPs
 * with a G409 touch, split into those where the frame (or its walls) touched a piece and those where
 * only a flap did; and <b>inside</b>, the most spilled pieces between the flaps or walls at once,
 * against G407's 4.
 *
 * <p>The flaps are placeholders like the rest of the robot (4 in tall, 0.25 in thick, the robot's
 * restitution), so read the comparison between shapes, not the counts.
 */
public class BodyShapeSpillTest {

    /**
     * A shape that fits R105's 18 × 24 in once the match starts. {@code slide}: side walls slid that
     * far forward (the long U); {@code out}/{@code ahead}: each flap's free end from its hinge at a
     * front corner.
     */
    static final class Body {
        final String name;
        final double length, width, slide, out, ahead;
        /** Flap height: {@link RobotDesign#flapHeightIn} unless set. */
        double flapHeight = Double.NaN;

        Body(String name, double length, double width, double slide, double out, double ahead) {
            this.name = name;
            this.length = length;
            this.width = width;
            this.slide = slide;
            this.out = out;
            this.ahead = ahead;
        }

        /** The same shape with flaps {@code height} tall. */
        Body low(double height) {
            Body b = new Body(name + String.format(Locale.ROOT, ", %.1f in tall", height), length, width, slide, out, ahead);
            b.flapHeight = height;
            return b;
        }

        RobotDesign design() {
            RobotDesign d = RobotDesign.standard().copy("turret, " + name);
            d.frameIn = length;
            d.frameWidthIn = width;
            d.intakeWidthIn = Math.min(d.intakeWidthIn, width);
            d.sideWallsSlideIn = slide;
            d.flapOutIn = out;
            d.flapForwardIn = ahead;
            if (!Double.isNaN(flapHeight)) d.flapHeightIn = flapHeight;
            return d.checked();
        }

        /** How far the front-most point is past the frame's front face. */
        double reach() {
            return Math.max(slide, ahead);
        }

        double[] footprint() {
            return design().footprintIn();
        }
    }

    static final Body PLAIN = new Body("plain 18", 18, 18, 0, 0, 0);
    static final Body LONG_U = new Body("long U", 18, 18, RobotAssets.WALL_SLIDE_IN, 0, 0);
    /** The smaller body alone, to tell what the flaps add from what the size does. */
    static final Body PLAIN_16 = new Body("plain 16", 16, 16, 0, 0, 0);
    /** (a) 16 × 16 with flaps 3 in out and 2 in forward: 22 × 18 in. */
    static final Body FLAPS_16 = new Body("16 + flaps 3 out 2 fwd", 16, 16, 0, 3, 2);
    /** (b) 16 × 16 with the flaps as wide as R105 allows: 24 × 18 in. */
    static final Body FLAPS_16_WIDE = new Body("16 + flaps 4 out 2 fwd", 16, 16, 0, 4, 2);
    /** (b) 15 × 15, out and forward as far as R105 allows: 24 × 18 in. */
    static final Body FLAPS_15 = new Body("15 + flaps 4.5 out 3 fwd", 15, 15, 0, 4.5, 3);
    /** (c) A full-width 18 in frame cut to 15 in long so 45° flaps fit: 24 × 18 in. */
    static final Body SHORT_18 = new Body("18 wide x 15 long + flaps 3 out 3 fwd", 15, 18, 0, 3, 3);
    /** (c) The other way round: a 16 in body with long flaps, 18 in across and 24 long (a flared long U). */
    static final Body FLARED_16 = new Body("16 + flaps 1 out 8 fwd", 16, 16, 0, 1, 8);

    static final Body[] BODIES = {PLAIN, LONG_U, PLAIN_16, FLAPS_16, FLAPS_16_WIDE, FLAPS_15, SHORT_18, FLARED_16};

    /**
     * Low guides (mentor, 5 Oct 2026, from a photo of a "floating intake": short wedges at the ends of
     * a narrow roller steer pieces in). {@link #LOW_GUIDE_IN} is just over NECTAR's radius, so a
     * rolling piece still meets it; the question is whether falling pieces touch it less.
     */
    static final double LOW_GUIDE_IN = 2.5;
    /** The 18 in robot with low ramps straight forward from its corners: a low long U with no doors. */
    static final Body RAMPS_18 = new Body("18 + ramps 0 out 6 fwd", 18, 18, 0, 0, 6).low(LOW_GUIDE_IN);
    static final Body[] LOW_BODIES = {FLAPS_16.low(LOW_GUIDE_IN), FLAPS_16_WIDE.low(LOW_GUIDE_IN),
            FLAPS_15.low(LOW_GUIDE_IN), SHORT_18.low(LOW_GUIDE_IN), FLARED_16.low(LOW_GUIDE_IN), RAMPS_18};

    static final int TIPS = SideWallSpillTest.PLAIN_PARK_TIPS;
    /** Front-most point from the wall, inches: around both baselines' last clean spot (35, 36). */
    static final double NOSE_FROM_IN = 31, NOSE_TO_IN = 39;

    /** One TIP's numbers. */
    static final class Run {
        int pieces, kept, touched, frame, flapOnly, mostInside;
    }

    /** All the TIPs at one position. */
    static final class Sweep {
        int pieces, kept, tipsTouched, tipsFrame, tipsFlapOnly, flapOnlyPieces, mostInside, overFour;

        void add(Run r) {
            pieces += r.pieces;
            kept += r.kept;
            if (r.touched > 0) tipsTouched++;
            if (r.frame > 0) tipsFrame++;
            if (r.touched > 0 && r.frame == 0) tipsFlapOnly++;
            flapOnlyPieces += r.flapOnly;
            mostInside = Math.max(mostInside, r.mostInside);
            if (r.mostInside > 4) overFour++;
        }

        double keptPercent() {
            return 100.0 * kept / pieces;
        }
    }

    /** Every R105 shape here fits 18 × 24, and one past it doesn't. */
    @Test
    public void everyShapeFitsR105() {
        List<Body> all = new ArrayList<>(java.util.Arrays.asList(BODIES));
        all.addAll(java.util.Arrays.asList(LOW_BODIES));
        for (Body b : all) {
            double[] f = b.footprint();
            assertTrue(b.name, (f[0] <= 24 && f[1] <= 18) || (f[0] <= 18 && f[1] <= 24));
        }
        Body tooFar = new Body("16 + flaps at 45°, 3 out 3 fwd", 16, 16, 0, 3, 3);  // 19 long, 22 across
        boolean threw = false;
        try {
            tooFar.design();
        } catch (IllegalArgumentException e) {
            threw = true;
        }
        assertTrue("a 16 in body's flaps can't reach 3 in out and 3 in forward", threw);
        assertEquals(22, FLAPS_16.footprint()[1], 1e-9);
        assertEquals(18, FLAPS_16.footprint()[0], 1e-9);
    }

    /** A piece dropped onto a flap from above counts as a flap touch, not a frame touch. */
    @Test
    public void aFlapTouchIsCountedApart() {
        double rx = 70, ry = 25;
        RobotDesign d = FLAPS_16.design();
        // Above the middle of the left flap: 8 + 1 in ahead of the centre, 8 + 1.5 in to the left.
        List<HiveAssets.StagedPiece> one = new ArrayList<>();
        one.add(new HiveAssets.StagedPiece("Pollen", "floor", rx + 9, ry + 9.5, FieldSim.POLLEN_RADIUS_IN));
        FieldSim sim = new FieldSim(one, 1);
        sim.main.design = d;
        sim.setRobot(rx, ry, 0, 0, 0, 0, false);
        FieldSim.Piece p = sim.pieces.get(sim.pieces.size() - 1);
        p.z = 12;
        p.touchedTile = false;
        for (int i = 0; i < 100; i++) sim.step(0.01);
        assertTrue("touched before the tiles", p.robotBeforeTile);
        assertTrue("on the flap", p.flapBeforeTile);
        assertFalse("not the frame", p.frameBeforeTile);
    }

    /**
     * The sweep: each shape at each front-most-point distance, both loads. Prints one line per
     * position, then each shape's closest clean spot (no G409 touch of any kind in either load) and
     * its closest spot with no frame touch (if flap touches weren't called).
     */
    @Test
    public void howCloseCanEachShapePark() {
        sweep(BODIES);
    }

    /** As {@link #howCloseCanEachShapePark}, the flap shapes with low guides instead of 4 in flaps. */
    @Test
    public void howCloseCanLowGuidesPark() {
        sweep(LOW_BODIES);
    }

    static void sweep(Body[] bodies) {
        FieldSim.Physics physics = HiveCalibration.current().fit();
        int[] loads = {0, HiveCalibration.NECTAR_AT_MATCH_START};
        List<double[]> jobs = new ArrayList<>();  // {body, nose}
        for (int b = 0; b < bodies.length; b++) {
            for (double nose = NOSE_FROM_IN; nose <= NOSE_TO_IN; nose += 1) jobs.add(new double[] {b, nose});
        }
        // Each TIP is its own seeded FieldSim, so positions run side by side and come back in order.
        List<Sweep[]> results = jobs.parallelStream().map(j -> {
            Sweep[] s = {new Sweep(), new Sweep()};
            for (int k = 0; k < 2; k++) {
                for (long seed = 1; seed <= TIPS; seed++) s[k].add(run(physics, seed, bodies[(int) j[0]], j[1], loads[k]));
            }
            return s;
        }).collect(Collectors.toList());
        List<String> best = new ArrayList<>();
        String clean = null, frameClean = null;
        for (int i = 0; i < jobs.size(); i++) {
            Body b = bodies[(int) jobs.get(i)[0]];
            double nose = jobs.get(i)[1];
            Sweep[] s = results.get(i);
            System.out.println(String.format(Locale.ROOT,
                    "BODY %-38s nose %2.0f (face %4.1f): 8 POLLEN kept %3.0f%%, G409 TIPs %3d (frame %3d, flap only %3d), inside max %d (over 4: %3d)"
                            + " | match start kept %3.0f%%, G409 TIPs %3d (frame %3d, flap only %3d), inside max %d (over 4: %3d)",
                    b.name, nose, nose - b.reach(),
                    s[0].keptPercent(), s[0].tipsTouched, s[0].tipsFrame, s[0].tipsFlapOnly, s[0].mostInside, s[0].overFour,
                    s[1].keptPercent(), s[1].tipsTouched, s[1].tipsFrame, s[1].tipsFlapOnly, s[1].mostInside, s[1].overFour));
            String summary = String.format(Locale.ROOT,
                    "nose %2.0f: kept %2.0f%% / %2.0f%%, G409 TIPs %d / %d (frame %d / %d), inside max %d / %d (over 4 in %d / %d TIPs)",
                    nose, s[0].keptPercent(), s[1].keptPercent(), s[0].tipsTouched, s[1].tipsTouched,
                    s[0].tipsFrame, s[1].tipsFrame, s[0].mostInside, s[1].mostInside, s[0].overFour, s[1].overFour);
            if (s[0].tipsTouched == 0 && s[1].tipsTouched == 0) clean = summary;
            if (s[0].tipsFrame == 0 && s[1].tipsFrame == 0) frameClean = summary;
            if (i == jobs.size() - 1 || jobs.get(i + 1)[0] != jobs.get(i)[0]) {
                double[] f = b.footprint();
                best.add(String.format(Locale.ROOT, "BODYBEST %-38s %4.1f x %4.1f in | no touch: %s | no frame touch: %s",
                        b.name, f[0], f[1], clean, frameClean));
                clean = frameClean = null;
            }
        }
        for (String r : best) System.out.println(r);
    }

    /** One TIP with the robot shaped as {@code b}, its front-most point {@code nose} in from the wall. */
    static Run run(FieldSim.Physics physics, long seed, Body b, double nose, int nectar) {
        FieldSim sim = new FieldSim(new ArrayList<>(), seed, physics);
        sim.red.locked = true;
        for (int i = 0; i < nectar; i++) {
            sim.placeInRaisedCell(sim.red, FieldSim.Kind.RED_NECTAR);
            HiveCalibration.settle(sim);
        }
        sim.red.locked = false;
        boolean towardHighY = sim.red.raisedEnd() > 0;
        double wallY = towardHighY ? 2 * FieldSim.CENTRE_IN : 0;
        double out = towardHighY ? -1 : 1;
        double half = b.length / 2, halfWidth = b.width / 2;
        double rx = FieldSim.RED_HIVE_X_IN, ry = wallY + out * (nose - b.reach() - half), heading = out * Math.PI / 2;
        FieldSim.Bot bot = sim.main;
        bot.design = b.design();
        bot.wallsOut = 0;  // the walls go out as the TIP starts, as in SideWallSpillTest; the flaps are out all match
        sim.setRobot(rx, ry, heading, 0, 0, 0, false);
        for (int k = 0; k < 12 && sim.red.tipsStarted == 0; k++) {
            sim.placeInRaisedCell(sim.red, FieldSim.Kind.POLLEN);
            for (int i = 0; i < 75 && sim.red.tipsStarted == 0; i++) sim.step(HiveCalibration.LOOP_S);
        }
        Run r = new Run();
        if (sim.red.tipsStarted == 0) return r;
        List<FieldSim.Piece> spill = new ArrayList<>();
        for (FieldSim.Piece p : sim.pieces) {
            if (p.where == FieldSim.Where.FIELD && p.cell != null && p.cell.alliance() == sim.red.alliance) spill.add(p);
        }
        double c = Math.cos(heading), s = Math.sin(heading);
        for (int i = 0; i < 300; i++) {
            if (b.slide > 0) bot.wallsOut = Math.min(1, bot.wallsOut + 0.01 / bot.design.sideWallsTravelS);
            sim.setRobot(rx, ry, heading, 0, 0, 0, false);
            sim.step(0.01);
            int inside = 0;
            for (FieldSim.Piece p : spill) {
                if (p.cell != null) continue;
                double lx = (p.x - rx) * c + (p.y - ry) * s - half, ly = Math.abs(-(p.x - rx) * s + (p.y - ry) * c);
                if (lx <= 0 || lx >= b.reach()) continue;
                // Between the walls, or between the flaps (which widen from the frame's corners).
                double across = b.slide > 0 ? halfWidth : halfWidth + lx * b.out / b.ahead;
                if (ly < across) inside++;
            }
            r.mostInside = Math.max(r.mostInside, inside);
        }
        for (FieldSim.Piece p : spill) {
            if (p.cell != null) continue;
            r.pieces++;
            double lx = (p.x - rx) * c + (p.y - ry) * s, ly = -(p.x - rx) * s + (p.y - ry) * c;
            double fromNose = lx - (half + b.reach());
            if (fromNose > -SideWallSpillTest.GATHER_BEHIND_NOSE_IN && fromNose < SideWallSpillTest.GATHER_AHEAD_IN
                    && Math.abs(ly) < SideWallSpillTest.GATHER_HALF_WIDTH_IN) r.kept++;
            if (p.robotBeforeTile) r.touched++;
            if (p.frameBeforeTile) r.frame++;
            if (p.flapBeforeTile && !p.frameBeforeTile) r.flapOnly++;
        }
        return r;
    }
}
