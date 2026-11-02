package org.firstinspires.ftc.teamcode.logging;

import org.firstinspires.ftc.teamcode.vision.HiveState;
import org.firstinspires.ftc.teamcode.vision.HiveTracker;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * How a real HIVE is calibrated, and the fit that makes {@link FieldSim}'s HIVE match it.
 *
 * <p><b>Published, so used as is.</b> FIRST has every HIVE at an event calibrated by field staff
 * (2026-2027 <i>Event Field Setup Guide</i> V1.0, §12, "Hive Calibration"): ballast washers are
 * added or removed until each CELL, with pieces gently placed against its back skin,
 * <ul>
 *   <li>tips on {@code [3] NECTAR + [3] POLLEN} and holds on {@code [3] NECTAR + [2] POLLEN};</li>
 *   <li>tips on {@code [8] POLLEN} and holds on {@code [7] POLLEN};</li>
 *   <li>and, "tossed in", still tips on the 3rd and 8th POLLEN and still holds on the 2nd and 7th
 *       (§12.3).</li>
 * </ul>
 * Those are {@link #TIP_CASES}. The weights are AndyMark's specification for the pieces
 * (am-5851 POLLEN 0.055 lb, am-5852 NECTAR 0.091 lb). A match starts with three NECTAR in each raised
 * CELL (§11.1), so from the start of a match the 3rd POLLEN tips it.
 *
 * <p><b>Not published, so measured by us</b> (checklist in {@code TeamCode/README.md}, under "Game
 * pieces and the HIVE in a simulated .wpilog"):
 * <ul>
 *   <li><b>Tip time.</b> Film a TIP, first movement to resting. This is the robot's own
 *       {@link HiveTracker.Tuning#tipSeconds} (HIVE lesson 3), so the robot and the simulation share
 *       one number.</li>
 *   <li><b>Bounce.</b> A POLLEN dropped on the tiles from a known height, and its first rebound.
 *       {@link #MEASURED_DROP_IN}, {@link #MEASURED_REBOUND_IN}.</li>
 * </ul>
 * Each is NaN until measured; until then the simulation uses the {@code ASSUMED_} value and the
 * log's metadata says so.
 *
 * <p><b>The fit</b> ({@link #fit}) runs FIRST's calibration in the simulation. With the rocker held,
 * it places each case's pieces against the back skin and reads the resting torque: once with the
 * case one POLLEN short (must hold) and once with it complete (must tip). The holding torque goes
 * in the middle of the range every case allows. It then times a TIP and scales the swing speed
 * until the TIP takes the measured time. {@code HiveCalibrationTest} runs all six rows of §12.3
 * against the result.
 */
final class HiveCalibration {

    /** A combination of pieces that must tip an upward CELL, and hold with one POLLEN fewer. */
    static final class TipCase {
        final int nectar;
        final int pollen;

        TipCase(int nectar, int pollen) {
            this.nectar = nectar;
            this.pollen = pollen;
        }

        @Override
        public String toString() {
            return nectar + " NECTAR + " + pollen + " POLLEN";
        }
    }

    // ---- Published --------------------------------------------------------------------------

    /** Event Field Setup Guide V1.0 §12 and §12.3. */
    static final List<TipCase> TIP_CASES = Arrays.asList(new TipCase(3, 3), new TipCase(0, 8));
    /** AndyMark's specification for am-5851 and am-5852. */
    static final double POLLEN_LB = 0.055;
    static final double NECTAR_LB = 0.091;
    /** Pieces in each upward CELL at the start of a match (Event Field Setup Guide §11.1). */
    static final int NECTAR_AT_MATCH_START = 3;

    // ---- Measured by us. NaN until measured. ----------------------------------------------------

    static final double MEASURED_DROP_IN = Double.NaN;
    static final double MEASURED_REBOUND_IN = Double.NaN;
    // The tip time is HiveTracker.Tuning.tipSeconds.

    // ---- Assumed until then: not measurements. ------------------------------------------------

    static final double ASSUMED_TIP_SECONDS = 1.0;
    static final double ASSUMED_TILE_RESTITUTION = 0.35;

    /** A gentle human toss into the CELL, in/s, for the "tossed in" rows of §12.3. */
    static final double TOSS_IN_PER_S = 60;

    final List<TipCase> tipCases;
    final double nectarPerPollen;
    final double tipSeconds;
    final double tileRestitution;
    /** The names of the values that are assumptions, not measurements. */
    final List<String> assumed;

    HiveCalibration(List<TipCase> tipCases, double nectarPerPollen, double tipSeconds, double tileRestitution,
                    List<String> assumed) {
        if (tipCases.isEmpty() || !(nectarPerPollen > 0) || !(tipSeconds > 0)
                || !(tileRestitution >= 0 && tileRestitution < 1)) {
            throw new IllegalArgumentException("calibration out of range");
        }
        this.tipCases = tipCases;
        this.nectarPerPollen = nectarPerPollen;
        this.tipSeconds = tipSeconds;
        this.tileRestitution = tileRestitution;
        this.assumed = assumed;
    }

    /** FIRST's calibration with a given tip time and bounce, all treated as measured: for tests. */
    static HiveCalibration of(double tipSeconds, double tileRestitution) {
        return new HiveCalibration(TIP_CASES, NECTAR_LB / POLLEN_LB, tipSeconds, tileRestitution, new ArrayList<>());
    }

    /** The tip time the HIVE is fitted to: the measured one if set, else the assumed one. */
    static double calibratedTipSeconds() {
        double tip = HiveTracker.Tuning.tipSeconds;
        return tip > 0 ? tip : ASSUMED_TIP_SECONDS;
    }

    /** FIRST's calibration, what we have measured, and the assumptions for the rest. */
    static HiveCalibration current() {
        List<String> assumed = new ArrayList<>();
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
        return new HiveCalibration(TIP_CASES, NECTAR_LB / POLLEN_LB, tip, restitution, assumed);
    }

    /** The POLLEN that tips a HIVE from the start of a match: the 3rd. */
    int pollenToTipFromMatchStart() {
        for (TipCase c : tipCases) if (c.nectar == NECTAR_AT_MATCH_START) return c.pollen;
        throw new IllegalStateException("no calibration case starts from the match-start NECTAR");
    }

    /** For the log's metadata. */
    String describe() {
        List<String> cases = new ArrayList<>();
        for (TipCase c : tipCases) cases.add(c.toString());
        return String.format(Locale.ROOT,
                "HIVE calibrated as FIRST's Event Field Setup Guide 12.3: tips on %s, holds on one POLLEN fewer;"
                        + " NECTAR %.2f POLLEN weights (AndyMark); tip %.2f s; tile restitution %.2f. %s",
                String.join(" and on ", cases), nectarPerPollen, tipSeconds, tileRestitution,
                assumed.isEmpty() ? "Tip time and bounce measured." : "Assumed, not measured: " + String.join(", ", assumed) + ".");
    }

    // ---- The fit --------------------------------------------------------------------------------

    private static final Map<String, FieldSim.Physics> FITTED = new HashMap<>();

    /**
     * The simulation constants that reproduce this calibration. Fitted once per calibration, always on
     * normal tiles: the fit settles pieces and times a TIP, and {@link FieldSim#frictionScale} (a what-if
     * for slow tiles) would otherwise fit a different HIVE for whichever friction ran first (6 Oct 2026:
     * AutoStudyTest on slow tiles fitted under 3x friction, ShapeMatchTest under 1x, and the same Auto and
     * seeds scored 61.3 and 57.3).
     */
    FieldSim.Physics fit() {
        String key = tipCases + "/" + nectarPerPollen + "/" + tipSeconds + "/" + tileRestitution;
        synchronized (FITTED) {
            FieldSim.Physics physics = FITTED.get(key);
            if (physics == null) {
                double friction = FieldSim.frictionScale;
                double[] tipRange = FieldSim.tipSecondsRange, dwellRange = FieldSim.tipDwellRange;
                FieldSim.frictionScale = 1;
                FieldSim.tipSecondsRange = null;  // fitted at the calibrated speed; FieldSim varies each TIP from it
                FieldSim.tipDwellRange = null;  // the fit times the swing itself; the dwell before it is FieldSim's
                try {
                    physics = computeFit();
                } finally {
                    FieldSim.frictionScale = friction;
                    FieldSim.tipSecondsRange = tipRange;
                    FieldSim.tipDwellRange = dwellRange;
                }
                FITTED.put(key, physics);
            }
            return physics;
        }
    }

    /** Forgets every fit, so a test can fit again (HiveCalibrationTest). */
    static void forgetFits() {
        synchronized (FITTED) {
            FITTED.clear();
        }
    }

    /** Seconds each placed piece is given to settle. */
    static final double SETTLE_S = 1.5;
    static final double LOOP_S = 0.02;

    private FieldSim.Physics computeFit() {
        // 1. The holding torque: above every "holds" case, below every "tips" case.
        FieldSim.Physics probe = new FieldSim.Physics(nectarPerPollen, 1, 1, tileRestitution);
        double highestHold = Double.NEGATIVE_INFINITY;
        double lowestTip = Double.POSITIVE_INFINITY;
        StringBuilder seen = new StringBuilder();
        for (TipCase c : tipCases) {
            double holds = restingTorque(probe, c.nectar, c.pollen - 1);
            double tips = restingTorque(probe, c.nectar, c.pollen);
            highestHold = Math.max(highestHold, holds);
            lowestTip = Math.min(lowestTip, tips);
            seen.append(String.format(Locale.ROOT, " %s: holds at %.1f, tips at %.1f;", c, holds, tips));
        }
        if (!(highestHold < lowestTip)) {
            throw new IllegalStateException("no holding torque satisfies every case:" + seen);
        }
        double hold = (highestHold + lowestTip) / 2;

        // 2. The swing speed that makes a TIP take the measured time.
        double swing = 1.5;
        for (int i = 0; i < 6; i++) {
            double took = timedTip(new FieldSim.Physics(nectarPerPollen, hold, swing, tileRestitution));
            if (Double.isNaN(took)) throw new IllegalStateException("the fitted HIVE did not tip");
            if (Math.abs(took - tipSeconds) < 0.01 * tipSeconds) break;
            swing *= took / tipSeconds;
        }
        return new FieldSim.Physics(nectarPerPollen, hold, swing, tileRestitution);
    }

    /** The red HIVE's tipping torque at rest, rocker held, with these pieces placed in its upward CELL. */
    static double restingTorque(FieldSim.Physics physics, int nectar, int pollen) {
        FieldSim sim = upwardCell(physics, nectar, pollen, true);
        return sim.tippingTorque(sim.red);
    }

    /** Times a TIP: match start, POLLEN placed one at a time until it tips. NaN if it never does. */
    double timedTip(FieldSim.Physics physics) {
        double[] tipRange = FieldSim.tipSecondsRange, dwellRange = FieldSim.tipDwellRange;
        FieldSim.tipSecondsRange = null;  // the calibrated speed, not one TIP's draw
        FieldSim.tipDwellRange = null;  // and the swing alone, without the dwell before it
        try {
            FieldSim sim = upwardCell(physics, NECTAR_AT_MATCH_START, 0, false);
            for (int k = 0; k < pollenToTipFromMatchStart() + 3 && sim.red.tips == 0; k++) {
                sim.placeInRaisedCell(sim.red, FieldSim.Kind.POLLEN);
                settleRocker(sim);
            }
            return sim.red.tips == 0 ? Double.NaN : sim.red.lastTipSeconds;
        } finally {
            FieldSim.tipSecondsRange = tipRange;
            FieldSim.tipDwellRange = dwellRange;
        }
    }

    /**
     * The red HIVE with {@code nectar} and {@code pollen} placed against the back skin of its
     * upward CELL, one at a time, each settled; the rocker {@code held} on its stop or free.
     */
    static FieldSim upwardCell(FieldSim.Physics physics, int nectar, int pollen, boolean held) {
        FieldSim sim = new FieldSim(new ArrayList<>(), 1, physics);
        sim.red.locked = true; // the starting NECTAR go in with the HIVE held, as field staff do
        for (int i = 0; i < nectar; i++) {
            sim.placeInRaisedCell(sim.red, FieldSim.Kind.RED_NECTAR);
            settle(sim);
        }
        sim.red.locked = held;
        for (int i = 0; i < pollen; i++) {
            if (sim.placeInRaisedCell(sim.red, FieldSim.Kind.POLLEN) == null) break;
            if (held) settle(sim);
            else settleRocker(sim);
        }
        return sim;
    }

    /** The match-start pieces in the CELLs only, from the field CAD. */
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

    static void settle(FieldSim sim) {
        for (int i = 0; i < Math.round(SETTLE_S / LOOP_S); i++) sim.step(LOOP_S);
    }

    /**
     * Lets a piece settle, and a rocker that has started to move finish: a piece rolling to the
     * back of the CELL can lift it off its stop for a moment without tipping it. A rocker over its
     * tipping weight in its dwell (FieldSim.tipDwellRange, up to 3.4 s) is waited out too.
     */
    static void settleRocker(FieldSim sim) {
        settle(sim);
        for (int i = 0; i < 1000 && (sim.rockerBusy(sim.red) || sim.rockerBusy(sim.blue)); i++) {
            sim.step(LOOP_S);
        }
    }
}
