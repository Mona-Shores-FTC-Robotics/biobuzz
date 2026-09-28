package org.firstinspires.ftc.teamcode.autokit;

import com.pedropathing.ivy.Command;

/**
 * One row of a "first of" card: a true/false test, a description for the log, and the cards to run
 * if it is the first to come true. Built with {@link AutoKit#when}, {@link AutoKit#afterMs} and the
 * other row methods, then {@link #then}.
 */
public final class Row {

    /** Tested once per loop while the card waits, with the seconds since the card started. */
    interface Test {
        boolean passes(double secondsWaited);
    }

    final String description;
    final Test test;
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
