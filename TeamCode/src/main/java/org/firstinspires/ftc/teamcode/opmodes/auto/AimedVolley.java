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
 * The robot's turret for {@code LaunchAllAimed} ({@link LaunchCards#launchAllAimed}): a volley from
 * the park spot with the turret aimed at the CELL at the robot's end, the TeleOp's own aim
 * ({@link TurretAimer}: odometry and the CELL's field point, or the camera when it sees that CELL).
 * An Auto otherwise holds the turret at home; this aims it for the one card, then sends it home.
 *
 * <p>It does not fire (the robot stays parked, {@link #refusal()}): without a referenced pose (the
 * aim would be from nowhere), with a turret locked forward (it cannot aim) or one that dropped its
 * hold ({@code TurretDriveWatch}: a sign wrong, or its encoder not moving), without an alliance, or
 * once the gate would open too late for the pieces held to be out before AUTO ends
 * ({@link #gateDeadlineS}). The feed starts only with the turret on its aim and the aim inside the
 * firing arc ({@link #onTarget()}); the launcher's speed is the card's to check.
 *
 * <p>Why (19429, 18 Nov 2026, L-UnderHive 5:10 PM): the RIGHT-up wait gave up at 22.0 s, the robot
 * was parked at LEFT_PARK by 26.2 s with 2 POLLEN still in the lane, and the LEFT CELL was up (camera,
 * 23 s). From that pose the TeleOp's aim error was 22.4° at 56 to 60 in: well inside the arc.
 */
final class AimedVolley implements LaunchCards.AimedShot {

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

    /**
     * @param matchSeconds seconds since the Auto's PLAY: NaN before it
     */
    AimedVolley(Robot robot, MatchSetup setup, DoubleSupplier matchSeconds) {
        this.robot = robot;
        this.setup = setup;
        this.matchSeconds = matchSeconds;
        this.aimer = new TurretAimer(robot);
    }

    @Override public void start() {
        aimer.start();
    }

    @Override public void aim() {
        aimer.update(alliance(), TurretAimer.SWITCH_BAND_IN);
    }

    @Override public String refusal() {
        return refusal(robot.drive.poseReferenced(), TurretSubsystem.lockedForward, robot.turret.faultNote(),
                alliance(), matchSeconds.getAsDouble(), robot.colorSensor.lane().count());
    }

    @Override public boolean onTarget() {
        return robot.turret.onTarget() && TurretArc.allowsFeed(robot.turret.angleDegrees(), aimer.aimDeg(), 0.0);
    }

    @Override public void done() {
        aimer.off();
        robot.turret.home();
    }

    private Alliance alliance() {
        Alliance alliance = setup == null ? null : setup.alliance();
        return alliance == null ? Alliance.UNKNOWN : alliance;
    }

    /**
     * Why an aimed volley may not open the gate now, or null. Pure, so a test can check it. NaN match
     * seconds (no clock) is never too late.
     */
    static String refusal(boolean poseReferenced, boolean lockedForward, String turretFault, Alliance alliance,
                          double matchSeconds, int piecesHeld) {
        if (lockedForward) return "the turret is locked forward (TurretSubsystem.lockedForward)";
        if (turretFault != null) return "the turret dropped its hold: " + turretFault;
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
     * The latest match second the gate may open for {@code piecesHeld} (counted by the colour sensor;
     * 0 counts as 1) to be out by the end of AUTO: 30 s less the first ball's
     * {@link #FIRST_BALL_S} and {@link #NEXT_BALL_S} for each one after it. 4 held: 27.9 s.
     */
    static double gateDeadlineS(int piecesHeld) {
        int pieces = Math.max(1, piecesHeld);
        return AutoKit.AUTO_LENGTH_S - FIRST_BALL_S - NEXT_BALL_S * (pieces - 1);
    }
}
