package org.firstinspires.ftc.teamcode.logging;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Where match logs go and what they are called: one file per OpMode run, named after the OpMode and
 * the time it started ({@code Drive_TeleOp_2026-10-04_14-32-10.wpilog}), so a restart starts a new
 * file and never overwrites the last one.
 */
public final class MatchLogFiles {

    /** The folder's name inside the SDK's FIRST folder ({@code /sdcard/FIRST/logs} on a hub). */
    public static final String FOLDER_NAME = "logs";

    private MatchLogFiles() {
    }

    /** A file in {@code dir} that does not exist yet, for an OpMode started at {@code epochMs}. */
    public static File next(File dir, String opModeName, long epochMs) {
        String stamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(new Date(epochMs));
        String base = safe(opModeName) + "_" + stamp;
        File file = new File(dir, base + ".wpilog");
        for (int n = 2; file.exists(); n++) {
            file = new File(dir, base + "_" + n + ".wpilog");
        }
        return file;
    }

    /** Letters, digits, '-' and '_' only, so the name is safe on every computer it is copied to. */
    static String safe(String name) {
        StringBuilder out = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            out.append(Character.isLetterOrDigit(c) || c == '-' || c == '_' ? c : '_');
        }
        return out.length() == 0 ? "OpMode" : out.toString();
    }
}
