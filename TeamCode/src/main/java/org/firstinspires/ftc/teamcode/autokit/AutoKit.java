package org.firstinspires.ftc.teamcode.autokit;

import com.pedropathing.api.Paths;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.CommandBuilder;
import com.pedropathing.ivy.behaviors.EndCondition;
import com.pedropathing.ivy.commands.Commands;
import com.pedropathing.ivy.groups.Groups;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

/**
 * The cards an Auto built in the Auto Builder is made of. A generated Auto class calls these, in
 * the same order as the card list in the editor, and gets back one Ivy {@link Command}.
 *
 * <p>Every card that waits is the same thing underneath — {@link #firstOf}: wait for the first of a
 * few true/false rows, then run that row's cards. A "Wait for" is a {@code firstOf} whose rows have
 * no cards; an "if" is one with an {@link #otherwise()} row.
 *
 * <p>Nothing here reads hardware or blocks. Triggers are read once per loop, only while a card
 * that uses them waits; paths are built at init, except a {@link #goTo} or routine exit line, which
 * is built once when its card starts.
 *
 * <p>Depends only on Ivy and Pedro — not on {@code Robot} or the FTC SDK — so it can move to its own
 * library later, and so the tests run on a laptop.
 */
public final class AutoKit {

    /** Length of the Autonomous period. */
    public static final double AUTO_LENGTH_S = 30.0;

    /** Extra time the endgame guard leaves on top of a park path's drive time. */
    public static final double GUARD_MARGIN_S = 0.5;

    /** Half the width of the robot, in inches: how close its centre may come to a keep-out. */
    public static final double ROBOT_HALF_WIDTH_IN = 9.0;

    /** How a {@link #together} card ends. */
    public enum Ends { ALL, FIRST }

    private final AutoDrive drive;
    private final AutoRegistry registry;
    private final DoubleSupplier secondsSinceStart;
    private final List<Pose[]> keepOuts = new ArrayList<>();
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

    /** A zone no straight line built at run time may cross ({@link #goTo}, routine exits). */
    public AutoKit keepOut(Pose... corners) {
        if (corners.length < 3) throw new IllegalArgumentException("A keep-out needs at least 3 corners");
        keepOuts.add(corners.clone());
        return this;
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
        return path(label, path, new String[0]);
    }

    /**
     * Drives {@code path}, running {@code whileActions} alongside it and each event's action once
     * the robot is that far along. Everything alongside stops when the path ends, including events
     * the robot never reached.
     */
    public Command path(String label, Path path, String[] whileActions, Marker... events) {
        List<Command> alongside = new ArrayList<>();
        for (String name : whileActions) alongside.add(registry.command(name));
        for (Marker event : events) {
            alongside.add(whenReached(event.fraction,
                    "event " + event.action + " at " + percent(event.fraction) + " of " + label,
                    registry.command(event.action)));
        }
        Command follow = traced("path " + label, follow(path));
        return alongside.isEmpty()
                ? follow
                : Groups.deadline(follow, alongside.toArray(new Command[0]));
    }

    /** An event: start {@code action} once the robot is {@code fraction} (0 to 1) along the path. */
    public static Marker at(double fraction, String action) {
        return new Marker(fraction, action);
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
     * HIVE has tipped, the rest of the launch is wasted); if it finishes first, a {@link #finished()}
     * row can fire. Null runs nothing alongside.
     */
    public Command firstOf(String label, Command alongside, Row... rows) {
        if (rows.length == 0) throw new IllegalArgumentException(label + " has no rows");
        for (Row row : rows) {
            if (row.whenAlongsideDone && alongside == null) {
                throw new IllegalArgumentException(label + " waits for a command to finish but runs none");
            }
        }
        final Command[] branches = new Command[rows.length];
        for (int i = 0; i < rows.length; i++) branches[i] = sequence(rows[i].cards);
        final double[] startedAt = new double[1];
        final Command[] chosen = new Command[1];
        // [0]: alongside still running; [1]: it finished by itself
        final boolean[] alongsideState = new boolean[2];
        final CommandBuilder card = new CommandBuilder();
        card.setStart(() -> {
            startedAt[0] = secondsSinceStart.getAsDouble();
            chosen[0] = null;
            trace.accept("wait " + label);
            for (Row row : rows) row.start.run();
            alongsideState[0] = alongside != null;
            alongsideState[1] = false;
            if (alongside != null) alongside.start();
        });
        card.setExecute(() -> {
            if (chosen[0] == null) {
                if (alongsideState[0]) {
                    alongside.execute();
                    if (alongside.done()) {
                        alongside.end(EndCondition.NATURALLY);
                        alongsideState[0] = false;
                        alongsideState[1] = true;
                    }
                }
                double waited = secondsSinceStart.getAsDouble() - startedAt[0];
                for (int i = 0; i < rows.length; i++) {
                    boolean fired = rows[i].whenAlongsideDone ? alongsideState[1] : rows[i].test.passes(waited);
                    if (fired) {
                        trace.accept(String.format(Locale.US, "%s: %s after %.2f s", label, rows[i].description, waited));
                        if (alongsideState[0]) {
                            alongside.end(EndCondition.INTERRUPTED);
                            alongsideState[0] = false;
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
            if (alongsideState[0]) {
                alongside.end(end);
                alongsideState[0] = false;
            }
            if (chosen[0] != null) chosen[0].end(end);
        });
        return card;
    }

    /** Runs {@code cards} at the same time; ends when all have ended, or when the first has. */
    public Command together(String label, Ends ends, Command... cards) {
        Command group = ends == Ends.ALL ? Groups.parallel(cards) : Groups.race(cards);
        return traced("together " + label, group);
    }

    /** Runs {@code alongside} while {@code main} runs, and stops them all when {@code main} ends. */
    public Command togetherUntil(String label, Command main, Command... alongside) {
        return traced("together " + label, Groups.deadline(main, alongside));
    }

    /**
     * Drives a straight line from wherever the robot is to {@code target} — only if the pose is
     * field-referenced, the line is at most {@code maxDistanceIn} long and it stays clear of every
     * keep-out. Otherwise runs {@code ifRefused}, or nothing if it is null.
     */
    public Command goTo(String label, Pose target, double maxDistanceIn, Command ifRefused) {
        return Commands.lazy(() -> {
            Pose from = drive.pose();
            String refusal = refusal(from, target, maxDistanceIn);
            if (refusal != null) {
                trace.accept("go-to " + label + " refused: " + refusal);
                return ifRefused == null ? nothing() : ifRefused;
            }
            trace.accept(String.format(Locale.US, "go-to %s: %.0f in", label, Geometry.distance(from, target)));
            return follow(line(from, target));
        });
    }

    /**
     * A routine: drives {@code pattern} with {@code whileActions} running, until {@code endsWhen} is
     * true, the pattern is finished or {@code timeoutMs} has passed, whichever is first; then drives
     * a straight line from wherever it stopped to {@code exit} with {@code exitActions} running. The
     * editor has already checked that every such exit line misses the keep-outs.
     */
    public Command routine(String label, Path pattern, String endsWhen, double timeoutMs,
                           String[] whileActions, String[] exitActions, Pose exit) {
        registry.requireTrigger(endsWhen);
        final BooleanSupplier[] ended = new BooleanSupplier[1];
        final double[] startedAt = new double[1];
        Command run = Groups.race(
                path(label, pattern, whileActions),
                Commands.waitUntil(() -> ended[0].getAsBoolean()),
                Commands.waitMs(timeoutMs));
        Command start = Commands.instant(() -> {
            startedAt[0] = secondsSinceStart.getAsDouble();
            ended[0] = registry.watch(endsWhen);
        });
        Command report = Commands.instant(() -> trace.accept(String.format(Locale.US, "%s: %s after %.2f s",
                label, ended[0].getAsBoolean() ? endsWhen : "stopped without " + endsWhen,
                secondsSinceStart.getAsDouble() - startedAt[0])));
        Command exitLine = Commands.lazy(() -> {
            List<Command> alongside = new ArrayList<>();
            for (String name : exitActions) alongside.add(registry.command(name));
            Command follow = follow(line(drive.pose(), exit));
            return alongside.isEmpty() ? follow : Groups.deadline(follow, alongside.toArray(new Command[0]));
        });
        return Groups.sequential(start, run, report, exitLine);
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

    /** True when any of the registered triggers is true. */
    public Row when(String... anyOfConditions) {
        if (anyOfConditions.length == 0) throw new IllegalArgumentException("A row needs a trigger");
        for (String name : anyOfConditions) registry.requireTrigger(name);
        final BooleanSupplier[] checks = new BooleanSupplier[anyOfConditions.length];
        Row row = new Row(String.join(" or ", anyOfConditions), waited -> {
            for (BooleanSupplier check : checks) if (check.getAsBoolean()) return true;
            return false;
        });
        // Watched from the moment the card starts waiting, so a "since" trigger means this wait.
        row.start = () -> {
            for (int i = 0; i < checks.length; i++) checks[i] = registry.watch(anyOfConditions[i]);
        };
        return row;
    }

    /** True once the command running alongside the wait (see {@link #firstOf(String, Command, Row...)}) has finished. */
    public Row finished() {
        Row row = new Row("the command alongside finished", waited -> false);
        row.whenAlongsideDone = true;
        return row;
    }

    /** True once {@code ms} have passed since the card started. */
    public Row afterMs(double ms) {
        return new Row(String.format(Locale.US, "%.0f ms passed", ms), waited -> waited * 1000 >= ms);
    }

    /** True while fewer than {@code seconds} are left in the Autonomous period. */
    public Row timeLeftBelow(double seconds) {
        return new Row(String.format(Locale.US, "under %.1f s left", seconds), waited -> timeLeft() < seconds);
    }

    /** Always true: as the last row, it turns a {@link #firstOf} into an "if". */
    public Row otherwise() {
        return new Row("otherwise", waited -> true);
    }

    /** True while the robot's centre is within {@code radiusIn} of {@code point}. */
    public Row nearPoint(Pose point, double radiusIn) {
        return new Row(String.format(Locale.US, "within %.0f in of (%.0f, %.0f)", radiusIn, point.x(), point.y()),
                waited -> Geometry.distance(drive.pose(), point) <= radiusIn);
    }

    /** True while the robot's centre is inside the box with corners {@code a} and {@code b}. */
    public Row inArea(Pose a, Pose b) {
        final double x0 = Math.min(a.x(), b.x()), x1 = Math.max(a.x(), b.x());
        final double y0 = Math.min(a.y(), b.y()), y1 = Math.max(a.y(), b.y());
        return new Row(String.format(Locale.US, "inside (%.0f, %.0f)-(%.0f, %.0f)", x0, y0, x1, y1), waited -> {
            Pose p = drive.pose();
            return p.x() >= x0 && p.x() <= x1 && p.y() >= y0 && p.y() <= y1;
        });
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

    /** A straight line that turns from the start heading to the target heading on the way. */
    private static Path line(Pose from, Pose to) {
        return Paths.line(from, to).linear(from, to);
    }

    private String refusal(Pose from, Pose target, double maxDistanceIn) {
        if (!drive.poseReferenced()) return "the pose is not field-referenced";
        double distance = Geometry.distance(from, target);
        if (distance > maxDistanceIn) {
            return String.format(Locale.US, "%.0f in is over the %.0f in limit", distance, maxDistanceIn);
        }
        for (Pose[] zone : keepOuts) {
            if (Geometry.lineHitsPolygon(from, target, zone, ROBOT_HALF_WIDTH_IN)) return "the line crosses a keep-out";
        }
        return null;
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

    /**
     * Starts {@code action} in the same loop the robot passes {@code fraction} of the current path.
     * One command rather than a wait followed by the action, so an event is not a loop late.
     */
    private Command whenReached(double fraction, String line, Command action) {
        final boolean[] fired = new boolean[1];
        return new CommandBuilder()
                .requiring(action.requirements())
                .setStart(() -> fired[0] = false)
                .setExecute(() -> {
                    if (!fired[0]) {
                        if (drive.pathProgress() < fraction) return;
                        fired[0] = true;
                        trace.accept(line);
                        action.start();
                    }
                    if (!action.done()) action.execute();
                })
                .setDone(() -> fired[0] && action.done())
                .setEnd(end -> {
                    if (fired[0]) action.end(end);
                });
    }

    private static String percent(double fraction) {
        return Math.round(fraction * 100) + "%";
    }
}
