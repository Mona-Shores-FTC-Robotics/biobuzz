package org.firstinspires.ftc.teamcode.logging;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Writes a fake but realistic match to a {@code .wpilog}, so the logger can be developed, and every
 * AdvantageScope view checked, with no robot.
 *
 * <p><b>Nothing here is BIOBUZZ strategy.</b> The positions are illustrative Pedro coordinates and
 * the mechanisms are generic; the point is to exercise every kind of data the real logger will
 * write, in the shapes AdvantageScope expects, over a real match timeline:
 * <ul>
 *   <li>match state: init, 30 s Auto, 8 s transition, 120 s TeleOp;</li>
 *   <li>the robot on the 2D/3D field, the raw-odometry ghost drifting away from it, vision fixes
 *       accepted and rejected, and the active path during Auto;</li>
 *   <li>both gamepads, with every press also written as an event;</li>
 *   <li>a three-lane flywheel with feedforward and feedback split, one lane deliberately slow;</li>
 *   <li>battery sag, loop time, and one loop over the 80 ms danger line with its likely cause
 *       logged just before it.</li>
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

    // Illustrative Pedro poses {x in, y in, heading rad}.
    static final double[] START = {9, 60, 0};
    static final double[] SHOOT = {60, 84, Math.toRadians(45)};
    static final double[] COLLECT_A = {24, 120, Math.toRadians(90)};
    static final double[] COLLECT_B = {44, 124, Math.toRadians(100)};
    static final double[] PARK = {36, 30, Math.toRadians(180)};

    static final double TARGET_RPM = 3200;
    static final double IDLE_RPM = 1500;
    static final double KV = 0.00028;
    static final double KP = 0.0006;

    /** A timed move between two poses, with an optional path shown during it. */
    static final class Leg {
        final double start, end;
        final double[] from, to;
        final boolean showPath;

        Leg(double start, double end, double[] from, double[] to, boolean showPath) {
            this.start = start;
            this.end = end;
            this.from = from;
            this.to = to;
            this.showPath = showPath;
        }
    }

    /** Something that happens at a time: a button press, a mechanism action, a note. */
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

    private final List<Leg> legs = new ArrayList<>();
    private final List<Action> actions = new ArrayList<>();
    private final Random random;

    public SimulatedMatch(long seed) {
        random = new Random(seed);
        script();
    }

    // ---- The match script -------------------------------------------------------------------

    private void script() {
        double a = AUTO_START;
        leg(a + 0.0, a + 2.5, START, SHOOT, true);
        shoot(a + 2.5, "auto");
        leg(a + 5.0, a + 8.0, SHOOT, COLLECT_A, true);
        intake(a + 8.0, a + 10.5, "auto");
        leg(a + 10.5, a + 13.5, COLLECT_A, SHOOT, true);
        shoot(a + 13.5, "auto");
        leg(a + 16.0, a + 19.0, SHOOT, COLLECT_B, true);
        intake(a + 19.0, a + 21.0, "auto");
        leg(a + 21.0, a + 24.0, COLLECT_B, SHOOT, true);
        shoot(a + 24.0, "auto");
        leg(a + 26.5, a + 29.0, SHOOT, PARK, true);

        double t = TELEOP_START;
        action(t + 0.5, "driver", "Y: Reset heading");
        leg(t + 1.0, t + 5.0, PARK, COLLECT_A, false);
        double[][] collects = {COLLECT_A, COLLECT_B, COLLECT_A, COLLECT_B, COLLECT_A};
        double cycleStart = t + 5.0;
        for (int i = 0; i < collects.length; i++) {
            double c = cycleStart + i * 21.0;
            intake(c, c + 3.0, "teleop");
            leg(c + 3.0, c + 7.0, collects[i], SHOOT, false);
            action(c + 5.5, "operator", "A: Spin up");
            shoot(c + 7.5, "teleop");
            double[] next = i + 1 < collects.length ? collects[i + 1] : PARK;
            leg(c + 11.0, c + 15.0, SHOOT, next, false);
        }

        // Two things for post-match analysis to find.
        action(TELEOP_START + 61.8, "vision", "CellFix rejected: residual 14.2 in (limit 6.0)");
        action(TELEOP_START + 88.00, "vision", "Pipeline restart requested (tag loss 2.1 s)");
        action(TELEOP_START + 88.05, "loop", "Loop 93 ms (danger > 80 ms)");
    }

    private void leg(double start, double end, double[] from, double[] to, boolean showPath) {
        legs.add(new Leg(start, end, from, to, showPath));
    }

    private void shoot(double at, String phase) {
        if (phase.equals("teleop")) {
            action(at, "driver", "RB: Launch all");
        } else {
            action(at - 1.5, "launcher", "IDLE -> SPINNING_UP");
        }
        action(at, "launcher", "READY -> FIRING");
        action(at + 0.4, "shot", "left");
        action(at + 0.9, "shot", "center");
        action(at + 1.4, "shot", "right");
        action(at + 2.2, "launcher", "FIRING -> IDLE");
    }

    private void intake(double start, double end, String phase) {
        if (phase.equals("teleop")) action(start, "driver", "LB: Intake (hold)");
        action(start, "intake", "OFF -> INTAKING");
        action(end - 0.2, "intake", "INTAKING -> FULL");
        action(end, "intake", "FULL -> OFF");
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
        log.putMetadata("Note", "Illustrative positions, not BIOBUZZ strategy");

        GamepadLog driverLog = new GamepadLog(0);
        GamepadLog operatorLog = new GamepadLog(1);
        GamepadLog.State driver = new GamepadLog.State();
        GamepadLog.State operator = new GamepadLog.State();

        double[] rpm = {IDLE_RPM * 0.2, IDLE_RPM * 0.2, IDLE_RPM * 0.2};
        double[] lagS = {0.30, 0.32, 0.65}; // right lane is the slow one
        String[] lanes = {"Left", "Center", "Right"};
        double[] drift = {0, 0, 0}; // raw odometry error since the start, Pedro in / rad
        double[] fusedError = {0, 0, 0}; // what the filter has not yet corrected
        double battery = 13.1;
        String lastMode = "";
        String launcherState = "IDLE";
        String intakeState = "OFF";
        boolean spinning = false;
        int nextAction = 0;
        double lastFix = 0;
        Leg lastPathLeg = null;
        double[] prevPose = START.clone();

        actions.sort((p, q) -> Double.compare(p.time, q.time));
        log.putEvent("OpMode init: Simulated match", 0);
        log.putEvent("Alliance RED (vision proposed, confirmed with X)", 1000);
        log.putEvent("Start check OK: 1.2 in, 2 deg from declared start", 2000);
        log.put(AdvantageScopeKeys.ALLIANCE_STATION, AdvantageScopeKeys.allianceStation(true, 1), 0);
        log.put(AdvantageScopeKeys.MATCH_NUMBER, 7L, 0);

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

            // Actions due this loop.
            driver.clear();
            operator.clear();
            while (nextAction < actions.size() && actions.get(nextAction).time <= t) {
                Action a = actions.get(nextAction++);
                switch (a.kind) {
                    case "driver":
                        log.putEvent("driver " + a.text, us);
                        break;
                    case "operator":
                        log.putEvent("operator " + a.text, us);
                        spinning = true;
                        launcherState = transition(log, "launcher", launcherState, "SPINNING_UP", us);
                        break;
                    case "launcher":
                        String to = a.text.substring(a.text.indexOf("-> ") + 3);
                        if (to.equals("FIRING") || to.equals("SPINNING_UP")) spinning = true;
                        if (to.equals("IDLE")) spinning = false;
                        launcherState = transition(log, "launcher", launcherState, to, us);
                        break;
                    case "intake":
                        intakeState = transition(log, "intake", intakeState,
                                a.text.substring(a.text.indexOf("-> ") + 3), us);
                        break;
                    case "shot":
                        int lane = a.text.equals("left") ? 0 : a.text.equals("center") ? 1 : 2;
                        rpm[lane] -= 450;
                        log.putEvent("launcher: shot " + a.text, us);
                        break;
                    default:
                        log.putEvent(a.kind + ": " + a.text, us);
                }
            }
            // Buttons held while their action is active.
            if (teleop && intakeState.equals("INTAKING")) driver.leftBumper = true;
            if (teleop && launcherState.equals("FIRING")) driver.rightBumper = true;
            if (teleop && launcherState.equals("SPINNING_UP")) operator.a = true;
            if (teleop && Math.abs(t - (TELEOP_START + 0.5)) < 0.15) driver.y = true;

            // Where the robot really is.
            Leg leg = activeLeg(t);
            double[] pose = leg == null ? prevPose.clone() : interpolate(leg, t);
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
            boolean nearHive = Math.hypot(pose[0] - SHOOT[0], pose[1] - SHOOT[1]) < 30;
            boolean rejected = Math.abs(t - (TELEOP_START + 61.8)) < LOOP_S / 2;
            if (rejected || ((auto || teleop) && nearHive && t - lastFix > 1.5)) {
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
            log.put("/Vision/TagsVisible", (auto || teleop) && nearHive ? 1L + (step / 40) % 2 : 0L, us);

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

            // The active path, written when it changes.
            Leg pathLeg = leg != null && leg.showPath ? leg : null;
            if (pathLeg != lastPathLeg) {
                log.putPose2dArray("/Path/Active", pathLeg == null ? new double[0] : pathPoses(pathLeg), us);
                lastPathLeg = pathLeg;
            }

            // Flywheel lanes: first-order toward target, feedforward plus proportional feedback.
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

    private Leg activeLeg(double t) {
        for (Leg l : legs) if (t >= l.start && t <= l.end) return l;
        return null;
    }

    /** Smooth start and stop (smoothstep), with a sideways bow so paths are visibly curved. */
    static double[] interpolate(Leg l, double t) {
        double u = (t - l.start) / (l.end - l.start);
        double s = u * u * (3 - 2 * u);
        double dx = l.to[0] - l.from[0];
        double dy = l.to[1] - l.from[1];
        double bow = 0.18 * Math.sin(Math.PI * s);
        return new double[] {
                l.from[0] + dx * s - dy * bow,
                l.from[1] + dy * s + dx * bow,
                l.from[2] + angleDiff(l.from[2], l.to[2]) * s};
    }

    private static double[] pathPoses(Leg l) {
        int n = 20;
        double[] out = new double[3 * (n + 1)];
        for (int i = 0; i <= n; i++) {
            double[] p = interpolate(l, l.start + (l.end - l.start) * i / n);
            out[3 * i] = AdvantageScopeFrame.xMeters(p[0], p[1]);
            out[3 * i + 1] = AdvantageScopeFrame.yMeters(p[0], p[1]);
            out[3 * i + 2] = AdvantageScopeFrame.headingRad(p[2]);
        }
        return out;
    }

    private static double angleDiff(double from, double to) {
        return AdvantageScopeFrame.wrap(to - from);
    }

    private static double clamp(double v) {
        return Math.max(-1, Math.min(1, v));
    }
}
