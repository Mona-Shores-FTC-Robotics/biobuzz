package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Enforces the loop contract {@link RobotOpMode} states: bulk caching, the Ivy scheduler and
 * teardown belong to it alone.
 *
 * <p>An OpMode that extends {@code RobotOpMode}, or a subsystem, doing any of these itself either
 * reads stale sensors (a second cache mode or a missed clear) or double-steps every mechanism (a
 * second {@code Scheduler.execute()}). Both compile and both run; only the robot notices.
 *
 * <p>A textual source scan, like {@code DeviceNameLiteralTest}, with comments stripped. Standalone
 * OpModes that do not extend {@code RobotOpMode} — the test rigs and the diagnostics — own their
 * own loops and are not checked.
 */
public class LoopContractTest {

    private static final String[] FORBIDDEN = {
            "setBulkCachingMode",
            "clearBulkCache",
            "Scheduler.execute",
            "Scheduler.reset",
            "robot.stop(",
            "robot.initialize(",
    };

    private static final Pattern EXTENDS_ROBOT_OPMODE = Pattern.compile("extends\\s+RobotOpMode\\b");

    private static final Pattern COMMENTS = Pattern.compile("/\\*.*?\\*/|//[^\\n]*", Pattern.DOTALL);

    @Test
    public void onlyRobotOpModeOwnsTheLoop() {
        List<File> checked = new ArrayList<>();
        List<String> violations = new ArrayList<>();

        for (File file : teamAuthoredSources()) {
            String code = COMMENTS.matcher(read(file)).replaceAll("");
            boolean isSubsystem = file.getParentFile().getName().equals("subsystems");
            if (!isSubsystem && !EXTENDS_ROBOT_OPMODE.matcher(code).find()) {
                continue;
            }
            checked.add(file);
            for (String call : FORBIDDEN) {
                if (code.contains(call)) {
                    violations.add(file.getName() + " calls " + call
                            + " — RobotOpMode does this; see its class javadoc");
                }
            }
        }

        assertTrue("Found no RobotOpMode subclasses or subsystems to check — is the scan broken?",
                checked.size() >= 3);
        if (!violations.isEmpty()) {
            fail(String.join("\n", violations));
        }
    }

    // -------------------------------------------------------------- helpers

    /** Team-authored sources; {@code pedro/procedures} is upstream and skipped. */
    private static List<File> teamAuthoredSources() {
        List<File> sources = new ArrayList<>();
        collectJava(mainJavaDirectory(), sources);
        return sources;
    }

    private static void collectJava(File directory, List<File> into) {
        File[] entries = directory.listFiles();
        if (entries == null) {
            return;
        }
        for (File entry : entries) {
            if (entry.isDirectory()) {
                if (!"procedures".equals(entry.getName())) {
                    collectJava(entry, into);
                }
            } else if (entry.getName().endsWith(".java")
                    && !"RobotOpMode.java".equals(entry.getName())) {
                into.add(entry);
            }
        }
    }

    private static String read(File file) {
        try {
            return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AssertionError("Could not read " + file, e);
        }
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
