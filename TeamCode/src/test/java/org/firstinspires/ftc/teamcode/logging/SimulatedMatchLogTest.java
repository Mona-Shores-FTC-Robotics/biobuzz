package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Writes the two desk-check logs and checks they hold what AdvantageScope needs.
 *
 * <p>Run {@code ./gradlew :TeamCode:testDebugUnitTest --tests '*SimulatedMatchLogTest*'} and open
 * the files it writes in AdvantageScope (File → Open Logs):
 * <ul>
 *   <li>{@code TeamCode/build/sim-logs/known-points.wpilog}: checks the field mapping.</li>
 *   <li>{@code TeamCode/build/sim-logs/sim-match.wpilog}: a whole fake match.</li>
 * </ul>
 * Set {@code -Dsimlog.dir=...} to write them somewhere else.
 */
public class SimulatedMatchLogTest {

    private static File dir() {
        return TeamCodeDir.simLogs();
    }

    @Test
    public void simulatedMatchHasEverythingAdvantageScopeUses() throws IOException {
        File file = new File(dir(), "sim-match.wpilog");
        new SimulatedMatch(3572L).write(file);
        WpiLogReader r = new WpiLogReader(Files.readAllBytes(file.toPath()));

        // Match timeline: disabled, auto, disabled, teleop, disabled.
        WpiLogReader.Entry enabled = r.entry(AdvantageScopeKeys.ENABLED);
        assertEquals("boolean", enabled.type);
        assertEquals(5, enabled.records.size());
        assertEquals(Math.round(SimulatedMatch.AUTO_START * 1e6), enabled.records.get(1).timestampUs, 20_000);
        assertEquals("autonomous", r.entry(AdvantageScopeKeys.ROBOT_MODE).records.get(1).asString());
        assertEquals(1L, r.entry(AdvantageScopeKeys.ALLIANCE_STATION).records.get(0).asInt64());

        // Field views.
        assertEquals("struct:Pose2d", r.entry("/Odometry/Robot").type);
        assertEquals("struct:Pose3d", r.entry("/Odometry/Robot3d").type);
        assertEquals("struct:Pose2d[]", r.entry("/Path/Active").type);
        assertFalse(r.entry("/Vision/CellFix").records.isEmpty());
        assertEquals(1, r.entry("/Vision/CellFixRejected").records.size());

        // Joysticks and events.
        assertEquals("int64", r.entry("/DriverStation/Joystick0/ButtonValues").type);
        boolean sawLoopDanger = false;
        boolean sawLaunch = false;
        long lastEventUs = -1;
        for (WpiLogReader.Record e : r.entry(AdvantageScopeKeys.EVENTS).records) {
            assertTrue("AdvantageScope drops events that share a timestamp", e.timestampUs > lastEventUs);
            lastEventUs = e.timestampUs;
            sawLoopDanger |= e.asString().contains("Loop 93 ms");
            sawLaunch |= e.asString().equals("driver RB: Launch all");
        }
        assertTrue(sawLoopDanger);
        assertTrue(sawLaunch);

        // Every pose stays on the field (±72 in from centre, with a little bow allowance).
        double limit = 76 * AdvantageScopeFrame.METERS_PER_INCH;
        for (WpiLogReader.Record p : r.entry("/Odometry/Robot").records) {
            double[] v = p.asDoubles();
            assertTrue("x off field: " + v[0], Math.abs(v[0]) < limit);
            assertTrue("y off field: " + v[1], Math.abs(v[1]) < limit);
        }
    }

    @Test
    public void knownPointsLogLabelsEveryPoint() throws IOException {
        File file = new File(dir(), "known-points.wpilog");
        new KnownPointsLog().write(file);
        WpiLogReader r = new WpiLogReader(Files.readAllBytes(file.toPath()));

        assertEquals(KnownPointsLog.POINTS.length, r.entry(AdvantageScopeKeys.EVENTS).records.size());
        assertEquals(6 * 3, r.entry("/Field/PedroXAxis").records.get(0).asDoubles().length);
        double[] centre = r.entry("/Odometry/Robot").records.get(0).asDoubles();
        assertEquals(0.0, centre[0], 1e-12);
        assertEquals(0.0, centre[1], 1e-12);
    }

    /** Same seed, same bytes: two runs of the simulation can be diffed. */
    @Test
    public void simulationIsDeterministic() throws IOException {
        File a = new File(dir(), "determinism-a.wpilog");
        File b = new File(dir(), "determinism-b.wpilog");
        new SimulatedMatch(1L).write(a);
        new SimulatedMatch(1L).write(b);
        assertTrue(java.util.Arrays.equals(Files.readAllBytes(a.toPath()), Files.readAllBytes(b.toPath())));
        a.delete();
        b.delete();
    }
}
