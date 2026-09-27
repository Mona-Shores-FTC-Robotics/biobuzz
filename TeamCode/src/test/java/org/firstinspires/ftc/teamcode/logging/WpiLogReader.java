package org.firstinspires.ftc.teamcode.logging;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@code .wpilog} reader for tests, written from the spec rather than from {@link WpiLogWriter},
 * so a misreading of the spec in the writer shows up as a disagreement here instead of being
 * repeated.
 */
final class WpiLogReader {

    static final class Record {
        final long timestampUs;
        final byte[] payload;

        Record(long timestampUs, byte[] payload) {
            this.timestampUs = timestampUs;
            this.payload = payload;
        }

        double asDouble() {
            return le().getDouble(0);
        }

        long asInt64() {
            return le().getLong(0);
        }

        boolean asBoolean() {
            return payload[0] != 0;
        }

        String asString() {
            return new String(payload, StandardCharsets.UTF_8);
        }

        double[] asDoubles() {
            double[] d = new double[payload.length / 8];
            ByteBuffer b = le();
            for (int i = 0; i < d.length; i++) d[i] = b.getDouble(8 * i);
            return d;
        }

        private ByteBuffer le() {
            return ByteBuffer.wrap(payload).order(ByteOrder.LITTLE_ENDIAN);
        }
    }

    static final class Entry {
        final String name;
        final String type;
        final String metadata;
        final long startTimestampUs;
        final List<Record> records = new ArrayList<>();

        Entry(String name, String type, String metadata, long startTimestampUs) {
            this.name = name;
            this.type = type;
            this.metadata = metadata;
            this.startTimestampUs = startTimestampUs;
        }
    }

    final int version;
    final String extraHeader;
    /** By name, in the order entries were started. */
    final Map<String, Entry> entries = new LinkedHashMap<>();

    WpiLogReader(byte[] file) {
        ByteBuffer b = ByteBuffer.wrap(file).order(ByteOrder.LITTLE_ENDIAN);
        byte[] magic = new byte[6];
        b.get(magic);
        if (!"WPILOG".equals(new String(magic, StandardCharsets.US_ASCII))) {
            throw new IllegalArgumentException("not a WPILOG file");
        }
        version = b.getShort() & 0xFFFF;
        byte[] extra = new byte[b.getInt()];
        b.get(extra);
        extraHeader = new String(extra, StandardCharsets.UTF_8);

        Map<Integer, Entry> byId = new HashMap<>();
        while (b.hasRemaining()) {
            int bitfield = b.get() & 0xFF;
            if ((bitfield & 0x80) != 0) throw new IllegalStateException("spare bit set");
            int id = (int) readUnsigned(b, (bitfield & 0x3) + 1);
            int size = (int) readUnsigned(b, ((bitfield >> 2) & 0x3) + 1);
            long ts = readUnsigned(b, ((bitfield >> 4) & 0x7) + 1);
            byte[] payload = new byte[size];
            b.get(payload);
            if (id == 0) {
                ByteBuffer p = ByteBuffer.wrap(payload).order(ByteOrder.LITTLE_ENDIAN);
                int kind = p.get() & 0xFF;
                int target = p.getInt();
                if (kind == 0) {
                    Entry e = new Entry(readString(p), readString(p), readString(p), ts);
                    if (byId.containsKey(target)) throw new IllegalStateException("id reused: " + target);
                    byId.put(target, e);
                    entries.put(e.name, e);
                } else if (kind == 1) {
                    byId.remove(target);
                } else if (kind != 2) {
                    throw new IllegalStateException("unknown control record " + kind);
                }
            } else {
                Entry e = byId.get(id);
                if (e == null) throw new IllegalStateException("record for unstarted id " + id);
                e.records.add(new Record(ts, payload));
            }
        }
    }

    Entry entry(String name) {
        Entry e = entries.get(name);
        if (e == null) throw new AssertionError("no entry named " + name);
        return e;
    }

    private static long readUnsigned(ByteBuffer b, int bytes) {
        long v = 0;
        for (int i = 0; i < bytes; i++) v |= (b.get() & 0xFFL) << (8 * i);
        return v;
    }

    private static String readString(ByteBuffer p) {
        byte[] s = new byte[p.getInt()];
        p.get(s);
        return new String(s, StandardCharsets.UTF_8);
    }
}
