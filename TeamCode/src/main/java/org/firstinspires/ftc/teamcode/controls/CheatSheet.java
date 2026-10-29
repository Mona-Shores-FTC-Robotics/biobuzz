package org.firstinspires.ftc.teamcode.controls;

import java.util.Locale;
import java.util.Map;

/**
 * A printable one-page controller sheet for a {@link ControlScheme}, in a team's colours: the
 * driver's and the operator's inputs side by side, from the same {@link ControlScheme#layout} the
 * robot runs, so the paper and the Driver Station's Controls page cannot disagree.
 * {@code CheatSheetTest} writes one HTML file per team and scheme to
 * {@code TeamCode/build/cheat-sheets/}; open it in a browser and print (landscape, one page).
 *
 * <p>The header carries the team's logo when one is given (a {@code data:} URI; the test reads
 * {@code TeamCode/cheat-sheets/logo-<number>.png} or {@code .svg} if it exists), and otherwise a
 * drawn emblem: the team number on its colour.
 */
public final class CheatSheet {

    private CheatSheet() {
    }

    /** Our two teams, with the colours their sheets are printed in. */
    public enum Team {
        INFRARED(19429, "Infrared", "#B0101E", "#FF6A00"),
        ULTRAVIOLET(20245, "Ultraviolet", "#3A0CA3", "#B14EFF");

        public final int number;
        public final String name;
        final String deep;
        final String bright;

        Team(int number, String name, String deep, String bright) {
            this.number = number;
            this.name = name;
            this.deep = deep;
            this.bright = bright;
        }
    }

    /** The order and grouping inputs are printed in: how a hand finds them on the pad. */
    private static final Input[][] GROUPS = {
            {Input.LEFT_STICK, Input.RIGHT_STICK_X, Input.L2, Input.R2, Input.L1, Input.R1},
            {Input.CROSS, Input.CIRCLE, Input.SQUARE, Input.TRIANGLE},
            {Input.DPAD_UP, Input.DPAD_DOWN, Input.DPAD_LEFT, Input.DPAD_RIGHT},
    };
    private static final String[] GROUP_TITLES = {"Sticks · triggers · bumpers", "Buttons", "D-pad"};

    /** One scheme's sheet for {@code team}, as a standalone HTML page. {@code logoDataUri} may be null. */
    public static String html(ControlScheme scheme, Team team, String logoDataUri) {
        Map<Action, Input> layout = scheme.layout(ControlScheme.DEFAULT_FLOWER_PICK);
        StringBuilder out = new StringBuilder();
        out.append("<!doctype html>\n<html><head><meta charset=\"utf-8\"><title>")
                .append(team.number).append(' ').append(team.name).append(" controls, scheme ")
                .append(scheme.name()).append("</title>\n<style>\n")
                .append(":root{--deep:").append(team.deep).append(";--bright:").append(team.bright).append(";}\n")
                .append(STYLE)
                .append("</style></head><body>\n<div class=\"page\">\n");

        out.append("<header>\n<div class=\"mark\">");
        if (logoDataUri != null) {
            out.append("<img src=\"").append(logoDataUri).append("\" alt=\"").append(escape(team.name)).append(" logo\">");
        } else {
            out.append(emblem(team));
        }
        out.append("</div>\n<div class=\"title\"><div class=\"team\">").append(escape(team.name.toUpperCase(Locale.US)))
                .append(" <span>").append(team.number).append("</span></div>")
                .append("<div class=\"sub\">BIOBUZZ TeleOp controls</div></div>\n")
                .append("<div class=\"scheme\"><div class=\"k\">Scheme</div><div class=\"v\">").append(scheme.name())
                .append("</div><div class=\"d\">").append(escape(scheme.title.substring(scheme.title.indexOf(':') + 1).trim()))
                .append("</div></div>\n</header>\n");

        out.append("<main>\n");
        column(out, "Driver", "gamepad 1", layout, Action.Pad.DRIVER);
        column(out, "Operator", "gamepad 2", layout, Action.Pad.OPERATOR);
        out.append("</main>\n");

        out.append("<footer><div><b>Before PLAY, either gamepad:</b> ").append(chip(Input.SQUARE)).append(" Blue · ")
                .append(chip(Input.CIRCLE)).append(" Red alliance</div>")
                .append("<div><b>Gamepad 1 Share:</b> next Driver Station page</div>")
                .append("<div class=\"src\">From <code>ControlScheme</code>, the table the robot runs · scheme set in Panels, applies at INIT</div></footer>\n");
        out.append("</div>\n</body></html>\n");
        return out.toString();
    }

    private static void column(StringBuilder out, String who, String pad, Map<Action, Input> layout, Action.Pad side) {
        out.append("<section><h2>").append(who).append(" <small>").append(pad).append("</small></h2>\n");
        for (int g = 0; g < GROUPS.length; g++) {
            StringBuilder rows = new StringBuilder();
            for (Input input : GROUPS[g]) {
                for (Map.Entry<Action, Input> entry : layout.entrySet()) {
                    if (entry.getKey().pad != side || entry.getValue() != input) continue;
                    rows.append("<tr><td>").append(chip(input)).append("</td><td>")
                            .append(escape(entry.getKey().description)).append("</td></tr>\n");
                }
            }
            if (rows.length() == 0) continue;
            out.append("<h3>").append(GROUP_TITLES[g]).append("</h3>\n<table>\n").append(rows).append("</table>\n");
        }
        out.append("</section>\n");
    }

    /** An input as a chip: PlayStation face buttons in their own colours, the rest neutral. */
    private static String chip(Input input) {
        String cls;
        String glyph;
        switch (input) {
            case CROSS: cls = "cross"; glyph = "✕"; break;
            case CIRCLE: cls = "circle"; glyph = "○"; break;
            case SQUARE: cls = "square"; glyph = "□"; break;
            case TRIANGLE: cls = "triangle"; glyph = "△"; break;
            case DPAD_UP: cls = "pad"; glyph = "▲"; break;
            case DPAD_DOWN: cls = "pad"; glyph = "▼"; break;
            case DPAD_LEFT: cls = "pad"; glyph = "◀"; break;
            case DPAD_RIGHT: cls = "pad"; glyph = "▶"; break;
            default: cls = "plain"; glyph = "";
        }
        return "<span class=\"chip " + cls + "\">" + (glyph.isEmpty() ? "" : "<i>" + glyph + "</i>")
                + escape(input.label) + "</span>";
    }

    /** The team number on a disc of the team's colour, with a wave across it: a stand-in for a logo. */
    private static String emblem(Team team) {
        return "<svg viewBox=\"0 0 120 120\" width=\"78\" height=\"78\" aria-label=\"" + team.number + "\">"
                + "<defs><linearGradient id=\"g\" x1=\"0\" y1=\"0\" x2=\"1\" y2=\"1\">"
                + "<stop offset=\"0\" stop-color=\"" + team.deep + "\"/><stop offset=\"1\" stop-color=\"" + team.bright + "\"/>"
                + "</linearGradient></defs>"
                + "<circle cx=\"60\" cy=\"60\" r=\"56\" fill=\"url(#g)\"/>"
                + "<circle cx=\"60\" cy=\"60\" r=\"49\" fill=\"none\" stroke=\"#fff\" stroke-opacity=\".55\" stroke-width=\"2\"/>"
                + "<path d=\"M14 78 Q27 66 40 78 T66 78 T92 78 T118 78\" fill=\"none\" stroke=\"#fff\" stroke-opacity=\".7\" stroke-width=\"3\"/>"
                + "<text x=\"60\" y=\"66\" text-anchor=\"middle\" font-family=\"Arial Black,Arial,sans-serif\" font-weight=\"900\""
                + " font-size=\"27\" fill=\"#fff\">" + team.number + "</text></svg>";
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static final String STYLE = ""
            + "@page{size:letter landscape;margin:0}\n"
            + "*{box-sizing:border-box}\n"
            + "html,body{margin:0;background:#fff;color:#16161d;font-family:'Helvetica Neue',Arial,sans-serif;"
            + "-webkit-print-color-adjust:exact;print-color-adjust:exact}\n"
            + ".page{width:11in;height:8.5in;padding:.35in .4in;display:flex;flex-direction:column;gap:.18in}\n"
            + "header{display:flex;align-items:center;gap:18px;padding:12px 18px;border-radius:14px;color:#fff;"
            + "background:linear-gradient(110deg,var(--deep),var(--bright))}\n"
            + ".mark{width:78px;height:78px;flex:none;display:flex;align-items:center;justify-content:center}\n"
            + ".mark img{max-width:78px;max-height:78px;background:#fff;border-radius:10px;padding:4px}\n"
            + ".title{flex:1}\n"
            + ".team{font-size:34px;font-weight:900;letter-spacing:.06em;line-height:1}\n"
            + ".team span{font-weight:300;opacity:.85}\n"
            + ".sub{font-size:15px;margin-top:6px;opacity:.92;letter-spacing:.04em}\n"
            + ".scheme{text-align:center;background:rgba(255,255,255,.16);border:1px solid rgba(255,255,255,.45);"
            + "border-radius:12px;padding:6px 16px}\n"
            + ".scheme .k{font-size:11px;text-transform:uppercase;letter-spacing:.14em;opacity:.9}\n"
            + ".scheme .v{font-size:40px;font-weight:900;line-height:1}\n"
            + ".scheme .d{font-size:11px;opacity:.95}\n"
            + "main{flex:1;display:grid;grid-template-columns:1fr 1fr;gap:.25in;min-height:0}\n"
            + "section{border:2px solid var(--deep);border-radius:14px;padding:10px 14px}\n"
            + "h2{margin:0 0 4px;font-size:22px;color:var(--deep);text-transform:uppercase;letter-spacing:.06em}\n"
            + "h2 small{font-size:13px;color:#666;text-transform:none;letter-spacing:0;font-weight:400}\n"
            + "h3{margin:12px 0 3px;font-size:12px;text-transform:uppercase;letter-spacing:.14em;color:var(--bright);"
            + "border-bottom:1px solid #ddd;padding-bottom:2px}\n"
            + "table{width:100%;border-collapse:collapse}\n"
            + "td{padding:4px 0;font-size:15.5px;vertical-align:middle}\n"
            + "td:first-child{width:8.4em}\n"
            + ".chip{display:inline-flex;align-items:center;gap:5px;padding:2px 9px 2px 7px;border-radius:999px;"
            + "font-size:13.5px;font-weight:700;background:#ececf1;color:#16161d;white-space:nowrap}\n"
            + ".chip i{font-style:normal;font-size:13px}\n"
            + ".chip.cross{background:#dbe7ff;color:#1d4ed8}\n"
            + ".chip.circle{background:#ffe0e0;color:#c81e1e}\n"
            + ".chip.square{background:#fbe1f2;color:#b0277f}\n"
            + ".chip.triangle{background:#d9f5e6;color:#11804a}\n"
            + ".chip.pad{background:#16161d;color:#fff}\n"
            + "footer{display:flex;gap:22px;align-items:center;font-size:12px;color:#333;flex-wrap:wrap}\n"
            + "footer .src{margin-left:auto;color:#888;font-size:10px}\n";
}
