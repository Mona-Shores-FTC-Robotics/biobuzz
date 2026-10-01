package org.firstinspires.ftc.teamcode.logging;

import org.firstinspires.ftc.teamcode.vision.HiveGeometry;
import org.junit.Test;

import java.util.Locale;

/**
 * Can one launcher, at one setting, shoot both POLLEN (2.8 in) and NECTAR (3.6 in, 1.65× heavier)?
 * A launcher built right may throw both at nearly the same speed; this works out how near is near
 * enough. For a few shooting spots it finds, for each piece, the range of launch speeds (as a
 * fraction of the ideal one) that end up in the raised CELL, and from those the range of
 * NECTAR-to-POLLEN speed ratios one setting can serve with room for the shot-to-shot spread. Opt in:
 *
 * <pre>
 * BIOBUZZ_LAUNCHER_STUDY=1 ./gradlew :TeamCode:testDebugUnitTest --tests '*LauncherStudyTest*' -i
 * </pre>
 * No air drag is simulated, which would slow the light POLLEN more than NECTAR.
 */
public class LauncherStudyTest {

    /** Spots drawn for RED; a y above the HIVE's centre shoots at the north CELL. */
    static final double[][] SPOTS = {
            {59, 9.5}, {36, 30}, {58, 30}, {58, 40}, {40, 116}, {59, 132.25}, {58, 101}};
    static final String[] NAMES = {
            "south wall, straight on", "south, angled", "south, close", "south, closer",
            "north, angled", "north wall, straight on", "north, close (out of the tunnel)"};
    /** Two sigmas of the placeholder speed spread, kept clear of each window's edges. */
    static final double MARGIN = 2 * FieldSim.PLACEHOLDER_SPEED_SPREAD;

    @Test
    public void windows() throws Exception {
        if (System.getenv("BIOBUZZ_LAUNCHER_STUDY") == null) return;
        for (double arc : new double[] {6, 15, 25}) {
            System.out.printf(Locale.ROOT, "Arc %.0f deg steeper than the flattest:%n", arc);
            for (int i = 0; i < SPOTS.length; i++) {
                double[] p = window(SPOTS[i], FieldSim.Kind.POLLEN, arc);
                double[] n = window(SPOTS[i], FieldSim.Kind.RED_NECTAR, arc);
                String ratio = "no ratio works";
                if (p != null && n != null) {
                    double lo = (n[0] + MARGIN) / (p[1] - MARGIN), hi = (n[1] - MARGIN) / (p[0] + MARGIN);
                    if (lo < hi) ratio = String.format(Locale.ROOT, "NECTAR/POLLEN %.2f to %.2f", lo, hi);
                }
                System.out.printf(Locale.ROOT, "  %-34s POLLEN %s  NECTAR %s  -> %s%n", NAMES[i], fmt(p), fmt(n), ratio);
            }
        }
    }

    private static String fmt(double[] w) {
        return w == null ? "     (none)     " : String.format(Locale.ROOT, "%5.1f%% to %+5.1f%%", (w[0] - 1) * 100, (w[1] - 1) * 100);
    }

    /** The contiguous range of speed fractions around 1 that score, or null. */
    static double[] window(double[] spot, FieldSim.Kind kind, double arc) throws Exception {
        boolean[] in = new boolean[81];
        for (int k = 0; k <= 80; k++) in[k] = scores(spot, kind, arc, 0.80 + k * 0.005);
        int mid = 40;
        if (!in[mid]) {
            for (int d = 1; d <= 40 && !in[mid]; d++) {
                if (mid - d >= 0 && in[40 - d]) mid = 40 - d;
                else if (40 + d <= 80 && in[40 + d]) mid = 40 + d;
            }
            if (!in[mid]) return null;
        }
        int lo = mid, hi = mid;
        while (lo > 0 && in[lo - 1]) lo--;
        while (hi < 80 && in[hi + 1]) hi++;
        return new double[] {0.80 + lo * 0.005, 0.80 + hi * 0.005};
    }

    static boolean scores(double[] spot, FieldSim.Kind kind, double arc, double factor) throws Exception {
        FieldSim sim = new FieldSim(HiveAssets.committedStagedPieces(), 1);
        FieldSim.Rocker r = sim.red;
        r.locked = true;
        if (spot[1] > FieldSim.CENTRE_IN) r.angle = FieldSim.TILT_RAD; // north CELL up
        double heading = Math.atan2(FieldSim.CENTRE_IN - spot[1], r.centreX - spot[0]);
        sim.setRobot(spot[0], spot[1], heading, 0, 0, 0, false);
        double[] aim = r.aimPoint();
        double[] from = sim.exitPoint();
        double[] v = sim.launchVelocity(from, aim, arc);
        if (v == null) return false;
        FieldSim.Piece p = new FieldSim.Piece(kind, FieldSim.Where.FIELD, from[0], from[1], from[2]);
        p.vx = v[0] * factor;
        p.vy = v[1] * factor;
        p.vz = v[2] * factor;
        sim.pieces.add(p);
        for (int i = 0; i < 120; i++) sim.step(0.02);
        return p.cell != null;
    }
}
