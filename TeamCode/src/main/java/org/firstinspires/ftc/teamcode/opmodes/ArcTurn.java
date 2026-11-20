package org.firstinspires.ftc.teamcode.opmodes;

import org.firstinspires.ftc.teamcode.subsystems.TurretArc;
import org.firstinspires.ftc.teamcode.subsystems.TurretEncoder;

/**
 * Circle with the turret AIMing, when the CELL is behind the turret's firing arc: the robot turns
 * just far enough for the aim to come inside the arc, the turret swings onto the CELL, and the shot
 * goes, with no light and no refusal (mentor, 20 Nov 2026: the orange light was gaudy and broke up
 * normal driving; the robot should sort it out in the background). {@link TeleOpConfig#circleTurnsIntoArc}.
 * Pure, so a test drives it.
 *
 * <p>The turn ends {@code marginDeg} inside the edge, not on it, so a robot that drifts a little
 * does not put the aim straight back outside. A press that starts with the aim already inside the
 * arc never turns ({@link #turning}): only one that starts, or goes, outside it.
 */
final class ArcTurn {

    private ArcTurn() {}

    /**
     * How far the robot must turn, degrees, + counter-clockwise (left), for {@code aimDeg} to sit
     * {@code marginDeg} inside the arc's nearer edge: 0 once it does, NaN with no aim or no arc.
     * Turning the robot left by d moves a direction on the robot d to the right, the sign the chassis
     * aim uses ({@link ChassisAimTurn}).
     *
     * @param aimDeg    the aim, in the turret's encoder frame (as {@code TurretAimer.aimDeg()})
     * @param centreDeg the arc's centre (the aim trim; 0 for a turret locked forward)
     * @param left      the arc's left limit about the centre, degrees
     * @param right     the arc's right limit about the centre, a positive number of degrees
     * @param marginDeg how far inside the edge to stop; kept below half the narrower side
     */
    static double errorDeg(double aimDeg, double centreDeg, double left, double right, double marginDeg) {
        if (Double.isNaN(aimDeg) || Double.isNaN(left) || Double.isNaN(right) || left + right >= 360.0) {
            return Double.NaN;
        }
        double margin = Double.isNaN(marginDeg) || marginDeg < 0.0 ? 0.0 : marginDeg;
        margin = Math.min(margin, 0.5 * Math.min(left, right));
        double rel = TurretEncoder.wrap180(aimDeg - (Double.isNaN(centreDeg) ? 0.0 : centreDeg));
        if (rel > left - margin) return rel - (left - margin);
        if (rel < -(right - margin)) return rel + (right - margin);
        return 0.0;
    }

    /**
     * Whether the turn runs this loop. It starts once the aim is outside the arc and runs until
     * {@code errorDeg} is within {@code doneDeg} (the aim about {@code marginDeg} inside, where the
     * turn's power stops: {@link ChassisAimTurn}'s {@code stopDeg}); no aim stops it.
     *
     * @param wasTurning whether it ran last loop
     * @param errorDeg   this loop's {@link #errorDeg}
     * @param outsideArc whether the aim is outside the arc this loop ({@code !TurretArc.contains})
     * @param doneDeg    the turn is done inside this, degrees; NaN or negative is 0
     */
    static boolean turning(boolean wasTurning, double errorDeg, boolean outsideArc, double doneDeg) {
        if (Double.isNaN(errorDeg)) return false;
        double done = Double.isNaN(doneDeg) || doneDeg < 0.0 ? 0.0 : doneDeg;
        return wasTurning ? Math.abs(errorDeg) > done : outsideArc;
    }

    /** {@link #errorDeg} with the robot's arc ({@link TurretArc}). */
    static double errorDeg(double aimDeg, double centreDeg, double marginDeg) {
        return errorDeg(aimDeg, centreDeg, TurretArc.left(), TurretArc.right(), marginDeg);
    }
}
