package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.autokit.AutoKit;
import org.firstinspires.ftc.teamcode.autokit.AutoRegistry;
import org.firstinspires.ftc.teamcode.localization.HiveFieldPoints;
import org.firstinspires.ftc.teamcode.controls.MatchSetup;
import org.firstinspires.ftc.teamcode.localization.AutoLaunchSpots;
import org.firstinspires.ftc.teamcode.opmodes.TurretAimer;
import org.firstinspires.ftc.teamcode.subsystems.TurretAim;
import org.firstinspires.ftc.teamcode.subsystems.TurretArc;
import org.firstinspires.ftc.teamcode.subsystems.TurretSubsystem;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.HiveCellState;
import org.firstinspires.ftc.teamcode.vision.Vec3;

import java.util.Locale;
import java.util.function.DoubleSupplier;

/**
 * The robot's turret for {@code LaunchAllAimed} ({@link LaunchCards#launchAllAimed}): a volley from the
 * park spot after the park, with the turret aimed at the CELL at the robot's end by the TeleOp's own aim
 * ({@link TurretAimer}: odometry and the CELL's field point, or the camera when it sees that CELL) to take
 * up what the park spot's chassis aim leaves. An Auto otherwise holds the turret at home; this aims it
 * for the one card, then sends it home.
 *
 * <p>It does not fire, and the robot stays parked ({@link #refusal()}), without a referenced pose or an
 * alliance, or once the gate would open too late for the pieces held to be out before AUTO ends
 * ({@link #gateDeadlineS}). A turret locked forward, or one that dropped its hold ({@code TurretDriveWatch}),
 * holds home and the volley fires from there, as a {@code LaunchAll}, only where home points at the CELL
 * ({@link #homeRefusal}: a spot that aims a locked turret, as the park spots do); anywhere else it does not fire
 * and the robot keeps its load. While the turret aims, the gate waits for it on target with the aim inside the
 * firing arc, less the card's margin ({@link #onTarget()}).
 *
 * <p>Why (19429, 18 Nov 2026, L-UnderHive 5:10 PM): the RIGHT-up wait gave up at 22.0 s, the robot was
 * parked at LEFT_PARK by 26.2 s with 2 POLLEN still in the lane, and the LEFT CELL was up (camera, 23 s).
 * The mentor's trial parks facing the LEFT CELL (LEFT_AUTO_PARK) and fires those 2 in AUTO (19 Nov 2026).
 */
final class AimedVolley implements LaunchCards.AimedShot, AutoRegistry.Approach {

    /**
     * Gate open to the first ball out, and each ball after it, seconds (19429, 18 Nov 2026: about
     * 0.75 s, then one about every 0.45 s). With {@link AutoKit#AUTO_LENGTH_S} they set the last
     * moment the gate may open ({@link #gateDeadlineS}).
     */
    static final double FIRST_BALL_S = 0.75;
    static final double NEXT_BALL_S = 0.45;

    private final Robot robot;
    private final MatchSetup setup;
    private final DoubleSupplier matchSeconds;
    private final TurretAimer aimer;
    /** Asked only where the CELL lies from home ({@link TurretAimer#solve}); it never moves the turret. */
    private final TurretAimer homeProbe;
    private final double arcMarginDeg;
    private final PathEndSettle receiveSpot;
    /** The card is under way; it asked the turret to aim. */
    private boolean active;
    private boolean aiming;
    private double drawnBearing = Double.NaN;
    private boolean keepThroughBranch;

    /**
     * @param matchSeconds seconds since the Auto's PLAY: NaN before it
     */
    AimedVolley(Robot robot, MatchSetup setup, DoubleSupplier matchSeconds) {
        this(robot, setup, matchSeconds, null, 0);
    }

    /**
     * @param receiveSpot the driven path's endpoint and settle check for a one-ball receive
     */
    AimedVolley(Robot robot, MatchSetup setup, DoubleSupplier matchSeconds, PathEndSettle receiveSpot) {
        this(robot, setup, matchSeconds, receiveSpot, AutoLaunchSpots.AIMED_SHOT_ARC_MARGIN_DEG);
    }

    private AimedVolley(Robot robot, MatchSetup setup, DoubleSupplier matchSeconds, PathEndSettle receiveSpot,
                        double arcMarginDeg) {
        this.receiveSpot = receiveSpot;
        this.arcMarginDeg = arcMarginDeg;
        this.robot = robot;
        this.setup = setup;
        this.matchSeconds = matchSeconds;
        this.aimer = new TurretAimer(robot);
        this.homeProbe = new TurretAimer(robot);
    }

    @Override public void start() {
        active = true;
        aiming = false;
        keepThroughBranch = false;
        drawnBearing = Double.NaN;
        if (receiveSpot != null && receiveSpot.endPose() != null) {
            Pose end = receiveSpot.endPose();
            HiveCell cell = TurretAim.nearEndCell(alliance(), end.y(), null, HiveFieldPoints.AXLE_Y_IN, 0);
            Vec3 row = cell == null ? null : HiveFieldPoints.rowCentre(cell, HiveCellState.UP);
            if (row != null) drawnBearing = TurretAim.fromFieldDeg(end.x(), end.y(), end.heading(),
                    TurretSubsystem.turretForwardIn, TurretSubsystem.turretLeftIn, row.x(), row.y());
        }
    }

    // The catch's approach moves only the turret. Its intake starts once the drive ends.
    @Override public boolean aimDuringApproachToWait() { return arcMarginDeg > 0; }
    @Override public void holdThroughWaitBranch() { keepThroughBranch = true; }
    void catchEnded() { if (!keepThroughBranch) done(); }
    @Override public void started(Pose end) { start(); aim(); }
    @Override public void approaching(Pose pose, double remainingIn) { aim(); }
    @Override public void cancel() { done(); }

    @Override public boolean aim() {
        boolean can = canAim(robot.drive.poseReferenced(), TurretSubsystem.lockedForward,
                robot.turret.faultNote(), alliance());
        if (arcMarginDeg > 0) {
            // start() on the aimer sends the turret home and re-arms a dropped hold. An angled
            // receive must keep an existing, zeroed hold and must leave a fault latched.
            if (!can || !Double.isFinite(drawnBearing) || !robot.turret.zeroed() || !robot.turret.holding()) return false;
            aimer.update(alliance(), TurretAimer.SWITCH_BAND_IN);
            return Double.isFinite(aimer.aimDeg()) && robot.turret.faultNote() == null;
        }
        if (can) {
            if (!aiming) aimer.start();
            aiming = true;
            aimer.update(alliance(), TurretAimer.SWITCH_BAND_IN);
            // The aim dropped the hold this loop: home, from the next one.
            if (robot.turret.faultNote() == null) return true;
        }
        if (aiming) {
            aiming = false;
            aimer.off();
            robot.turret.home();
        }
        return false;
    }

    @Override public boolean onTarget() {
        if (arcMarginDeg > 0) {
            return receiveSpot != null && receiveSpot.settled() && robot.turret.onTarget() && !robot.turret.atArcEdge()
                    && receiveAligned(robot.turret.angleDegrees(), aimer.aimDeg(), drawnBearing, arcMarginDeg);
        }
        return robot.turret.onTarget() && TurretArc.allowsFeed(robot.turret.angleDegrees(), aimer.aimDeg(), 0.0)
                && insideArcBy(aimer.aimDeg(), TurretSubsystem.arcLeftDeg, TurretSubsystem.arcRightDeg, arcMarginDeg);
    }

    @Override public String refusal() {
        String why = parkRefusal(robot.drive.poseReferenced(), alliance(), matchSeconds.getAsDouble(),
                robot.colorSensor.lane().count());
        if (why != null) return why;
        if (arcMarginDeg > 0) {
            return canAim(true, TurretSubsystem.lockedForward, robot.turret.faultNote(), alliance())
                    && robot.turret.holding() && robot.turret.zeroed() ? null : "no zeroed turret hold on the CELL";
        }
        // A turret that cannot aim fires from home only where home points at the CELL (a spot that aims a locked
        // turret). Anywhere else it keeps its load: home there points somewhere nobody chose.
        if (canAim(true, TurretSubsystem.lockedForward, robot.turret.faultNote(), alliance())) return null;
        boolean solved = homeProbe.solve(alliance(), TurretAimer.SWITCH_BAND_IN);
        return homeRefusal(solved ? homeProbe.aimDeg() : Double.NaN);
    }

    @Override public void done() {
        if (!active) return;
        active = false;
        aimer.off();
        if (arcMarginDeg > 0) {
            if (robot.turret.holding() && robot.turret.faultNote() == null) robot.turret.home();
        } else if (aiming || !robot.turret.holding()) robot.turret.home();
        aiming = false;
    }

    /** Check the measured angle against this loop's demand and the near CELL's field bearing.
     * The motor's on-target bit can still describe the previous loop's home setpoint. */
    static boolean receiveAligned(double measured, double demand, double fieldAim, double margin) {
        double left = TurretArc.left(), right = TurretArc.right();
        return TurretArc.active() && Double.isFinite(TurretSubsystem.arcLeftDeg)
                && Double.isFinite(TurretSubsystem.arcRightDeg) && margin > 0
                && insideArcBy(measured, left, right, margin) && insideArcBy(demand, left, right, margin)
                && insideArcBy(fieldAim, left, right, margin)
                && Math.abs(Math.IEEEremainder(measured - fieldAim, 360)) <= margin
                && Math.abs(Math.IEEEremainder(measured - demand, 360)) <= Math.min(margin, TurretSubsystem.holdToleranceDeg);
    }

    private Alliance alliance() {
        Alliance alliance = setup == null ? null : setup.alliance();
        return alliance == null ? Alliance.UNKNOWN : alliance;
    }

    /**
     * How far from straight ahead the CELL may lie for a turret that cannot aim (locked forward, a dropped hold) to
     * fire from home, degrees: a spot whose heading aims a locked turret, with the pose's and the hold's slack.
     */
    static final double HOME_AIM_TOLERANCE_DEG = 3.0;

    /**
     * Why a turret that cannot aim may not fire from home, or null when it may: the CELL lies {@code aimDeg} from
     * the robot's forward (+ left; NaN: not known), and home is a shot at it only within
     * {@link #HOME_AIM_TOLERANCE_DEG}. Pure.
     */
    static String homeRefusal(double aimDeg) {
        if (Double.isNaN(aimDeg)) return "the turret cannot aim and where the CELL lies from home is not known";
        double off = Math.IEEEremainder(aimDeg, 360.0);
        if (Math.abs(off) <= HOME_AIM_TOLERANCE_DEG) return null;
        return String.format(Locale.US, "the turret cannot aim (locked forward, or it dropped its hold) and home points %.0f deg"
                + " off the CELL", Math.abs(off));
    }

    /**
     * Whether an aim {@code aimDeg} from the robot's forward (+ left) is at least {@code marginDeg} inside an arc
     * of {@code arcLeftDeg} to the left and {@code arcRightDeg} to the right. No margin asks nothing; a NaN aim
     * with a margin is not inside. Pure.
     */
    static boolean insideArcBy(double aimDeg, double arcLeftDeg, double arcRightDeg, double marginDeg) {
        if (!(marginDeg > 0)) return true;
        if (Double.isNaN(aimDeg)) return false;
        double aim = Math.IEEEremainder(aimDeg, 360.0);
        return aim <= arcLeftDeg - marginDeg && aim >= -(arcRightDeg - marginDeg);
    }

    /** Whether the turret may aim: a referenced pose, an alliance, not locked forward, no dropped hold. Pure. */
    static boolean canAim(boolean poseReferenced, boolean lockedForward, String turretFault, Alliance alliance) {
        return poseReferenced && !lockedForward && turretFault == null
                && alliance != null && alliance != Alliance.UNKNOWN;
    }

    /**
     * Why the volley may not open the gate now, or null. Pure, so a test can check it. A turret that
     * cannot aim is not a reason: it fires from home. NaN match seconds (no clock) is never too late.
     */
    static String parkRefusal(boolean poseReferenced, Alliance alliance, double matchSeconds, int piecesHeld) {
        if (!poseReferenced) return "the pose is not referenced on the field";
        if (alliance == null || alliance == Alliance.UNKNOWN) return "no alliance";
        double deadline = gateDeadlineS(piecesHeld);
        if (matchSeconds > deadline) {
            return String.format(Locale.US, "%.1f s on the match clock, past %.1f s: the last of %d pieces would be out after AUTO",
                    matchSeconds, deadline, Math.max(1, piecesHeld));
        }
        return null;
    }

    /**
     * The latest match second the gate may open for {@code piecesHeld} (counted by the colour
     * sensor; 0 counts as 1) to be out by the end of AUTO: 30 s less the first ball's
     * {@link #FIRST_BALL_S} and {@link #NEXT_BALL_S} for each one after it. 4 held: 27.9 s.
     */
    static double gateDeadlineS(int piecesHeld) {
        int pieces = Math.max(1, piecesHeld);
        return AutoKit.AUTO_LENGTH_S - FIRST_BALL_S - NEXT_BALL_S * (pieces - 1);
    }
}
