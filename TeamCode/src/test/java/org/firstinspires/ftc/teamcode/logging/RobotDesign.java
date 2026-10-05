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
    /** Frame length, front to back; the frame is square at the start (R102: 18 in cube). */
    double frameIn = 18;
    /** How far the intake reaches past the frame once the match starts. */
    double intakeReachIn = 0;
    double intakeWidthIn = 14;
    /**
     * How tall the intake's opening is: a loose piece is taken only below this (its centre, or with
     * {@link #intakeOnContact} the whole piece). POLLEN is 2.8 in across, NECTAR 3.6 in.
     */
    double intakeHeightIn = 6;
    /**
     * Whether the intake takes a loose piece only once it touches the intake's face (a roller inside
     * the frame): its centre no further out than its radius plus {@link FieldSim#INTAKE_CONTACT_SLACK_IN}.
     * Otherwise a piece is taken with its centre up to 3 in out, before it touches. FLOWER pickups keep
     * the 3 in either way (the robot stops just short of the FLOWER's tube).
     */
    boolean intakeOnContact = false;
    /**
     * Where a launched piece leaves the robot: this far forward of the robot's centre (negative:
     * behind it) and this high. A piece doesn't collide with the robot that launched it until it
     * has left that robot's outline, so the exit may be inside the body.
     */
    double exitForwardIn = FieldSim.PLACEHOLDER_EXIT_FORWARD_IN;
    double exitHeightIn = FieldSim.PLACEHOLDER_EXIT_HEIGHT_IN;
    /** How tall the robot's body is: pieces above it pass over, pieces below bounce off. */
    double bodyHeightIn = FieldSim.PLACEHOLDER_ROBOT_HEIGHT_IN;
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
     * The robot the build team is building (4 Oct 2026): the spring hood with an intake across the
     * front (the name is from when it took a piece anywhere across the 18 in front).
     */
    static RobotDesign springHoodFullWidth() {
        RobotDesign d = springHood().copy("spring hood, full-width intake");
        // Mentor, 5 Oct 2026, conservative until the intake is built: the intake is the frame's front
        // edge, 90% of its width, centred, a 5 in tall opening, and takes a piece only when it touches.
        d.intakeWidthIn = 0.9 * d.frameIn;
        d.intakeHeightIn = 5;
        d.intakeOnContact = true;
        return d;
    }

    /**
     * The build team's prototype, read off their CAD (front, side and top views, 5 Oct 2026) using
     * the pieces in it as a scale (POLLEN 2.8 in, NECTAR 3.6 in), so each number is +-15% and will
     * move as they build. It tells the story of what changes from {@link #springHoodFullWidth}:
     * a smaller robot, a narrower intake, and the launcher at the back (still firing forward, over
     * the robot). Not modelled yet: the pinwheel at its right-front corner that takes POLLEN out of a
     * FLOWER (the robot still takes them with its intake, as the other designs do).
     */
    static RobotDesign buildersPrototype() {
        RobotDesign d = springHoodFullWidth().copy("builders' prototype (5 Oct CAD)");
        d.frameIn = 15; // about 15 x 15 in including the wheels (frame rails about 11 in apart)
        d.intakeWidthIn = 8; // a front roller between the front wheels, about 2 NECTAR wide
        d.exitForwardIn = -3; // two flywheels about 3 in behind the centre ...
        d.exitHeightIn = 8; // ... about 8 in up; the angle stays the spring hood's 75 deg (unmeasured)
        d.bodyHeightIn = 9; // frame and flywheel housings; the camera masts are thin and not modelled
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
        d.intakeReachIn = intakeReachIn;
        d.intakeWidthIn = intakeWidthIn;
        d.intakeHeightIn = intakeHeightIn;
        d.intakeOnContact = intakeOnContact;
        d.exitForwardIn = exitForwardIn;
        d.exitHeightIn = exitHeightIn;
        d.bodyHeightIn = bodyHeightIn;
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
        return d;
    }

    /** Throws if the design breaks a construction rule the manual states. */
    RobotDesign checked() {
        if (frameIn > 18) throw new IllegalArgumentException(name + ": frame over the 18 in start cube (R102)");
        if (frameIn + intakeReachIn > 24) throw new IllegalArgumentException(name + ": reach over 24 in (R105)");
        // An intake can be wider than the frame only by folding out sideways, which uses R105's
        // 24 in across instead of reaching forward.
        if (intakeWidthIn > (intakeReachIn == 0 ? 24 : frameIn)) {
            throw new IllegalArgumentException(name + ": intake wider than R105 allows");
        }
        return this;
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s (%s x%d, %.2f s/shot, intake %s %.0f in wide +%.0f in, %s)",
                name, launcher.name().toLowerCase(Locale.ROOT), launchers, shotIntervalS,
                intakeAtBack ? "back" : "front", intakeWidthIn, intakeReachIn,
                launchesNectar ? "POLLEN+NECTAR" : "POLLEN only");
    }
}
