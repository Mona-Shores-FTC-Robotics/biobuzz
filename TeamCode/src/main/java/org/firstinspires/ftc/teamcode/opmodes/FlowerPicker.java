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
 * <p>The one pick routine: the driver's FLOWER pick button runs it while held (BasicDriveTeleOp;
 * the button is in {@code controls/ControlScheme}), lining up first, square to the nearest FLOWER within {@link #maxRangeIn}, {@link #approachStandoffIn} back
 * ({@link FlowerFieldPoints}); an Auto's {@code PickFlower} card runs it to the end ({@link #command}),
 * squaring up first ({@link #squareUp}): onto the FLOWER's line, facing it, at the distance the
 * Auto's drive left the robot (held between {@link #minAutoStartIn} and {@link #approachStandoffIn}),
 * so the push always goes in along the FLOWER's line however the Auto's drawing approached it. It
 * refuses a spot more than {@link #autoMaxOffIn} off that line or {@link #autoMaxOffDeg} from facing
 * the FLOWER: that is a drawing error, not something to drive round. The card ends at the squared-up
 * spot. The square-up turns to the FLOWER's facing as odometry knows it, so a heading error in the
 * pose is a crooked push. Each phase is timed and written to the match log, so the pick is made
 * faster with numbers rather than by feel.
 *
 * <p>Every number here is a starting point to tune in Panels, not a measurement. The two standoffs
 * come from the Autos' drawings (contact about 12 in from the centre, as R-Quals draws its FLOWER
 * points) and the speeds from nothing at all. The Visualizer's picture of a pick
 * ({@code lib/simple/flowers.ts}) uses the same numbers: change one, change the other.
 */
@Configurable
public final class FlowerPicker extends GrabRoutine {

    /** Only a FLOWER whose centre is this close to the robot is picked, inches. */
    public static double maxRangeIn = 60.0;
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
    /**
     * An Auto's square-up never starts the push closer than this to the FLOWER's centre, inches, and
     * the stick goes down only once the robot is this far out, less 0.5 in for pose noise (a robot
     * closer than that is never "already square": it moves out first). The Autos draw their pick
     * spots about 19.5 in out, where the stick has swung down clear.
     */
    public static double minAutoStartIn = 18.0;
    /** An Auto's pick refuses a spot more than this far off the FLOWER's line, inches. */
    public static double autoMaxOffIn = 12.0;
    /** An Auto's pick refuses a spot facing more than this far from the FLOWER's facing, degrees. */
    public static double autoMaxOffDeg = 30.0;
    /** An Auto's square-up gives up after this long, ms: a short move, so a stuck one ends the card early. */
    public static double autoSquareTimeoutMs = 1500;
    /**
     * The push path's speed cap, a fraction of the robot's top path speed (0 to 1]. 0.5 since
     * 30 Oct 2026: at 0.35 the drive team found the approach too slow on 20245; 0.5 was best.
     */
    public static double pushSpeed = 0.5;
    /** The push gives up after this long, ms. */
    public static double pushTimeoutMs = 2000;
    /** Less progress than this, inches, in {@link #stallMs} means the robot is against the FLOWER. */
    public static double stallProgressIn = 0.25;
    /** See {@link #stallProgressIn}, ms. */
    public static double stallMs = 250;
    /**
     * The dwell's cap, ms. It normally ends sooner, from the intake current ({@link #dwellQuietMs},
     * {@link #dwellEmptyMs}), or when the driver lets go. On 29 Oct the last piece came as late
     * as 1.55 s into a 1.5 s dwell, so the cap sits above that.
     */
    public static double dwellMs = 2000;
    /**
     * The dwell ends this long after the intake current's last spike, ms (0: off). The longest
     * gap between two pieces' spikes on 29 Oct was 0.55 s.
     */
    public static double dwellQuietMs = 600;
    /**
     * The dwell ends if no piece has spiked the current this far in, ms (0: off). The latest first
     * piece on 29 Oct came 1.22 s in.
     */
    public static double dwellEmptyMs = 1300;
    /** A current this many amps over the roller's free-running 0.35 A is a piece (they spike 5–6 A). */
    public static double spikeAboveAmps = 1.0;
    /** The push starts once the robot has slowed below this after the line-up, inches/s. */
    public static double settledInPerSec = 2.0;
    /** Forward power during the dwell, to keep the stick seated. 0 for none. */
    public static double dwellPushPower = 0.1;
    /** The back-out path's speed cap, a fraction of the robot's top path speed (0 to 1]. */
    public static double backSpeed = 0.5;
    /**
     * The stick comes up once the robot has backed out this far, inches: on the back-out, and also
     * whenever a pick ends early (button let go, sticks moved, a timeout, an Auto's card cut short).
     * Either way it waits until the robot's centre is {@link #contactStandoffIn} plus this from the
     * FLOWER's centre, so it never swings up while still in the FLOWER.
     */
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

    /** An Auto's {@code PickFlower} card: a whole pick, squared up from where the robot is. */
    public static Command command(Robot robot, Consumer<String> event) {
        return GrabRoutine.command(() -> new FlowerPicker(robot, event, false));
    }

    @Override
    protected String what() {
        return "FLOWER pick";
    }

    @Override
    protected String findTarget(Pose pose) {
        // The nearest in range; in an Auto, only one on the robot's own half (GrabRoutine).
        flower = null;
        double best = nn(maxRangeIn);
        for (Flower f : FlowerFieldPoints.ALL) {
            if (inAuto() && !ownHalf(pose.x(), f.x)) continue;
            double range = Math.hypot(f.x - pose.x(), f.y - pose.y());
            if (range <= best) {
                flower = f;
                best = range;
            }
        }
        return flower == null ? null : flower.toString();
    }

    @Override
    protected String noTargetWhy() {
        return inAuto()
                ? String.format(Locale.US, "no FLOWER on our side of the centre line within %.0f in (an Auto may not cross it)", maxRangeIn)
                : String.format(Locale.US, "no FLOWER within %.0f in", maxRangeIn);
    }

    @Override
    protected Pose lineUpPose() {
        return FlowerFieldPoints.standoff(flower, nn(approachStandoffIn));
    }

    @Override
    protected InPlan inPose(Pose from, boolean linedUp) {
        // Both the button and an Auto's card are square to the FLOWER by now: push in along its line.
        return InPlan.to(FlowerFieldPoints.standoff(flower, nn(contactStandoffIn)));
    }

    @Override
    protected InPlan autoLineUp(Pose from) {
        String refusal = squareUpRefusal(flower, from, nn(autoMaxOffIn), nn(autoMaxOffDeg));
        return refusal != null ? InPlan.refuse(refusal)
                : InPlan.to(squareUpPose(flower, from, nn(minAutoStartIn), nn(approachStandoffIn)));
    }

    /**
     * Why an Auto's pick refuses to square up to {@code f} from {@code from}, or null when it may:
     * {@code from} is not in front of the FLOWER, is more than {@code maxOffIn} off its line, or faces
     * more than {@code maxOffDeg} from its facing. Also the simulator's rule ({@code AutoSim}).
     */
    public static String squareUpRefusal(Flower f, Pose from, double maxOffIn, double maxOffDeg) {
        double back = -alongTrackIn(from.x(), from.y(), f.x, f.y, f.facingRad);
        double off = Math.abs(lateralIn(from.x(), from.y(), f.x, f.y, f.facingRad));
        double turn = Math.abs(Math.toDegrees(wrapRad(from.heading() - f.facingRad)));
        if (!(back > 0.0)) {
            return String.format(Locale.US, "the Auto's pick spot is not in front of %s: nothing to push into", f);
        }
        if (!(off <= maxOffIn)) {
            return String.format(Locale.US,
                    "the Auto's pick spot is %.1f in off %s's line (at most %.0f): draw it closer", off, f, maxOffIn);
        }
        if (!(turn <= maxOffDeg)) {
            return String.format(Locale.US,
                    "the Auto's pick spot faces %.0f° from %s (at most %.0f°): draw it facing the FLOWER", turn, f, maxOffDeg);
        }
        return null;
    }

    /**
     * Where an Auto's pick squares up to: on {@code f}'s line, facing it, as far back from its centre
     * as {@code from} is, held between {@code minIn} and {@code maxIn}. Also the simulator's rule.
     */
    public static Pose squareUpPose(Flower f, Pose from, double minIn, double maxIn) {
        double back = -alongTrackIn(from.x(), from.y(), f.x, f.y, f.facingRad);
        return FlowerFieldPoints.standoff(f, Math.max(minIn, Math.min(maxIn, back)));
    }

    @Override
    protected boolean mayPrepAt(Pose pose) {
        // The button's line-up is never closer than approachStandoffIn; an Auto's may start closer, and
        // the stick swings down only once the robot is out where it swings clear (0.5 in for noise).
        return !inAuto() || Math.hypot(flower.x - pose.x(), flower.y - pose.y()) >= nn(minAutoStartIn) - 0.5;
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
     * The stick comes in once the robot is clear of the FLOWER, however the pick ends: finished,
     * let go, or cut short. The stick subsystem holds it down until then, after this pick is over.
     */
    private void retractWhenClear() {
        robot.flowerStick.retractWhenClear(flower.x, flower.y, nn(contactStandoffIn) + nn(retractAfterBackIn));
    }

    @Override protected double prepMs() { return stickDownMs; }
    @Override protected double prepWithinIn() { return lowerWithinIn; }
    @Override protected double prepWithinDeg() { return 180.0; } // any heading: the FLOWER is in open field
    @Override protected double atLineUpIn() { return atApproachIn; }
    @Override protected double atLineUpDeg() { return atApproachDeg; }
    @Override protected double lineUpTimeoutMs() { return approachTimeoutMs; }
    @Override protected double autoLineUpTimeoutMs() { return autoSquareTimeoutMs; }
    @Override protected double inSpeed() { return pushSpeed; }
    @Override protected double inTimeoutMs() { return pushTimeoutMs; }
    @Override protected double stallProgressIn() { return stallProgressIn; }
    @Override protected double stallMs() { return stallMs; }
    @Override protected double dwellMs() { return dwellMs; }
    @Override protected double dwellQuietMs() { return dwellQuietMs; }
    @Override protected double dwellEmptyMs() { return dwellEmptyMs; }
    @Override protected double spikeAboveAmps() { return spikeAboveAmps; }
    @Override protected double settledInPerSec() { return settledInPerSec; }
    @Override protected double dwellPushPower() { return dwellPushPower; }
    @Override protected double backSpeed() { return backSpeed; }
    @Override protected double unprepAfterBackIn() { return retractAfterBackIn; }
    @Override protected double backTimeoutMs() { return backTimeoutMs; }
    @Override protected double headingKp() { return headingKp; }
    @Override protected double lateralKp() { return lateralKp; }
    @Override protected double maxCorrection() { return maxCorrection; }
}
