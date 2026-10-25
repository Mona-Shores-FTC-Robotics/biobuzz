package org.firstinspires.ftc.teamcode.opmodes.calibration;

import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.qualcomm.hardware.digitalchickenlabs.OctoQuad;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.Utility;

import org.firstinspires.ftc.teamcode.hardware.ActiveConfig;
import org.firstinspires.ftc.teamcode.hardware.DeviceNames;
import org.firstinspires.ftc.teamcode.hardware.RobotIdentity;
import org.firstinspires.ftc.teamcode.util.LoopTimer;

import java.util.Locale;

/**
 * Bench test for the turret's angle sensing (#169): an OctoQuad on I2C and two REV Thru-Bore
 * encoders on its pulse-width channels, on a spare Control Hub with {@code robot_octoquad_rig}
 * active. No turret needed. It answers the facts-table rows that do not need one:
 *
 * <ul>
 *   <li>Does the OctoQuad answer on bus 2, and what firmware is it? SDK 12 needs v3.x.</li>
 *   <li>Which channels are the two encoders on? All four pulse-width channels (4 to 7) are shown,
 *       so plug in anywhere and read it off.</li>
 *   <li>Does each encoder read a clean 1 to 1024 µs pulse, and does it hold still when the shaft
 *       does? That is the noise the two-encoder trick has to beat.</li>
 *   <li>The two-encoder difference: turn the shaft that drives both encoders one full turn and
 *       watch {@code diff}. How far it moves per turn, against how much it jitters at rest, is the
 *       margin that says whether the trick can tell one turret turn from the next.</li>
 * </ul>
 *
 * <p>Standalone diagnostics, like {@code ValidateHardware}: no {@code Robot}, no {@code Subsystem}.
 * It is the exception CLAUDE.md allows for rigs; the robot's turret code is {@code TurretSubsystem}
 * on PR #174, which this bench feeds numbers to.
 *
 * <p>Nothing is saved to the OctoQuad's flash: the bank and pulse settings are sent at every init,
 * so a swapped OctoQuad works without a configuration menu visit, the same choice #174 makes.
 *
 * <p>X re-runs the setup after a cable change. Panels gets every reading under flat keys.
 */
@Disabled
@Utility(name = "OctoQuad Bench", description = "Two Thru-Bores through an OctoQuad; disabled since the turret chose the Cypher Max (#169)")
@Configurable
public class OctoQuadBench extends OpMode {

    /** First pulse-width channel of the OctoQuad's second bank. Channels 4 to 7 read pulse widths. */
    public static final int FIRST_PULSE_CHANNEL = 4;
    public static final int PULSE_CHANNELS = 4;

    /** A REV Thru-Bore's absolute output: a pulse 1 to 1024 µs wide, once per revolution. */
    public static final int PULSE_MIN_US = 1;
    public static final int PULSE_MAX_US = 1024;
    static final double DEGREES_PER_US = 360.0 / 1024.0;

    /** The channel encoder A is on (4 to 7). A bench choice; read it off the screen and set it here. */
    public static int channelA = 4;

    /** The channel encoder B is on (4 to 7). */
    public static int channelB = 5;

    private OctoQuad octoquad;
    private final OctoQuad.EncoderDataBlock data = new OctoQuad.EncoderDataBlock();
    private final LoopTimer loopTimer = new LoopTimer();

    private String configWarning;
    private String fault;
    private String firmware = "?";
    private boolean dataValid;
    private final int[] pulseUs = new int[PULSE_CHANNELS];
    private TelemetryManager panels;
    private String panelsProblem;
    private boolean xWasDown;

    @Override
    public void init() {
        configWarning = checkActiveConfig();
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
        telemetry.addLine("OctoQuad Bench. INIT shows the readings; PLAY is the same. Square (X) re-runs setup.");
        report();
    }

    /** Chip id, firmware, bank config and the pulse range on all four channels. Nothing saved to flash. */
    private void setUp() {
        try {
            byte chip = octoquad.getChipId();
            if (chip != OctoQuad.OCTOQUAD_CHIP_ID) {
                fault = "OctoQuad not answering on I2C (chip id " + chip + "): plugged in? on bus 2?";
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
                octoquad.setSingleChannelPulseWidthParams(channel, PULSE_MIN_US, PULSE_MAX_US);
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
        boolean x = gamepad1.x;
        if (x && !xWasDown && octoquad != null) {
            setUp();
        }
        xWasDown = x;

        dataValid = false;
        if (octoquad != null && fault == null) {
            try {
                octoquad.readAllEncoderData(data);
                dataValid = data.isDataValid();
                for (int i = 0; i < PULSE_CHANNELS; i++) {
                    pulseUs[i] = dataValid ? data.positions[FIRST_PULSE_CHANNEL + i] : 0;
                }
            } catch (RuntimeException e) {
                fault = "read failed: " + (e.getMessage() == null ? e.toString() : e.getMessage());
            }
        }
        report();
        publish();
    }

    private void report() {
        if (configWarning != null) telemetry.addLine("CONFIG: " + configWarning);
        if (fault != null) {
            telemetry.addLine("FAULT: " + fault);
        } else {
            telemetry.addLine("OctoQuad fw " + firmware + (dataValid ? "" : " · data NOT valid this loop"));
        }
        telemetry.addLine("");
        telemetry.addLine("ch    pulse us    degrees");
        for (int i = 0; i < PULSE_CHANNELS; i++) {
            int channel = FIRST_PULSE_CHANNEL + i;
            double deg = degreesFromPulse(pulseUs[i]);
            telemetry.addLine(String.format(Locale.US, "%d  %9d  %9s%s", channel, pulseUs[i],
                    Double.isNaN(deg) ? "no pulse" : String.format(Locale.US, "%.1f", deg),
                    channel == channelA ? "   <- A" : channel == channelB ? "   <- B" : ""));
        }
        telemetry.addLine("");
        double a = degreesFromPulse(pulseOn(channelA));
        double b = degreesFromPulse(pulseOn(channelB));
        double diff = wrap360(a - b);
        telemetry.addLine(String.format(Locale.US, "Encoder A %s · Encoder B %s · diff (A−B) %s",
                fmt(a), fmt(b), fmt(diff)));
        telemetry.addLine("Turn the shaft one full turn and note how far diff moves; at rest, how much it jitters.");
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
                int channel = FIRST_PULSE_CHANNEL + i;
                panels.addData("oq_ch" + channel + "_us", pulseUs[i]);
                panels.addData("oq_ch" + channel + "_deg", degreesFromPulse(pulseUs[i]));
            }
            double a = degreesFromPulse(pulseOn(channelA));
            double b = degreesFromPulse(pulseOn(channelB));
            panels.addData("oq_a_deg", a);
            panels.addData("oq_b_deg", b);
            panels.addData("oq_diff_deg", wrap360(a - b));
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

    private static String fmt(double deg) {
        return Double.isNaN(deg) ? "n/a" : String.format(Locale.US, "%.1f°", deg);
    }

    private static String checkActiveConfig() {
        String wanted = RobotIdentity.OCTOQUAD_RIG.configName;
        String active;
        try {
            active = ActiveConfig.name();
        } catch (RuntimeException e) {
            return "Could not read the active configuration: " + e.getMessage();
        }
        if (wanted.equals(active)) return null;
        return "Active config is \"" + (active == null ? "none" : active) + "\", not \"" + wanted
                + "\". Driver Station: Configure Robot -> " + wanted + " -> Activate.";
    }

    /**
     * A Thru-Bore pulse width to the encoder's own angle, degrees [0, 360), or NaN when the width
     * is outside the 1 to 1024 µs an absolute output can produce (no encoder, or the quadrature
     * wire on a pulse channel).
     */
    static double degreesFromPulse(int pulseUs) {
        if (pulseUs < PULSE_MIN_US || pulseUs > PULSE_MAX_US) return Double.NaN;
        return wrap360((pulseUs - PULSE_MIN_US) * DEGREES_PER_US);
    }

    /** Degrees into [0, 360). NaN stays NaN. */
    static double wrap360(double degrees) {
        if (Double.isNaN(degrees)) return Double.NaN;
        double d = degrees % 360.0;
        return d < 0 ? d + 360.0 : d;
    }
}
