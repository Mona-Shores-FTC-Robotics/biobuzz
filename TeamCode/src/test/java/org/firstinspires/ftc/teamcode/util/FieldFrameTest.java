package org.firstinspires.ftc.teamcode.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pins the field's size to one number, written once, in {@link FieldFrame}.
 *
 * <p>Last season's frame bugs came from two tools disagreeing about the field by an inch or two.
 * This season the Visualizer draws and mirrors on a 141.5 in field, so the robot code must too, and
 * the easiest way to break that is someone typing {@code 144 - x} or {@code new Pose(72, 72, 0)}
 * somewhere. The second test catches that.
 *
 * <p>It is a textual scan, like {@code DeviceNameLiteralTest}: it looks for the numbers 144, 72,
 * 141.5 and 70.75 in code (not comments) outside {@code FieldFrame}. If one of those is ever
 * genuinely not a field size, write it as a named constant whose name says what it is, in a
 * file that also explains why — and add that file to {@link #NOT_FIELD_SIZES} here with the reason.
 */
public class FieldFrameTest {

    private static final double EPS = 1e-9;

    /** Files where one of the numbers legitimately means something other than a field size. */
    private static final List<String> NOT_FIELD_SIZES = new ArrayList<>();

    /** 144, 72, 141.5 or 70.75 as a numeric literal, optionally with trailing zeros or a d/f suffix. */
    private static final Pattern FIELD_SIZE_LITERAL = Pattern.compile(
            "(?<![\\w.])(144|72|141\\.5|70\\.75)(?:\\.0+)?[dDfF]?(?![\\w.])");

    @Test
    public void theFieldIsTheSizeTheVisualizerUses() {
        assertEquals(141.5, FieldFrame.FIELD_SIZE_INCHES, EPS);
        assertEquals(70.75, FieldFrame.FIELD_CENTRE_INCHES, EPS);
    }

    @Test
    public void noOtherFileStatesTheFieldSize() {
        List<String> violations = new ArrayList<>();
        for (File file : teamAuthoredSources()) {
            if ("FieldFrame.java".equals(file.getName()) || NOT_FIELD_SIZES.contains(file.getName())) {
                continue;
            }
            String[] lines = read(file).split("\n", -1);
            for (int i = 0; i < lines.length; i++) {
                String code = withoutComment(lines[i]);
                Matcher literal = FIELD_SIZE_LITERAL.matcher(code);
                if (literal.find()) {
                    violations.add(file.getName() + ":" + (i + 1) + " — " + lines[i].trim());
                }
            }
        }
        if (!violations.isEmpty()) {
            fail("Field-size number(s) written outside FieldFrame:\n  - "
                    + String.join("\n  - ", violations)
                    + "\n\nThe field is FieldFrame.FIELD_SIZE_INCHES (141.5, wall face to wall face)"
                    + " and its centre is FieldFrame.FIELD_CENTRE_INCHES (70.75). Use those. 144 and 72"
                    + " are the nominal 12 ft field, which is 2.5 in too big and puts a mirrored pose"
                    + " in the wrong place.");
        }
    }

    /** Guards the scan itself, so a moved source tree can't turn the test above into a no-op. */
    @Test
    public void theScanActuallyReadsTheTeamSources() {
        List<File> sources = teamAuthoredSources();
        if (sources.size() < 10) {
            fail("Only found " + sources.size() + " .java files; the scan is not looking where the code is.");
        }
        if (!FIELD_SIZE_LITERAL.matcher("double x = 144 - pose.x();").find()
                || !FIELD_SIZE_LITERAL.matcher("new Pose(72.0, 70.75, 0)").find()
                || FIELD_SIZE_LITERAL.matcher("double gain = 1440; int t = 0.72;").find()) {
            fail("FIELD_SIZE_LITERAL no longer matches what it is meant to.");
        }
    }

    // ------------------------------------------------------------- plumbing

    /** Drops Javadoc/block-comment lines and anything after a line comment. Heuristic, not a lexer. */
    private static String withoutComment(String line) {
        String trimmed = line.trim();
        if (trimmed.startsWith("*") || trimmed.startsWith("/*") || trimmed.startsWith("//")) {
            return "";
        }
        int lineComment = line.indexOf("//");
        return lineComment >= 0 ? line.substring(0, lineComment) : line;
    }

    /** Every {@code .java} file we wrote; {@code pedro/procedures} is upstream and excluded. */
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
            } else if (entry.getName().endsWith(".java")) {
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
