package org.firstinspires.ftc.teamcode.pedro;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.firstinspires.ftc.teamcode.hardware.DeviceNames;
import org.firstinspires.ftc.teamcode.hardware.RobotIdentity;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Source checks on {@code pedro/robots/}, the one-file-per-robot tuned values.
 *
 * <p>Two mistakes the compiler and {@link ConstantsTest} cannot see:
 *
 * <ol>
 *   <li><b>A pasted device name that is not ours.</b> The Mecanum and Pinpoint Tuners emit
 *       {@code c.frontLeftName.set("frontLeft")} and {@code c.name.set("pinpoint")} with string
 *       literals, and those lines may stay so a block can be pasted exactly as the tuner shows it.
 *       {@link RobotConstants} overwrites every name from {@code DeviceNames} anyway. But a name
 *       that <em>differs</em> from {@code DeviceNames} means the tuner was run with a mistyped
 *       name field, so the file would claim a name the robot isn't using — and the numbers next
 *       to it came from a run worth double-checking.</li>
 *   <li><b>A robot file nobody registered.</b> A file in {@code pedro/robots/} that no
 *       {@code Constants.ROBOTS} entry points at is tuning that is never used — most likely a
 *       copied file whose line was forgotten, or a line pointing at the file it was copied
 *       from.</li>
 * </ol>
 */
public class PedroRobotsTest {

    /**
     * {@code .frontLeftName.set("…")}, {@code .name.set("…")} and the like: a name field set to a
     * string literal. Group 1 is the field, group 2 the literal. A name set from a
     * {@code DeviceNames} constant is not a literal and is not matched.
     */
    private static final Pattern NAME_LINE =
            Pattern.compile("\\.(\\w*[Nn]ame)\\s*\\.\\s*set\\s*\\(\\s*\"([^\"]*)\"");

    /** Each Pedro name field the tuners emit, and the {@code DeviceNames} value it must match. */
    private static final Map<String, String> EXPECTED_NAMES = new HashMap<>();

    static {
        EXPECTED_NAMES.put("frontLeftName", DeviceNames.FRONT_LEFT);
        EXPECTED_NAMES.put("frontRightName", DeviceNames.FRONT_RIGHT);
        EXPECTED_NAMES.put("backLeftName", DeviceNames.BACK_LEFT);
        EXPECTED_NAMES.put("backRightName", DeviceNames.BACK_RIGHT);
        EXPECTED_NAMES.put("name", DeviceNames.PINPOINT);
    }

    @Test
    public void pastedDeviceNamesMatchDeviceNames() throws IOException {
        List<String> offenders = new ArrayList<>();
        for (File file : robotFiles()) {
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                String code = lines.get(i).trim();
                if (code.startsWith("*") || code.startsWith("/*") || code.startsWith("//")) {
                    continue;
                }
                Matcher line = NAME_LINE.matcher(code);
                while (line.find()) {
                    String field = line.group(1);
                    String expected = EXPECTED_NAMES.get(field);
                    String where = file.getName() + ":" + (i + 1) + "  " + code;
                    if (expected == null) {
                        offenders.add(where + "\n      " + field + " is not a name this test"
                                + " knows. Add it to EXPECTED_NAMES with its DeviceNames value.");
                    } else if (!expected.equals(line.group(2))) {
                        offenders.add(where + "\n      expected \"" + expected + "\", the name in"
                                + " DeviceNames and on the robot.");
                    }
                }
            }
        }
        if (!offenders.isEmpty()) {
            fail("A robot file names a device differently from DeviceNames. The robot uses the"
                    + " DeviceNames name regardless (RobotConstants sets it), so a different name"
                    + " here means the tuner was run with a mistyped name field. Fix the name to"
                    + " match, and consider re-running that tuner:\n  "
                    + String.join("\n  ", offenders));
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
