package org.firstinspires.ftc.teamcode.controls;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A printable one-page controller sheet, in a team's colours: two PS5 controllers, the driver's
 * and the operator's, each button labelled with what it does. It is drawn from the same
 * {@link ControlScheme#layout} the robot runs, so the paper and the Driver Station's Controls page
 * cannot disagree. {@code CheatSheetTest} writes one HTML file per team to
 * {@code TeamCode/build/cheat-sheets/}; open it in a browser and print (portrait, one page).
 *
 * <p>The header carries the team's logo when one is given (a {@code data:} URI; the test reads
 * {@code TeamCode/cheat-sheets/logo-<number>.jpg}, {@code .png} or {@code .svg}), and otherwise a
 * drawn emblem: the team number on its colour. The sticks are left unlabelled: everyone knows they
 * drive.
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
        pad(out, "Driver", "gamepad 1", layout, Action.Pad.DRIVER);
        pad(out, "Operator", "gamepad 2", layout, Action.Pad.OPERATOR);
        out.append("</div>\n</body></html>\n");
        return out.toString();
    }

    private static void pad(StringBuilder out, String who, String which, Map<Action, Input> layout, Action.Pad pad) {
        out.append("<section><h2>").append(who).append(" <small>").append(which).append("</small></h2>\n")
                .append(drawing(layout, pad)).append('\n');
        for (Map.Entry<Action, Input> entry : layout.entrySet()) {
            if (entry.getKey().pad == pad && entry.getKey().description.startsWith("*")) {
                out.append("<p class=\"note\">* work in progress</p>\n");
                break;
            }
        }
        out.append("</section>\n");
    }

    // ----------------------------------------------------------------- the controller drawing

    /** Drawing units: the controller is drawn in a 400 x 290 box, shoulders at the top. */
    private static final double PAD_W = 400;
    /** One drawing, controller in the middle and labels either side, in page pixels. */
    private static final double KEY_W = 760;
    private static final double KEY_H = 380;
    private static final double SCALE = 0.95;
    private static final double PAD_X = (KEY_W - PAD_W * SCALE) / 2;
    private static final double PAD_Y = 30;
    /** Space between two label lines, page pixels. */
    private static final double LINE = 18;
    /** Space between two labels, page pixels. */
    private static final double GAP = 8;
    /** Characters in one label line before it wraps. */
    private static final int WRAP_AT = 18;
    /** Room between the controller and its labels, page pixels: the lines bend in here. */
    private static final double GUTTER = 36;
    /** From a label's baseline up to the middle of its first line, page pixels. */
    private static final double MID = 5.5;

    // Top to bottom, in the order their lines run: a button hidden from the side by its neighbour
    // (the D-pad's right key, Square) is reached from above, so its label comes first.
    private static final Input[] LEFT_SIDE = {Input.L2, Input.L1, Input.DPAD_RIGHT, Input.DPAD_UP,
            Input.DPAD_LEFT, Input.DPAD_DOWN};
    private static final Input[] RIGHT_SIDE = {Input.R2, Input.R1, Input.SQUARE, Input.TRIANGLE,
            Input.CIRCLE, Input.CROSS};

    /**
     * How a label's line reaches {@code input}, drawing units: it runs level at {@code y} until
     * {@code x}, then, for {@code fromAbove}, drops to the button's top at {@code edgeY}.
     * Otherwise {@code x} is the button's outer edge and the line ends there.
     */
    private static double[] route(Input input) {
        // {y, x, fromAbove (1/0), edgeY}
        switch (input) {
            case L2: return new double[]{12, 58, 0, 0};
            case L1: return new double[]{34, 44, 0, 0};
            case DPAD_RIGHT: return new double[]{86, 96, 1, 122};
            case DPAD_UP: return new double[]{112, 70, 0, 0};
            case DPAD_LEFT: return new double[]{130, 48, 0, 0};
            case DPAD_DOWN: return new double[]{152, 70, 0, 0};
            case R2: return new double[]{12, 342, 0, 0};
            case R1: return new double[]{34, 356, 0, 0};
            case SQUARE: return new double[]{84, 299, 1, 119};
            case TRIANGLE: return new double[]{107, 333, 0, 0};
            case CIRCLE: return new double[]{130, 356, 0, 0};
            default: return new double[]{153, 333, 0, 0}; // CROSS
        }
    }

    /**
     * {@code pad}'s controller: a PS5 controller seen from the front, its shoulders above, each
     * input that person uses in its colour with a line to what it does; the rest grey.
     */
    private static String drawing(Map<Action, Input> layout, Action.Pad pad) {
        StringBuilder svg = new StringBuilder();
        svg.append(String.format(Locale.US, "<svg viewBox=\"0 0 %.0f %.0f\">", KEY_W, KEY_H));
        svg.append(String.format(Locale.US, "<g transform=\"translate(%.1f %.1f) scale(%.2f)\">", PAD_X, PAD_Y, SCALE));
        // Shoulders: each trigger behind its bumper, both just above the body's top edge.
        shape(svg, layout, pad, Input.L2, "<path d=\"M52 30 C52 10 64 2 85 2 C106 2 118 10 118 30 Z\"");
        shape(svg, layout, pad, Input.R2, "<path d=\"M282 30 C282 10 294 2 315 2 C336 2 348 10 348 30 Z\"");
        shape(svg, layout, pad, Input.L1, "<rect x=\"44\" y=\"26\" width=\"82\" height=\"16\" rx=\"8\"");
        shape(svg, layout, pad, Input.R1, "<rect x=\"274\" y=\"26\" width=\"82\" height=\"16\" rx=\"8\"");
        // The body, a DualSense from the front, drawn 30 below the shoulders.
        svg.append("<g transform=\"translate(0 30)\">");
        svg.append("<path d=\"M200 18 C250 18 290 16 315 20 C350 24 378 40 388 80 C398 120 402 175 395 215 "
                + "C390 245 360 258 345 245 C325 228 305 205 285 198 C260 192 140 192 115 198 "
                + "C95 205 75 228 55 245 C40 258 10 245 5 215 C-2 175 2 120 12 80 C22 40 50 24 85 20 "
                + "C110 16 150 18 200 18 Z\" fill=\"#fff\" stroke=\"#2a2a33\" stroke-width=\"2\"/>");
        // Touchpad, its light bars, Create and Options, the PS button.
        svg.append("<path d=\"M132 20 H268 L260 84 Q258 92 250 92 H150 Q142 92 140 84 Z\" fill=\"#fafafc\" "
                + "stroke=\"#9a9aa5\" stroke-width=\"1.5\"/>")
                .append("<path d=\"M126 26 L118 86 M274 26 L282 86\" stroke=\"#c9c9d1\" stroke-width=\"3\" stroke-linecap=\"round\"/>")
                .append("<rect x=\"106\" y=\"32\" width=\"7\" height=\"16\" rx=\"3.5\" fill=\"#fff\" stroke=\"#b5b5be\" stroke-width=\"1.5\"/>")
                .append("<rect x=\"287\" y=\"32\" width=\"7\" height=\"16\" rx=\"3.5\" fill=\"#fff\" stroke=\"#b5b5be\" stroke-width=\"1.5\"/>")
                .append("<circle cx=\"200\" cy=\"140\" r=\"6\" fill=\"#fff\" stroke=\"#b5b5be\" stroke-width=\"1.5\"/>")
                .append("<path d=\"M190 160 H210\" stroke=\"#c9c9d1\" stroke-width=\"3\" stroke-linecap=\"round\"/>");
        // D-pad: four arrow-shaped keys round a centre.
        shape(svg, layout, pad, Input.DPAD_UP, "<path d=\"M70 70 H86 V88 L78 96 L70 88 Z\" stroke-linejoin=\"round\"");
        shape(svg, layout, pad, Input.DPAD_DOWN, "<path d=\"M70 130 H86 V112 L78 104 L70 112 Z\" stroke-linejoin=\"round\"");
        shape(svg, layout, pad, Input.DPAD_LEFT, "<path d=\"M48 92 V108 H66 L74 100 L66 92 Z\" stroke-linejoin=\"round\"");
        shape(svg, layout, pad, Input.DPAD_RIGHT, "<path d=\"M108 92 V108 H90 L82 100 L90 92 Z\" stroke-linejoin=\"round\"");
        // Face buttons, each with its symbol drawn in its own colour.
        face(svg, layout, pad, Input.TRIANGLE, 322, 77, "<path d=\"M322 71 L316 82 H328 Z\"");
        face(svg, layout, pad, Input.SQUARE, 299, 100, "<rect x=\"294\" y=\"95\" width=\"10\" height=\"10\"");
        face(svg, layout, pad, Input.CIRCLE, 345, 100, "<circle cx=\"345\" cy=\"100\" r=\"5.5\"");
        face(svg, layout, pad, Input.CROSS, 322, 123, "<path d=\"M317 118 L327 128 M327 118 L317 128\"");
        // Sticks: a ring and a cap.
        stick(svg, layout, pad, Input.LEFT_STICK, 140, 145);
        stick(svg, layout, pad, Input.RIGHT_STICK_X, 260, 145);
        svg.append("</g>");
        svg.append("</g>");
        labels(svg, layout, pad, LEFT_SIDE, true);
        labels(svg, layout, pad, RIGHT_SIDE, false);
        svg.append("</svg>");
        return svg.toString();
    }

    /** One part of the controller: tinted in its colour if {@code pad}'s person uses it, else outlined grey. */
    private static void shape(StringBuilder svg, Map<Action, Input> layout, Action.Pad pad, Input input, String open) {
        boolean used = actionFor(layout, pad, input) != null;
        svg.append(open).append(used ? usedStyle(input) : " fill=\"#fff\" stroke=\"#c9c9d1\" stroke-width=\"1.5\"").append("/>");
    }

    /** A face button at ({@code cx}, {@code cy}) with its symbol, in its colour if used. */
    private static void face(StringBuilder svg, Map<Action, Input> layout, Action.Pad pad, Input input,
                             double cx, double cy, String symbol) {
        boolean used = actionFor(layout, pad, input) != null;
        shape(svg, layout, pad, input, String.format(Locale.US, "<circle cx=\"%.0f\" cy=\"%.0f\" r=\"11\"", cx, cy));
        svg.append(symbol).append(" fill=\"none\" stroke=\"").append(used ? stroke(input) : "#c9c9d1")
                .append("\" stroke-width=\"1.8\" stroke-linejoin=\"round\" stroke-linecap=\"round\"/>");
    }

    /** A stick: a ring and a cap, never labelled. */
    private static void stick(StringBuilder svg, Map<Action, Input> layout, Action.Pad pad, Input input, double cx, double cy) {
        svg.append(String.format(Locale.US, "<circle cx=\"%.0f\" cy=\"%.0f\" r=\"25\" fill=\"#fff\" stroke=\"#9a9aa5\" stroke-width=\"1.5\"/>", cx, cy));
        shape(svg, layout, pad, input, String.format(Locale.US, "<circle cx=\"%.0f\" cy=\"%.0f\" r=\"16\"", cx, cy));
    }

    /** A used part: a light tint of its colour, outlined in the colour itself. */
    private static String usedStyle(Input input) {
        switch (input) {
            case CROSS: return " fill=\"#dbe7ff\" stroke=\"#1d4ed8\" stroke-width=\"2\"";
            case CIRCLE: return " fill=\"#ffe0e0\" stroke=\"#c81e1e\" stroke-width=\"2\"";
            case SQUARE: return " fill=\"#fbe1f2\" stroke=\"#b0277f\" stroke-width=\"2\"";
            case TRIANGLE: return " fill=\"#d9f5e6\" stroke=\"#11804a\" stroke-width=\"2\"";
            default: return " fill=\"var(--bright)\" fill-opacity=\".3\" stroke=\"var(--deep)\" stroke-width=\"2\"";
        }
    }

    /** A used face button's colour, for its symbol. */
    private static String stroke(Input input) {
        switch (input) {
            case CROSS: return "#1d4ed8";
            case CIRCLE: return "#c81e1e";
            case SQUARE: return "#b0277f";
            default: return "#11804a";
        }
    }

    /**
     * The labels down one side, each as level with its button as the labels above it allow. A
     * label's line bends at right angles: out from the label, a jog in the gutter beside the
     * controller if the label had to move down, level across to the button, and for a button hidden
     * by its neighbour a drop onto its top. The jogs step closer to the controller label by label,
     * which is what keeps the lines from crossing.
     */
    private static void labels(StringBuilder svg, Map<Action, Input> layout, Action.Pad pad, Input[] inputs, boolean left) {
        int n = 0;
        Input[] used = new Input[inputs.length];
        for (Input input : inputs) {
            if (actionFor(layout, pad, input) != null) used[n++] = input;
        }
        if (n == 0) return;
        List<List<String>> lines = new ArrayList<>();
        double[] y = new double[n];
        double next = 18;
        for (int i = 0; i < n; i++) {
            lines.add(wrap(actionFor(layout, pad, used[i]).description));
            y[i] = Math.max(PAD_Y + route(used[i])[0] * SCALE + MID, next);
            next = y[i] + LINE * lines.get(i).size() + GAP;
        }
        double side = left ? -1 : 1;
        double bodyEdge = left ? PAD_X : PAD_X + PAD_W * SCALE;
        double textX = bodyEdge + side * GUTTER;
        for (int i = 0; i < n; i++) {
            Input input = used[i];
            double[] r = route(input);
            double routeY = PAD_Y + r[0] * SCALE;
            double toX = PAD_X + r[1] * SCALE;
            double labelY = y[i] - MID;
            double jogX = textX - side * (10 + i * 3.0);
            StringBuilder points = new StringBuilder();
            points.append(String.format(Locale.US, "%.1f,%.1f %.1f,%.1f %.1f,%.1f %.1f,%.1f",
                    textX - side * 5, labelY, jogX, labelY, jogX, routeY, toX, routeY));
            double endX = toX;
            double endY = routeY;
            if (r[2] > 0) {
                endY = PAD_Y + r[3] * SCALE;
                points.append(String.format(Locale.US, " %.1f,%.1f", toX, endY));
            }
            svg.append("<polyline points=\"").append(points)
                    .append("\" fill=\"none\" stroke=\"#8a8a95\" stroke-width=\"1.2\" stroke-linejoin=\"round\"/>");
            svg.append(String.format(Locale.US, "<circle cx=\"%.1f\" cy=\"%.1f\" r=\"2.2\" fill=\"#16161d\"/>", endX, endY));
            svg.append(String.format(Locale.US,
                    "<text x=\"%.1f\" y=\"%.1f\" text-anchor=\"%s\" font-size=\"15.5\" font-weight=\"700\" fill=\"#16161d\">",
                    textX, y[i], left ? "end" : "start"));
            svg.append("<title>").append(escape(actionFor(layout, pad, input).description)).append("</title>");
            for (int l = 0; l < lines.get(i).size(); l++) {
                svg.append(String.format(Locale.US, "<tspan x=\"%.1f\" dy=\"%.0f\">", textX, l == 0 ? 0 : LINE))
                        .append(escape(lines.get(i).get(l))).append("</tspan>");
            }
            svg.append("</text>");
        }
    }

    /** What {@code pad}'s person does with {@code input}, or null. The sticks are never labelled. */
    private static Action actionFor(Map<Action, Input> layout, Action.Pad pad, Input input) {
        if (input.isStick()) return null;
        for (Map.Entry<Action, Input> entry : layout.entrySet()) {
            if (entry.getKey().pad == pad && entry.getValue() == input) return entry.getKey();
        }
        return null;
    }

    /**
     * {@code text} in lines of at most {@link #WRAP_AT} characters, broken at spaces. A slash stays
     * with the word before it ("Spin Up /"), and a too-long label breaks before its bracket first,
     * so "Gyro Reset" stays whole; the bracket shares the last line when it fits.
     */
    private static List<String> wrap(String text) {
        String glued = text.replace(" / ", "\u00a0/ ");
        int bracket = glued.indexOf(" (");
        if (glued.length() <= WRAP_AT || bracket < 0) return wrapWords(glued);
        List<String> lines = wrapWords(glued.substring(0, bracket));
        String tail = glued.substring(bracket + 1);
        String last = lines.get(lines.size() - 1);
        if (lines.size() > 1 && last.length() + 1 + tail.length() <= WRAP_AT) {
            lines.set(lines.size() - 1, last + " " + tail);
        } else {
            lines.addAll(wrapWords(tail));
        }
        return lines;
    }

    private static List<String> wrapWords(String text) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            if (line.length() > 0 && line.length() + 1 + word.length() > WRAP_AT) {
                lines.add(line.toString());
                line.setLength(0);
            }
            if (line.length() > 0) line.append(' ');
            line.append(word);
        }
        lines.add(line.toString());
        return lines;
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
            + "@page{size:letter portrait;margin:0}\n"
            + "*{box-sizing:border-box}\n"
            + "html,body{margin:0;background:#fff;color:#16161d;font-family:'Helvetica Neue',Arial,sans-serif;"
            + "-webkit-print-color-adjust:exact;print-color-adjust:exact}\n"
            + ".page{width:8.5in;height:11in;padding:.35in .4in;display:flex;flex-direction:column;gap:.18in}\n"
            + "header{display:flex;align-items:center;gap:18px;padding:12px 18px;border-radius:14px;color:#fff;"
            + "background:linear-gradient(110deg,var(--deep),var(--bright))}\n"
            + ".mark{width:78px;height:78px;flex:none;display:flex;align-items:center;justify-content:center}\n"
            + "header.logo{background:#fff;color:var(--deep);border-radius:0;padding:4px 4px 12px;"
            + "border-bottom:6px solid var(--deep);border-image:linear-gradient(90deg,var(--deep),var(--bright)) 1}\n"
            + ".wordmark{height:76px;width:auto;flex:none}\n"
            + "header.logo .sub{font-size:26px;font-weight:800;opacity:1;letter-spacing:.02em}\n"
            + ".title{flex:1}\n"
            + ".team{font-size:34px;font-weight:900;letter-spacing:.06em;line-height:1}\n"
            + ".team span{font-weight:300;opacity:.85}\n"
            + ".sub{font-size:15px;margin-top:6px;opacity:.92;letter-spacing:.04em}\n"
            + "section{flex:1;min-height:0;border:2px solid var(--deep);border-radius:14px;padding:10px 14px;"
            + "display:flex;flex-direction:column}\n"
            + "h2{margin:0;font-size:24px;color:var(--deep);text-transform:uppercase;letter-spacing:.06em}\n"
            + "h2 small{font-size:14px;color:#666;text-transform:none;letter-spacing:0;font-weight:400}\n"
            + ".note{margin:0;font-size:13px;color:#555}\n"
            + "section svg{display:block;width:100%;height:auto;margin:auto 0}\n";
}
