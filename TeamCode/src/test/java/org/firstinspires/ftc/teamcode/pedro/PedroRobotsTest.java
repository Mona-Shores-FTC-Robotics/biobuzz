package org.firstinspires.ftc.teamcode.pedro;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.firstinspires.ftc.teamcode.hardware.RobotIdentity;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Source checks on {@code pedro/robots/}, the one-file-per-robot tuned values.
 *
 * <p>Two mistakes the compiler and {@link ConstantsTest} cannot see:
 *
 * <ol>
 *   <li><b>A pasted device-name line.</b> The Mecanum and Pinpoint Tuners emit
 *       {@code c.frontLeftName.set("frontLeft")} and {@code c.name.set("pinpoint")} with string
 *       literals. {@link RobotConstants} overwrites every name from {@code DeviceNames}, so such a
 *       line does nothing — but it reads as if that robot's names can differ, and the next person
 *       to "fix" a name will edit it there and wonder why nothing changed. Delete it after
 *       pasting.</li>
 *   <li><b>A robot file nobody registered.</b> A file in {@code pedro/robots/} that no
 *       {@code Constants.ROBOTS} entry points at is tuning that is never used — most likely a
 *       copied file whose line was forgotten, or a line pointing at the file it was copied
 *       from.</li>
 * </ol>
 */
public class PedroRobotsTest {

    /**
     * {@code .frontLeftName.set(}, {@code .name.set(} and the like. Comment lines are skipped, so
     * the robot files' own javadoc can describe the lines it tells you to delete.
     */
    private static final Pattern NAME_LINE = Pattern.compile("\\.\\w*[Nn]ame\\s*\\.\\s*set\\s*\\(");

    @Test
    public void robotFilesDoNotSetDeviceNames() throws IOException {
        List<String> offenders = new ArrayList<>();
        for (File file : robotFiles()) {
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                String code = lines.get(i).trim();
                boolean comment = code.startsWith("*") || code.startsWith("/*")
                        || code.startsWith("//");
                if (!comment && NAME_LINE.matcher(code).find()) {
                    offenders.add(file.getName() + ":" + (i + 1) + "  " + lines.get(i).trim());
                }
            }
        }
        if (!offenders.isEmpty()) {
            fail("Device names are shared by every robot and set in RobotConstants from"
                    + " DeviceNames. Delete these lines — they came in with a tuner paste and are"
                    + " overwritten anyway:\n  " + String.join("\n  ", offenders));
        }
    }

    @Test
    public void everyRobotFileIsRegistered() {
        Set<String> registered = new HashSet<>();
        for (RobotIdentity identity : RobotIdentity.values()) {
            if (Constants.hasDrivetrain(identity)) {
                registered.add(Constants.forRobot(identity).fileName());
            }
        }

        List<String> orphans = new ArrayList<>();
        for (File file : robotFiles()) {
            if (!registered.contains(file.getName())) {
                orphans.add(file.getName());
            }
        }
        if (!orphans.isEmpty()) {
            fail("No Constants.ROBOTS entry uses " + orphans + ", so no robot ever runs on"
                    + " these values. Register each with one line in Constants.ROBOTS, or delete"
                    + " it.");
        }
    }

    private static List<File> robotFiles() {
        File dir = new File(mainJavaDirectory(), "org/firstinspires/ftc/teamcode/pedro/robots");
        File[] files = dir.listFiles((d, name) -> name.endsWith(".java"));
        assertTrue("Found no robot files in " + dir, files != null && files.length > 0);
        List<File> result = new ArrayList<>();
        for (File file : files) {
            result.add(file);
        }
        return result;
    }

    /** Same lookup as {@code DeviceNameLiteralTest}: module dir under Gradle, repo root in an IDE. */
    private static File mainJavaDirectory() {
        File candidate = new File(System.getProperty("user.dir"));
        for (int depth = 0; depth < 4 && candidate != null; depth++) {
            File direct = new File(candidate, "src/main/java");
            if (direct.isDirectory()) {
                return direct;
            }
            File viaModule = new File(candidate, "TeamCode/src/main/java");
            if (viaModule.isDirectory()) {
                return viaModule;
            }
            candidate = candidate.getParentFile();
        }
        throw new AssertionError(
                "Could not locate src/main/java from " + System.getProperty("user.dir"));
    }
}
