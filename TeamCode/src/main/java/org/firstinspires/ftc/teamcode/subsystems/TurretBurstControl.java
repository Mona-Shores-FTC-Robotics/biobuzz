package org.firstinspires.ftc.teamcode.subsystems;

/**
 * Aims a turret whose servo has only three commands: full speed counter-clockwise, full speed
 * clockwise, and off. A SWYFT standard servo (Balance, Speed, Torque) in its continuous mode is
 * like that: "Continuous rotation supports full speed in either direction only. Partial speed
 * control is not available," and "To stop the servo in continuous rotation mode, use the
 * disable-PWM command" (the SWYFT Servo Programmer V2's guide). The turret's angle comes from its
 * own encoder, which counts every turn, so the turret can turn without limit. Pure, so it is unit
 * tested; {@code TurretBurstBench} drives it on the robot.
 *
 * <p>Each loop it looks at where the turret will stop, not where it is: the encoder angle plus the
 * rate times {@code leadMs}, the coast after the pulse is cut (measured on the bench). Then:
 *
 * <ul>
 *   <li><b>Far</b> (predicted error past {@code burstBelowDeg}): full speed toward the target.</li>
 *   <li><b>Close</b>: bursts toward the target, {@code burstOnMs} on and {@code burstOffMs} off,
 *       so each burst moves it a few degrees and it steps onto the target instead of past it.
 *       Full speed in short bursts stands in for the partial speed the servo does not have.</li>
 *   <li><b>Within {@code toleranceDeg}</b>: off. A knock that moves it out starts the bursts
 *       again.</li>
 * </ul>
 *
 * <p>The target is taken the short way round when {@code shortestPath} is set: a target behind
 * the robot is reached by the nearer direction, whatever its number.
 */
public final class TurretBurstControl {

    /** The servo command: counter-clockwise, off, clockwise. */
    public enum Command {
        CCW(1), OFF(0), CW(-1);

        /** +1 counter-clockwise, -1 clockwise, 0 off. */
        public final int sign;

        Command(int sign) {
            this.sign = sign;
        }

        static Command toward(double errorDeg) {
            return errorDeg > 0 ? CCW : CW;
        }
    }

    /** The tunables. Fields, written from Panels each loop; no allocation in the loop. */
    public static final class Settings {
        public double leadMs = 0.0;
        public double burstBelowDeg = 20.0;
        public double burstOnMs = 20.0;
        public double burstOffMs = 80.0;
        public double toleranceDeg = 2.0;
        public boolean shortestPath = true;
    }

    private long burstCycleStartNs;
    private boolean inBurstZone;
    private double lastErrorDeg = Double.NaN;
    private double effectiveTargetDeg = Double.NaN;

    /**
     * This loop's command.
     *
     * @param targetDeg the angle wanted, degrees counter-clockwise from home (any number of turns)
     * @param angleDeg  the encoder's angle, same frame; NaN gives OFF
     * @param rateDps   the encoder's rate, counter-clockwise positive; NaN counts as 0
     * @param s         the settings
     * @param nowNs     {@code System.nanoTime()}
     */
    public Command step(double targetDeg, double angleDeg, double rateDps, Settings s, long nowNs) {
        if (Double.isNaN(angleDeg) || Double.isNaN(targetDeg)) {
            lastErrorDeg = Double.NaN;
            effectiveTargetDeg = Double.NaN;
            return Command.OFF;
        }
        double rate = Double.isNaN(rateDps) ? 0.0 : rateDps;
        effectiveTargetDeg = s.shortestPath ? angleDeg + TurretEncoder.wrap180(targetDeg - angleDeg) : targetDeg;
        double predicted = angleDeg + rate * Math.max(0.0, s.leadMs) / 1000.0;
        double errorDeg = effectiveTargetDeg - predicted;
        lastErrorDeg = errorDeg;
        double magnitude = Math.abs(errorDeg);
        if (magnitude <= Math.max(0.0, s.toleranceDeg)) {
            inBurstZone = false;
            return Command.OFF;
        }
        if (magnitude > Math.max(s.toleranceDeg, s.burstBelowDeg)) {
            inBurstZone = false;
            return Command.toward(errorDeg);
        }
        if (!inBurstZone) {
            inBurstZone = true;
            burstCycleStartNs = nowNs;
        }
        return burstOn(s, nowNs) ? Command.toward(errorDeg) : Command.OFF;
    }

    /** Target minus where the turret will stop, as of the last step; NaN with no angle. */
    public double lastErrorDegrees() {
        return lastErrorDeg;
    }

    /** The target as aimed at last step: the short way round when that is on. */
    public double effectiveTargetDegrees() {
        return effectiveTargetDeg;
    }

    private boolean burstOn(Settings s, long nowNs) {
        double on = Math.max(0.0, s.burstOnMs);
        double off = Math.max(0.0, s.burstOffMs);
        if (off <= 0.0) {
            return true;
        }
        double cycle = on + off;
        double into = (nowNs - burstCycleStartNs) / 1e6;
        if (into < 0 || into >= cycle) {
            burstCycleStartNs = nowNs;
            into = 0;
        }
        return into < on;
    }
}
