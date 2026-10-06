package org.firstinspires.ftc.teamcode.logging;

import org.firstinspires.ftc.teamcode.vision.HiveState;

import java.io.IOException;
import java.util.Arrays;

/**
 * Writes {@link FieldSim}'s state into a {@code .wpilog} under the keys AdvantageScope's 3D field
 * draws (see {@code TeamCode/README.md}, "Game pieces and the HIVE in a simulated .wpilog"). Shared
 * by every simulated log, so they all open the same way.
 *
 * <p>Each value is written only when it changes: pieces at rest and a settled HIVE cost nothing,
 * so the file stays small.
 */
final class FieldSimLog {

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

    private final double[][] pieces = new double[6][];
    private double[] components;
    private final String[] hiveState = new String[2];
    private final int[] tips = {-1, -1};
    private final int[] raisedCount = {-1, -1};
    private int held = -1;
    /** Whether this writes the held pieces too; false when {@link RobotInternalsLog} draws them. */
    boolean drawsHeld = true;

    /** How to open the log, and how the HIVE was calibrated. */
    static void putMetadata(WpiLog log, HiveCalibration calibration) throws IOException {
        log.putMetadata("GamePieces", "Field '" + HiveAssets.FIELD_NAME + "': add " + KEY_POLLEN + " and the two"
                + " NECTAR keys as Game Piece objects, and " + KEY_HIVE + " as a Robot ('" + HiveAssets.ROBOT_NAME
                + "') with " + KEY_HIVE_COMPONENTS + " as its components. Build the assets with HiveAssetsTest.");
        log.putMetadata("Calibration", calibration.describe());
    }

    /** The HIVE "robot" stands at the field origin for the whole log. */
    static void putHiveStructure(WpiLog log) throws IOException {
        log.putPose3dFlat(KEY_HIVE, 0, 0, 0, 0, 0);
    }

    void write(WpiLog log, FieldSim sim, long us) throws IOException {
        String[] keys = {KEY_POLLEN, KEY_RED_NECTAR, KEY_BLUE_NECTAR,
                KEY_HELD_POLLEN, KEY_HELD_RED_NECTAR, KEY_HELD_BLUE_NECTAR};
        FieldSim.Kind[] kinds = FieldSim.Kind.values();
        for (int i = 0; i < 6; i++) {
            if (i >= 3 && !drawsHeld) continue;
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
