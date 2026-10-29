package org.firstinspires.ftc.teamcode.localization;

import org.firstinspires.ftc.teamcode.vision.BiobuzzTags;
import org.firstinspires.ftc.teamcode.vision.HiveCell;

/**
 * What the camera says about the robot's placement over the time it has sat untouched: the
 * calibration sample the start check logs at PLAY. Pure, no allocation.
 *
 * <p>Only the settled period counts. {@link #reset} is called whenever odometry sees the robot
 * move, and {@link #add} starts again by itself when a frame's position jumps more than
 * {@link #JUMP_IN} from the average so far (lifted and set down, which odometry may not see). So
 * what is averaged at PLAY is the robot's final placement, and {@link #seconds} says for how long.
 *
 * <p>Per frame it keeps: the gated position against the pose (which at INIT is the declared
 * start), the camera's own heading from the tag row ({@link CellFix#fit}) against the pose's, and
 * per CELL, each accepted tag's position against the pose and its range. A heading difference that
 * changes run to run is placement; one that follows a robot everywhere is its camera mount; one on
 * every robot at one field is that field. Per CELL: one CELL off and the others not is that CELL on
 * that field. An error that grows with range is camera pitch.
 */
public final class SettledStats {

    /** A frame this far from the average so far means the robot was moved: start again, inches. */
    public static final double JUMP_IN = 3.0;

    private static final HiveCell[] CELLS = HiveCell.values();

    private long sinceNs = -1;
    private long lastNs = -1;
    private int frames;
    private double sumX;
    private double sumY;
    private int headingFrames;
    private double sumHeadingDiff;
    private final int[] cellTags = new int[CELLS.length];
    private final double[] cellSumDx = new double[CELLS.length];
    private final double[] cellSumDy = new double[CELLS.length];
    private final double[] cellSumRange = new double[CELLS.length];

    public void reset() {
        sinceNs = -1;
        lastNs = -1;
        frames = 0;
        sumX = 0;
        sumY = 0;
        headingFrames = 0;
        sumHeadingDiff = 0;
        for (int c = 0; c < CELLS.length; c++) {
            cellTags[c] = 0;
            cellSumDx[c] = 0;
            cellSumDy[c] = 0;
            cellSumRange[c] = 0;
        }
    }

    /**
     * One gated frame ({@code gate} solved with the pose's heading, verdict OK), with the robot at
     * {@code poseX/poseY/poseHeading} and the camera's own heading {@code cameraHeading} (NaN if the
     * tags were too close together to give one), at {@code nowNs}.
     */
    public void add(TagGate gate, double poseX, double poseY, double poseHeading,
                    double cameraHeading, long nowNs) {
        if (frames >= 3 && Math.hypot(gate.x() - x(), gate.y() - y()) > JUMP_IN) {
            reset();
        }
        if (sinceNs < 0) sinceNs = nowNs;
        lastNs = nowNs;
        frames++;
        sumX += gate.x();
        sumY += gate.y();
        if (!Double.isNaN(cameraHeading)) {
            headingFrames++;
            sumHeadingDiff += Math.IEEEremainder(cameraHeading - poseHeading, 2 * Math.PI);
        }
        for (int i = 0; i < gate.count(); i++) {
            if (gate.reason(i) != TagGate.Reason.ACCEPTED) continue;
            HiveCell cell = BiobuzzTags.cellForTag(gate.tagId(i));
            if (cell == null) continue;
            int c = cell.ordinal();
            cellTags[c]++;
            cellSumDx[c] += gate.candidateX(i) - poseX;
            cellSumDy[c] += gate.candidateY(i) - poseY;
            cellSumRange[c] += gate.rangeIn(i);
        }
    }

    /** Frames averaged. */
    public int frames() { return frames; }

    /** How long the robot has sat untouched with the camera seeing it, seconds. */
    public double seconds() {
        return sinceNs < 0 ? 0.0 : (lastNs - sinceNs) / 1e9;
    }

    /** Average camera position, field inches; NaN with no frames. */
    public double x() { return frames == 0 ? Double.NaN : sumX / frames; }

    public double y() { return frames == 0 ? Double.NaN : sumY / frames; }

    /** Average camera heading minus the pose's, degrees; NaN if no frame gave a heading. */
    public double headingDiffDeg() {
        return headingFrames == 0 ? Double.NaN : Math.toDegrees(sumHeadingDiff / headingFrames);
    }

    public int headingFrames() { return headingFrames; }

    /** Tag sightings of {@code cell} averaged (a frame with three of its tags counts three). */
    public int tags(HiveCell cell) { return cellTags[cell.ordinal()]; }

    /** Average of {@code cell}'s tags' positions minus the pose, field x, inches; NaN if unseen. */
    public double cellDx(HiveCell cell) { return mean(cellSumDx, cell); }

    public double cellDy(HiveCell cell) { return mean(cellSumDy, cell); }

    /** Average range to {@code cell}'s tags, inches; NaN if unseen. */
    public double cellRangeIn(HiveCell cell) { return mean(cellSumRange, cell); }

    private double mean(double[] sums, HiveCell cell) {
        int n = cellTags[cell.ordinal()];
        return n == 0 ? Double.NaN : sums[cell.ordinal()] / n;
    }
}
