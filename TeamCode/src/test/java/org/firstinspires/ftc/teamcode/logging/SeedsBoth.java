package org.firstinspires.ftc.teamcode.logging;
import java.io.File;
import java.lang.reflect.Field;
/**
 * Batch runs for tools/auto-routes/batch (not a test). args: Auto, partner, design, last seed, out dir, [first seed]: both robots the named design changed by SEEDS_MOD, 50 in/s. */
public class SeedsBoth {
    static RobotDesign mod(RobotDesign base) throws Exception {
        RobotDesign d = base.copy(base.name + " mod");
        for (String kv : System.getenv().getOrDefault("SEEDS_MOD", "").split(";")) {
            if (kv.isEmpty()) continue;
            String[] x = kv.split("=");
            Field f = RobotDesign.class.getDeclaredField(x[0]); f.setAccessible(true);
            if (f.getType() == double.class) f.setDouble(d, Double.parseDouble(x[1]));
            else if (f.getType() == boolean.class) f.setBoolean(d, Boolean.parseBoolean(x[1]));
            else if (f.getType() == int.class) f.setInt(d, Integer.parseInt(x[1]));
            else throw new IllegalArgumentException(x[0]);
        }
        return d;
    }
    public static void main(String[] a) throws Exception {
        // BIOBUZZ_AUTO_ROBOT_RESTITUTION, as AutoStudyTest.study() reads it (study() only, so read here too).
        String e = System.getenv("BIOBUZZ_AUTO_ROBOT_RESTITUTION");
        FieldSim.robotRestitution = e == null ? FieldSim.ROBOT_RESTITUTION : Double.parseDouble(e);
        RobotDesign d = mod(AutoStudyTest.designs().get(a[2])), p = mod(AutoStudyTest.designs().get(a[2]));
        new File(a[4]).mkdirs();
        for (long seed = a.length > 5 ? Long.parseLong(a[5]) : 1; seed <= Integer.parseInt(a[3]); seed++) {
            AutoSim.Result r = AutoStudyTest.run(a[0] + "," + a[1] + "@50", d, p, 50, seed, new File(a[4], seed + ".wpilog"));
            System.out.println("seed " + seed + " points " + r.autoPoints() + " tips " + r.autoTips() + (Double.isNaN(r.robotsCollidedAt) ? "" : String.format(" COLLIDE %.1f", r.robotsCollidedAt - 1.0)));
        }
    }
}
