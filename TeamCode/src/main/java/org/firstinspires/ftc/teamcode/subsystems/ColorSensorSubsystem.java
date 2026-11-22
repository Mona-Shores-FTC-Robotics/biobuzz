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
 * it: whether the lane is full (below), and the NECTAR gate ({@link NectarReject}): whether the piece
 * at the entrance is one to spit out (the other alliance's NECTAR, or any piece of no sure colour),
 * and whether one may still be in the lane, which keeps the feed gate shut
 * ({@link #wrongNectarMayBeIn()}). Its colours are otherwise data only. A glitch reading (any channel
 * at {@link #maxPlausibleChannel}) decides nothing.
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
     * The low end is from the Belding logs (7 Nov 2026, 17 matches), no longer midway to red
     * NECTAR's bench reading: a POLLEN seen dimly (brightness under 0.2) read 44° to 55°, and one at
     * 46° was spat out as red NECTAR (20245, Q31, on blue), while real red NECTAR read up to 40°.
     * The gap is narrow and rests on a few pieces: check it against the next logs.
     */
    public static double pollenHueMin = 43;
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

    /**
     * With the lane beam fitted ({@code BeamBreakSubsystem.laneFitted}), {@link #fullHoldS}'s place:
     * the entrance hold that, with the beam broken for {@link #laneBeamHoldS}, is full. Shorter,
     * because the beam rules out the false fulls the long hold was guarding against. 0.3 s: a piece
     * passing the entrance is under the sensor a median 0.29 s (19429) to 0.42 s (20245), and a
     * piece cannot pass the entrance once the beam's place ahead of it is filled, so the hold only
     * has to outlast one reading's jitter. <b>Not measured on a beam</b>: bench it.
     */
    public static double fullHoldWithBeamS = 0.3;

    /**
     * How long the lane beam must have been broken for "full", seconds. 0.3: longer than a piece
     * takes to pass it (120 to 250 ms at lane speed, #505), so it means a piece is sitting there:
     * the lane packed up to the beam behind the gate.
     */
    public static double laneBeamHoldS = 0.3;

    /**
     * Red NECTAR's hue range, degrees; it wraps through 0. Bench, 2 Nov 2026: 9° to 22°, saturation
     * 0.76. In the Belding matches it read mostly 10° to 24°, and 33° to 39° for a piece the lane
     * logged as red NECTAR (19429, the 16:39 playoff; a second run of 35° to 40° readings in its
     * 14:41 match looks the same), so the top is 42, not the 35 first proposed: 35 would have
     * kept those as POLLEN. It was 47 until 10 Nov 2026 ({@link #pollenHueMin} says why).
     */
    public static double redNectarHueMin = 330;
    public static double redNectarHueMax = 42;
    /**
     * The empty lane's hue range, degrees, for {@link #emptyTooBright()} only: the lane's own
     * grey-green with nothing in front of the sensor. 19429, 14 to 17 Nov 2026, with and without
     * the black bottom plate: 137° to 148° at saturation about 0.46 to 0.50, whatever the
     * brightness. Kept clear of {@link #pollenHueMax}: a preload half in view read 119°.
     */
    public static double emptyHueMin = 132;
    public static double emptyHueMax = 170;

    /** Blue NECTAR's hue range, degrees. Bench, 2 Nov 2026: 212° to 232°, saturation 0.62 to 0.88. */
    public static double blueNectarHueMin = 180;
    public static double blueNectarHueMax = 270;

    /**
     * Whether a wrong piece at the entrance is spat out ({@link NectarReject}): the other alliance's
     * NECTAR, or a piece the reading is not sure of. The roller, the stars and the prefeed reverse for
     * {@link #rejectBurstS}, the whole lane. Off: colours are logged only, and the feed gate is never
     * held shut. Read every loop, so a Panels edit applies at once.
     */
    public static boolean rejectEnemyNectar = true;

    /**
     * Whether the reject spits out <b>both</b> NECTAR colours, ours too, so the robot keeps only
     * POLLEN. Off: only the other alliance's NECTAR. Needs {@link #rejectEnemyNectar} on. A robot
     * locked out of NECTAR flips it in {@code RobotDifferences}. Read every loop.
     */
    public static boolean rejectAllNectar = false;

    /**
     * How long one reject burst runs, seconds. <b>Not yet measured:</b> 0.5 is a guess at long
     * enough for the roller at {@code IntakeSubsystem.outPower} to throw one piece clear of the
     * robot. Too short and the roller takes it straight back in (another burst follows); too long
     * only costs intake time. Set from a robot ({@code doc/robot-todo.md}).
     */
    public static double rejectBurstS = 0.5;

    /**
     * The roller's power during a reject burst, 0 to 1; the stars run at {@code IntakeSubsystem.starsPower}.
     * 1.0, not the eject's {@code IntakeSubsystem.outPower} (0.6): at 0.6 a burst on 20245 left a
     * NECTAR at the mouth, and it rolled back in (5 Nov 2026). Lower it if pieces fly too far.
     */
    public static double rejectRollerPower = 1.0;

    /**
     * Fresh readings in a row, each a wrong piece ({@link NectarReject#judge}), before a burst
     * starts. 1: the sensor makes a new reading only about every 100 ms, so at the 50 ms read rate
     * two in a row were most often the same reading twice (Fruitport, 21 Nov 2026), and a NECTAR
     * passing the entrance is under the sensor for 0.13 s (20245, Q-28). With the glitches dropped
     * ({@link #maxPlausibleChannel}) one reading is enough, and a false one costs a POLLEN at most.
     */
    public static int rejectAfterReadings = 1;

    /**
     * A reading with any colour channel at or above this is not of a real piece and is dropped
     * ({@link NectarReject#plausible}): no lane, full or reject decision sees it. Fruitport, 21 Nov
     * 2026: real pieces never read a channel above 0.19 (8030 readings at brightness over 0.5), while
     * 20245 read 92 glitches at brightness 1.00 with red at 1.0, two of which (red 1, green 0, blue
     * 0, hue 0°) fired a red NECTAR reject on BLUE in Q-20. NaN: only the brightness test (1.00).
     */
    public static double maxPlausibleChannel = 0.5;

    /**
     * A hue this many degrees from the other alliance's NECTAR range is that NECTAR too: an
     * uncertain piece is treated as wrong (mentor, 22 Nov 2026). The gap that matters is red NECTAR's
     * top ({@link #redNectarHueMax}, 42°, real red read up to 40° at Belding) against POLLEN's bottom
     * ({@link #pollenHueMin}, 43°, a dim POLLEN read 44° to 55°): on BLUE, 5 rejects a POLLEN read at
     * 47° or below. Not used for our own NECTAR, which is 170° from the other colour.
     */
    public static double rejectDoubtDeg = 5;

    /**
     * A dim reading from this brightness up, in or near the other alliance's NECTAR colour, is a
     * wrong NECTAR just off the sensor's spot ({@link NectarReject.Sighting#WRONG_FAINT}). The empty
     * lane read 0.006 to 0.012 on 20245 at Fruitport; in Q-11 the red NECTAR that was launched read
     * 0.018 to 0.022 inside the lane after the bursts. NaN: no faint sightings.
     */
    public static double rejectFaintAbove = 0.015;

    /**
     * A faint sighting needs at least this saturation: the empty lane read 0.33 to 0.42 on 20245
     * and 0.46 to 0.50 lit on 19429 (at a grey-green hue, never near either NECTAR); the faint red
     * NECTAR in Q-11 0.50 to 0.73.
     */
    public static double rejectFaintMinSaturation = 0.45;

    /**
     * How long after a bright wrong reading a faint one still counts, seconds; while the lane is
     * suspect it always does. A faint reading alone never makes the lane suspect: an empty lane on
     * 20245 once read 0.02 to 0.03 at a blue hue (5 Nov 2026).
     */
    public static double rejectFaintWindowS = 3.0;

    /**
     * Whether the feed gate stays shut while a wrong NECTAR may be in the lane
     * ({@link #wrongNectarMayBeIn()}). Off: the bursts only, as before 22 Nov 2026, when 20245
     * launched a red NECTAR on BLUE (Fruitport Q-11). Read every loop.
     */
    public static boolean shutGateOnWrongNectar = true;

    /**
     * After a wrong NECTAR was seen leaving the entrance while the lane ran outward, the sensor must
     * read clean this long before the lane is clear again, seconds. Q-28 (20245 on RED): the burst
     * carried a BLUE NECTAR off the spot in 0.4 s and the lane stayed clean.
     */
    public static double wrongNectarClearS = 1.0;

    /**
     * The lane running outward this long with no bright wrong reading (the driver's eject) clears a
     * suspect lane, seconds: long enough to carry anything in the lane out past the sensor.
     * <b>Not measured:</b> 1.0 is a guess at a full lane's eject; bench it ({@code doc/robot-todo.md}).
     * Above {@link #rejectBurstS}, so a burst alone never clears it this way.
     */
    public static double wrongNectarEjectClearS = 1.0;

    /**
     * After each burst, how long the roller and the stars stay stopped so the piece rolls clear,
     * seconds. The intake is asked to take in almost all the time (biobuzz#456), so with no hold-off
     * the roller would take the piece straight back. <b>Not yet measured:</b> 0.3 is a guess.
     * 0 turns it off.
     */
    public static double rejectHoldOffS = 0.3;

    /**
     * The backstop: more than this many bursts within {@link #rejectWindowS} and the reject stops
     * until the sensor sees something other than the wrong NECTAR. A piece wedged where a burst
     * cannot clear it then does not run the intake backwards for the rest of the match. 0: no cap.
     */
    public static int rejectMaxBursts = 3;
    /** The window for {@link #rejectMaxBursts}, seconds. */
    public static double rejectWindowS = 3.0;

    private final NormalizedColorSensor sensor;
    private final LauncherSubsystem launcher;
    private final IntakeSubsystem intake;
    private final PrefeedSubsystem prefeed;
    private final FeederSubsystem feeder;
    private final LaneTracker lane = new LaneTracker();
    private final LaneFull laneFull = new LaneFull();
    /** The lane beam ({@code BeamBreakSubsystem.lane()}); null while none is fitted, and full is the colour sensor's alone. */
    private BeamBreak laneBeam;
    private final NectarReject reject = new NectarReject();
    /** Our alliance, for which NECTAR to reject; UNKNOWN (never rejects) until {@link #alliance} is called. */
    private Alliance alliance = Alliance.UNKNOWN;
    private LaneTracker.Piece lastPiece = LaneTracker.Piece.UNKNOWN;
    /** Whether the latest reading could be of a real piece ({@link #maxPlausibleChannel}). */
    private boolean plausible = true;
    /** Readings dropped as implausible since init. */
    private long implausible;

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
    /** Since when, ms, every fresh reading has looked like a lit empty lane ({@link #emptyReadsAboveLeave}); -1 while not. */
    private long brightEmptySinceMs = -1;

    /**
     * How long the empty lane must read at or above {@link #leaveBelow} before
     * {@link #emptyTooBright()} says so, ms: four slow (idle) readings, so a piece passing the edge
     * of the sensor never trips it.
     */
    static final long BRIGHT_EMPTY_HOLD_MS = 1000;

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
        // A piece out of the lane: the throat beam's ball when one is fitted, else a counted dip.
        boolean shot = launcher.launchedThisLoop();
        // A reject's hold-off stops everything, but the piece may roll back under the sensor:
        // keep the fast rate (5 Nov 2026, the slow rate missed one rolling back in).
        boolean canMove = pieceCanMove(intake.state(), intake.starsState(), intake.holdingForFull(),
                prefeed.state(), feeder.state(), shot) || reject.phase() != NectarReject.Phase.IDLE;
        fresh = readDue(everRead, (now - lastReadNs) / 1_000_000L, canMove, watching,
                readEveryMs, readIdleEveryMs);
        watching = canMove;
        if (fresh) {
            read(now);
            plausible = NectarReject.plausible(red, green, blue, alpha, maxPlausibleChannel);
            if (!plausible) implausible++;
        }
        // A glitch reading is logged (fresh) but decides nothing: as if not read.
        boolean usable = fresh && plausible;
        // The lane runs outward when the stars do, whatever the roller does.
        boolean outward = intake.starsState() == IntakeSubsystem.State.OUT
                || prefeed.state() == PrefeedSubsystem.State.OUT;
        LaneTracker.Piece piece = LaneTracker.classify(hueDeg, pollenHueMin, pollenHueMax,
                redNectarHueMin, redNectarHueMax, blueNectarHueMin, blueNectarHueMax);
        if (usable) lastPiece = piece;
        lane.step(usable, alpha, piece, outward, shot, presentAbove, leaveBelow);
        // The OpMode's own eject (driver or operator unjam) clears the whole lane; a NECTAR reject does not.
        boolean ejecting = intake.requested() == IntakeSubsystem.State.OUT;
        if (ejecting) {
            lane.ejected();
            laneFull.clear();
        }
        // Seen above leaveBelow, not presentAbove: a NECTAR at the edge of the spot still counts.
        double seenAbove = Double.isNaN(leaveBelow) ? presentAbove : Math.min(leaveBelow, presentAbove);
        NectarReject.Sighting sighting = usable
                ? NectarReject.judge(hueDeg, saturation, alpha, alliance, rejectAllNectar, seenAbove,
                        rejectFaintAbove, rejectFaintMinSaturation, rejectDoubtDeg, pollenHueMin, pollenHueMax,
                        redNectarHueMin, redNectarHueMax, blueNectarHueMin, blueNectarHueMax)
                : NectarReject.Sighting.NOTHING;
        reject.step(now / 1_000_000L, usable, sighting, outward, rejectEnemyNectar, rejectAfterReadings,
                rejectBurstS * 1000.0, rejectHoldOffS * 1000.0, rejectMaxBursts, rejectWindowS * 1000.0,
                rejectFaintWindowS * 1000.0, wrongNectarClearS * 1000.0, wrongNectarEjectClearS * 1000.0);
        if (usable) {
            long nowMs = now / 1_000_000L;
            if (!emptyReadsAboveLeave(alpha, hueDeg, presentAbove, leaveBelow, emptyHueMin, emptyHueMax)) {
                brightEmptySinceMs = -1;
            } else if (brightEmptySinceMs < 0) {
                brightEmptySinceMs = nowMs;
            }
        }
        if (usable) {
            // Full is the sensor alone: a piece held under it for fullHoldS. With fullHoldS off
            // (NaN or negative), full is off.
            laneFull.step(now / 1_000_000L, alpha, intake.requested() == IntakeSubsystem.State.IN,
                    presentAbove, leaveBelow, (laneBeam == null ? fullHoldS : fullHoldWithBeamS) * 1000.0);
        }
    }

    /**
     * The lane is full: a piece held under the sensor ({@link LaneFull}), as of the last reading;
     * with the lane beam fitted, and the beam broken for {@link #laneBeamHoldS} too
     * ({@link #fullWithBeam}). The indicator's green, the intake's hold and the Autos'
     * {@code IntakeFull} all read this.
     */
    public boolean full() {
        return fullWithBeam(laneFull.full(), laneBeam, laneBeamHoldS);
    }

    /** The colour sensor's own full, whatever the lane beam says: for the match log, to compare the two. */
    public boolean colourFull() {
        return laneFull.full();
    }

    /**
     * Full by the colour sensor and the lane beam together: with no beam ({@code null}), the colour
     * sensor's alone; with one, both must agree, the beam broken for {@code beamHoldS} (NaN or
     * negative: as 0, any break).
     *
     * <p>Why both (Fruitport, 21 Nov 2026, 15 match logs): the colour sensor read FULL 37 s a match
     * on 19429 and 29 s on 20245. Judged by what the next feed fired, 20 s a match of 19429's (0.8 s
     * of 20245's) was a piece held at the entrance with two or fewer ahead of it: the intake stopped
     * with room for two. A beam where the third piece sits refuses every one of those; with three
     * ahead the lane really is full. Pure, so a test can check it.
     */
    static boolean fullWithBeam(boolean colourFull, BeamBreak beam, double beamHoldS) {
        if (!colourFull) return false;
        if (beam == null) return true;
        double holdMs = beamHoldS > 0 ? beamHoldS * 1000.0 : 0.0;
        return beam.blocked() && beam.blockedForMs() >= holdMs;
    }

    /**
     * Decide full with the lane beam from now on ({@code BeamBreakSubsystem.lane()}); null keeps the
     * colour sensor alone. Wired once by {@code Robot}, which updates the beam before this subsystem.
     */
    public void laneBeam(BeamBreak beam) {
        this.laneBeam = beam;
    }

    /** Whether full needs the lane beam to agree ({@link #laneBeam}). */
    public boolean usesLaneBeam() {
        return laneBeam != null;
    }

    /**
     * Whether the lane, with no piece under the sensor, reads at or above {@link #leaveBelow}, and
     * has for {@link #BRIGHT_EMPTY_HOLD_MS}: a piece that comes in is then never seen to leave, so
     * the lane reads full after one second of intake with nothing in it. It happened on 19429 from
     * 16 Nov 2026, when more ambient light reached the lane: the empty lane read 0.034 before and
     * 0.055 to 0.085 after, against a leaveBelow of 0.05, and the presence latched for whole runs.
     * An INIT warning ({@code InitWarnings.Kind.LANE_SENSOR_EMPTY}) reads it.
     */
    public boolean emptyTooBright() {
        return brightEmptySinceMs >= 0 && lastReadNs / 1_000_000L - brightEmptySinceMs >= BRIGHT_EMPTY_HOLD_MS;
    }

    /**
     * Whether one reading looks like an empty lane too bright for {@link #leaveBelow}: at or above
     * the leave level, in the empty lane's grey-green ({@link #emptyHueMin} to {@link #emptyHueMax}).
     * Colour, not a brightness band: a preload sitting half in view of the sensor in an Auto's INIT
     * read 0.096, between the leave and presence levels, at a hue of 119° (19429, 17 Nov 2026, with
     * the plate), and is a piece, not a lit empty lane. Pure, so a test can check it.
     */
    static boolean emptyReadsAboveLeave(double alpha, double hueDeg, double presentAbove, double leaveBelow,
                                        double emptyHueMin, double emptyHueMax) {
        if (Double.isNaN(alpha) || Double.isNaN(presentAbove)) return false;
        double leave = Double.isNaN(leaveBelow) ? presentAbove : Math.min(leaveBelow, presentAbove);
        return alpha >= leave && LaneTracker.inRange(hueDeg, emptyHueMin, emptyHueMax);
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
     * update. The intake reads it and reverses the roller and the stars; the prefeed follows them out.
     */
    public boolean rejecting() {
        return reject.rejecting();
    }

    /**
     * Whether a wrong NECTAR may be in the lane ({@link NectarReject#suspect()}) and the feed gate
     * must stay shut ({@link #shutGateOnWrongNectar}). {@code FeedGateSubsystem} reads it.
     */
    public boolean wrongNectarMayBeIn() {
        return shutGateOnWrongNectar && reject.suspect();
    }

    /** Whether the latest reading could be of a real piece; a glitch decides nothing ({@link #maxPlausibleChannel}). */
    public boolean plausible() {
        return plausible;
    }

    /** Readings dropped as glitches since init ({@link #maxPlausibleChannel}). */
    public long implausibleReadings() {
        return implausible;
    }

    /** Whether the hold-off after a burst is running: the intake keeps the roller and stars stopped. */
    public boolean holdingOffAfterReject() {
        return reject.holdingOff();
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
                (full() ? (laneBeam == null ? "FULL (a piece held under the sensor)" : "FULL (sensor and lane beam)")
                        : laneFull.full() ? "room: a piece held at the entrance, lane beam clear"
                        : laneFull.present() ? String.format(Locale.US, "piece under the sensor, %.0f ms",
                                laneFull.heldWhileIntakingMs())
                        : "room")
                        + String.format(Locale.US, " · read %.0f ms ago, every %d ms (%s)", readAgeMs(),
                        watching ? readEveryMs : readIdleEveryMs,
                        watching ? "a piece can move" : "nothing moving"));
        if (reject.rejecting() || reject.holdingOff()) {
            display.status("Reject", Display.Level.WARN, reject.rejecting()
                    ? "spitting out a wrong NECTAR" : "letting the rejected NECTAR roll clear");
        } else if (reject.gaveUp()) {
            display.status("Reject", Display.Level.WARN, "gave up after " + rejectMaxBursts
                    + " bursts: the wrong NECTAR may still be in");
        }
        if (wrongNectarMayBeIn()) {
            display.status("Feed gate", Display.Level.WARN,
                    "SHUT: a wrong NECTAR may be in the lane. Eject (unjam) to clear it");
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
