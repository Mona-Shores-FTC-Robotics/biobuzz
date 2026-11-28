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
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;
import java.util.function.DoubleUnaryOperator;

/**
 * The cards an Auto built in the Auto Builder is made of. A generated Auto class calls these, in
 * the same order as the card list in the editor, and gets back one Ivy {@link Command}.
 *
 * <p>There are three kinds of card: a command, a path, and the one branching block,
 * {@link #firstOf}: wait for a trigger, at most some time (or until a time on the match clock),
 * then run the cards of whichever came first. A plain "Wait for" is a {@code firstOf} whose rows
 * have no cards.
 *
 * <p><b>The drive team may change a few things in INIT</b> ({@link AutoOption}): the PARK spot and
 * a wait's match-clock limit. The generated code reads them while it builds, through
 * {@link #pick} and {@link #seconds}; with no picks ({@link #picks}) the Auto is as drawn.
 *
 * <p>Nothing here reads hardware or blocks. Triggers are read once per loop, only while a card
 * that uses them waits; paths are built at init. The endgame guard ({@link #guarded}) goes for the
 * next volley when its last piece can be out before AUTO ends, and otherwise parks when time runs
 * short, cutting a wait or a command but never a path, a grab or a feeding launch.
 *
 * <p><b>A path card hands over at Pedro's path end, with the robot still rolling.</b> A path card
 * is done when {@link AutoDrive#pathDone} says so: on the robot, Pedro's parametric end, where the
 * Follower switches to holding the end pose and brakes (measured 29 Oct to 2 Nov 2026: 10 to 24 in/s
 * at the hand-over). The kit adds no settling of its own. A card that needs the robot still waits
 * itself: a launch for {@code PathEndSettle} (aimed and slowing), unless it already opened the
 * gate on the way in; a grab for its own hand-over speed ({@code GrabRoutine}), since a pick squares
 * up on its own and ends on contact.
 *
 * <p><b>A path card right before a command card lets the command get ready on the way in</b>
 * ({@link AutoRegistry.Approach}): {@link #sequence}, {@link #guarded} and a {@link Row}'s cards
 * pair each path card with the command card that follows it, when that command has an approach, and
 * the path card feeds it every loop while it drives. A FLOWER pick's stick is down before the drive
 * ends, so the push need not wait for it (mentor, 3 Nov 2026); a {@code LaunchAll} opens the gate in
 * the last few inches once aimed and at speed, as a good TeleOp driver does, so its card only
 * finishes the feed (mentor, 4 Nov 2026). The cards stay what they are: the drive ends where it was
 * drawn, and the command is still the whole command. An approach whose command does not then run
 * (the guard refuses it or parks first, the path is cut short, another path starts) is cancelled.
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

    /**
     * The drive team's INIT picks ({@link AutoOption}): which choice of each option, and the time
     * of each match-clock option. Set with {@link #picks}; with none, every option is as drawn.
     */
    public interface Picks {
        /** The index into the option's choices, or -1 for the drawing's. */
        int choice(String option);

        /** The option's time in seconds on the match clock, or NaN for the drawing's. */
        double seconds(String option);
    }

    private static final Picks AS_DRAWN = new Picks() {
        @Override public int choice(String option) { return -1; }
        @Override public double seconds(String option) { return Double.NaN; }
    };

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
    private Picks picks = AS_DRAWN;
    /** True while a path card, at any depth, is driving its path: the guard never cuts then. */
    private boolean drivingPath;
    /** True while a grab ({@link AutoRegistry#grab}) runs: the guard never cuts one either. */
    private boolean grabbing;
    /** The running grab: when it started on the match clock, its planned seconds, and the seconds granted on top ({@link #grantGrabSeconds}). */
    private double grabStartedAt = Double.NaN;
    private double grabPlannedS;
    private double grabExtraS;
    /**
     * The park from the spot the robot last arrived at, set by a path card that carries one
     * ({@link #path(String, Path, double, Path, double)}); null before any, so the guard's own park path.
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
    /**
     * A drive or a grab refused to start because it and the park after it no longer fit: the
     * guard parks at its next look, from where the robot is, rather than run the next card.
     */
    private boolean parkWanted;
    /** Every distinct path a card of this Auto can drive, in the order the cards were built. */
    private final List<Path> allPaths = new ArrayList<>();
    private final Set<Path> knownPaths = Collections.newSetFromMap(new IdentityHashMap<>());
    /** Each path card built by {@link #path}, by the command handed out, for {@link #pairApproaches}. */
    private final Map<Command, PathCard> pathCards = new IdentityHashMap<>();
    /**
     * Each command card built by {@link #command} whose command has an approach, by the command
     * handed out, for {@link #pairApproaches}.
     */
    private final Map<Command, CommandCard> commandCards = new IdentityHashMap<>();
    /**
     * The approach a path card ran to its end, waiting for its command card to start; null when
     * none waits. Cancelled if the guard parks, or another path starts, before that card does.
     */
    private AutoRegistry.Approach armed;

    /** What a path card knows beyond its path: the approach of the command card after it, if any. */
    private static final class PathCard {
        AutoRegistry.Approach approach;
    }

    /** What a command card knows beyond its command: the approach a path card before it is running. */
    private static final class CommandCard {
        AutoRegistry.Approach before;
    }

    /**
     * Each CELL check ({@link #checkPlan}) whose volley has an approach and whose trigger is a state
     * trigger ({@link AutoRegistry#isStateTrigger}), by the check's card: the approach a path card
     * right before the check runs, for {@link #pairApproaches}.
     */
    private final Map<Command, CheckApproach> checkApproaches = new IdentityHashMap<>();
    /** Each {@link #guarded} list's first card, by the list's card: where a check's volley is found. */
    private final Map<Command, Command> guardedFirst = new IdentityHashMap<>();

    /**
     * The volley's approach, run by the path card before a CELL check, through the check: "drive
     * to the launch spot, wait for the CELL up, then LaunchAll" gets ready on the way in as
     * "drive, then LaunchAll" does. The launcher spins up for the path's end as the path starts;
     * the gate may open on the approach only while the check's trigger is true, so no piece goes
     * at a CELL not seen up. Once it has opened, the check takes its trigger row ({@link #firstOf}).
     * Before 23 Nov 2026 the check stood between the path and the volley and nothing paired: at
     * Belding and Fruitport no volley after an Auto's first opened the gate before the path's end,
     * which it did 0.1 to 0.6 s after, and the first ball left a median 0.85 s after the gate.
     */
    private final class CheckApproach implements AutoRegistry.Approach {
        final AutoRegistry.Approach volley;
        private final String trigger;
        private BooleanSupplier up;

        CheckApproach(AutoRegistry.Approach volley, String trigger) {
            this.volley = volley;
            this.trigger = trigger;
        }

        @Override public void started(Pose end) {
            up = registry.watch(trigger);
            volley.started(end);
        }

        @Override public void approaching(Pose pose, double remainingIn) {
            if (up != null && up.getAsBoolean()) volley.approaching(pose, remainingIn);
        }

        @Override public void cancel() {
            up = null;
            volley.cancel();
        }

        @Override public boolean committed() {
            return volley.committed();
        }
    }

    /**
     * What the guard can know ahead of each card it may look past ({@link #guarded}): how long it
     * takes, from the match second it starts at, and for a volley the seconds to its last piece.
     * A card with no plan here (a block, a drive with no time) ends the look-ahead.
     */
    private static final class Plan {
        final DoubleUnaryOperator secondsFrom;
        final double lastPieceS;
        /** A volley fired where the robot has parked ({@link AutoRegistry#parkedVolley}): no park after it. */
        final boolean parked;

        private Plan(DoubleUnaryOperator secondsFrom, double lastPieceS) {
            this(secondsFrom, lastPieceS, false);
        }

        private Plan(DoubleUnaryOperator secondsFrom, double lastPieceS, boolean parked) {
            this.secondsFrom = secondsFrom;
            this.lastPieceS = lastPieceS;
            this.parked = parked;
        }

        static Plan lasting(double seconds) {
            return new Plan(startsAt -> seconds, Double.NaN);
        }

        static Plan volley(double seconds, double lastPieceS, boolean parked) {
            return new Plan(startsAt -> seconds, lastPieceS, parked);
        }

        boolean isVolley() {
            return !Double.isNaN(lastPieceS);
        }
    }

    private final Map<Command, Plan> plans = new IdentityHashMap<>();
    /** Every {@link #guarded} list, and the volley's plan of each whose first card is a volley. */
    private final Set<Command> guards = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<Command, Plan> guardedVolleys = new IdentityHashMap<>();
    /** The CELL checks ({@link #checkPlan}): the look-ahead counts one as its volley, even as a list's last card. */
    private final Set<Command> checks = Collections.newSetFromMap(new IdentityHashMap<>());
    /**
     * The running guard's card, and every card after it up to the next volley, fit with that
     * volley's last piece out before AUTO ends: no park, and no cut, until the next card starts.
     */
    private boolean goingForVolley;
    /** When the volley the guard is going for has its last piece out, on the match clock. */
    private double volleyLastPieceAt = Double.NaN;
    /**
     * On {@link AutoOption#PARK_FIRST}: when the park after that volley is done (its last piece out,
     * then the park), on the match clock; NaN when no park is planned after it. For {@link #grantGrabSeconds}.
     */
    private double volleyParkedAt = Double.NaN;
    /** The volley card the guard is going for, and whether "going for it" has been traced for it. */
    private Command volleyTarget;
    private boolean goingForVolleyTraced;

    /** {@link #guard}: false only for a practice run that times the whole plan. */
    private boolean guardOn = true;
    /**
     * Set by {@link #goForVolleyIfItFits} on {@link AutoOption#PARK_FIRST}: the cards from here to
     * the next volley, that volley and then the park do not fit before AUTO ends, so the guard parks
     * now rather than start a trip whose volley would leave no time to park. Why, for the trace.
     */
    private String volleyLeavesNoPark;
    /** With the guard off, whether the one "would have parked" line has been traced. */
    private boolean guardWouldHaveActed;

    public AutoKit(AutoDrive drive, AutoRegistry registry, DoubleSupplier secondsSinceStart) {
        this.drive = drive;
        this.registry = registry;
        this.secondsSinceStart = secondsSinceStart;
        registry.answerGrabTime(this::grantGrabSeconds);
    }

    /**
     * A running grab asks for {@code extraS} more seconds than it was planned for (a FLOWER pick's
     * push again after a stall far short, {@code FlowerPicker.Retry}). Yes, and traced, when the
     * grab's planned end moved that much later still leaves what the guard planned around it: the
     * next volley's last piece out by 30 s and, on {@link AutoOption#PARK_FIRST}, the park after it
     * done by 30 s, when the guard is going for that volley; else the park, with its margin, done by
     * 30 s, while a guard watches; and always the grab itself over by 30 s. A yes also lets the
     * grab's card run {@code extraS} past its timeout. No for anything but a running grab. With the
     * guard off (practice) the answer is yes, and what the guard would have said is traced once.
     */
    public boolean grantGrabSeconds(double extraS) {
        if (!grabbing || !(extraS > 0.0)) return false;
        double now = secondsSinceStart.getAsDouble();
        double plannedEnd = grabStartedAt + grabPlannedS + grabExtraS;
        // A grab already running past its plan has moved everything after it by that much too.
        double shift = extraS + Math.max(0.0, now - plannedEnd);
        double end = Math.max(now, plannedEnd) + extraS;
        String no = null;
        if (end > AUTO_LENGTH_S) {
            no = String.format(Locale.US, "it would end at %.1f s, after AUTO", end);
        } else if (!Double.isNaN(guardParkSeconds)) {
            if (goingForVolley) {
                if (volleyLastPieceAt + shift > AUTO_LENGTH_S) {
                    no = String.format(Locale.US, "the next volley's last piece would be out at %.1f s", volleyLastPieceAt + shift);
                } else if (!Double.isNaN(volleyParkedAt) && volleyParkedAt + shift > AUTO_LENGTH_S) {
                    no = String.format(Locale.US, "the park after the next volley would end at %.1f s", volleyParkedAt + shift);
                }
            } else if (end + guardParkSeconds + GUARD_MARGIN_S > AUTO_LENGTH_S) {
                no = String.format(Locale.US, "the park (%.1f s + %.1f s margin) would end at %.1f s",
                        guardParkSeconds, GUARD_MARGIN_S, end + guardParkSeconds + GUARD_MARGIN_S);
            }
        }
        if (no != null && guardOn) {
            trace.accept(String.format(Locale.US, "%s: no %.1f s more for the grab: %s", guardLabel, extraS, no));
            return false;
        }
        if (no != null && !guardWouldHaveActed) {
            guardWouldHaveActed = true;
            trace.accept(String.format(Locale.US, "%s: guard OFF: would have refused the grab %.1f s more: %s", guardLabel, extraS, no));
        }
        grabExtraS += extraS;
        trace.accept(String.format(Locale.US, "%s: %.1f s more for the grab, to end by %.1f s", guardLabel, extraS, end));
        return true;
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
     * False runs every card whatever the clock says (practice only): the guard refuses and cuts
     * nothing, so the robot drives the whole plan and the log shows how long it really takes. It
     * still traces, once, the first moment it would have parked. On by default.
     */
    public AutoKit guard(boolean on) {
        this.guardOn = on;
        return this;
    }

    /** The drive team's INIT picks, read while the Auto is built. Null runs everything as drawn. */
    public AutoKit picks(Picks picks) {
        this.picks = picks == null ? AS_DRAWN : picks;
        return this;
    }

    /**
     * The value for the drive team's pick of {@code option}: {@code byChoice} lists one value per
     * choice, in the option's order, the drawing's first. With no pick, or one past the end, the
     * drawing's. {@code Pose park = kit.pick("Park", leftPark, rightPark)}.
     */
    @SafeVarargs
    public final <T> T pick(String option, T... byChoice) {
        if (byChoice.length == 0) throw new IllegalArgumentException(option + " has no values");
        int index = picks.choice(option);
        return index >= 0 && index < byChoice.length ? byChoice[index] : byChoice[0];
    }

    /** The drive team's time for the match-clock option {@code option}, else {@code drawn}. */
    public double seconds(String option, double drawn) {
        double picked = picks.seconds(option);
        return Double.isNaN(picked) ? drawn : picked;
    }

    /**
     * Every distinct path a card of this Auto can drive, in the order the cards were built: the
     * paths of {@link #path} cards and the park path of each {@link #guarded} block. Complete once
     * the Auto is built, so the match log can write the whole plan before PLAY moves the robot.
     */
    public List<Path> paths() {
        return Collections.unmodifiableList(allPaths);
    }

    /**
     * What {@link #route} hands back for a card that is not on the picked route. Every card list
     * the kit takes ({@link #sequence}, {@link #guarded}, a {@link Row}'s cards) drops it, so the
     * Auto runs as if the card had never been written: no empty step, no extra loop.
     */
    private static final Command OFF_ROUTE = Commands.instant(() -> { });

    /**
     * One card of a route choice: where the drive team's INIT pick of {@code option} is choice
     * {@code index} (0, the drawing's, when nothing was picked), the card {@code card} builds;
     * otherwise nothing. The Auto Builder writes the cards of each alternative leg one after
     * another, each wrapped in this, so the card list stays flat and the endgame guard plans
     * through whichever leg was picked exactly as through any other cards. The card is built only
     * when picked, so the leg not driven adds no path to {@link #paths()}.
     *
     * <pre>
     * kit.route("Pickup", 0, () -&gt; kit.path("to WALL_FLOWER_STAGING", toWallFlower, 2.1, park, 2.1)),
     * kit.route("Pickup", 0, () -&gt; kit.command("PickFlower", 4.5)),
     * kit.route("Pickup", 1, () -&gt; kit.path("to GARDEN_STAGING", toGarden, 2.4, park, 3.1)),
     * kit.route("Pickup", 1, () -&gt; kit.command("GardenGather", 5.5)),
     * </pre>
     */
    public Command route(String option, int index, Supplier<Command> card) {
        // A Priority route (AutoOption.PRIORITY) follows the Endgame row: its first leg, the third
        // TIP, on TIPS FIRST; its second, the park, on PARK FIRST. A pick of "Priority" itself is ignored.
        int picked = AutoOption.PRIORITY.equals(option) ? (tipsFirst() ? 0 : 1) : picks.choice(option);
        boolean on = (picked < 0 ? 0 : picked) == index;
        return on ? card.get() : OFF_ROUTE;
    }

    /** The drive team's {@link AutoOption#ENDGAME} is {@link AutoOption#TIPS_FIRST}, the default when unpicked. */
    private boolean tipsFirst() {
        return picks.choice(AutoOption.ENDGAME) != AutoOption.PARK_FIRST_INDEX;
    }

    /**
     * Whether a volley must leave time for the park after it: the drive team's {@link AutoOption#ENDGAME}
     * row: only when {@link AutoOption#PARK_FIRST} was picked ({@link AutoOption#TIPS_FIRST} is the default). On TIPS FIRST
     * the guard asks only that the volley's last piece is out by 30 s, and the robot ends AUTO where it
     * shot.
     */
    private boolean parkComesFirst() {
        return !tipsFirst();
    }

    /** {@code cards} without the ones {@link #route} left off; the same array when there are none. */
    private static Command[] onRoute(Command[] cards) {
        int off = 0;
        for (Command card : cards) if (card == OFF_ROUTE) off++;
        if (off == 0) return cards;
        Command[] out = new Command[cards.length - off];
        int n = 0;
        for (Command card : cards) if (card != OFF_ROUTE) out[n++] = card;
        return out;
    }

    /**
     * Whether the drive team picked the timer for the shots ({@link AutoOption#SHOT_TRIGGER}): the
     * robot does not look at the HIVE, and each {@link #shotWhen} fires at its time. Camera, the
     * drawing's, when nothing was picked.
     */
    public boolean shotsOnTimer() {
        return picks.choice(AutoOption.SHOT_TRIGGER) == 1;
    }

    /** Seconds left in the Autonomous period. */
    public double timeLeft() {
        return AUTO_LENGTH_S - secondsSinceStart.getAsDouble();
    }

    // ------------------------------------------------------------------ cards

    /** Runs {@code cards} one after another. A path card right before a command card runs the command's approach. */
    public Command sequence(Command... given) {
        final Command[] cards = onRoute(given);
        pairApproaches(cards);
        return cards.length == 0 ? nothing() : Groups.sequential(cards);
    }

    /**
     * Pairs each path card in {@code cards} with the command card right after it, when that command
     * has an {@link AutoRegistry.Approach}: the path card runs the approach while it drives, and the
     * command card takes over what it got ready, or cancels it if the guard refuses the command. Only
     * cards this kit built pair; anything else in the list is left alone. Called for every card list
     * a block runs, so a pair inside a {@link #firstOf} row pairs too.
     */
    private void pairApproaches(Command[] cards) {
        for (int i = 0; i + 1 < cards.length; i++) {
            PathCard path = pathCards.get(cards[i]);
            if (path == null) continue;
            CommandCard command = commandCards.get(cards[i + 1]);
            if (command != null) {
                path.approach = command.before;
                continue;
            }
            CheckApproach through = checkApproaches.get(cards[i + 1]);
            if (through != null) path.approach = through;
        }
    }

    /** The armed approach will not be taken over by its command: put away what it got ready. */
    private void cancelArmed() {
        AutoRegistry.Approach approach = armed;
        armed = null;
        if (approach != null) approach.cancel();
    }

    /** How long a command step may run when the Auto sets no timeout. */
    public static final double DEFAULT_TIMEOUT_S = 5.0;

    /** The registered command {@code name}, cut off after {@link #DEFAULT_TIMEOUT_S}. */
    public Command command(String name) {
        return command(name, DEFAULT_TIMEOUT_S);
    }

    /**
     * {@code name}, a parked volley ({@link AutoRegistry#parkedVolley}: {@code LaunchAllAimed}) drawn
     * where the robot has not parked (not right after a park card): an ordinary volley, which the
     * guard plans a park after as it does for {@code LaunchAll}. L-RightUnderHive fires its preloads
     * aimed from its NECTAR turn this way (mentor, 28 Nov 2026). The Visualizer's export writes it.
     */
    public Command commandAwayFromPark(String name) {
        return command(name, DEFAULT_TIMEOUT_S, false);
    }

    /** {@link #commandAwayFromPark(String)} with its own time limit, as {@link #command(String, double)}. */
    public Command commandAwayFromPark(String name, double timeoutS) {
        return command(name, timeoutS, false);
    }

    /**
     * The registered command {@code name}: runs until it finishes or {@code timeoutS} has passed,
     * whichever is first, so a command that never finishes cannot stall the Auto. The timeout waits
     * while the registry holds time limits ({@link AutoRegistry#holdTimeLimitsWhile}). A timeout is
     * traced, since it usually means a mechanism did not do its job.
     */
    public Command command(String name, double timeoutS) {
        return command(name, timeoutS, true);
    }

    /** {@link #command(String, double)}; {@code mayBeParked} false: never a parked volley ({@link #commandAwayFromPark}). */
    private Command command(String name, double timeoutS, boolean mayBeParked) {
        final Command inner = mayBeParked ? registry.command(name) : registry.commandAwayFromPark(name);
        final boolean grab = registry.isGrab(name);
        final double grabSeconds = grab ? registry.typicalSeconds(name) : 0.0;
        final boolean volley = registry.isVolley(name);
        final boolean parkedVolley = mayBeParked && registry.isParkedVolley(name);
        final double lastPieceSeconds = volley ? registry.lastPieceSeconds(name) : Double.NaN;
        final AutoRegistry.Approach approach = mayBeParked ? registry.approach(name) : registry.approachAwayFromPark(name);
        final CommandCard card = approach == null ? null : new CommandCard();
        if (card != null) card.before = approach;
        final double[] startedAt = new double[1];
        final boolean[] timedOut = new boolean[1];
        final boolean[] refused = new boolean[1];
        Command command = new CommandBuilder()
                .requiring(inner.requirements())
                .setStart(() -> {
                    trace.accept("command " + name);
                    startedAt[0] = secondsSinceStart.getAsDouble();
                    timedOut[0] = false;
                    // A grab the guard could not cut must not start unless it and the park both fit.
                    refused[0] = grab && !Double.isNaN(guardParkSeconds)
                            && tooLateToContinue(guardLabel, grabSeconds + guardParkSeconds, "not starting " + name);
                    // Nor a volley, on PARK FIRST: once it feeds it is never cut. The guard's
                    // look-ahead already decides for a volley in its own list or behind a "CELL up?"
                    // check (goingForVolley, or it parked before this card); this is a volley deeper
                    // in a block's row, which the look-ahead cannot see.
                    // A volley fired where the robot has parked needs no park after it, but always its last
                    // piece out before AUTO ends, whether or not a guard is still watching.
                    if (!refused[0] && parkedVolley) {
                        double lastPieceAt = startedAt[0] + lastPieceSeconds;
                        refused[0] = lastPieceAt > AUTO_LENGTH_S;
                        if (refused[0]) {
                            trace.accept(String.format(Locale.US,
                                    "not starting %s: its last piece would be out at %.1f s, after AUTO ends; staying parked",
                                    name, lastPieceAt));
                        }
                    }
                    if (!refused[0] && volley && !parkedVolley && parkComesFirst() && !Double.isNaN(guardParkSeconds)) {
                        refused[0] = noParkAfterVolley(guardLabel, startedAt[0], lastPieceSeconds,
                                parkSecondsNow(guardParkSeconds), name);
                    }
                    // Parked already, a refused volley leaves the robot where it is: nothing to park.
                    if (refused[0] && !parkedVolley) parkWanted = true;
                    // The drive before it may have got the grab's mechanisms ready: put them away.
                    if (refused[0]) {
                        if (card != null && armed == card.before) cancelArmed();
                        return;
                    }
                    // Starting: what the drive before it got ready is this command's to finish.
                    if (card != null && armed == card.before) armed = null;
                    grabbing = grab;
                    if (grab) {
                        grabStartedAt = startedAt[0];
                        grabPlannedS = grabSeconds;
                        grabExtraS = 0.0;
                    }
                    inner.start();
                })
                .setExecute(() -> {
                    if (!refused[0]) inner.execute();
                })
                .setDone(() -> {
                    if (refused[0] || inner.done()) return true;
                    if (registry.timeLimitsHeld()) return false;
                    // A grab given more time (grantGrabSeconds) has that much longer before its timeout.
                    timedOut[0] = secondsSinceStart.getAsDouble() - startedAt[0] >= timeoutS + (grab ? grabExtraS : 0.0);
                    return timedOut[0];
                })
                .setEnd(end -> {
                    if (refused[0]) return;
                    if (grab) grabbing = false;
                    if (timedOut[0]) {
                        trace.accept(String.format(Locale.US, "command %s timed out after %.1f s", name,
                                timeoutS + (grab ? grabExtraS : 0.0)));
                        inner.end(EndCondition.INTERRUPTED);
                    } else {
                        inner.end(end);
                    }
                });
        if (card != null) commandCards.put(command, card);
        plans.put(command, registry.isVolley(name)
                ? Plan.volley(registry.typicalSeconds(name), registry.lastPieceSeconds(name), parkedVolley)
                : Plan.lasting(registry.typicalSeconds(name)));
        return command;
    }

    /**
     * Drives {@code path} to its end. The robot is then somewhere no park was drawn from, so the
     * guard goes back to its own park path (see {@link #path(String, Path, double, Path, double)}).
     * With no drive time, the guard only checks that its park from here still fits before starting
     * it; an exported Auto gives the time ({@link #path(String, Path, double)}).
     */
    public Command path(String label, Path path) {
        PathCard card = new PathCard();
        return pathCard(card, traced("path " + label, follow(label, path, true, () -> guardParkSeconds, this::leftSpot, card)));
    }

    /**
     * {@link #path(String, Path)}, {@code driveSeconds} long: it does not start unless the drive and
     * then the guard's own park from where it ends both still fit.
     */
    public Command path(String label, Path path, double driveSeconds) {
        PathCard card = new PathCard();
        return planned(Plan.lasting(driveSeconds), pathCard(card, traced("path " + label,
                follow(label, path, true, () -> driveSeconds + ownParkSeconds, this::leftSpot, card))));
    }

    /**
     * Drives {@code path} ({@code driveSeconds} long) to its end, and from then on, if the endgame
     * guard parks before another such card, it drives {@code parkPath} ({@code parkSeconds} long)
     * from there instead of its own: the park drawn from this spot. It does not start unless the
     * drive and then that park both still fit, so the robot never sets off for a spot it could not
     * park from in time. Drives and grabs are never cut short, so the robot is always on the last
     * such spot when the guard parks.
     */
    public Command path(String label, Path path, double driveSeconds, Path parkPath, double parkSeconds) {
        return planned(Plan.lasting(driveSeconds),
                spotPath(label, path, () -> driveSeconds + parkSeconds, parkPath, parkSeconds));
    }

    /** {@link #path(String, Path, double, Path, double)} with no drive time: checked as {@link #path(String, Path)}. */
    public Command path(String label, Path path, Path parkPath, double parkSeconds) {
        return spotPath(label, path, () -> guardParkSeconds, parkPath, parkSeconds);
    }

    private Command spotPath(String label, Path path, DoubleSupplier needed, Path parkPath, double parkSeconds) {
        final Command park = traced("path " + label + " park", follow(label + " park", parkPath, false, null, null, null));
        PathCard card = new PathCard();
        return pathCard(card, traced("path " + label, follow(label, path, true, needed, () -> {
            spotPark = park;
            spotParkSeconds = parkSeconds;
            if (!Double.isNaN(guardParkSeconds)) guardParkSeconds = parkSeconds;
        }, card)));
    }

    /** Remembers what the guard can know ahead of {@code command}: see {@link Plan}. */
    private Command planned(Plan plan, Command command) {
        plans.put(command, plan);
        return command;
    }

    /** Remembers {@code command} as the path card {@code card}, for {@link #pairApproaches}. */
    private Command pathCard(PathCard card, Command command) {
        pathCards.put(command, card);
        return command;
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
        for (int i = 0; i < rows.length; i++) {
            rows[i].cards = onRoute(rows[i].cards);
            branches[i] = sequence(rows[i].cards);
        }
        final double[] startedAt = new double[1];
        final Command[] chosen = new Command[1];
        final boolean[] alongsideRunning = new boolean[1];
        // A CELL check the drive before it got ready through (CheckApproach): set below once built.
        final CheckApproach[] through = new CheckApproach[1];
        final int[] triggerRow = {-1};
        final boolean[] ownArmed = new boolean[1];
        final CommandBuilder card = new CommandBuilder();
        card.setStart(() -> {
            startedAt[0] = secondsSinceStart.getAsDouble();
            chosen[0] = null;
            ownArmed[0] = through[0] != null && armed == through[0];
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
                boolean held = registry.timeLimitsHeld();
                for (int i = 0; i < rows.length; i++) {
                    if (rows[i].timeLimit && held) continue;
                    // The gate opened on the drive in, with the CELL seen up: the volley is under way.
                    boolean committed = ownArmed[0] && i == triggerRow[0] && through[0].committed();
                    if (committed || rows[i].test.passes(waited)) {
                        trace.accept(String.format(Locale.US, "%s: %s after %.2f s", label,
                                committed ? rows[i].description + " (gate opened on the approach)" : rows[i].description, waited));
                        if (ownArmed[0]) {
                            ownArmed[0] = false;
                            // The volley's card takes over what the drive got ready, or it is put away.
                            if (armed == through[0]) {
                                if (i == triggerRow[0]) armed = through[0].volley;
                                else cancelArmed();
                            }
                        }
                        if (alongsideRunning[0]) {
                            alongside.end(EndCondition.INTERRUPTED);
                            alongsideRunning[0] = false;
                        }
                        // The look-ahead counted a CELL check as its volley as if the CELL were up at
                        // once. It has waited since, so that "going for it" is stale: the volley asks
                        // for itself as it starts, and does not start if the park no longer fits.
                        if (parkComesFirst() && checks.contains(card)) goingForVolley = false;
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
            if (ownArmed[0]) {
                ownArmed[0] = false;
                if (armed == through[0]) cancelArmed();
            }
            if (chosen[0] != null) chosen[0].end(end);
        });
        Plan plan = waitPlan(alongside, rows);
        if (plan != null) return planned(plan, card);
        plan = checkPlan(rows);
        if (plan == null) return card;
        checks.add(card);
        for (int i = 0; i < rows.length; i++) {
            if (rows[i].timeLimit) continue;
            triggerRow[0] = i;
            String trigger = rows[i].trigger;
            if (trigger == null || !registry.isStateTrigger(trigger) || rows[i].cards.length == 0) break;
            Command first = rows[i].cards[0];
            CommandCard volley = commandCards.get(guardedFirst.getOrDefault(first, first));
            if (volley == null || volley.before == null) break;
            through[0] = new CheckApproach(volley.before, trigger);
            checkApproaches.put(card, through[0]);
            break;
        }
        return planned(plan, card);
    }

    /**
     * A CELL check's plan: the volley it guards. A check is a wait whose one trigger row starts
     * with a volley: "wait for the CELL to be up, then launch; otherwise park" (the Visualizer's
     * Wait with an Otherwise that parks: the trigger row is its own {@link #guarded} list, whose
     * first card is the volley, and it is its list's last card), or the bare volley alone with no
     * cards on its time limits ("fire if the CELL is up, else go on"). The guard's look-ahead
     * counts the check as that volley, as if the check passed at once, so a trip toward a checked
     * volley that would leave no time to park is not begun, exactly as toward a bare one. A check
     * that spins up while it waits ("while SpinUp") is a check all the same: what runs alongside is
     * stopped when a row fires. Null for any other block. When the check has waited, the volley
     * asks again as it starts, and does not start if the park no longer fits after it (see
     * {@link #firstOf(String, Command, Row...)}); the drawing still keeps the check's own time limit
     * early enough that a volley starting at the limit leaves the park its time.
     */
    private Plan checkPlan(Row[] rows) {
        Plan volley = null;
        boolean guardedRow = false;
        boolean limited = false;
        for (Row row : rows) {
            if (row.timeLimit) {
                limited = true;
                continue;
            }
            if (volley != null || row.cards.length != 1) return null;
            volley = plans.get(row.cards[0]);
            if (volley == null) {
                volley = guardedVolleys.get(row.cards[0]);
                guardedRow = volley != null;
            }
            if (volley == null || !volley.isVolley()) return null;
        }
        if (volley == null || !limited) return null;
        // A time limit's cards run with the guard still "going for the volley" unless they are a
        // guarded list of their own: the bare form allows none.
        for (Row row : rows) {
            if (!row.timeLimit || row.cards.length == 0) continue;
            if (!guardedRow || row.cards.length != 1 || !guards.contains(row.cards[0])) return null;
        }
        return volley;
    }

    /**
     * A plain wait's plan: the earliest of its time limits. Null (no plan) for a wait with no time
     * limit, with cards in a row or with a command alongside: what it takes is not known ahead.
     */
    private static Plan waitPlan(Command alongside, Row[] rows) {
        if (alongside != null) return null;
        boolean limited = false;
        for (Row row : rows) {
            if (row.cards.length > 0) return null;
            limited |= row.timeLimit;
        }
        if (!limited) return null;
        return new Plan(startsAt -> {
            double longest = Double.POSITIVE_INFINITY;
            for (Row row : rows) if (row.timeLimit) longest = Math.min(longest, row.longestFrom.applyAsDouble(startsAt));
            return longest;
        }, Double.NaN);
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
     * starts still keeps a path from starting once the park no longer fits.
     *
     * <p><b>First, each card asks: can this, and every card up to the next volley, finish with
     * that volley's last piece out before AUTO ends?</b> A volley ({@link AutoRegistry#volley}: a
     * LaunchAll) is what TIPs the HIVE, and an AUTO TIP is worth 20 points and counts toward the
     * POLLINATOR RPs, the AUTO PARK 5 points. AUTO PARK counts toward the SWARM RP (both robots'
     * LEAVE and PARK points, AUTO and end of match, 16 of 26: manual TU10 Tables 10-2, 10-3, the same at the State Championship by Team Update 10), but
     * both robots' LEAVE and end-of-match PARK already make 16, so ours decides SWARM only when the
     * partner misses one of those. A TIP complete before TELEOP starts is assessed as AUTO
     * (§10.5 B); the same section says achievements scored during the AUTO-to-TELEOP transition
     * "may be subject to penalties", an open question, so the guard asks only that the volley's
     * last piece be out by 30 s and plans nothing that happens after (mentor, 4 Nov 2026, quals
     * and playoffs alike). The look-ahead sums each card's plan from where it
     * starts: an exported drive's time, a command's or grab's typical time, a plain wait's earliest
     * time limit, then the volley's last-piece time; a card it cannot time (a block, a drive with no
     * time, a wait with a command alongside or no limit) ends it with no. If yes, that card goes and
     * nothing parks or cuts it, whatever the park needs.
     *
     * <p><b>On {@link AutoOption#PARK_FIRST}, the park comes too.</b> The drive team's
     * {@link AutoOption#ENDGAME} row, on every Auto, decides this (mentor, 22 Nov 2026; before, it was
     * {@code BuiltAuto.parkAfterVolley}, on since 6 Nov 2026 and off on 20245 from 21 Nov). PARK FIRST,
     * the default, adds the park after the volley's last piece, with no margin (the robot parks from
     * the spot it fired on, along a path drawn from there; mentor, 22 Nov 2026: the whole card plus a
     * 1 s margin refused third volleys that fitted with their park by 0.2 to 0.5 s); it goes for the
     * volley only if that ends by 30 s, and if the volley is
     * found but that does not fit, the guard parks now, before the card starts: a trip (a drive, a
     * grab) toward a volley that would leave no time to park is never begun, since once there its
     * drive-in may open the gate and a feeding launch is never cut. A chain the look-ahead cannot
     * time falls to the park rule below, as before. It asks again at the next card, so a chain
     * that ran long falls back to the park rule below. The robot ends AUTO wherever the volley left
     * it, a launch spot off the walls, so LEAVE is kept: the grabs the chain runs end, at their
     * typical times, before the volley's last piece does.
     *
     * <p><b>Otherwise each drive and grab asks before it starts: can I do this, and then still park from
     * where it leaves me?</b> An exported drive knows its time ({@link #path(String, Path, double)})
     * and the park from the spot it goes to, so it does not set off unless both fit; a grab adds
     * its typical time to the park from here. One that does not fit does not start, and the guard
     * parks at once from the spot the robot is on, rather than run the cards after it from the
     * wrong spot (mentor, 29 Oct 2026). A drive with no time given ({@link #path(String, Path)})
     * only checks the park from here, so one started just before the deadline runs past it.
     *
     * <p><b>The same rules hold inside a block.</b> A {@link #firstOf} whose row runs its own cards
     * (L-Quals and the Sister Autos are mostly that) is one card to the guard, so it goes by what is
     * running inside: it cuts the block at the deadline while a wait or a command inside it runs,
     * never while a path inside it drives, and a path inside it that does not fit does not start:
     * it finishes at once, the block ends, and the guard parks.
     *
     * <p><b>A volley behind a CELL check counts as a volley</b> (12 Nov 2026). Every volley after an
     * Auto's first sits behind a check, the Visualizer's "Wait for CELL up, Otherwise park" (or,
     * inside an Otherwise that goes on, "Otherwise skip"), so that none goes at a down CELL
     * (Belding Q-14, M-7). The look-ahead counts the check as its volley ({@link #checkPlan}), so
     * the rules above hold for it unchanged, though it is its list's last card: each of its
     * outcomes is a guarded list of its own, which parks. A volley deeper in a block, which the
     * look-ahead cannot see, asks for itself as it starts, on {@link AutoOption#PARK_FIRST}: if its
     * last piece out and the park from here do not fit, it does not fire, the block ends and the
     * guard parks.
     *
     * <p><b>Grabs are treated as paths.</b> A grab ({@link AutoRegistry#grab}: a FLOWER pick, a
     * GARDEN gather) drives the robot off its spot and back, so the guard never cuts one either.
     *
     * <p><b>A launch that is feeding is never cut</b> (the registry holds time limits while it
     * feeds: {@link AutoRegistry#holdTimeLimitsWhile}): its pieces are already on their way. The
     * guard parks once it ends.
     *
     * <p><b>It parks from the spot the robot is on.</b> A path card that carries a park
     * ({@link #path(String, Path, double, Path, double)}) makes that park the guard's from when it
     * arrives, with that park's own time, until a path card without one arrives somewhere else. That
     * holds across a guard's start: a guard entered on such a spot (each outcome of a Wait in the
     * Visualizer's "then: park" form is its own guard) parks from it, not along its own park, until
     * the robot drives away (4 Nov 2026: R-Solo's TIP check cut a volley 0.9 s early before).
     * Since drives and grabs are never cut, the robot is on that spot whenever the guard parks, so
     * it drives a park drawn from where it is.
     *
     * <p>The last card is assumed to be the park itself and is never skipped or cut short.
     *
     * <p>History: until 1 Oct 2026 the guard checked only between cards. A "wait for Tip, at most
     * 7000 ms" started at 20 s then ran to 27 s before the guard looked, a 3.3 s park no longer
     * fit, and the robot lost AUTO PARK; so the guard began to cut the running card, whatever it
     * was. Since 18 Oct it cuts waits and commands, never paths. Since 27 Oct that holds inside a
     * block too (#142): before, the guard knew only its own top-level path cards, so it cut a block
     * mid-drive and let a block's next path start after the deadline. Since 4 Nov 2026 a volley that
     * fits comes before the park, and a feeding launch is never cut: before, the guard cut a launch
     * as soon as the park needed the time, and a drive refused to set off for a volley that would
     * have finished in time, to keep a 5-point park.
     */
    public Command guarded(String label, Path parkPath, double parkSeconds, Command... given) {
        final Command[] cards = onRoute(given);
        final Command park = traced("path " + label + " park", follow(label + " park", parkPath, false, null, null, null));
        pairApproaches(cards);
        final int[] index = new int[1];
        final Command[] current = new Command[1];
        final Command[] parking = new Command[1];
        final CommandBuilder card = new CommandBuilder();
        card.setStart(() -> {
            index[0] = 0;
            current[0] = null;
            parking[0] = null;
            // spotPark is kept: it is the park from the spot the robot last arrived at, whether or
            // not a guard was watching then. A block entered on that spot (a Wait's outcome in the
            // "then: park" form, each its own guard) parks from it until the robot drives away.
            parkWanted = false;
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
            // A launch that is feeding (time limits held) is never cut: its pieces are on their way.
            boolean cuttable = current[0] != parking[0] && !last && !drivingPath && !grabbing
                    && !registry.timeLimitsHeld();
            // A drive or grab inside a block that refused: the block's next cards would run from
            // the wrong spot, so the block ends here and the guard parks.
            if (cuttable && (parkWanted || tooLateToContinue(label, parkSecondsNow(parkSeconds), "cutting card short"))) {
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
        guards.add(card);
        if (cards.length > 0) guardedFirst.put(card, cards[0]);
        Plan first = cards.length > 0 ? plans.get(cards[0]) : null;
        if (first != null && first.isVolley()) guardedVolleys.put(card, first);
        return card;
    }

    private void watchFromHere(String label, double parkSeconds) {
        guardLabel = label;
        guardParkSeconds = spotPark != null ? spotParkSeconds : parkSeconds;
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
        goingForVolley = false;
    }

    /**
     * Before {@code cards[from]} starts: whether it and every card after it up to the next volley,
     * at their planned times, then that volley's last piece, are done before AUTO ends. The last
     * card is the park and is never looked at, unless it is a CELL check ({@link #checkPlan}: a
     * wait that parks either way), which counts as its volley; a card with no {@link Plan} ends
     * the look-ahead with no. Sets {@link #goingForVolley} until the next card starts.
     */
    private void goForVolleyIfItFits(Command[] cards, int from) {
        Command target = null;
        goingForVolley = false;
        volleyLeavesNoPark = null;
        volleyParkedAt = Double.NaN;
        double now = secondsSinceStart.getAsDouble();
        double at = now;
        int end = cards.length > 0 && checks.contains(cards[cards.length - 1]) ? cards.length : cards.length - 1;
        for (int i = from; i < end; i++) {
            Plan plan = plans.get(cards[i]);
            if (plan == null) break;
            if (plan.isVolley()) {
                volleyLastPieceAt = at + plan.lastPieceS;
                goingForVolley = volleyLastPieceAt <= AUTO_LENGTH_S;
                // A volley fired where the robot has parked has its park behind it, not after it.
                // On PARK FIRST the park after it must fit too: up to the volley's last piece out, then
                // the park, no margin (mentor, 22 Nov 2026: the whole card plus 1 s refused third
                // volleys that fit with their park by 0.2 to 0.5 s). The robot parks from the spot it
                // fired on, along a path drawn from there, which is what the margin was for.
                if (parkComesFirst() && !plan.parked && !Double.isNaN(ownParkSeconds)) {
                    double parkedAt = at + plan.lastPieceS + ownParkSeconds;
                    volleyParkedAt = parkedAt;
                    if (parkedAt > AUTO_LENGTH_S) {
                        goingForVolley = false;
                        volleyLeavesNoPark = String.format(Locale.US,
                                "%.1f s of cards to the next volley, its last piece out %.1f s in, and the park (%.1f s) end at %.1f s",
                                at - now, plan.lastPieceS, ownParkSeconds, parkedAt);
                    }
                }
                if (goingForVolley) target = cards[i];
                break;
            }
            double seconds = plan.secondsFrom.applyAsDouble(at);
            if (!(seconds >= 0) || Double.isInfinite(seconds)) break;
            at += seconds;
        }
        // Traced once per volley gone for, however many cards lead up to it.
        if (target != volleyTarget) goingForVolleyTraced = false;
        volleyTarget = target;
    }

    private void startPark(Command park, Command[] current, Command[] parking) {
        stopWatching();
        // The card the last drive got ready for is not going to run (a gate opened on the way in).
        cancelArmed();
        parkWanted = false;
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
        if (parkWanted) {
            trace.accept(label + ": the next drive or grab did not fit with its park: parking now");
            startPark(parkNow(park), current, parking);
            return;
        }
        goForVolleyIfItFits(cards, index[0]);
        if (!last && volleyLeavesNoPark != null) {
            if (guardOn) {
                trace.accept(label + ": " + volleyLeavesNoPark + ": no time to park after it, parking now");
                startPark(parkNow(park), current, parking);
                return;
            }
            if (!guardWouldHaveActed) {
                guardWouldHaveActed = true;
                trace.accept(label + ": guard OFF: " + volleyLeavesNoPark + ": would have parked now");
            }
        }
        if (!last && tooLateToContinue(label, parkSecondsNow(parkSeconds), "parking now")) {
            startPark(parkNow(park), current, parking);
            return;
        }
        if (last) stopWatching();
        current[0] = cards[index[0]];
        current[0].start();
    }

    /**
     * On {@link AutoOption#PARK_FIRST}, for a volley the look-ahead could not see (deeper in a block):
     * true, and traced, when its last piece out ({@code lastPieceS} after {@code startedAt}) and then
     * the park ({@code parkS}) end after AUTO, the look-ahead's own budget; with the guard off, false,
     * and what it would have done traced once.
     */
    private boolean noParkAfterVolley(String label, double startedAt, double lastPieceS, double parkS, String name) {
        double parkedAt = startedAt + lastPieceS + parkS;
        if (parkedAt <= AUTO_LENGTH_S) return false;
        String why = String.format(Locale.US, "its last piece out %.1f s in and the park (%.1f s) end at %.1f s",
                lastPieceS, parkS, parkedAt);
        if (!guardOn) {
            if (!guardWouldHaveActed) {
                guardWouldHaveActed = true;
                trace.accept(label + ": guard OFF: " + why + ": would not have started " + name);
            }
            return false;
        }
        trace.accept(label + ": " + why + ": not starting " + name + ", no time to park after it");
        return true;
    }

    /**
     * True, and traced, when the time left no longer covers the park plus {@link #GUARD_MARGIN_S},
     * unless the guard is going for the next volley ({@link #goingForVolley}): then false, and
     * traced once, since that volley's last piece is out before AUTO ends.
     */
    private boolean tooLateToContinue(String label, double parkSeconds, String action) {
        double left = timeLeft();
        double needed = parkSeconds + GUARD_MARGIN_S;
        if (left >= needed) return false;
        if (goingForVolley) {
            if (!goingForVolleyTraced) {
                goingForVolleyTraced = true;
                trace.accept(String.format(Locale.US,
                        "%s: %.1f s left, park needs %.1f s, but the next volley's last piece is out at %.1f s: going for it, not parking",
                        label, left, needed, volleyLastPieceAt));
            }
            return false;
        }
        if (!guardOn) {
            if (!guardWouldHaveActed) {
                guardWouldHaveActed = true;
                trace.accept(String.format(Locale.US, "%s: guard OFF: %.1f s left, park needs %.1f s: would have been %s",
                        label, left, needed, action));
            }
            return false;
        }
        trace.accept(String.format(Locale.US, "%s: %.1f s left, park needs %.1f s: %s", label, left, needed, action));
        return true;
    }

    // ------------------------------------------------------------------- rows

    /** True when the registered trigger is; watched from the moment the card starts waiting. */
    public Row when(String trigger) {
        registry.requireTrigger(trigger);
        final BooleanSupplier[] check = new BooleanSupplier[1];
        Row row = new Row(trigger, waited -> check[0].getAsBoolean());
        row.trigger = trigger;
        // Taken when the wait starts, so a "since" trigger means this wait.
        row.start = () -> check[0] = registry.watch(trigger);
        return row;
    }

    /**
     * True once {@code ms} have passed since the card started, and time limits are not held
     * ({@link AutoRegistry#holdTimeLimitsWhile}): a launch that has started feeding finishes first.
     */
    public Row afterMs(double ms) {
        Row row = new Row(String.format(Locale.US, "%.0f ms passed", ms), waited -> waited * 1000 >= ms);
        row.timeLimit = true;
        row.longestFrom = startsAt -> Math.max(0.0, ms / 1000.0);
        return row;
    }

    /**
     * True once the match clock reaches {@code seconds} (at once if it already has), and time
     * limits are not held, like {@link #afterMs}. A time limit that does not depend on when the
     * robot got there: "give up on the TIP at 11 s", whenever the drive before the wait ended.
     */
    public Row atMatchSeconds(double seconds) {
        Row row = new Row(String.format(Locale.US, "%.1f s on the match clock", seconds),
                waited -> secondsSinceStart.getAsDouble() >= seconds);
        row.timeLimit = true;
        row.longestFrom = startsAt -> Math.max(0.0, seconds - startsAt);
        return row;
    }

    /**
     * The "yes" row of a stop that asks "is this CELL up by this time?", for a robot that shoots by
     * the camera or by the clock ({@link #shotsOnTimer}). By the camera it is {@link #when}: true
     * when the registered trigger is. On the timer the robot cannot see the HIVE, so the row is
     * true once the match clock reaches {@code seconds}: the robot shoots at its time. Pair it with
     * {@link #shotLimit} on the same seconds, written first, for the "no" row.
     */
    public Row shotWhen(String trigger, double seconds) {
        registry.requireTrigger(trigger);
        if (!shotsOnTimer()) return when(trigger);
        Row row = new Row(String.format(Locale.US, "%s taken as true at %.1f s (shots on the timer)", trigger, seconds),
                waited -> secondsSinceStart.getAsDouble() >= seconds);
        row.longestFrom = startsAt -> Math.max(0.0, seconds - startsAt);
        return row;
    }

    /**
     * The "no" row of the same stop: {@link #atMatchSeconds} by the camera (the CELL was not seen up
     * in time), and never true on the timer, where the robot follows the "yes" row only.
     */
    public Row shotLimit(double seconds) {
        if (!shotsOnTimer()) return atMatchSeconds(seconds);
        return new Row(String.format(Locale.US, "no limit: shots on the timer (%.1f s)", seconds), waited -> false);
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
     * Drives {@code path} to its end: done when {@link AutoDrive#pathDone} says so, Pedro's path end,
     * with the robot still rolling (see the class comment). With {@code guardable}, a card that
     * starts while a guard is watching and the park no longer fits does not drive at all: it
     * finishes at once, untouched, and the guard parks on its next check. That is how a path inside
     * a block's row is kept from starting late, where the guard itself sees only the block.
     *
     * <p>With a {@code card} that {@link #pairApproaches} gave an approach, the command card after
     * this one gets ready on the way: told where the path ends as it starts, then every loop where
     * the robot is and how far it has to go (one square root, no allocation); cancelled if the path
     * is cut short, and armed for the command card if it ends ({@link #armed}). A park path has no
     * card: nothing follows a park.
     */
    private Command follow(String label, Path path, boolean guardable, DoubleSupplier needed, Runnable onArrived,
                           PathCard card) {
        if (knownPaths.add(path)) allPaths.add(path);
        final Pose endPose = path.endPose();
        final boolean[] refused = new boolean[1];
        return new CommandBuilder()
                .setStart(() -> {
                    refused[0] = guardable && !Double.isNaN(guardParkSeconds)
                            && tooLateToContinue(guardLabel, needed.getAsDouble(), "not starting " + label);
                    if (refused[0]) parkWanted = true;
                    if (refused[0]) return;
                    // An approach from an earlier drive whose command never started: not this one's.
                    cancelArmed();
                    drive.follow(path);
                    drivingPath = true;
                    watcher.pathStarted(path);
                    if (card != null && card.approach != null) card.approach.started(endPose);
                })
                .setExecute(() -> {
                    if (refused[0] || card == null || card.approach == null) return;
                    Pose pose = drive.pose();
                    if (pose == null) return;
                    card.approach.approaching(pose, Math.hypot(endPose.x() - pose.x(), endPose.y() - pose.y()));
                })
                .setDone(() -> refused[0] || drive.pathDone())
                .setEnd(end -> {
                    if (refused[0]) return;
                    drivingPath = false;
                    watcher.pathEnded(path);
                    if (card != null && card.approach != null) {
                        if (end == EndCondition.NATURALLY) armed = card.approach;
                        else card.approach.cancel();
                    }
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
