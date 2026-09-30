package org.firstinspires.ftc.teamcode.opmodes.auto;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.autokit.AutoRegistry;
import org.firstinspires.ftc.teamcode.controls.MatchSetup;
import org.firstinspires.ftc.teamcode.vision.CellSighting;
import org.firstinspires.ftc.teamcode.vision.HiveCell;

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

    private AutoRegistration() {}

    public static AutoRegistry forRobot(Robot robot, MatchSetup setup) {
        return new AutoRegistry()
                // The HIVE has left GARDEN_UP (mid-tip or settled LOADING_UP): after launching at
                // the GARDEN CELL. Reads the HIVE now, so a TIP during the launch already counts.
                .trigger("HiveLeftGarden", () -> robot.vision.hiveState(setup.alliance()).leftGarden())
                // The TIP back: the HIVE has left LOADING_UP.
                .trigger("HiveLeftLoading", () -> robot.vision.hiveState(setup.alliance()).leftLoading())
                .trigger("CameraBlind", () -> cameraBlind(robot));
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
