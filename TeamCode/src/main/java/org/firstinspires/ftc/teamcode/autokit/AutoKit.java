package org.firstinspires.ftc.teamcode.autokit;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.CommandBuilder;
import com.pedropathing.ivy.behaviors.EndCondition;
import com.pedropathing.ivy.commands.Commands;
import com.pedropathing.ivy.groups.Groups;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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
 * that uses them waits; paths are built at init. The endgame guard ({@link #guarded}) parks when
 * time runs short, cutting a wait or a command but never a path.
 *
 * <p>Depends only on Ivy and Pedro — not on {@code Robot} or the FTC SDK — so it can move to its own
 * library later, and so the tests run on a laptop. What the robot logs about an Auto's paths comes
 * through {@link PathWatcher} and {@link #paths()}, so the logging stays outside this package.
 */
public final class AutoKit {

    /**
     * Told when a path card starts driving its path and when it stops, whether it reached the end
     * or was interrupted. Set with {@link #watchPaths}; the match log draws the active path from it.
     */
    public interface PathWatcher {
        /** {@code path} has just been handed to the drivetrain. */
        void pathStarted(Path path);

        /** The card driving {@code path} has ended; nothing is being followed by this Auto now. */
        void pathEnded(Path path);
    }

    private static final PathWatcher NO_WATCHER = new PathWatcher() {
        @Override public void pathStarted(Path path) { }
        @Override public void pathEnded(Path path) { }
    };

    /** Length of the Autonomous period. */
    public static final double AUTO_LENGTH_S = 30.0;

    /**
     * Extra time the endgame guard leaves on top of a park path's drive time. The drive time is the
     * Auto Builder's estimate for the drawn path; a cut comes wherever the robot is, often off that
     * path and at rest (simulated 7 Oct 2026: a park cut at the GARDEN needed 0.3 s more than the
     * path drawn from the firing spot, and PARK was lost in 6 runs of 60 on a 0.5 s margin).
     */
    public static final double GUARD_MARGIN_S = 1.0;

    private final AutoDrive drive;
    private final AutoRegistry registry;
    private final DoubleSupplier secondsSinceStart;
    private Consumer<String> trace = line -> { };
    private PathWatcher watcher = NO_WATCHER;
    /** True while a path card, at any depth, is driving its path: the guard never cuts then. */
    private boolean drivingPath;
    /** True while a grab ({@link AutoRegistry#grab}) runs: the guard never cuts one either. */
    private boolean grabbing;
    /**
     * The park from the spot the robot last arrived at, set by a path card that carries one
     * ({@link #path(String, Path, Path, double)}); null before any, so the guard's own park path.
     */
    private Command spotPark;
    private double spotParkSeconds = Double.NaN;
    /** The running guard's own park seconds, for {@link #leftSpot}. */
    private double ownParkSeconds = Double.NaN;
    /**
     * The running guard's park seconds while it may still park, NaN otherwise. A path card nested
     * in a block reads it to refuse to start once the park no longer fits; see {@link #guarded}.
     */
    private double guardParkSeconds = Double.NaN;
    private String guardLabel = "";
    /** Every distinct path a card of this Auto can drive, in the order the cards were built. */
    private final List<Path> allPaths = new ArrayList<>();
    private final Set<Path> knownPaths = Collections.newSetFromMap(new IdentityHashMap<>());

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

    /** Where each path start and end goes (the match log). Null watches nothing. */
    public AutoKit watchPaths(PathWatcher watcher) {
        this.watcher = watcher == null ? NO_WATCHER : watcher;
        return this;
    }

    /**
     * Every distinct path a card of this Auto can drive, in the order the cards were built: the
     * paths of {@link #path} cards and the park path of each {@link #guarded} block. Complete once
     * the Auto is built, so the match log can write the whole plan before PLAY moves the robot.
     */
    public List<Path> paths() {
        return Collections.unmodifiableList(allPaths);
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
        final boolean grab = registry.isGrab(name);
        final double grabSeconds = grab ? registry.typicalSeconds(name) : 0.0;
        final double[] startedAt = new double[1];
        final boolean[] timedOut = new boolean[1];
        final boolean[] refused = new boolean[1];
        return new CommandBuilder()
                .requiring(inner.requirements())
                .setStart(() -> {
                    trace.accept("command " + name);
                    startedAt[0] = secondsSinceStart.getAsDouble();
                    timedOut[0] = false;
                    // A grab the guard could not cut must not start unless it and the park both fit.
                    refused[0] = grab && !Double.isNaN(guardParkSeconds)
                            && tooLateToContinue(guardLabel, grabSeconds + guardParkSeconds, "not starting " + name);
                    if (refused[0]) return;
                    grabbing = grab;
                    inner.start();
                })
                .setExecute(() -> {
                    if (!refused[0]) inner.execute();
                })
                .setDone(() -> {
                    if (refused[0] || inner.done()) return true;
                    timedOut[0] = secondsSinceStart.getAsDouble() - startedAt[0] >= timeoutS;
                    return timedOut[0];
                })
                .setEnd(end -> {
                    if (refused[0]) return;
                    if (grab) grabbing = false;
                    if (timedOut[0]) {
                        trace.accept(String.format(Locale.US, "command %s timed out after %.1f s", name, timeoutS));
                        inner.end(EndCondition.INTERRUPTED);
                    } else {
                        inner.end(end);
                    }
                });
    }

    /**
     * Drives {@code path} to its end. The robot is then somewhere no park was drawn from, so the
     * guard goes back to its own park path (see {@link #path(String, Path, Path, double)}).
     */
    public Command path(String label, Path path) {
        return traced("path " + label, follow(label, path, true, this::leftSpot));
    }

    /**
     * Drives {@code path} to its end, and from then on, if the endgame guard parks before another
     * such card, it drives {@code parkPath} ({@code parkSeconds} long) from there instead of its own:
     * the park drawn from this spot. Drives and grabs are never cut short, so the robot is always on
     * the last such spot when the guard parks.
     */
    public Command path(String label, Path path, Path parkPath, double parkSeconds) {
        final Command park = traced("path " + label + " park", follow(label + " park", parkPath, false, null));
        return traced("path " + label, follow(label, path, true, () -> {
            spotPark = park;
            spotParkSeconds = parkSeconds;
            if (!Double.isNaN(guardParkSeconds)) guardParkSeconds = parkSeconds;
        }));
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
     * The endgame guard. Runs {@code cards} in order, and drives {@code parkPath} instead as soon as
     * the time left no longer covers {@code parkSeconds} plus {@link #GUARD_MARGIN_S}. It checks
     * before each card starts and on every loop while a wait or a command runs, so one that would
     * run past that deadline (a long wait, a slow launch) is ended {@link EndCondition#INTERRUPTED}
     * at the deadline and the park starts in the same loop, from the spot the robot is standing on.
     *
     * <p><b>A path is never cut short.</b> A path the guard has let start runs to its end, and the
     * guard parks from there: cutting a drive part-way leaves the robot somewhere nobody drew a
     * path from, and the park path's lead-in was drawn from a spot (mentor, 18 Oct 2026; the
     * simulator had already cut a tunnel run between the HIVE's feet). The check before each card
     * starts still keeps a path from starting once the park no longer fits. (It does not know how
     * long the path itself will take: a path started just before the deadline runs past it.)
     *
     * <p><b>The same rules hold inside a block.</b> A {@link #firstOf} whose row runs its own cards
     * (L-Quals and the Sister Autos are mostly that) is one card to the guard, so it goes by what is
     * running inside: it cuts the block at the deadline while a wait or a command inside it runs,
     * never while a path inside it drives, and a path inside it that would start after the
     * deadline does not start: it finishes at once and the guard parks.
     *
     * <p><b>Grabs are treated as paths.</b> A grab ({@link AutoRegistry#grab}: a FLOWER pick, a GARDEN
 * gather) drives the robot off its spot and back, so the guard never cuts one either, and it does
 * not start unless its typical time and the park both still fit.
 *
 * <p><b>It parks from the spot the robot is on.</b> A path card that carries a park
 * ({@link #path(String, Path, Path, double)}) makes that park the guard's from when it arrives,
 * with that park's own time, until a path card without one arrives somewhere else. Since drives and
 * grabs are never cut, the robot is on that spot whenever the guard parks, so it drives a park
 * drawn from where it is.
 *
 * <p>The last card is assumed to be the park itself and is never skipped or cut short.
     *
     * <p>History: until 1 Oct 2026 the guard checked only between cards. A "wait for Tip, at most
     * 7000 ms" started at 20 s then ran to 27 s before the guard looked, a 3.3 s park no longer
     * fit, and the robot lost AUTO PARK; so the guard began to cut the running card, whatever it
     * was. Since 18 Oct it cuts waits and commands, never paths. Since 27 Oct that holds inside a
     * block too (#142): before, the guard knew only its own top-level path cards, so it cut a block
     * mid-drive and let a block's next path start after the deadline.
     */
    public Command guarded(String label, Path parkPath, double parkSeconds, Command... cards) {
        final Command park = traced("path " + label + " park", follow(label + " park", parkPath, false, null));
        final int[] index = new int[1];
        final Command[] current = new Command[1];
        final Command[] parking = new Command[1];
        final CommandBuilder card = new CommandBuilder();
        card.setStart(() -> {
            index[0] = 0;
            current[0] = null;
            parking[0] = null;
            spotPark = null;
            spotParkSeconds = Double.NaN;
            watchFromHere(label, parkSeconds);
            advance(label, parkSeconds, cards, index, current, park, parking);
        });
        card.setExecute(() -> {
            if (current[0] == null) return;
            if (current[0].done()) {
                current[0].end(EndCondition.NATURALLY);
                if (current[0] == parking[0]) {
                    current[0] = null;
                    index[0] = cards.length;
                    return;
                }
                index[0]++;
                advance(label, parkSeconds, cards, index, current, park, parking);
                return;
            }
            boolean last = index[0] == cards.length - 1;
            // drivingPath, not "is this card a path": a block's row may be driving one of its own.
            boolean cuttable = current[0] != parking[0] && !last && !drivingPath && !grabbing;
            if (cuttable && tooLateToContinue(label, parkSecondsNow(parkSeconds), "cutting card short")) {
                current[0].end(EndCondition.INTERRUPTED);
                startPark(parkNow(park), current, parking);
                return;
            }
            current[0].execute();
        });
        card.setDone(() -> current[0] == null);
        card.setEnd(end -> {
            if (current[0] != null) current[0].end(end);
            stopWatching();
        });
        return card;
    }

    private void watchFromHere(String label, double parkSeconds) {
        guardLabel = label;
        guardParkSeconds = parkSeconds;
        ownParkSeconds = parkSeconds;
    }

    /** The robot has arrived somewhere with no park of its own: the guard's own park again. */
    private void leftSpot() {
        spotPark = null;
        spotParkSeconds = Double.NaN;
        if (!Double.isNaN(guardParkSeconds)) guardParkSeconds = ownParkSeconds;
    }

    /** From the park, or the last card, on, nothing is the guard's to refuse. */
    private void stopWatching() {
        guardParkSeconds = Double.NaN;
    }

    private void startPark(Command park, Command[] current, Command[] parking) {
        stopWatching();
        if (park == spotPark) trace.accept("parking from the spot the robot is on");
        current[0] = park;
        parking[0] = park;
        park.start();
    }

    /** The park to drive now: from the spot the robot last arrived at, or the guard's own. */
    private Command parkNow(Command own) {
        return spotPark != null ? spotPark : own;
    }

    private double parkSecondsNow(double own) {
        return spotPark != null ? spotParkSeconds : own;
    }

    private void advance(String label, double parkSeconds, Command[] cards, int[] index, Command[] current,
                         Command park, Command[] parking) {
        if (index[0] >= cards.length) {
            current[0] = null;
            return;
        }
        boolean last = index[0] == cards.length - 1;
        if (!last && tooLateToContinue(label, parkSecondsNow(parkSeconds), "parking now")) {
            startPark(parkNow(park), current, parking);
            return;
        }
        if (last) stopWatching();
        current[0] = cards[index[0]];
        current[0].start();
    }

    /** True, and traced, when the time left no longer covers the park plus {@link #GUARD_MARGIN_S}. */
    private boolean tooLateToContinue(String label, double parkSeconds, String action) {
        double left = timeLeft();
        double needed = parkSeconds + GUARD_MARGIN_S;
        if (left >= needed) return false;
        trace.accept(String.format(Locale.US, "%s: %.1f s left, park needs %.1f s: %s", label, left, needed, action));
        return true;
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

    /**
     * Drives {@code path} to its end. With {@code guardable}, a card that starts while a guard is
     * watching and the park no longer fits does not drive at all: it finishes at once, untouched,
     * and the guard parks on its next check. That is how a path inside a block's row is kept from
     * starting late, where the guard itself sees only the block.
     */
    private Command follow(String label, Path path, boolean guardable, Runnable onArrived) {
        if (knownPaths.add(path)) allPaths.add(path);
        final boolean[] refused = new boolean[1];
        return new CommandBuilder()
                .setStart(() -> {
                    refused[0] = guardable && !Double.isNaN(guardParkSeconds)
                            && tooLateToContinue(guardLabel, guardParkSeconds, "not starting " + label);
                    if (refused[0]) return;
                    drive.follow(path);
                    drivingPath = true;
                    watcher.pathStarted(path);
                })
                .setDone(() -> refused[0] || drive.pathDone())
                .setEnd(end -> {
                    if (refused[0]) return;
                    drivingPath = false;
                    watcher.pathEnded(path);
                    if (onArrived != null && end == EndCondition.NATURALLY) onArrived.run();
                });
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
