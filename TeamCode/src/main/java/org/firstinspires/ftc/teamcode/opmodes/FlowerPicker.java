package org.firstinspires.ftc.teamcode.opmodes;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.ivy.Command;
import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.localization.FlowerFieldPoints;
import org.firstinspires.ftc.teamcode.localization.FlowerFieldPoints.Flower;

import java.util.Locale;
import java.util.function.Consumer;

/**
 * Picks the nearest FLOWER: the stick down and the intake on, a push straight in to
 * {@link #contactStandoffIn} from the FLOWER's centre, a dwell, and a back-out to where the push
 * started, the stick coming up on the way. The sequence is {@link GrabRoutine}'s, shared with the
 * GARDEN gather; this class says where a FLOWER is and what the stick does.
 *
 * <p>The one pick routine: the driver's D-pad up runs it while held (BasicDriveTeleOp), lining up
 * first, square to the nearest FLOWER within {@link #maxRangeIn}, {@link #approachStandoffIn} back
 * ({@link FlowerFieldPoints}); an Auto's {@code PickFlower} card runs it to the end ({@link #command})
 * with no line-up, pushing straight ahead from where the Auto's drive left the robot, and refuses
 * when the FLOWER is more than {@link #aheadToleranceIn} off that line. Each phase is timed and
 * written to the match log, so the pick is made faster with numbers rather than by feel.
 *
 * <p>Every number here is a starting point to tune in Panels, not a measurement. The two standoffs
 * come from the Autos' drawings (contact about 12 in from the centre, as R-Quals draws its FLOWER
 * points) and the speeds from nothing at all. The Visualizer's picture of a pick
 * ({@code lib/simple/flowers.ts}) uses the same numbers: change one, change the other.
 */
@Configurable
public final class FlowerPicker extends GrabRoutine {

    /** Only a FLOWER whose centre is this close to the robot is picked, inches. */
    public static double maxRangeIn = 48.0;
    /** The button's line-up: this far back from the FLOWER's centre, inches. */
    public static double approachStandoffIn = 24.0;
    /** Where the push stops: this far back from the FLOWER's centre, inches. */
    public static double contactStandoffIn = 12.0;
    /** The stick goes down and the intake starts once the robot is this close to the line-up, inches. */
    public static double lowerWithinIn = 18.0;
    /** Time the stick is given to swing down before the push may start, ms. */
    public static double stickDownMs = 700;
    /** The line-up path gives up after this long, ms. */
    public static double approachTimeoutMs = 4000;
    /** Already lined up: this close to the line-up pose, inches, skips the line-up path. */
    public static double atApproachIn = 1.5;
    /** Already lined up: this square to the FLOWER, degrees, skips the line-up path. */
    public static double atApproachDeg = 5.0;
    /** An Auto's pick refuses when the FLOWER is more than this far off the robot's line ahead, inches. */
    public static double aheadToleranceIn = 6.0;
    /** The push path's speed cap, a fraction of the robot's top path speed (0 to 1]. */
    public static double pushSpeed = 0.35;
    /** The push gives up after this long, ms. */
    public static double pushTimeoutMs = 2000;
    /** Less progress than this, inches, in {@link #stallMs} means the robot is against the FLOWER. */
    public static double stallProgressIn = 0.25;
    /** See {@link #stallProgressIn}, ms. */
    public static double stallMs = 250;
    /** The dwell, ms. A cap until pieces can be counted (#181). */
    public static double dwellMs = 1500;
    /** Forward power during the dwell, to keep the stick seated. 0 for none. */
    public static double dwellPushPower = 0.1;
    /** The back-out path's speed cap, a fraction of the robot's top path speed (0 to 1]. */
    public static double backSpeed = 0.5;
    /** The stick comes up once the robot has backed out this far, inches. */
    public static double retractAfterBackIn = 6.0;
    /** The back-out gives up after this long, ms. */
    public static double backTimeoutMs = 2000;
    /** Turn power per radian of heading error, while dwelling. */
    public static double headingKp = 1.0;
    /** Strafe power per inch off the line into the FLOWER, while dwelling. */
    public static double lateralKp = 0.05;
    /** The most turn or strafe power either correction may ask for. */
    public static double maxCorrection = 0.25;

    private Flower flower;

    /** The button's pick: lines up first. */
    public FlowerPicker(Robot robot, Consumer<String> event) {
        this(robot, event, true);
    }

    private FlowerPicker(Robot robot, Consumer<String> event, boolean lineUp) {
        super(robot, event, lineUp);
    }

    /** An Auto's {@code PickFlower} card: a whole pick straight ahead from where the robot is. */
    public static Command command(Robot robot, Consumer<String> event) {
        return GrabRoutine.command(() -> new FlowerPicker(robot, event, false));
    }

    @Override
    protected String what() {
        return "FLOWER pick";
    }

    @Override
    protected String findTarget(Pose pose) {
        flower = FlowerFieldPoints.nearest(pose.x(), pose.y(), nn(maxRangeIn));
        return flower == null ? null : flower.toString();
    }

    @Override
    protected String noTargetWhy() {
        return String.format(Locale.US, "no FLOWER within %.0f in", maxRangeIn);
    }

    @Override
    protected Pose lineUpPose() {
        return FlowerFieldPoints.standoff(flower, nn(approachStandoffIn));
    }

    @Override
    protected InPlan inPose(Pose from, boolean linedUp) {
        if (linedUp) return InPlan.to(FlowerFieldPoints.standoff(flower, nn(contactStandoffIn)));
        // An Auto's card: straight ahead from here, to the contact distance from the FLOWER.
        double off = Math.abs(lateralIn(flower.x, flower.y, from.x(), from.y(), from.heading()));
        if (off > nn(aheadToleranceIn)) {
            return InPlan.refuse(String.format(Locale.US,
                    "%s is %.1f in off the line ahead: the Auto's pick spot is not lined up", flower, off));
        }
        Pose contact = aheadTo(from, flower.x, flower.y, nn(contactStandoffIn));
        if (contact == null) {
            return InPlan.refuse(String.format(Locale.US, "%s is not far enough ahead to push to %.0f in from it",
                    flower, contactStandoffIn));
        }
        return InPlan.to(contact);
    }

    @Override
    protected void prep() {
        robot.flowerStick.extend();
        robot.intake.intake();
    }

    @Override
    protected void unprep() {
        robot.flowerStick.retract();
    }

    @Override
    protected void stopMechanisms() {
        robot.intake.idle();
        robot.flowerStick.retract();
    }

    @Override protected double prepMs() { return stickDownMs; }
    @Override protected double prepWithinIn() { return lowerWithinIn; }
    @Override protected double atLineUpIn() { return atApproachIn; }
    @Override protected double atLineUpDeg() { return atApproachDeg; }
    @Override protected double lineUpTimeoutMs() { return approachTimeoutMs; }
    @Override protected double inSpeed() { return pushSpeed; }
    @Override protected double inTimeoutMs() { return pushTimeoutMs; }
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
