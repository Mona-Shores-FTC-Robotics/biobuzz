package org.firstinspires.ftc.teamcode.logging;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** A request's optional LEFT roll after TIP 1. Both comparison arms use the same release and target. */
final class NectarStudy {
    final AutoSim.NectarRoll roll;
    final int missVolley;
    final boolean loseAim;

    private NectarStudy(AutoSim.NectarRoll roll, int missVolley, boolean loseAim) {
        this.roll = roll;
        this.missVolley = missVolley;
        this.loseAim = loseAim;
    }

    static NectarStudy from(Object value) {
        if (value == null) return null;
        if (!(value instanceof Map)) throw new IllegalArgumentException("nectarStudy must be an object");
        Map<?, ?> study = (Map<?, ?>) value;
        Object delivery = study.get("roll");
        AutoSim.NectarRoll roll = null;
        if (delivery != null) {
            if (!(delivery instanceof Map)) throw new IllegalArgumentException("roll must be an object or null");
            Map<?, ?> r = (Map<?, ?>) delivery;
            roll = new AutoSim.NectarRoll((String) r.get("spot"), number(r, "x"), number(r, "y"),
                    number(r, "speed"), number(r, "delay"), number(r, "earliest"),
                    optional(r, "aimBiasDeg"), optional(r, "speedSpread"), optional(r, "aimSpreadDeg"));
        }
        double miss = study.containsKey("missFirstPollenOfRVolley") ? number(study, "missFirstPollenOfRVolley") : 0;
        if (miss < 0 || miss != Math.rint(miss) || miss > 3) throw new IllegalArgumentException("unknown R volley: " + miss);
        return new NectarStudy(roll, (int) miss, Boolean.TRUE.equals(study.get("loseCatchAim")));
    }

    private static double number(Map<?, ?> json, String name) {
        if (!(json.get(name) instanceof Number)) throw new IllegalArgumentException("expected a number for " + name);
        double n = ((Number) json.get(name)).doubleValue();
        if (!Double.isFinite(n)) throw new IllegalArgumentException("expected a finite " + name);
        return n;
    }

    private static double optional(Map<?, ?> json, String name) {
        return json.containsKey(name) ? number(json, name) : 0;
    }

    void configureRobot(AutoSim sim, Class<?> type) {
        String source = SimRunTest.source(type);
        if (missVolley != 0 && source.startsWith("r-")) sim.missPollen(missVolley, 1);
        if (loseAim && source.startsWith("l-")) sim.loseCatchAim();
    }

    private static Double time(double value) {
        return Double.isNaN(value) ? null : value;
    }

    void addLedger(Map<String, Object> row, AutoSim.Result r) {
        for (int i = 0; i < r.robots.size(); i++) {
            AutoSim.RobotResult robot = r.robots.get(i);
            String role = robot.auto.startsWith("l-") ? "L" : "R";
            row.put(role + "Robot", i + 1);
            row.put(role + "LastShot", time(robot.lastShotAt));
            row.put(role + "Wall", time(robot.hitWallAt));
            row.put(role + "Flower", time(robot.hitFlowerAt));
            row.put(role + "StartProblem", robot.illegalStart);
            row.put(role + "MaxHeld", r.maxLoad[i]);
        }
        row.put("extraFeedStart", time(r.extraFeedStart));
        row.put("extraFeedEnd", time(r.extraFeedEnd));
        row.put("extraShotAt", time(r.extraShotAt));
        AutoSim.HumanReceipt h = r.humanRoll;
        Map<String, Object> receipt = new LinkedHashMap<>();
        receipt.put("release", time(h.releaseAt));
        receipt.put("capture", time(h.captureAt));
        receipt.put("launch", time(h.launchAt));
        receipt.put("score", time(h.scoreAt));
        receipt.put("capturedLoad", h.capturedLoad);
        receipt.put("captureRobot", h.captureRobot);
        receipt.put("launchRobot", h.launchRobot);
        receipt.put("releaseSpeed", time(h.releaseSpeed));
        receipt.put("releaseHeading", time(h.releaseHeadingDeg));
        receipt.put("captureX", time(h.captureX));
        receipt.put("captureY", time(h.captureY));
        receipt.put("scoreCell", h.scoreCell);
        receipt.put("retainedAtTip", h.retainedAtTip);
        row.put("human", receipt);
        List<Object> loads = new ArrayList<>();
        for (int[] load : r.settledLoads) {
            Map<String, Object> pieces = new LinkedHashMap<>();
            pieces.put("pollen", load[0]);
            pieces.put("nectar", load[1]);
            pieces.put("human", load[2] != 0);
            loads.add(pieces);
        }
        row.put("settledLoads", loads);
        List<Object> shots = new ArrayList<>();
        for (AutoSim.Shot s : r.shots) {
            Map<String, Object> shot = new LinkedHashMap<>();
            shot.put("t", s.t);
            shot.put("robot", s.robot);
            shot.put("volley", s.volley);
            shot.put("number", s.number);
            shot.put("pollen", s.pollen);
            shot.put("humanTip", s.humanTip);
            shot.put("left", s.aimedLeft);
            shot.put("down", s.down);
            shot.put("extra", s.extra);
            shot.put("x", s.x);
            shot.put("y", s.y);
            shot.put("scoreAt", time(s.scoreAt));
            shot.put("scoreCell", s.scoreCell);
            shot.put("headingDeg", s.headingDeg);
            shot.put("turretDeg", s.turretDeg);
            shots.add(shot);
        }
        row.put("shots", shots);
    }
}
