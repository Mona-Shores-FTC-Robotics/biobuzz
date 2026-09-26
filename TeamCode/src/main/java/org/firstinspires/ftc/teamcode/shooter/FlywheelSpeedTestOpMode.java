package org.firstinspires.ftc.teamcode.shooter;

import com.bylazar.configurables.PanelsConfigurables;
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
 * <h2>Gamepad 1</h2>
 * <table>
 *   <tr><td>A</td><td>spin up every enabled lane</td></tr>
 *   <tr><td>B</td><td>stop (wheels coast down)</td></tr>
 *   <tr><td>Dpad up / down</td><td>target RPM by the fine step (25 default)</td></tr>
 *   <tr><td>Dpad right / left</td><td>target RPM by the coarse step (100 default)</td></tr>
 * </table>
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
 *   <li>Check the RPM readout is believable before trusting anything else. If it
 *       is off by a constant factor, fix {@code measurement.ticksPerRev} or
 *       {@code gearRatio} first.</li>
 *   <li>If a wheel reads negative RPM, tick {@code reversed} for that lane.</li>
 *   <li>With kP at 0, raise kV until the measured RPM settles near the target.
 *       kV is roughly {@code steady-state power / target RPM}, and the rig shows
 *       you both numbers.</li>
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

    /** Wall-clock length of the previous loop, published so a spin-up curve can
     *  be read against a known sample interval instead of an assumed one. */
    private double loopMs = 0.0;
    private long lastLoopStartNs = 0L;

    private boolean prevA = false;
    private boolean prevB = false;
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
        telemetry.addLine("Tune everything else in Panels under FlywheelBank.");
        reportMissingMotors();
        telemetry.update();
    }

    @Override
    public void init_loop() {
        // Keep measuring during init so a missing motor is reported before
        // anyone presses play.
        bank.periodic();
        telemetry.addLine("Flywheel Speed Test ready.");
        telemetry.addLine("A = spin up, B = stop, dpad = target RPM.");
        reportMissingMotors();
        telemetry.update();
    }

    @Override
    public void loop() {
        long now = System.nanoTime();
        if (lastLoopStartNs != 0L) {
            loopMs = (now - lastLoopStartNs) / 1e6;
        }
        lastLoopStartNs = now;

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
        refreshPanelsConfig();
    }

    /**
     * Pushes a value this OpMode wrote back to the Panels configurables page.
     *
     * <p>Panels sends configurable values to the browser only when a tab
     * connects, after an edit made <em>in</em> the browser, or when told to with
     * {@code refreshClass}. Without this call a d-pad nudge moves the flywheel
     * but the page keeps showing the old {@code targetRpm}, and typing a new
     * number over that stale one looks like the edit did nothing.
     */
    private static void refreshPanelsConfig() {
        try {
            PanelsConfigurables.refreshClass(FlywheelBank.class);
        } catch (RuntimeException ignored) {
            // A dashboard problem must not stop the rig.
        }
    }

    /** Names any lane that is enabled but whose motor is not in the robot config. */
    private void reportMissingMotors() {
        for (FlywheelLane lane : FlywheelLane.values()) {
            FlywheelBank.Flywheel flywheel = bank.lane(lane);
            if (flywheel.isEnabled() && !flywheel.isConnected()) {
                telemetry.addData("NOT IN ROBOT CONFIG", "%s lane: \"%s\"",
                        lane.name(), flywheel.getMotorName());
            }
        }
    }

    private void publishDriverStation() {
        telemetry.addLine(bank.isSpinning() ? ">>> SPINNING  (B to stop)" : "--- STOPPED   (A to spin up)");
        telemetry.addData("Target RPM", "%.0f   dpad U/D %.0f, R/L %.0f",
                FlywheelBank.config.target.targetRpm,
                FlywheelBank.config.target.fineStepRpm,
                FlywheelBank.config.target.coarseStepRpm);

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
        telemetry.addLine(allAtSpeed
                ? "*** ALL ENABLED LANES AT SPEED ***"
                : "    (not all enabled lanes at speed)");

        if (allAtSpeed && !prevAllAtSpeed) {
            gamepad1.rumble(200);
        }
        prevAllAtSpeed = allAtSpeed;

        for (FlywheelLane lane : FlywheelLane.values()) {
            FlywheelBank.Flywheel flywheel = bank.lane(lane);
            if (flywheel.isRunningBackwards()) {
                telemetry.addData("WRONG DIRECTION", "%s lane is spinning backwards — tick \"reversed\" for it in Panels",
                        lane.name());
            }
        }
        reportMissingMotors();
    }

    /** One fixed-width row of the lane table, so three lanes read as a table. */
    private String laneLine(FlywheelLane lane) {
        FlywheelBank.Flywheel flywheel = bank.lane(lane);
        if (!flywheel.isEnabled()) {
            return String.format(Locale.US, "%s   [ off ]", lane.tag);
        }
        if (!flywheel.isConnected()) {
            return String.format(Locale.US, "%s   [ n/c ]  \"%s\" not in robot config", lane.tag, flywheel.getMotorName());
        }
        String state;
        if (flywheel.getCommandedRpm() <= 0.0) {
            state = "[stop ]";
        } else if (flywheel.isAtSpeed()) {
            state = "[READY]";
        } else {
            state = "[spin ]";
        }
        double spinUpMs = flywheel.getLastSpinUpMs();
        return String.format(Locale.US, "%s   %s %6.0f %6.0f  %5.2f   %s",
                lane.tag,
                state,
                flywheel.getMeasuredRpm(),
                flywheel.getErrorRpm(),
                flywheel.getAppliedPower(),
                Double.isNaN(spinUpMs) ? "  --" : String.format(Locale.US, "%.0f ms", spinUpMs));
    }

    /**
     * Numeric series via addData so Panels can graph them — a measured-vs-target
     * trace is how you see overshoot and droop that the numbers alone hide.
     *
     * <p>This used to be half the rig's telemetry: the same values also went to
     * an FTC Dashboard packet stream, keyed {@code shooter/<lane>/…} so desktop
     * AdvantageScope would render them as a browsable tree. That dependency is
     * gone — see {@code TeamCode/README.md} § "Why FTC Dashboard is not in the
     * dependency set" — so the four series only Dashboard carried are published
     * here instead, and nothing measured has been lost.
     *
     * <p>Two of those four are why the rig exists. {@code _ff} and {@code _fb}
     * are the feedforward and feedback halves of the applied power, and reading
     * them against each other is the fastest way to tell a kV problem from a kP
     * problem: feedforward should carry nearly all of the power, and a feedback
     * term doing real work means kV is off.
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

                // Carried over from the Dashboard packet stream.
                panels.addData(prefix + "_tps", flywheel.getMeasuredTicksPerSec());
                panels.addData(prefix + "_ff", flywheel.getFeedforwardPower());
                panels.addData(prefix + "_fb", flywheel.getFeedbackPower());
                panels.addData(prefix + "_spin_up_ms", flywheel.getLastSpinUpMs());

                double amps = flywheel.getCurrentAmps();
                panels.addData(prefix + "_amps", amps);
                double volts = bank.getBatteryVoltage();
                if (Double.isFinite(amps) && Double.isFinite(volts)) {
                    panels.addData(prefix + "_watts", amps * volts);
                }
            }
            panels.addData("spinning", bank.isSpinning() ? 1.0 : 0.0);
            panels.addData("battery_volts", bank.getBatteryVoltage());
            // Voltage compensation scales applied power, so kV read off a graph
            // without this number is wrong by exactly this factor.
            panels.addData("voltage_multiplier", bank.getVoltageMultiplier());
            panels.addData("loop_ms", loopMs);
            panels.debug(bank.isSpinning() ? "SPINNING" : "STOPPED");
            for (FlywheelLane lane : FlywheelLane.values()) {
                panels.debug(laneLine(lane));
            }
            panels.update();
        } catch (Exception ignored) {
            // A Panels hiccup must never take the rig down mid-spin.
        }
    }
}
