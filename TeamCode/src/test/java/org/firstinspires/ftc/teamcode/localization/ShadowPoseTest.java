package org.firstinspires.ftc.teamcode.localization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.pedropathing.math.Pose;

import org.junit.Test;

public class ShadowPoseTest {

    @Test
    public void ridesOnTheRealPoseAndSnapsToAFix() {
        ShadowPose shadow = new ShadowPose();
        Pose real = new Pose(55.6, 7.7, Math.toRadians(90));
        assertEquals(55.6, shadow.of(real).x(), 1e-9);
        assertNull(shadow.of(null));

        shadow.applyFix(54.5, 7.1, real);               // the camera says 1.1 left, 0.6 behind
        assertEquals(54.5, shadow.of(real).x(), 1e-9);
        assertEquals(7.1, shadow.of(real).y(), 1e-9);
        assertEquals(Math.toRadians(90), shadow.of(real).heading(), 1e-12);
        assertEquals(Math.hypot(1.1, 0.6), shadow.lastFixMoveIn(), 1e-9);

        Pose later = new Pose(80.0, 40.0, Math.toRadians(45));   // odometry moved both the same
        assertEquals(78.9, shadow.of(later).x(), 1e-9);
        assertEquals(39.4, shadow.of(later).y(), 1e-9);
        assertEquals(1, shadow.fixesApplied());
    }

    @Test
    public void aSecondFixReplacesTheOffsetAndAHardSetClearsIt() {
        ShadowPose shadow = new ShadowPose();
        Pose real = new Pose(10, 10, 0);
        shadow.applyFix(12, 10, real);
        shadow.applyFix(12.5, 10, real);
        assertEquals(2.5, shadow.offsetX(), 1e-9);
        assertEquals(0.5, shadow.lastFixMoveIn(), 1e-9);
        assertEquals(2, shadow.fixesApplied());
        shadow.follow();
        assertEquals(0.0, shadow.offsetIn(), 1e-9);
        assertEquals(2, shadow.fixesApplied());
    }
}
