package org.firstinspires.ftc.teamcode.vision;

import java.util.EnumMap;
import java.util.Map;

/**
 * The eight resting poses of the BIOBUZZ AprilTag stickers — four CELLs, two states
 * each — transcribed from FIRST's field CAD.
 *
 * <h2>Source</h2>
 *
 * {@code am-5850 BIOBUZZ 8-10-26.STEP}, exported from the public BIOBUZZ field
 * document in Onshape (STEP timestamp 2026-09-09). Parts used:
 * {@code am-5888-{red1,red2,blue1,blue2}: ... Goal April Tag} for the stickers and
 * the F608ZZ bearings in each {@code Goal Pivot Assembly} for the pivot axis. The
 * STEP units are metres; everything here is inches.
 *
 * <p>The CAD agrees with the Competition Manual everywhere the two overlap, which is
 * why it is trusted for the numbers the manual leaves out: pivot axis at 43.950 in
 * (manual 43.95), HIVE centres 25.50 in apart (manual 25.5), tilt exactly 30°, and
 * the raised opening's rim at 53.36 / 65.48 in (manual 53.5 / 65.6).
 *
 * <p>The sticker placement in the CAD is the reference-hole-aligned one §9.9
 * describes. Each HIVE Goal Bottom Skin carries two ½ in holes 14 in apart, and
 * their centres sit exactly on the sticker's notched long edge, 1.5 in in from each
 * end.
 *
 * <h2>Which poses are modelled and which are derived</h2>
 *
 * The CAD is one snapshot, so it holds each CELL in only one state: BLUE SCORING and
 * RED AUDIENCE raised, BLUE AUDIENCE and RED SCORING lowered. The other four poses
 * come from rotating the same rigid rocker 60° about its pivot bearing axis, from
 * one 30° stop to the other. {@code HiveCellPosesTest} checks that the rotated poses
 * match the independently modelled CELLs of the other HIVE (mirrored across the
 * field) to 0.001 in, so both halves of the table rest on the CAD.
 *
 * <h2>Frame</h2>
 *
 * A <b>HIVE frame</b>, not the FTC field frame, because the two can't be reconciled
 * from what's available here:
 * <ul>
 *   <li>origin at the centre of the field, on the top surface of the TILES;</li>
 *   <li>+X toward the BLUE HIVE (the red HIVE is at −X);</li>
 *   <li>+Y toward the two SCORING CELLs (the AUDIENCE CELLs are at −Y);</li>
 *   <li>+Z up. Right-handed.</li>
 * </ul>
 * The axes are named after things the CAD states outright — part names — rather than
 * after walls. ftc-docs defines FTC +X as away from the audience and +Y as from the
 * Red Wall toward Blue, with the Red Wall on the audience's right. Take "AUDIENCE"
 * CELLs to be on the audience side of the field and that is left-handed against this
 * CAD. So either that CELL name doesn't mean what it suggests or the red HIVE isn't
 * the one nearer the Red Wall. Settle it on a real field, then map to FTC axes in the
 * field-pose layer; nothing here needs to change.
 *
 * <h2>What is still not known — tag placement on the sticker</h2>
 *
 * The CAD models the sticker as a blank 17 × 5 × 0.01 in plate. The tag artwork is
 * not in it, and AndyMark publishes no placement drawing. Two things follow:
 * <ul>
 *   <li>Where the 3.25 in tag row sits across the 5 in strip:
 *       {@link #TAG_ROW_OFFSET_TOWARD_OPENING_IN} is NaN. Geometry bounds it to
 *       ±{@link #TAG_ROW_OFFSET_BOUND_IN}, the 0.25 in border on each side.</li>
 *   <li>Which end of the row carries the lowest tag id. {@link Pose#rowAxis()} is a
 *       geometric axis and makes no claim about id order.</li>
 * </ul>
 * Both can be measured in minutes with a ruler on a real sticker, or read off
 * <b>Vision: Raw Tag Dump</b>.
 *
 * <p>Laterally the row is taken to be centred on the sticker. Both the sticker and
 * the SDK's member offsets (±2.75, ±6.5 in) are symmetric about their centres, and
 * the sticker centre lands on the HIVE centreline to 0.01 in.
 */
public final class HiveCellPoses {

    private HiveCellPoses() {
        // Constants only.
    }

    /** Sticker length along the tag row, inches. CAD, and AndyMark's listing. */
    public static final double STICKER_LENGTH_IN = 17.0;

    /** Sticker width across the tag row, inches. CAD, and AndyMark's listing. */
    public static final double STICKER_WIDTH_IN = 5.0;

    /** Pivot axis in this frame: parallel to X, at Y = 0, Z = this. CAD bearing centre. */
    public static final double PIVOT_Z_IN = 43.950;

    /**
     * How far the tag row's centreline sits from the sticker's centreline, measured
     * in the sticker plane toward the CELL opening, inches. <b>Not established.</b>
     *
     * <p>The CAD has no artwork. It is bounded by {@link #TAG_ROW_OFFSET_BOUND_IN}.
     * For a candidate, not a value: if the SDK's cluster origin sits exactly in the
     * plane of the opening, which FIRST's "centre of the CELL opening" wording
     * suggests, the offset would be about +0.19 in. That is an inference about
     * FIRST's definition, so it stays out of the number.
     */
    public static final double TAG_ROW_OFFSET_TOWARD_OPENING_IN = Double.NaN;

    /** Half the width left over when a 3.25 in tag sits in a 5 in strip with 0.25 in borders. */
    public static final double TAG_ROW_OFFSET_BOUND_IN =
            (STICKER_WIDTH_IN - 2 * 0.25 - BiobuzzTags.TAG_SIZE_INCHES) / 2.0;

    private static final double C30 = 0.8660254; // cos 30°, as in the CAD rotation matrices
    private static final double S30 = 0.5;

    /** One resting pose of one CELL's AprilTag sticker, in the HIVE frame. */
    public static final class Pose {
        private final Vec3 stickerCentre;
        private final Vec3 outwardNormal;
        private final Vec3 openingFacing;

        Pose(Vec3 stickerCentre, Vec3 outwardNormal, Vec3 openingFacing) {
            this.stickerCentre = stickerCentre;
            this.outwardNormal = outwardNormal;
            this.openingFacing = openingFacing;
        }

        /** Centre of the 17 × 5 in sticker, on its printed face, inches. */
        public Vec3 stickerCentre() { return stickerCentre; }

        /** Unit normal of the printed face, pointing out of the CELL toward a viewer. */
        public Vec3 outwardNormal() { return outwardNormal; }

        /**
         * Unit vector the CELL opening faces: perpendicular to the opening, pointing
         * out of the CELL. It lies in the sticker plane, across the tag row.
         */
        public Vec3 openingFacing() { return openingFacing; }

        /**
         * Unit vector along the tag row, completing the right-handed triple
         * ({@code rowAxis}, {@code openingFacing}, {@code outwardNormal}). Geometric
         * only: which end carries the lowest tag id is not established.
         */
        public Vec3 rowAxis() {
            Vec3 a = openingFacing, b = outwardNormal;
            return new Vec3(a.y() * b.z() - a.z() * b.y(),
                    a.z() * b.x() - a.x() * b.z(),
                    a.x() * b.y() - a.y() * b.x());
        }

        /**
         * Centre of the tag row, inches. NaN in every component until
         * {@link #TAG_ROW_OFFSET_TOWARD_OPENING_IN} is measured.
         */
        public Vec3 tagRowCentre() {
            return stickerCentre.plus(openingFacing.times(TAG_ROW_OFFSET_TOWARD_OPENING_IN));
        }
    }

    private static final Map<HiveCell, Pose> UP = new EnumMap<>(HiveCell.class);
    private static final Map<HiveCell, Pose> DOWN = new EnumMap<>(HiveCell.class);

    static {
        // Raised: sticker 12.885 in out from the pivot and 5.720 in above it.
        // Opening faces away from the pivot, tilted 30° upward.
        UP.put(HiveCell.RED_AUDIENCE, new Pose(          // modelled
                new Vec3(-12.742, -12.885, 49.670),
                new Vec3(0, -S30, -C30), new Vec3(0, -C30, S30)));
        UP.put(HiveCell.BLUE_SCORING, new Pose(          // modelled
                new Vec3(12.758, 12.885, 49.670),
                new Vec3(0, S30, -C30), new Vec3(0, C30, S30)));
        UP.put(HiveCell.RED_SCORING, new Pose(           // rotated 60° from DOWN
                new Vec3(-12.742, 12.885, 49.670),
                new Vec3(0, S30, -C30), new Vec3(0, C30, S30)));
        UP.put(HiveCell.BLUE_AUDIENCE, new Pose(         // rotated 60° from DOWN
                new Vec3(12.758, -12.885, 49.670),
                new Vec3(0, -S30, -C30), new Vec3(0, -C30, S30)));

        // Lowered: sticker 11.397 in out from the pivot and 8.299 in below it.
        // Opening faces away from the pivot, tilted 30° downward.
        DOWN.put(HiveCell.RED_SCORING, new Pose(         // modelled
                new Vec3(-12.742, 11.397, 35.651),
                new Vec3(0, -S30, -C30), new Vec3(0, C30, -S30)));
        DOWN.put(HiveCell.BLUE_AUDIENCE, new Pose(       // modelled
                new Vec3(12.758, -11.397, 35.651),
                new Vec3(0, S30, -C30), new Vec3(0, -C30, -S30)));
        DOWN.put(HiveCell.RED_AUDIENCE, new Pose(        // rotated 60° from UP
                new Vec3(-12.742, -11.397, 35.651),
                new Vec3(0, S30, -C30), new Vec3(0, -C30, -S30)));
        DOWN.put(HiveCell.BLUE_SCORING, new Pose(        // rotated 60° from UP
                new Vec3(12.758, 11.397, 35.651),
                new Vec3(0, -S30, -C30), new Vec3(0, C30, -S30)));
    }

    /**
     * Rise of a sticker's centre from its lowered to its raised pose, inches.
     * <b>Derived</b> from the table above: about 14.02 in.
     *
     * <p>This, not {@link HiveGeometry#STATE_HEIGHT_DELTA_IN}, is the separation a
     * tag-row height classifier sees. The {@code 2 · dx · sin(tilt)} argument holds,
     * but {@code dx} is the sticker's own distance from the pivot with the rocker
     * level, 14.02 in. That is well outboard of half the CELL spacing. The unknown
     * row offset moves it by at most ±{@link #TAG_ROW_OFFSET_BOUND_IN}.
     */
    public static final double STICKER_STATE_RISE_IN =
            UP.get(HiveCell.BLUE_SCORING).stickerCentre().z()
                    - DOWN.get(HiveCell.BLUE_SCORING).stickerCentre().z();

    /**
     * The pose of {@code cell}'s sticker when settled in {@code state}.
     *
     * @throws IllegalArgumentException for {@link HiveCellState#UNKNOWN}, which has no
     *     pose — that is the point of it
     */
    public static Pose of(HiveCell cell, HiveCellState state) {
        switch (state) {
            case UP: return UP.get(cell);
            case DOWN: return DOWN.get(cell);
            default:
                throw new IllegalArgumentException("No pose for a cell in state " + state);
        }
    }
}
