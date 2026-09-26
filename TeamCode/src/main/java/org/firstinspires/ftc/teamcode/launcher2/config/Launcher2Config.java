package org.firstinspires.ftc.teamcode.launcher2.config;

/**
 * The whole tunable surface of the two-wheel launcher rig — one object, so
 * Panels renders it as a single tree under {@code Launcher2}.
 *
 * <p>Copied from the three-wheel rig's {@code FlywheelTuningConfig} and cut
 * down to one motor. The rig's two motors share a port through a Y-cable, so
 * there is one power, one encoder and one set of gains; see
 * {@code robot_launcher_rig.xml}. Left out, and why:
 *
 * <ul>
 *   <li><b>Per-lane enable and {@code rpmTrim}</b> — there is only one lane.
 *       Trimming one wheel against the other is not possible in software on
 *       this wiring.</li>
 *   <li><b>{@code encoderReversed}</b> — the rig reads the absolute value of
 *       the encoder velocity instead, so the reading is positive whichever way
 *       the encoder counts. See {@code Launcher2}.</li>
 * </ul>
 *
 * <p>Defaults match the three-wheel rig. Both use goBILDA 6000 RPM motors
 * direct to the wheel, so its kS/kV/kP are a real starting point — but two
 * motors on one port carry more load than one, so expect kV to move.
 */
public class Launcher2Config {

    public Measurement measurement = new Measurement();
    public Target target = new Target();
    public Readiness readiness = new Readiness();
    public VoltageCompensation voltageCompensation = new VoltageCompensation();
    public Motor motor = new Motor();

    /** How encoder ticks become RPM. Get this wrong and every number below lies. */
    public static class Measurement {
        /** 28 is the bare-shaft count for a goBILDA 6000 RPM (1:1) motor. */
        public double ticksPerRev = 28;

        /** Wheel revolutions per motor revolution. 1.0: wheel on the motor shaft. */
        public double gearRatio = 1.0;
    }

    /** The speed command. Also steppable from the gamepad. */
    public static class Target {
        /** Starts slow: a rig on a stand should be walked upwards deliberately. */
        public double targetRpm = 1500;

        /** Hard ceiling. Nothing is ever commanded above this. */
        public double maxRpm = 6000;

        /** Dpad up/down step. Same as the three-wheel rig. */
        public double fineStepRpm = 25;

        /** Dpad right/left step. Same as the three-wheel rig. */
        public double coarseStepRpm = 100;
    }

    /** What counts as "at speed". Same hysteresis as the three-wheel rig. */
    public static class Readiness {
        /** How close to target the wheel must get to <em>become</em> READY, RPM. */
        public double rpmToleranceRpm = 60;

        /** How far a READY wheel may wander before it drops back, RPM. */
        public double dropOutToleranceRpm = 100;

        /** Time the error must stay inside tolerance before READY, ms. */
        public double atSpeedHoldMs = 250;
    }

    public static class VoltageCompensation {
        /** Scale power by {@code nominal / measured}. Leave on while tuning kS/kV. */
        public boolean enabled = true;

        public double nominalVoltage = 12.5;

        /** Below this, compensation is clamped for safety. */
        public double minVoltage = 9.0;
    }

    /** The one motor output — which drives both wheels. */
    public static class Motor {
        /**
         * Flips the direction of <em>both</em> wheels together; the Y-cable
         * means they cannot be flipped separately. Tick it if the game piece
         * gets pulled backwards. The RPM readout cannot tell you — it is an
         * absolute value — so watch the wheels.
         */
        public boolean reversed = false;

        /** Power to break away from rest (0.0–1.0). */
        public double kS = 0.10;

        /** Power per RPM of target — the main knob. */
        public double kV = 0.00017;

        /** Proportional correction per RPM of error. 0 = pure feedforward. */
        public double kP = 0.001;
    }
}
