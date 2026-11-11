package org.firstinspires.ftc.teamcode.opmodes.calibration;

import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.qualcomm.hardware.digitalchickenlabs.OctoQuad;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.Utility;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.teamcode.RobotDifferences;
import org.firstinspires.ftc.teamcode.hardware.ActiveConfig;
import org.firstinspires.ftc.teamcode.hardware.DeviceNames;
import org.firstinspires.ftc.teamcode.hardware.RobotIdentity;
import org.firstinspires.ftc.teamcode.subsystems.TurretAbsolute;
import org.firstinspires.ftc.teamcode.subsystems.TurretSubsystem;
import org.firstinspires.ftc.teamcode.util.LoopTimer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * The guided setup of the turret's two absolute encoders: two Studica Cypher Maxes on the 176T
 * ring (A on the 64T gear, B on the 44T), their absolute (PWM) outputs on the OctoQuad's channels
 * {@link TurretSubsystem#ABS_CHANNEL_A} and {@link TurretSubsystem#ABS_CHANNEL_B}. Runs on a
 * competition robot or on {@code robot_octoquad_rig}. Nothing moves: the turret is turned by hand.
 *
 * <p>The Driver Station shows one step at a time ({@link AbsoluteSetup}), each moving on by
 * itself when it has what it needs: the OctoQuad answers; each encoder's pulse range; which way
 * each turns; the home readings; a check. At the end it shows the lines to paste into
 * {@code RobotDifferences} for this robot, puts them on Panels (copy them from the laptop's browser)
 * and in the Robot Controller log, and applies them until the app restarts, so
 * {@code Bench: Turret Absolute} can be tried straight away.
 *
 * <p>Cross (A) does what a step asks. Square (X) starts the step again (on the first step, sets the
 * OctoQuad up again after a cable change). D-pad left goes back a step.
 *
 * <p>Standalone, like {@code ValidateHardware}: no {@code Robot}, no {@code Subsystem}. It applies
 * this robot's {@code RobotDifferences} first, as {@code RobotOpMode} would. Nothing is saved to the
 * OctoQuad's flash: the bank and pulse settings are sent at every init, as {@code TurretSubsystem}
 * does.
 */
@Utility(name = "Bench: OctoQuad", description = "Guided setup of the turret's two Cypher Max absolute encoders: step by step, turret turned by hand")
public class OctoQuadBench extends OpMode {

    /** First pulse-width channel of the OctoQuad's second bank. Channels 4 to 7 read pulse widths. */
    public static final int FIRST_PULSE_CHANNEL = 4;
    public static final int PULSE_CHANNELS = 4;

    /** Wider than any Cypher Max pulse (its frame is about 1.7 to 2.1 ms). */
    static final int PULSE_LIMIT_US = 4000;

    private OctoQuad octoquad;
    private final OctoQuad.EncoderDataBlock data = new OctoQuad.EncoderDataBlock();
    private final LoopTimer loopTimer = new LoopTimer();
    private AbsoluteSetup setup;

    private String configWarning;
    private String robotName = "TEAM_XXXXX";
    private final String date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    private String fault;
    private String firmware = "?";
    private boolean dataValid;
    private final int[] pulseUs = new int[PULSE_CHANNELS];
    private TelemetryManager panels;
    private String panelsProblem;
    private boolean crossWas;
    private boolean squareWas;
    private boolean leftWas;
    private boolean logged;

    @Override
    public void init() {
        configWarning = checkActiveConfig();
        try {
            RobotDifferences.applyForActiveRobot();
        } catch (RuntimeException e) {
            if (configWarning == null) configWarning = "robot differences not applied: " + e.getMessage();
        }
        // After this robot's values are applied, so the ratios are the ones it runs with.
        setup = new AbsoluteSetup(TurretSubsystem.turnsPerTurretTurn, TurretSubsystem.absRatioB);
        try {
            octoquad = hardwareMap.get(OctoQuad.class, DeviceNames.OCTOQUAD);
        } catch (RuntimeException e) {
            octoquad = null;
            fault = "OctoQuad \"" + DeviceNames.OCTOQUAD + "\" is not in the active configuration";
        }
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
        boolean cross = gamepad1.a && !crossWas;
        boolean square = gamepad1.x && !squareWas;
        boolean left = gamepad1.dpad_left && !leftWas;
        crossWas = gamepad1.a;
        squareWas = gamepad1.x;
        leftWas = gamepad1.dpad_left;

        if (square) {
            if (setup.step() == AbsoluteSetup.Step.BOARD && octoquad != null) setUp();
            setup.redo();
        }
        if (left) setup.back();

        dataValid = false;
        if (octoquad != null && fault == null) {
            try {
                octoquad.readAllEncoderData(data);
                dataValid = data.isDataValid();
                if (dataValid) {
                    for (int i = 0; i < PULSE_CHANNELS; i++) {
                        pulseUs[i] = data.positions[FIRST_PULSE_CHANNEL + i];
                    }
                }
            } catch (RuntimeException e) {
                fault = "read failed: " + (e.getMessage() == null ? e.toString() : e.getMessage());
            }
        }
        String boardProblem = fault != null ? fault : dataValid ? null : "OctoQuad data not valid";
        setup.update(fault == null && dataValid, boardProblem,
                pulseOn(TurretSubsystem.ABS_CHANNEL_A), pulseOn(TurretSubsystem.ABS_CHANNEL_B), cross);

        if (setup.measured()) {
            apply();
            double fa = TurretAbsolute.fractionFromPulse(pulseOn(TurretSubsystem.ABS_CHANNEL_A),
                    TurretSubsystem.absPulseMinUsA, TurretSubsystem.absPulseMaxUsA);
            double fb = TurretAbsolute.fractionFromPulse(pulseOn(TurretSubsystem.ABS_CHANNEL_B),
                    TurretSubsystem.absPulseMinUsB, TurretSubsystem.absPulseMaxUsB);
            double separation = TurretAbsolute.separationDeg(TurretSubsystem.turnsPerTurretTurn, TurretSubsystem.absRatioB);
            double maxResidual = Double.isNaN(TurretSubsystem.absMaxResidualDeg)
                    ? separation / 3.0 : TurretSubsystem.absMaxResidualDeg;
            setup.checkSolution(TurretAbsolute.solve(fa, fb, TurretSubsystem.absHomeA, TurretSubsystem.absHomeB,
                    TurretSubsystem.turnsPerTurretTurn, TurretSubsystem.absRatioB,
                    TurretSubsystem.absDirectionA, TurretSubsystem.absDirectionB, maxResidual, separation / 2.0));
        }
        if (setup.step() == AbsoluteSetup.Step.DONE && !logged) {
            logged = true;
            for (String line : setup.pasteLines(robotName, date)) RobotLog.ii("OctoQuadBench", line);
        } else if (setup.step() != AbsoluteSetup.Step.DONE) {
            logged = false;
        }
        report();
        publish();
    }

    /** This run's measured values into TurretSubsystem, until the app restarts. */
    private void apply() {
        TurretSubsystem.absPulseMinUsA = setup.minA();
        TurretSubsystem.absPulseMaxUsA = setup.maxA();
        TurretSubsystem.absPulseMinUsB = setup.minB();
        TurretSubsystem.absPulseMaxUsB = setup.maxB();
        TurretSubsystem.absDirectionA = setup.dirA();
        TurretSubsystem.absDirectionB = setup.dirB();
        TurretSubsystem.absHomeA = setup.homeA();
        TurretSubsystem.absHomeB = setup.homeB();
        if (setup.step() == AbsoluteSetup.Step.DONE) TurretSubsystem.useAbsoluteZero = true;
    }

    private void report() {
        if (configWarning != null) telemetry.addLine("CONFIG: " + configWarning);
        telemetry.addLine("== " + setup.title() + " ==");
        if (setup.problem() != null) telemetry.addLine("⚠ " + setup.problem());
        for (String line : setup.lines()) telemetry.addLine(line);
        if (setup.step() == AbsoluteSetup.Step.DONE) {
            telemetry.addLine("");
            for (String line : setup.pasteLines(robotName, date)) telemetry.addLine(line);
        }
        telemetry.addLine("");
        telemetry.addLine("Cross (A) do it · Square (X) start this step again · D-pad left back");
        StringBuilder raw = new StringBuilder("fw " + firmware + " · ");
        for (int i = 0; i < PULSE_CHANNELS; i++) {
            int channel = FIRST_PULSE_CHANNEL + i;
            raw.append("ch").append(channel)
                    .append(channel == TurretSubsystem.ABS_CHANNEL_A ? "(A)" : channel == TurretSubsystem.ABS_CHANNEL_B ? "(B)" : "")
                    .append(' ').append(pulseUs[i]).append(" µs  ");
        }
        telemetry.addLine(raw.toString());
        telemetry.addLine(loopTimer.summary());
        if (panelsProblem != null) telemetry.addLine("Panels: " + panelsProblem);
        telemetry.update();
    }

    private void publish() {
        if (panels == null) return;
        try {
            panels.addData("oq_step", setup.step().ordinal() + 1);
            panels.addData("oq_data_valid", dataValid ? 1.0 : 0.0);
            panels.addData("oq_a_us", pulseOn(TurretSubsystem.ABS_CHANNEL_A));
            panels.addData("oq_b_us", pulseOn(TurretSubsystem.ABS_CHANNEL_B));
            if (setup.step() == AbsoluteSetup.Step.DONE) {
                List<String> paste = setup.pasteLines(robotName, date);
                for (int i = 0; i < paste.size(); i++) {
                    panels.addData(String.format(Locale.US, "oq_paste_%02d", i + 1), paste.get(i));
                }
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

    /** Null if the active config carries the OctoQuad; otherwise what to activate. Sets the paste lines' robot. */
    private String checkActiveConfig() {
        String active;
        try {
            active = ActiveConfig.name();
        } catch (RuntimeException e) {
            return "Could not read the active configuration: " + e.getMessage();
        }
        RobotIdentity identity = RobotIdentity.fromConfigName(active);
        if (identity != null) robotName = identity.name();
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
