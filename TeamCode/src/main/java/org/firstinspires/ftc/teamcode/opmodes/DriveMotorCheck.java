package org.firstinspires.ftc.teamcode.opmodes;

import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.teamcode.hardware.DeviceNames;
import org.firstinspires.ftc.teamcode.pedro.Constants;
import org.firstinspires.ftc.teamcode.pedro.RobotConstants;
import org.firstinspires.ftc.teamcode.util.WheelComparison;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Measures each drive wheel's speed and current, so "one motor is weaker" can be checked with
 * numbers instead of by eye. <b>Lift the robot first — the wheels must be off the ground.</b>
 *
 * <p>Written for a robot that strafes in an arc: the Foresight Tuner's strafe step, and Basic
 * Drive robot-centric at full power, both curved, so the cause is the robot. This OpMode narrows
 * down <em>what</em> on the robot.
 *
 * <h2>What it runs</h2>
 *
 * <ol>
 *   <li><b>Init: the hand-turn check.</b> Shows each wheel's encoder count. Press A to zero them,
 *       then turn each wheel exactly one turn by hand. The four counts should match. goBILDA's
 *       encoder is on the motor shaft, before the gearbox, so a gearbox with a different ratio
 *       reads a normal RPM later — this is the only step that catches it.</li>
 *   <li><b>Each wheel alone</b> at full power: its own top speed and current.</li>
 *   <li><b>All four: forward, strafe left, strafe right</b>, through Pedro's {@link Mecanum} with
 *       the robot's own motor directions — the same call the Foresight Tuner makes.</li>
 * </ol>
 *
 * <p>After each step it names any wheel that is slow and says whether its current points at
 * drag or at a weak motor; {@link WheelComparison} holds that judgement and its reasoning. Every
 * wheel's RPM is also published to Panels as {@code <wheel>_rpm} for graphing.
 *
 * <p>RPM is motor-shaft RPM (28 ticks per rev), not wheel RPM. It is for comparing wheels with
 * each other; a bare goBILDA 5202/5203 motor runs roughly 6000 RPM at 12 V.
 *
 * <p><b>Rejected: running it on the floor.</b> Loaded numbers would be closer to the real
 * problem, but at full power the robot covers several feet per step and would need a clear field
 * and someone chasing it. Lifted, a dragging wheel still draws more current and a weak motor still
 * runs slower, which is what separates the causes. If the lifted run finds nothing, the cause is
 * one it cannot see: roller orientation, weight balance, or the floor.
 */
@Configurable
@TeleOp(name = "Drive Motor Check", group = "Diagnostics")
public class DriveMotorCheck extends LinearOpMode {

    /** Power for every step. Full power is where the arc showed up. */
    public static double power = 1.0;

    /** Seconds to let a wheel reach speed before measuring. */
    public static double settleSec = 1.0;

    /** Seconds to average over once at speed. */
    public static double sampleSec = 1.5;

    /** Seconds to let the wheels stop between steps, so one step does not bleed into the next. */
    public static double coastSec = 1.0;

    /** Encoder ticks per motor-shaft revolution: 28 for the goBILDA 5202/5203 series. */
    public static double ticksPerRev = 28.0;

    /** A wheel this many percent below the fastest counts as slow. */
    public static double slowPercent = 5.0;

    /** A slow wheel drawing this multiple of the others' median current is dragging. */
    public static double dragCurrentRatio = 1.25;

    /** Pedro's {@link Mecanum} wheel order: FL, FR, BL, BR — the indices of {@code wheelPowers}. */
    private static final String[] NAMES = {
            DeviceNames.FRONT_LEFT, DeviceNames.FRONT_RIGHT,
            DeviceNames.BACK_LEFT, DeviceNames.BACK_RIGHT};

    private final List<String> report = new ArrayList<>();
    private final DcMotorEx[] motors = new DcMotorEx[4];
    private final int[] zeroCounts = new int[4];
    private TelemetryManager panels;

    @Override
    public void runOpMode() {
        for (LynxModule hub : hardwareMap.getAll(LynxModule.class)) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }
        panels = tryGetPanels();

        // Throws, naming the robot, if the active config has no drivetrain. Building the Mecanum
        // applies this robot's motor directions to the same motor objects read below.
        RobotConstants robot = Constants.forActiveRobot();
        Mecanum mecanum = new Mecanum(hardwareMap, robot.drivetrainConfig);
        for (int i = 0; i < 4; i++) {
            motors[i] = hardwareMap.get(DcMotorEx.class, NAMES[i]);
        }
        zeroEncoders();

        while (opModeInInit()) {
            if (gamepad1.a) {
                zeroEncoders();
            }
            report.clear();
            line("LIFT THE ROBOT: all four wheels off the ground before PLAY.");
            line("Robot: " + robot.fileName() + "   Battery: " + batteryText());
            line("");
            line("Hand-turn check: press A to zero, then turn each wheel ONE full turn.");
            line("The sizes should match (the sign may differ). One that differs in size");
            line("has a different gearbox ratio from the others.");
            for (int i = 0; i < 4; i++) {
                line(String.format(Locale.US, "  %-10s %6d ticks", NAMES[i],
                        motors[i].getCurrentPosition() - zeroCounts[i]));
            }
            publish();
            sleep(50);
        }
        if (!opModeIsActive()) {
            return;
        }

        report.clear();
        line("Robot: " + robot.fileName() + "   Battery at start: " + batteryText());

        List<WheelComparison.Sample> alone = new ArrayList<>();
        for (int i = 0; i < 4 && opModeIsActive(); i++) {
            double[] commanded = new double[4];
            commanded[i] = power;
            motors[i].setPower(power);
            WheelComparison.Sample[] measured = measure("alone: " + NAMES[i], commanded);
            stopAll(mecanum);
            if (measured != null) {
                alone.add(measured[i]);
            }
        }
        if (alone.size() == 4) {
            summarize("Each wheel alone", alone);
        }

        runTogether(mecanum, "Forward", new DrivePowers(power, 0, 0));
        runTogether(mecanum, "Strafe left", new DrivePowers(0, power, 0));
        runTogether(mecanum, "Strafe right", new DrivePowers(0, -power, 0));

        line("");
        line(opModeIsActive() ? "Done. Results stay here until STOP." : "Stopped early.");
        while (opModeIsActive()) {
            publish();
            sleep(100);
        }
        stopAll(mecanum);
    }

    /** One all-four step through Pedro, exactly as a tuner drives it. */
    private void runTogether(Mecanum mecanum, String label, DrivePowers command) {
        if (!opModeIsActive()) {
            return;
        }
        mecanum.drive(command, false);
        WheelComparison.Sample[] measured = measure(label, mecanum.wheelPowers.clone());
        stopAll(mecanum);
        if (measured != null) {
            List<WheelComparison.Sample> samples = new ArrayList<>();
            for (WheelComparison.Sample sample : measured) {
                samples.add(sample);
            }
            summarize(label, samples);
        }
    }

    /**
     * Waits {@link #settleSec}, then averages every wheel's velocity and current over
     * {@link #sampleSec}, publishing live RPM throughout. Returns null if stopped partway.
     */
    private WheelComparison.Sample[] measure(String label, double[] commanded) {
        ElapsedTime timer = new ElapsedTime();
        double[] ticksSum = new double[4];
        double[] ampsSum = new double[4];
        int n = 0;
        while (opModeIsActive() && timer.seconds() < settleSec + sampleSec) {
            boolean sampling = timer.seconds() >= settleSec;
            for (int i = 0; i < 4; i++) {
                double tps = motors[i].getVelocity();
                if (panels != null) {
                    panels.addData(NAMES[i] + "_rpm", Math.abs(tps) / ticksPerRev * 60.0);
                }
                if (sampling) {
                    ticksSum[i] += tps;
                    ampsSum[i] += motors[i].getCurrent(CurrentUnit.AMPS);
                }
            }
            if (sampling) {
                n++;
            }
            List<String> live = new ArrayList<>(report);
            live.add("");
            live.add("Running: " + label + (sampling ? "  (measuring)" : "  (settling)"));
            publish(live);
        }
        if (!opModeIsActive() || n == 0) {
            return null;
        }
        WheelComparison.Sample[] samples = new WheelComparison.Sample[4];
        for (int i = 0; i < 4; i++) {
            samples[i] = new WheelComparison.Sample(NAMES[i], commanded[i], ticksSum[i] / n,
                    ampsSum[i] / n);
        }
        return samples;
    }

    private void summarize(String label, List<WheelComparison.Sample> samples) {
        List<WheelComparison.Result> results =
                WheelComparison.compare(samples, ticksPerRev, slowPercent, dragCurrentRatio);
        line("");
        line(label + ":");
        for (WheelComparison.Result r : results) {
            line(String.format(Locale.US, "  %-10s %5.0f RPM  %4s  %4.1f A  %s", r.name, r.rpm,
                    Double.isNaN(r.percentOfFastest) ? "--"
                            : String.format(Locale.US, "%.0f%%", r.percentOfFastest),
                    r.amps, r.finding == WheelComparison.Finding.OK ? "" : r.finding.name()));
        }
        for (String verdict : WheelComparison.verdict(results)) {
            line("  -> " + verdict);
        }
    }

    private void stopAll(Mecanum mecanum) {
        mecanum.stop();
        for (DcMotorEx motor : motors) {
            motor.setPower(0.0);
        }
        ElapsedTime timer = new ElapsedTime();
        while (opModeIsActive() && timer.seconds() < coastSec) {
            publish();
            sleep(20);
        }
    }

    private void zeroEncoders() {
        for (int i = 0; i < 4; i++) {
            zeroCounts[i] = motors[i].getCurrentPosition();
        }
    }

    private String batteryText() {
        double lowest = Double.POSITIVE_INFINITY;
        for (VoltageSensor sensor : hardwareMap.voltageSensor) {
            double v = sensor.getVoltage();
            if (v > 0) {
                lowest = Math.min(lowest, v);
            }
        }
        if (Double.isInfinite(lowest)) {
            return "unknown";
        }
        return String.format(Locale.US, "%.1f V%s", lowest, lowest < 12.5 ? " (LOW: charge it)" : "");
    }

    private void line(String text) {
        report.add(text);
    }

    private void publish() {
        publish(report);
    }

    /** Panels and the Driver Station together; the Driver Station alone if Panels fails. */
    private void publish(List<String> lines) {
        if (panels != null) {
            try {
                for (String entry : lines) {
                    panels.debug(entry);
                }
                panels.update(telemetry);
                return;
            } catch (RuntimeException ignored) {
                // Fall through to the Driver Station only.
            }
        }
        telemetry.clearAll();
        for (String entry : lines) {
            telemetry.addLine(entry);
        }
        telemetry.update();
    }

    private static TelemetryManager tryGetPanels() {
        try {
            return PanelsTelemetry.INSTANCE.getTelemetry();
        } catch (RuntimeException e) {
            return null;
        }
    }
}
