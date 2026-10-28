package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PwmControl;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.hardware.DeviceNames;

/**
 * The FLOWER stick: one servo swinging a stick, the FLOWER extractor's first edition (#177).
 *
 * <p>The smallest real subsystem in the repo, and the pattern every mechanism follows: the OpMode
 * says what it wants ({@link #extend()}, {@link #retract()}, {@link #toggle()}), {@link #update()}
 * does it, once per loop. Which button triggers it belongs in the OpMode's bindings, not here.
 *
 * <p>The servo only swings the stick. Once the stick is down, its wheels sit on the tile and the
 * push from the FLOWER goes into the tile through the arm, not into the servo, as long as nothing
 * is holding the arm somewhere else. A positional servo cannot "rest" on the tile: a position a
 * hair above the tile carries the stick and takes the push, a hair below stalls against the tile.
 * So after the swing the pulse is switched off ({@link PwmControl#setPwmDisable()}): the servo goes
 * limp and the stick rests on the tile by its own weight. Stowed, the servo keeps holding, because
 * the stowed hard stop may not exist yet; {@link #LIMP_STOWED} releases it there too once it does.
 *
 * <p>The two positions are {@code @Configurable}: drag them in Panels with the servo moving until
 * the stick sits on each stop, then write the numbers back here with a comment saying which servo
 * and stick they were measured on. Panels edits are lost on restart.
 */
@Configurable
public class FlowerStickSubsystem implements Subsystem {

    /**
     * Servo position with the stick in, 0 to 1. Measured on 20245 on 26 Oct 2026 with the goBILDA
     * Torque servo on Expansion Hub servo port 1; 19429 not yet checked.
     */
    public static double RETRACTED = 0.6;

    /** Servo position with the stick out, 0 to 1. Measured with {@link #RETRACTED}. */
    public static double EXTENDED = 0.0;

    /**
     * Milliseconds the servo is given to finish a swing before the pulse is cut. A goBILDA Torque
     * servo does 60 degrees in 0.25 s unloaded, so the 162 degree swing takes about 0.7 s; the stick
     * on it, more. If the stick is still moving when the servo goes limp it just drops the rest of
     * the way onto the tile.
     */
    public static int SETTLE_MS = 1500;

    /** Cut the pulse once the stick is out and settled, so the stick rests on the tile. */
    public static boolean LIMP_EXTENDED = true;

    /** Cut the pulse once the stick is in and settled. Only once a stowed hard stop exists. */
    public static boolean LIMP_STOWED = false;

    private final Servo servo;
    private final PwmControl pwm;   // null if this servo cannot switch its pulse off
    private boolean out = false;
    private boolean lastOut = false;
    private boolean limp = false;
    private long movedAtMs = 0;

    /** Looks the servo up once. A missing servo fails init with the SDK's own message. */
    public FlowerStickSubsystem(HardwareMap hardwareMap) {
        servo = hardwareMap.get(Servo.class, DeviceNames.FLOWER_STICK);
        pwm = servo instanceof PwmControl ? (PwmControl) servo : null;
    }

    /** Ask for the stick out. Takes effect in the next {@link #update()}. */
    public void extend() {
        out = true;
    }

    /** Ask for the stick in. Takes effect in the next {@link #update()}. */
    public void retract() {
        out = false;
    }

    /**
     * Ask for the other end: in if the stick is out, out if it is in. One button on a gamepad does
     * both; a press mid-swing turns the stick round, which the servo handles as a new target.
     */
    public void toggle() {
        out = !out;
    }

    /** Whether the stick has been asked out. The servo has no sensor, so this is the request. */
    public boolean extended() {
        return out;
    }

    /** Whether the servo's pulse is off and the stick is resting on its own. */
    public boolean limp() {
        return limp;
    }

    /** Start with the stick in and the servo holding it, so the robot fits its starting envelope. */
    @Override
    public void initialize() {
        out = false;
        lastOut = false;
        power(true);
        servo.setPosition(unitRange(RETRACTED));
        movedAtMs = System.currentTimeMillis();
    }

    /**
     * One write per loop while the servo is working: the position the request asks for, clamped so
     * Panels cannot overdrive it. A change of request powers the servo and starts the swing; once
     * the swing has had {@link #SETTLE_MS} the pulse is cut if that end asks for it.
     */
    @Override
    public void update() {
        long now = System.currentTimeMillis();
        if (out != lastOut) {
            lastOut = out;
            movedAtMs = now;
            power(true);
        }
        boolean wantLimp = shouldGoLimp(now - movedAtMs, SETTLE_MS, out ? LIMP_EXTENDED : LIMP_STOWED);
        if (wantLimp) {
            power(false);
        } else {
            power(true);
            servo.setPosition(unitRange(out ? EXTENDED : RETRACTED));
        }
    }

    /** A servo holds where it is; there is nothing to cut. */
    @Override
    public void stop() {
    }

    @Override
    public String health() {
        if (pwm == null) {
            return "servo configured; it cannot switch its pulse off, so it holds the stick down";
        }
        return "servo configured; presence unknown (a servo reports nothing back)";
    }

    /** Switches the servo's pulse on or off, if it can, and remembers which. */
    private void power(boolean on) {
        if (pwm == null) {
            limp = false;
            return;
        }
        if (on && limp) {
            pwm.setPwmEnable();
        } else if (!on && !limp) {
            pwm.setPwmDisable();
        }
        limp = !on;
    }

    /**
     * Whether the servo should be limp: the swing has had its settle time and this end asks for it.
     * Pure, so the test can pin it down.
     */
    static boolean shouldGoLimp(long sinceMoveMs, int settleMs, boolean limpWanted) {
        return limpWanted && sinceMoveMs >= settleMs;
    }

    /**
     * Clamps a Panels-editable position to [0, 1], and treats NaN as 0. A typo in Panels should
     * leave the stick in, never command the servo past its travel or send NaN.
     */
    static double unitRange(double value) {
        return value > 0.0 ? Math.min(value, 1.0) : 0.0;
    }
}
