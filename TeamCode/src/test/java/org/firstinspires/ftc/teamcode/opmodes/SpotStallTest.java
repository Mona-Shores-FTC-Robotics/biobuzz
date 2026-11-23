package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.subsystems.FeedGateSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.FeederSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.LaunchSpeedTable;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/** {@link SpotStall}: a Square press blocked short of its spot fires Circle's AIM shot, only where it may. */
public class SpotStallTest {

    private static final double STALL_S = 0.6;
    private static final double TOL = 2.0;
    private static final double SLOW = 3.0;
    private static final double TURN = 20.0;
    private static final double PROGRESS = 1.0;

    /** One loop every 20 ms from {@code fromS} to {@code toS}, at {@code speed} and closing at {@code closingInPerS}. */
    private static boolean run(SpotStall stall, double fromS, double toS, double startRemainingIn,
                               double speed, double closingInPerS) {
        boolean stalled = false;
        for (double t = fromS; t <= toS + 1e-9; t += 0.02) {
            double remaining = startRemainingIn - closingInPerS * (t - fromS);
            stalled = stall.update(t, remaining, speed, 0.0, STALL_S, TOL, SLOW, TURN, PROGRESS);
        }
        return stalled;
    }

    @Test
    public void blockedAndStillShortOfTheSpotStallsAfterStallS() {
        SpotStall stall = new SpotStall();
        assertFalse(run(stall, 0.0, 0.5, 20.0, 0.5, 0.0));
        assertTrue(run(stall, 0.52, 0.62, 20.0, 0.5, 0.0));
        assertEquals(0.62, stall.stalledForS(0.62), 1e-6);
    }

    @Test
    public void slowButGettingThereNeverStalls() {
        // 2.5 in/s, under the speed threshold, but closing 2.5 in/s: more than an inch every 0.6 s.
        SpotStall stall = new SpotStall();
        assertFalse(run(stall, 0.0, 5.0, 20.0, 2.5, 2.5));
    }

    @Test
    public void movingOrTurningIsNotAStall() {
        SpotStall stall = new SpotStall();
        assertFalse(run(stall, 0.0, 3.0, 20.0, 10.0, 0.0));
        for (double t = 0.0; t < 3.0; t += 0.02) {
            assertFalse(stall.update(t, 20.0, 0.0, 45.0, STALL_S, TOL, SLOW, TURN, PROGRESS));
        }
    }

    @Test
    public void withinTheToleranceIsPedroFinishingNotAStall() {
        SpotStall stall = new SpotStall();
        assertFalse(run(stall, 0.0, 3.0, 1.5, 0.0, 0.0));
    }

    @Test
    public void aLoopOfMovementStartsTheWaitAgain() {
        SpotStall stall = new SpotStall();
        assertFalse(run(stall, 0.0, 0.5, 20.0, 0.0, 0.0));
        assertFalse(stall.update(0.52, 20.0, 8.0, 0.0, STALL_S, TOL, SLOW, TURN, PROGRESS));
        assertFalse(run(stall, 0.54, 1.0, 20.0, 0.0, 0.0));
        assertTrue(run(stall, 1.02, 1.2, 20.0, 0.0, 0.0));
    }

    @Test
    public void noFollowerOrNoPoseIsNeverAStall() {
        SpotStall stall = new SpotStall();
        for (double t = 0.0; t < 3.0; t += 0.02) {
            assertFalse(stall.update(t, Double.NaN, 0.0, 0.0, STALL_S, TOL, SLOW, TURN, PROGRESS));
            assertFalse(stall.update(t, 20.0, Double.NaN, 0.0, STALL_S, TOL, SLOW, TURN, PROGRESS));
        }
    }

    @Test
    public void stalledInRangeOnOurSideFires() {
        assertNull(SpotStall.refusal(true, true, -20.0, 48.0, true, 5.0, true, true));
        // Aim behind the arc: Circle's arc turn brings it in, as a Circle press in AIM does.
        assertNull(SpotStall.refusal(true, true, -20.0, 48.0, true, 150.0, false, true));
    }

    @Test
    public void stalledPastTheCentreLineEndsWithTheReason() {
        assertEquals("past the centre line", SpotStall.refusal(true, true, 3.0, 48.0, true, 5.0, true, true));
        assertEquals("past the centre line", SpotStall.refusal(true, true, 0.0, 48.0, true, 5.0, true, true));
        assertEquals("past the centre line", SpotStall.refusal(true, true, Double.NaN, 48.0, true, 5.0, true, true));
    }

    @Test
    public void stalledOutOfRangeEndsWithTheReason() {
        assertEquals("out of range (90 in)", SpotStall.refusal(true, true, -20.0, 90.0, false, 5.0, true, true));
        assertEquals("range unknown", SpotStall.refusal(true, true, -20.0, Double.NaN, false, 5.0, true, true));
    }

    @Test
    public void theOtherGuardsEachEndWithTheirReason() {
        assertEquals("turret not aiming (AIM needed)", SpotStall.refusal(false, true, -20.0, 48.0, true, 5.0, true, true));
        assertEquals("pose not referenced", SpotStall.refusal(true, false, -20.0, 48.0, true, 5.0, true, true));
        assertEquals("no aim at the CELL", SpotStall.refusal(true, true, -20.0, 48.0, true, Double.NaN, false, true));
        assertEquals("aim outside the firing arc", SpotStall.refusal(true, true, -20.0, 48.0, true, 150.0, false, false));
    }

    @Test
    public void theSpeedTablesMeasuredRangeHasNoExtension() {
        double lo = Math.min(LaunchSpeedTable.row1RangeIn, LaunchSpeedTable.row12RangeIn);
        double hi = Math.max(LaunchSpeedTable.row1RangeIn, LaunchSpeedTable.row12RangeIn);
        assertTrue(LaunchSpeedTable.measured(lo));
        assertTrue(LaunchSpeedTable.measured((lo + hi) / 2.0));
        assertTrue(LaunchSpeedTable.measured(hi));
        assertFalse("the table's trend extension is not measured", LaunchSpeedTable.measured(lo - 1.0));
        assertFalse(LaunchSpeedTable.measured(hi + 1.0));
        assertFalse(LaunchSpeedTable.measured(Double.NaN));
    }

    @Test
    public void panelsTyposAreGuarded() {
        assertEquals(0.6, SpotStall.guarded(Double.NaN, 0.6, 0.2, 5.0), 1e-9);
        assertEquals(0.2, SpotStall.guarded(-1.0, 0.6, 0.2, 5.0), 1e-9);
        assertEquals(5.0, SpotStall.guarded(60.0, 0.6, 0.2, 5.0), 1e-9);
        assertEquals(0.6, SpotStall.guarded(Double.POSITIVE_INFINITY, 0.6, 0.2, 5.0), 1e-9);
    }

    /**
     * The NECTAR gate still holds: the stall shot feeds only through Circle's request in
     * BasicDriveTeleOp (FeederSubsystem.requestFor, then the feed gate, shut while a wrong NECTAR
     * may be in), never a feed path of its own.
     */
    @Test
    public void theStallShotHasNoFeedPathAroundTheNectarGate() {
        for (String name : new String[] {"LaunchSpotDrive.java", "SpotStall.java"}) {
            String source = read(new File(opmodesDirectory(), name));
            assertFalse(name + " must not feed", source.contains("feeder.feed(") || source.contains("feeder.reverse("));
            assertFalse(name + " must not move the feed gate", source.contains("feedGate."));
        }
        assertEquals(FeedGateSubsystem.State.BLOCKING,
                FeedGateSubsystem.requestFor(FeederSubsystem.State.FEED, true));
    }

    private static String read(File file) {
        try {
            return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AssertionError("Could not read " + file, e);
        }
    }

    private static File opmodesDirectory() {
        File candidate = new File(System.getProperty("user.dir"));
        for (int depth = 0; depth < 4 && candidate != null; depth++) {
            for (String root : new String[] {"src/main/java", "TeamCode/src/main/java"}) {
                File dir = new File(candidate, root + "/org/firstinspires/ftc/teamcode/opmodes");
                if (dir.isDirectory()) return dir;
            }
            candidate = candidate.getParentFile();
        }
        throw new AssertionError("Could not locate the opmodes sources from " + System.getProperty("user.dir"));
    }
}
