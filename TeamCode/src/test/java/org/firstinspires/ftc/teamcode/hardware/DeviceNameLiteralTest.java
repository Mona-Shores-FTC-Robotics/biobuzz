package org.firstinspires.ftc.teamcode.hardware;

import static org.junit.Assert.fail;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enforces the rule {@link DeviceNames} states in bold: a device name is written
 * in exactly one place.
 *
 * <p>{@code RobotConfigXmlTest} already proves {@code DeviceNames} and the bundled
 * XML agree. It cannot see a name that never reaches {@code DeviceNames} at all —
 * a literal handed straight to {@code hardwareMap.get}, or a private constant
 * holding one. Those are invisible to every other check in the build: the device
 * is absent from {@code DeviceNames.ALL}, so {@code ValidateHardware} does not
 * look for it and no {@code robot_*.xml} is required to declare it. The symptom
 * arrives at a meeting, as a subsystem that silently reports itself unavailable.
 *
 * <p>That is not hypothetical. The launcher motors spent their first weeks as
 * {@code "launcher_left"} and friends on a dashboard-editable config object,
 * exactly the pattern {@code DeviceNames} calls out as last season's mistake,
 * and moved into {@code DeviceNames} only later. This test is what stops the
 * next one drifting back.
 *
 * <h2>What it looks for, and what it cannot see</h2>
 *
 * Two patterns, both textual — this is a source scan, not a compiler:
 *
 * <ol>
 *   <li>A string literal passed as the name argument of {@code hardwareMap.get}
 *       or {@code hardwareMap.tryGet}.</li>
 *   <li>A {@code static final String} constant whose <em>field name</em> suggests
 *       it holds a device name ({@code DEVICE}, {@code MOTOR}, {@code SERVO} or
 *       {@code NAME}), declared anywhere outside {@code DeviceNames}.</li>
 * </ol>
 *
 * <p>Both are heuristics and neither is complete. A name assembled at run time,
 * or one held in a field named something else entirely, slips through. That is
 * an accepted limit: the check is cheap, it costs nothing to keep, and it covers
 * the two shapes this repository has actually produced. {@code ValidateHardware}
 * remains the backstop on a real robot.
 *
 * <p>A generated-source or annotation-processor approach would be exact, but
 * {@code RobotConfigXmlTest} already records why codegen was rejected here — the
 * build's Sloth / Load / AGP versions are documented as delicately locked, and a
 * plain JVM test buys most of the guarantee with none of that risk. Same
 * reasoning, same conclusion.
 */
public class DeviceNameLiteralTest {

    /**
     * Device names that are knowingly not {@code DeviceNames} constants yet.
     *
     * <p>Every entry is a bug with a reason, not an exemption. Delete the entry
     * when the underlying work lands — {@link #theAllowlistDoesNotOutliveItsEntries}
     * fails if one is left behind after the name reaches {@code DeviceNames}.
     *
     * <ul>
     *   <li><b>limelight</b> — {@code LimelightVisionSubsystem.DEFAULT_DEVICE_NAME}.
     *       Adding it to {@code DeviceNames} is not a one-line change: {@code Kind}
     *       has only {@code MOTOR}, {@code SERVO} and {@code I2C}, and a Limelight
     *       is none of them, so {@code RobotConfigXmlTest.kindOf} would classify
     *       its XML element as a motor and then port-range-check it against 0-3.
     *       Until that is sorted, neither {@code robot_*.xml} declares a Limelight
     *       and all three Vision OpModes report LIMELIGHT NOT FOUND on both
     *       robots.</li>
     * </ul>
     */
    private static final Set<String> KNOWN_MISSING_FROM_DEVICE_NAMES =
            new HashSet<>(Arrays.asList("limelight"));

    /** A string literal handed straight to a hardware map lookup. */
    private static final Pattern LOOKUP_LITERAL = Pattern.compile(
            "hardwareMap\\s*\\.\\s*(?:get|tryGet)\\s*\\(\\s*[\\w.]+\\.class\\s*,\\s*\"([^\"]*)\"");

    /** A constant whose field name suggests it holds a device name. */
    private static final Pattern DEVICE_NAME_CONSTANT = Pattern.compile(
            "static\\s+final\\s+String\\s+(\\w*(?:DEVICE|MOTOR|SERVO|NAME)\\w*)\\s*=\\s*\"([^\"]*)\"");

    // ---------------------------------------------------------------- tests

    @Test
    public void noDeviceNameIsWrittenOutsideDeviceNames() {
        Set<String> registered = registeredNames();
        List<String> violations = new ArrayList<>();

        for (File file : teamAuthoredSources()) {
            String source = read(file);

            Matcher literal = LOOKUP_LITERAL.matcher(source);
            while (literal.find()) {
                String name = literal.group(1);
                if (!registered.contains(name) && !KNOWN_MISSING_FROM_DEVICE_NAMES.contains(name)) {
                    violations.add(file.getName() + " passes the literal \"" + name
                            + "\" to a hardwareMap lookup");
                }
            }

            Matcher constant = DEVICE_NAME_CONSTANT.matcher(source);
            while (constant.find()) {
                String field = constant.group(1);
                String name = constant.group(2);
                if (!registered.contains(name) && !KNOWN_MISSING_FROM_DEVICE_NAMES.contains(name)) {
                    violations.add(file.getName() + " declares " + field + " = \"" + name + "\"");
                }
            }
        }

        if (!violations.isEmpty()) {
            fail("Device name(s) written outside DeviceNames:\n  - "
                    + String.join("\n  - ", violations)
                    + "\n\nA device name is not a tunable and not a local constant. Add a"
                    + " constant to DeviceNames, an entry in each robot's device list there, and the element"
                    + " to each of those robot_*.xml — then refer to the constant here."
                    + "\n\nIf the name genuinely cannot join DeviceNames yet, add it to"
                    + " KNOWN_MISSING_FROM_DEVICE_NAMES in this test with the reason, so the"
                    + " gap is reviewable instead of invisible."
                    + "\n\nRegistered names: " + new TreeSet<>(registered));
        }
    }

    /**
     * An allowlist entry is a bug being tracked, so it has to disappear when the
     * bug is fixed. Without this, a stale entry would silently keep suppressing a
     * check that has started passing on its own.
     */
    @Test
    public void theAllowlistDoesNotOutliveItsEntries() {
        Set<String> registered = registeredNames();
        Set<String> stale = new TreeSet<>();
        for (String allowed : KNOWN_MISSING_FROM_DEVICE_NAMES) {
            if (registered.contains(allowed)) {
                stale.add(allowed);
            }
        }
        if (!stale.isEmpty()) {
            fail("KNOWN_MISSING_FROM_DEVICE_NAMES still lists " + stale + ", but"
                    + " DeviceNames now registers those names. Delete the entries — the"
                    + " work they were tracking is done.");
        }
    }

    /**
     * Guards the scan itself. If the source tree moves, or the patterns stop
     * matching anything, the two tests above would pass by reading nothing at
     * all — the classic way a source-scanning check rots into a no-op.
     */
    @Test
    public void theScanActuallyReadsTheTeamSources() {
        List<File> sources = teamAuthoredSources();
        if (sources.size() < 10) {
            fail("Only found " + sources.size() + " team-authored .java files under "
                    + mainJavaDirectory().getAbsolutePath() + "; the scan is not looking"
                    + " where the code is.");
        }

        boolean sawADeviceNameConstant = false;
        for (File file : sources) {
            if (DEVICE_NAME_CONSTANT.matcher(read(file)).find()) {
                sawADeviceNameConstant = true;
                break;
            }
        }
        if (!sawADeviceNameConstant) {
            fail("The device-name-constant pattern matched nothing anywhere in the tree."
                    + " Either the naming convention changed or the pattern is broken;"
                    + " either way this test is no longer checking anything.");
        }
    }

    // ------------------------------------------------------------- plumbing

    private static Set<String> registeredNames() {
        Set<String> names = new HashSet<>();
        for (DeviceNames.Device device : DeviceNames.ALL) {
            names.add(device.name);
        }
        return names;
    }

    /**
     * Every {@code .java} file we wrote, which is the only code this rule binds.
     *
     * <p>{@code pedro/procedures} is upstream Quickstart code, re-copied verbatim
     * on a Pedro upgrade — a finding there would be unfixable without diverging
     * from upstream. {@code DeviceNames} itself is the one place the names are
     * supposed to live.
     */
    private static List<File> teamAuthoredSources() {
        List<File> sources = new ArrayList<>();
        collectJava(mainJavaDirectory(), sources);
        sources.sort((a, b) -> a.getAbsolutePath().compareTo(b.getAbsolutePath()));
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
                    && !"DeviceNames.java".equals(entry.getName())) {
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

    /**
     * Locates {@code src/main/java}. Gradle runs unit tests with the module
     * directory as the working directory; walking up keeps this working when a
     * test is run straight from an IDE with the repository root instead. Same
     * approach as {@code RobotConfigXmlTest}.
     */
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
