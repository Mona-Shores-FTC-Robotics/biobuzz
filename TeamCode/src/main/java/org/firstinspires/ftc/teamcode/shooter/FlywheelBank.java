package org.firstinspires.ftc.teamcode.shooter;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

import org.firstinspires.ftc.teamcode.hardware.DeviceNames;
import org.firstinspires.ftc.teamcode.shooter.config.FlywheelLaneConfig;
import org.firstinspires.ftc.teamcode.shooter.config.FlywheelTuningConfig;

import java.util.EnumMap;

/**
 * The three flywheel motors and the speed control around them.
 *
 * <p>Control model, ported verbatim from DECODE's {@code LauncherSubsystem}:
 *
 * <pre>power = (kS + kV * targetRpm) + kP * (targetRpm - measuredRpm)</pre>
 *
 * feedforward for the bulk of the power, a small proportional term for the
 * error, the whole thing scaled by battery voltage and clipped to [0, 1]. The
 * rig never commands negative power, so a wrongly-configured lane coasts rather
 * than fighting itself.
 *
 * <p>Two deliberate departures from DECODE, both because this is a measurement
 * rig and not a match robot:
 *
 * <ul>
 *   <li><b>Measured RPM keeps its sign.</b> DECODE took
 *       {@code Math.abs(motor.getVelocity())}, which makes a backwards-spinning
 *       wheel look perfectly healthy. Here a backwards wheel reads negative,
 *       never reaches speed, and the OpMode says so in as many words.</li>
 *   <li><b>No ready-on-a-timer fallback.</b> See
 *       {@code FlywheelTuningConfig.Readiness#atSpeedHoldMs}.</li>
 * </ul>
 *
 * <p>Motors are intentionally not wrapped in CachingHardware — DECODE's note
 * applies unchanged: a flywheel sits at near-constant power for the whole run,
 * so the cache drops every repeat {@code setPower}, and if the hub ever zeroes
 * the motor behind the cache's back the stale cache suppresses every recovery
 * write.
 *
 * <p>This is a plain class, not an Ivy subsystem. The rig has one behaviour and
 * no command to schedule, so there is nothing for a scheduler to arbitrate.
 */
@Configurable
public class FlywheelBank {

    /**
     * The live tuning tree Panels edits. Static because that is how Panels'
     * {@code @Configurable} scan finds it.
     *
     * <p>Unlike DECODE this needs no {@code reloadProfileConfigs()} dance: these
     * defaults are plain constants, not a per-robot profile resolved from the
     * WiFi SSID, so nothing here depends on the robot having been identified by
     * the time the boot-time scan runs.
     */
    public static FlywheelTuningConfig config = new FlywheelTuningConfig();

    private final HardwareMap hardwareMap;
    private final EnumMap<FlywheelLane, Flywheel> flywheels = new EnumMap<>(FlywheelLane.class);

    private boolean spinning = false;
    private double lastVoltage = Double.NaN;
    private double lastVoltageMultiplier = 1.0;

    public FlywheelBank(HardwareMap hardwareMap) {
        this.hardwareMap = hardwareMap;
        for (FlywheelLane lane : FlywheelLane.values()) {
            flywheels.put(lane, new Flywheel(lane));
        }
    }

    /** Binds motors and leaves everything stopped. Safe to call more than once. */
    public void initialize() {
        spinning = false;
        for (Flywheel flywheel : flywheels.values()) {
            flywheel.bind();
            flywheel.stopHardware();
        }
    }

    /** Commands every enabled lane to its target speed. */
    public void spinUp() {
        spinning = true;
    }

    /** Cuts power to every lane. Wheels coast down (zero-power behaviour is FLOAT). */
    public void stop() {
        spinning = false;
    }

    public boolean isSpinning() {
        return spinning;
    }

    /** Run once per OpMode loop: re-reads config, measures, and drives the motors. */
    public void periodic() {
        lastVoltageMultiplier = computeVoltageMultiplier();
        for (Flywheel flywheel : flywheels.values()) {
            flywheel.bind();
            flywheel.update(lastVoltageMultiplier);
        }
    }

    public Flywheel lane(FlywheelLane lane) {
        return flywheels.get(lane);
    }

    /** Battery voltage last sampled, or NaN if no voltage sensor answered. */
    public double getBatteryVoltage() {
        return lastVoltage;
    }

    public double getVoltageMultiplier() {
        return lastVoltageMultiplier;
    }

    /** True when at least one lane is enabled and every enabled lane is at speed. */
    public boolean isEveryEnabledLaneAtSpeed() {
        boolean any = false;
        for (Flywheel flywheel : flywheels.values()) {
            if (!flywheel.isEnabled()) {
                continue;
            }
            any = true;
            if (!flywheel.isAtSpeed()) {
                return false;
            }
        }
        return any;
    }

    /**
     * Ported from DECODE's {@code getVoltageCompensationMultiplier()}: scale
     * power by {@code nominal / measured} so gains found on a full pack still
     * hold on a tired one.
     */
    private double computeVoltageMultiplier() {
        try {
            lastVoltage = hardwareMap.voltageSensor.iterator().next().getVoltage();
        } catch (Exception ignored) {
            // No voltage sensor in this configuration, or the hub did not answer.
            lastVoltage = Double.NaN;
            return 1.0;
        }
        FlywheelTuningConfig.VoltageCompensation compensation = config.voltageCompensation;
        if (!compensation.enabled) {
            return 1.0;
        }
        double voltage = Math.max(lastVoltage, compensation.minVoltage);
        if (voltage <= 0.0) {
            return 1.0;
        }
        return Range.clip(compensation.nominalVoltage / voltage, 0.5, 2.0);
    }

    /**
     * The hardware name for a lane. Comes from {@link DeviceNames}, which
     * {@code RobotConfigXmlTest} holds to the bundled robot configurations, so
     * a name that is not on the robot fails the build rather than the meeting.
     */
    private static String motorNameFor(FlywheelLane lane) {
        switch (lane) {
            case LEFT:
                return DeviceNames.LAUNCHER_LEFT;
            case CENTER:
                return DeviceNames.LAUNCHER_CENTER;
            case RIGHT:
            default:
                return DeviceNames.LAUNCHER_RIGHT;
        }
    }

    private FlywheelLaneConfig configFor(FlywheelLane lane) {
        switch (lane) {
            case LEFT:
                return config.left;
            case CENTER:
                return config.center;
            case RIGHT:
            default:
                return config.right;
        }
    }

    /**
     * Safely retrieves a motor from the hardware map. Returns null if the motor
     * is not found or the name is empty, so a rig with only one wheel wired up
     * still runs that wheel instead of crashing at init.
     */
    private static DcMotorEx tryGetMotor(HardwareMap hardwareMap, String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        try {
            return hardwareMap.get(DcMotorEx.class, name.trim());
        } catch (IllegalArgumentException ignored) {
            // Motor not in the active Robot Configuration — degrade gracefully.
            return null;
        }
    }

    /** One flywheel: its motor, its measurements, and its slice of the control law. */
    public class Flywheel {

        private final FlywheelLane lane;

        private DcMotorEx motor;
        /** True once {@link #bind()} has run, whether or not it found the motor. */
        private boolean bound;
        /** Direction currently applied, so a live edit is applied with power cut. */
        private Boolean appliedReversed;

        private double commandedRpm = 0.0;
        private double measuredRpm = 0.0;
        private double measuredTicksPerSec = 0.0;
        /** Straight from the SDK, before encoderReversed is applied. */
        private double rawTicksPerSec = 0.0;
        private double appliedPower = 0.0;
        private double feedforwardPower = 0.0;
        private double feedbackPower = 0.0;
        private boolean atSpeed = false;

        private long commandedAtNs = 0L;
        private long inToleranceSinceNs = 0L;
        private double lastSpinUpMs = Double.NaN;

        Flywheel(FlywheelLane lane) {
            this.lane = lane;
        }

        public FlywheelLane getLane() {
            return lane;
        }

        /** False when the lane is switched off in Panels. */
        public boolean isEnabled() {
            return cfg().enabled;
        }

        /**
         * The direction the running code last applied to the motor — read from
         * this OpMode's own copy of the config, not from what Panels displays.
         * If the Panels tick box and this disagree, the edit went somewhere the
         * running code cannot see.
         */
        public boolean isReversed() {
            return Boolean.TRUE.equals(appliedReversed);
        }

        /** True when this lane's reading is being negated by {@code encoderReversed}. */
        public boolean isEncoderReversed() {
            return cfg().encoderReversed;
        }

        /**
         * Encoder velocity exactly as the SDK reports it, before
         * {@code encoderReversed}. Its sign follows the motor direction; if it
         * is negative with the wheel spinning the right way and {@code reversed}
         * unticked, the encoder counts backwards and {@code encoderReversed} is
         * the fix.
         */
        public double getRawTicksPerSec() {
            return rawTicksPerSec;
        }

        /** False when no motor by the configured name exists in the Robot Configuration. */
        public boolean isConnected() {
            return motor != null;
        }

        public String getMotorName() {
            return motorNameFor(lane);
        }

        /** Signed — a negative value means the wheel is turning the wrong way. */
        public double getMeasuredRpm() {
            return measuredRpm;
        }

        public double getMeasuredTicksPerSec() {
            return measuredTicksPerSec;
        }

        /** Zero whenever the lane is stopped or disabled. */
        public double getCommandedRpm() {
            return commandedRpm;
        }

        public double getErrorRpm() {
            return commandedRpm - measuredRpm;
        }

        public double getAppliedPower() {
            return appliedPower;
        }

        /**
         * The feedforward half of the last command, {@code kS + kV * target},
         * before voltage compensation. Published separately from
         * {@link #getFeedbackPower()} because the split is the whole story when
         * tuning: feedforward should be carrying nearly all of it, and a
         * feedback term doing heavy lifting means kV is wrong.
         */
        public double getFeedforwardPower() {
            return feedforwardPower;
        }

        /** The feedback half, {@code kP * error}, before voltage compensation. */
        public double getFeedbackPower() {
            return feedbackPower;
        }

        /**
         * Motor current, or NaN if the hardware did not answer. Ported back from
         * DECODE: after RPM this is the most informative signal on a flywheel,
         * because it shows load — a binding wheel or an over-tight belt reads
         * here long before it shows up as a speed the rig cannot hold.
         */
        public double getCurrentAmps() {
            if (motor == null) {
                return Double.NaN;
            }
            try {
                return motor.getCurrent(CurrentUnit.AMPS);
            } catch (Exception ignored) {
                // Hardware can transiently fail (hub stalls, OpMode shutdown).
                // NaN lets telemetry consumers drop the sample.
                return Double.NaN;
            }
        }

        public boolean isAtSpeed() {
            return atSpeed;
        }

        /**
         * Milliseconds from the last change of command to first touching
         * tolerance, or NaN if it has not got there since. This is the number
         * worth writing down: it is what "characterize the spin-up" means.
         */
        public double getLastSpinUpMs() {
            return lastSpinUpMs;
        }

        /**
         * True when the wheel is turning backwards under a forward command —
         * i.e. the {@code reversed} tick box for this lane is wrong.
         */
        public boolean isRunningBackwards() {
            return commandedRpm > 0.0 && measuredRpm < -1.0;
        }

        /**
         * Looks the motor up once. The name is a constant now, so there is
         * nothing to re-bind — and a miss must not be retried every loop,
         * because a failed {@code hardwareMap.get()} costs a thrown exception
         * each time and the map cannot change mid-OpMode.
         */
        void bind() {
            if (bound) {
                return;
            }
            bound = true;
            motor = tryGetMotor(hardwareMap, motorNameFor(lane));
            appliedReversed = null;
            commandedRpm = 0.0;
            commandedAtNs = 0L;
            resetReadiness();
            if (motor != null) {
                motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
                motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                applyDirection();
            }
        }

        void stopHardware() {
            appliedPower = 0.0;
            commandedRpm = 0.0;
            commandedAtNs = 0L;
            resetReadiness();
            if (motor != null) {
                motor.setPower(0.0);
            }
        }

        void update(double voltageMultiplier) {
            if (motor == null) {
                rawTicksPerSec = 0.0;
                measuredTicksPerSec = 0.0;
                measuredRpm = 0.0;
                appliedPower = 0.0;
                feedforwardPower = 0.0;
                feedbackPower = 0.0;
                commandedRpm = 0.0;
                commandedAtNs = 0L;
                resetReadiness();
                return;
            }

            applyDirection();

            rawTicksPerSec = motor.getVelocity();
            double encoderSign = cfg().encoderReversed ? -1.0 : 1.0;
            measuredTicksPerSec = encoderSign * rawTicksPerSec;
            measuredRpm = ticksPerSecondToRpm(measuredTicksPerSec);

            double target = desiredRpm();
            setCommandedRpm(target);

            if (target <= 0.0) {
                motor.setPower(0.0);
                appliedPower = 0.0;
                feedforwardPower = 0.0;
                feedbackPower = 0.0;
                return;
            }

            FlywheelLaneConfig laneConfig = cfg();
            double feedforward = laneConfig.kS + laneConfig.kV * target;
            // A negative reading against a positive target is a direction or
            // encoder-sign fault, not a speed error. Feeding it to kP asks for
            // more power, which spins the wheel faster the wrong way and reads
            // more negative — a runaway to full power. Hold feedforward only
            // until the sign is fixed; isRunningBackwards() reports it.
            double feedback = measuredRpm < 0.0
                    ? 0.0
                    : laneConfig.kP * (target - measuredRpm);
            feedforwardPower = feedforward;
            feedbackPower = feedback;
            double power = Range.clip((feedforward + feedback) * voltageMultiplier, 0.0, 1.0);

            motor.setPower(power);
            appliedPower = power;

            updateReadiness(target);
        }

        /** Target this lane should be at right now: 0 unless spinning and enabled. */
        private double desiredRpm() {
            FlywheelLaneConfig laneConfig = cfg();
            if (!spinning || !laneConfig.enabled) {
                return 0.0;
            }
            double target = config.target.targetRpm + laneConfig.rpmTrim;
            return Range.clip(target, 0.0, Math.max(0.0, config.target.maxRpm));
        }

        /**
         * Records the new command and, when it actually changed, restarts the
         * spin-up clock so the reported time belongs to the current target.
         */
        private void setCommandedRpm(double target) {
            boolean changed = Math.abs(target - commandedRpm) > 1.0;
            commandedRpm = target;
            if (changed) {
                resetReadiness();
                restartSpinUpClock();
            }
        }

        private void updateReadiness(double target) {
            double error = Math.abs(target - measuredRpm);
            FlywheelTuningConfig.Readiness readiness = config.readiness;
            if (atSpeed) {
                // Already READY: only the wider drop-out band can take it away,
                // so jitter around target does not flicker the state.
                double dropOut = Math.max(readiness.dropOutToleranceRpm, readiness.rpmToleranceRpm);
                if (error <= dropOut) {
                    return;
                }
                inToleranceSinceNs = 0L;
                atSpeed = false;
                return;
            }
            boolean within = error <= readiness.rpmToleranceRpm;
            if (!within) {
                inToleranceSinceNs = 0L;
                atSpeed = false;
                return;
            }
            long now = System.nanoTime();
            if (inToleranceSinceNs == 0L) {
                inToleranceSinceNs = now;
                if (commandedAtNs != 0L && Double.isNaN(lastSpinUpMs)) {
                    lastSpinUpMs = (now - commandedAtNs) / 1e6;
                }
            }
            atSpeed = (now - inToleranceSinceNs) >= readiness.atSpeedHoldMs * 1e6;
        }

        private void resetReadiness() {
            inToleranceSinceNs = 0L;
            atSpeed = false;
            lastSpinUpMs = Double.NaN;
        }

        /**
         * Restarts the spin-up stopwatch. Called whenever the wheel is going to
         * have to get back to speed from wherever it is now — a new target, or a
         * direction flip that cut power mid-run.
         */
        private void restartSpinUpClock() {
            commandedAtNs = commandedRpm > 0.0 ? System.nanoTime() : 0L;
        }

        /**
         * Applies the {@code reversed} tick box if it has changed since last
         * loop, cutting power first — the wheel then coasts through the flip
         * rather than being commanded hard the other way.
         */
        private void applyDirection() {
            boolean reversed = cfg().reversed;
            if (appliedReversed != null && appliedReversed == reversed) {
                return;
            }
            motor.setPower(0.0);
            appliedPower = 0.0;
            motor.setDirection(reversed ? DcMotorSimple.Direction.REVERSE : DcMotorSimple.Direction.FORWARD);
            appliedReversed = reversed;
            resetReadiness();
            restartSpinUpClock();
        }

        private double ticksPerSecondToRpm(double ticksPerSecond) {
            double ticksPerRev = config.measurement.ticksPerRev;
            double gearRatio = config.measurement.gearRatio;
            double divisor = ticksPerRev * gearRatio;
            if (divisor == 0.0) {
                return 0.0;
            }
            return ticksPerSecond * 60.0 / divisor;
        }

        private FlywheelLaneConfig cfg() {
            return configFor(lane);
        }
    }
}
