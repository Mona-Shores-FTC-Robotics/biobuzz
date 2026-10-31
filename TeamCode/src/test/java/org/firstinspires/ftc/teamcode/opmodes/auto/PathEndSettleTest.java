package org.firstinspires.ftc.teamcode.opmodes.auto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pedropathing.api.Paths;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link PathEndSettle} against a made-up drivetrain: when the robot counts as settled after a
 * path, and what the path-end log says.
 */
public class PathEndSettleTest {

    private static final double EPS = 1e-9;

    private final List<String> events = new ArrayList<>();
    private double now;
    private Pose pose = new Pose(0, 0, 0);
    private double speed;
    private double turn;
    private PathEndSettle settle;

    @Before
    @After
    public void defaults() {
        PathEndSettle.aimToleranceDeg = 3.0;
        PathEndSettle.stillSpeedInPerSec = 2.0;
        PathEndSettle.stillTurnDegPerSec = 5.0;
        PathEndSettle.maxSettleWaitS = 1.0;
    }

    @Before
    public void setUp() {
        events.clear();
        now = 0;
        speed = 0;
        turn = 0;
        settle = new PathEndSettle(new PathEndSettle.Motion() {
            @Override public Pose pose() { return pose; }
            @Override public double speedInPerSec() { return speed; }
            @Override public double turnRadPerSec() { return turn; }
            @Override public String followerMode() { return "HOLD"; }
            @Override public boolean atParametricEnd() { return true; }
            @Override public double distanceToEndpoint() { return 5.5; }
        }, () -> now, events::add);
    }

    /** A straight path from (0, 0) to (100, 0), ending at {@code endDeg}. */
    private static Path pathEndingAt(double endDeg) {
        return Paths.line(new Pose(0, 0, 0), new Pose(100, 0, 0)).constant(Math.toRadians(endDeg));
    }

    @Test
    public void beforeAnyPathOnlyStillnessCounts() {
        pose = new Pose(0, 0, Math.toRadians(45));
        assertTrue(settle.settled());
        speed = 10;
        assertFalse(settle.settled());
    }

    /** 25 Oct: Pedro said done with the heading 15 to 20° short of a 180° turn. */
    @Test
    public void aHeadingShortOfThePathsEndIsNotSettledEvenWhenStill() {
        Path path = pathEndingAt(180);
        settle.pathStarted(path);
        pose = new Pose(100, 0, Math.toRadians(163));
        settle.pathEnded(path);
        assertFalse(settle.settled());
        pose = new Pose(100, 0, Math.toRadians(178));
        assertTrue(settle.settled());
    }

    /** The heading error wraps: -179° and 179° are 2° apart. */
    @Test
    public void theHeadingErrorWrapsAroundHalfATurn() {
        Path path = pathEndingAt(180);
        settle.pathStarted(path);
        pose = new Pose(100, 0, Math.toRadians(-179));
        assertEquals(1.0, Math.abs(settle.headingErrorDeg(pose)), 1e-6);
        assertTrue(settle.settled());
    }

    @Test
    public void movingOrTurningIsNotSettled() {
        Path path = pathEndingAt(0);
        settle.pathStarted(path);
        pose = new Pose(100, 0, 0);
        speed = 3;
        assertFalse("overshooting, 5 in past the point on 25 Oct", settle.settled());
        speed = 0;
        turn = Math.toRadians(10);
        assertFalse(settle.settled());
        turn = 0;
        assertTrue(settle.settled());
    }

    @Test
    public void aBadToleranceTurnsItsCheckOffRatherThanBlockingTheFire() {
        Path path = pathEndingAt(180);
        settle.pathStarted(path);
        pose = new Pose(100, 0, 0);
        speed = 50;
        PathEndSettle.aimToleranceDeg = Double.NaN;
        PathEndSettle.stillSpeedInPerSec = 0;
        assertTrue(settle.settled());
    }

    @Test
    public void theLogSaysHowEachPathEndedAndWhenTheRobotSettled() {
        Path path = pathEndingAt(180);
        settle.pathStarted(path);
        now = 10;
        pose = new Pose(105.5, 0, Math.toRadians(163));
        speed = 8;
        settle.pathEnded(path);
        assertEquals(events.toString(), 1, events.size());
        String end = events.get(0);
        assertTrue(end, end.contains("mode HOLD, atParametricEnd true, distanceToEndpoint 5.50 in"));
        assertTrue(end, end.contains("off end 5.50 in, heading -17.0°, 8.0 in/s"));

        now = 10.2;
        settle.update();
        assertEquals("still moving: nothing yet", 1, events.size());
        now = 10.6;
        speed = 0;
        pose = new Pose(100.4, 0, Math.toRadians(179));
        settle.update();
        assertEquals(events.toString(), 2, events.size());
        assertTrue(events.get(1), events.get(1).startsWith("settled 0.60 s after path end (100.0, 0.0, 180°)"));
        settle.update();
        assertEquals("one settle event per path end", 2, events.size());
    }

    @Test
    public void aBadMaxWaitIsZero() {
        PathEndSettle.maxSettleWaitS = -2;
        assertEquals(0.0, PathEndSettle.maxWaitS(), EPS);
        PathEndSettle.maxSettleWaitS = Double.NaN;
        assertEquals(0.0, PathEndSettle.maxWaitS(), EPS);
    }
}
