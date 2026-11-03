package org.firstinspires.ftc.teamcode.autokit;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Something about an Auto the drive team may change during INIT, at the field, without a new
 * export: which PARK spot it ends on, or when a wait gives up on the match clock. A generated Auto
 * lists its options in {@code OPTIONS}; the OpMode shows them on the INIT page and the generated
 * {@code build} reads the drive team's picks through {@link AutoKit#pick} and
 * {@link AutoKit#seconds}, falling back to the drawing's own value when nothing was picked.
 *
 * <p>The Auto Builder writes these. The drawing's value is always the default: an Auto nobody
 * touches in INIT runs exactly as drawn.
 */
public final class AutoOption {

    /** The option that picks the PARK spot (the endgame guard's park and the Auto's last drive). */
    public static final String PARK = "Park";

    public enum Kind {
        /** One of a few named choices; the first is the drawing's. */
        CHOICE,
        /** A time on the match clock, seconds since the Auto started. */
        MATCH_SECONDS
    }

    /** What the generated code asks for it by: {@link #PARK}, or a wait's label. */
    public final String name;
    public final Kind kind;
    /** {@link Kind#CHOICE}: the choices, the drawing's first. Empty otherwise. */
    public final List<String> choices;
    /** {@link Kind#MATCH_SECONDS}: the drawing's time. NaN otherwise. */
    public final double drawnSeconds;

    private AutoOption(String name, Kind kind, List<String> choices, double drawnSeconds) {
        this.name = name;
        this.kind = kind;
        this.choices = choices;
        this.drawnSeconds = drawnSeconds;
    }

    /** The PARK spot: the drawing's first, then the others the drive team may pick. */
    public static AutoOption park(String drawn, String... others) {
        String[] all = new String[others.length + 1];
        all[0] = drawn;
        System.arraycopy(others, 0, all, 1, others.length);
        return choice(PARK, all);
    }

    /** One of {@code choices}; the first is the drawing's. */
    public static AutoOption choice(String name, String... choices) {
        if (choices.length < 2) throw new IllegalArgumentException(name + " needs at least two choices");
        return new AutoOption(name, Kind.CHOICE, Collections.unmodifiableList(Arrays.asList(choices.clone())), Double.NaN);
    }

    /** A wait's time limit on the match clock, {@code drawnSeconds} as drawn. */
    public static AutoOption matchSeconds(String name, double drawnSeconds) {
        if (!(drawnSeconds >= 0)) throw new IllegalArgumentException(name + ": " + drawnSeconds + " s");
        return new AutoOption(name, Kind.MATCH_SECONDS, Collections.<String>emptyList(), drawnSeconds);
    }

    @Override
    public String toString() {
        return kind == Kind.CHOICE ? name + " " + choices : name + " at " + drawnSeconds + " s";
    }
}
