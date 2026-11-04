package org.firstinspires.ftc.teamcode.opmodes.auto;

import org.firstinspires.ftc.teamcode.autokit.AutoKit;
import org.firstinspires.ftc.teamcode.autokit.AutoOption;
import org.firstinspires.ftc.teamcode.controls.Display;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The drive team's picks for an Auto's {@link AutoOption}s, made during INIT at the field: the
 * partner says where they park and when their last preload leaves while the robots queue, and
 * gamepad 1's D-pad changes it.
 *
 * <ul>
 *   <li><b>D-pad left / right</b>: the PARK spot. A choice named {@code LEFT_…} or {@code RIGHT_…}
 *       goes with that key (sides as the drive team sees them; the robot turns the spots for
 *       BLUE, so the names hold for both alliances); other names step through the list.</li>
 *   <li><b>D-pad up / down</b>: the partner's last shot, their answer to "when does your last
 *       preload leave?", {@link #STEP_S} a press. The wait gives up
 *       {@link AutoRegistration#PARTNER_TIP_LAG_S} after it, on the match clock, and the page shows
 *       both numbers, never only the sum: "partner's last shot 8 s, waiting until 12 s". One such
 *       wait per Auto (mentor, 3 Nov 2026).</li>
 * </ul>
 *
 * <p>Nothing touched, the Auto runs as drawn. Each option takes one line of the INIT page, amber
 * once changed so the coach sees it before PLAY. {@link #lock()} at PLAY freezes them, as the
 * alliance is. Gamepad 1 only: gamepad 2's D-pad up is the turret's zero.
 */
public final class InitOptions implements AutoKit.Picks {

    /** One press of up or down. */
    public static final double STEP_S = 1.0;
    /** The latest a partner's last shot may be set to: past this the Auto has nothing left to wait for. */
    public static final double LATEST_LAST_SHOT_S = 25.0;

    private final AutoOption[] options;
    private final int[] choice;
    private final double[] seconds;
    private boolean locked;
    private boolean wasLeft;
    private boolean wasRight;
    private boolean wasUp;
    private boolean wasDown;

    public InitOptions(AutoOption... options) {
        this.options = options.clone();
        int timed = 0;
        int parks = 0;
        for (AutoOption option : options) {
            if (option.kind == AutoOption.Kind.MATCH_SECONDS) timed++;
            if (option.kind == AutoOption.Kind.CHOICE) parks++;
        }
        // One per D-pad axis: two of either would need a way to say which a press is for.
        if (timed > 1) throw new IllegalArgumentException("only one wait may be changed in INIT, not " + timed);
        if (parks > 1) throw new IllegalArgumentException("only one choice may be changed in INIT, not " + parks);
        this.choice = new int[options.length];
        this.seconds = new double[options.length];
        for (int i = 0; i < options.length; i++) {
            choice[i] = -1;
            seconds[i] = Double.NaN;
        }
    }

    public boolean isEmpty() {
        return options.length == 0;
    }

    /** Called every INIT loop with gamepad 1's D-pad; acts on each key's press, not its hold. */
    public void update(boolean left, boolean right, boolean up, boolean down) {
        if (!locked) {
            if (left && !wasLeft) side("LEFT_", -1);
            if (right && !wasRight) side("RIGHT_", +1);
            if (up && !wasUp) lastShot(+STEP_S);
            if (down && !wasDown) lastShot(-STEP_S);
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

    private void side(String prefix, int step) {
        for (int i = 0; i < options.length; i++) {
            AutoOption option = options[i];
            if (option.kind != AutoOption.Kind.CHOICE) continue;
            int named = -1;
            for (int c = 0; c < option.choices.size(); c++) {
                if (option.choices.get(c).startsWith(prefix)) named = c;
            }
            int n = option.choices.size();
            int now = choice[i] < 0 ? 0 : choice[i];
            int next = named >= 0 ? named : ((now + step) % n + n) % n;
            choice[i] = next == 0 ? -1 : next;
        }
    }

    private void lastShot(double step) {
        for (int i = 0; i < options.length; i++) {
            AutoOption option = options[i];
            if (option.kind != AutoOption.Kind.MATCH_SECONDS) continue;
            double shot = Math.max(0, Math.min(LATEST_LAST_SHOT_S, partnerLastShot(i) + step));
            double limit = AutoRegistration.partnerWaitUntilS(shot);
            seconds[i] = Math.abs(limit - option.drawnSeconds) < 1e-9 ? Double.NaN : limit;
        }
    }

    /**
     * The partner's last shot the option's limit stands for: the limit less
     * {@link AutoRegistration#PARTNER_TIP_LAG_S}. As drawn, the drawing's limit less the lag.
     */
    private double partnerLastShot(int i) {
        return Math.max(0, limit(i) - AutoRegistration.PARTNER_TIP_LAG_S);
    }

    private double limit(int i) {
        return Double.isNaN(seconds[i]) ? options[i].drawnSeconds : seconds[i];
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
     * The options on one line of the INIT page, green as drawn and amber once any is changed:
     * "● Park ◀▶: RIGHT_PARK · partner's last shot ▲▼ 8 s, waiting until 12 s". Shorter when the page is
     * nearly full ("shot ▲▼ 8+4=12 s"), and left off when it is full: the warnings above it come first, and the picks
     * still hold and go to the match log (the page is one screen, {@link Display}).
     */
    public void describe(Display display) {
        if (options.length == 0) return;
        String park = null;
        String tipLong = null;
        String tipShort = null;
        boolean changed = false;
        for (int i = 0; i < options.length; i++) {
            AutoOption option = options[i];
            if (option.kind == AutoOption.Kind.CHOICE) {
                changed |= choice[i] >= 0;
                park = option.name + " ◀▶: " + option.choices.get(choice[i] < 0 ? 0 : choice[i]);
            } else {
                changed |= !Double.isNaN(seconds[i]);
                // Short, for a nearly full page, and still all three numbers: the last shot, the lag, the wait.
                tipShort = "shot ▲▼ " + fmt(partnerLastShot(i)) + "+" + fmt(AutoRegistration.PARTNER_TIP_LAG_S) + "=" + fmt(limit(i)) + " s";
                tipLong = "partner's last shot ▲▼ " + fmt(partnerLastShot(i)) + " s, waiting until " + fmt(limit(i)) + " s";
            }
        }
        Display.Level level = changed ? Display.Level.WARN : Display.Level.OK;
        String[] tries = {join(park, tipLong), join(park, tipShort)};
        for (String text : tries) {
            String line = Display.statusLine(level, text);
            if (display.room(Display.rows(line))) {
                display.line(line);
                return;
            }
        }
    }

    private static String join(String a, String b) {
        if (a == null) return b;
        return b == null ? a : a + " · " + b;
    }

    /** For the match log: every option, as picked and as drawn. Empty with none. */
    public String summary() {
        List<String> parts = new ArrayList<>();
        for (int i = 0; i < options.length; i++) {
            AutoOption option = options[i];
            if (option.kind == AutoOption.Kind.CHOICE) {
                parts.add(String.format(Locale.US, "%s=%s (drawn %s)", option.name,
                        option.choices.get(choice[i] < 0 ? 0 : choice[i]), option.choices.get(0)));
            } else {
                parts.add(String.format(Locale.US, "%s: partner's last shot %s s, until %s s (drawn %s s)", option.name,
                        fmt(partnerLastShot(i)), fmt(limit(i)), fmt(option.drawnSeconds)));
            }
        }
        return String.join("; ", parts);
    }

    private static String fmt(double s) {
        return Math.abs(s - Math.rint(s)) < 1e-9 ? String.format(Locale.US, "%.0f", s) : String.format(Locale.US, "%.1f", s);
    }
}
