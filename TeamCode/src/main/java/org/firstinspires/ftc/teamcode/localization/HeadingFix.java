package org.firstinspires.ftc.teamcode.localization;

/**
 * The rule behind the heading cross-check ({@link LocalizationTuning#headingFixDeg}): pure, so a test
 * can pin it. The camera's heading comes from the whole-pose fit of two or more tags ({@code CellFix.fit},
 * the MegaTag1 of this code); the position fix uses the Pinpoint's heading ({@code CellFix.position},
 * the MegaTag2). When the two headings disagree by more than the fit's noise, the Pinpoint is the one
 * that is wrong, exactly the DECODE-season check, rebuilt on {@code HiveFieldPoints} because MegaTag
 * cannot know a tipped CELL. The fit has a heading only with tags on both HIVEs
 * ({@link LocalizationTuning#minFitLeverIn}); a one-CELL fit's heading is noise of 3-12 deg, and
 * is never compared.
 */
public final class HeadingFix {

    private HeadingFix() {
    }

    /** Whether {@code diffDeg} (camera minus Pinpoint, averaged over {@code frames}) calls for a correction. */
    public static boolean due(double diffDeg, int frames) {
        return frames >= LocalizationTuning.headingFixFrames && !Double.isNaN(diffDeg)
                && Math.abs(diffDeg) > LocalizationTuning.headingFixDeg;
    }

    /** The Pinpoint heading {@code headingRad} moved by the camera's {@code diffDeg}. */
    public static double corrected(double headingRad, double diffDeg) {
        return headingRad + Math.toRadians(diffDeg);
    }
}
