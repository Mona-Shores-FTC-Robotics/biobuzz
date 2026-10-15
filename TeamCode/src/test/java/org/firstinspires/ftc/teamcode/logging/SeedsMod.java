package org.firstinspires.ftc.teamcode.logging;
import java.io.File;
import java.lang.reflect.Field;
/** Batch runs for tools/auto-routes/batch (not a test): seeds with robot 1's design changed by SEEDS_MOD ("field=value;field=value", RobotDesign fields); robot 2 keeps the named design. */
public class SeedsMod {
    public static void main(String[] a) throws Exception {
        String v = System.getenv("SEEDS_VARIETY"), sc = System.getenv("SEEDS_SCATTER");
        if (v != null) FieldSim.spillVariety = Double.parseDouble(v);
        if (sc != null) FieldSim.bounceScatter = Double.parseDouble(sc);
        RobotDesign base = AutoStudyTest.designs().get(a[2]), d = base.copy(a[2] + " mod"), p = AutoStudyTest.designs().get(a[2]);
        for (String kv : System.getenv().getOrDefault("SEEDS_MOD", "").split(";")) {
            if (kv.isEmpty()) continue;
            String[] x = kv.split("=");
            Field f = RobotDesign.class.getDeclaredField(x[0]); f.setAccessible(true);
            if (f.getType() == double.class) f.setDouble(d, Double.parseDouble(x[1]));
            else if (f.getType() == boolean.class) f.setBoolean(d, Boolean.parseBoolean(x[1]));
            else if (f.getType() == int.class) f.setInt(d, Integer.parseInt(x[1]));
            else throw new IllegalArgumentException(x[0]);
        }
        new File(a[4]).mkdirs();
        for (long seed = 1; seed <= Integer.parseInt(a[3]); seed++) {
            AutoSim.Result r = AutoStudyTest.run(a[0] + "," + a[1] + "@50", d, p, 50, seed, new File(a[4], seed + ".wpilog"));
            System.out.println("seed " + seed + " points " + r.autoPoints() + " tips " + r.autoTips());
        }
    }
}
