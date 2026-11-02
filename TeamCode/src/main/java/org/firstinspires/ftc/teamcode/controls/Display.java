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

    private final Consumer<String> sink;
    private Page page = Page.MATCH;
    /** Screen rows written since the last {@link #header()}, wrapping included. */
    private int used;

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

    /** Screen rows written since the page's {@link #header()}, wrapping included. */
    public int linesUsed() {
        return used;
    }

    /** Whether {@code rows} more fit on one screen. */
    public boolean room(int rows) {
        return used + rows <= SCREEN_LINES;
    }

    /**
     * The screen rows {@code html} takes: its visible text over the width of one row, at least one,
     * {@code <big>} text wider, one more for each {@code <br>}. An estimate from the text's length,
     * on the safe side.
     */
    public static int rows(String html) {
        int breaks = 0;
        for (int i = html.indexOf("<br>"); i >= 0; i = html.indexOf("<br>", i + 4)) breaks++;
        String text = html.replaceAll("<[^>]+>", "").replaceAll("&[a-zA-Z#0-9]+;", "x");
        double perRow = CHARS_PER_LINE;
        if (html.contains("<big>")) perRow /= BIG_SCALE;
        else if (html.startsWith("<small>")) perRow /= SMALL_SCALE;
        return breaks + Math.max(1, (int) Math.ceil(text.length() / perRow));
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
        add("<font color=\"" + level.color + "\">●</font> " + label + ": " + detail);
    }

    /**
     * A warning at the top of the Match page: what is wrong, big and amber (never red: red is an
     * alliance), then the one thing to do about it. See {@code InitWarnings}.
     */
    public void alert(String what, String action) {
        add("<big><b><font color=\"" + Level.WARN.color + "\">" + what + "</font></b></big>");
        add("<b>→ " + action + "</b>");
    }

    /** A plain line. */
    public void line(String text) {
        add(text);
    }
}
