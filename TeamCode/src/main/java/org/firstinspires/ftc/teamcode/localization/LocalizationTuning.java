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

    /**
     * Whether camera fixes correct the pose all match. <b>Off</b> until it is shown to help: a well
     * tuned Pinpoint tracks a whole match on its own, the CELLs' tags sit on thin polycarbonate that
     * flexes and moves, and a bad fix drags the pose somewhere false (Chief Delphi, "Idea about FTC
     * BIOBUZZ localization", #3, #4, #15). Turn on for #82 session 5, which compares a taped path
     * with and without it. Fixes are still counted while off, for the start check.
     */
    public static boolean continuousFixes = false;

    /**
     * Whether an unreferenced pose (no declared start, no Auto handoff) is set once from the
     * camera: x, y and heading from a settled CELL with two or more tags ({@code CellFix.pose}),
     * the same frames agreeing {@link #seedFrames} times running. The Pinpoint carries it from
     * there. DECODE did this with MegaTag1 ("HEADING_UNKNOWN").
     */
    public static boolean seedFromCamera = true;

    /** Camera poses in a row that must agree before seeding, so one bad frame cannot. */
    public static int seedFrames = 3;

    /** How close those camera poses must agree: inches, and degrees. */
    public static double seedAgreementIn = 2.0;
    public static double seedAgreementDeg = 3.0;

    /** A relocalize further than this from the current pose is refused, inches (DECODE's 18). */
    public static double maxRelocalizeJumpIn = 18.0;

    /** And further than this in heading, degrees (DECODE's 20). */
    public static double maxRelocalizeJumpDeg = 20.0;

    /**
     * "Still" for the stationary relocalize check: slower than this, inches per second, and turning
     * slower than {@link #stillTurnDegPerSec}, for at least {@link #stillForMs}.
     */
    public static double stillSpeedInPerSec = 1.0;
    public static double stillTurnDegPerSec = 2.0;
    public static double stillForMs = 250;

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
