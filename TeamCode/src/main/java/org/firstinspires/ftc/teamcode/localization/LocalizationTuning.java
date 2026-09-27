package org.firstinspires.ftc.teamcode.localization;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.localization.FusionLocalizer;
import com.pedropathing.localization.Localizer;
import com.pedropathing.math.Pose;

/**
 * The numbers localization runs on. <b>Starting values</b>: the filter's are DECODE's, which ran
 * Pedro's {@link FusionLocalizer} all last season; the fix model's are a first guess until the Vision
 * Noise Tuner measures them on this robot. Read when the OpMode initializes.
 */
@Configurable
public final class LocalizationTuning {

    private LocalizationTuning() {
    }

    // Pedro FusionLocalizer, as DECODE configured it (pedroPathing/Constants.createFollower).
    public static double initialVarianceXY = 0.25;
    public static double initialVarianceHeadingDeg = 2.0;
    public static double processVarianceXY = 1.0;
    public static double processVarianceHeadingDegPerMin = 0.5;
    public static double headingMeasurementVariance = 0.0248;
    public static int historySize = 100;

    /** One-sigma error of a camera fix at close range, inches. Measure with Vision: Noise Tuner. */
    public static double fixSigmaIn = 1.5;

    /** Extra fix sigma per inch of range. From the Noise Tuner at two or three distances. */
    public static double fixSigmaPerInch = 0.02;

    /**
     * Added to a single-tag fix's variance, in², because one tag leaves the point up to 6.5 in off
     * along the row (see {@code CellSighting}).
     */
    public static double singleTagVarianceIn2 = 14.0;

    /** Start check: a robot further than this from its declared start should be nudged, inches. */
    public static double startMarginIn = 2.0;

    /** Pedro's filter over {@code odometry}, with the values above. */
    public static FusionLocalizer newFusionLocalizer(Localizer odometry) {
        double measuredXY = fixVariance(0.0, 2);
        return new FusionLocalizer(odometry,
                new Pose(initialVarianceXY, initialVarianceXY, Math.toRadians(initialVarianceHeadingDeg)),
                new Pose(processVarianceXY, processVarianceXY,
                        Math.toRadians(processVarianceHeadingDegPerMin) / 60.0),
                new Pose(measuredXY, measuredXY, headingMeasurementVariance),
                historySize);
    }

    /** Variance of a fix at {@code rangeIn} from {@code tagCount} tags, in². */
    static double fixVariance(double rangeIn, int tagCount) {
        double sigma = fixSigmaIn + fixSigmaPerInch * rangeIn;
        double variance = sigma * sigma;
        return tagCount <= 1 ? variance + singleTagVarianceIn2 : variance;
    }
}
