package org.firstinspires.ftc.teamcode.logging;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Writes the WPILib data log format ({@code .wpilog}), version 1.0, the format AdvantageScope
 * reads natively and the one Systemcore robots write.
 *
 * <p>This is the byte layer only: entries are started by id and records are appended by id.
 * {@link WpiLog} is the keyed layer on top ({@code put("Shooter/RPM", ...)}) that everything else
 * should use.
 *
 * <p>Follows <i>WPILib Data Log File Format Specification, Version 1.0</i>
 * ({@code allwpilib/datalog/doc/datalog.adoc}) exactly:
 * <ul>
 *   <li>Header: {@code "WPILOG"}, version {@code 0x0100}, a 4-byte extra-header length, then the
 *       extra header as UTF-8.</li>
 *   <li>Record: one bitfield byte giving the width of the three fields that follow (entry id 1–4
 *       bytes, payload size 1–4 bytes, timestamp 1–8 bytes), then those fields, then the payload.
 *       Each field uses the fewest bytes that hold its value.</li>
 *   <li>Entry id 0 is reserved for control records (Start, Finish, Set Metadata).</li>
 *   <li>Everything is little-endian. Timestamps are integer microseconds.</li>
 * </ul>
 *
 * <p>Deliberately has no dependency on the FTC SDK and writes to any {@link OutputStream}, which is
 * what lets {@code WpiLogWriterTest} check the exact bytes against the spec's own examples and lets
 * {@code SimulatedMatchLog} produce a real file on a laptop. It is not thread-safe: one thread owns
 * a writer, which on the robot will be the background logging thread, never the loop.
 */
public final class WpiLogWriter implements Closeable {

    /** Version 1.0: the major version in the high byte, minor in the low byte. */
    public static final int VERSION = 0x0100;

    private static final byte[] MAGIC = "WPILOG".getBytes(StandardCharsets.US_ASCII);

    private static final int CONTROL_START = 0;
    private static final int CONTROL_FINISH = 1;
    private static final int CONTROL_SET_METADATA = 2;

    private final OutputStream out;
    /** Scratch space for a record header and small payloads; grown on demand, never shrunk. */
    private byte[] scratch = new byte[256];
    private int nextEntryId = 1;
    private boolean closed = false;

    /**
     * Writes the file header immediately.
     *
     * @param extraHeader free text stored in the header; may be empty
     */
    public WpiLogWriter(OutputStream out, String extraHeader) throws IOException {
        this.out = out;
        byte[] extra = extraHeader.getBytes(StandardCharsets.UTF_8);
        out.write(MAGIC);
        writeLE(VERSION, 2);
        writeLE(extra.length, 4);
        out.write(extra);
    }

    // ---- Control records --------------------------------------------------------------------

    /**
     * Declares a new entry and returns its id. Must precede any record for that id.
     *
     * @param type a WPILOG type string: {@code double}, {@code boolean}, {@code int64},
     *             {@code float}, {@code string}, {@code double[]}, {@code struct:Pose2d}, …
     * @param metadata usually empty; JSON by convention if not
     */
    public int start(String name, String type, String metadata, long timestampUs) throws IOException {
        int id = nextEntryId++;
        byte[] n = utf8(name);
        byte[] t = utf8(type);
        byte[] m = utf8(metadata);
        int size = 1 + 4 + 4 + n.length + 4 + t.length + 4 + m.length;
        writeRecordHeader(0, size, timestampUs);
        out.write(CONTROL_START);
        writeLE(id, 4);
        writeLE(n.length, 4);
        out.write(n);
        writeLE(t.length, 4);
        out.write(t);
        writeLE(m.length, 4);
        out.write(m);
        return id;
    }

    /** Marks an entry as finished; no further records may use {@code id}. */
    public void finish(int id, long timestampUs) throws IOException {
        writeRecordHeader(0, 5, timestampUs);
        out.write(CONTROL_FINISH);
        writeLE(id, 4);
    }

    /** Replaces an entry's metadata string. */
    public void setMetadata(int id, String metadata, long timestampUs) throws IOException {
        byte[] m = utf8(metadata);
        writeRecordHeader(0, 1 + 4 + 4 + m.length, timestampUs);
        out.write(CONTROL_SET_METADATA);
        writeLE(id, 4);
        writeLE(m.length, 4);
        out.write(m);
    }

    // ---- Data records -----------------------------------------------------------------------

    public void appendRaw(int id, byte[] data, int offset, int length, long timestampUs)
            throws IOException {
        writeRecordHeader(id, length, timestampUs);
        out.write(data, offset, length);
    }

    public void appendRaw(int id, byte[] data, long timestampUs) throws IOException {
        appendRaw(id, data, 0, data.length, timestampUs);
    }

    public void appendBoolean(int id, boolean value, long timestampUs) throws IOException {
        writeRecordHeader(id, 1, timestampUs);
        out.write(value ? 1 : 0);
    }

    public void appendInt64(int id, long value, long timestampUs) throws IOException {
        writeRecordHeader(id, 8, timestampUs);
        writeLE(value, 8);
    }

    public void appendFloat(int id, float value, long timestampUs) throws IOException {
        writeRecordHeader(id, 4, timestampUs);
        writeLE(Float.floatToIntBits(value), 4);
    }

    public void appendDouble(int id, double value, long timestampUs) throws IOException {
        writeRecordHeader(id, 8, timestampUs);
        writeLE(Double.doubleToLongBits(value), 8);
    }

    public void appendString(int id, String value, long timestampUs) throws IOException {
        byte[] s = utf8(value);
        appendRaw(id, s, 0, s.length, timestampUs);
    }

    public void appendBooleanArray(int id, boolean[] values, long timestampUs) throws IOException {
        writeRecordHeader(id, values.length, timestampUs);
        for (boolean v : values) out.write(v ? 1 : 0);
    }

    public void appendInt64Array(int id, long[] values, long timestampUs) throws IOException {
        writeRecordHeader(id, values.length * 8, timestampUs);
        for (long v : values) writeLE(v, 8);
    }

    public void appendFloatArray(int id, float[] values, long timestampUs) throws IOException {
        writeRecordHeader(id, values.length * 4, timestampUs);
        for (float v : values) writeLE(Float.floatToIntBits(v), 4);
    }

    public void appendDoubleArray(int id, double[] values, long timestampUs) throws IOException {
        writeRecordHeader(id, values.length * 8, timestampUs);
        for (double v : values) writeLE(Double.doubleToLongBits(v), 8);
    }

    /** Array length first, then each string as a 4-byte length and its UTF-8 bytes. */
    public void appendStringArray(int id, String[] values, long timestampUs) throws IOException {
        byte[][] encoded = new byte[values.length][];
        int size = 4;
        for (int i = 0; i < values.length; i++) {
            encoded[i] = utf8(values[i]);
            size += 4 + encoded[i].length;
        }
        writeRecordHeader(id, size, timestampUs);
        writeLE(values.length, 4);
        for (byte[] s : encoded) {
            writeLE(s.length, 4);
            out.write(s);
        }
    }

    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        if (closed) return;
        closed = true;
        out.close();
    }

    // ---- Encoding ---------------------------------------------------------------------------

    /**
     * The one-byte length bitfield, then entry id, payload size and timestamp, each in the fewest
     * bytes that hold it (spec § Records).
     */
    private void writeRecordHeader(int entryId, int payloadSize, long timestampUs) throws IOException {
        if (timestampUs < 0) throw new IllegalArgumentException("negative timestamp: " + timestampUs);
        int idLen = byteLength(entryId & 0xFFFFFFFFL, 4);
        int sizeLen = byteLength(payloadSize & 0xFFFFFFFFL, 4);
        int tsLen = byteLength(timestampUs, 8);
        int bitfield = (idLen - 1) | ((sizeLen - 1) << 2) | ((tsLen - 1) << 4);
        int n = 0;
        ensureScratch(1 + idLen + sizeLen + tsLen);
        scratch[n++] = (byte) bitfield;
        n = putLE(scratch, n, entryId, idLen);
        n = putLE(scratch, n, payloadSize, sizeLen);
        n = putLE(scratch, n, timestampUs, tsLen);
        out.write(scratch, 0, n);
    }

    /** Fewest bytes (at least 1, at most {@code max}) that represent an unsigned value. */
    static int byteLength(long unsignedValue, int max) {
        int len = 1;
        while (len < max && (unsignedValue >>> (8 * len)) != 0) len++;
        return len;
    }

    private void writeLE(long value, int bytes) throws IOException {
        ensureScratch(bytes);
        int n = putLE(scratch, 0, value, bytes);
        out.write(scratch, 0, n);
    }

    private static int putLE(byte[] dst, int at, long value, int bytes) {
        for (int i = 0; i < bytes; i++) dst[at + i] = (byte) (value >>> (8 * i));
        return at + bytes;
    }

    private void ensureScratch(int size) {
        if (scratch.length < size) scratch = new byte[Math.max(size, scratch.length * 2)];
    }

    private static byte[] utf8(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }
}
