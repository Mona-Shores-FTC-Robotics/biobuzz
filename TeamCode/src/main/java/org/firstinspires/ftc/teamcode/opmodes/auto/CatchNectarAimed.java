package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.CommandBuilder;
import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.autokit.AutoRegistry;
import org.firstinspires.ftc.teamcode.controls.MatchSetup;
import org.firstinspires.ftc.teamcode.localization.AutoLaunchSpots;
import org.firstinspires.ftc.teamcode.localization.HiveFieldPoints;
import org.firstinspires.ftc.teamcode.opmodes.TurretAimer;
import org.firstinspires.ftc.teamcode.subsystems.BeamBreak;
import org.firstinspires.ftc.teamcode.subsystems.BeamBreakSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.FeedWatch;
import org.firstinspires.ftc.teamcode.subsystems.TurretAim;
import org.firstinspires.ftc.teamcode.subsystems.TurretArc;
import org.firstinspires.ftc.teamcode.subsystems.TurretEncoder;
import org.firstinspires.ftc.teamcode.subsystems.TurretSubsystem;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.HiveCellState;
import org.firstinspires.ftc.teamcode.vision.HiveState;
import org.firstinspires.ftc.teamcode.vision.HiveTracker;
import org.firstinspires.ftc.teamcode.vision.Vec3;

import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

/** One optional shot after the normal preload volley. The chassis turns toward the roll while
 * the turret tracks the near CELL. Only the command can open the gate, once, after the turn. */
public final class CatchNectarAimed implements AutoRegistry.Approach {
    public static final double DECIDE_AT_S = 12.5;
    public static final double FEED_BACKSTOP_S = 1.0;
    private static final double MIN_FEED_BUDGET_S = 0.85;
    public static final double ARC_MARGIN_DEG = AutoLaunchSpots.AIMED_SHOT_ARC_MARGIN_DEG;
    /** This stationary catch only fires within two inches of its drawn centre. */
    private static final double MAX_RECEIVE_ERROR_IN = 2.0;
    /** Includes the expected wait after the turn and the one-ball feed, for the park planner. */
    public static final double PLANNED_S = 2.5;

    public enum Phase { WAIT, FEED, DONE }

    /** The robot and simulator sample this same decision once per loop, in seconds since PLAY. */
    public static final class Decision {
        private Phase phase = Phase.DONE;
        private int countAtOpen;

        public void reset(double startSeconds) {
            phase = startSeconds >= 0 && startSeconds <= DECIDE_AT_S ? Phase.WAIT : Phase.DONE;
        }

        public Phase step(double now, boolean safeAim, boolean loadedAndReady, int launched) {
            if (!Double.isFinite(now)) phase = Phase.DONE;
            if (phase == Phase.WAIT && now >= DECIDE_AT_S) {
                if (safeAim && loadedAndReady && launched >= 0
                        && now + MIN_FEED_BUDGET_S <= DECIDE_AT_S + FEED_BACKSTOP_S) {
                    phase = Phase.FEED;
                    countAtOpen = launched;
                } else {
                    phase = Phase.DONE;
                }
            } else if (phase == Phase.FEED && (!safeAim || launched != countAtOpen
                    || now >= DECIDE_AT_S + FEED_BACKSTOP_S)) {
                phase = Phase.DONE;
            }
            return phase;
        }
    }

    private final Robot robot;
    private final MatchSetup setup;
    private final PathEndSettle settle;
    private final LaunchCards launch;
    private final DoubleSupplier matchSeconds;
    private final Consumer<String> note;
    private final TurretAimer aimer;
    private final Decision decision = new Decision();
    private Phase phase = Phase.DONE;
    private Pose receivePose;
    private double spotBearingDeg = Double.NaN;
    private boolean sawCellGoDown;

    CatchNectarAimed(Robot robot, MatchSetup setup, PathEndSettle settle, LaunchCards launch,
                     DoubleSupplier matchSeconds, Consumer<String> note) {
        this.robot = robot;
        this.setup = setup;
        this.settle = settle;
        this.launch = launch;
        this.matchSeconds = matchSeconds;
        this.note = note == null ? line -> { } : note;
        aimer = new TurretAimer(robot);
    }

    /** An Auto has zero arc trim. Invalid or disabled limits cannot authorize this shot. */
    public static boolean insideArc(double deg) {
        return Double.isFinite(deg) && Double.isFinite(TurretSubsystem.arcLeftDeg)
                && Double.isFinite(TurretSubsystem.arcRightDeg) && TurretArc.active()
                && AimedVolley.insideArcBy(deg, TurretArc.left(), TurretArc.right(), ARC_MARGIN_DEG);
    }

    /** Compare this loop's CELL demand with the measured angle, even when the previous target was home. */
    public static boolean aligned(double measured, double demand, double spotBearing) {
        double error = Math.abs(TurretEncoder.wrap180(demand - measured));
        // An old/home target, even if reported on target, cannot authorize a shot off this spot's bearing.
        return insideArc(demand) && insideArc(measured) && insideArc(spotBearing)
                && Math.abs(TurretEncoder.wrap180(measured - spotBearing)) <= ARC_MARGIN_DEG
                && error <= Math.min(TurretSubsystem.holdToleranceDeg, ARC_MARGIN_DEG);
    }

    private boolean trackCell() {
        Alliance alliance = setup == null ? null : setup.alliance();
        if (receivePose != null && alliance != null && alliance != Alliance.UNKNOWN) {
            HiveTracker hive = robot.hive.of(alliance);
            if (hive != null && !hive.assumed()) {
                // The earlier camera check saw this CELL up. If it is positively seen down
                // since the turn started, its TIP opportunity is over, even after it leaves view.
                sawCellGoDown |= oppositeSettled(hive.state(), AutoRegistration.aimedLeft(alliance, receivePose.y()));
            }
        }
        if (!AimedVolley.canAim(robot.drive.poseReferenced(), TurretSubsystem.lockedForward,
                robot.turret.faultNote(), alliance) || !robot.turret.zeroed() || !robot.turret.holding()) return false;
        // start() would command home and re-arm a dropped hold. The Auto already holds its turret.
        aimer.update(alliance, TurretAimer.SWITCH_BAND_IN);
        return !sawCellGoDown && robot.turret.onTarget() && !robot.turret.atArcEdge() && Double.isFinite(robot.turret.power())
                && aligned(robot.turret.angleDegrees(), aimer.aimDeg(), spotBearingDeg);
    }

    /** UNSEEN after turning away supplies no evidence. Only a confirmed opposite rest vetoes the shot. */
    public static boolean oppositeSettled(HiveState observed, boolean aimingLeft) {
        return observed == (aimingLeft ? HiveState.RIGHT_CELL_UP : HiveState.LEFT_CELL_UP);
    }

    /** Turret bearing to the raised row from the approved drawing, in the robot's frame. */
    public static double spotBearing(Pose end, Alliance alliance) {
        if (end == null || alliance == null || alliance == Alliance.UNKNOWN) return Double.NaN;
        HiveCell cell = TurretAim.nearEndCell(alliance, end.y(), null, HiveFieldPoints.AXLE_Y_IN, 0);
        Vec3 row = cell == null ? null : HiveFieldPoints.rowCentre(cell, HiveCellState.UP);
        return row == null ? Double.NaN : TurretAim.fromFieldDeg(end.x(), end.y(), end.heading(),
                TurretSubsystem.turretForwardIn, TurretSubsystem.turretLeftIn, row.x(), row.y());
    }

    @Override public void started(Pose end) {
        receivePose = end;
        sawCellGoDown = false;
        spotBearingDeg = spotBearing(end, setup == null ? null : setup.alliance());
        robot.feeder.idle();
        launch.spinUpFor(end);
        trackCell();
    }

    @Override public void approaching(Pose pose, double remainingIn) {
        trackCell();
    }

    @Override public void cancel() {
        robot.feeder.idle();
        receivePose = null;
        spotBearingDeg = Double.NaN;
        phase = Phase.DONE;
        aimer.off();
        // Keep a dropped hold latched. A later command may not silently clear the fault here.
        if (robot.turret.holding() && robot.turret.faultNote() == null) robot.turret.home();
    }

    boolean feeding() { return phase == Phase.FEED; }

    public static boolean atReceiveSpot(Pose pose, Pose end) {
        double radius = PathEndSettle.launchWithinIn;
        double yaw = PathEndSettle.aimToleranceDeg;
        if (end == null || pose == null || !(radius > 0) || !Double.isFinite(radius)
                || !(yaw > 0) || !Double.isFinite(yaw)) return false;
        double error = Math.abs(TurretEncoder.wrap180(Math.toDegrees(pose.heading() - end.heading())));
        return Math.hypot(pose.x() - end.x(), pose.y() - end.y()) <= Math.min(radius, MAX_RECEIVE_ERROR_IN)
                && error <= Math.min(yaw, ARC_MARGIN_DEG);
    }

    static boolean pieceWaiting(FeedWatch.Lane lane, boolean behindGate, BeamBreak throat) {
        return lane == FeedWatch.Lane.HOLDS || (behindGate && throat != null && throat.blocked());
    }

    Command command() {
        return new CommandBuilder()
                .setStart(() -> {
                    robot.feeder.idle();
                    phase = Phase.WAIT;
                    decision.reset(matchSeconds.getAsDouble());
                })
                .setExecute(() -> {
                    boolean safe = trackCell() && atReceiveSpot(robot.drive.pose(), receivePose) && settle.settled()
                            && !robot.colorSensor.wrongNectarMayBeIn();
                    boolean ready = robot.launcher.atSpeed() && pieceWaiting(robot.laneHolds(),
                            BeamBreakSubsystem.throatBehindGate, robot.beams.throat());
                    Phase next = decision.step(matchSeconds.getAsDouble(), safe, ready, robot.launcher.launched());
                    if (next == Phase.FEED && phase != Phase.FEED) {
                        robot.feeder.feed();
                        note.accept("CatchNectarAimed: one ball, gate opened on CELL aim");
                    } else if (next == Phase.DONE) {
                        robot.feeder.idle();
                        if (phase == Phase.WAIT) note.accept("CatchNectarAimed: skipped, gate stayed shut");
                    }
                    phase = next;
                })
                .setDone(() -> phase == Phase.DONE)
                .setEnd(end -> cancel());
    }
}
