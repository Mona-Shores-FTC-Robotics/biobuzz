package org.firstinspires.ftc.teamcode.opmodes;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.ivy.Command;
import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.autokit.AutoRegistry;
import org.firstinspires.ftc.teamcode.localization.GardenFieldPoints;
import org.firstinspires.ftc.teamcode.localization.GardenFieldPoints.Garden;

import java.util.Locale;
import java.util.function.Consumer;

/**
 * Gathers from the nearest GARDEN the way the FLOWER pick works: the FLOWER stick down and the
 * intake on, a drive in to the GARDEN's gather pose (plus {@link DriveIn#extraDepthIn}), a dwell, and a
 * drive back to where the drive in started, the stick coming up once the robot has backed
 * {@link Stick#retractAfterBackIn} out. The sequence is {@link GrabRoutine}'s, shared with the FLOWER
 * pick; this class says where a GARDEN is and what the mechanisms do.
 *
 * <p>The stick went in on 30 Oct 2026: a drive in with the intake alone did not reach deep enough
 * into the GARDEN. A sweep, coming in at an angle, or another helper is a change here, and every
 * Auto that gathers gets it.
 *
 * <p>The driver's GARDEN gather button runs it while held (BasicDriveTeleOp; the button is in
 * {@code controls/ControlScheme}), lining up first at the GARDEN's line-up pose
 * ({@link GardenFieldPoints}); an Auto's {@code GardenGather} card runs it to the end
 * ({@link #command}) with no line-up: it drives in to the GARDEN from wherever the card is called and
 * comes back there. Each phase is timed and written to the match log.
 *
 * <p><b>In an Auto the stick comes down on the drive to the card.</b> The path card before a
 * {@code GardenGather} card runs this gather's {@link #approach} ({@code AutoKit},
 * {@code AutoRegistry.Approach}): over the last {@link AutoStick#approachLowerWithinIn} of that drive the stick
 * goes down and the intake starts, so the stick is down when the card starts ({@link #readySinceNs}).
 * Measured, 3 to 14 Nov 2026: all ten Auto gathers stood 0.70 to 0.75 s at GARDEN_STAGING for the
 * stick before they moved (the wait was 700 ms then; it is {@link Stick#stickDownMs} now). <b>The stick never comes down while the robot is
 * turning or facing another way</b> ({@link #lowerOnApproach}): the drive must end facing the GARDEN,
 * and the robot must have held that facing (to {@link AutoStick#approachFacingDeg}) for {@link AutoStick#facingHoldMs}.
 * So only a drive whose turn is finished early gains the time; one that turns all the way in
 * (R-NoCross's) leaves the stick to the card, as before.
 *
 * <p><b>What the logs say (72 gathers on both robots that drove in, 1 to 14 Nov 2026):</b> with the
 * stick down the robot stops with the stick on the audience wall, its centre about 13 in out. That
 * is the stick doing its job: it is the ramp the GARDEN's POLLEN pop over as the robot drives in
 * (mentor, 13 Nov 2026: three come over it; the corner one is a mechanical fix, printed guides on
 * the arms). The nine Auto gathers of 3 to 7 Nov fired 0 to 2 in their next volley, 1.0 on average.
 * On 14 Nov at the field, with {@link DriveIn#inSpeed} 1.0 and the stick lowered sooner, the lane count rose
 * by 2 or 3 in most gathers, and it reads low after a gather.
 *
 * <p>Every number here is a starting point to tune in Panels, not a measurement. The Visualizer's
 * picture of a gather ({@code lib/simple/garden.ts}) uses the same numbers: change one, change the
 * other.
 */
@Configurable
public final class GardenGather extends GrabRoutine {

    /** The TeleOp button's line-up, before the drive in. */
    public static final class LineUp {
        /**
         * The line-up spot, moved this much further from the side wall than the GARDEN's line-up pose
         * ({@link GardenFieldPoints}), inches: room to turn in the corner (30 Oct 2026). The drive in
         * then runs at a slant to the gather pose.
         */
        public double lineUpOutIn = 3.0;
        /**
         * And this much further back from the GARDEN (away from the audience wall), inches. 6 since
         * 16 Nov 2026 (mentor; 3 before, the value of the 14 Nov field session): 3 in more drive in, about
         * 0.12 s, for the stick to finish its swing in, now that nothing stands for it
         * ({@link Stick#stickDownMs}). Not yet run. The Autos start at GARDEN_STAGING, which is unchanged.
         */
        public double lineUpBackIn = 6.0;
        /** The line-up path gives up after this long, ms. */
        public double lineUpTimeoutMs = 4000;
        /** Already lined up: this close to the line-up pose, inches, skips the line-up path. */
        public double atLineUpIn = 1.5;
        /** Already lined up: this square, degrees, skips the line-up path. */
        public double atLineUpDeg = 5.0;
    }

    /** See {@link LineUp}. */
    public static LineUp lineUp = new LineUp();

    /** The FLOWER stick: whether it is used, when it goes down on the button's line-up or an Auto's card, and when it comes back up. */
    public static final class Stick {
        /**
         * The FLOWER stick goes down for the gather. Off: the stick stays in, there is no stick wait,
         * and the drive in aims {@link DriveIn#noStickExtraDepthIn} deeper, so the intake itself goes all the
         * way to the audience wall. A test (mentor, 6 Nov 2026) of whether the stick helps at all: it
         * went in on 30 Oct because the intake alone did not reach deep enough, but that was the
         * diagonal drive in, which stopped short anyway.
         */
        public boolean useStick = true;
        /**
         * The stick goes down and the intake starts once the robot is this close to the line-up,
         * inches, and faces the GARDEN to within {@link Stick#lowerWithinDeg}. The GARDEN is in a corner, so
         * a stick swung down early, while the robot still turns, came down on the wall (30 Oct 2026).
         * 10 in and 30° since 14 Nov 2026 (mentor, on 19429 at the field; 6 in and 10° before): the stick
         * comes down sooner on the line-up, and 8 gathers, 4 in a row, had no stick on the wall. 16 in
         * since 16 Nov (mentor, from the 14 Nov logs, not yet run): at 10 in the stick was only 0.3 s
         * into its swing when the line-up ended.
         */
        public double lowerWithinIn = 16.0;
        /** See {@link Stick#lowerWithinIn}, degrees. */
        public double lowerWithinDeg = 30.0;
        /**
         * And the stick comes down only while the robot is turning slower than this, degrees a second:
         * facing the right way is not enough if it is still swinging round (mentor, 16 Nov 2026). On
         * 14 Nov the stick was lowered 41 times; 12 of them with the robot turning 57 to 148°/s, and four
         * more than 18° off. The turn was under 20°/s within 0.04 to 0.5 s each time, so the stick waits
         * that long at most. It applies on the button's line-up, at an Auto's card, and on an Auto's
         * drive in ({@link #approach}). An unknown turn rate (no Pinpoint) does not hold it.
         */
        public double lowerMaxTurnDegPerSec = 30.0;
        /**
         * The drive in may start once the stick has been on its way down this long, ms, counted from
         * when it was lowered (on the line-up, or on an Auto's drive to the card), not from arrival: a
         * stick lowered earlier than this costs no standing at all. 0 since 16 Nov 2026 (mentor; 700
         * before): the robot never stands for the stick, which finishes its swing while the robot drives
         * in. Not yet run. The margin, from the 14 Nov logs: at {@link DriveIn#inSpeed} 1.0 the robot was within
         * 4 in of where it stops 0.53 to 0.78 s after the drive in began (13 gathers), and the FLOWER
         * films put the swing at about 0.3 to 0.5 s. So a stick lowered only as the drive in starts (an
         * Auto whose drive turns all the way in) lands with 0 to 0.2 s to spare. If it is seen landing
         * on the pieces, raise this to 300, or start the gather further back ({@link LineUp#lineUpBackIn}; the
         * Autos' GARDEN_STAGING spot): each inch further is about 0.04 s more.
         */
        public double stickDownMs = 0;
        /**
         * The stick comes up this long after the drive back has stopped, ms: it stays down the whole way
         * out and rises only once the robot is still (mentor, 6 Nov 2026), so it does not drag pieces out
         * with it. NaN or negative: the old rule, up once the robot has backed {@link Stick#retractAfterBackIn}.
         * A gather stopped before its drive back (let go, cut) still uses the old rule.
         */
        public double raiseAfterStopMs = 250;
        /** Without {@link Stick#raiseAfterStopMs}: the stick comes up once the robot has backed out this far from the drive in's end, inches. */
        public double retractAfterBackIn = 6.0;
    }

    /** See {@link Stick}. */
    public static Stick stick = new Stick();

    /** In an Auto: the stick lowered on the drive before the gather's card. */
    public static final class AutoStick {
        /**
         * In an Auto, the drive before the gather's card lowers the stick and starts the intake on its way
         * in ({@link #approach}). Off: the card lowers it itself and stands {@link Stick#stickDownMs} first, as
         * every Auto gather has so far.
         */
        public boolean lowerOnDriveIn = true;
        /**
         * With {@link AutoStick#lowerOnDriveIn}: the stick goes down once the robot is this close to the end of the
         * drive before the card, inches. 24 is the FLOWER pick's ({@code FlowerPicker.lowerWithinIn}): a
         * plan, not yet run at the GARDEN. A drive that arrived facing the GARDEN (L-Infraviolet's, 6 and
         * 7 Nov) was 24 in out 0.77 to 0.86 s before its end, enough for the whole {@link Stick#stickDownMs}.
         */
        public double approachLowerWithinIn = 24.0;
        /**
         * With {@link AutoStick#lowerOnDriveIn}: how squarely the robot must face the GARDEN, degrees, for the stick
         * to come down on the drive in. Tighter than the button's {@link Stick#lowerWithinDeg}: the button
         * lowers it within 10 in of its line-up, slow; a drive lowers it up to 24 in out, at speed.
         */
        public double approachFacingDeg = 10.0;
        /**
         * With {@link AutoStick#lowerOnDriveIn}: the robot must have faced the GARDEN (to within
         * {@link AutoStick#approachFacingDeg}) for this long without a break, ms, before the stick may come down on
         * the drive in: the turn is over, not passing through. The stick came down on the field wall when
         * it was lowered with the robot facing the wrong way (mentor, 13 Nov 2026). A drive that turns all
         * the way in (R-NoCross's and R-Clockwise's turn half a turn evenly over the whole drive: 22° off
         * with 6 in to go, facing only for its last 0.1 s) never lowers the stick early; its card does,
         * standing, as before. To gain the 0.7 s there, draw the turn finished early in the Visualizer.
         */
        public double facingHoldMs = 200;
    }

    /** See {@link AutoStick}. */
    public static AutoStick autoStick = new AutoStick();

    /** The drive in: which GARDEN, where it aims, how fast, and how it knows it has hit the wall. Used by both ways in (the curve, and twoStep). */
    public static final class DriveIn {
        /**
         * Only a GARDEN this close to the robot is gathered from, inches. 45 since 14 Nov 2026 (mentor, on
         * 19429 at the field; 160, the whole of our half, before): the button only gathers near the
         * GARDEN. An Auto's card starts at GARDEN_STAGING, about 13 in away.
         */
        public double maxRangeIn = 45.0;
        /**
         * The drive in starts once the robot has slowed below this after the drive before it, inches/s:
         * the gather's hand-over speed. Pedro calls a path done at its parametric end with the robot
         * still rolling (10 to 24 in/s in the 29 Oct to 2 Nov 2026 match logs; under 8 in/s 0.07 to
         * 0.19 s later, under 2 in/s 0.13 to 0.51 s later). The gather drives itself in from the staging
         * spot and ends on the wall, so it need not be still first: 8 since 3 Nov 2026 (mentor), the same
         * as the FLOWER pick's, from the 2 a launch waits for ({@code PathEndSettle}). 20 since 14 Nov
         * 2026 (mentor, on 19429 at the field): the drive in starts without waiting to slow.
         */
        public double settledInPerSec = 20.0;
        /**
         * How much further than the GARDEN's gather pose the drive in aims, inches, along the robot's
         * heading. A drive in that hits the wall first just stops there (it stalls), so too much is
         * safe; tune it in Panels until the gather reaches deep enough. 10 since 18 Nov 2026 (mentor,
         * from 1): Pedro brakes to stop at the path's end, and on the short drive in from GARDEN_STAGING
         * an end at the wall meant the robot crept the last inches; an end well past the wall keeps it
         * driving when it meets the wall, and the stall ends the drive in there. 19429 ran 4 in Panels
         * that day, still slow.
         */
        public double extraDepthIn = 10.0;
        /**
         * Without the stick ({@link Stick#useStick} off), how much deeper the drive in aims, inches, on top of
         * {@link DriveIn#extraDepthIn}: the gather pose puts the nose 1.5 in beyond the robot's front at the
         * wall, and a drive in that meets the wall first just stalls there, so a little more is safe.
         */
        public double noStickExtraDepthIn = 2.0;
        /**
         * The drive in curves over to the side wall first: from wherever it starts (the button's line-up
         * spot, or GARDEN_STAGING where an Auto's card starts) it is a Pedro curve whose control point is
         * on the side wall's line level with the start ({@link #wallControl}), so the robot moves across
         * to the wall early and runs down it into the corner. Off: one straight diagonal. Why (6 Nov
         * 2026, all ten gathers on both robots): on the diagonal the side met the side wall first, the
         * robot pinned itself there, and the drive in stalled with the nose 2 to 4.5 in short of the
         * audience wall. The line-up spots are unchanged.
         */
        public boolean curveToWall = true;
        /**
         * How much closer to the alliance (side) wall than the gather pose the drive in aims, inches: the
         * intake has to reach all the way into the corner to scoop the whole GARDEN (30 Oct 2026). More
         * than the robot can go is meant: the wall stops it, the drive in ends when it stalls, and the
         * dwell keeps steering it against the wall.
         */
        public double towardWallIn = 2.0;
        /**
         * With {@link DriveIn#curveToWall}: how much closer to the side wall than the gather pose the drive in
         * ends, inches. Enough to keep the side on the wall at the end: the wall stops it, and the drive
         * in ends when it stalls. 2.0 since 14 Nov 2026 (mentor, at the field: the gather sat too far off
         * the wall at 0.5); 3.0 since 18 Nov 2026 (mentor); 5.0 since 19 Nov 2026 (mentor, on 19429:
         * blue's gathers reached the side wall and worked, red's sat 1 to 2 in off it and missed). Not
         * much more: an end the robot can never reach sideways keeps Pedro's strafe correction
         * saturated, which leaves less power for the drive along the wall (19429 tried 6 in Panels on
         * 18 Nov and the drive in was slow).
         */
        public double laneTowardWallIn = 5.0;
        /**
         * The drive in's speed cap, a fraction of the robot's top path speed (0 to 1]. 1.0 since 14 Nov
         * 2026 (mentor, at the field): at 0.5 the curve over to the side wall, mostly a strafe, was very
         * slow.
         */
        public double inSpeed = 1.0;
        /**
         * How far the drive in, the push and the dwell turn the robot toward the side wall, degrees, so
         * the intake angles into the corner and takes the corner piece: red's 270° becomes 270 minus this
         * (blue's 90° the same, turned half a turn). Only the heading changes; where the drive in ends
         * does not. 0 until the mentor's trial on 19429 (18 Nov 2026: square to the audience wall the
         * gather missed the corner ball; 2 to 5 to try).
         */
        public double intoCornerDeg = 0.0;
        /** The drive in gives up after this long, ms. */
        public double inTimeoutMs = 4000;
        /** Less progress than this, inches, in {@link DriveIn#stallMs} means the robot is against something. */
        public double stallProgressIn = 0.25;
        /** See {@link DriveIn#stallProgressIn}, ms. */
        public double stallMs = 250;
    }

    /** See {@link DriveIn}. */
    public static DriveIn driveIn = new DriveIn();

    /** The two-step drive in, instead of the curve: over to the side wall, then a straight push down it. */
    public static final class TwoStep {
        /**
         * The drive in in two steps, instead of the one curve ({@link DriveIn#curveToWall}): a path over to the
         * side wall's line (the gather pose's, level with where the drive in starts), then a straight
         * push down the wall at {@link TwoStep#pushPower} until the robot stalls against the audience wall. The
         * push is not a path, so nothing brakes before the wall, and it has no acceleration limit; it
         * steers onto its line as the dwell does ({@link Steering#lateralKp}, {@link Steering#headingKp},
         * {@link Steering#maxCorrection}), the line {@link DriveIn#laneTowardWallIn} into the side wall. The back out is
         * unchanged. Off until tried (mentor, 18 Nov 2026: the curve's drive in was slow).
         */
        public boolean twoStepIn = false;
        /** With {@link TwoStep#twoStepIn}: the push's forward power, 0 to 1. */
        public double pushPower = 1.0;
    }

    /** See {@link TwoStep}. */
    public static TwoStep twoStep = new TwoStep();

    /** The dwell: sitting in the corner with the intake on. */
    public static final class Dwell {
        /**
         * The dwell, ms, intake running, at the end of the drive in: a cap (a full lane ends it sooner,
         * {@link Dwell#dwellEndsOnFull}).
         * 400 since 6 Nov 2026 (mentor): in, a short dwell, and straight back out, rather than the 1.5 s
         * every gather that night sat for while bringing in 0 to 1 pieces. Tune it at the practice field
         * with the button.
         */
        public double dwellMs = 400;
        /**
         * The dwell ends as soon as the lane is full (the colour sensor's full), as the FLOWER pick's does.
         * Until this switch a gather with a full lane sat out the whole dwell (14 Nov 13:13, 4 held, twice).
         */
        public boolean dwellEndsOnFull = true;
        /**
         * Ending the dwell from the intake current, as the FLOWER pick does: off (0) until a GARDEN
         * gather has data for it (1 of 17 dwells on 29 Oct had a spike).
         */
        public double dwellQuietMs = 0;
        public double dwellEmptyMs = 0;
        public double spikeAboveAmps = 1.0;
        /**
         * Forward power during the dwell, to keep the stick seated and the intake pressed into the
         * corner. 0 for none. 1.0 since 14 Nov 2026 (mentor, on 19429 at the field: 8 gathers, 4 in a
         * row; 0.1 before).
         */
        public double dwellPushPower = 1.0;
    }

    /** See {@link Dwell}. */
    public static Dwell dwell = new Dwell();

    /** Steering onto the line into the GARDEN: during the dwell, and during twoStep's push. */
    public static final class Steering {
        /** Turn power per radian of heading error, while dwelling. */
        public double headingKp = 1.0;
        /** Strafe power per inch off the line into the GARDEN, while dwelling. */
        public double lateralKp = 0.05;
        /** The most turn or strafe power either correction may ask for. */
        public double maxCorrection = 0.25;
    }

    /** See {@link Steering}. */
    public static Steering steering = new Steering();

    /** The back out. */
    public static final class Back {
        /**
         * The drive back's speed cap, a fraction of the robot's top path speed (0 to 1]. 0.8 since 6 Nov
         * 2026 (mentor): back out briskly with the stick still down, then stop.
         */
        public double backSpeed = 0.8;
        /**
         * The back out curves too, the drive in run backwards: straight back up the side wall first,
         * then over to where the drive in started, so the stick, still down, does not sweep sideways
         * across the corner on the way out. Off: one straight diagonal back. Untried (6 Nov 2026):
         * flip it at the practice field to compare.
         */
        public boolean curveBackOut = true;
        /** The drive back gives up after this long, ms. */
        public double backTimeoutMs = 4000;
        /**
         * The drive back ends where it is once the robot has got no closer to where the drive in started
         * for this long, ms; 0 waits for the path's end, up to {@link Back#backTimeoutMs}. Why (14 Nov 2026,
         * 19429, battery sagging to 8 V): twice the back out stopped about an inch short and crept there
         * at 1 in/s, 2.4 and 3.7 s in all, against 0.6 to 0.85 s for the other 30. In an Auto that is
         * seconds lost standing. 400 is a plan.
         */
        public double backStallMs = 400;
    }

    /** See {@link Back}. */
    public static Back back = new Back();


    private Garden garden;

    /** The button's gather: lines up first. */
    public GardenGather(Robot robot, Consumer<String> event) {
        this(robot, event, true);
    }

    private GardenGather(Robot robot, Consumer<String> event, boolean lineUp) {
        super(robot, event, lineUp);
    }

    /** An Auto's {@code GardenGather} card: a whole gather from where the robot is, and back there. */
    public static Command command(Robot robot, Consumer<String> event) {
        return GrabRoutine.command(() -> new GardenGather(robot, event, false));
    }

    /**
     * What the drive before an Auto's {@code GardenGather} card does for it: the stick down and the
     * intake on over the last {@link AutoStick#approachLowerWithinIn} of the drive, once the robot's turn is
     * over and it faces the GARDEN the gather will go for from the drive's end
     * ({@link #lowerOnApproach}). Registered with the grab ({@code AutoRegistration}); {@code AutoKit}
     * runs it while that path card drives. Cancelled, the stick comes back up.
     */
    public static AutoRegistry.Approach approach(Robot robot, Consumer<String> event) {
        return new Approach(robot, event);
    }

    private static final class Approach implements AutoRegistry.Approach {
        private final Robot robot;
        private final Consumer<String> event;
        /** The GARDEN the gather after this drive will go for, from where the drive ends; null for none. */
        private Garden garden;
        /** Whether the drive ends facing that GARDEN: one that does not never lowers the stick. */
        private boolean endsFacing;
        /** Since when the robot has faced the GARDEN without a break, System.nanoTime, or 0 while it does not. */
        private long facingSinceNs;
        private boolean lowered;

        Approach(Robot robot, Consumer<String> event) {
            this.robot = robot;
            this.event = event;
        }

        @Override
        public void started(Pose end) {
            garden = nearest(end.x(), end.y(), true);
            endsFacing = garden != null && facing(garden, end.heading());
            facingSinceNs = 0;
            lowered = false;
        }

        @Override
        public void approaching(Pose pose, double remainingIn) {
            if (lowered || garden == null || !endsFacing) return;
            long now = System.nanoTime();
            if (!facing(garden, pose.heading()) || !steady(robot.drive.odometryTurnRadPerSec())) {
                facingSinceNs = 0;
                return;
            }
            if (facingSinceNs == 0) facingSinceNs = now;
            if (!lowerOnApproach(remainingIn, (now - facingSinceNs) / 1e6)) return;
            lowered = true;
            robot.flowerStick.extend();
            robot.intake.intake();
            event.accept(String.format(Locale.US, "GARDEN gather: stick down on the drive in, %.1f in to go", remainingIn));
        }

        @Override
        public void cancel() {
            if (!lowered) return;
            lowered = false;
            robot.flowerStick.retract();
            event.accept("GARDEN gather: not running after this drive, stick up");
        }
    }

    /**
     * Whether the drive in to a gather may lower the stick, {@code remainingIn} from the drive's end,
     * with the robot having faced the GARDEN for {@code facedMs} without a break on a drive that ends
     * facing it: with the stick in use and {@link AutoStick#lowerOnDriveIn} on, within
     * {@link AutoStick#approachLowerWithinIn} of the end, and the facing held {@link AutoStick#facingHoldMs}. The
     * facing rules are what keep the stick off the field wall: it came down on it when lowered while
     * the robot still turned in the corner (30 Oct 2026) and when facing the wrong way (13 Nov).
     */
    static boolean lowerOnApproach(double remainingIn, double facedMs) {
        return stick.useStick && autoStick.lowerOnDriveIn
                && remainingIn <= nn(autoStick.approachLowerWithinIn)
                && facedMs >= nn(autoStick.facingHoldMs);
    }

    /**
     * Whether a robot turning at {@code turnRadPerSec} is steady enough for the stick to come down:
     * slower than {@link Stick#lowerMaxTurnDegPerSec}. An unknown rate (NaN) counts as steady.
     */
    static boolean steady(double turnRadPerSec) {
        return Double.isNaN(turnRadPerSec) || Math.abs(Math.toDegrees(turnRadPerSec)) <= nn(stick.lowerMaxTurnDegPerSec);
    }

    /** Whether a robot at {@code headingRad} faces {@code g} to within {@link AutoStick#approachFacingDeg}. */
    static boolean facing(Garden g, double headingRad) {
        return Math.abs(Math.toDegrees(wrapRad(g.gather.heading() - headingRad))) <= nn(autoStick.approachFacingDeg);
    }

    /**
     * The GARDEN whose gather pose is nearest ({@code x}, {@code y}) within {@link DriveIn#maxRangeIn}, or
     * null; with {@code ownHalfOnly}, only one on the same side of the centre line (an Auto's rule).
     */
    static Garden nearest(double x, double y, boolean ownHalfOnly) {
        Garden nearest = null;
        double best = nn(driveIn.maxRangeIn);
        for (Garden f : GardenFieldPoints.ALL) {
            if (ownHalfOnly && !ownHalf(x, f.gather.x())) continue;
            double range = Math.hypot(f.gather.x() - x, f.gather.y() - y);
            if (range <= best) {
                nearest = f;
                best = range;
            }
        }
        return nearest;
    }

    @Override
    protected String what() {
        return "GARDEN gather";
    }

    @Override
    protected String findTarget(Pose pose) {
        // The nearest in range; in an Auto, only one on the robot's own half (GrabRoutine).
        garden = nearest(pose.x(), pose.y(), inAuto());
        return garden == null ? null : garden.toString();
    }

    @Override
    protected String noTargetWhy() {
        return inAuto()
                ? String.format(Locale.US, "no GARDEN on our side of the centre line within %.0f in (an Auto may not cross it)", driveIn.maxRangeIn)
                : String.format(Locale.US, "no GARDEN within %.0f in", driveIn.maxRangeIn);
    }

    @Override
    protected Pose lineUpPose() {
        // Back is against the heading; out is the robot's left, which faces away from the side wall
        // for both alliances (red faces 270°, left is +x; blue faces 90°, left is -x).
        Pose p = garden.lineUp;
        double h = p.heading();
        double backIn = lineUp.lineUpBackIn;
        double outIn = lineUp.lineUpOutIn;
        return new Pose(p.x() - backIn * Math.cos(h) - outIn * Math.sin(h),
                p.y() - backIn * Math.sin(h) + outIn * Math.cos(h), h);
    }

    @Override
    protected Pose inControl(Pose from, Pose to) {
        return driveIn.curveToWall ? wallControl(from, to) : null;
    }

    @Override
    protected Pose backControl(Pose from, Pose to) {
        // The mirror of the drive in: on the wall's line through where the drive in ended, level
        // with where it started.
        return back.curveBackOut ? wallControl(to, new Pose(from.x(), from.y(), garden.gather.heading())) : null;
    }

    /**
     * The drive in's control point: on the line through {@code to} along its heading (the side wall's
     * line into the corner), level with {@code from}. A curve through it leaves {@code from} toward the
     * wall and arrives at {@code to} running along the wall.
     */
    public static Pose wallControl(Pose from, Pose to) {
        double h = to.heading();
        double along = (from.x() - to.x()) * Math.cos(h) + (from.y() - to.y()) * Math.sin(h);
        return new Pose(to.x() + along * Math.cos(h), to.y() + along * Math.sin(h), h);
    }

    /**
     * With {@link TwoStep#twoStepIn}: the first leg's end, on the gather pose's line along the side wall
     * (not the In's, which is {@link DriveIn#laneTowardWallIn} into the wall), level with {@code from}.
     */
    @Override
    protected Pose inOver(Pose from, Pose to) {
        return twoStep.twoStepIn ? overTo(garden, from) : null;
    }

    /** A two-step drive in's first leg's end at {@code g}, from {@code from}: see {@link #inOver}. */
    static Pose overTo(Garden g, Pose from) {
        return wallControl(from, g.gather);
    }

    @Override
    protected double inPushPower() {
        return twoStep.pushPower;
    }

    @Override
    protected InPlan inPose(Pose from, boolean linedUp) {
        // Deeper is along the heading; toward the wall is the robot's right at the GARDEN heading,
        // the side wall for both alliances (red faces 270°, right is -x; blue faces 90°, right is +x).
        Pose g = garden.gather;
        double h = g.heading();
        double deeper = nn(driveIn.extraDepthIn) + (stick.useStick ? 0.0 : nn(driveIn.noStickExtraDepthIn));
        // A curved drive in reaches the wall's line early: it ends barely toward it, not pinned into it.
        double wall = driveIn.curveToWall ? nn(driveIn.laneTowardWallIn) : nn(driveIn.towardWallIn);
        // Angled into the corner: toward the robot's right, the side wall, so the heading turns clockwise.
        double corner = Math.toRadians(Math.max(-15.0, Math.min(15.0, nn(driveIn.intoCornerDeg))));
        return InPlan.to(new Pose(g.x() + deeper * Math.cos(h) + wall * Math.sin(h),
                g.y() + deeper * Math.sin(h) - wall * Math.cos(h), h - corner));
    }

    @Override
    protected void prep() {
        if (stick.useStick) robot.flowerStick.extend();
        robot.intake.intake();
    }

    /**
     * The stick already down when the gather starts (an Auto's drive lowered it on the way in,
     * {@link #approach}; or the driver has it down): the stick wait counts from then.
     */
    @Override
    protected long readySinceNs() {
        return stick.useStick && robot.flowerStick.extended() ? robot.flowerStick.extendedSinceNs() : 0;
    }

    @Override
    protected boolean steadyForPrep() {
        return !stick.useStick || steady(robot.drive.odometryTurnRadPerSec());
    }

    @Override
    protected void unprep() {
        // With a timed raise the stick stays down the whole way out (stopMechanisms raises it).
        if (!timedRaise()) retractWhenClear();
    }

    @Override
    protected void stopMechanisms() {
        // The drive back done (or stopped while backing out): raise the stick once the robot is still.
        if (timedRaise() && phase() == Phase.BACK) {
            robot.flowerStick.retractAfter(stick.raiseAfterStopMs);
        } else {
            retractWhenClear();
        }
    }

    private static boolean timedRaise() {
        return stick.raiseAfterStopMs >= 0.0;
    }

    /**
     * The stick comes in once the robot is {@link Stick#retractAfterBackIn} from the gather pose, however
     * the gather ends, as the FLOWER pick does it (the stick subsystem holds it down until then).
     */
    private void retractWhenClear() {
        robot.flowerStick.retractWhenClear(garden.gather.x(), garden.gather.y(), nn(stick.retractAfterBackIn));
    }

    @Override protected double prepMs() { return stick.useStick ? stick.stickDownMs : 0.0; }
    @Override protected double settledInPerSec() { return driveIn.settledInPerSec; }
    @Override protected double dwellQuietMs() { return dwell.dwellQuietMs; }
    @Override protected boolean dwellFull() { return dwell.dwellEndsOnFull && robot.colorSensor.full(); }
    @Override protected double dwellEmptyMs() { return dwell.dwellEmptyMs; }
    @Override protected double spikeAboveAmps() { return dwell.spikeAboveAmps; }
    @Override protected double prepWithinIn() { return stick.lowerWithinIn; }
    @Override protected double prepWithinDeg() { return stick.lowerWithinDeg; }
    @Override protected double atLineUpIn() { return lineUp.atLineUpIn; }
    @Override protected double atLineUpDeg() { return lineUp.atLineUpDeg; }
    @Override protected double lineUpTimeoutMs() { return lineUp.lineUpTimeoutMs; }
    @Override protected double inSpeed() { return driveIn.inSpeed; }
    @Override protected double inTimeoutMs() { return driveIn.inTimeoutMs; }
    @Override protected double stallProgressIn() { return driveIn.stallProgressIn; }
    @Override protected double stallMs() { return driveIn.stallMs; }
    @Override protected double dwellMs() { return dwell.dwellMs; }
    @Override protected double dwellPushPower() { return dwell.dwellPushPower; }
    @Override protected double backSpeed() { return back.backSpeed; }
    @Override protected double unprepAfterBackIn() { return stick.retractAfterBackIn; }
    @Override protected double backTimeoutMs() { return back.backTimeoutMs; }
    @Override protected double backStallMs() { return back.backStallMs; }
    @Override protected double headingKp() { return steering.headingKp; }
    @Override protected double lateralKp() { return steering.lateralKp; }
    @Override protected double maxCorrection() { return steering.maxCorrection; }
}
