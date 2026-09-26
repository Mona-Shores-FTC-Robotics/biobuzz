package org.firstinspires.ftc.teamcode.launcher2;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

import org.firstinspires.ftc.teamcode.hardware.DeviceNames;
import org.firstinspires.ftc.teamcode.launcher2.config.Launcher2Config;

/**
 * The two-wheel pinch launcher on the FTC-EoM3 bench rig, and the speed
 * control around it.
 *
 * <p>A standalone copy of the three-wheel rig's {@code FlywheelBank}, cut down
 * to one motor. It is a copy rather than a generalisation on purpose: the
 * three-wheel rig was tuned on the robot on 26 Sep 2026, and a spike should not
 * be able to break it. Control law unchanged:
 *
 * <pre>power = (kS + kV * targetRpm) + kP * (targetRpm - measuredRpm)</pre>
 *
 * scaled by battery voltage and clipped to [0, 1].
 *
 * <h2>One motor, two wheels</h2>
 * <p>Both motors are Y-cabled to Control Hub motor port 0, with one encoder on
 * encoder port 0. The SDK sees one {@code DcMotorEx}; every {@code setPower}
 * reaches both motors, and the RPM is that of whichever motor carries the
 * encoder. The current reading is the port's, so it is the two motors
 * combined.
 *
 * <h2>Absolute encoder velocity — a deliberate departure</h2>
 * <p>The three-wheel rig keeps the sign of the reading so a backwards wheel
 * shows up as negative RPM. This rig takes {@code Math.abs}, at the team's
 * request, so the reading is right whichever way the encoder counts and
 * whichever motor it is plugged into. The cost: a rig spinning the wrong way
 * reads a healthy positive RPM. Direction is checked by eye, by watching which
 * way the game piece goes, and fixed with {@code motor.reversed}.
 *
 * <p>It also removes the three-wheel rig's runaway guard, which held kP at
 * zero while the reading was negative. That guard existed because a negative
 * reading made kP ask for ever more power. An absolute reading rises with
 * power whichever way the wheel turns, so the loop cannot run away.
 */
@Configurable
public class Launcher2 {

    /** The live tuning tree Panels edits. Static because Panels' scan needs it. */
    public static Launcher2Config config = new Launcher2Config();

    private final HardwareMap hardwareMap;
    private DcMotorEx motor;
    /** Direction currently applied, so a live edit is applied with power cut. */
    private Boolean appliedReversed;

    private boolean spinning = false;
    private double lastVoltage = Double.NaN;
    private double voltageMultiplier = 1.0;

    private double commandedRpm = 0.0;
    private double measuredRpm = 0.0;
    private double rawTicksPerSec = 0.0;
    private double appliedPower = 0.0;
    private double feedforwardPower = 0.0;
    private double feedbackPower = 0.0;
    private boolean atSpeed = false;

    private long commandedAtNs = 0L;
    private long inToleranceSinceNs = 0L;
    private double lastSpinUpMs = Double.NaN;

    public Launcher2(HardwareMap hardwareMap) {
        this.hardwareMap = hardwareMap;
    }

    /**
     * Looks the motor up and leaves it stopped. A missing motor is not an
     * exception here — the OpMode reports it by name on every loop — because a
     * rig that crashes at init tells you less than one that says what is
     * missing.
     */
    public void initialize() {
        spinning = false;
        try {
            motor = hardwareMap.get(DcMotorEx.class, DeviceNames.LAUNCHER2);
        } catch (IllegalArgumentException notInConfig) {
            motor = null;
        }
        appliedReversed = null;
        if (motor != null) {
            motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
            motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            applyDirection();
            motor.setPower(0.0);
        }
        commandedRpm = 0.0;
        commandedAtNs = 0L;
        appliedPower = 0.0;
        resetReadiness();
    }

    public void spinUp() {
        spinning = true;
    }

    /** Cuts power. Wheels coast down (zero-power behaviour is FLOAT). */
    public void stop() {
        spinning = false;
    }

    public boolean isSpinning() {
        return spinning;
    }

    /** Run once per loop: re-reads config, measures, and drives the motor. */
    public void periodic() {
        voltageMultiplier = computeVoltageMultiplier();

        if (motor == null) {
            rawTicksPerSec = 0.0;
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
        measuredRpm = ticksPerSecondToRpm(Math.abs(rawTicksPerSec));

        double target = desiredRpm();
        setCommandedRpm(target);

        if (target <= 0.0) {
            motor.setPower(0.0);
            appliedPower = 0.0;
            feedforwardPower = 0.0;
            feedbackPower = 0.0;
            return;
        }

        Launcher2Config.Motor gains = config.motor;
        feedforwardPower = gains.kS + gains.kV * target;
        feedbackPower = gains.kP * (target - measuredRpm);
        appliedPower = Range.clip((feedforwardPower + feedbackPower) * voltageMultiplier, 0.0, 1.0);
        motor.setPower(appliedPower);

        updateReadiness(target);
    }

    // ------------------------------------------------------------ readouts

    public boolean isConnected() {
        return motor != null;
    }

    public String getMotorName() {
        return DeviceNames.LAUNCHER2;
    }

    /** Never negative — see the class comment on absolute velocity. */
    public double getMeasuredRpm() {
        return measuredRpm;
    }

    /** Encoder velocity exactly as the SDK reports it, sign included. */
    public double getRawTicksPerSec() {
        return rawTicksPerSec;
    }

    /** Zero whenever stopped. */
    public double getCommandedRpm() {
        return commandedRpm;
    }

    public double getErrorRpm() {
        return commandedRpm - measuredRpm;
    }

    public double getAppliedPower() {
        return appliedPower;
    }

    /** {@code kS + kV * target}, before voltage compensation. */
    public double getFeedforwardPower() {
        return feedforwardPower;
    }

    /** {@code kP * error}, before voltage compensation. */
    public double getFeedbackPower() {
        return feedbackPower;
    }

    public boolean isReversed() {
        return Boolean.TRUE.equals(appliedReversed);
    }

    public boolean isAtSpeed() {
        return atSpeed;
    }

    /** Milliseconds from the last change of command to first reaching tolerance, or NaN. */
    public double getLastSpinUpMs() {
        return lastSpinUpMs;
    }

    /** Current on the port — both motors together — or NaN if the hub did not answer. */
    public double getCurrentAmps() {
        if (motor == null) {
            return Double.NaN;
        }
        try {
            return motor.getCurrent(CurrentUnit.AMPS);
        } catch (Exception ignored) {
            return Double.NaN;
        }
    }

    public double getBatteryVoltage() {
        return lastVoltage;
    }

    public double getVoltageMultiplier() {
        return voltageMultiplier;
    }

    // ------------------------------------------------------------ internals

    private double desiredRpm() {
        if (!spinning) {
            return 0.0;
        }
        return Range.clip(config.target.targetRpm, 0.0, Math.max(0.0, config.target.maxRpm));
    }

    /** Restarts the spin-up clock when the command actually changes. */
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
        Launcher2Config.Readiness readiness = config.readiness;
        if (atSpeed) {
            double dropOut = Math.max(readiness.dropOutToleranceRpm, readiness.rpmToleranceRpm);
            if (error <= dropOut) {
                return;
            }
            inToleranceSinceNs = 0L;
            atSpeed = false;
            return;
        }
        if (error > readiness.rpmToleranceRpm) {
            inToleranceSinceNs = 0L;
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

    private void restartSpinUpClock() {
        commandedAtNs = commandedRpm > 0.0 ? System.nanoTime() : 0L;
    }

    /** Applies {@code motor.reversed} if it changed, cutting power first. */
    private void applyDirection() {
        boolean reversed = config.motor.reversed;
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
        double divisor = config.measurement.ticksPerRev * config.measurement.gearRatio;
        if (divisor == 0.0) {
            return 0.0;
        }
        return ticksPerSecond * 60.0 / divisor;
    }

    /** Same compensation as the three-wheel rig: {@code nominal / measured}, clamped. */
    private double computeVoltageMultiplier() {
        try {
            lastVoltage = hardwareMap.voltageSensor.iterator().next().getVoltage();
        } catch (Exception ignored) {
            lastVoltage = Double.NaN;
            return 1.0;
        }
        Launcher2Config.VoltageCompensation compensation = config.voltageCompensation;
        if (!compensation.enabled) {
            return 1.0;
        }
        double voltage = Math.max(lastVoltage, compensation.minVoltage);
        if (voltage <= 0.0) {
            return 1.0;
        }
        return Range.clip(compensation.nominalVoltage / voltage, 0.5, 2.0);
    }
}
