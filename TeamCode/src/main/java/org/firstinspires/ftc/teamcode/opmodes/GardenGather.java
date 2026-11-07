package org.firstinspires.ftc.teamcode.opmodes;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.ivy.Command;
import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.Robot;
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
     * How much further than the GARDEN's gather pose the drive in aims, inches, along the robot's
     * heading. A drive in that hits the wall first just stops there (it stalls), so too much is
     * safe; tune it in Panels until the gather reaches deep enough.
     */
    public static double extraDepthIn = 0.0;
    /**
     * How much closer to the alliance (side) wall than the gather pose the drive in aims, inches: the
     * intake has to reach all the way into the corner to scoop the whole GARDEN (30 Oct 2026). More
     * than the robot can go is meant: the wall stops it, the drive in ends when it stalls, and the
     * dwell keeps steering it against the wall.
     */
    public static double towardWallIn = 2.0;
    /**
     * An Auto's gather strafes over first: from where the Auto's drive left it, the robot slides
     * sideways onto the GARDEN's line along the side wall, at the depth it is at, lowering the stick
     * on the way, and then drives straight in along that wall ({@link #strafePose}). Off: the drive
     * in is one diagonal from where the card starts. Why (6 Nov 2026, all ten gathers on both robots):
     * on the diagonal the side met the side wall first, the robot pinned itself there, and the drive
     * in stalled with the nose 2 to 4.5 in short of the audience wall. The button's gather lines up
     * on that same line (at the line-up's depth, {@link #lineUpBackIn} back) rather than
     * {@link #lineUpOutIn} out from it, so it too drives straight in: one routine for the button and
     * an Auto's card, tried at the practice field with the button.
     */
    public static boolean strafeFirst = true;
    /**
     * With {@link #strafeFirst}: how much closer to the side wall than the gather pose the straight
     * drive in aims, inches. A little, to keep the side on the wall as it goes in; not the diagonal's
     * {@link #towardWallIn}, which pins it.
     */
    public static double laneTowardWallIn = 0.5;
    /**
     * With {@link #strafeFirst}: the strafe stops at least this far back from the gather pose, inches,
     * so the stick swings down clear of the audience wall, and at most {@link #strafeMaxBackIn}.
     */
    public static double strafeMinBackIn = 8.0;
    /** See {@link #strafeMinBackIn}, inches. */
    public static double strafeMaxBackIn = 18.0;
    /** The strafe gives up after this long, ms: a short move, so a stuck one ends the card early. */
    public static double strafeTimeoutMs = 1500;
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
     * The dwell, ms, intake running, at the end of the drive in: a cap (a full lane ends it sooner).
     * 250 since 6 Nov 2026 (mentor): in, a short dwell, and straight back out, rather than the 1.5 s
     * every gather that night sat for while bringing in 0 to 1 pieces. Tune it at the practice field
     * with the button.
     */
    public static double dwellMs = 250;
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

    @Override
    protected String what() {
        return "GARDEN gather";
    }

    @Override
    protected String findTarget(Pose pose) {
        // The nearest in range; in an Auto, only one on the robot's own half (GrabRoutine).
        garden = null;
        double best = nn(maxRangeIn);
        for (Garden f : GardenFieldPoints.ALL) {
            if (inAuto() && !ownHalf(pose.x(), f.gather.x())) continue;
            double range = Math.hypot(f.gather.x() - pose.x(), f.gather.y() - pose.y());
            if (range <= best) {
                garden = f;
                best = range;
            }
        }
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
        if (strafeFirst) return strafeLineUpPose();
        Pose p = garden.lineUp;
        double h = p.heading();
        double back = lineUpBackIn;
        double out = lineUpOutIn;
        return new Pose(p.x() - back * Math.cos(h) - out * Math.sin(h),
                p.y() - back * Math.sin(h) + out * Math.cos(h), h);
    }

    /**
     * The button's line-up: with {@link #strafeFirst}, onto the side wall's line at the line-up's
     * depth (where an Auto's gather strafes to from GARDEN_STAGING), so the drive in is straight;
     * otherwise {@link #lineUpOutIn} out from it and {@link #lineUpBackIn} back, the old diagonal.
     */
    private Pose strafeLineUpPose() {
        // GardenFieldPoints' line-up is on the gather pose's line already: only back, never out.
        Pose p = garden.lineUp;
        double h = p.heading();
        double back = nn(lineUpBackIn);
        return new Pose(p.x() - back * Math.cos(h), p.y() - back * Math.sin(h), h);
    }

    @Override
    protected InPlan autoLineUp(Pose from) {
        return strafeFirst ? InPlan.to(strafePose(garden, from, nn(strafeMinBackIn), nn(strafeMaxBackIn))) : null;
    }

    @Override protected double autoLineUpTimeoutMs() { return strafeTimeoutMs; }

    /**
     * Where an Auto's gather strafes to: on {@code g}'s line (its gather pose's side-wall distance),
     * facing in, as far back from the gather pose as {@code from} is, held between {@code minIn} and
     * {@code maxIn}. Back is against the GARDEN's heading.
     */
    public static Pose strafePose(Garden g, Pose from, double minIn, double maxIn) {
        Pose p = g.gather;
        double h = p.heading();
        double back = -((from.x() - p.x()) * Math.cos(h) + (from.y() - p.y()) * Math.sin(h));
        double b = Math.max(minIn, Math.min(maxIn, back));
        return new Pose(p.x() - b * Math.cos(h), p.y() - b * Math.sin(h), h);
    }

    @Override
    protected InPlan inPose(Pose from, boolean linedUp) {
        // Deeper is along the heading; toward the wall is the robot's right at the GARDEN heading,
        // the side wall for both alliances (red faces 270°, right is -x; blue faces 90°, right is +x).
        Pose g = garden.gather;
        double h = g.heading();
        double deeper = nn(extraDepthIn);
        // A strafe-first gather (the button's and an Auto's) is on the wall's line already: in
        // straight, barely toward it.
        double wall = strafeFirst ? nn(laneTowardWallIn) : nn(towardWallIn);
        return InPlan.to(new Pose(g.x() + deeper * Math.cos(h) + wall * Math.sin(h),
                g.y() + deeper * Math.sin(h) - wall * Math.cos(h), h));
    }

    @Override
    protected void prep() {
        robot.flowerStick.extend();
        robot.intake.intake();
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

    @Override protected double prepMs() { return stickDownMs; }
    @Override protected double settledInPerSec() { return settledInPerSec; }
    @Override protected double dwellQuietMs() { return dwellQuietMs; }
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
