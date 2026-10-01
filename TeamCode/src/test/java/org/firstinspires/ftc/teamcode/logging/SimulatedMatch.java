package org.firstinspires.ftc.teamcode.logging;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.vision.HiveState;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Writes a fake but realistic match to a {@code .wpilog}, so the logger can be developed, and every
 * AdvantageScope view checked, with no robot.
 *
 * <p><b>Nothing here is BIOBUZZ strategy.</b> The mechanisms are generic and the driving is a
 * simple planner ({@link SimDriver}); the point is to exercise every kind of data the real logger
 * will write, in the shapes AdvantageScope expects, over a real match timeline:
 * <ul>
 *   <li>match state: init, 30 s Auto, 8 s transition, 120 s TeleOp;</li>
 *   <li>the robot on the 2D/3D field, the raw-odometry ghost drifting away from it, vision fixes
 *       accepted and rejected, and the active path during Auto;</li>
 *   <li>both gamepads, with every press also written as an event;</li>
 *   <li>a three-lane flywheel with feedforward and feedback split, one lane deliberately slow;</li>
 *   <li>battery sag, loop time, and one loop over the 80 ms danger line with its likely cause
 *       logged just before it;</li>
 *   <li>every POLLEN and NECTAR, and both HIVE rockers, moving under {@link FieldSim}'s physics:
 *       the robot picks pieces up, launches them in arcs into its raised CELL, tips the HIVE, and
 *       goes round to the other side. See {@code TeamCode/README.md}, "Game pieces and the HIVE".</li>
 * </ul>
 *
 * <p>Deterministic: the same seed gives the same file, so two runs can be compared.
 */
public final class SimulatedMatch {

    static final double LOOP_S = 0.020;
    static final double INIT_S = 3.0;
    static final double AUTO_S = 30.0;
    static final double TRANSITION_S = 8.0;
    static final double TELEOP_S = 120.0;
    static final double POST_S = 2.0;

    static final double AUTO_START = INIT_S;
    static final double AUTO_END = AUTO_START + AUTO_S;
    static final double TELEOP_START = AUTO_END + TRANSITION_S;
    static final double TELEOP_END = TELEOP_START + TELEOP_S;
    static final double MATCH_END = TELEOP_END + POST_S;

    /** Illustrative red start {x in, y in, heading rad}: against the red wall. */
    static final double[] START = {9, 60, 0};

    static final double TARGET_RPM = 3200;
    static final double IDLE_RPM = 1500;
    static final double KV = 0.00028;
    static final double KP = 0.0006;

    /** The keys the game-piece and HIVE views use; see {@code TeamCode/README.md}. */
    static final String KEY_POLLEN = "/Sim/GamePieces/Pollen";
    static final String KEY_RED_NECTAR = "/Sim/GamePieces/RedNectar";
    static final String KEY_BLUE_NECTAR = "/Sim/GamePieces/BlueNectar";
    /** The same, for pieces inside the robot. */
    static final String KEY_HELD_POLLEN = "/Sim/GamePieces/Held/Pollen";
    static final String KEY_HELD_RED_NECTAR = "/Sim/GamePieces/Held/RedNectar";
    static final String KEY_HELD_BLUE_NECTAR = "/Sim/GamePieces/Held/BlueNectar";
    static final String KEY_HIVE = "/Sim/Hive/Structure";
    static final String KEY_HIVE_COMPONENTS = "/Sim/Hive/Components";
    static final String KEY_SHOT = "/Sim/Shot/Trajectory";

    /** Something that happens at a time, not driven by the robot: a note, a fault. */
    static final class Action {
        final double time;
        final String kind;
        final String text;

        Action(double time, String kind, String text) {
            this.time = time;
            this.kind = kind;
            this.text = text;
        }
    }

    private final List<Action> actions = new ArrayList<>();
    private final Random random;
    private final long seed;

    public SimulatedMatch(long seed) {
        this.seed = seed;
        random = new Random(seed);
        action(TELEOP_START + 0.5, "driver", "Y: Reset heading");
        // Two things for post-match analysis to find.
        action(TELEOP_START + 61.8, "vision", "CellFix rejected: residual 14.2 in (limit 6.0)");
        action(TELEOP_START + 88.00, "vision", "Pipeline restart requested (tag loss 2.1 s)");
        action(TELEOP_START + 88.05, "loop", "Loop 93 ms (danger > 80 ms)");
    }

    private void action(double time, String kind, String text) {
        actions.add(new Action(time, kind, text));
    }

    // ---- Writing ----------------------------------------------------------------------------

    public void write(File file) throws IOException {
        file.getParentFile().mkdirs();
        try (WpiLog log = new WpiLog(new WpiLogWriter(
                new BufferedOutputStream(new FileOutputStream(file), 1 << 16),
                "BIOBUZZ simulated match"))) {
            write(log);
        }
    }

    void write(WpiLog log) throws IOException {
        log.putMetadata("Generator", "SimulatedMatch (TeamCode test sources)");
        log.putMetadata("RobotIdentity", "simulated");
        log.putMetadata("GitSHA", "simulated");
        log.putMetadata("PoseFrame", AdvantageScopeFrame.DESCRIPTION);
        log.putMetadata("Note", "Illustrative driving, not BIOBUZZ strategy");
        log.putMetadata("GamePieces", "Field '" + HiveAssets.FIELD_NAME + "': add " + KEY_POLLEN + " and the two"
                + " NECTAR keys as Game Piece objects, and " + KEY_HIVE + " as a Robot ('" + HiveAssets.ROBOT_NAME
                + "') with " + KEY_HIVE_COMPONENTS + " as its components. Build the assets with HiveAssetsTest.");
        log.putMetadata("Physics", "FieldSim: placeholder masses, restitution and HIVE tip threshold");

        FieldSim sim = new FieldSim(HiveAssets.committedStagedPieces(), seed);
        SimDriver driverBot = new SimDriver(sim, Alliance.RED, START);

        GamepadLog driverLog = new GamepadLog(0);
        GamepadLog operatorLog = new GamepadLog(1);
        GamepadLog.State driver = new GamepadLog.State();
        GamepadLog.State operator = new GamepadLog.State();

        double[] rpm = {IDLE_RPM * 0.2, IDLE_RPM * 0.2, IDLE_RPM * 0.2};
        double[] lagS = {0.30, 0.32, 0.65}; // right lane is the slow one
        String[] lanes = {"Left", "Center", "Right"};
        String[] laneNames = {"left", "center", "right"};
        double[] drift = {0, 0, 0}; // raw odometry error since the start, Pedro in / rad
        double[] fusedError = {0, 0, 0}; // what the filter has not yet corrected
        double battery = 13.1;
        String lastMode = "";
        String launcherState = "IDLE";
        String intakeState = "OFF";
        int nextAction = 0;
        double lastFix = 0;
        SimDriver.Route lastPath = null;
        double[] prevPose = START.clone();
        double clearShotAt = -1;
        Logged logged = new Logged();

        log.putEvent("OpMode init: Simulated match", 0);
        log.putEvent("Alliance RED (vision proposed, confirmed with X)", 1000);
        log.putEvent("Start check OK: 1.2 in, 2 deg from declared start", 2000);
        log.put(AdvantageScopeKeys.ALLIANCE_STATION, AdvantageScopeKeys.allianceStation(true, 1), 0);
        log.put(AdvantageScopeKeys.MATCH_NUMBER, 7L, 0);
        log.putPose3dFlat(KEY_HIVE, 0, 0, 0, 0, 0);

        for (long step = 0; step * LOOP_S <= MATCH_END; step++) {
            double t = step * LOOP_S;
            long us = Math.round(t * 1e6);

            // Match state, written when it changes.
            boolean auto = t >= AUTO_START && t < AUTO_END;
            boolean teleop = t >= TELEOP_START && t < TELEOP_END;
            String mode = auto ? "autonomous" : teleop ? "teleop" : "disabled";
            if (!mode.equals(lastMode)) {
                log.put(AdvantageScopeKeys.ENABLED, auto || teleop, us);
                log.put(AdvantageScopeKeys.AUTONOMOUS, auto, us);
                log.put(AdvantageScopeKeys.ROBOT_MODE, mode, us);
                log.putEvent("Match: " + mode, us);
                lastMode = mode;
            }

            // Timed notes and faults.
            driver.clear();
            operator.clear();
            while (nextAction < actions.size() && actions.get(nextAction).time <= t) {
                Action a = actions.get(nextAction++);
                log.putEvent((a.kind.equals("driver") ? "driver " : a.kind + ": ") + a.text, us);
            }

            // The robot decides what to do; the mechanisms follow.
            SimDriver.Output want = driverBot.update(t, auto || teleop, auto, launcherState.equals("READY"));
            String intakeWant = want.intake
                    ? (sim.stored.size() >= FieldSim.PLACEHOLDER_ROBOT_CAPACITY ? "FULL" : "INTAKING") : "OFF";
            if (!intakeWant.equals(intakeState)) {
                if (teleop && intakeWant.equals("INTAKING") && intakeState.equals("OFF")) {
                    log.putEvent("driver LB: Intake (hold)", us);
                }
                intakeState = transition(log, "intake", intakeState, intakeWant, us);
            }
            if (want.spin && launcherState.equals("IDLE")) {
                if (teleop) log.putEvent("operator A: Spin up", us);
                launcherState = transition(log, "launcher", launcherState, "SPINNING_UP", us);
            }
            if (want.fire && launcherState.equals("READY")) {
                if (teleop) log.putEvent("driver RB: Launch all", us);
                launcherState = transition(log, "launcher", launcherState, "FIRING", us);
            }
            if (!want.spin && !launcherState.equals("IDLE")) {
                launcherState = transition(log, "launcher", launcherState, "IDLE", us);
            }
            if (want.shotLane >= 0) {
                rpm[want.shotLane] -= 450;
                log.putEvent("launcher: shot " + laneNames[want.shotLane], us);
                log.putPose3dArray(KEY_SHOT, FieldSim.trajectory(want.shotArc), us);
                clearShotAt = t + 1.5;
            } else if (clearShotAt >= 0 && t >= clearShotAt) {
                log.putPose3dArray(KEY_SHOT, new double[0], us);
                clearShotAt = -1;
            }

            // Buttons held while their action is active.
            if (teleop && intakeState.equals("INTAKING")) driver.leftBumper = true;
            if (teleop && launcherState.equals("FIRING")) driver.rightBumper = true;
            if (teleop && launcherState.equals("SPINNING_UP")) operator.a = true;
            if (teleop && Math.abs(t - (TELEOP_START + 0.5)) < 0.15) driver.y = true;

            // Where the robot really is.
            double[] pose = driverBot.pose();
            double vx = (pose[0] - prevPose[0]) / LOOP_S;
            double vy = (pose[1] - prevPose[1]) / LOOP_S;
            double w = angleDiff(prevPose[2], pose[2]) / LOOP_S;
            prevPose = pose;
            if (teleop) {
                driver.leftStickX = clamp(vx / 45.0);
                driver.leftStickY = clamp(-vy / 45.0);
                driver.rightStickX = clamp(-w / 3.0);
                driver.leftTrigger = intakeState.equals("INTAKING") ? 1.0 : 0.0;
            }

            // The field moves.
            sim.setRobot(pose[0], pose[1], pose[2], vx, vy, w, intakeState.equals("INTAKING"));
            sim.step(LOOP_S);
            for (String e : sim.drainEvents()) log.putEvent("sim: " + e, us);
            logged.write(log, sim, us);

            // Odometry drifts while moving; vision pulls the fused pose back.
            double speed = Math.hypot(vx, vy);
            if (auto || teleop) {
                drift[0] += (0.004 + 0.0015 * random.nextGaussian()) * speed * LOOP_S;
                drift[1] += (-0.003 + 0.0015 * random.nextGaussian()) * speed * LOOP_S;
                drift[2] += 0.0009 * Math.abs(w) * LOOP_S + 0.00002;
                fusedError[0] += (0.004 + 0.0015 * random.nextGaussian()) * speed * LOOP_S;
                fusedError[1] += (-0.003 + 0.0015 * random.nextGaussian()) * speed * LOOP_S;
                fusedError[2] += 0.0009 * Math.abs(w) * LOOP_S + 0.00002;
            }
            boolean seesHive = seesHive(pose);
            boolean rejected = Math.abs(t - (TELEOP_START + 61.8)) < LOOP_S / 2;
            if (rejected || ((auto || teleop) && seesHive && t - lastFix > 1.5)) {
                double[] fix = {
                        pose[0] + 0.4 * random.nextGaussian() + (rejected ? 13.0 : 0),
                        pose[1] + 0.4 * random.nextGaussian() + (rejected ? -5.0 : 0),
                        pose[2] + 0.01 * random.nextGaussian()};
                String key = rejected ? "/Vision/CellFixRejected" : "/Vision/CellFix";
                putPedroPose(log, key, fix, us);
                if (!rejected) {
                    log.putEvent(String.format(
                            "vision: CellFix accepted (moved pose %.1f in)",
                            Math.hypot(fusedError[0], fusedError[1])), us);
                    fusedError[0] = fusedError[1] = fusedError[2] = 0;
                }
                lastFix = t;
            }
            log.put("/Vision/TagsVisible", (auto || teleop) && seesHive ? 1L + (step / 40) % 2 : 0L, us);

            double[] fused = {pose[0] + fusedError[0], pose[1] + fusedError[1], pose[2] + fusedError[2]};
            double[] raw = {pose[0] + drift[0], pose[1] + drift[1], pose[2] + drift[2]};
            putPedroPose(log, "/Odometry/Robot", fused, us);
            log.putPose3dFlat("/Odometry/Robot3d", AdvantageScopeFrame.xMeters(fused[0], fused[1]),
                    AdvantageScopeFrame.yMeters(fused[0], fused[1]), 0.0,
                    AdvantageScopeFrame.headingRad(fused[2]), us);
            putPedroPose(log, "/Odometry/PinpointOnly", raw, us);
            putPedroPose(log, "/Sim/TruePose", pose, us);
            log.put("/Odometry/PedroInches/X", fused[0], us);
            log.put("/Odometry/PedroInches/Y", fused[1], us);
            log.put("/Odometry/PedroInches/HeadingDeg", Math.toDegrees(fused[2]), us);

            // The active path (Auto only), written when it changes.
            SimDriver.Route route = driverBot.route();
            SimDriver.Route path = route != null && route.showPath ? route : null;
            if (path != lastPath) {
                log.putPose2dArray("/Path/Active", path == null ? new double[0] : path.packed(), us);
                lastPath = path;
            }

            // Flywheel lanes: first-order toward target, feedforward plus proportional feedback.
            boolean spinning = !launcherState.equals("IDLE");
            double target = spinning ? TARGET_RPM : (auto || teleop) ? IDLE_RPM : 0;
            double totalAmps = 0;
            for (int i = 0; i < 3; i++) {
                double ff = KV * target;
                double fb = target > 0 ? KP * (target - rpm[i]) : 0;
                double power = Math.max(-1, Math.min(1, ff + fb));
                rpm[i] += (power / KV - rpm[i]) * (LOOP_S / lagS[i]) + 8 * random.nextGaussian();
                double amps = Math.max(0, 1.5 + 9.0 * Math.abs(power) + (target - rpm[i]) * 0.004);
                totalAmps += amps;
                String p = "/Shooter/" + lanes[i] + "/";
                log.put(p + "RPM", rpm[i], us);
                log.put(p + "FeedforwardPower", ff, us);
                log.put(p + "FeedbackPower", fb, us);
                log.put(p + "CurrentAmps", amps, us);
            }
            log.put("/Shooter/TargetRPM", target, us);
            if (launcherState.equals("SPINNING_UP") && Math.abs(target - rpm[0]) < 150
                    && Math.abs(target - rpm[1]) < 150 && Math.abs(target - rpm[2]) < 150) {
                launcherState = transition(log, "launcher", launcherState, "READY", us);
            }

            // Battery sags under flywheel current and recovers slowly.
            double driveAmps = (auto || teleop) ? 0.12 * speed : 0;
            double rest = 13.1 - 0.0002 * t;
            battery += ((rest - 0.035 * (totalAmps + driveAmps)) - battery) * 0.15;
            log.put("/Robot/BatteryVolts", battery + 0.01 * random.nextGaussian(), us);

            // Loop time: steady, the odd GC blip, one real stall.
            double loopMs = 6.5 + 0.8 * random.nextGaussian();
            if (random.nextDouble() < 0.004) loopMs += 12 + 10 * random.nextDouble();
            if (Math.abs(t - (TELEOP_START + 88.06)) < LOOP_S / 2) loopMs = 93.0;
            log.put("/Robot/LoopMs", Math.max(2.0, loopMs), us);

            driverLog.write(log, driver, us);
            operatorLog.write(log, operator, us);
        }
        log.putEvent("OpMode stopped", Math.round(MATCH_END * 1e6));
    }

    /**
     * The sim's state, written only when it changes: pieces at rest and a settled HIVE cost
     * nothing, so the file stays small.
     */
    private static final class Logged {
        private final double[][] pieces = new double[6][];
        private double[] components;
        private final String[] hiveState = new String[2];
        private final int[] tips = {-1, -1};
        private final int[] raisedCount = {-1, -1};
        private int held = -1;

        void write(WpiLog log, FieldSim sim, long us) throws IOException {
            String[] keys = {KEY_POLLEN, KEY_RED_NECTAR, KEY_BLUE_NECTAR,
                    KEY_HELD_POLLEN, KEY_HELD_RED_NECTAR, KEY_HELD_BLUE_NECTAR};
            FieldSim.Kind[] kinds = FieldSim.Kind.values();
            for (int i = 0; i < 6; i++) {
                double[] now = sim.pieces(kinds[i % 3], i >= 3);
                if (!Arrays.equals(now, pieces[i])) {
                    log.putPose3dArray(keys[i], now, us);
                    pieces[i] = now;
                }
            }
            double[] c = sim.hiveComponents();
            if (!Arrays.equals(c, components)) {
                log.putPose3dArray(KEY_HIVE_COMPONENTS, c, us);
                components = c;
            }
            FieldSim.Rocker[] rockers = {sim.red, sim.blue};
            String[] names = {"Red", "Blue"};
            for (int i = 0; i < 2; i++) {
                FieldSim.Rocker r = rockers[i];
                String prefix = "/Sim/Hive/" + names[i] + "/";
                String state = r.state().name();
                if (!state.equals(hiveState[i])) {
                    log.put(prefix + "State", state, us);
                    hiveState[i] = state;
                }
                if (r.tips != tips[i]) {
                    log.put(prefix + "Tips", (long) r.tips, us);
                    tips[i] = r.tips;
                }
                int end = r.raisedEnd();
                int count = end == 0 ? 0 : sim.count(r.cell(end));
                if (count != raisedCount[i]) {
                    log.put(prefix + "RaisedCellPieces", (long) count, us);
                    raisedCount[i] = count;
                }
                if (r.state() == HiveState.TRANSITION || r.rate != 0) {
                    log.put(prefix + "AngleDeg", Math.toDegrees(r.angle), us);
                }
            }
            if (sim.stored.size() != held) {
                log.put("/Sim/Robot/Held", (long) sim.stored.size(), us);
                held = sim.stored.size();
            }
        }
    }

    /** Close to the HIVE and facing it: the Limelight can see a CELL's tags. */
    private static boolean seesHive(double[] pose) {
        double dx = FieldSim.CENTRE_IN - pose[0], dy = FieldSim.CENTRE_IN - pose[1];
        double bearing = Math.abs(angleDiff(pose[2], Math.atan2(dy, dx)));
        return Math.hypot(dx, dy) < 75 && bearing < Math.toRadians(55);
    }

    /** Writes a Pose2d converted from Pedro inches to AdvantageScope's frame. */
    static void putPedroPose(WpiLog log, String key, double[] pedro, long us) throws IOException {
        log.putPose2d(key, AdvantageScopeFrame.xMeters(pedro[0], pedro[1]),
                AdvantageScopeFrame.yMeters(pedro[0], pedro[1]),
                AdvantageScopeFrame.headingRad(pedro[2]), us);
    }

    private static String transition(WpiLog log, String name, String from, String to, long us)
            throws IOException {
        if (from.equals(to)) return to;
        log.putEvent(name + ": " + from + " -> " + to, us);
        log.put("/" + Character.toUpperCase(name.charAt(0)) + name.substring(1) + "/State", to, us);
        return to;
    }

    private static double angleDiff(double from, double to) {
        return AdvantageScopeFrame.wrap(to - from);
    }

    private static double clamp(double v) {
        return Math.max(-1, Math.min(1, v));
    }
}
