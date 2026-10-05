package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Where a TIP's spill first touches the tiles, on the HIVE alone: the red rocker loaded as at the
 * start of a match (3 NECTAR), POLLEN placed one at a time until it tips, as in the 3 Oct 2026
 * films. For every piece in the CELL that goes down: how far from the alliance wall it first
 * touches, and how long after the TIP started.
 *
 * <p>The films (IMG_1957–1960, 120 fps) show the pieces pouring out of the lowered CELL's lip and
 * arcing out a little, first touching the tiles just under 2 tiles, about 42 in, out from the wall
 * (a mentor's estimate from the films), about 1.15–1.2 s after the rocker starts to move. The test
 * checks the simulation lands them there.
 */
public class SpillLandingTest {

    /** First touch, from the films: about 42 in out from the wall (a mentor's estimate), give or take. */
    static final double FILMED_NEAR_IN = 38, FILMED_FAR_IN = 48;
    /** First touch after the rocker starts to move, from the films. */
    static final double FILMED_FIRST_TOUCH_S = 1.15;

    static final class Landing {
        final double fromWallIn, x, y, seconds;
        /** Where it is 3 s after the TIP started: how far from the wall, and x. */
        double restFromWallIn = Double.NaN, restX = Double.NaN;
        /** How far it has gone from where it first touched, 0.5 s and 1 s later. */
        double travel05 = Double.NaN, travel1 = Double.NaN;
        Landing(double fromWallIn, double x, double y, double seconds) {
            this.fromWallIn = fromWallIn;
            this.x = x;
            this.y = y;
            this.seconds = seconds;
        }
    }

    /** Every spilled piece's first touch, over {@code seeds} TIPs. */
    static List<Landing> landings(int seeds) {
        FieldSim.Physics physics = HiveCalibration.current().fit();
        List<Landing> out = new ArrayList<>();
        for (long seed = 1; seed <= seeds; seed++) {
            FieldSim sim = new FieldSim(new ArrayList<>(), seed, physics);
            sim.red.locked = true;
            for (int i = 0; i < HiveCalibration.NECTAR_AT_MATCH_START; i++) {
                sim.placeInRaisedCell(sim.red, FieldSim.Kind.RED_NECTAR);
                HiveCalibration.settle(sim);
            }
            sim.red.locked = false;
            int down = sim.red.raisedEnd();
            Map<FieldSim.Piece, Boolean> tracked = new HashMap<>();
            Map<FieldSim.Piece, Landing> landed = new HashMap<>();
            double started = Double.NaN;
            for (int k = 0; k < 12 && sim.red.tipsStarted == 0; k++) {
                sim.placeInRaisedCell(sim.red, FieldSim.Kind.POLLEN);
                for (int i = 0; i < 75 && sim.red.tipsStarted == 0; i++) sim.step(HiveCalibration.LOOP_S);
            }
            if (sim.red.tipsStarted == 0) continue;
            started = sim.time;
            for (FieldSim.Piece p : sim.pieces) {
                if (p.where == FieldSim.Where.FIELD && p.cell != null && p.cell.alliance() == sim.red.alliance) {
                    tracked.put(p, false);
                }
            }
            // The CELL that was raised goes down: its end of the rocker is the spill's side.
            double wallY = down > 0 ? 2 * FieldSim.CENTRE_IN : 0;
            for (int i = 0; i < 300; i++) {
                sim.step(0.01);
                for (Map.Entry<FieldSim.Piece, Boolean> e : tracked.entrySet()) {
                    FieldSim.Piece p = e.getKey();
                    Landing seen = landed.get(p);
                    if (seen != null && p.where == FieldSim.Where.FIELD) {
                        double after = sim.time - started - seen.seconds, d = Math.hypot(p.x - seen.x, p.y - seen.y);
                        if (Double.isNaN(seen.travel05) && after >= 0.5) seen.travel05 = d;
                        if (Double.isNaN(seen.travel1) && after >= 1.0) seen.travel1 = d;
                    }
                    if (e.getValue() || p.where != FieldSim.Where.FIELD || p.cell != null) continue;
                    if (p.z < p.kind.radius + 0.3) {
                        e.setValue(true);
                        Landing l = new Landing(Math.abs(p.y - wallY), p.x, p.y, sim.time - started);
                        out.add(l);
                        landed.put(p, l);
                    }
                }
            }
            for (Map.Entry<FieldSim.Piece, Landing> e : landed.entrySet()) {
                e.getValue().restFromWallIn = Math.abs(e.getKey().y - wallY);
                e.getValue().restX = e.getKey().x;
            }
        }
        return out;
    }

    /** How far the pieces go after they land, in words. */
    static String spread(List<Landing> ls) {
        List<Double> t05 = new ArrayList<>(), t1 = new ArrayList<>(), rd = new ArrayList<>(), rx = new ArrayList<>();
        int nearWall = 0;
        for (Landing l : ls) {
            if (!Double.isNaN(l.travel05)) t05.add(l.travel05);
            if (!Double.isNaN(l.travel1)) t1.add(l.travel1);
            rd.add(l.restFromWallIn);
            rx.add(l.restX);
            if (l.restFromWallIn < 12) nearWall++;
        }
        return String.format(Locale.ROOT, "travel 0.5 s after first touch p50/p90 %.0f / %.0f in, 1 s %.0f / %.0f in;"
                        + " 3 s after the TIP: from the wall %.0f / %.0f / %.0f in, x %.0f-%.0f, %d of %d within 12 in of the wall",
                pct(t05, 0.5), pct(t05, 0.9), pct(t1, 0.5), pct(t1, 0.9), pct(rd, 0.1), pct(rd, 0.5), pct(rd, 0.9),
                pct(rx, 0.1), pct(rx, 0.9), nearWall, ls.size());
    }

    static double pct(List<Double> v, double q) {
        List<Double> s = new ArrayList<>(v);
        Collections.sort(s);
        return s.get((int) Math.min(s.size() - 1, Math.floor(q * s.size())));
    }

    @Test
    public void theSpillLandsWhereTheFilmsShow() {
        // BIOBUZZ_BOUNCE_SCATTER=0,0.2,0.4 prints how the spill spreads for each bounce scatter instead.
        String scatter = System.getenv("BIOBUZZ_BOUNCE_SCATTER");
        if (scatter != null) {
            for (String v : scatter.split(",")) {
                FieldSim.bounceScatter = Double.parseDouble(v);
                List<Landing> ls = landings(10);
                FieldSim.bounceScatter = FieldSim.FILMED_BOUNCE_SCATTER;
                System.out.println("SCATTER " + v + ": " + spread(ls));
            }
            return;
        }
        // BIOBUZZ_SPILL_EXIT=1,0.5,0.25 prints the landing for each exit scale instead (how it was fitted).
        String sweep = System.getenv("BIOBUZZ_SPILL_EXIT");
        if (sweep != null) {
            for (String v : sweep.split(",")) {
                FieldSim.spillExitScale = Double.parseDouble(v);
                List<Double> ds = new ArrayList<>(), ts = new ArrayList<>();
                for (Landing l : landings(10)) {
                    ds.add(l.fromWallIn);
                    ts.add(l.seconds);
                }
                FieldSim.spillExitScale = FieldSim.FILMED_SPILL_EXIT_SCALE;
                System.out.printf(Locale.ROOT, "SWEEP exit %.2f: from wall %.0f / %.0f / %.0f in, time %.2f / %.2f / %.2f s%n",
                        Double.parseDouble(v), pct(ds, 0.1), pct(ds, 0.5), pct(ds, 0.9), pct(ts, 0.1), pct(ts, 0.5), pct(ts, 0.9));
            }
            return;
        }
        List<Landing> all = landings(10);
        List<Double> d = new ArrayList<>(), t = new ArrayList<>(), x = new ArrayList<>();
        List<Double> rd = new ArrayList<>(), rx = new ArrayList<>();
        for (Landing l : all) {
            d.add(l.fromWallIn);
            t.add(l.seconds);
            x.add(l.x);
            rd.add(l.restFromWallIn);
            rx.add(l.restX);
        }
        assertTrue("no piece landed", !d.isEmpty());
        System.out.printf(Locale.ROOT,
                "SPILL %d pieces; first touch from the wall p10/p50/p90: %.0f / %.0f / %.0f in;"
                        + " x %.0f / %.0f / %.0f; time after the TIP starts %.2f / %.2f / %.2f s%n",
                d.size(), pct(d, 0.1), pct(d, 0.5), pct(d, 0.9), pct(x, 0.1), pct(x, 0.5), pct(x, 0.9),
                pct(t, 0.1), pct(t, 0.5), pct(t, 0.9));
        System.out.println("SPILL " + spread(all));
        System.out.printf(Locale.ROOT, "SPILL 3 s after the TIP starts, from the wall p10/p50/p90: %.0f / %.0f / %.0f in; x %.0f / %.0f / %.0f%n",
                pct(rd, 0.1), pct(rd, 0.5), pct(rd, 0.9), pct(rx, 0.1), pct(rx, 0.5), pct(rx, 0.9));
        double median = pct(d, 0.5);
        assertTrue(String.format(Locale.ROOT, "median first touch %.2f s after the TIP starts; filmed about 1.15", pct(t, 0.5)),
                Math.abs(pct(t, 0.5) - FILMED_FIRST_TOUCH_S) < 0.15);
        assertTrue(String.format(Locale.ROOT, "median first touch %.0f in from the wall; filmed %.0f–%.0f",
                median, FILMED_NEAR_IN, FILMED_FAR_IN), median >= FILMED_NEAR_IN && median <= FILMED_FAR_IN);
    }

    /** TIPs behind tools/spill-window/draw.py's picture of where the spill lands. */
    static final int FIRST_TOUCH_TIPS = 200;

    /**
     * Every spilled piece's first touch on the tiles over {@value #FIRST_TOUCH_TIPS} TIPs, for
     * {@code tools/spill-window/draw.py}: {@code build/sim-logs/spill-first-touch.csv}, one row per
     * piece: inches from the wall, x, seconds after the TIP started, and where it lies 3 s later.
     */
    @Test
    public void writesWhereTheSpillFirstTouches() throws java.io.IOException {
        StringBuilder out = new StringBuilder("# fromWallIn,xIn,seconds,restFromWallIn,restXIn; Pedro inches, "
                + FIRST_TOUCH_TIPS + " TIPs, no robot (SpillLandingTest)\n");
        List<Landing> all = landings(FIRST_TOUCH_TIPS);
        for (Landing l : all) {
            out.append(String.format(Locale.ROOT, "%.2f,%.2f,%.3f,%.2f,%.2f%n", l.fromWallIn, l.x, l.seconds, l.restFromWallIn, l.restX));
        }
        java.io.File file = new java.io.File(TeamCodeDir.simLogs(), "spill-first-touch.csv");
        file.getParentFile().mkdirs();
        java.nio.file.Files.write(file.toPath(), out.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        assertTrue("no piece landed", !all.isEmpty());
    }
}
