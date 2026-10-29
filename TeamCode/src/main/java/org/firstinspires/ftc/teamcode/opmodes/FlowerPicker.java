package org.firstinspires.ftc.teamcode.opmodes;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.api.Paths;
import com.pedropathing.config.Modifier;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.CommandBuilder;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.pedropathing.paths.interpolator.Interpolator;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.localization.FlowerFieldPoints;
import org.firstinspires.ftc.teamcode.localization.FlowerFieldPoints.Flower;
import org.firstinspires.ftc.teamcode.pedro.Constants;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Picks the nearest FLOWER: line up square to it, lower the stick and run the intake on the way in,
 * drive in, dwell, back out, raise the stick. The one pick routine: the driver's D-pad up runs it
 * while held (BasicDriveTeleOp), and an Auto's {@code PickFlower} card runs it to the end
 * ({@link #command}). The same pick every time, each phase timed and written to the match log, so
 * it is made faster with numbers rather than by feel.
 *
 * <p><b>Its contract: it ends where it starts.</b> Started at the approach pose ({@link
 * #approachStandoffIn} back from the FLOWER, square to it), which is where an Auto's drive to a
 * pick spot leaves the robot, it backs out to that same pose. So the step after a pick starts from
 * the pick spot, and the Visualizer draws the pick as it is: in along the FLOWER's line to
 * {@link #contactStandoffIn}, a dwell, and back out.
 *
 * <p>On the button: hold to run. Letting go, or moving a drive stick, stops it where it is: the drive stops, the
 * intake idles, the stick comes up, and the sticks have the robot again on the next loop. When it
 * finishes it stays finished until the button is let go and pressed again.
 *
 * <h2>The phases</h2>
 *
 * <ol>
 *   <li><b>Approach.</b> A Pedro path from wherever the robot is to {@link #approachStandoffIn} back
 *       from the nearest FLOWER within {@link #maxRangeIn}, square to its wall and facing it
 *       ({@link FlowerFieldPoints}). Skipped when the robot is already there (within
 *       {@link #atApproachIn} and {@link #atApproachDeg}), as after an Auto's drive to a pick spot.
 *       The stick goes down and the intake starts once the robot is within {@link #lowerWithinIn}
 *       of the contact point, so the stick's swing overlaps the drive.</li>
 *   <li><b>Stick.</b> If the stick has not had {@link #stickDownMs} to swing down by the end of the
 *       approach, the robot holds the approach pose until it has. A long wait here says the stick
 *       should go down earlier.</li>
 *   <li><b>Push.</b> A Pedro path straight in along the FLOWER's line to {@link #contactStandoffIn}
 *       from its centre, capped at {@link #pushSpeed} of the robot's top speed, heading held. It
 *       ends when the path does, or when the robot stops making progress (it is against the
 *       FLOWER), or at {@link #pushTimeoutMs}.</li>
 *   <li><b>Dwell.</b> {@link #dwellMs} with the intake running and a light push, a cap: nothing here
 *       can count pieces yet (#181).</li>
 *   <li><b>Back.</b> A Pedro path straight back out to the approach pose, capped at
 *       {@link #backSpeed}; the stick comes up once the robot has backed {@link #retractAfterBackIn}.
 *       Then the intake idles.</li>
 * </ol>
 *
 * <p>It refuses, and says why, without a Pinpoint, without a field-referenced pose
 * ({@code robot.drive.poseReferenced()}: an Auto's handoff or a camera seed or relocalize), with no
 * FLOWER in range, or on a robot whose path follower cannot be built (no Foresight block yet).
 *
 * <p>It drives the robot by calling {@code robot.drive} each loop, as the sticks and the Auto's paths
 * do; {@link #command} does not require the drive, which would interrupt the drive's own
 * {@code periodic()}. Nothing else may drive while {@link #active()}.
 *
 * <p>Every number here is a starting point to tune in Panels, not a measurement. The two standoffs
 * come from the Autos' drawings (contact about 12 in from the centre, as R-Quals draws its FLOWER
 * points) and the speeds from nothing at all. The Visualizer's picture of a pick
 * ({@code lib/simple/flowers.ts}) uses the same standoffs: change one, change the other.
 */
@Configurable
public final class FlowerPicker {

    /** Only a FLOWER whose centre is this close to the robot is picked, inches. */
    public static double maxRangeIn = 48.0;
    /** Where the approach path ends: this far back from the FLOWER's centre, inches. */
    public static double approachStandoffIn = 24.0;
    /** Where the push stops: the tracked point this far back from the FLOWER's centre, inches. */
    public static double contactStandoffIn = 12.0;
    /** The stick goes down and the intake starts once the robot is this close to the contact point, inches. */
    public static double lowerWithinIn = 30.0;
    /** Time the stick is given to swing down before the push may start, ms. */
    public static double stickDownMs = 700;
    /** The approach path gives up after this long, ms. */
    public static double approachTimeoutMs = 4000;
    /** Already at the approach pose: this close to it, inches, skips the approach path. */
    public static double atApproachIn = 1.5;
    /** Already at the approach pose: this square to the FLOWER, degrees, skips the approach path. */
    public static double atApproachDeg = 5.0;
    /** The push path's speed cap, a fraction of the robot's top path speed (0 to 1]. */
    public static double pushSpeed = 0.35;
    /** The push gives up after this long, ms. */
    public static double pushTimeoutMs = 2000;
    /** Less progress than this, inches, in {@link #stallMs} means the robot is against the FLOWER. */
    public static double stallProgressIn = 0.25;
    /** See {@link #stallProgressIn}, ms. */
    public static double stallMs = 250;
    /** The dwell, ms. A cap until pieces can be counted. */
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

    /** Where a pick is. */
    public enum Phase { IDLE, APPROACH, STICK, PUSH, DWELL, BACK, DONE, ABORTED, REFUSED }

    private final Robot robot;
    private final Consumer<String> event;

    private Phase phase = Phase.IDLE;
    private Flower flower;
    private Pose contact;
    private Pose approach;
    private boolean lowered;
    private long startNs;
    private long phaseStartNs;
    private long loweredNs;
    private long progressNs;
    private double progressAlongIn;
    private double backFromAlongIn;
    private String pushEnd = "";
    private final double[] phaseMs = new double[Phase.values().length];
    private String result = "";

    /**
     * @param event where phase lines go: the OpMode's {@code log.event}, looked up each time, since
     *              the match log is opened after the OpMode is built
     */
    public FlowerPicker(Robot robot, Consumer<String> event) {
        this.robot = robot;
        this.event = event;
    }

    /**
     * An Auto's {@code PickFlower} card: a whole pick, as if the button were held to the end. It ends
     * when the pick does (done, stopped or refused, each in the match log); cut short by the card's
     * timeout, it stops the pick where it is. It does not require the drive (see the class comment).
     */
    public static Command command(Robot robot, Consumer<String> event) {
        final FlowerPicker[] picker = new FlowerPicker[1];
        return new CommandBuilder()
                .setStart(() -> {
                    picker[0] = new FlowerPicker(robot, event);
                    picker[0].update(true, false);
                })
                .setExecute(() -> picker[0].update(true, false))
                .setDone(() -> picker[0].finished())
                .setEnd(end -> {
                    if (picker[0].active()) picker[0].update(false, false);
                });
    }

    /** The pick has ended: done, stopped, or refused. */
    public boolean finished() {
        return phase == Phase.DONE || phase == Phase.ABORTED || phase == Phase.REFUSED;
    }

    /** Whether a pick is driving the robot, so the OpMode must leave the drive alone. */
    public boolean active() {
        return phase == Phase.APPROACH || phase == Phase.STICK || phase == Phase.PUSH
                || phase == Phase.DWELL || phase == Phase.BACK;
    }

    public Phase phase() {
        return phase;
    }

    /**
     * One step, every loop.
     *
     * @param held         the pick button is held
     * @param sticksMoved  a drive stick is off centre: the driver wants the robot back
     */
    public void update(boolean held, boolean sticksMoved) {
        long now = System.nanoTime();
        if (!held) {
            if (active()) abort(now, "released");
            phase = Phase.IDLE;
            return;
        }
        if (phase == Phase.IDLE) {
            start(now);
            return;
        }
        if (!active()) return; // DONE, ABORTED or REFUSED: wait for the button to be let go
        if (sticksMoved) {
            abort(now, "sticks moved");
            return;
        }
        Pose pose = robot.drive.pose();
        double along = alongTrackIn(pose.x(), pose.y(), contact.x(), contact.y(), contact.heading());
        switch (phase) {
            case APPROACH:
                lowerIfClose(now, -along);
                if (robot.drive.pathDone()) {
                    lowerIfClose(now, 0.0); // lowered by now whatever lowerWithinIn says
                    next(now, Phase.STICK);
                } else if (ms(now, phaseStartNs) > nn(approachTimeoutMs)) {
                    abort(now, "approach timed out");
                }
                break;
            case STICK:
                // The follower holds the approach pose until the next drive command.
                if (ms(now, loweredNs) >= nn(stickDownMs)) {
                    progressNs = now;
                    progressAlongIn = along;
                    next(now, Phase.PUSH);
                    straightTo(pose, contact, pushSpeed);
                }
                break;
            case PUSH:
                if (along > progressAlongIn + nn(stallProgressIn)) {
                    progressAlongIn = along;
                    progressNs = now;
                }
                if (along >= 0.0 || robot.drive.pathDone()) {
                    pushEnd = along >= 0.0 ? "reached" : String.format(Locale.US, "path done %.1f in short", -along);
                } else if (ms(now, progressNs) > nn(stallMs)) {
                    pushEnd = String.format(Locale.US, "stalled %.1f in short", -along);
                } else if (ms(now, phaseStartNs) > nn(pushTimeoutMs)) {
                    pushEnd = String.format(Locale.US, "timed out %.1f in short", -along);
                }
                if (!pushEnd.isEmpty()) {
                    next(now, Phase.DWELL);
                    steer(pose, dwellPushPower);
                }
                break;
            case DWELL:
                if (ms(now, phaseStartNs) >= nn(dwellMs)) {
                    backFromAlongIn = along;
                    next(now, Phase.BACK);
                    straightTo(pose, approach, backSpeed);
                } else {
                    steer(pose, dwellPushPower);
                }
                break;
            case BACK:
                double backed = backFromAlongIn - along;
                if (backed >= nn(retractAfterBackIn)) robot.flowerStick.retract();
                if (robot.drive.pathDone() || ms(now, phaseStartNs) > nn(backTimeoutMs)) {
                    finish(now);
                }
                break;
            default:
                break;
        }
    }

    /** For the Match page: what a pick is doing, or how the last one went. Empty before the first. */
    public String line() {
        if (active()) {
            return String.format(Locale.US, "FLOWER pick: %s %.1f s · %s", phase,
                    ms(System.nanoTime(), startNs) / 1000.0, flower);
        }
        return result;
    }

    // ------------------------------------------------------------------ steps

    private void start(long now) {
        startNs = now;
        phaseStartNs = now;
        Arrays.fill(phaseMs, 0.0);
        lowered = false;
        pushEnd = "";
        Pose pose = robot.drive.pose();
        if (pose == null) {
            refuse("no Pinpoint");
            return;
        }
        if (!robot.drive.poseReferenced()) {
            refuse("the pose is not on the field yet (an Auto's handoff, or relocalize from the camera)");
            return;
        }
        flower = FlowerFieldPoints.nearest(pose.x(), pose.y(), nn(maxRangeIn));
        if (flower == null) {
            refuse(String.format(Locale.US, "no FLOWER within %.0f in", maxRangeIn));
            return;
        }
        contact = FlowerFieldPoints.standoff(flower, nn(contactStandoffIn));
        approach = FlowerFieldPoints.standoff(flower, nn(approachStandoffIn));
        boolean there = atPose(pose, approach, nn(atApproachIn), nn(atApproachDeg));
        try {
            robot.drive.preparePathFollowing();
            if (!there) {
                // The same call shape as the generated Autos' paths.
                robot.drive.setAccelLimited(true);
                robot.drive.follow(Paths.line(pose, approach).heading(
                        Interpolator.piecewise().until(1, Interpolator.linear(pose, approach))));
            }
        } catch (RuntimeException e) {
            refuse("no path following on this robot: " + e.getMessage());
            return;
        }
        phase = there ? Phase.STICK : Phase.APPROACH;
        event.accept(String.format(Locale.US,
                "flower pick: start, %s, %.0f in away, from (%.1f, %.1f, %.0f°)%s",
                flower, Math.hypot(flower.x - pose.x(), flower.y - pose.y()),
                pose.x(), pose.y(), Math.toDegrees(pose.heading()), there ? ", already at the approach" : ""));
        lowerIfClose(now, there ? 0.0 : -alongTrackIn(pose.x(), pose.y(), contact.x(), contact.y(), contact.heading()));
    }

    /** Down goes the stick, on goes the intake, once {@code toContactIn} is within reach. Once. */
    private void lowerIfClose(long now, double toContactIn) {
        if (lowered || toContactIn > nn(lowerWithinIn)) return;
        lowered = true;
        loweredNs = now;
        robot.flowerStick.extend();
        robot.intake.intake();
        event.accept(String.format(Locale.US, "flower pick: stick down, intake on, %.1f in to contact, %.2f s in",
                Math.max(0.0, toContactIn), ms(now, startNs) / 1000.0));
    }

    private void next(long now, Phase to) {
        phaseMs[phase.ordinal()] = ms(now, phaseStartNs);
        event.accept(String.format(Locale.US, "flower pick: %s %.2f s%s, then %s", phase,
                phaseMs[phase.ordinal()] / 1000.0,
                phase == Phase.PUSH ? " (" + pushEnd + ")" : "", to));
        phase = to;
        phaseStartNs = now;
    }

    private void finish(long now) {
        phaseMs[phase.ordinal()] = ms(now, phaseStartNs);
        stopMechanisms();
        phase = Phase.DONE;
        result = String.format(Locale.US,
                "Last FLOWER pick: %.2f s · approach %.2f, stick %.2f, push %.2f (%s), dwell %.2f, back %.2f",
                ms(now, startNs) / 1000.0, phaseMs[Phase.APPROACH.ordinal()] / 1000.0,
                phaseMs[Phase.STICK.ordinal()] / 1000.0, phaseMs[Phase.PUSH.ordinal()] / 1000.0, pushEnd,
                phaseMs[Phase.DWELL.ordinal()] / 1000.0, phaseMs[Phase.BACK.ordinal()] / 1000.0);
        event.accept("flower pick: done, " + flower + ". " + result);
    }

    private void abort(long now, String why) {
        String during = phase.toString();
        stopMechanisms();
        phase = Phase.ABORTED;
        result = String.format(Locale.US, "Last FLOWER pick: stopped in %s after %.2f s (%s)",
                during, ms(now, startNs) / 1000.0, why);
        event.accept("flower pick: " + result);
    }

    private void refuse(String why) {
        phase = Phase.REFUSED;
        result = "FLOWER pick refused: " + why;
        event.accept("flower pick: refused, " + why);
    }

    /** Drive stops (the sticks take the next loop), intake idles, stick comes up. */
    private void stopMechanisms() {
        robot.drive.drive(0.0, 0.0, 0.0);
        robot.intake.idle();
        robot.flowerStick.retract();
    }

    /**
     * A Pedro path straight from {@code from} to {@code to}, holding the FLOWER's heading, capped at
     * {@code speed} of the robot's top path speed: the push and the back-out, which the Visualizer
     * draws as the same straight lines.
     */
    private void straightTo(Pose from, Pose to, double speed) {
        Path path = Paths.line(from, to).constant(to.heading()).with(speedCap(speed));
        robot.drive.setAccelLimited(true);
        robot.drive.follow(path);
    }

    /** Pedro's per-path cap on speed, as a fraction of the active robot's top path speed. */
    private static Modifier speedCap(double fraction) {
        return Constants.forActiveRobot().foresightConfig.maxPathSpeed.at(unitFraction(fraction));
    }

    /** Robot-centric drive at {@code forward}, steering back onto the line into the FLOWER and square to it. */
    private void steer(Pose pose, double forward) {
        double lateral = lateralIn(pose.x(), pose.y(), contact.x(), contact.y(), contact.heading());
        double headingError = wrapRad(contact.heading() - pose.heading());
        robot.drive.driveRobotCentric(clamp(forward, 1.0),
                clamp(-lateralKp * lateral, maxCorrection),
                clamp(headingKp * headingError, maxCorrection));
    }

    // ------------------------------------------------------------- pure math

    /**
     * How far (px, py) is past the point (cx, cy) along heading {@code h}, inches: negative before
     * it, positive beyond it.
     */
    static double alongTrackIn(double px, double py, double cx, double cy, double h) {
        return (px - cx) * Math.cos(h) + (py - cy) * Math.sin(h);
    }

    /**
     * How far (px, py) is to the left of the line through (cx, cy) along heading {@code h}, inches;
     * negative to the right. A robot facing along the line strafes right (negative) to correct a
     * positive value.
     */
    static double lateralIn(double px, double py, double cx, double cy, double h) {
        return -(px - cx) * Math.sin(h) + (py - cy) * Math.cos(h);
    }

    /** An angle in (-π, π]. */
    static double wrapRad(double a) {
        double w = Math.IEEEremainder(a, 2 * Math.PI);
        return w <= -Math.PI ? w + 2 * Math.PI : w;
    }

    /** {@code value} clamped to ±{@code limit}; NaN and a bad limit give 0. */
    static double clamp(double value, double limit) {
        if (!(limit > 0.0) || Double.isNaN(value)) return 0.0;
        return Math.max(-limit, Math.min(limit, value));
    }

    /** Whether {@code pose} is within {@code inches} and {@code degrees} of {@code target}. */
    static boolean atPose(Pose pose, Pose target, double inches, double degrees) {
        return Math.hypot(pose.x() - target.x(), pose.y() - target.y()) <= inches
                && Math.abs(Math.toDegrees(wrapRad(pose.heading() - target.heading()))) <= degrees;
    }

    /** A Panels-editable speed fraction in (0, 1]: a typo makes a slow drive, never a stopped or wild one. */
    static double unitFraction(double fraction) {
        if (!(fraction > 0.05)) return 0.05;
        return Math.min(1.0, fraction);
    }

    /** A Panels-editable distance or time: negative and NaN read as 0, so a typo stops early, never runs away. */
    static double nn(double value) {
        return value > 0.0 ? value : 0.0;
    }

    private static double ms(long now, long since) {
        return (now - since) / 1e6;
    }
}
