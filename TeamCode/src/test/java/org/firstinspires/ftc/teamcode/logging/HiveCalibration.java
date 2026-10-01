package org.firstinspires.ftc.teamcode.logging;

import org.firstinspires.ftc.teamcode.vision.HiveState;
import org.firstinspires.ftc.teamcode.vision.HiveTracker;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * What a team measures about the HIVE and the pieces on a real field, and the fit that makes
 * {@link FieldSim} reproduce it: the HIVE in the simulation tips on the same POLLEN, and takes as
 * long, as the one on the field.
 *
 * <p><b>The measurements</b> (a field-session checklist is in {@code TeamCode/README.md}, "Calibrating
 * the HIVE"):
 * <ul>
 *   <li><b>POLLEN to tip.</b> Set a HIVE up for the start of a match (three NECTAR in the raised
 *       CELL). Drop POLLEN into the raised CELL one at a time, letting each settle. The count when
 *       it tips. {@link #MEASURED_POLLEN_TO_TIP}.</li>
 *   <li><b>Tip time.</b> Film a TIP in slow motion, first movement to resting. This is the robot's
 *       own {@link HiveTracker.Tuning#tipSeconds} (HIVE lesson 3), so the robot and the simulation
 *       share one number.</li>
 *   <li><b>Weights.</b> A POLLEN and a NECTAR on a kitchen scale. {@link #MEASURED_POLLEN_GRAMS},
 *       {@link #MEASURED_NECTAR_GRAMS}.</li>
 *   <li><b>Bounce.</b> Drop a POLLEN onto the tiles from a known height and film the first
 *       rebound against a tape measure. {@link #MEASURED_DROP_IN}, {@link #MEASURED_REBOUND_IN}.</li>
 * </ul>
 * Each is NaN until measured. Until then the simulation uses the {@code ASSUMED_} value and says so
 * in the log's metadata, so nobody mistakes a guess for a measurement.
 *
 * <p><b>The fit</b> ({@link #fit}) turns those into {@link FieldSim.Physics}, by running the same
 * experiments in the simulation:
 * <ul>
 *   <li>With the rocker held, it drops POLLEN into the raised CELL one at a time exactly as above
 *       and reads the resting torque after each. The holding torque goes halfway between the
 *       torque after one POLLEN short of the count and after the count, so the count tips it and one
 *       fewer does not.</li>
 *   <li>It then lets the rocker go, repeats the experiment, times the TIP, and scales the swing
 *       speed until the TIP takes the measured time.</li>
 *   <li>NECTAR's weight is the weight ratio; the tiles' restitution is √(rebound / drop).</li>
 * </ul>
 */
final class HiveCalibration {

    // ---- Measured on a field. NaN until measured. ----------------------------------------------

    static final double MEASURED_POLLEN_TO_TIP = Double.NaN;
    static final double MEASURED_POLLEN_GRAMS = Double.NaN;
    static final double MEASURED_NECTAR_GRAMS = Double.NaN;
    static final double MEASURED_DROP_IN = Double.NaN;
    static final double MEASURED_REBOUND_IN = Double.NaN;

    // ---- Assumed until then: not measurements. ------------------------------------------------

    static final int ASSUMED_POLLEN_TO_TIP = 3;
    static final double ASSUMED_NECTAR_PER_POLLEN = 1.5;
    static final double ASSUMED_TIP_SECONDS = 1.0;
    static final double ASSUMED_TILE_RESTITUTION = 0.35;

    final int pollenToTip;
    final double nectarPerPollen;
    final double tipSeconds;
    final double tileRestitution;
    /** The names of the values that are assumptions, not measurements. */
    final List<String> assumed;

    HiveCalibration(int pollenToTip, double nectarPerPollen, double tipSeconds, double tileRestitution,
                    List<String> assumed) {
        if (pollenToTip < 1) throw new IllegalArgumentException("POLLEN to tip must be at least 1");
        if (!(nectarPerPollen > 0) || !(tipSeconds > 0) || !(tileRestitution >= 0 && tileRestitution < 1)) {
            throw new IllegalArgumentException("calibration out of range");
        }
        this.pollenToTip = pollenToTip;
        this.nectarPerPollen = nectarPerPollen;
        this.tipSeconds = tipSeconds;
        this.tileRestitution = tileRestitution;
        this.assumed = assumed;
    }

    /** A calibration from given numbers, all treated as measured: for tests and what-ifs. */
    static HiveCalibration of(int pollenToTip, double nectarPerPollen, double tipSeconds, double tileRestitution) {
        return new HiveCalibration(pollenToTip, nectarPerPollen, tipSeconds, tileRestitution, new ArrayList<>());
    }

    /** What is measured, and the assumptions for the rest. */
    static HiveCalibration current() {
        List<String> assumed = new ArrayList<>();
        int pollen = ASSUMED_POLLEN_TO_TIP;
        if (MEASURED_POLLEN_TO_TIP >= 1) pollen = (int) Math.round(MEASURED_POLLEN_TO_TIP);
        else assumed.add("POLLEN to tip");
        double nectar = MEASURED_NECTAR_GRAMS / MEASURED_POLLEN_GRAMS;
        if (!(nectar > 0)) {
            nectar = ASSUMED_NECTAR_PER_POLLEN;
            assumed.add("NECTAR weight");
        }
        double tip = HiveTracker.Tuning.tipSeconds;
        if (!(tip > 0)) {
            tip = ASSUMED_TIP_SECONDS;
            assumed.add("tip time");
        }
        double restitution = Math.sqrt(MEASURED_REBOUND_IN / MEASURED_DROP_IN);
        if (!(restitution >= 0 && restitution < 1)) {
            restitution = ASSUMED_TILE_RESTITUTION;
            assumed.add("bounce");
        }
        return new HiveCalibration(pollen, nectar, tip, restitution, assumed);
    }

    /** For the log's metadata. */
    String describe() {
        return String.format(Locale.ROOT,
                "HIVE tips on POLLEN %d from match start; tip %.2f s; NECTAR %.2f POLLEN weights; tile restitution %.2f. %s",
                pollenToTip, tipSeconds, nectarPerPollen, tileRestitution,
                assumed.isEmpty() ? "All measured." : "Assumed, not measured: " + String.join(", ", assumed) + ".");
    }

    // ---- The fit --------------------------------------------------------------------------------

    private static final Map<String, FieldSim.Physics> FITTED = new HashMap<>();

    /** The simulation constants that reproduce this calibration. Fitted once per calibration. */
    FieldSim.Physics fit() {
        String key = pollenToTip + "/" + nectarPerPollen + "/" + tipSeconds + "/" + tileRestitution;
        synchronized (FITTED) {
            FieldSim.Physics physics = FITTED.get(key);
            if (physics == null) {
                physics = computeFit();
                FITTED.put(key, physics);
            }
            return physics;
        }
    }

    /** Seconds each dropped POLLEN is given to settle, on a real field and here. */
    static final double SETTLE_S = 1.5;
    private static final double LOOP_S = 0.02;

    private FieldSim.Physics computeFit() {
        // 1. The resting torque after each POLLEN, with the rocker held on its stop.
        double[] torque = restingTorques(pollenToTip);
        double hold = (torque[pollenToTip - 1] + torque[pollenToTip]) / 2;
        if (!(torque[pollenToTip] > torque[pollenToTip - 1])) {
            throw new IllegalStateException("the last POLLEN did not add torque; the CELL is full?");
        }

        // 2. The swing speed that makes that TIP take the measured time.
        double swing = 1.5;
        for (int i = 0; i < 6; i++) {
            double took = timedTip(new FieldSim.Physics(nectarPerPollen, hold, swing, tileRestitution));
            if (Double.isNaN(took)) throw new IllegalStateException("the fitted HIVE did not tip");
            if (Math.abs(took - tipSeconds) < 0.01 * tipSeconds) break;
            swing *= took / tipSeconds;
        }
        return new FieldSim.Physics(nectarPerPollen, hold, swing, tileRestitution);
    }

    /** {@code torque[k]}: the red HIVE's tipping torque at rest with k POLLEN dropped in, rocker held. */
    double[] restingTorques(int count) {
        FieldSim sim = matchStart(new FieldSim.Physics(nectarPerPollen, 1, 1, tileRestitution));
        sim.red.locked = true;
        double[] torque = new double[count + 1];
        torque[0] = settle(sim, sim.red);
        for (int k = 1; k <= count; k++) {
            sim.dropIntoRaisedCell(sim.red);
            torque[k] = settle(sim, sim.red);
        }
        return torque;
    }

    /**
     * The calibration experiment on a free rocker: drops POLLEN one at a time until it tips, and
     * returns how long the TIP took, or NaN if it never did.
     */
    double timedTip(FieldSim.Physics physics) {
        FieldSim sim = matchStart(physics);
        for (int k = 0; k < pollenToTip + 3 && sim.red.tips == 0; k++) {
            sim.dropIntoRaisedCell(sim.red);
            settleRocker(sim);
        }
        return sim.red.tips == 0 ? Double.NaN : sim.red.lastTipSeconds;
    }

    /** How many POLLEN, dropped one at a time, it takes to tip a HIVE with these physics; 0 if none do. */
    static int pollenThatTip(FieldSim.Physics physics, int max) {
        FieldSim sim = matchStart(physics);
        for (int k = 1; k <= max; k++) {
            sim.dropIntoRaisedCell(sim.red);
            settleRocker(sim);
            if (sim.red.tips > 0) return k;
        }
        return 0;
    }

    /**
     * Lets a dropped POLLEN settle, and a rocker that has started to move finish: a piece rolling
     * to the back of the CELL can lift it off its stop for a moment without tipping it.
     */
    private static void settleRocker(FieldSim sim) {
        for (int i = 0; i < Math.round(SETTLE_S / LOOP_S); i++) sim.step(LOOP_S);
        for (int i = 0; i < 1000 && sim.red.state() == HiveState.TRANSITION; i++) sim.step(LOOP_S);
    }

    /** Only what is in the CELLs at the start of a match: the three NECTAR in each raised CELL. */
    static FieldSim matchStart(FieldSim.Physics physics) {
        List<HiveAssets.StagedPiece> inCells = new ArrayList<>();
        try {
            for (HiveAssets.StagedPiece p : HiveAssets.committedStagedPieces()) {
                if (p.holder.equals("cell")) inCells.add(p);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return new FieldSim(inCells, 1, physics);
    }

    private static double settle(FieldSim sim, FieldSim.Rocker r) {
        for (int i = 0; i < Math.round(SETTLE_S / LOOP_S); i++) sim.step(LOOP_S);
        return sim.tippingTorque(r);
    }
}
