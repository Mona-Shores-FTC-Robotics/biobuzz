package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.pedropathing.ivy.commands.Commands;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.autokit.AutoRegistry;
import org.firstinspires.ftc.teamcode.controls.MatchSetup;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.vision.CellSighting;
import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.HiveCellState;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;

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
 *
 * <h2>Placeholders</h2>
 *
 * <p>The Autos were drawn before the mechanisms existed. Every name they use is registered, so
 * they run on the robot today as timed scripts: a placeholder command finishes at once, and a
 * placeholder trigger never fires, so a wait on it runs to its time limit. {@link #PLACEHOLDERS}
 * lists them, {@link BuiltAuto} shows the list on the Driver Station at INIT, and each one goes
 * when its mechanism lands:
 * <ul>
 *   <li>{@code LaunchAll}, {@code LaunchOne}, {@code SpinUp}, {@code Empty}: the launcher.</li>
 *   <li>{@code IntakeFull}: the intake, counting pieces (#181).</li>
 *   <li>{@code LeftCellUp}, {@code RightCellUp}: which CELL is up as the drive team sees it;
 *       needs the left/right naming of the CELLs (#114) and the HIVE tracker (#173).</li>
 * </ul>
 */
public final class AutoRegistration {

    /** How long without a HIVE tag before the camera counts as blind. */
    public static final double BLIND_AFTER_MS = 500;

    /** The names registered as placeholders, in registration order. Shown at INIT. */
    public static final List<String> PLACEHOLDERS = Collections.unmodifiableList(Arrays.asList(
            "LaunchAll", "LaunchOne", "SpinUp", "Empty", "IntakeFull", "LeftCellUp", "RightCellUp"));

    private static final HiveCell[] CELLS = HiveCell.values();

    private AutoRegistration() {}

    /**
     * The registry for this robot. {@code robot} and {@code setup} may be null only to
     * {@link AutoRegistry#describe() describe} the list; a null robot's triggers are never called.
     */
    public static AutoRegistry forRobot(Robot robot, MatchSetup setup) {
        return new AutoRegistry()
                // ---- placeholders: the launcher and the intake do not exist on this robot yet.
                .command("LaunchAll", 2.0, () -> Commands.instant(() -> { }))
                .command("LaunchOne", 0.5, () -> Commands.instant(() -> { }))
                .command("SpinUp", 0.1, () -> Commands.instant(() -> { }))
                .trigger("Empty", () -> false)
                .trigger("IntakeFull", () -> false)
                .trigger("LeftCellUp", () -> false)
                .trigger("RightCellUp", () -> false)
                // ---- real: the camera.
                // Our HIVE has tipped since this wait began: one of our CELLs that was settled UP
                // or DOWN when the wait started is now settled the other way. Settled state comes
                // from the Limelight's CellStateTracker, which the vision subsystem keeps current.
                // Mid-tip detection (tags vanishing while the robot holds still) is #173's.
                // A camera that never saw the HIVE, or no alliance chosen, never says Tip.
                .triggerSince("Tip", () -> tipSince(robot, setup))
                .trigger("CameraBlind", () -> cameraBlind(robot));
    }

    /** The check for one wait: true once one of our CELLs has settled in the other state. */
    static BooleanSupplier tipSince(Robot robot, MatchSetup setup) {
        if (robot == null || setup == null) return () -> false;
        final Alliance alliance = setup.alliance();
        final HiveCellState[] before = new HiveCellState[CELLS.length];
        for (int i = 0; i < CELLS.length; i++) {
            before[i] = CELLS[i].belongsTo(alliance) ? robot.vision.state(CELLS[i]) : HiveCellState.UNKNOWN;
        }
        return () -> {
            for (int i = 0; i < CELLS.length; i++) {
                if (before[i] == HiveCellState.UNKNOWN) continue;
                HiveCellState now = robot.vision.state(CELLS[i]);
                if (now != HiveCellState.UNKNOWN && now != before[i]) return true;
            }
            return false;
        };
    }

    /** No HIVE tag from any CELL for {@link #BLIND_AFTER_MS}, or no camera at all. */
    static boolean cameraBlind(Robot robot) {
        if (robot == null || !robot.vision.isAvailable()) return true;
        for (HiveCell cell : CELLS) {
            CellSighting sighting = robot.vision.sighting(cell);
            if (sighting != null && sighting.ageMs() <= BLIND_AFTER_MS) return false;
        }
        return true;
    }
}
