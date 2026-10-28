package org.firstinspires.ftc.teamcode.controls;

import java.util.Map;

/**
 * A printable one-page controller sheet for a {@link ControlScheme}: the driver's and the
 * operator's inputs, from the same {@link ControlScheme#layout} the robot runs, so the paper and
 * the Driver Station's Controls page cannot disagree. {@code CheatSheetTest} writes one HTML file
 * per scheme to {@code TeamCode/build/cheat-sheets/}; open it in a browser and print.
 */
public final class CheatSheet {

    private CheatSheet() {
    }

    /** One scheme's sheet, as a standalone HTML page sized for one printed page. */
    public static String html(ControlScheme scheme) {
        Map<Action, Input> layout = scheme.layout(ControlScheme.DEFAULT_FLOWER_PICK);
        StringBuilder out = new StringBuilder();
        out.append("<!doctype html>\n<html><head><meta charset=\"utf-8\"><title>BIOBUZZ controls, scheme ")
                .append(scheme.name()).append("</title>\n<style>")
                .append("body{font-family:sans-serif;margin:24px;color:#111}")
                .append("h1{font-size:22px;margin:0 0 4px}p{margin:0 0 12px;font-size:13px}")
                .append("h2{font-size:17px;margin:16px 0 6px}")
                .append("table{border-collapse:collapse;width:100%;font-size:13px}")
                .append("td{border:1px solid #999;padding:4px 8px;vertical-align:top}")
                .append("td:first-child{width:28%;font-weight:bold}")
                .append("@page{size:letter;margin:12mm}")
                .append("</style></head><body>\n");
        out.append("<h1>BIOBUZZ TeleOp controls: scheme ").append(escape(scheme.title)).append("</h1>\n");
        out.append("<p>Generated from <code>ControlScheme</code>, the same table the robot runs. ")
                .append("The scheme is chosen in Panels (TeleOpConfig.scheme) and applies at INIT.</p>\n");
        section(out, "Driver: gamepad 1", layout, Action.Pad.DRIVER);
        section(out, "Operator: gamepad 2", layout, Action.Pad.OPERATOR);
        out.append("<h2>Either gamepad</h2>\n<table>\n");
        row(out, "Square (X) / Circle (B), before PLAY", "Choose Blue / Red alliance");
        row(out, "Share (Back), gamepad 1", "Next Driver Station page (Match, Controls, Robot)");
        out.append("</table>\n</body></html>\n");
        return out.toString();
    }

    private static void section(StringBuilder out, String title, Map<Action, Input> layout, Action.Pad pad) {
        out.append("<h2>").append(escape(title)).append("</h2>\n<table>\n");
        for (Map.Entry<Action, Input> entry : layout.entrySet()) {
            if (entry.getKey().pad != pad) continue;
            row(out, entry.getValue().label, entry.getKey().description);
        }
        out.append("</table>\n");
    }

    private static void row(StringBuilder out, String input, String action) {
        out.append("<tr><td>").append(escape(input)).append("</td><td>").append(escape(action))
                .append("</td></tr>\n");
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
