package org.firstinspires.ftc.teamcode.localization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TagGateTest {

    private static final double EPS = 1e-9;
    private static final double ROW_Z = 49.76;

    private int minFixTags;
    private double maxTagHeightErrorIn;
    private double maxTagRangeIn;
    private double tagOutlierIn;

    private final TagGate gate = new TagGate(16);

    @Before
    public void keepTuning() {
        minFixTags = LocalizationTuning.minFixTags;
        maxTagHeightErrorIn = LocalizationTuning.maxTagHeightErrorIn;
        maxTagRangeIn = LocalizationTuning.maxTagRangeIn;
        tagOutlierIn = LocalizationTuning.tagOutlierIn;
        LocalizationTuning.minFixTags = 2;
        LocalizationTuning.maxTagHeightErrorIn = 3.0;
        LocalizationTuning.maxTagRangeIn = 96.0;
        LocalizationTuning.tagOutlierIn = 3.0;
    }

    @After
    public void restoreTuning() {
        LocalizationTuning.minFixTags = minFixTags;
        LocalizationTuning.maxTagHeightErrorIn = maxTagHeightErrorIn;
        LocalizationTuning.maxTagRangeIn = maxTagRangeIn;
        LocalizationTuning.tagOutlierIn = tagOutlierIn;
    }

    /** A tag at field (fx, fy, ROW_Z) as a robot at (x, y, h) sees it, plus {@code dz} of height error. */
    private void see(int id, double fx, double fy, double x, double y, double h, double dz) {
        double dx = fx - x, dy = fy - y;
        double c = Math.cos(h), s = Math.sin(h);
        gate.add(id, fx, fy, ROW_Z, c * dx + s * dy, -s * dx + c * dy, ROW_Z + dz);
    }

    @Test
    public void fourGoodTagsGiveThePositionAtAnyHeading() {
        for (double h : new double[]{0, 0.7, Math.PI / 2, -2.5}) {
            gate.clear();
            for (int i = 0; i < 4; i++) see(30 + i, 60 + 3.75 * i, 84, 50, 40, h, 0);

            assertEquals(TagGate.Verdict.OK, gate.solve(h));
            assertEquals(4, gate.acceptedCount());
            assertEquals(50, gate.x(), EPS);
            assertEquals(40, gate.y(), EPS);
        }
    }

    @Test
    public void aTagOffItsMapHeightIsDropped() {
        see(30, 60, 84, 50, 40, 0, 0);
        see(31, 64, 84, 50, 40, 0, 0);
        see(32, 68, 84, 50, 40, 0, 5.0);

        assertEquals(TagGate.Verdict.OK, gate.solve(0));
        assertEquals(TagGate.Reason.HEIGHT, gate.reason(2));
        assertEquals(2, gate.acceptedCount());
    }

    @Test
    public void aTagTooFarAwayIsDropped() {
        see(30, 60, 84, 50, 40, 0, 0);
        see(31, 64, 84, 50, 40, 0, 0);
        see(32, 160, 84, 50, 40, 0, 0);

        gate.solve(0);
        assertEquals(TagGate.Reason.RANGE, gate.reason(2));
    }

    @Test
    public void oneTagThatDisagreesWithTheOthersIsDroppedAndTheRestStillGiveThePosition() {
        see(30, 60, 84, 50, 40, 0, 0);
        see(31, 64, 84, 50, 40, 0, 0);
        see(32, 68, 84, 50, 40, 0, 0);
        // Seen 6 in further left than it really is: a bad corner, a flexed panel.
        see(33, 72, 84, 50, 34, 0, 0);

        assertEquals(TagGate.Verdict.OK, gate.solve(0));
        assertEquals(TagGate.Reason.OUTLIER, gate.reason(3));
        assertEquals(3, gate.acceptedCount());
        assertEquals(50, gate.x(), EPS);
        assertEquals(40, gate.y(), EPS);
    }

    @Test
    public void twoTagsThatDisagreeGiveNoFix() {
        see(30, 60, 84, 50, 40, 0, 0);
        see(31, 64, 84, 50, 32, 0, 0);

        assertEquals(TagGate.Verdict.TOO_FEW_TAGS, gate.solve(0));
        assertEquals(0, gate.acceptedCount());
    }

    @Test
    public void oneTagAloneIsNotEnough() {
        see(30, 60, 84, 50, 40, 0, 0);

        assertEquals(TagGate.Verdict.TOO_FEW_TAGS, gate.solve(0));
        assertEquals(1, gate.acceptedCount());
    }

    @Test
    public void aPositionOffTheFieldIsRefused() {
        see(30, 2, 84, -10, 40, 0, 0);
        see(31, 6, 84, -10, 40, 0, 0);

        assertEquals(TagGate.Verdict.OUTSIDE_FIELD, gate.solve(0));
    }

    @Test
    public void withNoHeadingOnlyHeightAndRangeAreCheckedAndTheTagsArePackedForTheFit() {
        see(30, 60, 84, 50, 40, 0.3, 0);
        see(31, 64, 84, 50, 40, 0.3, 4.0);
        see(32, 68, 84, 50, 40, 0.3, 0);

        assertEquals(TagGate.Verdict.OK, gate.solve(Double.NaN));
        assertEquals(2, gate.acceptedCount());
        assertTrue(Double.isNaN(gate.x()));
        assertEquals(68, gate.acceptedFieldX()[1], EPS);
        com.pedropathing.math.Pose fit = CellFix.fit(gate.acceptedFieldX(), gate.acceptedFieldY(),
                gate.acceptedRobotX(), gate.acceptedRobotY(), gate.acceptedCount(), 1.5);
        assertEquals(50, fit.x(), 1e-6);
        assertEquals(40, fit.y(), 1e-6);
        assertEquals(0.3, fit.heading(), 1e-6);
    }

    @Test
    public void clearForgetsTheLastFrame() {
        see(30, 60, 84, 50, 40, 0, 0);
        see(31, 64, 84, 50, 40, 0, 0);
        gate.solve(0);
        gate.clear();

        assertEquals(0, gate.count());
        assertEquals(TagGate.Verdict.NO_TAGS, gate.solve(0));
    }
}
