package org.firstinspires.ftc.teamcode.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.util.WheelComparison.Finding;
import org.firstinspires.ftc.teamcode.util.WheelComparison.Result;
import org.firstinspires.ftc.teamcode.util.WheelComparison.Sample;
import org.junit.Test;

import java.util.List;

/**
 * Pins how {@link WheelComparison} tells the causes of a mismatched drive wheel apart. These are
 * the calls a student reads off the Driver Station and acts on — "swap that motor" versus "that
 * bearing is dragging" — so the boundaries are worth fixing in place.
 */
public class WheelComparisonTest {

    private static final double TICKS_PER_REV = 28.0;
    private static final double SLOW_PERCENT = 5.0;
    private static final double DRAG_RATIO = 1.25;

    /** 6000 motor RPM at 28 ticks/rev. */
    private static final double FULL_TPS = 6000.0 / 60.0 * 28.0;

    private static List<Result> compare(Sample... samples) {
        return WheelComparison.compare(WheelComparison.samples(samples), TICKS_PER_REV,
                SLOW_PERCENT, DRAG_RATIO);
    }

    @Test
    public void matchedWheelsAreAllOk() {
        List<Result> results = compare(
                new Sample("fl", 1, FULL_TPS, 2.0),
                new Sample("fr", 1, FULL_TPS * 0.98, 2.1),
                new Sample("bl", 1, FULL_TPS * 0.99, 1.9),
                new Sample("br", 1, FULL_TPS * 0.97, 2.0));
        for (Result r : results) {
            assertEquals(r.name, Finding.OK, r.finding);
        }
        assertEquals(6000.0, results.get(0).rpm, 1e-6);
        assertEquals(100.0, results.get(0).percentOfFastest, 1e-6);
        assertEquals(1, WheelComparison.verdict(results).size());
    }

    @Test
    public void aSlowWheelDrawingMoreCurrentIsDragging() {
        List<Result> results = compare(
                new Sample("fl", 1, FULL_TPS, 2.0),
                new Sample("fr", 1, FULL_TPS * 0.85, 3.2),
                new Sample("bl", 1, FULL_TPS, 2.0),
                new Sample("br", 1, FULL_TPS, 2.1));
        assertEquals(Finding.SLOW_DRAG, results.get(1).finding);
        assertEquals(85.0, results.get(1).percentOfFastest, 1e-6);
        assertTrue(WheelComparison.verdict(results).get(0).startsWith("fr:"));
    }

    @Test
    public void aSlowWheelAtNormalCurrentIsWeak() {
        List<Result> results = compare(
                new Sample("fl", 1, FULL_TPS, 2.0),
                new Sample("fr", 1, FULL_TPS, 2.0),
                new Sample("bl", 1, FULL_TPS * 0.88, 2.1),
                new Sample("br", 1, FULL_TPS, 2.0));
        assertEquals(Finding.SLOW_WEAK, results.get(2).finding);
    }

    @Test
    public void justInsideTheSlowThresholdIsOk() {
        List<Result> results = compare(
                new Sample("fl", 1, FULL_TPS, 2.0),
                new Sample("fr", 1, FULL_TPS * 0.951, 2.0),
                new Sample("bl", 1, FULL_TPS, 2.0),
                new Sample("br", 1, FULL_TPS, 2.0));
        assertEquals(Finding.OK, results.get(1).finding);
    }

    /** A dead encoder must not become the "fastest" baseline or drag every other wheel's %. */
    @Test
    public void aWheelReadingZeroIsFlaggedAndExcludedFromTheBaseline() {
        List<Result> results = compare(
                new Sample("fl", 1, 0.0, 2.0),
                new Sample("fr", 1, FULL_TPS, 2.0),
                new Sample("bl", 1, FULL_TPS, 2.0),
                new Sample("br", 1, FULL_TPS, 2.0));
        assertEquals(Finding.NO_ENCODER, results.get(0).finding);
        assertTrue(Double.isNaN(results.get(0).percentOfFastest));
        assertEquals(Finding.OK, results.get(1).finding);
        assertEquals(100.0, results.get(1).percentOfFastest, 1e-6);
    }

    @Test
    public void aWheelSpinningAgainstItsCommandIsWrongWay() {
        List<Result> results = compare(
                new Sample("fl", -1, -FULL_TPS, 2.0),
                new Sample("fr", 1, -FULL_TPS, 2.0),
                new Sample("bl", 1, FULL_TPS, 2.0),
                new Sample("br", -1, -FULL_TPS, 2.0));
        assertEquals(Finding.OK, results.get(0).finding);
        assertEquals(Finding.WRONG_WAY, results.get(1).finding);
        assertEquals(Finding.OK, results.get(3).finding);
    }

    /** In the "alone" step the other wheels are uncommanded; they must not be judged. */
    @Test
    public void uncommandedWheelsAreNotJudged() {
        List<Result> results = compare(
                new Sample("fl", 1, FULL_TPS, 2.0),
                new Sample("fr", 0, 0.0, 0.0),
                new Sample("bl", 0, 0.0, 0.0),
                new Sample("br", 0, 0.0, 0.0));
        for (Result r : results) {
            assertEquals(r.name, Finding.OK, r.finding);
        }
    }
}
