package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Metadata sent after the file is open lands on the Metadata tab beside what it opened with. */
public class MatchLogMetadataTest {

    private long nowUs;
    private final MatchLog.Clock clock = () -> nowUs;

    private void loop(MatchLog log) {
        nowUs += 20_000;
        log.commit();
    }

    @Test
    public void metadataSentDuringTheRunReadsBackAsMetadata() {
        Map<String, String> opening = new LinkedHashMap<>();
        opening.put("OpMode", "Test Auto");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatchLog log = MatchLog.toStream("test", out, "test", opening, clock);

        loop(log);
        log.metadata("Hardware/DriveSubsystem", "Pinpoint present");
        log.metadata("BatteryAtInit", "12.83 V");
        loop(log);
        log.metadata("Tunable/IntakeSubsystem.inPower", "1.0");
        log.metadata("Tunable/IntakeSubsystem.inPower", "0.8");   // edited in Panels: the latest shows
        loop(log);
        log.close("stop", 5000);
        assertNull(log.failure());

        WpiLogReader r = new WpiLogReader(out.toByteArray());
        assertEquals("Test Auto", last(r, "/RealMetadata/OpMode"));
        assertEquals("Pinpoint present", last(r, "/RealMetadata/Hardware/DriveSubsystem"));
        assertEquals("12.83 V", last(r, "/RealMetadata/BatteryAtInit"));
        assertEquals("0.8", last(r, "/RealMetadata/Tunable/IntakeSubsystem.inPower"));
        assertEquals("string", r.entry("/RealMetadata/Hardware/DriveSubsystem").type);
    }

    @Test
    public void moreMetadataThanOneLoopCarriesIsCountedNotThrown() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatchLog log = MatchLog.toStream("test", out, "test", null, clock);
        for (int i = 0; i < MatchLog.MAX_METADATA_PER_LOOP + 5; i++) {
            log.metadata("Tunable/K" + i, "v");
        }
        loop(log);
        log.close("stop", 5000);
        assertNull(log.failure());
        WpiLogReader r = new WpiLogReader(out.toByteArray());
        assertEquals("v", last(r, "/RealMetadata/Tunable/K0"));
        assertEquals("v", last(r, "/RealMetadata/Tunable/K" + (MatchLog.MAX_METADATA_PER_LOOP - 1)));
        assertNull(r.entries.get("/RealMetadata/Tunable/K" + MatchLog.MAX_METADATA_PER_LOOP));
    }

    private static String last(WpiLogReader r, String key) {
        List<WpiLogReader.Record> records = r.entry(key).records;
        return records.get(records.size() - 1).asString();
    }
}
