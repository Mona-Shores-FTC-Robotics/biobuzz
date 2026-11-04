package org.firstinspires.ftc.teamcode.autokit;

import com.pedropathing.ivy.Command;

/**
 * One row of a "first of" card: a true/false test, a description for the log, and the cards to run
 * if it is the first to come true. Built with {@link AutoKit#when}, {@link AutoKit#afterMs},
 * {@link AutoKit#atMatchSeconds} and the other row methods, then {@link #then}.
 */
public final class Row {

    /** Tested once per loop while the card waits, with the seconds since the card started. */
    interface Test {
        boolean passes(double secondsWaited);
    }

    final String description;
    final Test test;
    /** A time limit ({@link AutoKit#afterMs}, {@link AutoKit#atMatchSeconds}): it waits while the registry holds time limits. */
    boolean timeLimit;
    /**
     * For a time limit: the longest the wait can last if it starts at {@code startsAt} on the match
     * clock, so the endgame guard can plan past it ({@link AutoKit#guarded}). Unused otherwise.
     */
    java.util.function.DoubleUnaryOperator longestFrom = startsAt -> Double.NaN;
    /** Run when the card starts waiting, before the first test. */
    Runnable start = () -> { };
    Command[] cards = new Command[0];

    Row(String description, Test test) {
        this.description = description;
        this.test = test;
    }

    /** The cards to run, in order, if this row fires first. None means carry straight on. */
    public Row then(Command... cards) {
        this.cards = cards;
        return this;
    }
}
