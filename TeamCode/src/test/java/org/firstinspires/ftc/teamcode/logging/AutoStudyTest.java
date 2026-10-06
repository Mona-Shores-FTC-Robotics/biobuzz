package org.firstinspires.ftc.teamcode.logging;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.PartnerThreeTipAuto;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

import java.io.File;
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
 * Optional: {@code BIOBUZZ_AUTO_RUNS} (default 10); {@code BIOBUZZ_AUTO_PER_SEED} prints each seed's points and TIP times. PartnerThreeTipAuto gets its standing partner.
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
     * The spill shapes on option 3, the baseline robot (mentor, 5 Oct 2026): its 14.5 in square chassis and
     * 14 in intake with a one-armed hook ({@code arm} in, plus a crossbeam across its width) as {@link #hook}
     * has, or ({@code arm} 0) fixed flaps from its front corners out to 18 in wide, a rigid V. The Autos are
     * drawn for it (qual_right.py), so it starts where they say.
     */
    static RobotDesign option3Shape(String name, double arm) {
        return option3Shape(name, arm, arm == 0);
    }

    /** As above, with ({@code rigidV}) or without the rigid V's guides; a hook ({@code arm} over 0) can have both. */
    static RobotDesign option3Shape(String name, double arm, boolean rigidV) {
        RobotDesign d = RobotDesign.buildersOption3().copy(name);
        if (rigidV) {
            d.guideOutIn = (18 - d.frameWidthIn) / 2;
            d.guideForwardIn = d.guideOutIn;
        }
        if (arm == 0) return d.checked();
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
     * Option 3 shortened to 12.5 in (mentor, 6 Oct 2026: "we would just make the chassis shorter"), its width and
     * intake as built, with the rigid V and, {@code arm} over 0, a hook that long: 12.5 + 11.5 = 24 in, R105's limit.
     * The shorter chassis lets the robot sit 2 in further from the spill, so the V's tips clear it, with the hook's
     * crossbeam where the 14.5 in robot's was.
     */
    static RobotDesign shortOption3(String name, double arm) {
        RobotDesign d = RobotDesign.buildersOption3().copy(name);
        d.frameIn = 12.5;
        d.guideOutIn = (18 - d.frameWidthIn) / 2;
        d.guideForwardIn = d.guideOutIn;
        if (arm > 0) {
            d.flapForwardIn = arm;
            d.flapLeft = false;
            d.flapCrossbeam = true;
            d.flapsDeploy = true;
            d.flapTowardCentre = true;
            d.sideWallsDeployS = 0;
            d.sideWallsTravelS = 0.3;
        }
        return d.checked();
    }

    static Map<String, RobotDesign> designs() {
        Map<String, RobotDesign> m = new LinkedHashMap<>();
        m.put("turret", RobotDesign.standard());
        m.put("spring hood", RobotDesign.springHood());
        RobotDesign slow = RobotDesign.springHood().copy("spring hood, 3 s spin-up");
        slow.spinUpS = 3.0;
        m.put(slow.name, slow);
        RobotDesign ratio = RobotDesign.springHood().copy("spring hood, NECTAR 5% slow");
        ratio.pollenSpeedFactor = 1.025;
        ratio.nectarSpeedFactor = 0.975;
        m.put(ratio.name, ratio);
        RobotDesign twin = RobotDesign.springHood().copy("two spring hoods");
        twin.launchers = 2;
        m.put(twin.name, twin);
        m.put("spring hood, full-width intake", RobotDesign.springHoodFullWidth());
        RobotDesign proto = RobotDesign.buildersPrototype();
        m.put(proto.name, proto);
        // What the prototype's narrow intake costs: the same robot with an intake 90% of its frame.
        RobotDesign protoWide = proto.copy(proto.name + ", 13.5 in intake");
        protoWide.intakeWidthIn = 0.9 * protoWide.frameIn;
        m.put(protoWide.name, protoWide);
        RobotDesign option3 = RobotDesign.buildersOption3();
        m.put(option3.name, option3);
        // Mentor, 5 Oct 2026: walls down both sides that slide 6 in forward when our CELL starts to
        // TIP, so the spill doesn't scatter, with one-way flaps that let POLLEN in and keep NECTAR out.
        RobotDesign walls = RobotDesign.springHoodFullWidth().copy("spring hood, full-width intake, side walls");
        walls.sideWallsSlideIn = RobotAssets.WALL_SLIDE_IN;
        m.put(walls.name, walls);
        // The same walls out as soon as our CELL starts to TIP (SideWallSpillTest: the spill scatters
        // the moment it lands, so walls that wait for it to land catch no more than no walls).
        RobotDesign early = walls.copy("spring hood, full-width intake, side walls out at the TIP");
        early.sideWallsDeployS = 0;
        m.put(early.name, early);
        // The spill shapes (sim-review/body-shapes-shortlist.png, mentor 5 Oct 2026), on the same robot.
        // A rigid V: a 14 in wide, 16 in long chassis with fixed flaps out to an 18 x 18 in outline.
        RobotDesign rigid = RobotDesign.springHoodFullWidth().copy("spring hood, rigid V");
        rigid.frameIn = 16;
        rigid.frameWidthIn = 14;
        rigid.intakeWidthIn = 14;
        rigid.flapOutIn = 2;
        rigid.flapForwardIn = 2;
        rigid.startBackedToWall = true;
        m.put(rigid.name, rigid);
        // Right hooks: one arm and a crossbeam that pivot down 0.4 s after our CELL starts to TIP (the
        // spill lands 1.1-1.4 s after), on the side facing the centre line; folded while driving.
        RobotDesign largeHook = hook("spring hood, large right hook", 14, 10);
        m.put(largeHook.name, largeHook);
        RobotDesign smallHook = hook("spring hood, small right hook", 16, 8);
        m.put(smallHook.name, smallHook);
        // The same on option 3: a 9.5 in arm is the longest R105 allows down (14.5 + 9.5 = 24 in).
        for (RobotDesign d : new RobotDesign[] {option3Shape("option 3, rigid V", 0),
                option3Shape("option 3, large right hook", 9.5), option3Shape("option 3, small right hook", 8),
                option3Shape("option 3, rigid V + large right hook", 9.5, true),
                shortOption3("option 3 short, rigid V", 0), shortOption3("option 3 short, rigid V + large right hook", 11.5)}) {
            m.put(d.name, d);
        }
        RobotDesign catcher = RobotDesign.springHood().copy("spring hood, 24 in catcher");
        catcher.intakeWidthIn = 24;
        m.put(catcher.name, catcher);
        RobotDesign twinCatcher = twin.copy("two spring hoods, 24 in catcher");
        twinCatcher.intakeWidthIn = 24;
        m.put(twinCatcher.name, twinCatcher);
        RobotDesign twinFull = twin.copy("two spring hoods, full-width intake");
        twinFull.intakeWidthIn = 18;
        m.put(twinFull.name, twinFull);
        RobotDesign dedicated = twinCatcher.copy("POLLEN + NECTAR launchers, 24 in catcher");
        dedicated.dedicatedLaunchers = true;
        m.put(dedicated.name, dedicated);
        // A catapult throws everything it holds at once; laid out in a pattern across a wide arm its
        // pieces spread less and collide less than a plain one's. Re-cocking stands in for spin-up.
        RobotDesign cat = RobotDesign.springHood().copy("patterned catapult, 24 in catcher");
        cat.launcher = RobotDesign.Launcher.CATAPULT;
        cat.fixedPitchDeg = 60;
        cat.spinUpS = 0.8;
        cat.catapultSpread = 1.0;
        cat.catapultSideIn = 4.5;
        cat.intakeWidthIn = 24;
        m.put(cat.name, cat);
        RobotDesign plainCat = cat.copy("plain catapult, 24 in catcher");
        plainCat.catapultSpread = 2.0;
        plainCat.catapultSideIn = 2.5;
        m.put(plainCat.name, plainCat);
        // As a real arm throws: the clump shares one error and stays together (see catapultClump).
        RobotDesign clumpCat = cat.copy("clump catapult, 24 in catcher");
        clumpCat.catapultClump = true;
        m.put(clumpCat.name, clumpCat);
        // Steeper, so it drops into the opening from where the spring-hood Autos fire (31-44 in back).
        RobotDesign steepCat = clumpCat.copy("clump catapult 72 deg, 24 in catcher");
        steepCat.fixedPitchDeg = 72;
        m.put(steepCat.name, steepCat);
        RobotDesign triangle = steepCat.copy("clump catapult 72 deg, triangle cup");
        triangle.catapultCup = RobotDesign.Cup.TRIANGLE;
        m.put(triangle.name, triangle);
        // What could flip the catapult's result: the clump not staying together, a slow re-cock, the angle off.
        RobotDesign loose = steepCat.copy("clump catapult 72 deg, loose clump");
        loose.catapultResidual = 1.0;
        m.put(loose.name, loose);
        RobotDesign slowCock = steepCat.copy("clump catapult 72 deg, 1.5 s re-cock");
        slowCock.spinUpS = 1.5;
        m.put(slowCock.name, slowCock);
        for (double deg : new double[] {68, 76}) {
            RobotDesign off = steepCat.copy("clump catapult " + (int) deg + " deg, 24 in catcher");
            off.fixedPitchDeg = deg;
            m.put(off.name, off);
        }
        // Mentor review: a 24 in catcher can't go through the tunnel (23.7 in between the foot bar and
        // the centre line). How much does the width buy over one that fits?
        RobotDesign narrow = twinCatcher.copy("two spring hoods, 20 in intake");
        narrow.intakeWidthIn = 20;
        m.put(narrow.name, narrow);
        // So the designs the review set uses: the frame's own width (mentor decision, 1 Oct 2026: no
        // 24 in catcher for now; it hits the foot bar or reaches over the centre line in the tunnel).
        for (RobotDesign base : new RobotDesign[] {steepCat, triangle}) {
            RobotDesign f = base.copy(base.name.replace(", 24 in catcher", "") + ", full-width intake");
            f.intakeWidthIn = 18;
            m.put(f.name, f);
        }
        // Mentor review: would a turret, an intake at the back, or no piece counter change the answer?
        for (RobotDesign base : new RobotDesign[] {twinCatcher, twinFull}) {
            RobotDesign turret = base.copy(base.name + ", turret");
            turret.launcher = RobotDesign.Launcher.TURRET;
            m.put(turret.name, turret);
            RobotDesign back = base.copy(base.name + ", intake at back");
            back.intakeAtBack = true;
            m.put(back.name, back);
            RobotDesign blind = base.copy(base.name + ", no piece counter");
            blind.countsPieces = false;
            m.put(blind.name, blind);
        }
        // Mentor, 3 Oct 2026: slats that flip the launcher to throw straight back, instead of a turret.
        RobotDesign both = m.get("clump catapult 72 deg, triangle cup, full-width intake").copy(
                "clump catapult 72 deg, triangle cup, full-width intake, shoots both ways");
        both.launchesBothWays = true;
        m.put(both.name, both);
        // Mentor review: do we need to take and fire NECTAR as well as POLLEN? The same robots, POLLEN only.
        for (RobotDesign base : new RobotDesign[] {twinCatcher, triangle, twinFull, m.get("clump catapult 72 deg, triangle cup, full-width intake")}) {
            RobotDesign c = base.copy(base.name + ", POLLEN only");
            c.launchesNectar = false;
            m.put(c.name, c);
        }
        for (RobotDesign base : new RobotDesign[] {catcher, twinCatcher}) {
            RobotDesign c = base.copy(base.name + ", fires on the move");
            c.compensatesMotion = true;
            m.put(c.name, c);
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

    static AutoSim.Result run(String spec, RobotDesign design, long seed, File file) throws Exception {
        // The partner can differ from us: BIOBUZZ_AUTO_PARTNER_SPEED and BIOBUZZ_AUTO_PARTNER_DESIGN.
        String ps = System.getenv("BIOBUZZ_AUTO_PARTNER_SPEED"), pd = System.getenv("BIOBUZZ_AUTO_PARTNER_DESIGN");
        return run(spec, design, pd == null ? null : designs().get(pd), ps == null ? Double.NaN : Double.parseDouble(ps), seed, file);
    }

    /** As {@link #run(String, RobotDesign, long, File)}, with the partner's design and speed (null and NaN: as ours). */
    static AutoSim.Result run(String spec, RobotDesign design, RobotDesign partnerDesign, double partnerSpeed, long seed, File file)
            throws Exception {
        return run(spec, design, partnerDesign, partnerSpeed, seed, Alliance.RED, Collections.emptyMap(), file);
    }

    /**
     * As above, for {@code alliance} (the Autos are rotated when they were drawn for the other one),
     * with {@code metadata} added to the log's Metadata tab.
     */
    static AutoSim.Result run(String spec, RobotDesign design, RobotDesign partnerDesign, double partnerSpeed, long seed,
                              Alliance alliance, Map<String, String> metadata, File file) throws Exception {
        String[] at = spec.split("@");
        double speed = at.length > 1 ? Double.parseDouble(at[1]) : 50;
        String[] autos = at[0].split(",");
        Class<?> first = Class.forName(PKG + autos[0]);
        AutoSim sim = new AutoSim(first, alliance, seed).speed(speed, speed * 0.9).design(design);
        if (first == PartnerThreeTipAuto.class) {
            sim.partner(DesignComparisonTest.LEFT_PARTNER, DesignComparisonTest.LEFT_PARTNER_POLLEN);
        }
        if (autos.length > 1) {
            double pSpeed = Double.isNaN(partnerSpeed) ? speed : partnerSpeed;
            Class<?> second = Class.forName(PKG + autos[1]);
            sim.alsoRun(second).speed(pSpeed, pSpeed * 0.9).design(partnerDesign == null ? design : partnerDesign);
            // The reference partner that only leaves and parks sets its preloads out for us (mentor review).
            double[][] staged = stagedFor(second.getSimpleName());
            if (staged != null) sim.stagesPreloads(staged);
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
        try {
            studyAll(specs);
        } finally {
            FieldSim.frictionScale = 1;
        }
    }

    private void studyAll(String specs) throws Exception {
        String only = System.getenv("BIOBUZZ_AUTO_DESIGNS");
        String runsEnv = System.getenv("BIOBUZZ_AUTO_RUNS");
        int runs = runsEnv == null ? 10 : Integer.parseInt(runsEnv);
        for (String spec : specs.split(";")) {
            for (Map.Entry<String, RobotDesign> e : designs().entrySet()) {
                if (only != null && !java.util.Arrays.asList(only.split("\\|")).contains(e.getKey())) continue;
                int[] count = new int[8];
                double[] sum = new double[8];
                double points = 0, load = 0, held = 0;
                int parked = 0, robots = 0, problems = 0, g409 = 0, g409Runs = 0;
                // BIOBUZZ_AUTO_SEEDS="6,18": only these seeds (to write one run's log), instead of 1..runs.
                String seedList = System.getenv("BIOBUZZ_AUTO_SEEDS");
                long[] seeds = seedList != null
                        ? java.util.Arrays.stream(seedList.split(",")).mapToLong(Long::parseLong).toArray()
                        : java.util.stream.LongStream.rangeClosed(1, runs).toArray();
                runs = seeds.length;
                for (long seed : seeds) {
                    File file = new File(TeamCodeDir.simLogs(), "study-" + spec.replaceAll("[^A-Za-z0-9]+", "-")
                            + "-" + e.getKey().replaceAll("[^A-Za-z0-9]+", "-") + "-" + seed + ".wpilog");
                    AutoSim.Result r = run(spec, e.getValue(), seed, file);
                    if (System.getenv("BIOBUZZ_AUTO_PER_SEED") != null) {
                        System.out.printf(Locale.ROOT, "STUDY   seed %d: %d pts, TIPs at %s%n", seed, r.autoPoints(), r.tipsAt);
                    }
                    String tl = System.getenv("BIOBUZZ_AUTO_TIMELINE");
                    if (tl != null && (tl.equals("1") ? seed == 1 : tl.equals("fail") ? (r.robots.stream().anyMatch(x -> !x.park)) : Long.parseLong(tl) == seed)) {
                        System.out.println("STUDY   seed " + seed + ": " + r);
                        for (AutoSim.RobotResult robot : r.robots) {
                            for (String t : robot.timeline) System.out.println("STUDY     " + robot.auto + " " + t);
                        }
                    }
                    for (int i = 0; i < r.autoTips(); i++) {
                        count[i]++;
                        sum[i] += r.tipsAt.get(i);
                    }
                    points += r.autoPoints();
                    g409 += r.g409;
                    if (r.g409 > 0) g409Runs++;
                    load += r.cellLoad;
                    held += r.held;
                    for (AutoSim.RobotResult robot : r.robots) {
                        robots++;
                        if (robot.leave && robot.park) parked++;
                        if (robot.illegalStart != null || !Double.isNaN(robot.crossedAt) || !Double.isNaN(robot.hitHiveAt)
                                || !Double.isNaN(robot.hitFlowerAt)) {
                            if (problems++ == 0) System.out.println("STUDY   first problem: " + robot);
                        }
                    }
                    if (!Double.isNaN(r.robotsCollidedAt) && problems++ == 0) {
                        System.out.printf(Locale.ROOT, "STUDY   first problem: robots collide at %.1f s%n", r.robotsCollidedAt);
                    }
                }
                StringBuilder line = new StringBuilder(String.format(Locale.ROOT, "%-36s %-28s", spec, e.getKey()));
                for (int i = 0; i < 6 && count[i] > 0; i++) {
                    line.append(String.format(Locale.ROOT, " TIP%d %2d/%d@%4.1f", i + 1, count[i], runs, sum[i] / count[i]));
                }
                line.append(String.format(Locale.ROOT, " | %.1f pts, parked %d/%d, CELL %.0f%%, held %.1f, G409 %.1f (%d runs)%s",
                        points / runs, parked, robots, 100 * load / runs, held / runs, (double) g409 / runs, g409Runs,
                        problems == 0 ? "" : ", PROBLEMS " + problems));
                System.out.println("STUDY " + line);
            }
        }
    }
}
