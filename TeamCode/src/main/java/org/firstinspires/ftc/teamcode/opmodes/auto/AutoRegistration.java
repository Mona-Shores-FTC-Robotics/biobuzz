package org.firstinspires.ftc.teamcode.opmodes.auto;


import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.autokit.AutoRegistry;
import org.firstinspires.ftc.teamcode.controls.MatchSetup;
import org.firstinspires.ftc.teamcode.opmodes.FlowerPicker;
import org.firstinspires.ftc.teamcode.opmodes.GardenGather;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.FieldFrame;
import org.firstinspires.ftc.teamcode.vision.CellSighting;
import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.HiveCellState;
import org.firstinspires.ftc.teamcode.vision.HiveState;
import org.firstinspires.ftc.teamcode.vision.HiveTracker;

import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

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
 * when its mechanism lands. None is left: {@code IntakeFull} is the colour sensor's full
 * ({@code ColorSensorSubsystem.full()}, a piece held under it), the same signal that stops the
 * roller and turns the light green.
 *
 * <p>{@code PickFlower} is {@link FlowerPicker}, the routine the driver's D-pad up runs, and
 * {@code GardenGather} is {@link GardenGather}, the driver's GARDEN gather button. Give their cards a timeout
 * longer than the grab (the Visualizer's builder sets 8 s): a card's default 5 s can cut one short.
 * Both are registered as grabs ({@code AutoRegistry.grab}): they drive the robot off its spot and
 * back (a FLOWER pick squares up first, onto the FLOWER's line, and comes back to that squared-up
 * spot, not the drawn one: {@link FlowerPicker}), so the endgame guard never cuts one and does not start one unless its typical time
 * ({@link #PICK_FLOWER_TYPICAL_S}, {@link #GARDEN_GATHER_TYPICAL_S}) and the park both fit, or it,
 * the cards after it and the next {@code LaunchAll}'s last piece ({@link #LAUNCH_ALL_LAST_PIECE_S})
 * all fit before AUTO ends. {@code PickFlower} also
 * has an approach ({@code AutoRegistry.Approach}, {@link FlowerPicker#approach}): the path card
 * right before it lowers the stick over its last 18 in, so the pick's stick wait is over when the
 * card starts. {@code GardenGather} has none yet: its stick came down on the wall when swung early
 * in the corner (30 Oct 2026), so it still lowers it from its own card.
 *
 * <p>{@code SpinUp}, {@code LaunchAll}, {@code LaunchOne} and {@code Empty} are real since the
 * launcher and feeder landed (#194): see {@link LaunchCards}, including what {@code Empty} means
 * without a piece sensor. {@code LaunchAll} has an approach too ({@link LaunchCards#approach()}): the
 * path card right before it opens the gate in the last few inches once the robot is aimed and the
 * launcher at speed, so the card only finishes the feed.
 *
 * <h2>LeftCellUp and RightCellUp</h2>
 *
 * <p>True while our HIVE's {@link HiveTracker} says that CELL is up, LEFT and RIGHT as our drive
 * team sees them from our alliance area ({@link HiveState}). They are conditions, not events: the
 * Auto Builder lists only {@code Tip} as an event, so a wait on {@code RightCellUp} ends at once if
 * the RIGHT CELL is already up when it starts. In an Autonomous the HIVE starts where the manual
 * puts it: at PLAY {@link BuiltAuto} has the tracker assume RIGHT_CELL_UP
 * ({@link HiveTracker#assumeMatchStart()}), so {@code RightCellUp} is true from PLAY and
 * {@code LeftCellUp} false, until the camera sees the HIVE settle (which replaces the assumption)
 * or a TIP moves it. Otherwise:
 * <ul>
 *   <li>No alliance chosen: false. {@link BuiltAuto} does not move without one, so this only
 *       matters to a caller outside it.</li>
 *   <li>{@link HiveState#UNSEEN} or {@link HiveState#TRANSITION}: false for both, so the wait runs
 *       to its limit or another row ends it. Outside an Autonomous nothing is assumed: a HIVE the
 *       camera has not seen is not "RIGHT CELL up".</li>
 *   <li>A position the tracker assumes, at an Autonomous's PLAY or after a TIP it saw start but
 *       not finish ({@link HiveTracker#assumed()}): taken as it stands, and held only until the
 *       camera next sees the HIVE settle.</li>
 * </ul>
 *
 * <h2>CellUp</h2>
 *
 * <p>The one wait the Simple builder offers, as "Wait for TIP" (mentor, 3 Nov 2026): the CELL this
 * robot is aimed at is up. True at once if it already is, so an Auto that arrives at a launch spot
 * with its CELL up fires without waiting; otherwise the wait holds until the camera sees that CELL
 * settle up, or the step's time limit passes. Which CELL is a pose question: with the turret at
 * home the robot fires straight ahead, so it is our HIVE's CELL at the robot's end of the field
 * ({@link #aimedLeft}). What "up" means is {@code LeftCellUp}/{@code RightCellUp} below, from the
 * tracker, which the camera updates whenever it sees the HIVE settle; at a launch spot it is
 * looking at it. {@code Tip}, {@code LeftCellUp} and {@code RightCellUp} stay registered for the
 * benched Autos that use them.
 *
 * <h2>Tip</h2>
 *
 * <p>True once one of our CELLs settles, in the camera, opposite to where it was last known to rest
 * when the wait began. "Last known" is the vision subsystem's {@code lastKnown}: the camera's last
 * settled state, kept while it does not see the CELL. At an Autonomous's PLAY, with the alliance
 * locked, {@link BuiltAuto} seeds all four CELLs to their match-start state (each alliance's LEFT
 * DOWN, RIGHT UP: the manual fixes it), and the camera's settled sightings replace the seed. So a
 * TIP counts even when the camera had not settled the CELL before PLAY (31 Oct 2026: L-Simple on
 * blue read BLUE AUDIENCE as UNKNOWN at PLAY, and its first wait always ran to 7 s). A later wait
 * starts from wherever the TIPs before it left the CELLs. TeleOp seeds nothing: there, a CELL the
 * camera has never settled never says Tip.
 *
 * <p>The assumption is for timing only. Position fixes ({@code TagGate}, {@code CellFix}) still use
 * nothing but what the camera settled on, so a HIVE set up wrong costs a mistimed wait, never a
 * wrong pose.
 */
public final class AutoRegistration {

    /**
     * {@code LaunchAll}'s typical seconds, from the card's start to its end: the settle
     * ({@code PathEndSettle}), the wait for launch speed, and {@code LaunchCards.launchAllFeedS}'
     * set 3.5 s feed. Measured on 20245 from every LaunchAll with the 3.5 s feed in the 2 to 4 Nov
     * 2026 match logs (read_logs, all {@code logs/*} branches): after a drive, 3.60 to 4.64 s, median
     * 4.04 (8; the slowest, 4.45 and 4.64 s, on 4 Nov 00:48, where the launcher overshot to about
     * 1680 ticks/s of 1595 &plusmn;60 after the drive and was not at speed for about 0.7 s); the first
     * at PLAY, which waits for the spin-up from rest, 4.45 to 4.83 s, median 4.50 (7). 4.5 covers
     * all but the two slowest after a drive and half of those at PLAY. What the guard needs to know
     * about a volley's pieces is {@link #LAUNCH_ALL_LAST_PIECE_S}; this is for the drive after it.
     * The Visualizer's Typical preview times a LaunchAll from {@code TeamCode/autos/timing.json}
     * instead ({@code spinUpS}, {@code volleyS}); this is what its Instant preview shows.
     */
    public static final double LAUNCH_ALL_TYPICAL_S = 4.5;

    /**
     * Seconds from a {@code LaunchAll} card's start to its last piece leaving the robot: what the
     * endgame guard counts when it asks whether a volley can be fired before AUTO ends
     * ({@code AutoRegistry.volley}, {@code AutoKit.guarded}). The gate opened 0.05 to 1.12 s after
     * the card started following a drive (0.98 to 1.12 s on 4 Nov 00:48: the settle 0.38 to 0.57 s,
     * then the launcher's overshoot) and 0.96 to 1.33 s at PLAY; the last piece left 1.7 to 2.9 s
     * after the gate opened (the launcher's shot count, 3 and 4 Nov 2026: 2.2 to 2.85 s on 4 Nov
     * 00:48). Card start to last piece after a drive: 2.1, 2.75, 3.31 and 3.97 s; at PLAY 3.16 to
     * 3.39 s, and 4.82 s once (3 Nov 18:06, the gate 1.33 s in, the fourth piece 3.49 s after it).
     * 4.0 covers every volley after a drive but one doubtful 4.2 s (2 Nov R-Quals: a single shot
     * counted, 3.09 s into the feed). The feed itself runs 3.5 s whatever: this is when the pieces
     * are out, not when the card ends. Re-measure from the next Auto logs.
     */
    public static final double LAUNCH_ALL_LAST_PIECE_S = 4.0;

    /**
     * {@code PickFlower}'s typical seconds, from the card's start to its end. Measured on 20245
     * from the six Auto picks since the drive in lowers the stick ({@code FlowerPicker.approach},
     * 3 and 4 Nov 2026): 1.82 to 3.65 s, median 3.0 (the pick's own line: line-up 0 to 0.61 s,
     * hand-over 0.02 to 0.19, push 0.67 to 1.10, dwell 0.06 to 2.01, back-out 0.58 to 0.71). 3.7
     * covers the slowest, 3.65 s (3 Nov 17:30, a 0.31 s line-up and the whole 2 s dwell, the lane
     * never full); 4 Nov 00:48's took 3.41 s (line-up 0.61, dwell 1.02). Not seen yet, but possible:
     * the longest line-up and the whole dwell together, about 4.1 s. The picks logged before the
     * approach (29 Oct to 2 Nov) took 2.1 to 4.3 s, up to 0.7 s of each the stick wait gone now.
     * Was 4.4, planned for that slow pick (mentor, 3 Nov 2026). Re-measure from the next Auto picks.
     */
    public static final double PICK_FLOWER_TYPICAL_S = 3.7;

    /**
     * {@code GardenGather}'s typical seconds: a drive in, a dwell and a drive back from wherever the
     * card is called. One Auto gather so far, from {@code GARDEN_STAGING}: 4.21 s (3 Nov 2026 18:06,
     * R-Partner-Tip: prep 0.74, in 1.17, dwell 1.51, back 0.77); five button gathers on 29 Oct and
     * 1 Nov took 3.0 to 5.0 s from beside the GARDEN. 5.0 covers both; was 6.0 before an Auto
     * gather was logged.
     */
    public static final double GARDEN_GATHER_TYPICAL_S = 5.0;

    /**
     * Seconds from the partner's last shot to our CELL seen up, added to the last shot their team
     * gives us to make the partner wait ({@link #partnerWaitUntilS}): the shot's flight, the dwell
     * before the rocker moves, the swing, and the camera settling on the raised CELL. The
     * simulator's model of each gives 1.5 to 5 s ({@code FieldSim.FILMED_TIP_SECONDS} 0.58 to 1.12 s,
     * {@code FILMED_TIP_DWELL_SECONDS} 0.25 to 3.4 s, a flight of about 0.6 s, the tracker's settle);
     * 4 s covers all but the longest dwells. An estimate until a match log times one. Written in
     * {@code TeamCode/autos/timing.json} too ({@code partnerTipLagS}), which
     * {@code PartnerWaitTest} keeps equal to this.
     */
    public static final double PARTNER_TIP_LAG_S = 4.0;

    /**
     * The match second the Auto's partner wait (its one match-clock {@code AutoOption}, set on the
     * D-pad in INIT: {@link InitOptions}) ends at, for the partner's last shot as their team said
     * it: that plus {@link #PARTNER_TIP_LAG_S}. NaN gives NaN. The simulator sets its partner wait
     * from this too, as the driver would.
     */
    public static double partnerWaitUntilS(double partnerLastShotS) {
        return Double.isNaN(partnerLastShotS) ? Double.NaN : partnerLastShotS + PARTNER_TIP_LAG_S;
    }

    /** How long without a HIVE tag before the camera counts as blind. */
    public static final double BLIND_AFTER_MS = 500;

    /** The names registered as placeholders, in registration order. Shown at INIT. */
    public static final List<String> PLACEHOLDERS = Collections.emptyList();

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
        final LaunchCards launch = robot == null ? null
                : LaunchCards.forRobot(robot, settle, note, setup == null ? null : setup::alliance);
        return new AutoRegistry()
                // A step's time limit never cuts a feed: it guards the wait for spin-up only, and the
                // feed runs LaunchCards' set time (one number to tune, not one per step).
                .holdTimeLimitsWhile(() -> launch != null && launch.feeding())
                // ---- real: the launcher and the feeder. Typical seconds are what the editor's
                // preview shows; the feed times themselves are LaunchCards' Panels numbers.
                // A volley: the endgame guard goes for the next one when its last piece can be out
                // before AUTO ends, rather than park (AutoKit.guarded). Its approach opens the gate
                // on the drive in, once aimed and at speed (LaunchCards.approach).
                .volley("LaunchAll", LAUNCH_ALL_TYPICAL_S, LAUNCH_ALL_LAST_PIECE_S, () -> launch.launchAll(),
                        launch == null ? null : launch.approach())
                .command("LaunchOne", 0.5, () -> launch.launchOne())
                .command("SpinUp", 0.1, () -> launch.spinUp())
                // ---- real: the FLOWER pick, the same routine as the driver's D-pad up. It starts at
                // the pick spot an Auto drives to and ends back there (FlowerPicker's contract). The
                // drive before it lowers the stick on the way in (its approach), so the pick's stick
                // wait is over when the card starts, and its dwell ends when the lane is full.
                .grab("PickFlower", PICK_FLOWER_TYPICAL_S, () -> FlowerPicker.command(robot, note),
                        robot == null ? null : FlowerPicker.approach(robot, note))
                // ---- real: the GARDEN gather, the same routine as the driver's GARDEN gather button. It drives in
                // to the GARDEN from wherever the card is called and back there (GrabRoutine's contract).
                .grab("GardenGather", GARDEN_GATHER_TYPICAL_S, () -> GardenGather.command(robot, note))
                .triggerSince("Empty", () -> launch == null ? () -> false : launch.emptySince())
                // ---- real: a piece held under the colour sensor (LaneFull), as the full hold and the light.
                .trigger("IntakeFull", robot == null ? () -> false : robot.colorSensor::full)
                // ---- real: the camera, through our HIVE's tracker. See the class comment.
                .trigger("LeftCellUp", () -> cellUp(ourHive(robot, setup), true))
                .trigger("RightCellUp", () -> cellUp(ourHive(robot, setup), false))
                // ---- real: the CELL this robot is aimed at is up, the Simple builder's "Wait for TIP":
                // true at once if it already is, else when the camera sees it settle up. See the class
                // comment.
                .trigger("CellUp", () -> aimedCellUp(robot, setup))
                // Our HIVE has tipped since this wait began: one of our CELLs is now settled opposite
                // to where it was last known to rest (seeded at an Auto's PLAY). See the class
                // comment. Mid-tip detection (tags vanishing while the robot holds still) is #173's.
                // No alliance chosen never says Tip.
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

    /**
     * {@code CellUp}: the CELL this robot is aimed at is up. With the turret at home the robot fires
     * straight ahead, so from its end of the field it can only be aimed at our HIVE's CELL at that
     * end: {@link #aimedLeft}. False with no robot, no alliance, or a CELL the tracker has not
     * settled.
     */
    static boolean aimedCellUp(Robot robot, MatchSetup setup) {
        if (robot == null || setup == null || setup.alliance() == null) return false;
        return cellUp(ourHive(robot, setup), aimedLeft(setup.alliance(), robot.drive.pose().y()));
    }

    /**
     * Whether the CELL at the robot's end of the field ({@code y}, Pedro frame) is our LEFT one as the
     * drive team sees it: red's left is the scoring end (past the field's centre), blue's left is
     * the audience end, since blue's side is red's turned half a turn.
     */
    static boolean aimedLeft(Alliance alliance, double y) {
        boolean scoringEnd = y >= FieldFrame.FIELD_CENTRE_INCHES;
        return alliance == Alliance.BLUE ? !scoringEnd : scoringEnd;
    }

    /** Our alliance's HIVE; null with no robot, no setup or no alliance chosen. */
    private static HiveTracker ourHive(Robot robot, MatchSetup setup) {
        return robot == null || setup == null ? null : robot.hive.of(setup.alliance());
    }

    /** The check for one wait, on the robot's camera: see {@link #tipSince(Alliance, Function, Function)}. */
    static BooleanSupplier tipSince(Robot robot, MatchSetup setup) {
        if (robot == null || setup == null) return () -> false;
        return tipSince(setup.alliance(), robot.vision::lastKnown, robot.vision::state);
    }

    /**
     * The check for one wait: true once one of our CELLs settles opposite to where it was last
     * known to rest when the wait began ({@code lastKnown}: the camera's last settled state, kept
     * through the gaps when it does not see the CELL, or the match-start state an Autonomous seeds
     * at PLAY). {@code now} is the camera's settled state this loop; UNKNOWN never fires. A CELL
     * with no last known state when the wait begins is never counted. A later wait starts from
     * wherever the TIPs before it left the CELLs. The other alliance's CELLs never count.
     */
    static BooleanSupplier tipSince(Alliance alliance,
                                    Function<HiveCell, HiveCellState> lastKnown,
                                    Function<HiveCell, HiveCellState> now) {
        final HiveCellState[] before = new HiveCellState[CELLS.length];
        for (int i = 0; i < CELLS.length; i++) {
            before[i] = CELLS[i].belongsTo(alliance) ? lastKnown.apply(CELLS[i]) : HiveCellState.UNKNOWN;
        }
        return () -> {
            for (int i = 0; i < CELLS.length; i++) {
                if (before[i] == HiveCellState.UNKNOWN) continue;
                HiveCellState state = now.apply(CELLS[i]);
                if (state != HiveCellState.UNKNOWN && state != before[i]) return true;
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
