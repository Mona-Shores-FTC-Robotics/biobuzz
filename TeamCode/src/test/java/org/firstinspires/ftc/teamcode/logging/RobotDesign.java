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
    boolean intakeAtBack = false;
    /** Time between two pieces through the intake, picking up off the tiles. */
    double intakeIntervalS = 0.15;
    /** Time to drag one POLLEN out of a FLOWER's retrieval opening (only the bottom one fits). */
    double flowerPullS = 0.5;
    Launcher launcher = Launcher.TURRET;
    /** Launchers side by side: each shot interval fires this many. */
    int launchers = 1;
    double shotIntervalS = 0.45;
    double spinUpS = 1.0;
    /** Whether it can take in and launch NECTAR (3.6 in) as well as POLLEN (2.8 in). */
    boolean launchesNectar = true;
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
     * Whether the shooter software allows for the robot's own motion when it fires on the move
     * (aiming off by the robot's velocity). Without it a piece fired while driving carries the
     * robot's velocity.
     */
    boolean compensatesMotion = false;

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
        d.intakeReachIn = intakeReachIn;
        d.intakeWidthIn = intakeWidthIn;
        d.intakeAtBack = intakeAtBack;
        d.intakeIntervalS = intakeIntervalS;
        d.flowerPullS = flowerPullS;
        d.launcher = launcher;
        d.launchers = launchers;
        d.shotIntervalS = shotIntervalS;
        d.spinUpS = spinUpS;
        d.launchesNectar = launchesNectar;
        d.nectarSpeedFactor = nectarSpeedFactor;
        d.pollenSpeedFactor = pollenSpeedFactor;
        d.arcExtraPitchDeg = arcExtraPitchDeg;
        d.fixedPitchDeg = fixedPitchDeg;
        d.maxTurnRadPerS = maxTurnRadPerS;
        d.dedicatedLaunchers = dedicatedLaunchers;
        d.catapultSpread = catapultSpread;
        d.catapultSideIn = catapultSideIn;
        d.compensatesMotion = compensatesMotion;
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
