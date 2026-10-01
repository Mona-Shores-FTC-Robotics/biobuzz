package org.firstinspires.ftc.teamcode.logging;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The field at chosen moments of one simulated run, as JSON: every piece and every robot. For
 * drawing top-down pictures of where spills go and where robots stand
 * ({@code tools/auto-routes/snapshots.py} turns it into a PNG). Opt in:
 *
 * <pre>
 * BIOBUZZ_SNAPSHOT=HomeSouthAuto,HomeNorthAuto BIOBUZZ_SNAPSHOT_DESIGN="spring hood" \
 *   BIOBUZZ_SNAPSHOT_TIMES=12,13,14 ./gradlew :TeamCode:testDebugUnitTest --tests '*SnapshotTest*'
 * </pre>
 * Optional: BIOBUZZ_SNAPSHOT_SPEED (50), BIOBUZZ_SNAPSHOT_SEED (1). Writes
 * build/sim-logs/snapshots/snapshot.json.
 */
public class SnapshotTest {

    @Test
    public void snapshot() throws Exception {
        String autos = System.getenv("BIOBUZZ_SNAPSHOT");
        if (autos == null) return;
        String designName = env("BIOBUZZ_SNAPSHOT_DESIGN", "spring hood");
        double speed = Double.parseDouble(env("BIOBUZZ_SNAPSHOT_SPEED", "50"));
        long seed = Long.parseLong(env("BIOBUZZ_SNAPSHOT_SEED", "1"));
        List<Double> times = new ArrayList<>();
        for (String t : env("BIOBUZZ_SNAPSHOT_TIMES", "5,10,15,20,25,30").split(",")) times.add(Double.parseDouble(t));
        RobotDesign design = AutoStudyTest.designs().get(designName);
        String pkg = "org.firstinspires.ftc.teamcode.opmodes.auto.generated.";
        String[] names = autos.split(",");
        AutoSim sim = new AutoSim(Class.forName(pkg + names[0]), Alliance.RED, seed).speed(speed, speed * 0.9).design(design);
        if (names.length > 1) sim.alsoRun(Class.forName(pkg + names[1])).speed(speed, speed * 0.9).design(design);
        StringBuilder json = new StringBuilder("{\"autos\":\"" + autos + "\",\"design\":\"" + designName + "\",\"frames\":[");
        int[] next = {0};
        sim.observer = (field, now) -> {
            if (next[0] >= times.size() || now + 1e-9 < times.get(next[0])) return;
            if (next[0] > 0) json.append(',');
            json.append(String.format(Locale.ROOT, "{\"t\":%.2f,\"angle\":%.4f,\"pieces\":[", now, field.red.angle));
            boolean first = true;
            for (FieldSim.Piece p : field.pieces) {
                if (p.where != FieldSim.Where.FIELD) continue;
                json.append(first ? "" : ",").append(String.format(Locale.ROOT, "[\"%s\",%.1f,%.1f,%.1f,%d]",
                        p.kind.name(), p.x, p.y, p.z, p.cell != null ? 1 : 0));
                first = false;
            }
            json.append("],\"robots\":[");
            first = true;
            for (FieldSim.Bot b : field.bots) {
                double[] pose = b.pose();
                json.append(first ? "" : ",").append(String.format(Locale.ROOT, "[%.1f,%.1f,%.3f,%.1f,%.1f,%d]",
                        pose[0], pose[1], pose[2], b.design.frameIn, b.design.intakeWidthIn, b.stored.size()));
                first = false;
            }
            json.append("]}");
            next[0]++;
        };
        sim.write(new File(TeamCodeDir.simLogs(), "snapshot.wpilog"));
        json.append("]}");
        File dir = new File(TeamCodeDir.simLogs(), "snapshots");
        dir.mkdirs();
        File out = new File(dir, "snapshot.json");
        try (PrintWriter w = new PrintWriter(out)) {
            w.print(json);
        }
        System.out.println("SNAPSHOT " + out);
    }

    static String env(String k, String def) {
        String v = System.getenv(k);
        return v == null ? def : v;
    }
}
