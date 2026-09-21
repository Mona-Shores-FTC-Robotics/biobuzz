package org.firstinspires.ftc.teamcode.shooter;

import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.shooter.config.FlywheelTuningConfig;

import java.util.List;
import java.util.Locale;

/**
 * Flywheel speed test rig — spin the three launcher wheels up, watch the RPM,
 * and tune the gains without touching the code.
 *
 * <p>Built to be run at a meeting with no laptop-side coding at all. Everything
 * that has a number lives in the Panels tree under {@code FlywheelBank}, and the
 * gamepad covers the two things you want your hands on while a wheel is
 * spinning: start/stop and the target speed.
 *
 * <p>It does not require three wheels, or any particular number. Every lane is
 * independently switched off in Panels, so a two-wheel bench prototype is the
 * three-lane rig with one lane unticked, and the readout says {@code [ off ]}
 * rather than pretending something is broken.
 *
 * <h2>Gamepad 1</h2>
 * <table>
 *   <tr><td>A</td><td>spin up every enabled lane</td></tr>
 *   <tr><td>B</td><td>stop (wheels coast down)</td></tr>
 *   <tr><td>X</td><td>toggle open loop — fixed power, no speed target</td></tr>
 *   <tr><td>Dpad up / down</td><td>target RPM by the fine step (25 default)</td></tr>
 *   <tr><td>Dpad right / left</td><td>target RPM by the coarse step (100 default)</td></tr>
 *   <tr><td>Bumpers right / left</td><td>open-loop power by the power step (0.05 default)</td></tr>
 * </table>
 *
 * <p>The dpad always moves the closed-loop target and the bumpers always move
 * the open-loop power, whichever mode is live — so you can set up one mode
 * while running the other and switch with a single button.
 *
 * <p>The Driver Station STOP button always cuts power, and so does
 * {@link #stop()} when the OpMode ends for any other reason.
 *
 * <h2>In Panels</h2>
 * <p>{@code FlywheelBank → config}: target RPM and its limits, tolerance,
 * ticks-per-rev and gear ratio, voltage compensation, and per-lane enable,
 * reverse, RPM trim and kS/kV/kP. Every one of them takes effect on the next
 * loop. The motor names are not among them — they come from {@code DeviceNames}
 * and are checked against the bundled robot configurations at build time.
 *
 * <h2>Tuning order that works</h2>
 * <ol>
 *   <li><b>Open loop first (X).</b> Nudge the power up with the bumpers until
 *       the wheel is turning, and confirm the RPM readout moves at all. A lane
 *       stuck at zero under real power reports {@code [ENCDR]} — fix that before
 *       tuning anything, because no gain closes a loop with no measurement in
 *       it.</li>
 *   <li>Check the RPM readout is believable before trusting anything else. If it
 *       is off by a constant factor, fix {@code measurement.ticksPerRev} or
 *       {@code gearRatio} first.</li>
 *   <li>If a wheel reads negative RPM, tick {@code reversed} for that lane.</li>
 *   <li><b>Still in open loop, read kV off directly.</b> Hold a power, let the
 *       RPM settle, and kV is {@code power / settled RPM}. Two or three powers
 *       across the range is a better kV than any amount of guessing, and takes
 *       a minute.</li>
 *   <li>Switch back to closed loop (X), enter that kV with kP at 0, and check
 *       the measured RPM lands near the target.</li>
 *   <li>Raise kS until the wheel breaks away cleanly from rest.</li>
 *   <li>Only then add a little kP to close the remaining error.</li>
 * </ol>
 *
 * <p>The spin-up column is the number to write down: milliseconds from the
 * command to the first time the wheel touched tolerance.
 */
@TeleOp(name = "Flywheel Speed Test", group = "Shooter")
public class FlywheelSpeedTestOpMode extends OpMode {

    private FlywheelBank bank;
    private TelemetryManager panels;

    private boolean prevA = false;
    private boolean prevB = false;
    private boolean prevX = false;
    private boolean prevRightBumper = false;
    private boolean prevLeftBumper = false;
    private boolean prevDpadUp = false;
    private boolean prevDpadDown = false;
    private boolean prevDpadRight = false;
    private boolean prevDpadLeft = false;

    /** So the rumble fires on the transition into "all lanes ready", not every loop. */
    private boolean prevAllAtSpeed = false;

    @Override
    public void init() {
        // AUTO bulk caching: one hub read per loop for the three velocities,
        // without MANUAL's requirement to clear the cache by hand every loop.
        List<LynxModule> hubs = hardwareMap.getAll(LynxModule.class);
        for (LynxModule hub : hubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }

        bank = new FlywheelBank(hardwareMap);
        bank.initialize();

        try {
            panels = PanelsTelemetry.INSTANCE.getTelemetry();
        } catch (Exception ignored) {
            // Panels not available on this device — the Driver Station readout
            // below is the whole rig's UI in that case, and still works.
            panels = null;
        }

        telemetry.addLine("Flywheel Speed Test ready.");
        telemetry.addLine("A = spin up, B = stop, dpad = target RPM.");
        telemetry.addLine("X = open loop (fixed power), bumpers = that power.");
        telemetry.addLine("Tune everything else in Panels under FlywheelBank.");
        reportProblems();
        telemetry.update();
    }

    @Override
    public void init_loop() {
        // Keep measuring during init so a missing motor is reported before
        // anyone presses play.
        bank.periodic();
        telemetry.addLine("Flywheel Speed Test ready.");
        telemetry.addLine("A = spin up, B = stop, dpad = target RPM.");
        telemetry.addLine("X = open loop (fixed power), bumpers = that power.");
        reportProblems();
        telemetry.update();
    }

    @Override
    public void loop() {
        handleGamepad();
        bank.periodic();
        publishDriverStation();
        publishPanels();
    }

    @Override
    public void stop() {
        if (bank != null) {
            bank.stop();
            bank.periodic();
        }
    }

    private void handleGamepad() {
        FlywheelTuningConfig.Target target = FlywheelBank.config.target;

        if (gamepad1.a && !prevA) {
            bank.spinUp();
        }
        if (gamepad1.b && !prevB) {
            bank.stop();
        }
        if (gamepad1.x && !prevX) {
            // Cut power across the mode change. The two modes command wildly
            // different things from the same button set, and a wheel should
            // never be handed from one to the other while spinning.
            bank.stop();
            FlywheelBank.config.openLoop.enabled = !FlywheelBank.config.openLoop.enabled;
        }
        if (gamepad1.right_bumper && !prevRightBumper) {
            nudgeOpenLoopPower(FlywheelBank.config.openLoop.stepPower);
        }
        if (gamepad1.left_bumper && !prevLeftBumper) {
            nudgeOpenLoopPower(-FlywheelBank.config.openLoop.stepPower);
        }
        if (gamepad1.dpad_up && !prevDpadUp) {
            nudgeTarget(target.fineStepRpm);
        }
        if (gamepad1.dpad_down && !prevDpadDown) {
            nudgeTarget(-target.fineStepRpm);
        }
        if (gamepad1.dpad_right && !prevDpadRight) {
            nudgeTarget(target.coarseStepRpm);
        }
        if (gamepad1.dpad_left && !prevDpadLeft) {
            nudgeTarget(-target.coarseStepRpm);
        }

        prevA = gamepad1.a;
        prevB = gamepad1.b;
        prevX = gamepad1.x;
        prevRightBumper = gamepad1.right_bumper;
        prevLeftBumper = gamepad1.left_bumper;
        prevDpadUp = gamepad1.dpad_up;
        prevDpadDown = gamepad1.dpad_down;
        prevDpadRight = gamepad1.dpad_right;
        prevDpadLeft = gamepad1.dpad_left;
    }

    private void nudgeTarget(double deltaRpm) {
        FlywheelTuningConfig.Target target = FlywheelBank.config.target;
        double updated = target.targetRpm + deltaRpm;
        double ceiling = Math.max(0.0, target.maxRpm);
        if (updated < 0.0) {
            updated = 0.0;
        } else if (updated > ceiling) {
            updated = ceiling;
        }
        target.targetRpm = updated;
    }

    private void nudgeOpenLoopPower(double delta) {
        FlywheelTuningConfig.OpenLoop openLoop = FlywheelBank.config.openLoop;
        double updated = openLoop.power + delta;
        if (updated < 0.0) {
            updated = 0.0;
        } else if (updated > 1.0) {
            updated = 1.0;
        }
        openLoop.power = updated;
    }

    /**
     * Names every lane that needs a human, and says what to do about it.
     *
     * <p>Replaces the old missing-motor-only report. The motor being absent from
     * the configuration was never the only thing worth interrupting for — a
     * lane spinning backwards, or one drawing power with a dead encoder, both
     * waste more of a meeting than a name typo does, because neither announces
     * itself.
     */
    private void reportProblems() {
        for (FlywheelLane lane : FlywheelLane.values()) {
            FlywheelBank.Flywheel flywheel = bank.lane(lane);
            FlywheelDiagnosis diagnosis = flywheel.getDiagnosis();
            if (!diagnosis.needsAttention()) {
                continue;
            }
            telemetry.addData(diagnosis.name(), "%s lane (\"%s\"): %s",
                    lane.name(), flywheel.getMotorName(), diagnosis.advice());
        }
    }

    private void publishDriverStation() {
        boolean openLoop = FlywheelBank.config.openLoop.enabled;

        telemetry.addLine(bank.isSpinning() ? ">>> SPINNING  (B to stop)" : "--- STOPPED   (A to spin up)");
        telemetry.addLine(openLoop
                ? "MODE: OPEN LOOP — fixed power, no speed target   (X for closed loop)"
                : "MODE: CLOSED LOOP — holding a speed target        (X for open loop)");

        if (openLoop) {
            telemetry.addData("Open-loop power", "%.2f   bumpers L/R %.2f",
                    FlywheelBank.config.openLoop.power,
                    FlywheelBank.config.openLoop.stepPower);
            telemetry.addLine("  kV = power / settled RPM. Write down both.");
        } else {
            telemetry.addData("Target RPM", "%.0f   dpad U/D %.0f, R/L %.0f",
                    FlywheelBank.config.target.targetRpm,
                    FlywheelBank.config.target.fineStepRpm,
                    FlywheelBank.config.target.coarseStepRpm);
        }

        double voltage = bank.getBatteryVoltage();
        telemetry.addData("Battery", Double.isNaN(voltage)
                ? "no sensor"
                : String.format(Locale.US, "%.2f V  (power x%.2f)", voltage, bank.getVoltageMultiplier()));

        telemetry.addLine();
        telemetry.addLine("    state    rpm    err    pwr   spin-up");
        for (FlywheelLane lane : FlywheelLane.values()) {
            telemetry.addLine(laneLine(lane));
        }

        boolean allAtSpeed = bank.isEveryEnabledLaneAtSpeed();
        telemetry.addLine();
        if (!openLoop) {
            telemetry.addLine(allAtSpeed
                    ? "*** ALL ENABLED LANES AT SPEED ***"
                    : "    (not all enabled lanes at speed)");
        }

        if (allAtSpeed && !prevAllAtSpeed) {
            gamepad1.rumble(200);
        }
        prevAllAtSpeed = allAtSpeed;

        reportProblems();
    }

    /** One fixed-width row of the lane table, so the lanes read as a table. */
    private String laneLine(FlywheelLane lane) {
        FlywheelBank.Flywheel flywheel = bank.lane(lane);
        FlywheelDiagnosis diagnosis = flywheel.getDiagnosis();

        if (diagnosis == FlywheelDiagnosis.OFF) {
            return String.format(Locale.US, "%s   %s", lane.tag, diagnosis.tag);
        }
        if (diagnosis == FlywheelDiagnosis.NOT_IN_CONFIG) {
            return String.format(Locale.US, "%s   %s  \"%s\" not in robot config",
                    lane.tag, diagnosis.tag, flywheel.getMotorName());
        }

        // Error and spin-up are both measured against a speed target, so in open
        // loop they have nothing to say. Printing a number there would invite
        // somebody to tune against it.
        boolean hasTarget = !FlywheelBank.config.openLoop.enabled;
        double spinUpMs = flywheel.getLastSpinUpMs();

        return String.format(Locale.US, "%s   %s %6.0f %6s  %5.2f   %s",
                lane.tag,
                diagnosis.tag,
                flywheel.getMeasuredRpm(),
                hasTarget ? String.format(Locale.US, "%.0f", flywheel.getErrorRpm()) : "--",
                flywheel.getAppliedPower(),
                (hasTarget && !Double.isNaN(spinUpMs))
                        ? String.format(Locale.US, "%.0f ms", spinUpMs)
                        : "  --");
    }

    /**
     * Numeric series via addData so Panels can graph them — a measured-vs-target
     * trace is how you see overshoot and droop that the numbers alone hide.
     */
    private void publishPanels() {
        if (panels == null) {
            return;
        }
        try {
            for (FlywheelLane lane : FlywheelLane.values()) {
                FlywheelBank.Flywheel flywheel = bank.lane(lane);
                String prefix = lane.name().toLowerCase(Locale.US);
                panels.addData(prefix + "_rpm", flywheel.getMeasuredRpm());
                panels.addData(prefix + "_target", flywheel.getCommandedRpm());
                panels.addData(prefix + "_error", flywheel.getErrorRpm());
                panels.addData(prefix + "_power", flywheel.getAppliedPower());
                panels.addData(prefix + "_at_speed", flywheel.isAtSpeed() ? 1.0 : 0.0);
            }
            panels.addData("battery_volts", bank.getBatteryVoltage());
            panels.addData("open_loop", FlywheelBank.config.openLoop.enabled ? 1.0 : 0.0);
            panels.debug(bank.isSpinning() ? "SPINNING" : "STOPPED");
            panels.debug(FlywheelBank.config.openLoop.enabled ? "OPEN LOOP" : "CLOSED LOOP");
            for (FlywheelLane lane : FlywheelLane.values()) {
                panels.debug(laneLine(lane));
            }
            panels.update();
        } catch (Exception ignored) {
            // A Panels hiccup must never take the rig down mid-spin.
        }
    }
}
