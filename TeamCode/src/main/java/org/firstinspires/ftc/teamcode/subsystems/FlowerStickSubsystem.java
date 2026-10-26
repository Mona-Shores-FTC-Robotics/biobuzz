package org.firstinspires.ftc.teamcode.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.hardware.DeviceNames;

/**
 * The FLOWER stick: one servo swinging a stick, the FLOWER extractor's first edition (#177).
 *
 * <p>The smallest real subsystem in the repo, and the pattern every mechanism follows: the OpMode
 * says what it wants ({@link #extend()}, {@link #retract()}), {@link #update()} does it, once per
 * loop. Which button triggers it belongs in the OpMode's bindings, not here.
 *
 * <p>The two positions are {@code @Configurable}: drag them in Panels with the servo moving until
 * the stick is where the build team wants it, then write the numbers back here with a comment
 * saying which servo and stick they were measured on. Panels edits are lost on restart.
 */
@Configurable
public class FlowerStickSubsystem implements Subsystem {

    /**
     * Servo position with the stick in, 0 to 1. <b>Not yet measured:</b> starts at one end of the
     * servo's travel, which is a fact of the servo API, not a guess about the stick.
     */
    public static double RETRACTED = 0.0;

    /** Servo position with the stick out, 0 to 1. <b>Not yet measured</b>, see {@link #RETRACTED}. */
    public static double EXTENDED = 1.0;

    private final Servo servo;
    private boolean out = false;

    /** Looks the servo up once. A missing servo fails init with the SDK's own message. */
    public FlowerStickSubsystem(HardwareMap hardwareMap) {
        servo = hardwareMap.get(Servo.class, DeviceNames.FLOWER_STICK);
    }

    /** Ask for the stick out. Takes effect in the next {@link #update()}. */
    public void extend() {
        out = true;
    }

    /** Ask for the stick in. Takes effect in the next {@link #update()}. */
    public void retract() {
        out = false;
    }

    /** Whether the stick has been asked out. The servo has no sensor, so this is the request. */
    public boolean extended() {
        return out;
    }

    /** Start with the stick in, so the robot fits its starting envelope. */
    @Override
    public void initialize() {
        out = false;
        servo.setPosition(unitRange(RETRACTED));
    }

    /** One write per loop: the position the request asks for, clamped so Panels cannot overdrive it. */
    @Override
    public void update() {
        servo.setPosition(unitRange(out ? EXTENDED : RETRACTED));
    }

    /** A servo holds where it is; there is nothing to cut. */
    @Override
    public void stop() {
    }

    /**
     * Clamps a Panels-editable position to [0, 1], and treats NaN as 0. A typo in Panels should
     * leave the stick in, never command the servo past its travel or send NaN.
     */
    static double unitRange(double value) {
        return value > 0.0 ? Math.min(value, 1.0) : 0.0;
    }
}
