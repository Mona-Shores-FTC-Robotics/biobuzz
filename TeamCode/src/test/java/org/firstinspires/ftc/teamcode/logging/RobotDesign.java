package org.firstinspires.ftc.teamcode.logging;

import java.util.Locale;

/**
 * The robot the simulation pretends to be: where the intake is, what kind of launcher, how fast
 * each mechanism works. It is how a design question ("turret or fixed launcher?", "do we need to
 * launch NECTAR?") becomes something {@link AutoSim} can run, so ideas can be compared before
 * anything is built.
 *
 * <p>The sizes come from the Competition Manual: an 18 in cube at the start (R102) and an 18 × 24 in
 * footprint fully expanded (R105), so an intake can reach at most 6 in past an 18 in frame. Every
 * time is a placeholder until a mechanism exists to time; change one, run the comparison again,
 * and see whether the answer changes.
 */
final class RobotDesign {

    enum Launcher {
        /** Aims on its own: launches from any heading, no turning first. */
        TURRET,
        /** Fixed to the frame: the drivetrain turns the robot to face the CELL, then it launches. */
        FIXED,
        /** Fixed to the frame, and throws everything it holds at once, with more spread. */
        CATAPULT
    }

    /** R105: the fully expanded footprint is 18 × 24 in, so an 18 in frame can reach 6 in further. */
    static final double MAX_REACH_IN = 6.0;
    /** A FLOWER's retrieval opening is 3.55 in tall (Competition Manual §9.7). */
    static final double FLOWER_OPENING_HEIGHT_IN = 3.55;

    final String name;
    /** Frame length, front to back (R102: at most 18 in at the start). */
    double frameIn = 18;
    /**
     * Frame width, side to side (R102: at most 18 in). Only {@link FieldSim}'s collisions and
     * {@link #checked} use it; {@link AutoSim}'s outlines still draw a square {@link #frameIn}.
     */
    double frameWidthIn = 18;
    /** How far the intake reaches past the frame once the match starts. */
    double intakeReachIn = 0;
    double intakeWidthIn = 14;
    boolean intakeAtBack = false;
    /**
     * Time between two pieces through the intake, picking up off the tiles: 4 take about 1 s
     * (mentor review: 0.15 s refilled a robot standing still unrealistically fast). A placeholder
     * until an intake is timed.
     */
    double intakeIntervalS = 0.35;
    /** Time to drag one POLLEN out of a FLOWER's retrieval opening (only the bottom one fits). */
    double flowerPullS = 0.5;
    /**
     * Catching (mentor review: it was perfect). A loose piece that reaches the intake is kept with
     * this chance, and not at all if it is moving faster than intakeMaxSpeedInPerS relative to the
     * robot. Placeholders until an intake is tested: toss pieces in at a few speeds and count.
     */
    double intakeGrabChance = 0.85;
    double intakeMaxSpeedInPerS = 60;
    Launcher launcher = Launcher.TURRET;
    /** Launchers side by side: each shot interval fires this many. */
    int launchers = 1;
    double shotIntervalS = 0.45;
    double spinUpS = 1.0;
    /** Whether it can take in and launch NECTAR (3.6 in) as well as POLLEN (2.8 in). */
    boolean launchesNectar = true;
    /**
     * Whether the robot knows how many pieces it holds (a beam break or distance sensor per slot).
     * Without one, an Auto can't tell full or empty: IntakeFull and Empty never fire, so every wait
     * on them runs to its time limit.
     */
    boolean countsPieces = true;
    /**
     * NECTAR's launch speed as a fraction of what was aimed for: 1 for a launcher that knows which
     * piece it holds and compensates; below 1 for one tuned for POLLEN that throws the heavier
     * NECTAR short.
     */
    double nectarSpeedFactor = 1.0;
    /**
     * POLLEN's launch speed as a fraction of what was aimed for. With {@link #nectarSpeedFactor} it
     * describes one launcher at one setting for both pieces: set between the two, each piece
     * misses its ideal speed by half the difference.
     */
    double pollenSpeedFactor = 1.0;
    /** How much steeper than the flattest arc into the opening the launcher shoots, degrees. */
    double arcExtraPitchDeg = 6;
    /**
     * A launcher whose angle is built in (a fixed hood) shoots at this pitch, degrees, and sets only
     * its speed for the distance. NaN: the angle follows {@link #arcExtraPitchDeg}.
     */
    double fixedPitchDeg = Double.NaN;
    /** How fast the drivetrain turns, rad/s, when a path or an aim asks it to. */
    double maxTurnRadPerS = Math.toRadians(300);
    /**
     * Two launchers, one set up for POLLEN and one for NECTAR: each interval fires at most one of
     * each kind, each at its own ideal speed (no shared setting).
     */
    boolean dedicatedLaunchers = false;
    /** A catapult volley's extra spread, and how far apart its pieces sit across the arm, in. */
    double catapultSpread = 2.0;
    double catapultSideIn = 2.5;
    /**
     * A catapult that throws its pieces as one clump: packed 2 by 2 (none overlapping), all with the
     * arm's one error for the throw, plus {@link #catapultResidual} of a flywheel's scatter each.
     */
    boolean catapultClump = false;
    /** How the clump sits in the cup: 2 by 2, or a triangle of 3 with 1 on top. */
    enum Cup { SQUARE, TRIANGLE }
    Cup catapultCup = Cup.SQUARE;
    double catapultResidual = 0.3;
    /**
     * Whether the shooter software allows for the robot's own motion when it fires on the move
     * (aiming off by the robot's velocity). Without it a piece fired while driving carries the
     * robot's velocity.
     */
    boolean compensatesMotion = false;
    /**
     * A frame-fixed launcher whose slats flip to throw straight back as well as forward (mentor,
     * 3 Oct 2026, instead of a turret): the robot turns whichever end is nearer to facing the CELL.
     */
    boolean launchesBothWays = false;
    /** Time for the slats to flip between forward and back (a guess until one is built). */
    double flipS = 0.3;
    /**
     * Side walls (mentor, 5 Oct 2026; {@code RobotAssets}' side-wall sketch): a wall down each side,
     * flush with the frame, that slides this far forward when out, so it stops a spill scattering
     * from beside the robot. Each has a one-way flap at the bottom: POLLEN rolls in under it and
     * cannot roll back out, and the opening is too low for NECTAR. 0: none. With the frame it makes
     * the footprint 18 × (18 + slide), so R105 allows at most 6 in.
     */
    double sideWallsSlideIn = 0;
    /**
     * The other shape that fits R105's 18 × 24 in: the walls swing this far out sideways instead of
     * forward (a "wide U", 24 in across at most, so its arms cannot also reach forward). 0: the
     * long U, walls sliding forward by sideWallsSlideIn.
     */
    double sideWallsOutIn = 0;
    /** How long each wall is, front to back, ending at the frame's front (plus any forward slide). */
    double sideWallsLengthIn = 18;
    /** Seconds the walls take to slide all the way out or in. A guess until they are built. */
    double sideWallsTravelS = 0.3;
    /**
     * When the walls start out, in seconds after our CELL starts to TIP. The spill lands 1.13 to 1.43 s
     * after (SpillLandingTest, from the 3 Oct films), so from 1.5 s they never meet a piece still in
     * the air: walls opening as pieces fall would look like G409's example B, "a MECHANISM ... that
     * opens wide to accept SCORING ELEMENTS".
     */
    double sideWallsDeployS = 1.5;

    /**
     * Passive flaps (mentor, 5 Oct 2026): a thin plate hinged at each front corner of the frame,
     * folded against the side at the start (inside R102's 18 in) and flipped out once the match
     * starts, its free end this far out sideways and forward of the hinge: a funnel in front of the
     * intake. 0 and 0: none. Out the whole match; nothing drives them.
     */
    double flapOutIn = 0;
    double flapForwardIn = 0;
    /** How tall a flap is; a placeholder until one is drawn (it must stop a rolling 2.8 in POLLEN). */
    double flapHeightIn = 4;
    static final double FLAP_THICKNESS_IN = 0.25;
    /** Which front corners have a flap (mentor, 5 Oct 2026: a "right hook" has only the right one). */
    boolean flapLeft = true, flapRight = true;
    /**
     * A beam joining the flaps' free ends across the robot's whole width (a "C" in front of the robot,
     * its front face the fourth side), as tall as the flaps. With one flap it hangs off that one.
     */
    boolean flapCrossbeam = false;

    boolean hasFlaps() {
        return flapOutIn > 0 || flapForwardIn > 0;
    }

    /** Length of each flap, hinge to free end. */
    double flapLengthIn() {
        return Math.hypot(flapOutIn, flapForwardIn);
    }

    RobotDesign(String name) {
        this.name = name;
    }

    /** What the simulation has always assumed: front intake, a turret that aims itself. */
    static RobotDesign standard() {
        return new RobotDesign("turret");
    }

    static RobotDesign fixedLauncher() {
        RobotDesign d = new RobotDesign("fixed launcher");
        d.launcher = Launcher.FIXED;
        return d;
    }

    /**
     * The spring-hood launcher in {@code cad/spring-hood-launcher}: fixed to the frame, a 75° hood,
     * NECTAR at 99% of POLLEN's speed with the setting tuned between them. Four 82 mm steel flywheels
     * on one 6000 rpm motor take about 1.9 s to reach 2700 rpm from rest (J ≈ 7.3e-4 kg·m², stall
     * torque 0.144 N·m: t = J·ω_free/T_stall · ln(1/(1 − 2700/6000))), so 2 s.
     */
    static RobotDesign springHood() {
        RobotDesign d = new RobotDesign("spring hood");
        d.launcher = Launcher.FIXED;
        d.fixedPitchDeg = 75;
        d.spinUpS = 2.0;
        d.pollenSpeedFactor = 1.005;
        d.nectarSpeedFactor = 0.995;
        return d;
    }

    /**
     * The spring hood with an intake as wide as the frame, its corners shaped to steer pieces off
     * a wall into the middle: it takes a piece anywhere across its 18 in front.
     */
    static RobotDesign springHoodFullWidth() {
        RobotDesign d = springHood().copy("spring hood, full-width intake");
        d.intakeWidthIn = 18;
        return d;
    }

    static RobotDesign catapult() {
        RobotDesign d = new RobotDesign("catapult");
        d.launcher = Launcher.CATAPULT;
        d.spinUpS = 0.6; // re-cocking, not spinning up
        return d;
    }

    RobotDesign copy(String newName) {
        RobotDesign d = new RobotDesign(newName);
        d.frameIn = frameIn;
        d.frameWidthIn = frameWidthIn;
        d.intakeReachIn = intakeReachIn;
        d.intakeWidthIn = intakeWidthIn;
        d.intakeAtBack = intakeAtBack;
        d.intakeIntervalS = intakeIntervalS;
        d.intakeGrabChance = intakeGrabChance;
        d.intakeMaxSpeedInPerS = intakeMaxSpeedInPerS;
        d.flowerPullS = flowerPullS;
        d.launcher = launcher;
        d.launchers = launchers;
        d.shotIntervalS = shotIntervalS;
        d.spinUpS = spinUpS;
        d.launchesNectar = launchesNectar;
        d.countsPieces = countsPieces;
        d.nectarSpeedFactor = nectarSpeedFactor;
        d.pollenSpeedFactor = pollenSpeedFactor;
        d.arcExtraPitchDeg = arcExtraPitchDeg;
        d.fixedPitchDeg = fixedPitchDeg;
        d.maxTurnRadPerS = maxTurnRadPerS;
        d.dedicatedLaunchers = dedicatedLaunchers;
        d.catapultSpread = catapultSpread;
        d.catapultSideIn = catapultSideIn;
        d.catapultClump = catapultClump;
        d.catapultCup = catapultCup;
        d.catapultResidual = catapultResidual;
        d.compensatesMotion = compensatesMotion;
        d.launchesBothWays = launchesBothWays;
        d.flipS = flipS;
        d.sideWallsSlideIn = sideWallsSlideIn;
        d.sideWallsOutIn = sideWallsOutIn;
        d.sideWallsLengthIn = sideWallsLengthIn;
        d.sideWallsTravelS = sideWallsTravelS;
        d.sideWallsDeployS = sideWallsDeployS;
        d.flapOutIn = flapOutIn;
        d.flapForwardIn = flapForwardIn;
        d.flapHeightIn = flapHeightIn;
        d.flapLeft = flapLeft;
        d.flapRight = flapRight;
        d.flapCrossbeam = flapCrossbeam;
        return d;
    }

    /** Throws if the design breaks a construction rule the manual states. */
    RobotDesign checked() {
        if (frameIn > 18 || frameWidthIn > 18) {
            throw new IllegalArgumentException(name + ": frame over the 18 in start cube (R102)");
        }
        if (sideWallsSlideIn > 0 && sideWallsOutIn > 0) {
            throw new IllegalArgumentException(name + ": side walls both forward and out don't fit 18 x 24 in (R105)");
        }
        // A flap folds back against the side to start inside the 18 in cube.
        if (hasFlaps() && flapLengthIn() > frameIn) {
            throw new IllegalArgumentException(name + ": flaps longer than the frame can't fold inside 18 in (R102)");
        }
        // An intake can be wider than the frame only by folding out sideways, which uses R105's
        // 24 in across instead of reaching forward.
        if (intakeWidthIn > (intakeReachIn == 0 ? 24 : frameWidthIn)) {
            throw new IllegalArgumentException(name + ": intake wider than R105 allows");
        }
        // R105: everything out, the robot fits an 18 x 24 in box, either way round.
        double[] f = footprintIn();
        if (!(f[0] <= 24 && f[1] <= 18) && !(f[0] <= 18 && f[1] <= 24)) {
            throw new IllegalArgumentException(String.format(Locale.ROOT,
                    "%s: %.1f in long x %.1f in across doesn't fit 18 x 24 in (R105)", name, f[0], f[1]));
        }
        return this;
    }

    /** {front to back, side to side} with everything out: what R105 limits. */
    double[] footprintIn() {
        double ahead = Math.max(Math.max(intakeReachIn, sideWallsSlideIn), flapForwardIn);
        double across = frameWidthIn + 2 * Math.max(sideWallsOutIn, flapOutIn);
        if (intakeReachIn == 0) across = Math.max(across, intakeWidthIn);
        return new double[] {frameIn + ahead, across};
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s (%s x%d, %.2f s/shot, intake %s %.0f in wide +%.0f in, %s)",
                name, launcher.name().toLowerCase(Locale.ROOT), launchers, shotIntervalS,
                intakeAtBack ? "back" : "front", intakeWidthIn, intakeReachIn,
                launchesNectar ? "POLLEN+NECTAR" : "POLLEN only");
    }
}
