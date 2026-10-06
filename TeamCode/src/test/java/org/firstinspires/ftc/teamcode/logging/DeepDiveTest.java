package org.firstinspires.ftc.teamcode.logging;

import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs {@link DeepDive} on the cases in a file, one a line ({@code #} comments), on normal and slow tiles. Opt in:
 *
 * <pre>
 * BIOBUZZ_DEEP_DIVE=tools/auto-routes/deep-dive-cases.txt ./gradlew :TeamCode:testDebugUnitTest --tests '*DeepDiveTest*' -i
 * </pre>
 */
public class DeepDiveTest {

    @Test
    public void everyCase() throws Exception {
        String path = System.getenv("BIOBUZZ_DEEP_DIVE");
        if (path == null) return;
        File f = new File(path);
        if (!f.isAbsolute()) f = new File(TeamCodeDir.get().getParentFile(), path);
        List<String> args = new ArrayList<>();
        args.add(System.getenv().getOrDefault("BIOBUZZ_DEEP_DIVE_FRICTION", "1,3"));
        for (String line : Files.readAllLines(f.toPath(), StandardCharsets.UTF_8)) {
            line = line.trim();
            if (!line.isEmpty() && !line.startsWith("#")) args.add(line);
        }
        DeepDive.main(args.toArray(new String[0]));
    }
}
