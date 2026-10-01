package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * The robot's match log, run the way {@code RobotOpMode} runs it: the loop fills and commits frames,
 * the background writer turns them into a {@code .wpilog}, read back here with the independent
 * {@link WpiLogReader}.
 */
public class MatchLogTest {

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    private long nowUs;
    private final MatchLog.Clock clock = () -> nowUs;

    private static Map<String, String> metadata() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("OpMode", "Test TeleOp");
        m.put("RobotConfig", "robot_19429");
        return m;
    }

    private void loop(MatchLog log) {
        nowUs += 20_000;
        log.commit();
    }

    @Test
    public void aMatchReadsBackWithItsStatePoseKeysAndEvents() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatchLog log = MatchLog.toStream("test", out, "test", metadata(), clock);

        log.event("OpMode init: Test TeleOp");
        for (int i = 0; i < 5; i++) {
            log.match(MatchLog.Mode.DISABLED, 0);
            loop(log);
        }
        for (int i = 0; i < 10; i++) {
            log.match(MatchLog.Mode.TELEOP, AdvantageScopeKeys.allianceStation(true, 1));
            log.pose(10 + i, 20, Math.PI / 2);
            log.loopMs(12.5);
            log.put("/Drive/Speed", i);
            if (i == 3) log.event("PLAY");
            loop(log);
        }
        log.close("OpMode stopped", 5000);
        assertNull(log.failure());

        WpiLogReader r = new WpiLogReader(out.toByteArray());
        assertEquals(2, r.entry(AdvantageScopeKeys.ROBOT_MODE).records.size()); // disabled, teleop
        assertEquals("teleop", last(r, AdvantageScopeKeys.ROBOT_MODE).asString());
        assertTrue(last(r, AdvantageScopeKeys.ENABLED).asBoolean());
        assertEquals(1L, last(r, AdvantageScopeKeys.ALLIANCE_STATION).asInt64());
        assertEquals(10, r.entry("/Odometry/Robot").records.size());
        assertEquals("struct:Pose2d", r.entry("/Odometry/Robot").type);
        assertEquals(19.0, last(r, "/Odometry/PedroInches/X").asDouble(), 0.0);
        assertEquals(9.0, last(r, "/Drive/Speed").asDouble(), 0.0);
        assertEquals(12.5, last(r, "/Robot/LoopMs").asDouble(), 0.0);
        assertEquals("Test TeleOp", r.entry(AdvantageScopeKeys.METADATA_PREFIX + "OpMode").records.get(0).asString());

        List<String> events = strings(r, AdvantageScopeKeys.EVENTS);
        assertEquals(java.util.Arrays.asList("OpMode init: Test TeleOp", "PLAY", "OpMode stopped"), events);
    }

    @Test
    public void gamepadsAreWrittenOnlyWhenTheyChange() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatchLog log = MatchLog.toStream("test", out, "", Collections.<String, String>emptyMap(), clock);
        for (int i = 0; i < 20; i++) {
            log.gamepad1().a = i >= 10 && i < 12;
            log.gamepad1().leftStickY = i < 15 ? 0.0 : -1.0;
            loop(log);
        }
        log.close(null, 5000);

        WpiLogReader r = new WpiLogReader(out.toByteArray());
        // First loop, A down, A up, stick moved.
        assertEquals(4, r.entry(AdvantageScopeKeys.JOYSTICK_PREFIX + "0/ButtonValues").records.size());
        assertEquals(1, r.entry(AdvantageScopeKeys.JOYSTICK_PREFIX + "1/ButtonValues").records.size());
    }

    @Test
    public void severalEventsInOneLoopAllSurviveInOrder() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatchLog log = MatchLog.toStream("test", out, "", null, clock);
        log.event("one");
        log.event("two");
        log.event("three");
        loop(log);
        log.close(null, 5000);

        WpiLogReader.Entry e = new WpiLogReader(out.toByteArray()).entry(AdvantageScopeKeys.EVENTS);
        assertEquals(java.util.Arrays.asList("one", "two", "three"),
                strings(new WpiLogReader(out.toByteArray()), AdvantageScopeKeys.EVENTS));
        assertTrue(e.records.get(1).timestampUs > e.records.get(0).timestampUs);
        assertTrue(e.records.get(2).timestampUs > e.records.get(1).timestampUs);
    }

    /** A writer that cannot keep up costs the loop data, never time. */
    @Test
    public void aStalledWriterDropsLoopsInsteadOfBlockingTheLoop() throws Exception {
        CountDownLatch release = new CountDownLatch(1);
        OutputStream stuck = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                try {
                    release.await();
                } catch (InterruptedException e) {
                    throw new IOException(e);
                }
            }
        };
        MatchLog log = MatchLog.toStream("stuck", stuck, "", null, clock);
        long started = System.nanoTime();
        for (int i = 0; i < MatchLog.FRAMES * 3; i++) {
            log.put("/X", i);
            loop(log);
        }
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
        assertTrue("the loop waited " + tookMs + " ms", tookMs < 1000);
        assertTrue(log.droppedLoops() > 0);
        release.countDown();
        log.close(null, 5000);
    }

    /** A failing file stops the log, says why, and the loop carries on. */
    @Test
    public void aWriteFailureStopsLoggingAndNeverReachesTheLoop() throws Exception {
        OutputStream broken = new OutputStream() {
            int written;

            @Override
            public void write(int b) throws IOException {
                if (++written > 2000) throw new IOException("storage full");
            }
        };
        MatchLog log = MatchLog.toStream("broken", broken, "", null, clock);
        for (int i = 0; i < 500 && log.failure() == null; i++) {
            log.pose(i, i, 0);
            log.put("/X", i);
            loop(log);
            Thread.sleep(1);
        }
        assertNotNull("the failure is reported", log.failure());
        assertTrue(log.failure(), log.failure().contains("storage full"));
        assertFalse(log.isLogging());
        for (int i = 0; i < 100; i++) {
            log.put("/X", i);
            log.event("still running");
            loop(log);
        }
        log.close("stop", 100);
    }

    @Test
    public void aDisabledLogAcceptsEverythingAndRecordsNothing() {
        MatchLog log = MatchLog.disabled("no storage");
        for (int i = 0; i < MatchLog.MAX_EVENTS_PER_LOOP * 3; i++) log.event("e" + i);
        for (int i = 0; i < MatchLog.MAX_CHANNELS * 2; i++) log.put("/K" + i, i);
        log.pose(1, 2, 3);
        loop(log);
        log.close("stop", 100);
        assertEquals("no storage", log.failure());
        assertEquals(0, log.framesWritten());
    }

    @Test
    public void aFileIsCreatedWithItsFolder() throws Exception {
        File file = new File(tmp.getRoot(), "logs/nested/Test_2026-10-01_10-00-00.wpilog");
        MatchLog log = MatchLog.toFile(file, "test", metadata(), clock);
        assertTrue(log.failure(), log.isLogging());
        log.match(MatchLog.Mode.AUTONOMOUS, 4);
        loop(log);
        log.close("done", 5000);
        WpiLogReader r = new WpiLogReader(Files.readAllBytes(file.toPath()));
        assertEquals("autonomous", last(r, AdvantageScopeKeys.ROBOT_MODE).asString());
    }

    @Test
    public void aFileThatCannotBeOpenedGivesADisabledLogSayingWhy() throws Exception {
        File notAFolder = tmp.newFile("plain-file");
        MatchLog log = MatchLog.toFile(new File(notAFolder, "x.wpilog"), "", null, clock);
        assertFalse(log.isLogging());
        assertNotNull(log.failure());
        loop(log);
    }

    @Test
    public void filesAreNamedByOpModeAndTimeAndNeverOverwritten() throws Exception {
        File dir = tmp.newFolder("logs");
        File first = MatchLogFiles.next(dir, "Drive TeleOp", 1790000000000L);
        assertTrue(first.getName(), first.getName().startsWith("Drive_TeleOp_"));
        assertTrue(first.getName(), first.getName().endsWith(".wpilog"));
        assertTrue(first.createNewFile());
        File second = MatchLogFiles.next(dir, "Drive TeleOp", 1790000000000L);
        assertFalse(first.equals(second));
    }

    private static WpiLogReader.Record last(WpiLogReader r, String key) {
        List<WpiLogReader.Record> records = r.entry(key).records;
        return records.get(records.size() - 1);
    }

    private static List<String> strings(WpiLogReader r, String key) {
        List<String> out = new ArrayList<>();
        for (WpiLogReader.Record rec : r.entry(key).records) out.add(rec.asString());
        return out;
    }
}
