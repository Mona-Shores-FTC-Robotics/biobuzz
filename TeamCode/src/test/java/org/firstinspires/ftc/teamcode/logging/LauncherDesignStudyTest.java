package org.firstinspires.ftc.teamcode.logging;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Which flywheel launchers can shoot both POLLEN and NECTAR into the HIVE at one wheel speed?
 *
 * <p>In the air the two pieces behave almost alike: drag and spin lift scale with area over mass,
 * 0.159 cm²/g for POLLEN and 0.161 for NECTAR. So a launcher that sends both out at the same speed,
 * angle and spin lands both together, and any difference comes from inside the launcher. This
 * study works that out in two steps:
 * <ol>
 *   <li><b>Windows.</b> With {@link FieldSim}'s drag and spin lift on, for each piece, shooting spot,
 *       launch angle and spin, the range of exit speeds that end up in the raised CELL.</li>
 *   <li><b>Designs.</b> For thousands of {@link LauncherModel} launchers (kind, wheel, tread, gap,
 *       spring, hood friction, flywheel), what each piece leaves at, as a function of wheel speed.
 *       At each spot it finds the one wheel speed that scores both best. That takes into account
 *       wheel speed control (1%), the pieces' size variation (1%) and the wheel slowing over a
 *       burst of 4 shots 0.45 s apart.</li>
 * </ol>
 * It prints the best designs of each kind and why the usual fixed-gap launcher does not work.
 * Opt in; it takes a few minutes:
 *
 * <pre>
 * BIOBUZZ_LAUNCHER_DESIGN=1 ./gradlew :TeamCode:testDebugUnitTest --tests '*LauncherDesignStudyTest*' -i
 * </pre>
 * The windows are cached in {@code TeamCode/build/sim-logs/launcher-windows.csv}; delete it after
 * changing the HIVE or the air.
 */
public class LauncherDesignStudyTest {

    /** Shooting spots on the south side, drawn for RED: {x, y}, the robot facing the HIVE. */
    static final double[][] SPOTS = {{59, 9.5}, {58, 30}, {36, 30}};
    static final String[] SPOT_NAMES = {"wall, straight on", "20 in out, straight on", "angled, 30 in out"};
    static final double[] ANGLES = {40, 45, 50, 55, 60, 65, 70, 75};
    /** Spin numbers r·ω/v, backspin positive. */
    static final double[] SPINS = {-0.6, -0.3, 0, 0.3, 0.6, 1.0};
    static final double EXIT_HEIGHT_IN = 17;

    static final double RPM_JITTER = 0.01;
    static final double SIZE_JITTER = 0.01;
    static final double ANGLE_JITTER_DEG = 0.75;
    static final double BURST_INTERVAL_S = 0.45;

    // ---- Step 1: windows -------------------------------------------------------------------------

    /** windows[ball][spot][angle][spin] = {vmin, vmax} in m/s, or null. */
    static double[][][][][] windows() throws IOException {
        File cache = new File(TeamCodeDir.simLogs(), "launcher-windows.csv");
        double[][][][][] w = new double[2][SPOTS.length][ANGLES.length][SPINS.length][];
        if (cache.exists()) {
            for (String line : Files.readAllLines(cache.toPath(), StandardCharsets.UTF_8)) {
                String[] f = line.split(",");
                if (f.length == 6) {
                    w[Integer.parseInt(f[0])][Integer.parseInt(f[1])][Integer.parseInt(f[2])][Integer.parseInt(f[3])] =
                            new double[] {Double.parseDouble(f[4]), Double.parseDouble(f[5])};
                }
            }
            return w;
        }
        StringBuilder csv = new StringBuilder();
        for (int b = 0; b < 2; b++) {
            FieldSim.Kind kind = b == 0 ? FieldSim.Kind.POLLEN : FieldSim.Kind.RED_NECTAR;
            for (int s = 0; s < SPOTS.length; s++) {
                for (int a = 0; a < ANGLES.length; a++) {
                    for (int k = 0; k < SPINS.length; k++) {
                        double lo = Double.NaN, hi = Double.NaN;
                        for (double v = 3; v <= 13; v += 0.05) {
                            if (scores(kind, SPOTS[s], ANGLES[a], SPINS[k], v)) {
                                if (Double.isNaN(lo)) lo = v;
                                hi = v;
                            } else if (!Double.isNaN(lo)) {
                                break; // the first contiguous window
                            }
                        }
                        if (!Double.isNaN(lo)) {
                            w[b][s][a][k] = new double[] {lo - 0.025, hi + 0.025};
                            csv.append(String.format(Locale.ROOT, "%d,%d,%d,%d,%.3f,%.3f%n", b, s, a, k, lo - 0.025, hi + 0.025));
                        }
                    }
                }
            }
        }
        Files.write(cache.toPath(), csv.toString().getBytes(StandardCharsets.UTF_8));
        return w;
    }

    private static final java.util.List<HiveAssets.StagedPiece> EMPTY = new ArrayList<>();

    /** One shot, with air, at the raised south CELL, from {@code spot} at a fixed angle and speed. */
    static boolean scores(FieldSim.Kind kind, double[] spot, double angleDeg, double spinNumber, double speedMps) {
        FieldSim sim = new FieldSim(EMPTY, 1, HiveCalibration.current().fit());
        sim.air = true;
        sim.red.locked = true;
        double[] aim = sim.red.aimPoint();
        double bearing = Math.atan2(aim[1] - spot[1], aim[0] - spot[0]);
        double ex = spot[0] + FieldSim.PLACEHOLDER_EXIT_FORWARD_IN * Math.cos(bearing);
        double ey = spot[1] + FieldSim.PLACEHOLDER_EXIT_FORWARD_IN * Math.sin(bearing);
        double v = speedMps / 0.0254, pitch = Math.toRadians(angleDeg);
        FieldSim.Piece p = new FieldSim.Piece(kind, FieldSim.Where.FIELD, ex, ey, EXIT_HEIGHT_IN);
        p.vx = v * Math.cos(pitch) * Math.cos(bearing);
        p.vy = v * Math.cos(pitch) * Math.sin(bearing);
        p.vz = v * Math.sin(pitch);
        // Backspin: about the horizontal axis to the left of travel, negative sense.
        double w = spinNumber * v / kind.radius;
        p.wx = w * Math.sin(bearing);
        p.wy = -w * Math.cos(bearing);
        sim.pieces.add(p);
        for (int i = 0; i < 70; i++) {
            sim.step(0.02);
            if (p.z < kind.radius + 0.1 && p.cell == null) return false;
        }
        return p.cell != null;
    }

    /** The window at an angle and spin number between the grid's points (nearest angle, nearest spin). */
    static double[] window(double[][][][][] w, int ball, int spot, double angleDeg, double spinNumber) {
        int a = nearest(ANGLES, angleDeg), k = nearest(SPINS, spinNumber);
        return w[ball][spot][a][k];
    }

    private static int nearest(double[] grid, double v) {
        int best = 0;
        for (int i = 1; i < grid.length; i++) if (Math.abs(grid[i] - v) < Math.abs(grid[best] - v)) best = i;
        return best;
    }

    // ---- Step 2: designs -------------------------------------------------------------------------

    /** How one design does. */
    static final class Verdict {
        LauncherModel design;
        /** Per spot: the wheel speed chosen (rpm) and the chance each piece scores. */
        double[] rpm = new double[SPOTS.length];
        double[] pPollen = new double[SPOTS.length];
        double[] pNectar = new double[SPOTS.length];
        double score;
        String pollenShot, nectarShot;
        String problem;

        double worst() {
            double m = 1;
            for (int s = 0; s < SPOTS.length; s++) m = Math.min(m, Math.min(pPollen[s], pNectar[s]));
            return m;
        }
    }

    /** Exit speed and spin of a 4-shot burst's worst shot, for each ball size: {speed, spinNumber}. */
    static double[][] burst(LauncherModel d, LauncherModel.Ball ball, double wheelRad) {
        double om1 = wheelRad, om2 = wheelRad * d.secondWheelSpeed;
        double minV = Double.MAX_VALUE, maxV = 0, spin = 0;
        for (int shot = 0; shot < 4; shot++) {
            LauncherModel.Shot s = d.fire(ball, om1, om2);
            if (!s.ok()) return null;
            minV = Math.min(minV, s.exitSpeed);
            maxV = Math.max(maxV, s.exitSpeed);
            spin = s.spinNumber;
            om1 = d.recover(s.wheelAfter, wheelRad, BURST_INTERVAL_S);
            om2 = d.recover(s.secondWheelAfter, wheelRad * d.secondWheelSpeed, BURST_INTERVAL_S);
        }
        return new double[][] {{minV, maxV, spin}};
    }

    /** P(normal(mean, sd) in [lo, hi]). */
    static double inside(double mean, double sd, double lo, double hi) {
        return phi((hi - mean) / sd) - phi((lo - mean) / sd);
    }

    static double phi(double z) {
        // Abramowitz-Stegun erf approximation.
        double t = 1 / (1 + 0.3275911 * Math.abs(z) / Math.sqrt(2));
        double y = 1 - (((((1.061405429 * t - 1.453152027) * t) + 1.421413741) * t - 0.284496736) * t + 0.254829592) * t
                * Math.exp(-z * z / 2);
        return z >= 0 ? 0.5 * (1 + y) : 0.5 * (1 - y);
    }

    static final double[] PROFILE_FRACTIONS = {0.15, 0.25, 0.35, 0.45, 0.55, 0.65, 0.75, 0.85, 1.0};

    /**
     * What one piece leaves a design at, across wheel speeds: for each, the slowest and fastest of a
     * 4-shot burst over the size variation, the nominal shot, and its spin number.
     */
    static final class Profile {
        final double[] wheel = new double[PROFILE_FRACTIONS.length];
        final double[] lo = new double[PROFILE_FRACTIONS.length];
        final double[] hi = new double[PROFILE_FRACTIONS.length];
        final double[] nominal = new double[PROFILE_FRACTIONS.length];
        final double[] spin = new double[PROFILE_FRACTIONS.length];
        final boolean[] ok = new boolean[PROFILE_FRACTIONS.length];
        final String[] said = new String[PROFILE_FRACTIONS.length];

        Profile(LauncherModel d, LauncherModel.Ball ball, double top) {
            for (int i = 0; i < wheel.length; i++) {
                double om = top * PROFILE_FRACTIONS[i];
                wheel[i] = om;
                LauncherModel.Shot shot = d.fire(ball, om, om * d.secondWheelSpeed);
                said[i] = shot.toString();
                if (!shot.ok()) continue;
                double[][] small = burst(d, ball.scaled(1 - SIZE_JITTER), om);
                double[][] big = burst(d, ball.scaled(1 + SIZE_JITTER), om);
                if (small == null || big == null) continue;
                ok[i] = true;
                lo[i] = Math.min(small[0][0], big[0][0]);
                hi[i] = Math.max(small[0][1], big[0][1]);
                nominal[i] = shot.exitSpeed;
                spin[i] = shot.spinNumber;
            }
        }

        /** {lo, hi, nominal, spin} at wheel speed {@code om}, interpolated; null where it does not work. */
        double[] at(double om) {
            for (int i = 0; i + 1 < wheel.length; i++) {
                if (om < wheel[i] - 1e-9 || om > wheel[i + 1] + 1e-9) continue;
                if (!ok[i] || !ok[i + 1]) return null;
                double f = (om - wheel[i]) / (wheel[i + 1] - wheel[i]);
                return new double[] {lo[i] + f * (lo[i + 1] - lo[i]), hi[i] + f * (hi[i + 1] - hi[i]),
                        nominal[i] + f * (nominal[i + 1] - nominal[i]), spin[i] + f * (spin[i + 1] - spin[i])};
            }
            return null;
        }

        String describe(double om) {
            int best = 0;
            for (int i = 1; i < wheel.length; i++) if (Math.abs(wheel[i] - om) < Math.abs(wheel[best] - om)) best = i;
            return said[best];
        }
    }

    /**
     * The chance a piece scores with the wheel at {@code om}: exit speed spread by wheel speed
     * control, size variation and burst slowdown, against the window at the design's angle (narrowed
     * by the angle jitter).
     */
    static double chance(double[][][][][] w, int ballIndex, int spot, LauncherModel d, Profile profile, double om) {
        double[] at = profile.at(om);
        if (at == null) return 0;
        double lo = at[0], hi = at[1];
        double mean = (lo + hi) / 2;
        double sd = Math.sqrt(Math.pow((hi - lo) / 4, 2) + Math.pow(at[2] * RPM_JITTER, 2)) + 1e-6;
        double[] win = null;
        for (double da = -ANGLE_JITTER_DEG; da <= ANGLE_JITTER_DEG + 1e-9; da += ANGLE_JITTER_DEG) {
            double[] here = window(w, ballIndex, spot, d.exitAngleDeg + da, at[3]);
            if (here == null) return 0;
            win = win == null ? here.clone() : new double[] {Math.max(win[0], here[0]), Math.min(win[1], here[1])};
        }
        if (win[1] <= win[0]) return 0;
        return inside(mean, sd, win[0], win[1]);
    }

    static Verdict judge(double[][][][][] w, LauncherModel d, Profile pollen, Profile nectar) {
        Verdict v = new Verdict();
        v.design = d;
        double top = d.maxWheelRadPerS() * 0.9; // leave the motor room to recover
        for (int s = 0; s < SPOTS.length; s++) {
            double best = -1;
            v.rpm[s] = Double.NaN;
            for (double frac = 0.15; frac <= 1.0001; frac += 0.0125) {
                double om = top * frac;
                double pp = chance(w, 0, s, d, pollen, om);
                double pn = chance(w, 1, s, d, nectar, om);
                double joint = Math.min(pp, pn) + 1e-3 * (pp + pn);
                if (joint > best) {
                    best = joint;
                    v.rpm[s] = om * 60 / (2 * Math.PI);
                    v.pPollen[s] = pp;
                    v.pNectar[s] = pn;
                }
            }
        }
        v.score = v.worst();
        double om0 = Double.isNaN(v.rpm[0]) ? top * 0.6 : v.rpm[0] * 2 * Math.PI / 60;
        v.pollenShot = pollen.describe(om0);
        v.nectarShot = nectar.describe(om0);
        return v;
    }

    static Verdict judge(double[][][][][] w, LauncherModel d) {
        double top = d.maxWheelRadPerS() * 0.9;
        return judge(w, d, new Profile(d, LauncherModel.pollen(), top), new Profile(d, LauncherModel.nectar(), top));
    }

    /** Every design the sweep tries. */
    static List<LauncherModel> designs() {
        List<LauncherModel> out = new ArrayList<>();
        // goBILDA 72 mm and 96 mm, and a 4 in wheel.
        double[] wheels = {2.83, 3.78, 4.0};
        // Firm rubber, medium, soft compliant tread.
        double[] treads = {50_000, 20_000, 8_000};
        // The wheel alone; a light added flywheel; a heavy one (a 4 in steel disc 0.5 in thick).
        double[] inertias = {0.6e-4, 3e-4, 1e-3};
        int[] motorCounts = {1, 2};
        for (LauncherModel.Kind kind : LauncherModel.Kind.values()) {
            boolean dbl = kind == LauncherModel.Kind.DOUBLE || kind == LauncherModel.Kind.DOUBLE_SPRING;
            boolean sprung = kind == LauncherModel.Kind.HOOD_SPRING || kind == LauncherModel.Kind.DOUBLE_SPRING;
            double[] gaps = sprung ? new double[] {2.4, 2.6, 2.7} : new double[] {2.2, 2.4, 2.5, 2.6, 2.7, 2.9, 3.1, 3.3, 3.5};
            double[] preloads = sprung ? new double[] {5, 15, 30} : new double[] {0};
            double[] rates = sprung ? new double[] {500, 2_000, 6_000} : new double[] {0};
            double[] hoods = dbl ? new double[] {0} : new double[] {0.25};
            double[] seconds = dbl ? new double[] {1.0, 0.85} : new double[] {1.0};
            for (double wheel : wheels) for (double tread : treads) for (double gap : gaps)
                for (double pre : preloads) for (double rate : rates) for (double hood : hoods)
                    for (double sec : seconds) for (double inertia : inertias) for (int motors : motorCounts) {
                        LauncherModel d = new LauncherModel();
                        d.kind = kind;
                        d.wheelDiameterIn = wheel;
                        d.wheelStiffness = tread;
                        d.gapIn = gap;
                        d.springPreloadN = pre;
                        d.springRate = rate;
                        d.hoodFriction = hood;
                        d.secondWheelSpeed = sec;
                        d.inertia = inertia;
                        d.motors = motors;
                        d.contactIn = Math.PI / 2 * (wheel / 2 + 1.6); // a quarter turn round the wheel
                        out.add(d);
                    }
        }
        return out;
    }

    @Test
    public void sweep() throws Exception {
        if (System.getenv("BIOBUZZ_LAUNCHER_DESIGN") == null) return;
        double[][][][][] w = windows();
        printWindows(w);

        List<LauncherModel> designs = designs();
        List<Verdict> all = Collections.synchronizedList(new ArrayList<>());
        designs.parallelStream().forEach(base -> {
            // The launch angle is the builder's choice: keep the best.
            Verdict best = null;
            double top = base.maxWheelRadPerS() * 0.9;
            Profile pollen = new Profile(base, LauncherModel.pollen(), top);
            Profile nectar = new Profile(base, LauncherModel.nectar(), top);
            for (double angle : new double[] {60, 65, 70, 75}) {
                LauncherModel d = base.copy();
                d.exitAngleDeg = angle;
                Verdict v = judge(w, d, pollen, nectar);
                if (best == null || v.score > best.score) best = v;
            }
            all.add(best);
        });
        Collections.sort(all, (a, b) -> Double.compare(b.score, a.score));
        System.out.printf(Locale.ROOT, "LD %d designs tried%n", all.size());
        for (LauncherModel.Kind kind : LauncherModel.Kind.values()) {
            System.out.printf(Locale.ROOT, "LD == %s: best five%n", kind);
            int shown = 0;
            for (Verdict v : all) {
                if (v.design.kind != kind) continue;
                print(v);
                if (++shown == 5) break;
            }
        }
        // The launcher most teams build first: one wheel, a fixed hood, the gap set for one piece.
        System.out.printf(Locale.ROOT, "LD == The usual first launcher, gap set for POLLEN or for NECTAR%n");
        for (double gap : new double[] {2.5, 3.3}) {
            LauncherModel d = new LauncherModel();
            d.gapIn = gap;
            d.contactIn = Math.PI / 2 * (d.wheelDiameterIn / 2 + 1.6);
            Verdict v = judge(w, d);
            print(v);
        }
    }

    /**
     * One launcher, described on the command line, so a build team can check a prototype or an
     * idea and feed in what they measured:
     *
     * <pre>
     * BIOBUZZ_LAUNCHER_TRY="kind=HOOD_SPRING wheel=3.78 tread=8000 gap=2.6 preload=15 rate=2000 \
     *   inertia=1e-3 motors=2 angle=70 pollenStiffness=17500 nectarStiffness=14000" \
     *   ./gradlew :TeamCode:testDebugUnitTest --tests '*LauncherDesignStudyTest*' -i
     * </pre>
     * Keys: kind (HOOD, HOOD_SPRING, DOUBLE, DOUBLE_SPRING), wheel (in), tread (N/m), gap (in),
     * preload (N), rate (N/m), travel (in), hood (friction), friction (wheel), contact (in), angle
     * (deg), inertia (kg·m²), motors, gear, second (double's second wheel speed fraction),
     * rolling, pollenStiffness and nectarStiffness (N/m). Stiffness: the force to squeeze a piece by
     * 0.25 in, in lbf, times 700.
     */
    @Test
    public void tryOne() throws Exception {
        String spec = System.getenv("BIOBUZZ_LAUNCHER_TRY");
        if (spec == null) return;
        LauncherModel d = new LauncherModel();
        d.contactIn = Double.NaN;
        LauncherModel.Ball pollen = LauncherModel.pollen(), nectar = LauncherModel.nectar();
        for (String kv : spec.trim().split("\\s+")) {
            String[] f = kv.split("=");
            double x = f[0].equals("kind") ? 0 : Double.parseDouble(f[1]);
            switch (f[0]) {
                case "kind": d.kind = LauncherModel.Kind.valueOf(f[1]); break;
                case "wheel": d.wheelDiameterIn = x; break;
                case "tread": d.wheelStiffness = x; break;
                case "gap": d.gapIn = x; break;
                case "preload": d.springPreloadN = x; break;
                case "rate": d.springRate = x; break;
                case "travel": d.springTravelIn = x; break;
                case "hood": d.hoodFriction = x; break;
                case "friction": d.wheelFriction = x; break;
                case "contact": d.contactIn = x; break;
                case "angle": d.exitAngleDeg = x; break;
                case "inertia": d.inertia = x; break;
                case "motors": d.motors = (int) x; break;
                case "gear": d.gear = x; break;
                case "second": d.secondWheelSpeed = x; break;
                case "rolling": d.rollingLoss = x; break;
                case "pollenStiffness": pollen.stiffness = x; break;
                case "nectarStiffness": nectar.stiffness = x; break;
                default: throw new IllegalArgumentException("unknown key " + f[0]);
            }
        }
        if (Double.isNaN(d.contactIn)) d.contactIn = Math.PI / 2 * (d.wheelDiameterIn / 2 + 1.6);
        double[][][][][] w = windows();
        double top = d.maxWheelRadPerS() * 0.9;
        Verdict v = judge(w, d, new Profile(d, pollen, top), new Profile(d, nectar, top));
        print(v);
        System.out.printf(Locale.ROOT, "LD  wheel speed sweep (rpm: POLLEN exit / NECTAR exit, m/s):%n");
        for (double frac = 0.2; frac <= 1.0001; frac += 0.1) {
            double om = top * frac;
            LauncherModel.Shot p = d.fire(pollen, om, om * d.secondWheelSpeed);
            LauncherModel.Shot n = d.fire(nectar, om, om * d.secondWheelSpeed);
            System.out.printf(Locale.ROOT, "LD    %5.0f rpm: POLLEN %s | NECTAR %s%s%n", om * 60 / (2 * Math.PI), p, n,
                    p.ok() && n.ok() ? String.format(Locale.ROOT, " | NECTAR/POLLEN %.2f", n.exitSpeed / p.exitSpeed) : "");
        }
    }

    static void print(Verdict v) {
        StringBuilder spots = new StringBuilder();
        for (int s = 0; s < SPOTS.length; s++) {
            spots.append(String.format(Locale.ROOT, " | %s: %s rpm, POLLEN %.0f%%, NECTAR %.0f%%", SPOT_NAMES[s],
                    Double.isNaN(v.rpm[s]) ? "no" : String.format(Locale.ROOT, "%.0f", v.rpm[s]),
                    100 * v.pPollen[s], 100 * v.pNectar[s]));
        }
        System.out.printf(Locale.ROOT, "LD  worst %.0f%%: %s%nLD      POLLEN %s; NECTAR %s%nLD     %s%n",
                100 * v.score, v.design.describe(), v.pollenShot, v.nectarShot, spots);
    }

    static void printWindows(double[][][][][] w) {
        for (int s = 0; s < SPOTS.length; s++) {
            for (int a = 0; a < ANGLES.length; a++) {
                StringBuilder sb = new StringBuilder();
                for (int k = 0; k < SPINS.length; k++) {
                    double[] p = w[0][s][a][k], n = w[1][s][a][k];
                    sb.append(String.format(Locale.ROOT, "  spin %+.1f: P %s N %s", SPINS[k], fmt(p), fmt(n)));
                }
                System.out.printf(Locale.ROOT, "LW %s, %2.0f°:%s%n", SPOT_NAMES[s], ANGLES[a], sb);
            }
        }
    }

    private static String fmt(double[] win) {
        return win == null ? "   --    " : String.format(Locale.ROOT, "%.2f-%.2f", win[0], win[1]);
    }
}
