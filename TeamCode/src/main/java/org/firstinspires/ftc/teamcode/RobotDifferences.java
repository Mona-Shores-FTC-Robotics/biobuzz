package org.firstinspires.ftc.teamcode;

import org.firstinspires.ftc.teamcode.hardware.ActiveConfig;
import org.firstinspires.ftc.teamcode.hardware.RobotIdentity;
import org.firstinspires.ftc.teamcode.opmodes.GardenGather;
import org.firstinspires.ftc.teamcode.opmodes.TeleOpConfig;
import org.firstinspires.ftc.teamcode.opmodes.auto.BuiltAuto;
import org.firstinspires.ftc.teamcode.opmodes.auto.LaunchCards;
import org.firstinspires.ftc.teamcode.subsystems.ColorSensorSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.FlowerStickSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.IntakeSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.PrefeedSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.TurretSubsystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * <b>The one place a robot's mechanism values differ from the other robot's.</b>
 *
 * <p>Both robots run the same code and the same numbers: each mechanism's {@code @Configurable}
 * field (say {@code IntakeSubsystem.reversed}) is the shared value. When one robot has to be
 * different — its intake motor was swapped and now turns the other way, its stick servo sits at a
 * different position, it needs a feature switched off — that is <b>one line below</b>, under that
 * robot, with the reason. Nothing else changes, and the other robot is untouched.
 *
 * <pre>
 *   differs(RobotIdentity.TEAM_19429, "Intake motor swapped, turns the other way (5 Nov 2026)",
 *           () -&gt; IntakeSubsystem.reversed = false);
 * </pre>
 *
 * <p>Then Sloth Load. {@code RobotOpMode} applies the active robot's lines before the robot is
 * built, so every constructor and every loop sees them, and the match log's Metadata tab lists them
 * under {@code RobotDifferences} (and the PLAY snapshot of every tunable shows the values that ran).
 *
 * <h2>Rules</h2>
 *
 * <ul>
 *   <li><b>Only a value that was seen to differ on that robot.</b> Not "probably", not "to be
 *       safe": an unneeded line is a second copy of a number that drifts from the first.</li>
 *   <li><b>The shared value stays in the subsystem.</b> A line here assigns a {@code public static}
 *       field of a {@code @Configurable} class, nothing else. If the difference is behaviour, not a
 *       number ("19429 has no colour sensor yet"), add a {@code public static boolean} switch to
 *       that subsystem with the shared default and flip it here.</li>
 *   <li><b>Device ports are not here.</b> A motor on a different port, or a different kind of
 *       motor, is that robot's {@code res/xml/robot_<name>.xml}. Pedro's drivetrain, Pinpoint and
 *       Foresight values are {@code pedro/robots/Robot<team>.java}.</li>
 *   <li><b>Once per app run.</b> A robot's lines are applied the first time an OpMode runs, so a
 *       Panels edit made later sticks for the rest of the session, as it does on the shared
 *       values. If the active config then changes to another robot after a robot with lines ran,
 *       the next OpMode refuses to start until the Robot Controller app is restarted, rather than
 *       run one robot with the other's values (last season's bug).</li>
 * </ul>
 */
public final class RobotDifferences {

    private RobotDifferences() {}

    private static final Table TABLE = new Table();

    static {
        // ---------------------------------------------------------------- 19429 (Infrared)
        // One line per difference, with what was seen and the date, and the subsystem's import at
        // the top of this file.
        differs(RobotIdentity.TEAM_19429,
                "Prefeed servo does not feed inward at the default direction; reversed makes it feed"
                        + " inward in Basic Drive's intake (6 Nov 2026)",
                () -> PrefeedSubsystem.reversed = true);
        differs(RobotIdentity.TEAM_19429,
                "Intake star wheels split onto two servos, right and left (13 Nov 2026, from the build"
                        + " team; their ports are in robot_19429.xml)",
                () -> IntakeSubsystem.rightStarWheel = true);
        differs(RobotIdentity.TEAM_19429,
                "New FLOWER stick with a roller on its end, a continuous-rotation servo (17 Nov 2026,"
                        + " from the build team; its port is in robot_19429.xml)",
                () -> FlowerStickSubsystem.roller = true);
        differs(RobotIdentity.TEAM_19429,
                "FLOWER stick's pivot measured in Bench: FLOWER Stick: in at 1.00, out at 0.44"
                        + " (mentor, 20 Nov 2026; out was 0.49 from the Driver Station's recorded values"
                        + " earlier that day, then set to 0.44 by the mentor; the swing now runs the"
                        + " other way: was in 0.30, out 0.91 on 18 Nov)",
                () -> {
                    FlowerStickSubsystem.RETRACTED = 1.0;
                    FlowerStickSubsystem.EXTENDED = 0.44;
                });
        differs(RobotIdentity.TEAM_19429,
                "Lane colour sensor's empty lane reads 0.064 (top 0.1% 0.088) since more ambient light"
                        + " reached the lane on 16 Nov, above the shared leave level 0.05, so a piece never"
                        + " left and the lane read FULL empty; leave at 0.09, presence at 0.11 (dim POLLEN"
                        + " parked 0.09 to 0.25), from 52 runs of 16 and 17 Nov 2026. Re-measure after"
                        + " the black bottom plate (mentor, 18 Nov 2026; was presentAbove 0.068, 17 Nov)",
                () -> {
                    ColorSensorSubsystem.leaveBelow = 0.09;
                    ColorSensorSubsystem.presentAbove = 0.11;
                });
        differs(RobotIdentity.TEAM_19429,
                "Turret unlocked: rewired like 20245, the 25-3 Speed servo (continuous) on Expansion Hub"
                        + " servo port 4 and the REV Through Bore encoder in the intake's encoder socket"
                        + " (mentor, 18 Nov 2026)",
                () -> TurretSubsystem.lockedForward = false);
        differs(RobotIdentity.TEAM_19429,
                "Shots sit to one side: turret aimed 5 degrees round from its mark, what worked in"
                        + " Panels with the speed and gate lines here (mentor, 18 Nov 2026)",
                () -> TurretSubsystem.aimOffsetDeg = 5.0);
        differs(RobotIdentity.TEAM_19429,
                "Turret floor 0.065: on Bench: Turret it turns steadily at +0.06 (10 to 12 deg/s) and"
                        + " -0.065 (10 to 13 deg/s), the larger kept; smoother than 20245 (0.09, sticky below"
                        + " that). With 20245's floor it came in fast, coasted past its band and hunted (mentor,"
                        + " 20 Nov 2026)",
                () -> TurretSubsystem.minPower = 0.065);
        differs(RobotIdentity.TEAM_19429,
                "Turret kP 0.008, from 20245's 0.02: with its lower floor (0.065) and smoother turret,"
                        + " 0.02 came in fast and hunted; 0.008 set in Panels by the mentor (20 Nov 2026)",
                () -> TurretSubsystem.kP = 0.008);
        differs(RobotIdentity.TEAM_19429,
                "Early gate: the Auto opens the feed gate 0.1 s ahead of launch speed (LaunchCards"
                        + " gateLeadS), set in Panels with the speed and aim lines here (mentor, 18 Nov 2026)",
                () -> LaunchCards.gateLeadS = 0.1);

        differs(RobotIdentity.TEAM_19429,
                "Black bottom plate under the intake: it may drive backward with a load, so the"
                        + " backward-leg Auto trials may run (mentor, 18 Nov 2026; doc/robot-todo.md has"
                        + " the backward drive that must pass first)",
                () -> IntakeSubsystem.holdsLoadDrivingBackward = true);
        differs(RobotIdentity.TEAM_19429,
                "GARDEN gather's drive in ends 1 in short of the wall, not 3: at 20245's -3 it did not"
                        + " go deep enough into the corner, likely its roller FLOWER stick meeting the wall"
                        + " sooner; -1 was good in Panels (mentor, 21 Nov 2026)",
                () -> GardenGather.driveIn.extraDepthIn = -1.0);
        differs(RobotIdentity.TEAM_19429,
                "Keeps only POLLEN: the reject spits out both NECTAR colours, ours too (mentor, 21 Nov 2026)",
                () -> ColorSensorSubsystem.rejectAllNectar = true);

        // ---------------------------------------------------------------- 20245 (Ultraviolet)
        // The shared values were mostly measured on 20245.
        differs(RobotIdentity.TEAM_20245,
                "Turret unlocked: the 25-3 Speed servo and the REV Through Bore encoder are fitted,"
                        + " checked on Bench: Turret, and it held and aimed in Basic Drive (17 Nov 2026)",
                () -> TurretSubsystem.lockedForward = false);
        differs(RobotIdentity.TEAM_20245,
                "Shots sit to one side: turret aimed 5 degrees round from its mark, set by the mentor"
                        + " with the speed table measured that night (18 Nov 2026; 6 until 20 Nov 2026,"
                        + " when the mentor set it to 5)",
                () -> TurretSubsystem.aimOffsetDeg = 5.0);
        differs(RobotIdentity.TEAM_20245,
                "Firing arc not enforced in the TeleOp: the driver did not see why a shot was held back"
                        + " and the arc refused shots the driver wanted, so the driver makes the call"
                        + " (mentor, 20 Nov 2026; the turret still holds at the arc's edge, the Auto still"
                        + " enforces it)",
                () -> TeleOpConfig.enforceFiringArc = false);
        differs(RobotIdentity.TEAM_20245,
                "Bottom plate under the intake, half fitted: it may drive backward with a load, so the"
                        + " backward-leg Auto trials may run (mentor, 20 Nov 2026, on before the plate is finished;"
                        + " doc/robot-todo.md step 9 has the backward drive that must pass first, and the lane"
                        + " sensor to re-measure with the plate)",
                () -> IntakeSubsystem.holdsLoadDrivingBackward = true);
        differs(RobotIdentity.TEAM_20245,
                "FLOWER stick's SWYFT servo holds the stick out instead of going limp: limp, the FLOWER"
                        + " lifted the stick mid push. In R-Solo practice (21 Nov 2026) the second pick lowers"
                        + " the stick on the drive in, 1.3 s before the push, so SETTLE_MS (1.5 s) cut the"
                        + " pulse 0.2 s into IN; that pick stalled 2.2 to 4.7 in short with 0 to 2 POLLEN,"
                        + " while the first pick, its pulse cut only as IN stalled, took 3 to 4. TeleOp picks"
                        + " went limp mid IN too",
                () -> FlowerStickSubsystem.LIMP_EXTENDED = false);
        differs(RobotIdentity.TEAM_20245,
                "Endgame guard goes for a volley before the park (BuiltAuto.parkAfterVolley off, the"
                        + " 4 Nov rule): beside a partner whose TIP 2 comes early, R-NoCross's GARDEN volley"
                        + " fits by 30 s but not with the park after it (mentor, 21 Nov 2026)",
                () -> BuiltAuto.parkAfterVolley = false);
    }

    /**
     * Declares that {@code robot} differs: {@code why} is what was seen, with the date, and
     * {@code assignment} sets the field, e.g. {@code () -> IntakeSubsystem.reversed = false}.
     */
    private static void differs(RobotIdentity robot, String why, Runnable assignment) {
        TABLE.add(robot, why, assignment);
    }

    /**
     * Applies the active robot's lines (once per app run) and returns their reasons, empty if it
     * runs the shared values.
     *
     * @throws IllegalStateException if no config, or an unknown one, is active
     *     ({@link ActiveConfig#requireIdentity()}), or if another robot's lines were applied
     *     earlier in this app run.
     */
    public static List<String> applyForActiveRobot() {
        return TABLE.apply(ActiveConfig.requireIdentity());
    }

    /** The reasons listed for {@code robot}, without applying anything. */
    public static List<String> of(RobotIdentity robot) {
        return TABLE.reasons(robot);
    }

    /** The table and its once-per-run bookkeeping, apart from the real lines so it can be tested. */
    static final class Table {

        private final Map<RobotIdentity, List<String>> reasons = new EnumMap<>(RobotIdentity.class);
        private final Map<RobotIdentity, List<Runnable>> assignments = new EnumMap<>(RobotIdentity.class);
        private RobotIdentity applied;

        void add(RobotIdentity robot, String why, Runnable assignment) {
            if (robot == null || assignment == null) {
                throw new IllegalArgumentException("RobotDifferences: a line needs a robot and an assignment");
            }
            if (why == null || why.trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "RobotDifferences: every line for " + robot + " says why it differs, and when");
            }
            listFor(reasons, robot).add(why.trim());
            listFor(assignments, robot).add(assignment);
        }

        List<String> reasons(RobotIdentity robot) {
            List<String> list = reasons.get(robot);
            return list == null ? Collections.<String>emptyList() : Collections.unmodifiableList(list);
        }

        synchronized List<String> apply(RobotIdentity robot) {
            if (robot == null) {
                throw new IllegalArgumentException("RobotDifferences: no robot to apply");
            }
            if (applied == robot) {
                return reasons(robot);
            }
            if (applied != null && !reasons(applied).isEmpty()) {
                throw new IllegalStateException(
                        "The active config changed from " + applied.configName + " to " + robot.configName
                                + " since this app started, and " + applied.configName + "'s robot "
                                + "differences are still loaded. Restart the Robot Controller app so "
                                + robot.configName + " runs its own values.");
            }
            List<Runnable> list = assignments.get(robot);
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    list.get(i).run();
                }
            }
            applied = robot;
            return reasons(robot);
        }

        private static <T> List<T> listFor(Map<RobotIdentity, List<T>> map, RobotIdentity robot) {
            List<T> list = map.get(robot);
            if (list == null) {
                list = new ArrayList<>();
                map.put(robot, list);
            }
            return list;
        }
    }
}
