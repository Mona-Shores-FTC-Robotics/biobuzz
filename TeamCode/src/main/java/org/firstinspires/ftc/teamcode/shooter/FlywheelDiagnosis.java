package org.firstinspires.ftc.teamcode.shooter;

import org.firstinspires.ftc.teamcode.shooter.config.FlywheelTuningConfig;

/**
 * What one flywheel lane is actually doing, and what to do about it.
 *
 * <p>This exists because of the one failure the rig could not previously name.
 * A lane with no working encoder reads <b>0 RPM while power climbs to 1.0</b>,
 * and the old readout rendered that as {@code [spin ] 0 1500 1.00} — visually
 * identical to a wheel that is merely slow. On a bench prototype that is the
 * single most likely thing to be wrong, and chasing it as a gains problem costs
 * a meeting. Issue #21's rule — <em>report it by name, never stop the robot</em>
 * — applies to a test rig at least as much as to a match robot.
 *
 * <p>The decision is a {@code static} function of plain numbers rather than a
 * method on {@link FlywheelBank}, so it is unit tested in CI without an Android
 * device, a {@code HardwareMap}, or a robot. {@code FlywheelBank} keeps the
 * hardware and hands the numbers here.
 *
 * <p><b>Rejected:</b> inferring a dead encoder from {@code getVelocity()}
 * returning exactly {@code 0.0}. A disconnected encoder port does return a
 * clean zero, but so does a wheel that has not broken away yet, and an encoder
 * with a flaky ground returns sporadic noise rather than zero. Keying on
 * "commanded real power for a real length of time and still not moving" catches
 * the flaky case too, and cannot fire during a normal spin-up.
 */
public enum FlywheelDiagnosis {

    /** Lane unticked in Panels. Its motor is never powered. */
    OFF("[ off ]", ""),

    /** No motor by this lane's name in the active Robot Configuration. */
    NOT_IN_CONFIG("[ n/c ]", "not in the active Robot Configuration"),

    /** Powered down — either the rig is stopped or this lane's target is zero. */
    STOPPED("[stop ]", ""),

    /**
     * Real power has been applied for long enough that the wheel should be
     * moving, and the encoder still reads nothing.
     */
    DEAD_ENCODER("[ENCDR]",
            "power is applied but the encoder reads zero. Check the encoder cable"
                    + " at both ends, then check the wheel actually turns by hand."
                    + " Closed-loop speed control cannot work until this reads RPM"
                    + " — use open loop (X) to spin it meanwhile."),

    /** Turning backwards under a forward command: this lane's direction is wrong. */
    BACKWARDS("[BACK!]", "spinning backwards — tick \"reversed\" for this lane in Panels"),

    /** Running at a commanded power with no speed target, because open loop is on. */
    OPEN_LOOP("[open ]", ""),

    /** Commanded, moving, not yet inside tolerance for long enough. */
    SPINNING_UP("[spin ]", ""),

    /** Inside tolerance for at least {@code readiness.atSpeedHoldMs}. */
    AT_SPEED("[READY]", "");

    /** Fixed-width cell so the lane table lines up as a table. */
    public final String tag;

    private final String advice;

    FlywheelDiagnosis(String tag, String advice) {
        this.tag = tag;
        this.advice = advice;
    }

    /** True when this state is something somebody has to go and fix. */
    public boolean needsAttention() {
        return !advice.isEmpty();
    }

    /** The fix, in one sentence, or empty when nothing is wrong. */
    public String advice() {
        return advice;
    }

    /**
     * Classifies one lane from its measurements.
     *
     * <p>Order matters. A backwards wheel and a dead encoder are both checked
     * before anything reports progress, because both look like "not at speed
     * yet" and only one of them resolves itself by waiting.
     *
     * @param enabled      lane is ticked on in Panels
     * @param connected    a motor by this lane's name exists in the config
     * @param openLoop     the rig is commanding raw power, not a speed
     * @param driven       this lane is being asked to spin right now
     * @param atSpeed      inside tolerance for the required hold time
     * @param measuredRpm  signed — negative means the wheel turns backwards
     * @param appliedPower what was last written to the motor, 0.0–1.0
     * @param msAtPower    how long power has continuously been at or above
     *                     {@code limits.deadEncoderPower}; 0 if it is not
     */
    public static FlywheelDiagnosis evaluate(
            boolean enabled,
            boolean connected,
            boolean openLoop,
            boolean driven,
            boolean atSpeed,
            double measuredRpm,
            double appliedPower,
            double msAtPower,
            FlywheelTuningConfig.Diagnostics limits) {

        if (!enabled) {
            return OFF;
        }
        if (!connected) {
            return NOT_IN_CONFIG;
        }
        if (!driven) {
            return STOPPED;
        }
        if (measuredRpm < -limits.backwardsRpm) {
            return BACKWARDS;
        }
        if (appliedPower >= limits.deadEncoderPower
                && Math.abs(measuredRpm) < limits.deadEncoderRpm
                && msAtPower >= limits.deadEncoderAfterMs) {
            return DEAD_ENCODER;
        }
        if (openLoop) {
            return OPEN_LOOP;
        }
        return atSpeed ? AT_SPEED : SPINNING_UP;
    }
}
