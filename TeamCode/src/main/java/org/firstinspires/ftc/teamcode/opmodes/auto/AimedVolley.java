package org.firstinspires.ftc.teamcode.opmodes.auto;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.autokit.AutoKit;
import org.firstinspires.ftc.teamcode.controls.MatchSetup;
import org.firstinspires.ftc.teamcode.opmodes.TurretAimer;
import org.firstinspires.ftc.teamcode.subsystems.TurretArc;
import org.firstinspires.ftc.teamcode.subsystems.TurretSubsystem;
import org.firstinspires.ftc.teamcode.util.Alliance;

import java.util.Locale;
import java.util.function.DoubleSupplier;

/**
 * The robot's turret for an aimed volley ({@link LaunchCards.AimedShot}): aimed at the CELL at the
 * robot's end with the TeleOp's own aim ({@link TurretAimer}: odometry and the CELL's field point, or the
 * camera when it sees that CELL), to take up what the chassis leaves. An Auto otherwise holds the turret
 * at home; this aims it for one card, then sends it home.
 *
 * <p>It aims only with a referenced pose, an alliance, and a turret that is neither locked forward nor
 * has dropped its hold ({@code TurretDriveWatch}); otherwise the turret holds home and the volley fires
 * from there, as a {@code LaunchAll} does, since the chassis already faces the CELL. The gate waits for
 * the turret only while it aims ({@link #onTarget()}: on its aim, and the aim inside the firing arc).
 *
 * <p>Two kinds ({@link Kind}). A {@link Kind#SPOT} volley ({@code LaunchAllAimed}, from a launch spot)
 * always fires. A {@link Kind#PARK} volley ({@code LaunchAllFromPark}, from the park spot after the park)
 * does not fire, and the robot stays parked, without a referenced pose or an alliance, or once the gate
 * would open too late for the pieces held to be out before AUTO ends ({@link #gateDeadlineS}).
 *
 * <p>Why (19429, 18 Nov 2026, L-UnderHive 5:10 PM): the RIGHT-up wait gave up at 22.0 s, the robot was
 * parked at LEFT_PARK by 26.2 s with 2 POLLEN still in the lane, and the LEFT CELL was up (camera, 23 s).
 * The mentor's trial parks facing the LEFT CELL (LEFT_AUTO_PARK) and fires there, with the turret aiming
 * through all of the Auto's volleys (19 Nov 2026).
 */
final class AimedVolley implements LaunchCards.AimedShot {

    /** Which volley this aims: from a launch spot, or from the park spot after the park. */
    enum Kind { SPOT, PARK }

    /**
     * Gate open to the first ball out, and each ball after it, seconds (19429, 18 Nov 2026: about
     * 0.75 s, then one about every 0.45 s). With {@link AutoKit#AUTO_LENGTH_S} they set the last
     * moment a park volley's gate may open ({@link #gateDeadlineS}).
     */
    static final double FIRST_BALL_S = 0.75;
    static final double NEXT_BALL_S = 0.45;

    private final Robot robot;
    private final MatchSetup setup;
    private final DoubleSupplier matchSeconds;
    private final Kind kind;
    private final TurretAimer aimer;
    /** A card (or its approach) is under way; aiming was given up for it; it asked the turret to aim. */
    private boolean active;
    private boolean stopped;
    private boolean aiming;

    /**
     * @param matchSeconds seconds since the Auto's PLAY: NaN before it
     */
    AimedVolley(Robot robot, MatchSetup setup, DoubleSupplier matchSeconds, Kind kind) {
        this.robot = robot;
        this.setup = setup;
        this.matchSeconds = matchSeconds;
        this.kind = kind;
        this.aimer = new TurretAimer(robot);
    }

    @Override public void start() {
        // The approach starts it; the card after it carries on with the same aim.
        if (active) return;
        active = true;
        stopped = false;
        aiming = false;
    }

    @Override public boolean aim() {
        boolean can = !stopped && canAim(robot.drive.poseReferenced(), TurretSubsystem.lockedForward,
                robot.turret.faultNote(), alliance());
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
        return robot.turret.onTarget() && TurretArc.allowsFeed(robot.turret.angleDegrees(), aimer.aimDeg(), 0.0);
    }

    @Override public void stopAiming() {
        stopped = true;
        if (aiming) {
            aiming = false;
            aimer.off();
            robot.turret.home();
        }
    }

    @Override public String refusal() {
        if (kind != Kind.PARK) return null;
        return parkRefusal(robot.drive.poseReferenced(), alliance(), matchSeconds.getAsDouble(),
                robot.colorSensor.lane().count());
    }

    @Override public void done() {
        if (!active) return;
        active = false;
        aimer.off();
        if (aiming || !robot.turret.holding()) robot.turret.home();
        aiming = false;
    }

    private Alliance alliance() {
        Alliance alliance = setup == null ? null : setup.alliance();
        return alliance == null ? Alliance.UNKNOWN : alliance;
    }

    /** Whether the turret may aim: a referenced pose, an alliance, not locked forward, no dropped hold. Pure. */
    static boolean canAim(boolean poseReferenced, boolean lockedForward, String turretFault, Alliance alliance) {
        return poseReferenced && !lockedForward && turretFault == null
                && alliance != null && alliance != Alliance.UNKNOWN;
    }

    /**
     * Why a park volley may not open the gate now, or null. Pure, so a test can check it. A turret that
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
     * The latest match second a park volley's gate may open for {@code piecesHeld} (counted by the colour
     * sensor; 0 counts as 1) to be out by the end of AUTO: 30 s less the first ball's
     * {@link #FIRST_BALL_S} and {@link #NEXT_BALL_S} for each one after it. 4 held: 27.9 s.
     */
    static double gateDeadlineS(int piecesHeld) {
        int pieces = Math.max(1, piecesHeld);
        return AutoKit.AUTO_LENGTH_S - FIRST_BALL_S - NEXT_BALL_S * (pieces - 1);
    }
}
