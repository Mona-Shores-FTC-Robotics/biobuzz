package org.firstinspires.ftc.teamcode.localization;

import com.bylazar.configurables.annotations.Configurable;

/**
 * The numbers the fusion runs on. <b>Starting values, not measurements</b> — each says how to get
 * the real one. Read once when the OpMode initializes; restart the OpMode after a Panels edit.
 */
@Configurable
public final class LocalizationTuning {

    private LocalizationTuning() {
    }

    /**
     * Odometry drift, in² of variance per inch driven. Measure: drive a known 100 in several times,
     * compare the Pinpoint's distance with the tape; variance of the error ÷ 100.
     */
    public static double driftVariancePerInch = 0.01;

    /**
     * Base one-sigma error of a camera fix at close range, inches. Measure: Vision: Noise Tuner on a
     * stationary robot, the range standard deviation.
     */
    public static double fixSigmaIn = 1.0;

    /** Extra fix sigma per inch of range. From the Noise Tuner at two or three distances. */
    public static double fixSigmaPerInch = 0.02;

    /** Pinpoint heading one-sigma, radians — a heading error becomes position error times range. */
    public static double headingSigmaRad = Math.toRadians(1.0);

    /**
     * Added to a single-tag fix's variance, in², because one tag leaves the point up to 6.5 in off
     * along the row (see {@code CellSighting}). 14 in² is that ±6.5 in spread as a uniform variance.
     */
    public static double singleTagVarianceIn2 = 14.0;

    /** A fix further than this many sigmas from the estimate is an outlier. */
    public static double gateSigma = 3.0;

    /** Frames taken while turning faster than this are not used, rad/s. */
    public static double maxTurnRateRadPerSec = 2.0;

    /** The pose is "trusted" — pose-dependent features may use it — under this one-sigma, inches. */
    public static double trustedSigmaIn = 3.0;

    /** After this many outliers in a row, the estimate's uncertainty is inflated so it can recover. */
    public static int rejectsBeforeInflate = 5;

    /** How much to inflate the variance by, after {@link #rejectsBeforeInflate} outliers. */
    public static double inflateFactor = 4.0;

    /** Sigma for a pose handed over from Autonomous, inches. */
    public static double handoffSigmaIn = 2.0;

    /**
     * Sigma for a declared start position before the camera confirms it, inches. Loose on purpose:
     * wide enough that fixes can pull a badly placed robot to where it really is, which is how the
     * start check measures the placement.
     */
    public static double declaredStartSigmaIn = 12.0;

    /** Start check: a robot placed further than this from its declared start should be nudged, in. */
    public static double startMarginIn = 2.0;

    static PoseFusion newFusion() {
        return new PoseFusion(driftVariancePerInch, gateSigma, maxTurnRateRadPerSec, trustedSigmaIn,
                rejectsBeforeInflate, inflateFactor);
    }

    /** Variance of a fix at {@code rangeIn} from {@code tagCount} tags, in². */
    static double fixVariance(double rangeIn, int tagCount) {
        double sigma = fixSigmaIn + fixSigmaPerInch * rangeIn;
        double headingTerm = rangeIn * headingSigmaRad;
        double variance = sigma * sigma + headingTerm * headingTerm;
        return tagCount <= 1 ? variance + singleTagVarianceIn2 : variance;
    }
}
