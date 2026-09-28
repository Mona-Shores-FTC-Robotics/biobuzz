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
                .action("ShootAll", () -> record("ShootAll"))
                .action("SpinUp", () -> record("SpinUp"))
                .action("IntakeOn", () -> record("IntakeOn"))
                .action("IntakeOff", () -> record("IntakeOff"))
                .condition("HiveTipped", () -> tipped)
                .condition("CameraBlind", () -> blind)
                .condition("IntakeFull", () -> full);
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

    @Test
    public void decisionTakesTheConditionRowWhenItComesTrueFirst() {
        Command auto = kit.firstOf("Did the HIVE tip?",
                kit.when("HiveTipped", "CameraBlind").then(kit.action("ShootAll")),
                kit.afterMs(1500).then(kit.action("SpinUp")));
        blind = true; // either condition in the OR is enough
        run(auto, 5);
        assertEquals("[ShootAll]", log.toString());
        assertTrue(trace.toString(), trace.contains("Did the HIVE tip?: HiveTipped or CameraBlind after 0.00 s"));
    }

    @Test
    public void decisionTakesTheTimeRowWhenNothingHappens() {
        Command auto = kit.firstOf("Did the HIVE tip?",
                kit.when("HiveTipped").then(kit.action("ShootAll")),
                kit.afterMs(1500).then(kit.action("SpinUp")));
        run(auto, 5);
        assertEquals("[SpinUp]", log.toString());
        assertTrue(now >= 1.5 && now < 1.6);
    }

    @Test
    public void earlierRowWinsWhenTwoAreTrueInTheSameLoop() {
        tipped = true;
        run(kit.firstOf("both", kit.when("HiveTipped").then(kit.action("ShootAll")),
                kit.otherwise().then(kit.action("SpinUp"))), 1);
        assertEquals("[ShootAll]", log.toString());
    }

    @Test
    public void aWaitWithoutCardsCarriesOnAfterItsRowFires() {
        Command auto = kit.sequence(
                kit.firstOf("Wait for IntakeFull", kit.when("IntakeFull"), kit.afterMs(800)),
                kit.action("ShootAll"));
        run(auto, 3);
        assertEquals("[ShootAll]", log.toString());
        assertTrue(now >= 0.8);
    }

    @Test
    public void timeLeftRowBailsOutNearTheEndOfAuto() {
        now = 23;
        run(kit.firstOf("Second cycle?",
                kit.timeLeftBelow(8).then(kit.action("SpinUp")),
                kit.otherwise().then(kit.action("ShootAll"))), 1);
        assertEquals("[SpinUp]", log.toString());
    }

    // ----------------------------------------------------------- paths

    @Test
    public void pathEventsFireAtTheirFractionAndWhileActionsStartWithThePath() {
        drive.stepPerLoop = 0.1;
        run(kit.path("Collect", line(0, 40), new String[] {"SpinUp"}, AutoKit.at(0.6, "IntakeOn")), 3);
        assertEquals("[SpinUp, IntakeOn]", log.toString());
        assertEquals(0.6, drive.progressWhen.get("IntakeOn"), 0.11);
    }

    @Test
    public void eventsBeyondTheEndOfAPathThatFinishesEarlyAreDropped() {
        drive.stepPerLoop = 1.0;
        drive.capProgress = 0.5;  // the path "ends" at half way, e.g. cut short
        run(kit.path("Short", line(0, 40), new String[0], AutoKit.at(0.9, "IntakeOn")), 3);
        assertEquals("[]", log.toString());
    }

    // ----------------------------------------------------------- endgame guard

    @Test
    public void guardParksWhenTheTimeLeftDoesNotCoverTheParkPath() {
        Path park = line(0, 10);
        now = 27; // 3 s left; park needs 2 + 0.5
        run(kit.guarded("If tipped", park, 2.0,
                kit.firstOf("slow", kit.afterMs(2000)),
                kit.action("ShootAll"),
                kit.path("Park", park)), 5);
        // 3 s left covers the first card; after its 2 s wait, 1 s does not cover the park.
        assertTrue(trace.toString(), trace.stream().anyMatch(t -> t.endsWith("park needs 2.5 s: parking now")));
        assertFalse("ShootAll must be skipped", log.contains("ShootAll"));
        assertEquals(park, drive.followed.get(drive.followed.size() - 1));
    }

    @Test
    public void guardRunsEveryCardWhenThereIsTime() {
        Path park = line(0, 10);
        run(kit.guarded("If tipped", park, 2.0, kit.action("ShootAll"), kit.action("SpinUp"),
                kit.path("Park", park)), 5);
        assertEquals("[ShootAll, SpinUp]", log.toString());
        assertEquals(1, drive.followed.size());
    }

    // ----------------------------------------------------------- go-to

    @Test
    public void goToRefusesWithoutAFieldReferencedPose() {
        drive.referenced = false;
        run(kit.goTo("Back", new Pose(10, 0, 0), 36, kit.action("SpinUp")), 1);
        assertEquals("[SpinUp]", log.toString());
        assertTrue(drive.followed.isEmpty());
    }

    @Test
    public void goToRefusesALineThatCrossesAKeepOut() {
        kit.keepOut(new Pose(10, -5, 0), new Pose(20, -5, 0), new Pose(20, 5, 0), new Pose(10, 5, 0));
        run(kit.goTo("Across", new Pose(30, 0, 0), 36, null), 1);
        assertTrue(drive.followed.isEmpty());
        assertTrue(trace.toString(), trace.contains("go-to Across refused: the line crosses a keep-out"));
    }

    @Test
    public void goToRefusesALineOverTheDistanceCap() {
        run(kit.goTo("Far", new Pose(100, 0, 0), 36, null), 1);
        assertTrue(drive.followed.isEmpty());
    }

    @Test
    public void goToDrivesAStraightLineWhenAllowed() {
        drive.stepPerLoop = 0.25;
        run(kit.goTo("Back", new Pose(10, 10, 0), 36, null), 2);
        assertEquals(1, drive.followed.size());
        Pose end = drive.followed.get(0).endPose();
        assertEquals(10, end.x(), 1e-9);
        assertEquals(10, end.y(), 1e-9);
    }

    // ----------------------------------------------------------- routine

    @Test
    public void routineStopsWhenItsConditionComesTrueThenExitsToItsPoint() {
        drive.stepPerLoop = 0.05;
        Command routine = kit.routine("CollectFar", line(0, 40), "IntakeFull", 10_000,
                new String[] {"IntakeOn"}, new String[] {"IntakeOff"}, new Pose(0, 30, 0));
        routine.schedule();
        for (int i = 0; i < 6; i++) { Scheduler.execute(); drive.tick(); now += LOOP_S; }
        full = true;
        while (routine.isScheduled() && now < 10) { Scheduler.execute(); drive.tick(); now += LOOP_S; }
        assertFalse(routine.isScheduled());
        assertEquals("[IntakeOn, IntakeOff]", log.toString());
        assertEquals(2, drive.followed.size());
        Pose exitEnd = drive.followed.get(1).endPose();
        assertEquals(0, exitEnd.x(), 1e-9);
        assertEquals(30, exitEnd.y(), 1e-9);
        assertTrue(trace.toString(), trace.stream().anyMatch(t -> t.startsWith("CollectFar: IntakeFull after")));
    }

    // ----------------------------------------------------------- registry

    @Test
    public void registryNamesEveryMissingNameAtOnce() {
        AutoRegistry registry = new AutoRegistry().action("ShootAll", () -> record("ShootAll"));
        try {
            registry.requireAll(new String[] {"ShootAll", "SpinUp"}, new String[] {"HiveTipped"});
            fail();
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("action SpinUp, condition HiveTipped"));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void registryRefusesADuplicateName() {
        new AutoRegistry().action("A", () -> record("A")).action("A", () -> record("A"));
    }

    // ----------------------------------------------------------- geometry

    @Test
    public void progressCountsTheSegmentsAlreadyDriven() {
        Path compound = Paths.path(line(0, 10), line(10, 40));
        assertEquals(0.25, Geometry.progress(compound, 1, new Pose(10, 0, 0)), 1e-6);
        assertEquals(0.625, Geometry.progress(compound, 1, new Pose(25, 0, 0)), 1e-6);
        assertEquals(1.0, Geometry.progress(compound, 2, new Pose(40, 0, 0)), 1e-6);
    }

    @Test
    public void lineNearAPolygonCountsAsAHitWithinTheMargin() {
        Pose[] box = {new Pose(0, 0, 0), new Pose(10, 0, 0), new Pose(10, 10, 0), new Pose(0, 10, 0)};
        assertTrue(Geometry.lineHitsPolygon(new Pose(-5, 15, 0), new Pose(15, 15, 0), box, 9));
        assertFalse(Geometry.lineHitsPolygon(new Pose(-5, 20, 0), new Pose(15, 20, 0), box, 9));
    }

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

        @Override public double pathProgress() {
            return current == null ? 1 : progress;
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
