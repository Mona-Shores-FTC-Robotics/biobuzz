package org.firstinspires.ftc.teamcode.opmodes.auto;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.autokit.AutoRegistry;
import org.firstinspires.ftc.teamcode.controls.MatchSetup;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.vision.CellSighting;
import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.HiveCellState;

/**
 * The names this robot offers the Auto Builder: every action and condition an Auto drawn in the
 * editor may use. The editor's registry panel must list the same names.
 *
 * <p>Add a name here when the mechanism behind it exists. A generated Auto that uses a name missing
 * here stops at INIT and lists what is missing — it never runs half an Auto.
 *
 * <p>Conditions read state a subsystem already keeps current; they are called once per loop, only
 * while a card that uses them is waiting, so they must not read hardware or allocate.
 */
public final class AutoRegistration {

    /** How long without a HIVE tag before the camera counts as blind. */
    public static final double BLIND_AFTER_MS = 500;

    private static final HiveCell[] CELLS = HiveCell.values();

    private AutoRegistration() {}

    public static AutoRegistry forRobot(Robot robot, MatchSetup setup) {
        return new AutoRegistry()
                .condition("HiveTipped", () -> hiveTipped(robot, setup.alliance()))
                .condition("CameraBlind", () -> cameraBlind(robot));
    }

    /**
     * Our HIVE has tipped: our LOADING CELL is settled UP. Every match starts with each alliance's
     * GARDEN CELL up and its LOADING CELL down, so no starting state needs remembering.
     */
    static boolean hiveTipped(Robot robot, Alliance alliance) {
        HiveCell loading = loadingCell(alliance);
        return loading != null && robot.vision.state(loading) == HiveCellState.UP;
    }

    /**
     * The alliance's LOADING CELL: the one that starts DOWN. The red one is at the rear (the SDK's
     * "RED SCORING" cluster), the blue one at the audience end. #114 renames the enum to say so.
     */
    static HiveCell loadingCell(Alliance alliance) {
        if (alliance == Alliance.RED) return HiveCell.RED_SCORING;
        if (alliance == Alliance.BLUE) return HiveCell.BLUE_AUDIENCE;
        return null;
    }

    /** No HIVE tag from any CELL for {@link #BLIND_AFTER_MS}, or no camera at all. */
    static boolean cameraBlind(Robot robot) {
        if (!robot.vision.isAvailable()) return true;
        for (HiveCell cell : CELLS) {
            CellSighting sighting = robot.vision.sighting(cell);
            if (sighting != null && sighting.ageMs() <= BLIND_AFTER_MS) return false;
        }
        return true;
    }
}
