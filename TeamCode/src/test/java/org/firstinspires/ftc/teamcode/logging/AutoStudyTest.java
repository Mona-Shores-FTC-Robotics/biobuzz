package org.firstinspires.ftc.teamcode.logging;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.PartnerThreeTipAuto;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Runs exported Autos on robot designs, several seeds each, and prints what each earns in AUTO and
 * the head start it leaves TELEOP. Opt in, listing Autos (two joined by a comma run together as an
 * alliance) with their drivetrain speed:
 *
 * <pre>
 * BIOBUZZ_AUTO_STUDY="SoloTwoTipAuto@40;Recycle3RightAuto,Recycle3LeftAuto@50" \
 *   BIOBUZZ_AUTO_DESIGNS="turret|spring hood" ./gradlew :TeamCode:testDebugUnitTest --tests '*AutoStudyTest*' -i
 * </pre>
 * The number after {@code @} is the drivetrain's top speed in in/s, accelerating and braking at 0.9 of it;
 * {@code @77/65/55} gives the top speed, the acceleration and the braking separately (in/s, in/s², in/s²: the
 * 25 Oct 2026 measurements of 20245, issue #155).
 * An Auto followed by {@code :blind} ({@code BackupLAuto:blind,PartnerLeftVAuto@50}) runs with its camera down
 * ({@link AutoSim#cameraDown}): its HIVE waits run to their time limits, as Smart Auto's BACKUP plan would.
 * Optional: {@code BIOBUZZ_AUTO_RUNS} (default 10); {@code BIOBUZZ_AUTO_PER_SEED} prints each seed's points and TIP times. PartnerThreeTipAuto gets its standing partner.
 * {@code BIOBUZZ_AUTO_VARIETY=0} turns off the field-frame variety (tile slopes, spill kicks, bounce scatter), which
 * makes a seed the same run on red and on blue: for checking that an Auto's blue version is its red one turned.
 */
public class AutoStudyTest {

    /** An 18 in wide right hook: a {@code length} in chassis and a {@code arm} in arm with its crossbeam. */
    static RobotDesign hook(String name, double length, double arm) {
        RobotDesign d = RobotDesign.springHoodFullWidth().copy(name);
        d.frameIn = length;
        d.flapForwardIn = arm;
        d.flapLeft = false;
        d.flapCrossbeam = true;
        d.flapsDeploy = true;
        d.flapTowardCentre = true;
        d.startBackedToWall = true;
        // It swings down (90 degrees in 0.3 s) as soon as our CELL starts to TIP, before the spill lands (mentor,
        // 5 Oct 2026: at 0.4 s, waiting for the robot to stop, it was still swinging as the pieces fell).
        d.sideWallsDeployS = 0;
        d.sideWallsTravelS = 0.3;
        return d;
    }

    /**
     * A spill guide on the Flat Intake, the baseline robot (mentor, 5-6 Oct 2026): its 14.5 in square chassis
     * and 14 in intake with ({@code arm} 0) fixed flaps from its front corners out to 18 in wide, the Rigid V,
     * or a one-armed hook ({@code arm} in, plus a crossbeam across its width) as {@link #hook} has: the Ramp
     * Hook, simulated as the 8 in hook until its ramp is designed. The Autos are drawn for it (qual_right.py),
     * so it starts where they say.
     */
    static RobotDesign flatIntakeWith(String name, double arm) {
        RobotDesign d = RobotDesign.flatIntake().copy(name);
        if (arm == 0) {
            d.flapOutIn = (18 - d.frameWidthIn) / 2;
            d.flapForwardIn = d.flapOutIn;
            return d.checked();
        }
        d.flapForwardIn = arm;
        d.flapLeft = false;
        d.flapCrossbeam = true;
        d.flapsDeploy = true;
        d.flapTowardCentre = true;
        d.sideWallsDeployS = 0;
        d.sideWallsTravelS = 0.3;
        return d.checked();
    }

    /**
     * The Rigid V's variants, {tips across (in), angle from straight ahead (degrees)}. R105: wider than 18 in, the robot
     * must stay within 18 in long, so wide flaps can't reach far forward (22 in only at 60 degrees); wider than the
     * 18 in start cube (R102) they fold in for the start and swing out, which the simulator doesn't model.
     */
    static final double[][] RIGID_V_VARIANTS = {{18, 30}, {18, 60}, {20, 45}, {20, 60}, {22, 60}};

    /** The Flat Intake with a Rigid V whose flap tips are {@code tipsIn} apart, each flap {@code angleDeg} from straight ahead. */
    static RobotDesign rigidV(double tipsIn, double angleDeg) {
        RobotDesign d = RobotDesign.flatIntake().copy(rigidVName(tipsIn, angleDeg));
        d.flapOutIn = (tipsIn - d.frameWidthIn) / 2;
        d.flapForwardIn = d.flapOutIn / Math.tan(Math.toRadians(angleDeg));
        return d.checked();
    }

    /**
     * The Rigid V as drawn on the robot (CAD session, 6 Oct 2026, cad/intake-b/, doc/robot-cad.md): body 15.12 in long (the
     * side rails, front to back) by 15.24 in wide (across the wheel shafts), a 13.8 in roller 1.94 in ahead of the face; 1/8 in aluminium flaps to tips 8.89 in from the centre,
     * 2.8 in ahead of the face (17.8 in apart). Here each flap runs from the front corner to that tip. Variants: the flap
     * height, its bounce, one flap or none.
     */
    static RobotDesign drawnV(String variant, double heightIn, double restitution, boolean left, boolean right) {
        RobotDesign d = RobotDesign.flatIntake().copy("flat intake, rigid V as drawn" + variant);
        d.frameIn = 15.12;
        d.frameWidthIn = 15.24;
        d.intakeWidthIn = 13.8;
        d.intakeReachIn = 1.94;
        if (left || right) {
            d.flapOutIn = 8.89 - d.frameWidthIn / 2;
            d.flapForwardIn = 2.8;
            d.flapHeightIn = heightIn;
            d.flapRestitution = restitution;
            d.flapLeft = left;
            d.flapRight = right;
        }
        return d.checked();
    }

    static String rigidVName(double tipsIn, double angleDeg) {
        return String.format(java.util.Locale.ROOT, "flat intake, rigid V %.0f in, %.0f deg", tipsIn, angleDeg);
    }

    /**
     * The designs the simulator offers: the real robot and its variants ({@link RobotDesign#real}). Until
     * 31 Oct 2026 there were 88 what-if designs (launchers, catchers, hooks, rigid Vs, extractors), all on the
     * 18 in start cube; they are in git history. A design is added here once the robot has it.
     */
    static Map<String, RobotDesign> designs() {
        Map<String, RobotDesign> m = new LinkedHashMap<>();
        RobotDesign real = RobotDesign.real(), locked = RobotDesign.realLocked();
        for (RobotDesign d : new RobotDesign[] {real, locked,
                RobotDesign.pollenOnly(real, RobotDesign.REAL_POLLEN),
                RobotDesign.pollenOnly(locked, RobotDesign.REAL_LOCKED_POLLEN)}) {
            m.put(d.name, d.checked());
        }
        return m;
    }

    /**
     * Where partner-leave-park sets its 4 preloads: a row along the field side of a robot at
     * (24, 132.25) facing the HIVE, so it can drive straight off to park without going round them
     * (mentor review), and we can drive up the row intake first.
     */
    static final double[][] LEAVE_PARTNER_STAGED = {{34.6, 128.6}, {34.6, 131.4}, {34.6, 134.2}, {34.6, 137.0}};

    /**
     * Where a staging partner sets its preloads, from its name: {@code PartnerStage<x><Side|Front>...},
     * a robot at (x, 132.25) facing the HIVE. Side: a row along its field side. Front: a row across its
     * front. Each touches the robot, as G304 asks of preloads left on the tiles. Null for other partners.
     */
    static double[][] stagedFor(String partnerClass) {
        if (partnerClass.equals("PartnerLeaveParkAuto")) return LEAVE_PARTNER_STAGED;
        if (partnerClass.equals("PartnerAngledParkAuto")) return alongLeftSide(ANGLED_PARTNER);
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("PartnerStage(\\d+)(Side|Front)").matcher(partnerClass);
        if (!m.lookingAt()) return null;
        double x = Double.parseDouble(m.group(1)), y = 132.25, gap = 9 + FieldSim.POLLEN_RADIUS_IN + 0.2;
        double[][] spots = new double[4][];
        for (int i = 0; i < 4; i++) {
            double along = (i - 1.5) * (2 * FieldSim.POLLEN_RADIUS_IN);
            spots[i] = m.group(2).equals("Side") ? new double[] {x + gap, y + along} : new double[] {x + along, y - gap};
        }
        return spots;
    }

    /**
     * partner-angled-park (tools/auto-routes/qual_right.py, mentor, 5 Oct 2026: a partner that can't
     * shoot or set pieces down): an 18 in robot angled with its back-right corner against the wall
     * behind the left CELL, aimed so that driving straight forward parks it in the far end of the
     * LOADING ZONE, its 4 POLLEN on the tiles along its left side. x, y, heading (deg), drawn for RED.
     */
    static final double[] ANGLED_PARTNER = {32.0, 128.53, 227.3};

    /** 4 POLLEN in a row along the left side of an 18 in robot at {x, y, heading}, each touching it. */
    static double[][] alongLeftSide(double[] pose) {
        double h = Math.toRadians(pose[2]), gap = 9 + FieldSim.POLLEN_RADIUS_IN + 0.2;
        double[] f = {Math.cos(h), Math.sin(h)}, l = {-Math.sin(h), Math.cos(h)};
        double[][] spots = new double[4][];
        for (int i = 0; i < 4; i++) {
            double along = (i - 1.5) * (2 * FieldSim.POLLEN_RADIUS_IN);
            spots[i] = new double[] {pose[0] + gap * l[0] + along * f[0], pose[1] + gap * l[1] + along * f[1]};
        }
        return spots;
    }

    static final String PKG = "org.firstinspires.ftc.teamcode.opmodes.auto.generated.";

    /** A spec's Auto may carry {@code :blind}: it runs with its camera down ({@link AutoSim#cameraDown}). */
    static final String BLIND = ":blind";

    static String autoName(String specAuto) {
        return specAuto.endsWith(BLIND) ? specAuto.substring(0, specAuto.length() - BLIND.length()) : specAuto;
    }

    static boolean cameraDown(String specAuto) {
        return specAuto.endsWith(BLIND);
    }

    static AutoSim.Result run(String spec, RobotDesign design, long seed, File file) throws Exception {
        // The partner can differ from us: BIOBUZZ_AUTO_PARTNER_SPEED and BIOBUZZ_AUTO_PARTNER_DESIGN.
        String ps = System.getenv("BIOBUZZ_AUTO_PARTNER_SPEED"), pd = System.getenv("BIOBUZZ_AUTO_PARTNER_DESIGN");
        return run(spec, design, pd == null ? null : designs().get(pd), ps == null ? Double.NaN : Double.parseDouble(ps), seed, file);
    }

    /** As {@link #run(String, RobotDesign, long, File)}, with the partner's design and speed (null and NaN: as ours). */
    static AutoSim.Result run(String spec, RobotDesign design, RobotDesign partnerDesign, double partnerSpeed, long seed, File file)
            throws Exception {
        // BIOBUZZ_AUTO_ALLIANCE=BLUE: our pair on blue (the Autos turned half a turn), the opponents, if any, on red.
        String a = System.getenv("BIOBUZZ_AUTO_ALLIANCE");
        return run(spec, design, partnerDesign, partnerSpeed, seed, a == null ? Alliance.RED : Alliance.valueOf(a), Collections.emptyMap(), file);
    }

    /**
     * As above, for {@code alliance} (the Autos are rotated when they were drawn for the other one),
     * with {@code metadata} added to the log's Metadata tab.
     */
    /**
     * BIOBUZZ_AUTO_DESIGN_SET="field=value,field=value": every robot's design with those RobotDesign fields set (a
     * what-if sweep: "intakeWidthIn=28,shotIntervalS=0.1,firesOnTheMove=true"), on a copy, so the named designs stay.
     */
    static RobotDesign withSet(RobotDesign design) throws Exception {
        String set = System.getenv("BIOBUZZ_AUTO_DESIGN_SET");
        if (design == null || set == null || set.isEmpty()) return design;
        RobotDesign d = design.checked();
        for (String kv : set.split(",")) {
            String[] p = kv.trim().split("=");
            java.lang.reflect.Field f = RobotDesign.class.getDeclaredField(p[0].trim());
            f.setAccessible(true);
            Class<?> t = f.getType();
            if (t == double.class) f.setDouble(d, Double.parseDouble(p[1].trim()));
            else if (t == int.class) f.setInt(d, Integer.parseInt(p[1].trim()));
            else if (t == boolean.class) f.setBoolean(d, Boolean.parseBoolean(p[1].trim()));
            else throw new IllegalArgumentException("BIOBUZZ_AUTO_DESIGN_SET: " + p[0] + " is not a number or a boolean");
        }
        return d;
    }

    /**
     * A spec's drive profile after its {@code @}: {@code "50"} (top speed; accelerate and brake at 0.9 of it),
     * {@code "77/65"} (top speed and acceleration, braking the same) or {@code "77/65/55"} (and braking), in/s and
     * in/s². Returned as {top speed, acceleration, braking}.
     */
    static double[] profile(String spec) {
        String[] p = spec.split("/");
        double v = Double.parseDouble(p[0]);
        double a = p.length > 1 ? Double.parseDouble(p[1]) : v * 0.9;
        double d = p.length > 2 ? Double.parseDouble(p[2]) : a;
        return new double[] {v, a, d};
    }

    static AutoSim.Result run(String spec, RobotDesign design, RobotDesign partnerDesign, double partnerSpeed, long seed,
                              Alliance alliance, Map<String, String> metadata, File file) throws Exception {
        design = withSet(design);
        partnerDesign = withSet(partnerDesign);
        String[] at = spec.split("@");
        // "@50": top speed 50 in/s, accelerating and braking at 0.9 of it (the Visualizer's preview is 50 and 45).
        // "@77/65/55" (25 Oct 2026): top speed, acceleration and braking as measured on the robot (issue #155).
        double[] profile = profile(at.length > 1 ? at[1] : "50");
        double speed = profile[0];
        // "A,B|C,D": our alliance's Autos, then the opponents' (mentor, 8 Oct 2026: all four robots, so the other
        // alliance's pieces, spills and traffic are there, and the blue versions of our Autos get run).
        String[] sides = at[0].split("\\|");
        String[] autos = sides[0].split(",");
        Class<?> first = Class.forName(PKG + autoName(autos[0]));
        AutoSim sim = new AutoSim(first, alliance, seed).speed(speed, profile[1], profile[2]).design(design);
        // The drive team's NECTAR into the LOADING ZONE after each TIP (G426) is on by default, for both alliances
        // (team decision, 10 Oct 2026). BIOBUZZ_AUTO_HUMAN_NECTAR=0 turns it off for a comparison.
        if ("0".equals(System.getenv("BIOBUZZ_AUTO_HUMAN_NECTAR"))) sim.humanNectar(false);
        // BIOBUZZ_AUTO_HUMAN_ENTRY=roll / BIOBUZZ_AUTO_HUMAN_BANK=N / BIOBUZZ_AUTO_HUMAN_CUE=1: how it goes in (see
        // AutoSim). Read here, not in the study loop, so a direct run() gets them too (the body-designs chat, 9 Oct).
        AutoSim.humanRolls = "roll".equals(System.getenv("BIOBUZZ_AUTO_HUMAN_ENTRY"));
        String bank = System.getenv("BIOBUZZ_AUTO_HUMAN_BANK");
        AutoSim.humanBank = bank == null ? 0 : Integer.parseInt(bank);
        AutoSim.humanCue = "1".equals(System.getenv("BIOBUZZ_AUTO_HUMAN_CUE"));
        // BIOBUZZ_AUTO_ROBOT_RESTITUTION=0.1: the robot's surfaces' bounce for a what-if (FieldSim.ROBOT_RESTITUTION
        // otherwise). Read here so a direct run() gets it too (the body-designs chat, 15 Oct 2026).
        String bounce = System.getenv("BIOBUZZ_AUTO_ROBOT_RESTITUTION");
        FieldSim.robotRestitution = bounce == null ? FieldSim.ROBOT_RESTITUTION : Double.parseDouble(bounce);
        if (cameraDown(autos[0])) sim.cameraDown();
        if (first == PartnerThreeTipAuto.class) {
            sim.partner(DesignComparisonTest.LEFT_PARTNER, DesignComparisonTest.LEFT_PARTNER_POLLEN);
        }
        if (autos.length > 1) {
            double[] pProfile = Double.isNaN(partnerSpeed) ? profile : profile(Double.toString(partnerSpeed));
            Class<?> second = Class.forName(PKG + autoName(autos[1]));
            sim.alsoRun(second).speed(pProfile[0], pProfile[1], pProfile[2]).design(partnerDesign == null ? design : partnerDesign);
            if (cameraDown(autos[1])) sim.cameraDown();
            // The reference partner that only leaves and parks sets its preloads out for us (mentor review).
            double[][] staged = stagedFor(second.getSimpleName());
            if (staged != null) sim.stagesPreloads(staged);
        }
        if (sides.length > 1) {
            String[] theirs = sides[1].split(",");
            for (int i = 0; i < theirs.length; i++) {
                Class<?> opponent = Class.forName(PKG + autoName(theirs[i]));
                // Their first robot like ours, their second like our partner.
                double[] s = i == 0 || Double.isNaN(partnerSpeed) ? profile : profile(Double.toString(partnerSpeed));
                sim.alsoRunOpponent(opponent).speed(s[0], s[1], s[2]).design(i == 0 || partnerDesign == null ? design : partnerDesign);
                if (cameraDown(theirs[i])) sim.cameraDown();
                double[][] staged = stagedFor(opponent.getSimpleName());
                if (staged != null) sim.stagesPreloads(staged);
            }
        }
        for (Map.Entry<String, String> m : metadata.entrySet()) sim.metadata(m.getKey(), m.getValue());
        return sim.write(file);
    }

    @Test
    public void study() throws Exception {
        String specs = System.getenv("BIOBUZZ_AUTO_STUDY");
        if (specs == null) return;
        // BIOBUZZ_AUTO_FRICTION scales rolling and contact friction (FieldSim.frictionScale), for asking
        // what changes if real pieces stop sooner than the sim's do.
        String friction = System.getenv("BIOBUZZ_AUTO_FRICTION");
        FieldSim.frictionScale = friction == null ? 1 : Double.parseDouble(friction);
        // BIOBUZZ_AUTO_TIP_SECONDS: every TIP this long (by default each is drawn from
        // FieldSim.tipSecondsRange, the 6 Oct 2026 videos' range, doc/tip-timing.md).
        // BIOBUZZ_AUTO_SPREAD scales the launcher's shot-to-shot spread (FieldSim.spreadScale; 1 = the placeholder,
        // about 5 shots in 6 scoring; 0 = every shot the same), for asking what a more accurate launcher buys.
        String spread = System.getenv("BIOBUZZ_AUTO_SPREAD");
        FieldSim.spreadScale = spread == null ? 1 : Double.parseDouble(spread);
        // BIOBUZZ_AUTO_AIM_DEG: fire once the robot faces the CELL this closely (2 by default); BIOBUZZ_AUTO_FIRE_STILL=1:
        // and only once it is still. Together with BIOBUZZ_AUTO_SPREAD=0 they are "every shot from a known position".
        String aim = System.getenv("BIOBUZZ_AUTO_AIM_DEG");
        AutoSim.AIM_TOLERANCE_RAD = Math.toRadians(aim == null ? 2 : Double.parseDouble(aim));
        AutoSim.fireOnlyWhenStill = "1".equals(System.getenv("BIOBUZZ_AUTO_FIRE_STILL"));
        String tip = System.getenv("BIOBUZZ_AUTO_TIP_SECONDS");
        double tipBefore = org.firstinspires.ftc.teamcode.vision.HiveTracker.Tuning.tipSeconds;
        double[] tipRange = FieldSim.tipSecondsRange;
        if (tip != null) {  // every TIP this long, instead of each drawn from FieldSim.tipSecondsRange
            org.firstinspires.ftc.teamcode.vision.HiveTracker.Tuning.tipSeconds = Double.parseDouble(tip);
            FieldSim.tipSecondsRange = null;
        }
        // BIOBUZZ_AUTO_TIP_DWELL=0: no dwell before a TIP (the simulator before issue #167), for before/after comparisons.
        double[] dwellRange = FieldSim.tipDwellRange;
        if ("0".equals(System.getenv("BIOBUZZ_AUTO_TIP_DWELL"))) FieldSim.tipDwellRange = null;
        // BIOBUZZ_AUTO_TURRET_DEG_PER_S: every turret's slew (0: aims at once, as the simulation did before 9 Oct 2026).
        String slew = System.getenv("BIOBUZZ_AUTO_TURRET_DEG_PER_S");
        AutoSim.turretSlewOverrideRadPerS = slew == null ? Double.NaN : Math.toRadians(Double.parseDouble(slew));
        // BIOBUZZ_AUTO_TURRET_TRAVEL_DEG: every turret's travel window (0: without limit, a slip ring).
        // BIOBUZZ_AUTO_CAPACITY: pieces a robot holds (4 is G407's limit; a what-if).
        String cap = System.getenv("BIOBUZZ_AUTO_CAPACITY");
        FieldSim.ROBOT_CAPACITY = cap == null ? 4 : Integer.parseInt(cap);
        String travel = System.getenv("BIOBUZZ_AUTO_TURRET_TRAVEL_DEG");
        AutoSim.turretTravelOverrideDeg = travel == null ? Double.NaN : Double.parseDouble(travel);
        if ("0".equals(System.getenv("BIOBUZZ_AUTO_VARIETY"))) { FieldSim.spillVariety = 0; FieldSim.bounceScatter = 0; }
        try {
            studyAll(specs);
        } finally {
            FieldSim.frictionScale = 1;
            FieldSim.spreadScale = 1;
            AutoSim.AIM_TOLERANCE_RAD = Math.toRadians(2);
            AutoSim.fireOnlyWhenStill = false;
            org.firstinspires.ftc.teamcode.vision.HiveTracker.Tuning.tipSeconds = tipBefore;
            FieldSim.tipSecondsRange = tipRange;
            FieldSim.tipDwellRange = dwellRange;
        }
    }

    /** One run's numbers, as the summary line adds them up (and as a child process hands them back). */
    static final class Row {
        long seed;
        double points, load, held;
        int g409, parked, robots, problems;
        String firstProblem = "";
        final List<Double> tips = new ArrayList<>();
        /** Our robot's intake misses by why, "interval=3,height=2" (AutoSim.RobotResult#misses). */
        String misses = "";
        /** Our alliance's shots launched and scored (every robot's). */
        int launched, scored;
        /** The other alliance's AUTO points when opponents ran; NaN otherwise. */
        double theirPoints = Double.NaN;

        String line() {
            StringBuilder t = new StringBuilder();
            for (double x : tips) t.append(t.length() == 0 ? "" : ",").append(x);
            return seed + "\t" + points + "\t" + load + "\t" + held + "\t" + g409 + "\t" + parked + "\t" + robots + "\t"
                    + problems + "\t" + t + "\t" + misses + "\t" + launched + "\t" + scored + "\t"
                    + firstProblem.replace('\t', ' ').replace('\n', ' ') + "\t" + theirPoints;
        }

        static Row parse(String[] f, int at) {
            Row r = new Row();
            r.seed = Long.parseLong(f[at]);
            r.points = Double.parseDouble(f[at + 1]);
            r.load = Double.parseDouble(f[at + 2]);
            r.held = Double.parseDouble(f[at + 3]);
            r.g409 = Integer.parseInt(f[at + 4]);
            r.parked = Integer.parseInt(f[at + 5]);
            r.robots = Integer.parseInt(f[at + 6]);
            r.problems = Integer.parseInt(f[at + 7]);
            if (f.length > at + 8 && !f[at + 8].isEmpty()) for (String x : f[at + 8].split(",")) r.tips.add(Double.parseDouble(x));
            r.misses = f.length > at + 9 ? f[at + 9] : "";
            r.launched = f.length > at + 10 ? Integer.parseInt(f[at + 10]) : 0;
            r.scored = f.length > at + 11 ? Integer.parseInt(f[at + 11]) : 0;
            r.firstProblem = f.length > at + 12 ? f[at + 12] : "";
            r.theirPoints = f.length > at + 13 ? Double.parseDouble(f[at + 13]) : Double.NaN;
            return r;
        }
    }

    /** A child process: runs its seeds and writes their rows here (BIOBUZZ_AUTO_CHILD_OUT) instead of summing. */
    public static void main(String[] args) throws Exception {
        new AutoStudyTest().study();
    }

    private void studyAll(String specs) throws Exception {
        String only = System.getenv("BIOBUZZ_AUTO_DESIGNS");
        String runsEnv = System.getenv("BIOBUZZ_AUTO_RUNS");
        int runs = runsEnv == null ? 10 : Integer.parseInt(runsEnv);
        // BIOBUZZ_AUTO_SEEDS="6,18": only these seeds (to write one run's log), instead of 1..runs.
        String seedList = System.getenv("BIOBUZZ_AUTO_SEEDS");
        long[] seeds = seedList != null
                ? java.util.Arrays.stream(seedList.split(",")).mapToLong(Long::parseLong).toArray()
                : java.util.stream.LongStream.rangeClosed(1, runs).toArray();
        List<Object[]> jobs = new ArrayList<>();  // {spec, design name, design}
        for (String spec : specs.split(";")) {
            for (Map.Entry<String, RobotDesign> e : designs().entrySet()) {
                if (only != null && !java.util.Arrays.asList(only.split("\\|")).contains(e.getKey())) continue;
                jobs.add(new Object[] {spec, e.getKey(), e.getValue()});
            }
        }
        String childOut = System.getenv("BIOBUZZ_AUTO_CHILD_OUT");
        int forks = Math.min(seeds.length, forks());
        if (childOut == null && forks > 1) {
            List<List<Row>> rows = inChildren(seeds, forks, jobs.size());
            for (int j = 0; j < jobs.size(); j++) summarize((String) jobs.get(j)[0], (String) jobs.get(j)[1], rows.get(j));
            return;
        }
        StringBuilder out = new StringBuilder();
        for (int j = 0; j < jobs.size(); j++) {
            List<Row> rows = runSeeds((String) jobs.get(j)[0], (String) jobs.get(j)[1], (RobotDesign) jobs.get(j)[2], seeds);
            if (childOut == null) {
                summarize((String) jobs.get(j)[0], (String) jobs.get(j)[1], rows);
            } else {
                for (Row r : rows) out.append(j).append('\t').append(r.line()).append('\n');
            }
        }
        if (childOut != null) Files.write(new File(childOut).toPath(), out.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * How many processes share a study's seeds (BIOBUZZ_AUTO_FORKS, default up to 4 of the machine's cores). The
     * simulation can't run two matches in one process (the robot code's command Scheduler is one static), so
     * each share is its own JVM on this test's classpath; same seeds, same results, a few times faster.
     */
    static int forks() {
        String f = System.getenv("BIOBUZZ_AUTO_FORKS");
        return f != null ? Integer.parseInt(f) : Math.min(4, Runtime.getRuntime().availableProcessors());
    }

    /** Runs the seeds in {@code forks} child processes and collects their rows, per job, in seed order. */
    private List<List<Row>> inChildren(long[] seeds, int forks, int jobs) throws Exception {
        String java = new File(System.getProperty("java.home"), "bin/java").getPath();
        List<Process> procs = new ArrayList<>();
        List<File> outs = new ArrayList<>(), logs = new ArrayList<>();
        for (int k = 0; k < forks; k++) {
            StringBuilder share = new StringBuilder();
            for (int i = k; i < seeds.length; i += forks) share.append(share.length() == 0 ? "" : ",").append(seeds[i]);
            File out = File.createTempFile("study-rows-", ".tsv"), log = File.createTempFile("study-out-", ".txt");
            ProcessBuilder pb = new ProcessBuilder(java, "-cp", System.getProperty("java.class.path"), AutoStudyTest.class.getName());
            pb.environment().put("BIOBUZZ_AUTO_SEEDS", share.toString());
            pb.environment().put("BIOBUZZ_AUTO_CHILD_OUT", out.getPath());
            pb.redirectErrorStream(true).redirectOutput(log);
            procs.add(pb.start());
            outs.add(out);
            logs.add(log);
        }
        List<List<Row>> rows = new ArrayList<>();
        for (int j = 0; j < jobs; j++) rows.add(new ArrayList<>());
        for (int k = 0; k < forks; k++) {
            int code = procs.get(k).waitFor();
            for (String l : Files.readAllLines(logs.get(k).toPath(), StandardCharsets.UTF_8)) if (l.contains("STUDY")) System.out.println(l);
            if (code != 0) {
                throw new IllegalStateException("study child " + k + " failed:\n"
                        + String.join("\n", Files.readAllLines(logs.get(k).toPath(), StandardCharsets.UTF_8)));
            }
            for (String l : Files.readAllLines(outs.get(k).toPath(), StandardCharsets.UTF_8)) {
                if (l.isEmpty()) continue;
                String[] f = l.split("\t", -1);
                rows.get(Integer.parseInt(f[0])).add(Row.parse(f, 1));
            }
            outs.get(k).delete();
            logs.get(k).delete();
        }
        for (List<Row> r : rows) r.sort((x, y) -> Long.compare(x.seed, y.seed));
        return rows;
    }

    /** Runs one Auto pair on one design for these seeds, in this process. */
    private List<Row> runSeeds(String spec, String designName, RobotDesign design, long[] seeds) throws Exception {
        List<Row> rows = new ArrayList<>();
        for (long seed : seeds) {
            File file = new File(TeamCodeDir.simLogs(), "study-" + spec.replaceAll("[^A-Za-z0-9]+", "-")
                    + "-" + designName.replaceAll("[^A-Za-z0-9]+", "-") + "-" + seed + ".wpilog");
            AutoSim.Result r = run(spec, design, seed, file);
            // A study's per-seed logs (about 4 MB each; a day's studies filled 24 GB on 6 Oct 2026) are kept only when
            // asked: BIOBUZZ_AUTO_LOGS=1. The published logs come from the Simulate Auto workflow, not from here.
            if (!"1".equals(System.getenv("BIOBUZZ_AUTO_LOGS")) && !file.delete()) file.deleteOnExit();
            if (System.getenv("BIOBUZZ_AUTO_PER_SEED") != null) {
                System.out.printf(Locale.ROOT, "STUDY   seed %d: %d pts, TIPs at %s%n", seed, r.autoPoints(), r.tipsAt);
            }
            String tl = System.getenv("BIOBUZZ_AUTO_TIMELINE");
            if (tl != null && tl.isEmpty()) tl = null;  // BIOBUZZ_AUTO_TIMELINE= (a script's empty default) means unset
            if (tl != null && (tl.equals("1") ? seed == 1 : tl.equals("all") || (tl.equals("fail") ? (r.robots.stream().anyMatch(x -> !x.park)) : !tl.equals("issues") && Long.parseLong(tl) == seed))) {
                System.out.println("STUDY   seed " + seed + ": " + r);
                for (AutoSim.RobotResult robot : r.robots) {
                    for (String t : robot.timeline) System.out.println("STUDY     " + robot.auto + " " + t);
                }
            }
            Row row = new Row();
            row.seed = seed;
            for (int i = 0; i < r.autoTips(); i++) row.tips.add(r.tipsAt.get(i));
            row.points = r.autoPoints();
            if (r.opponents) row.theirPoints = r.theirAutoPoints();
            row.g409 = r.g409;
            row.load = r.cellLoad;
            row.held = r.held;
            StringBuilder m = new StringBuilder();
            for (Map.Entry<String, Integer> e : r.robots.get(0).misses.entrySet()) {
                m.append(m.length() == 0 ? "" : ",").append(e.getKey()).append('=').append(e.getValue());
            }
            row.misses = m.toString();
            row.scored = r.scored;
            for (AutoSim.RobotResult robot : r.robots) if (robot.alliance == r.alliance) row.launched += robot.launched;
            for (AutoSim.RobotResult robot : r.robots) {
                if (robot.alliance == r.alliance) {  // the parked count is our alliance's; problems count on both sides
                    row.robots++;
                    if (robot.leave && robot.park) row.parked++;
                }
                if (robot.illegalStart != null || !Double.isNaN(robot.crossedAt) || !Double.isNaN(robot.hitHiveAt)
                        || !Double.isNaN(robot.hitFlowerAt) || !Double.isNaN(robot.hitWallAt)) {
                    if (row.problems++ == 0) row.firstProblem = robot.toString();
                }
            }
            if (!Double.isNaN(r.robotsCollidedAt) && row.problems++ == 0) {
                row.firstProblem = String.format(Locale.ROOT, "robots collide at %.1f s", r.robotsCollidedAt);
            }
            // A G409 touch is a foul every time and STRATEGIC when routine (the Q&A's Q17 and Q23, doc/game/qa-review.md,
            // 14 Oct 2026): a run with one is a problem run, not a risk to weigh.
            if (r.g409 > 0 && row.problems++ == 0) {
                row.firstProblem = String.format(Locale.ROOT, "G409: %d spilled piece%s touched before the tiles", r.g409, r.g409 == 1 ? "" : "s");
            }
            // BIOBUZZ_AUTO_SEED_ROWS=1: one line per seed for a script to count (TIPs, points, each robot's PARK and
            // problems, the robots' collision).
            if (System.getenv("BIOBUZZ_AUTO_SEED_ROWS") != null) {
                StringBuilder sr = new StringBuilder(String.format(Locale.ROOT, "STUDY SEEDROW %s|%s|%d|%d|%d|%s", spec, designName,
                        seed, r.autoTips(), r.autoPoints(), Double.isNaN(r.robotsCollidedAt) ? "-" : String.format(Locale.ROOT, "%.1f", r.robotsCollidedAt)));
                if (r.opponents) {  // a whole match: the other alliance's TIPs and points, and when the alliances first met
                    sr.append(String.format(Locale.ROOT, "|vs|%d|%d|%s", r.theirTipsAt.size(), r.theirAutoPoints(),
                            Double.isNaN(r.alliancesCollidedAt) ? "-" : String.format(Locale.ROOT, "%.1f", r.alliancesCollidedAt)));
                }
                for (AutoSim.RobotResult robot : r.robots) {
                    sr.append('|').append(robot.leave && robot.park ? "P" : "-")
                            .append(Double.isNaN(robot.hitHiveAt) ? "" : "H").append(Double.isNaN(robot.hitFlowerAt) ? "" : "F")
                            .append(Double.isNaN(robot.hitWallAt) ? "" : "W").append(Double.isNaN(robot.crossedAt) ? "" : "C")
                            .append(robot.illegalStart == null ? "" : "S");
                }
                System.out.println(sr);
            }
            // BIOBUZZ_AUTO_TIMELINE=issues: the timeline of every run with a G409 touch or a problem.
            if ("issues".equals(tl) && (row.g409 > 0 || row.problems > 0)) {
                System.out.println("STUDY   seed " + seed + ": " + r);
                for (AutoSim.RobotResult robot : r.robots) {
                    for (String t : robot.timeline) System.out.println("STUDY     " + robot.auto + " " + t);
                }
            }
            rows.add(row);
        }
        return rows;
    }

    /** The STUDY line for one Auto pair on one design: its runs added up. */
    private static void summarize(String spec, String designName, List<Row> rows) {
        int[] count = new int[8];
        double[] sum = new double[8];
        double points = 0, load = 0, held = 0, theirPoints = 0;
        int theirRuns = 0;
        int parked = 0, robots = 0, problems = 0, g409 = 0, g409Runs = 0, runs = rows.size();
        Map<String, Integer> misses = new java.util.TreeMap<>();
        int launched = 0, scored = 0;
        String firstProblem = null;
        for (Row r : rows) {
            for (int i = 0; i < r.tips.size() && i < count.length; i++) {
                count[i]++;
                sum[i] += r.tips.get(i);
            }
            points += r.points;
            if (!Double.isNaN(r.theirPoints)) {
                theirPoints += r.theirPoints;
                theirRuns++;
            }
            g409 += r.g409;
            if (r.g409 > 0) g409Runs++;
            load += r.load;
            held += r.held;
            parked += r.parked;
            robots += r.robots;
            problems += r.problems;
            if (firstProblem == null && !r.firstProblem.isEmpty()) firstProblem = r.firstProblem;
            launched += r.launched;
            scored += r.scored;
            if (!r.misses.isEmpty()) {
                for (String kv : r.misses.split(",")) {
                    String[] p = kv.split("=");
                    misses.merge(p[0], Integer.parseInt(p[1]), Integer::sum);
                }
            }
        }
        if (!misses.isEmpty()) {
            StringBuilder m = new StringBuilder("STUDY   misses per run (intake, and shots that fell):");
            for (Map.Entry<String, Integer> e : misses.entrySet()) {
                m.append(String.format(Locale.ROOT, " %s %.1f", e.getKey(), (double) e.getValue() / runs));
            }
            System.out.println(m);
        }
        if (firstProblem != null) System.out.println("STUDY   first problem: " + firstProblem);
        StringBuilder line = new StringBuilder(String.format(Locale.ROOT, "%-36s %-28s", spec, designName));
        for (int i = 0; i < 6 && count[i] > 0; i++) {
            line.append(String.format(Locale.ROOT, " TIP%d %2d/%d@%4.1f", i + 1, count[i], runs, sum[i] / count[i]));
        }
        line.append(String.format(Locale.ROOT, " | %.1f pts, parked %d/%d, CELL %.0f%%, held %.1f, G409 %.1f (%d runs)%s",
                points / runs, parked, robots, 100 * load / runs, held / runs, (double) g409 / runs, g409Runs,
                problems == 0 ? "" : ", PROBLEMS " + problems)
                + (theirRuns == 0 ? "" : String.format(Locale.ROOT, ", the other alliance %.1f pts", theirPoints / theirRuns))
                + (launched == 0 ? "" : String.format(Locale.ROOT, ", shots %.0f%% of %.1f", 100.0 * scored / launched, (double) launched / runs)));
        System.out.println("STUDY " + line);
    }
}
