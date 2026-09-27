package org.firstinspires.ftc.teamcode.logging;

import java.io.IOException;

/**
 * Logs a gamepad in the layout AdvantageScope's Joysticks tab reads, so the tab shows which buttons
 * the driver pressed with no configuration.
 *
 * <p>Uses the <b>SDL gamepad</b> layout, the one AdvantageScope v27 uses for 2027 and Systemcore:
 * {@code ButtonsAvailable}/{@code ButtonValues} are 64-bit masks indexed by SDL button, and
 * {@code AxesAvailable}/{@code AxisValues} are indexed by SDL axis. The indices below are the ones
 * AdvantageScope's own PS4 controller layout ({@code Joystick_PS4ControllerV3/config.json},
 * {@code sdlSourceIndex}) reads. AdvantageScope v26 predates this layout, so on v26 the Joysticks
 * tab will not draw these; everything else in the log is unaffected.
 *
 * <p>PsiKit's {@code GamepadWrapper} does the same job for the older layout, and is where the idea
 * of mapping an FTC gamepad onto this tab comes from.
 *
 * <p>Takes a plain {@link State} rather than the SDK's {@code Gamepad} so the simulated match can
 * build one; on the robot, {@link State#copyFrom} fills it once per loop.
 */
public final class GamepadLog {

    // SDL gamepad buttons, as AdvantageScope's controller layouts index them.
    static final int A = 0;               // cross
    static final int B = 1;               // circle
    static final int X = 2;               // square
    static final int Y = 3;               // triangle
    static final int BACK = 4;            // share
    static final int GUIDE = 5;           // PS
    static final int START = 6;           // options
    static final int LEFT_STICK = 7;
    static final int RIGHT_STICK = 8;
    static final int LEFT_BUMPER = 9;
    static final int RIGHT_BUMPER = 10;
    static final int DPAD_UP = 11;
    static final int DPAD_DOWN = 12;
    static final int DPAD_LEFT = 13;
    static final int DPAD_RIGHT = 14;
    static final int TOUCHPAD = 20;

    static final long BUTTONS_AVAILABLE = mask(A, B, X, Y, BACK, GUIDE, START, LEFT_STICK, RIGHT_STICK,
            LEFT_BUMPER, RIGHT_BUMPER, DPAD_UP, DPAD_DOWN, DPAD_LEFT, DPAD_RIGHT, TOUCHPAD);

    /** SDL axes 0–5: left x, left y, right x, right y, left trigger, right trigger. */
    static final long AXES_AVAILABLE = 0b111111L;

    /** One gamepad's state, named as the FTC SDK names it. Sticks: +y is down, as on the SDK. */
    public static final class State {
        public boolean a, b, x, y;
        public boolean back, guide, start;
        public boolean leftStickButton, rightStickButton;
        public boolean leftBumper, rightBumper;
        public boolean dpadUp, dpadDown, dpadLeft, dpadRight;
        public boolean touchpad;
        public double leftStickX, leftStickY, rightStickX, rightStickY;
        public double leftTrigger, rightTrigger;

        public long buttonBits() {
            long bits = 0L;
            if (a) bits |= 1L << A;
            if (b) bits |= 1L << B;
            if (x) bits |= 1L << X;
            if (y) bits |= 1L << Y;
            if (back) bits |= 1L << BACK;
            if (guide) bits |= 1L << GUIDE;
            if (start) bits |= 1L << START;
            if (leftStickButton) bits |= 1L << LEFT_STICK;
            if (rightStickButton) bits |= 1L << RIGHT_STICK;
            if (leftBumper) bits |= 1L << LEFT_BUMPER;
            if (rightBumper) bits |= 1L << RIGHT_BUMPER;
            if (dpadUp) bits |= 1L << DPAD_UP;
            if (dpadDown) bits |= 1L << DPAD_DOWN;
            if (dpadLeft) bits |= 1L << DPAD_LEFT;
            if (dpadRight) bits |= 1L << DPAD_RIGHT;
            if (touchpad) bits |= 1L << TOUCHPAD;
            return bits;
        }

        public void clear() {
            a = b = x = y = back = guide = start = false;
            leftStickButton = rightStickButton = leftBumper = rightBumper = false;
            dpadUp = dpadDown = dpadLeft = dpadRight = touchpad = false;
            leftStickX = leftStickY = rightStickX = rightStickY = leftTrigger = rightTrigger = 0.0;
        }
    }

    private final String prefix;
    private final double[] axes = new double[6];

    /** @param index 0 for gamepad 1 (driver), 1 for gamepad 2 (operator) */
    public GamepadLog(int index) {
        this.prefix = AdvantageScopeKeys.JOYSTICK_PREFIX + index + "/";
    }

    public void write(WpiLog log, State g, long timestampUs) throws IOException {
        axes[0] = g.leftStickX;
        axes[1] = g.leftStickY;
        axes[2] = g.rightStickX;
        axes[3] = g.rightStickY;
        axes[4] = g.leftTrigger;
        axes[5] = g.rightTrigger;
        log.put(prefix + "ButtonsAvailable", BUTTONS_AVAILABLE, timestampUs);
        log.put(prefix + "ButtonValues", g.buttonBits(), timestampUs);
        log.put(prefix + "AxesAvailable", AXES_AVAILABLE, timestampUs);
        log.put(prefix + "AxisValues", axes, timestampUs);
        log.put(prefix + "POVsAvailable", 0L, timestampUs);
    }

    private static long mask(int... bits) {
        long m = 0L;
        for (int b : bits) m |= 1L << b;
        return m;
    }
}
