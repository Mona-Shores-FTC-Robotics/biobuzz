package org.firstinspires.ftc.teamcode.autokit;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.CommandBuilder;
import com.pedropathing.ivy.behaviors.EndCondition;
import com.pedropathing.ivy.commands.Commands;
import com.pedropathing.ivy.groups.Groups;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;

import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

/**
 * The cards an Auto built in the Auto Builder is made of. A generated Auto class calls these, in
 * the same order as the card list in the editor, and gets back one Ivy {@link Command}.
 *
 * <p>There are three kinds of card: a command, a path, and the one branching block,
 * {@link #firstOf}: wait for a trigger, at most some time, then run the cards of whichever came
 * first. A plain "Wait for" is a {@code firstOf} whose rows have no cards.
 *
 * <p>Nothing here reads hardware or blocks. Triggers are read once per loop, only while a card
 * that uses them waits; paths are built at init.
 *
 * <p>Depends only on Ivy and Pedro — not on {@code Robot} or the FTC SDK — so it can move to its own
 * library later, and so the tests run on a laptop.
 */
public final class AutoKit {

    /** Length of the Autonomous period. */
    public static final double AUTO_LENGTH_S = 30.0;

    /** Extra time the endgame guard leaves on top of a park path's drive time. */
    public static final double GUARD_MARGIN_S = 0.5;

    private final AutoDrive drive;
    private final AutoRegistry registry;
    private final DoubleSupplier secondsSinceStart;
    private Consumer<String> trace = line -> { };

    public AutoKit(AutoDrive drive, AutoRegistry registry, DoubleSupplier secondsSinceStart) {
        this.drive = drive;
        this.registry = registry;
        this.secondsSinceStart = secondsSinceStart;
    }

    /** Where one line per card start, row fired and guard decision goes (telemetry, a log file). */
    public AutoKit trace(Consumer<String> sink) {
        this.trace = sink == null ? line -> { } : sink;
        return this;
    }

    /** Seconds left in the Autonomous period. */
    public double timeLeft() {
        return AUTO_LENGTH_S - secondsSinceStart.getAsDouble();
    }

    // ------------------------------------------------------------------ cards

    /** Runs {@code cards} one after another. */
    public Command sequence(Command... cards) {
        return cards.length == 0 ? nothing() : Groups.sequential(cards);
    }

    /** How long a command step may run when the Auto sets no timeout. */
    public static final double DEFAULT_TIMEOUT_S = 5.0;

    /** The registered command {@code name}, cut off after {@link #DEFAULT_TIMEOUT_S}. */
    public Command command(String name) {
        return command(name, DEFAULT_TIMEOUT_S);
    }

    /**
     * The registered command {@code name}: runs until it finishes or {@code timeoutS} has passed,
     * whichever is first, so a command that never finishes cannot stall the Auto. A timeout is
     * traced, since it usually means a mechanism did not do its job.
     */
    public Command command(String name, double timeoutS) {
        final Command inner = registry.command(name);
        final double[] startedAt = new double[1];
        final boolean[] timedOut = new boolean[1];
        return new CommandBuilder()
                .requiring(inner.requirements())
                .setStart(() -> {
                    trace.accept("command " + name);
                    startedAt[0] = secondsSinceStart.getAsDouble();
                    timedOut[0] = false;
                    inner.start();
                })
                .setExecute(inner::execute)
                .setDone(() -> {
                    if (inner.done()) return true;
                    timedOut[0] = secondsSinceStart.getAsDouble() - startedAt[0] >= timeoutS;
                    return timedOut[0];
                })
                .setEnd(end -> {
                    if (timedOut[0]) {
                        trace.accept(String.format(Locale.US, "command %s timed out after %.1f s", name, timeoutS));
                        inner.end(EndCondition.INTERRUPTED);
                    } else {
                        inner.end(end);
                    }
                });
    }

    /** Drives {@code path} to its end. */
    public Command path(String label, Path path) {
        return traced("path " + label, follow(path));
    }

    /**
     * Waits for the first of {@code rows} to come true, then runs that row's cards. Rows are checked
     * in order each loop, so when two come true in the same loop the earlier one wins.
     */
    public Command firstOf(String label, Row... rows) {
        return firstOf(label, null, rows);
    }

    /**
     * {@link #firstOf(String, Row...)} while {@code alongside} runs: "wait for Tip while LaunchAll".
     * The command starts with the wait. When a row fires it is stopped if still running (once the
     * HIVE has tipped, the rest of the launch is wasted); if it finishes first, the wait goes on.
     * Null runs nothing alongside.
     */
    public Command firstOf(String label, Command alongside, Row... rows) {
        if (rows.length == 0) throw new IllegalArgumentException(label + " has no rows");
        final Command[] branches = new Command[rows.length];
        for (int i = 0; i < rows.length; i++) branches[i] = sequence(rows[i].cards);
        final double[] startedAt = new double[1];
        final Command[] chosen = new Command[1];
        final boolean[] alongsideRunning = new boolean[1];
        final CommandBuilder card = new CommandBuilder();
        card.setStart(() -> {
            startedAt[0] = secondsSinceStart.getAsDouble();
            chosen[0] = null;
            trace.accept("wait " + label);
            for (Row row : rows) row.start.run();
            alongsideRunning[0] = alongside != null;
            if (alongside != null) alongside.start();
        });
        card.setExecute(() -> {
            if (chosen[0] == null) {
                if (alongsideRunning[0]) {
                    alongside.execute();
                    if (alongside.done()) {
                        alongside.end(EndCondition.NATURALLY);
                        alongsideRunning[0] = false;
                    }
                }
                double waited = secondsSinceStart.getAsDouble() - startedAt[0];
                for (int i = 0; i < rows.length; i++) {
                    if (rows[i].test.passes(waited)) {
                        trace.accept(String.format(Locale.US, "%s: %s after %.2f s", label, rows[i].description, waited));
                        if (alongsideRunning[0]) {
                            alongside.end(EndCondition.INTERRUPTED);
                            alongsideRunning[0] = false;
                        }
                        chosen[0] = branches[i];
                        chosen[0].start();
                        break;
                    }
                }
                return;
            }
            if (!chosen[0].done()) chosen[0].execute();
        });
        card.setDone(() -> chosen[0] != null && chosen[0].done());
        card.setEnd(end -> {
            if (alongsideRunning[0]) {
                alongside.end(end);
                alongsideRunning[0] = false;
            }
            if (chosen[0] != null) chosen[0].end(end);
        });
        return card;
    }

    /**
     * The endgame guard. Runs {@code cards} in order, but before each one checks that the time left
     * covers {@code parkSeconds} plus {@link #GUARD_MARGIN_S}; if not, it stops there and drives
     * {@code parkPath} instead. Checks happen only between cards, when the robot is at a known point.
     * The last card is assumed to be the park itself and is never skipped.
     */
    public Command guarded(String label, Path parkPath, double parkSeconds, Command... cards) {
        final Command park = traced("path " + label + " park", follow(parkPath));
        final int[] index = new int[1];
        final Command[] current = new Command[1];
        final CommandBuilder card = new CommandBuilder();
        card.setStart(() -> {
            index[0] = 0;
            current[0] = null;
            advance(label, parkSeconds, cards, index, current, park);
        });
        card.setExecute(() -> {
            if (current[0] == null) return;
            if (current[0].done()) {
                current[0].end(EndCondition.NATURALLY);
                if (current[0] == park) {
                    current[0] = null;
                    index[0] = cards.length;
                    return;
                }
                index[0]++;
                advance(label, parkSeconds, cards, index, current, park);
                return;
            }
            current[0].execute();
        });
        card.setDone(() -> current[0] == null);
        card.setEnd(end -> {
            if (current[0] != null) current[0].end(end);
        });
        return card;
    }

    private void advance(String label, double parkSeconds, Command[] cards, int[] index, Command[] current,
                         Command park) {
        if (index[0] >= cards.length) {
            current[0] = null;
            return;
        }
        boolean last = index[0] == cards.length - 1;
        double left = timeLeft();
        if (!last && left < parkSeconds + GUARD_MARGIN_S) {
            trace.accept(String.format(Locale.US, "%s: %.1f s left, park needs %.1f s: parking now",
                    label, left, parkSeconds + GUARD_MARGIN_S));
            current[0] = park;
        } else {
            current[0] = cards[index[0]];
        }
        current[0].start();
    }

    // ------------------------------------------------------------------- rows

    /** True when the registered trigger is; watched from the moment the card starts waiting. */
    public Row when(String trigger) {
        registry.requireTrigger(trigger);
        final BooleanSupplier[] check = new BooleanSupplier[1];
        Row row = new Row(trigger, waited -> check[0].getAsBoolean());
        // Taken when the wait starts, so a "since" trigger means this wait.
        row.start = () -> check[0] = registry.watch(trigger);
        return row;
    }

    /** True once {@code ms} have passed since the card started. */
    public Row afterMs(double ms) {
        return new Row(String.format(Locale.US, "%.0f ms passed", ms), waited -> waited * 1000 >= ms);
    }

    // ---------------------------------------------------------------- helpers

    /**
     * A card that does nothing and is finished at once. Not {@link Command#NOOP}: that is a bare
     * builder whose {@code done()} is always false, so it would never let a sequence move on.
     */
    private static Command nothing() {
        return Commands.instant(() -> { });
    }

    private Command follow(Path path) {
        return new CommandBuilder()
                .setStart(() -> drive.follow(path))
                .setDone(drive::pathDone);
    }

    /**
     * {@code command}, with {@code line} traced as it starts. A wrapper rather than a sequence with
     * an instant command in front, because Ivy's sequence moves on one step per loop and every card
     * would start a loop late.
     */
    private Command traced(String line, Command command) {
        return new CommandBuilder()
                .requiring(command.requirements())
                .setStart(() -> {
                    trace.accept(line);
                    command.start();
                })
                .setExecute(command::execute)
                .setDone(command::done)
                .setEnd(command::end);
    }
}
