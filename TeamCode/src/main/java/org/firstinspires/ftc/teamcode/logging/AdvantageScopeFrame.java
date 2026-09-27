package org.firstinspires.ftc.teamcode.logging;

/**
 * Converts a Pedro pose (field corner origin, inches, radians CCW) into the frame AdvantageScope
 * draws the BIOBUZZ field in: <b>Center/Rotated</b>, meters and radians.
 *
 * <p><b>What is known.</b> The BIOBUZZ field asset ({@code Field2d_20262027FTCFieldV1}) declares
 * {@code "coordinateSystem": "center-rotated"}, 143.182 in square. AdvantageScope's docs define that
 * frame as "origin in the center of the field with the +X axis facing to the right from the
 * perspective of the red alliance wall", and its source (v26.0.2 and v27.0.0-alpha-6,
 * {@code geometry.ts}) turns a center-rotated pose into its internal frame as
 * {@code (x, y, θ) → (y, −x, θ − π/2)}.
 *
 * <p><b>The mapping</b>, from PsiKit ({@code psilynx/PsiKit, PedroFollowerOdometryLogger}, BSD
 * licence):
 * <pre>
 *     x_cr = −(y_pedro − 72) · 0.0254
 *     y_cr =  (x_pedro − 72) · 0.0254
 *     θ_cr =   θ_pedro + π/2
 * </pre>
 *
 * <p><b>Why it is right, traced through both programs' source.</b> "Pedro's frame" is the Pedro
 * Visualizer's ({@code Pedro-Pathing/Visualizer}, {@code App.svelte}): x from 0 to 144 in runs left
 * to right across the field image and y from 0 to 144 in runs bottom to top, so the origin is the
 * image's bottom-left corner. AdvantageScope undoes Center/Rotated with {@code (x, y, θ) → (y, −x,
 * θ − π/2)} ({@code geometry.ts}), which after this mapping gives {@code (x − 72, y − 72, θ)} in
 * inches, and its 2D renderer ({@code Field2dRenderer.ts}) draws that centred with +x to the right
 * and +y up the image. The two BIOBUZZ field images ({@code biobuzz.webp} and
 * {@code Field2d_20262027FTCFieldV1/image.png}) are drawn the same way up. So a pose lands at the
 * same place on the field in both. PsiKit's comment that "+X points toward the red alliance wall"
 * describes this badly, but its arithmetic is correct.
 *
 * <p>Still worth one look with real data: {@code VisualizerPathLogTest} turns Visualizer files into
 * logs, so the same Auto can be opened in both. Once seen, this belongs in {@code util/FieldFrame}.
 *
 * <p>Field centre is 72 in, Pedro's half-width. The asset is 143.182 in, so the drawing can be
 * off by up to ~0.4 in at the walls; that is below what the view can show.
 */
public final class AdvantageScopeFrame {

    public static final double METERS_PER_INCH = 0.0254;
    /** Pedro's field is 144 in; its centre is the origin AdvantageScope uses. */
    public static final double PEDRO_FIELD_CENTER_IN = 72.0;

    private AdvantageScopeFrame() {
    }

    public static double xMeters(double xPedroIn, double yPedroIn) {
        return -(yPedroIn - PEDRO_FIELD_CENTER_IN) * METERS_PER_INCH;
    }

    public static double yMeters(double xPedroIn, double yPedroIn) {
        return (xPedroIn - PEDRO_FIELD_CENTER_IN) * METERS_PER_INCH;
    }

    public static double headingRad(double headingPedroRad) {
        return wrap(headingPedroRad + Math.PI / 2.0);
    }

    /** Wraps an angle to [−π, π). */
    static double wrap(double rad) {
        double a = rad % (2.0 * Math.PI);
        if (a >= Math.PI) a -= 2.0 * Math.PI;
        if (a < -Math.PI) a += 2.0 * Math.PI;
        return a;
    }
}
