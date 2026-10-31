package org.firstinspires.ftc.teamcode.logging;

import java.io.File;

/**
 * The {@code TeamCode} module directory, found the way {@code LoopContractTest} finds
 * {@code src/main/java}: the working directory is the module under Gradle and the repo root in an
 * IDE, so look for either.
 */
final class TeamCodeDir {

    private TeamCodeDir() {
    }

    static File get() {
        File candidate = new File(System.getProperty("user.dir"));
        for (int depth = 0; depth < 4 && candidate != null; depth++) {
            if (new File(candidate, "src/test/java").isDirectory()
                    && new File(candidate, "src/main/java").isDirectory()) {
                return candidate;
            }
            File viaModule = new File(candidate, "TeamCode");
            if (new File(viaModule, "src/test/java").isDirectory()) {
                return viaModule;
            }
            candidate = candidate.getParentFile();
        }
        throw new AssertionError("Could not locate TeamCode from " + System.getProperty("user.dir"));
    }

    /** Where the desk-check logs go: {@code -Dsimlog.dir}, or {@code TeamCode/build/sim-logs}. */
    static File simLogs() {
        String override = System.getProperty("simlog.dir");
        return override != null ? new File(override) : new File(get(), "build/sim-logs");
    }
}
