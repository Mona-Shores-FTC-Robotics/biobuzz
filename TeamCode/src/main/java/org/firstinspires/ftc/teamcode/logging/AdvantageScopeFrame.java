package org.firstinspires.ftc.teamcode.logging;

import org.firstinspires.ftc.teamcode.util.FieldFrame;

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
 * licence), with {@code c} the field centre:
 * <pre>
 *     x_cr = −(y_pedro − c) · 0.0254
 *     y_cr =  (x_pedro − c) · 0.0254
 *     θ_cr =   θ_pedro + π/2
 * </pre>
 *
 * <p><b>Why it is right, traced through both programs' source.</b> "Pedro's frame" is the Pedro
 * Visualizer's ({@code Pedro-Pathing/Visualizer}, {@code App.svelte}): x from 0 to 141.5 in runs left
 * to right across the field image and y from 0 to 141.5 in runs bottom to top, so the origin is the
 * image's bottom-left corner. AdvantageScope undoes Center/Rotated with {@code (x, y, θ) → (y, −x,
 * θ − π/2)} ({@code geometry.ts}), which after this mapping gives {@code (x − c, y − c, θ)} in
 * inches, and its 2D renderer ({@code Field2dRenderer.ts}) draws that centred with +x to the right
 * and +y up the image. The two BIOBUZZ field images ({@code biobuzz.webp} and
 * {@code Field2d_20262027FTCFieldV1/image.png}) are drawn the same way up. So a pose lands at the
 * same place on the field in both. PsiKit's comment that "+X points toward the red alliance wall"
 * describes this badly, but its arithmetic is correct.
 *
 * <p><b>Field centre: 70.75 in.</b> The current Visualizer (format 1.5.0) draws a 141.5 in field
 * ({@code FIELD_SIZE} in {@code src/config/defaults.ts}), and its Pedro 3 code export mirrors
 * alliances about {@code FIELD_SIZE / 2} ({@code PoseFactory.degrees().mirrorX(70.75)}). So 70.75 is
 * the centre Pedro 3 Autos are drawn around. AdvantageScope's BIOBUZZ asset is 143.182 in across;
 * the ~0.8 in difference in scale shows only at the walls and is below what the view resolves.
 *
 * <p>Still worth one look with real data: {@code VisualizerPathLogTest} turns Visualizer files into
 * logs, so the same Auto can be opened in both. Once seen, this belongs in {@code util/FieldFrame}.
 */
public final class AdvantageScopeFrame {

    public static final double METERS_PER_INCH = 0.0254;
    /** Half the Visualizer's 141.5 in field: the Pedro point AdvantageScope puts at its origin. */
    public static final double PEDRO_FIELD_CENTER_IN = FieldFrame.FIELD_CENTRE_INCHES;

    /** For log metadata: what frame the poses are in. */
    public static final String DESCRIPTION =
            "Pedro Visualizer inches (" + FieldFrame.FIELD_SIZE_INCHES + " in field, centre "
                    + FieldFrame.FIELD_CENTRE_INCHES + ") -> AdvantageScope Center/Rotated meters";

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
