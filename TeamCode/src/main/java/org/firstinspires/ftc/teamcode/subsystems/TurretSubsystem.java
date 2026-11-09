package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.hardware.digitalchickenlabs.OctoQuad;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PwmControl;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.controls.Display;
import org.firstinspires.ftc.teamcode.hardware.DeviceNames;
import org.firstinspires.ftc.teamcode.util.Throttle;

import java.util.Locale;

/**
 * The turret: a goBILDA servo in standard (position) mode ({@link DeviceNames#TURRET}) turning
 * it, and a Studica Cypher Max encoder on its drive, read as a relative quadrature count through
 * the encoder socket of {@link DeviceNames#TURRET_ENCODER_HOST}. The servo holds the position it
 * is sent with its own feedback, so the turret resists a push from PLAY on, with or without the
 * encoder. The servo went from continuous to position mode on 6 Nov 2026 (the mentor's call), so
 * the turret holds its home instead of turning freely; the encoder numbers below are from the
 * {@code TurretBench} session of 24 Oct 2026 on 20245.
 *
 * <h2>What the OpMode asks for</h2>
 *
 * <ul>
 *   <li>{@link #zeroHere()}: the turret is physically at home (its mark lined up, pointing along
 *       the robot's nose). The count is relative, so a person does this before INIT ends, and
 *       nothing here can tell if they forgot: {@link #zeroed()} only says whether it was asked.</li>
 *   <li>{@link #aim(double)}: hold a chassis-frame angle, degrees counter-clockwise from the
 *       nose. The aim math that produces it is {@link TurretAim}; which target is the OpMode's.</li>
 *   <li>{@link #home()}: hold 0. {@link #idle()}: pulse off, limp, hold nothing.</li>
 * </ul>
 *
 * <p>{@link #update()} reads the encoder once and, from PLAY, sends the servo the position for the
 * held angle: {@link #homePosition} plus the angle over {@link #degreesPerUnit}. Nothing blocks.
 * While {@link #lockedForward} it always holds {@link #homePosition}.
 *
 * <h2>Hardware it shares</h2>
 *
 * <p>The encoder host is another mechanism's motor. This class reads its position and velocity and
 * <b>never</b> sets its power or mode, and never resets its count: zero is a software offset, so
 * the feeder (today's host) is untouched.
 *
 * <h2>The absolute zero (two Cypher Maxes through the OctoQuad)</h2>
 *
 * <p>With {@link #useAbsoluteZero} on, the turret also knows its angle with nothing moving: the
 * Cypher Max above (encoder A, {@link #turnsPerTurretTurn} turns per turret turn) and a second
 * Cypher Max on a different gear (encoder B, {@link #absRatioB}) send their absolute PWM outputs
 * to the OctoQuad ({@link DeviceNames#OCTOQUAD}), channels {@link #ABS_CHANNEL_A} and
 * {@link #ABS_CHANNEL_B}. {@link TurretAbsolute} turns the two readings into the turret's angle,
 * and during INIT that sets the zero ({@link ZeroSource#ABSOLUTE}), so the turret need not sit on
 * its mark at PLAY and a TeleOp after a crash knows where it is. A person's zero button still
 * wins. The quadrature count carries the angle from there: the OctoQuad is read only in INIT,
 * so the match loop pays nothing for it. Off until a robot's values are measured on
 * {@code Bench: OctoQuad}; then on for that robot in {@link org.firstinspires.ftc.teamcode.RobotDifferences}.
 *
 * <p>The values below are both robots', measured on 20245. One that 19429 measures differently is
 * a line in {@link org.firstinspires.ftc.teamcode.RobotDifferences}, not a second field here.
 */
@Configurable
public class TurretSubsystem implements Subsystem {

    // ------------------------------------------------------------ measured on the bench (20245)

    /** Encoder turns per turret turn: 2038 counts per flag turn on the bench, so 1 (24 Oct 2026). */
    public static double turnsPerTurretTurn = 1.0;

    /** +1 if the count rises as the turret turns counter-clockwise seen from above. -1 on 20245: it falls (robot, 24 Oct 2026). */
    public static int direction = -1;

    /**
     * The servo position, 0 to 1, with the turret straight ahead (home), held from PLAY. 19429's
     * is measured (0.514, its line in {@link org.firstinspires.ftc.teamcode.RobotDifferences}).
     * 0.5 here is 20245's, not measured: the servo's centre, the 1500 us pulse the old code sent at
     * PLAY, which is also "stop" if 20245's servo is still in continuous mode (as it was on the
     * 24 Oct bench). So 20245 does what it did until its servo's mode is checked
     * ({@code doc/robot-todo.md}): any other value sent to a continuous servo is a speed.
     */
    public static double homePosition = 0.5;

    /**
     * The lowest and highest servo position any code may send, the bench included. Past the ends
     * of what the servo senses it never arrives and keeps turning (19429, Bench: Turret, 6 Nov
     * 2026: below about 0.22 it ran away, sometimes for good), and if the servo does not turn the
     * turret 1:1 a runaway also moves {@link #homePosition} (it moved from 0.350 to 0.514 there).
     * So nothing goes past these: a home or an aim outside them is refused (limp), and an aim is
     * clamped into them. 0 and 1 here are 20245's, not measured; 19429's are in
     * {@link org.firstinspires.ftc.teamcode.RobotDifferences}.
     */
    public static double minPosition = 0.0;
    public static double maxPosition = 1.0;

    /**
     * Correct the servo's position from the encoder ({@link TurretRehome}): once the turret is
     * still, the encoder's angle against the one asked for moves an offset on every position sent,
     * so a turret twirled by hand while limp (its servo then out of step through the 2.75:1
     * gearing) comes back to its mark at PLAY, and a knock is undone. Needs a trusted encoder,
     * zeroed with the turret on its mark (at PLAY), and {@link #degreesPerUnit}; it uses the
     * encoder's real angle even while {@link #lockedForward}. Off unless a robot's encoder is
     * trusted: on for 19429 in {@link org.firstinspires.ftc.teamcode.RobotDifferences}.
     */
    public static boolean rehomeFromEncoder = false;
    /** Slower than this the turret counts as still, deg/s. */
    public static double rehomeStillDps = 5.0;
    /** Still this long, the servo has finished moving and a correction is made, ms. */
    public static double rehomeStillMs = 250.0;
    /** Errors this small are left alone, degrees. */
    public static double rehomeToleranceDeg = 1.5;

    /**
     * Turret degrees, counter-clockwise seen from above, per +1.0 of servo position; negative if a
     * higher position turns it clockwise. Includes the gearing. NaN until measured with
     * {@code Bench: Turret}: until then the turret holds home and refuses any other angle.
     */
    public static double degreesPerUnit = Double.NaN;

    /** Inside this many degrees of the target, {@link #onTarget()}. */
    public static double holdToleranceDeg = 1.0;

    /**
     * Where the turret's axis sits from the robot's tracked point, inches, robot frame (+forward,
     * +left). <b>From the CAD, not yet taped:</b> the turret ring's centre measured against three
     * wheel axles in Onshape (24 Oct 2026): 84.5 mm behind the midpoint of the four wheels and
     * 47.6 mm to the right of it. Assumes the Pinpoint tracks that midpoint. Tape it once the
     * turret is bolted on, and re-measure if the ring moves in the CAD.
     */
    public static double turretForwardIn = -3.33;
    public static double turretLeftIn = -1.87;

    /** How far the turret may be asked to turn each way from home, degrees. NaN = no clamp: the turret has no cables, so only the servo's own travel (positions 0 to 1) limits it. */
    public static double travelEachWayDeg = Double.NaN;

    /**
     * An encoder angle this far from the held one means the servo turned it the wrong way, or the
     * encoder reads the wrong way: the hold is dropped and the turret goes limp. The Robot page
     * says so. Flip the sign of {@link #degreesPerUnit} (the turret went the wrong way) or
     * {@link #direction} (it went the right way but the angle read the other), then aim again.
     */
    public static double runawayDeg = 180.0;

    /**
     * The turret is held straight ahead and its encoder is not trusted: its angle is 0, whatever
     * the encoder socket reads, and from PLAY the servo holds {@link #homePosition}, never another
     * angle. The mentor's call on 1 Nov 2026: both robots play locked, POLLEN only; since 6 Nov the
     * servo is plugged in, in position mode on 19429, so the turret resists a push. On 20245 the
     * socket ({@code DeviceNames.TURRET_ENCODER_HOST}) carried the <b>feeder's</b> encoder, so
     * reading it as the turret's angle gave thousands of degrees once the feeder ran, and the
     * driver's chassis aim (CELL bearing minus turret angle) spun the robot without end; on 19429
     * the Cypher Max replaced the feeder's cable in it on 6 Nov. Set false only on a robot with the
     * Cypher Max in that socket, its ratio checked on {@code Bench: Turret}, and
     * {@link #degreesPerUnit} measured.
     */
    public static boolean lockedForward = true;

    // ------------------------------------------------------------ absolute zero (OctoQuad)

    /**
     * The OctoQuad channels the two Cypher Maxes' absolute (PWM) outputs are wired to. A
     * specification, like a port in {@code robot_*.xml}: wire them here. The one place Java knows a
     * port, because the XML has no element for an OctoQuad channel (CLAUDE.md). Channels 4 to 7 are
     * the OctoQuad's second bank, set to pulse width.
     */
    public static final int ABS_CHANNEL_A = 4;
    public static final int ABS_CHANNEL_B = 5;

    /**
     * Zero the turret from the two absolute encoders in INIT. Off: the OctoQuad is not touched and
     * the turret zeroes as before (at PLAY, from the Auto, or by hand). Turn it on for a robot in
     * {@code RobotDifferences} once every value below is measured.
     */
    public static boolean useAbsoluteZero = false;

    /**
     * Encoder B's turns per turret turn: the ring's teeth over B's gear's. From the build team,
     * checked on {@code Bench: OctoQuad} (one turret turn by hand). Encoder A's is
     * {@link #turnsPerTurretTurn}: A is the Cypher Max whose quadrature is in the hub socket.
     */
    public static double absRatioB = Double.NaN;

    /** +1 if A's absolute reading rises as the turret turns counter-clockwise from above, -1 if not. 0 = not measured. */
    public static int absDirectionA = 0;
    /** The same for B. Its gear may mesh the other way round from A's. 0 = not measured. */
    public static int absDirectionB = 0;

    /** A's absolute reading with the turret on its home mark, a fraction of a turn [0, 1). NaN until measured. */
    public static double absHomeA = Double.NaN;
    /** B's, the same way. */
    public static double absHomeB = Double.NaN;

    /**
     * Each encoder's narrowest and widest absolute pulse, µs, read off {@code Bench: OctoQuad}
     * while the encoder turns through its own zero. The Cypher Max's PWM frequency differs from
     * one encoder to the next (490 to 603 Hz), so these are per encoder, never the datasheet's.
     */
    public static double absPulseMinUsA = Double.NaN;
    public static double absPulseMaxUsA = Double.NaN;
    public static double absPulseMinUsB = Double.NaN;
    public static double absPulseMaxUsB = Double.NaN;

    /** How far the turret can turn each way from home, degrees, for the search. From the CAD or by turning it to each end. NaN until known. */
    public static double absEachWayDeg = Double.NaN;

    /**
     * Readings further apart than this, in degrees of encoder B, are refused (slipped, or a value
     * here is wrong). NaN: a third of {@link TurretAbsolute#separationDeg} for these gears, the
     * design's own budget, until the bench measures the real spread.
     */
    public static double absMaxResidualDeg = Double.NaN;

    /** In INIT, the zero is taken again from the absolute pair if the two disagree by more than this, degrees. */
    public static double absRezeroDeg = 1.0;

    // ------------------------------------------------------------ state

    private enum Mode { IDLE, HOLD }

    /** Where the zero came from, for the Robot page and the log. */
    public enum ZeroSource {
        NONE("not zeroed yet"),
        HAND("zeroed by hand"),
        PLAY("zeroed at PLAY, assumed at home"),
        AUTO("zero carried from the Auto"),
        ABSOLUTE("zeroed from the two absolute encoders");

        public final String text;

        ZeroSource(String text) {
            this.text = text;
        }
    }

    private final DcMotorEx encoderHost;
    private final Servo servo;
    private final PwmControl pwm;   // null if this servo cannot switch its pulse off
    /** Read only while {@link #useAbsoluteZero} is on, and only in INIT. */
    private final OctoQuad octoquad;
    private final OctoQuad.EncoderDataBlock octoData = new OctoQuad.EncoderDataBlock();
    /** Why the OctoQuad is not read, or null while it is. */
    private String absFault;
    private TurretAbsolute.Solution absSolution;
    private int absPulseA;
    private int absPulseB;
    /** Absolute angle minus the quadrature angle at the last solve, degrees; NaN without one. */
    private double absCheckDeg = Double.NaN;

    private TurretEncoder encoder;
    private double encoderTurnsSetting = Double.NaN;
    private int directionSetting;
    private String settingsProblem;

    private Mode mode = Mode.IDLE;
    private boolean started;
    private double targetDeg;

    private long counts;
    private long zeroCounts;
    private boolean zeroed;
    private ZeroSource zeroSource = ZeroSource.NONE;
    private double angleDeg = Double.NaN;
    private double rateDps = Double.NaN;
    private double errorDeg = Double.NaN;
    /**
     * What the servo was last told, to the PWM's own step; NaN before the first and while limp. A
     * write is a 2–3 ms round trip to the Expansion Hub, so an unchanged one waits for
     * {@link #resend}, which keeps the repeat that recovers a servo a hub has reset.
     */
    private double servoOut = Double.NaN;
    private boolean limp = true;
    private final Throttle resend = new Throttle(250, 0);
    private String runawayNote;
    /** Why the held angle could not be sent, or null. */
    private String aimNote;
    private final TurretRehome rehome = new TurretRehome();
    /** The encoder's own angle and rate, whether or not the turret is locked; NaN without one. */
    private double encoderAngleDeg = Double.NaN;
    private double encoderRateDps = Double.NaN;

    /** Looks the devices up once. A missing one fails init with the SDK's own message. */
    public TurretSubsystem(HardwareMap hardwareMap) {
        encoderHost = hardwareMap.get(DcMotorEx.class, DeviceNames.TURRET_ENCODER_HOST);
        servo = hardwareMap.get(Servo.class, DeviceNames.TURRET);
        pwm = servo instanceof PwmControl ? (PwmControl) servo : null;
        // Always in the configuration (the SDK builds it from the XML whether or not a board is
        // plugged in), so "found" never means "answering": setUpOctoQuad() checks that.
        octoquad = hardwareMap.get(OctoQuad.class, DeviceNames.OCTOQUAD);
    }

    // ------------------------------------------------------------ requests

    /** The turret is at home right now: angles are measured from here on. A person's request, and it wins. */
    public void zeroHere() {
        zeroCounts = counts;
        zeroed = true;
        zeroSource = ZeroSource.HAND;
    }

    /** At PLAY: the turret is assumed at home, so zero here unless a person or the handoff already did. */
    public void zeroAtPlay() {
        if (zeroed) {
            return;
        }
        zeroCounts = counts;
        zeroed = true;
        zeroSource = ZeroSource.PLAY;
    }

    /**
     * Take the zero an Autonomous left behind. The count is the hub's and was never reset, so the
     * same offset still gives the same angle, wherever the Auto turned the turret to.
     */
    public void restoreZero(long autoZeroCounts) {
        zeroCounts = autoZeroCounts;
        zeroed = true;
        zeroSource = ZeroSource.AUTO;
    }

    /** The offset that makes an angle: the encoder count at home. For the handoff to carry. */
    public long zeroCounts() {
        return zeroCounts;
    }

    /**
     * The drive team discarded the handoff ("start fresh"): drop a zero carried from the last
     * OpMode, so the turret zeroes at PLAY as on any fresh start. A zero set by hand stays.
     */
    public void forgetCarriedZero() {
        if (zeroSource == ZeroSource.AUTO) {
            zeroed = false;
            zeroSource = ZeroSource.NONE;
        }
    }

    /** Where the current zero came from. */
    public ZeroSource zeroSource() {
        return zeroSource;
    }

    /** Hold {@code deg} from home, counter-clockwise positive, chassis frame. Clamped into the travel if known. */
    public void aim(double deg) {
        if (Double.isNaN(deg)) {
            return;
        }
        if (mode != Mode.HOLD) {
            runawayNote = null;
        }
        double limit = travelEachWayDeg;
        targetDeg = Double.isNaN(limit) || limit <= 0 ? deg : Math.max(-limit, Math.min(limit, deg));
        mode = Mode.HOLD;
    }

    /** Hold straight ahead. */
    public void home() {
        aim(0.0);
    }

    /**
     * Pulse off: the servo goes limp and the turret stays wherever it is pushed. While
     * {@link #lockedForward} the turret holds home all the same.
     */
    public void idle() {
        mode = Mode.IDLE;
    }

    // ------------------------------------------------------------ reads

    /** Degrees from home, counter-clockwise positive, not wrapped; NaN until the settings are valid. */
    public double angleDegrees() {
        return angleDeg;
    }

    /** Degrees per second, counter-clockwise positive. */
    public double rateDegreesPerSecond() {
        return rateDps;
    }

    /** The angle being held, or NaN when not holding. */
    public double targetDegrees() {
        return mode == Mode.HOLD ? targetDeg : Double.NaN;
    }

    /** Target minus angle, or NaN when not holding. */
    public double errorDegrees() {
        return errorDeg;
    }

    /** Holding, and within the tolerance of the target. */
    public boolean onTarget() {
        return mode == Mode.HOLD && !Double.isNaN(errorDeg) && Math.abs(errorDeg) <= Math.abs(finite(holdToleranceDeg));
    }

    /** Whether {@link #zeroHere()} has been called since this OpMode started. */
    public boolean zeroed() {
        return zeroed;
    }

    /** Whether the turret is holding an angle. */
    public boolean holding() {
        return mode == Mode.HOLD;
    }

    // ------------------------------------------------------------ lifecycle

    /**
     * Idle, angle relative to wherever it sits until {@link #zeroHere()}. The servo is not written
     * until PLAY: unwritten it is limp, since nothing may move or be held by a servo before PLAY
     * (G304.H, G403; see {@link Subsystem}). So the drive team sets the turret at home by its mark
     * in INIT, and at PLAY the servo takes it to {@link #homePosition} and holds it there.
     */
    @Override
    public void initialize() {
        mode = Mode.IDLE;
        started = false;
        refreshSettings();
        counts = encoderHost.getCurrentPosition();
        zeroCounts = counts;
        zeroed = false;
        zeroSource = ZeroSource.NONE;
        rehome.reset();
        absSolution = null;
        absCheckDeg = Double.NaN;
        absFault = useAbsoluteZero ? setUpOctoQuad() : null;
    }

    /** Null if the OctoQuad is ready; otherwise why not. Sent every init, never saved to its flash. */
    private String setUpOctoQuad() {
        try {
            byte chip = octoquad.getChipId();
            if (chip != OctoQuad.OCTOQUAD_CHIP_ID) {
                return "OctoQuad not answering on I2C (chip id " + chip + "): plugged in? right bus?";
            }
            OctoQuad.FirmwareVersion version = octoquad.getFirmwareVersion();
            if (version.maj < OctoQuad.SUPPORTED_FW_VERSION_MAJ) {
                return "OctoQuad firmware " + version + "; SDK 12 needs "
                        + OctoQuad.SUPPORTED_FW_VERSION_MAJ + ".x";
            }
            octoquad.setChannelBankConfig(OctoQuad.ChannelBankConfig.BANK1_QUADRATURE_BANK2_PULSE_WIDTH);
            for (int channel : new int[] {ABS_CHANNEL_A, ABS_CHANNEL_B}) {
                octoquad.setSingleChannelPulseWidthParams(channel, 1, ABS_PULSE_LIMIT_US);
                octoquad.setSingleChannelPulseWidthTracksWrap(channel, false);
            }
            return null;
        } catch (RuntimeException e) {
            return "OctoQuad setup failed: " + (e.getMessage() == null ? e.toString() : e.getMessage());
        }
    }

    /** Wider than any Cypher Max pulse (a frame is about 1.7 to 2.1 ms). */
    static final int ABS_PULSE_LIMIT_US = 4000;

    /**
     * One OctoQuad read and solve, in INIT only. A good solve sets the zero unless a person set it,
     * and sets it again if the quadrature angle has drifted from it (the turret was moved while
     * the first solve was taken).
     */
    private void readAbsolute() {
        if (absFault != null) {
            return;
        }
        try {
            octoquad.readAllEncoderData(octoData);
        } catch (RuntimeException e) {
            absFault = "OctoQuad read failed: " + (e.getMessage() == null ? e.toString() : e.getMessage());
            return;
        }
        if (!octoData.isDataValid()) {
            absSolution = new TurretAbsolute.Solution("OctoQuad data not valid this loop",
                    Double.NaN, Double.NaN, Double.NaN);
            return;
        }
        absPulseA = octoData.positions[ABS_CHANNEL_A];
        absPulseB = octoData.positions[ABS_CHANNEL_B];
        double fa = TurretAbsolute.fractionFromPulse(absPulseA, absPulseMinUsA, absPulseMaxUsA);
        double fb = TurretAbsolute.fractionFromPulse(absPulseB, absPulseMinUsB, absPulseMaxUsB);
        double separation = TurretAbsolute.separationDeg(turnsPerTurretTurn, absRatioB, absEachWayDeg);
        double maxResidual = Double.isNaN(absMaxResidualDeg) ? separation / 3.0 : absMaxResidualDeg;
        absSolution = TurretAbsolute.solve(fa, fb, absHomeA, absHomeB, turnsPerTurretTurn, absRatioB,
                absDirectionA, absDirectionB, absEachWayDeg, maxResidual, separation / 2.0);
        if (!absSolution.ok() || encoder == null) {
            absCheckDeg = Double.NaN;
            return;
        }
        double quadDeg = zeroed ? encoder.degrees(counts, zeroCounts) : Double.NaN;
        absCheckDeg = absSolution.turretDeg - quadDeg;
        if (!TurretZeroPolicy.absoluteMayZero(zeroSource == ZeroSource.HAND)) {
            return;
        }
        if (zeroSource != ZeroSource.ABSOLUTE || !(Math.abs(absCheckDeg) <= Math.abs(finite(absRezeroDeg)))) {
            zeroCounts = counts - Math.round(absSolution.turretDeg / (encoder.direction * encoder.degreesPerCount()));
            zeroed = true;
            zeroSource = ZeroSource.ABSOLUTE;
            absCheckDeg = 0.0;
        }
    }
    /** PLAY: the servo may be written from now on. */
    @Override
    public void start() {
        started = true;
    }


    /** One encoder read, and from PLAY one servo write. */
    @Override
    public void update() {
        refreshSettings();
        counts = encoderHost.getCurrentPosition();
        double countsPerSecond = encoderHost.getVelocity();
        if (encoder != null) {
            encoderAngleDeg = encoder.degrees(counts, zeroCounts);
            encoderRateDps = encoder.degreesPerSecond(countsPerSecond);
        } else {
            encoderAngleDeg = Double.NaN;
            encoderRateDps = Double.NaN;
        }
        if (useAbsoluteZero && !started) {
            readAbsolute();
            if (encoder != null && zeroed) {
                encoderAngleDeg = encoder.degrees(counts, zeroCounts);
            }
        }
        if (lockedForward) {
            angleDeg = 0.0;
            rateDps = 0.0;
        } else if (encoder != null) {
            angleDeg = encoder.degrees(counts, zeroCounts);
            rateDps = encoder.degreesPerSecond(countsPerSecond);
        } else {
            angleDeg = Double.NaN;
            rateDps = Double.NaN;
        }

        double position = Double.NaN;   // NaN: limp
        aimNote = null;
        if (lockedForward) {
            errorDeg = Double.NaN;
            position = positionFor(0.0, homePosition, degreesPerUnit, minPosition, maxPosition);
        } else if (mode == Mode.HOLD) {
            errorDeg = Double.isNaN(angleDeg) ? Double.NaN : targetDeg - angleDeg;
            if (Math.abs(errorDeg) > Math.max(finite(runawayDeg), 1.0)) {
                runawayNote = String.format(Locale.US,
                        "RUNAWAY: %.0f° from target; hold dropped. Flip degreesPerUnit's sign (or direction) and aim again.",
                        errorDeg);
                mode = Mode.IDLE;
            } else {
                position = positionFor(targetDeg, homePosition, degreesPerUnit, minPosition, maxPosition);
                if (Double.isNaN(position)) {
                    aimNote = "degreesPerUnit not measured: holding home, not " + Math.round(targetDeg) + "°";
                    position = homePosition;
                }
            }
        } else {
            errorDeg = Double.NaN;
        }
        if (!started) {
            return;
        }
        if (Double.isNaN(position)) {
            goLimp();
            return;
        }
        if (rehomeFromEncoder && zeroed) {
            double wantDeg = lockedForward ? 0.0 : targetDeg;
            position = rehome.step(position, wantDeg, encoderAngleDeg, encoderRateDps, degreesPerUnit,
                    Math.max(0.0, minPosition), Math.min(1.0, maxPosition), rehomeStillDps, rehomeStillMs,
                    rehomeToleranceDeg, System.nanoTime());
        }
        double out = pwmStep(position);
        if (limp || out != servoOut || resend.due(System.nanoTime())) {
            servo.setPosition(out);
            servoOut = out;
            if (limp && pwm != null) {
                pwm.setPwmEnable();
            }
            limp = false;
        }
    }

    /**
     * The servo position for {@code deg} from home: {@code home + deg / degreesPerUnit}, clamped
     * into the limits {@code [min, max]} (themselves kept inside [0, 1]). 0 degrees is
     * {@code home} whatever {@code degreesPerUnit} is; any other angle needs a measured, non-zero
     * {@code degreesPerUnit}, or the answer is NaN. A {@code home} outside the limits, or limits
     * that are not a range, give NaN too: a bad value goes limp, never past an end.
     */
    static double positionFor(double deg, double home, double degreesPerUnit, double min, double max) {
        double lo = Math.max(0.0, min);
        double hi = Math.min(1.0, max);
        if (Double.isNaN(lo) || Double.isNaN(hi) || lo > hi) {
            return Double.NaN;
        }
        if (Double.isNaN(home) || home < lo || home > hi || Double.isNaN(deg)) {
            return Double.NaN;
        }
        if (deg == 0.0) {
            return home;
        }
        if (Double.isNaN(degreesPerUnit) || Double.isInfinite(degreesPerUnit) || degreesPerUnit == 0.0) {
            return Double.NaN;
        }
        return Math.max(lo, Math.min(hi, home + deg / degreesPerUnit));
    }

    private void goLimp() {
        if (!limp && pwm != null) {
            pwm.setPwmDisable();
        }
        limp = pwm != null;
        servoOut = Double.NaN;
    }

    /**
     * A servo position to a step the PWM can tell apart: positions 0 to 1 span the hub's pulse
     * range (about 2000 µs), so 0.001 is about two microseconds. Finer changes only cost writes.
     */
    static double pwmStep(double position) {
        return Math.round(position * 1000.0) / 1000.0;
    }

    /** Final teardown: pulse off, limp. */
    @Override
    public void stop() {
        if (pwm != null) {
            pwm.setPwmDisable();
        }
        limp = true;
    }

    @Override
    public String health() {
        String abs = useAbsoluteZero
                ? "; absolute zero: " + (absFault != null ? absFault : "OctoQuad answers on I2C")
                : "";
        return turretHealth() + abs;
    }

    private String turretHealth() {
        if (lockedForward) {
            return "locked straight ahead (lockedForward): from PLAY holds servo position "
                    + homePosition + "; presence unknown (a servo reports nothing back)";
        }
        return "servo configured (position mode); encoder read through " + DeviceNames.TURRET_ENCODER_HOST
                + "; zero: " + zeroSource.text
                + (settingsProblem != null ? "; settings: " + settingsProblem : "");
    }

    @Override
    public void describe(Display display) {
        if (lockedForward) {
            boolean good = !Double.isNaN(positionFor(0.0, homePosition, degreesPerUnit, minPosition, maxPosition));
            display.status("Turret", good ? Display.Level.OK : Display.Level.FAULT, good
                    ? String.format(Locale.US, "locked straight ahead (0°): %s position %.3f",
                            started ? "holding" : "holds from PLAY at", homePosition)
                    : "homePosition must be inside minPosition..maxPosition: limp");
            describeAbsolute(display);
            describeRehome(display);
            return;
        }
        if (settingsProblem != null) {
            display.status("Turret", Display.Level.FAULT, settingsProblem);
            return;
        }
        String what;
        Display.Level level;
        switch (mode) {
            case HOLD:
                what = String.format(Locale.US, "%.1f° → %.1f° (%+.1f)", angleDeg, targetDeg, errorDeg);
                level = onTarget() ? Display.Level.OK : Display.Level.WARN;
                break;
            default:
                what = String.format(Locale.US, "%.1f° · idle", angleDeg);
                level = Display.Level.OK;
                break;
        }
        display.status("Turret", level, what + " · "
                + (zeroed ? zeroSource.text : "not zeroed yet: it zeroes where it stands at PLAY, or zero it now"));
        if (runawayNote != null) {
            display.status("Turret", Display.Level.FAULT, runawayNote);
        }
        if (aimNote != null) {
            display.status("Turret", Display.Level.WARN, aimNote);
        }
        describeAbsolute(display);
        describeRehome(display);
    }

    /** The absolute zero's line, when it is on. */
    private void describeAbsolute(Display display) {
        if (!useAbsoluteZero) {
            return;
        }
        if (absFault != null) {
            display.status("Turret abs", Display.Level.FAULT, absFault);
        } else if (started) {
            display.status("Turret abs", Display.Level.OK, "not read after PLAY; the count carries the angle");
        } else if (absSolution == null) {
            display.status("Turret abs", Display.Level.WARN, "no reading yet");
        } else if (!absSolution.ok()) {
            display.status("Turret abs", Display.Level.WARN, String.format(Locale.US,
                    "%s · pulses A %d B %d µs", absSolution.problem, absPulseA, absPulseB));
        } else {
            display.status("Turret abs", Display.Level.OK, String.format(Locale.US,
                    "%.1f° · residual %.1f° · margin %.0f° · vs count %+.1f°",
                    absSolution.turretDeg, absSolution.residualDeg, absSolution.marginDeg, absCheckDeg));
        }
    }

    /** The encoder correction's line, when it is on. */
    private void describeRehome(Display display) {
        if (!rehomeFromEncoder) {
            return;
        }
        if (Double.isNaN(degreesPerUnit) || degreesPerUnit == 0.0) {
            display.status("Turret re-home", Display.Level.WARN, "off: degreesPerUnit not measured");
        } else if (rehome.outOfReach()) {
            display.status("Turret re-home", Display.Level.FAULT, String.format(Locale.US,
                    "out of reach: encoder %.0f°, turret at a limit. Stop, turn the turret one full turn by hand, put it on its mark, run again",
                    encoderAngleDeg));
        } else {
            display.status("Turret re-home", Display.Level.OK, String.format(Locale.US,
                    "encoder %.1f° · offset %+.3f · %d corrections%s", encoderAngleDeg, rehome.offset(),
                    rehome.corrections(), zeroed ? "" : " · waits for the zero at PLAY"));
        }
    }

    /** Why the last hold was dropped, or null. Shown by the OpMode too. */
    public String runawayNote() {
        return runawayNote;
    }

    // ------------------------------------------------------------ helpers

    /** Rebuilds the encoder's converter only when its two Panels inputs change. */
    private void refreshSettings() {
        if (encoderTurnsSetting == turnsPerTurretTurn && directionSetting == direction && encoder != null) {
            return;
        }
        encoderTurnsSetting = turnsPerTurretTurn;
        directionSetting = direction;
        try {
            encoder = new TurretEncoder(turnsPerTurretTurn, direction);
            settingsProblem = null;
        } catch (IllegalArgumentException e) {
            encoder = null;
            settingsProblem = "bad settings: " + e.getMessage();
        }
    }

    private static double finite(double value) {
        return Double.isNaN(value) || Double.isInfinite(value) ? 0.0 : value;
    }
}
