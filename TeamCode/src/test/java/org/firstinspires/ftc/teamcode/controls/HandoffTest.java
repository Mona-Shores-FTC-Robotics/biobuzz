package org.firstinspires.ftc.teamcode.controls;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import com.pedropathing.math.Pose;

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
        assertNull(Handoff.fresh(T0));
    }

    @Test
    public void teleOpGetsWhatAutoLeft() {
        Pose end = new Pose(24, 48, Math.PI / 2);
        Handoff.record(Alliance.RED, end, T0);

        Handoff.Snapshot snapshot = Handoff.fresh(T0 + 8_000);
        assertNotNull(snapshot);
        assertEquals(Alliance.RED, snapshot.alliance);
        assertSame(end, snapshot.pose);
        assertEquals(8_000, snapshot.ageMs(T0 + 8_000));
    }

    @Test
    public void reinitializingTeleOpStillGetsIt() {
        Handoff.record(Alliance.BLUE, null, T0);

        assertNotNull(Handoff.fresh(T0 + 1_000));
        assertNotNull(Handoff.fresh(T0 + 60_000));
    }

    @Test
    public void aStaleHandoffIsIgnored() {
        Handoff.record(Alliance.BLUE, new Pose(0, 0, 0), T0);

        assertNotNull(Handoff.fresh(T0 + Handoff.MAX_AGE_MS));
        assertNull(Handoff.fresh(T0 + Handoff.MAX_AGE_MS + 1));
    }

    @Test
    public void aClockThatWentBackwardsIsNotTrusted() {
        Handoff.record(Alliance.BLUE, null, T0);

        assertNull(Handoff.fresh(T0 - 1));
    }

    @Test
    public void theNextAutoReplacesTheLast() {
        Handoff.record(Alliance.RED, null, T0);
        Handoff.record(Alliance.BLUE, null, T0 + 1_000);

        assertEquals(Alliance.BLUE, Handoff.fresh(T0 + 2_000).alliance);
    }

    @Test
    public void aNullAllianceIsRecordedAsUnknown() {
        Handoff.record(null, null, T0);

        assertEquals(Alliance.UNKNOWN, Handoff.fresh(T0).alliance);
    }
}
