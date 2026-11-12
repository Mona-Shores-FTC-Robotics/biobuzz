package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.teamcode.controls.Display;

import java.util.Locale;
import java.util.function.BooleanSupplier;
import org.firstinspires.ftc.teamcode.hardware.DeviceNames;
import org.firstinspires.ftc.teamcode.util.Throttle;

/**
 * The intake roller: one motor, run in, run out, or idle. The first edition, enough to spin the
 * roller from a gamepad so the build team can test the mechanism (#160).
 *
 * <p>Same shape as {@link FlowerStickSubsystem}: the OpMode says what it wants ({@link #intake()},
 * {@link #eject()}, {@link #idle()}), {@link #update()} does it, once per loop. Which button does
 * what belongs in the OpMode's bindings, not here.
 *
 * <p>It reads the
 * roller's speed once per loop and its current four times a second ({@link #ticksPerSecond()},
 * {@link #currentAmps()}),
 * which {@code RobotOpMode} logs and the Match page shows: slipping is high speed at low current,
 * stalling is low speed at high current.
 *
 * <p>The powers and the direction are {@code @Configurable}. Nobody can tell from a desk which way
 * the roller is wired: if it spits pieces out on "intake", flip {@link #reversed} in Panels and
 * write {@code true} back here with a note saying which robot it was checked on.
 *
 * <h2>The star wheels</h2>
 *
 * <p>The intake-assist wheels (#160): SWYFT wheels cut into stars, driven by a continuous-rotation
 * servo on {@link DeviceNames#INTAKE_STARS}, with one-way clutch bearings so a ball can overrun
 * them inward. They run at {@link #starsPower} whenever the roller is taking in, backwards at the
 * same power while it ejects (asked for on 26 Oct 2026, so the stars push a piece out with the
 * roller rather than sit in its way), and stop when the roller is idle. Their direction is
 * {@link #starsReversed}, unknown until a robot has shown it.
 *
 * <h2>Running for a feed</h2>
 *
 * <p>With four pieces loaded the last one sits at the roller and the stars, so a feed that moved
 * only the feeder, the prefeed and the gate left it behind (20245, 1 Nov 2026). So the intake
 * follows the feeder's request every {@link #update()}, as {@code PrefeedSubsystem} does: while
 * the feeder feeds, the stars turn inward, and so does the roller unless
 * {@link #rollerWhileFeeding} is off; while it reverses, both back out. {@link #rollerFor} and
 * {@link #starsFor} are the whole policy, pure, so a test can check them, and the TeleOp, the bench
 * and every Auto get it with no OpMode change. The OpMode's own request still wins where it says
 * more: eject (unjam) backs everything out whatever the feeder does, and a driver holding intake
 * while firing is unchanged. {@link #feedAssist()} says when the intake runs only because of a
 * feed, for the match log.
 *
 * <h2>Holding the last piece when the lane is full</h2>
 *
 * <p>When the sensor says full, capture the roller encoder position once and actively hold it
 * with RUN_TO_POSITION. The stars continue inward to retain the last piece. Feeding,
 * ejecting and rejection release the hold before driving normally. The prefeed also stops
 * ({@code PrefeedSubsystem.stopWhenFull}). Holding starts only after PLAY.
 *
 * <h2>Spitting out the other alliance's NECTAR</h2>
 *
 * <p>While the colour sensor says so ({@code ColorSensorSubsystem.rejecting()}, fed to this class by
 * {@link #rejectWhile}), the roller and the stars back out at
 * {@code ColorSensorSubsystem.rejectRollerPower} and {@link #starsPower}, over whatever was asked, the full hold and a feed included: the piece at the
 * entrance is the other alliance's NECTAR, and holding one is a big penalty. The prefeed follows
 * the stars out ({@code PrefeedSubsystem.requestFor}), so the whole lane backs out, as an eject
 * does: losing a POLLEN costs far less than keeping the NECTAR (mentor, 5 Nov 2026; holding the
 * prefeed still left a NECTAR that had got past the stars in the lane). The OpMode's own eject and
 * the feeder reversing back everything out anyway, so they are unchanged.
 * {@link #rejecting()} says when it is happening. After each burst the roller and the stars stay
 * stopped for a hold-off ({@link #holdOffWhile}, {@link #holdingOff()}) over a request to take in
 * and the full hold, so the piece rolls clear rather than being taken straight back in; a feed
 * still runs through it.
 *
 * <p>The star servo is not written before PLAY: a continuous servo told "stop" creeps if its stop
 * is not quite centred, and nothing may move after INIT (G304.H, G403; see {@link Subsystem}).
 */
@Configurable
public class IntakeSubsystem implements Subsystem {

    /** What the roller is being asked to do. */
    public enum State { IDLE, IN, OUT }

    /** Motor power while taking pieces in, 0 to 1. <b>Not yet tuned.</b> */
    public static double inPower = 1.0;

    /** Motor power while pushing pieces back out, 0 to 1. <b>Not yet tuned.</b> */
    public static double outPower = 0.6;

    /**
     * Flips the motor so that positive power takes pieces in. {@code true} as checked on 20245 on
     * 26 Oct 2026: with the SDK's default direction R2 ran the roller backwards. 19429 is wired
     * the same way by intent and has not been checked yet. (Set to {@code false} on 4 Nov 2026 by
     * a mix-up, when it was the star wheels that were turned round; put back the same day.)
     *
     * <p>It sets the motor's direction ({@link #directionFor}), which the SDK applies to the power
     * and the encoder alike, so taking pieces in always reads as a positive speed in the log and in
     * Panels, and pushing them out as a negative one. Since 1 Nov 2026: it used to negate the power
     * only, so on 20245 a correct intake read about -2760 ticks/s. Read every loop, so a Panels
     * edit applies at once.
     */
    public static boolean reversed = true;

    /**
     * Encoder ticks per revolution of the roller motor's output shaft, for {@link #rpm()}. <b>NaN
     * until read off the motor's label:</b> a goBILDA 5203 counts 28 per motor revolution, so
     * 1150 RPM = 145.1, 435 RPM = 384.5, 312 RPM = 537.7, 223 RPM = 751.8, 117 RPM = 1425.1. Speed
     * is shown in ticks/s regardless; RPM only once this is set.
     */
    public static double ticksPerRev = Double.NaN;

    /** Star-wheel servo power while taking pieces in, 0 to 1. <b>Not yet tuned.</b> */
    public static double starsPower = 1.0;

    /**
     * Flips the star-wheel servo so that positive power turns the stars inward. {@code true} since
     * 4 Nov 2026: the builders turned the star-wheel servo round on both robots, so the SDK's
     * default direction now turns the stars outward. <b>Not yet checked on a robot since.</b>
     */
    public static boolean starsReversed = true;

    /**
     * Whether the roller runs inward while the feeder feeds, to push the last piece up from the
     * intake. The stars always do. Off if the roller pulls loose pieces in while firing; the
     * mentor's switch for the lab, read every loop so a Panels edit applies at once.
     */
    public static boolean rollerWhileFeeding = true;

    /** Enable the full-lane hold, whether intake is requested or idle. */
    public static boolean holdWhenFull = true;

    /**
     * Panels switch: active encoder hold on 20245; RobotDifferences defaults this off on 19429
     * while its encoder wire is unplugged. Off restores holdPowerWhenFull in normal power mode.
     * Read every loop, so changing it releases or engages position hold immediately after PLAY.
     */
    public static boolean positionHoldWhenFull = true;

    /** Maximum correction effort, not a continuous intake speed. Initial value needs robot confirmation. */
    public static double positionHoldMaxPower = 0.3;

    /** Legacy inward roller power, used only with positionHoldWhenFull off. */
    public static double holdPowerWhenFull = 0.0;

    /** Star-wheel power inward during a full hold; preserves the existing retention behavior. */
    public static double starsPowerWhenFull = 1.0;

    /**
     * Back the roller and stars out for a moment when the roller is wedged ({@link WedgeClear}):
     * stalled at high current while taking in, with nothing under the lane sensor. On since
     * 10 Nov 2026, from the Belding logs; <b>not run on a robot</b>. Off, a wedge holds until the
     * driver ejects.
     */
    public static boolean clearWedge = true;

    /** Below this roller speed, ticks/s, the roller is not turning. It runs at about 2400; Belding's stalls read under 50. */
    public static double wedgeStallTicksPerSecond = 100.0;

    /** At or above this roller current, amps, a roller that is not turning is held. Belding's stalls drew 7 to 10 A; running, under 3.4 A nine times in ten. */
    public static double wedgeStallAmps = 5.0;

    /** A stall this long without a break is a wedge, ms. Belding's stalls that let go by themselves lasted under 0.8 s. */
    public static double wedgeStallMs = 1000.0;

    /** How long a wedge clear backs out, ms. A guess, <b>not measured</b>. Clamped to [0, 1000]. */
    public static double wedgeBackMs = 250.0;

    /** Bursts in a row, with the roller never running free between them, before it stops trying. */
    public static int wedgeMaxBursts = 3;

    private boolean positionHolding;
    private int holdTargetTicks;
    private int positionTicks;

    private final DcMotorEx motor;
    private final CRServo stars;
    private final FeederSubsystem feeder;
    /** What the OpMode asked for. */
    private State request = State.IDLE;
    /** What the roller and the stars are doing as of the last update: the request, or the feeder's. */
    private State roller = State.IDLE;
    private State starsState = State.IDLE;
    /**
     * Roller and star powers for the OpMode's current eject only ({@link #eject(double, double)});
     * NaN uses {@link #outPower} and {@link #starsPower}. Any other request clears them.
     */
    private double ejectRollerPower = Double.NaN;
    private double ejectStarsPower = Double.NaN;
    private boolean feedAssist;
    private boolean holdingForFull;
    private boolean rejecting;
    /** Whether to spit out the piece at the entrance; never until {@link #rejectWhile} is called. */
    private BooleanSupplier reject = () -> false;
    private boolean holdingOff;
    private boolean unwedging;
    private final WedgeClear wedge = new WedgeClear();
    /** Whether a stall is the lane backed up, not a wedge; never until {@link #laneBackedUp} is called, so no wedge is cleared. */
    private BooleanSupplier laneBackedUp = () -> true;
    /** Whether a reject's hold-off is running; never until {@link #holdOffWhile} is called. */
    private BooleanSupplier holdOff = () -> false;
    /** Whether the lane is full; never until {@link #laneFull} is called, so the roller never stops for it. */
    private BooleanSupplier laneFull = () -> false;
    private boolean started;
    private double ticksPerSecond = 0.0;
    /** The direction last sent to the motor, so {@link #reversed} is written only when it changes. */
    private DcMotorSimple.Direction appliedDirection;
    private double currentAmps = Double.NaN;
    /**
     * How often a motor current that is only shown and logged is read, ms: the intake's, the
     * feeder's and each launcher wheel's. Current is not in the bulk read: each read is its own
     * round trip to the Expansion Hub, 2–4 ms in 20245's logs. At 100 ms, four of them (the intake,
     * the feeder, two wheels) were about 120 ms of every second with everything running, some 3 ms
     * a loop; at 250 ms it is a third of that, and four samples a second still show a jam or a
     * stall, which last far longer. Each reader has its own offset in the period, so no two land in
     * the same loop. A decision taken from current reads it every loop instead
     * ({@link #readCurrentEveryLoop}).
     */
    static final double CURRENT_READ_MS = 250;

    /** Staggered against the feeder's and the launcher wheels' ({@link #CURRENT_READ_MS}). */
    private final Throttle currentRead = new Throttle(CURRENT_READ_MS, 0);
    /** A grab is timing its dwell from the current: read it every loop while it runs. */
    private boolean currentEveryLoop;

    /**
     * Looks the motor and the star-wheel servo up once. A missing device fails init with the SDK's own message.
     *
     * @param feeder read for its request, which the intake joins; updated before this subsystem
     */
    public IntakeSubsystem(HardwareMap hardwareMap, FeederSubsystem feeder) {
        motor = hardwareMap.get(DcMotorEx.class, DeviceNames.INTAKE);
        stars = hardwareMap.get(CRServo.class, DeviceNames.INTAKE_STARS);
        this.feeder = feeder;
    }

    /**
     * Where to read whether the lane is full, for {@link #holdWhenFull}. Wired once by
     * {@code Robot}, which builds the colour sensor after this subsystem.
     */
    public void laneFull(BooleanSupplier full) {
        laneFull = full;
    }

    /**
     * Where to read whether to spit out the piece at the entrance (the other alliance's NECTAR).
     * Wired once by {@code Robot} to the colour sensor, which updates after this subsystem, so a
     * burst starts one loop after the reading that called for it.
     */
    public void rejectWhile(BooleanSupplier rejecting) {
        reject = rejecting;
    }

    /**
     * Where to read whether a stalled roller is only pressing on a backed-up lane: a piece under
     * the lane sensor, or a sensor that is not answering (then nothing can tell, and no wedge is
     * cleared). Wired once by {@code Robot}, as {@link #laneFull}.
     */
    public void laneBackedUp(BooleanSupplier backedUp) {
        laneBackedUp = backedUp;
    }

    /** Where to read whether a reject's hold-off is running. Wired once by {@code Robot}, as {@link #rejectWhile}. */
    public void holdOffWhile(BooleanSupplier holdingOff) {
        holdOff = holdingOff;
    }

    /** Ask for pieces in. Takes effect in the next {@link #update()}. */
    public void intake() {
        request = State.IN;
        clearEjectPowers();
    }

    /** Ask for pieces out. Takes effect in the next {@link #update()}. */
    public void eject() {
        request = State.OUT;
        clearEjectPowers();
    }

    /**
     * Ask for pieces out at these powers (0 to 1) instead of {@link #outPower} and
     * {@link #starsPower}, for this request only: a gentle eject that sets pieces down rather than
     * throwing them ({@code Bench: Wall Eject}). The feeder reversing still backs out at the
     * configured powers when the OpMode asks for nothing.
     */
    public void eject(double rollerPower, double starsPower) {
        request = State.OUT;
        ejectRollerPower = rollerPower;
        ejectStarsPower = starsPower;
    }

    /** Ask for the roller to stop. Not {@link #stop()}: the subsystem keeps running. */
    public void idle() {
        request = State.IDLE;
        clearEjectPowers();
    }

    private void clearEjectPowers() {
        ejectRollerPower = Double.NaN;
        ejectStarsPower = Double.NaN;
    }

    /**
     * What the roller is doing as of the last {@link #update()}: the OpMode's request, or the
     * feeder's where the request said nothing ({@link #rollerFor}). What was commanded, not a
     * measurement.
     */
    public State state() {
        return roller;
    }

    /** What the OpMode asked for, before the feeder had its say. */
    public State requested() {
        return request;
    }

    /** Whether active encoder position holding is engaged. */
    public boolean positionHolding() { return positionHolding; }

    /** Fixed target for this hold, encoder ticks. */
    public int holdTargetTicks() { return holdTargetTicks; }

    /** Target minus measured encoder position from the latest update, ticks. */
    public int holdErrorTicks() { return holdTargetTicks - positionTicks; }

    /** What the star wheels are doing as of the last {@link #update()}; inward by default while full. */
    public State starsState() {
        return starsState;
    }

    /** Whether the intake is holding the last piece because the lane is full ({@link #holdWhenFull}). */
    public boolean holdingForFull() {
        return holdingForFull;
    }

    /** Whether the roller and the stars are spitting out the other alliance's NECTAR (class comment). */
    public boolean rejecting() {
        return rejecting;
    }

    /** Whether the roller and the stars are stopped after a reject so the piece rolls clear (class comment). */
    public boolean holdingOff() {
        return holdingOff;
    }

    /**
     * Whether the roller and the stars are spitting out a piece, or stopped after one (reject's
     * whole time), or backing out of a wedge: the roller's current is not a piece coming in.
     */
    public boolean rejectActive() {
        return rejecting || holdingOff || unwedging;
    }

    /** Whether the roller and the stars are backing out of a wedge this loop ({@link WedgeClear}). */
    public boolean unwedging() {
        return unwedging;
    }

    /** Wedge clears since this OpMode started. */
    public int wedgeClears() {
        return wedge.bursts();
    }

    /** Whether the roller or the stars run only because the feeder is feeding or reversing. */
    public boolean feedAssist() {
        return feedAssist;
    }

    /** Roller motor speed from the last {@link #update()}, encoder ticks per second. 0 without an encoder. */
    public double ticksPerSecond() {
        return ticksPerSecond;
    }

    /** {@link #ticksPerSecond()} in output-shaft RPM, or NaN until {@link #ticksPerRev} is set. */
    public double rpm() {
        return rpm(ticksPerSecond, ticksPerRev);
    }

    /**
     * Roller motor current from the last {@link #update()}, amps. NaN while idle: the hub reads
     * current outside the bulk read, so it is only asked for while the roller is doing something.
     */
    public double currentAmps() {
        return currentAmps;
    }

    /** Start idle, in plain power mode, the roller at zero and the star servo unwritten until PLAY. */
    @Override
    public void initialize() {
        request = State.IDLE;
        roller = State.IDLE;
        starsState = State.IDLE;
        feedAssist = false;
        clearEjectPowers();
        holdingForFull = false;
        rejecting = false;
        holdingOff = false;
        started = false;
        positionHolding = false;
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        applyDirection();
        motor.setPower(0.0);
    }

    /** Sends {@link #reversed} to the motor when it differs from what the motor last got. */
    private void applyDirection() {
        DcMotorSimple.Direction wanted = directionFor(reversed);
        if (wanted != appliedDirection) {
            // Direction changes invert encoder coordinates; release and recapture any hold.
            releasePositionHold();
            motor.setDirection(wanted);
            appliedDirection = wanted;
        }
    }
    /** PLAY: the star servo may be written from now on. */
    @Override
    public void start() {
        started = true;
    }


    /**
     * Decide what the roller and the stars do from the request and the feeder's ({@link #rollerFor},
     * {@link #starsFor}); then one write per loop to the roller and, from PLAY, one to the star
     * wheels, clamped so Panels cannot overdrive them; then one read of speed (from the bulk read)
     * and, while running, one of current every {@link #CURRENT_READ_MS}. Before PLAY the roller gets zero and the
     * stars nothing, whatever is asked (see {@link Subsystem}).
     */
    @Override
    public void update() {
        applyDirection();
        FeederSubsystem.State feed = feeder.state();
        roller = started ? rollerFor(request, feed, rollerWhileFeeding) : State.IDLE;
        starsState = started ? starsFor(request, feed) : State.IDLE;
        holdingForFull = started && holdsForFull(request, feed, laneFull.getAsBoolean(), holdWhenFull);
        if (holdingForFull) {
            roller = State.IN;
            starsState = unitRange(starsPowerWhenFull) > 0 ? State.IN : State.IDLE;
        }
        rejecting = started && rejects(request, feed, reject.getAsBoolean());
        if (rejecting) {
            holdingForFull = false;
            roller = State.OUT;
            starsState = State.OUT;
        }
        holdingOff = started && !rejecting && holdsOff(request, feed, holdOff.getAsBoolean());
        if (holdingOff) {
            holdingForFull = false;
            roller = State.IDLE;
            starsState = State.IDLE;
        }
        // A wedge: the OpMode's own intake, stalled, with the lane not backed up. The speed and
        // current are the last loop's.
        boolean takingIn = started && clearWedge && request == State.IN && roller == State.IN
                && !holdingForFull && !rejecting && !holdingOff && !laneBackedUp.getAsBoolean();
        unwedging = wedge.update(takingIn, ticksPerSecond, currentAmps, System.nanoTime(),
                wedgeStallTicksPerSecond, wedgeStallAmps, wedgeStallMs,
                Math.min(Math.max(wedgeBackMs, 0.0), 1000.0), wedgeMaxBursts);
        if (unwedging) {
            roller = State.OUT;
            starsState = State.OUT;
        }
        feedAssist = !holdingForFull && !rejecting && !holdingOff && request == State.IDLE && (roller != State.IDLE || starsState != State.IDLE);
        boolean ownEject = request == State.OUT;
        positionTicks = motor.getCurrentPosition();
        if (holdingForFull && positionHoldWhenFull) {
            if (!positionHolding) {
                motor.setPower(0.0);
                holdTargetTicks = positionTicks;
                motor.setTargetPosition(holdTargetTicks);
                motor.setTargetPositionTolerance(2);
                motor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                positionHolding = true;
            }
            // Do not use isBusy(): a hold must continue correcting after reaching its target.
            motor.setPower(unitRange(positionHoldMaxPower));
        } else {
            releasePositionHold();
            motor.setPower(powerFor(roller, holdingForFull ? holdPowerWhenFull : inPower,
                    rejecting ? ColorSensorSubsystem.rejectRollerPower
                            : ownEject ? orConfigured(ejectRollerPower, outPower) : outPower));
        }
        if (started) {
            stars.setPower(starsPowerFor(starsState,
                    holdingForFull ? starsPowerWhenFull
                            : ownEject && starsState == State.OUT ? orConfigured(ejectStarsPower, starsPower) : starsPower,
                    starsReversed));
        }
        ticksPerSecond = motor.getVelocity();
        if (roller == State.IDLE) {
            currentAmps = Double.NaN;
            currentRead.reset();
        } else if (currentEveryLoop || currentRead.due(System.nanoTime())) {
            currentAmps = motor.getCurrent(CurrentUnit.AMPS);
        }
    }

    /** Remove correction power before restoring normal power control. */
    private void releasePositionHold() {
        if (positionHolding) {
            motor.setPower(0.0);
            motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            positionHolding = false;
        }
    }

    /**
     * Read the current every loop rather than every {@link #CURRENT_READ_MS}: a FLOWER pick ending its dwell from
     * the current needs each piece's spike on time. A 2–3 ms read, for the seconds a grab runs.
     */
    public void readCurrentEveryLoop(boolean on) {
        currentEveryLoop = on;
    }

    /** Cut power. Safe to call whether or not {@link #initialize()} ran. */
    @Override
    public void stop() {
        request = State.IDLE;
        clearEjectPowers();
        roller = State.IDLE;
        starsState = State.IDLE;
        feedAssist = false;
        rejecting = false;
        holdingOff = false;
        holdingForFull = false;
        started = false;
        releasePositionHold();
        motor.setPower(0.0);
        stars.setPower(0.0);
    }

    @Override
    public String health() {
        return "roller motor and star servo configured; presence unknown until they move";
    }

    @Override
    public void describe(Display display) {
        boolean moving = roller != State.IDLE || starsState != State.IDLE;
        String what = unwedging ? "backing out of a wedge"
                : rejecting ? "spitting out the other alliance's NECTAR"
                : holdingOff ? "stopped: letting the rejected NECTAR roll clear"
                : holdingForFull ? String.format(Locale.US,
                "holding the last piece (lane full) · %s · stars %.2f",
                positionHolding ? String.format(Locale.US, "position %d, error %d ticks, max %.2f",
                        holdTargetTicks, holdTargetTicks - positionTicks, unitRange(positionHoldMaxPower))
                        : String.format(Locale.US, "roller power %.2f", unitRange(holdPowerWhenFull)),
                unitRange(starsPowerWhenFull))
                : roller == State.IN ? "IN · stars turning"
                : roller == State.OUT ? "OUT · stars backing"
                : starsState == State.IN ? "roller idle · stars turning"
                : starsState == State.OUT ? "roller idle · stars backing"
                : "idle";
        display.status("Intake", moving ? Display.Level.WARN : Display.Level.OK,
                what + (feedAssist ? " (for the feed)" : ""));
        double rpm = rpm();
        display.line(String.format(Locale.US, "Roller %.0f ticks/s%s · %s",
                ticksPerSecond,
                Double.isNaN(rpm) ? "" : String.format(Locale.US, " (%.0f rpm)", rpm),
                Double.isNaN(currentAmps) ? "current not read while idle"
                        : String.format(Locale.US, "%.2f A", currentAmps)));
    }

    /** {@code override} unless it is NaN, then {@code configured}. Pure, so a test can check it. */
    static double orConfigured(double override, double configured) {
        return Double.isNaN(override) ? configured : override;
    }

    /** Ticks per second to RPM, or NaN when ticks per revolution is unset (NaN or not positive). */
    static double rpm(double ticksPerSecond, double ticksPerRev) {
        return ticksPerRev > 0.0 ? ticksPerSecond / ticksPerRev * 60.0 : Double.NaN;
    }

    /**
     * The motor's direction for {@link #reversed}: the SDK flips the power and the encoder together,
     * so a correct intake reads positive either way. Pure, so a test can check it.
     */
    static DcMotorSimple.Direction directionFor(boolean reversed) {
        return reversed ? DcMotorSimple.Direction.REVERSE : DcMotorSimple.Direction.FORWARD;
    }

    /**
     * The roller's state: the OpMode's request, or the feeder's where the request says nothing.
     * Backing out wins (eject, or the feeder reversing to clear a jam); then in, if asked or if the
     * feeder is feeding and {@code rollerWhileFeeding}; else idle. Pure, so a test can check it.
     */
    static State rollerFor(State request, FeederSubsystem.State feeder, boolean rollerWhileFeeding) {
        if (request == State.OUT || feeder == FeederSubsystem.State.REVERSE) return State.OUT;
        if (request == State.IN || (feeder == FeederSubsystem.State.FEED && rollerWhileFeeding)) return State.IN;
        return State.IDLE;
    }

    /**
     * Whether a full lane holds the last piece (position hold by default):
     * {@code enabled}, the lane full, the OpMode not ejecting, and the feeder neither feeding nor
     * reversing. Taking in or idle alike. Pure, so a test can check it.
     */
    static boolean holdsForFull(State request, FeederSubsystem.State feeder, boolean laneFull, boolean enabled) {
        return enabled && laneFull && request != State.OUT
                && feeder != FeederSubsystem.State.FEED && feeder != FeederSubsystem.State.REVERSE;
    }

    /**
     * Whether to spit out the piece at the entrance: the colour sensor says so, and nothing is
     * already backing the whole lane out (the OpMode's eject, the feeder reversing). Over a request
     * to take in, the full hold, and a feed alike. Pure, so a test can check it.
     */
    static boolean rejects(State request, FeederSubsystem.State feeder, boolean sensorSaysReject) {
        return sensorSaysReject && request != State.OUT && feeder != FeederSubsystem.State.REVERSE;
    }

    /**
     * Whether a reject's hold-off stops the roller and the stars: the colour sensor says so, and
     * nothing else moves pieces (no eject, no feed, no unjam), so it stops a request to take in
     * and the full hold only. Pure, so a test can check it.
     */
    static boolean holdsOff(State request, FeederSubsystem.State feeder, boolean sensorSaysHoldOff) {
        return sensorSaysHoldOff && request != State.OUT && feeder == FeederSubsystem.State.IDLE;
    }

    /** The star wheels' state: as {@link #rollerFor}, but a feed always turns them. */
    static State starsFor(State request, FeederSubsystem.State feeder) {
        return rollerFor(request, feeder, true);
    }

    /**
     * The motor power for a request, in the intake's own sense (the motor's direction carries
     * {@link #reversed}): {@code +in} for IN, {@code -out} for OUT, 0 when idle, each magnitude
     * clamped to [0, 1]. Pure, so a test can check it.
     */
    static double powerFor(State state, double in, double out) {
        double power;
        switch (state) {
            case IN:
                power = unitRange(in);
                break;
            case OUT:
                power = -unitRange(out);
                break;
            default:
                power = 0.0;
        }
        return power;
    }

    /**
     * The star-wheel servo's power for a request: {@code +power} while taking in, {@code -power}
     * while ejecting, 0 when idle. Negated when {@code starsReversed}, except that stopped is always
     * a plain 0, never -0. Pure, so a test can check it.
     */
    static double starsPowerFor(State state, double power, boolean reversed) {
        double p = state == State.IN ? unitRange(power) : state == State.OUT ? -unitRange(power) : 0.0;
        if (p == 0.0) return 0.0;
        return reversed ? -p : p;
    }

    /**
     * Clamps a Panels-editable power to [0, 1], and treats NaN as 0. A typo in Panels should give
     * a roller that is too slow, never one commanded past full power or sent NaN.
     */
    static double unitRange(double value) {
        return value > 0.0 ? Math.min(value, 1.0) : 0.0;
    }
}
