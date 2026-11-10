package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import org.firstinspires.ftc.teamcode.logging.WpiLogReader;
import org.firstinspires.ftc.teamcode.subsystems.LauncherSubsystem.ShotDetector;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Master's {@link ShotDetector} replayed over the Belding Qualifier's match logs (7 Nov 2026), volley by volley,
 * against the balls counted leaving the robot on the event video: {@code doc/robot-todo.md} §7c, "Is the shot count
 * good enough to end a feed?". A count above the video ends a {@code LaunchAll} feed with a ball still in the robot;
 * one below only keeps the feed to its set time.
 *
 * <p>A volley is one gate-open stretch ({@code /FeedGate/Feeding}). The detector is fed every logged loop the way
 * {@code LauncherSubsystem.update()} feeds it: reset while the wheels are idle or reversing, the at-speed band from
 * {@link LauncherSubsystem#toleranceTicksPerSecond}, the count depth {@link LauncherSubsystem#shotDipTicksPerSecond}.
 * A volley's count is the shots counted from the gate opening to its shutting, as {@code LaunchAll} reads it.
 *
 * <p>Opt-in, since it reads the logs branch: fetch it
 * ({@code git fetch origin 'refs/heads/events/*:refs/remotes/origin/events/*'}) and set
 * {@code BIOBUZZ_SHOT_REPLAY=1} (reads {@code origin/events/2026-11-belding} through {@code git show}), or set it to a
 * directory holding that branch's files ({@code <team>/matches/<log>.wpilog.gz}). Prints every volley of every
 * match log, then the video table.
 *
 * <p>Result (10 Nov 2026, with the level seeded once the wheels hold the target): no volley counted above the
 * video; 12 of the 14 clearly counted volleys exact (the 8 Nov detector: 11; the Belding rule's logged count: 9),
 * the other two one short: Q-14's 2nd volley (a 36 ticks/s dip, as deep as the shut-gate noise) and M-10's TeleOp
 * single ball (no dip at all). In Q-7's 2nd volley, the one with ball times, each count comes 0.13–0.17 s after its
 * ball leaves, never before.
 */
public class ShotCountVideoTest {

    static final String REF = "origin/events/2026-11-belding";

    /** One volley counted on video. */
    static final class Seen {
        final String match;
        final int team;
        final String log;
        final boolean auto;
        /**
         * TeleOp: the gate opening, seconds after the TeleOp's PLAY, to within {@link #MATCH_S}. Auto: unused, the
         * volley is the Auto's 1st, 2nd or 3rd gate opening, as the note begins.
         */
        final double atS;
        /** Balls that left, fewest and most the video allows. */
        final int min;
        final int max;
        /** Counted clearly enough to judge "exactly right". */
        final boolean clear;
        final String note;

        Seen(String match, int team, String log, boolean auto, double atS, int min, int max, boolean clear, String note) {
            this.match = match;
            this.team = team;
            this.log = log;
            this.auto = auto;
            this.atS = atS;
            this.min = min;
            this.max = max;
            this.clear = clear;
            this.note = note;
        }
    }

    /** A video volley's time and the logged gate opening agree this well, s. */
    static final double MATCH_S = 1.5;

    static final String Q7 = "20245/matches/2026-11-07_11-55-58_R-Solo_11-05_8_09PM.wpilog.gz";
    static final String Q14 = "20245/matches/2026-11-07_13-19-36_L-NoCross_11-05_8_03PM.wpilog.gz";
    static final String M3 = "20245/matches/2026-11-07_16-22-23_L-NoCross_11-05_8_03PM.wpilog.gz";
    static final String M10_20245 = "20245/matches/2026-11-07_17-40-34_L-NoCross_11-05_8_03PM.wpilog.gz";
    static final String Q13 = "19429/matches/2026-11-07_13-20-32_R-Solo_11-05_8_09PM.wpilog.gz";
    static final String Q4 = "19429/matches/2026-11-07_11-36-28_R-Solo_11-05_8_09PM.wpilog.gz";
    static final String M7 = "19429/matches/2026-11-07_17-04-49_L-NoCross_11-05_8_03PM.wpilog.gz";
    static final String M10_19429 = "19429/matches/2026-11-07_17-52-07_L-NoCross_11-05_8_03PM.wpilog.gz";
    static final String Q32 = "19429/matches/2026-11-07_15-20-08_R-Solo_11-05_8_09PM.wpilog.gz";

    /** Hand-counted from the event video (mentor, 9 Nov 2026). Times: the logged gate opening, after PLAY. */
    static final Seen[] VIDEO = {
            new Seen("Q-7", 20245, Q7, true, 0, 4, 4, true, "preload"),
            new Seen("Q-7", 20245, Q7, true, 0, 5, 5, true, "2nd; balls out at 18.13, 18.50, 18.96, 19.40, 19.86 s"),
            new Seen("Q-14", 20245, Q14, true, 0, 3, 4, false, "preload, at least 3"),
            new Seen("Q-14", 20245, Q14, true, 0, 4, 4, true, "2nd"),
            new Seen("M-3", 20245, M3, true, 0, 3, 5, false, "1st, about 4"),
            new Seen("M-3", 20245, M3, true, 0, 4, 4, true, "2nd"),
            new Seen("M-10", 20245, M10_20245, false, 46.5, 1, 1, true, "TeleOp 46.5 s"),
            new Seen("M-10", 20245, M10_20245, false, 50.5, 4, 4, true, "TeleOp 50.5 s"),
            new Seen("Q-13", 19429, Q13, true, 0, 4, 4, true, "preload"),
            new Seen("Q-13", 19429, Q13, true, 0, 1, 1, true, "2nd"),
            new Seen("Q-13", 19429, Q13, true, 0, 4, 4, true, "3rd"),
            new Seen("Q-13", 19429, Q13, false, 28, 4, 4, true, "TeleOp 28 s"),
            new Seen("Q-4", 19429, Q4, true, 0, 0, 0, true, "2nd"),
            new Seen("Q-4", 19429, Q4, true, 0, 0, 0, true, "3rd"),
            new Seen("M-7", 19429, M7, true, 0, 4, 4, true, "2nd"),
            new Seen("M-10", 19429, M10_19429, true, 0, 3, 4, false, "2nd, 3 (likely 4)"),
            new Seen("M-10", 19429, M10_19429, false, 25.1, 0, 0, true, "TeleOp 25.1 s"),
            new Seen("Q-32", 19429, Q32, false, 89, 3, 4, false, "TeleOp after the launcher restart, 89–91.5 s"),
    };

    /** One gate-open stretch, replayed. */
    static final class Volley {
        boolean auto;
        double openS;
        double closeS;
        int oldCount;
        int newCount;
        /** Times of the new detector's counts, s after the phase's PLAY. */
        final List<Double> countsAtS = new ArrayList<>();
        int afterClose;
    }

    /** A match log, replayed: its volleys in order, Auto's first. */
    static final class Replay {
        final List<Volley> volleys = new ArrayList<>();
        double autoPlayS = Double.NaN;
        double teleOpPlayS = Double.NaN;
        final List<String> fedEvents = new ArrayList<>();
    }

    @Test
    public void theShotCountNeverExceedsTheVideo() throws IOException {
        String source = System.getenv("BIOBUZZ_SHOT_REPLAY");
        assumeTrue("set BIOBUZZ_SHOT_REPLAY=1 (or a directory of " + REF + ") to replay the Belding logs",
                source != null && !source.isEmpty());

        Map<String, Replay> replays = new LinkedHashMap<>();
        for (Seen s : VIDEO) {
            if (!replays.containsKey(s.log)) replays.put(s.log, replay(new WpiLogReader(read(source, s.log))));
        }
        for (Map.Entry<String, Replay> e : replays.entrySet()) print(e.getKey(), e.getValue());

        System.out.println();
        System.out.println(String.format(Locale.US, "%-5s %-6s %-7s %-6s %5s %5s %5s  %s",
                "match", "team", "phase", "open s", "video", "old", "new", "note"));
        int over = 0;
        int clear = 0;
        int exact = 0;
        int oldExact = 0;
        List<String> overs = new ArrayList<>();
        for (Seen s : VIDEO) {
            Volley v = find(replays.get(s.log), s);
            assertTrue("no logged volley for " + s.match + " " + s.team + " " + s.note, v != null);
            String video = s.min == s.max ? String.valueOf(s.min) : s.min + "–" + s.max;
            System.out.println(String.format(Locale.US, "%-5s %-6d %-7s %6.2f %5s %5d %5d  %s counts at %s",
                    s.match, s.team, s.auto ? "Auto" : "TeleOp", v.openS, video, v.oldCount, v.newCount, s.note,
                    times(v.countsAtS)));
            if (v.newCount > s.max) {
                over++;
                overs.add(s.match + " " + s.team + " " + s.note);
            }
            if (s.clear) {
                clear++;
                if (v.newCount == s.min) exact++;
                if (v.oldCount == s.min) oldExact++;
            }
        }
        System.out.println(String.format(Locale.US, "over the video: %d; clearly counted: %d, new exact %d, old exact %d",
                over, clear, exact, oldExact));
        assertTrue("counted more than left the robot: " + overs, over == 0);
    }

    static Volley find(Replay r, Seen s) {
        if (!s.auto) {
            Volley best = null;
            for (Volley v : r.volleys) {
                if (v.auto || Math.abs(v.openS - s.atS) > MATCH_S) continue;
                if (best == null || Math.abs(v.openS - s.atS) < Math.abs(best.openS - s.atS)) best = v;
            }
            return best;
        }
        int ordinal = s.note.startsWith("preload") || s.note.startsWith("1st") ? 1
                : s.note.startsWith("2nd") ? 2 : s.note.startsWith("3rd") ? 3 : 0;
        int n = 0;
        for (Volley v : r.volleys) {
            if (v.auto && ++n == ordinal) return v;
        }
        return null;
    }

    static Replay replay(WpiLogReader log) {
        Replay r = new Replay();
        for (WpiLogReader.Record e : log.entry("/Events").records) {
            String text = e.asString();
            double t = e.timestampUs / 1e6;
            if (text.startsWith("PLAY: field forward")) {
                if (Double.isNaN(r.teleOpPlayS)) r.teleOpPlayS = t;
            } else if (text.matches("PLAY: \\S+ alliance.*") && Double.isNaN(r.autoPlayS) && Double.isNaN(r.teleOpPlayS)) {
                r.autoPlayS = t;
            }
            if (text.contains(" fed ") && text.contains("shots detected")) {
                r.fedEvents.add(String.format(Locale.US, "%7.2f s  %s", t, text));
            }
        }
        // A TeleOp-only log: its first alliance PLAY was the TeleOp's.
        if (!Double.isNaN(r.autoPlayS) && !Double.isNaN(r.teleOpPlayS) && Math.abs(r.autoPlayS - r.teleOpPlayS) < 0.5) {
            r.autoPlayS = Double.NaN;
        }

        List<WpiLogReader.Record> left = log.entry("/Launcher/LeftTicksPerSecond").records;
        List<WpiLogReader.Record> right = log.entry("/Launcher/RightTicksPerSecond").records;
        List<WpiLogReader.Record> target = log.entry("/Launcher/TargetTicksPerSecond").records;
        List<WpiLogReader.Record> gate = log.entry("/FeedGate/Feeding").records;
        List<WpiLogReader.Record> reversing = log.entry("/Launcher/Reversing").records;
        List<WpiLogReader.Record> logged = log.entry("/Launcher/Shots").records;
        int n = Math.min(Math.min(left.size(), right.size()), Math.min(target.size(), gate.size()));
        n = Math.min(n, Math.min(reversing.size(), logged.size()));

        ShotDetector d = new ShotDetector();
        boolean wasReversing = false;
        boolean wasOpen = false;
        Volley open = null;
        Volley lastClosed = null;
        double lastCloseS = Double.NaN;
        int oldAtOpen = 0;
        for (int i = 0; i < n; i++) {
            long us = left.get(i).timestampUs;
            double t = us / 1e6;
            double l = number(log, "/Launcher/LeftTicksPerSecond", left.get(i));
            double rt = number(log, "/Launcher/RightTicksPerSecond", right.get(i));
            double tg = number(log, "/Launcher/TargetTicksPerSecond", target.get(i));
            boolean g = number(log, "/FeedGate/Feeding", gate.get(i)) > 0.5;
            boolean rev = number(log, "/Launcher/Reversing", reversing.get(i)) > 0.5;
            int old = (int) Math.round(number(log, "/Launcher/Shots", logged.get(i)));
            boolean counted = false;
            if (rev != wasReversing) {
                wasReversing = rev;
                d.reset();
            }
            if (!rev) {
                if (tg > 0.0) {
                    double tol = LauncherSubsystem.toleranceTicksPerSecond;
                    boolean within = Math.abs(l - tg) <= tol && Math.abs(rt - tg) <= tol;
                    counted = d.update(tg, l, tg, rt, within, g, tg, LauncherSubsystem.shotDipTicksPerSecond,
                            us / 1000L);
                } else {
                    d.reset();
                }
            }
            if (g && !wasOpen) {
                open = new Volley();
                open.auto = inAuto(r, t);
                open.openS = t - playFor(r, open.auto);
                oldAtOpen = old;
            }
            if (counted && open != null) open.countsAtS.add(t - playFor(r, open.auto));
            if (counted && open == null && lastClosed != null && t - lastCloseS <= 0.4) lastClosed.afterClose++;
            if (!g && wasOpen && open != null) {
                open.closeS = t - playFor(r, open.auto);
                open.oldCount = old - oldAtOpen;
                open.newCount = open.countsAtS.size();
                r.volleys.add(open);
                lastClosed = open;
                lastCloseS = t;
                open = null;
            }
            wasOpen = g;
        }
        return r;
    }

    static boolean inAuto(Replay r, double t) {
        return !Double.isNaN(r.autoPlayS) && t >= r.autoPlayS && (Double.isNaN(r.teleOpPlayS) || t < r.teleOpPlayS);
    }

    static double playFor(Replay r, boolean auto) {
        double p = auto ? r.autoPlayS : r.teleOpPlayS;
        return Double.isNaN(p) ? 0.0 : p;
    }

    /** A logged sample as a number, whatever type the log wrote it as (the robot logs flags as doubles). */
    static double number(WpiLogReader log, String key, WpiLogReader.Record rec) {
        switch (log.entry(key).type) {
            case "int64":
                return rec.asInt64();
            case "boolean":
                return rec.asBoolean() ? 1.0 : 0.0;
            case "float":
                return java.nio.ByteBuffer.wrap(rec.payload).order(java.nio.ByteOrder.LITTLE_ENDIAN).getFloat(0);
            default:
                return rec.asDouble();
        }
    }

    static void print(String name, Replay r) {
        System.out.println();
        System.out.println(name + String.format(Locale.US, "  (Auto PLAY %.2f s, TeleOp PLAY %.2f s)",
                r.autoPlayS, r.teleOpPlayS));
        for (Volley v : r.volleys) {
            System.out.println(String.format(Locale.US, "  %-6s open %6.2f  shut %6.2f  (%.2f s)  old %d  new %d%s  at %s",
                    v.auto ? "Auto" : "TeleOp", v.openS, v.closeS, v.closeS - v.openS, v.oldCount, v.newCount,
                    v.afterClose > 0 ? "  (+" + v.afterClose + " just after shutting)" : "", times(v.countsAtS)));
        }
        for (String f : r.fedEvents) System.out.println("  event " + f);
    }

    static String times(List<Double> ts) {
        StringBuilder b = new StringBuilder("[");
        for (int i = 0; i < ts.size(); i++) b.append(i == 0 ? "" : ", ").append(String.format(Locale.US, "%.2f", ts.get(i)));
        return b.append("]").toString();
    }

    static byte[] read(String source, String path) throws IOException {
        byte[] gz;
        if ("1".equals(source)) {
            Process p = new ProcessBuilder("git", "show", REF + ":" + path).redirectErrorStream(false).start();
            gz = all(p.getInputStream());
            try {
                if (p.waitFor() != 0) throw new IOException("git show " + REF + ":" + path + " failed (fetch the branch)");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException(e);
            }
        } else {
            gz = Files.readAllBytes(new File(source, path).toPath());
        }
        try (InputStream in = new GZIPInputStream(new java.io.ByteArrayInputStream(gz))) {
            return all(in);
        }
    }

    static byte[] all(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1 << 16];
        int k;
        while ((k = in.read(buf)) > 0) out.write(buf, 0, k);
        return out.toByteArray();
    }
}
