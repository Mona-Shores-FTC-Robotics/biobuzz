package org.firstinspires.ftc.teamcode.logging;

import java.util.Locale;

/**
 * A robot shape for the spill studies ({@link BodyShapeSpillTest}), one that fits R105's 18 × 24 in
 * once the match starts: a {@code length} × {@code width} frame; {@code slide}, side walls slid that
 * far forward (the long U); {@code out}/{@code ahead}, each flap's free end from its hinge at a front
 * corner. {@link RobotAssets#shapesRobot} draws them for AdvantageScope.
 */
final class BodyShape {

    final String name;
    final double length, width, slide, out, ahead;
    /** Flap height: {@link RobotDesign#flapHeightIn} unless set. */
    double flapHeight = Double.NaN;

    BodyShape(String name, double length, double width, double slide, double out, double ahead) {
        this.name = name;
        this.length = length;
        this.width = width;
        this.slide = slide;
        this.out = out;
        this.ahead = ahead;
    }

    /** The same shape with flaps {@code height} tall. */
    BodyShape low(double height) {
        BodyShape b = new BodyShape(name + String.format(Locale.ROOT, ", %.1f in tall", height), length, width, slide, out, ahead);
        b.flapHeight = height;
        return b;
    }

    RobotDesign design() {
        RobotDesign d = RobotDesign.standard().copy("turret, " + name);
        d.frameIn = length;
        d.frameWidthIn = width;
        d.intakeWidthIn = Math.min(d.intakeWidthIn, width);
        d.sideWallsSlideIn = slide;
        d.flapOutIn = out;
        d.flapForwardIn = ahead;
        if (!Double.isNaN(flapHeight)) d.flapHeightIn = flapHeight;
        return d.checked();
    }

    /** How far the front-most point is past the frame's front face. */
    double reach() {
        return Math.max(slide, ahead);
    }

    double[] footprint() {
        return design().footprintIn();
    }

    static final BodyShape PLAIN = new BodyShape("plain 18", 18, 18, 0, 0, 0);
    static final BodyShape LONG_U = new BodyShape("long U", 18, 18, RobotAssets.WALL_SLIDE_IN, 0, 0);
    /** The smaller body alone, to tell what the flaps add from what the size does. */
    static final BodyShape PLAIN_16 = new BodyShape("plain 16", 16, 16, 0, 0, 0);
    /** (a) 16 × 16 with flaps 3 in out and 2 in forward: 22 × 18 in. */
    static final BodyShape FLAPS_16 = new BodyShape("16 + flaps 3 out 2 fwd", 16, 16, 0, 3, 2);
    /** (b) 16 × 16 with the flaps as wide as R105 allows: 24 × 18 in. */
    static final BodyShape FLAPS_16_WIDE = new BodyShape("16 + flaps 4 out 2 fwd", 16, 16, 0, 4, 2);
    /** (b) 15 × 15, out and forward as far as R105 allows: 24 × 18 in. */
    static final BodyShape FLAPS_15 = new BodyShape("15 + flaps 4.5 out 3 fwd", 15, 15, 0, 4.5, 3);
    /** (c) A full-width 18 in frame cut to 15 in long so 45° flaps fit: 24 × 18 in. */
    static final BodyShape SHORT_18 = new BodyShape("18 wide x 15 long + flaps 3 out 3 fwd", 15, 18, 0, 3, 3);
    /** (c) The other way round: a 16 in body with long flaps, 18 in across and 24 long (a flared long U). */
    static final BodyShape FLARED_16 = new BodyShape("16 + flaps 1 out 8 fwd", 16, 16, 0, 1, 8);

    static final BodyShape[] BODIES = {PLAIN, LONG_U, PLAIN_16, FLAPS_16, FLAPS_16_WIDE, FLAPS_15, SHORT_18, FLARED_16};

    /**
     * Low guides (mentor, 5 Oct 2026, from a photo of a "floating intake": short wedges at the ends of
     * a narrow roller steer pieces in). {@link #LOW_GUIDE_IN} is just over NECTAR's radius, so a
     * rolling piece still meets it; the question is whether falling pieces touch it less.
     */
    static final double LOW_GUIDE_IN = 2.5;
    /** The 18 in robot with low ramps straight forward from its corners: a low long U with no doors. */
    static final BodyShape RAMPS_18 = new BodyShape("18 + ramps 0 out 6 fwd", 18, 18, 0, 0, 6).low(LOW_GUIDE_IN);
    static final BodyShape[] LOW_BODIES = {FLAPS_16.low(LOW_GUIDE_IN), FLAPS_16_WIDE.low(LOW_GUIDE_IN),
            FLAPS_15.low(LOW_GUIDE_IN), SHORT_18.low(LOW_GUIDE_IN), FLARED_16.low(LOW_GUIDE_IN), RAMPS_18};

    /**
     * The shapes to look at, in AdvantageScope ({@code BIOBUZZ Robot (shapes)}, one component each, in
     * this order) and in the pictures, each with a file name and its closest spot with no G409 touch
     * in either load ({@link BodyShapeSpillTest#howCloseCanEachShapePark}, 5 Oct 2026): its front-most
     * point that far from the wall.
     */
    static final BodyShape[] SHOWN = {PLAIN, LONG_U, PLAIN_16, FLAPS_16, FLAPS_16_WIDE, FLAPS_15, SHORT_18, FLARED_16, RAMPS_18};
    static final String[] SHOWN_FILE = {"plain-18", "long-u", "plain-16", "a-16-flaps-3x2", "b-16-flaps-4x2",
            "b-15-flaps-4.5x3", "c-18x15-flaps-3x3", "flared-16-flaps-1x8", "low-ramps-18"};
    static final double[] SHOWN_CLEAN_NOSE_IN = {35, 36, 35, 36, 36, 37, 37, 35, 35};
}
