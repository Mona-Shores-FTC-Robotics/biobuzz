package org.firstinspires.ftc.teamcode.localization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.localization.FlowerFieldPoints.Flower;
import org.firstinspires.ftc.teamcode.util.FieldFrame;
import org.junit.Test;

/** The four FLOWERs from the field model, the poses into them, and the nearest one. */
public class FlowerFieldPointsTest {

    private static final double EPS = 1e-9;

    /**
     * The field model's staged POLLEN stacks ({@code biobuzz-staged-pieces.csv} on
     * {@code claude/simulator}, from AdvantageScope's 2026-27 field): one stack per FLOWER, at these
     * centres to the hundredth.
     */
    @Test
    public void theFourCentresAreTheFieldModels() {
        assertCentre(FlowerFieldPoints.RED_WALL, 2.71, 47.36);
        assertCentre(FlowerFieldPoints.RED_FAR, 47.36, 138.79);
        assertCentre(FlowerFieldPoints.BLUE_WALL, 138.79, 94.14);
        assertCentre(FlowerFieldPoints.BLUE_FAR, 94.14, 2.71);
    }

    /** Each blue FLOWER is its red one turned 180° about the field centre, facing the other way. */
    @Test
    public void blueIsRedTurnedAboutTheCentre() {
        double c = FieldFrame.FIELD_CENTRE_INCHES;
        assertTurned(FlowerFieldPoints.RED_WALL, FlowerFieldPoints.BLUE_WALL, c);
        assertTurned(FlowerFieldPoints.RED_FAR, FlowerFieldPoints.BLUE_FAR, c);
    }

    /** A robot facing a FLOWER faces its wall: the FLOWER is the nearer side, the wall just beyond it. */
    @Test
    public void facingPointsAtTheWall() {
        double s = FieldFrame.FIELD_SIZE_INCHES;
        for (Flower f : FlowerFieldPoints.ALL) {
            double wallX = f.x + FlowerFieldPoints.CENTRE_FROM_WALL_IN * Math.cos(f.facingRad);
            double wallY = f.y + FlowerFieldPoints.CENTRE_FROM_WALL_IN * Math.sin(f.facingRad);
            boolean onAWall = Math.abs(wallX) < 1e-6 || Math.abs(wallX - s) < 1e-6
                    || Math.abs(wallY) < 1e-6 || Math.abs(wallY - s) < 1e-6;
            assertEquals(f.name + " faces its wall", true, onAWall);
        }
    }

    /** R-Quals' far FLOWER point (47.36, 126.64) facing 90° is this model's red FAR FLOWER, 12.15 in back. */
    @Test
    public void theDrawnFarFlowerPointIsAStandoffFromThisOne() {
        Pose drawn = FlowerFieldPoints.standoff(FlowerFieldPoints.RED_FAR, 12.15);
        assertEquals(47.36, drawn.x(), 1e-6);
        assertEquals(126.64, drawn.y(), 1e-6);
        assertEquals(Math.PI / 2, drawn.heading(), EPS);
    }

    @Test
    public void standoffBacksAwayFromTheWall() {
        Pose p = FlowerFieldPoints.standoff(FlowerFieldPoints.RED_WALL, 20.0);
        assertEquals(22.71, p.x(), 1e-6);
        assertEquals(47.36, p.y(), 1e-6);
        assertEquals(Math.PI, p.heading(), EPS);
    }

    @Test
    public void nearestWithinRangeOrNone() {
        assertSame(FlowerFieldPoints.RED_WALL, FlowerFieldPoints.nearest(20, 50, 48));
        assertSame(FlowerFieldPoints.RED_FAR, FlowerFieldPoints.nearest(47, 110, 48));
        assertSame(FlowerFieldPoints.BLUE_FAR, FlowerFieldPoints.nearest(94, 30, 48));
        assertNull("the field centre is far from every FLOWER", FlowerFieldPoints.nearest(
                FieldFrame.FIELD_CENTRE_INCHES, FieldFrame.FIELD_CENTRE_INCHES, 48));
        assertNull("a zero range finds nothing off a FLOWER", FlowerFieldPoints.nearest(20, 50, 0));
    }

    private static void assertCentre(Flower f, double x, double y) {
        assertEquals(f.name + " x", x, f.x, 0.005);
        assertEquals(f.name + " y", y, f.y, 0.005);
    }

    private static void assertTurned(Flower red, Flower blue, double c) {
        assertEquals(2 * c - red.x, blue.x, 1e-6);
        assertEquals(2 * c - red.y, blue.y, 1e-6);
        assertEquals(0.0, Math.IEEEremainder(blue.facingRad - red.facingRad - Math.PI, 2 * Math.PI), 1e-9);
    }
}
