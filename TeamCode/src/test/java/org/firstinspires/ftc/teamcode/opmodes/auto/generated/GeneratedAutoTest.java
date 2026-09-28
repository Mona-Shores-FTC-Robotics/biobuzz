package org.firstinspires.ftc.teamcode.opmodes.auto.generated;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.ivy.commands.Commands;
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
 * Runs {@link HiveRushAuto} — exactly what the Auto Builder exports for
 * {@code src/test/resources/auto-builder/hive-rush.pp} — through autokit and Ivy's real Scheduler,
 * against a fake drivetrain. If the editor and the library drift apart, this stops compiling or
 * fails. To refresh it after changing the editor's export, export the .pp again over this file.
 */
public class GeneratedAutoTest {

    private static final double LOOP_S = 0.02;

    private final List<String> actions = new ArrayList<>();
    private final List<String> trace = new ArrayList<>();
    private final Fake drive = new Fake();
    private double now;
    private double tipAt = Double.NaN;
    private boolean blind;

    @Before
    public void setUp() {
        Scheduler.reset();
    }

    private AutoKit kit() {
        AutoRegistry registry = new AutoRegistry();
        for (String name : HiveRushAuto.ACTIONS) {
            registry.action(name, () -> Commands.instant(() -> actions.add(name)));
        }
        registry.condition("LauncherReady", () -> now > 0.3)
                .condition("IntakeFull", () -> true)
                .condition("HiveTipped", () -> !Double.isNaN(tipAt) && now >= tipAt)
                .condition("CameraBlind", () -> blind);
        registry.requireAll(HiveRushAuto.ACTIONS, HiveRushAuto.CONDITIONS);
        return new AutoKit(drive, registry, () -> now).trace(trace::add);
    }

    private void runAuto(boolean mirrored) {
        drive.pose = HiveRushAuto.startPose(mirrored);
        Command auto = HiveRushAuto.build(kit(), mirrored);
        auto.schedule();
        while (auto.isScheduled() && now < AutoKit.AUTO_LENGTH_S) {
            Scheduler.execute();
            drive.tick();
            now += LOOP_S;
        }
        assertFalse("Still running at 30 s. Trace: " + trace, auto.isScheduled());
    }

    @Test
    public void aTipOnTheFirstVolleyTakesTheFastBranchAndParksFar() {
        tipAt = 1.0;
        runAuto(false);
        assertTrue(trace.toString(), trace.stream().anyMatch(t -> t.startsWith("Did the HIVE tip?: HiveTipped or CameraBlind")));
        assertTrue(trace.toString(), trace.contains("path ParkFarPath"));
        assertEquals("SpinUp", actions.get(0));
        assertTrue(actions.toString(), actions.contains("IntakeOn"));
    }

    @Test
    public void aBlindCameraBetsOnTheTip() {
        blind = true;
        runAuto(false);
        assertTrue(trace.toString(), trace.stream().anyMatch(t -> t.startsWith("Did the HIVE tip?: HiveTipped or CameraBlind")));
    }

    @Test
    public void noTipShootsAgainAndChecksAgain() {
        runAuto(false);
        assertTrue(trace.toString(), trace.stream().anyMatch(t -> t.startsWith("Did the HIVE tip?: 1500 ms passed")));
        assertTrue(trace.toString(), trace.stream().anyMatch(t -> t.startsWith("Did it tip this time?:")));
        assertTrue(trace.toString(), trace.contains("path BackToShoot"));
    }

    @Test
    public void theOtherAllianceRunsTheSameAutoMirrored() {
        tipAt = 1.0;
        runAuto(true);
        Pose start = HiveRushAuto.startPose(true);
        assertEquals(141.5 - 38, start.x(), 1e-9);
        assertEquals(71, start.y(), 1e-9);
        Pose farEnd = drive.followed.get(0).endPose();
        assertEquals(141.5 - 116, farEnd.x(), 1e-6);
    }

    /** Moves 10% of the current path per loop; the pose follows the path. */
    private static final class Fake implements AutoDrive {
        final List<Path> followed = new ArrayList<>();
        Pose pose = new Pose(0, 0, 0);
        private Path current;
        private double progress = 1;

        void tick() {
            if (current == null || progress >= 1) return;
            progress = Math.min(1, progress + 0.1);
            pose = current.get(progress);
        }

        @Override public void follow(Path path) { current = path; progress = 0; followed.add(path); }
        @Override public boolean pathDone() { return current == null || progress >= 1; }
        @Override public double pathProgress() { return current == null ? 1 : progress; }
        @Override public Pose pose() { return pose; }
        @Override public boolean poseReferenced() { return true; }
        @Override public void hold(Pose pose) { current = null; }
    }
}
