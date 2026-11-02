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
import org.firstinspires.ftc.teamcode.util.FieldFrame;

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
 *   <li><b>Line up.</b> A Pedro path from the robot's localized pose to the target's line-up pose:
 *       {@link #lineUpPose} on the button; on an Auto's card, {@link #autoLineUp}, the square-up from
 *       where the Auto's drive left the robot (none unless the subclass gives one). Skipped when
 *       already there. It ends once the
 *       path is done <em>and</em> the robot faces the line-up's heading to {@link #atLineUpDeg}
 *       (waiting at most {@link #SETTLE_MAX_MS} for that): Pedro calls a short path done before the
 *       robot has turned.</li>
 *   <li><b>Prep.</b> The mechanisms get ready ({@link #prep}), and the robot waits {@link #prepMs}
 *       for them. Prep starts during the line-up once the robot is within {@link #prepWithinIn}.
 *       The In also waits for the robot to slow below {@link #settledInPerSec} (at most
 *       {@link #SETTLE_MAX_MS}), since Pedro ends a path still rolling.</li>
 *   <li><b>In.</b> A Pedro path to where the grab happens ({@link #inPose}), capped at
 *       {@link #inSpeed} of the robot's top speed. It ends when the path does, or when the robot
 *       stops getting closer (it is against something; not counted in the path's first moments,
 *       while it ramps up from rest), or at {@link #inTimeoutMs}.</li>
 *   <li><b>Dwell</b>, pushing lightly ({@link #dwellPushPower}), steered, until the intake's current
 *       says the pieces have stopped coming ({@link DwellWatch}: quiet for {@link #dwellQuietMs}
 *       after the last spike, or no spike by {@link #dwellEmptyMs}), at most {@link #dwellMs}. The
 *       current is read every loop while a grab runs.</li>
 *   <li><b>Back.</b> A Pedro path back to exactly where the In started, capped at
 *       {@link #backSpeed}. {@link #unprep} once the robot has backed {@link #unprepAfterBackIn}.</li>
 * </ol>
 *
 * <p><b>Its contract: it ends where its In started.</b> On the button that is the line-up pose. On an
 * Auto's card ({@link #command}) it is the square-up pose when the subclass squares up
 * ({@link #autoLineUp}: a FLOWER pick), else where the card started (a GARDEN gather): the Auto's
 * next drive starts from there, and the Visualizer ({@code lib/simple/flowers.ts}, {@code garden.ts})
 * draws each grab as it is, from the same numbers.
 *
 * <p>On the button: hold to run. Letting go before the dwell, or moving a drive stick at any time,
 * stops it where it is: the drive stops, the mechanisms stop, and the sticks have the robot again on
 * the next loop. <b>Letting go during the dwell or the back-out means done</b>
 * ({@link #releaseFinishes}): the dwell ends there and the robot backs out on its own, putting the
 * mechanisms away as usual, so a driver who sees the pieces are in need not hold on, nor wait out
 * {@link #dwellMs}. A stick still stops it. It refuses, and
 * says why, without a Pinpoint, without a field-referenced pose, with no target in range, or on a
 * robot whose path follower cannot be built (no Foresight block yet).
 *
 * <p>It drives the robot by calling {@code robot.drive} each loop, as the sticks and the Auto's paths
 * do; {@link #command} does not require the drive, which would interrupt the drive's own
 * {@code periodic()}. Nothing else may drive while {@link #active()}.
 *
 * <p><b>An Auto never leaves its half.</b> A robot may not cross the centre line during the Auto, so
 * an Auto's card only considers targets on the robot's own side of it ({@link #ownHalf}); the button
 * considers every one, since in TeleOp they are all ours to use. The robot is always on its own half
 * in an Auto, so this needs no alliance.
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
    /** When the line-up's path was first done, or -1. */
    private long lineUpDoneNs = -1;
    /** This grab's line-up gives up after this long: the button's, or an Auto's square-up's. */
    private double lineUpLimitMs;
    /** Let go during the dwell or the back-out: the grab runs to its end without the button. */
    private boolean finishing;
    private final DwellWatch dwell = new DwellWatch();
    private String dwellEndWhy = "";
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

    /** An Auto's card, which must not leave the robot's half; the button may. */
    protected boolean inAuto() {
        return !lineUp;
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

    /**
     * An Auto's card only: the pose to square up to before the In, from where the Auto's drive left
     * the robot; null for none (the In starts from here); or why the robot refuses. Skipped, like the
     * button's line-up, when already within {@link #atLineUpIn} and {@link #atLineUpDeg} of it and
     * {@link #mayPrepAt} allows prep where the robot is; the In is then planned as after a line-up.
     * None by default.
     */
    protected InPlan autoLineUp(Pose from) {
        return null;
    }

    /** An Auto's square-up gives up after this long, ms. The button's line-up time by default. */
    protected double autoLineUpTimeoutMs() {
        return lineUpTimeoutMs();
    }

    /**
     * Whether prep may start during the line-up with the robot at {@code pose}, as well as within
     * {@link #prepWithinIn} and {@link #prepWithinDeg}; also whether a robot near its line-up pose
     * counts as already there. Always, by default.
     */
    protected boolean mayPrepAt(Pose pose) {
        return true;
    }

    /** The mechanisms get ready (stick down, intake on). Called once. */
    protected abstract void prep();

    /** The mechanisms put away during the back-out (stick up). Called once, if prepped. */
    protected abstract void unprep();

    /** Everything stops: the end of a grab, finished or not. The drive is stopped by the caller. */
    protected abstract void stopMechanisms();

    protected abstract double prepMs();
    protected abstract double prepWithinIn();
    /**
     * Prep during the line-up also waits until the robot faces the line-up's heading to within this
     * many degrees: a mechanism swung out while the robot still turns can catch a wall.
     */
    protected abstract double prepWithinDeg();
    protected abstract double atLineUpIn();
    protected abstract double atLineUpDeg();
    protected abstract double lineUpTimeoutMs();
    /** The In starts once the robot is slower than this, inches/s (see {@link #SETTLE_MAX_MS}). */
    protected abstract double settledInPerSec();
    protected abstract double inSpeed();
    protected abstract double inTimeoutMs();
    protected abstract double stallProgressIn();
    protected abstract double stallMs();
    protected abstract double dwellMs();
    /** The dwell ends this long after the intake current's last spike, ms; 0 turns it off. */
    protected abstract double dwellQuietMs();
    /** The dwell ends if the current has had no spike this far into it, ms; 0 turns it off. */
    protected abstract double dwellEmptyMs();
    /** A current this many amps over the roller's free-running current is a piece. */
    protected abstract double spikeAboveAmps();
    protected abstract double dwellPushPower();
    protected abstract double backSpeed();
    protected abstract double unprepAfterBackIn();
    protected abstract double backTimeoutMs();
    protected abstract double headingKp();
    protected abstract double lateralKp();
    protected abstract double maxCorrection();

    // --------------------------------------------------------------------------- the routine

    /**
     * An Auto's card for a grab: the whole grab from where the robot is, as if the button were held
     * to the end, with the subclass's square-up ({@link #autoLineUp}) in place of the button's line-up. It ends when the grab does (done, stopped or refused, each in the
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
        if (!held && !(finishing && active())) {
            if (active() && releaseFinishes(phase, lineUp)) {
                finishing = true;
                event.accept(String.format(Locale.US, "%s: let go in %s, %.2f s in: done, backing out",
                        what(), phase, ms(now, phaseStartNs) / 1000.0));
                if (phase == Phase.DWELL) {
                    if (sticksMoved) {
                        abort(now, "sticks moved");
                    } else {
                        dwellEndWhy = "let go";
                        startBack(now, robot.drive.pose());
                    }
                }
                return;
            }
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
        if (prepped) dwell.observe(ms(now, startNs), robot.intake.currentAmps(), nn(spikeAboveAmps()));
        switch (phase) {
            case LINE_UP:
                if (Math.abs(Math.toDegrees(wrapRad(in.heading() - pose.heading()))) <= nn(prepWithinDeg())
                        && mayPrepAt(pose)) {
                    prepIfClose(now, remaining); // in is the line-up pose until the line-up ends
                }
                boolean pathDone = robot.drive.pathDone();
                if (pathDone && lineUpDoneNs < 0) lineUpDoneNs = now;
                boolean facing = Math.abs(Math.toDegrees(wrapRad(in.heading() - pose.heading())))
                        <= nn(atLineUpDeg());
                // The follower holds, and turns, after the path; wait for it to face, but not long.
                if (pathDone && (facing || ms(now, lineUpDoneNs) >= SETTLE_MAX_MS)) {
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
                } else if (ms(now, phaseStartNs) > nn(lineUpLimitMs)) {
                    abort(now, lineUp ? "line-up timed out" : "square-up timed out");
                }
                break;
            case PREP:
                // The follower holds where the line-up ended until the next drive command.
                if (ms(now, preppedNs) >= nn(prepMs())
                        && (settled(robot.drive.speedInPerSec(), settledInPerSec())
                                || ms(now, phaseStartNs) >= SETTLE_MAX_MS)) {
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
                boolean capped = ms(now, phaseStartNs) >= nn(dwellMs());
                if (capped || dwell.done(ms(now, startNs), ms(phaseStartNs, startNs), dwellQuietMs(), dwellEmptyMs())) {
                    dwellEndWhy = capped ? "dwellMs" : dwell.sawSpike() ? "pieces in" : "nothing came";
                    startBack(now, pose);
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

    /**
     * Whether letting go of the button in {@code phase} means done (back out on its own) rather than
     * stop here: during the dwell and the back-out, on the button only. An Auto's card lets go only
     * when it is cut short, which always stops the grab.
     */
    static boolean releaseFinishes(Phase phase, boolean button) {
        return button && (phase == Phase.DWELL || phase == Phase.BACK);
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
        finishing = false;
        lineUpDoneNs = -1;
        dwell.reset();
        dwellEndWhy = "";
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
        Pose lineUpPose = null;
        if (lineUp) {
            lineUpPose = lineUpPose();
        } else {
            InPlan square = autoLineUp(pose);
            if (square != null && square.in == null) {
                refuse(square.refusal);
                return;
            }
            if (square != null) lineUpPose = square.in;
        }
        boolean squares = lineUpPose != null;
        lineUpLimitMs = lineUp ? lineUpTimeoutMs() : autoLineUpTimeoutMs();
        if (squares) {
            // Not "there" where prep may not start: the line-up moves the robot out first.
            there = atPose(pose, lineUpPose, nn(atLineUpIn()), nn(atLineUpDeg())) && mayPrepAt(pose);
            if (!there) {
                in = lineUpPose; // what "remaining" measures until the line-up is done
                // The same call shape as the generated Autos' paths.
                robot.drive.setAccelLimited(true);
                robot.drive.follow(Paths.line(pose, lineUpPose).heading(
                        Interpolator.piecewise().until(1, Interpolator.linear(pose, lineUpPose))));
            }
        }
        if (there) {
            InPlan plan = inPose(pose, squares);
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
                !lineUp && !squares ? ", from here (Auto)"
                        : !lineUp ? (there ? ", Auto, already square" : ", Auto, squaring up")
                        : there ? ", already lined up" : ""));
        if (there) prepIfClose(now, 0.0);
    }

    /** Prep, once {@code remainingIn} is within reach. Once. */
    private void prepIfClose(long now, double remainingIn) {
        if (prepped || remainingIn > nn(prepWithinIn())) return;
        prepped = true;
        preppedNs = now;
        prep();
        robot.intake.readCurrentEveryLoop(true);
        event.accept(String.format(Locale.US, "%s: prep, %.1f in to go, %.2f s in", what(),
                Math.max(0.0, remainingIn), ms(now, startNs) / 1000.0));
    }

    private void next(long now, Phase to) {
        phaseMs[phase.ordinal()] = ms(now, phaseStartNs);
        event.accept(String.format(Locale.US, "%s: %s %.2f s%s, then %s", what(), phase,
                phaseMs[phase.ordinal()] / 1000.0,
                phase == Phase.IN ? " (" + inEndWhy + ")"
                        : phase == Phase.DWELL && !dwellEndWhy.isEmpty() ? " (" + dwellEndWhy + ")" : "", to));
        phase = to;
        phaseStartNs = now;
    }

    /** The dwell is over (its time, or the button let go): back out to where the In started. */
    private void startBack(long now, Pose pose) {
        next(now, Phase.BACK);
        drivePath(pose, home, backSpeed());
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
        robot.intake.readCurrentEveryLoop(false);
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
     * The longest a grab waits, ms, for the robot to face the line-up's heading after its path, and
     * again for it to slow before the In: a robot that never quite gets there still goes.
     */
    static final double SETTLE_MAX_MS = 500;

    /** Slower than {@code limitInPerSec}; an unknown speed counts as settled. */
    static boolean settled(double speedInPerSec, double limitInPerSec) {
        return Double.isNaN(speedInPerSec) || speedInPerSec < nn(limitInPerSec);
    }

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
     * Whether a target at {@code targetX} is on the same side of the centre line (x = the field's
     * centre) as a robot at {@code robotX}: the only targets an Auto may go to.
     */
    static boolean ownHalf(double robotX, double targetX) {
        double c = FieldFrame.FIELD_CENTRE_INCHES;
        return (robotX < c) == (targetX < c);
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
