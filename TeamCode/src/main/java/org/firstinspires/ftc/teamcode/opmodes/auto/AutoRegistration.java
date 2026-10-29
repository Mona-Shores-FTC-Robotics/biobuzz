package org.firstinspires.ftc.teamcode.opmodes.auto;


import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.autokit.AutoRegistry;
import org.firstinspires.ftc.teamcode.controls.MatchSetup;
import org.firstinspires.ftc.teamcode.opmodes.FlowerPicker;
import org.firstinspires.ftc.teamcode.opmodes.GardenGather;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.vision.CellSighting;
import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.HiveCellState;
import org.firstinspires.ftc.teamcode.vision.HiveState;
import org.firstinspires.ftc.teamcode.vision.HiveTracker;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

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
 *   <li>{@code IntakeFull}: the intake, counting pieces (#181). The roller itself already runs
 *       for the whole Auto ({@link BuiltAuto}); only the count is missing.</li>
 * </ul>
 *
 * <p>{@code PickFlower} is {@link FlowerPicker}, the routine the driver's D-pad up runs, and
 * {@code GardenGather} is {@link GardenGather}, the driver's D-pad down. Give their cards a timeout
 * longer than the grab (the Visualizer's builder sets 8 s): a card's default 5 s can cut one short.
 *
 * <p>{@code SpinUp}, {@code LaunchAll}, {@code LaunchOne} and {@code Empty} are real since the
 * launcher and feeder landed (#194): see {@link LaunchCards}, including what {@code Empty} means
 * without a piece sensor.
 *
 * <h2>LeftCellUp and RightCellUp</h2>
 *
 * <p>True while our HIVE's {@link HiveTracker} says that CELL is up, LEFT and RIGHT as our drive
 * team sees them from our alliance area ({@link HiveState}). They are conditions, not events: the
 * Auto Builder lists only {@code Tip} as an event, so a wait on {@code RightCellUp} ends at once if
 * the RIGHT CELL is already up when it starts. Nothing is guessed:
 * <ul>
 *   <li>No alliance chosen: false. {@link BuiltAuto} does not move without one, so this only
 *       matters to a caller outside it.</li>
 *   <li>{@link HiveState#UNSEEN} or {@link HiveState#TRANSITION}: false for both, so the wait runs
 *       to its limit or another row ends it. In particular the match-start position is not
 *       assumed: a HIVE the camera has not seen yet is not "RIGHT CELL up".</li>
 *   <li>A position the tracker assumes after a TIP it saw start but not finish
 *       ({@link HiveTracker#assumed()}): taken as it stands. That TIP was seen, and the tracker
 *       holds the guess only until the camera next sees the HIVE.</li>
 * </ul>
 */
public final class AutoRegistration {

    /** How long without a HIVE tag before the camera counts as blind. */
    public static final double BLIND_AFTER_MS = 500;

    /** The names registered as placeholders, in registration order. Shown at INIT. */
    public static final List<String> PLACEHOLDERS = Collections.unmodifiableList(Arrays.asList(
            "IntakeFull"));

    private static final HiveCell[] CELLS = HiveCell.values();

    private AutoRegistration() {}

    /**
     * The registry for this robot. {@code robot}, {@code setup} and {@code settle} may be null only
     * to {@link AutoRegistry#describe() describe} the list; a null robot's triggers are never called.
     * The launch cards fire only once {@code settle} says the robot is settled; {@code note} gets a
     * line when one fires without.
     */
    public static AutoRegistry forRobot(Robot robot, MatchSetup setup, PathEndSettle settle, Consumer<String> note) {
        // The launcher's cards share one object, so Empty can see which LaunchAll has finished. A
        // null robot only describes the list; its factories are never called.
        final LaunchCards launch = robot == null ? null : LaunchCards.forRobot(robot, settle, note);
        return new AutoRegistry()
                // ---- real: the launcher and the feeder. Typical seconds are what the editor's
                // preview shows; the feed times themselves are LaunchCards' Panels numbers.
                .command("LaunchAll", 2.0, () -> launch.launchAll())
                .command("LaunchOne", 0.5, () -> launch.launchOne())
                .command("SpinUp", 0.1, () -> launch.spinUp())
                // ---- real: the FLOWER pick, the same routine as the driver's D-pad up. It starts at
                // the pick spot an Auto drives to and ends back there (FlowerPicker's contract).
                .command("PickFlower", 4.0, () -> FlowerPicker.command(robot, note))
                // ---- real: the GARDEN gather, the same routine as the driver's D-pad down. It drives in
                // to the GARDEN from wherever the card is called and back there (GrabRoutine's contract).
                .command("GardenGather", 4.0, () -> GardenGather.command(robot, note))
                .triggerSince("Empty", () -> launch == null ? () -> false : launch.emptySince())
                // ---- placeholder: the intake does not count pieces yet (#181).
                .trigger("IntakeFull", () -> false)
                // ---- real: the camera, through our HIVE's tracker. See the class comment.
                .trigger("LeftCellUp", () -> cellUp(ourHive(robot, setup), true))
                .trigger("RightCellUp", () -> cellUp(ourHive(robot, setup), false))
                // Our HIVE has tipped since this wait began: one of our CELLs that was settled UP
                // or DOWN when the wait started is now settled the other way. Settled state comes
                // from the Limelight's CellStateTracker, which the vision subsystem keeps current.
                // Mid-tip detection (tags vanishing while the robot holds still) is #173's.
                // A camera that never saw the HIVE, or no alliance chosen, never says Tip.
                .triggerSince("Tip", () -> tipSince(robot, setup))
                .trigger("CameraBlind", () -> cameraBlind(robot));
    }

    /**
     * {@code LeftCellUp} ({@code left}) or {@code RightCellUp}: what our HIVE's tracker says, and
     * false with no tracker (no alliance chosen). UNSEEN and TRANSITION are false for both.
     */
    static boolean cellUp(HiveTracker hive, boolean left) {
        if (hive == null) return false;
        return left ? hive.leftCellUp() : hive.rightCellUp();
    }

    /** Our alliance's HIVE; null with no robot, no setup or no alliance chosen. */
    private static HiveTracker ourHive(Robot robot, MatchSetup setup) {
        return robot == null || setup == null ? null : robot.hive.of(setup.alliance());
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
