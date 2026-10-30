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
    /** The dwell, ms, intake running. A cap until pieces can be counted (#181). */
    public static double dwellMs = 1500;
    /** Forward power during the dwell, to keep the stick seated. 0 for none. */
    public static double dwellPushPower = 0.1;
    /** The drive back's speed cap, a fraction of the robot's top path speed (0 to 1]. */
    public static double backSpeed = 0.6;
    /** The stick comes up once the robot has backed out this far from the drive in's end, inches. */
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
        Pose p = garden.lineUp;
        double h = p.heading();
        double back = lineUpBackIn;
        double out = lineUpOutIn;
        return new Pose(p.x() - back * Math.cos(h) - out * Math.sin(h),
                p.y() - back * Math.sin(h) + out * Math.cos(h), h);
    }

    @Override
    protected InPlan inPose(Pose from, boolean linedUp) {
        Pose g = garden.gather;
        double deeper = nn(extraDepthIn);
        return InPlan.to(new Pose(g.x() + deeper * Math.cos(g.heading()), g.y() + deeper * Math.sin(g.heading()),
                g.heading()));
    }

    @Override
    protected void prep() {
        robot.flowerStick.extend();
        robot.intake.intake();
    }

    @Override
    protected void unprep() {
        retractWhenClear();
    }

    @Override
    protected void stopMechanisms() {
        robot.intake.idle();
        retractWhenClear();
    }

    /**
     * The stick comes in once the robot is {@link #retractAfterBackIn} from the gather pose, however
     * the gather ends, as the FLOWER pick does it (the stick subsystem holds it down until then).
     */
    private void retractWhenClear() {
        robot.flowerStick.retractWhenClear(garden.gather.x(), garden.gather.y(), nn(retractAfterBackIn));
    }

    @Override protected double prepMs() { return stickDownMs; }
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
