package org.firstinspires.ftc.teamcode.logging;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.CommandBuilder;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.api.Paths;
import com.pedropathing.ivy.commands.Commands;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;

import org.firstinspires.ftc.teamcode.autokit.AutoDrive;
import org.firstinspires.ftc.teamcode.autokit.AutoKit;
import org.firstinspires.ftc.teamcode.autokit.AutoRegistry;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.vision.HiveState;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Runs an Autonomous exported by the Auto Builder, unchanged, against {@link FieldSim}, and writes
 * what happens to a {@code .wpilog}: does this Auto tip the HIVE, from which spot, and when?
 *
 * <p>The Auto is the generated class itself, built with its own {@code build(kit, rotated)} and
 * run through autokit and Ivy's real {@link Scheduler}, exactly as {@code BuiltAuto} runs it on the
 * robot. Only what is underneath is simulated:
 * <ul>
 *   <li><b>Driving</b> ({@link SimDrive}): each path is Pedro's own {@code Path}, driven along its
 *       length with a trapezoidal speed profile, the same approximation {@code VisualizerPath}
 *       uses. The real follower carries speed through joins, so real timing differs a little.</li>
 *   <li><b>Commands and triggers</b> ({@code Bot.registry}): every name a generated Auto may use, backed
 *       by the simulation. The robot starts holding its 4 preloaded POLLEN (Competition Manual
 *       §10.3.4) and never holds more than 4 (G407). The intake runs whenever the robot has room,
 *       unless the Auto turns it off. A launch aims at the alliance's raised CELL from wherever the
 *       robot is (as if the robot aims); whether it goes in is up to the physics. {@code Tip} is
 *       true once the alliance's HIVE has started to tip since the wait began, as the robot's
 *       {@code HiveTracker} reports it, but from the simulation's truth rather than a camera.</li>
 *   <li><b>The other robot</b> ({@link #alsoRun}): an alliance's second robot can run its own Auto on
 *       the same field at the same time; the log shows it as {@code /Odometry/Partner3d} and both
 *       robots together as {@link FieldRobot#ALL_3D}. A partner that stands still
 *       ({@link #partner}) is logged the same way. Both are
 *       judged for LEAVE and AUTO PARK when AUTO ends (§10.5.4), and the run notes it if they ever
 *       overlap or one reaches into the other alliance's half (G402).</li>
 * </ul>
 * An Auto that needs a name not simulated here fails at the start and lists it, as the robot
 * would at INIT.
 *
 * <p>{@code AutoSimTest} runs every exported Auto for both alliances and writes
 * {@code TeamCode/build/sim-logs/auto-<name>-<alliance>.wpilog}.
 */
public final class AutoSim {

    static final double LOOP_S = 0.020;
    /**
     * Logged past the end of the Auto: the 8 s between AUTO and TELEOP (Competition Manual §10.1).
     * A TIP that completes in it still counts for AUTO (§10.5 B), so a shot launched just before
     * 30 s can still score its TIP.
     */
    static final double AFTER_S = 8.0;
    /** When after a TIP starts its spill is counted ({@link Result#tipSpills}): landed and scattered, not yet driven into. */
    static final double SPILL_LOOK_S = 3.0;
    /**
     * Disabled time on each side of the run, robots standing where they are, so AdvantageScope's
     * timeline has room to grab the start and the end. AUTO starts at {@code PRE_ROLL_S} in the log.
     * 1 s each (mentor, 4 Oct 2026; was 10 s and 7 s): enough to grab, without scrolling past idle robots.
     */
    static final double PRE_ROLL_S = 1.0;
    static final double POST_ROLL_S = 1.0;
    /** How often the {@code /Match/} clock is logged. */
    static final double CLOCK_STEP_S = 0.1;

    // Drivetrain profile: the Visualizer log's defaults. A robot that is not tuned yet; the Auto
    // Builder previews at 60 in/s and 55 in/s², which {@link #speed} can set.
    static final double MAX_SPEED_IN_PER_S = 40;
    static final double ACCEL_IN_PER_S2 = 30;

    // Mechanisms: not measured, like the rest of the simulated robot.
    /** How long after a TIP a drive-team member gets a NECTAR into the LOADING ZONE. */
    static final double HUMAN_DELAY_S = 2.0;
    /** A frame-fixed launcher launches once the robot faces the CELL this closely. */
    static double AIM_TOLERANCE_RAD = Math.toRadians(2);
    /** Fire only once the robot is still (under 1 in/s and 2 deg/s): "known positions, no disruption" (mentor, 6 Oct 2026). */
    static boolean fireOnlyWhenStill = false;

    // The webcam CollectSeen drives by (the robot's PieceVisionSubsystem): it looks the way the
    // intake faces and sees loose pieces on the tiles. Placeholders, like the rest of the robot.
    static final double CAMERA_HALF_FOV_RAD = Math.toRadians(35);
    /** Beyond this from the raised CELL the launcher holds fire: past ~56 in nothing scores (ShotMapTest). */
    static final double MAX_SHOT_RANGE_IN = 60;
    static final double CAMERA_RANGE_IN = 60;
    /** A start pose's frame corner this near the perimeter counts as touching the wall. */
    static final double START_WALL_IN = 1.0;

    /** CollectSeen stays off the walls this long before AUTO ends, so it never costs LEAVE. */
    static final double WALL_CLEAR_S = 1.5;

    /** How long a reversed intake takes to set down each piece it holds (a guess; time one). */
    static final double SET_DOWN_INTERVAL_S = 0.25;
    /**
     * How fast {@code Outtake} pushes a piece out of the intake's mouth, in/s relative to the robot (a guess:
     * film the reversed intake). With the placeholder rolling friction a POLLEN pushed out at 20 in/s rolls about
     * 5 in on normal tiles and under 2 in on 3x tiles. Not final: StagedPreloadsTest sweeps it.
     */
    static double placeholderOuttakeInPerS = 20;

    /** CollectSeen keeps within this far of where it started, so it does not wander off. */
    static final double COLLECT_RADIUS_IN = 36;
    /**
     * With nothing in view, CollectSeen turns this far to look before giving up (mentor review: a
     * full turn on the spot looked lost, and a webcam already sees 70 deg).
     */
    static final double LOOK_AROUND_RAD = Math.toRadians(90);

    /** What one robot did in a run. */
    static final class RobotResult {
        final String auto;
        int launched;
        boolean finished;
        double finishedAt = Double.NaN;
        final List<String> decisions = new ArrayList<>();
        /** The decisions again, each with the time it happened, for reading where the time goes. */
        final List<String> timeline = new ArrayList<>();
        final List<double[]> poses = new ArrayList<>();
        /** Pieces at this robot's intake that were not taken, by why (FieldSim "miss: ..." events, one per piece per 0.3 s). */
        final java.util.Map<String, Integer> misses = new java.util.TreeMap<>();
        /** LEAVE (Competition Manual §10.5.4): not touching the perimeter wall when AUTO ends. */
        boolean leave;
        /** AUTO PARK (§10.5.4): at least partly in the alliance's LOADING ZONE when AUTO ends. */
        boolean park;
        /**
         * When the robot first reached into the other alliance's half, which G402 calls risky in
         * AUTO; NaN if it never did.
         */
        double crossedAt = Double.NaN;
        /**
         * Why its start is not legal, or null: a robot starts touching the perimeter wall, on its own
         * alliance's half (mentor, 3 Oct 2026).
         */
        String illegalStart;
        /** When the robot first ran into the HIVE frame's feet, which a real one cannot; NaN if never. */
        double hitHiveAt = Double.NaN;
        /** When the robot's body first overlapped a FLOWER holder, which a real one cannot; NaN if never. */
        double hitFlowerAt = Double.NaN;

        RobotResult(String auto) {
            this.auto = auto;
        }

        @Override
        public String toString() {
            return String.format(Locale.ROOT, "%s launched %d, %s, LEAVE %s, PARK %s%s", auto, launched,
                    finished ? String.format(Locale.ROOT, "finished at %.1f s", finishedAt) : "still running at 30 s",
                    leave ? "yes" : "no", park ? "yes" : "no", (illegalStart == null ? "" : ", ILLEGAL START: " + illegalStart)
                            + (Double.isNaN(crossedAt) ? ""
                            : String.format(Locale.ROOT, ", CROSSES THE CENTRE LINE at %.1f s", crossedAt))
                            + (Double.isNaN(hitHiveAt) ? ""
                            : String.format(Locale.ROOT, ", DRIVES INTO THE HIVE FRAME at %.1f s", hitHiveAt))
                            + (Double.isNaN(hitFlowerAt) ? ""
                            : String.format(Locale.ROOT, ", DRIVES INTO A FLOWER at %.1f s", hitFlowerAt)));
        }
    }

    /** What one run did, for a test or a person to read. */
    static final class Result {
        final String auto;
        final Alliance alliance;
        int launched;
        int scored;
        /** When each TIP completed, including in the transition after AUTO, where they still count. */
        final List<Double> tipsAt = new ArrayList<>();
        /** Each robot's own account, in the order they were added. */
        final List<RobotResult> robots = new ArrayList<>();
        /** When two robots first overlapped, which real robots cannot; NaN if they never did. */
        double robotsCollidedAt = Double.NaN;
        /** Spilled pieces a robot touched before they reached the tiles: G409 fouls. */
        int g409;
        /**
         * Each of our CELL's TIPs, in order: {pieces it spilled, those lying on the other alliance's half
         * {@link #SPILL_LOOK_S} after it started} (for the spill shapes: a right hook's arm is there to keep
         * them on ours). A TIP whose 3 s ran past the end of the log is not listed.
         */
        final List<int[]> tipSpills = new ArrayList<>();
        /**
         * When TELEOP starts: loose pieces on the tiles on the alliance's own half (what a hook keeps there for
         * TELEOP), not counting pieces in a CELL or held.
         */
        int ourHalfLoose;
        /**
         * Each of our CELL's TIPs, in order (whoever caused it): {when it started (s, AUTO time), the pieces in the
         * CELL then, how many of those our robot (the first Auto) picked up after it started, how many the other
         * robot did, seconds from the start to our robot's 4th (NaN if it never had 4)}. "Keep 4": the plan is to
         * pick up 4 of each spill and fire them at the other CELL.
         */
        final List<double[]> tipKeeps = new ArrayList<>();
        /**
         * When TELEOP starts: how far the pieces already in the alliance's raised CELL go toward the
         * next TIP (1 = enough), and how many pieces the alliance's robots hold. AUTO scores only
         * TIPs, LEAVE and PARK, so this is what an Auto's spare seconds can still buy.
         */
        double cellLoad;
        int held;
        // The first robot's, kept here for runs with only one.
        boolean finished;
        double finishedAt = Double.NaN;
        final List<String> decisions;
        final List<String> timeline;
        final List<double[]> poses;

        Result(String auto, Alliance alliance, List<String> autos) {
            this.auto = auto;
            this.alliance = alliance;
            for (String a : autos) robots.add(new RobotResult(a));
            decisions = robots.get(0).decisions;
            timeline = robots.get(0).timeline;
            poses = robots.get(0).poses;
        }

        /** TIPs that count for AUTO: complete before TELEOP starts (§10.5 B). */
        int autoTips() {
            int n = 0;
            for (double t : tipsAt) if (t < AutoKit.AUTO_LENGTH_S + AFTER_S) n++;
            return n;
        }

        /** The head start TELEOP gets, in words. */
        String headStart() {
            return String.format(Locale.ROOT, "TELEOP starts with the raised CELL %.0f%% loaded and %d held", 100 * cellLoad, held);
        }

        /** AUTO points from TIPs, LEAVE and PARK (Table 10-2); CELL and GARDEN points count later. */
        int autoPoints() {
            int points = 20 * autoTips();
            for (RobotResult r : robots) points += (r.leave ? 3 : 0) + (r.park ? 5 : 0);
            return points;
        }

        @Override
        public String toString() {
            StringBuilder tips = new StringBuilder();
            for (double t : tipsAt) tips.append(String.format(Locale.ROOT, " %.1f s", t));
            String head = String.format(Locale.ROOT, "%s %s: launched %d, scored %d, HIVE tipped at%s", auto,
                    alliance, launched, scored, tipsAt.isEmpty() ? " (never)" : tips.toString());
            if (robots.size() == 1) {
                RobotResult r = robots.get(0);
                return head + String.format(Locale.ROOT, "; %s; LEAVE %s, PARK %s; %s",
                        finished ? String.format(Locale.ROOT, "finished at %.1f s", finishedAt) : "still running at 30 s",
                        r.leave ? "yes" : "no", r.park ? "yes" : "no", headStart())
                        + (g409 > 0 ? "; G409: " + g409 + " spilled pieces touched before the tiles" : "");
            }
            StringBuilder out = new StringBuilder(head);
            for (RobotResult r : robots) out.append("; ").append(r);
            out.append(String.format(Locale.ROOT, "; %d AUTO points; %s", autoPoints(), headStart()));
            if (!Double.isNaN(robotsCollidedAt)) {
                out.append(String.format(Locale.ROOT, "; ROBOTS COLLIDE at %.1f s", robotsCollidedAt));
            }
            if (g409 > 0) out.append("; G409: ").append(g409).append(" spilled pieces touched before the tiles");
            return out.toString();
        }
    }

    private final Alliance alliance;
    private final long seed;
    private final List<Bot> bots = new ArrayList<>();
    /** The robot {@link #speed} and {@link #design} configure: the last one added. */
    private Bot configuring;

    private FieldSim sim;

    /** The field as the last run left it, or as it is now during one: for working out a plan. */
    FieldSim field() {
        return sim;
    }

    /** Extra Metadata-tab entries for the log, such as the commit and .pp the Auto came from. */
    private final java.util.Map<String, String> metadata = new java.util.LinkedHashMap<>();

    /** Adds {@code name}: {@code value} to the log's Metadata tab, to say what this run simulated. */
    AutoSim metadata(String name, String value) {
        metadata.put(name, value);
        return this;
    }

    /** Called once a loop with the field and the time: for working out where pieces go. */
    java.util.function.BiConsumer<FieldSim, Double> observer;
    private double[] partnerPose;
    private double[][] partnerSpots;
    private boolean humanNectar;
    private final List<Double> nectarDueAt = new ArrayList<>();
    private double now;
    private List<double[]> lastArc;
    private int lane;

    /** One robot running {@code autoClass} for {@code alliance}. */
    AutoSim(Class<?> autoClass, Alliance alliance, long seed) {
        this.alliance = alliance;
        this.seed = seed;
        alsoRun(autoClass);
    }

    /**
     * Adds the alliance's other robot, running its own exported Auto on the same field at the same
     * time. {@link #speed} and {@link #design} after this call set up that robot.
     */
    AutoSim alsoRun(Class<?> autoClass) {
        if (bots.size() == 2) throw new IllegalStateException("an alliance has two robots");
        configuring = new Bot(autoClass, bots.size());
        bots.add(configuring);
        return this;
    }

    /**
     * This robot sets its 4 preloaded POLLEN on the tiles at {@code spots} (RED-drawn), touching its
     * front, instead of carrying them (G304, §10.3.4): a partner leaving them for us to collect.
     */
    AutoSim stagesPreloads(double[][] spots) {
        configuring.stagedPreloads = spots;
        return this;
    }

    /** Sets the drivetrain's top speed and acceleration, in/s and in/s². */
    AutoSim speed(double maxInPerS, double accelInPerS2) {
        configuring.drive.maxSpeed = maxInPerS;
        configuring.drive.accel = accelInPerS2;
        return this;
    }

    /**
     * Limits this robot's CollectSeen to pieces with x between the two (Pedro inches, drawn for RED
     * like the Auto): how two robots that cannot talk to each other share a spill, by agreeing
     * beforehand who takes which side.
     */
    AutoSim collectZone(double minX, double maxX) {
        configuring.zone = new double[] {minX, maxX};
        return this;
    }

    /** Simulates this robot instead of {@link RobotDesign#standard}. */
    AutoSim design(RobotDesign robot) {
        configuring.design = robot.checked();
        configuring.drive.maxTurn = robot.maxTurnRadPerS;
        return this;
    }

    /**
     * Adds a partner that stands still at {@code pose} (Pedro {x, y, heading}, drawn for RED like
     * the Auto, rotated for BLUE), with its 4 preloaded POLLEN on the tiles at {@code spots}.
     */
    AutoSim partner(double[] pose, double[][] spots) {
        partnerPose = pose;
        partnerSpots = spots;
        return this;
    }

    /**
     * Has the drive team enter one NECTAR into the LOADING ZONE {@link #HUMAN_DELAY_S} after each
     * TIP of their HIVE, as G426 allows. Off unless asked for: whether that is allowed during AUTO is
     * worth confirming with the Q&amp;A before an Auto counts on it.
     */
    AutoSim humanNectar(boolean on) {
        humanNectar = on;
        return this;
    }

    /** The staged pieces' rings: {@code /Sim/Staged/Ring<i>}, one a piece Outtake pushed out. */
    static final String KEY_STAGED_RING = "/Sim/Staged/Ring";
    private final List<double[]> stagedRings = new ArrayList<>();

    /**
     * A ring on the tiles round each piece {@code Outtake} pushed out (FieldSim#outtaken), while it lies loose on
     * the field (empty once a robot holds it or it is in a CELL), so the layout can pick the staged preloads out
     * of the other POLLEN. Written only when one moves.
     */
    private void putStaged(WpiLog log, long us) throws IOException {
        for (int i = 0; i < sim.outtaken.size(); i++) {
            FieldSim.Piece p = sim.outtaken.get(i);
            double[] ring = new double[0];
            if (p.where == FieldSim.Where.FIELD && p.cell == null) {
                List<double[]> pts = new ArrayList<>();
                for (int k = 0; k <= 12; k++) {
                    double a = 2 * Math.PI * k / 12, r = p.kind.radius + 0.6;
                    pts.add(new double[] {Math.round((p.x + r * Math.cos(a)) * 10) / 10.0,
                            Math.round((p.y + r * Math.sin(a)) * 10) / 10.0, 0.2});
                }
                ring = FieldSim.trajectory(pts);
            }
            if (i >= stagedRings.size()) stagedRings.add(null);
            if (!java.util.Arrays.equals(ring, stagedRings.get(i))) {
                log.putPose3dArray(KEY_STAGED_RING + i, ring, us);
                stagedRings.set(i, ring);
            }
        }
    }

    /** The exported Auto's {@code SOURCE}, without ".pp". */
    static String name(Class<?> autoClass) {
        try {
            String source = (String) autoClass.getField("SOURCE").get(null);
            return source.endsWith(".pp") ? source.substring(0, source.length() - 3) : source;
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(autoClass.getName() + " is not an exported Auto", e);
        }
    }

    /**
     * The match clock at {@code t} seconds after AUTO starts (negative before it), for reading the
     * time in AdvantageScope: {@code /Match/Time}, {@code /Match/AutoTimeLeft} and a {@code /Match/Clock}
     * line such as "AUTO 21.5 s, 8.5 left".
     */
    private static void putClock(WpiLog log, double t) throws IOException {
        long us = Math.round((PRE_ROLL_S + t) * 1e6);
        double auto = AutoKit.AUTO_LENGTH_S;
        double tenths = Math.round(t * 10) / 10.0;
        double left = Math.round(Math.max(0, Math.min(auto, auto - t)) * 10) / 10.0;
        String clock;
        if (t < 0) clock = String.format(Locale.ROOT, "BEFORE AUTO %.1f s", tenths);
        else if (t < auto) clock = String.format(Locale.ROOT, "AUTO %.1f s, %.1f left", tenths, left);
        else if (t <= auto + AFTER_S) clock = String.format(Locale.ROOT, "AFTER AUTO %.1f s (TIPs finishing still count)", tenths);
        else clock = String.format(Locale.ROOT, "DONE %.1f s", tenths);
        log.put("/Match/Time", tenths, us);
        log.put("/Match/AutoTimeLeft", left, us);
        log.put("/Match/Clock", clock, us);
    }

    private String runName() {
        StringBuilder sb = new StringBuilder();
        for (Bot b : bots) sb.append(sb.length() == 0 ? "" : " + ").append(name(b.autoClass));
        return sb.toString();
    }

    Result write(File file) throws IOException {
        file.getParentFile().mkdirs();
        try (WpiLog log = new WpiLog(new WpiLogWriter(
                new BufferedOutputStream(new FileOutputStream(file), 1 << 16),
                "BIOBUZZ simulated Auto: " + runName()))) {
            return write(log);
        }
    }

    Result write(WpiLog log) throws IOException {
        List<String> names = new ArrayList<>();
        for (Bot b : bots) names.add(name(b.autoClass));
        Result result = new Result(runName(), alliance, names);
        HiveCalibration calibration = HiveCalibration.current();
        sim = new FieldSim(HiveAssets.committedStagedPieces(), seed, calibration.fit());
        boolean red = alliance == Alliance.RED;
        // Every robot on the field, in FieldRobot slot order: the ones running Autos, then a
        // partner that stands still. Logged together each loop so AdvantageScope can show them all.
        if (partnerPose != null && bots.size() > 1) {
            throw new IllegalStateException("an alliance has two robots: a standing partner and two Autos is three");
        }
        int robots = bots.size() + (partnerPose != null ? 1 : 0);
        double[] allRobots = new double[3 * robots];
        String[] what = new String[robots];
        for (Bot b : bots) what[b.index] = name(b.autoClass);
        if (partnerPose != null) {
            double[][] spots = new double[partnerSpots.length][];
            for (int i = 0; i < spots.length; i++) spots[i] = forAlliance(partnerSpots[i], red);
            double[] standing = forAlliance(partnerPose, red);
            sim.stagePartner(alliance, standing, spots);
            System.arraycopy(standing, 0, allRobots, 3 * bots.size(), 3);
            what[bots.size()] = "stands still";
            FieldRobot.slot(bots.size()).putPose(log, standing[0], standing[1], standing[2], 0);
        }

        log.putMetadata("Generator", "AutoSim (TeamCode test sources)");
        log.putMetadata("PoseFrame", AdvantageScopeFrame.DESCRIPTION);
        log.putMetadata("Note", "Simulated robot: Pedro paths on a trapezoid profile, intake on whenever there"
                + " is room, launches aimed at the raised CELL");
        FieldSimLog.putMetadata(log, calibration);
        for (java.util.Map.Entry<String, String> e : metadata.entrySet()) log.putMetadata(e.getKey(), e.getValue());
        FieldRobot.putViewingHint(log, robots, what);
        log.put(AdvantageScopeKeys.ALLIANCE_STATION, AdvantageScopeKeys.allianceStation(red, 1), 0);
        FieldSimLog.putHiveStructure(log);

        Scheduler.reset();
        for (Bot b : bots) b.start(log, result.robots.get(b.index));

        FieldSimLog fieldLog = new FieldSimLog();
        // Pre-roll: the field as it starts, disabled, before AUTO begins at PRE_ROLL_S.
        log.put(AdvantageScopeKeys.ENABLED, false, 0);
        log.put(AdvantageScopeKeys.AUTONOMOUS, true, 0);
        log.put(AdvantageScopeKeys.ROBOT_MODE, "disabled", 0);
        for (Bot b : bots) b.putStill(log, 0);
        if (robots > 1) {
            for (Bot b : bots) System.arraycopy(b.prev, 0, allRobots, 3 * b.index, 3);
            FieldRobot.putAll(log, allRobots, 0);
        }
        fieldLog.write(log, sim, 0);
        putClock(log, -PRE_ROLL_S);
        long autoStartUs = Math.round(PRE_ROLL_S * 1e6);
        log.put(AdvantageScopeKeys.ENABLED, true, autoStartUs);
        log.put(AdvantageScopeKeys.ROBOT_MODE, "autonomous", autoStartUs);
        log.putEvent("AUTO starts", autoStartUs);
        int tipsSeen = 0, startsSeen = 0;
        // Our TIPs' spills to count: {when, the pieces that were in our CELLs as the TIP started}.
        List<Object[]> spills = new ArrayList<>();
        // ... and every TIP's pieces, to count who picks them up: {FieldSim time, pieces, counted, Result#tipKeeps row}.
        List<Object[]> keeps = new ArrayList<>();
        boolean scored = false;
        for (long step = 0; step * LOOP_S <= AutoKit.AUTO_LENGTH_S + AFTER_S; step++) {
            now = step * LOOP_S;
            long us = Math.round((PRE_ROLL_S + now) * 1e6);
            if (step % Math.round(CLOCK_STEP_S / LOOP_S) == 0) putClock(log, now);
            if (Math.abs(now - AutoKit.AUTO_LENGTH_S) < LOOP_S / 2) {
                // The robots stop at 30 s, so the log says so (mentor, 4 Oct 2026: AdvantageScope showed
                // AUTO running to 38 s). The field runs on for AFTER_S: a TIP finishing then still counts.
                log.put(AdvantageScopeKeys.ENABLED, false, us);
                log.put(AdvantageScopeKeys.ROBOT_MODE, "disabled", us);
                log.putEvent("AUTO ends (TIPs that finish in the next 8 s still count)", us);
            }
            boolean running = now < AutoKit.AUTO_LENGTH_S;
            if (running) {
                boolean any = false;
                for (Bot b : bots) any |= b.auto.isScheduled();
                if (any) Scheduler.execute();
            }
            if (!running && !scored) {
                // LEAVE and AUTO PARK are judged where the robots are when AUTO ends (§10.5 F).
                scored = true;
                for (Bot b : bots) b.judge(result.robots.get(b.index));
            }
            for (Bot b : bots) b.afterScheduler(log, result.robots.get(b.index), running, us);
            for (Bot b : bots) b.launcher(log, result, us);
            for (Bot b : bots) b.move(log, result.robots.get(b.index), running, step, us);
            if (robots > 1) {
                for (Bot b : bots) System.arraycopy(b.prev, 0, allRobots, 3 * b.index, 3);
                FieldRobot.putAll(log, allRobots, us);
            }
            if (bots.size() > 1 && Double.isNaN(result.robotsCollidedAt) && overlap(bots.get(0), bots.get(1))) {
                result.robotsCollidedAt = now;
                log.putEvent("ROBOTS COLLIDE: the two paths cross at the same time", us);
            }

            sim.step(LOOP_S);
            for (String e : sim.drainEvents()) {
                if (e.startsWith("score: ") && e.contains(alliance.name())) result.scored++;
                if (e.startsWith("miss: ")) {  // "miss: <why> <piece> robot <n>"
                    String[] w = e.split(" ");
                    int robot = Integer.parseInt(w[w.length - 1]) - 1;
                    if (robot >= 0 && robot < result.robots.size()) {
                        RobotResult rr = result.robots.get(robot);
                        rr.misses.merge(w[1], 1, Integer::sum);
                        rr.timeline.add(String.format(Locale.ROOT, "%5.2f %s", now, e));
                    }
                }
                if (e.startsWith("G409: ")) {
                    result.g409++;
                    // In the touching robot's timeline too, so a study's timeline shows during which step it happened.
                    int robot = e.charAt(12) - '1';  // "G409: robot N..."
                    if (robot >= 0 && robot < result.robots.size()) {
                        result.robots.get(robot).timeline.add(String.format(Locale.ROOT, "%5.2f %s", now, e));
                    }
                }
                log.putEvent("sim: " + e, us);
            }
            FieldSim.Rocker ours = sim.rocker(alliance);
            if (ours.tipsStarted > startsSeen) {
                startsSeen = ours.tipsStarted;
                List<FieldSim.Piece> inCell = new ArrayList<>();
                for (FieldSim.Piece p : sim.pieces) {
                    if (p.where == FieldSim.Where.FIELD && p.cell != null && p.cell.alliance() == alliance) inCell.add(p);
                }
                spills.add(new Object[] {now + SPILL_LOOK_S, inCell});
                keeps.add(new Object[] {sim.time, inCell, new java.util.HashSet<FieldSim.Piece>(), new double[] {
                        now, inCell.size(), 0, 0, Double.NaN}});
                result.tipKeeps.add((double[]) keeps.get(keeps.size() - 1)[3]);
            }
            for (Object[] k : keeps) {
                double started = (Double) k[0];
                @SuppressWarnings("unchecked")
                List<FieldSim.Piece> spilledPieces = (List<FieldSim.Piece>) k[1];
                @SuppressWarnings("unchecked")
                java.util.Set<FieldSim.Piece> counted = (java.util.Set<FieldSim.Piece>) k[2];
                double[] row = (double[]) k[3];
                for (FieldSim.Piece p : spilledPieces) {
                    if (p.capturedBy == null || !(p.capturedAt >= started) || !counted.add(p)) continue;
                    if (p.capturedBy == bots.get(0).body) {
                        row[2]++;
                        if (row[2] == 4) row[4] = p.capturedAt - started;
                    } else {
                        row[3]++;
                    }
                }
            }
            while (!spills.isEmpty() && now >= (Double) spills.get(0)[0]) {
                @SuppressWarnings("unchecked")
                List<FieldSim.Piece> spilled = (List<FieldSim.Piece>) spills.remove(0)[1];
                int out = 0, other = 0;
                for (FieldSim.Piece p : spilled) {
                    if (p.where == FieldSim.Where.FIELD && p.cell != null) continue;  // stayed in the CELL
                    out++;
                    boolean over = alliance == Alliance.BLUE ? p.x < FieldSim.CENTRE_IN : p.x > FieldSim.CENTRE_IN;
                    if (p.where == FieldSim.Where.FIELD && over) other++;
                }
                result.tipSpills.add(new int[] {out, other});
            }
            if (ours.tips > tipsSeen) {
                tipsSeen = ours.tips;
                result.tipsAt.add(now);
                if (humanNectar) nectarDueAt.add(now + HUMAN_DELAY_S);
            }
            while (!nectarDueAt.isEmpty() && now >= nectarDueAt.get(0)) {
                nectarDueAt.remove(0);
                sim.enterNectar(alliance);
            }
            fieldLog.write(log, sim, us);
            putStaged(log, us);
            if (observer != null) observer.accept(sim, now);
        }
        for (Bot b : bots) result.launched += result.robots.get(b.index).launched;
        FieldSim.Rocker ours = sim.rocker(alliance);
        result.cellLoad = Math.max(0, sim.tippingTorque(ours) / sim.physics.holdTorque);
        for (Bot b : bots) result.held += b.body.stored.size();
        for (FieldSim.Piece p : sim.pieces) {
            boolean ourHalf = alliance == Alliance.BLUE ? p.x > FieldSim.CENTRE_IN : p.x < FieldSim.CENTRE_IN;
            if (p.where == FieldSim.Where.FIELD && p.cell == null && p.flower < 0 && ourHalf) result.ourHalfLoose++;
        }
        RobotResult first = result.robots.get(0);
        result.finished = first.finished;
        result.finishedAt = first.finishedAt;
        long end = Math.round((PRE_ROLL_S + AutoKit.AUTO_LENGTH_S + AFTER_S) * 1e6);
        log.putEvent(result.toString(), end);
        // Post-roll: everything stays where it ended, so the end is easy to grab on the timeline.
        double last = AutoKit.AUTO_LENGTH_S + AFTER_S + POST_ROLL_S;
        long lastUs = Math.round((PRE_ROLL_S + last) * 1e6);
        for (Bot b : bots) b.putStill(log, lastUs);
        fieldLog.write(log, sim, lastUs);
        putClock(log, last);
        Scheduler.reset();
        return result;
    }

    /** Whether two robots' 18 in footprints overlap now. */
    private static boolean overlap(Bot a, Bot b) {
        double[] pa = pedro(a.drive.pose), pb = pedro(b.drive.pose);
        if (Math.hypot(pa[0] - pb[0], pa[1] - pb[1]) > 2 * 0.7072 * 18 + 1) return false;
        for (double[] c : corners(pa, a.design)) if (inside(c, pb, b.design)) return true;
        for (double[] c : corners(pb, b.design)) if (inside(c, pa, a.design)) return true;
        return false;
    }

    /**
     * The robot's footprint as {@link #corners}, plus the intake's front edge where it is wider than
     * the frame (a 24 in catcher sticks out 3 in each side): what can hit the HIVE frame or reach over
     * the centre line (mentor review: a 24 in catcher can't go through the tunnel under the HIVE).
     */
    private static List<double[]> outline(double[] pose, RobotDesign design, double now, double wallsOut, int flapsOnly) {
        List<double[]> out = corners(pose, design);
        if (design.hasGuides()) {  // the rigid V's free ends
            double c = Math.cos(pose[2]), s = Math.sin(pose[2]);
            double lx = design.frameIn / 2 + design.guideForwardIn;
            for (int side = -1; side <= 1; side += 2) {
                double ly = side * (design.frameWidthIn / 2 + design.guideOutIn);
                out.add(new double[] {pose[0] + lx * c - ly * s, pose[1] + lx * s + ly * c});
            }
        }
        if (design.hasFlaps() && (!design.flapsDeploy || wallsOut >= 1)) {  // flaps and crossbeam, every 2 in or so
            double c = Math.cos(pose[2]), s = Math.sin(pose[2]), half = design.frameIn / 2, halfW = design.frameWidthIn / 2;
            for (int side = -1; side <= 1; side += 2) {
                if (flapsOnly != 0 ? side != flapsOnly : side > 0 ? !design.flapLeft : !design.flapRight) continue;
                for (double f = 0.25; f <= 1.0001; f += 0.25) {
                    double lx = half + f * design.flapForwardIn, ly = side * (halfW + f * design.flapOutIn);
                    out.add(new double[] {pose[0] + lx * c - ly * s, pose[1] + lx * s + ly * c});
                }
            }
            if (design.flapCrossbeam) {
                double lx = half + design.flapForwardIn, span = halfW + design.flapOutIn;
                for (double f = -1; f <= 1.0001; f += 0.25) {
                    double ly = f * span;
                    out.add(new double[] {pose[0] + lx * c - ly * s, pose[1] + lx * s + ly * c});
                }
            }
        }
        if ((design.sideWallsSlideIn > 0 || design.sideWallsOutIn > 0) && wallsOut > 0) {  // the side walls' front ends
            double c = Math.cos(pose[2]), s = Math.sin(pose[2]);
            double lx = design.frameIn / 2 + wallsOut * design.sideWallsSlideIn;
            for (int side = -1; side <= 1; side += 2) {
                double ly = side * (design.frameWidthIn / 2 + wallsOut * design.sideWallsOutIn);
                out.add(new double[] {pose[0] + lx * c - ly * s, pose[1] + lx * s + ly * c});
            }
        }
        // It starts folded inside the 18 in start size (R102) and is out within the first second.
        if (design.intakeWidthIn <= design.frameWidthIn || now < CATCHER_DEPLOY_S) return out;
        double c = Math.cos(pose[2]), s = Math.sin(pose[2]);
        double lx = (design.intakeAtBack ? -1 : 1) * design.frameIn / 2, half = design.intakeWidthIn / 2;
        for (int j = -4; j <= 4; j++) {
            double ly = half * j / 4;
            out.add(new double[] {pose[0] + lx * c - ly * s, pose[1] + lx * s + ly * c});
        }
        return out;
    }

    static final double CATCHER_DEPLOY_S = 1.0;

    /** Side walls go out only this close to the HIVE's centre, where a spill lands beside it, and
     * come in once the robot is further. */
    static final double WALLS_NEAR_HIVE_IN = 60;
    /** ... or drives faster than this (it is leaving, and will turn) ... */
    static final double WALLS_DRIVE_OFF_IN_PER_S = 25;
    /** ... or turns faster than this ... */
    static final double WALLS_TURN_RAD_PER_S = 0.3;
    /** ... or this long after going out, when the spill has been gathered or has scattered. */
    static final double WALLS_HOLD_S = 3.0;
    /**
     * A hook (RobotDesign#flapsDeploy) swings down only while the robot is this slow (it may still be
     * sliding to its spot, as the Autos do once the TIP starts) ...
     */
    static final double HOOK_LOWER_IN_PER_S = 25;
    /** ... and not at all if the robot is still faster this long after the TIP starts (the spill is landing). */
    static final double HOOK_LATEST_S = 0.9;
    /**
     * Once the robot has stopped with the hook down, it swings up as soon as the robot drives faster than
     * this: its arm reaches 8-10 in ahead, into where the spill lands.
     */
    static final double HOOK_STILL_IN_PER_S = 3;

    /** Points every inch or so around the edge of a {@code size}-square footprint at {@code pose}. */
    private static List<double[]> edges(double[] pose, double size) {
        List<double[]> out = new ArrayList<>();
        double c = Math.cos(pose[2]), s = Math.sin(pose[2]), half = size / 2;
        int n = (int) Math.ceil(size);
        for (int k = 0; k <= n; k++) {
            double t = -half + size * k / n;
            for (double[] l : new double[][] {{t, -half}, {t, half}, {-half, t}, {half, t}}) {
                out.add(new double[] {pose[0] + l[0] * c - l[1] * s, pose[1] + l[0] * s + l[1] * c});
            }
        }
        return out;
    }

    /** Points around and inside an {@code size}-square footprint at {@code pose}. */
    private static List<double[]> corners(double[] pose, double size) {
        return corners(pose, size, size);
    }

    /** Points around and inside the design's frame ({@link RobotDesign#frameIn} long, {@link RobotDesign#frameWidthIn} wide). */
    private static List<double[]> corners(double[] pose, RobotDesign design) {
        return corners(pose, design.frameIn, design.frameWidthIn);
    }

    private static List<double[]> corners(double[] pose, double length, double width) {
        List<double[]> out = new ArrayList<>();
        double c = Math.cos(pose[2]), s = Math.sin(pose[2]), halfL = length / 2, halfW = width / 2;
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                double lx = halfL * i / 2, ly = halfW * j / 2;
                out.add(new double[] {pose[0] + lx * c - ly * s, pose[1] + lx * s + ly * c});
            }
        }
        return out;
    }

    private static boolean inside(double[] point, double[] pose, RobotDesign design) {
        double c = Math.cos(pose[2]), s = Math.sin(pose[2]);
        double dx = point[0] - pose[0], dy = point[1] - pose[1];
        double lx = dx * c + dy * s, ly = -dx * s + dy * c;
        return Math.abs(lx) < design.frameIn / 2 && Math.abs(ly) < design.frameWidthIn / 2;
    }

    /** The frame's longer side: the square the planners keep clear of the HIVE and FLOWERs. */
    private static double span(RobotDesign design) {
        return Math.max(design.frameIn, design.frameWidthIn);
    }

    /** One robot: its exported Auto, drivetrain, launcher and intake. */
    /**
     * Designs drawn with the CAD's whole-robot model ({@code BIOBUZZ Robot}) rather than {@code BIOBUZZ Robot
     * (designs)}: the baseline, "rigid V", and the name it had while the body-designs branch drew it.
     */
    static boolean cadModel(String design) {
        return design.equals("rigid V") || design.startsWith("flat intake, rigid V as drawn");
    }
    /** The CAD model's extractor shaft (cad/advantagescope/Robot_BIOBUZZ/extractor_poses.json): along +Y through here, m. */
    static final double[] EXTRACTOR_PIVOT_M = {0.25298, 0, 0.1143};
    static final double EXTRACTOR_STOWED_DEG = 146;

    /** The CAD model's turret axis, +Z through here (m); positive yaw turns left, 0 facing forward as drawn. */
    static final double TURRET_AXIS_X_M = -0.080518;

    /**
     * The CAD model's four component poses (translation m, quaternion w x y z; its extractor_poses.json): the FLOWER
     * extractor turned about its shaft by -angle about +Y (0 deg down as drawn, 146 stowed), for {@code down} from 0
     * (stowed) to 1 (down); the floating roller, down; the turret turned {@code turretYawRad} about its axis; the
     * transfer's J arm at rest. The roller and the J arm do not move in the logs yet.
     */
    static double[] cadComponents(double down, double turretYawRad) {
        double a = Math.toRadians(EXTRACTOR_STOWED_DEG * (1 - down));
        double px = EXTRACTOR_PIVOT_M[0], pz = EXTRACTOR_PIVOT_M[2];
        // R (about +Y by -a) applied to the pivot; translation = pivot - R pivot.
        double rx = px * Math.cos(a) - pz * Math.sin(a), rz = px * Math.sin(a) + pz * Math.cos(a);
        // The turret about +Z through (TURRET_AXIS_X_M, 0): translation = axis - R axis.
        double tx = TURRET_AXIS_X_M, c = Math.cos(turretYawRad), s = Math.sin(turretYawRad);
        return new double[] {
                px - rx, 0, pz - rz, Math.cos(a / 2), 0, -Math.sin(a / 2), 0,
                0, 0, 0, 1, 0, 0, 0,
                tx - tx * c, -tx * s, 0, Math.cos(turretYawRad / 2), 0, 0, Math.sin(turretYawRad / 2),
                0, 0, 0, 1, 0, 0, 0};
    }

    private final class Bot {
        final Class<?> autoClass;
        final int index;
        final SimDrive drive = new SimDrive();
        RobotDesign design = RobotDesign.standard();
        /** CollectSeen's x range, drawn for RED; null for anywhere. */
        double[] zone;
        /** Where this robot sets its preloads on the tiles, drawn for RED; null to carry them. */
        double[][] stagedPreloads;
        FieldSim.Bot body;
        Command auto;
        final List<String> pending = new ArrayList<>();
        /** Which robot this is in the log: its keys. */
        final FieldRobot robot;
        String keyPrefix;
        double[] prev;
        Path lastPath;
        boolean spinning;
        double spinStartedAt;
        boolean intakeEnabled = true;
        boolean firing;
        /** StreamOn: fire whatever is held whenever the launcher is ready, while the intake keeps taking more. */
        boolean streaming;
        int shotsFired;
        int shotTarget;
        double nextShotAt;

        Bot(Class<?> autoClass, int index) {
            this.autoClass = autoClass;
            this.index = index;
            this.robot = FieldRobot.slot(index);
        }

        void start(WpiLog log, RobotResult result) throws IOException {
            body = index == 0 ? sim.main : sim.addBot();
            body.design = design;
            if (stagedPreloads == null) {
                sim.preload(body, alliance);
            } else {
                double[][] spots = new double[stagedPreloads.length][];
                for (int i = 0; i < spots.length; i++) spots[i] = forAlliance(stagedPreloads[i], alliance == Alliance.RED);
                sim.stagePreloads(alliance, spots);
                // A robot that sets its preloads down has no use for its intake, which would only
                // pick them straight back up (mentor review).
                intakeEnabled = false;
            }
            keyPrefix = robot.prefix;
            String drawnFor;
            String[] commands;
            String[] triggers;
            Method startPose;
            Method build;
            try {
                drawnFor = (String) autoClass.getField("DRAWN_FOR").get(null);
                commands = (String[]) autoClass.getField("COMMANDS").get(null);
                triggers = (String[]) autoClass.getField("TRIGGERS").get(null);
                startPose = autoClass.getMethod("startPose", boolean.class);
                build = autoClass.getMethod("build", AutoKit.class, boolean.class);
            } catch (ReflectiveOperationException e) {
                throw new IllegalArgumentException(autoClass.getName() + " is not an exported Auto", e);
            }
            boolean rotated = !alliance.name().equals(drawnFor);
            String robot = index == 0 ? "" : " (robot " + (index + 1) + ")";
            log.putMetadata("Auto" + (index == 0 ? "" : String.valueOf(index + 1)), result.auto + " ("
                    + autoClass.getSimpleName() + "), drawn for " + drawnFor
                    + (rotated ? ", run rotated for " + alliance : ""));
            log.putMetadata("RobotDesign" + (index == 0 ? "" : String.valueOf(index + 1)), design.toString());
            AutoRegistry registry = registry();
            registry.requireAll(commands, triggers);
            AutoKit kit = new AutoKit(drive, registry, () -> now).trace(pending::add);
            try {
                drive.pose = (Pose) startPose.invoke(null, rotated);
                // An Auto drawn for an 18 in robot backed against the wall: a shorter chassis that should
                // start against it too has its start pose set that much further back on the robot.
                if (design.startBackedToWall && design.frameIn < 18) {
                    Pose p = drive.pose;
                    double back = (18 - design.frameIn) / 2;
                    drive.pose = new Pose(p.x() - back * Math.cos(p.heading()), p.y() - back * Math.sin(p.heading()), p.heading());
                }
                auto = (Command) build.invoke(null, kit, rotated);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("could not build " + result.auto, e);
            }
            prev = pedro(drive.pose);
            if (design.flapsDeploy && design.flapTowardCentre && design.flapEitherSide) {
                log.putEvent(tag() + "dual hook: an arm each side; the one facing the centre line comes down", index);
            } else if (design.flapsDeploy && design.flapTowardCentre) {
                // A one-armed hook is built with its arm on one side (mentor, 5 Oct 2026): the side that faces
                // the centre line when the robot faces the HIVE from the end of the field it starts at (the same side
                // for both alliances: one runs the Auto rotated half a turn).
                double heading = prev[1] > FieldSim.CENTRE_IN ? 1.5 * Math.PI : 0.5 * Math.PI;
                body.flapsOnly = towardCentre(prev[0], heading);
                log.putEvent(tag() + "hook built with its arm on the " + (body.flapsOnly > 0 ? "left" : "right"), index);
            }
            log.putEvent("Auto: " + result.auto + robot + " for " + alliance + (rotated ? " (rotated)" : ""), index);
            auto.schedule();
        }

        /** Which side of a robot at {@code x} facing {@code heading} faces the centre line: +1 its left, -1 its right. */
        int towardCentre(double x, double heading) {
            return -Math.sin(heading) * (FieldSim.CENTRE_IN - x) > 0 ? 1 : -1;
        }

        String tag() {
            return index == 0 ? "auto: " : "auto" + (index + 1) + ": ";
        }

        void afterScheduler(WpiLog log, RobotResult result, boolean running, long us) throws IOException {
            if (running && !auto.isScheduled() && !result.finished) {
                result.finished = true;
                result.finishedAt = now;
                log.putEvent(String.format(Locale.ROOT, "%sfinished at %.2f s", tag(), now), us);
            } else if (!running && auto.isScheduled()) {
                auto.cancel();
                log.putEvent(tag() + "still running at 30 s: stopped", us);
                result.decisions.add("still running at 30 s");
            }
            if (!running) {
                spinning = false;
                firing = false;
                streaming = false;
            }
            for (String line : pending) {
                result.decisions.add(line);
                result.timeline.add(String.format(Locale.ROOT, "%5.2f %s (holds %d)", now, line, body.stored.size()));
                log.putEvent(tag() + line, us);
            }
            pending.clear();
        }

        /** While a launch command runs and the launcher is ready, one volley per interval. */
        void launcher(WpiLog log, Result result, long us) throws IOException {
            if (!(firing || streaming) || !launcherReady() || body.stored.isEmpty()) return;
            double[] aim = sim.rocker(alliance).aimPoint();
            double yawError = 0;
            if (aim != null && design.launcher != RobotDesign.Launcher.TURRET) {
                // A frame-fixed launcher: the drivetrain turns the robot to face the CELL first (or,
                // with slats that flip, to put its back to it, if that is the smaller turn).
                double[] at = pedro(drive.pose);
                double bearing = Math.atan2(aim[1] - at[1], aim[0] - at[0]);
                yawError = AdvantageScopeFrame.wrap(bearing - at[2]);
                if (design.launchesBothWays) {
                    double backError = AdvantageScopeFrame.wrap(bearing + Math.PI - at[2]);
                    boolean back = Math.abs(backError) < Math.abs(yawError);
                    if (back != body.launchingBack) {
                        body.launchingBack = back;
                        nextShotAt = Math.max(nextShotAt, now + design.flipS);
                    }
                    if (back) {
                        yawError = backError;
                        bearing += Math.PI;
                    }
                }
                if (drive.pathDone()) drive.turnToward(bearing, LOOP_S);
            }
            if (aim == null || Math.abs(yawError) >= AIM_TOLERANCE_RAD || now < nextShotAt) return;
            if (fireOnlyWhenStill && (body.speed() > 1 || body.spin() > Math.toRadians(2))) return;
            // Out of range: no shot (mentor review: a robot whose own CELL never rose lobbed its
            // pieces at the far CELL from home). The real LaunchAll needs the same check.
            double[] here = pedro(drive.pose);
            if (Math.hypot(aim[0] - here[0], aim[1] - here[1]) > MAX_SHOT_RANGE_IN) return;
            // Paths finish while the robot is still braking into their end (SimDrive.JOIN_IN): it fires
            // once nearly stopped, unless it is streaming on purpose.
            if (!streaming && drive.speedNow > SimDrive.FIRE_SPEED_IN_PER_S) return;
            // The next piece is still in the transfer (RobotDesign#transferFeedS): fire once it has arrived.
            if (now < body.stored.get(0).readyAt) return;
            boolean catapult = design.launcher == RobotDesign.Launcher.CATAPULT;
            int volley = catapult ? FieldSim.ROBOT_CAPACITY : design.launchers;
            boolean dedicated = design.dedicatedLaunchers && !catapult;
            boolean clump = catapult && design.catapultClump;
            if (clump) {
                sim.volleyNoise = new double[] {sim.random.nextGaussian(), sim.random.nextGaussian(), sim.random.nextGaussian()};
                sim.volleyResidual = design.catapultResidual;
            }
            // A catapult throws everything in its cup once a volley starts: LaunchOne on it is a whole
            // volley too (mentor review), but no new volley starts once the command has its count.
            for (int i = 0; i < volley && !body.stored.isEmpty() && (streaming || (catapult && i > 0) || shotsFired < shotTarget); i++) {
                if (dedicated) {
                    // Launcher 0 takes POLLEN, launcher 1 NECTAR: bring that kind to the front, or skip.
                    boolean wantPollen = i == 0;
                    int pick = -1;
                    for (int k = 0; k < body.stored.size() && pick < 0; k++) {
                        if ((body.stored.get(k).kind == FieldSim.Kind.POLLEN) == wantPollen) pick = k;
                    }
                    if (pick < 0) continue;
                    body.stored.add(0, body.stored.remove(pick));
                }
                double side = volley == 1 ? 0 : (i - (volley - 1) / 2.0) * (catapult ? design.catapultSideIn : 6.0);
                if (clump) {
                    // A NECTAR's width apart plus a little, so nothing starts overlapping: 2 by 2, or a
                    // triangle (3 across the bottom of the cup, 1 nested on top).
                    double pitch = 2 * FieldSim.NECTAR_RADIUS_IN + 0.2;
                    if (design.catapultCup == RobotDesign.Cup.TRIANGLE) {
                        side = i < 3 ? (i - 1) * pitch : 0;
                        sim.volleyUpIn = i < 3 ? 0 : pitch * Math.sqrt(3) / 2;
                    } else {
                        side = ((i % 2) - 0.5) * pitch;
                        sim.volleyUpIn = (i / 2) * pitch;
                    }
                }
                double[] from = body.exitPoint(side);
                double[] v = sim.launch(body, aim, yawError, side, clump ? 1.0 : catapult ? design.catapultSpread : 1.0);
                if (v == null) break;
                result.robots.get(index).launched++;
                shotsFired++;
                lastArc = FieldSim.arc(from, v, aim[2] - 4, 30);
                log.putEvent((index == 0 ? "" : "robot " + (index + 1) + " ") + "launcher: shot "
                        + new String[] {"left", "center", "right"}[lane], us);
                lane = (lane + 1) % 3;
            }
            sim.volleyNoise = null;
            sim.volleyUpIn = 0;
            if (lastArc != null) log.putPose3dArray(FieldSimLog.KEY_SHOT, FieldSim.trajectory(lastArc), us);
            nextShotAt = now + (catapult ? design.spinUpS : design.shotIntervalS);
        }

        void move(WpiLog log, RobotResult result, boolean running, long step, long us) throws IOException {
            drive.tick(now);
            double[] pose = pedro(drive.pose);
            double vx = (pose[0] - prev[0]) / LOOP_S, vy = (pose[1] - prev[1]) / LOOP_S;
            double w = AdvantageScopeFrame.wrap(pose[2] - prev[2]) / LOOP_S;
            prev = pose;
            boolean intaking = running && intakeEnabled && body.stored.size() < FieldSim.ROBOT_CAPACITY;
            if (design.sideWallsSlideIn > 0 || design.sideWallsOutIn > 0 || design.flapsDeploy) {
                sideWalls(log, pose, Math.hypot(vx, vy), w, running, us);
            }
            if (!Double.isNaN(design.extractorSeatIn)) extractor(log, pose, running, us);
            body.set(pose[0], pose[1], pose[2], vx, vy, w, intaking);
            if (Double.isNaN(result.hitHiveAt)) {
                for (double[] c : outline(pose, design, now, body.wallsOut, body.flapsOnly)) {
                    if (FieldSim.inHiveFrame(c[0], c[1])) {
                        result.hitHiveAt = now;
                        log.putEvent(tag() + "drives into the HIVE frame", us);
                        break;
                    }
                }
            }
            if (Double.isNaN(result.hitFlowerAt) && sim.hitsFlower(pose[0], pose[1], pose[2], design.frameIn, design.frameWidthIn)) {
                result.hitFlowerAt = now;
                log.putEvent(tag() + "drives into a FLOWER", us);
            }
            if (step == 0) result.illegalStart = startProblem(pose);
            if (running && Double.isNaN(result.crossedAt)) {
                for (double[] c : outline(pose, design, now, body.wallsOut, body.flapsOnly)) {
                    boolean over = alliance == Alliance.BLUE ? c[0] < FieldSim.CENTRE_IN : c[0] > FieldSim.CENTRE_IN;
                    if (over) {
                        result.crossedAt = now;
                        log.putEvent(tag() + "G402: reaches into the other alliance's half", us);
                        break;
                    }
                }
            }

            robot.putPose(log, pose[0], pose[1], pose[2], us);
            if (drive.current != lastPath || step == 0) {
                // Logged on the first loop too, so a robot that never drives still has the key.
                lastPath = drive.current;
                log.putPose2dArray(robot.activePath,
                        lastPath == null ? new double[0] : packed(lastPath), us);
            }
            log.put(keyPrefix + "/Launcher/Spinning", spinning, us);
            log.put(keyPrefix + "/Intake/On", intaking, us);
            if (step % 5 == 0) log.putPose3dArray(keyPrefix + "/Intake/Zone", intakeZone(pose, intaking), us);
            if (step % 10 == 0) result.poses.add(pose);
        }

        /**
         * Where the intake takes a piece (FieldSim.inIntake), as a closed outline on the tiles, for
         * AdvantageScope to draw as a trajectory: its width (24 in for a catcher) and its reach in front
         * of the frame. Empty while the intake is off or full.
         */
        double[] intakeZone(double[] pose, boolean on) {
            if (!on) return new double[0];
            double mouth = design.frameIn / 2 + design.intakeReachIn, w = design.intakeWidthIn / 2;
            // Drawn for a POLLEN (FieldSim.inIntake): touching the face, or up to 3 in out.
            double reach = design.intakeOnContact ? FieldSim.POLLEN_RADIUS_IN + FieldSim.INTAKE_CONTACT_SLACK_IN : 3;
            double near = mouth - 2, far = mouth + reach, sign = design.intakeAtBack ? -1 : 1;
            double c = Math.cos(pose[2]), sn = Math.sin(pose[2]);
            List<double[]> pts = new ArrayList<>();
            for (double[] k : new double[][] {{near, -w}, {far, -w}, {far, w}, {near, w}, {near, -w}}) {
                double lx = sign * k[0], ly = k[1];
                pts.add(new double[] {pose[0] + lx * c - ly * sn, pose[1] + lx * sn + ly * c, 0.5});
            }
            return FieldSim.trajectory(pts);
        }

        /** Our CELL's TIPs started so far, as the robot's {@code HiveTracker.tipsStarted()} counts them. */
        int wallTipsSeen;
        /** When the last one started; NaN once the walls have answered it. */
        double wallTipAt = Double.NaN;
        double tipStartedAt = Double.NaN;
        /** Which wall the last TIP's spill falls toward: true for the far one (high y). */
        boolean wallSpillHighY;
        /** Whether the walls are going out (true) or in, and when they were last sent out. */
        boolean wallsWanted;
        /** Whether the robot has stopped with its hook down since it last swung down (it lifts on the next move). */
        boolean hookStopped;
        /** Whether the Auto holds the hook down (HookDown, until HookUp): sideWalls' own reasons do not lift it. */
        boolean hookHeld;
        double wallsSentAt = Double.NaN;

        /**
         * The side walls (RobotDesign#sideWallsSlideIn), run by the robot rather than the Auto
         * (mentor, 5 Oct 2026). Out RobotDesign#sideWallsDeployS after our CELL starts to TIP, once the
         * spill is on the tiles (G409), if the robot is near the HIVE and has room; they stay out while
         * it gathers the spill, and come in once it holds 4 (G407), drives off fast or starts to turn
         * (out, the robot is 24 in long; turning, its corners swing over the centre line, and they
         * take RobotDesign#sideWallsTravelS to come in), leaves the HIVE, or
         * {@link #WALLS_HOLD_S} later. They slide at RobotDesign#sideWallsTravelS. Logged as
         * {@code SideWalls/Out} (0 in to 1 out) and as component poses that slide the walls of the
         * {@code BIOBUZZ Robot (side walls)} model.
         */
        void sideWalls(WpiLog log, double[] pose, double speed, double turnRate, boolean running, long us) throws IOException {
            FieldSim.Rocker rocker = sim.rocker(alliance);
            int started = rocker.tipsStarted;
            if (started > wallTipsSeen) {
                wallTipsSeen = started;
                wallTipAt = now;
                tipStartedAt = now;
                // The CELL that was up comes down: its spill falls toward the wall at that end (mid-swing
                // raisedEnd() says neither).
                wallSpillHighY = rocker.fallingEnd() > 0;
            }
            boolean near = Math.hypot(pose[0] - FieldSim.CENTRE_IN, pose[1] - FieldSim.CENTRE_IN) < WALLS_NEAR_HIVE_IN;
            String guides = design.flapsDeploy ? "hook" : "side walls";
            boolean hookWaits = design.flapsDeploy && speed > HOOK_LOWER_IN_PER_S && now - wallTipAt < HOOK_LATEST_S;
            if (!Double.isNaN(wallTipAt) && now - wallTipAt >= design.sideWallsDeployS && !hookWaits) {
                boolean tooLate = design.flapsDeploy && speed > HOOK_LOWER_IN_PER_S;
                wallTipAt = Double.NaN;
                // A hook comes down only where the spill lands: not for a TIP whose spill falls at the other end,
                // nor where its arm would be on the outside, away from the centre line.
                boolean spillHere = !design.flapsDeploy || (pose[1] > FieldSim.CENTRE_IN) == wallSpillHighY;
                if (design.flapEitherSide && body.wallsOut == 0) body.flapsOnly = towardCentre(pose[0], pose[2]);
                boolean armInside = !design.flapTowardCentre || towardCentre(pose[0], pose[2]) == body.flapsOnly;
                if (running && !wallsWanted && near && spillHere && !tooLate && body.stored.size() < FieldSim.ROBOT_CAPACITY) {
                    if (!armInside) {
                        log.putEvent(tag() + "hook stays up: its arm would be on the side away from the centre line", us);
                    } else {
                        wallsWanted = true;
                        hookStopped = false;
                        wallsSentAt = now;
                        log.putEvent(tag() + guides + (design.flapsDeploy ? " down" : " out") + ": our CELL started to TIP "
                                + String.format(java.util.Locale.ROOT, "%.1f", now - tipStartedAt) + " s ago", us);
                    }
                }
            }
            if (wallsWanted) {
                hookStopped |= body.wallsOut >= 1 && speed < HOOK_STILL_IN_PER_S;
                String why = !running ? "AUTO ended"
                        : hookHeld ? null
                        : body.stored.size() >= FieldSim.ROBOT_CAPACITY ? "holding 4"
                        : !near ? "left the HIVE"
                        : speed > (design.flapsDeploy && hookStopped ? HOOK_STILL_IN_PER_S : WALLS_DRIVE_OFF_IN_PER_S) ? "driving off"
                        // A hook still sliding to its spot may turn a little on the way.
                        : Math.abs(turnRate) > WALLS_TURN_RAD_PER_S && (!design.flapsDeploy || hookStopped) ? "turning"
                        : now - wallsSentAt > WALLS_HOLD_S ? "spill gathered" : null;
                if (why != null) {
                    wallsWanted = false;
                    hookHeld = false;
                    log.putEvent(tag() + guides + (design.flapsDeploy ? " up: " : " in: ") + why, us);
                }
            }
            double step = LOOP_S / design.sideWallsTravelS;
            double before = body.wallsOut;
            body.wallsOut = wallsWanted ? Math.min(1, before + step) : Math.max(0, before - step);
            if (body.wallsOut != before || !wallsLogged) putWalls(log, us);
        }

        boolean wallsLogged;

        /** How far ahead of the face a FLOWER's centre may be for the extractor to come down on the approach. */
        static final double EXTRACTOR_DEPLOY_AHEAD_IN = 18;
        /** How far off the robot's centre line that FLOWER may be. */
        static final double EXTRACTOR_DEPLOY_ASIDE_IN = 6;
        boolean extractorWanted, extractorBlocked;

        /** How far past the seat a FLOWER may be, and how far aside, for the extractor to count as still on it. */
        static final double EXTRACTOR_SEATED_SLACK_IN = 6;  // the block's tip clear of the FLOWER's footprint before it lifts
        static final double EXTRACTOR_SEATED_ASIDE_IN = 4;
        /**
         * How near a FLOWER's seat the current path's end must be for the extractor to come down on the way there:
         * this much short of the seat (the approach point, where the turn in finishes) to this much past it.
         */
        static final double EXTRACTOR_PATH_END_SHORT_IN = 12;
        static final double EXTRACTOR_PATH_END_PAST_IN = 6;
        /** A FLOWER nearer the face than the seat plus this is under the block's swing: it cannot come down onto it. */
        static final double EXTRACTOR_BLOCKED_PAST_IN = 2;

        /**
         * The FLOWER extractor (RobotDesign#extractorSeatIn), run by the robot rather than the Auto, like the side
         * walls. Down as soon as the path being driven ends at a FLOWER's seat (so it is down before the turn in
         * finishes; mentor review, 7 Oct 2026), or while a FLOWER is ahead of the face (within
         * {@link #EXTRACTOR_DEPLOY_AHEAD_IN}, near the centre line), as long as the robot has room for pieces.
         * Never lifted while a FLOWER is still in its seat zone (it would lift through the FLOWER; the route backs
         * straight out first), and up once clear with nothing ahead, or full, or AUTO over. It swings in
         * RobotDesign#extractorDeployS; a FLOWER gives up pieces only while it is down and seated
         * (FieldSim.inIntake). Logged as {@code Extractor/Down} (0 up to 1 down) and as the CAD model's component
         * pose ({@link #putShape}). A real robot needs the same behaviour, in the Auto or in the subsystem; the
         * generated Autos do not carry an action for it yet.
         */
        void extractor(WpiLog log, double[] pose, boolean running, long us) throws IOException {
            boolean flowerAhead = false, seated = false, pathToFlower = false, inTheWay = false;
            double c = Math.cos(pose[2]), s = Math.sin(pose[2]);
            double face = design.frameIn / 2, seat = face + design.extractorSeatIn;
            double[] end = drive.pathEnd();
            for (double[] f : sim.flowers) {
                double lx = (f[0] - pose[0]) * c + (f[1] - pose[1]) * s;
                double ly = -(f[0] - pose[0]) * s + (f[1] - pose[1]) * c;
                if (design.intakeAtBack) lx = -lx;
                if (lx > face && lx < face + EXTRACTOR_DEPLOY_AHEAD_IN && Math.abs(ly) < EXTRACTOR_DEPLOY_ASIDE_IN) flowerAhead = true;
                if (lx > face - 1 && lx < seat + EXTRACTOR_SEATED_SLACK_IN && Math.abs(ly) < EXTRACTOR_SEATED_ASIDE_IN) seated = true;
                if (lx > face - 1 && lx < seat + EXTRACTOR_BLOCKED_PAST_IN && Math.abs(ly) < EXTRACTOR_SEATED_ASIDE_IN) inTheWay = true;
                if (end != null && !drive.pathDone()) {
                    double beyond = Math.hypot(f[0] - end[0], f[1] - end[1]) - seat;
                    if (beyond > -EXTRACTOR_PATH_END_PAST_IN && beyond < EXTRACTOR_PATH_END_SHORT_IN) pathToFlower = true;
                }
            }
            // Down on the way in whether or not the robot has room (mentor, 7 Oct 2026: deploy it before you get there,
            // then drive into it): the FLOWER's pieces stay in it until a shot makes room (FieldSim.hasRoom).
            boolean want = (body.extractorDown > 0 && seated) || (running && (pathToFlower || flowerAhead));
            // It cannot swing down onto a FLOWER already at the face: the block lands on it. Down only if it was down
            // before the robot got there; otherwise it stays where it is and takes nothing (FieldSim.inIntake).
            boolean blocked = want && inTheWay && body.extractorDown < 1;
            if (blocked != extractorBlocked) {
                extractorBlocked = blocked;
                if (blocked) log.putEvent(tag() + "extractor blocked: at the FLOWER before it was down", us);
            }
            if (want != extractorWanted) {
                extractorWanted = want;
                log.putEvent(tag() + "extractor " + (want ? (pathToFlower ? "down: driving to a FLOWER" : "down: a FLOWER ahead")
                        : running ? "up: leaving the FLOWER" : "up: AUTO ended"), us);
            }
            double step = LOOP_S / design.extractorDeployS;
            double before = body.extractorDown;
            body.extractorDown = blocked ? before : want ? Math.min(1, before + step) : Math.max(0, before - step);
            if (body.extractorDown != before) {
                log.put(keyPrefix + "/Extractor/Down", body.extractorDown, us);
                putShape(log, us);
            }
        }

        /** The walls' slide as AdvantageScope component poses: left wall, right wall, robot frame, metres. */
        void putWalls(WpiLog log, long us) throws IOException {
            wallsLogged = true;
            putShape(log, us);
            double x = body.wallsOut * design.sideWallsSlideIn * AdvantageScopeFrame.METERS_PER_INCH;
            double y = body.wallsOut * design.sideWallsOutIn * AdvantageScopeFrame.METERS_PER_INCH;
            log.put(keyPrefix + "/SideWalls/Out", body.wallsOut, us);
            log.putPose3dArray(keyPrefix + "/SideWalls/Components", new double[] {x, y, 0, 1, 0, 0, 0, x, -y, 0, 1, 0, 0, 0}, us);
        }

        /**
         * Which of {@code BIOBUZZ Robot (match shapes)}'s components to show ({@link BodyShape#MATCH}),
         * and how: the chassis (for any design without a shape there, the plain 18 in one) and a hook,
         * swung up as far as it is ({@code SideWalls/Out}: 0 stowed, 1 down).
         */
        void putShape(WpiLog log, long us) throws IOException {
            if (cadModel(design.name)) {
                // The whole-robot model built from the CAD (cad/advantagescope/Robot_BIOBUZZ): the FLOWER extractor at
                // its angle (146 deg stowed, 0 down), the roller, the turret turned to the CELL while the launcher is
                // spinning (to the nearest degree, so a still robot writes nothing), the J arm.
                double down = body == null ? 0 : body.extractorDown;
                double yaw = 0;
                double[] aim = spinning && design.launcher == RobotDesign.Launcher.TURRET ? sim.rocker(alliance).aimPoint() : null;
                if (aim != null) {
                    double[] at = pedro(drive.pose);
                    yaw = Math.toRadians(Math.round(Math.toDegrees(AdvantageScopeFrame.wrap(Math.atan2(aim[1] - at[1], aim[0] - at[0]) - at[2]))));
                }
                double key = down + 1000 * yaw;
                if (key == shapeLogged) return;
                shapeLogged = key;
                log.putPose3dArray(keyPrefix + "/BodyShape/Components", cadComponents(down, yaw), us);
                return;
            }
            double out = body == null ? 0 : body.wallsOut;
            if (out == shapeLogged) return;
            shapeLogged = out;
            int side = body == null || body.flapsOnly == 0 ? 1 : body.flapsOnly;
            int hook = design.flapsDeploy ? BodyShape.matchHook(design.name, side) : -1;
            double[] poses = RobotAssets.hookComponent(BodyShape.matchComponent(design.name), hook, 1 - out, design.frameIn);
            // A dual hook: the other one stays stowed.
            if (design.flapsDeploy && design.flapEitherSide) RobotAssets.swing(poses, BodyShape.matchHook(design.name, -side), 1, design.frameIn);
            log.putPose3dArray(keyPrefix + "/BodyShape/Components", poses, us);
        }

        double shapeLogged = -1;

        /** The robot standing where it is, for the disabled time before and after the run. */
        void putStill(WpiLog log, long us) throws IOException {
            putShape(log, us);
            if ((design.sideWallsSlideIn > 0 || design.sideWallsOutIn > 0) && body != null) putWalls(log, us);
            double[] pose = pedro(drive.pose);
            robot.putPose(log, pose[0], pose[1], pose[2], us);
            log.put(keyPrefix + "/Launcher/Spinning", false, us);
            log.put(keyPrefix + "/Intake/On", false, us);
            log.putPose3dArray(keyPrefix + "/Intake/Zone", new double[0], us);
        }

        /** LEAVE and AUTO PARK, from where the robot is as AUTO ends. */
        void judge(RobotResult result) {
            double[] pose = pedro(drive.pose);
            double[] zone = FieldSim.loadingZone(alliance);
            boolean touchesWall = false;
            boolean inZone = false;
            for (double[] c : corners(pose, design)) {
                touchesWall |= c[0] < 0.25 || c[1] < 0.25 || c[0] > FieldSim.FIELD_SIZE_IN - 0.25
                        || c[1] > FieldSim.FIELD_SIZE_IN - 0.25;
                inZone |= c[0] > zone[0] && c[0] < zone[1] && c[1] > zone[2] && c[1] < zone[3];
            }
            result.leave = !touchesWall;
            result.park = inZone;
        }

        // ---- What the Auto may ask for -----------------------------------------------------------

        /** Every command and trigger name the Auto Builder's Autos use, backed by the simulation. */
        private AutoRegistry registry() {
            return new AutoRegistry()
                    .command("LaunchAll", 3.0, () -> launch(Integer.MAX_VALUE))
                    .command("ShootAll", 3.0, () -> launch(Integer.MAX_VALUE))
                    .command("LaunchOne", 0.5, () -> launch(1))
                    .command("CollectSeen", 2.0, this::collectSeen)
                    .command("SpinUp", 0.1, () -> Commands.instant(this::spinUp))
                    .command("SpinDown", 0.1, () -> Commands.instant(() -> spinning = false))
                    .command("StreamOn", 0.1, () -> Commands.instant(() -> {
                        spinUp();
                        streaming = true;
                        nextShotAt = Math.max(nextShotAt, now);
                    }))
                    .command("StreamOff", 0.1, () -> Commands.instant(() -> streaming = false))
                    .command("IntakeOn", 0.1, () -> Commands.instant(() -> intakeEnabled = true))
                    .command("IntakeOff", 0.1, () -> Commands.instant(() -> intakeEnabled = false))
                    .command("SetDown", 1.5, this::setDown)
                    .command("Outtake", 1.0, this::outtake)
                    // The hook from the Auto (mentor, 5 Oct 2026), next to the TIP-triggered swing in sideWalls:
                    // down until HookUp, whatever the robot does meanwhile; done once it has finished swinging.
                    .command("HookDown", RobotDesign.standard().sideWallsTravelS, () -> hook(true))
                    .command("HookUp", RobotDesign.standard().sideWallsTravelS, () -> hook(false))
                    .trigger("IntakeFull", () -> design.countsPieces && body.stored.size() >= FieldSim.ROBOT_CAPACITY)
                    .trigger("LauncherReady", this::launcherReady)
                    .triggerSince("Tip", () -> {
                        FieldSim.Rocker hive = sim.rocker(alliance);
                        int before = hive.tipsStarted - (hive.state() == HiveState.TRANSITION ? 1 : 0);
                        return () -> hive.tipsStarted > before;
                    })
                    // The alliance's HIVE is no longer as it started the match.
                    .trigger("HiveTipped", () -> sim.rocker(alliance).state() == HiveState.LEFT_CELL_UP)
                    // Which CELL is up and settled, as HiveTracker reports it: unlike Tip, true for as
                    // long as it lasts, so a long wait can be split into short ones.
                    .trigger("LeftCellUp", () -> sim.rocker(alliance).state() == HiveState.LEFT_CELL_UP)
                    .trigger("RightCellUp", () -> sim.rocker(alliance).state() == HiveState.RIGHT_CELL_UP)
                    .trigger("Empty", () -> design.countsPieces && body.stored.isEmpty())
                    .trigger("CameraBlind", () -> false);
        }

        /**
         * Spins up if needed and fires up to {@code count} pieces at the raised CELL, a volley per
         * {@link RobotDesign#shotIntervalS}; done when that many have gone or the robot is empty.
         */
        private Command launch(int count) {
            return new CommandBuilder()
                    .setStart(() -> {
                        spinUp();
                        firing = true;
                        nextShotAt = Math.max(nextShotAt, now);
                        shotTarget = count == Integer.MAX_VALUE ? Integer.MAX_VALUE : shotsFired + count;
                    })
                    .setDone(() -> body.stored.isEmpty() || shotsFired >= shotTarget)
                    .setEnd(end -> firing = false);
        }

        /**
         * Stages what the robot holds: the intake runs backwards and sets the pieces down in a row
         * across its front, one per {@link #SET_DOWN_INTERVAL_S}, and stays off afterwards (the route
         * turns it back on once clear, or it would take them straight back).
         */
        private Command setDown() {
            final double[] next = new double[1];
            final int[] slot = new int[1];
            return new CommandBuilder()
                    .setStart(() -> {
                        intakeEnabled = false;
                        next[0] = now;
                        slot[0] = 0;
                    })
                    .setExecute(() -> {
                        if (now + 1e-9 >= next[0] && sim.setDown(body, slot[0])) {
                            slot[0]++;
                            next[0] = now + SET_DOWN_INTERVAL_S;
                        }
                    })
                    .setDone(() -> body.stored.isEmpty() && now + 1e-9 >= next[0]);
        }

        /**
         * Stages what the robot holds by pushing it out: the intake runs backwards and pushes the pieces out of its
         * mouth one per {@link #SET_DOWN_INTERVAL_S}, rolling at {@link #placeholderOuttakeInPerS}
         * ({@link FieldSim#outtake}), and stays off afterwards, as SetDown does.
         */
        private Command outtake() {
            final double[] next = new double[1];
            final int[] slot = new int[1];
            return new CommandBuilder()
                    .setStart(() -> {
                        intakeEnabled = false;
                        next[0] = now;
                        slot[0] = 0;
                    })
                    .setExecute(() -> {
                        if (now + 1e-9 >= next[0] && sim.outtake(body, slot[0], placeholderOuttakeInPerS)) {
                            slot[0]++;
                            next[0] = now + SET_DOWN_INTERVAL_S;
                        }
                    })
                    .setDone(() -> body.stored.isEmpty() && now + 1e-9 >= next[0]);
        }

        /**
         * HookDown / HookUp: swings the hook (RobotDesign#flapsDeploy) down and holds it there until HookUp, or
         * lifts it. While the Auto holds it down, none of sideWalls' own reasons lift it (driving, turning, holding
         * 4, 3 s): only HookUp or the end of AUTO. Done when it has finished swinging. On a robot without a hook,
         * it does nothing.
         */
        private Command hook(boolean down) {
            return new CommandBuilder()
                    .setStart(() -> {
                        if (!design.flapsDeploy) {
                            pending.add("no hook on this robot: Hook" + (down ? "Down" : "Up") + " does nothing");
                            return;
                        }
                        hookHeld = down;
                        if (down && !wallsWanted) {
                            hookStopped = false;
                            wallsSentAt = now;
                        }
                        if (wallsWanted != down) pending.add("hook " + (down ? "down" : "up") + ": the Auto asked");
                        wallsWanted = down;
                    })
                    .setDone(() -> !design.flapsDeploy || (down ? body.wallsOut >= 1 : body.wallsOut <= 0));
        }

        /**
         * Picks up what the webcam sees: drives the intake onto the nearest loose piece in view, then
         * the next, until full or nothing is left in view (it turns to look around once first).
         * Takes only POLLEN and the alliance's own NECTAR (G408), stays on its own half (G402) and
         * within {@link #COLLECT_RADIUS_IN} of where it started, and leaves pieces under the HIVE
         * alone (G409).
         */
        private Command collectSeen() {
            final double[] origin = new double[3];
            final FieldSim.Piece[] target = new FieldSim.Piece[1];
            final double[] lookedAround = new double[1];
            return new CommandBuilder()
                    .setStart(() -> {
                        double[] at = pedro(drive.pose);
                        origin[0] = at[0];
                        origin[1] = at[1];
                        origin[2] = at[2];
                        target[0] = null;
                        lookedAround[0] = 0;
                    })
                    .setExecute(() -> {
                        if (target[0] != null && target[0].where == FieldSim.Where.FIELD && !drive.pathDone()) return;
                        target[0] = nearestSeen(origin);
                        if (target[0] != null) {
                            driveOnto(target[0]);
                        } else if (drive.pathDone() && lookedAround[0] < LOOK_AROUND_RAD && canTurnHere()) {
                            // Nothing in view: turn on the spot to look around.
                            drive.turnToward(pedro(drive.pose)[2] + 0.6, LOOP_S);
                            lookedAround[0] += Math.min(0.6, design.maxTurnRadPerS * LOOP_S);
                        }
                    })
                    .setDone(() -> body.stored.size() >= FieldSim.ROBOT_CAPACITY
                            || (target[0] == null && (lookedAround[0] >= LOOK_AROUND_RAD || !canTurnHere())))
                    .setEnd(end -> drive.hold(drive.pose));
        }

        /**
         * Whether the robot can turn on the spot here: its corners sweep a circle about 0.71 frames wide,
         * which must stay off the walls, FLOWERs and the HIVE's feet, and on our side of the centre line.
         */
        private boolean canTurnHere() {
            double[] at = pedro(drive.pose);
            double reach = span(design) / Math.sqrt(2);
            if (at[0] < reach + 1 || at[1] < reach + 1 || at[0] > FieldSim.FIELD_SIZE_IN - reach - 1
                    || at[1] > FieldSim.FIELD_SIZE_IN - reach - 1) return false;
            if (sim.hitsFlower(at[0], at[1], 0, 2 * reach)) return false;
            // Nor sweep a corner into the HIVE's feet or over the centre line (G402).
            if (alliance == Alliance.BLUE ? at[0] - reach < FieldSim.CENTRE_IN : at[0] + reach > FieldSim.CENTRE_IN) return false;
            for (int k = 0; k < 36; k++) {
                double a = 2 * Math.PI * k / 36;
                for (double rr = reach / 2; rr <= reach + 1.5; rr += reach / 4) {
                    if (FieldSim.inHiveFrame(at[0] + rr * Math.cos(a), at[1] + rr * Math.sin(a))) return false;
                }
            }
            return true;
        }

        private FieldSim.Piece nearestSeen(double[] origin) {
            double[] at = pedro(drive.pose);
            double facing = at[2] + (design.intakeAtBack ? Math.PI : 0);
            FieldSim.Kind theirs = alliance == Alliance.BLUE ? FieldSim.Kind.RED_NECTAR : FieldSim.Kind.BLUE_NECTAR;
            FieldSim.Piece best = null;
            double bestD = Double.MAX_VALUE;
            for (FieldSim.Piece p : sim.pieces) {
                if (p.where != FieldSim.Where.FIELD || p.flower >= 0 || p.cell != null || p.z > 4) continue;
                if (p.kind == theirs || (p.kind != FieldSim.Kind.POLLEN && !design.launchesNectar)) continue;
                if (FieldSim.underHive(p.x, p.y)) continue;
                double dx = p.x - at[0], dy = p.y - at[1], d = Math.hypot(dx, dy);
                if (d > CAMERA_RANGE_IN || Math.hypot(p.x - origin[0], p.y - origin[1]) > COLLECT_RADIUS_IN) continue;
                if (Math.abs(AdvantageScopeFrame.wrap(Math.atan2(dy, dx) - facing)) > CAMERA_HALF_FOV_RAD) continue;
                if (alliance == Alliance.BLUE ? p.x < FieldSim.CENTRE_IN : p.x > FieldSim.CENTRE_IN) continue;
                if (zone != null) {
                    double xRed = alliance == Alliance.BLUE ? FieldSim.FIELD_SIZE_IN - p.x : p.x;
                    if (xRed < zone[0] || xRed > zone[1]) continue;
                }
                if (approachHitsFrame(p, at, origin[2])) continue;
                if (d < bestD) {
                    bestD = d;
                    best = p;
                }
            }
            return best;
        }

        /**
         * Whether driving onto {@code p} would put the robot into the HIVE frame's feet, a FLOWER, over
         * the centre line or through a wall, or against one near the end of AUTO (mentor review: it chased pieces into the
         * far FLOWER and lost LEAVE on the wall). The real CollectSeen needs the same rule.
         */
        private boolean approachHitsFrame(FieldSim.Piece p, double[] at, double originHeading) {
            // Touching the wall costs LEAVE only if the robot still touches it when AUTO ends (§10.5.4):
            // it may drive up to the wall for a piece until the last WALL_CLEAR_S, then stays 1 in off.
            double wall = now < AutoKit.AUTO_LENGTH_S - WALL_CLEAR_S ? 0 : 1;
            double bearing = Math.atan2(p.y - at[1], p.x - at[0]);
            double mouth = design.frameIn / 2 + design.intakeReachIn;
            double[] end = {p.x - (mouth - 1) * Math.cos(bearing), p.y - (mouth - 1) * Math.sin(bearing),
                    design.intakeAtBack ? bearing + Math.PI : bearing};
            for (double f = 0; f <= 1.0001; f += 0.25) {
                double[] mid = {at[0] + (end[0] - at[0]) * f, at[1] + (end[1] - at[1]) * f, end[2]};
                if (sim.hitsFlower(mid[0], mid[1], mid[2], span(design))) return true;
                // 2.5 in clear of the HIVE's feet, along every edge: a spill lands right in front of the foot
                // bars' ends (2 in wide, narrower than corners' spacing), and the robot drifts as it arrives.
                // It turns on the way: try every heading between the one it starts with and the one it ends with.
                double turn = AdvantageScopeFrame.wrap(end[2] - at[2]);
                for (double g = 0; g <= 1.0001; g += 0.25) {
                    double h = at[2] + turn * g;
                    for (double[] c : edges(new double[] {mid[0], mid[1], h}, span(design) + 5)) {
                        if (FieldSim.inHiveFrame(c[0], c[1])) return true;
                    }
                }
                // And from anywhere on the way (the wait can end mid-drive), turn back to the heading it
                // started collecting with: the Auto's next path starts from that heading.
                double back = AdvantageScopeFrame.wrap(originHeading - end[2]);
                for (double g = 0; g <= 1.0001; g += 0.25) {
                    for (double[] c : edges(new double[] {mid[0], mid[1], end[2] + back * g}, span(design) + 5)) {
                        if (FieldSim.inHiveFrame(c[0], c[1])) return true;
                    }
                }
                for (double[] c : corners(mid, design)) {
                    // G402: no part of the robot past the centre line (a spill scatters right up to it).
                    if (alliance == Alliance.BLUE ? c[0] < FieldSim.CENTRE_IN : c[0] > FieldSim.CENTRE_IN) return true;
                    if (c[0] < wall || c[1] < wall || c[0] > FieldSim.FIELD_SIZE_IN - wall || c[1] > FieldSim.FIELD_SIZE_IN - wall) return true;
                }
            }
            return false;
        }

        /** Why a robot at {@code pose} may not start there, or null: it must touch the wall, on its own half. */
        private String startProblem(double[] pose) {
            boolean touching = false;
            for (double[] c : corners(pose, design)) {
                if (alliance == Alliance.BLUE ? c[0] < FieldSim.CENTRE_IN : c[0] > FieldSim.CENTRE_IN) {
                    return "reaches into the other alliance's half";
                }
                double gap = Math.min(Math.min(c[0], c[1]), Math.min(FieldSim.FIELD_SIZE_IN - c[0], FieldSim.FIELD_SIZE_IN - c[1]));
                if (gap < START_WALL_IN) touching = true;
            }
            return touching ? null : "not touching the wall";
        }

        /** A straight drive that brings the intake's mouth onto {@code p}, facing it. */
        private void driveOnto(FieldSim.Piece p) {
            double[] at = pedro(drive.pose);
            double bearing = Math.atan2(p.y - at[1], p.x - at[0]);
            double heading = design.intakeAtBack ? bearing + Math.PI : bearing;
            double mouth = design.frameIn / 2 + design.intakeReachIn;
            double tx = p.x - (mouth - 1) * Math.cos(bearing), ty = p.y - (mouth - 1) * Math.sin(bearing);
            // Stay on our own half (with room for the corners of a robot turned any way), and inside
            // the walls.
            double half = span(design) / 2, corner = span(design) * Math.sqrt(0.5);
            tx = alliance == Alliance.BLUE ? Math.max(FieldSim.CENTRE_IN + corner, tx) : Math.min(FieldSim.CENTRE_IN - corner, tx);
            tx = Math.max(half + 0.5, Math.min(FieldSim.FIELD_SIZE_IN - half - 0.5, tx));
            ty = Math.max(half + 0.5, Math.min(FieldSim.FIELD_SIZE_IN - half - 0.5, ty));
            Pose from = new Pose(at[0], at[1], at[2]);
            Pose to = new Pose(tx, ty, heading);
            if (Math.hypot(tx - at[0], ty - at[1]) < 0.5) {
                drive.turnToward(heading, LOOP_S);
                return;
            }
            drive.follow(Paths.line(from, to).linear(from, to));
        }

        private void spinUp() {
            if (!spinning) {
                spinning = true;
                spinStartedAt = now;
            }
        }

        private boolean launcherReady() {
            return spinning && now - spinStartedAt >= design.spinUpS;
        }
    }

    // ---- The drivetrain ------------------------------------------------------------------------

    /** Drives Pedro paths on a trapezoidal speed profile; holds still otherwise. */
    static final class SimDrive implements AutoDrive {
        Pose pose = new Pose(0, 0, 0);
        Path current;
        double maxSpeed = MAX_SPEED_IN_PER_S;
        double accel = ACCEL_IN_PER_S2;
        /** Turning is rate-limited too: a path ends when the robot is there and facing its way. */
        double maxTurn = RobotDesign.standard().maxTurnRadPerS;
        private double startedAt = Double.NaN;
        private double length;
        /** A straight run from where the robot was to where the path starts, if it was elsewhere. */
        private double leadIn;
        private double leadFromX, leadFromY;
        private boolean done = true;
        /**
         * Pedro's follower does not stop dead at the end of a path: it reports the path finished as it
         * brakes into the end, and a path started straight after carries the robot's speed on. So a
         * path is done within this far of its end (and facing its way), the robot keeps braking to the
         * end point if nothing follows, and a following path starts at the speed it had, projected
         * onto the new direction. (Mentor review, 1 Oct 2026: the old dead stop was pessimistic.)
         */
        static final double JOIN_IN = 4.0;
        /** The launcher waits until the robot has nearly stopped, as it did when paths stopped dead. */
        static final double FIRE_SPEED_IN_PER_S = 6.0;
        private double entrySpeed;
        private double speedNow;
        private double dirX, dirY;

        @Override
        public void follow(Path path) {
            // Carry the current speed into the new path, as far as it points the same way.
            Pose a = path.get(0), b = path.get(0.02);
            double tx = b.x() - a.x(), ty = b.y() - a.y(), tn = Math.hypot(tx, ty);
            double along = tn < 1e-9 ? 0 : (dirX * tx + dirY * ty) / tn;
            entrySpeed = Math.max(0, speedNow * along);
            current = path;
            startedAt = Double.NaN; // starts on the next tick
            length = path.curve.length();
            // A path that starts away from the robot (the endgame guard's park, cut in early): the
            // follower drives to it first, rather than jumping.
            Pose start = path.get(0);
            leadIn = Math.hypot(start.x() - pose.x(), start.y() - pose.y());
            if (leadIn < 1) leadIn = 0;
            leadFromX = pose.x();
            leadFromY = pose.y();
            if (leadIn > 0) entrySpeed = 0;
            done = false;
            aimHeading = Double.NaN;
        }

        @Override
        public boolean pathDone() {
            return done;
        }

        /** Where the path being followed ends (x, y), or null without one. */
        double[] pathEnd() {
            if (current == null) return null;
            Pose end = current.endPose();
            return new double[] {end.x(), end.y()};
        }

        @Override
        public Pose pose() {
            return pose;
        }

        @Override
        public boolean poseReferenced() {
            return true;
        }

        @Override
        public void hold(Pose target) {
            current = null;
            speedNow = 0;
            done = true;
        }

        void tick(double now) {
            if (current == null) {
                speedNow = 0;
                return;
            }
            if (Double.isNaN(startedAt)) startedAt = now;
            double total = leadIn + length;
            double s = distanceAt(now - startedAt, total, maxSpeed, accel, entrySpeed);
            double px = pose.x(), py = pose.y();
            // The profile's last step lands on the length only to rounding: finish within a micro-inch.
            boolean arrived = s >= total - 1e-6;
            if (done && arrived) {
                // Braked to the end with nothing following: stand still, square to the path's end.
                speedNow = 0;
                Pose end = current.get(1);
                // Square to the path's end, unless the launcher has since turned it to aim (mentor
                // review: squaring back undid every aiming turn, so a robot 2.4 deg off never fired).
                double want = Double.isNaN(aimHeading) ? end.heading() : aimHeading;
                pose = new Pose(end.x(), end.y(), turned(pose.heading(), want, maxTurn * LOOP_S));
                return;
            }
            Pose goal;
            if (s < leadIn) {
                Pose start = current.get(0);
                double f = s / leadIn;
                goal = new Pose(leadFromX + (start.x() - leadFromX) * f, leadFromY + (start.y() - leadFromY) * f,
                        start.heading());
            } else {
                double completion = length == 0 || arrived ? 1 : (s - leadIn) / length;
                goal = current.get(clamp01(current.curve.parameter(clamp01(completion))));
            }
            double heading = turned(pose.heading(), goal.heading(), maxTurn * LOOP_S);
            pose = new Pose(goal.x(), goal.y(), heading);
            double mx = pose.x() - px, my = pose.y() - py, moved = Math.hypot(mx, my);
            speedNow = moved / LOOP_S;
            if (moved > 1e-9) {
                dirX = mx / moved;
                dirY = my / moved;
            }
            Pose end = current.get(1);
            boolean facing = Math.abs(AdvantageScopeFrame.wrap(end.heading() - heading)) < Math.toRadians(arrived ? 1 : 5);
            if (!done && facing && (arrived || total - s <= JOIN_IN)) done = true;
        }

        /** Turns in place toward {@code target} for {@code dt} seconds (an aim); only while idle. */
        /** The heading the launcher last turned toward since the last path started; NaN if none. */
        double aimHeading = Double.NaN;

        void turnToward(double target, double dt) {
            aimHeading = target;
            pose = new Pose(pose.x(), pose.y(), turned(pose.heading(), target, maxTurn * dt));
        }

        private static double turned(double from, double to, double maxStep) {
            double error = AdvantageScopeFrame.wrap(to - from);
            return from + Math.max(-maxStep, Math.min(maxStep, error));
        }

        /** Distance along a rest-to-rest trapezoid {@code t} seconds in. */
        /** As {@link #distanceAt(double, double, double, double)}, starting at speed {@code v0}. */
        static double distanceAt(double t, double length, double v, double a, double v0) {
            if (v0 <= 1e-9) return distanceAt(t, length, v, a);
            v0 = Math.min(v0, Math.min(v, Math.sqrt(2 * a * length))); // can always stop by the end
            double peak = Math.min(v, Math.sqrt(a * length + v0 * v0 / 2));
            double tUp = (peak - v0) / a, sUp = (peak * peak - v0 * v0) / (2 * a);
            double sDown = peak * peak / (2 * a), tDown = peak / a;
            double tCruise = Math.max(0, (length - sUp - sDown) / peak);
            if (t <= tUp) return v0 * t + 0.5 * a * t * t;
            if (t <= tUp + tCruise) return sUp + peak * (t - tUp);
            double td = Math.min(t - tUp - tCruise, tDown);
            return Math.min(length, sUp + peak * tCruise + peak * td - 0.5 * a * td * td);
        }

        static double distanceAt(double t, double length, double v, double a) {
            double peak = Math.min(v, Math.sqrt(length * a));
            double tRamp = peak / a, sRamp = peak * peak / (2 * a);
            double tCruise = (length - 2 * sRamp) / peak;
            if (t <= tRamp) return 0.5 * a * t * t;
            if (t <= tRamp + tCruise) return sRamp + peak * (t - tRamp);
            double td = Math.min(t - tRamp - tCruise, tRamp);
            return Math.min(length, sRamp + peak * tCruise + peak * td - 0.5 * a * td * td);
        }

        private static double clamp01(double v) {
            return Math.max(0, Math.min(1, v));
        }
    }

    /** A RED-drawn {x, y, heading} for this alliance: as is, or turned about the field centre. */
    private static double[] forAlliance(double[] p, boolean red) {
        if (red) return p.clone();
        double[] out = p.clone();
        out[0] = FieldSim.FIELD_SIZE_IN - p[0];
        out[1] = FieldSim.FIELD_SIZE_IN - p[1];
        if (p.length > 2) out[2] = AdvantageScopeFrame.wrap(p[2] + Math.PI);
        return out;
    }

    private static double[] pedro(Pose p) {
        return new double[] {p.x(), p.y(), p.heading()};
    }

    private static double[] packed(Path path) {
        Pose[] samples = VisualizerPath.sample(path, 24);
        double[] out = new double[3 * samples.length];
        for (int i = 0; i < samples.length; i++) {
            out[3 * i] = AdvantageScopeFrame.xMeters(samples[i].x(), samples[i].y());
            out[3 * i + 1] = AdvantageScopeFrame.yMeters(samples[i].x(), samples[i].y());
            out[3 * i + 2] = AdvantageScopeFrame.headingRad(samples[i].heading());
        }
        return out;
    }
}
