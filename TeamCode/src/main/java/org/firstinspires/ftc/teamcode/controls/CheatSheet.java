package org.firstinspires.ftc.teamcode.controls;

import java.util.Locale;
import java.util.Map;

/**
 * A printable one-page controller sheet, in a team's colours: the driver's and the operator's
 * inputs side by side, from the same {@link ControlScheme#layout} the robot runs, so the paper and
 * the Driver Station's Controls page cannot disagree. {@code CheatSheetTest} writes one HTML file
 * per team to
 * {@code TeamCode/build/cheat-sheets/}; open it in a browser and print (landscape, one page).
 *
 * <p>The header carries the team's logo when one is given (a {@code data:} URI; the test reads
 * {@code TeamCode/cheat-sheets/logo-<number>.jpg}, {@code .png} or {@code .svg}), and otherwise a
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
            {Input.L2, Input.R2, Input.L1, Input.R1},
            {Input.CROSS, Input.CIRCLE, Input.SQUARE, Input.TRIANGLE},
            {Input.DPAD_UP, Input.DPAD_DOWN, Input.DPAD_LEFT, Input.DPAD_RIGHT},
    };
    private static final String[] GROUP_TITLES = {"Triggers · bumpers", "Buttons", "D-pad"};

    /** {@code team}'s sheet, as a standalone HTML page. {@code logoDataUri} may be null. */
    public static String html(Team team, String logoDataUri) {
        Map<Action, Input> layout = ControlScheme.layout(ControlScheme.DEFAULT_FLOWER_PICK);
        StringBuilder out = new StringBuilder();
        out.append("<!doctype html>\n<html><head><meta charset=\"utf-8\"><title>")
                .append(team.number).append(' ').append(team.name).append(" controls</title>\n<style>\n")
                .append(":root{--deep:").append(team.deep).append(";--bright:").append(team.bright).append(";}\n")
                .append(STYLE)
                .append("</style></head><body>\n<div class=\"page\">\n");

        if (logoDataUri != null) {
            // The logo is the team's name and number: the header stays white around it.
            out.append("<header class=\"logo\">\n<img class=\"wordmark\" src=\"").append(logoDataUri)
                    .append("\" alt=\"").append(escape(team.name)).append(' ').append(team.number).append("\">")
                    .append("\n<div class=\"title\"><div class=\"sub\">BIOBUZZ TeleOp controls</div></div>\n");
        } else {
            out.append("<header>\n<div class=\"mark\">").append(emblem(team))
                    .append("</div>\n<div class=\"title\"><div class=\"team\">").append(escape(team.name.toUpperCase(Locale.US)))
                    .append(" <span>").append(team.number).append("</span></div>")
                    .append("<div class=\"sub\">BIOBUZZ TeleOp controls</div></div>\n");
        }
        out.append("</header>\n");

        out.append("<main>\n");
        column(out, "Driver", "gamepad 1", layout, Action.Pad.DRIVER);
        column(out, "Operator", "gamepad 2", layout, Action.Pad.OPERATOR);
        out.append("</main>\n");
        out.append("<div class=\"pad-key\">").append(padKey()).append("</div>\n");

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

    // ----------------------------------------------------------------- the controller drawing

    /** Drawing units: the controller is drawn in a 400 x 250 box, shoulders at the top. */
    private static final double PAD_W = 400;
    /** The whole key, controller in the middle and names either side, in page pixels. */
    private static final double KEY_W = 960;
    private static final double KEY_H = 220;
    private static final double SCALE = 0.82;
    private static final double PAD_X = (KEY_W - PAD_W * SCALE) / 2;
    private static final double PAD_Y = 6;
    /** Space between two names' baselines, page pixels. */
    private static final double LABEL_STEP = 24;

    /** Where each input sits on the drawing, in drawing units; the name's line ends here. */
    private static double[] anchor(Input input) {
        switch (input) {
            case L2: return new double[]{92, 14};
            case L1: return new double[]{100, 43};
            case R2: return new double[]{308, 14};
            case R1: return new double[]{300, 43};
            case DPAD_UP: return new double[]{100, 95};
            case DPAD_LEFT: return new double[]{80, 115};
            case DPAD_RIGHT: return new double[]{120, 115};
            case DPAD_DOWN: return new double[]{100, 135};
            case TRIANGLE: return new double[]{300, 93};
            case SQUARE: return new double[]{278, 115};
            case CIRCLE: return new double[]{322, 115};
            case CROSS: return new double[]{300, 137};
            case LEFT_STICK: return new double[]{150, 165};
            default: return new double[]{250, 165}; // RIGHT_STICK_X
        }
    }

    private static final Input[] LEFT_SIDE = {Input.L2, Input.L1, Input.DPAD_UP, Input.DPAD_LEFT,
            Input.DPAD_RIGHT, Input.DPAD_DOWN, Input.LEFT_STICK};
    private static final Input[] RIGHT_SIDE = {Input.R2, Input.R1, Input.TRIANGLE, Input.SQUARE,
            Input.CIRCLE, Input.CROSS, Input.RIGHT_STICK_X};

    /**
     * Where the buttons are: one PS5 controller seen from the front, its shoulders above, every
     * input named with a line to it, in the same colours as the chips in the tables.
     */
    private static String padKey() {
        StringBuilder svg = new StringBuilder();
        svg.append(String.format(Locale.US, "<svg viewBox=\"0 0 %.0f %.0f\">", KEY_W, KEY_H));
        svg.append(String.format(Locale.US, "<g transform=\"translate(%.1f %.1f) scale(%.2f)\">", PAD_X, PAD_Y, SCALE));
        // Triggers behind, bumpers in front, then the body over the bumpers' lower edge.
        shape(svg, Input.L2, "<path d=\"M62 34 C62 8 78 2 92 2 C108 2 122 8 122 34 Z\"");
        shape(svg, Input.R2, "<path d=\"M278 34 C278 8 292 2 308 2 C322 2 338 8 338 34 Z\"");
        shape(svg, Input.L1, "<rect x=\"58\" y=\"32\" width=\"84\" height=\"20\" rx=\"9\"");
        shape(svg, Input.R1, "<rect x=\"258\" y=\"32\" width=\"84\" height=\"20\" rx=\"9\"");
        svg.append("<path d=\"M108 50 C150 44 250 44 292 50 C340 54 362 68 372 108 C384 156 396 206 386 230 "
                + "C378 248 350 250 338 232 C322 206 300 190 270 186 C230 182 170 182 130 186 C100 190 78 206 62 232 "
                + "C50 250 22 248 14 230 C4 206 16 156 28 108 C38 68 60 54 108 50 Z\" fill=\"#fff\" stroke=\"#16161d\" "
                + "stroke-width=\"2.5\"/>");
        svg.append("<rect x=\"152\" y=\"54\" width=\"96\" height=\"62\" rx=\"10\" fill=\"none\" stroke=\"#bbb\" stroke-width=\"2\"/>");
        shape(svg, Input.DPAD_UP, "<rect x=\"91\" y=\"86\" width=\"18\" height=\"20\" rx=\"3\"");
        shape(svg, Input.DPAD_DOWN, "<rect x=\"91\" y=\"124\" width=\"18\" height=\"20\" rx=\"3\"");
        shape(svg, Input.DPAD_LEFT, "<rect x=\"70\" y=\"106\" width=\"20\" height=\"18\" rx=\"3\"");
        shape(svg, Input.DPAD_RIGHT, "<rect x=\"110\" y=\"106\" width=\"20\" height=\"18\" rx=\"3\"");
        shape(svg, Input.TRIANGLE, "<circle cx=\"300\" cy=\"93\" r=\"11\"");
        shape(svg, Input.SQUARE, "<circle cx=\"278\" cy=\"115\" r=\"11\"");
        shape(svg, Input.CIRCLE, "<circle cx=\"322\" cy=\"115\" r=\"11\"");
        shape(svg, Input.CROSS, "<circle cx=\"300\" cy=\"137\" r=\"11\"");
        svg.append("<path d=\"M300 87 L295 97 L305 97 Z M274 111 h8 v8 h-8 Z\" fill=\"none\" stroke=\"#fff\" stroke-width=\"1.8\"/>")
                .append("<circle cx=\"322\" cy=\"115\" r=\"4.5\" fill=\"none\" stroke=\"#fff\" stroke-width=\"1.8\"/>")
                .append("<path d=\"M296 133 L304 141 M304 133 L296 141\" stroke=\"#fff\" stroke-width=\"1.8\"/>");
        shape(svg, Input.LEFT_STICK, "<circle cx=\"150\" cy=\"165\" r=\"19\"");
        shape(svg, Input.RIGHT_STICK_X, "<circle cx=\"250\" cy=\"165\" r=\"19\"");
        svg.append("</g>");
        names(svg, LEFT_SIDE, true);
        names(svg, RIGHT_SIDE, false);
        svg.append("</svg>");
        return svg.toString();
    }

    /** One part of the controller, in its chip's colour. */
    private static void shape(StringBuilder svg, Input input, String open) {
        svg.append(open).append(" fill=\"").append(fill(input)).append("\" stroke=\"#16161d\" stroke-width=\"2\"/>");
    }

    /** The names down one side, each as level with its input as the names above it allow. */
    private static void names(StringBuilder svg, Input[] inputs, boolean left) {
        double next = 14;
        double textX = left ? PAD_X - 70 : PAD_X + PAD_W * SCALE + 70;
        for (Input input : inputs) {
            double[] a = anchor(input);
            double ax = PAD_X + a[0] * SCALE;
            double ay = PAD_Y + a[1] * SCALE;
            double y = Math.max(ay + 5, next);
            next = y + LABEL_STEP;
            svg.append(String.format(Locale.US,
                    "<polyline points=\"%.1f,%.1f %.1f,%.1f %.1f,%.1f\" fill=\"none\" stroke=\"#8a8a95\" stroke-width=\"1\"/>",
                    textX + (left ? 6 : -6), y - 5, textX + (left ? 20 : -20), y - 5, ax, ay));
            svg.append(String.format(Locale.US, "<circle cx=\"%.1f\" cy=\"%.1f\" r=\"2.2\" fill=\"#16161d\"/>", ax, ay));
            svg.append(String.format(Locale.US,
                    "<text x=\"%.1f\" y=\"%.1f\" text-anchor=\"%s\" font-size=\"15\" font-weight=\"700\" fill=\"%s\">%s</text>",
                    textX, y, left ? "end" : "start", text(input), escape(input.label)));
        }
    }

    /** A part's fill on the drawing: the face buttons' own colours, the rest the team colour. */
    private static String fill(Input input) {
        switch (input) {
            case CROSS: return "#1d4ed8";
            case CIRCLE: return "#c81e1e";
            case SQUARE: return "#b0277f";
            case TRIANGLE: return "#11804a";
            case LEFT_STICK: case RIGHT_STICK_X: return "#d9d9e0";
            default: return "var(--bright)";
        }
    }

    /** A name's colour: the face buttons' own, the rest near-black. */
    private static String text(Input input) {
        return input.isFaceOrDpad() && !input.name().startsWith("DPAD") ? fill(input) : "#16161d";
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
            + "header.logo{background:#fff;color:var(--deep);border-radius:0;padding:4px 4px 12px;"
            + "border-bottom:6px solid var(--deep);border-image:linear-gradient(90deg,var(--deep),var(--bright)) 1}\n"
            + ".wordmark{height:70px;width:auto;flex:none}\n"
            + "header.logo .sub{font-size:26px;font-weight:800;opacity:1;letter-spacing:.02em}\n"
            + ".title{flex:1}\n"
            + ".team{font-size:34px;font-weight:900;letter-spacing:.06em;line-height:1}\n"
            + ".team span{font-weight:300;opacity:.85}\n"
            + ".sub{font-size:15px;margin-top:6px;opacity:.92;letter-spacing:.04em}\n"
            + "main{display:grid;grid-template-columns:1fr 1fr;gap:.25in;min-height:0}\n"
            + "section{border:2px solid var(--deep);border-radius:14px;padding:10px 14px}\n"
            + ".pad-key svg{display:block;width:100%;height:auto}\n"
            + "h2{margin:0 0 4px;font-size:22px;color:var(--deep);text-transform:uppercase;letter-spacing:.06em}\n"
            + "h2 small{font-size:13px;color:#666;text-transform:none;letter-spacing:0;font-weight:400}\n"
            + "h3{margin:8px 0 2px;font-size:12px;text-transform:uppercase;letter-spacing:.14em;color:var(--bright);"
            + "border-bottom:1px solid #ddd;padding-bottom:2px}\n"
            + "table{width:100%;border-collapse:collapse}\n"
            + "td{padding:2px 0;font-size:15px;vertical-align:middle}\n"
            + "td:first-child{width:8.4em}\n"
            + ".chip{display:inline-flex;align-items:center;gap:5px;padding:2px 9px 2px 7px;border-radius:999px;"
            + "font-size:13.5px;font-weight:700;background:#ececf1;color:#16161d;white-space:nowrap}\n"
            + ".chip i{font-style:normal;font-size:13px}\n"
            + ".chip.cross{background:#dbe7ff;color:#1d4ed8}\n"
            + ".chip.circle{background:#ffe0e0;color:#c81e1e}\n"
            + ".chip.square{background:#fbe1f2;color:#b0277f}\n"
            + ".chip.triangle{background:#d9f5e6;color:#11804a}\n"
            + ".chip.pad{background:#16161d;color:#fff}\n";
}
