package org.firstinspires.ftc.teamcode.autokit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.pedropathing.api.Paths;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.ivy.commands.Commands;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs cards through Ivy's real {@link Scheduler}, one simulated 20 ms loop at a time, against a
 * fake drivetrain that moves a fixed fraction of the current path each loop.
 */
public class AutoKitTest {

    private static final double LOOP_S = 0.02;

    private final List<String> log = new ArrayList<>();
    private final List<String> trace = new ArrayList<>();
    private FakeDrive drive;
    private double now;
    private boolean tipped;
    private boolean blind;
    private boolean full;
    private AutoKit kit;

    @Before
    public void setUp() {
        Scheduler.reset();
        drive = new FakeDrive();
        now = 0;
        AutoRegistry registry = new AutoRegistry()
                .command("ShootAll", 1.0, () -> record("ShootAll"))
                .command("SpinUp", 1.0, () -> record("SpinUp"))
                .command("IntakeOn", 1.0, () -> record("IntakeOn"))
                .command("IntakeOff", 1.0, () -> record("IntakeOff"))
                .trigger("HiveTipped", () -> tipped)
                .trigger("CameraBlind", () -> blind)
                .trigger("IntakeFull", () -> full);
        kit = new AutoKit(drive, registry, () -> now).trace(trace::add);
    }

    private Command record(String name) {
        return Commands.instant(() -> log.add(name));
    }

    /** Runs loops until the command ends or {@code maxSeconds} of simulated time have passed. */
    private void run(Command command, double maxSeconds) {
        command.schedule();
        double end = now + maxSeconds;
        while (now < end) {
            Scheduler.execute();
            drive.tick();
            if (!command.isScheduled()) return;
            now += LOOP_S;
        }
        fail("Still running after " + maxSeconds + " s. Trace: " + trace);
    }

    private static Path line(double x0, double x1) {
        return Paths.line(new Pose(x0, 0, 0), new Pose(x1, 0, 0)).tangent();
    }

    // -------------------------------------------------------------- paths

    /** What the match log hangs off: every path once, and each start and end as it happens. */
    @Test
    public void pathsAreListedOnceEachAndTheWatcherSeesEveryStartAndEnd() {
        Path a = line(0, 10), b = line(10, 20), park = line(20, 30);
        List<String> seen = new ArrayList<>();
        kit.watchPaths(new AutoKit.PathWatcher() {
            @Override public void pathStarted(Path path) { seen.add("start " + name(path)); }
            @Override public void pathEnded(Path path) { seen.add("end " + name(path)); }

            private String name(Path path) {
                return path == a ? "a" : path == b ? "b" : path == park ? "park" : "?";
            }
        });
        Command auto = kit.guarded("Auto", park, 1.0, kit.path("A", a), kit.path("B", b), kit.path("park", park));
        // The cards are built (left to right) before guarded() itself sees the park path again.
        assertEquals("every path once, in the order the cards were built",
                java.util.Arrays.asList(a, b, park), kit.paths());
        assertTrue(seen.isEmpty());
        run(auto, 10);
        assertEquals(java.util.Arrays.asList("start a", "end a", "start b", "end b", "start park", "end park"), seen);
    }

    // ------------------------------------------------------------ first of

    /** A command that runs for {@code seconds} of simulated time, logging how it ended. */
    private Command lasting(String name, double seconds) {
        final double[] startedAt = new double[1];
        return new com.pedropathing.ivy.CommandBuilder()
                .setStart(() -> {
                    startedAt[0] = now;
                    log.add(name + " start");
                })
                .setDone(() -> now - startedAt[0] >= seconds)
                .setEnd(end -> log.add(name + " " + end));
    }

    @Test
    public void aWaitWhileACommandRunsStopsTheCommandWhenTheTriggerFires() {
        AutoKit kit = new AutoKit(drive, new AutoRegistry()
                .command("LaunchAll", 3.0, () -> lasting("LaunchAll", 3.0))
                .command("ShootAll", 0.1, () -> record("ShootAll"))
                .trigger("HiveTipped", () -> now >= 1.0), () -> now).trace(trace::add);
        run(kit.sequence(
                kit.firstOf("Launch and watch", kit.command("LaunchAll"),
                        kit.when("HiveTipped").then(kit.command("ShootAll")),
                        kit.afterMs(4000))), 5);
        assertTrue(log.toString(), log.contains("LaunchAll start"));
        assertTrue(log.toString(), log.contains("LaunchAll INTERRUPTED"));
        assertFalse(log.toString(), log.contains("LaunchAll NATURALLY"));
        assertTrue(log.toString(), log.indexOf("LaunchAll INTERRUPTED") < log.indexOf("ShootAll"));
        assertTrue(trace.toString(), trace.stream().anyMatch(line -> line.contains("HiveTipped after 1.0")));
    }

    /** Mentor, 1 Nov 2026: a step's "at most" guards the wait for spin-up; it never cuts a feed. */
    @Test
    public void anAtMostRowWaitsWhileTimeLimitsAreHeld() {
        // Feeding from the start until the 3 s launch ends.
        AutoKit kit = new AutoKit(drive, new AutoRegistry()
                .holdTimeLimitsWhile(() -> now < 3.0)
                .command("LaunchAll", 3.0, () -> lasting("LaunchAll", 3.0)), () -> now).trace(trace::add);
        run(kit.firstOf("Fire", kit.command("LaunchAll"), kit.afterMs(1000)), 5);
        assertTrue("the feed ran to its end: " + log, log.contains("LaunchAll NATURALLY"));
        assertFalse(log.toString(), log.contains("LaunchAll INTERRUPTED"));
        assertTrue(trace.toString(), trace.stream().anyMatch(line -> line.contains("1000 ms passed after 3.")));
    }

    @Test
    public void aCommandTimeoutWaitsWhileTimeLimitsAreHeld() {
        AutoKit kit = new AutoKit(drive, new AutoRegistry()
                .holdTimeLimitsWhile(() -> now < 3.0)
                .command("LaunchAll", 3.0, () -> lasting("LaunchAll", 3.0)), () -> now).trace(trace::add);
        run(kit.command("LaunchAll", 1.0), 5);
        assertTrue("not cut at 1 s while feeding: " + log, log.contains("LaunchAll NATURALLY"));
    }

    @Test
    public void withNothingHeldAnAtMostRowStillCutsTheCommand() {
        AutoKit kit = new AutoKit(drive, new AutoRegistry()
                .holdTimeLimitsWhile(() -> false)
                .command("LaunchAll", 3.0, () -> lasting("LaunchAll", 3.0)), () -> now).trace(trace::add);
        run(kit.firstOf("Fire", kit.command("LaunchAll"), kit.afterMs(1000)), 5);
        assertTrue("cut at the limit: " + log, log.contains("LaunchAll INTERRUPTED"));
    }

    @Test
    public void aWaitGoesOnAfterItsCommandFinishes() {
        AutoKit kit = new AutoKit(drive, new AutoRegistry()
                .command("LaunchAll", 1.0, () -> lasting("LaunchAll", 1.0))
                .trigger("HiveTipped", () -> now >= 2.0), () -> now).trace(trace::add);
        run(kit.firstOf("Launch and watch", kit.command("LaunchAll"),
                kit.when("HiveTipped"),
                kit.afterMs(4000)), 5);
        assertTrue(log.toString(), log.contains("LaunchAll NATURALLY"));
        assertTrue(trace.toString(), trace.stream().anyMatch(line -> line.contains("HiveTipped after 2.0")));
    }

    @Test
    public void aSinceTriggerIsWatchedFromWhenTheWaitStarts() {
        // Something happens at 0.2 s (before either wait) and again at 1.5 s.
        AutoKit kit = new AutoKit(drive, new AutoRegistry()
                .command("ShootAll", 0.1, () -> record("ShootAll"))
                .triggerSince("Tip", () -> {
                    int before = happenings();
                    return () -> happenings() > before;
                }), () -> now).trace(trace::add);
        now = 0.5;
        run(kit.firstOf("First wait", kit.when("Tip").then(kit.command("ShootAll")), kit.afterMs(500)), 2);
        assertFalse("what happened before the wait must not count: " + log, log.contains("ShootAll"));
        run(kit.firstOf("Second wait", kit.when("Tip").then(kit.command("ShootAll")), kit.afterMs(2000)), 3);
        assertTrue("what happened during the wait counts: " + log, log.contains("ShootAll"));
        assertTrue(trace.toString(), trace.stream().anyMatch(line -> line.startsWith("Second wait: Tip")));
    }

    private int happenings() {
        return (now >= 0.2 ? 1 : 0) + (now >= 1.5 ? 1 : 0);
    }

    @Test
    public void decisionTakesTheConditionRowWhenItComesTrueFirst() {
        Command auto = kit.firstOf("Did the HIVE tip?",
                kit.when("HiveTipped").then(kit.command("ShootAll")),
                kit.afterMs(1500).then(kit.command("SpinUp")));
        tipped = true;
        run(auto, 5);
        assertEquals("[ShootAll]", log.toString());
        assertTrue(trace.toString(), trace.contains("Did the HIVE tip?: HiveTipped after 0.00 s"));
    }

    @Test
    public void decisionTakesTheTimeRowWhenNothingHappens() {
        Command auto = kit.firstOf("Did the HIVE tip?",
                kit.when("HiveTipped").then(kit.command("ShootAll")),
                kit.afterMs(1500).then(kit.command("SpinUp")));
        run(auto, 5);
        assertEquals("[SpinUp]", log.toString());
        assertTrue(now >= 1.5 && now < 1.6);
    }

    /** "Give up at 8.5 s": however late the wait starts, it ends at that time on the match clock. */
    @Test
    public void aMatchClockLimitEndsAtThatTimeWhenTheWaitStarted() {
        now = 6.0;
        run(kit.firstOf("Wait for TIP", kit.when("HiveTipped"),
                kit.atMatchSeconds(8.5).then(kit.command("SpinUp"))), 5);
        assertEquals("[SpinUp]", log.toString());
        assertTrue("ended at " + now, now >= 8.5 && now < 8.6);
        assertTrue(trace.toString(), trace.stream().anyMatch(line -> line.startsWith("Wait for TIP: 8.5 s on the match clock after 2.5")));
    }

    /** Arriving after the limit: the wait ends at once, and the time row's cards run. */
    @Test
    public void aMatchClockLimitAlreadyPastEndsTheWaitAtOnce() {
        now = 9.0;
        run(kit.firstOf("Wait for TIP", kit.when("HiveTipped"),
                kit.atMatchSeconds(8.5).then(kit.command("SpinUp"))), 1);
        assertEquals("[SpinUp]", log.toString());
        assertTrue(now < 9.1);
    }

    @Test
    public void theTriggerStillEndsAMatchClockWaitEarly() {
        tipped = true;
        run(kit.firstOf("Wait for TIP", kit.when("HiveTipped").then(kit.command("ShootAll")),
                kit.atMatchSeconds(20)), 1);
        assertEquals("[ShootAll]", log.toString());
    }

    @Test
    public void withNoPicksEveryOptionIsAsDrawn() {
        assertEquals("LEFT", kit.pick(AutoOption.PARK, "LEFT", "RIGHT"));
        assertEquals(8.5, kit.seconds("Wait for TIP", 8.5), 1e-9);
    }

    @Test
    public void picksChooseTheValueAndTheTime() {
        kit.picks(new AutoKit.Picks() {
            @Override public int choice(String option) { return option.equals(AutoOption.PARK) ? 1 : -1; }
            @Override public double seconds(String option) { return option.equals("Wait for TIP") ? 11 : Double.NaN; }
        });
        assertEquals("RIGHT", kit.pick(AutoOption.PARK, "LEFT", "RIGHT"));
        assertEquals("unknown options stay as drawn", "A", kit.pick("Other", "A", "B"));
        assertEquals(11, kit.seconds("Wait for TIP", 8.5), 1e-9);
        assertEquals(3, kit.seconds("Other", 3), 1e-9);
    }

    @Test
    public void earlierRowWinsWhenTwoAreTrueInTheSameLoop() {
        tipped = true;
        run(kit.firstOf("both", kit.when("HiveTipped").then(kit.command("ShootAll")),
                kit.afterMs(0).then(kit.command("SpinUp"))), 1);
        assertEquals("[ShootAll]", log.toString());
    }

    @Test
    public void aWaitWithoutCardsCarriesOnAfterItsRowFires() {
        Command auto = kit.sequence(
                kit.firstOf("Wait for IntakeFull", kit.when("IntakeFull"), kit.afterMs(800)),
                kit.command("ShootAll"));
        run(auto, 3);
        assertEquals("[ShootAll]", log.toString());
        assertTrue(now >= 0.8);
    }

    // ----------------------------------------------------------- paths

    // ----------------------------------------------------------- endgame guard

    @Test
    public void guardParksBeforeACardWhenTheTimeLeftDoesNotCoverTheParkPath() {
        Path park = line(0, 10);
        now = 27.6; // 2.4 s left; park needs 2 + the margin
        run(kit.guarded("If tipped", park, 2.0,
                kit.command("ShootAll"),
                kit.path("Park", park)), 5);
        String needs = String.format(java.util.Locale.ROOT, "park needs %.1f s: parking now", 2.0 + AutoKit.GUARD_MARGIN_S);
        assertTrue(trace.toString(), trace.stream().anyMatch(t -> t.endsWith(needs)));
        assertFalse("ShootAll must be skipped", log.contains("ShootAll"));
        assertEquals(java.util.Collections.singletonList(park), drive.followed);
    }

    /** Practice runs (BuiltAuto.endgameGuard off): every card runs, and the trace says once where it would have parked. */
    @Test
    public void guardOffRunsEveryCardAndTracesWhereItWouldHaveParked() {
        Path park = line(0, 10);
        Path drawnPark = line(0, 20);
        now = 27.6; // 2.4 s left; the guard on would park at once
        run(kit.guard(false).guarded("If tipped", park, 2.0,
                kit.command("ShootAll"),
                kit.path("Park", drawnPark)), 5);
        assertTrue("ShootAll must run: " + log, log.contains("ShootAll"));
        assertEquals(java.util.Collections.singletonList(drawnPark), drive.followed);
        assertEquals(trace.toString(), 1, trace.stream().filter(t -> t.contains("guard OFF")).count());
    }

    @Test
    public void guardCutsARunningCardShortAtTheDeadlineAndParks() {
        AutoKit kit = new AutoKit(drive, new AutoRegistry()
                .command("LaunchAll", 10.0, () -> lasting("LaunchAll", 10.0))
                .command("ShootAll", 0.1, () -> record("ShootAll"))
                .trigger("HiveTipped", () -> false), () -> now).trace(trace::add);
        Path park = line(0, 10);
        double deadline = AutoKit.AUTO_LENGTH_S - (2.0 + AutoKit.GUARD_MARGIN_S); // 27.0 s
        now = deadline - 5; // the wait would run 5 s past the deadline
        run(kit.guarded("If tipped", park, 2.0,
                kit.firstOf("Wait for Tip", kit.command("LaunchAll", 10.0),
                        kit.when("HiveTipped"),
                        kit.afterMs(10000)),
                kit.command("ShootAll"),
                kit.path("Park", park)), 10);
        assertTrue(trace.toString(), trace.stream().anyMatch(t -> t.endsWith("cutting card short")));
        assertTrue("the running card is interrupted: " + log, log.contains("LaunchAll INTERRUPTED"));
        assertFalse("the card after it is skipped: " + log, log.contains("ShootAll"));
        assertEquals(java.util.Collections.singletonList(park), drive.followed);
        assertEquals("the park starts at the deadline", deadline, drive.followedAt.get(0), 0.03);
    }

    @Test
    public void guardNeverCutsARunningPathButParksAsSoonAsItEnds() {
        Path leg = line(0, 20);
        Path park = line(20, 30);
        drive.stepPerLoop = 0.01; // the leg takes 2 s (100 loops)
        double deadline = AutoKit.AUTO_LENGTH_S - (2.0 + AutoKit.GUARD_MARGIN_S); // 27.0 s
        now = deadline - 1; // the leg starts with 1 s in hand and runs 1 s past the deadline
        run(kit.guarded("Auto", park, 2.0,
                kit.path("Leg", leg),
                kit.command("ShootAll"),
                kit.path("Park", park)), 10);
        assertTrue("no card was cut: " + trace, trace.stream().noneMatch(t -> t.endsWith("cutting card short")));
        assertEquals("the leg, then the park", java.util.Arrays.asList(leg, park), drive.followed);
        assertEquals("the park starts when the leg ends, not at the deadline",
                deadline + 1, drive.followedAt.get(1), 0.05);
        assertFalse("the card after the leg is skipped: " + log, log.contains("ShootAll"));
        assertTrue(trace.toString(), trace.stream().anyMatch(t -> t.endsWith("parking now")));
    }

    // ------------------------------------------------- park from the spot, grabs

    /** A spot's own park path: the guard parks along it, and its shorter time lets the robot work longer. */
    @Test
    public void guardParksFromTheSpotTheRobotIsOn() {
        AutoKit kit = new AutoKit(drive, new AutoRegistry()
                .command("LaunchAll", 10.0, () -> lasting("LaunchAll", 10.0)), () -> now).trace(trace::add);
        Path toSpot = line(0, 10), spotPark = line(10, 12), park = line(10, 30);
        now = 20.0;
        run(kit.guarded("Auto", park, 4.0,
                kit.path("to SPOT", toSpot, spotPark, 1.0),
                kit.command("LaunchAll", 10.0),
                kit.path("Park", park)), 15);
        assertEquals("the spot's own park, not the guard's", java.util.Arrays.asList(toSpot, spotPark), drive.followed);
        assertTrue(trace.toString(), trace.contains("parking from the spot the robot is on"));
        double deadline = AutoKit.AUTO_LENGTH_S - (1.0 + AutoKit.GUARD_MARGIN_S); // 28 s, not 25
        assertEquals("the launch runs until the spot's park needs the time", deadline, drive.followedAt.get(1), 0.03);
    }

    /**
     * The Visualizer's "then: park" Wait: each outcome is its own guard, entered on the spot the
     * drive before the wait arrived at. That spot's park (1 s) must survive entering the block, so
     * the launch runs until it needs the time (28 s), not the block's own 4 s park (25 s).
     */
    @Test
    public void aGuardEnteredOnASpotParksFromThatSpot() {
        AutoKit kit = new AutoKit(drive, new AutoRegistry()
                .command("LaunchAll", 10.0, () -> lasting("LaunchAll", 10.0))
                .trigger("CellUp", () -> true), () -> now).trace(trace::add);
        Path toSpot = line(0, 10), spotPark = line(10, 12), park = line(10, 30);
        now = 20.0;
        run(kit.sequence(
                kit.path("to SPOT", toSpot, 1.0, spotPark, 1.0),
                kit.firstOf("Wait for TIP or CELL up",
                        kit.when("CellUp").then(
                                kit.guarded("If CellUp", park, 4.0,
                                        kit.command("LaunchAll", 10.0),
                                        kit.path("Park", park))),
                        kit.afterMs(750))), 15);
        assertEquals("the spot's own park, not the block's", java.util.Arrays.asList(toSpot, spotPark), drive.followed);
        assertTrue(trace.toString(), trace.contains("parking from the spot the robot is on"));
        double deadline = AutoKit.AUTO_LENGTH_S - (1.0 + AutoKit.GUARD_MARGIN_S); // 28 s, not 25
        assertEquals("the launch runs until the spot's park needs the time", deadline, drive.followedAt.get(1), 0.03);
    }

    /** Somewhere no park was drawn from: the guard's own park path again. */
    @Test
    public void aDriveWithNoParkOfItsOwnGoesBackToTheGuardsPark() {
        AutoKit kit = new AutoKit(drive, new AutoRegistry()
                .command("LaunchAll", 10.0, () -> lasting("LaunchAll", 10.0)), () -> now).trace(trace::add);
        Path toSpot = line(0, 10), spotPark = line(10, 12), onward = line(10, 20), park = line(20, 30);
        now = 20.0;
        run(kit.guarded("Auto", park, 2.0,
                kit.path("to SPOT", toSpot, spotPark, 1.0),
                kit.path("onward", onward),
                kit.command("LaunchAll", 10.0),
                kit.path("Park", park)), 15);
        assertEquals(java.util.Arrays.asList(toSpot, onward, park), drive.followed);
    }

    /**
     * Before it sets off, a drive asks: can I get there and still park from there? 5 s left: the
     * guard's own park (1 s + margin) fits, the drive (3 s) and the park from its spot (2 s) do
     * not, so it does not start, and the guard parks at once rather than launch from here.
     */
    @Test
    public void aDriveThatWouldNotLeaveTimeToParkFromWhereItGoesDoesNotStart() {
        AutoKit kit = new AutoKit(drive, new AutoRegistry()
                .command("LaunchAll", 1.0, () -> lasting("LaunchAll", 1.0)), () -> now).trace(trace::add);
        Path toSpot = line(0, 10), spotPark = line(10, 12), park = line(0, 30);
        now = 25.0;
        run(kit.guarded("Auto", park, 1.0,
                kit.path("to SPOT", toSpot, 3.0, spotPark, 2.0),
                kit.command("LaunchAll", 2.0),
                kit.path("Park", park)), 10);
        assertEquals(java.util.Collections.singletonList(park), drive.followed);
        assertTrue(trace.toString(), trace.stream().anyMatch(t -> t.endsWith("not starting to SPOT")));
        assertFalse("nothing runs after a refused drive: " + log, log.contains("LaunchAll start"));
        assertEquals("parks at once", 25.0, drive.followedAt.get(0), 0.05);
    }

    /** The same question with the guard's own park: 5 s left, a 2 s drive and its 1 s park fit. */
    @Test
    public void aDriveThatFitsWithTheParkAfterItStarts() {
        AutoKit kit = new AutoKit(drive, new AutoRegistry(), () -> now).trace(trace::add);
        Path onward = line(0, 10), park = line(10, 30);
        now = 25.0;
        run(kit.guarded("Auto", park, 1.0,
                kit.path("onward", onward, 2.0),
                kit.path("Park", park)), 10);
        assertEquals(java.util.Arrays.asList(onward, park), drive.followed);
    }

    /** A grab that does not fit parks at once too, rather than run the next card from here. */
    @Test
    public void aRefusedGrabParksAtOnce() {
        AutoKit kit = new AutoKit(drive, new AutoRegistry()
                .grab("PickFlower", 4.0, () -> lasting("PickFlower", 4.0))
                .command("LaunchAll", 1.0, () -> lasting("LaunchAll", 1.0)), () -> now).trace(trace::add);
        Path park = line(0, 10);
        now = 23.5;
        run(kit.guarded("Auto", park, 2.0,
                kit.command("PickFlower", 8.0),
                kit.command("LaunchAll", 2.0),
                kit.path("Park", park)), 10);
        assertFalse("nothing runs after a refused grab: " + log, log.contains("LaunchAll start"));
        assertEquals(java.util.Collections.singletonList(park), drive.followed);
    }

    /** A grab drives the robot off its spot and back: cutting one would strand the robot. */
    @Test
    public void guardNeverCutsAGrab() {
        AutoKit kit = new AutoKit(drive, new AutoRegistry()
                .grab("PickFlower", 1.0, () -> lasting("PickFlower", 3.0)), () -> now).trace(trace::add);
        Path park = line(0, 10);
        double deadline = AutoKit.AUTO_LENGTH_S - (2.0 + AutoKit.GUARD_MARGIN_S); // 27.0 s
        now = deadline - 1.5; // starts with its typical 1 s and the park in hand, runs 1.5 s past
        run(kit.guarded("Auto", park, 2.0,
                kit.command("PickFlower", 8.0),
                kit.path("Park", park)), 10);
        assertTrue("the grab ran to its end: " + log, log.contains("PickFlower NATURALLY"));
        assertTrue(trace.toString(), trace.stream().noneMatch(t -> t.endsWith("cutting card short")));
        assertEquals("the park starts when the grab ends", deadline + 1.5, drive.followedAt.get(0), 0.05);
    }

    /** Not started unless the grab's typical time and the park both fit. */
    @Test
    public void aGrabThatWouldNotLeaveTimeToParkDoesNotStart() {
        AutoKit kit = new AutoKit(drive, new AutoRegistry()
                .grab("GardenGather", 4.0, () -> lasting("GardenGather", 4.0)), () -> now).trace(trace::add);
        Path park = line(0, 10);
        now = 23.5; // 6.5 s left: the park alone fits (3 s), the gather and the park (7 s) do not
        run(kit.guarded("Auto", park, 2.0,
                kit.command("GardenGather", 8.0),
                kit.path("Park", park)), 10);
        assertFalse("the gather must not start: " + log, log.contains("GardenGather start"));
        assertTrue(trace.toString(), trace.stream().anyMatch(t -> t.endsWith("not starting GardenGather")));
        assertEquals(java.util.Collections.singletonList(park), drive.followed);
    }

    // ------------------------------------------- a volley before the park (4 Nov 2026)

    /** A kit whose LaunchAll is a volley: 4.5 s long, its last piece out 4.0 s in. */
    private AutoKit volleyKit(AutoRegistry registry) {
        return new AutoKit(drive, registry
                .volley("LaunchAll", 4.5, 4.0, () -> lasting("LaunchAll", 4.5)), () -> now).trace(trace::add);
    }

    private boolean traced(String ending) {
        return trace.stream().anyMatch(t -> t.endsWith(ending));
    }

    /**
     * 5 s left: the drive (1 s) and the park from its spot (3.5 s + margin) do not fit, so the old
     * rule would park at once; the drive and the volley's last piece (4 s) do. A TIP is worth more
     * than the park: it goes, fires the whole volley, and no park cuts it.
     */
    @Test
    public void aDriveGoesForTheVolleyWhenItsLastPieceIsOutBeforeTheEnd() {
        AutoKit kit = volleyKit(new AutoRegistry());
        Path toSpot = line(0, 10), spotPark = line(10, 12), park = line(10, 30);
        now = 25.0;
        run(kit.guarded("Auto", park, 1.0,
                kit.path("to SPOT", toSpot, 1.0, spotPark, 3.5),
                kit.command("LaunchAll"),
                kit.path("Park", park)), 10);
        assertTrue(trace.toString(), traced("going for it, not parking"));
        assertFalse(trace.toString(), traced("not starting to SPOT"));
        assertTrue("the whole volley: " + log, log.contains("LaunchAll NATURALLY"));
        assertTrue(trace.toString(), trace.stream().noneMatch(t -> t.endsWith("cutting card short")));
        assertEquals("the drive, then the last card", java.util.Arrays.asList(toSpot, park), drive.followed);
    }

    /** 4.5 s left: the drive and the last piece (5 s) do not fit either, so it parks as before. */
    @Test
    public void aDriveWhoseVolleyWouldBeLateParksAsBefore() {
        AutoKit kit = volleyKit(new AutoRegistry());
        Path toSpot = line(0, 10), spotPark = line(10, 12), park = line(0, 30);
        now = 25.5;
        run(kit.guarded("Auto", park, 1.0,
                kit.path("to SPOT", toSpot, 1.0, spotPark, 3.5),
                kit.command("LaunchAll"),
                kit.path("Park", park)), 10);
        assertTrue(trace.toString(), traced("not starting to SPOT"));
        assertFalse(trace.toString(), traced("going for it, not parking"));
        assertFalse(log.toString(), log.contains("LaunchAll start"));
        assertEquals(java.util.Collections.singletonList(park), drive.followed);
    }

    /** A volley that has started with its last piece in time is not cut for the park. */
    @Test
    public void aVolleyWhoseLastPieceFitsIsNotCutForThePark() {
        AutoKit kit = volleyKit(new AutoRegistry());
        Path park = line(0, 10);
        now = 25.5; // last piece out at 29.5; the park (2 s + margin) would cut it at 27.0
        run(kit.guarded("Auto", park, 2.0,
                kit.command("LaunchAll"),
                kit.path("Park", park)), 10);
        assertTrue("the whole volley: " + log, log.contains("LaunchAll NATURALLY"));
        assertTrue(trace.toString(), traced("going for it, not parking"));
        assertEquals("the park is the last card, after the volley", 30.0, drive.followedAt.get(0), 0.05);
    }

    /** One whose last piece would be out after 30 s is cut for the park, as before. */
    @Test
    public void aVolleyWhoseLastPieceIsTooLateIsCutForThePark() {
        AutoKit kit = volleyKit(new AutoRegistry());
        Path park = line(0, 10);
        now = 26.5; // last piece out at 30.5
        run(kit.guarded("Auto", park, 2.0,
                kit.command("LaunchAll"),
                kit.path("Park", park)), 10);
        assertTrue(log.toString(), log.contains("LaunchAll INTERRUPTED"));
        assertTrue(trace.toString(), traced("cutting card short"));
        assertEquals("the park starts at the deadline", 27.0, drive.followedAt.get(0), 0.03);
    }

    /** A launch that is feeding (time limits held) is never cut, volley or not: its pieces are on their way. */
    @Test
    public void aFeedingLaunchIsNeverCut() {
        AutoKit kit = new AutoKit(drive, new AutoRegistry()
                .holdTimeLimitsWhile(() -> now >= 26.5 - 1e-9)
                .command("LaunchAll", 3.0, () -> lasting("LaunchAll", 3.0)), () -> now).trace(trace::add);
        Path park = line(0, 10);
        now = 26.0; // feeding from 26.5; the park's deadline is 27.0
        run(kit.guarded("Auto", park, 2.0,
                kit.command("LaunchAll"),
                kit.path("Park", park)), 10);
        assertTrue("the feed ran to its end: " + log, log.contains("LaunchAll NATURALLY"));
        assertTrue(trace.toString(), trace.stream().noneMatch(t -> t.endsWith("cutting card short")));
        assertEquals("parks when the launch ends", 29.0, drive.followedAt.get(0), 0.05);
    }

    /**
     * The look-ahead adds every card up to the volley: a grab (3 s typical), a drive (1 s) and a
     * wait's time limit (1 s), then the last piece (4 s): 9 s. With 9 s left it goes, though the
     * grab and the guard's 5.5 s park (+ margin) would not fit.
     */
    @Test
    public void theLookAheadAddsEveryCardUpToTheVolley() {
        AutoKit kit = volleyKit(new AutoRegistry()
                .grab("PickFlower", 3.0, () -> lasting("PickFlower", 1.0))
                .trigger("CellUp", () -> false));
        Path toSpot = line(0, 10), park = line(10, 30);
        now = 21.0;
        run(kit.guarded("Auto", park, 5.5,
                kit.command("PickFlower", 8.0),
                kit.path("to SPOT", toSpot, 1.0),
                kit.firstOf("Wait for TIP or CELL up", kit.when("CellUp"), kit.afterMs(1000)),
                kit.command("LaunchAll"),
                kit.path("Park", park)), 15);
        assertTrue(log.toString(), log.contains("PickFlower NATURALLY"));
        assertTrue(log.toString(), log.contains("LaunchAll NATURALLY"));
        assertFalse(trace.toString(), traced("not starting PickFlower"));
        assertEquals(java.util.Arrays.asList(toSpot, park), drive.followed);
    }

    /** The same cards with 8.9 s left: 9 s does not fit, and the grab and park do not either: it parks. */
    @Test
    public void theLookAheadRefusesWhenTheSumIsJustOver() {
        AutoKit kit = volleyKit(new AutoRegistry()
                .grab("PickFlower", 3.0, () -> lasting("PickFlower", 1.0))
                .trigger("CellUp", () -> false));
        Path toSpot = line(0, 10), park = line(10, 30);
        now = 21.1;
        run(kit.guarded("Auto", park, 5.5,
                kit.command("PickFlower", 8.0),
                kit.path("to SPOT", toSpot, 1.0),
                kit.firstOf("Wait for TIP or CELL up", kit.when("CellUp"), kit.afterMs(1000)),
                kit.command("LaunchAll"),
                kit.path("Park", park)), 15);
        assertTrue(trace.toString(), traced("not starting PickFlower"));
        assertFalse(log.toString(), log.contains("PickFlower start"));
        assertEquals(java.util.Collections.singletonList(park), drive.followed);
    }

    /** A card the guard cannot time (a drive with no time given) ends the look-ahead: the park rule decides. */
    @Test
    public void aCardWithNoTimeEndsTheLookAhead() {
        AutoKit kit = volleyKit(new AutoRegistry());
        Path toSpot = line(0, 10), park = line(0, 30);
        now = 25.5; // 4.5 s left: the volley alone fits, the guard's 4 s park (+ margin) does not
        run(kit.guarded("Auto", park, 4.0,
                kit.path("to SPOT", toSpot),
                kit.command("LaunchAll"),
                kit.path("Park", park)), 10);
        assertFalse(trace.toString(), traced("going for it, not parking"));
        assertEquals(java.util.Collections.singletonList(park), drive.followed);
    }

    /** Practice runs (guard off) still run every card, and say once where the guard would have parked. */
    @Test
    public void guardOffStillRunsEveryCard() {
        AutoKit kit = volleyKit(new AutoRegistry()).guard(false);
        Path toSpot = line(0, 10), spotPark = line(10, 12), park = line(10, 30);
        now = 25.5;
        run(kit.guarded("Auto", park, 1.0,
                kit.path("to SPOT", toSpot, 1.0, spotPark, 3.5),
                kit.command("LaunchAll"),
                kit.path("Park", park)), 10);
        assertTrue(log.toString(), log.contains("LaunchAll NATURALLY"));
        assertEquals(java.util.Arrays.asList(toSpot, park), drive.followed);
        assertEquals(trace.toString(), 1, trace.stream().filter(t -> t.contains("guard OFF")).count());
    }

    // ------------------------------------------------------------ approaches

    /** A grab's approach that records what the kit tells it. */
    private static final class RecordingApproach implements AutoRegistry.Approach {
        final List<String> seen = new ArrayList<>();
        double lowestRemaining = Double.NaN;

        @Override public void started(Pose end) {
            seen.add(String.format(java.util.Locale.US, "started at (%.0f, %.0f)", end.x(), end.y()));
            lowestRemaining = Double.NaN;
        }

        @Override public void approaching(Pose pose, double remainingIn) {
            if (Double.isNaN(lowestRemaining) || remainingIn < lowestRemaining) lowestRemaining = remainingIn;
            if (seen.size() == 1) seen.add("approaching");
        }

        @Override public void cancel() {
            seen.add("cancelled");
        }
    }

    /**
     * The path card right before a grab card runs the grab's approach while it drives: told where the
     * path ends as it starts, then where the robot is every loop it drives. A path with no grab after
     * it runs none, and the grab itself starts the loop after the path ends, as before.
     */
    @Test
    public void aPathCardBeforeAGrabRunsTheGrabsApproachWhileItDrives() {
        RecordingApproach approach = new RecordingApproach();
        AutoKit kit = new AutoKit(drive, new AutoRegistry()
                .grab("PickFlower", 1.0, () -> lasting("PickFlower", 0.5), approach)
                .command("LaunchAll", 1.0, () -> lasting("LaunchAll", 0.5)), () -> now).trace(trace::add);
        Path toSpot = line(0, 20), onward = line(20, 40);
        run(kit.sequence(
                kit.path("to SPOT", toSpot),
                kit.command("PickFlower", 8.0),
                kit.path("onward", onward),
                kit.command("LaunchAll")), 10);
        assertEquals(java.util.Arrays.asList("started at (20, 0)", "approaching"), approach.seen);
        // Ivy asks done() before execute(), so the last call is the loop before the path ends: one
        // fake step (a fifth of the 20 in) short; one 20 ms loop on the robot.
        assertEquals("fed every loop until the path ends", 4.0, approach.lowestRemaining, 1e-9);
        assertEquals("the pick starts when the path ends, as before", 1.0, drive.progressWhen.get("PickFlower start"), 1e-9);
    }

    /** A pair inside a firstOf row pairs too: the rows' cards are a card list like any other. */
    @Test
    public void aPathCardBeforeAGrabInsideARowRunsTheApproach() {
        RecordingApproach approach = new RecordingApproach();
        AutoKit kit = new AutoKit(drive, new AutoRegistry()
                .grab("PickFlower", 1.0, () -> lasting("PickFlower", 0.5), approach)
                .trigger("HiveTipped", () -> true), () -> now).trace(trace::add);
        run(kit.firstOf("Tipped?", kit.when("HiveTipped").then(
                kit.path("to SPOT", line(0, 20)),
                kit.command("PickFlower", 8.0))), 10);
        assertEquals(java.util.Arrays.asList("started at (20, 0)", "approaching"), approach.seen);
    }

    /** Only a grab right after a path gets an approach; a command between them, or no approach registered, means none. */
    @Test
    public void onlyAGrabRightAfterAPathGetsItsApproachRun() {
        RecordingApproach approach = new RecordingApproach();
        AutoKit kit = new AutoKit(drive, new AutoRegistry()
                .grab("PickFlower", 1.0, () -> lasting("PickFlower", 0.5), approach)
                .grab("GardenGather", 1.0, () -> lasting("GardenGather", 0.5))
                .command("SpinUp", 0.1, () -> record("SpinUp")), () -> now).trace(trace::add);
        run(kit.sequence(
                kit.path("to SPOT", line(0, 20)),
                kit.command("SpinUp"),
                kit.command("PickFlower", 8.0),
                kit.path("to GARDEN", line(20, 40)),
                kit.command("GardenGather", 8.0)), 10);
        assertTrue("nothing told the approach: " + approach.seen, approach.seen.isEmpty());
    }

    /**
     * The guard refuses the grab after the drive (it and the park no longer fit): the stick the
     * drive lowered must come back up, so the approach is cancelled, and the guard parks.
     */
    @Test
    public void aRefusedGrabCancelsTheApproachTheDriveRan() {
        RecordingApproach approach = new RecordingApproach();
        AutoKit kit = new AutoKit(drive, new AutoRegistry()
                .grab("PickFlower", 4.0, () -> lasting("PickFlower", 4.0), approach), () -> now).trace(trace::add);
        Path toSpot = line(0, 10), park = line(10, 20);
        now = 23.5; // 6.5 s left: the 1 s drive and its park fit; the 4 s grab and the park (6 s + margin) do not
        run(kit.guarded("Auto", park, 2.0,
                kit.path("to SPOT", toSpot, 1.0),
                kit.command("PickFlower", 8.0),
                kit.path("Park", park)), 10);
        assertEquals(java.util.Arrays.asList("started at (10, 0)", "approaching", "cancelled"), approach.seen);
        assertFalse("the grab did not start: " + log, log.contains("PickFlower start"));
        assertEquals(java.util.Arrays.asList(toSpot, park), drive.followed);
    }

    /**
     * Not only grabs: a plain command with an approach (LaunchAll's opens the gate on the way in,
     * mentor 4 Nov 2026) gets it run by the path card before it, and the command starts the loop
     * after the path ends, as before. Its approach is not cancelled when the command takes over.
     */
    @Test
    public void aPathCardBeforeAnyCommandWithAnApproachRunsIt() {
        RecordingApproach approach = new RecordingApproach();
        AutoKit kit = new AutoKit(drive, new AutoRegistry()
                .command("LaunchAll", 1.0, () -> lasting("LaunchAll", 0.5), approach), () -> now).trace(trace::add);
        run(kit.sequence(
                kit.path("to LAUNCH", line(0, 20)),
                kit.command("LaunchAll")), 10);
        assertEquals(java.util.Arrays.asList("started at (20, 0)", "approaching"), approach.seen);
        assertEquals("the launch starts when the path ends", 1.0, drive.progressWhen.get("LaunchAll start"), 1e-9);
    }

    /**
     * The guard parks after the drive instead of starting the command it got ready for: a gate the
     * approach opened must close, so the approach is cancelled. Before 4 Nov 2026 only a refused grab
     * cancelled; a stick lowered for a pick the guard skipped stayed down.
     */
    @Test
    public void theGuardParkingBeforeTheCommandCancelsItsApproach() {
        RecordingApproach approach = new RecordingApproach();
        AutoKit kit = new AutoKit(drive, new AutoRegistry()
                .command("LaunchAll", 1.0, () -> lasting("LaunchAll", 0.5), approach), () -> now).trace(trace::add);
        Path toSpot = line(0, 10), park = line(10, 20);
        now = 26.99; // the drive and the 2 s park fit as it starts; once it ends, the park no longer does
        run(kit.guarded("Auto", park, 2.0,
                kit.path("to LAUNCH", toSpot, 0.0),
                kit.command("LaunchAll"),
                kit.path("Park", park)), 10);
        assertEquals(java.util.Arrays.asList("started at (10, 0)", "approaching", "cancelled"), approach.seen);
        assertFalse("the launch did not start: " + log, log.contains("LaunchAll start"));
        assertEquals(java.util.Arrays.asList(toSpot, park), drive.followed);
    }

    /**
     * #142, inside a block: L-Quals runs most of its Auto as the cards of one firstOf row. A path in
     * the row that is driving at the deadline runs to its end; the row's next path, which would
     * start after the deadline, does not start; the guard parks from where the first one ended.
     */
    @Test
    public void guardNeverCutsAPathInsideABlockAndStartsNoPathThereAfterTheDeadline() {
        Path leg = line(0, 20);
        Path next = line(20, 40);
        Path park = line(20, 30);
        drive.stepPerLoop = 0.01; // a path takes 2 s (100 loops)
        double deadline = AutoKit.AUTO_LENGTH_S - (2.0 + AutoKit.GUARD_MARGIN_S); // 27.0 s
        now = deadline - 1; // the leg starts with 1 s in hand and runs 1 s past the deadline
        tipped = true;
        run(kit.guarded("Auto", park, 2.0,
                kit.firstOf("Left CELL up?",
                        kit.when("HiveTipped").then(
                                kit.path("Leg", leg),
                                kit.path("Next", next),
                                kit.command("ShootAll")),
                        kit.afterMs(7000)),
                kit.path("Park", park)), 10);
        assertEquals("the leg, then the park; never the row's next path", java.util.Arrays.asList(leg, park), drive.followed);
        assertEquals("the park starts when the leg ends", deadline + 1, drive.followedAt.get(1), 0.1);
        assertTrue(trace.toString(), trace.stream().anyMatch(t -> t.endsWith("not starting Next")));
        assertFalse("the row's command is skipped: " + log, log.contains("ShootAll"));
    }

    /** #142, inside a block: a wait in a row's cards is cut at the deadline, as at the top level. */
    @Test
    public void guardCutsAWaitInsideABlockAtTheDeadline() {
        Path park = line(0, 10);
        double deadline = AutoKit.AUTO_LENGTH_S - (2.0 + AutoKit.GUARD_MARGIN_S); // 27.0 s
        now = deadline - 5;
        tipped = true;
        run(kit.guarded("Auto", park, 2.0,
                kit.firstOf("Tipped?",
                        kit.when("HiveTipped").then(
                                kit.firstOf("Catch", kit.when("IntakeFull"), kit.afterMs(10000)),
                                kit.command("ShootAll")),
                        kit.afterMs(7000)),
                kit.path("Park", park)), 10);
        assertTrue(trace.toString(), trace.stream().anyMatch(t -> t.endsWith("cutting card short")));
        assertEquals(java.util.Collections.singletonList(park), drive.followed);
        assertEquals("the park starts at the deadline", deadline, drive.followedAt.get(0), 0.03);
        assertFalse(log.toString(), log.contains("ShootAll"));
    }

    @Test
    public void guardNeverCutsTheParkCardShort() {
        Path park = line(0, 10);
        drive.stepPerLoop = 0.001; // a park that would take 20 s
        now = 27; // 3 s left; park needs 2.5 s
        run(kit.guarded("If tipped", park, 2.0, kit.path("Park", park)), 25);
        assertTrue(trace.toString(), trace.stream().noneMatch(t -> t.contains("park needs")));
        assertEquals(1, drive.followed.size());
    }

    @Test
    public void guardRunsEveryCardWhenThereIsTime() {
        Path park = line(0, 10);
        run(kit.guarded("If tipped", park, 2.0, kit.command("ShootAll"), kit.command("SpinUp"),
                kit.path("Park", park)), 5);
        assertEquals("[ShootAll, SpinUp]", log.toString());
        assertEquals(1, drive.followed.size());
    }

    // ----------------------------------------------------------- go-to

    // ----------------------------------------------------------- routine

    // ----------------------------------------------------------- registry

    @Test
    public void registryNamesEveryMissingNameAtOnce() {
        AutoRegistry registry = new AutoRegistry().command("ShootAll", 1.0, () -> record("ShootAll"));
        try {
            registry.requireAll(new String[] {"ShootAll", "SpinUp"}, new String[] {"HiveTipped"});
            fail();
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("command SpinUp, trigger HiveTipped"));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void registryRefusesADuplicateName() {
        new AutoRegistry().command("A", 1.0, () -> record("A")).command("A", 1.0, () -> record("A"));
    }

    @Test
    public void aCommandThatNeverFinishesIsCutOffAtItsTimeout() {
        AutoRegistry registry = new AutoRegistry().command("Stuck", 3.0, () -> Commands.waitUntil(() -> false));
        AutoKit stuck = new AutoKit(drive, registry, () -> now).trace(trace::add);
        run(stuck.command("Stuck", 2.0), 10);
        assertTrue(trace.toString(), trace.contains("command Stuck timed out after 2.0 s"));
        assertEquals(2.0, now, 0.05);
    }

    @Test
    public void aCommandWithoutATimeoutGetsFiveSeconds() {
        AutoRegistry registry = new AutoRegistry().command("Stuck", 3.0, () -> Commands.waitUntil(() -> false));
        AutoKit stuck = new AutoKit(drive, registry, () -> now).trace(trace::add);
        run(stuck.command("Stuck"), 10);
        assertEquals(AutoKit.DEFAULT_TIMEOUT_S, now, 0.05);
    }

    @Test
    public void aCommandThatFinishesIsNotReportedAsTimedOut() {
        run(kit.command("ShootAll", 2.0), 5);
        assertEquals(java.util.Collections.singletonList("ShootAll"), log);
        assertTrue(trace.toString(), trace.stream().noneMatch(line -> line.contains("timed out")));
    }

    @Test
    public void theRegistryDescribesItselfForTheEditor() {
        String json = new AutoRegistry()
                .command("LaunchAll", 3.0, () -> record("LaunchAll"))
                .command("IntakeOn", 0.2, () -> record("IntakeOn"))
                .trigger("IntakeFull", () -> full)
                .describe();
        assertEquals("{\n  \"commands\": [\n"
                + "    {\"name\": \"LaunchAll\", \"typicalS\": 3.0},\n"
                + "    {\"name\": \"IntakeOn\", \"typicalS\": 0.2}\n  ],\n"
                + "  \"triggers\": [\n    \"IntakeFull\"\n  ]\n}\n", json);
    }

    // ----------------------------------------------------------- geometry

    // ----------------------------------------------------------- fake

    /** Moves {@link #stepPerLoop} of the current path each loop; the pose follows the path. */
    private final class FakeDrive implements AutoDrive {
        double stepPerLoop = 0.2;
        double capProgress = 1.0;
        boolean referenced = true;
        final List<Path> followed = new ArrayList<>();
        final List<Double> followedAt = new ArrayList<>();
        final java.util.Map<String, Double> progressWhen = new java.util.HashMap<>();
        private Path current;
        private double progress = 1;
        private Pose pose = new Pose(0, 0, 0);

        void tick() {
            if (current == null) return;
            if (progress < capProgress) progress = Math.min(capProgress, progress + stepPerLoop);
            pose = current.get(Math.min(1, progress));
            for (String name : log) progressWhen.putIfAbsent(name, progress);
        }

        @Override public void follow(Path path) {
            current = path;
            progress = 0;
            followed.add(path);
            followedAt.add(now);
        }

        @Override public boolean pathDone() {
            return current == null || progress >= capProgress;
        }

        @Override public Pose pose() {
            return pose;
        }

        @Override public boolean poseReferenced() {
            return referenced;
        }

        @Override public void hold(Pose pose) {
            current = null;
        }
    }
}
