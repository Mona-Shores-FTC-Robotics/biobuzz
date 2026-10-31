package org.firstinspires.ftc.teamcode.localization;

import org.firstinspires.ftc.teamcode.util.FieldFrame;

/**
 * Decides which tags in one camera frame may move the pose, and where they put the robot. Pure
 * math, filled and solved once per frame with no allocation.
 *
 * <p>The checks are FRC 3572's (Wavelength's {@code Vision.java}, the 6328 pattern), turned into
 * what a heading-locked solve on moving CELLs can test:
 *
 * <table>
 *   <tr><th>Wavelength</th><th>Here</th></tr>
 *   <tr><td>no tags</td><td>fewer than {@link LocalizationTuning#minFixTags} accepted ({@link Verdict#TOO_FEW_TAGS})</td></tr>
 *   <tr><td>|z| too big</td><td>a tag's measured height is off its map height
 *       ({@link Reason#HEIGHT}): a camera pitch error, a flexing CELL or a misread state shows
 *       here first, one tag at a time</td></tr>
 *   <tr><td>tag too far</td><td>{@link Reason#RANGE}</td></tr>
 *   <tr><td>single-tag ambiguity</td><td>the Limelight reports none, so a tag that disagrees
 *       with the others' median is dropped instead ({@link Reason#OUTLIER})</td></tr>
 *   <tr><td>outside the field</td><td>{@link Verdict#OUTSIDE_FIELD}</td></tr>
 * </table>
 *
 * <p>Each tag gives a position on its own: its field point minus where the robot sees it, turned
 * into the field by the Pinpoint's heading ({@link CellFix#position}). Heading is never solved
 * here; with no heading ({@code NaN}, an unreferenced pose) only the height and range checks run,
 * and {@link #accepted} feeds {@link CellFix#fit} instead.
 */
public final class TagGate {

    /** What happened to one tag. */
    public enum Reason { ACCEPTED, HEIGHT, RANGE, OUTLIER }

    /** What happened to the frame. */
    public enum Verdict { OK, NO_TAGS, TOO_FEW_TAGS, OUTSIDE_FIELD }

    private final int capacity;
    private int count;
    private final int[] tagId;
    private final double[] fieldX;
    private final double[] fieldY;
    private final double[] fieldZ;
    private final double[] robotX;
    private final double[] robotY;
    private final double[] robotZ;

    private final double[] candidateX;
    private final double[] candidateY;
    private final Reason[] reason;
    private final double[] scratch;

    /** The accepted tags, packed, for {@link CellFix#fit}. */
    private final double[] acceptedFieldX;
    private final double[] acceptedFieldY;
    private final double[] acceptedRobotX;
    private final double[] acceptedRobotY;
    private int accepted;

    private Verdict verdict = Verdict.NO_TAGS;
    private double x = Double.NaN;
    private double y = Double.NaN;
    private double nearestRangeIn = Double.NaN;

    public TagGate(int capacity) {
        this.capacity = capacity;
        tagId = new int[capacity];
        fieldX = new double[capacity];
        fieldY = new double[capacity];
        fieldZ = new double[capacity];
        robotX = new double[capacity];
        robotY = new double[capacity];
        robotZ = new double[capacity];
        candidateX = new double[capacity];
        candidateY = new double[capacity];
        reason = new Reason[capacity];
        scratch = new double[capacity];
        acceptedFieldX = new double[capacity];
        acceptedFieldY = new double[capacity];
        acceptedRobotX = new double[capacity];
        acceptedRobotY = new double[capacity];
    }

    /** Empties the frame. */
    public void clear() {
        count = 0;
        accepted = 0;
        verdict = Verdict.NO_TAGS;
        x = Double.NaN;
        y = Double.NaN;
        nearestRangeIn = Double.NaN;
    }

    /**
     * One tag: where it is on the field and where the robot sees it (robot frame, inches, z above
     * the floor). Ignored once full.
     */
    public void add(int id, double fx, double fy, double fz, double rx, double ry, double rz) {
        if (count >= capacity) return;
        tagId[count] = id;
        fieldX[count] = fx;
        fieldY[count] = fy;
        fieldZ[count] = fz;
        robotX[count] = rx;
        robotY[count] = ry;
        robotZ[count] = rz;
        reason[count] = Reason.ACCEPTED;
        candidateX[count] = Double.NaN;
        candidateY[count] = Double.NaN;
        count++;
    }

    /**
     * Runs every check on the tags added since {@link #clear}, with the robot at
     * {@code headingRad} (NaN if not known: height and range checks only), and returns the verdict.
     */
    public Verdict solve(double headingRad) {
        accepted = 0;
        x = Double.NaN;
        y = Double.NaN;
        nearestRangeIn = Double.NaN;
        if (count == 0) return verdict = Verdict.NO_TAGS;

        boolean headed = !Double.isNaN(headingRad);
        double c = headed ? Math.cos(headingRad) : 0.0;
        double s = headed ? Math.sin(headingRad) : 0.0;
        int alive = 0;
        for (int i = 0; i < count; i++) {
            if (Math.abs(robotZ[i] - fieldZ[i]) > LocalizationTuning.maxTagHeightErrorIn) {
                reason[i] = Reason.HEIGHT;
            } else if (Math.hypot(robotX[i], robotY[i]) > LocalizationTuning.maxTagRangeIn) {
                reason[i] = Reason.RANGE;
            } else {
                reason[i] = Reason.ACCEPTED;
                alive++;
            }
            if (headed) {
                candidateX[i] = fieldX[i] - (c * robotX[i] - s * robotY[i]);
                candidateY[i] = fieldY[i] - (s * robotX[i] + c * robotY[i]);
            }
        }

        if (headed && alive >= 2) {
            double medianX = median(candidateX);
            double medianY = median(candidateY);
            for (int i = 0; i < count; i++) {
                if (reason[i] == Reason.ACCEPTED && Math.hypot(candidateX[i] - medianX,
                        candidateY[i] - medianY) > LocalizationTuning.tagOutlierIn) {
                    reason[i] = Reason.OUTLIER;
                }
            }
        }

        double sumX = 0, sumY = 0, nearest = Double.POSITIVE_INFINITY;
        for (int i = 0; i < count; i++) {
            if (reason[i] != Reason.ACCEPTED) continue;
            acceptedFieldX[accepted] = fieldX[i];
            acceptedFieldY[accepted] = fieldY[i];
            acceptedRobotX[accepted] = robotX[i];
            acceptedRobotY[accepted] = robotY[i];
            accepted++;
            nearest = Math.min(nearest, Math.hypot(robotX[i], robotY[i]));
            if (headed) {
                sumX += candidateX[i];
                sumY += candidateY[i];
            }
        }
        if (accepted > 0) nearestRangeIn = nearest;
        if (accepted < Math.max(1, LocalizationTuning.minFixTags)) return verdict = Verdict.TOO_FEW_TAGS;
        if (!headed) return verdict = Verdict.OK;

        x = sumX / accepted;
        y = sumY / accepted;
        if (x < 0 || x > FieldFrame.FIELD_SIZE_INCHES || y < 0 || y > FieldFrame.FIELD_SIZE_INCHES) {
            return verdict = Verdict.OUTSIDE_FIELD;
        }
        return verdict = Verdict.OK;
    }

    /** The median over the tags still accepted. Only called with two or more. */
    private double median(double[] values) {
        int n = 0;
        for (int i = 0; i < count; i++) {
            if (reason[i] != Reason.ACCEPTED) continue;
            // Insertion sort: at most 16 values, and no allocation.
            double v = values[i];
            int j = n++;
            while (j > 0 && scratch[j - 1] > v) {
                scratch[j] = scratch[j - 1];
                j--;
            }
            scratch[j] = v;
        }
        return n % 2 == 1 ? scratch[n / 2] : (scratch[n / 2 - 1] + scratch[n / 2]) / 2.0;
    }

    // ----------------------------------------------------------- results

    public Verdict verdict() { return verdict; }

    /** The robot's field x from the accepted tags, inches; NaN unless solved with a heading. */
    public double x() { return x; }

    /** The robot's field y, inches; NaN unless solved with a heading. */
    public double y() { return y; }

    /** Tags added this frame. */
    public int count() { return count; }

    /** Tags that passed every check. */
    public int acceptedCount() { return accepted; }

    /** Ground distance to the nearest accepted tag, inches; NaN with none. */
    public double nearestRangeIn() { return nearestRangeIn; }

    /** Packed field x of the accepted tags, {@link #acceptedCount} long; likewise the others. */
    public double[] acceptedFieldX() { return acceptedFieldX; }
    public double[] acceptedFieldY() { return acceptedFieldY; }
    public double[] acceptedRobotX() { return acceptedRobotX; }
    public double[] acceptedRobotY() { return acceptedRobotY; }

    // --------------------------------------------------------- per tag

    public int tagId(int i) { return tagId[i]; }

    public Reason reason(int i) { return reason[i]; }

    /** Where tag {@code i} alone puts the robot, field x, inches; NaN without a heading. */
    public double candidateX(int i) { return candidateX[i]; }

    public double candidateY(int i) { return candidateY[i]; }

    /** Measured height minus map height of tag {@code i}, inches. */
    public double heightErrorIn(int i) { return robotZ[i] - fieldZ[i]; }

    /** Ground distance to tag {@code i}, inches. */
    public double rangeIn(int i) { return Math.hypot(robotX[i], robotY[i]); }
}
