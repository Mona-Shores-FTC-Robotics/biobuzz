package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.PartnerRLaunchParkAuto;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.PartnerRStillAuto;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A simulate request's partner entries, without running a simulation: the partner's last shot
 * ({@code lastShotS}) and how it fails ({@code fails}), read and checked, and what the simulator
 * derives from the last shot. Fast, so it runs with the unit tests; {@link SimRunTest} runs them.
 */
public class SimRequestTest {

    private static Map<String, Object> entry(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) m.put((String) kv[i], kv[i + 1]);
        return m;
    }

    private static List<SimRunTest.Entry> entries(Map<String, Object> partner) throws Exception {
        Map<String, Object> request = entry("spec", "LPartnerAuto@50", "partners", new ArrayList<Object>(Arrays.asList(partner)));
        return SimRunTest.entries(request);
    }

    @Test
    public void aLastShotIsReadAndNamesTheFolder() throws Exception {
        List<SimRunTest.Entry> list = entries(entry("partner", "PartnerRLaunchParkAuto", "lastShotS", 8.0));
        assertEquals(2, list.size());
        AutoStudyTest.PartnerTiming t = list.get(1).timing;
        assertEquals(8, t.lastShotS, 1e-9);
        assertNull(t.fails);
        assertEquals("partner-r-launch-park/last8s", list.get(1).folder);
    }

    @Test
    public void missesIsTheOnlyFailure() throws Exception {
        AutoStudyTest.PartnerTiming t = entries(entry("partner", "PartnerRLaunchParkAuto", "lastShotS", 8.0, "fails", "misses")).get(1).timing;
        assertEquals("misses", t.fails);
        try {
            entries(entry("partner", "PartnerRLaunchParkAuto", "fails", "late"));
            fail("\"late\" is not a failure the simulator models");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains("still stand-in"));
        }
    }

    @Test
    public void aLastShotPastAutoIsRefused() throws Exception {
        try {
            entries(entry("partner", "PartnerRLaunchParkAuto", "lastShotS", 31.0));
            fail();
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains("lastShotS"));
        }
    }

    /** At the robot's own interval, starting late by the difference: the last of four leaves at the second asked. */
    @Test
    public void aLateLastShotStartsLateAtTheRobotsInterval() {
        RobotDesign real = RobotDesign.real();
        AutoStudyTest.PartnerTiming t = AutoStudyTest.PartnerTiming.of(null, entry("lastShotS", 12.0));
        t.derive(PartnerRLaunchParkAuto.class, real);
        assertEquals(real.shotIntervalS, t.derivedLaunchS, 1e-9);
        assertEquals(12, t.derivedStartDelayS + real.spinUpS + 3 * t.derivedLaunchS, 1e-9);
    }

    /** Sooner than the robot's own interval allows: from PLAY, faster volleys. */
    @Test
    public void anEarlyLastShotFiresFasterFromPlay() {
        RobotDesign real = RobotDesign.real();
        AutoStudyTest.PartnerTiming t = AutoStudyTest.PartnerTiming.of(null, entry("lastShotS", 2.0));
        t.derive(PartnerRLaunchParkAuto.class, real);
        assertEquals(0, t.derivedStartDelayS, 1e-9);
        assertEquals(2, real.spinUpS + 3 * t.derivedLaunchS, 1e-9);
    }

    /** A raw knob given in "timing" wins over the derivation; a still stand-in derives nothing. */
    @Test
    public void givenKnobsWinAndAStillPartnerDerivesNothing() {
        AutoStudyTest.PartnerTiming t = AutoStudyTest.PartnerTiming.of(entry("launchS", 1.5), entry("lastShotS", 12.0));
        t.derive(PartnerRLaunchParkAuto.class, RobotDesign.real());
        assertEquals(1.5, t.derivedLaunchS, 1e-9);
        AutoStudyTest.PartnerTiming still = AutoStudyTest.PartnerTiming.of(null, entry("lastShotS", 8.0));
        still.derive(PartnerRStillAuto.class, RobotDesign.real());
        assertTrue(Double.isNaN(still.derivedStartDelayS));
    }

    /** The waits a results row reads: a partner wait that ran out is not a trigger, and CellUp is our first CellUp. */
    @Test
    public void aPartnerWaitReadsAsRunningOutAndCellUpIsFound() {
        List<Object> waits = SimRunTest.waits(Arrays.asList(
                " 1.20 wait Wait for TIP or CELL up",
                "12.00 Wait for TIP or CELL up: partner wait until 12.0 s after 10.80 s",
                "14.00 wait Second",
                "15.50 Second: CellUp after 1.50 s"));
        @SuppressWarnings("unchecked")
        Map<String, Object> first = (Map<String, Object>) waits.get(0);
        assertEquals("partner wait until 12.0 s", first.get("outcome"));
        assertEquals(false, first.get("fired"));
        assertEquals(15.5, (Double) SimRunTest.firstCellUp(waits), 1e-9);
    }

    /** Every committed request still reads, so a CI run never fails on a typo. */
    @Test
    public void everyCommittedRequestReads() throws Exception {
        File dir = requestsDir();
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        assertTrue("no requests in " + dir, files != null && files.length > 0);
        for (File f : files) {
            @SuppressWarnings("unchecked")
            Map<String, Object> request = (Map<String, Object>) MiniJson.parse(
                    new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8));
            SimRunTest.entries(request);
        }
    }

    private static File requestsDir() {
        File candidate = new File(System.getProperty("user.dir"));
        for (int depth = 0; depth < 4 && candidate != null; depth++) {
            for (String path : new String[]{"sim-requests", "TeamCode/sim-requests"}) {
                File dir = new File(candidate, path);
                if (dir.isDirectory()) return dir.getAbsoluteFile();
            }
            candidate = candidate.getParentFile();
        }
        throw new AssertionError("TeamCode/sim-requests not found from " + System.getProperty("user.dir"));
    }
}
