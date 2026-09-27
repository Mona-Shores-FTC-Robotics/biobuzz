package org.firstinspires.ftc.teamcode.controls;

import org.firstinspires.ftc.robotcore.external.Telemetry;

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

    private final Telemetry telemetry;
    private Page page = Page.MATCH;

    public Display(Telemetry telemetry) {
        this.telemetry = telemetry;
        telemetry.setDisplayFormat(Telemetry.DisplayFormat.HTML);
    }

    public Page page() {
        return page;
    }

    public void nextPage() {
        page = PAGES[(page.ordinal() + 1) % PAGES.length];
    }

    /** The first line of every page: where you are and how to move on. */
    public void header() {
        telemetry.addLine("<b>" + page.title + "</b>  <small>(" + (page.ordinal() + 1)
                + "/" + PAGES.length + " · Back/Share for next page)</small>");
    }

    /** A bold heading. */
    public void section(String title) {
        telemetry.addLine("<br><b>" + title + "</b>");
    }

    /** A coloured dot, a label and a detail: "● Pinpoint present". */
    public void status(String label, Level level, String detail) {
        telemetry.addLine("<font color=\"" + level.color + "\">●</font> " + label + ": " + detail);
    }

    /** A plain line. */
    public void line(String text) {
        telemetry.addLine(text);
    }
}
