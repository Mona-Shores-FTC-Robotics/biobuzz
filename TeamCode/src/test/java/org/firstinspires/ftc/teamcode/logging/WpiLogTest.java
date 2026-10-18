package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The keyed layer: entries declared on first use, types held fixed, and pose structs written the
 * way AdvantageScope needs them to draw on the field.
 */
public class WpiLogTest {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    private WpiLog newLog() throws IOException {
        return new WpiLog(new WpiLogWriter(out, ""));
    }

    @Test
    public void aKeyIsDeclaredOnceAndAppendedAfter() throws IOException {
        WpiLog log = newLog();
        log.put("/Shooter/RPM", 1000.0, 10);
        log.put("/Shooter/RPM", 2000.0, 20);

        WpiLogReader r = new WpiLogReader(out.toByteArray());
        WpiLogReader.Entry e = r.entry("/Shooter/RPM");
        assertEquals("double", e.type);
        assertEquals(2, e.records.size());
        assertEquals(2000.0, e.records.get(1).asDouble(), 0.0);
    }

    @Test
    public void aKeyCannotChangeType() throws IOException {
        WpiLog log = newLog();
        log.put("/Mode", "auto", 0);
        try {
            log.put("/Mode", 1.0, 1);
            fail("a key logged as string then double must throw");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("/Mode"));
        }
    }

    /**
     * AdvantageScope needs each struct's layout, and the layouts it refers to, as
     * {@code /.schema/struct:<Type>} entries before it can decode a pose. Written once each.
     */
    @Test
    public void poseWritesItsSchemasOnceAndFirst() throws IOException {
        WpiLog log = newLog();
        log.putPose2d("/Odometry/Robot", 1.0, 2.0, 0.5, 100);
        log.putPose2d("/Odometry/Robot", 1.1, 2.1, 0.6, 200);
        log.putPose2d("/Odometry/Ghost", 0.0, 0.0, 0.0, 300);

        WpiLogReader r = new WpiLogReader(out.toByteArray());
        List<String> names = new ArrayList<>(r.entries.keySet());
        assertEquals("/.schema/struct:Translation2d", names.get(0));
        assertEquals("/.schema/struct:Rotation2d", names.get(1));
        assertEquals("/.schema/struct:Pose2d", names.get(2));
        assertEquals("/Odometry/Robot", names.get(3));
        assertEquals(5, names.size());

        WpiLogReader.Entry schema = r.entry("/.schema/struct:Pose2d");
        assertEquals("structschema", schema.type);
        assertEquals("Translation2d translation;Rotation2d rotation", schema.records.get(0).asString());
        assertEquals("double x;double y", r.entry("/.schema/struct:Translation2d").records.get(0).asString());
        assertEquals("double value", r.entry("/.schema/struct:Rotation2d").records.get(0).asString());

        WpiLogReader.Entry pose = r.entry("/Odometry/Robot");
        assertEquals("struct:Pose2d", pose.type);
        assertArrayEquals(new double[] {1.1, 2.1, 0.6}, pose.records.get(1).asDoubles(), 0.0);
    }

    @Test
    public void poseArrayIsPackedBackToBack() throws IOException {
        WpiLog log = newLog();
        log.putPose2dArray("/Path", new double[] {1, 2, 3, 4, 5, 6}, 0);

        WpiLogReader r = new WpiLogReader(out.toByteArray());
        WpiLogReader.Entry e = r.entry("/Path");
        assertEquals("struct:Pose2d[]", e.type);
        assertArrayEquals(new double[] {1, 2, 3, 4, 5, 6}, e.records.get(0).asDoubles(), 0.0);
    }

    /** A flat Pose3d is translation then the yaw quaternion (w, x, y, z). */
    @Test
    public void flatPose3dCarriesYawAsAQuaternion() throws IOException {
        WpiLog log = newLog();
        log.putPose3dFlat("/Odometry/Robot3d", 1.0, 2.0, 0.0, Math.PI / 2, 0);

        WpiLogReader r = new WpiLogReader(out.toByteArray());
        assertEquals("Translation3d translation;Rotation3d rotation",
                r.entry("/.schema/struct:Pose3d").records.get(0).asString());
        assertEquals("Quaternion q", r.entry("/.schema/struct:Rotation3d").records.get(0).asString());
        assertEquals("double w;double x;double y;double z",
                r.entry("/.schema/struct:Quaternion").records.get(0).asString());
        double h = Math.sqrt(0.5);
        assertArrayEquals(new double[] {1.0, 2.0, 0.0, h, 0.0, 0.0, h},
                r.entry("/Odometry/Robot3d").records.get(0).asDoubles(), 1e-12);
    }

    /** Game pieces and HIVE components: whole Pose3d values, any rotation, back to back. */
    @Test
    public void pose3dArrayCarriesFullRotations() throws IOException {
        WpiLog log = newLog();
        double h = Math.sqrt(0.5);
        double[] poses = {1, 2, 3, h, 0, h, 0, -1, -2, 0.5, 1, 0, 0, 0};
        log.putPose3dArray("/Sim/GamePieces/Pollen", poses, 0);
        log.putPose3dArray("/Sim/GamePieces/Pollen", new double[0], 1);

        WpiLogReader r = new WpiLogReader(out.toByteArray());
        WpiLogReader.Entry e = r.entry("/Sim/GamePieces/Pollen");
        assertEquals("struct:Pose3d[]", e.type);
        assertArrayEquals(poses, e.records.get(0).asDoubles(), 0.0);
        assertEquals(0, e.records.get(1).asDoubles().length);
        try {
            log.putPose3dArray("/Sim/Bad", new double[] {1, 2, 3}, 2);
            fail("a pose array must be whole poses");
        } catch (IllegalArgumentException expected) {
            // good
        }
    }

    /**
     * AdvantageScope keeps one value per timestamp, so two events in one loop must not share a
     * timestamp or the first one disappears. Found by loading the simulated match with
     * AdvantageScope's own decoder: "driver RB: Launch all" was hidden by the launcher state change
     * it caused.
     */
    @Test
    public void eventsInOneLoopKeepDistinctIncreasingTimestamps() throws IOException {
        WpiLog log = newLog();
        log.putEvent("driver RB: Launch all", 5_000_000);
        log.putEvent("launcher: READY -> FIRING", 5_000_000);
        log.putEvent("later", 5_020_000);

        WpiLogReader r = new WpiLogReader(out.toByteArray());
        List<WpiLogReader.Record> events = r.entry(AdvantageScopeKeys.EVENTS).records;
        assertEquals(5_000_000, events.get(0).timestampUs);
        assertEquals(5_000_001, events.get(1).timestampUs);
        assertEquals(5_020_000, events.get(2).timestampUs);
        assertEquals("driver RB: Launch all", events.get(0).asString());
    }

    @Test
    public void metadataGoesWhereTheMetadataTabLooks() throws IOException {
        WpiLog log = newLog();
        log.putMetadata("GitSHA", "abc123");

        WpiLogReader r = new WpiLogReader(out.toByteArray());
        assertEquals("abc123", r.entry("/RealMetadata/GitSHA").records.get(0).asString());
    }

    @Test
    public void gamepadUsesSdlButtonIndices() throws IOException {
        WpiLog log = newLog();
        GamepadLog.State g = new GamepadLog.State();
        g.a = true;
        g.rightBumper = true;
        g.dpadLeft = true;
        g.leftTrigger = 0.75;
        new GamepadLog(0).write(log, g, 0);

        WpiLogReader r = new WpiLogReader(out.toByteArray());
        assertEquals((1L << 0) | (1L << 10) | (1L << 13),
                r.entry("/DriverStation/Joystick0/ButtonValues").records.get(0).asInt64());
        assertEquals(0.75, r.entry("/DriverStation/Joystick0/AxisValues").records.get(0).asDoubles()[4], 0.0);
        assertEquals(0b111111L, r.entry("/DriverStation/Joystick0/AxesAvailable").records.get(0).asInt64());
    }
}
