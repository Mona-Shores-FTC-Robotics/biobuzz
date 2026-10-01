package org.firstinspires.ftc.teamcode.logging;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Where match logs go and what they are called: the match ID (when the match's first OpMode
 * started), then that OpMode's name.
 *
 * <pre>
 * 2026-10-04_14-32-10_Right_Start_Tip.wpilog    (a match: its Auto, the break and its TeleOp)
 * 2026-10-04_14-40-55_Drive_TeleOp.wpilog       (a practice TeleOp with no Auto before it)
 * </pre>
 *
 * <p>Each new file gets a name not yet taken: a clash gets {@code _2}, {@code _3}…, so nothing is
 * overwritten.
 */
public final class MatchLogFiles {

    /** The folder inside the SDK's FIRST folder: {@code /sdcard/FIRST/logs} on a hub. */
    public static final String LOGS_FOLDER = "logs";

    private MatchLogFiles() {
    }

    /** A match ID for a match starting now: its local start time, to the second. */
    public static String matchId(long epochMs) {
        return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(new Date(epochMs));
    }

    /** A file in {@code dir} that does not exist yet. */
    public static File next(File dir, String matchId, String opModeName) {
        String base = safe(matchId) + "_" + safe(opModeName);
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
