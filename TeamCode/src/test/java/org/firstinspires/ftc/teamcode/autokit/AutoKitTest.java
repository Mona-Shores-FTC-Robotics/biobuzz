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
    public void guardParksWhenTheTimeLeftDoesNotCoverTheParkPath() {
        Path park = line(0, 10);
        now = 27; // 3 s left; park needs 2 + 0.5
        run(kit.guarded("If tipped", park, 2.0,
                kit.firstOf("slow", kit.afterMs(2000)),
                kit.command("ShootAll"),
                kit.path("Park", park)), 5);
        // 3 s left covers the first card; after its 2 s wait, 1 s does not cover the park.
        assertTrue(trace.toString(), trace.stream().anyMatch(t -> t.endsWith("park needs 2.5 s: parking now")));
        assertFalse("ShootAll must be skipped", log.contains("ShootAll"));
        assertEquals(park, drive.followed.get(drive.followed.size() - 1));
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
