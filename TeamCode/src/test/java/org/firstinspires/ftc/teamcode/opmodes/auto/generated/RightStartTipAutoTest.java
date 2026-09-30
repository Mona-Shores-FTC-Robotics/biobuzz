package org.firstinspires.ftc.teamcode.opmodes.auto.generated;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.CommandBuilder;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;

import org.firstinspires.ftc.teamcode.autokit.AutoDrive;
import org.firstinspires.ftc.teamcode.autokit.AutoKit;
import org.firstinspires.ftc.teamcode.autokit.AutoRegistry;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs {@link RightStartTipAuto} — the Auto Builder's export of the design's sample
 * ({@code samples/right-start-tip.pp} in the Visualizer) — through autokit and Ivy's real
 * Scheduler: "LaunchAll · wait for Tip", a drive-through chain, and a retry that rejoins the main
 * plan at RIGHT_HIVE_ENTRANCE — driving on through the chain — then runs the shared steps.
 */
public class RightStartTipAutoTest {

    private static final double LOOP_S = 0.02;

    private final List<String> trace = new ArrayList<>();
    private final Fake drive = new Fake();
    private double now;
    /** Tip is true from this time on. */
    private double tipFrom = Double.POSITIVE_INFINITY;

    @Before
    public void setUp() {
        Scheduler.reset();
    }

    /** A LaunchAll that takes 3 s. */
    private Command launchAll() {
        final double[] startedAt = new double[1];
        return new CommandBuilder().setStart(() -> startedAt[0] = now).setDone(() -> now - startedAt[0] >= 3.0);
    }

    private void run() {
        AutoRegistry registry = new AutoRegistry()
                .command("LaunchAll", 3.0, this::launchAll)
                .trigger("Tip", () -> now >= tipFrom)
                .trigger("IntakeFull", () -> true);
        registry.requireAll(RightStartTipAuto.COMMANDS, RightStartTipAuto.TRIGGERS);
        AutoKit kit = new AutoKit(drive, registry, () -> now).trace(trace::add);
        drive.pose = RightStartTipAuto.startPose(false);
        Command auto = RightStartTipAuto.build(kit, false);
        auto.schedule();
        while (auto.isScheduled() && now < AutoKit.AUTO_LENGTH_S) {
            Scheduler.execute();
            drive.tick();
            now += LOOP_S;
        }
        assertFalse("Still running at 30 s. Trace: " + trace, auto.isScheduled());
    }

    private int at(String line) {
        return trace.indexOf(line);
    }

    @Test
    public void aTipDuringTheLaunchDrivesThroughTheEntrancesToTheFlowerInOneDrive() {
        tipFrom = 1.0;
        run();
        assertTrue(trace.toString(), trace.stream().anyMatch(t -> t.startsWith("Did the HIVE tip?: Tip after 1.0")));
        assertTrue(trace.toString(), trace.contains(
                "path to RIGHT_HIVE_ENTRANCE → to LEFT_HIVE_ENTRANCE → to LEFT_FLOWER"));
        assertTrue(trace.toString(), trace.contains("path LEFT_SHOT to LEFT_LOADING_ZONE"));
    }

    @Test
    public void aRetryThatTipsRejoinsTheMainPlanAtTheRightHiveEntrance() {
        tipFrom = 5.0; // after the first wait's 4 s limit, during the retry's launch
        run();
        assertTrue(trace.toString(), trace.stream().anyMatch(t -> t.startsWith("Did the HIVE tip?: 4000 ms passed")));
        int rejoin = at("path RIGHT_SHOT to RIGHT_HIVE_ENTRANCE → to LEFT_HIVE_ENTRANCE → to LEFT_FLOWER");
        int shared = at("path to LEFT_SHOT");
        int park = at("path LEFT_SHOT to LEFT_LOADING_ZONE");
        assertTrue(trace.toString(), rejoin >= 0 && shared > rejoin && park > shared);
        assertFalse("the main plan's own drive from the start is not driven: " + trace,
                trace.contains("path to RIGHT_HIVE_ENTRANCE → to LEFT_HIVE_ENTRANCE → to LEFT_FLOWER"));
    }

    @Test
    public void aRetryThatFailsParksInTheRightLoadingZone() {
        run();
        assertTrue(trace.toString(), trace.contains("path RIGHT_SHOT to RIGHT_LOADING_ZONE"));
        assertFalse(trace.toString(), trace.stream().anyMatch(t -> t.contains("RIGHT_SHOT to RIGHT_HIVE_ENTRANCE")));
    }

    /** Moves 10% of the current path per loop. */
    private static final class Fake implements AutoDrive {
        Pose pose = new Pose(0, 0, 0);
        private Path current;
        private double progress = 1;

        void tick() {
            if (current == null || progress >= 1) return;
            progress = Math.min(1, progress + 0.1);
            pose = current.get(progress);
        }

        @Override public void follow(Path path) { current = path; progress = 0; }
        @Override public boolean pathDone() { return current == null || progress >= 1; }
        @Override public Pose pose() { return pose; }
        @Override public boolean poseReferenced() { return true; }
        @Override public void hold(Pose pose) { current = null; }
    }
}
