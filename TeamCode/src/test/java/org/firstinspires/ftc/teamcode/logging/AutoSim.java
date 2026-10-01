package org.firstinspires.ftc.teamcode.logging;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.CommandBuilder;
import com.pedropathing.ivy.Scheduler;
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
 *   <li><b>Commands and triggers</b> ({@link #registry}): every name a generated Auto may use, backed
 *       by the simulation. The robot starts holding its 4 preloaded POLLEN (Competition Manual
 *       §10.3.4) and never holds more than 4 (G407). The intake runs whenever the robot has room,
 *       unless the Auto turns it off. A launch aims at the alliance's raised CELL from wherever the
 *       robot is (as if the robot aims); whether it goes in is up to the physics. {@code Tip} is
 *       true once the alliance's HIVE has started to tip since the wait began, as the robot's
 *       {@code HiveTracker} reports it, but from the simulation's truth rather than a camera.</li>
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

    /** What one run did, for a test or a person to read. */
    static final class Result {
        final String auto;
        final Alliance alliance;
        int launched;
        int scored;
        /** When each TIP completed, including in the transition after AUTO, where they still count. */
        final List<Double> tipsAt = new ArrayList<>();
        boolean finished;
        double finishedAt = Double.NaN;
        final List<String> decisions = new ArrayList<>();
        /** The decisions again, each with the time it happened, for reading where the time goes. */
        final List<String> timeline = new ArrayList<>();
        final List<double[]> poses = new ArrayList<>();

        Result(String auto, Alliance alliance) {
            this.auto = auto;
            this.alliance = alliance;
        }

        @Override
        public String toString() {
            StringBuilder tips = new StringBuilder();
            for (double t : tipsAt) tips.append(String.format(Locale.ROOT, " %.1f s", t));
            return String.format(Locale.ROOT, "%s %s: launched %d, scored %d, HIVE tipped at%s; %s",
                    auto, alliance, launched, scored, tipsAt.isEmpty() ? " (never)" : tips.toString(),
                    finished ? String.format(Locale.ROOT, "finished at %.1f s", finishedAt) : "still running at 30 s");
        }
    }

    private final Class<?> autoClass;
    private final Alliance alliance;
    private final long seed;

    // The simulated robot underneath the Auto.
    private FieldSim sim;
    private final SimDrive drive = new SimDrive();

    private RobotDesign design = RobotDesign.standard();
    private double[] partnerPose;
    private double[][] partnerSpots;
    private boolean humanNectar;
    private final List<Double> nectarDueAt = new ArrayList<>();

    /** Sets the drivetrain's top speed and acceleration, in/s and in/s². */
    AutoSim speed(double maxInPerS, double accelInPerS2) {
        drive.maxSpeed = maxInPerS;
        drive.accel = accelInPerS2;
        return this;
    }

    /** Simulates this robot instead of {@link RobotDesign#standard}. */
    AutoSim design(RobotDesign robot) {
        design = robot.checked();
        drive.maxTurn = robot.maxTurnRadPerS;
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

    private double now;
    private boolean spinning;
    private double spinStartedAt;
    private boolean intakeEnabled = true;
    private boolean firing;
    private int shotsFired;
    private int shotTarget;
    private double nextShotAt;
    private List<double[]> lastArc;
    private int lane;

    AutoSim(Class<?> autoClass, Alliance alliance, long seed) {
        this.autoClass = autoClass;
        this.alliance = alliance;
        this.seed = seed;
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

    Result write(File file) throws IOException {
        file.getParentFile().mkdirs();
        try (WpiLog log = new WpiLog(new WpiLogWriter(
                new BufferedOutputStream(new FileOutputStream(file), 1 << 16),
                "BIOBUZZ simulated Auto: " + name(autoClass)))) {
            return write(log);
        }
    }

    Result write(WpiLog log) throws IOException {
        Result result = new Result(name(autoClass), alliance);
        HiveCalibration calibration = HiveCalibration.current();
        sim = new FieldSim(HiveAssets.committedStagedPieces(), seed, calibration.fit());
        sim.design = design;
        sim.preload(alliance);

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
        boolean red = alliance == Alliance.RED;
        if (partnerPose != null) {
            double[][] spots = new double[partnerSpots.length][];
            for (int i = 0; i < spots.length; i++) spots[i] = forAlliance(partnerSpots[i], red);
            sim.stagePartner(alliance, forAlliance(partnerPose, red), spots);
        }

        log.putMetadata("Generator", "AutoSim (TeamCode test sources)");
        log.putMetadata("Auto", result.auto + " (" + autoClass.getSimpleName() + "), drawn for " + drawnFor
                + (rotated ? ", run rotated for " + alliance : ""));
        log.putMetadata("PoseFrame", AdvantageScopeFrame.DESCRIPTION);
        log.putMetadata("Note", "Simulated robot: Pedro paths on a trapezoid profile, intake on whenever there"
                + " is room, launches aimed at the raised CELL");
        log.putMetadata("RobotDesign", design.toString());
        FieldSimLog.putMetadata(log, calibration);
        log.put(AdvantageScopeKeys.ALLIANCE_STATION, AdvantageScopeKeys.allianceStation(alliance == Alliance.RED, 1), 0);
        FieldSimLog.putHiveStructure(log);

        Scheduler.reset();
        AutoRegistry registry = registry();
        registry.requireAll(commands, triggers);
        List<String> pending = new ArrayList<>();
        AutoKit kit = new AutoKit(drive, registry, () -> now).trace(pending::add);
        Command auto;
        try {
            drive.pose = (Pose) startPose.invoke(null, rotated);
            auto = (Command) build.invoke(null, kit, rotated);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("could not build " + result.auto, e);
        }

        FieldSimLog fieldLog = new FieldSimLog();
        double[] prev = pedro(drive.pose);
        Path lastPath = null;
        log.put(AdvantageScopeKeys.ENABLED, true, 0);
        log.put(AdvantageScopeKeys.AUTONOMOUS, true, 0);
        log.put(AdvantageScopeKeys.ROBOT_MODE, "autonomous", 0);
        log.putEvent("Auto: " + result.auto + " for " + alliance + (rotated ? " (rotated)" : ""), 0);
        auto.schedule();
        int tipsSeen = 0;
        for (long step = 0; step * LOOP_S <= AutoKit.AUTO_LENGTH_S + AFTER_S; step++) {
            now = step * LOOP_S;
            long us = Math.round(now * 1e6);
            boolean running = now < AutoKit.AUTO_LENGTH_S;
            if (running && auto.isScheduled()) {
                Scheduler.execute();
            } else if (running && !result.finished) {
                result.finished = true;
                result.finishedAt = now;
                log.putEvent(String.format(Locale.ROOT, "Auto finished at %.2f s", now), us);
            } else if (!running && auto.isScheduled()) {
                auto.cancel();
                log.putEvent("Auto still running at 30 s: stopped", us);
                result.decisions.add("still running at 30 s");
            }
            if (!running) {
                spinning = false;
                firing = false;
            }
            for (String line : pending) {
                result.decisions.add(line);
                result.timeline.add(String.format(Locale.ROOT, "%5.2f %s", now, line));
                log.putEvent("auto: " + line, us);
            }
            pending.clear();

            // Launcher: while a launch command runs and the launcher is ready, one volley per interval.
            if (firing && launcherReady() && !sim.stored.isEmpty()) {
                double[] aim = sim.rocker(alliance).aimPoint();
                double yawError = 0;
                if (aim != null && design.launcher != RobotDesign.Launcher.TURRET) {
                    // A frame-fixed launcher: the drivetrain turns the robot to face the CELL first.
                    double[] at = pedro(drive.pose);
                    double bearing = Math.atan2(aim[1] - at[1], aim[0] - at[0]);
                    yawError = AdvantageScopeFrame.wrap(bearing - at[2]);
                    if (drive.pathDone()) drive.turnToward(bearing, LOOP_S);
                }
                if (aim != null && Math.abs(yawError) < AIM_TOLERANCE_RAD && now >= nextShotAt) {
                    boolean catapult = design.launcher == RobotDesign.Launcher.CATAPULT;
                    int volley = catapult ? FieldSim.ROBOT_CAPACITY : design.launchers;
                    for (int i = 0; i < volley && !sim.stored.isEmpty() && shotsFired < shotTarget; i++) {
                        double side = volley == 1 ? 0 : (i - (volley - 1) / 2.0) * (catapult ? 2.5 : 6.0);
                        double[] from = sim.exitPoint(side);
                        double[] v = sim.launch(aim, yawError, side, catapult ? 2.0 : 1.0);
                        if (v == null) break;
                        result.launched++;
                        shotsFired++;
                        lastArc = FieldSim.arc(from, v, aim[2] - 4, 30);
                        log.putEvent("launcher: shot " + new String[] {"left", "center", "right"}[lane], us);
                        lane = (lane + 1) % 3;
                    }
                    if (lastArc != null) log.putPose3dArray(FieldSimLog.KEY_SHOT, FieldSim.trajectory(lastArc), us);
                    nextShotAt = now + (catapult ? design.spinUpS : design.shotIntervalS);
                }
            }

            // The robot, then the field.
            drive.tick(now);
            double[] pose = pedro(drive.pose);
            double vx = (pose[0] - prev[0]) / LOOP_S, vy = (pose[1] - prev[1]) / LOOP_S;
            double w = AdvantageScopeFrame.wrap(pose[2] - prev[2]) / LOOP_S;
            prev = pose;
            boolean intaking = running && intakeEnabled && sim.stored.size() < FieldSim.ROBOT_CAPACITY;
            sim.setRobot(pose[0], pose[1], pose[2], vx, vy, w, intaking);
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

            SimulatedMatch.putPedroPose(log, "/Odometry/Robot", pose, us);
            log.putPose3dFlat("/Odometry/Robot3d", AdvantageScopeFrame.xMeters(pose[0], pose[1]),
                    AdvantageScopeFrame.yMeters(pose[0], pose[1]), 0.0, AdvantageScopeFrame.headingRad(pose[2]), us);
            if (drive.current != lastPath) {
                lastPath = drive.current;
                log.putPose2dArray("/Path/Active", lastPath == null ? new double[0] : packed(lastPath), us);
            }
            log.put("/Launcher/Spinning", spinning, us);
            log.put("/Intake/On", intaking, us);
            if (step % 10 == 0) result.poses.add(pose);
        }
        log.put(AdvantageScopeKeys.ENABLED, false, Math.round((AutoKit.AUTO_LENGTH_S + AFTER_S) * 1e6));
        log.putEvent(result.toString(), Math.round((AutoKit.AUTO_LENGTH_S + AFTER_S) * 1e6));
        Scheduler.reset();
        return result;
    }

    // ---- What the Auto may ask for ---------------------------------------------------------------

    /** Every command and trigger name the Auto Builder's Autos use, backed by the simulation. */
    private AutoRegistry registry() {
        return new AutoRegistry()
                .command("LaunchAll", 3.0, () -> launch(Integer.MAX_VALUE))
                .command("ShootAll", 3.0, () -> launch(Integer.MAX_VALUE))
                .command("LaunchOne", 0.5, () -> launch(1))
                .command("SpinUp", 0.1, () -> Commands.instant(this::spinUp))
                .command("SpinDown", 0.1, () -> Commands.instant(() -> spinning = false))
                .command("IntakeOn", 0.1, () -> Commands.instant(() -> intakeEnabled = true))
                .command("IntakeOff", 0.1, () -> Commands.instant(() -> intakeEnabled = false))
                .trigger("IntakeFull", () -> sim.stored.size() >= FieldSim.ROBOT_CAPACITY)
                .trigger("LauncherReady", this::launcherReady)
                .triggerSince("Tip", () -> {
                    FieldSim.Rocker hive = sim.rocker(alliance);
                    int before = hive.tipsStarted - (hive.state() == HiveState.TRANSITION ? 1 : 0);
                    return () -> hive.tipsStarted > before;
                })
                // The alliance's HIVE is no longer as it started the match.
                .trigger("HiveTipped", () -> sim.rocker(alliance).state() == HiveState.LEFT_CELL_UP)
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
                .setDone(() -> sim.stored.isEmpty() || shotsFired >= shotTarget)
                .setEnd(end -> firing = false);
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
        private boolean done = true;

        @Override
        public void follow(Path path) {
            current = path;
            startedAt = Double.NaN; // starts on the next tick
            length = path.curve.length();
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
            double s = distanceAt(now - startedAt, length, maxSpeed, accel);
            // The profile's last step lands on the length only to rounding: finish within a micro-inch.
            boolean arrived = s >= length - 1e-6;
            double completion = length == 0 || arrived ? 1 : s / length;
            Pose goal = current.get(clamp01(current.curve.parameter(clamp01(completion))));
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
