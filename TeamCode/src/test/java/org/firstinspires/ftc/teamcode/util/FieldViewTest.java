package org.firstinspires.ftc.teamcode.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** The drawn robot is its 16 × 16 in square, turned to its heading. */
public class FieldViewTest {

    private static final double EPS = 1e-9;
    private static final double H = FieldView.ROBOT_HALF_SIZE_INCHES;

    @Test
    public void facingPlusYTheFrontLeftCornerIsUpAndToTheLeft() {
        double c = Math.cos(Math.PI / 2), s = Math.sin(Math.PI / 2);
        // Facing +y, the robot's left is -x: front left is (x - 8, y + 8).
        assertEquals(54.06 - H, FieldView.cornerX(54.06, c, s, H, 0), EPS);
        assertEquals(7.72 + H, FieldView.cornerY(7.72, c, s, H, 0), EPS);
        // Back right, two corners on: (x + 8, y - 8).
        assertEquals(54.06 + H, FieldView.cornerX(54.06, c, s, H, 2), EPS);
        assertEquals(7.72 - H, FieldView.cornerY(7.72, c, s, H, 2), EPS);
    }

    @Test
    public void theFourCornersMakeA16InchSquare() {
        double heading = Math.toRadians(30);
        double c = Math.cos(heading), s = Math.sin(heading);
        for (int i = 0; i < 4; i++) {
            int j = (i + 1) % 4;
            double side = Math.hypot(FieldView.cornerX(0, c, s, H, j) - FieldView.cornerX(0, c, s, H, i),
                    FieldView.cornerY(0, c, s, H, j) - FieldView.cornerY(0, c, s, H, i));
            assertEquals(2 * H, side, EPS);
        }
    }
}
