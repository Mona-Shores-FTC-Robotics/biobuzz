package org.firstinspires.ftc.teamcode.localization;

import com.pedropathing.math.Pose;

import java.util.Arrays;
import java.util.Locale;

/**
 * What the pose <i>would</i> be with {@link LocalizationTuning#continuousFixes} on: a second pose that
 * rides on the real one, takes every gated camera frame seen while moving and every still fix, and is
 * logged beside it, never driven on. So every run, a practice or a match, measures continuous fixes
 * with no risk to the robot. Its score is the one {@code fusion_replay.py} (the localization-triage
 * skill) uses on old logs: at the first still fix after a drive, how far the camera disagreed with
 * the real pose and with this one on arrival ({@link #summary}). Still frames are the camera's best
 * reading, so a smaller arrival gap is a pose that stayed nearer the camera while driving.
 *
 * <p><b>The same arithmetic as Pedro's {@code FusionLocalizer}, in x and y.</b> Driving grows the
 * covariance by {@link LocalizationTuning#processVarianceXY} per inch, along the robot's own axes
 * ({@code FusionLocalizer.updateCovariance}); a camera position is a Kalman measurement at its
 * capture time, with a Joseph-form covariance update. Two simplifications: the real pose at the
 * capture time is interpolated from the last few loops (Pedro replays its history), and a frame
 * more than {@code jumpIn} from the shadow is not used (Pedro has no gate; the caller applies the same
 * one to the real pose). Heading is the real pose's: camera fixes never set heading here.
 *
 * <p>Pure arithmetic, allocation-free per loop: the shadow is the real pose plus an offset, odometry
 * moves both the same, and a hard set of the real pose clears the offset ({@link #follow}). With
 * {@code continuousFixes} on the real pose does what the shadow models, so the caller zeroes the
 * offset ({@link #matchReal}) and the arrival scores then read the real pose with them on.
 */
public final class FusionShadow {

    /** A stop counts as an arrival after at least this much driving since the last still fix, inches. */
    public static final double ARRIVAL_MIN_DRIVE_IN = 3.0;

    /** Real poses kept for capture-time interpolation: about half a second of loops. */
    private static final int HISTORY = 64;

    /** Arrival scores kept for {@link #summary}. */
    private static final int ARRIVALS_KEPT = 256;

    private final long[] histNs = new long[HISTORY];
    private final double[] histX = new double[HISTORY];
    private final double[] histY = new double[HISTORY];
    private int histNext;
    private int histCount;

    private double offsetX;
    private double offsetY;
    private double pxx;
    private double pxy;
    private double pyy;
    private double lastX = Double.NaN;
    private double lastY = Double.NaN;
    private double drivenSinceFixIn = Double.POSITIVE_INFINITY;

    private int framesUsed;
    private int framesGated;
    private int arrivals;
    private double lastArrivalRealIn = Double.NaN;
    private double lastArrivalShadowIn = Double.NaN;
    private double lastArrivalDrivenIn = Double.NaN;
    private final double[] arrivalReal = new double[ARRIVALS_KEPT];
    private final double[] arrivalShadow = new double[ARRIVALS_KEPT];

    public FusionShadow() {
        follow();
    }

    /** The shadow for the real pose {@code real}. Null for null. */
    public Pose of(Pose real) {
        return real == null ? null : new Pose(real.x() + offsetX, real.y() + offsetY, real.heading());
    }

    /** The real pose was set on purpose (a start, a handoff, a reset, a relocalize): the shadow goes with it. */
    public void follow() {
        offsetX = 0;
        offsetY = 0;
        pxx = LocalizationTuning.initialVarianceXY;
        pyy = LocalizationTuning.initialVarianceXY;
        pxy = 0;
        histCount = 0;
        lastX = Double.NaN;
        lastY = Double.NaN;
        drivenSinceFixIn = Double.POSITIVE_INFINITY;
    }

    /** The real pose already takes continuous fixes: the shadow sits on it. Keeps the covariance and the scores. */
    public void matchReal() {
        offsetX = 0;
        offsetY = 0;
    }

    /**
     * Every loop, with the real pose after odometry: the covariance grows with the distance driven,
     * along the robot's own forward and left, as Pedro's does.
     */
    public void odometry(Pose real, long nowNs) {
        if (real == null) return;
        if (!Double.isNaN(lastX)) {
            double dx = real.x() - lastX;
            double dy = real.y() - lastY;
            double c = Math.cos(real.heading());
            double s = Math.sin(real.heading());
            double forward = Math.abs(c * dx + s * dy) * LocalizationTuning.processVarianceXY;
            double left = Math.abs(-s * dx + c * dy) * LocalizationTuning.processVarianceXY;
            pxx += c * c * forward + s * s * left;
            pyy += s * s * forward + c * c * left;
            pxy += c * s * (forward - left);
            drivenSinceFixIn += Math.hypot(dx, dy);
        }
        lastX = real.x();
        lastY = real.y();
        histNs[histNext] = nowNs;
        histX[histNext] = real.x();
        histY[histNext] = real.y();
        histNext = (histNext + 1) % HISTORY;
        histCount = Math.min(histCount + 1, HISTORY);
    }

    /**
     * A gated camera position taken while moving, captured at {@code captureNs} (the same clock as
     * {@link #odometry}), with measurement variance {@code variance} (in²). Not used, and counted, if
     * it is more than {@code jumpIn} from the shadow. Returns whether it was used.
     */
    public boolean continuousFix(double cameraX, double cameraY, long captureNs, double variance, double jumpIn) {
        if (histCount == 0) return false;
        double[] real = realAt(captureNs);
        double ix = cameraX - (real[0] + offsetX);
        double iy = cameraY - (real[1] + offsetY);
        if (Math.hypot(ix, iy) > jumpIn) {
            framesGated++;
            return false;
        }
        update(ix, iy, variance);
        framesUsed++;
        return true;
    }

    private final double[] realAtScratch = new double[2];

    /** The real pose at {@code t}, interpolated from the history; the nearest end outside it. */
    private double[] realAt(long t) {
        int newest = (histNext - 1 + HISTORY) % HISTORY;
        int index = newest;
        if (t < histNs[newest]) {
            // Walk back to the newest entry at or before t; it then has a newer neighbour.
            for (int k = 1; k < histCount; k++) {
                int older = (newest - k + HISTORY) % HISTORY;
                if (histNs[older] <= t) {
                    int newer = (older + 1) % HISTORY;
                    double span = histNs[newer] - histNs[older];
                    double f = span > 0 ? (t - histNs[older]) / span : 0.0;
                    realAtScratch[0] = histX[older] + f * (histX[newer] - histX[older]);
                    realAtScratch[1] = histY[older] + f * (histY[newer] - histY[older]);
                    return realAtScratch;
                }
                index = older;   // older than everything kept: the oldest
            }
        }
        realAtScratch[0] = histX[index];
        realAtScratch[1] = histY[index];
        return realAtScratch;
    }

    /**
     * A still fix at ({@code cameraX}, {@code cameraY}) with variance {@code variance}: the real pose was
     * {@code before} and is now {@code after} (the same pose when still fixes are off). Scores the
     * arrival if the robot drove here, then takes the fix as a measurement.
     */
    public void stillFix(double cameraX, double cameraY, double variance, Pose before, Pose after) {
        double shadowX = before.x() + offsetX;
        double shadowY = before.y() + offsetY;
        if (drivenSinceFixIn >= ARRIVAL_MIN_DRIVE_IN && drivenSinceFixIn != Double.POSITIVE_INFINITY) {
            lastArrivalRealIn = Math.hypot(cameraX - before.x(), cameraY - before.y());
            lastArrivalShadowIn = Math.hypot(cameraX - shadowX, cameraY - shadowY);
            lastArrivalDrivenIn = drivenSinceFixIn;
            arrivalReal[arrivals % ARRIVALS_KEPT] = lastArrivalRealIn;
            arrivalShadow[arrivals % ARRIVALS_KEPT] = lastArrivalShadowIn;
            arrivals++;
        }
        update(cameraX - shadowX, cameraY - shadowY, variance);
        // The real pose may have moved onto the fix: keep the shadow where the update put it.
        offsetX += before.x() - after.x();
        offsetY += before.y() - after.y();
        lastX = after.x();
        lastY = after.y();
        histCount = 0;
        drivenSinceFixIn = 0;
    }

    /** Kalman update of the offset with innovation (ix, iy) and isotropic variance r; Joseph form. */
    private void update(double ix, double iy, double r) {
        double sxx = pxx + r;
        double syy = pyy + r;
        double det = sxx * syy - pxy * pxy;
        if (!(det > 0)) return;
        double ixx = syy / det;
        double ixy = -pxy / det;
        double iyy = sxx / det;
        double kxx = pxx * ixx + pxy * ixy;
        double kxy = pxx * ixy + pxy * iyy;
        double kyx = pxy * ixx + pyy * ixy;
        double kyy = pxy * ixy + pyy * iyy;
        offsetX += kxx * ix + kxy * iy;
        offsetY += kyx * ix + kyy * iy;
        double a = 1 - kxx;
        double b = -kxy;
        double c = -kyx;
        double d = 1 - kyy;
        double ap00 = a * pxx + b * pxy;
        double ap01 = a * pxy + b * pyy;
        double ap10 = c * pxx + d * pxy;
        double ap11 = c * pxy + d * pyy;
        double nxx = ap00 * a + ap01 * b + r * (kxx * kxx + kxy * kxy);
        double nxy = ap00 * c + ap01 * d + r * (kxx * kyx + kxy * kyy);
        double nyy = ap10 * c + ap11 * d + r * (kyx * kyx + kyy * kyy);
        pxx = Math.max(nxx, 1e-6);
        pxy = nxy;
        pyy = Math.max(nyy, 1e-6);
    }

    public double offsetX() { return offsetX; }
    public double offsetY() { return offsetY; }

    /** How far the shadow sits from the real pose, inches. */
    public double offsetIn() { return Math.hypot(offsetX, offsetY); }

    /** The shadow's x variance, in². */
    public double varianceX() { return pxx; }

    /** The shadow's y variance, in². */
    public double varianceY() { return pyy; }

    public int framesUsed() { return framesUsed; }
    public int framesGated() { return framesGated; }

    /** Stops scored so far: still fixes after at least {@link #ARRIVAL_MIN_DRIVE_IN} of driving. */
    public int arrivals() { return arrivals; }

    /** At the last scored stop: how far the camera was from the real pose on arrival, inches. */
    public double lastArrivalRealIn() { return lastArrivalRealIn; }

    /** At the last scored stop: how far the camera was from this shadow on arrival, inches. */
    public double lastArrivalShadowIn() { return lastArrivalShadowIn; }

    /** At the last scored stop: inches driven since the still fix before it. */
    public double lastArrivalDrivenIn() { return lastArrivalDrivenIn; }

    /** One line for the match log: the run's arrival scores, real against shadow. */
    public String summary() {
        int n = Math.min(arrivals, ARRIVALS_KEPT);
        if (n == 0) {
            return String.format(Locale.US, "fusion shadow: no stops scored (frames used %d, gated %d)",
                    framesUsed, framesGated);
        }
        double[] real = Arrays.copyOf(arrivalReal, n);
        double[] shadow = Arrays.copyOf(arrivalShadow, n);
        int nearer = 0;
        int worse = 0;
        for (int i = 0; i < n; i++) {
            if (shadow[i] < real[i]) nearer++;
            if (shadow[i] > real[i] + 1.0) worse++;
        }
        Arrays.sort(real);
        Arrays.sort(shadow);
        return String.format(Locale.US,
                "fusion shadow: %d stops; arrival median real %.2f in, shadow %.2f in; worst real %.1f,"
                        + " shadow %.1f; shadow nearer at %d, over 1 in further at %d; frames used %d, gated %d",
                n, median(real), median(shadow), real[n - 1], shadow[n - 1], nearer, worse, framesUsed, framesGated);
    }

    private static double median(double[] sorted) {
        int n = sorted.length;
        return n % 2 == 1 ? sorted[n / 2] : (sorted[n / 2 - 1] + sorted[n / 2]) / 2.0;
    }
}
