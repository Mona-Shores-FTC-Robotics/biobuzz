package org.firstinspires.ftc.teamcode.subsystems;

import com.qualcomm.hardware.digitalchickenlabs.OctoQuad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.hardware.ActiveConfig;
import org.firstinspires.ftc.teamcode.hardware.DeviceNames;

import java.util.Locale;

/**
 * The turret. <b>So far, only its angle sensor</b>: a REV Thru-Bore encoder on the turret, driven
 * 1:1, its absolute output read by an OctoQuad on I2C (#169, decided on #81). Rotating the turret
 * comes with its motor, once the mechanism exists.
 *
 * <p>This is a mentor spike, built ahead of the mechanism so the robot is not waiting on code when
 * it is. Students rebuild it once it is proven on the robot.
 *
 * <h2>What it gives the rest of the code</h2>
 *
 * <ul>
 *   <li>{@link #angleDegrees()}: the turret's angle from forward, (-180°, 180°], CCW positive, or
 *       NaN when unknown. The turret rotates continuously (a slip ring, no stops), so there are no
 *       limits: aiming takes the shortest way round.</li>
 *   <li>{@link #health()}: the INIT check, read with nothing moving. See {@link TurretHealth}.</li>
 *   <li>{@link #zeroVerified()}: whether the zero was confirmed at the start of this match. Settled
 *       at PLAY, and handed from Auto to TeleOp by {@code RobotOpMode}.</li>
 * </ul>
 *
 * <h2>Why there is no homing</h2>
 *
 * <p>At 1:1 the absolute encoder reads the true angle wherever the turret is, at power-on, with
 * nothing moving. The home check only catches the encoder slipping on its shaft, and it can only do
 * that at the start of a match, when the turret is known to be at home.
 *
 * <h2>A missing OctoQuad</h2>
 *
 * <p>Not swallowed: if the device is not in the configuration, does not answer on I2C, or runs
 * firmware older than 3.x, health is {@link TurretHealth#NO_SIGNAL} and the Robot page says which.
 * The robot still drives; Smart Auto and Backup lock the turret forward. Once the OctoQuad has
 * failed its check at init it is not polled again, so a missing device costs no loop time.
 */
public class TurretSubsystem implements Subsystem {

    /**
     * The OctoQuad channel the encoder's absolute output is wired to. A specification, like a port
     * in {@code robot_*.xml}: wire it to this channel. It is the one port Java knows, because the
     * XML has no element for an OctoQuad channel. Channels 4–7 are bank 2, set to pulse width below.
     */
    public static final int ENCODER_CHANNEL = 4;

    private final OctoQuad octoquad;
    private final TurretCalibration calibration;

    /** Allocated once: the OctoQuad fills it every loop. */
    private final OctoQuad.EncoderDataBlock data = new OctoQuad.EncoderDataBlock();

    /** Why the OctoQuad is not being read, or null while it is. */
    private String fault;
    private String firmware = "?";

    private double rawDeg = Double.NaN;
    private double angleDeg = Double.NaN;
    private TurretHealth health = TurretHealth.NO_SIGNAL;
    private boolean zeroVerified;
    private boolean started;

    public TurretSubsystem(HardwareMap hardwareMap) {
        OctoQuad found = null;
        String why = null;
        try {
            found = hardwareMap.get(OctoQuad.class, DeviceNames.OCTOQUAD);
        } catch (RuntimeException e) {
            why = "OctoQuad \"" + DeviceNames.OCTOQUAD + "\" not in the active configuration";
        }
        octoquad = found;
        fault = why;
        calibration = TurretCalibration.forRobot(ActiveConfig.requireIdentity());
    }

    /**
     * Checks the OctoQuad answers and has firmware 3.x, then sets the encoder's channel up. Set
     * every init rather than saved to the OctoQuad's flash: it costs a few I2C writes, survives a
     * swapped board, and does not wear the flash.
     */
    @Override
    public void initialize() {
        if (octoquad != null) {
            try {
                fault = checkAndConfigure();
            } catch (RuntimeException e) {
                fault = "OctoQuad setup failed: "
                        + (e.getMessage() == null ? e.toString() : e.getMessage());
            }
        }
        // A first reading now, so the INIT screen's first frame shows the real check.
        update();
    }

    /** Null if the OctoQuad is ready to read; otherwise why not. */
    private String checkAndConfigure() {
        byte chip = octoquad.getChipId();
        if (chip != OctoQuad.OCTOQUAD_CHIP_ID) {
            return "OctoQuad not answering on I2C (chip id " + chip + ") — plugged in? on bus 2?";
        }
        OctoQuad.FirmwareVersion version = octoquad.getFirmwareVersion();
        firmware = version.toString();
        if (version.maj < OctoQuad.SUPPORTED_FW_VERSION_MAJ) {
            return "OctoQuad firmware " + firmware + "; SDK 12 needs "
                    + OctoQuad.SUPPORTED_FW_VERSION_MAJ + ".x — flash it";
        }
        octoquad.setChannelBankConfig(OctoQuad.ChannelBankConfig.BANK1_QUADRATURE_BANK2_PULSE_WIDTH);
        octoquad.setSingleChannelPulseWidthParams(ENCODER_CHANNEL,
                TurretCalibration.PULSE_MIN_US, TurretCalibration.PULSE_MAX_US);
        octoquad.setSingleChannelPulseWidthTracksWrap(ENCODER_CHANNEL, false);
        return null;
    }

    /** Reads the encoder once and classifies it. Runs in INIT too, so the check works before PLAY. */
    @Override
    public void update() {
        if (octoquad == null || fault != null) {
            rawDeg = Double.NaN;
        } else {
            try {
                octoquad.readAllEncoderData(data);
                rawDeg = data.isDataValid()
                        ? TurretCalibration.rawDegrees(data.positions[ENCODER_CHANNEL])
                        : Double.NaN;
            } catch (RuntimeException e) {
                rawDeg = Double.NaN;
            }
        }
        health = TurretHealth.classify(rawDeg, calibration, TurretCalibration.HOME_TOLERANCE_DEG);
        angleDeg = calibration.turretDegrees(rawDeg);
    }

    /** No motor yet, so nothing to cut. */
    @Override
    public void stop() {
    }

    // -------------------------------------------------------------------- reads

    /** The INIT check. See {@link TurretHealth}. */
    public TurretHealth health() {
        return health;
    }

    /** The turret's angle from forward, degrees in (-180, 180], CCW positive; NaN when unknown. */
    public double angleDegrees() {
        return angleDeg;
    }

    /** The encoder's own angle, degrees [0, 360), NaN with no signal. What re-zeroing writes down. */
    public double rawDegrees() {
        return rawDeg;
    }

    /** True if the zero was confirmed at the start of this match (here, or by Autonomous). */
    public boolean zeroVerified() {
        return zeroVerified;
    }

    /**
     * Settles {@link #zeroVerified()} at PLAY. Called once, by {@code RobotOpMode}.
     *
     * @param verifiedByAuto true if Autonomous confirmed the zero and handed that on. TeleOp then
     *     inherits it: Auto leaves the turret wherever it ended, so any angle is normal. Without it,
     *     the zero is verified only if the turret reads {@link TurretHealth#HOME} right now.
     */
    public void settleZeroAtPlay(boolean verifiedByAuto) {
        zeroVerified = verifiedByAuto || health == TurretHealth.HOME;
        started = true;
    }

    // ------------------------------------------------------------------ display

    @Override
    public void describe(Display display) {
        if (health == TurretHealth.NO_SIGNAL) {
            display.status("Turret", Display.Level.FAULT, "NO SIGNAL — "
                    + (fault != null ? fault : "OctoQuad answers, but no valid pulse on channel "
                    + ENCODER_CHANNEL + " — encoder plugged in? ABS wire on pin 5?"));
            return;
        }
        Display.Level level = health == TurretHealth.HOME ? Display.Level.OK : Display.Level.WARN;
        String angle = Double.isNaN(angleDeg) ? "" : String.format(Locale.US, " (%.1f°)", angleDeg);
        display.status("Turret", level, health.description + angle);
        if (started) {
            display.status("Zero", zeroVerified ? Display.Level.OK : Display.Level.WARN,
                    zeroVerified ? "verified this match" : "UNVERIFIED this match");
        }
        display.line(String.format(Locale.US, "raw %.1f° · fw %s", rawDeg, firmware));
    }
}
