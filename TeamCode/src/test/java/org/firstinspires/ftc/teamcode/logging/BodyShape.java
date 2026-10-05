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
    /** Which arms it has, and a crossbeam across their free ends ({@link RobotDesign#flapCrossbeam}). */
    boolean leftArm = true, rightArm = true, crossbeam = false;

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

    /** The same shape with a crossbeam, and only the arms named. */
    BodyShape fenced(String newName, boolean left, boolean right) {
        BodyShape b = new BodyShape(newName, length, width, slide, out, ahead);
        b.flapHeight = flapHeight;
        b.leftArm = left;
        b.rightArm = right;
        b.crossbeam = true;
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
        d.flapLeft = leftArm;
        d.flapRight = rightArm;
        d.flapCrossbeam = crossbeam;
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

    /** Ideas sheet 8: an 18 wide x 12 long chassis, 12 in arms pivoted down in front, joined by a crossbeam. */
    static final BodyShape FRONT_C = new BodyShape("front C 18x12, arms 12", 12, 18, 0, 0, 12).fenced("front C 18x12, arms 12", true, true);
    /** The same box on a 14 in chassis with 10 in arms, both arms (to see what dropping one changes). */
    static final BodyShape C_14 = new BodyShape("C 18x14, arms 10", 14, 18, 0, 0, 10).fenced("C 18x14, arms 10", true, true);
    /** Ideas sheet 13: the 14 in chassis with only its right arm and the crossbeam; the left side open. */
    static final BodyShape RIGHT_HOOK = new BodyShape("right hook 18x14, arm 10", 14, 18, 0, 0, 10).fenced("right hook 18x14, arm 10", false, true);
    /**
     * The small right hook (mentor, 5 Oct 2026): as small as fits around the 90% box of both spills, an 8 in arm
     * from the near 90% line to the far one, so the chassis can be 16 in long.
     */
    static final BodyShape RIGHT_HOOK_SMALL = new BodyShape("small right hook 18x16, arm 8", 16, 18, 0, 0, 8)
            .fenced("small right hook 18x16, arm 8", false, true);

    /*
     * Rigid V guides inside 18 x 18 (mentor, 5 Oct 2026): a narrower chassis with fixed flaps from its front
     * corners to the 18 in box's edges, so nothing deploys and the robot is 18 x 18 all match.
     */
    static final BodyShape RIGID_A = new BodyShape("rigid V 14x16, 2 out 2 fwd", 16, 14, 0, 2, 2);
    static final BodyShape RIGID_B = new BodyShape("rigid V 12x15, 3 out 3 fwd", 15, 12, 0, 3, 3);
    static final BodyShape RIGID_C = new BodyShape("rigid V 14x14, 2 out 4 fwd", 14, 14, 0, 2, 4);

    /**
     * The robots in {@code BIOBUZZ Robot (match shapes)}, for {@link AutoSim}'s match logs: the plain
     * chassis, the rigid V, and each right hook folded, with its arm down on the right, and down on the
     * left (a hook's arm goes on whichever side faces the centre line). {@link #matchComponent} picks one.
     */
    static final BodyShape[] MATCH = {
            PLAIN, RIGID_A,
            new BodyShape("large right hook, folded", 14, 18, 0, 0, 0), RIGHT_HOOK,
            new BodyShape("right hook 18x14, arm 10", 14, 18, 0, 0, 10).fenced("large hook, arm on the left", true, false),
            new BodyShape("small right hook, folded", 16, 18, 0, 0, 0), RIGHT_HOOK_SMALL,
            new BodyShape("small right hook 18x16, arm 8", 16, 18, 0, 0, 8).fenced("small hook, arm on the left", true, false)};

    /**
     * Which {@link #MATCH} component draws the robot design named {@code design}: down, its hook's
     * arm on the left ({@code side} +1) or right (-1); a design with no shape here, the plain chassis.
     */
    static int matchComponent(String design, boolean down, int side) {
        int hook = design.contains("large right hook") ? 2 : design.contains("small right hook") ? 5 : -1;
        if (hook >= 0) return !down ? hook : side > 0 ? hook + 2 : hook + 1;
        return design.contains("rigid V") ? 1 : 0;
    }

    /** The shapes {@link BodyShapeSpillTest#atTheLandingLine} parks at the spill's edge. */
    static final BodyShape[] LANDING = {PLAIN, LONG_U, FLAPS_16, FLAPS_16_WIDE, FLAPS_15, SHORT_18, FLARED_16, RAMPS_18};

    /**
     * The shapes to look at, in AdvantageScope ({@code BIOBUZZ Robot (shapes)}, one component each, in
     * this order) and in the pictures, each with a file name. They park as {@link #LANDING}: chassis
     * face on the spill's 100% line.
     */
    static final BodyShape[] SHOWN = LANDING;
    static final String[] SHOWN_FILE = {"plain-18", "long-u", "a-16-flaps-3x2", "b-16-flaps-4x2",
            "b-15-flaps-4.5x3", "c-18x15-flaps-3x3", "16-long-flaps-1x8", "18-low-ramps"};
}
