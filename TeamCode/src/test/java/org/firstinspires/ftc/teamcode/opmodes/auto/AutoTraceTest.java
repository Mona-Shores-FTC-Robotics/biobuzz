package org.firstinspires.ftc.teamcode.opmodes.auto;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AutoTraceTest {

    @Test
    public void everyLineGoesToTheLogWithThePrefix() {
        List<String> events = new ArrayList<>();
        AutoTrace trace = new AutoTrace(2, events::add);
        trace.accept("wait TIP 1");
        trace.accept("TIP 1: Tip after 0.98 s");
        trace.accept("path S_CATCH to GARDEN_IN");
        assertEquals(Arrays.asList("auto: wait TIP 1", "auto: TIP 1: Tip after 0.98 s", "auto: path S_CATCH to GARDEN_IN"),
                events);
    }

    @Test
    public void theMatchPageGetsTheLatestLinesOldestFirst() {
        AutoTrace trace = new AutoTrace(2, line -> { });
        trace.accept("a");
        trace.accept("b");
        trace.accept("c");
        List<String> shown = new ArrayList<>();
        trace.forEachRecent(shown::add);
        assertEquals(Arrays.asList("b", "c"), shown);
    }

    @Test
    public void nothingTracedShowsNothing() {
        List<String> shown = new ArrayList<>();
        new AutoTrace(4, line -> { }).forEachRecent(shown::add);
        assertEquals(0, shown.size());
    }
}
