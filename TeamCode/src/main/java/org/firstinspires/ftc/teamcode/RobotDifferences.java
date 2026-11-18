package org.firstinspires.ftc.teamcode;

import org.firstinspires.ftc.teamcode.hardware.ActiveConfig;
import org.firstinspires.ftc.teamcode.hardware.RobotIdentity;
import org.firstinspires.ftc.teamcode.subsystems.ColorSensorSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.FlowerStickSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.IntakeSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.LaunchSpeedTable;
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
                "New FLOWER stick's pivot measured in Bench: FLOWER Stick: in at 0.06, out at 0.55,"
                        + " the other way round from 20245's stick (17 Nov 2026)",
                () -> {
                    FlowerStickSubsystem.RETRACTED = 0.06;
                    FlowerStickSubsystem.EXTENDED = 0.55;
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
                "Shoots long: every ball of the Auto's volleys long, not only the first, so it is the"
                        + " speed, not the early gate (gateLeadS 0.3 in that run); 50 ticks/s off every"
                        + " launch speed, TeleOp and Autos, as 20245 (mentor, 18 Nov 2026)",
                () -> LaunchSpeedTable.robotOffsetTicksPerSecond = -50.0);

        // ---------------------------------------------------------------- 20245 (Ultraviolet)
        // The shared values were mostly measured on 20245.
        differs(RobotIdentity.TEAM_20245,
                "Turret unlocked: the 25-3 Speed servo and the REV Through Bore encoder are fitted,"
                        + " checked on Bench: Turret, and it held and aimed in Basic Drive (17 Nov 2026)",
                () -> TurretSubsystem.lockedForward = false);
        differs(RobotIdentity.TEAM_20245,
                "Shoots long at every range: 50 ticks/s off every launch speed, TeleOp and Autos (drive"
                        + " team, 17 Nov 2026, after the turret was refitted)",
                () -> LaunchSpeedTable.robotOffsetTicksPerSecond = -50.0);
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
