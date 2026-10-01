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

    // Drivetrain profile: the Visualizer log's defaults. A robot that is not tuned yet; the Auto
    // Builder previews at 60 in/s and 55 in/s², which {@link #speed} can set.
    static final double MAX_SPEED_IN_PER_S = 40;
    static final double ACCEL_IN_PER_S2 = 30;

    // Mechanisms: not measured, like the rest of the simulated robot.
    /** How long after a TIP a drive-team member gets a NECTAR into the LOADING ZONE. */
    static final double HUMAN_DELAY_S = 2.0;
    /** A frame-fixed launcher launches once the robot faces the CELL this closely. */
    static final double AIM_TOLERANCE_RAD = Math.toRadians(2);

    // The webcam CollectSeen drives by (the robot's PieceVisionSubsystem): it looks the way the
    // intake faces and sees loose pieces on the tiles. Placeholders, like the rest of the robot.
    static final double CAMERA_HALF_FOV_RAD = Math.toRadians(35);
    static final double CAMERA_RANGE_IN = 60;
    /** CollectSeen keeps within this far of where it started, so it does not wander off. */
    static final double COLLECT_RADIUS_IN = 36;

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
        /** LEAVE (Competition Manual §10.5.4): not touching the perimeter wall when AUTO ends. */
        boolean leave;
        /** AUTO PARK (§10.5.4): at least partly in the alliance's LOADING ZONE when AUTO ends. */
        boolean park;
        /**
         * When the robot first reached into the other alliance's half, which G402 calls risky in
         * AUTO; NaN if it never did.
         */
        double crossedAt = Double.NaN;
        /** When the robot first ran into the HIVE frame's feet, which a real one cannot; NaN if never. */
        double hitHiveAt = Double.NaN;

        RobotResult(String auto) {
            this.auto = auto;
        }

        @Override
        public String toString() {
            return String.format(Locale.ROOT, "%s launched %d, %s, LEAVE %s, PARK %s%s", auto, launched,
                    finished ? String.format(Locale.ROOT, "finished at %.1f s", finishedAt) : "still running at 30 s",
                    leave ? "yes" : "no", park ? "yes" : "no", (Double.isNaN(crossedAt) ? ""
                            : String.format(Locale.ROOT, ", CROSSES THE CENTRE LINE at %.1f s", crossedAt))
                            + (Double.isNaN(hitHiveAt) ? ""
                            : String.format(Locale.ROOT, ", DRIVES INTO THE HIVE FRAME at %.1f s", hitHiveAt)));
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
                return head + String.format(Locale.ROOT, "; %s; LEAVE %s, PARK %s",
                        finished ? String.format(Locale.ROOT, "finished at %.1f s", finishedAt) : "still running at 30 s",
                        r.leave ? "yes" : "no", r.park ? "yes" : "no");
            }
            StringBuilder out = new StringBuilder(head);
            for (RobotResult r : robots) out.append("; ").append(r);
            out.append(String.format(Locale.ROOT, "; %d AUTO points", autoPoints()));
            if (!Double.isNaN(robotsCollidedAt)) {
                out.append(String.format(Locale.ROOT, "; ROBOTS COLLIDE at %.1f s", robotsCollidedAt));
            }
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

    /** The exported Auto's {@code SOURCE}, without ".pp". */
    static String name(Class<?> autoClass) {
        try {
            String source = (String) autoClass.getField("SOURCE").get(null);
            return source.endsWith(".pp") ? source.substring(0, source.length() - 3) : source;
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(autoClass.getName() + " is not an exported Auto", e);
        }
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
        FieldRobot.putViewingHint(log, robots, what);
        log.put(AdvantageScopeKeys.ALLIANCE_STATION, AdvantageScopeKeys.allianceStation(red, 1), 0);
        FieldSimLog.putHiveStructure(log);

        Scheduler.reset();
        for (Bot b : bots) b.start(log, result.robots.get(b.index));

        FieldSimLog fieldLog = new FieldSimLog();
        log.put(AdvantageScopeKeys.ENABLED, true, 0);
        log.put(AdvantageScopeKeys.AUTONOMOUS, true, 0);
        log.put(AdvantageScopeKeys.ROBOT_MODE, "autonomous", 0);
        int tipsSeen = 0;
        boolean scored = false;
        for (long step = 0; step * LOOP_S <= AutoKit.AUTO_LENGTH_S + AFTER_S; step++) {
            now = step * LOOP_S;
            long us = Math.round(now * 1e6);
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
                log.putEvent("sim: " + e, us);
            }
            FieldSim.Rocker ours = sim.rocker(alliance);
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
            if (observer != null) observer.accept(sim, now);
        }
        for (Bot b : bots) result.launched += result.robots.get(b.index).launched;
        RobotResult first = result.robots.get(0);
        result.finished = first.finished;
        result.finishedAt = first.finishedAt;
        long end = Math.round((AutoKit.AUTO_LENGTH_S + AFTER_S) * 1e6);
        log.put(AdvantageScopeKeys.ENABLED, false, end);
        log.putEvent(result.toString(), end);
        Scheduler.reset();
        return result;
    }

    /** Whether two robots' 18 in footprints overlap now. */
    private static boolean overlap(Bot a, Bot b) {
        double[] pa = pedro(a.drive.pose), pb = pedro(b.drive.pose);
        if (Math.hypot(pa[0] - pb[0], pa[1] - pb[1]) > 2 * 0.7072 * 18 + 1) return false;
        for (double[] c : corners(pa, a.design.frameIn)) if (inside(c, pb, b.design.frameIn)) return true;
        for (double[] c : corners(pb, b.design.frameIn)) if (inside(c, pa, a.design.frameIn)) return true;
        return false;
    }

    /** Points around and inside an {@code size}-square footprint at {@code pose}. */
    private static List<double[]> corners(double[] pose, double size) {
        List<double[]> out = new ArrayList<>();
        double c = Math.cos(pose[2]), s = Math.sin(pose[2]), half = size / 2;
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                double lx = half * i / 2, ly = half * j / 2;
                out.add(new double[] {pose[0] + lx * c - ly * s, pose[1] + lx * s + ly * c});
            }
        }
        return out;
    }

    private static boolean inside(double[] point, double[] pose, double size) {
        double c = Math.cos(pose[2]), s = Math.sin(pose[2]);
        double dx = point[0] - pose[0], dy = point[1] - pose[1];
        double lx = dx * c + dy * s, ly = -dx * s + dy * c;
        return Math.abs(lx) < size / 2 && Math.abs(ly) < size / 2;
    }

    /** One robot: its exported Auto, drivetrain, launcher and intake. */
    private final class Bot {
        final Class<?> autoClass;
        final int index;
        final SimDrive drive = new SimDrive();
        RobotDesign design = RobotDesign.standard();
        /** CollectSeen's x range, drawn for RED; null for anywhere. */
        double[] zone;
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
            sim.preload(body, alliance);
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
                auto = (Command) build.invoke(null, kit, rotated);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("could not build " + result.auto, e);
            }
            prev = pedro(drive.pose);
            log.putEvent("Auto: " + result.auto + robot + " for " + alliance + (rotated ? " (rotated)" : ""), index);
            auto.schedule();
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
            }
            for (String line : pending) {
                result.decisions.add(line);
                result.timeline.add(String.format(Locale.ROOT, "%5.2f %s", now, line));
                log.putEvent(tag() + line, us);
            }
            pending.clear();
        }

        /** While a launch command runs and the launcher is ready, one volley per interval. */
        void launcher(WpiLog log, Result result, long us) throws IOException {
            if (!firing || !launcherReady() || body.stored.isEmpty()) return;
            double[] aim = sim.rocker(alliance).aimPoint();
            double yawError = 0;
            if (aim != null && design.launcher != RobotDesign.Launcher.TURRET) {
                // A frame-fixed launcher: the drivetrain turns the robot to face the CELL first.
                double[] at = pedro(drive.pose);
                double bearing = Math.atan2(aim[1] - at[1], aim[0] - at[0]);
                yawError = AdvantageScopeFrame.wrap(bearing - at[2]);
                if (drive.pathDone()) drive.turnToward(bearing, LOOP_S);
            }
            if (aim == null || Math.abs(yawError) >= AIM_TOLERANCE_RAD || now < nextShotAt) return;
            boolean catapult = design.launcher == RobotDesign.Launcher.CATAPULT;
            int volley = catapult ? FieldSim.ROBOT_CAPACITY : design.launchers;
            for (int i = 0; i < volley && !body.stored.isEmpty() && shotsFired < shotTarget; i++) {
                double side = volley == 1 ? 0 : (i - (volley - 1) / 2.0) * (catapult ? 2.5 : 6.0);
                double[] from = body.exitPoint(side);
                double[] v = sim.launch(body, aim, yawError, side, catapult ? 2.0 : 1.0);
                if (v == null) break;
                result.robots.get(index).launched++;
                shotsFired++;
                lastArc = FieldSim.arc(from, v, aim[2] - 4, 30);
                log.putEvent((index == 0 ? "" : "robot " + (index + 1) + " ") + "launcher: shot "
                        + new String[] {"left", "center", "right"}[lane], us);
                lane = (lane + 1) % 3;
            }
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
            body.set(pose[0], pose[1], pose[2], vx, vy, w, intaking);
            if (Double.isNaN(result.hitHiveAt)) {
                for (double[] c : corners(pose, design.frameIn)) {
                    if (FieldSim.inHiveFrame(c[0], c[1])) {
                        result.hitHiveAt = now;
                        log.putEvent(tag() + "drives into the HIVE frame", us);
                        break;
                    }
                }
            }
            if (running && Double.isNaN(result.crossedAt)) {
                for (double[] c : corners(pose, design.frameIn)) {
                    boolean over = alliance == Alliance.BLUE ? c[0] < FieldSim.CENTRE_IN : c[0] > FieldSim.CENTRE_IN;
                    if (over) {
                        result.crossedAt = now;
                        log.putEvent(tag() + "G402: reaches into the other alliance's half", us);
                        break;
                    }
                }
            }

            robot.putPose(log, pose[0], pose[1], pose[2], us);
            if (drive.current != lastPath) {
                lastPath = drive.current;
                log.putPose2dArray(robot.activePath,
                        lastPath == null ? new double[0] : packed(lastPath), us);
            }
            log.put(keyPrefix + "/Launcher/Spinning", spinning, us);
            log.put(keyPrefix + "/Intake/On", intaking, us);
            if (step % 10 == 0) result.poses.add(pose);
        }

        /** LEAVE and AUTO PARK, from where the robot is as AUTO ends. */
        void judge(RobotResult result) {
            double[] pose = pedro(drive.pose);
            double[] zone = FieldSim.loadingZone(alliance);
            boolean touchesWall = false;
            boolean inZone = false;
            for (double[] c : corners(pose, design.frameIn)) {
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
                    .command("IntakeOn", 0.1, () -> Commands.instant(() -> intakeEnabled = true))
                    .command("IntakeOff", 0.1, () -> Commands.instant(() -> intakeEnabled = false))
                    .trigger("IntakeFull", () -> body.stored.size() >= FieldSim.ROBOT_CAPACITY)
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
                    .trigger("Empty", () -> body.stored.isEmpty())
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
         * Picks up what the webcam sees: drives the intake onto the nearest loose piece in view, then
         * the next, until full or nothing is left in view (it turns to look around once first).
         * Takes only POLLEN and the alliance's own NECTAR (G408), stays on its own half (G402) and
         * within {@link #COLLECT_RADIUS_IN} of where it started, and leaves pieces under the HIVE
         * alone (G409).
         */
        private Command collectSeen() {
            final double[] origin = new double[2];
            final FieldSim.Piece[] target = new FieldSim.Piece[1];
            final double[] lookedAround = new double[1];
            return new CommandBuilder()
                    .setStart(() -> {
                        double[] at = pedro(drive.pose);
                        origin[0] = at[0];
                        origin[1] = at[1];
                        target[0] = null;
                        lookedAround[0] = 0;
                    })
                    .setExecute(() -> {
                        if (target[0] != null && target[0].where == FieldSim.Where.FIELD && !drive.pathDone()) return;
                        target[0] = nearestSeen(origin);
                        if (target[0] != null) {
                            driveOnto(target[0]);
                        } else if (drive.pathDone() && lookedAround[0] < 2 * Math.PI) {
                            // Nothing in view: turn on the spot to look around.
                            drive.turnToward(pedro(drive.pose)[2] + 0.6, LOOP_S);
                            lookedAround[0] += Math.min(0.6, design.maxTurnRadPerS * LOOP_S);
                        }
                    })
                    .setDone(() -> body.stored.size() >= FieldSim.ROBOT_CAPACITY
                            || (target[0] == null && lookedAround[0] >= 2 * Math.PI))
                    .setEnd(end -> drive.hold(drive.pose));
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
                if (approachHitsFrame(p, at)) continue;
                if (d < bestD) {
                    bestD = d;
                    best = p;
                }
            }
            return best;
        }

        /** Whether driving onto {@code p} would put the robot into the HIVE frame's feet. */
        private boolean approachHitsFrame(FieldSim.Piece p, double[] at) {
            double bearing = Math.atan2(p.y - at[1], p.x - at[0]);
            double mouth = design.frameIn / 2 + design.intakeReachIn;
            double[] end = {p.x - (mouth - 1) * Math.cos(bearing), p.y - (mouth - 1) * Math.sin(bearing),
                    design.intakeAtBack ? bearing + Math.PI : bearing};
            for (double f = 0; f <= 1.0001; f += 0.25) {
                double[] mid = {at[0] + (end[0] - at[0]) * f, at[1] + (end[1] - at[1]) * f, end[2]};
                for (double[] c : corners(mid, design.frameIn)) if (FieldSim.inHiveFrame(c[0], c[1])) return true;
            }
            return false;
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
            double half = design.frameIn / 2, corner = design.frameIn * Math.sqrt(0.5);
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

        @Override
        public void follow(Path path) {
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
            done = false;
        }

        @Override
        public boolean pathDone() {
            return done;
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
            done = true;
        }

        void tick(double now) {
            if (current == null || done) return;
            if (Double.isNaN(startedAt)) startedAt = now;
            double total = leadIn + length;
            double s = distanceAt(now - startedAt, total, maxSpeed, accel);
            // The profile's last step lands on the length only to rounding: finish within a micro-inch.
            boolean arrived = s >= total - 1e-6;
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
            if (arrived && Math.abs(AdvantageScopeFrame.wrap(goal.heading() - heading)) < Math.toRadians(1)) done = true;
        }

        /** Turns in place toward {@code target} for {@code dt} seconds (an aim); only while idle. */
        void turnToward(double target, double dt) {
            pose = new Pose(pose.x(), pose.y(), turned(pose.heading(), target, maxTurn * dt));
        }

        private static double turned(double from, double to, double maxStep) {
            double error = AdvantageScopeFrame.wrap(to - from);
            return from + Math.max(-maxStep, Math.min(maxStep, error));
        }

        /** Distance along a rest-to-rest trapezoid {@code t} seconds in. */
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
