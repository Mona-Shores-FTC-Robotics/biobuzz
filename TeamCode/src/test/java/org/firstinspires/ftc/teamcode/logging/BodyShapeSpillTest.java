package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
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

    static final int TIPS = SideWallSpillTest.PLAIN_PARK_TIPS;
    /** Front-most point from the wall, inches: around both baselines' last clean spot (35, 36). */
    static final double NOSE_FROM_IN = 31, NOSE_TO_IN = 39;

    /** One TIP's numbers. */
    static final class Run {
        int pieces, kept, touched, frame, flapOnly, mostInside;
        /** Sim seconds when the TIP started and when the run ended (0 if no TIP). */
        double tipAt, endAt;
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
        List<BodyShape> all = new ArrayList<>(java.util.Arrays.asList(BodyShape.BODIES));
        all.addAll(java.util.Arrays.asList(BodyShape.LOW_BODIES));
        for (BodyShape b : all) {
            double[] f = b.footprint();
            assertTrue(b.name, (f[0] <= 24 && f[1] <= 18) || (f[0] <= 18 && f[1] <= 24));
        }
        BodyShape tooFar = new BodyShape("16 + flaps at 45°, 3 out 3 fwd", 16, 16, 0, 3, 3);  // 19 long, 22 across
        boolean threw = false;
        try {
            tooFar.design();
        } catch (IllegalArgumentException e) {
            threw = true;
        }
        assertTrue("a 16 in body's flaps can't reach 3 in out and 3 in forward", threw);
        assertEquals(22, BodyShape.FLAPS_16.footprint()[1], 1e-9);
        assertEquals(18, BodyShape.FLAPS_16.footprint()[0], 1e-9);
    }

    /** A piece dropped onto a flap from above counts as a flap touch, not a frame touch. */
    @Test
    public void aFlapTouchIsCountedApart() {
        double rx = 70, ry = 25;
        RobotDesign d = BodyShape.FLAPS_16.design();
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
        sweep(BodyShape.BODIES);
    }

    /** As {@link #howCloseCanEachShapePark}, the flap shapes with low guides instead of 4 in flaps. */
    @Test
    public void howCloseCanLowGuidesPark() {
        sweep(BodyShape.LOW_BODIES);
    }

    static void sweep(BodyShape[] bodies) {
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
            BodyShape b = bodies[(int) jobs.get(i)[0]];
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
    static Run run(FieldSim.Physics physics, long seed, BodyShape b, double nose, int nectar) {
        try {
            return run(physics, seed, b, nose, nectar, null, -1);
        } catch (IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }

    /**
     * As above, logged for AdvantageScope unless {@code log} is null, the robot drawn as
     * {@link BodyShape#SHOWN}{@code [shown]}.
     */
    static Run run(FieldSim.Physics physics, long seed, BodyShape b, double nose, int nectar, WpiLog log, int shown)
            throws IOException {
        return run(physics, seed, b, nose, nectar, log, shown, 0, 0);
    }

    /** As above, logging only from sim time {@code logFrom} on, at {@code offsetUs} plus the sim time. */
    static Run run(FieldSim.Physics physics, long seed, BodyShape b, double nose, int nectar, WpiLog log, int shown,
                   double logFrom, long offsetUs) throws IOException {
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
        Shown view = new Shown(sim, log, shown, rx, ry, heading, logFrom, offsetUs);
        for (int k = 0; k < 12 && sim.red.tipsStarted == 0; k++) {
            sim.placeInRaisedCell(sim.red, FieldSim.Kind.POLLEN);
            for (int i = 0; i < 75 && sim.red.tipsStarted == 0; i++) view.step(HiveCalibration.LOOP_S);
        }
        Run r = new Run();
        if (sim.red.tipsStarted == 0) return r;
        r.tipAt = view.t;
        view.event("our CELL starts to TIP");
        List<FieldSim.Piece> spill = new ArrayList<>();
        for (FieldSim.Piece p : sim.pieces) {
            if (p.where == FieldSim.Where.FIELD && p.cell != null && p.cell.alliance() == sim.red.alliance) spill.add(p);
        }
        double c = Math.cos(heading), s = Math.sin(heading);
        for (int i = 0; i < 300; i++) {
            if (b.slide > 0) bot.wallsOut = Math.min(1, bot.wallsOut + 0.01 / bot.design.sideWallsTravelS);
            sim.setRobot(rx, ry, heading, 0, 0, 0, false);
            view.step(0.01);
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
        view.event(String.format(Locale.ROOT, "3 s after the TIP: %d of %d spilled pieces kept (in the dotted patch),"
                        + " G409: %d touched before the tiles (%d on the frame, %d on a flap only), most inside at once %d",
                r.kept, r.pieces, r.touched, r.frame, r.flapOnly, r.mostInside));
        r.endAt = view.t;
        return r;
    }

    /** Steps a run and, if it has a log, logs what AdvantageScope draws about 50 times a second. */
    private static final class Shown {
        final FieldSim sim;
        final WpiLog log;
        final double[] components;
        final double x, y, heading, logFrom;
        final long offsetUs;
        final FieldSimLog field = new FieldSimLog();
        double t, lastLogged = -1;

        Shown(FieldSim sim, WpiLog log, int shown, double x, double y, double heading, double logFrom, long offsetUs) {
            this.sim = sim;
            this.logFrom = logFrom;
            this.offsetUs = offsetUs;
            this.log = log;
            this.components = log == null ? null : RobotAssets.shapeComponents(shown);
            this.x = x;
            this.y = y;
            this.heading = heading;
        }

        void step(double dt) throws IOException {
            sim.step(dt);
            t += dt;
            if (log == null || t < logFrom || t - lastLogged < 0.019) {
                if (log != null && t < logFrom) sim.drainEvents();
                return;
            }
            lastLogged = t;
            long us = offsetUs + Math.round(t * 1e6);
            FieldRobot.slot(0).putPose(log, x, y, heading, us);
            log.putPose3dArray(COMPONENTS_KEY, components, us);
            field.write(log, sim, us);
            for (String e : sim.drainEvents()) log.putEvent("sim: " + e, us);
        }

        void event(String text) throws IOException {
            if (log != null) log.putEvent(text, offsetUs + Math.round(t * 1e6));
        }
    }

    /** Where a shape log puts {@link RobotAssets#SHAPES_NAME}'s component poses. */
    static final String COMPONENTS_KEY = "/BodyShape/Components";

    /**
     * One TIP per shape to watch in AdvantageScope ({@code BIOBUZZ Robot (shapes)}, layout
     * {@code sim-review/advantagescope-layout-shapes.json}), in {@code build/sim-logs}:
     * {@code body-<shape>.wpilog}, each parked at its closest clean spot
     * ({@link BodyShape#SHOWN_CLEAN_NOSE_IN}), the same TIP for all of them (8 POLLEN; of seeds 1–20,
     * the one where the long U keeps nearest its average). And {@code body-...-too-close.wpilog}: two
     * flap shapes 3 in nearer, on the first TIP where a piece lands on a flap.
     */
    @Test
    public void writesShapeLogs() throws IOException {
        FieldSim.Physics physics = HiveCalibration.current().fit();
        long seed = typicalSeed(physics);
        File dir = TeamCodeDir.simLogs();
        for (int i = 0; i < BodyShape.SHOWN.length; i++) {
            write(physics, seed, i, BodyShape.SHOWN_CLEAN_NOSE_IN[i], new File(dir, "body-" + BodyShape.SHOWN_FILE[i] + ".wpilog"));
        }
        for (BodyShape b : new BodyShape[] {BodyShape.FLAPS_16, BodyShape.FLARED_16}) {
            int i = java.util.Arrays.asList(BodyShape.SHOWN).indexOf(b);
            double nose = BodyShape.SHOWN_CLEAN_NOSE_IN[i] + 3;
            long touched = 1;
            while (touched < TIPS && run(physics, touched, b, nose, 0).flapOnly == 0) touched++;
            write(physics, touched, i, nose,
                    new File(dir, "body-" + BodyShape.SHOWN_FILE[i] + "-too-close.wpilog"));
        }
    }

    /** Seconds of each segment in {@link #writesAllShapesInOneLog} before its TIP starts, and the pause after it. */
    static final double SEGMENT_LEAD_S = 1.0, SEGMENT_GAP_S = 0.5;

    /**
     * Every shape in one log, {@code build/sim-logs/body-all-shapes.wpilog}: the same TIP as
     * {@link #writesShapeLogs}, one shape after another (each from 1 s before the TIP to 3 s after),
     * then the two too-close runs. The robot model switches by itself; the Console names each shape as
     * it starts and ends it with what it kept, and {@code /BodyShape/Name} holds the shape on screen.
     */
    @Test
    public void writesAllShapesInOneLog() throws IOException {
        FieldSim.Physics physics = HiveCalibration.current().fit();
        long seed = typicalSeed(physics);
        List<Object[]> segments = new ArrayList<>();  // {shown index, nose, seed, label}
        for (int i = 0; i < BodyShape.SHOWN.length; i++) {
            segments.add(new Object[] {i, BodyShape.SHOWN_CLEAN_NOSE_IN[i], seed, ""});
        }
        for (BodyShape b : new BodyShape[] {BodyShape.FLAPS_16, BodyShape.FLARED_16}) {
            int i = java.util.Arrays.asList(BodyShape.SHOWN).indexOf(b);
            double nose = BodyShape.SHOWN_CLEAN_NOSE_IN[i] + 3;
            long touched = 1;
            while (touched < TIPS && run(physics, touched, b, nose, 0).flapOnly == 0) touched++;
            segments.add(new Object[] {i, nose, touched, " (too close: a different TIP, one where a piece lands on a flap)"});
        }
        File file = new File(TeamCodeDir.simLogs(), "body-all-shapes.wpilog");
        WpiLog log = new WpiLog(new WpiLogWriter(new java.io.BufferedOutputStream(new java.io.FileOutputStream(file), 1 << 16),
                "BIOBUZZ body shapes"));
        log.putMetadata("Generator", "BodyShapeSpillTest.writesAllShapesInOneLog (TeamCode test sources)");
        log.putMetadata("PoseFrame", AdvantageScopeFrame.DESCRIPTION);
        FieldSimLog.putMetadata(log, HiveCalibration.current());
        FieldSimLog.putHiveStructure(log);
        log.put(AdvantageScopeKeys.ALLIANCE_STATION, AdvantageScopeKeys.allianceStation(true, 1), 0);
        StringBuilder timeline = new StringBuilder();
        long cursor = 0;
        for (int k = 0; k < segments.size(); k++) {
            int i = (Integer) segments.get(k)[0];
            double nose = (Double) segments.get(k)[1];
            long s = (Long) segments.get(k)[2];
            BodyShape b = BodyShape.SHOWN[i];
            double tipAt = run(physics, s, b, nose, 0).tipAt;
            double from = Math.max(0, tipAt - SEGMENT_LEAD_S);
            long offset = cursor - Math.round(from * 1e6);
            String label = String.format(Locale.ROOT, "%d of %d: %s, front-most point %.0f in from the wall%s",
                    k + 1, segments.size(), b.name, nose, segments.get(k)[3]);
            log.put("/BodyShape/Name", label, cursor);
            log.putEvent("SHAPE " + label, cursor);
            Run r = run(physics, s, b, nose, 0, log, i, from, offset);
            timeline.append(String.format(Locale.ROOT, "%5.1f s  %s%n", cursor / 1e6, label));
            System.out.printf(Locale.ROOT, "ALLSHAPES %5.1f s %s: kept %d of %d, G409 %d (flap only %d)%n",
                    cursor / 1e6, label, r.kept, r.pieces, r.touched, r.flapOnly);
            cursor = offset + Math.round(r.endAt * 1e6) + Math.round(SEGMENT_GAP_S * 1e6);
        }
        log.putMetadata("Timeline", timeline.toString());
        log.close();
        // AdvantageScope reads each key's records in time order: the segments must not overlap.
        WpiLogReader read = new WpiLogReader(java.nio.file.Files.readAllBytes(file.toPath()));
        for (WpiLogReader.Entry e : read.entries.values()) {
            for (int j = 1; j < e.records.size(); j++) {
                assertTrue(e.name, e.records.get(j).timestampUs >= e.records.get(j - 1).timestampUs);
            }
        }
        assertEquals(segments.size(), read.entries.get("/BodyShape/Name").records.size());
        assertTrue(read.entries.get(COMPONENTS_KEY).records.size() > 100);
        assertTrue(read.entries.containsKey("/Odometry/Robot3d"));
    }

    private static void write(FieldSim.Physics physics, long seed, int shown, double nose, File file) throws IOException {
        BodyShape b = BodyShape.SHOWN[shown];
        file.getParentFile().mkdirs();
        WpiLog log = new WpiLog(new WpiLogWriter(new java.io.BufferedOutputStream(new java.io.FileOutputStream(file), 1 << 16),
                "BIOBUZZ body shapes"));
        log.putMetadata("Generator", "BodyShapeSpillTest (TeamCode test sources)");
        log.putMetadata("PoseFrame", AdvantageScopeFrame.DESCRIPTION);
        log.putMetadata("Shape", String.format(Locale.ROOT, "%s, front-most point %.0f in from the wall (front face %.0f), TIP seed %d, 8 POLLEN",
                b.name, nose, nose - b.reach(), seed));
        FieldSimLog.putMetadata(log, HiveCalibration.current());
        FieldSimLog.putHiveStructure(log);
        log.put(AdvantageScopeKeys.ALLIANCE_STATION, AdvantageScopeKeys.allianceStation(true, 1), 0);
        log.putEvent("Shape: " + b.name + String.format(Locale.ROOT, ", front-most point %.0f in from the wall", nose), 0);
        Run r = run(physics, seed, b, nose, 0, log, shown);
        log.close();
        System.out.printf(Locale.ROOT, "SHAPELOG %-38s nose %2.0f: kept %d of %d, G409 %d (frame %d, flap only %d) -> %s%n",
                b.name, nose, r.kept, r.pieces, r.touched, r.frame, r.flapOnly, file.getName());
    }

    /** Of seeds 1–20, the TIP where the long U at its clean spot keeps nearest its average share. */
    static long typicalSeed(FieldSim.Physics physics) {
        double[] share = new double[21];
        double sum = 0;
        for (int seed = 1; seed <= 20; seed++) {
            Run r = run(physics, seed, BodyShape.LONG_U, 36, 0);
            share[seed] = r.pieces == 0 ? Double.NaN : (double) r.kept / r.pieces;
            sum += share[seed];
        }
        long best = 1;
        for (int seed = 1; seed <= 20; seed++) {
            if (Math.abs(share[seed] - sum / 20) < Math.abs(share[(int) best] - sum / 20)) best = seed;
        }
        return best;
    }
}
