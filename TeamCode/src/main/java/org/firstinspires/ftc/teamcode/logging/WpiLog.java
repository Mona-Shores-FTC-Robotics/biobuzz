package org.firstinspires.ftc.teamcode.logging;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A {@code .wpilog} addressed by key: {@code put("/Shooter/Left/RPM", 3120.0, t)}. The first
 * {@code put} to a key declares its entry with that value's type; later ones only append.
 *
 * <p>Keys are slash paths, the way AdvantageKit writes them, so AdvantageScope shows a tree. The
 * keys AdvantageScope gives special meaning to (match state, joysticks, metadata) are in
 * {@link AdvantageScopeKeys}; use those constants rather than retyping the strings.
 *
 * <p>A key keeps the type it was first written with. Writing a different type to it is a bug in
 * the caller and throws, because AdvantageScope would otherwise show one of the two silently.
 *
 * <p>Poses go in through {@link #putPose2d} and friends, which also write the struct layouts
 * AdvantageScope needs the first time each struct type is used.
 *
 * <p>Single-threaded, like {@link WpiLogWriter} underneath it.
 */
public final class WpiLog implements Closeable {

    private static final class Entry {
        final int id;
        final String type;

        Entry(int id, String type) {
            this.id = id;
            this.type = type;
        }
    }

    private final WpiLogWriter writer;
    private final Map<String, Entry> entries = new HashMap<>();
    private final Set<String> writtenSchemas = new HashSet<>();
    private byte[] structBuffer = new byte[WpiStructs.POSE3D.size];
    private long lastEventUs = -1;

    public WpiLog(WpiLogWriter writer) {
        this.writer = writer;
    }

    public void put(String key, double value, long timestampUs) throws IOException {
        writer.appendDouble(entry(key, "double", timestampUs), value, timestampUs);
    }

    public void put(String key, long value, long timestampUs) throws IOException {
        writer.appendInt64(entry(key, "int64", timestampUs), value, timestampUs);
    }

    public void put(String key, boolean value, long timestampUs) throws IOException {
        writer.appendBoolean(entry(key, "boolean", timestampUs), value, timestampUs);
    }

    public void put(String key, String value, long timestampUs) throws IOException {
        writer.appendString(entry(key, "string", timestampUs), value, timestampUs);
    }

    public void put(String key, double[] values, long timestampUs) throws IOException {
        writer.appendDoubleArray(entry(key, "double[]", timestampUs), values, timestampUs);
    }

    public void put(String key, float[] values, long timestampUs) throws IOException {
        writer.appendFloatArray(entry(key, "float[]", timestampUs), values, timestampUs);
    }

    public void put(String key, String[] values, long timestampUs) throws IOException {
        writer.appendStringArray(entry(key, "string[]", timestampUs), values, timestampUs);
    }

    /**
     * One line on the events stream ({@link AdvantageScopeKeys#EVENTS}, shown on the Console tab).
     *
     * <p>AdvantageScope keeps one value per timestamp per key: a second value at the same
     * microsecond replaces the first ({@code LogField.putData}). Several events in one loop are
     * normal (a button press and the state change it causes), so each event after the first in a
     * loop is moved 1 µs later. That keeps every event, in order, without visibly moving it.
     */
    public void putEvent(String text, long timestampUs) throws IOException {
        long t = Math.max(timestampUs, lastEventUs + 1);
        put(AdvantageScopeKeys.EVENTS, text, t);
        lastEventUs = t;
    }

    /** A metadata value, shown on AdvantageScope's Metadata tab. Written once, at time 0. */
    public void putMetadata(String name, String value) throws IOException {
        put(AdvantageScopeKeys.METADATA_PREFIX + name, value, 0L);
    }

    /** A pose in AdvantageScope's frame: meters and radians. */
    public void putPose2d(String key, double xMeters, double yMeters, double headingRad, long timestampUs)
            throws IOException {
        int id = structEntry(key, WpiStructs.POSE2D, false, timestampUs);
        ensureStructBuffer(WpiStructs.POSE2D.size);
        WpiStructs.packPose2d(structBuffer, 0, xMeters, yMeters, headingRad);
        writer.appendRaw(id, structBuffer, 0, WpiStructs.POSE2D.size, timestampUs);
    }

    /**
     * Several poses as one value, e.g. a path or every vision fix this loop.
     *
     * @param xyHeading packed {@code x0, y0, θ0, x1, y1, θ1, …} in meters and radians
     */
    public void putPose2dArray(String key, double[] xyHeading, long timestampUs) throws IOException {
        if (xyHeading.length % 3 != 0) {
            throw new IllegalArgumentException("pose array length must be a multiple of 3");
        }
        int id = structEntry(key, WpiStructs.POSE2D, true, timestampUs);
        int count = xyHeading.length / 3;
        ensureStructBuffer(count * WpiStructs.POSE2D.size);
        int at = 0;
        for (int i = 0; i < count; i++) {
            at = WpiStructs.packPose2d(structBuffer, at, xyHeading[3 * i], xyHeading[3 * i + 1], xyHeading[3 * i + 2]);
        }
        writer.appendRaw(id, structBuffer, 0, at, timestampUs);
    }

    /** A flat pose for the 3D view: on the field at {@code zMeters}, turned {@code yawRad}. */
    public void putPose3dFlat(String key, double xMeters, double yMeters, double zMeters, double yawRad,
                              long timestampUs) throws IOException {
        int id = structEntry(key, WpiStructs.POSE3D, false, timestampUs);
        ensureStructBuffer(WpiStructs.POSE3D.size);
        WpiStructs.packPose3dFlat(structBuffer, 0, xMeters, yMeters, zMeters, yawRad);
        writer.appendRaw(id, structBuffer, 0, WpiStructs.POSE3D.size, timestampUs);
    }

    /**
     * Several 3D poses as one value: game pieces, or a mechanism's components.
     *
     * @param xyzWxyz packed {@code x, y, z, qw, qx, qy, qz} per pose, meters and a unit quaternion
     */
    public void putPose3dArray(String key, double[] xyzWxyz, long timestampUs) throws IOException {
        if (xyzWxyz.length % 7 != 0) {
            throw new IllegalArgumentException("pose array length must be a multiple of 7");
        }
        int id = structEntry(key, WpiStructs.POSE3D, true, timestampUs);
        int count = xyzWxyz.length / 7;
        ensureStructBuffer(count * WpiStructs.POSE3D.size);
        int at = 0;
        for (int i = 0; i < count; i++) {
            int k = 7 * i;
            at = WpiStructs.packPose3d(structBuffer, at, xyzWxyz[k], xyzWxyz[k + 1], xyzWxyz[k + 2],
                    xyzWxyz[k + 3], xyzWxyz[k + 4], xyzWxyz[k + 5], xyzWxyz[k + 6]);
        }
        writer.appendRaw(id, structBuffer, 0, at, timestampUs);
    }

    public void flush() throws IOException {
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

    private int entry(String key, String type, long timestampUs) throws IOException {
        Entry e = entries.get(key);
        if (e == null) {
            e = new Entry(writer.start(key, type, "", timestampUs), type);
            entries.put(key, e);
        } else if (!e.type.equals(type)) {
            throw new IllegalStateException(
                    "key " + key + " was first logged as " + e.type + ", now as " + type);
        }
        return e.id;
    }

    private int structEntry(String key, WpiStructs.Type type, boolean array, long timestampUs)
            throws IOException {
        writeSchema(type, timestampUs);
        return entry(key, array ? type.arrayEntryType() : type.entryType(), timestampUs);
    }

    /** Writes a struct's layout, and the layouts it refers to, the first time it is used. */
    private void writeSchema(WpiStructs.Type type, long timestampUs) throws IOException {
        if (writtenSchemas.contains(type.name)) return;
        for (WpiStructs.Type dependency : type.dependencies) writeSchema(dependency, timestampUs);
        int id = writer.start(type.schemaEntryName(), "structschema", "", timestampUs);
        writer.appendString(id, type.schema, timestampUs);
        writtenSchemas.add(type.name);
    }

    private void ensureStructBuffer(int size) {
        if (structBuffer.length < size) structBuffer = new byte[Math.max(size, structBuffer.length * 2)];
    }
}
