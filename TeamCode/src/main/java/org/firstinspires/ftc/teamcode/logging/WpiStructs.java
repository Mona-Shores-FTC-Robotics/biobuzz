package org.firstinspires.ftc.teamcode.logging;

/**
 * The WPILib geometry structs AdvantageScope draws on its 2D and 3D field views, as layouts we
 * write by hand.
 *
 * <p>AdvantageScope reads a pose as a <i>struct</i>: an entry typed {@code struct:Pose2d} whose
 * records are the packed bytes, plus one {@code structschema} entry per struct type, named
 * {@code /.schema/struct:<Type>}, whose single record is the layout text. The older "number
 * array" form ({@code double[]{x, y, θ}}) is deprecated in AdvantageScope v26 and removed in v27,
 * so it is not offered here.
 *
 * <p>Layouts are copied from WPILib's own {@code *Struct.getSchema()} (wpimath, main branch), and
 * packing follows <i>WPILib Packed Struct Serialization Specification 1.0</i>: members in
 * declaration order, little-endian, no padding. Every member here is a {@code double}, so a
 * Pose2d is 24 bytes and a Pose3d is 56.
 *
 * <p>Units are WPILib's: meters and radians. Converting from Pedro's inches, and from Pedro's
 * frame to AdvantageScope's, is {@link AdvantageScopeFrame}'s job, not this class's.
 */
public final class WpiStructs {

    private WpiStructs() {
    }

    /** A struct type: its name, its schema text, and the struct types that text refers to. */
    public static final class Type {
        public final String name;
        public final String schema;
        public final int size;
        final Type[] dependencies;

        Type(String name, String schema, int size, Type... dependencies) {
            this.name = name;
            this.schema = schema;
            this.size = size;
            this.dependencies = dependencies;
        }

        /** The entry type string for one value, e.g. {@code struct:Pose2d}. */
        public String entryType() {
            return "struct:" + name;
        }

        /** The entry type string for an array, e.g. {@code struct:Pose2d[]}. */
        public String arrayEntryType() {
            return "struct:" + name + "[]";
        }

        /** Where AdvantageScope looks for this type's layout. */
        public String schemaEntryName() {
            return "/.schema/struct:" + name;
        }
    }

    public static final Type ROTATION2D = new Type("Rotation2d", "double value", 8);
    public static final Type TRANSLATION2D = new Type("Translation2d", "double x;double y", 16);
    public static final Type POSE2D = new Type(
            "Pose2d", "Translation2d translation;Rotation2d rotation", 24, TRANSLATION2D, ROTATION2D);

    public static final Type QUATERNION = new Type("Quaternion", "double w;double x;double y;double z", 32);
    public static final Type ROTATION3D = new Type("Rotation3d", "Quaternion q", 32, QUATERNION);
    public static final Type TRANSLATION3D = new Type("Translation3d", "double x;double y;double z", 24);
    public static final Type POSE3D = new Type(
            "Pose3d", "Translation3d translation;Rotation3d rotation", 56, TRANSLATION3D, ROTATION3D);

    /** Packs a Pose2d at {@code at}; returns the index just past it. */
    public static int packPose2d(byte[] dst, int at, double xMeters, double yMeters, double headingRad) {
        at = putDouble(dst, at, xMeters);
        at = putDouble(dst, at, yMeters);
        return putDouble(dst, at, headingRad);
    }

    /**
     * Packs a Pose3d lying flat on the field at height {@code zMeters}, turned {@code yawRad} about
     * the vertical axis. That is all a drivetrain pose needs; the quaternion for a pure yaw is
     * {@code (cos(yaw/2), 0, 0, sin(yaw/2))}.
     */
    public static int packPose3dFlat(byte[] dst, int at, double xMeters, double yMeters, double zMeters,
                                     double yawRad) {
        at = putDouble(dst, at, xMeters);
        at = putDouble(dst, at, yMeters);
        at = putDouble(dst, at, zMeters);
        at = putDouble(dst, at, Math.cos(yawRad / 2.0));
        at = putDouble(dst, at, 0.0);
        at = putDouble(dst, at, 0.0);
        return putDouble(dst, at, Math.sin(yawRad / 2.0));
    }

    static int putDouble(byte[] dst, int at, double value) {
        long bits = Double.doubleToLongBits(value);
        for (int i = 0; i < 8; i++) dst[at + i] = (byte) (bits >>> (8 * i));
        return at + 8;
    }
}
