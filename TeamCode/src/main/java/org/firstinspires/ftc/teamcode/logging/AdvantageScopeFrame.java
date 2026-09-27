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
 * <p><b>What is assumed.</b> The mapping below is PsiKit's
 * ({@code psilynx/PsiKit, PedroFollowerOdometryLogger}, BSD licence):
 * <pre>
 *     x_cr = −(y_pedro − 72) · 0.0254
 *     y_cr =  (x_pedro − 72) · 0.0254
 *     θ_cr =   θ_pedro + π/2
 * </pre>
 * Composed with AdvantageScope's own transform, that draws a Pedro pose with its centre moved to
 * the field centre and its axes unchanged, which is the right answer if and only if Pedro's axes
 * line up with AdvantageScope's internal ones on the BIOBUZZ field. PsiKit's comment says Pedro
 * +X points toward the red wall; AdvantageScope's source puts its internal +X away from the red
 * wall (Center/Red is its identity case). Those two statements disagree, so <b>this mapping is
 * unconfirmed</b>.
 *
 * <p>"Pedro's frame" here means the Pedro Visualizer's: inches on a 144 in field, drawn on the
 * season's field image. Pedro 3's {@code Pose} carries no frame of its own, so the Visualizer is
 * what defines it, and paths drawn there are what the robot drives. The check is therefore
 * {@code VisualizerPathLogTest}: it turns DECODE Autos the team really ran into logs, and opening
 * one in AdvantageScope on the 2025–26 field next to the same {@code .pp} in the Visualizer shows
 * at once whether this mapping is right, mirrored, or rotated. Once settled, this belongs in
 * {@code util/FieldFrame} as a measured fact.
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
