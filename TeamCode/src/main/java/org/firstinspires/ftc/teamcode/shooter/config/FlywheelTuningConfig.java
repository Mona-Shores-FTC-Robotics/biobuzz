package org.firstinspires.ftc.teamcode.shooter.config;

/**
 * The whole tunable surface of the flywheel speed test rig — one object, so
 * Panels renders it as a single tree and nothing needs a recompile at a meeting.
 *
 * <p>Ported forward from DECODE's {@code LauncherFlywheelConfig},
 * {@code LauncherVoltageCompensationConfig} and {@code LauncherTimingConfig},
 * merged because on a test rig they are one knob set, not three.
 *
 * <p>Defaults come from DECODE's {@code RobotProfile}. kS/kV/kP were identical
 * across both DECODE robots (0.10 / 0.00017 / 0.001), so they are a real
 * starting point rather than a guess; {@code targetRpm} starts at DECODE's idle
 * speed (1500) rather than a shooting speed, because a rig on a stand should
 * come up slow and be walked upwards deliberately.
 */
public class FlywheelTuningConfig {

    public Measurement measurement = new Measurement();
    public Target target = new Target();
    public Limits limits = new Limits();
    public OpenLoop openLoop = new OpenLoop();
    public Readiness readiness = new Readiness();
    public Diagnostics diagnostics = new Diagnostics();
    public VoltageCompensation voltageCompensation = new VoltageCompensation();

    public FlywheelLaneConfig left = new FlywheelLaneConfig();
    public FlywheelLaneConfig center = new FlywheelLaneConfig();
    public FlywheelLaneConfig right = new FlywheelLaneConfig();

    /** How encoder ticks become RPM. Get this wrong and every number below lies. */
    public static class Measurement {
        /**
         * Encoder ticks per motor revolution. 28 is the bare-shaft count for a
         * REV/goBILDA brushed motor — correct for a flywheel driven straight off
         * the motor. If the readout is off by a constant factor, this is the
         * first thing to check.
         */
        public double ticksPerRev = 28;

        /**
         * Output wheel revolutions per motor revolution. 1.0 for a wheel mounted
         * directly on the shaft; set to the belt/gear ratio otherwise.
         */
        public double gearRatio = 1.0;

        /**
         * The motor's no-load free speed at the output, in RPM. Not used to
         * control anything — it is the number every limit in {@link Limits} is
         * reasoned against, and the readout reports speed as a percentage of
         * it, because "3,400 RPM" means nothing on its own and "57% of free
         * speed" means quite a lot.
         *
         * <p>6000 is the bench prototype's pair of 1:1 motors. A motor with a
         * gearbox is slower at the output by exactly {@code gearRatio}, so set
         * this to what the <em>wheel</em> can do, not the bare motor.
         */
        public double freeSpeedRpm = 6000;
    }

    /** The shared speed command. Also nudgeable from the gamepad. */
    public static class Target {
        /** Speed every enabled lane is driven to, before its own {@code rpmTrim}. */
        public double targetRpm = 1500;

        /**
         * Hard ceiling applied after trim. Nothing is ever commanded above this.
         *
         * <p><b>This must stay below {@code measurement.freeSpeedRpm}, and by a
         * real margin.</b> It defaulted to 6000 when the rig was written against
         * DECODE — which is exactly the free speed of the motors now on the
         * bench, so the ceiling permitted precisely the thing it existed to
         * prevent. Worse, a flywheel under load cannot reach free speed at all,
         * so a target set there is one the controller can never satisfy: the
         * error never closes, and the P term holds power at maximum for as long
         * as the rig is running.
         *
         * <p>75% of free speed. Deliberately conservative — raise it on purpose,
         * having watched the wheel, rather than discovering it was already high.
         */
        public double maxRpm = 4500;

        /** Dpad up/down step. */
        public double fineStepRpm = 25;

        /** Dpad right/left step. */
        public double coarseStepRpm = 100;
    }

    /**
     * What the rig will not do to a motor, whatever it has been asked for.
     *
     * <p>{@code target.maxRpm} caps what can be <em>requested</em>. These cap
     * what can actually <em>happen</em>, which is a different guarantee: a
     * loaded wheel chasing a target it cannot reach sits at full power, and full
     * power is free speed the moment the load comes off — a ball clearing the
     * wheels, a belt jumping, someone lifting the prototype off the bench.
     *
     * <p>The three numbers are chosen as a chain, and only make sense together:
     *
     * <table>
     *   <tr><td>{@code target.maxRpm}</td><td>4500</td><td>75% of free speed — the most that can be asked for</td></tr>
     *   <tr><td>{@code maxPower}</td><td>0.85</td><td>≈5100 RPM unloaded — the most the motor can physically do</td></tr>
     *   <tr><td>{@code overspeedRpm}</td><td>5400</td><td>90% of free speed — the cutout, above what maxPower can reach</td></tr>
     * </table>
     *
     * <p>Each sits above the one before, so the cutout cannot nuisance-trip on a
     * rig that is behaving: reaching it means something is wrong that the power
     * cap alone did not stop. Raise one and the others need looking at.
     */
    public static class Limits {
        /**
         * Ceiling on applied motor power, 0.0-1.0, in both control modes.
         *
         * <p>This is the limit that actually binds. An RPM target is a request;
         * power is what the motor receives, and 1.0 is free speed unloaded
         * however modest the target looked.
         */
        public double maxPower = 0.85;

        /**
         * Measured speed at which the rig cuts that lane's power and latches it
         * off until you press stop.
         *
         * <p>Latched rather than momentary on purpose. Cutting power drops the
         * speed, which clears the condition, which restores power — a wheel
         * oscillating against its own safety limit at the top of its range is
         * worse than either state. Stop is the only way out, so a trip is
         * something a person sees and decides about.
         *
         * <p>Compared against the <em>magnitude</em> of the measured speed, so a
         * wheel running backwards is caught by it too.
         *
         * <p>Set to 0 to disable, which is worth understanding before doing: on
         * a wrong {@code ticksPerRev} this cutout is the thing that notices the
         * wheel is at twice the speed the screen claims.
         */
        public double overspeedRpm = 5400;
    }

    /**
     * Drive the wheels at a fixed power with no speed target at all.
     *
     * <p>Two jobs, and the rig needs both:
     *
     * <ul>
     *   <li><b>It measures kV directly instead of by bisection.</b> Hold a
     *       power, wait for the RPM to settle, and kV is
     *       {@code power / settled RPM} with kS folded in at the low end. The
     *       closed-loop instructions used to say "raise kV until the measured
     *       RPM settles near the target", which is the same measurement done
     *       backwards, by hand, one guess at a time.</li>
     *   <li><b>It still spins a wheel whose encoder does not work.</b> A
     *       prototype thrown together the week before a meeting often has motor
     *       power and nothing else wired. Closed loop cannot run without an
     *       encoder — but shots can still be thrown and a hood angle still
     *       judged, so the rig should not be the reason the afternoon stops.
     *       {@link org.firstinspires.ftc.teamcode.shooter.FlywheelDiagnosis#DEAD_ENCODER}
     *       names that case; this is what you do about it.</li>
     * </ul>
     */
    public static class OpenLoop {
        /**
         * When on, every enabled lane is driven at {@link #power} and the RPM
         * readout becomes a measurement rather than a target. kS/kV/kP,
         * {@code targetRpm} and readiness are all ignored while this is on.
         */
        public boolean enabled = false;

        /**
         * Motor power, 0.0-1.0, applied to every enabled lane. Starts low: a
         * bench prototype is usually clamped to a table by hope alone.
         */
        public double power = 0.20;

        /** Left/right bumper step. */
        public double stepPower = 0.05;
    }

    /** What counts as "at speed". */
    public static class Readiness {
        /** Acceptable RPM error when considering a lane ready to fire. */
        public double rpmToleranceRpm = 50;

        /**
         * The error must stay inside tolerance this long before the lane reports
         * READY, so the indicator doesn't flicker as the wheel crosses target on
         * the way up.
         *
         * <p>DECODE also had a {@code fallbackReadyMs} that declared a lane ready
         * on a timer when the encoder looked dead. That is a match-time
         * safeguard and is deliberately not ported: on a characterization rig a
         * lane that never reaches speed must keep saying so.
         */
        public double atSpeedHoldMs = 250;
    }

    /**
     * Thresholds for the faults the rig names out loud. See
     * {@link org.firstinspires.ftc.teamcode.shooter.FlywheelDiagnosis}.
     *
     * <p>These are deliberately tunable rather than constants: a direct-drive
     * flywheel and a belted one reach measurable speed at very different powers,
     * and a threshold that cries wolf gets ignored, which is worse than not
     * having it.
     */
    public static class Diagnostics {
        /**
         * Power at or above which the wheel is definitely being told to move.
         * Below this, a stationary wheel is not evidence of anything.
         */
        public double deadEncoderPower = 0.15;

        /** Speed below which the encoder is reporting "not turning". */
        public double deadEncoderRpm = 10;

        /**
         * How long both conditions must hold together before the rig calls it.
         * One second is many times longer than any flywheel takes to show its
         * first non-zero tick, so this cannot fire during a healthy spin-up.
         */
        public double deadEncoderAfterMs = 1000;

        /**
         * Negative RPM past which a lane is called backwards rather than noisy.
         * The first version of this test used 1 RPM, which a stationary encoder
         * can produce from quantization alone.
         */
        public double backwardsRpm = 10;
    }

    /** Ported unchanged from DECODE's {@code LauncherVoltageCompensationConfig}. */
    public static class VoltageCompensation {
        /**
         * Scale motor power by {@code nominal / measured} so the same target RPM
         * needs the same gains on a fresh pack and a sagging one. Leave this on
         * while tuning kS/kV, or the numbers you find will only be valid at the
         * battery voltage you found them at.
         */
        public boolean enabled = true;

        /** Nominal battery voltage for calibration (typically 12.5V for full charge). */
        public double nominalVoltage = 12.5;

        /** Minimum voltage threshold - below this, compensation is clamped for safety. */
        public double minVoltage = 9.0;
    }
}
