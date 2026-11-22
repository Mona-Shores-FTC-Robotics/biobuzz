package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.pedropathing.math.Pose;

import org.junit.Test;

/** Why a grab's dwell ends: a full lane first, then the cap, then the intake current's answer. */
public class GrabRoutineTest {

    @Test
    public void aFullLaneEndsTheDwellWhateverElseSays() {
        assertEquals("full", GrabRoutine.dwellEnd(false, true, false, false));
        assertEquals("full", GrabRoutine.dwellEnd(true, true, true, true));
    }

    @Test
    public void theCapEndsItWhenTheLaneIsNotFull() {
        assertEquals("dwellMs", GrabRoutine.dwellEnd(true, false, false, false));
        assertEquals("dwellMs", GrabRoutine.dwellEnd(true, false, true, true));
    }

    @Test
    public void theCurrentEndsItWhenItSaysSo() {
        assertEquals("pieces in", GrabRoutine.dwellEnd(false, false, true, true));
        assertEquals("nothing came", GrabRoutine.dwellEnd(false, false, true, false));
    }

    @Test
    public void otherwiseItGoesOn() {
        assertNull(GrabRoutine.dwellEnd(false, false, false, true));
    }

    @Test
    public void anotherInFollowsEveryDwellButTheLast() {
        assertEquals(true, GrabRoutine.anotherInAhead(2, 1, 3.0, 8.0));
        assertEquals("one push: never", false, GrabRoutine.anotherInAhead(1, 1, 3.0, 8.0));
        assertEquals("the second of two is the last", false, GrabRoutine.anotherInAhead(2, 2, 3.0, 8.0));
        assertEquals("the second of three is not", true, GrabRoutine.anotherInAhead(3, 2, 3.0, 8.0));
        assertEquals("a back-off under an inch is no back-off", false, GrabRoutine.anotherInAhead(2, 1, 0.5, 8.0));
        assertEquals("a typo (NaN) is no back-off", false, GrabRoutine.anotherInAhead(2, 1, Double.NaN, 8.0));
        assertEquals("no room behind: the In started here", false, GrabRoutine.anotherInAhead(2, 1, 3.0, 0.2));
    }

    /**
     * The FLOWER pick study's Auto pushes (doc/flower-picks, 22 Nov 2026), at FlowerPicker.Retry's
     * 4.0 in: the three that stalled 4.0 in or more short took nothing and left the FLOWER full, so
     * they push again; the shortest stalls of picks that took 4 (0.7 in, Fruitport Q-39 19429; 3.4 in,
     * Belding Q-31 20245) and the longest that took some (3.5 in, Belding Q-19 19429) do not.
     */
    @Test
    public void onlyAnAutoPushThatStalledFarShortPushesAgain() {
        double at = 4.0;
        double[] emptyPicks = {4.0, 4.6, 4.7}; // Belding Q-4 19429 both picks, Fruitport Q-4 20245 pick 2
        for (double shortIn : emptyPicks) {
            assertEquals("stalled " + shortIn + " in short", true,
                    GrabRoutine.retryPush(at, true, 1, 1, false, String.format("stalled %.1f in short", shortIn), shortIn, 9.0));
        }
        double[] goodPicks = {0.7, 2.8, 3.4, 3.5};
        for (double shortIn : goodPicks) {
            assertEquals("stalled " + shortIn + " in short", false,
                    GrabRoutine.retryPush(at, true, 1, 1, false, String.format("stalled %.1f in short", shortIn), shortIn, 9.0));
        }
        assertEquals("off", false, GrabRoutine.retryPush(0.0, true, 1, 1, false, "stalled 4.6 in short", 4.6, 9.0));
        assertEquals("a typo (NaN) is off", false, GrabRoutine.retryPush(Double.NaN, true, 1, 1, false, "stalled 4.6 in short", 4.6, 9.0));
        assertEquals("the button: the driver pushes again", false, GrabRoutine.retryPush(at, false, 1, 1, false, "stalled 4.6 in short", 4.6, 9.0));
        assertEquals("only once", false, GrabRoutine.retryPush(at, true, 1, 1, true, "stalled 4.6 in short", 4.6, 9.0));
        assertEquals("a grab of several pushes already goes in again", false,
                GrabRoutine.retryPush(at, true, 2, 1, false, "stalled 4.6 in short", 4.6, 9.0));
        assertEquals("only a stall: a timeout is something else", false,
                GrabRoutine.retryPush(at, true, 1, 1, false, "timed out 4.6 in short", 4.6, 9.0));
        assertEquals("a path that ended short is not pressed against anything", false,
                GrabRoutine.retryPush(at, true, 1, 1, false, "path done 4.6 in short", 4.6, 9.0));
        assertEquals("no room to back off", false, GrabRoutine.retryPush(at, true, 1, 1, false, "stalled 4.6 in short", 4.6, 0.5));
    }

    @Test
    public void theBackHandsOverOnceBackedFarEnoughAndOnlyWhenAsked() {
        assertEquals("off: back all the way", false, GrabRoutine.handOverNow(0.0, 9.0));
        assertEquals(false, GrabRoutine.handOverNow(6.0, 5.9));
        assertEquals(true, GrabRoutine.handOverNow(6.0, 6.0));
        assertEquals("a typo (NaN) backs all the way", false, GrabRoutine.handOverNow(Double.NaN, 9.0));
    }

    @Test
    public void theBackOffGoesTowardWhereTheInStartedAndNeverPastIt() {
        Pose home = new Pose(22.0, 47.4, Math.PI);
        Pose at = new Pose(14.0, 47.4, Math.PI - 0.05);
        Pose off = GrabRoutine.backOffPose(at, home, 5.0);
        assertEquals(19.0, off.x(), 1e-9);
        assertEquals(47.4, off.y(), 1e-9);
        assertEquals("square again, as the In started", Math.PI, off.heading(), 1e-9);
        assertEquals("never past the start", 22.0, GrabRoutine.backOffPose(at, home, 30.0).x(), 1e-9);
        assertEquals("already there: stays", 22.0, GrabRoutine.backOffPose(home, home, 5.0).x(), 1e-9);
        assertEquals("a negative typo backs off nothing", 14.0, GrabRoutine.backOffPose(at, home, -3.0).x(), 1e-9);
    }

    @Test
    public void lettingGoDuringTheBackOffMeansDone() {
        assertEquals(true, GrabRoutine.releaseFinishes(GrabRoutine.Phase.BACK_OFF, true));
        assertEquals("an Auto's card only lets go when cut short", false,
                GrabRoutine.releaseFinishes(GrabRoutine.Phase.BACK_OFF, false));
    }
}
