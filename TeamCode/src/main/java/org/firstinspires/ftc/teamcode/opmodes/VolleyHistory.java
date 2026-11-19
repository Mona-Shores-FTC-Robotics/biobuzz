package org.firstinspires.ftc.teamcode.opmodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Every volley this OpMode fired, for tuning the speed table from the Driver Station: when it went,
 * how many shots the launcher counted, the range they were taken from and the speed the launcher
 * was asked for. Fed one counted shot at a time ({@link #shot}); shots closer together than
 * {@link #VOLLEY_GAP_S} are one volley. The Auto shows it on the Match page, Basic Drive on the
 * Robot page, and both write {@link #summary} and every volley to the match log at STOP.
 *
 * <p>The lines are built when a shot arrives, never in the loop, so showing them every loop costs
 * nothing (CLAUDE.md, "Loop time").
 */
public final class VolleyHistory {

    /** A shot more than this long after the last one starts a new volley, seconds. A 4-ball feed is about 1.5 s. */
    public static final double VOLLEY_GAP_S = 1.5;

    private static final class Volley {
        final int number;
        final double startS;
        int shots;
        double minTarget = Double.NaN, maxTarget = Double.NaN;
        double rangeSum;
        int ranges;
        String source = "";
        double maxDip = Double.NaN;
        double lastS;
        String line = "";

        Volley(int number, double startS) {
            this.number = number;
            this.startS = startS;
            this.lastS = startS;
        }
    }

    private final List<Volley> volleys = new ArrayList<>();
    private int shots;
    private double minRange = Double.NaN, maxRange = Double.NaN;
    private double minTarget = Double.NaN, maxTarget = Double.NaN;
    private String summary = "Volleys: none yet";

    /**
     * One shot the launcher counted.
     *
     * @param tS       seconds since PLAY
     * @param targetTps the launcher's common target on the shot, ticks per second; NaN unknown
     * @param dipTps   how far the slower wheel fell below it, ticks per second; NaN unknown
     * @param rangeIn  range from the turret axis to the CELL, inches; NaN unknown
     * @param source   where the range came from ("camera", "pose"), short; null or empty unknown
     */
    public void shot(double tS, double targetTps, double dipTps, double rangeIn, String source) {
        Volley v = volleys.isEmpty() ? null : volleys.get(volleys.size() - 1);
        if (v == null || !(tS - v.lastS <= VOLLEY_GAP_S)) {
            v = new Volley(volleys.size() + 1, tS);
            volleys.add(v);
        }
        v.lastS = tS;
        v.shots++;
        shots++;
        v.minTarget = min(v.minTarget, targetTps);
        v.maxTarget = max(v.maxTarget, targetTps);
        minTarget = min(minTarget, targetTps);
        maxTarget = max(maxTarget, targetTps);
        if (!Double.isNaN(rangeIn)) {
            v.rangeSum += rangeIn;
            v.ranges++;
            minRange = min(minRange, rangeIn);
            maxRange = max(maxRange, rangeIn);
            if (v.source.isEmpty() && source != null) v.source = source;
        }
        v.maxDip = max(v.maxDip, dipTps);
        v.line = lineOf(v);
        summary = String.format(Locale.US, "Volleys: %d · %d shots · %s · %s", volleys.size(), shots,
                span(minRange, maxRange, " in"), span(minTarget, maxTarget, " ticks/s"));
    }

    /** How many volleys so far. */
    public int volleys() {
        return volleys.size();
    }

    /** One line for every volley so far: count, shots, range span, speed span. */
    public String summary() {
        return summary;
    }

    /** The last {@code max} volleys' lines, oldest first. */
    public void forEachRecent(int max, Consumer<String> out) {
        for (int i = Math.max(0, volleys.size() - max); i < volleys.size(); i++) out.accept(volleys.get(i).line);
    }

    /** Every volley's line, oldest first. */
    public void forEach(Consumer<String> out) {
        forEachRecent(Integer.MAX_VALUE, out);
    }

    private static String lineOf(Volley v) {
        String range = v.ranges == 0 ? "range ?" : String.format(Locale.US, "%.0f in%s", v.rangeSum / v.ranges,
                v.source.isEmpty() ? "" : " " + v.source);
        return String.format(Locale.US, "%d) %.1f s · %d shot%s · %s · %s%s", v.number, v.startS, v.shots,
                v.shots == 1 ? "" : "s", range, span(v.minTarget, v.maxTarget, " ticks/s"),
                Double.isNaN(v.maxDip) ? "" : String.format(Locale.US, " · dip %.0f", v.maxDip));
    }

    private static String span(double lo, double hi, String unit) {
        if (Double.isNaN(lo)) return "?" + unit;
        if (Math.round(lo) == Math.round(hi)) return String.format(Locale.US, "%.0f%s", lo, unit);
        return String.format(Locale.US, "%.0f–%.0f%s", lo, hi, unit);
    }

    private static double min(double a, double b) {
        return Double.isNaN(a) ? b : Double.isNaN(b) ? a : Math.min(a, b);
    }

    private static double max(double a, double b) {
        return Double.isNaN(a) ? b : Double.isNaN(b) ? a : Math.max(a, b);
    }
}
