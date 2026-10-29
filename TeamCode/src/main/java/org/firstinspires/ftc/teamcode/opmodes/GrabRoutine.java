package org.firstinspires.ftc.teamcode.opmodes;

import com.pedropathing.api.Paths;
import com.pedropathing.config.Modifier;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.CommandBuilder;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.pedropathing.paths.interpolator.Interpolator;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.pedro.Constants;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * The one sequence behind every grab the robot makes from a fixed place on the field: a FLOWER pick
 * ({@link FlowerPicker}) and a GARDEN gather ({@link GardenGather}). Each subclass says only where it
 * goes and what its mechanisms do; the driving, the timing, the stops and the log are here, once.
 *
 * <h2>The sequence</h2>
 *
 * <ol>
 *   <li><b>Line up</b> (the button only). A Pedro path from the robot's localized pose to the
 *       target's line-up pose ({@link #lineUpPose}). Skipped when already there.</li>
 *   <li><b>Prep.</b> The mechanisms get ready ({@link #prep}), and the robot waits {@link #prepMs}
 *       for them. Prep starts during the line-up once the robot is within {@link #prepWithinIn}.</li>
 *   <li><b>In.</b> A Pedro path to where the grab happens ({@link #inPose}), capped at
 *       {@link #inSpeed} of the robot's top speed. It ends when the path does, or when the robot
 *       stops getting closer (it is against something; not counted in the path's first moments,
 *       while it ramps up from rest), or at {@link #inTimeoutMs}.</li>
 *   <li><b>Dwell</b> for {@link #dwellMs}, pushing lightly ({@link #dwellPushPower}), steered.</li>
 *   <li><b>Back.</b> A Pedro path back to exactly where the In started, capped at
 *       {@link #backSpeed}. {@link #unprep} once the robot has backed {@link #unprepAfterBackIn}.</li>
 * </ol>
 *
 * <p><b>Its contract: it ends where its In started.</b> On the button that is the line-up pose. On an
 * Auto's card ({@link #command}) there is no line-up, the Auto's drive having put the robot where
 * it grabs from, so the card ends where it started: the Auto's next drive starts from the spot the
 * Auto drew, and the Visualizer ({@code lib/simple/flowers.ts}, {@code garden.ts}) draws each grab
 * as it is, from the same numbers.
 *
 * <p>On the button: hold to run. Letting go, or moving a drive stick, stops it where it is: the drive
 * stops, the mechanisms stop, and the sticks have the robot again on the next loop. It refuses, and
 * says why, without a Pinpoint, without a field-referenced pose, with no target in range, or on a
 * robot whose path follower cannot be built (no Foresight block yet).
 *
 * <p>It drives the robot by calling {@code robot.drive} each loop, as the sticks and the Auto's paths
 * do; {@link #command} does not require the drive, which would interrupt the drive's own
 * {@code periodic()}. Nothing else may drive while {@link #active()}.
 *
 * <p>The robot comes to rest at the start and the end of each grab. A grab that keeps moving into
 * the next drive is a later change, here, for every grab at once.
 */
public abstract class GrabRoutine {

    /** Where a grab is. */
    public enum Phase { IDLE, LINE_UP, PREP, IN, DWELL, BACK, DONE, ABORTED, REFUSED }

    /** Where the In goes, or why there is none. */
    protected static final class InPlan {
        final Pose in;
        final String refusal;

        private InPlan(Pose in, String refusal) {
            this.in = in;
            this.refusal = refusal;
        }

        static InPlan to(Pose in) {
            return new InPlan(in, null);
        }

        static InPlan refuse(String why) {
            return new InPlan(null, why);
        }
    }

    protected final Robot robot;
    private final Consumer<String> event;
    private final boolean lineUp;

    private Phase phase = Phase.IDLE;
    private String target = "";
    private Pose in;
    private Pose home;
    private boolean prepped;
    private long startNs;
    private long phaseStartNs;
    private long preppedNs;
    private long progressNs;
    private double bestRemainingIn;
    private Pose inEnd;
    private String inEndWhy = "";
    private boolean unprepped;
    private final double[] phaseMs = new double[Phase.values().length];
    private String result = "";

    /**
     * @param event  where phase lines go: the OpMode's {@code log.event}, looked up each time, since
     *               the match log is opened after the OpMode is built
     * @param lineUp the button's way: line up first, from the localized pose
     */
    protected GrabRoutine(Robot robot, Consumer<String> event, boolean lineUp) {
        this.robot = robot;
        this.event = event;
        this.lineUp = lineUp;
    }

    // ------------------------------------------------------------- what a grab says about itself

    /** For the log and the Match page: "FLOWER pick". */
    protected abstract String what();

    /**
     * The target nearest the robot, by name, or null with {@link #noTargetWhy} when none is in range.
     * Called once, at the start; the subclass keeps what it found for the methods below.
     */
    protected abstract String findTarget(Pose pose);

    /** Why {@link #findTarget} found none. */
    protected abstract String noTargetWhy();

    /** The button's line-up pose for the target found. */
    protected abstract Pose lineUpPose();

    /**
     * Where the In goes from {@code from}: after a line-up ({@code linedUp}), or from wherever an
     * Auto's card started; or why the robot refuses.
     */
    protected abstract InPlan inPose(Pose from, boolean linedUp);

    /** The mechanisms get ready (stick down, intake on). Called once. */
    protected abstract void prep();

    /** The mechanisms put away during the back-out (stick up). Called once, if prepped. */
    protected abstract void unprep();

    /** Everything stops: the end of a grab, finished or not. The drive is stopped by the caller. */
    protected abstract void stopMechanisms();

    protected abstract double prepMs();
    protected abstract double prepWithinIn();
    protected abstract double atLineUpIn();
    protected abstract double atLineUpDeg();
    protected abstract double lineUpTimeoutMs();
    protected abstract double inSpeed();
    protected abstract double inTimeoutMs();
    protected abstract double stallProgressIn();
    protected abstract double stallMs();
    protected abstract double dwellMs();
    protected abstract double dwellPushPower();
    protected abstract double backSpeed();
    protected abstract double unprepAfterBackIn();
    protected abstract double backTimeoutMs();
    protected abstract double headingKp();
    protected abstract double lateralKp();
    protected abstract double maxCorrection();

    // --------------------------------------------------------------------------- the routine

    /**
     * An Auto's card for a grab: the whole grab from where the robot is, with no line-up, as if the
     * button were held to the end. It ends when the grab does (done, stopped or refused, each in the
     * match log); cut short by the card's timeout, it stops the grab where it is. It does not
     * require the drive (see the class comment).
     */
    protected static Command command(java.util.function.Supplier<GrabRoutine> make) {
        final GrabRoutine[] grab = new GrabRoutine[1];
        return new CommandBuilder()
                .setStart(() -> {
                    grab[0] = make.get();
                    grab[0].update(true, false);
                })
                .setExecute(() -> grab[0].update(true, false))
                .setDone(() -> grab[0].finished())
                .setEnd(end -> {
                    if (grab[0].active()) grab[0].update(false, false);
                });
    }

    /** Whether a grab is driving the robot, so the OpMode must leave the drive alone. */
    public boolean active() {
        return phase == Phase.LINE_UP || phase == Phase.PREP || phase == Phase.IN
                || phase == Phase.DWELL || phase == Phase.BACK;
    }

    /** The grab has ended: done, stopped, or refused. */
    public boolean finished() {
        return phase == Phase.DONE || phase == Phase.ABORTED || phase == Phase.REFUSED;
    }

    public Phase phase() {
        return phase;
    }

    /**
     * One step, every loop.
     *
     * @param held         the button is held (an Auto's card holds it to the end)
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
        double remaining = Math.hypot(in.x() - pose.x(), in.y() - pose.y());
        switch (phase) {
            case LINE_UP:
                prepIfClose(now, remaining);
                if (robot.drive.pathDone()) {
                    pose = robot.drive.pose(); // where the line-up ended, not where this loop began
                    InPlan plan = inPose(pose, true);
                    if (plan.in == null) {
                        abort(now, plan.refusal);
                        return;
                    }
                    in = plan.in;
                    prepIfClose(now, 0.0); // prepped by now whatever prepWithinIn says
                    home = pose;
                    next(now, Phase.PREP);
                } else if (ms(now, phaseStartNs) > nn(lineUpTimeoutMs())) {
                    abort(now, "line-up timed out");
                }
                break;
            case PREP:
                // The follower holds where the line-up ended until the next drive command.
                if (ms(now, preppedNs) >= nn(prepMs())) {
                    progressNs = now;
                    bestRemainingIn = remaining;
                    next(now, Phase.IN);
                    drivePath(pose, in, inSpeed());
                }
                break;
            case IN:
                if (remaining < bestRemainingIn - nn(stallProgressIn())) {
                    bestRemainingIn = remaining;
                    progressNs = now;
                }
                if (robot.drive.pathDone()) {
                    pose = robot.drive.pose();
                    remaining = Math.hypot(in.x() - pose.x(), in.y() - pose.y());
                    inEndWhy = remaining <= 0.5 ? "reached"
                            : String.format(Locale.US, "path done %.1f in short", remaining);
                } else if (ms(now, phaseStartNs) > 2 * nn(stallMs()) && ms(now, progressNs) > nn(stallMs())) {
                    // (Not in the first moments: a path ramps up from rest.)
                    inEndWhy = String.format(Locale.US, "stalled %.1f in short", remaining);
                } else if (ms(now, phaseStartNs) > nn(inTimeoutMs())) {
                    inEndWhy = String.format(Locale.US, "timed out %.1f in short", remaining);
                }
                if (!inEndWhy.isEmpty()) {
                    inEnd = pose;
                    next(now, Phase.DWELL);
                    steer(pose, dwellPushPower());
                }
                break;
            case DWELL:
                if (ms(now, phaseStartNs) >= nn(dwellMs())) {
                    next(now, Phase.BACK);
                    drivePath(pose, home, backSpeed());
                } else {
                    steer(pose, dwellPushPower());
                }
                break;
            case BACK:
                double backed = Math.hypot(pose.x() - inEnd.x(), pose.y() - inEnd.y());
                if (!unprepped && backed >= nn(unprepAfterBackIn())) {
                    unprepped = true;
                    unprep();
                }
                if (robot.drive.pathDone() || ms(now, phaseStartNs) > nn(backTimeoutMs())) {
                    finish(now);
                }
                break;
            default:
                break;
        }
    }

    /** For the Match page: what a grab is doing, or how the last one went. Empty before the first. */
    public String line() {
        if (active()) {
            return String.format(Locale.US, "%s: %s %.1f s · %s", what(), phase,
                    ms(System.nanoTime(), startNs) / 1000.0, target);
        }
        return result;
    }

    // ------------------------------------------------------------------ steps

    private void start(long now) {
        startNs = now;
        phaseStartNs = now;
        Arrays.fill(phaseMs, 0.0);
        prepped = false;
        unprepped = false;
        inEndWhy = "";
        Pose pose = robot.drive.pose();
        if (pose == null) {
            refuse("no Pinpoint");
            return;
        }
        if (!robot.drive.poseReferenced()) {
            refuse("the pose is not on the field yet (an Auto's handoff, or relocalize from the camera)");
            return;
        }
        String found = findTarget(pose);
        if (found == null) {
            refuse(noTargetWhy());
            return;
        }
        target = found;
        try {
            robot.drive.preparePathFollowing();
        } catch (RuntimeException e) {
            refuse("no path following on this robot: " + e.getMessage());
            return;
        }
        boolean there = true;
        if (lineUp) {
            Pose lineUpPose = lineUpPose();
            there = atPose(pose, lineUpPose, nn(atLineUpIn()), nn(atLineUpDeg()));
            if (!there) {
                in = lineUpPose; // what "remaining" measures until the line-up is done
                // The same call shape as the generated Autos' paths.
                robot.drive.setAccelLimited(true);
                robot.drive.follow(Paths.line(pose, lineUpPose).heading(
                        Interpolator.piecewise().until(1, Interpolator.linear(pose, lineUpPose))));
            }
        }
        if (there) {
            InPlan plan = inPose(pose, lineUp);
            if (plan.in == null) {
                refuse(plan.refusal);
                return;
            }
            in = plan.in;
            home = pose;
        }
        phase = there ? Phase.PREP : Phase.LINE_UP;
        event.accept(String.format(Locale.US, "%s: start, %s, from (%.1f, %.1f, %.0f°)%s", what(), target,
                pose.x(), pose.y(), Math.toDegrees(pose.heading()),
                !lineUp ? ", from here (Auto)" : there ? ", already lined up" : ""));
        if (there) prepIfClose(now, 0.0);
    }

    /** Prep, once {@code remainingIn} is within reach. Once. */
    private void prepIfClose(long now, double remainingIn) {
        if (prepped || remainingIn > nn(prepWithinIn())) return;
        prepped = true;
        preppedNs = now;
        prep();
        event.accept(String.format(Locale.US, "%s: prep, %.1f in to go, %.2f s in", what(),
                Math.max(0.0, remainingIn), ms(now, startNs) / 1000.0));
    }

    private void next(long now, Phase to) {
        phaseMs[phase.ordinal()] = ms(now, phaseStartNs);
        event.accept(String.format(Locale.US, "%s: %s %.2f s%s, then %s", what(), phase,
                phaseMs[phase.ordinal()] / 1000.0,
                phase == Phase.IN ? " (" + inEndWhy + ")" : "", to));
        phase = to;
        phaseStartNs = now;
    }

    private void finish(long now) {
        phaseMs[phase.ordinal()] = ms(now, phaseStartNs);
        stopAll();
        phase = Phase.DONE;
        result = String.format(Locale.US,
                "Last %s: %.2f s · line-up %.2f, prep %.2f, in %.2f (%s), dwell %.2f, back %.2f", what(),
                ms(now, startNs) / 1000.0, phaseMs[Phase.LINE_UP.ordinal()] / 1000.0,
                phaseMs[Phase.PREP.ordinal()] / 1000.0, phaseMs[Phase.IN.ordinal()] / 1000.0, inEndWhy,
                phaseMs[Phase.DWELL.ordinal()] / 1000.0, phaseMs[Phase.BACK.ordinal()] / 1000.0);
        event.accept(what() + ": done, " + target + ". " + result);
    }

    private void abort(long now, String why) {
        String during = phase.toString();
        stopAll();
        phase = Phase.ABORTED;
        result = String.format(Locale.US, "Last %s: stopped in %s after %.2f s (%s)", what(),
                during, ms(now, startNs) / 1000.0, why);
        event.accept(what() + ": " + result);
    }

    private void refuse(String why) {
        phase = Phase.REFUSED;
        result = what() + " refused: " + why;
        event.accept(what() + ": refused, " + why);
    }

    /** Drive stops (the sticks take the next loop), and the mechanisms stop. */
    private void stopAll() {
        robot.drive.drive(0.0, 0.0, 0.0);
        stopMechanisms();
    }

    /**
     * A Pedro path straight from {@code from} to {@code to}, turning to {@code to}'s heading, capped
     * at {@code speed} of the robot's top path speed: the In and the Back, which the Visualizer draws
     * as the same straight lines.
     */
    private void drivePath(Pose from, Pose to, double speed) {
        Path path = Paths.line(from, to)
                .heading(Interpolator.piecewise().until(1, Interpolator.linear(from, to)))
                .with(speedCap(speed));
        robot.drive.setAccelLimited(true);
        robot.drive.follow(path);
    }

    /** Pedro's per-path cap on speed, as a fraction of the active robot's top path speed. */
    private static Modifier speedCap(double fraction) {
        return Constants.forActiveRobot().foresightConfig.maxPathSpeed.at(unitFraction(fraction));
    }

    /** Robot-centric drive at {@code forward}, steering back onto the line into the In and square to it. */
    private void steer(Pose pose, double forward) {
        double lateral = lateralIn(pose.x(), pose.y(), in.x(), in.y(), in.heading());
        double headingError = wrapRad(in.heading() - pose.heading());
        robot.drive.driveRobotCentric(clamp(forward, 1.0),
                clamp(-lateralKp() * lateral, maxCorrection()),
                clamp(headingKp() * headingError, maxCorrection()));
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

    /**
     * The point straight ahead of {@code pose}, along its heading, where the point (fx, fy) is
     * {@code standoffIn} further on. Null when (fx, fy) is not that far ahead.
     */
    static Pose aheadTo(Pose pose, double fx, double fy, double standoffIn) {
        double h = pose.heading();
        double ahead = (fx - pose.x()) * Math.cos(h) + (fy - pose.y()) * Math.sin(h);
        double drive = ahead - standoffIn;
        if (!(drive > 0.5)) return null;
        return new Pose(pose.x() + drive * Math.cos(h), pose.y() + drive * Math.sin(h), h);
    }

    /** Whether {@code pose} is within {@code inches} and {@code degrees} of {@code target}. */
    static boolean atPose(Pose pose, Pose target, double inches, double degrees) {
        return Math.hypot(pose.x() - target.x(), pose.y() - target.y()) <= inches
                && Math.abs(Math.toDegrees(wrapRad(pose.heading() - target.heading()))) <= degrees;
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
