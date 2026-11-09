package org.firstinspires.ftc.teamcode.opmodes.auto;

import static org.junit.Assert.*;

import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.config.Modifier;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;

import org.firstinspires.ftc.teamcode.autokit.AutoDrive;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.PartnerNudgeTestAuto;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class NudgeTestRunTest {
    private final ForesightConfig config = new ForesightConfig(c -> { });
    private final FakeDrive drive = new FakeDrive();
    private double clock;
    private double speed;
    private int releases;

    @Before public void reset() { Scheduler.reset(); }

    private NudgeTestRun run(double pathSpeed, double timeout) {
        return new NudgeTestRun(drive, config, () -> speed, () -> clock,
                () -> releases++, line -> { }, pathSpeed, timeout);
    }

    @Test public void bothDrawnPathsHaveAScopedSpeedCapAndCompletionReleasesDrive() {
        NudgeTestRun run = run(0.2, 8);
        run.start(false);
        for (int i = 0; i < 100 && run.running(); i++) {
            Scheduler.execute();
            if (drive.current != null) drive.pose = drive.current.endPose();
            clock += 0.02;
            run.update();
        }
        assertFalse(run.running());
        assertEquals(2, drive.paths.size());
        assertEquals(1, releases);
        assertTrue(run.result().startsWith("Complete"));
        for (Path path : drive.paths) {
            for (Modifier modifier : path.modifiers) modifier.apply();
            assertEquals(0.2, config.maxPathSpeed.get(), 0);
            // Never Pedro's acceleration cap: from rest it targets one loop's worth of speed and
            // never starts the robot (6 Nov).
            assertEquals(Double.POSITIVE_INFINITY, config.maxAccelerationConstraint.get(), 0);
            assertEquals(Double.POSITIVE_INFINITY, config.maxVelocityConstraint.get(), 0);
            for (int i = path.modifiers.size() - 1; i >= 0; i--) path.modifiers.get(i).revert();
            assertEquals(Double.POSITIVE_INFINITY, config.maxPathSpeed.get(), 0);
        }
    }

    @Test public void earlyPathDoneDoesNotRetreatAndBlockedPushTimesOut() {
        NudgeTestRun run = run(0.2, 8);
        run.start(false);
        for (int i = 0; i < 100; i++) {
            Scheduler.execute();
            clock += 0.02;
            run.update();
        }
        // Fake says pathDone immediately, but odometry still says we never moved.
        assertEquals(1, drive.paths.size());
        assertTrue(run.running());
        clock = 8;
        run.update();
        assertFalse(run.running());
        assertTrue(run.result().startsWith("Timed out"));
        assertEquals(1, releases);
        for (int i = 0; i < 10; i++) Scheduler.execute();
        assertEquals(1, drive.paths.size()); // cancellation must not schedule the retreat later
    }

    @Test public void arrivalMustAlsoBeSlowAndAimed() {
        NudgeTestRun run = run(0.2, 8);
        run.start(false);
        for (int i = 0; i < 10; i++) Scheduler.execute();
        Pose end = drive.current.endPose();
        drive.pose = end;
        speed = 3;
        for (int i = 0; i < 10; i++) Scheduler.execute();
        assertEquals(1, drive.paths.size());
        speed = 0;
        drive.pose = new Pose(end.x(), end.y(), end.heading() + Math.toRadians(10));
        for (int i = 0; i < 10; i++) Scheduler.execute();
        assertEquals(1, drive.paths.size());
        drive.pose = end;
        for (int i = 0; i < 10; i++) Scheduler.execute();
        assertEquals(2, drive.paths.size());
        run.stop("test finished");
    }

    @Test public void stopAndPoseLossReleaseDriveWithoutContinuing() {
        NudgeTestRun run = run(0.2, 8);
        run.start(true);
        for (int i = 0; i < 10; i++) Scheduler.execute();
        // Blue follows the rotated drawing, with the same limits.
        Pose red = PartnerNudgeTestAuto.startPose(false);
        Pose blue = PartnerNudgeTestAuto.startPose(true);
        assertEquals(141.5 - red.x(), blue.x(), 0.001);
        drive.referenced = false;
        run.update();
        assertFalse(run.running());
        assertEquals(1, releases);
        assertTrue(run.result().contains("reference lost"));
    }

    @Test public void invalidTunablesNeverScheduleMotion() {
        for (double value : new double[]{Double.NaN, Double.POSITIVE_INFINITY, -1, 0, 0.05, 0.6, 1, 4}) {
            try { run(value, 8); fail("accepted path speed " + value); }
            catch (IllegalArgumentException expected) { }
        }
        try { run(0.2, 0); fail("accepted zero timeout"); }
        catch (IllegalArgumentException expected) { }
        assertTrue(drive.paths.isEmpty());
    }

    private static final class FakeDrive implements AutoDrive {
        final List<Path> paths = new ArrayList<>();
        Path current;
        Pose pose = PartnerNudgeTestAuto.startPose(false);
        boolean referenced = true;
        @Override public void follow(Path path) { current = path; paths.add(path); }
        @Override public boolean pathDone() { return true; }
        @Override public Pose pose() { return pose; }
        @Override public boolean poseReferenced() { return referenced; }
        @Override public void hold(Pose target) { }
    }
}
