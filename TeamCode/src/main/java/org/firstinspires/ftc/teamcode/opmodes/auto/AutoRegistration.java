package org.firstinspires.ftc.teamcode.opmodes.auto;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.autokit.AutoRegistry;
import org.firstinspires.ftc.teamcode.controls.MatchSetup;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.vision.CellSighting;
import org.firstinspires.ftc.teamcode.vision.HiveCell;

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

    /** How many TIPs an Auto can ask about: Tip1 to Tip{MAX_TIPS}. */
    public static final int MAX_TIPS = 4;

    private static final HiveCell[] CELLS = HiveCell.values();

    private AutoRegistration() {}

    public static AutoRegistry forRobot(Robot robot, MatchSetup setup) {
        AutoRegistry registry = new AutoRegistry()
                .condition("CameraBlind", () -> cameraBlind(robot));
        for (int n = 1; n <= MAX_TIPS; n++) {
            final int tip = n;
            registry.condition("Tip" + n, () -> hiveTipped(robot, setup.alliance(), tip));
        }
        return registry;
    }

    /**
     * Our HIVE's {@code n}th TIP has happened, by us or our partner. Once true it stays true, so
     * it does not matter when a card asks. Counted by the camera from the match-start position
     * (see {@link org.firstinspires.ftc.teamcode.vision.HiveTipCounter}).
     */
    static boolean hiveTipped(Robot robot, Alliance alliance, int n) {
        return robot.vision.hiveTips(alliance) >= n;
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
