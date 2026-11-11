package org.firstinspires.ftc.teamcode.opmodes.calibration;

import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.qualcomm.hardware.digitalchickenlabs.OctoQuad;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.Utility;

import org.firstinspires.ftc.teamcode.RobotDifferences;
import org.firstinspires.ftc.teamcode.hardware.ActiveConfig;
import org.firstinspires.ftc.teamcode.hardware.DeviceNames;
import org.firstinspires.ftc.teamcode.hardware.RobotIdentity;
import org.firstinspires.ftc.teamcode.subsystems.TurretAbsolute;
import org.firstinspires.ftc.teamcode.subsystems.TurretSubsystem;
import org.firstinspires.ftc.teamcode.util.LoopTimer;

import java.util.Locale;

/**
 * Measures the turret's two-encoder absolute zero: two Studica Cypher Maxes geared to the turret at
 * different ratios, their absolute (PWM) outputs on the OctoQuad's channels
 * {@link TurretSubsystem#ABS_CHANNEL_A} and {@link TurretSubsystem#ABS_CHANNEL_B}. Runs on a
 * competition robot (the turret fitted) or on {@code robot_octoquad_rig} (the encoders in hand).
 * Every value it finds goes into {@code TurretSubsystem}'s {@code abs*} fields, or that robot's
 * lines in {@code RobotDifferences}. Nothing moves: the turret is turned by hand.
 *
 * <h2>The checklist, in order</h2>
 *
 * <ol>
 *   <li><b>Does the OctoQuad answer?</b> The first line says its firmware; SDK 12 needs 3.x. A
 *       FAULT says why not (not plugged in, wrong bus).</li>
 *   <li><b>Is each encoder on its channel?</b> All four pulse channels (4 to 7) are shown with the
 *       live pulse width in µs; A and B are marked. A channel reading 0 has no pulse: the
 *       Cypher Max's absolute wire is not on that channel's signal pin, or its switch is not on
 *       3V3.</li>
 *   <li><b>Each encoder's pulse range</b> ({@code absPulseMinUs*}, {@code absPulseMaxUs*}): turn
 *       the turret slowly through at least one full turn of each encoder and read the running min
 *       and max. The Cypher Max's PWM frequency differs from encoder to encoder, so its widths do
 *       too. Square (X) clears them and re-runs the setup.</li>
 *   <li><b>Directions</b> ({@code absDirectionA}, {@code absDirectionB}): turn the turret
 *       counter-clockwise seen from above; +1 for each fraction that rises, -1 for each that
 *       falls. Through a gear mesh the two may differ.</li>
 *   <li><b>Ratio of B</b> ({@code absRatioB}, ring teeth over B's gear teeth): from the build team;
 *       check it by counting B's wraps over one turret turn. A's is
 *       {@code TurretSubsystem.turnsPerTurretTurn}, already measured on {@code Bench: Turret}.</li>
 *   <li><b>Homes</b> ({@code absHomeA}, {@code absHomeB}): turret on its home mark; read the two
 *       fractions. Set it on the mark a few times and note the spread.</li>
 *   <li><b>Check</b>: with every value in, the solve line gives the turret's direction, (-180°,
 *       180°] from home, with its residual and margin. Turn the turret slowly through several full
 *       turns and back home: the angle must follow, and the residual stay well under a third of
 *       the design line's separation (half of it is the error budget).</li>
 * </ol>
 *
 * <p>Standalone, like {@code ValidateHardware}: no {@code Robot}, no {@code Subsystem}. It applies
 * this robot's {@code RobotDifferences} first, as {@code RobotOpMode} would, so it shows the values
 * the turret will run with. Nothing is saved to the OctoQuad's flash: the bank and pulse settings
 * are sent at every init, as {@code TurretSubsystem} does.
 */
@Utility(name = "Bench: OctoQuad", description = "The turret's two Cypher Max absolute encoders through the OctoQuad: pulse ranges, homes, the solve")
public class OctoQuadBench extends OpMode {

    /** First pulse-width channel of the OctoQuad's second bank. Channels 4 to 7 read pulse widths. */
    public static final int FIRST_PULSE_CHANNEL = 4;
    public static final int PULSE_CHANNELS = 4;

    private OctoQuad octoquad;
    private final OctoQuad.EncoderDataBlock data = new OctoQuad.EncoderDataBlock();
    private final LoopTimer loopTimer = new LoopTimer();

    private String configWarning;
    private String fault;
    private String firmware = "?";
    private boolean dataValid;
    private final int[] pulseUs = new int[PULSE_CHANNELS];
    private final int[] minUs = new int[PULSE_CHANNELS];
    private final int[] maxUs = new int[PULSE_CHANNELS];
    private TurretAbsolute.Solution solution;
    private double fracA = Double.NaN;
    private double fracB = Double.NaN;
    private TelemetryManager panels;
    private String panelsProblem;
    private boolean xWasDown;

    @Override
    public void init() {
        configWarning = checkActiveConfig();
        try {
            RobotDifferences.applyForActiveRobot();
        } catch (RuntimeException e) {
            if (configWarning == null) configWarning = "robot differences not applied: " + e.getMessage();
        }
        try {
            octoquad = hardwareMap.get(OctoQuad.class, DeviceNames.OCTOQUAD);
        } catch (RuntimeException e) {
            octoquad = null;
            fault = "OctoQuad \"" + DeviceNames.OCTOQUAD + "\" is not in the active configuration";
        }
        clearRanges();
        if (octoquad != null) {
            setUp();
        }
        try {
            panels = PanelsTelemetry.INSTANCE.getTelemetry();
            if (panels == null) panelsProblem = "PanelsTelemetry returned no telemetry manager";
        } catch (RuntimeException | LinkageError e) {
            panels = null;
            panelsProblem = "not available at init: " + e;
        }
        report();
    }

    /** Chip id, firmware, bank config and the pulse range on all four channels. Nothing saved to flash. */
    private void setUp() {
        try {
            byte chip = octoquad.getChipId();
            if (chip != OctoQuad.OCTOQUAD_CHIP_ID) {
                fault = "OctoQuad not answering on I2C (chip id " + chip + "): plugged in? on the XML's bus?";
                return;
            }
            OctoQuad.FirmwareVersion version = octoquad.getFirmwareVersion();
            firmware = version.toString();
            if (version.maj < OctoQuad.SUPPORTED_FW_VERSION_MAJ) {
                fault = "OctoQuad firmware " + firmware + "; SDK 12 needs "
                        + OctoQuad.SUPPORTED_FW_VERSION_MAJ + ".x. Flash it with the OctoQuad config menu sample.";
                return;
            }
            octoquad.setChannelBankConfig(OctoQuad.ChannelBankConfig.BANK1_QUADRATURE_BANK2_PULSE_WIDTH);
            for (int channel = FIRST_PULSE_CHANNEL; channel < FIRST_PULSE_CHANNEL + PULSE_CHANNELS; channel++) {
                octoquad.setSingleChannelPulseWidthParams(channel, 1, PULSE_LIMIT_US);
                octoquad.setSingleChannelPulseWidthTracksWrap(channel, false);
            }
            fault = null;
        } catch (RuntimeException e) {
            fault = "OctoQuad setup failed: " + (e.getMessage() == null ? e.toString() : e.getMessage());
        }
    }

    /** Wider than any Cypher Max pulse (its frame is about 1.7 to 2.1 ms). */
    static final int PULSE_LIMIT_US = 4000;

    private void clearRanges() {
        for (int i = 0; i < PULSE_CHANNELS; i++) {
            minUs[i] = Integer.MAX_VALUE;
            maxUs[i] = Integer.MIN_VALUE;
        }
    }

    @Override
    public void init_loop() {
        step();
    }

    @Override
    public void loop() {
        step();
    }

    private void step() {
        loopTimer.lap();
        boolean x = gamepad1.x;
        if (x && !xWasDown && octoquad != null) {
            clearRanges();
            setUp();
        }
        xWasDown = x;

        dataValid = false;
        if (octoquad != null && fault == null) {
            try {
                octoquad.readAllEncoderData(data);
                dataValid = data.isDataValid();
                if (dataValid) {
                    for (int i = 0; i < PULSE_CHANNELS; i++) {
                        pulseUs[i] = data.positions[FIRST_PULSE_CHANNEL + i];
                        if (pulseUs[i] > 0) {
                            minUs[i] = Math.min(minUs[i], pulseUs[i]);
                            maxUs[i] = Math.max(maxUs[i], pulseUs[i]);
                        }
                    }
                }
            } catch (RuntimeException e) {
                fault = "read failed: " + (e.getMessage() == null ? e.toString() : e.getMessage());
            }
        }
        fracA = TurretAbsolute.fractionFromPulse(pulseOn(TurretSubsystem.ABS_CHANNEL_A),
                TurretSubsystem.absPulseMinUsA, TurretSubsystem.absPulseMaxUsA);
        fracB = TurretAbsolute.fractionFromPulse(pulseOn(TurretSubsystem.ABS_CHANNEL_B),
                TurretSubsystem.absPulseMinUsB, TurretSubsystem.absPulseMaxUsB);
        double separation = separation();
        double maxResidual = Double.isNaN(TurretSubsystem.absMaxResidualDeg)
                ? separation / 3.0 : TurretSubsystem.absMaxResidualDeg;
        solution = TurretAbsolute.solve(fracA, fracB, TurretSubsystem.absHomeA, TurretSubsystem.absHomeB,
                TurretSubsystem.turnsPerTurretTurn, TurretSubsystem.absRatioB,
                TurretSubsystem.absDirectionA, TurretSubsystem.absDirectionB,
                maxResidual, separation / 2.0);
        report();
        publish();
    }

    private static double separation() {
        double ratioB = TurretSubsystem.absRatioB;
        if (!(ratioB > 0)) return Double.NaN;
        return TurretAbsolute.separationDeg(TurretSubsystem.turnsPerTurretTurn, ratioB);
    }

    private void report() {
        if (configWarning != null) telemetry.addLine("CONFIG: " + configWarning);
        if (fault != null) {
            telemetry.addLine("FAULT: " + fault);
        } else {
            telemetry.addLine("OctoQuad fw " + firmware + (dataValid ? "" : " · data NOT valid this loop"));
        }
        telemetry.addLine("Square (X): clear min/max and re-run setup. Turn the turret by hand.");
        telemetry.addLine("");
        telemetry.addLine("ch   pulse us    min    max");
        for (int i = 0; i < PULSE_CHANNELS; i++) {
            int channel = FIRST_PULSE_CHANNEL + i;
            telemetry.addLine(String.format(Locale.US, "%d  %8d  %6s %6s%s", channel, pulseUs[i],
                    minUs[i] == Integer.MAX_VALUE ? "-" : Integer.toString(minUs[i]),
                    maxUs[i] == Integer.MIN_VALUE ? "-" : Integer.toString(maxUs[i]),
                    channel == TurretSubsystem.ABS_CHANNEL_A ? "   <- A"
                            : channel == TurretSubsystem.ABS_CHANNEL_B ? "   <- B" : ""));
        }
        telemetry.addLine("");
        telemetry.addLine(String.format(Locale.US, "A fraction %s (home %s, dir %d) · B fraction %s (home %s, dir %d)",
                fmt(fracA), fmt(TurretSubsystem.absHomeA), TurretSubsystem.absDirectionA,
                fmt(fracB), fmt(TurretSubsystem.absHomeB), TurretSubsystem.absDirectionB));
        telemetry.addLine("  (fractions need that encoder's absPulseMinUs / absPulseMaxUs)");
        telemetry.addLine(String.format(Locale.US, "Design: A %.4f, B %s turns per turret turn: repeats every %d turret turns, separation %s°",
                TurretSubsystem.turnsPerTurretTurn, fmt(TurretSubsystem.absRatioB),
                TurretAbsolute.periodTurretTurns(TurretSubsystem.turnsPerTurretTurn, TurretSubsystem.absRatioB),
                fmt(separation())));
        if (solution == null) {
            telemetry.addLine("Solve: no reading yet");
        } else if (solution.ok()) {
            telemetry.addLine(String.format(Locale.US, "Solve: turret %.1f° · residual %.1f° · margin %s°",
                    solution.turretDeg, solution.residualDeg, fmt(solution.marginDeg)));
        } else {
            telemetry.addLine("Solve: " + solution.problem);
        }
        telemetry.addLine("");
        telemetry.addLine(loopTimer.summary());
        if (panelsProblem != null) telemetry.addLine("Panels: " + panelsProblem);
        telemetry.update();
    }

    private void publish() {
        if (panels == null) return;
        try {
            panels.addData("oq_fault", fault == null ? 0.0 : 1.0);
            panels.addData("oq_data_valid", dataValid ? 1.0 : 0.0);
            for (int i = 0; i < PULSE_CHANNELS; i++) {
                panels.addData("oq_ch" + (FIRST_PULSE_CHANNEL + i) + "_us", pulseUs[i]);
            }
            panels.addData("oq_frac_a", fracA);
            panels.addData("oq_frac_b", fracB);
            if (solution != null) {
                panels.addData("oq_turret_deg", solution.turretDeg);
                panels.addData("oq_residual_deg", solution.residualDeg);
                panels.addData("oq_margin_deg", solution.marginDeg);
            }
            panels.addData("loop_ms", loopTimer.lastMs());
            panels.update();
        } catch (RuntimeException ignored) {
            // Telemetry never takes the bench down.
        }
    }

    private int pulseOn(int channel) {
        int i = channel - FIRST_PULSE_CHANNEL;
        return i >= 0 && i < PULSE_CHANNELS ? pulseUs[i] : 0;
    }

    private static String fmt(double value) {
        return Double.isNaN(value) ? "n/a" : String.format(Locale.US, "%.3f", value);
    }

    /** Null if the active config carries the OctoQuad; otherwise what to activate. */
    private static String checkActiveConfig() {
        String active;
        try {
            active = ActiveConfig.name();
        } catch (RuntimeException e) {
            return "Could not read the active configuration: " + e.getMessage();
        }
        RobotIdentity identity = RobotIdentity.fromConfigName(active);
        if (identity != null && hasOctoQuad(identity)) return null;
        return "Active config is \"" + (active == null ? "none" : active)
                + "\", which has no OctoQuad. Driver Station: Configure Robot -> robot_19429, robot_20245 or "
                + RobotIdentity.OCTOQUAD_RIG.configName + " -> Activate.";
    }

    static boolean hasOctoQuad(RobotIdentity identity) {
        for (DeviceNames.Device device : identity.devices) {
            if (DeviceNames.OCTOQUAD.equals(device.name)) return true;
        }
        return false;
    }
}
