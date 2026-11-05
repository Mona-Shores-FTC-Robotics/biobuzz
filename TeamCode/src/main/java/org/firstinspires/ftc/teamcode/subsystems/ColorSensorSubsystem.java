package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;

import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.hardware.DeviceNames;
import org.firstinspires.ftc.teamcode.util.Alliance;

import java.util.Locale;

/**
 * The REV Color Sensor V3 in the transfer: what colour is in front of it, and how bright. Its
 * numbers go to the match log ({@code /ColorSensor/*}) and the Robot page. Two things are decided from
 * it: whether the lane is full (below), and whether the piece at the entrance is the other
 * alliance's NECTAR, to spit out (below); its colours are otherwise data only.
 *
 * <p>It is an I2C device, so every read is a round trip to the hub that the bulk cache does not
 * cover: the colour block only. Its proximity side (IR distance) is never read: "full" goes by
 * brightness, and a second transaction per read cost about as much again (2 Nov 2026, p50 5.2 ms of
 * every loop with both). A read of the colour block alone is 3 to 9 ms (20245's logs, 3–4 Nov
 * 2026), so it is never read every loop; {@code /Loop/Subsystem/ColorSensorSubsystemMs} shows what
 * a read costs. It moves nothing, so it reads in INIT too: the preloads are in front of it before
 * PLAY.
 *
 * <h2>When it reads</h2>
 *
 * <p>Every {@link #readEveryMs} while something can move a piece under it ({@link #pieceCanMove}):
 * the roller or the stars taking in or backing out, the prefeed running, the feeder feeding or
 * reversing, or a shot just counted. Every {@link #readIdleEveryMs} otherwise, which includes the
 * intake <b>holding a full lane</b> ({@code IntakeSubsystem.holdingForFull()}: the stars and a slow
 * roller press on a lane with no room, so the piece under the sensor stays). The robot spends a
 * large part of TeleOp full and driving to a launch spot (up to half of some 3 Nov sessions), and
 * reads there decided nothing. The slow read still catches a piece that rolls back out while
 * holding, within {@link #readIdleEveryMs}. The moment something can move a piece again (a feed, a
 * shot, an eject, the intake taking in) it reads in that same loop rather than waiting out the
 * slow interval, so "full" clears as soon as the piece has gone.
 *
 * <p>Between reads every answer here ({@link #full()}, the lane, the colours) is as of the last
 * reading, never a guess, and says so: {@link #readAgeMs()} is how old it is, and
 * {@link #watching()} says which rate is running (the Robot page shows both, the match log
 * {@code /ColorSensor/Watching}).
 *
 * <p>It says whether the lane is <b>full</b> ({@link #full()}, {@link LaneFull}): a piece held under
 * it for {@link #fullHoldS} while the intake takes in, and nothing else: the lane tracker's count
 * plays no part (until 5 Nov 2026 a count of 4 cut the hold to 0.3 s, and on a fresh battery the
 * shot count missed shots, so the count stuck at 4 and a single piece reaching the sensor ended a
 * FLOWER pick as full). The indicator's green, the intake's roller
 * stop and the Autos' {@code IntakeFull} all read that one signal.
 *
 * <p>It also keeps {@link LaneTracker}: what it thinks is in the lane, slot by slot, from pieces
 * arriving under it, shots, and ejects. Data only, logged as {@code /Lane/*}: a count drifts on any
 * missed event, so nothing decides anything from it (mentor, 2 Nov 2026). Its thresholds
 * ({@link #presentAbove}, {@link #leaveBelow}, the hue ranges) come from one bench reading of each
 * piece on robot 20245, 2 Nov 2026, with the sensor at slot 3. The sensor is now at slot 4, the
 * entrance, where full has been seen to work; the numbers still want confirming there
 * ({@code doc/robot-todo.md}), and logs will refine them.
 *
 * <p>The colour is the SDK's normalised reading at the sensor's default gain, unchanged, plus its
 * hue and saturation. Hue and saturation do not depend on the gain or on how far the ball is; the
 * raw numbers and the distance do. No threshold or classification here until a log says where one
 * belongs.
 */
@Configurable
public class ColorSensorSubsystem implements Subsystem {

    /**
     * How often to read the sensor, ms. 50 (20 a second) sees a ball that passes in a tenth of a
     * second at least once and costs a fraction of reading every loop. 0 reads every loop.
     */
    public static int readEveryMs = 50;

    /**
     * How often to read while nothing can move a piece under the sensor ({@link #pieceCanMove}):
     * everything stopped, or the intake only holding a full lane. Then a reading refreshes the
     * screens and catches a piece placed by hand (the preloads in INIT) or one rolling back out of
     * a full lane. 250 sees one within a quarter second. Never faster than {@link #readEveryMs}.
     * (From the closed #366, 2 Nov 2026.)
     */
    public static int readIdleEveryMs = 250;

    /**
     * A piece is under the sensor above this brightness (the clear channel, {@link #alpha()}). NaN
     * turns arrivals off. Brightness, not distance: the proximity reading is IR reflectance, and a
     * POLLEN reflects so little that it read 44 mm against an empty lane's 54 (NECTAR 18). Measured
     * on 20245, 2 Nov 2026, as mounted at the entrance: empty 0.014 to 0.020 (steady), POLLEN at
     * rest 0.155 to 0.45, NECTAR 0.29 to 0.97; a piece pushed right up to it reads up to 0.99. 0.10
     * keeps clear of the dimmest POLLEN; replaying that session, 0.15 split one hold into two.
     */
    public static double presentAbove = 0.10;
    /** And has gone below this brightness (hysteresis). NaN: the same as {@link #presentAbove}. */
    public static double leaveBelow = 0.05;
    /**
     * POLLEN's hue range, degrees (min above max wraps through 0). Bench, 2 Nov 2026: 73° to 94° at
     * rest, saturation about 0.7 (it falls to 0.35 when pressed against the sensor; the hue holds).
     * The low end sits midway to red NECTAR's highest reading.
     */
    public static double pollenHueMin = 48;
    public static double pollenHueMax = 130;
    /**
     * A piece held under the sensor this long while the intake takes in means the lane is full,
     * seconds ({@link LaneFull}). Negative or NaN: never full. Full is there to say "you're full,
     * go" quickly while the robot intakes facing away from the drivers (mentor, 2 Nov 2026).
     *
     * <p>From 20245's experimental logs of 1 Nov 2026 (sensor at slot 3, before the dead-zone fix,
     * at {@link #presentAbove} 0.10 / {@link #leaveBelow} 0.05): a piece passing with the intake
     * running was under the sensor for 0.3 to 1.0 s; a parked one for 11 s or more. Normal-play
     * logs will tighten it (likely 0.6 to 0.8 s): every full on and off is a match-log event with
     * the dwell and the lane tracker's count.
     *
     * <p>A false full does not mend itself: the prefeed stops and the roller drops to its hold power
     * ({@code PrefeedSubsystem.stopWhenFull}), so a piece that only paused under the sensor stays
     * until a shot or an unjam moves it. At slot 4, the entrance, a piece held there almost always
     * means the lane really is full; still, tighten this only from logs, and if false fulls stick
     * at an event, turn {@code stopWhenFull} off first.
     */
    public static double fullHoldS = 1.0;

    /** Red NECTAR's hue range, degrees; it wraps through 0. Bench, 2 Nov 2026: 9° to 22°, saturation 0.76. */
    public static double redNectarHueMin = 330;
    public static double redNectarHueMax = 47;
    /** Blue NECTAR's hue range, degrees. Bench, 2 Nov 2026: 212° to 232°, saturation 0.62 to 0.88. */
    public static double blueNectarHueMin = 180;
    public static double blueNectarHueMax = 270;

    /**
     * Whether the other alliance's NECTAR at the entrance is spat out ({@link NectarReject}): the
     * roller and the stars reverse for {@link #rejectBurstS}, the prefeed still. Off: colours are
     * logged only. Read every loop, so a Panels edit applies at once.
     */
    public static boolean rejectEnemyNectar = true;

    /**
     * How long one reject burst runs, seconds. <b>Not yet measured:</b> 0.5 is a guess at long
     * enough for the roller at {@code IntakeSubsystem.outPower} to throw one piece clear of the
     * robot. Too short and the roller takes it straight back in (another burst follows); too long
     * only costs intake time. Set from a robot ({@code doc/robot-todo.md}).
     */
    public static double rejectBurstS = 0.5;

    /**
     * Fresh readings in a row, each the other alliance's NECTAR with a piece present, before a
     * burst starts. 2, at the 50 ms read rate, decides in about 0.1 s; a piece passing with the
     * intake running is under the sensor 0.3 to 1.0 s (1 Nov logs). 1 trusts a single reading.
     */
    public static int rejectAfterReadings = 2;

    private final NormalizedColorSensor sensor;
    private final LauncherSubsystem launcher;
    private final IntakeSubsystem intake;
    private final PrefeedSubsystem prefeed;
    private final FeederSubsystem feeder;
    private final LaneTracker lane = new LaneTracker();
    private final LaneFull laneFull = new LaneFull();
    private final NectarReject reject = new NectarReject();
    /** Our alliance, for which NECTAR to reject; UNKNOWN (never rejects) until {@link #alliance} is called. */
    private Alliance alliance = Alliance.UNKNOWN;
    private LaneTracker.Piece lastPiece = LaneTracker.Piece.UNKNOWN;

    private long lastReadNs;
    private long lastUpdateNs;
    private boolean everRead;
    private boolean fresh;
    /** Whether the last {@link #update()} found that a piece could move under the sensor. */
    private boolean watching;
    private float red;
    private float green;
    private float blue;
    private float alpha;
    private double hueDeg = Double.NaN;
    private double saturation = Double.NaN;
    private String atInit;

    /**
     * @param hardwareMap the sensor is {@link DeviceNames#COLOR_SENSOR}
     * @param launcher    read for its shots; updated before this subsystem
     * @param intake      read for running outward; updated before this subsystem
     * @param prefeed     read for running outward; updated before this subsystem
     * @param feeder      read for feeding or reversing, when pieces move; updated before this subsystem
     */
    public ColorSensorSubsystem(HardwareMap hardwareMap, LauncherSubsystem launcher,
                                IntakeSubsystem intake, PrefeedSubsystem prefeed, FeederSubsystem feeder) {
        this.launcher = launcher;
        this.intake = intake;
        this.prefeed = prefeed;
        this.feeder = feeder;
        sensor = hardwareMap.get(NormalizedColorSensor.class, DeviceNames.COLOR_SENSOR);
    }

    /** One read, so {@link #health()} can say what the sensor answered. Not in the loop. */
    @Override
    public void initialize() {
        read(System.nanoTime());
        lastUpdateNs = lastReadNs;
        watching = false;
        atInit = allZero()
                ? "reads all zero: unplugged, or not on the bus robot_<name>.xml gives it"
                : String.format(Locale.US, "answering: %s, gain %.1f", summary(), sensor.getGain());
    }

    @Override
    public void start() {
    }

    @Override
    public void update() {
        long now = System.nanoTime();
        lastUpdateNs = now;
        boolean shot = launcher.shotJustDetected();
        boolean canMove = pieceCanMove(intake.state(), intake.starsState(), intake.holdingForFull(),
                prefeed.state(), feeder.state(), shot);
        fresh = readDue(everRead, (now - lastReadNs) / 1_000_000L, canMove, watching,
                readEveryMs, readIdleEveryMs);
        watching = canMove;
        if (fresh) {
            read(now);
        }
        // The lane runs outward when the stars do, whatever the roller does.
        boolean outward = intake.starsState() == IntakeSubsystem.State.OUT
                || prefeed.state() == PrefeedSubsystem.State.OUT;
        LaneTracker.Piece piece = LaneTracker.classify(hueDeg, pollenHueMin, pollenHueMax,
                redNectarHueMin, redNectarHueMax, blueNectarHueMin, blueNectarHueMax);
        if (fresh) lastPiece = piece;
        lane.step(fresh, alpha, piece, outward, shot, presentAbove, leaveBelow);
        reject.step(now / 1_000_000L, fresh, alpha, piece, alliance, rejectEnemyNectar,
                presentAbove, rejectAfterReadings, rejectBurstS * 1000.0);
        if (fresh) {
            // Full is the sensor alone: a piece held under it for fullHoldS. With fullHoldS off
            // (NaN or negative), full is off.
            laneFull.step(now / 1_000_000L, alpha, intake.requested() == IntakeSubsystem.State.IN,
                    presentAbove, leaveBelow, fullHoldS * 1000.0);
        }
    }

    /** The lane is full: a piece held under the sensor ({@link LaneFull}). As of the last reading. */
    public boolean full() {
        return laneFull.full();
    }

    /** The full detector itself, for the match log's dwell of each piece. */
    public LaneFull laneFull() {
        return laneFull;
    }

    /** Our alliance, for which NECTAR to reject. {@code RobotOpMode} sets it at PLAY, from {@code setup.alliance()}. */
    public void alliance(Alliance ours) {
        alliance = ours == null ? Alliance.UNKNOWN : ours;
    }

    /**
     * Whether the other alliance's NECTAR is being spat out ({@link NectarReject}), as of the last
     * update. The intake reads it and reverses the roller and the stars; the prefeed holds still.
     */
    public boolean rejecting() {
        return reject.rejecting();
    }

    /** The reject itself, for the match log's bursts. */
    public NectarReject reject() {
        return reject;
    }

    /** The colour of the latest reading ({@link LaneTracker#classify}), for the match log. */
    public LaneTracker.Piece lastPiece() {
        return lastPiece;
    }

    /** What it thinks is in the lane. Data only: never decide anything from it. */
    public LaneTracker lane() {
        return lane;
    }

    /**
     * Whether a piece can move under the sensor this loop, so it is read every {@link #readEveryMs}
     * (class comment, "When it reads"): a shot just counted, the feeder feeding or reversing (the
     * gate open, or the lane backing out), the prefeed running, or the roller or the stars turning,
     * unless all they do is hold a full lane ({@code holdingForFull}: pressed on a lane with no
     * room, the piece under the sensor stays). The feeder's standby (its wheels turning behind the
     * shut gate) is not a feed: its state stays IDLE. Pure, so a test can check it.
     */
    static boolean pieceCanMove(IntakeSubsystem.State roller, IntakeSubsystem.State stars, boolean holdingForFull,
                                PrefeedSubsystem.State prefeed, FeederSubsystem.State feeder,
                                boolean shotJustDetected) {
        if (shotJustDetected) return true;
        if (feeder != FeederSubsystem.State.IDLE) return true;
        if (prefeed != PrefeedSubsystem.State.IDLE) return true;
        if (holdingForFull) return false;
        return roller != IntakeSubsystem.State.IDLE || stars != IntakeSubsystem.State.IDLE;
    }

    /**
     * Whether to read this loop: the first time; at once when a piece can move and could not last
     * loop (a feed, a shot, an eject or the intake starting), so nothing waits out the slow
     * interval; otherwise once {@link #readIntervalMs} has passed since the last read. Pure.
     */
    static boolean readDue(boolean everRead, long sinceLastReadMs, boolean canMove, boolean couldMoveLastLoop,
                           int readEveryMs, int readIdleEveryMs) {
        if (!everRead) return true;
        if (canMove && !couldMoveLastLoop) return true;
        return sinceLastReadMs >= readIntervalMs(canMove, readEveryMs, readIdleEveryMs);
    }

    /**
     * How long between reads, ms: {@code readEveryMs} while a piece can move under the sensor,
     * else {@code readIdleEveryMs}, never faster than the first. Negatives read as 0 (every loop).
     * Pure, so a test can check it.
     */
    static long readIntervalMs(boolean laneMoving, int readEveryMs, int readIdleEveryMs) {
        long moving = Math.max(0, readEveryMs);
        return laneMoving ? moving : Math.max(moving, readIdleEveryMs);
    }

    private void read(long now) {
        NormalizedRGBA c = sensor.getNormalizedColors();
        red = c.red;
        green = c.green;
        blue = c.blue;
        alpha = c.alpha;
        hueDeg = hueDegrees(red, green, blue);
        saturation = saturation(red, green, blue);
        lastReadNs = now;
        everRead = true;
    }

    @Override
    public void stop() {
    }

    /** Whether the latest {@link #update()} read the sensor; the log writes only fresh readings. */
    public boolean fresh() {
        return fresh;
    }

    /**
     * Whether the latest {@link #update()} read at the fast rate, {@link #readEveryMs}, because a
     * piece could move under the sensor ({@link #pieceCanMove}); false while it reads every
     * {@link #readIdleEveryMs}, when every answer here may be up to that old.
     */
    public boolean watching() {
        return watching;
    }

    /** How old the reading behind every answer here is, ms, as of the latest {@link #update()}; NaN before the first. */
    public double readAgeMs() {
        return everRead ? (lastUpdateNs - lastReadNs) / 1e6 : Double.NaN;
    }

    public float red() {
        return red;
    }

    public float green() {
        return green;
    }

    public float blue() {
        return blue;
    }

    /** The clear channel: total light, colour or not. */
    public float alpha() {
        return alpha;
    }

    /** Hue, 0 to 360°: 0 red, 120 green, 240 blue. NaN for no colour at all (grey or dark). */
    public double hueDegrees() {
        return hueDeg;
    }

    /** Saturation, 0 (grey) to 1 (pure colour). */
    public double saturation() {
        return saturation;
    }

    @Override
    public String health() {
        return atInit;
    }

    @Override
    public void describe(Display display) {
        display.status("Color sensor", allZero() ? Display.Level.WARN : Display.Level.OK,
                allZero() ? "reads all zero: unplugged?" : summary());
        display.status("Lane", Display.Level.OK,
                (laneFull.full() ? "FULL (a piece held under the sensor)"
                        : laneFull.present() ? String.format(Locale.US, "piece under the sensor, %.0f ms",
                                laneFull.heldWhileIntakingMs())
                        : "room")
                        + String.format(Locale.US, " · read %.0f ms ago, every %d ms (%s)", readAgeMs(),
                        watching ? readEveryMs : readIdleEveryMs,
                        watching ? "a piece can move" : "nothing moving"));
        if (reject.rejecting()) {
            display.status("Reject", Display.Level.WARN, "spitting out the other alliance's NECTAR");
        }
        display.status("Lane (count, log only)", Double.isNaN(presentAbove) ? Display.Level.WARN : Display.Level.OK,
                lane.lettersFrontToBack() + " front to back, " + lane.count() + " in"
                        + (Double.isNaN(presentAbove) ? "; presentAbove not set: counts shots only" : ""));
    }

    /**
     * Whether the sensor reads all zero: unplugged, or not on the bus its XML gives it. Then the
     * lane is never full, so an Auto's INIT warns ({@code InitWarnings}).
     */
    public boolean silent() {
        return allZero();
    }

    private boolean allZero() {
        return red == 0f && green == 0f && blue == 0f && alpha == 0f;
    }

    private String summary() {
        return String.format(Locale.US, "R %.3f G %.3f B %.3f A %.3f, hue %.0f°, sat %.2f",
                red, green, blue, alpha, hueDeg, saturation);
    }

    /**
     * The hue of an RGB colour, 0 to 360°, the standard HSV formula. Pure, so a test can check it.
     * NaN when the three are equal: grey has no hue.
     */
    static double hueDegrees(double r, double g, double b) {
        double max = Math.max(r, Math.max(g, b));
        double min = Math.min(r, Math.min(g, b));
        double span = max - min;
        if (!(span > 0)) return Double.NaN;
        double h;
        if (max == r) {
            h = (g - b) / span;
        } else if (max == g) {
            h = 2.0 + (b - r) / span;
        } else {
            h = 4.0 + (r - g) / span;
        }
        h *= 60.0;
        return h < 0 ? h + 360.0 : h;
    }

    /** HSV saturation, 0 to 1. 0 for black. Pure, so a test can check it. */
    static double saturation(double r, double g, double b) {
        double max = Math.max(r, Math.max(g, b));
        double min = Math.min(r, Math.min(g, b));
        return max > 0 ? (max - min) / max : 0.0;
    }
}
