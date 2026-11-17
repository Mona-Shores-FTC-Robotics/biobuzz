package org.firstinspires.ftc.teamcode.opmodes;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.localization.FlowerFieldPoints;
import org.firstinspires.ftc.teamcode.localization.FlowerFieldPoints.Flower;
import org.firstinspires.ftc.teamcode.localization.HiveFieldPoints;
import org.firstinspires.ftc.teamcode.subsystems.LaunchSpeedTable;
import org.firstinspires.ftc.teamcode.subsystems.LauncherSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.TurretAim;
import org.firstinspires.ftc.teamcode.subsystems.TurretSubsystem;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.HiveCellState;
import org.firstinspires.ftc.teamcode.vision.Vec3;

import java.util.Locale;
import java.util.function.Consumer;

/**
 * The FLOWER 8-shot: a robot that arrives at a FLOWER with a load fires it from the FLOWER while
 * the stick and the intake bring the FLOWER's pieces in behind it, so eight go up in one feed with
 * no drive between the pick and the volley (mentor's idea, 17 Nov 2026). It is {@link FlowerPicker}'s
 * pick, staying against the FLOWER until the feed is over ({@code FlowerPicker.staying}); this
 * class spins the launcher up, opens the gate, counts the shots and says when to leave. The turret
 * is the caller's: it must be aiming at our CELL every loop ({@link TurretAimer}), since the robot
 * faces the wall and the CELL is behind it.
 *
 * <p><b>Only at the SCORING FLOWER</b> (drawn for red; blue's is its AUDIENCE FLOWER). Pressed
 * against it the turret is about 43 in from the near CELL's raised row and about 11° off square
 * ({@code FlowerVolleyTest}): the speed table's lowest measured row (a push that stalls short, 10
 * to 12.5 in from the FLOWER's centre, is 40 to 42 in). Any spot further back from the FLOWER is
 * nearer the HIVE than that, under every measured row (29 in at the pick's line-up), which is why
 * the load is fired from the FLOWER and not on the way in. From the WALL FLOWER the near CELL is
 * about 80° off square, which no shot makes; {@link #start} refuses it.
 *
 * <p><b>G407 (four pieces at most) is the open question.</b> A FLOWER piece
 * that comes in while all four of the load are still aboard is a fifth. So the gate opens as the
 * push starts ({@link #feedStart}), about as long before contact as the first ball takes to leave,
 * and the match log says for each run when each shot left against when the robot touched the
 * FLOWER, and how many times the lane's count went past four ({@code LaneTracker.overflows()}).
 * Film it: the film, not the count, decides whether a fifth piece was ever in the robot.
 *
 * <p>It runs from Basic Drive's super FLOWER pick, the button's way ({@code Drive: Super FLOWER
 * Pick}: {@link #start(Alliance, double, boolean, boolean)}, then {@link #update(boolean, boolean)}
 * every loop while {@link #active()}), which by default fires only the load and keeps the FLOWER's
 * pieces. The caller aims the turret. Every number here is a starting point, not a measurement.
 */
@Configurable
public final class FlowerVolley {

    /** When the gate may open. */
    public enum FeedStart {
        /** As the push into the FLOWER starts: the first ball leaves about as the stick arrives. */
        PUSH,
        /** Once the robot is against the FLOWER. The FLOWER's first pieces come in on a full robot. */
        CONTACT
    }

    /** When the gate may open; see {@link FeedStart}. A cleared Panels field is {@code PUSH}. */
    public static FeedStart feedStart = FeedStart.PUSH;
    /** The feed ends once the launcher has counted this many shots since the gate opened. 0 or NaN: off. */
    public static double endAfterShots = 8;
    /**
     * The feed ends this long after the gate opened, seconds, whatever was counted: the backstop
     * for a count that misses balls. Eight balls 0.47 s apart and 0.8 s to the first is 4.1 s.
     */
    public static double feedCapS = 6.0;
    /**
     * At the FLOWER, the feed ends when no shot has been counted for this long, ms: the FLOWER gave
     * fewer than four, and waiting out {@link #feedCapS} would be time lost. Not before the robot
     * has been against the FLOWER this long either. 0: off.
     */
    public static double quietEndMs = 1500;
    /**
     * The gate opens once the launcher is at speed and the turret is within {@link #aimToleranceDeg};
     * if that has not happened this long after it first could, ms, it opens anyway and the log says
     * so (a slightly off shot beats sitting on a full robot at the FLOWER).
     */
    public static double gateWaitMs = 1500;
    /**
     * Fire the load, keep the FLOWER's ({@link #start}'s {@code keepFlowerLoad}): the feed ends once
     * the launcher has counted this many shots, the load the robot arrived with. 0 or NaN: off.
     */
    public static double loadShots = 4;
    /**
     * Fire the load, keep the FLOWER's: the feed ends this long after the gate opened whatever was
     * counted, seconds. {@code LaunchCards.launchAllFeedS}' 3.5, for the same four balls.
     */
    public static double loadFeedCapS = 3.5;
    /**
     * Fire the load, keep the FLOWER's: once the gate has shut the robot stays against the FLOWER
     * until the lane is full or this long has passed, ms, so the FLOWER's pieces finish coming in.
     */
    public static double pickOnMs = 1500;
    /** The turret is aimed when its error is within this, degrees. */
    public static double aimToleranceDeg = 3.0;
    /** The near CELL may be this far off square to the HIVE, degrees, for the volley to be tried at all. */
    public static double maxOffSquareDeg = 26.0;

    /** Shot times kept for the log: more than a trial can fire. */
    private static final int MAX_SHOTS_LOGGED = 12;

    private final Robot robot;
    private final Consumer<String> event;
    private FlowerPicker picker;
    private boolean ended = true;
    private String result = "";

    private FeedStart running = FeedStart.PUSH;
    private long startNs;
    private long armedNs;
    private long fedNs;
    private long contactNs;
    private long lastShotNs;
    private long gateClosedNs;
    /** The button's way: lined up first, and following the button ({@link #update(boolean, boolean)}). */
    private boolean button;
    /** Fire the load only and keep the FLOWER's pieces, rather than fire all eight. */
    private boolean keepFlowerLoad;
    private boolean gateClosed;
    private int shotsAtFeed;
    private int shotsSeen;
    private int overflowsAtStart;
    private double rangeIn = Double.NaN;
    private double speedTps = Double.NaN;
    private double aimErrorAtGateDeg = Double.NaN;
    private String overWhy = "";
    private final double[] shotAtS = new double[MAX_SHOTS_LOGGED];

    /**
     * @param event where the trial's lines go: the OpMode's {@code log.event}, looked up each time
     */
    public FlowerVolley(Robot robot, Consumer<String> event) {
        this.robot = robot;
        this.event = event;
    }

    /**
     * Begin: spin the launcher up for the shot from the FLOWER and start the pick from where the
     * robot is (in front of the FLOWER, facing it, as an Auto's pick spot). Returns why not, or
     * null when it has started.
     *
     * @param alliance ours: which CELL the volley is for
     * @param trimTps  added to the launch speed, ticks/s (the operator's launch trim)
     */
    public String start(Alliance alliance, double trimTps) {
        return start(alliance, trimTps, false, false);
    }

    /**
     * {@link #start(Alliance, double)} with the two choices a button has.
     *
     * @param button         line up first, from anywhere within reach of a FLOWER, and follow the
     *                       button: run it with {@link #update(boolean, boolean)}
     * @param keepFlowerLoad fire only the load the robot arrived with, shut the gate, and stay until
     *                       the FLOWER's pieces are in: the robot leaves holding them (a pick that
     *                       scores four on the way in). False fires all eight.
     */
    public String start(Alliance alliance, double trimTps, boolean button, boolean keepFlowerLoad) {
        if (active()) return "already running";
        Pose pose = robot.drive.pose();
        if (pose == null || !robot.drive.poseReferenced()) return "no pose on the field";
        Flower flower = FlowerPicker.nearest(pose.x(), pose.y(), !button);
        if (flower == null) return button ? "no FLOWER in range" : "no FLOWER on our side of the centre line in range";
        Pose contact = FlowerFieldPoints.standoff(flower, FlowerPicker.contactStandoffIn);
        double off = offSquareDeg(contact, alliance);
        rangeIn = rangeIn(contact, alliance);
        if (Double.isNaN(off) || Double.isNaN(rangeIn)) return "no alliance, or no field point for our CELL";
        if (off > maxOffSquareDeg) {
            return String.format(Locale.US, "from %s the CELL is %.0f° off square (at most %.0f): no shot from there",
                    flower, off, maxOffSquareDeg);
        }
        running = feedStart == null ? FeedStart.PUSH : feedStart;
        this.button = button;
        this.keepFlowerLoad = keepFlowerLoad;
        gateClosedNs = 0;
        startNs = System.nanoTime();
        armedNs = 0;
        fedNs = 0;
        contactNs = 0;
        lastShotNs = 0;
        gateClosed = false;
        shotsSeen = 0;
        overWhy = "";
        aimErrorAtGateDeg = Double.NaN;
        overflowsAtStart = robot.colorSensor.lane().overflows();
        speedTps = LaunchSpeedTable.launchSpeed(rangeIn, LauncherSubsystem.targetTicksPerSecond)
                + (Double.isNaN(trimTps) ? 0.0 : trimTps);
        robot.launcher.spinUp(speedTps);
        event.accept(String.format(Locale.US,
                "FLOWER volley: start, %s, %s, gate opens at %s, %.0f ticks/s for %.1f in, CELL %.0f° off square",
                flower, keepFlowerLoad ? "fire the load and keep the FLOWER's" : "fire all eight",
                running, speedTps, rangeIn, off));
        picker = button ? FlowerPicker.stayingButton(robot, event, this::over)
                : FlowerPicker.staying(robot, event, this::over);
        ended = false;
        picker.update(true, false);
        return null;
    }

    /** One step, every loop while {@link #active()}, held to the end. */
    public void update() {
        update(true, false);
    }

    /**
     * One step, every loop while {@link #active()}, following a button as a pick does: let go
     * before the robot is against the FLOWER and it stops; let go once it is and it backs out; a
     * drive stick moved stops it.
     */
    public void update(boolean held, boolean sticksMoved) {
        if (ended) return;
        long now = System.nanoTime();
        picker.update(held, sticksMoved);
        // Let go before the dwell: the pick has stopped and gone back to idle.
        if (picker.phase() == GrabRoutine.Phase.IDLE) {
            if (overWhy.isEmpty()) overWhy = "let go";
            end(now);
            return;
        }
        GrabRoutine.Phase phase = picker.phase();
        if (contactNs == 0 && phase == GrabRoutine.Phase.DWELL) {
            contactNs = now;
            event.accept(String.format(Locale.US, "FLOWER volley: at the FLOWER, %s",
                    fedNs == 0 ? "gate not open yet" : String.format(Locale.US,
                            "gate opened %.2f s before, %d shots counted so far", ms(now, fedNs) / 1000.0, shotsSeen)));
        }
        boolean atFlower = phase == GrabRoutine.Phase.IN || phase == GrabRoutine.Phase.DWELL
                || phase == GrabRoutine.Phase.BACK_OFF;
        if (armedNs == 0 && atFlower && (running == FeedStart.PUSH || contactNs != 0)) armedNs = now;
        if (fedNs == 0 && armedNs != 0 && atFlower) {
            double error = robot.turret.errorDegrees();
            boolean aimed = robot.turret.holding() && Math.abs(error) <= nn(aimToleranceDeg);
            boolean atSpeed = robot.launcher.atSpeed();
            if (gateMayOpen(atSpeed, aimed, ms(now, armedNs), nn(gateWaitMs))) {
                fedNs = now;
                lastShotNs = now;
                shotsAtFeed = robot.launcher.shots();
                aimErrorAtGateDeg = error;
                robot.feeder.feed();
                event.accept(String.format(Locale.US, "FLOWER volley: gate open in %s%s, aim error %+.1f°, launcher %s",
                        phase, atSpeed && aimed ? "" : " WITHOUT waiting longer", error,
                        atSpeed ? "at speed" : "below speed"));
            }
        }
        if (fedNs != 0 && !gateClosed) {
            int shots = robot.launcher.shots() - shotsAtFeed;
            if (shots > shotsSeen) {
                for (int i = shotsSeen; i < shots && i < MAX_SHOTS_LOGGED; i++) shotAtS[i] = ms(now, fedNs) / 1000.0;
                shotsSeen = shots;
                lastShotNs = now;
            }
        }
        if (picker.finished()) end(now);
    }

    /** Whether the gate is open now: Basic Drive keeps the feeder feeding while it is. */
    public boolean feeding() {
        return !ended && fedNs != 0 && !gateClosed;
    }

    /** Stop now: the pick stops where it is, and the gate closes. The launcher is left to the caller. */
    public void cancel() {
        if (ended) return;
        overWhy = "cancelled";
        if (picker.active()) picker.update(false, false);
        end(System.nanoTime());
    }

    /** Whether the volley is running, so the OpMode must leave the drive alone. */
    public boolean active() {
        return !ended;
    }

    /** For the Match page: what it is doing, or how the last one went. Empty before the first. */
    public String line() {
        if (ended) return result;
        return String.format(Locale.US, "FLOWER volley: %s · %s · %d shots", picker.phase(),
                fedNs == 0 ? "gate shut" : gateClosed ? "gate closed" : "feeding", shotsSeen);
    }

    /** The range the launch speed was chosen for, inches; NaN before the first start. */
    public double rangeIn() {
        return rangeIn;
    }

    /** The launch speed asked for, ticks/s; NaN before the first start. */
    public double speedTps() {
        return speedTps;
    }

    /** The pick's stay: asked every loop of its last dwell; a reason closes the gate and backs the robot out. */
    private String over() {
        long now = System.nanoTime();
        if (!gateClosed) {
            String why = over(fedNs != 0, shotsSeen, fedNs == 0 ? 0.0 : ms(now, fedNs) / 1000.0,
                    ms(now, lastShotNs), contactNs == 0 ? 0.0 : ms(now, contactNs),
                    keepFlowerLoad ? loadShots : endAfterShots,
                    nn(keepFlowerLoad ? loadFeedCapS : feedCapS), nn(quietEndMs));
            if (why == null) return null;
            overWhy = why;
            closeGate();
            gateClosedNs = now;
            if (!keepFlowerLoad) return why;
            event.accept("FLOWER volley: gate shut on " + why + ", picking on");
        }
        if (!keepFlowerLoad) return overWhy;
        // The load is out and the gate is shut: stay for the FLOWER's pieces, as a pick's dwell does.
        String then = pickOver(robot.colorSensor.full(), ms(now, gateClosedNs), nn(pickOnMs));
        if (then == null) return null;
        overWhy = overWhy + ", then " + then;
        return overWhy;
    }

    private void closeGate() {
        if (fedNs == 0 || gateClosed) return;
        gateClosed = true;
        robot.feeder.idle();
    }

    private void end(long now) {
        closeGate();
        ended = true;
        StringBuilder shots = new StringBuilder();
        for (int i = 0; i < shotsSeen && i < MAX_SHOTS_LOGGED; i++) {
            shots.append(i == 0 ? "" : " ").append(String.format(Locale.US, "%.2f", shotAtS[i]));
        }
        int overflows = robot.colorSensor.lane().overflows() - overflowsAtStart;
        result = String.format(Locale.US,
                "Last FLOWER volley: %d shots counted%s · %s · %s · whole thing %.2f s · %s · lane past four %d time%s"
                        + " · %.0f ticks/s for %.1f in, aim error at the gate %+.1f°",
                shotsSeen,
                shotsSeen == 0 ? "" : " at " + shots + " s after the gate",
                fedNs == 0 ? "gate never opened"
                        : contactNs == 0 ? "never reached the FLOWER"
                        : String.format(Locale.US, "gate opened %.2f s %s contact",
                        Math.abs(contactNs - fedNs) / 1e9, fedNs <= contactNs ? "before" : "after"),
                overWhy.isEmpty() ? "pick ended: " + picker.phase() : "ended on " + overWhy,
                ms(now, startNs) / 1000.0, picker.line(), overflows, overflows == 1 ? "" : "s",
                speedTps, rangeIn, aimErrorAtGateDeg);
        event.accept(result);
    }

    // ------------------------------------------------------------------ the rules, pure

    /**
     * Whether the gate opens this loop, given that it may ({@link FeedStart}): the launcher is at
     * speed and the turret aimed, or the wait for both has run out.
     */
    static boolean gateMayOpen(boolean atSpeed, boolean aimed, double waitedMs, double gateWaitMs) {
        return (atSpeed && aimed) || waitedMs >= gateWaitMs;
    }

    /**
     * Why the robot leaves the FLOWER this loop, or null while it stays: the count is reached, the
     * feed has run its cap, or (once it has been against the FLOWER {@code quietEndMs}) no shot has
     * been counted for {@code quietEndMs}. Before the gate opens it stays: {@link #gateMayOpen}
     * always opens it in the end.
     */
    static String over(boolean fed, int shots, double fedForS, double sinceShotMs, double atFlowerMs,
                       double endAfterShots, double feedCapS, double quietEndMs) {
        if (!fed) return null;
        if (endAfterShots >= 1 && shots >= endAfterShots) return shots + " shots counted";
        if (fedForS >= feedCapS) return "the feed's cap";
        if (quietEndMs > 0 && atFlowerMs >= quietEndMs && sinceShotMs >= quietEndMs) return "no more shots";
        return null;
    }

    /**
     * Fire the load, keep the FLOWER's: why the robot leaves once the gate has shut, or null while
     * it stays: the lane is full, or it has picked on for {@code pickOnMs}.
     */
    static String pickOver(boolean laneFull, double sinceGateShutMs, double pickOnMs) {
        if (laneFull) return "the lane full";
        if (sinceGateShutMs >= pickOnMs) return "the pick's time";
        return null;
    }

    /** Our CELL at {@code pose}'s end of the field, raised, or null. */
    private static Vec3 nearRow(Pose pose, Alliance alliance) {
        if (pose == null || alliance == null || alliance == Alliance.UNKNOWN) return null;
        HiveCell cell = TurretAim.nearEndCell(alliance, pose.y(), null, HiveFieldPoints.AXLE_Y_IN, 0.0);
        return cell == null ? null : HiveFieldPoints.rowCentre(cell, HiveCellState.UP);
    }

    /** The turret's range from {@code pose} to the near CELL's raised row, inches; NaN with none. */
    static double rangeIn(Pose pose, Alliance alliance) {
        Vec3 row = nearRow(pose, alliance);
        if (row == null) return Double.NaN;
        return TurretAim.rangeFromFieldIn(pose.x(), pose.y(), pose.heading(),
                TurretSubsystem.turretForwardIn, TurretSubsystem.turretLeftIn, row.x(), row.y());
    }

    /**
     * How far off square to the HIVE the shot from {@code pose} to the near CELL's raised row is,
     * degrees, 0 to 90: 0 is straight across the axle, the way a CELL opens. NaN with none.
     */
    static double offSquareDeg(Pose pose, Alliance alliance) {
        Vec3 row = nearRow(pose, alliance);
        if (row == null) return Double.NaN;
        double c = Math.cos(pose.heading());
        double s = Math.sin(pose.heading());
        double axisX = pose.x() + TurretSubsystem.turretForwardIn * c - TurretSubsystem.turretLeftIn * s;
        double axisY = pose.y() + TurretSubsystem.turretForwardIn * s + TurretSubsystem.turretLeftIn * c;
        return Math.toDegrees(Math.atan2(Math.abs(row.x() - axisX), Math.abs(row.y() - axisY)));
    }

    private static double ms(long now, long thenNs) {
        return (now - thenNs) / 1e6;
    }

    private static double nn(double v) {
        return Double.isNaN(v) || v < 0.0 ? 0.0 : v;
    }
}
