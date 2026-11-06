package org.firstinspires.ftc.teamcode.opmodes.auto;

import static org.junit.Assert.*;
import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.config.Modifier;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.pedropathing.ivy.commands.Commands;
import org.firstinspires.ftc.teamcode.autokit.AutoDrive;
import org.firstinspires.ftc.teamcode.autokit.AutoKit;
import org.firstinspires.ftc.teamcode.autokit.AutoRegistry;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.PartnerNudgeTestAuto;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RSoloNudgeAuto;
import org.junit.Test;

public class NudgeContactDriveTest {
    private final ForesightConfig config = new ForesightConfig(c -> { });
    private final FakeDrive drive = new FakeDrive();
    private double time, speed;

    private NudgeContactDrive wrapper(boolean rotated) {
        AutoKit kit = new AutoKit(drive, new AutoRegistry(), () -> 0);
        PartnerNudgeTestAuto.build(kit, rotated);
        return new NudgeContactDrive(drive, config, PartnerNudgeTestAuto.startPose(rotated),
                kit.paths().get(0).endPose(), () -> speed, () -> time);
    }
    private AutoKit full(boolean rotated) {
        AutoRegistry registry = new AutoRegistry();
        for (String name : RSoloNudgeAuto.COMMANDS) registry.command(name, 0, () -> Commands.instant(() -> { }));
        for (String name : RSoloNudgeAuto.TRIGGERS) registry.trigger(name, () -> true);
        AutoKit kit = new AutoKit(drive, registry, () -> 0);
        RSoloNudgeAuto.build(kit, rotated);
        return kit;
    }
    @Test public void onlyTwoShortContactPathsAreLimitedOnEitherAlliance() {
        for (boolean rotated : new boolean[] {false, true}) {
            NudgeContactDrive wrapper = wrapper(rotated);
            int limited = 0;
            for (Path path : full(rotated).paths()) {
                wrapper.follow(path);
                if (drive.path != path) {
                    limited++;
                    assertTrue("Only short contact strokes may be slowed", path.curve.length() < 9);
                    for (Modifier m : drive.path.modifiers) m.apply();
                    assertEquals(4, config.maxVelocityConstraint.get(), 0);
                    assertEquals(8, config.maxAccelerationConstraint.get(), 0);
                    for (int i = drive.path.modifiers.size() - 1; i >= 0; i--) drive.path.modifiers.get(i).revert();
                    assertEquals(Double.POSITIVE_INFINITY, config.maxVelocityConstraint.get(), 0);
                    assertEquals(Double.POSITIVE_INFINITY, config.maxAccelerationConstraint.get(), 0);
                }
            }
            assertEquals("Full drawing must keep the test's contact/push endpoints", 2, limited);
        }
    }
    @Test public void contactRequiresArrivalAndSlowSpeedAndBlockedContactTimesOut() {
        NudgeContactDrive wrapper = wrapper(false);
        AutoKit stroke = new AutoKit(drive, new AutoRegistry(), () -> 0);
        PartnerNudgeTestAuto.build(stroke, false);
        wrapper.follow(stroke.paths().get(0));
        assertFalse(wrapper.pathDone());
        time = 6;
        assertTrue(wrapper.contactTimedOut());
        drive.pose = drive.path.endPose();
        speed = 2;
        assertFalse(wrapper.pathDone());
        speed = 0;
        assertTrue(wrapper.pathDone());
        assertFalse(wrapper.contactTimedOut());
        Path ordinary = full(false).paths().get(0);
        wrapper.follow(ordinary);
        time = 99;
        assertSame(ordinary, drive.path);
        assertTrue(wrapper.pathDone());
        assertFalse(wrapper.contactTimedOut());
    }
    private static final class FakeDrive implements AutoDrive {
        Path path;
        Pose pose = new Pose(0, 0, 0);
        public void follow(Path path) { this.path = path; }
        public boolean pathDone() { return true; }
        public Pose pose() { return pose; }
        public boolean poseReferenced() { return true; }
        public void hold(Pose pose) { this.pose = pose; }
    }
}
