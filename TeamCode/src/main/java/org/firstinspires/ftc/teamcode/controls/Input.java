package org.firstinspires.ftc.teamcode.controls;

import com.qualcomm.robotcore.hardware.Gamepad;

import java.util.function.Predicate;

/**
 * Every gamepad input a {@link ControlScheme} may give an {@link Action}, with the name the
 * drivers see (PS5 first, the SDK's Xbox name after) and how it is read.
 *
 * <p>What is <b>not</b> here is the point of this list. Share (Back) cycles the Driver Station
 * pages, Options (Start) is half of the Driver Station's gamepad-assign chord (Options + Cross or
 * Circle), and the touchpad and PS button are too easy to hit by accident. None of them can be
 * bound, because a scheme can only name what is in this enum.
 */
public enum Input {
    LEFT_STICK("Left stick", null),
    RIGHT_STICK_X("Right stick X", null),
    CROSS("Cross (A)", g -> g.a),
    CIRCLE("Circle (B)", g -> g.b),
    SQUARE("Square (X)", g -> g.x),
    TRIANGLE("Triangle (Y)", g -> g.y),
    L1("L1", g -> g.left_bumper),
    R1("R1", g -> g.right_bumper),
    L2("L2", g -> g.left_trigger > Input.TRIGGER_PRESSED),
    R2("R2", g -> g.right_trigger > Input.TRIGGER_PRESSED),
    DPAD_UP("D-pad up", g -> g.dpad_up),
    DPAD_DOWN("D-pad down", g -> g.dpad_down),
    DPAD_LEFT("D-pad left", g -> g.dpad_left),
    DPAD_RIGHT("D-pad right", g -> g.dpad_right);

    /** A trigger pulled past this counts as pressed. Well clear of a resting trigger's drift. */
    public static final double TRIGGER_PRESSED = 0.5;

    public final String label;
    private final Predicate<Gamepad> reader;

    Input(String label, Predicate<Gamepad> reader) {
        this.label = label;
        this.reader = reader;
    }

    /** A stick: read by the OpMode as a number, documented here, never a button. */
    public boolean isStick() {
        return reader == null;
    }

    /** A face button or a D-pad direction: the inputs a configurable button may move between. */
    public boolean isFaceOrDpad() {
        switch (this) {
            case CROSS: case CIRCLE: case SQUARE: case TRIANGLE:
            case DPAD_UP: case DPAD_DOWN: case DPAD_LEFT: case DPAD_RIGHT:
                return true;
            default:
                return false;
        }
    }

    /** Whether this input is pressed on {@code gamepad}. False for a stick. */
    public boolean read(Gamepad gamepad) {
        return reader != null && reader.test(gamepad);
    }
}
