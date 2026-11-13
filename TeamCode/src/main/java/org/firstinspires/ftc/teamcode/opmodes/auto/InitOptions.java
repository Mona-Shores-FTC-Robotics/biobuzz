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
 *   <li><b>The partner wait</b> ({@link AutoOption.Kind#MATCH_SECONDS}): the row is the second
 *       on the match clock at which the robot stops waiting for the partner's TIP, "waiting for
 *       partner's TIP until 9 s", and left / right move that second, {@link #STEP_S} a press.
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
 * <p>Nothing touched, the Auto runs as drawn. The page shows the selected row, then every row's
 * value on one line, amber once anything is changed so the coach sees it before PLAY.
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
     * The menu on the INIT page, green as drawn and amber once any row is changed:
     * <pre>
     * ● ▶ Pickup ◀▶: WALL_FLOWER · ▲▼ row 1 of 8
     *   Pickup WALL_FLOWER · Shot trigger CAMERA · T2 18 s · …
     *   Partner runs L-Clockwise: Pickup GARDEN · Shot trigger CAMERA
     * </pre>
     * The selected row comes first and is the last to go: on a nearly full page the list of every
     * row's value goes, then the partner line, then the selected row is shortened, and on a full
     * page nothing is written. The warnings above come first; the picks still hold and go to the
     * match log (the page is one screen, {@link Display}).
     */
    public void describe(Display display) {
        if (options.length == 0) return;
        boolean any = false;
        for (int i = 0; i < options.length; i++) any |= changed(i);
        Display.Level level = any ? Display.Level.WARN : Display.Level.OK;
        if (rows.length > 0) {
            int i = rows[selected];
            String where = rows.length > 1 ? " · ▲▼ row " + (selected + 1) + " of " + rows.length : "";
            String[] tries = {"▶ " + row(i, true) + where, row(i, false)};
            boolean written = false;
            for (String text : tries) {
                String line = Display.statusLine(level, text);
                if (!display.room(Display.rows(line))) continue;
                display.line(line);
                written = true;
                break;
            }
            if (!written) return;
        }
        String partner = partnerLine();
        if (partner != null) {
            String line = Display.statusLine(level, partner);
            if (display.room(Display.rows(line))) display.line(line);
        }
        if (rows.length > 1) {
            List<String> all = new ArrayList<>();
            for (int i : rows) all.add(brief(i));
            String line = "<small>" + String.join(" · ", all) + "</small>";
            if (display.room(Display.rows(line))) display.line(line);
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
                return keys ? "waiting for partner's TIP ◀▶ until " + fmt(value(i)) + " s"
                        : "TIP ◀▶ until " + fmt(value(i)) + " s";
            default:
                return option.name + arrows + ": " + fmt(value(i)) + " s";
        }
    }

    /** One row for the list of them all: its name and value. */
    private String brief(int i) {
        AutoOption option = options[i];
        String text;
        switch (option.kind) {
            case CHOICE:
                text = option.name + " " + chosen(i);
                break;
            case MATCH_SECONDS:
                text = "partner's TIP until " + fmt(value(i)) + " s";
                break;
            default:
                text = option.name + " " + fmt(value(i)) + " s";
                break;
        }
        return changed(i) ? "<b>" + text + "</b>" : text;
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
                    parts.add(String.format(Locale.US, "%s: waiting for partner's TIP until %s s (drawn %s s)", option.name,
                            fmt(value(i)), fmt(option.drawnSeconds)));
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
