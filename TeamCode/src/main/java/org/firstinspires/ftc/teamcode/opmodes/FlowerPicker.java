package org.firstinspires.ftc.teamcode.opmodes;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.ivy.Command;
import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.autokit.AutoRegistry;
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
 * <p><b>In an Auto the stick comes down on the drive in.</b> The path card before a {@code PickFlower}
 * card runs this pick's {@link #approach} ({@code AutoKit}, {@code AutoRegistry.Approach}): over the
 * last {@link #lowerWithinIn} of that drive the stick goes down and the intake starts, as they would
 * at the pick's own line-up, so when the card starts the {@link #stickDownMs} wait has usually run
 * out already ({@link #readySinceNs}). Measured 29 Oct to 2 Nov 2026: the push started 0.70 to
 * 0.73 s after the drive ended, all of it the stick wait. The pick itself is unchanged: square up,
 * push to contact, dwell, back out (mentor, 3 Nov 2026: the two stay separate cards).
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
    /**
     * Where the push aims: this far back from the FLOWER's centre, inches. The push also ends when the
     * robot stalls against the FLOWER. The 29 Oct 2026 logs on 20245 put that first touch at 12.2 to
     * 12.5 in (as odometry had it), so a target of 12.0 ends "reached" with the stick short of the
     * FLOWER whenever the pose is a fraction of an inch long, and on 3 Nov an Auto's FAR pick did
     * exactly that (camera 1.8 in further out than odometry, intake current flat, nothing taken). Pushes
     * aimed at 5.0 and at 7.0 on the red WALL FLOWER both stalled at 10.0 to 10.4 in from the centre
     * (3 Nov), so any target under about 9 ends the same way, by stall, and 7.0 leaves about 3 in of grace
     * for a long pose. At the target the face is about 1.6 in from the wall (7.0 less the frame's 8.1
     * half, plus the FLOWER's 2.71 off the wall), so a pose more than that short drives the face into
     * the wall (by odometry, 5 to 6 did on blue). The intake roller stalls (0 ticks/s at 8 to 10 A, 1 to 4 s) in
     * launch feeds at any value, so it is not this number's doing. Three picks per value is the check
     * (doc/robot-todo.md); the pick's start line in the match log says the value in use. The
     * Visualizer's {@code CONTACT_STANDOFF_IN} changes with it.
     */
    public static double contactStandoffIn = 7.0;
    /**
     * The stick goes down and the intake starts once the robot is this close to the line-up, inches:
     * the button's line-up, or, in an Auto, the end of the drive before the pick ({@link #approach}).
     */
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
     * The dwell's cap, ms. It normally ends sooner: when the lane is full ({@link #dwellEndsOnFull}),
     * or when the driver lets go. On 29 Oct the last piece came as late as 1.55 s into a 1.5 s
     * dwell, so the cap sits above that; a FLOWER that does not fill the lane (empty, or the robot
     * already held some) dwells the whole cap.
     */
    public static double dwellMs = 2000;
    /**
     * The dwell ends the moment the lane is full ({@code ColorSensorSubsystem.full()}, the colour
     * sensor's full: the signal the full hold, the light and an Auto's {@code IntakeFull} use), since
     * 3 Nov 2026 (mentor: "we have an intake full sensor now"). A full lane takes nothing more, so
     * dwelling on is time lost. In the 29 Oct to 2 Nov 2026 logs on 20245 the last piece's current
     * spike came a median 1.5 s after the push started (0.64 s into the dwell, latest 1.35 s; 33
     * picks that took pieces), and full latches 0.3 to 1.0 s after a piece stops under the sensor
     * ({@code ColorSensorSubsystem.fullHoldS}); the one pick that filled the lane (2 Nov) was full
     * 1.43 s after its push started, 0.44 s into the dwell. False: the dwell runs to {@link #dwellMs}
     * as before.
     */
    public static boolean dwellEndsOnFull = true;
    /**
     * The dwell ends this long after the intake current's last spike, ms (0: off). <b>Off</b> since
     * 1 Nov 2026, the mentor's call: on 20245 the pick barely dwelt at all. A spike during the push
     * counts too, so a push that ran longer than this after loading the roller ended the dwell the
     * moment it began. The full sensor ({@link #dwellEndsOnFull}) is the end now. (The longest gap
     * between two pieces' spikes on 29 Oct was 0.55 s, the 600 it was.)
     */
    public static double dwellQuietMs = 0;
    /**
     * The dwell ends if no piece has spiked the current this far in, ms (0: off). The latest first
     * piece on 29 Oct came 1.22 s in.
     */
    public static double dwellEmptyMs = 1300;
    /** A current this many amps over the roller's free-running 0.35 A is a piece (they spike 5–6 A). */
    public static double spikeAboveAmps = 1.0;
    /**
     * The push starts once the robot has slowed below this after the drive before it, inches/s: the
     * pick's hand-over speed. Pedro calls a path done at its parametric end and brakes in HOLD, with
     * the robot still rolling: 10 to 24 in/s in the 29 Oct to 2 Nov 2026 match logs, under 8 in/s
     * 0.07 to 0.19 s later, under 2 in/s 0.13 to 0.51 s later. A pick need not be still, it squares
     * up on its own and the push ends on contact, so 8 since 3 Nov 2026 (mentor), from the 2 a launch
     * waits for ({@code PathEndSettle}). Capped by {@code GrabRoutine.SETTLE_MAX_MS} either way.
     */
    public static double settledInPerSec = 8.0;
    /** Forward power during the dwell, to keep the stick seated. 0 for none. */
    public static double dwellPushPower = 0.1;
    /** The back-out path's speed cap, a fraction of the robot's top path speed (0 to 1]. */
    public static double backSpeed = 0.5;
    /**
     * The stick comes up once the robot has backed out this far, inches: on the back-out, and also
     * whenever a pick ends early (button let go, sticks moved, a timeout, an Auto's card cut short).
     * Either way it waits until the robot's centre is this much further from the FLOWER's centre
     * than the nearest the push got (never nearer than {@link #contactStandoffIn}), so it never
     * swings up while still in the FLOWER, however deep the push aimed or how short it stopped.
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

    /**
     * What the drive before an Auto's {@code PickFlower} card does for it: the stick down and the
     * intake on over the last {@link #lowerWithinIn} of the drive, for the FLOWER the pick will go
     * for from the drive's end ({@link #lowerOnApproach}). Registered with the grab
     * ({@code AutoRegistration}); {@code AutoKit} runs it while that path card drives. Cancelled, the
     * stick comes back up.
     */
    public static AutoRegistry.Approach approach(Robot robot, Consumer<String> event) {
        return new Approach(robot, event);
    }

    private static final class Approach implements AutoRegistry.Approach {
        private final Robot robot;
        private final Consumer<String> event;
        /** The FLOWER the pick after this drive will go for, from where the drive ends; null for none. */
        private Flower flower;
        private boolean lowered;

        Approach(Robot robot, Consumer<String> event) {
            this.robot = robot;
            this.event = event;
        }

        @Override
        public void started(Pose end) {
            flower = nearest(end.x(), end.y(), true);
            lowered = false;
        }

        @Override
        public void approaching(Pose pose, double remainingIn) {
            if (lowered || flower == null || !lowerOnApproach(flower, pose, remainingIn)) return;
            lowered = true;
            robot.flowerStick.extend();
            robot.intake.intake();
            event.accept(String.format(Locale.US, "FLOWER pick: stick down on the drive in, %.1f in to go", remainingIn));
        }

        @Override
        public void cancel() {
            if (!lowered) return;
            lowered = false;
            robot.flowerStick.retract();
            event.accept("FLOWER pick: not running after this drive, stick up");
        }
    }

    /**
     * Whether the drive in to a pick of {@code f} may lower the stick with the robot at {@code pose},
     * {@code remainingIn} from the drive's end: within {@link #lowerWithinIn} of it, and no closer to
     * the FLOWER than the stick swings down clear ({@link #mayPrepAt}'s rule). Any heading: a FLOWER
     * is in open field.
     */
    static boolean lowerOnApproach(Flower f, Pose pose, double remainingIn) {
        return remainingIn <= nn(lowerWithinIn)
                && Math.hypot(f.x - pose.x(), f.y - pose.y()) >= nn(minAutoStartIn) - 0.5;
    }

    /**
     * The FLOWER nearest ({@code x}, {@code y}) within {@link #maxRangeIn}, or null; with
     * {@code ownHalfOnly}, only one on the same side of the centre line (an Auto's rule).
     */
    static Flower nearest(double x, double y, boolean ownHalfOnly) {
        Flower nearest = null;
        double best = nn(maxRangeIn);
        for (Flower f : FlowerFieldPoints.ALL) {
            if (ownHalfOnly && !ownHalf(x, f.x)) continue;
            double range = Math.hypot(f.x - x, f.y - y);
            if (range <= best) {
                nearest = f;
                best = range;
            }
        }
        return nearest;
    }

    @Override
    protected String what() {
        return "FLOWER pick";
    }

    /** How deep this push aims, as it is now: Panels can change it after the log's metadata is written. */
    @Override
    protected String startNote() {
        return String.format(Locale.US, ", contact %.1f in", nn(contactStandoffIn));
    }

    @Override
    protected String findTarget(Pose pose) {
        // The nearest in range; in an Auto, only one on the robot's own half (GrabRoutine).
        flower = nearest(pose.x(), pose.y(), inAuto());
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

    /** A stick already down (lowered on the drive in, or by the driver) has had its time since it was asked out. */
    @Override
    protected long readySinceNs() {
        return robot.flowerStick.extended() ? robot.flowerStick.extendedSinceNs() : 0;
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
     * let go, or cut short. Clear is {@link #retractAfterBackIn} out from the nearest the push got
     * to the FLOWER (where it stalled, or where the robot is if cut during the push), and never
     * nearer than {@link #contactStandoffIn} plus that: a push aimed deeper than the FLOWER lets the
     * robot in, and a pose that stops it short does not bring the lift-off point in with it. The
     * stick subsystem holds it down until then, after this pick is over.
     */
    private void retractWhenClear() {
        double contact = nn(contactStandoffIn);
        double nearest = Math.max(contact, closestOnInIn(flower.x, flower.y, contact));
        robot.flowerStick.retractWhenClear(flower.x, flower.y, nearest + nn(retractAfterBackIn));
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
    @Override protected boolean dwellFull() { return dwellEndsOnFull && robot.colorSensor.full(); }
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
