package org.firstinspires.ftc.teamcode.localization;

/**
 * Fuses the Pinpoint's odometry with occasional AprilTag position fixes. Pure math, no hardware.
 *
 * <h2>How</h2>
 *
 * <p>The fused position is <b>the Pinpoint's position plus a correction offset</b> (x, y). The
 * Pinpoint keeps doing what it does best — integrating its pods at ~1.5 kHz — and this only
 * estimates how far it has drifted. Heading is never corrected: the Pinpoint's IMU heading is the
 * best heading on the robot, and fixes are computed <em>from</em> it (DECODE made the same call).
 *
 * <p>The offset has a variance {@code p} (in², same on both axes). It grows with distance driven,
 * because odometry drifts when wheels slip, not while parked. A fix with variance {@code r} moves
 * the offset by the Kalman gain {@code p / (p + r)} and shrinks {@code p}.
 *
 * <p><b>Latency.</b> A fix describes where the robot was when the camera took the frame, tens of
 * milliseconds ago. The last half-second of Pinpoint poses sits in a preallocated ring buffer; the
 * fix is compared with the Pinpoint's position <em>at the capture time</em>. Because the offset is
 * the same at every instant, nothing has to be replayed — DECODE's filter re-integrated velocity
 * through {@code TreeMap}s, which both allocated every loop and threw away the Pinpoint's own
 * integration accuracy.
 *
 * <h2>When a fix is believed</h2>
 *
 * <ol>
 *   <li>The heading is field-referenced — the pose was set from a known place (a declared start or
 *       Autonomous's handoff). Otherwise heading 0 means "the way the robot faced at init", and a
 *       fix computed from it is garbage.</li>
 *   <li>The capture time is inside the buffer.</li>
 *   <li>The robot was not turning fast at that moment — timing error becomes position error.</li>
 *   <li>The fix is within {@link #gateSigma} standard deviations of where the filter already thinks
 *       the robot is. One wild reading cannot move the pose. A run of rejections inflates {@code p}
 *       so that, after a real jolt (a collision), consistent fixes can win it back.</li>
 * </ol>
 *
 * <p>Every rejection is counted by reason, for the Driver Station's Robot page.
 */
public final class PoseFusion {

    /** Why the last fix was or was not used. */
    public enum Verdict {
        ACCEPTED, HEADING_NOT_REFERENCED, TOO_OLD, TURNING, OUTLIER
    }

    private static final int CAPACITY = 64;

    // Tuning. Starting values; measure the real ones (see LocalizationTuning).
    private final double driftVariancePerInch;
    private final double gateSigma;
    private final double maxTurnRateRadPerSec;
    private final double trustedSigmaIn;
    private final int rejectsBeforeInflate;
    private final double inflateFactor;

    private final long[] time = new long[CAPACITY];
    private final double[] rawX = new double[CAPACITY];
    private final double[] rawY = new double[CAPACITY];
    private final double[] rawH = new double[CAPACITY];
    private int newest = -1;
    private int count;

    private double offsetX;
    private double offsetY;
    private double p;
    private boolean headingReferenced;

    private int consecutiveRejects;
    private final int[] verdicts = new int[Verdict.values().length];
    private Verdict lastVerdict;

    public PoseFusion(double driftVariancePerInch, double gateSigma, double maxTurnRateRadPerSec,
                      double trustedSigmaIn, int rejectsBeforeInflate, double inflateFactor) {
        this.driftVariancePerInch = driftVariancePerInch;
        this.gateSigma = gateSigma;
        this.maxTurnRateRadPerSec = maxTurnRateRadPerSec;
        this.trustedSigmaIn = trustedSigmaIn;
        this.rejectsBeforeInflate = rejectsBeforeInflate;
        this.inflateFactor = inflateFactor;
        reset(Double.POSITIVE_INFINITY, false);
    }

    /**
     * Start over: zero offset, variance {@code sigmaIn²}, empty history. Call whenever the underlying
     * odometry is moved, since recorded history is in the old frame. {@code headingReferenced} says
     * whether headings from here on are field headings.
     */
    public void reset(double sigmaIn, boolean headingReferenced) {
        offsetX = 0.0;
        offsetY = 0.0;
        p = sigmaIn * sigmaIn;
        this.headingReferenced = headingReferenced;
        count = 0;
        newest = -1;
        consecutiveRejects = 0;
    }

    /** Record one odometry sample. Called every loop. Allocation-free. */
    public void recordOdometry(long timeNs, double x, double y, double heading) {
        if (count > 0) {
            double step = Math.hypot(x - rawX[newest], y - rawY[newest]);
            p += driftVariancePerInch * step;
        }
        newest = (newest + 1) % CAPACITY;
        time[newest] = timeNs;
        rawX[newest] = x;
        rawY[newest] = y;
        rawH[newest] = heading;
        if (count < CAPACITY) count++;
    }

    /**
     * Offer a fix: a cell's row centre at {@code (fieldX, fieldY)} was seen at
     * {@code (robotX, robotY)} in the robot frame (inches, +x forward, +y left), from a frame
     * captured at {@code captureNs}, with position variance {@code varianceIn2}.
     */
    public Verdict addFix(long captureNs, double fieldX, double fieldY,
                          double robotX, double robotY, double varianceIn2) {
        Verdict verdict = evaluate(captureNs, fieldX, fieldY, robotX, robotY, varianceIn2);
        verdicts[verdict.ordinal()]++;
        lastVerdict = verdict;
        if (verdict == Verdict.ACCEPTED) {
            consecutiveRejects = 0;
        } else if (verdict == Verdict.OUTLIER && ++consecutiveRejects >= rejectsBeforeInflate) {
            p *= inflateFactor;
            consecutiveRejects = 0;
        }
        return verdict;
    }

    private Verdict evaluate(long captureNs, double fieldX, double fieldY,
                             double robotX, double robotY, double r) {
        if (!headingReferenced) return Verdict.HEADING_NOT_REFERENCED;
        int after = indexAtOrAfter(captureNs);
        if (after < 0) return Verdict.TOO_OLD;
        int before = previous(after);
        if (before < 0) {
            if (time[after] != captureNs) return Verdict.TOO_OLD;
            before = after;
        }

        double f = time[after] == time[before] ? 0.0
                : (double) (captureNs - time[before]) / (time[after] - time[before]);
        double dh = wrap(rawH[after] - rawH[before]);
        double heading = rawH[before] + f * dh;
        if (time[after] != time[before]) {
            double rate = Math.abs(dh) / ((time[after] - time[before]) / 1e9);
            if (rate > maxTurnRateRadPerSec) return Verdict.TURNING;
        }
        double x = rawX[before] + f * (rawX[after] - rawX[before]);
        double y = rawY[before] + f * (rawY[after] - rawY[before]);

        // Where the fix says the robot was: the field point minus the sighting, rotated into the field.
        double c = Math.cos(heading);
        double s = Math.sin(heading);
        double measuredX = fieldX - (c * robotX - s * robotY);
        double measuredY = fieldY - (s * robotX + c * robotY);

        double innovX = measuredX - (x + offsetX);
        double innovY = measuredY - (y + offsetY);
        double total = p + r;
        if (!Double.isInfinite(p)
                && (innovX * innovX + innovY * innovY) / total > gateSigma * gateSigma) {
            return Verdict.OUTLIER;
        }
        double gain = Double.isInfinite(p) ? 1.0 : p / total;
        offsetX += gain * innovX;
        offsetY += gain * innovY;
        p = Double.isInfinite(p) ? r : (1.0 - gain) * p;
        return Verdict.ACCEPTED;
    }

    /** Index of the oldest sample at or after {@code t}, or -1 if {@code t} is outside the buffer. */
    private int indexAtOrAfter(long t) {
        if (count == 0 || t > time[newest]) return -1;
        int i = newest;
        for (int n = 0; n < count; n++) {
            int prev = previous(i);
            if (prev < 0 || time[prev] < t) return (time[i] >= t) ? i : -1;
            i = prev;
        }
        return -1;
    }

    private int previous(int i) {
        int oldest = (newest - count + 1 + CAPACITY) % CAPACITY;
        return i == oldest ? -1 : (i - 1 + CAPACITY) % CAPACITY;
    }

    private static double wrap(double a) {
        while (a > Math.PI) a -= 2 * Math.PI;
        while (a < -Math.PI) a += 2 * Math.PI;
        return a;
    }

    // ---------------------------------------------------------------- reads

    public double offsetX() { return offsetX; }

    public double offsetY() { return offsetY; }

    /** One standard deviation of the position estimate, inches. Infinite until anything anchors it. */
    public double sigmaIn() { return Math.sqrt(p); }

    /** Whether the fused position is good to {@code trustedSigmaIn} and headings are field headings. */
    public boolean isTrusted() {
        return headingReferenced && sigmaIn() <= trustedSigmaIn;
    }

    public boolean isHeadingReferenced() { return headingReferenced; }

    public int count(Verdict verdict) { return verdicts[verdict.ordinal()]; }

    public Verdict lastVerdict() { return lastVerdict; }
}
