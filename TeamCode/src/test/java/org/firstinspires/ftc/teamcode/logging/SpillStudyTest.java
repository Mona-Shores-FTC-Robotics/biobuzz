package org.firstinspires.ftc.teamcode.logging;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.DuoLzNorthAuto;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.DuoLzSouthAuto;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Where and when a TIP's spill lands: for every piece in the CELL that tips, the time from the
 * TIP starting to the piece first touching the tiles, where it lands, and where it is 2 s later.
 * The data behind where a robot should wait to catch a spill. Opt in:
 *
 * <pre>
 * BIOBUZZ_SPILL_STUDY=1 ./gradlew :TeamCode:testDebugUnitTest --tests '*SpillStudyTest*' -i
 * </pre>
 */
public class SpillStudyTest {

    static final class Track {
        final boolean north;
        final double startedAt;
        double landedAt = Double.NaN, landX, landY, restX = Double.NaN, restY;
        Track(boolean north, double startedAt) {
            this.north = north;
            this.startedAt = startedAt;
        }
    }

    @Test
    public void spills() throws Exception {
        if (System.getenv("BIOBUZZ_SPILL_STUDY") == null) return;
        List<Track> all = new ArrayList<>();
        for (long seed = 1; seed <= 10; seed++) {
            Map<FieldSim.Piece, Track> live = new HashMap<>();
            int[] started = {0};
            String pair = System.getenv("BIOBUZZ_SPILL_PAIR");
            String pkg = "org.firstinspires.ftc.teamcode.opmodes.auto.generated.";
            Class<?> a = pair == null ? DuoLzSouthAuto.class : Class.forName(pkg + pair.split(",")[0]);
            Class<?> b = pair == null ? DuoLzNorthAuto.class : Class.forName(pkg + pair.split(",")[1]);
            AutoSim sim = new AutoSim(a, Alliance.RED, seed).speed(50, 45).design(RobotDesign.springHood())
                    .alsoRun(b).speed(50, 45).design(RobotDesign.springHood());
            sim.observer = (field, now) -> {
                FieldSim.Rocker r = field.red;
                if (r.tipsStarted > started[0]) {
                    started[0] = r.tipsStarted;
                    for (FieldSim.Piece p : field.pieces) {
                        if (p.where == FieldSim.Where.FIELD && p.cell != null && p.cell.alliance() == Alliance.RED) {
                            Track t = new Track(p.y > FieldSim.CENTRE_IN, now);
                            live.put(p, t);
                            all.add(t);
                        }
                    }
                }
                for (Map.Entry<FieldSim.Piece, Track> e : live.entrySet()) {
                    FieldSim.Piece p = e.getKey();
                    Track t = e.getValue();
                    if (p.where != FieldSim.Where.FIELD) continue;
                    if (Double.isNaN(t.landedAt) && p.cell == null && p.z < p.kind.radius + 0.3) {
                        t.landedAt = now - t.startedAt;
                        t.landX = p.x;
                        t.landY = p.y;
                    }
                    if (!Double.isNaN(t.landedAt) && Double.isNaN(t.restX) && now - t.startedAt >= t.landedAt + 2) {
                        t.restX = p.x;
                        t.restY = p.y;
                    }
                }
            };
            sim.write(new File(TeamCodeDir.simLogs(), "spill-study.wpilog"));
        }
        for (boolean north : new boolean[] {false, true}) {
            int n = 0, landed = 0;
            double t = 0;
            int[][] land = new int[12][8], rest = new int[12][8];
            for (Track k : all) {
                if (k.north != north) continue;
                n++;
                if (Double.isNaN(k.landedAt)) continue;
                landed++;
                t += k.landedAt;
                bin(land, k.landX, k.landY);
                if (!Double.isNaN(k.restX)) bin(rest, k.restX, k.restY);
            }
            System.out.printf(Locale.ROOT, "SPILL %s CELL: %d pieces, %d landed, first touch %.2f s after the TIP starts%n",
                    north ? "north" : "south", n, landed, landed == 0 ? 0 : t / landed);
            print("SPILL   where they land", land);
            print("SPILL   where they are 2 s later", rest);
            int[][] fine = new int[12][10];
            for (Track k : all) {
                if (k.north != north || Double.isNaN(k.restX)) continue;
                double y0 = north ? 96 : 0;
                int row = (int) ((k.restY - y0) / 4), col = (int) ((k.restX - 36) / 4);
                if (row >= 0 && row < 12 && col >= 0 && col < 10) fine[row][col]++;
            }
            System.out.println("SPILL   2 s later, 4 in cells, x 36..76 across, y " + (north ? "96..144" : "0..48") + " up");
            for (int row = 11; row >= 0; row--) {
                StringBuilder line = new StringBuilder(String.format(Locale.ROOT, "SPILL   y%4.0f ", (north ? 96 : 0) + row * 4.0));
                for (int col = 0; col < 10; col++) line.append(fine[row][col] == 0 ? "   ." : String.format(Locale.ROOT, "%4d", fine[row][col]));
                System.out.println(line);
            }
        }
    }

    /** 12 rows of 12 in (y 0..144) by 8 columns of 12 in (x 0..96). */
    static void bin(int[][] g, double x, double y) {
        int row = (int) Math.max(0, Math.min(11, y / 12)), col = (int) Math.max(0, Math.min(7, x / 12));
        g[row][col]++;
    }

    static void print(String title, int[][] g) {
        System.out.println(title + " (rows y, 12 in each, north at top; columns x 0..96 in 12 in steps)");
        for (int row = 11; row >= 0; row--) {
            StringBuilder s = new StringBuilder(String.format(Locale.ROOT, "SPILL   y%3d-%3d ", row * 12, row * 12 + 12));
            for (int col = 0; col < 8; col++) s.append(g[row][col] == 0 ? "   ." : String.format(Locale.ROOT, "%4d", g[row][col]));
            System.out.println(s);
        }
    }
}
