package org.firstinspires.ftc.teamcode.opmodes.auto;

import static org.junit.Assert.assertEquals;

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
        String expected = AutoRegistration.forRobot(null, null).describe();
        File file = new File(teamCode(), "auto-registry.json");
        if ("1".equals(System.getenv("UPDATE_AUTO_REGISTRY"))) {
            Files.write(file.toPath(), expected.getBytes(StandardCharsets.UTF_8));
        }
        String actual = file.exists() ? new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8) : "(missing)";
        assertEquals("auto-registry.json is out of date: run this test with UPDATE_AUTO_REGISTRY=1 and commit it",
                expected, actual);
    }

    /** The TeamCode module: the working directory under Gradle, a child of it in an IDE. */
    private static File teamCode() {
        File dir = new File(System.getProperty("user.dir"));
        if (new File(dir, "src/main/java").isDirectory()) return dir;
        return new File(dir, "TeamCode");
    }
}
