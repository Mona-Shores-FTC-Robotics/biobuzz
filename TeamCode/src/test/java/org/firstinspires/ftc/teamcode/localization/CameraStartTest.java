package org.firstinspires.ftc.teamcode.localization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.pedropathing.math.Pose;

import org.junit.Test;

public class CameraStartTest {

    private static final StartPosition START = StartPositions.RED_AUDIENCE;

    private static Pose off(double dx, double dy, double ddeg) {
        return new Pose(START.pose.x() + dx, START.pose.y() + dy, START.pose.heading() + Math.toRadians(ddeg));
    }

    @Test
    public void onTheMarkTheStartStands() {
        CameraStart cs = new CameraStart();
        for (int i = 0; i < LocalizationTuning.seedFrames + 2; i++) {
            assertNull(cs.update(off(1.0, -0.6, 1.5), START)); // the usual camera bias, inside the margin
        }
    }

    @Test
    public void offTheMarkTheCameraWinsOnceTheFitsAgree() {
        CameraStart cs = new CameraStart();
        Pose taken = null;
        for (int i = 0; i < LocalizationTuning.seedFrames; i++) {
            assertNull("not before the frames agree", taken);
            taken = cs.update(off(20.0, 5.0, 0.0), START);
        }
        assertNotNull(taken);
        assertEquals(START.pose.x() + 20.0, taken.x(), 1e-9);
        assertEquals(Math.hypot(20.0, 5.0), cs.offIn(), 1e-9);
    }

    @Test
    public void aWrongHeadingAloneIsEnoughButAWallsFewDegreesAreNot() {
        CameraStart cs = new CameraStart();
        Pose taken = null;
        for (int i = 0; i < LocalizationTuning.seedFrames; i++) taken = cs.update(off(0.5, 0.2, 15.0), START);
        assertNotNull(taken);
        assertEquals(15.0, cs.offDeg(), 1e-9);
        // 2 Nov 2026: the fit sat 3-4 deg from the wall-set heading with the robot on its mark.
        CameraStart onMark = new CameraStart();
        for (int i = 0; i < LocalizationTuning.seedFrames + 2; i++) {
            assertNull(onMark.update(off(0.5, 0.2, 4.0), START));
        }
    }

    @Test
    public void disagreeingFitsNeverCount() {
        CameraStart cs = new CameraStart();
        for (int i = 0; i < 4 * LocalizationTuning.seedFrames; i++) {
            // alternate two fits 10 in apart: never seedFrames in a row that agree
            assertNull(cs.update(off(20.0 + (i % 2) * 10.0, 0.0, 0.0), START));
        }
    }
}
