package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.teamcode.controls.Display;

import java.util.Locale;
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
 * <p>What this does not do yet: count pieces or stop at four. What it does do for #181 is read the
 * roller's speed once per loop and its current ten times a second ({@link #ticksPerSecond()},
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
 * <h2>The beam break</h2>
 *
 * <p>An IR break-beam on {@link DeviceNames#INTAKE_BEAM} crosses the place the fourth piece sits.
 * A piece rolling past breaks it for a moment; a fourth piece held there breaks it for good. So
 * the robot is full ({@link #isFull()}) once the beam has stayed broken for {@link #fullAfterMs},
 * and stops being full the moment it clears. That is the Auto's {@code IntakeFull}. It is read
 * once per loop from the bulk read. Which level means "broken" depends on the sensor's wiring:
 * {@link #beamBrokenWhenLow}, unknown until a robot has shown it.
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
     * the same way by intent and has not been checked yet.
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
     * Flips the star-wheel servo so that positive power turns the stars inward. <b>Not yet checked
     * on a robot:</b> {@code false} is the SDK's default direction, not a fact about the wiring.
     */
    public static boolean starsReversed = false;

    /**
     * How long the beam must stay broken before the robot counts as full, in ms, so a piece rolling
     * past is not a full robot. <b>Not yet tuned:</b> a match log's {@code /Intake/BeamBroken}
     * shows how long a passing piece breaks it.
     */
    public static double fullAfterMs = 150.0;

    /**
     * True when the beam break's input reads low while the beam is broken, as an open-collector IR
     * receiver does. <b>Not yet checked on a robot:</b> block the beam by hand and watch the Robot
     * page; flip this in Panels if it says the opposite, then write it back here with a note.
     */
    public static boolean beamBrokenWhenLow = true;

    private final DcMotorEx motor;
    private final CRServo stars;
    private final DigitalChannel beam;
    private boolean beamBroken;
    /** When the beam last went from clear to broken, System.nanoTime(); meaningless while clear. */
    private long brokenSinceNs;
    private boolean full;
    private State state = State.IDLE;
    private double ticksPerSecond = 0.0;
    private double currentAmps = Double.NaN;
    /**
     * Current is not in the bulk read: each read is its own round trip to the hub, 2–3 ms. It is
     * only shown and logged, so ten a second will do; staggered against the feeder and launcher.
     */
    private final Throttle currentRead = new Throttle(100, 0);
    /** A grab is timing its dwell from the current: read it every loop while it runs. */
    private boolean currentEveryLoop;

    /** Looks the motor and the star-wheel servo up once. A missing device fails init with the SDK's own message. */
    public IntakeSubsystem(HardwareMap hardwareMap) {
        motor = hardwareMap.get(DcMotorEx.class, DeviceNames.INTAKE);
        stars = hardwareMap.get(CRServo.class, DeviceNames.INTAKE_STARS);
        beam = hardwareMap.get(DigitalChannel.class, DeviceNames.INTAKE_BEAM);
    }

    /** Ask for pieces in. Takes effect in the next {@link #update()}. */
    public void intake() {
        state = State.IN;
    }

    /** Ask for pieces out. Takes effect in the next {@link #update()}. */
    public void eject() {
        state = State.OUT;
    }

    /** Ask for the roller to stop. Not {@link #stop()}: the subsystem keeps running. */
    public void idle() {
        state = State.IDLE;
    }

    /** What the roller has been asked to do. The request, not a measurement. */
    public State state() {
        return state;
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

    /** Whether the beam break was broken at the last {@link #update()}. */
    public boolean beamBroken() {
        return beamBroken;
    }

    /**
     * Whether the robot is full: the beam has stayed broken for {@link #fullAfterMs}. The Auto's
     * {@code IntakeFull}. Read-only, from the last {@link #update()}.
     */
    public boolean isFull() {
        return full;
    }

    /** Start idle, in plain power mode. */
    @Override
    public void initialize() {
        state = State.IDLE;
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motor.setPower(0.0);
        stars.setPower(0.0);
        beam.setMode(DigitalChannel.Mode.INPUT);
        beamBroken = false;
        full = false;
    }

    /**
     * One write per loop to the roller and one to the star wheels, the powers the request asks for,
     * clamped so Panels cannot overdrive them; then one read of speed (from the bulk read) and,
     * while running, one of current every 100 ms.
     */
    @Override
    public void update() {
        motor.setPower(powerFor(state, inPower, outPower, reversed));
        stars.setPower(starsPowerFor(state, starsPower, starsReversed));
        ticksPerSecond = motor.getVelocity();
        long now = System.nanoTime();
        boolean broken = beam.getState() != beamBrokenWhenLow;
        if (broken && !beamBroken) brokenSinceNs = now;
        beamBroken = broken;
        full = isFull(broken, (now - brokenSinceNs) / 1e6, fullAfterMs);
        if (state == State.IDLE) {
            currentAmps = Double.NaN;
            currentRead.reset();
        } else if (currentEveryLoop || currentRead.due(System.nanoTime())) {
            currentAmps = motor.getCurrent(CurrentUnit.AMPS);
        }
    }

    /**
     * Read the current every loop rather than every 100 ms: a FLOWER pick ending its dwell from
     * the current needs each piece's spike on time. A 2–3 ms read, for the seconds a grab runs.
     */
    public void readCurrentEveryLoop(boolean on) {
        currentEveryLoop = on;
    }

    /** Cut power. Safe to call whether or not {@link #initialize()} ran. */
    @Override
    public void stop() {
        state = State.IDLE;
        motor.setPower(0.0);
        stars.setPower(0.0);
    }

    @Override
    public String health() {
        return "roller motor, star servo and beam break configured; presence unknown until they move"
                + " or the beam is blocked";
    }

    @Override
    public void describe(Display display) {
        display.status("Intake", state == State.IDLE ? Display.Level.OK : Display.Level.WARN,
                state == State.IN ? "IN · stars turning" : state == State.OUT ? "OUT · stars backing" : "idle");
        double rpm = rpm();
        display.line(String.format(Locale.US, "Roller %.0f ticks/s%s · %s",
                ticksPerSecond,
                Double.isNaN(rpm) ? "" : String.format(Locale.US, " (%.0f rpm)", rpm),
                Double.isNaN(currentAmps) ? "current not read while idle"
                        : String.format(Locale.US, "%.2f A", currentAmps)));
        display.line("Beam " + (beamBroken ? "broken" : "clear") + (full ? " · FULL" : ""));
    }

    /**
     * Full when the beam is broken and has been for at least {@code afterMs} (NaN or negative
     * counts as 0). Pure, so a test can check it.
     */
    static boolean isFull(boolean broken, double brokenForMs, double afterMs) {
        return broken && brokenForMs >= (afterMs > 0.0 ? afterMs : 0.0);
    }

    /** Ticks per second to RPM, or NaN when ticks per revolution is unset (NaN or not positive). */
    static double rpm(double ticksPerSecond, double ticksPerRev) {
        return ticksPerRev > 0.0 ? ticksPerSecond / ticksPerRev * 60.0 : Double.NaN;
    }

    /**
     * The motor power for a request: {@code +in} for IN, {@code -out} for OUT, 0 when idle, each
     * magnitude clamped to [0, 1] and negated when {@code reversed}. Pure, so a test can check it.
     */
    static double powerFor(State state, double in, double out, boolean reversed) {
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
        return reversed ? -power : power;
    }

    /**
     * The star-wheel servo's power for a request: {@code +power} while taking in, {@code -power}
     * while ejecting, 0 when idle. Negated when {@code starsReversed}. Pure, so a test can check it.
     */
    static double starsPowerFor(State state, double power, boolean reversed) {
        double p = state == State.IN ? unitRange(power) : state == State.OUT ? -unitRange(power) : 0.0;
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
