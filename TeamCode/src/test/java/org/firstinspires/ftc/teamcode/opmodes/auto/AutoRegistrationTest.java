package org.firstinspires.ftc.teamcode.opmodes.auto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.vision.HiveTracker;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Keeps {@code TeamCode/auto-registry.json}, the list of commands and triggers the Auto Builder
 * loads, in step with {@link AutoRegistration}. Registering reads nothing, so it needs no robot.
 *
 * <p>After changing what {@code AutoRegistration} registers, write the file again with
 * {@code UPDATE_AUTO_REGISTRY=1 ./gradlew :TeamCode:testDebugUnitTest --tests '*AutoRegistrationTest*'}
 * and commit it.
 */
public class AutoRegistrationTest {

    @Test
    public void theEditorsListMatchesWhatTheRobotRegisters() throws IOException {
        String expected = AutoRegistration.forRobot(null, null, null, null).describe();
        File file = new File(teamCode(), "auto-registry.json");
        if ("1".equals(System.getenv("UPDATE_AUTO_REGISTRY"))) {
            Files.write(file.toPath(), expected.getBytes(StandardCharsets.UTF_8));
        }
        String actual = file.exists() ? new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8) : "(missing)";
        assertEquals("auto-registry.json is out of date: run this test with UPDATE_AUTO_REGISTRY=1 and commit it",
                expected, actual);
    }

    /** IntakeFull is the colour sensor's full now (LaneFull): nothing is a placeholder. */
    @Test
    public void nothingIsStillAPlaceholder() {
        assertEquals(java.util.Collections.emptyList(), AutoRegistration.PLACEHOLDERS);
    }

    /** The CELL a robot is aimed at: at the audience end, red's RIGHT one and blue's LEFT one. */
    @Test
    public void theAimedCellIsOurCellAtTheRobotsEndOfTheField() {
        assertFalse(AutoRegistration.aimedLeft(Alliance.RED, 18));
        assertTrue(AutoRegistration.aimedLeft(Alliance.RED, 123.5));
        assertTrue(AutoRegistration.aimedLeft(Alliance.BLUE, 18));
        assertFalse(AutoRegistration.aimedLeft(Alliance.BLUE, 123.5));
        assertFalse("no robot, no CELL", AutoRegistration.aimedCellUp(null, null));
    }

    /** No alliance chosen means no HIVE to ask: neither CELL is up. */
    @Test
    public void noHiveMeansNeitherCellIsUp() {
        assertFalse(AutoRegistration.cellUp(null, true));
        assertFalse(AutoRegistration.cellUp(null, false));
    }

    /**
     * A HIVE the camera has not seen is UNSEEN, not the match-start RIGHT_CELL_UP: a wait on
     * either trigger runs on rather than ending on a guess. (HiveTrackerTest covers each state.)
     */
    @Test
    public void anUnseenHiveIsNeitherCellUp() {
        HiveTracker unseen = new HiveTracker();
        assertFalse(AutoRegistration.cellUp(unseen, true));
        assertFalse(AutoRegistration.cellUp(unseen, false));
    }

    /**
     * R-Solo, 5 Nov 2026 00:32: the match-start position the tracker only assumed is not a
     * CELL seen up. The wait runs on until the camera sees it.
     */
    @Test
    public void anAssumedPositionIsNeitherCellUp() {
        HiveTracker hive = new HiveTracker();
        hive.assumeMatchStart();
        assertTrue("the tracker believes it", hive.rightCellUp());
        assertTrue(hive.assumed());
        assertFalse("but CellUp waits for the camera", AutoRegistration.cellUp(hive, false));
        assertFalse(AutoRegistration.cellUp(hive, true));
    }

    /** The TeamCode module: the working directory under Gradle, a child of it in an IDE. */
    private static File teamCode() {
        File dir = new File(System.getProperty("user.dir"));
        if (new File(dir, "src/main/java").isDirectory()) return dir;
        return new File(dir, "TeamCode");
    }

    /**
     * A CELL is scored only from its own end of the field: a HIVE's two CELLs open away from each other, so no
     * shot from the left end goes into the RIGHT CELL (mentor, 11 Nov 2026, after a "LaunchAllFar" card was drawn
     * as a fallback and hit the HIVE in every simulated run). The launch commands are the ones that aim at the
     * CELL at the robot's end ({@code TurretAimer}); a new one needs the same aim, and its name here.
     */
    @Test
    public void everyLaunchIsAtTheCellAtTheRobotsEnd() throws Exception {
        String json = new String(java.nio.file.Files.readAllBytes(new File(teamCode(), "auto-registry.json").toPath()),
                java.nio.charset.StandardCharsets.UTF_8);
        java.util.regex.Matcher names = java.util.regex.Pattern.compile("\\{\"name\": \"([^\"]+)\"").matcher(json);
        java.util.List<String> launches = new java.util.ArrayList<>();
        while (names.find()) {
            String name = names.group(1);
            if (name.startsWith("Launch") || name.startsWith("Shoot") || name.startsWith("Fire")) launches.add(name);
        }
        assertEquals("a launch command that is not LaunchAll or LaunchOne: both aim at the CELL at the robot's end, and"
                        + " no other CELL can be scored from there (CLAUDE.md, Hard constraints)",
                java.util.Arrays.asList("LaunchAll", "LaunchOne"), launches);
    }
}
