package org.firstinspires.ftc.teamcode.opmodes;

import org.firstinspires.ftc.teamcode.controls.ControlsExport;
import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.localization.PinpointTrust;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The TeleOp's Driver Station Match page as data: every line {@link BasicDriveTeleOp#onLoop} can
 * write there, in its order, each with when it shows and its HTML in the states a driver can reach.
 * {@code CheatSheetTest} writes it to {@code TeamCode/cheat-sheets/match-page.json}, beside
 * {@code controls.json} ({@link ControlsExport}), and fails while the committed copy is stale. The
 * Visualizer's driver drill draws its simulated Driver Station from that file, so the drill shows
 * the robot's own lines and never a copy typed in by hand.
 *
 * <p>Every sample is the real helper's output ({@code BasicDriveTeleOp.launchTrimLine} and the
 * rest), so it lives beside them in this package; they stay package-private.
 *
 * <p>Shared values only, as in {@link ControlsExport}: a robot's {@code RobotDifferences} lines
 * are not applied.
 */
public final class MatchPageExport {

    /** The launch trim's samples: −{@value} … +{@value} ticks/s. */
    static final int LAUNCH_TRIM_SPAN_TPS = 150;
    static final int LAUNCH_TRIM_STEP_TPS = 5;
    /** The aim trim's fine samples: −{@value} … +{@value}°, every half degree. */
    static final double AIM_TRIM_SPAN_DEG = 10.0;
    static final double AIM_TRIM_STEP_DEG = 0.5;
    /** Past the fine span, a loud sample every {@value}° out to ±180. */
    static final int AIM_TRIM_LOUD_STEP_DEG = 5;

    private MatchPageExport() {
    }

    /** One Match-page line: an id, when it shows, and its HTML by state (a list of lines for the readout). */
    static final class Row {
        final String id;
        final String shownWhen;
        final Map<String, Object> samples = new LinkedHashMap<>();

        Row(String id, String shownWhen) {
            this.id = id;
            this.shownWhen = shownWhen;
        }

        Row add(String key, Object html) {
            samples.put(key, html);
            return this;
        }
    }

    /** The rows, in {@code onLoop}'s order. */
    static List<Row> rows() {
        List<Row> rows = new ArrayList<>();
        rows.add(new Row("arcBlocked", "A fire is refused by the firing arc: TeleOpConfig.enforceFiringArc is on and the"
                + " turret or its aim is outside the arc.")
                .add("aim", BasicDriveTeleOp.arcBlockedLine(true))
                .add("locked", BasicDriveTeleOp.arcBlockedLine(false)));
        rows.add(new Row("outsideArc", "The turret or its aim is outside the firing arc on a robot whose"
                + " TeleOpConfig.enforceFiringArc is off (instead of arcBlocked).")
                .add("outside", BasicDriveTeleOp.outsideArcLine()));
        rows.add(new Row("wrongNectar", "Wrong NECTAR may be in the lane (colorSensor.wrongNectarMayBeIn()): the feed"
                + " gate is shut to every fire button.")
                .add("shut", BasicDriveTeleOp.wrongNectarLine()));
        rows.add(practiceReadout());
        rows.add(new Row("turretMode", "Always, unless the turret is locked forward (TurretSubsystem.lockedForward).")
                .add("aim", BasicDriveTeleOp.turretModeLine(true))
                .add("locked", BasicDriveTeleOp.turretModeLine(false)));
        Row launchTrim = new Row("launchTrim", "Always: the operator's launch-speed trim, in ticks/s (keys are the trim).");
        for (int t = -LAUNCH_TRIM_SPAN_TPS; t <= LAUNCH_TRIM_SPAN_TPS; t += LAUNCH_TRIM_STEP_TPS) {
            launchTrim.add(String.valueOf(t), BasicDriveTeleOp.launchTrimLine(t));
        }
        rows.add(launchTrim);
        rows.add(new Row("launcher", "Always: IDLE while nothing spins the launcher, SPIN while it spins up, READY at speed.")
                .add("IDLE", BasicDriveTeleOp.launcherLine(false, false))
                .add("SPIN", BasicDriveTeleOp.launcherLine(true, false))
                .add("READY", BasicDriveTeleOp.launcherLine(true, true)));
        Row pieces = new Row("pieces", "Always: the lane tracker's count, green while the sensor reads the lane full"
                + " (keys: the count, \"_full\" while full).");
        for (int n = 0; n <= 4; n++) {
            pieces.add(String.valueOf(n), BasicDriveTeleOp.piecesLine(n, false));
            pieces.add(n + "_full", BasicDriveTeleOp.piecesLine(n, true));
        }
        rows.add(pieces);
        rows.add(new Row("fullHold", "TeleOpConfig.fullLoadGate holds a fire until the lane reads full.")
                .add("holding", BasicDriveTeleOp.fullHoldLine()));
        rows.add(new Row("cellHold", "TeleOpConfig.holdFireAtDownCell holds a fire while the camera sees our CELL down.")
                .add("holding", BasicDriveTeleOp.cellHoldLine()));
        Row cell = new Row("ourCell", "Always: our end's CELL as the camera sees it now, else the last reading within "
                + num(OurCell.MEMORY_S) + " s and its age, else ? (keys: UP, DOWN, ?, or the last reading and its"
                + " age in whole seconds, \"UP_3s\").")
                .add("UP", OurCell.line(OurCell.Reading.UP, OurCell.Reading.UP, 0.0))
                .add("DOWN", OurCell.line(OurCell.Reading.DOWN, OurCell.Reading.DOWN, 0.0));
        for (OurCell.Reading last : new OurCell.Reading[]{OurCell.Reading.UP, OurCell.Reading.DOWN}) {
            for (int age = 0; age <= (int) OurCell.MEMORY_S; age++) {
                cell.add(last + "_" + age + "s", OurCell.line(OurCell.Reading.UNKNOWN, last, age));
            }
        }
        cell.add("?", OurCell.line(OurCell.Reading.UNKNOWN, OurCell.Reading.UNKNOWN, Double.NaN));
        rows.add(cell);
        rows.add(new Row("loop", "Always: the last loop's time, which only the robot knows.")
                .add("--", BasicDriveTeleOp.loopLine(Double.NaN)));
        rows.add(new Row("pinpoint", "Always: the Pinpoint's state (OK; CHECK while it reads again unconfirmed;"
                + " LOST; NONE when it is not there; the fault count once it has lost the pose since INIT).")
                .add("OK", BasicDriveTeleOp.pinpointLine(true, PinpointTrust.State.TRUSTED, 0))
                .add("CHECK", BasicDriveTeleOp.pinpointLine(true, PinpointTrust.State.CONFIRMING, 0))
                .add("LOST", BasicDriveTeleOp.pinpointLine(true, PinpointTrust.State.FAULTED, 0))
                .add("NONE", BasicDriveTeleOp.pinpointLine(false, PinpointTrust.State.TRUSTED, 0))
                .add("1 FAULT", BasicDriveTeleOp.pinpointLine(true, PinpointTrust.State.TRUSTED, 1)));
        List<Double> aimTrims = aimTrims();
        Row aimTrim = new Row("aimTrim", "Always, unless the turret is locked forward: the operator's aim trim in"
                + " degrees, left positive (keys: the trim to 0.1).");
        Row aimHint = new Row("aimTrimHint", "Under aimTrim while the aim trim is past "
                + num(BasicDriveTeleOp.AIM_TRIM_LOUD_DEG) + "° either way (keys as aimTrim's).");
        for (double d : aimTrims) {
            String key = String.format(Locale.ROOT, "%.1f", d);
            aimTrim.add(key, BasicDriveTeleOp.aimTrimLine(d));
            String hint = BasicDriveTeleOp.aimTrimHint(d);
            if (hint != null) aimHint.add(key, hint);
        }
        rows.add(aimTrim);
        rows.add(aimHint);
        Row alert = new Row("alert", "The one thing that needs doing, at most one, the first of these that holds"
                + " (keys in that order).");
        for (String a : BasicDriveTeleOp.MATCH_ALERTS) alert.add(a, BasicDriveTeleOp.matchAlertLine(a));
        rows.add(alert);
        rows.add(new Row("keepHolding", "A reset (gyro, camera, corner, turret zero) is held but not yet for "
                + num(BasicDriveTeleOp.RESET_HOLD_MS / 1000.0) + " s.")
                .add("holding", BasicDriveTeleOp.KEEP_HOLDING));
        return rows;
    }

    /** The aim trim's sample grid, from −180 up. */
    static List<Double> aimTrims() {
        List<Double> out = new ArrayList<>();
        for (int d = -180; d < -AIM_TRIM_SPAN_DEG; d += AIM_TRIM_LOUD_STEP_DEG) out.add((double) d);
        int steps = (int) Math.round(2 * AIM_TRIM_SPAN_DEG / AIM_TRIM_STEP_DEG);
        for (int i = 0; i <= steps; i++) out.add(-AIM_TRIM_SPAN_DEG + i * AIM_TRIM_STEP_DEG);
        for (int d = (int) AIM_TRIM_SPAN_DEG + AIM_TRIM_LOUD_STEP_DEG; d <= 180; d += AIM_TRIM_LOUD_STEP_DEG) out.add((double) d);
        return out;
    }

    /**
     * {@link CycleReadout#show}'s lines at the start and after each of two scripted cycles, run
     * through the real readout at 50 Hz: four pieces in, the fire press, a four-ball feed.
     */
    private static Row practiceReadout() {
        Row row = new Row("practiceReadout", "TeleOpConfig.practiceReadout is on (a practice drill), at the top of the"
                + " page; each sample is the readout's lines, in order, as an array.");
        CycleReadout readout = new CycleReadout(text -> { });
        readout.start(0.0);
        row.add("start", shown(readout));
        double[][] cycles = {
                // piece-in times, the press, the feed's open and close
                {2.0, 3.0, 4.0, 5.0, 7.0, 7.5, 8.5},
                {11.5, 12.0, 13.0, 14.5, 16.0, 16.5, 17.5},
        };
        double dt = 0.02;
        double t = 0.0;
        int lane = 0;
        int launched = 0;
        for (int c = 0; c < cycles.length; c++) {
            double[] k = cycles[c];
            double end = k[6] + 2.0;
            while (t < end - 1e-9) {
                double prev = t;
                t += dt;
                boolean pieceIn = false;
                for (int i = 0; i < 4; i++) if (k[i] > prev && k[i] <= t) pieceIn = true;
                if (pieceIn) lane++;
                boolean press = k[4] > prev && k[4] <= t;
                boolean gate = t >= k[5] && t < k[6];
                for (int b = 0; b < 4; b++) {
                    double shot = k[5] + (k[6] - k[5]) * (b + 0.5) / 4;
                    if (shot > prev && shot <= t) {
                        launched++;
                        lane = Math.max(0, lane - 1);
                    }
                }
                readout.update(t, pieceIn, lane >= 4, press, gate, launched);
            }
            row.add("cycle" + (c + 1), shown(readout));
        }
        return row;
    }

    private static List<String> shown(CycleReadout readout) {
        List<String> lines = new ArrayList<>();
        readout.show(new Display(lines::add));
        return lines;
    }

    /** The file, as JSON text with a newline at the end. */
    public static String json() {
        StringBuilder out = new StringBuilder();
        out.append("{\n");
        out.append("  \"about\": ").append(ControlsExport.quote("Generated by CheatSheetTest from TeamCode"
                + " opmodes/BasicDriveTeleOp's Match page (MatchPageExport); do not edit. The Driver Station's"
                + " Match page in the TeleOp: the header, then rows in the order the TeleOp writes them. Each row"
                + " shows only when its shownWhen holds; a sample is the row's HTML in one state (the practice"
                + " readout's is a list of lines). The DS renders the HTML: <big> 1.25x, <small> 0.8x, <tt>"
                + " monospace; repeated spaces collapse.")).append(",\n");
        out.append("  \"screenLines\": ").append(num(Display.SCREEN_LINES)).append(",\n");
        out.append("  \"charsPerLine\": ").append(num(Display.CHARS_PER_LINE)).append(",\n");
        out.append("  \"ttCharsPerLine\": ").append(num(Display.TT_CHARS_PER_LINE)).append(",\n");
        out.append("  \"boldScale\": ").append(num(Display.BOLD_SCALE)).append(",\n");
        out.append("  \"bigScale\": ").append(num(Display.BIG_SCALE)).append(",\n");
        out.append("  \"smallScale\": ").append(num(Display.SMALL_SCALE)).append(",\n");
        out.append("  \"levels\": {");
        Display.Level[] levels = Display.Level.values();
        for (int i = 0; i < levels.length; i++) {
            out.append(ControlsExport.quote(levels[i].name())).append(": ").append(ControlsExport.quote(levels[i].color()))
                    .append(i + 1 < levels.length ? ", " : "");
        }
        out.append("},\n");
        out.append("  \"header\": ").append(ControlsExport.quote(Display.headerLine(Display.Page.MATCH))).append(",\n");
        out.append("  \"rows\": [\n");
        List<Row> rows = rows();
        for (int r = 0; r < rows.size(); r++) {
            Row row = rows.get(r);
            out.append("    {\"id\": ").append(ControlsExport.quote(row.id))
                    .append(", \"shownWhen\": ").append(ControlsExport.quote(row.shownWhen))
                    .append(", \"samples\": {\n");
            int i = 0;
            for (Map.Entry<String, Object> e : row.samples.entrySet()) {
                out.append("      ").append(ControlsExport.quote(e.getKey())).append(": ").append(value(e.getValue()))
                        .append(++i < row.samples.size() ? ",\n" : "\n");
            }
            out.append("    }}").append(r + 1 < rows.size() ? ",\n" : "\n");
        }
        out.append("  ]\n");
        out.append("}\n");
        return out.toString();
    }

    private static String value(Object v) {
        if (v instanceof String) return ControlsExport.quote((String) v);
        StringBuilder b = new StringBuilder("[");
        List<?> lines = (List<?>) v;
        for (int i = 0; i < lines.size(); i++) {
            b.append(ControlsExport.quote((String) lines.get(i))).append(i + 1 < lines.size() ? ", " : "");
        }
        return b.append(']').toString();
    }

    private static String num(double v) {
        return v == Math.rint(v) && Math.abs(v) < 1e9 ? String.format(Locale.ROOT, "%.1f", v) : String.valueOf(v);
    }
}
