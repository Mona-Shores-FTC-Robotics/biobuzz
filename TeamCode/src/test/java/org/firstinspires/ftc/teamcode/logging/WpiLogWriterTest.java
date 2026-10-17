package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Checks {@link WpiLogWriter} byte for byte against the worked examples in the WPILib Data Log
 * spec v1.0. If these pass, AdvantageScope and every other WPILOG reader agree with us on the
 * framing; what the entries mean is tested elsewhere.
 */
public class WpiLogWriterTest {

    private static final long ONE_SECOND_US = 1_000_000L;

    /** Spec § Header: "57 50 49 4c 4f 47 00 01 00 00 00 00". */
    @Test
    public void emptyHeaderMatchesSpec() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new WpiLogWriter(out, "");
        assertArrayEquals(bytes(0x57, 0x50, 0x49, 0x4c, 0x4f, 0x47, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00),
                out.toByteArray());
    }

    /** Spec § Start: an int64 entry "test", id 1, at 1 s — 32 bytes. */
    @Test
    public void startRecordMatchesSpec() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        WpiLogWriter w = new WpiLogWriter(out, "");
        int id = w.start("test", "int64", "", ONE_SECOND_US);
        assertEquals(1, id);
        assertArrayEquals(bytes(
                0x20, 0x00, 0x1a, 0x40, 0x42, 0x0f,
                0x00, 0x01, 0x00, 0x00, 0x00,
                0x04, 0x00, 0x00, 0x00, 0x74, 0x65, 0x73, 0x74,
                0x05, 0x00, 0x00, 0x00, 0x69, 0x6e, 0x74, 0x36, 0x34,
                0x00, 0x00, 0x00, 0x00), afterHeader(out));
    }

    /** Spec § Records: int64 value 3 for id 1 at 1 s — 14 bytes. */
    @Test
    public void dataRecordMatchesSpec() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        WpiLogWriter w = new WpiLogWriter(out, "");
        int id = w.start("test", "int64", "", ONE_SECOND_US);
        int startLength = out.size();
        w.appendInt64(id, 3L, ONE_SECOND_US);
        byte[] all = out.toByteArray();
        assertArrayEquals(bytes(0x20, 0x01, 0x08, 0x40, 0x42, 0x0f, 0x03, 0, 0, 0, 0, 0, 0, 0),
                Arrays.copyOfRange(all, startLength, all.length));
    }

    /** Spec § Finish: id 1 at 1 s — 11 bytes. */
    @Test
    public void finishRecordMatchesSpec() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        WpiLogWriter w = new WpiLogWriter(out, "");
        int before = out.size();
        w.finish(1, ONE_SECOND_US);
        byte[] all = out.toByteArray();
        assertArrayEquals(bytes(0x20, 0x00, 0x05, 0x40, 0x42, 0x0f, 0x01, 0x01, 0x00, 0x00, 0x00),
                Arrays.copyOfRange(all, before, all.length));
    }

    /** Spec § Set Metadata: {"source":"NT"} for id 1 at 1 s — 30 bytes. */
    @Test
    public void setMetadataRecordMatchesSpec() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        WpiLogWriter w = new WpiLogWriter(out, "");
        int before = out.size();
        w.setMetadata(1, "{\"source\":\"NT\"}", ONE_SECOND_US);
        byte[] all = out.toByteArray();
        assertArrayEquals(bytes(0x20, 0x00, 0x18, 0x40, 0x42, 0x0f, 0x02, 0x01, 0x00, 0x00, 0x00,
                        0x0f, 0x00, 0x00, 0x00,
                        0x7b, 0x22, 0x73, 0x6f, 0x75, 0x72, 0x63, 0x65, 0x22, 0x3a, 0x22, 0x4e, 0x54, 0x22, 0x7d),
                Arrays.copyOfRange(all, before, all.length));
    }

    @Test
    public void fieldWidthsGrowOnlyAsNeeded() {
        assertEquals(1, WpiLogWriter.byteLength(0, 8));
        assertEquals(1, WpiLogWriter.byteLength(255, 8));
        assertEquals(2, WpiLogWriter.byteLength(256, 8));
        assertEquals(3, WpiLogWriter.byteLength(ONE_SECOND_US, 8));
        assertEquals(5, WpiLogWriter.byteLength(1L << 32, 8));
        assertEquals(4, WpiLogWriter.byteLength(0xFFFFFFFFL, 4));
    }

    /** Every type the writer offers comes back through an independent reader unchanged. */
    @Test
    public void everyTypeRoundTrips() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        WpiLogWriter w = new WpiLogWriter(out, "round trip");
        long t = 123_456_789_012L; // 5-byte timestamp, well past a match
        w.appendBoolean(w.start("b", "boolean", "", t), true, t);
        w.appendInt64(w.start("i", "int64", "", t), -42L, t);
        w.appendDouble(w.start("d", "double", "", t), 3.25, t);
        w.appendString(w.start("s", "string", "", t), "héllo", t);
        w.appendDoubleArray(w.start("da", "double[]", "", t), new double[] {1.5, -2.5}, t);
        w.appendStringArray(w.start("sa", "string[]", "", t), new String[] {"a", "bc"}, t);

        WpiLogReader r = new WpiLogReader(out.toByteArray());
        assertEquals(0x0100, r.version);
        assertEquals("round trip", r.extraHeader);
        assertEquals(true, r.entry("b").records.get(0).asBoolean());
        assertEquals(-42L, r.entry("i").records.get(0).asInt64());
        assertEquals(3.25, r.entry("d").records.get(0).asDouble(), 0.0);
        assertEquals("héllo", r.entry("s").records.get(0).asString());
        assertArrayEquals(new double[] {1.5, -2.5}, r.entry("da").records.get(0).asDoubles(), 0.0);
        assertEquals(t, r.entry("d").records.get(0).timestampUs);
        assertEquals(4 + 4 + 1 + 4 + 2, r.entry("sa").records.get(0).payload.length);
    }

    private static byte[] afterHeader(ByteArrayOutputStream out) {
        byte[] all = out.toByteArray();
        return Arrays.copyOfRange(all, 12, all.length);
    }

    private static byte[] bytes(int... values) {
        byte[] b = new byte[values.length];
        for (int i = 0; i < values.length; i++) b[i] = (byte) values[i];
        return b;
    }
}
