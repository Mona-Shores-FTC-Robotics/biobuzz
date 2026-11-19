package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class VolleyHistoryTest {

    private static List<String> lines(VolleyHistory h, int max) {
        List<String> out = new ArrayList<>();
        h.forEachRecent(max, out::add);
        return out;
    }

    @Test public void noShotsSaysSo() {
        VolleyHistory h = new VolleyHistory();
        assertEquals(0, h.volleys());
        assertEquals("Volleys: none yet", h.summary());
        assertEquals(0, lines(h, 5).size());
    }

    @Test public void shotsCloseTogetherAreOneVolleyAndAGapStartsTheNext() {
        VolleyHistory h = new VolleyHistory();
        h.shot(2.0, 1557, 150, 53.2, "camera");
        h.shot(2.4, 1557, 180, 53.0, "camera");
        h.shot(2.9, 1557, 160, 52.8, "camera");
        h.shot(3.3, 1557, 140, 53.0, "camera");
        h.shot(14.0, 1495, 120, 40.4, "pose");
        h.shot(14.5, 1505, 130, 47.6, "pose");
        assertEquals(2, h.volleys());
        assertEquals("1) 2.0 s · 4 shots · 53 in camera · 1557 ticks/s · dip 180", lines(h, 5).get(0));
        assertEquals("2) 14.0 s · 2 shots · 44 in pose · 1495–1505 ticks/s · dip 130", lines(h, 5).get(1));
        assertEquals("Volleys: 2 · 6 shots · 40–53 in · 1495–1557 ticks/s", h.summary());
    }

    @Test public void recentKeepsTheNewestAndUnknownsReadAsQuestionMarks() {
        VolleyHistory h = new VolleyHistory();
        h.shot(1, 1500, Double.NaN, Double.NaN, null);
        h.shot(10, 1500, Double.NaN, Double.NaN, null);
        h.shot(20, 1500, Double.NaN, 41, "camera");
        assertEquals(2, lines(h, 2).size());
        assertEquals("2) 10.0 s · 1 shot · range ? · 1500 ticks/s", lines(h, 2).get(0));
        assertEquals("3) 20.0 s · 1 shot · 41 in camera · 1500 ticks/s", lines(h, 2).get(1));
    }
}
