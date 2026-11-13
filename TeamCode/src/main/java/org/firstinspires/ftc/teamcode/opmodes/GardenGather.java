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
 * intake on, a drive in to the GARDEN's gather pose (plus {@link #extraDepthIn}), a dwell, and a
 * drive back to where the drive in started, the stick coming up once the robot has backed
 * {@link #retractAfterBackIn} out. The sequence is {@link GrabRoutine}'s, shared with the FLOWER
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
 * {@code AutoRegistry.Approach}): over the last {@link #approachLowerWithinIn} of that drive, once the
 * robot faces the GARDEN to within {@link #lowerWithinDeg}, the stick goes down and the intake starts,
 * so the {@link #stickDownMs} wait has run when the card starts ({@link #readySinceNs}). Measured,
 * 3 to 7 Nov 2026: all nine Auto gathers stood 0.70 to 0.75 s at GARDEN_STAGING for the stick before
 * they moved. The facing rule is the button's own, which has lowered the stick at the same spot since
 * 30 Oct, so a drive that still turns as it arrives (R-NoCross's) lowers it only at the very end.
 *
 * <p><b>What the logs say (13 Nov 2026, the 36 gathers of 1 to 12 Nov on both robots that drove
 * in):</b> with the stick down every drive in stalled short. 32 stopped with the robot's centre 11.5
 * to 14.8 in from the audience wall (median 13.1), 3.6 in short of the gather pose, the old diagonal
 * and the 12 Nov curve alike; the other four stopped further out still. A FLOWER
 * push stalls at the same distance from its wall (10.0 to 10.4 in from a FLOWER that is 2.71 in off
 * it), and on 29 Oct, before the stick, six gathers ran their whole path to the wall. So it is the
 * stick on the wall that stops the robot, with the intake's nose about 3.6 in from the wall and the
 * GARDEN's POLLEN (2.8 in across, against the wall) just out of its reach. The nine Auto gathers
 * brought in 0 to 2 pieces, 1.0 on average, never 3. {@link #useStick} off is the check that decides
 * it (doc/robot-todo.md).
 *
 * <p>Every number here is a starting point to tune in Panels, not a measurement. The Visualizer's
 * picture of a gather ({@code lib/simple/garden.ts}) uses the same numbers: change one, change the
 * other.
 */
@Configurable
public final class GardenGather extends GrabRoutine {

    /**
     * Only a GARDEN this close to the robot is gathered from, inches: the whole of our half, since in
     * an Auto the centre line is the limit that matters.
     */
    public static double maxRangeIn = 160.0;
    /**
     * The line-up spot, moved this much further from the side wall than the GARDEN's line-up pose
     * ({@link GardenFieldPoints}), inches: room to turn in the corner (30 Oct 2026). The drive in
     * then runs at a slant to the gather pose.
     */
    public static double lineUpOutIn = 3.0;
    /** And this much further back from the GARDEN (away from the audience wall), inches. */
    public static double lineUpBackIn = 3.0;
    /** The line-up path gives up after this long, ms. */
    public static double lineUpTimeoutMs = 4000;
    /**
     * The stick goes down and the intake starts once the robot is this close to the line-up,
     * inches, and faces the GARDEN to within {@link #lowerWithinDeg}. The GARDEN is in a corner, so
     * a stick swung down early, while the robot still turns, came down on the wall (30 Oct 2026).
     */
    public static double lowerWithinIn = 6.0;
    /** See {@link #lowerWithinIn}, degrees. */
    public static double lowerWithinDeg = 10.0;
    /** Time the stick is given to swing down before the drive in may start, ms. */
    public static double stickDownMs = 700;
    /**
     * In an Auto, the drive before the gather's card lowers the stick and starts the intake on its way
     * in ({@link #approach}). Off: the card lowers it itself and stands {@link #stickDownMs} first, as
     * every gather did before 13 Nov 2026.
     */
    public static boolean lowerOnDriveIn = true;
    /**
     * With {@link #lowerOnDriveIn}: the stick goes down once the robot is this close to the end of the
     * drive before the card, inches, and faces the GARDEN to within {@link #lowerWithinDeg}. 24 is the
     * FLOWER pick's ({@code FlowerPicker.lowerWithinIn}): a plan, not yet run at the GARDEN. A drive that
     * arrived facing the GARDEN (L-Infraviolet's, 6 and 7 Nov) was 24 in out 0.77 to 0.86 s before its
     * end, enough for the whole {@link #stickDownMs}.
     */
    public static double approachLowerWithinIn = 24.0;
    /**
     * How much further than the GARDEN's gather pose the drive in aims, inches, along the robot's
     * heading. A drive in that hits the wall first just stops there (it stalls), so too much is
     * safe; tune it in Panels until the gather reaches deep enough. 1 since 6 Nov 2026 (mentor), a
     * starting point for the practice field.
     */
    public static double extraDepthIn = 1.0;
    /**
     * How much closer to the alliance (side) wall than the gather pose the drive in aims, inches: the
     * intake has to reach all the way into the corner to scoop the whole GARDEN (30 Oct 2026). More
     * than the robot can go is meant: the wall stops it, the drive in ends when it stalls, and the
     * dwell keeps steering it against the wall.
     */
    public static double towardWallIn = 2.0;
    /**
     * The drive in curves over to the side wall first: from wherever it starts (the button's line-up
     * spot, or GARDEN_STAGING where an Auto's card starts) it is a Pedro curve whose control point is
     * on the side wall's line level with the start ({@link #wallControl}), so the robot moves across
     * to the wall early and runs down it into the corner. Off: one straight diagonal. Why (6 Nov
     * 2026, all ten gathers on both robots): on the diagonal the side met the side wall first, the
     * robot pinned itself there, and the drive in stalled with the nose 2 to 4.5 in short of the
     * audience wall. The line-up spots are unchanged.
     */
    public static boolean curveToWall = true;
    /**
     * With {@link #curveToWall}: how much closer to the side wall than the gather pose the drive in
     * ends, inches. A little, to keep the side on the wall at the end; not the diagonal's
     * {@link #towardWallIn}, which pins it.
     */
    public static double laneTowardWallIn = 0.5;
    /**
     * The back out curves too, the drive in run backwards: straight back up the side wall first,
     * then over to where the drive in started, so the stick, still down, does not sweep sideways
     * across the corner on the way out. Off: one straight diagonal back. Untried (6 Nov 2026):
     * flip it at the practice field to compare.
     */
    public static boolean curveBackOut = true;
    /**
     * The FLOWER stick goes down for the gather. Off: the stick stays in, there is no stick wait,
     * and the drive in aims {@link #noStickExtraDepthIn} deeper, so the intake itself goes all the
     * way to the audience wall. A test (mentor, 6 Nov 2026) of whether the stick helps at all: it
     * went in on 30 Oct because the intake alone did not reach deep enough, but that was the
     * diagonal drive in, which stopped short anyway.
     */
    public static boolean useStick = true;
    /**
     * Without the stick ({@link #useStick} off), how much deeper the drive in aims, inches, on top of
     * {@link #extraDepthIn}: the gather pose puts the nose 1.5 in beyond the robot's front at the
     * wall, and a drive in that meets the wall first just stalls there, so a little more is safe.
     */
    public static double noStickExtraDepthIn = 2.0;
    /**
     * After the dwell, the robot turns in place by {@link #twistDeg} (counterclockwise positive),
     * still pressed toward the audience wall, before it backs out (and turns back on the way), to try
     * rolling balls into the intake (mentor, 6 Nov 2026). Off by default: a test, with the stick or
     * without ({@link #useStick}).
     */
    public static boolean twistOnBackOut = false;
    /** The twist, degrees, counterclockwise positive (the same turn on both alliances). */
    public static double twistDeg = 30.0;
    /** The twist's most turn power, (0, 1]. */
    public static double twistPower = 0.4;
    /** The twist gives up after this long, ms, and backs out from wherever it got to. */
    public static double twistTimeoutMs = 800;
    /** Already lined up: this close to the line-up pose, inches, skips the line-up path. */
    public static double atLineUpIn = 1.5;
    /** Already lined up: this square, degrees, skips the line-up path. */
    public static double atLineUpDeg = 5.0;
    /** The drive in's speed cap, a fraction of the robot's top path speed (0 to 1]. */
    public static double inSpeed = 0.5;
    /** The drive in gives up after this long, ms. */
    public static double inTimeoutMs = 4000;
    /** Less progress than this, inches, in {@link #stallMs} means the robot is against something. */
    public static double stallProgressIn = 0.25;
    /** See {@link #stallProgressIn}, ms. */
    public static double stallMs = 250;
    /**
     * The dwell, ms, intake running, at the end of the drive in: a cap (a full lane ends it sooner,
     * {@link #dwellEndsOnFull}). 400 since 6 Nov 2026 (mentor): in, a short dwell, and straight back
     * out, rather than the 1.5 s every gather that night sat for while bringing in 0 to 1 pieces. Tune
     * it at the practice field with the button. The robot has already stood at the wall 0.27 to 0.47 s
     * (median 0.36) when the dwell starts: that is how long the stall rule ({@link #stallMs}) takes to
     * end the drive in. In the logs a piece that came in was counted 0.25 to 0.5 s after the robot
     * stopped, and a longer dwell brought no more: 1.0 a gather at 1.5 s (9 Auto gathers), against
     * 0 to 2 at 0.4 s (10 button gathers, 12 Nov).
     */
    public static double dwellMs = 400;
    /**
     * The dwell ends as soon as the lane is full (the colour sensor's full), as the FLOWER pick's does.
     * Until 13 Nov 2026 a gather with a full lane sat out the whole dwell (3 Nov 18:18, 4 held: 4.6 s).
     */
    public static boolean dwellEndsOnFull = true;
    /**
     * Ending the dwell from the intake current, as the FLOWER pick does: off (0) until a GARDEN
     * gather has data for it (1 of 17 dwells on 29 Oct had a spike).
     */
    public static double dwellQuietMs = 0;
    public static double dwellEmptyMs = 0;
    public static double spikeAboveAmps = 1.0;
    /**
     * The drive in starts once the robot has slowed below this after the drive before it, inches/s:
     * the gather's hand-over speed. Pedro calls a path done at its parametric end with the robot
     * still rolling (10 to 24 in/s in the 29 Oct to 2 Nov 2026 match logs; under 8 in/s 0.07 to
     * 0.19 s later, under 2 in/s 0.13 to 0.51 s later). The gather drives itself in from the staging
     * spot and ends on the wall, so it need not be still first: 8 since 3 Nov 2026 (mentor), the same
     * as the FLOWER pick's, from the 2 a launch waits for ({@code PathEndSettle}).
     */
    public static double settledInPerSec = 8.0;
    /** Forward power during the dwell, to keep the stick seated. 0 for none. */
    public static double dwellPushPower = 0.1;
    /**
     * The drive back's speed cap, a fraction of the robot's top path speed (0 to 1]. 0.8 since 6 Nov
     * 2026 (mentor): back out briskly with the stick still down, then stop.
     */
    public static double backSpeed = 0.8;
    /**
     * The stick comes up this long after the drive back has stopped, ms: it stays down the whole way
     * out and rises only once the robot is still (mentor, 6 Nov 2026), so it does not drag pieces out
     * with it. NaN or negative: the old rule, up once the robot has backed {@link #retractAfterBackIn}.
     * A gather stopped before its drive back (let go, cut) still uses the old rule.
     */
    public static double raiseAfterStopMs = 250;
    /** Without {@link #raiseAfterStopMs}: the stick comes up once the robot has backed out this far from the drive in's end, inches. */
    public static double retractAfterBackIn = 6.0;
    /** The drive back gives up after this long, ms. */
    public static double backTimeoutMs = 4000;
    /** Turn power per radian of heading error, while dwelling. */
    public static double headingKp = 1.0;
    /** Strafe power per inch off the line into the GARDEN, while dwelling. */
    public static double lateralKp = 0.05;
    /** The most turn or strafe power either correction may ask for. */
    public static double maxCorrection = 0.25;

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
     * intake on over the last {@link #approachLowerWithinIn} of the drive, once the robot faces the
     * GARDEN the gather will go for from the drive's end ({@link #lowerOnApproach}). Registered with
     * the grab ({@code AutoRegistration}); {@code AutoKit} runs it while that path card drives.
     * Cancelled, the stick comes back up.
     */
    public static AutoRegistry.Approach approach(Robot robot, Consumer<String> event) {
        return new Approach(robot, event);
    }

    private static final class Approach implements AutoRegistry.Approach {
        private final Robot robot;
        private final Consumer<String> event;
        /** The GARDEN the gather after this drive will go for, from where the drive ends; null for none. */
        private Garden garden;
        private boolean lowered;

        Approach(Robot robot, Consumer<String> event) {
            this.robot = robot;
            this.event = event;
        }

        @Override
        public void started(Pose end) {
            garden = nearest(end.x(), end.y(), true);
            lowered = false;
        }

        @Override
        public void approaching(Pose pose, double remainingIn) {
            if (lowered || garden == null || !lowerOnApproach(garden, pose, remainingIn)) return;
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
     * Whether the drive in to a gather from {@code g} may lower the stick with the robot at
     * {@code pose}, {@code remainingIn} from the drive's end: with the stick in use and
     * {@link #lowerOnDriveIn} on, within {@link #approachLowerWithinIn} of the end, and facing the
     * GARDEN to within {@link #lowerWithinDeg}. The facing rule is what keeps the stick off the side
     * wall: swung down while the robot still turned in the corner, it came down on it (30 Oct 2026).
     */
    static boolean lowerOnApproach(Garden g, Pose pose, double remainingIn) {
        return useStick && lowerOnDriveIn
                && remainingIn <= nn(approachLowerWithinIn)
                && Math.abs(Math.toDegrees(wrapRad(g.gather.heading() - pose.heading()))) <= nn(lowerWithinDeg);
    }

    /**
     * The GARDEN whose gather pose is nearest ({@code x}, {@code y}) within {@link #maxRangeIn}, or
     * null; with {@code ownHalfOnly}, only one on the same side of the centre line (an Auto's rule).
     */
    static Garden nearest(double x, double y, boolean ownHalfOnly) {
        Garden nearest = null;
        double best = nn(maxRangeIn);
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
                ? String.format(Locale.US, "no GARDEN on our side of the centre line within %.0f in (an Auto may not cross it)", maxRangeIn)
                : String.format(Locale.US, "no GARDEN within %.0f in", maxRangeIn);
    }

    @Override
    protected Pose lineUpPose() {
        // Back is against the heading; out is the robot's left, which faces away from the side wall
        // for both alliances (red faces 270°, left is +x; blue faces 90°, left is -x).
        Pose p = garden.lineUp;
        double h = p.heading();
        double back = lineUpBackIn;
        double out = lineUpOutIn;
        return new Pose(p.x() - back * Math.cos(h) - out * Math.sin(h),
                p.y() - back * Math.sin(h) + out * Math.cos(h), h);
    }

    @Override
    protected Pose inControl(Pose from, Pose to) {
        return curveToWall ? wallControl(from, to) : null;
    }

    @Override
    protected Pose backControl(Pose from, Pose to) {
        // The mirror of the drive in: on the wall's line through where the drive in ended, level
        // with where it started.
        return curveBackOut ? wallControl(to, new Pose(from.x(), from.y(), garden.gather.heading())) : null;
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

    @Override
    protected InPlan inPose(Pose from, boolean linedUp) {
        // Deeper is along the heading; toward the wall is the robot's right at the GARDEN heading,
        // the side wall for both alliances (red faces 270°, right is -x; blue faces 90°, right is +x).
        Pose g = garden.gather;
        double h = g.heading();
        double deeper = nn(extraDepthIn) + (useStick ? 0.0 : nn(noStickExtraDepthIn));
        // A curved drive in reaches the wall's line early: it ends barely toward it, not pinned into it.
        double wall = curveToWall ? nn(laneTowardWallIn) : nn(towardWallIn);
        return InPlan.to(new Pose(g.x() + deeper * Math.cos(h) + wall * Math.sin(h),
                g.y() + deeper * Math.sin(h) - wall * Math.cos(h), h));
    }

    @Override
    protected void prep() {
        if (useStick) robot.flowerStick.extend();
        robot.intake.intake();
    }

    /**
     * The stick already down when the gather starts (an Auto's drive lowered it on the way in,
     * {@link #approach}; or the driver has it down): the stick wait counts from then.
     */
    @Override
    protected long readySinceNs() {
        return useStick && robot.flowerStick.extended() ? robot.flowerStick.extendedSinceNs() : 0;
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
            robot.flowerStick.retractAfter(raiseAfterStopMs);
        } else {
            retractWhenClear();
        }
    }

    private static boolean timedRaise() {
        return raiseAfterStopMs >= 0.0;
    }

    /**
     * The stick comes in once the robot is {@link #retractAfterBackIn} from the gather pose, however
     * the gather ends, as the FLOWER pick does it (the stick subsystem holds it down until then).
     */
    private void retractWhenClear() {
        robot.flowerStick.retractWhenClear(garden.gather.x(), garden.gather.y(), nn(retractAfterBackIn));
    }

    @Override protected double backTwistDeg() { return twistOnBackOut ? twistDeg : 0.0; }
    @Override protected double twistPower() { return twistPower; }
    @Override protected double twistTimeoutMs() { return twistTimeoutMs; }
    @Override protected double prepMs() { return useStick ? stickDownMs : 0.0; }
    @Override protected double settledInPerSec() { return settledInPerSec; }
    @Override protected double dwellQuietMs() { return dwellQuietMs; }
    @Override protected boolean dwellFull() { return dwellEndsOnFull && robot.colorSensor.full(); }
    @Override protected double dwellEmptyMs() { return dwellEmptyMs; }
    @Override protected double spikeAboveAmps() { return spikeAboveAmps; }
    @Override protected double prepWithinIn() { return lowerWithinIn; }
    @Override protected double prepWithinDeg() { return lowerWithinDeg; }
    @Override protected double atLineUpIn() { return atLineUpIn; }
    @Override protected double atLineUpDeg() { return atLineUpDeg; }
    @Override protected double lineUpTimeoutMs() { return lineUpTimeoutMs; }
    @Override protected double inSpeed() { return inSpeed; }
    @Override protected double inTimeoutMs() { return inTimeoutMs; }
    @Override protected double stallProgressIn() { return stallProgressIn; }
    @Override protected double stallMs() { return stallMs; }
    @Override protected double dwellMs() { return dwellMs; }
    @Override protected double dwellPushPower() { return dwellPushPower; }
    @Override protected double backSpeed() { return backSpeed; }
    @Override protected double unprepAfterBackIn() { return retractAfterBackIn; }
    @Override protected double backTimeoutMs() { return backTimeoutMs; }
    @Override protected double headingKp() { return headingKp; }
    @Override protected double lateralKp() { return lateralKp; }
    @Override protected double maxCorrection() { return maxCorrection; }
}
