package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.firstinspires.ftc.teamcode.opmodes.auto.generated.PartnerRLaunchParkAuto;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.PartnerRBrokenDownAuto;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Every Auto's simulation request ({@code TeamCode/sim-requests/<name>.json}) is one the "Simulate Auto"
 * workflow can run: it belongs to an Auto we run, names a generated class and a robot design the simulator
 * knows, and each {@code partners} entry names a stand-in from {@code TeamCode/autos/partners/} (the curated
 * set the mentor picks per Auto, 3 Nov 2026). A typo would otherwise fail only in the workflow, after the
 * save. The format is {@link SimRunTest}'s, and each request must read as {@link SimRunTest#entries} reads it.
 *
 * <p>Below that, the partner entries without running a simulation: the partner's last shot ({@code lastShotS})
 * and how it fails ({@code fails}), read and checked, and what the simulator derives from the last shot.
 */
public class SimRequestsTest {

    @Test
    @SuppressWarnings("unchecked")
    public void everyRequestNamesAnAutoItsDesignAndRealStandIns() throws Exception {
        File teamCode = TeamCodeDir.get();
        File autos = new File(teamCode, "autos");
        File partners = new File(autos, "partners");
        File[] files = new File(teamCode, "sim-requests").listFiles((d, n) -> n.endsWith(".json"));
        assertTrue("TeamCode/sim-requests/ is missing or empty", files != null && files.length > 0);

        Set<String> designs = AutoStudyTest.designs().keySet();
        List<String> problems = new ArrayList<>();
        for (File file : files) {
            String name = file.getName().substring(0, file.getName().length() - ".json".length());
            Map<String, Object> request;
            try {
                request = (Map<String, Object>) MiniJson.parse(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));
            } catch (RuntimeException e) {
                problems.add(file.getName() + ": not JSON (" + e.getMessage() + ")");
                continue;
            }
            // The request is the Auto's own: its .pp beside it in TeamCode/autos/, under the same name.
            if (!("TeamCode/autos/" + name + ".pp").equals(request.get("pp"))) {
                problems.add(file.getName() + ": pp should be TeamCode/autos/" + name + ".pp, not " + request.get("pp"));
            }
            if (!new File(autos, name + ".pp").isFile()) {
                problems.add(file.getName() + ": there is no TeamCode/autos/" + name + ".pp; a request belongs to an Auto we run");
            }
            String spec = (String) request.get("spec");
            if (spec == null || spec.isEmpty()) {
                problems.add(file.getName() + ": no spec");
                continue;
            }
            // Our Auto, then the drive profile; a comma names a partner the old way, which still runs, but the
            // curated partners belong in "partners".
            String ours = spec.split("@")[0].split("\\|")[0];
            if (ours.contains(",")) {
                problems.add(file.getName() + ": spec \"" + spec + "\" names a partner; the curated partners go in \"partners\"");
                ours = ours.split(",")[0];
            }
            if (!generatedExists(ours)) {
                problems.add(file.getName() + ": spec names " + ours + ", which is not a generated Auto");
            }
            checkDesign(problems, file, request.get("design"), designs);
            try {
                SimRunTest.entries(request);  // lastShotS, fails and timing, as a run reads them
            } catch (IllegalArgumentException e) {
                problems.add(file.getName() + ": " + e.getMessage());
            }
            Object list = request.get("partners");
            if (list == null) continue;  // a request with no partner runs is fine: the Auto alone
            if (!(list instanceof List)) {
                problems.add(file.getName() + ": \"partners\" should be a list of entries");
                continue;
            }
            for (Object o : (List<Object>) list) {
                if (!(o instanceof Map)) {
                    problems.add(file.getName() + ": a partners entry is not an object: " + o);
                    continue;
                }
                Map<String, Object> entry = (Map<String, Object>) o;
                Object partner = entry.get("partner");
                if (!(partner instanceof String) || ((String) partner).isEmpty()) {
                    problems.add(file.getName() + ": a partners entry has no \"partner\" class: " + o);
                    continue;
                }
                String partnerClass = (String) partner;
                if (!generatedExists(partnerClass)) {
                    problems.add(file.getName() + ": partner " + partnerClass + " is not a generated Auto");
                    continue;
                }
                // A partner is a stand-in from TeamCode/autos/partners/, never one of the Autos we run.
                String source = (String) Class.forName(AutoStudyTest.PKG + partnerClass).getField("SOURCE").get(null);
                if (!new File(partners, source).isFile()) {
                    problems.add(file.getName() + ": partner " + partnerClass + " comes from " + source
                            + ", which is not in TeamCode/autos/partners/ (a partner is a stand-in, not an Auto we run)");
                }
                checkDesign(problems, file, entry.get("design"), designs);
            }
        }
        if (!problems.isEmpty()) fail(String.join("\n", problems));
    }

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
        still.derive(PartnerRBrokenDownAuto.class, RobotDesign.real());
        assertTrue(Double.isNaN(still.derivedStartDelayS));
    }

    /** The waits a results row reads: a partner wait that ran out is not a trigger, and CellUp is our first CellUp. */
    @Test
    public void aPartnerWaitReadsAsRunningOutAndCellUpIsFound() {
        List<Object> waits = SimRunTest.waits(Arrays.asList(
                " 1.20 wait Wait for TIP or CELL up",
                "12.00 Wait for TIP or CELL up: 12.0 s on the match clock after 10.80 s",
                "14.00 wait Second",
                "15.50 Second: CellUp after 1.50 s"));
        @SuppressWarnings("unchecked")
        Map<String, Object> first = (Map<String, Object>) waits.get(0);
        assertEquals("12.0 s on the match clock", first.get("outcome"));
        assertEquals(false, first.get("fired"));
        assertEquals(15.5, (Double) SimRunTest.firstCellUp(waits), 1e-9);
    }

    private static void checkDesign(List<String> problems, File file, Object design, Set<String> designs) {
        if (design != null && !designs.contains(String.valueOf(design))) {
            problems.add(file.getName() + ": design \"" + design + "\" is not one the simulator knows " + designs);
        }
    }

    private static boolean generatedExists(String className) {
        try {
            Class.forName(AutoStudyTest.PKG + className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
