package org.firstinspires.ftc.teamcode.opmodes.auto;

import org.firstinspires.ftc.teamcode.autokit.AutoKit;
import org.firstinspires.ftc.teamcode.autokit.AutoOption;
import org.firstinspires.ftc.teamcode.controls.Display;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The drive team's picks for an Auto's {@link AutoOption}s, made during INIT at the field, as one
 * menu on gamepad 1's D-pad: <b>up / down selects a row, left / right changes it</b>. Every Auto's
 * options are rows of it, in the order the Auto lists them:
 *
 * <ul>
 *   <li><b>A choice</b> (the PARK spot, a paired Auto's pickup, the shot trigger): left / right
 *       steps through the choices. A choice named {@code LEFT_…} or {@code RIGHT_…} goes with that
 *       key (sides as the drive team sees them; the robot turns the spots for BLUE, so the names
 *       hold for both alliances).</li>
 *   <li><b>The wait for a TIP</b> ({@link AutoOption.Kind#MATCH_SECONDS}): the row is the second
 *       on the match clock at which the robot stops waiting for a TIP, named as the drawing
 *       names it, "waiting for TIP 1 until 9 s" (mentor, 13 Nov 2026: the TIP's number says more
 *       than "partner's TIP", which is what an Auto drawn before then still reads), and left /
 *       right move that second, {@link #STEP_S} a press.
 *       One number, because a partner rarely knows when its last preload leaves (mentor,
 *       12 Nov 2026). A partner that shoots later than at the start: set it about
 *       {@link AutoRegistration#PARTNER_TIP_LAG_S} after the second they say; 0 does not wait.</li>
 *   <li><b>A time</b> ({@link AutoOption.Kind#TIME}): a match-clock time the Auto acts on, set as
 *       it is, {@link #TIME_STEP_S} a press.</li>
 * </ul>
 *
 * <p>An Auto that runs beside a partner's Auto also says what it assumes of the partner
 * ({@link AutoOption.Kind#PARTNER}): which Auto, and what the partner's own menu must show for the
 * settings the two share. That line follows this robot's picks, so the two drive teams can read
 * their pages to each other.
 *
 * <p>Nothing touched, the Auto runs as drawn. The page lists every row, one a line, with a pointer
 * at the selected one, amber once anything is changed so the coach sees it before PLAY.
 * {@link #lock()} at PLAY freezes them, as the alliance is. Gamepad 1 only: gamepad 2's D-pad up
 * is the turret's zero.
 */
public final class InitOptions implements AutoKit.Picks {

    /** One press on the partner wait. */
    public static final double STEP_S = 1.0;
    /** The latest the partner wait may be set to: past this the Auto has nothing left to wait for. */
    public static final double LATEST_WAIT_S = 28.0;
    /** One press on a time. */
    public static final double TIME_STEP_S = 0.5;

    private final AutoOption[] options;
    /** The options a row stands for, in order: every one but the partner notes. */
    private final int[] rows;
    private final int[] choice;
    private final double[] seconds;
    private int selected;
    private boolean locked;
    private boolean wasLeft;
    private boolean wasRight;
    private boolean wasUp;
    private boolean wasDown;

    public InitOptions(AutoOption... options) {
        this.options = options.clone();
        int n = 0;
        for (AutoOption option : options) if (option.kind != AutoOption.Kind.PARTNER) n++;
        this.rows = new int[n];
        this.choice = new int[options.length];
        this.seconds = new double[options.length];
        n = 0;
        for (int i = 0; i < options.length; i++) {
            choice[i] = -1;
            seconds[i] = Double.NaN;
            if (options[i].kind != AutoOption.Kind.PARTNER) rows[n++] = i;
            for (int j = 0; j < i; j++) {
                if (options[j].name.equals(options[i].name)) {
                    throw new IllegalArgumentException("two options are named \"" + options[i].name + "\"");
                }
            }
        }
    }

    public boolean isEmpty() {
        return options.length == 0;
    }

    /** Called every INIT loop with gamepad 1's D-pad; acts on each key's press, not its hold. */
    public void update(boolean left, boolean right, boolean up, boolean down) {
        if (!locked && rows.length > 0) {
            if (up && !wasUp) selected = (selected + rows.length - 1) % rows.length;
            if (down && !wasDown) selected = (selected + 1) % rows.length;
            if (left && !wasLeft) change(rows[selected], -1);
            if (right && !wasRight) change(rows[selected], +1);
        }
        wasLeft = left;
        wasRight = right;
        wasUp = up;
        wasDown = down;
    }

    /** PLAY: the picks hold for the match. */
    public void lock() {
        locked = true;
    }

    /** The name of the row the D-pad's left and right change now; null with no rows. */
    public String selected() {
        return rows.length == 0 ? null : options[rows[selected]].name;
    }

    private void change(int i, int step) {
        AutoOption option = options[i];
        switch (option.kind) {
            case CHOICE: {
                int named = -1;
                String prefix = step < 0 ? "LEFT_" : "RIGHT_";
                for (int c = 0; c < option.choices.size(); c++) {
                    if (option.choices.get(c).startsWith(prefix)) named = c;
                }
                int n = option.choices.size();
                int now = choice[i] < 0 ? 0 : choice[i];
                int next = named >= 0 ? named : ((now + step) % n + n) % n;
                choice[i] = next == 0 ? -1 : next;
                break;
            }
            case MATCH_SECONDS:
                set(i, Math.max(0, Math.min(LATEST_WAIT_S, value(i) + step * STEP_S)));
                break;
            case TIME:
                set(i, Math.max(0, Math.min(AutoKit.AUTO_LENGTH_S, value(i) + step * TIME_STEP_S)));
                break;
            default:
                break;
        }
    }

    /** {@code value} for option {@code i}, or as drawn when it is the drawing's. */
    private void set(int i, double value) {
        seconds[i] = Math.abs(value - options[i].drawnSeconds) < 1e-9 ? Double.NaN : value;
    }

    private double value(int i) {
        return Double.isNaN(seconds[i]) ? options[i].drawnSeconds : seconds[i];
    }

    private boolean changed(int i) {
        return choice[i] >= 0 || !Double.isNaN(seconds[i]);
    }

    private String chosen(int i) {
        return options[i].choices.get(choice[i] < 0 ? 0 : choice[i]);
    }

    @Override
    public int choice(String option) {
        for (int i = 0; i < options.length; i++) {
            if (options[i].name.equals(option)) return choice[i];
        }
        return -1;
    }

    @Override
    public double seconds(String option) {
        for (int i = 0; i < options.length; i++) {
            if (options[i].name.equals(option)) return seconds[i];
        }
        return Double.NaN;
    }

    /**
     * The menu on the INIT page: every row on its own line, with a pointer at the selected one,
     * which also shows the arrows that change it. The dot is green as drawn, amber once any row is
     * changed; a changed row is bold. No title line: the page is one screen, and a line more would
     * push the start map off it.
     * <pre>
     * ● ▶ waiting for TIP 2 ◀▶ until 23.2 s
     *      Pickup: WALL_FLOWER
     *      Priority: THREE_TIP
     * </pre>
     * The whole list is written only if the page has room for it. On a nearly full page the menu
     * is its selected row alone, then that row shortened, and on a full page nothing is written.
     * The warnings above come first; the picks still hold and go to the match log (the page is one
     * screen, {@link Display}).
     */
    public void describe(Display display) {
        if (options.length == 0 || rows.length == 0) return;
        boolean any = false;
        for (int i = 0; i < options.length; i++) any |= changed(i);
        Display.Level level = any ? Display.Level.WARN : Display.Level.OK;

        List<String> menu = new ArrayList<>();
        for (int r = 0; r < rows.length; r++) {
            int i = rows[r];
            String text = r == selected ? "▶ " + row(i, true) : INDENT + plain(i);
            if (changed(i)) text = "<b>" + text + "</b>";
            menu.add(r == 0 ? Display.statusLine(level, text) : INDENT + text);
        }
        String partner = partnerLine();
        if (partner != null) menu.add("<small>" + partner + "</small>");
        double needs = 0;
        for (String line : menu) needs += Display.rows(line);
        if (display.room(needs)) {
            for (String line : menu) display.line(line);
            return;
        }
        // No room for the list: the selected row alone, whole and then short.
        int i = rows[selected];
        for (String text : new String[] {"▶ " + row(i, true), row(i, false)}) {
            String line = Display.statusLine(level, text);
            if (!display.room(Display.rows(line))) continue;
            display.line(line);
            return;
        }
    }

    /** Lines an unselected row up under the selected one's pointer: no-break spaces, which HTML keeps. */
    private static final String INDENT = "\u00A0\u00A0\u00A0";

    /** An unselected row: its name and value, with no arrows. */
    private String plain(int i) {
        AutoOption option = options[i];
        switch (option.kind) {
            case CHOICE:
                return option.name + ": " + chosen(i);
            case MATCH_SECONDS:
                return "waiting for " + option.tipName() + " until " + fmt(value(i)) + " s";
            default:
                return option.name + ": " + fmt(value(i)) + " s";
        }
    }

    /** One row in words: with {@code keys}, the arrows that change it. */
    private String row(int i, boolean keys) {
        AutoOption option = options[i];
        String arrows = keys ? " ◀▶" : "";
        switch (option.kind) {
            case CHOICE:
                return option.name + arrows + ": " + chosen(i);
            case MATCH_SECONDS:
                // The one number the drive team moves; short on a nearly full page.
                return keys ? "waiting for " + option.tipName() + " ◀▶ until " + fmt(value(i)) + " s"
                        : (option.tip > 0 ? option.tipName() : "TIP") + " ◀▶ until " + fmt(value(i)) + " s";
            default:
                return option.name + arrows + ": " + fmt(value(i)) + " s";
        }
    }

    /**
     * What the partner's robot must be running and set to, for this robot's picks: "Partner runs
     * L-Clockwise: Pickup GARDEN · Shot trigger TIMER". The setting a partner note follows comes
     * from the note; every other choice the two Autos share by name (the shot trigger) must simply
     * be the same. Null when the Auto says nothing of a partner.
     */
    private String partnerLine() {
        StringBuilder out = null;
        for (AutoOption note : options) {
            if (note.kind != AutoOption.Kind.PARTNER) continue;
            if (out == null) out = new StringBuilder("Partner runs " + note.name);
            List<String> settings = new ArrayList<>();
            for (int i = 0; i < options.length; i++) {
                AutoOption option = options[i];
                if (option.kind != AutoOption.Kind.CHOICE) continue;
                int picked = choice[i] < 0 ? 0 : choice[i];
                if (option.name.equals(note.follows)) {
                    if (picked < note.choices.size()) settings.add(option.name + " " + note.choices.get(picked));
                } else if (option.name.equals(AutoOption.SHOT_TRIGGER)) {
                    settings.add(option.name + " " + chosen(i));
                }
            }
            if (!settings.isEmpty()) out.append(": ").append(String.join(" · ", settings));
        }
        return out == null ? null : out.toString();
    }

    /** For the match log: every option, as picked and as drawn. Empty with none. */
    public String summary() {
        List<String> parts = new ArrayList<>();
        for (int i = 0; i < options.length; i++) {
            AutoOption option = options[i];
            switch (option.kind) {
                case CHOICE:
                    parts.add(String.format(Locale.US, "%s=%s (drawn %s)", option.name, chosen(i), option.choices.get(0)));
                    break;
                case MATCH_SECONDS:
                    parts.add(String.format(Locale.US, "%s: waiting for %s until %s s (drawn %s s)", option.name,
                            option.tipName(), fmt(value(i)), fmt(option.drawnSeconds)));
                    break;
                case TIME:
                    parts.add(String.format(Locale.US, "%s=%s s (drawn %s s)", option.name, fmt(value(i)), fmt(option.drawnSeconds)));
                    break;
                default:
                    break;
            }
        }
        String partner = partnerLine();
        if (partner != null) parts.add(partner);
        return String.join("; ", parts);
    }

    private static String fmt(double s) {
        return Math.abs(s - Math.rint(s)) < 1e-9 ? String.format(Locale.US, "%.0f", s) : String.format(Locale.US, "%.1f", s);
    }
}
