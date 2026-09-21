package org.firstinspires.ftc.teamcode.vision;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One frame's worth of measurement of a single {@link HiveCell}, in the robot frame.
 *
 * <p>Immutable. Everything here is <em>relative to the robot</em> — there is no
 * field pose, because BIOBUZZ publishes no tag field positions. See
 * {@link BiobuzzTags} for why.
 *
 * <h2>What point is being measured</h2>
 *
 * The SDK puts each cluster's origin at the centre of the CELL opening, offset
 * out of the plane the four tags lie in. Recovering that exact point needs the
 * cluster's full 3D orientation, which in turn needs the Euler convention the
 * Limelight reports yaw/pitch/roll in — and that isn't pinned down by anything in
 * the SDK. So this class measures a point it can derive from tag positions alone:
 *
 * <ul>
 *   <li>{@link #rowCentreRobot()} — the centre of the row of four member tags.
 *       With two or more tags visible this is exact, no orientation needed.</li>
 *   <li>The opening centre is a <b>fixed</b> further offset from there:
 *       {@code (0, +7.187, -5.622)} inches in the cluster's own plane frame,
 *       as published by the SDK. Apply it once the orientation convention has
 *       been confirmed on the robot.</li>
 * </ul>
 *
 * <p>That residual offset is mostly vertical and depthward, so it affects
 * {@link #elevationRad()} and slightly {@link #groundRangeIn()}, but very little
 * of {@link #bearingRad()}. A turret aiming on bearing can use this as-is; a
 * shooter solving for elevation should not, until the offset is applied.
 *
 * <h2>Single-tag sightings are less precise</h2>
 *
 * The four members sit at x = -6.5, -2.75, +2.75, +6.5 inches within the cluster.
 * With two or more visible, the direction of the cluster's lateral axis can be
 * derived from the tag positions and the row centre recovered exactly. With only
 * one visible that direction is unknowable and no lateral correction is applied,
 * so the measured point can sit up to 6.5 inches off along the row. Check
 * {@link #lateralCorrectionApplied()} before trusting a tight aim.
 */
public final class CellSighting {

    private final HiveCell cell;
    private final List<Integer> tagIds;
    private final Vec3 rowCentreRobot;
    private final boolean lateralCorrectionApplied;
    private final double totalAreaPercent;
    private final long captureTimeNs;

    private CellSighting(
            HiveCell cell,
            List<Integer> tagIds,
            Vec3 rowCentreRobot,
            boolean lateralCorrectionApplied,
            double totalAreaPercent,
            long captureTimeNs) {
        this.cell = cell;
        this.tagIds = Collections.unmodifiableList(tagIds);
        this.rowCentreRobot = rowCentreRobot;
        this.lateralCorrectionApplied = lateralCorrectionApplied;
        this.totalAreaPercent = totalAreaPercent;
        this.captureTimeNs = captureTimeNs;
    }

    /** One visible member tag, already converted into the robot frame. */
    public static final class Member {
        public final int tagId;
        public final Vec3 positionRobot;
        public final double areaPercent;

        public Member(int tagId, Vec3 positionRobot, double areaPercent) {
            this.tagId = tagId;
            this.positionRobot = positionRobot;
            this.areaPercent = areaPercent;
        }
    }

    /**
     * Combines the visible members of one cell into a sighting.
     *
     * @param cell          the cell all {@code members} belong to
     * @param members       at least one member, all from {@code cell}
     * @param captureTimeNs when the underlying camera frame was captured
     * @return the combined sighting
     * @throws IllegalArgumentException if {@code members} is empty or mixes cells
     */
    public static CellSighting fromMembers(HiveCell cell, List<Member> members, long captureTimeNs) {
        if (members == null || members.isEmpty()) {
            throw new IllegalArgumentException("Need at least one member tag to build a sighting");
        }

        List<Integer> ids = new ArrayList<>(members.size());
        Vec3 positionSum = Vec3.ZERO;
        double clusterXSum = 0.0;
        double areaSum = 0.0;

        for (Member member : members) {
            if (BiobuzzTags.cellForTag(member.tagId) != cell) {
                throw new IllegalArgumentException(
                        "Tag " + member.tagId + " does not belong to " + cell);
            }
            ids.add(member.tagId);
            positionSum = positionSum.plus(member.positionRobot);
            clusterXSum += BiobuzzTags.memberOffset(member.tagId).x();
            areaSum += member.areaPercent;
        }

        int count = members.size();
        Vec3 centroid = positionSum.times(1.0 / count);
        double meanClusterX = clusterXSum / count;

        // With two or more tags, the direction of the cluster's lateral axis falls
        // out of the tag positions themselves — no orientation convention needed.
        // Use the widest-separated pair so the estimate is least sensitive to noise.
        Vec3 rowCentre = centroid;
        boolean corrected = false;
        if (count >= 2) {
            Vec3 lateralAxis = lateralAxisRobot(members);
            if (lateralAxis != null) {
                rowCentre = centroid.minus(lateralAxis.times(meanClusterX));
                corrected = true;
            }
        }

        return new CellSighting(cell, ids, rowCentre, corrected, areaSum, captureTimeNs);
    }

    /**
     * Robot-frame direction of the cluster's lateral (+x) axis, scaled so that one
     * inch of cluster x is one unit of the returned vector. Null if the members
     * chosen sit at the same cluster x, which leaves the direction undetermined.
     */
    private static Vec3 lateralAxisRobot(List<Member> members) {
        Member low = null;
        Member high = null;
        double lowX = Double.POSITIVE_INFINITY;
        double highX = Double.NEGATIVE_INFINITY;

        for (Member member : members) {
            double x = BiobuzzTags.memberOffset(member.tagId).x();
            if (x < lowX) { lowX = x; low = member; }
            if (x > highX) { highX = x; high = member; }
        }

        double span = highX - lowX;
        if (low == null || high == null || Math.abs(span) < 1e-9) return null;
        return high.positionRobot.minus(low.positionRobot).times(1.0 / span);
    }

    public HiveCell cell() { return cell; }

    /** Ids of the member tags that contributed, in the order they were supplied. */
    public List<Integer> tagIds() { return tagIds; }

    public int tagCount() { return tagIds.size(); }

    /** Centre of the four-tag row, robot frame, inches. See the class docs. */
    public Vec3 rowCentreRobot() { return rowCentreRobot; }

    /** False when only one tag was visible, so the row centre may be off along the row. */
    public boolean lateralCorrectionApplied() { return lateralCorrectionApplied; }

    /** Horizontal distance to the measured point, inches. */
    public double groundRangeIn() { return rowCentreRobot.normXY(); }

    /** Straight-line distance to the measured point, inches. */
    public double slantRangeIn() { return rowCentreRobot.norm(); }

    /**
     * Horizontal angle to the measured point, radians, counter-clockwise positive
     * — so a positive bearing means "target is to the robot's left".
     *
     * <p>This is in the <em>chassis</em> frame. For a turret that does not carry
     * the camera, the command is this bearing minus the turret's current angle in
     * the same frame.
     */
    public double bearingRad() { return Math.atan2(rowCentreRobot.y(), rowCentreRobot.x()); }

    /** Angle above the horizontal to the measured point, radians. */
    public double elevationRad() { return Math.atan2(rowCentreRobot.z(), rowCentreRobot.normXY()); }

    /** Summed target area of the contributing tags, percent of image. A crude quality proxy. */
    public double totalAreaPercent() { return totalAreaPercent; }

    /** {@code System.nanoTime()}-based capture time of the frame this came from. */
    public long captureTimeNs() { return captureTimeNs; }

    /** Age of this sighting in milliseconds, as of now. */
    public double ageMs() { return (System.nanoTime() - captureTimeNs) / 1_000_000.0; }

    @Override
    public String toString() {
        return String.format(
                "%s tags=%s range=%.1fin bearing=%.1f° elev=%.1f°%s",
                cell, tagIds, groundRangeIn(),
                Math.toDegrees(bearingRad()), Math.toDegrees(elevationRad()),
                lateralCorrectionApplied ? "" : " (single tag, uncorrected)");
    }
}
