package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * {@link Tunables#CLASSES} is every {@code @Configurable} class a match OpMode can run with, and
 * nothing else: a source scan, so a new tunable class cannot be left out of the log, and a bench's
 * knobs cannot creep in.
 */
public class TunablesTest {

    private static final Pattern CONFIGURABLE = Pattern.compile("(?m)^@Configurable\\b");

    /** Benches, rigs and diagnostics: their tunables are not a match's. */
    private static final String[] NOT_A_MATCH = {"opmodes/calibration/", "launcher2/", "opmodes/DriveMotorCheck.java"};

    @Test
    public void everyMatchConfigurableClassIsListedAndNoBenchIs() throws IOException {
        TreeSet<String> annotated = new TreeSet<>();
        for (File file : javaSources(teamCodeRoot())) {
            String code = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            if (!CONFIGURABLE.matcher(code).find()) continue;
            String path = file.getPath().replace('\\', '/');
            boolean bench = false;
            for (String folder : NOT_A_MATCH) {
                if (path.contains(folder)) bench = true;
            }
            if (!bench) annotated.add(file.getName().replace(".java", ""));
        }
        TreeSet<String> listed = new TreeSet<>();
        for (Class<?> cls : Tunables.CLASSES) {
            listed.add(cls.getSimpleName());
        }
        assertEquals("Tunables.CLASSES must be exactly the @Configurable classes outside the benches",
                annotated, listed);
    }

    @SuppressWarnings("unused")
    static class Sample {
        public static double knob = 1.5;
        public static boolean flag = true;
        public static String name = "x";
        public static Thread.State mode = Thread.State.NEW;
        public static final double CONSTANT = 2.0;
        public static Object object = new Object();
        static double packagePrivate = 1.0;
        public double instance = 1.0;
    }

    @Test
    public void onlyPublicStaticNonFinalScalarsAreTunables() throws NoSuchFieldException {
        assertTrue(Tunables.isTunable(Sample.class.getDeclaredField("knob")));
        assertTrue(Tunables.isTunable(Sample.class.getDeclaredField("flag")));
        assertTrue(Tunables.isTunable(Sample.class.getDeclaredField("name")));
        assertTrue(Tunables.isTunable(Sample.class.getDeclaredField("mode")));
        assertFalse(Tunables.isTunable(Sample.class.getDeclaredField("CONSTANT")));
        assertFalse(Tunables.isTunable(Sample.class.getDeclaredField("object")));
        assertFalse(Tunables.isTunable(Sample.class.getDeclaredField("packagePrivate")));
        assertFalse(Tunables.isTunable(Sample.class.getDeclaredField("instance")));
    }

    /** The module's team sources, wherever Gradle runs the test from. */
    private static File teamCodeRoot() {
        String tail = "src/main/java/org/firstinspires/ftc/teamcode";
        File here = new File(tail);
        if (here.isDirectory()) return here;
        File fromRepo = new File("TeamCode", tail);
        assertTrue("cannot find " + tail + " from " + new File(".").getAbsolutePath(), fromRepo.isDirectory());
        return fromRepo;
    }

    private static List<File> javaSources(File dir) {
        List<File> out = new ArrayList<>();
        File[] children = dir.listFiles();
        if (children == null) return out;
        for (File child : children) {
            if (child.isDirectory()) out.addAll(javaSources(child));
            else if (child.getName().endsWith(".java")) out.add(child);
        }
        return out;
    }
}
