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
    public Readiness readiness = new Readiness();
    public VoltageCompensation voltageCompensation = new VoltageCompensation();

    public FlywheelLaneConfig left = FlywheelLaneConfig.forMotor("launcher_left");
    public FlywheelLaneConfig center = FlywheelLaneConfig.forMotor("launcher_center");
    public FlywheelLaneConfig right = FlywheelLaneConfig.forMotor("launcher_right");

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
    }

    /** The shared speed command. Also nudgeable from the gamepad. */
    public static class Target {
        /** Speed every enabled lane is driven to, before its own {@code rpmTrim}. */
        public double targetRpm = 1500;

        /** Hard ceiling applied after trim. Nothing is ever commanded above this. */
        public double maxRpm = 6000;

        /** Dpad up/down step. */
        public double fineStepRpm = 25;

        /** Dpad right/left step. */
        public double coarseStepRpm = 100;
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
