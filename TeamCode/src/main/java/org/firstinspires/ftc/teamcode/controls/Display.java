package org.firstinspires.ftc.teamcode.controls;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.function.Consumer;

/**
 * The Driver Station screen: three pages, cycled with gamepad 1's Back/Share button.
 *
 * <table>
 *   <caption>Pages</caption>
 *   <tr><td>{@link Page#MATCH}</td><td>What the OpMode writes in {@code onLoop()}. The default.</td></tr>
 *   <tr><td>{@link Page#CONTROLS}</td><td>Every binding, generated from {@link Bindings} labels.</td></tr>
 *   <tr><td>{@link Page#ROBOT}</td><td>One block per subsystem, from {@code Subsystem.describe}.</td></tr>
 * </table>
 *
 * <p>{@code RobotOpMode} owns switching and drawing the Controls and Robot pages. An OpMode only
 * writes its Match page, with these helpers or plain {@code telemetry} calls.
 *
 * <p>The Driver Station renders in HTML mode, which is what makes bold headings and coloured status
 * dots possible. Plain {@code telemetry.addData} still works; repeated spaces collapse, so align with
 * separators rather than padding.
 *
 * <p><b>The INIT page fits one screen.</b> The drive team reads it at the field and nobody scrolls
 * (mentor, 2 Nov 2026). Every line written through this class is counted, wrapping included
 * ({@link #linesUsed()}, {@link #rows}), so a page can leave out what does not fit
 * ({@link #room}) and a test can hold the worst case to {@link #SCREEN_LINES}. Lines written with
 * plain {@code telemetry} calls are not counted. After PLAY nobody at the field reads it: that page
 * is for the people testing, and may run longer.
 *
 * <p>Panels is for numbers and graphs; this is the human-readable view. DECODE's equivalent was a
 * telemetry service, ten data classes and three formatters. This is one class.
 */
public final class Display {

    public enum Page {
        MATCH("MATCH"), CONTROLS("CONTROLS"), ROBOT("ROBOT");

        final String title;

        Page(String title) {
            this.title = title;
        }
    }

    /** Status colour. */
    public enum Level {
        OK("#4CAF50"), WARN("#FFB300"), FAULT("#E53935");

        final String color;

        Level(String color) {
            this.color = color;
        }
    }

    private static final Page[] PAGES = Page.values();

    /**
     * Rows of ordinary telemetry the REV Driver Hub shows without scrolling: 16. Worked out on
     * 2 Nov 2026 from the v12.0 Driver Station app's layouts and FIRST's Driver Hub screenshot (no
     * source states it): the hub runs the landscape layout by default, telemetry has the right half
     * of its 800 x 480 screen, 14 sp text, about 23 px a row, and starts about 103 px down. A phone
     * in portrait shows far fewer; we use Driver Hubs.
     */
    public static final double SCREEN_LINES = 16;
    /** Characters of ordinary text in one row before it wraps: about 50 measured; 44 to be safe. */
    public static final double CHARS_PER_LINE = 44;
    /** Bold text is wider: this many times as many characters wrap it. */
    public static final double BOLD_SCALE = 0.9;
    /** Characters of monospace ({@code <tt>}) text in one row: about 34. */
    public static final double TT_CHARS_PER_LINE = 34;
    /** {@code <big>} text is 1.25 times ordinary (Android's RelativeSizeSpan): wider and taller. */
    public static final double BIG_SCALE = 1.25;
    /** {@code <small>} text is 0.8 times ordinary. */
    public static final double SMALL_SCALE = 0.8;

    private final Consumer<String> sink;
    private Page page = Page.MATCH;
    /** Screen height written since the last {@link #header()}, in ordinary rows, wrapping included. */
    private double used;

    public Display(Telemetry telemetry) {
        telemetry.setDisplayFormat(Telemetry.DisplayFormat.HTML);
        this.sink = telemetry::addLine;
    }

    /** A display that writes its lines somewhere else: a test's list. */
    public Display(Consumer<String> sink) {
        this.sink = sink;
    }

    private void add(String html) {
        sink.accept(html);
        used += rows(html);
    }

    /** Screen height written since the page's {@link #header()}, in ordinary rows, wrapping included. */
    public double linesUsed() {
        return used;
    }

    /** Ordinary rows left on one screen. */
    public double left() {
        return SCREEN_LINES - used;
    }

    /** Whether {@code rows} more ordinary rows fit on one screen. */
    public boolean room(double rows) {
        return used + rows <= SCREEN_LINES;
    }

    /**
     * The screen height {@code html} takes, in ordinary rows: its visible text over the width of one
     * row (at least one row), each row as tall as its largest text ({@code <big>} 1.25, all
     * {@code <small>} 0.8, {@code <tt>} narrower), plus one for each {@code <br>}. An estimate from
     * the text's length, on the safe side.
     */
    public static double rows(String html) {
        int breaks = 0;
        for (int i = html.indexOf("<br>"); i >= 0; i = html.indexOf("<br>", i + 4)) breaks++;
        int text = visibleLength(html);
        double perRow = html.contains("<tt>") ? TT_CHARS_PER_LINE : CHARS_PER_LINE;
        boolean bold = (html.startsWith("<b>") && html.endsWith("</b>")) || html.startsWith("<big><b>");
        if (bold) perRow *= BOLD_SCALE;
        double height = 1.0;
        if (html.contains("<big>")) {
            perRow /= BIG_SCALE;
            height = BIG_SCALE;
        } else if (html.startsWith("<small>") && html.endsWith("</small>")) {
            perRow /= SMALL_SCALE;
            height = SMALL_SCALE;
        }
        return breaks + height * Math.max(1, Math.ceil(text / perRow));
    }

    /**
     * Characters of {@code html} the screen shows: tags ({@code <...>}) count nothing, an entity
     * ({@code &amp;}, {@code &#176;}) counts one, everything else one each. One pass, no regex:
     * {@link #rows} runs on every line of every loop, and the two {@code String.replaceAll} calls it
     * used recompiled their patterns each time (2 Nov 2026: 14 ms a loop on Vision: HIVE &amp; Pose's
     * forty lines).
     */
    static int visibleLength(String html) {
        int n = 0;
        int len = html.length();
        int i = 0;
        while (i < len) {
            char c = html.charAt(i);
            if (c == '<') {
                int end = html.indexOf('>', i + 1);
                if (end > i + 1) {
                    i = end + 1;
                    continue;
                }
            } else if (c == '&') {
                int j = i + 1;
                while (j < len && (Character.isLetterOrDigit(html.charAt(j)) || html.charAt(j) == '#')) j++;
                if (j > i + 1 && j < len && html.charAt(j) == ';') {
                    n++;
                    i = j + 1;
                    continue;
                }
            }
            n++;
            i++;
        }
        return n;
    }

    public Page page() {
        return page;
    }

    public void nextPage() {
        page = PAGES[(page.ordinal() + 1) % PAGES.length];
    }

    /** The first line of every page: where you are and how to move on. */
    public void header() {
        used = 0;
        add("<b>" + page.title + "</b>  <small>(" + (page.ordinal() + 1)
                + "/" + PAGES.length + " · Share (Back) for next page)</small>");
    }

    /** A bold heading. */
    public void section(String title) {
        add("<br><b>" + title + "</b>");
    }

    /** A coloured dot, a label and a detail: "● Pinpoint present". */
    public void status(String label, Level level, String detail) {
        add(statusLine(level, label + ": " + detail));
    }

    /** {@link #status}'s line, so a caller can measure it ({@link #rows}) before writing. */
    public static String statusLine(Level level, String text) {
        return "<font color=\"" + level.color + "\">●</font> " + text;
    }

    /**
     * A warning at the top of the Match page: what is wrong, big and amber (never red: red is an
     * alliance), then the one thing to do about it. See {@code InitWarnings}.
     */
    public void alert(String what, String action) {
        add(alertWhat(what));
        add(alertAction(action));
    }

    /** {@link #alert}'s first line, so a caller can measure it ({@link #rows}) before writing. */
    public static String alertWhat(String what) {
        return "<big><b><font color=\"" + Level.WARN.color + "\">" + what + "</font></b></big>";
    }

    /** {@link #alert}'s second line. */
    public static String alertAction(String action) {
        return "<b>→ " + action + "</b>";
    }

    /** A plain line. */
    public void line(String text) {
        add(text);
    }
}
