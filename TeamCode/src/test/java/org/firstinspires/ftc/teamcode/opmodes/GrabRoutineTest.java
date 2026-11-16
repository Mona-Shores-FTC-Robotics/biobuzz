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
