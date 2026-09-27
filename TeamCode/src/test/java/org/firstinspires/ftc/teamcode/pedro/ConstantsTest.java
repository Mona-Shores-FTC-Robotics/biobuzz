package org.firstinspires.ftc.teamcode.pedro;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.firstinspires.ftc.teamcode.hardware.DeviceNames;
import org.firstinspires.ftc.teamcode.hardware.RobotIdentity;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Pins how {@link Constants} picks a robot's Pedro values, and the untuned-Foresight guard.
 *
 * <p>Everything here goes through the pure {@link Constants#forRobot(RobotIdentity)} and
 * {@link Constants#createAlgorithm(RobotIdentity)}. The no-argument factories read the active
 * Driver Station configuration through {@code ActiveConfig}, which needs the Robot Controller app
 * and cannot run on a build machine.
 *
 * <h2>Selection</h2>
 *
 * The bug this guards against is last season's: a silent 19429/20245 fallback ran one robot on
 * the other's tuning. So the tests insist that the two competition robots get <em>different</em>
 * objects (a copied robot file still pointing at the other robot's fields would pass everything
 * else), that a robot with no drivetrain is refused by name, and that every robot carrying the
 * drivetrain has values — so adding a {@code RobotIdentity} without its Pedro file fails CI, not a
 * meeting.
 *
 * <h2>The untuned-Foresight guard</h2>
 *
 * Twelve of {@code ForesightConfig}'s variables are {@code required} with no default, and reading
 * an unset one throws {@code IllegalStateException("Config variable has not been set")} — no field
 * name, no robot, no hint, and it surfaces partway through following a path rather than at init.
 * {@code createAlgorithm()} turns that into a sentence naming the robot, the tuner, and the file.
 * That guard looks like a try/catch that rethrows the same exception type, which is exactly the
 * kind of code a later reader deletes as redundant; this test records that the <em>message</em> is
 * the point.
 *
 * <p>It also encodes the current state of the robots: Foresight is untuned on both. When one
 * robot's Foresight Tuner output is pasted into its file, the guard test starts failing <em>for
 * that robot</em>, and says so. That is the intended signal, not a bug: "19429 has been tuned" is a
 * real event worth noticing in the build.
 */
public class ConstantsTest {

    @Test
    public void bothCompetitionRobotsResolveToTheirOwnValues() {
        RobotConstants a = Constants.forRobot(RobotIdentity.TEAM_19429);
        RobotConstants b = Constants.forRobot(RobotIdentity.TEAM_20245);

        assertTrue("TEAM_19429 must use Robot19429.java, but uses " + a.file,
                a.fileName().equals("Robot19429.java"));
        assertTrue("TEAM_20245 must use Robot20245.java, but uses " + b.file,
                b.fileName().equals("Robot20245.java"));

        String shared = " is the same object for both robots, so tuning one would change the"
                + " other. Check that each robot file's constants() passes its own fields.";
        assertNotSame("drivetrainConfig" + shared, a.drivetrainConfig, b.drivetrainConfig);
        assertNotSame("localizerConfig" + shared, a.localizerConfig, b.localizerConfig);
        assertNotSame("foresightConfig" + shared, a.foresightConfig, b.foresightConfig);
    }

    /** Pairwise, over every registered robot — not only the two competition ones. */
    @Test
    public void noTwoRobotsShareAConfigObjectOrAFile() {
        List<RobotIdentity> registered = registeredRobots();
        for (int i = 0; i < registered.size(); i++) {
            for (int j = i + 1; j < registered.size(); j++) {
                RobotConstants a = Constants.forRobot(registered.get(i));
                RobotConstants b = Constants.forRobot(registered.get(j));
                String pair = registered.get(i) + " and " + registered.get(j);

                assertFalse(pair + " both use " + a.file, a.file.equals(b.file));
                assertNotSame(pair + " share a drivetrainConfig",
                        a.drivetrainConfig, b.drivetrainConfig);
                assertNotSame(pair + " share a localizerConfig",
                        a.localizerConfig, b.localizerConfig);
                assertNotSame(pair + " share a foresightConfig",
                        a.foresightConfig, b.foresightConfig);
            }
        }
    }

    @Test
    public void aRobotWithNoDrivetrainIsRefusedByName() {
        assertFalse("LAUNCHER_RIG has no drivetrain; if that changed, pick another identity for"
                + " this test", Constants.hasDrivetrain(RobotIdentity.LAUNCHER_RIG));

        try {
            RobotConstants values = Constants.forRobot(RobotIdentity.LAUNCHER_RIG);
            fail("LAUNCHER_RIG has no drivetrain, yet forRobot returned " + values.file
                    + ". Falling back to another robot's values is last season's bug.");
        } catch (IllegalStateException expected) {
            String message = String.valueOf(expected.getMessage());
            assertTrue("The message must name the identity, but was: " + message,
                    message.contains("LAUNCHER_RIG"));
            assertTrue("The message must name the config to check, but was: " + message,
                    message.contains(RobotIdentity.LAUNCHER_RIG.configName));
        }
    }

    /**
     * Adding a {@code RobotIdentity} whose device list includes the drivetrain, without adding its
     * {@code pedro/robots/} file and {@code Constants.ROBOTS} line, fails here.
     */
    @Test
    public void everyRobotWithADrivetrainHasPedroValues() {
        assertTrue("TEAM_19429 carries the drivetrain; if hasDrivetrain says otherwise this"
                + " test checks nothing", Constants.hasDrivetrain(RobotIdentity.TEAM_19429));

        List<String> missing = new ArrayList<>();
        for (RobotIdentity identity : RobotIdentity.values()) {
            if (!Constants.hasDrivetrain(identity)) {
                continue;
            }
            try {
                Constants.forRobot(identity);
            } catch (IllegalStateException e) {
                missing.add(identity + ": " + e.getMessage());
            }
        }
        if (!missing.isEmpty()) {
            fail("These robots carry the drivetrain but have no Pedro values. Copy"
                    + " pedro/robots/Robot19429.java for each and add one line to"
                    + " Constants.ROBOTS:\n  - " + String.join("\n  - ", missing));
        }
    }

    /** The robot files carry no device names; {@link RobotConstants} sets the shared ones. */
    @Test
    public void everyRobotUsesTheSharedDeviceNames() {
        for (RobotIdentity identity : registeredRobots()) {
            RobotConstants robot = Constants.forRobot(identity);
            String where = identity + " (" + robot.fileName() + ")";
            assertEquals(where, DeviceNames.FRONT_LEFT, robot.drivetrainConfig.frontLeftName.get());
            assertEquals(where, DeviceNames.FRONT_RIGHT, robot.drivetrainConfig.frontRightName.get());
            assertEquals(where, DeviceNames.BACK_LEFT, robot.drivetrainConfig.backLeftName.get());
            assertEquals(where, DeviceNames.BACK_RIGHT, robot.drivetrainConfig.backRightName.get());
            assertEquals(where, DeviceNames.PINPOINT, robot.localizerConfig.name.get());
        }
    }

    @Test
    public void createAlgorithmRefusesToBuildAnUntunedForesight() {
        for (RobotIdentity identity : registeredRobots()) {
            RobotConstants robot = Constants.forRobot(identity);
            try {
                Constants.createAlgorithm(identity);
            } catch (IllegalStateException expected) {
                String message = String.valueOf(expected.getMessage());

                assertTrue("The guard's whole purpose is naming the tuner to run, but the"
                                + " message was: " + message,
                        message.contains("Foresight Tuner"));
                assertTrue("The message must name the robot, but it was: " + message,
                        message.contains(identity.toString()));
                assertTrue("The message must name the file to paste into, but it was: "
                                + message,
                        message.contains(robot.fileName()) && message.contains("foresightConfig"));
                assertTrue("The original cause must be kept, or the underlying config error is"
                                + " lost from the stack trace",
                        expected.getCause() instanceof IllegalStateException);
                continue;
            }

            fail("Constants.createAlgorithm(" + identity + ") built a Foresight without"
                    + " throwing, which means " + robot.fileName() + "'s foresightConfig now has"
                    + " its twelve required values set."
                    + "\n\nIf the Foresight Tuner has been run on " + identity + " and its block"
                    + " pasted in, that is good news — exclude " + identity + " from this test"
                    + " and add one asserting its tuned values are present and sane."
                    + "\n\nIf it has not, the guard in createAlgorithm() has stopped working and"
                    + " an untuned robot will now fail mid-path instead of at init.");
        }
    }

    private static List<RobotIdentity> registeredRobots() {
        List<RobotIdentity> registered = new ArrayList<>();
        for (RobotIdentity identity : RobotIdentity.values()) {
            if (Constants.hasDrivetrain(identity)) {
                registered.add(identity);
            }
        }
        return registered;
    }
}
