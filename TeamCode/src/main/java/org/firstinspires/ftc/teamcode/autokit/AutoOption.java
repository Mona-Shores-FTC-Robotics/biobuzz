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

    /** The option that picks what a stop's shot goes by: the camera's sight of the CELL, or the clock. */
    public static final String SHOT_TRIGGER = "Shot trigger";
    /** {@link #SHOT_TRIGGER}'s choices, the camera first: the drawing's. */
    public static final String CAMERA = "CAMERA";
    public static final String TIMER = "TIMER";

    public enum Kind {
        /** One of a few named choices; the first is the drawing's. */
        CHOICE,
        /**
         * The partner wait: a wait's limit on the match clock, which the drive team moves as
         * it is ("waiting for partner's TIP until 9 s").
         */
        MATCH_SECONDS,
        /** A time on the match clock the Auto acts on, seconds since it started, set as it is. */
        TIME,
        /** Nothing to change: what this Auto assumes of the partner's, shown so the two robots' settings match. */
        PARTNER
    }

    /** What the generated code asks for it by: {@link #PARK}, or a wait's label. */
    public final String name;
    public final Kind kind;
    /** {@link Kind#CHOICE}: the choices, the drawing's first. Empty otherwise. */
    public final List<String> choices;
    /** {@link Kind#MATCH_SECONDS}, {@link Kind#TIME}: the drawing's time. NaN otherwise. */
    public final double drawnSeconds;
    /**
     * {@link Kind#PARTNER}: the choice option this follows, or null; {@link #choices} then holds
     * what the partner must be set to for each of that option's choices, in its order.
     */
    public final String follows;

    private AutoOption(String name, Kind kind, List<String> choices, double drawnSeconds) {
        this(name, kind, choices, drawnSeconds, null);
    }

    private AutoOption(String name, Kind kind, List<String> choices, double drawnSeconds, String follows) {
        this.name = name;
        this.kind = kind;
        this.choices = choices;
        this.drawnSeconds = drawnSeconds;
        this.follows = follows;
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

    /** A time on the match clock the Auto acts on ("T2"), {@code drawnSeconds} as drawn. */
    public static AutoOption time(String name, double drawnSeconds) {
        if (!(drawnSeconds >= 0)) throw new IllegalArgumentException(name + ": " + drawnSeconds + " s");
        return new AutoOption(name, Kind.TIME, Collections.<String>emptyList(), drawnSeconds);
    }

    /** {@link #SHOT_TRIGGER}: the camera (the drawing's) or the timer. */
    public static AutoOption shotTrigger() {
        return choice(SHOT_TRIGGER, CAMERA, TIMER);
    }

    /**
     * What this Auto assumes of its partner: the Auto the other robot runs ({@code name},
     * "L-Clockwise"), and, for each choice of this Auto's option {@code follows}, what the partner's
     * same option must be set to. Shown on the INIT page; the drive team changes nothing here.
     * {@code partner("L-Clockwise", "Pickup", "GARDEN", "WALL_FLOWER")}: with our Pickup on its
     * first choice the partner's is GARDEN, on its second WALL_FLOWER.
     */
    public static AutoOption partner(String name, String follows, String... partnerSetting) {
        return new AutoOption(name, Kind.PARTNER, Collections.unmodifiableList(Arrays.asList(partnerSetting.clone())),
                Double.NaN, follows);
    }

    @Override
    public String toString() {
        if (kind == Kind.PARTNER) return "partner " + name + (follows == null ? "" : " by " + follows + " " + choices);
        return kind == Kind.CHOICE ? name + " " + choices : name + " at " + drawnSeconds + " s";
    }
}
