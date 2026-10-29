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
 * Gathers from the nearest GARDEN: the intake on, a drive in to the GARDEN's gather pose, a dwell,
 * and a drive back to where the drive in started. No stick. The sequence is {@link GrabRoutine}'s,
 * shared with the FLOWER pick; this class says where a GARDEN is and that only the intake runs.
 *
 * <p>For now it is the plainest gather there is, a drive in and back out. A sweep, coming in at an
 * angle, or a helper mechanism is a change here, and every Auto that gathers gets it.
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
    /** The line-up path gives up after this long, ms. */
    public static double lineUpTimeoutMs = 4000;
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
    /** Forward power during the dwell. 0 for none. */
    public static double dwellPushPower = 0.0;
    /** The drive back's speed cap, a fraction of the robot's top path speed (0 to 1]. */
    public static double backSpeed = 0.6;
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
        return garden.lineUp;
    }

    @Override
    protected InPlan inPose(Pose from, boolean linedUp) {
        return InPlan.to(garden.gather);
    }

    @Override
    protected void prep() {
        robot.intake.intake();
    }

    @Override
    protected void unprep() {
        // Nothing to put away: the intake idles when the gather ends.
    }

    @Override
    protected void stopMechanisms() {
        robot.intake.idle();
    }

    @Override protected double prepMs() { return 0.0; }
    @Override protected double prepWithinIn() { return Double.POSITIVE_INFINITY; }
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
    @Override protected double unprepAfterBackIn() { return 0.0; }
    @Override protected double backTimeoutMs() { return backTimeoutMs; }
    @Override protected double headingKp() { return headingKp; }
    @Override protected double lateralKp() { return lateralKp; }
    @Override protected double maxCorrection() { return maxCorrection; }
}
