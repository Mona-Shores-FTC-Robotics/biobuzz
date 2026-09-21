package org.firstinspires.ftc.teamcode.shooter.config;

/**
 * The tunable settings for one flywheel lane. One instance per lane lives inside
 * {@link FlywheelTuningConfig}, so Panels shows three identical sub-trees under
 * {@code left} / {@code center} / {@code right}.
 *
 * <p>Ported from DECODE's {@code LauncherFlywheelConfig}, which declared three
 * separate near-identical classes for the same job. One shared class keeps the
 * Panels tree the same shape while leaving a single place to edit a field.
 *
 * <p><b>The motor name is not here.</b> It lives in
 * {@link org.firstinspires.ftc.teamcode.hardware.DeviceNames}, checked against
 * the bundled robot configurations by {@code RobotConfigXmlTest}. An earlier
 * version of this class carried an editable {@code motorName}, ported forward
 * from DECODE — that is the exact pattern {@code DeviceNames} was written to
 * end, and a wrong name is now a failed build rather than something to notice
 * at a meeting.
 *
 * <p><b>No constructor arguments.</b> Panels reflects over the live object, so
 * every config object it walks has to stay a plain mutable bean.
 */
public class FlywheelLaneConfig {

    /** Untick to leave this lane's motor alone entirely (it is never powered). */
    public boolean enabled = true;

    /**
     * Flip if the wheel spins backwards. Applied live: the rig cuts power, sets
     * the new direction, then resumes. Watch the RPM readout — a negative RPM
     * while commanding a positive target is exactly the symptom this fixes.
     */
    public boolean reversed = false;

    /**
     * Per-lane offset added to the shared target RPM, so one wheel can be run
     * slightly slower or faster than the other two without splitting the target.
     * DECODE needed this on robot 20245, where the right lane ran ~100 RPM below
     * the other two.
     */
    public double rpmTrim = 0.0;

    /**
     * Minimum motor power to overcome friction and start spinning (0.0–1.0).
     * Increase if the flywheel won't spin up from rest; decrease if it creeps
     * when it shouldn't.
     */
    public double kS = 0.10;

    /**
     * Extra power added per RPM of target speed (feedforward velocity gain).
     * This is the main tuning knob — higher values spin up faster but may
     * overshoot.
     */
    public double kV = 0.00017;

    /**
     * How aggressively to correct RPM errors (proportional feedback gain).
     * Set to 0 for pure feedforward. Increase slowly if RPM drifts under load.
     */
    public double kP = 0.001;
}
