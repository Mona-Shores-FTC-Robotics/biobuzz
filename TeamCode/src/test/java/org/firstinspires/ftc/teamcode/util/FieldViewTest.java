package org.firstinspires.ftc.teamcode.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** The drawn robot: its drivetrain wheel to wheel, and its frame, turned to its heading. */
public class FieldViewTest {

    private static final double EPS = 1e-9;

    @Test
    public void theDrivetrainIs15By14CentredOnThePose() {
        double[] d = FieldView.DRIVETRAIN;
        assertEquals(15.43, d[0] + d[1], EPS);
        assertEquals(13.788, d[2] + d[3], EPS);
        assertEquals("centred", d[0], d[1], EPS);
    }

    @Test
    public void theFrameIs16By16FlushAtTheBack() {
        double[] f = FieldView.FRAME;
        assertEquals(16.0, f[0] + f[1], EPS);
        assertEquals(16.0, f[2] + f[3], EPS);
        assertEquals("back flush with the rear wheels", 7.72, f[0], 0.01);
    }

    @Test
    public void facingPlusYTheFrameReachesFurtherAheadThanBehind() {
        double c = Math.cos(Math.PI / 2), s = Math.sin(Math.PI / 2);
        double[] f = FieldView.FRAME;
        // Facing +y from the red start (54.06, 7.72): front left is (x - 8, y + 8.28), back right (x + 8, y - 7.72).
        assertEquals(54.06 - 8.0, FieldView.cornerX(54.06, c, s, f, 0), EPS);
        assertEquals(7.72 + 8.28, FieldView.cornerY(7.72, c, s, f, 0), EPS);
        assertEquals(54.06 + 8.0, FieldView.cornerX(54.06, c, s, f, 2), EPS);
        assertEquals(0.0, FieldView.cornerY(7.72, c, s, f, 2), EPS);
    }

    @Test
    public void theCornersMakeTheOutlineAtAnyHeading() {
        double heading = Math.toRadians(30);
        double c = Math.cos(heading), s = Math.sin(heading);
        double[] f = FieldView.FRAME;
        double front = Math.hypot(FieldView.cornerX(0, c, s, f, 3) - FieldView.cornerX(0, c, s, f, 0),
                FieldView.cornerY(0, c, s, f, 3) - FieldView.cornerY(0, c, s, f, 0));
        double side = Math.hypot(FieldView.cornerX(0, c, s, f, 1) - FieldView.cornerX(0, c, s, f, 0),
                FieldView.cornerY(0, c, s, f, 1) - FieldView.cornerY(0, c, s, f, 0));
        assertEquals(16.0, front, EPS);
        assertEquals(16.0, side, EPS);
    }
}
