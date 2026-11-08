package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.pedropathing.api.Paths;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.pedropathing.paths.interpolator.Interpolator;

import org.junit.Test;

/**
 * The simulator's drivetrain ({@link AutoSim.SimDrive}, at a request's default "@50" profile) drives the
 * paths of R-Solo and L-NoCross about as long as the robots did at the Belding Qualifier (7 Nov 2026).
 * Each expected time is the median, over both robots, of the logged seconds from the card's
 * {@code auto: path to …} to its {@code path end} (Pedro done; the robot still rolling, as the
 * simulator's {@link AutoSim.SimDrive#JOIN_IN}). The paths are copied from the generated Autos as
 * they were at Belding (saved 11-05 8:03 and 8:09 PM), in the RED frame; a re-export that moves them
 * changes nothing here. Fast: no field, no Auto, just the drive.
 */
public class SimDriveTimesTest {

    /** How far the simulator may be from the logs' median, s: the logs' own spread is 0.1 to 0.7 s per path. */
    private static final double TOLERANCE_S = 0.45;

    private static final PoseFactory P = PoseFactory.degrees();

    @Test
    public void aDriveThatBarelyTurns() {
        // R-Solo's first drive, 51 in, a 90° turn along it: 1.77 to 1.89 s (9 runs), drawn 2.1 s.
        Pose start = P.of(55.6, 7.7, 90), end = P.of(23.1, 47.2, 180);
        assertDrives(1.82, Paths.line(start, end).linear(start, end));
        // L-NoCross's park, 43 in: 1.60 to 1.93 s (5 runs), drawn 1.9 s.
        Pose catchSpot = P.of(57.5, 123.5, 273.4), park = P.of(14.5, 117.4, 0);
        assertDrives(1.80, Paths.line(catchSpot, park).linear(catchSpot, park));
    }

    @Test
    public void aLongDriveThatTurnsAtOneEnd() {
        // L-NoCross LEFT_LAUNCH_CATCH to RIGHT_LAUNCH_CLOSE, 98 in, 177° in the first fifth, then
        // straight at up to 63 in/s: 2.86 and 3.00 s (20245), drawn 3.3 s. The one kind of drive the
        // simulator gets wrong: the robot turned there at about 150°/s while still doing 17 to 22 in/s,
        // more than the wheel sharing that fits every other drive allows, so the simulator takes about
        // 1 s longer. Too slow is the safe side for a plan; this checks it stays there.
        Pose from = P.of(57.5, 123.5, 273.4), to = P.of(56.1, 26, 90);
        Path path = Paths.curve(from, P.of(56.7614, 93.4601, 0), P.of(68.2657, 72.9873, 0), P.of(58.6996, 26.4005, 0), to)
                .heading(Interpolator.piecewise()
                        .until(0.2, Interpolator.linear(P.of(56.1, 26, 273.4), to))
                        .until(1, Interpolator.linear(to, to)));
        double simulated = drive(path);
        assertTrue("quicker than the robot: " + simulated, simulated >= 2.93 - TOLERANCE_S);
        assertTrue("more than 1.2 s slower than the robot: " + simulated, simulated <= 2.93 + 1.2);
    }

    @Test
    public void aCurveThatTurnsAllTheWay() {
        // R-Solo WALL_FLOWER_STAGING to LEFT_LAUNCH_CLOSE, 90 in, turning 270° on the way: driven at
        // 20 to 30 in/s, 3.56 to 4.59 s (8 runs; one blocked run of 8.0 s left out), drawn 2.9 s.
        Pose from = P.of(23.1, 47.2, 180), to = P.of(59.9, 117.5, 270);
        Path path = Paths.curve(from, P.of(29.2986, 127.3052, 0), to)
                .heading(Interpolator.piecewise()
                        .until(0.5, Interpolator.linear(P.of(59.9, 117.5, 180), P.of(59.9, 117.5, 90)))
                        .until(1, Interpolator.longLinear(P.of(59.9, 117.5, 90), to)));
        assertDrives(3.90, path);
    }

    @Test
    public void aShortHopThatTurnsAround() {
        // R-Solo LEFT_LAUNCH_CLOSE to SCORING_FLOWER_STAGING, 12.8 in and 180°: 1.09 to 1.31 s (7 runs), drawn 1.3 s.
        Pose from = P.of(59.9, 117.5, 270), to = P.of(47.2, 119.3, 90);
        assertDrives(1.19, Paths.line(from, to).linear(from, to));
    }

    private static void assertDrives(double loggedS, Path path) {
        double now = drive(path);
        System.out.printf("SimDriveTimesTest: %.2f s simulated, %.2f s logged%n", now, loggedS);
        assertEquals("seconds to drive the path", loggedS, now, TOLERANCE_S);
    }

    /** Seconds the simulator takes from starting {@code path} at rest to calling it done. */
    private static double drive(Path path) {
        AutoSim.SimDrive drive = new AutoSim.SimDrive();
        drive.pose = path.get(0);
        drive.follow(path);
        double now = 0;
        drive.tick(now);
        while (!drive.pathDone() && now < 15) {
            now += AutoSim.LOOP_S;
            drive.tick(now);
        }
        return now;
    }
}
