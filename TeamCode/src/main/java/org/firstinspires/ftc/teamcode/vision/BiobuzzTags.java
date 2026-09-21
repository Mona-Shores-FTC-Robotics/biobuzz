package org.firstinspires.ftc.teamcode.vision;

import org.firstinspires.ftc.vision.apriltag.AprilTagClusterMetadata;
import org.firstinspires.ftc.vision.apriltag.AprilTagGameDatabase;
import org.firstinspires.ftc.vision.apriltag.AprilTagLibrary;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * BIOBUZZ AprilTag geometry, sourced from the FTC SDK's own tag library.
 *
 * <h2>Why there is no field position here</h2>
 *
 * There isn't one to have. The SDK's BIOBUZZ library publishes <b>no tag field
 * positions at all</b> — {@code getAllTags()} is empty, {@code lookupTag(id)}
 * returns null for every id 30–45, and all four clusters carry
 * {@code fieldPosition = (0, 0, 0)}. That is deliberate, and matches how FIRST
 * has flagged movable tags before: in DECODE the fixed goal tags 20/24 carried
 * real coordinates while the Obelisk tags 21–23, which are repositioned between
 * matches, were published as zeros.
 *
 * <p>The SDK 12.0 release notes say why, in bold:
 * <i>"Unfortunately, since BIOBUZZ AprilTags move, they are not suitable for
 * absolute Field Localization."</i>
 *
 * <p>So nothing in this package converts a tag sighting into a field pose, and
 * nothing should be added that does. What a sighting gives you is the cell's
 * position <em>relative to the robot</em> — which is what a turret needs anyway.
 *
 * <h2>What the SDK does publish, and how we use it</h2>
 *
 * The cluster-internal geometry is real and useful: each member tag's position
 * relative to the cluster origin, where the origin sits at the centre of the
 * CELL opening rather than on the tags themselves. Seeing any one member locates
 * the opening.
 *
 * <p>Cell identity is read from the SDK at class load via the public
 * {@link AprilTagLibrary#lookupCluster(int)}, so a future SDK correction is
 * picked up for free. The member offsets are <em>not</em> reachable through
 * public API ({@code AprilTagClusterMetadata.clusterMembers} is package-private),
 * so they are transcribed below — and {@code BiobuzzTagsTest} reads the SDK's
 * real values reflectively and fails if these ever drift from them. Transcribed
 * constants that nothing checks are how a field-geometry bug survives a season.
 */
public final class BiobuzzTags {

    private BiobuzzTags() {
        // Constants only.
    }

    /** Lowest BIOBUZZ cluster member tag id. */
    public static final int FIRST_TAG_ID = 30;

    /** Highest BIOBUZZ cluster member tag id. */
    public static final int LAST_TAG_ID = 45;

    /** Member tags per cluster. */
    public static final int MEMBERS_PER_CLUSTER = 4;

    /**
     * Edge length of a BIOBUZZ member tag, inches.
     *
     * <p>This has to match the tag size configured in the Limelight's pipeline.
     * If it doesn't, every range the camera reports is wrong by a constant
     * factor — and nothing about the output will look obviously broken.
     */
    public static final double TAG_SIZE_INCHES = 3.25;

    /**
     * Lateral offsets of the four member tags from the cluster origin, inches,
     * ordered by ascending tag id within a cluster.
     */
    private static final double[] MEMBER_X_INCHES = { -6.5, -2.75, 2.75, 6.5 };

    /** All members share this offset; see {@link #memberOffset(int)} for the frame. */
    private static final double MEMBER_Y_INCHES = 7.187399864196777;

    /** All members share this offset; see {@link #memberOffset(int)} for the frame. */
    private static final double MEMBER_Z_INCHES = -5.622000217437744;

    /** Tag id to the cell that carries it, built from the SDK library at class load. */
    private static final Map<Integer, HiveCell> CELL_BY_TAG_ID = buildCellByTagId();

    private static Map<Integer, HiveCell> buildCellByTagId() {
        Map<Integer, HiveCell> map = new HashMap<>();
        AprilTagLibrary library = AprilTagGameDatabase.getBioBuzzTagLibrary();
        for (int tagId = FIRST_TAG_ID; tagId <= LAST_TAG_ID; tagId++) {
            AprilTagClusterMetadata cluster = library.lookupCluster(tagId);
            if (cluster == null) continue;
            HiveCell cell = HiveCell.forClusterName(cluster.name);
            if (cell != null) map.put(tagId, cell);
        }
        return Collections.unmodifiableMap(map);
    }

    /** True if {@code tagId} is a BIOBUZZ cluster member we know about. */
    public static boolean isHiveTag(int tagId) {
        return CELL_BY_TAG_ID.containsKey(tagId);
    }

    /** The cell carrying {@code tagId}, or null if it isn't a BIOBUZZ cluster member. */
    public static HiveCell cellForTag(int tagId) {
        return CELL_BY_TAG_ID.get(tagId);
    }

    /**
     * Where {@code tagId} sits relative to its cluster origin — that is, relative
     * to the centre of the CELL opening — in the cluster's own plane frame, inches.
     *
     * <p>Axes are the SDK's: {@code x} runs along the row of tags, {@code y} and
     * {@code z} are the fixed offset from the tag plane out to the opening centre.
     * All four members share the same y and z; only x differs.
     *
     * <p>To go from a <em>tag</em> position to the <em>opening</em> position you
     * subtract this vector, rotated into whatever frame the tag position is in.
     *
     * @throws IllegalArgumentException if {@code tagId} is not a BIOBUZZ member tag
     */
    public static Vec3 memberOffset(int tagId) {
        if (!isHiveTag(tagId)) {
            throw new IllegalArgumentException("Not a BIOBUZZ cluster member tag: " + tagId);
        }
        int indexInCluster = (tagId - FIRST_TAG_ID) % MEMBERS_PER_CLUSTER;
        return new Vec3(MEMBER_X_INCHES[indexInCluster], MEMBER_Y_INCHES, MEMBER_Z_INCHES);
    }

    /** Every tag id belonging to {@code cell}, ascending. */
    public static int[] tagIdsFor(HiveCell cell) {
        int[] ids = new int[MEMBERS_PER_CLUSTER];
        int next = 0;
        for (int tagId = FIRST_TAG_ID; tagId <= LAST_TAG_ID && next < ids.length; tagId++) {
            if (CELL_BY_TAG_ID.get(tagId) == cell) ids[next++] = tagId;
        }
        if (next != ids.length) {
            throw new IllegalStateException("Expected " + MEMBERS_PER_CLUSTER
                    + " tags for " + cell + ", found " + next);
        }
        return ids;
    }
}
