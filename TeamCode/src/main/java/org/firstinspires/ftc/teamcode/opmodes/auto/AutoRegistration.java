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
 * card starts. {@code GardenGather} has one too ({@link GardenGather#approach}): the path card right
 * before it lowers the stick over its last 24 in, but only once the robot faces the GARDEN (a stick
 * swung down while the robot still turned in the corner came down on the wall, 30 Oct 2026).
 *
 * <p>{@code SpinUp}, {@code LaunchAll}, {@code LaunchOne} and {@code Empty} are real since the
 * launcher and feeder landed (#194): see {@link LaunchCards}, including what {@code Empty} means
 * without a piece sensor. {@code LaunchAll} has an approach too ({@link LaunchCards#approach()}): the
 * path card right before it opens the gate in the last few inches once the robot is aimed and the
 * launcher at speed, so the card only finishes the feed.
 *
 * <h2>LeftCellUp and RightCellUp</h2>
 *
 * <p>True while our HIVE's {@link HiveTracker} has <em>seen</em> that CELL up, LEFT and RIGHT as
 * our drive team sees them from our alliance area ({@link HiveState}). They are conditions, not
 * events: the Auto Builder lists only {@code Tip} as an event, so a wait on {@code RightCellUp}
 * ends at once if the RIGHT CELL is already seen up when it starts. In an Autonomous the tracker
 * assumes the manual's RIGHT_CELL_UP at PLAY ({@link HiveTracker#assumeMatchStart()}), but an
 * assumption is not a sighting: both are false until the camera sees the HIVE settle. Otherwise:
 * <ul>
 *   <li>No alliance chosen: false. {@link BuiltAuto} does not move without one, so this only
 *       matters to a caller outside it.</li>
 *   <li>{@link HiveState#UNSEEN} or {@link HiveState#TRANSITION}: false for both, so the wait runs
 *       to its limit or another row ends it. Outside an Autonomous nothing is assumed: a HIVE the
 *       camera has not seen is not "RIGHT CELL up".</li>
 *   <li>The position the tracker assumes at an Autonomous's PLAY
 *       ({@link HiveTracker#assumed()}): false for both, until the camera sees the HIVE
 *       settle ({@link #cellUp}: R-Solo 5 Nov 00:32 fired at a down CELL on an assumed TIP).</li>
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
     * instead ({@code spinUpS}, {@code volleyS}); this is what its Instant preview shows. Since
     * 5 Nov a full volley ends at its 4th counted shot ({@code LaunchCards.launchAllEndAfterShots}),
     * about 1 s sooner. 4.0 since 6 Nov 2026, measured that night on both robots (every LaunchAll
     * card after a drive, 19429 and 20245): 3.7 to 4.0 s from the card's start to its end, the feed
     * running its full 3.5 s whenever fewer than 4 shots were counted; at PLAY 2.5 to 5.0 s. The
     * guard now also needs the park to fit after a volley ({@code AutoKit.parkAfterVolley}), so
     * this decides whether a late volley is fired or parked: at 4.5 R-Solo would have parked
     * instead of a third volley that fitted (its park was in by 28.2 s on 6 Nov 19:10).
     * Belding (7 Nov 2026, both robots, 38 LaunchAlls): 2.20 to 4.90 s; after a drive all but two
     * (4.67, 4.90 s) under 4.5, at PLAY 2.59 to 4.18 s. Most volleys counted fewer than 4 shots, so
     * the feed ran its whole 3.5 s. A card that runs past the planned 4.0 eats into the park's
     * margin after it; recheck against the next event's logs.
     * 2.5 since 12 Nov 2026 (the mentor's call, with the shot counter ending a full volley at its
     * 4th shot: 19429's L-NoCross volleys at Belding ran 2.20 and 2.35 s that way). Not yet measured
     * as the usual case: a volley whose counter misses a ball still feeds its whole 3.5 s and runs
     * about 4 s, which now eats the park's margin. Read {@code read_logs.py steps} on the next
     * Auto logs and raise this again if most volleys run long.
     */
    public static final double LAUNCH_ALL_TYPICAL_S = 2.5;

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
     * counted, 3.09 s into the feed). The feed ends at the 4th counted shot or after 3.5 s
     * ({@code LaunchCards.launchAllEndAfterShots}): this is when the pieces are out, not when the
     * card ends. Re-measure from the next Auto logs. Belding (7 Nov 2026, 32 LaunchAlls that counted a
     * shot): the last counted shot 0.94 to 4.59 s after the card started, all but one by 3.88 s. The
     * one, 4.59 s (19429 L-NoCross, 17:52), opened the gate about 1.2 s in and fed the last two
     * pieces 1.5 s apart: the gate's delay plus the whole 3.5 s feed, about 4.7 s, is the worst case.
     * 4.0 stayed after Belding (the mentor's call: more would make the guard give up on more volleys).
     * 2.5 since 12 Nov 2026, with {@link #LAUNCH_ALL_TYPICAL_S} (the mentor's call): at Belding the
     * last counted shot was later than 2.5 s into the card in many volleys, so a late volley the
     * guard now lets go may have a piece still in the robot at 30 s. Recheck with the next logs.
     */
    public static final double LAUNCH_ALL_LAST_PIECE_S = 2.5;

    /**
     * {@code PickFlower}'s typical seconds, from the card's start to its end. Measured on 20245
     * from the six Auto picks since the drive in lowers the stick ({@code FlowerPicker.approach},
     * 3 and 4 Nov 2026): 1.82 to 3.65 s, median 3.0 (the pick's own line: line-up 0 to 0.61 s,
     * hand-over 0.02 to 0.19, push 0.67 to 1.10, dwell 0.06 to 2.01, back-out 0.58 to 0.71). 3.7
     * covers the slowest, 3.65 s (3 Nov 17:30, a 0.31 s line-up and the whole 2 s dwell, the lane
     * never full); 4 Nov 00:48's took 3.41 s (line-up 0.61, dwell 1.02). Not seen yet, but possible:
     * the longest line-up and the whole dwell together, about 4.1 s. The picks logged before the
     * approach (29 Oct to 2 Nov) took 2.1 to 4.3 s, up to 0.7 s of each the stick wait gone now.
     * Was 4.4, planned for that slow pick (mentor, 3 Nov 2026). The Belding Qualifier (7 Nov 2026, both
     * robots, R-Solo and L-NoCross): 23 Auto picks, 2.73 to 3.99 s, median 3.68, 8 of them over 3.7
     * (the whole 2 s dwell every time but two: the lane was never full). 4.0 covers all 23.
     */
    public static final double PICK_FLOWER_TYPICAL_S = 4.0;

    /**
     * {@code GardenGather}'s typical seconds: a drive in, a dwell and a drive back from wherever the
     * card is called. Nine Auto gathers are logged, from {@code GARDEN_STAGING} on both robots (3 to
     * 7 Nov 2026): 4.13 to 4.76 s, median 4.43 (stick wait 0.70 to 0.75, drive in 1.12 to 1.66, dwell
     * 1.5, back 0.74 to 0.89). The dwell has been 0.4 s and the back out faster since 6 Nov: the ten
     * gathers of 12 Nov dwelt 0.40 to 0.43 and backed out in 0.61 to 0.67, and the one that started
     * at the staging spot took 3.26 s. The slowest of each phase add up to 3.73 s (0.75 + 1.66 +
     * 0.43 + 0.89), so 4.0 covers a gather as the code runs it now, before the stick comes down on
     * the drive in ({@link GardenGather#approach}), which takes up to 0.7 s more off. No Auto has
     * been logged with these settings yet (doc/robot-todo.md). Was 5.0 (one Auto gather, 4.21 s with
     * the long dwell), and 6.0 before any was logged.
     */
    public static final double GARDEN_GATHER_TYPICAL_S = 4.0;

    /**
     * The TIP lag a partner wait allows: seconds from the partner's last ball leaving its launcher to
     * our camera reading the CELL up, added to the partner's last shot
     * ({@link #partnerWaitUntilS}). It is a give-up limit, not a prediction: the wait ends the moment
     * {@code CellUp} fires, so a longer limit costs time only when the TIP is not coming.
     * Measured from the Belding Qualifier's films (7 Nov 2026; {@code TeamCode/autos/timing-terms.md}
     * has every TIP): for a CELL at its tipping weight, as a partner's four preloads leave it, the
     * other CELL settled 3.4 to 6.5 s after the last ball left (13 TIPs, median 3.9) and a watching
     * camera read it 0.2 s later. 5.5 s catches 11 of the 13; the 4.5 s used until 12 Nov 2026 caught
     * 9. The two it misses (5.6 and 6.7 s) are left to the Auto's no-TIP route, which still scores.
     * Written in {@code TeamCode/autos/timing.json} too ({@code partnerTipLagS}), which
     * {@code PartnerWaitTest} keeps equal to this.
     */
    public static final double PARTNER_TIP_LAG_S = 5.5;

    /**
     * The match second a partner wait should give up at, for a partner whose last ball leaves at
     * {@code partnerLastShotS}: that plus {@link #PARTNER_TIP_LAG_S}. NaN gives NaN. It is how a
     * wait's drawn limit is chosen (a partner's preloads are away by about 3.5 s, so 9 s), and what
     * the simulator, which is told a stand-in's last shot, sets the wait to. On the robot the drive
     * team moves the limit itself, in INIT ({@link InitOptions}).
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
                // to the GARDEN from wherever the card is called and back there (GrabRoutine's contract). The
                // drive before it lowers the stick on the way in once it faces the GARDEN (its approach).
                .grab("GardenGather", GARDEN_GATHER_TYPICAL_S, () -> GardenGather.command(robot, note),
                        robot == null ? null : GardenGather.approach(robot, note))
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
                .trigger("CameraBlind", () -> cameraBlind(robot))
                // Always true: a wait on it asks only its time. The time row is checked first, so
                // "Always, at most until 19 s on the match clock" takes its time row when the clock
                // is already past 19 s and its trigger row at once otherwise: "is there still time?".
                .trigger("Always", () -> true);
    }

    /**
     * {@code LeftCellUp} ({@code left}) or {@code RightCellUp}: what our HIVE's tracker says it has
     * <em>seen</em>, and false with no tracker (no alliance chosen). UNSEEN and TRANSITION are false
     * for both, and so is the match-start position the tracker only assumed
     * ({@link HiveTracker#assumed()}). Why (R-Solo, 5 Nov 2026 00:32, 20245): the tracker of the
     * time started a TIP that never happened as the robot turned away from the start, assumed it
     * finished 3.5 s later, and {@code CellUp} at LEFT_LAUNCH_CLOSE passed in 0.02 s on that guess,
     * before the camera had a frame from there, and the robot fired at a CELL that was down. The
     * tracker no longer assumes a TIP, and this wait lasts until the camera sees the CELL up, or
     * its time runs out and the Auto takes the other way.
     */
    static boolean cellUp(HiveTracker hive, boolean left) {
        if (hive == null || hive.assumed()) return false;
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
