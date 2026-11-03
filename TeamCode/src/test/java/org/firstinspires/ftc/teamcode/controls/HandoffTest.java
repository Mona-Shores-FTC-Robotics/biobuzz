package org.firstinspires.ftc.teamcode.controls;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.logging.MatchLog;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HandoffTest {

    private static final long T0 = 1_000_000L;

    @Before
    @After
    public void clear() {
        Handoff.clear();
    }

    @Test
    public void noAutoMeansNoHandoff() {
        assertNull(Handoff.last());
    }

    @Test
    public void teleOpGetsWhatAutoLeft() {
        Pose end = new Pose(24, 48, Math.PI / 2);
        Handoff.record(Alliance.RED, end, null, null, T0);

        Handoff.Snapshot snapshot = Handoff.last();
        assertNotNull(snapshot);
        assertEquals(Alliance.RED, snapshot.alliance);
        assertSame(end, snapshot.pose);
        assertEquals(8_000, snapshot.ageMs(T0 + 8_000));
    }

    @Test
    public void reinitializingTeleOpStillGetsIt() {
        Handoff.record(Alliance.BLUE, null, null, null, T0);

        assertNotNull(Handoff.last());
        assertNotNull(Handoff.last());
    }

    @Test
    public void aHandoffNeverGoesStale() {
        // The 2026-11-03 R-Solo run: Basic Drive initialized 6 min 25 s after the Auto stopped,
        // and the old three-minute limit threw the pose, the alliance and the turret's zero away.
        Pose end = new Pose(14.5, 95.7, 0);
        Handoff.record(Alliance.RED, end, null, null, Long.valueOf(5), T0);

        Handoff.Snapshot snapshot = Handoff.last();
        assertNotNull(snapshot);
        assertSame(end, snapshot.pose);
        assertEquals(Alliance.RED, snapshot.alliance);
        assertEquals(Long.valueOf(5), snapshot.turretZeroCounts);
        assertEquals("6 min 25 s ago", snapshot.ageText(T0 + (6 * 60 + 25) * 1000L));
        assertEquals("60 min 0 s ago", snapshot.ageText(T0 + 3_600_000L));
        assertEquals("2 h 5 min ago", snapshot.ageText(T0 + (2 * 60 + 5) * 60_000L));
    }

    @Test
    public void ageTextIsInSecondsFirst() {
        Handoff.record(Alliance.RED, null, null, null, T0);

        assertEquals("8s ago", Handoff.last().ageText(T0 + 8_000));
        assertEquals("119s ago", Handoff.last().ageText(T0 + 119_000));
    }

    @Test
    public void aClockThatWentBackwardsStillHandsOnWithAnAgeOfZero() {
        Handoff.record(Alliance.BLUE, null, null, null, T0);

        assertNotNull(Handoff.last());
        assertEquals(0, Handoff.last().ageMs(T0 - 1));
    }

    @Test
    public void theNextAutoReplacesTheLast() {
        Handoff.record(Alliance.RED, null, null, null, T0);
        Handoff.record(Alliance.BLUE, null, null, null, T0 + 1_000);

        assertEquals(Alliance.BLUE, Handoff.last().alliance);
    }

    @Test
    public void aNullAllianceIsRecordedAsUnknown() {
        Handoff.record(null, null, null, null, T0);

        assertEquals(Alliance.UNKNOWN, Handoff.last().alliance);
    }

    @Test
    public void theTurretsZeroRidesAlongSoTeleOpKeepsTheAutosAngles() {
        Handoff.record(Alliance.RED, null, null, null, Long.valueOf(-1234), T0);

        assertEquals(Long.valueOf(-1234), Handoff.last().turretZeroCounts);
    }

    @Test
    public void anAutoWithNoTurretZeroHandsOnNone() {
        Handoff.record(Alliance.RED, null, null, null, T0);
        assertNull(Handoff.last().turretZeroCounts);

        Handoff.record(Alliance.RED, null, null, null, (Long) null, T0 + 1_000);
        assertNull(Handoff.last().turretZeroCounts);
    }

    @Test
    public void aZeroOfZeroCountsIsStillAZero() {
        Handoff.record(Alliance.RED, null, null, null, Long.valueOf(0), T0);

        assertNotNull(Handoff.last().turretZeroCounts);
        assertEquals(0L, Handoff.last().turretZeroCounts.longValue());
    }

    // ---------------------------------------------------------------- the latest loop's state

    /** One loop of an OpMode at pose (x, y, 0.5) with a turret zero of 77. */
    private static void loop(boolean fromAuto, double x, double y, String matchId, long nowMs) {
        Handoff.update(fromAuto, Alliance.BLUE, true, x, y, 0.5, true, 77L, matchId, nowMs);
    }

    @Test
    public void anAutoThatCrashedStillHandsOnWhereItWas() {
        // 15 seconds in, no stop code ran, so there is no final record: only the loop state.
        loop(true, 30, 40, "m1", T0);
        loop(true, 31, 41, "m1", T0 + 15_000);

        Handoff.Snapshot snapshot = Handoff.last();
        assertNotNull(snapshot);
        assertEquals(31, snapshot.pose.x(), 1e-9);
        assertEquals(41, snapshot.pose.y(), 1e-9);
        assertEquals(0.5, snapshot.pose.heading(), 1e-9);
        assertEquals(Alliance.BLUE, snapshot.alliance);
        assertEquals(Long.valueOf(77), snapshot.turretZeroCounts);
        assertEquals("m1", snapshot.matchId);
        assertEquals(true, snapshot.fromAuto);
    }

    @Test
    public void aCrashedAutosStateDoesNotExpireEither() {
        loop(true, 1, 2, "m1", T0);

        assertNotNull(Handoff.last());
    }

    @Test
    public void aRestartedTeleOpResumesWhereItWas() {
        Pose autoEnd = new Pose(24, 48, Math.PI / 2);
        Handoff.record(Alliance.RED, autoEnd, "m1", null, Long.valueOf(5), T0);
        // TeleOp ran for a while and moved the robot, then crashed at T0 + 50 s.
        loop(false, 90, 20, "m1", T0 + 49_980);

        Handoff.Snapshot snapshot = Handoff.last();
        assertNotNull(snapshot);
        assertEquals(90, snapshot.pose.x(), 1e-9);
        assertEquals(20, snapshot.pose.y(), 1e-9);
        assertEquals(false, snapshot.fromAuto);
    }

    @Test
    public void aTeleOpsStateLastsAndTheOlderAutoDoesNotReplaceIt() {
        Handoff.record(Alliance.RED, new Pose(24, 48, 0), "m1", null, T0);
        loop(false, 90, 20, "m1", T0 + 50_000);

        // However long after: it is the TeleOp's pose, not the older Auto's.
        assertEquals(90, Handoff.last().pose.x(), 1e-9);
        assertEquals(false, Handoff.last().fromAuto);
    }

    @Test
    public void anAutosFinalRecordWinsOverItsOwnLastLoop() {
        loop(true, 30, 40, "m1", T0);
        Pose end = new Pose(33, 44, 1.0);
        Handoff.record(Alliance.BLUE, end, "m1", null, T0 + 100);

        assertSame(end, Handoff.last().pose);
    }

    @Test
    public void aTieGoesToTheFinalRecord() {
        loop(true, 30, 40, "m1", T0);
        Pose end = new Pose(33, 44, 1.0);
        Handoff.record(Alliance.BLUE, end, "m1", null, T0);

        assertSame(end, Handoff.last().pose);
    }

    @Test
    public void aPoseThatIsNotOnTheFieldIsNotHandedOn() {
        Handoff.update(true, Alliance.RED, false, 9, 9, 9, false, 0L, "m1", T0);

        Handoff.Snapshot snapshot = Handoff.last();
        assertNotNull(snapshot);
        assertNull(snapshot.pose);
        assertNull(snapshot.turretZeroCounts);
        assertEquals(Alliance.RED, snapshot.alliance);
    }

    @Test
    public void aTurretZeroOfZeroCountsIsStillCarriedFromTheLoopState() {
        Handoff.update(false, Alliance.RED, true, 1, 2, 3, true, 0L, "m1", T0);

        assertEquals(Long.valueOf(0), Handoff.last().turretZeroCounts);
    }

    @Test
    public void anOlderMatchsPausedLogIsNotHandedToANewMatch() {
        // A finished match's Auto record, then a different match's Auto that crashed.
        MatchLog oldLog = MatchLog.disabled("old match");
        Handoff.record(Alliance.RED, null, "old-match", oldLog, T0);
        loop(true, 1, 2, "new-match", T0 + 1_000);

        Handoff.Snapshot snapshot = Handoff.last();
        assertEquals("new-match", snapshot.matchId);
        assertNull(snapshot.log);
    }

    @Test
    public void aRestartedTeleOpOfTheSameMatchStillGetsTheAutosLog() {
        MatchLog autoLog = MatchLog.disabled("auto");
        Handoff.record(Alliance.RED, null, "m1", autoLog, T0);
        loop(false, 1, 2, "m1", T0 + 20_000);

        assertSame(autoLog, Handoff.last().log);
    }

    @Test
    public void theMatchIdRidesAlongSoTeleOpsLogCanNameItsAuto() {
        Handoff.record(Alliance.RED, null, "2026-10-04_14-32-10", null, T0);

        assertEquals("2026-10-04_14-32-10", Handoff.last().matchId);
    }
}
