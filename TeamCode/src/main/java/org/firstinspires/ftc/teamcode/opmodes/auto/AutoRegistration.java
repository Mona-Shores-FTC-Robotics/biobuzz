package org.firstinspires.ftc.teamcode.opmodes.auto;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.autokit.AutoRegistry;
import org.firstinspires.ftc.teamcode.controls.MatchSetup;
import org.firstinspires.ftc.teamcode.vision.CellSighting;
import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.HiveTracker;

/**
 * The commands and triggers this robot offers the Auto Builder: every name an Auto drawn in the
 * editor may use. The editor loads the same list from {@code TeamCode/auto-registry.json}, which
 * {@code AutoRegistrationTest} keeps in step with this class.
 *
 * <p>Add a command when the mechanism behind it exists, with its typical time (what the preview
 * shows). A generated Auto that uses a name missing here stops at INIT and lists what is missing:
 * it never runs half an Auto.
 *
 * <p>Triggers read state a subsystem already keeps current; they are called once per loop, only
 * while a step waits on them, so they must not read hardware or allocate. Registering reads
 * nothing, so the list can be written without a robot.
 */
public final class AutoRegistration {

    /** How long without a HIVE tag before the camera counts as blind. */
    public static final double BLIND_AFTER_MS = 500;

    private static final HiveCell[] CELLS = HiveCell.values();
    private static final HiveTracker NO_HIVE = new HiveTracker();

    private AutoRegistration() {}

    public static AutoRegistry forRobot(Robot robot, MatchSetup setup) {
        return new AutoRegistry()
                // Our HIVE, LEFT and RIGHT as our drivers see them. "Down" is true from the moment
                // that CELL starts down (seen mid-tip, or its tags vanish while the robot keeps
                // looking), because a TIP that has started finishes; "Up" once it is seen
                // settled, or once the measured TIP time has passed. Reads the HIVE now, so a
                // TIP during a launch already counts when the wait starts.
                .trigger("RightCellDown", () -> hive(robot, setup).rightCellDown())
                .trigger("LeftCellUp", () -> hive(robot, setup).leftCellUp())
                .trigger("LeftCellDown", () -> hive(robot, setup).leftCellDown())
                .trigger("RightCellUp", () -> hive(robot, setup).rightCellUp())
                .trigger("CameraBlind", () -> cameraBlind(robot));
    }

    /** Our alliance's HIVE; a tracker that never moves if the alliance is still UNKNOWN. */
    private static HiveTracker hive(Robot robot, MatchSetup setup) {
        HiveTracker hive = robot.hive.of(setup.alliance());
        return hive == null ? NO_HIVE : hive;
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
