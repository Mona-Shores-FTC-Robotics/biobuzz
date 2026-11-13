package org.firstinspires.ftc.teamcode;

import org.firstinspires.ftc.teamcode.hardware.ActiveConfig;
import org.firstinspires.ftc.teamcode.hardware.RobotIdentity;
import org.firstinspires.ftc.teamcode.subsystems.ColorSensorSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.IntakeSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.TurretSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.PrefeedSubsystem;

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
                "Turret servo (SWYFT, position mode, 320° range) points straight ahead at 0.514, on Bench:"
                        + " Turret (6 Nov 2026, checked again after flashing it out of SWYFT's continuous mode)",
                () -> TurretSubsystem.homePosition = 0.514);
        differs(RobotIdentity.TEAM_19429,
                "Cypher Max rides the goBILDA turret's 64T drive gear, 2.75 turns per turret turn"
                        + " (6 Nov 2026, from the build team; check one turret turn on Bench: Turret)",
                () -> TurretSubsystem.turnsPerTurretTurn = 2.75);
        differs(RobotIdentity.TEAM_19429,
                "Turret encoder trusted (Cypher Max in its socket, counting, 6 Nov 2026): the servo is"
                        + " re-homed from it, so a turret twirled in INIT comes back to its mark at PLAY",
                () -> TurretSubsystem.rehomeFromEncoder = true);
        differs(RobotIdentity.TEAM_19429,
                "Prefeed servo does not feed inward at the default direction; reversed makes it feed"
                        + " inward in Basic Drive's intake (6 Nov 2026)",
                () -> PrefeedSubsystem.reversed = true);
        differs(RobotIdentity.TEAM_19429,
                "Locked out of NECTAR for Belding: both colours, ours too, are spat out at the entrance"
                        + " and only POLLEN is kept (mentor's call, 7 Nov 2026)",
                () -> ColorSensorSubsystem.rejectAllNectar = true);
        differs(RobotIdentity.TEAM_19429,
                "Intake star wheels split onto two servos: right on Control Hub servo port 4, left on"
                        + " port 5 (13 Nov 2026, from the build team)",
                () -> IntakeSubsystem.rightStarWheel = true);

        // ---------------------------------------------------------------- 20245 (Ultraviolet)
        // Nothing yet: the shared values were mostly measured on 20245.
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
