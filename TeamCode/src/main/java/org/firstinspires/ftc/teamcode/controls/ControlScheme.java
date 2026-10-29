package org.firstinspires.ftc.teamcode.controls;

import java.util.EnumMap;
import java.util.Map;

/**
 * Which input runs which {@link Action}: the only place a button is chosen. A scheme is picked
 * in Panels ({@code TeleOpConfig.scheme}) and read once, at INIT, so a change can never strand a
 * held mechanism mid-match; it takes effect the next time INIT is pressed.
 *
 * <p>The schemes share the driver's layout and differ in who may run the intake:
 *
 * <ul>
 *   <li>{@link #A}: the driver alone.</li>
 *   <li>{@link #B}: the operator too, on L1; whenever the driver presses intake or reverse,
 *       the driver wins ({@link IntakeArbiter}).</li>
 * </ul>
 *
 * <p>A third scheme is a new constant and its lines in {@link #layout}; nothing else changes.
 * {@code ControlSchemeTest} checks every scheme: no input used twice on one gamepad, every action
 * the OpMode needs present.
 */
public enum ControlScheme {
    A("A: driver-only intake"),
    B("B: shared intake, driver wins");

    /** Where the FLOWER pick goes unless {@code TeleOpConfig.flowerPickButton} says otherwise. */
    public static final Input DEFAULT_FLOWER_PICK = Input.DPAD_UP;

    public final String title;

    ControlScheme(String title) {
        this.title = title;
    }

    /**
     * This scheme's inputs, with the FLOWER pick on {@code flowerPick}. The pick may be any face
     * button or D-pad direction; whatever driver action had that input moves to the pick's default
     * ({@link #DEFAULT_FLOWER_PICK}), so the two swap and nothing is lost. Anything else (a stick,
     * a trigger, null) leaves the pick on its default.
     */
    public Map<Action, Input> layout(Input flowerPick) {
        EnumMap<Action, Input> map = new EnumMap<>(Action.class);
        map.put(Action.DRIVE, Input.LEFT_STICK);
        map.put(Action.TURN, Input.RIGHT_STICK_X);
        map.put(Action.INTAKE, Input.R2);
        map.put(Action.REVERSE_INTAKE, Input.L2);
        map.put(Action.SLOW, Input.L1);
        map.put(Action.TURBO, Input.R1);
        map.put(Action.FLOWER_PICK, DEFAULT_FLOWER_PICK);
        map.put(Action.FLOWER_STICK, Input.CROSS);
        map.put(Action.CHASSIS_AIM, Input.CIRCLE);
        map.put(Action.RESET_TO_SPOT, Input.SQUARE);
        map.put(Action.DRIVER_RELOCALIZE, Input.DPAD_LEFT);
        map.put(Action.RESET_HEADING, Input.TRIANGLE);
        map.put(Action.AUTO_PARK, Input.DPAD_DOWN);

        map.put(Action.LAUNCHER_TOGGLE, Input.R1);
        map.put(Action.FIRE, Input.R2);
        map.put(Action.UNJAM, Input.L2);
        map.put(Action.TRIM_LEFT, Input.DPAD_LEFT);
        map.put(Action.TRIM_RIGHT, Input.DPAD_RIGHT);
        map.put(Action.TRIM_RESET, Input.DPAD_DOWN);
        map.put(Action.TURRET_LOCK, Input.TRIANGLE);
        map.put(Action.TURRET_ZERO, Input.DPAD_UP);
        map.put(Action.RELOCALIZE, Input.CROSS);
        switch (this) {
            case A:
                break;
            case B:
                map.put(Action.OPERATOR_INTAKE, Input.L1);
                break;
        }
        if (flowerPick != null && flowerPick != DEFAULT_FLOWER_PICK && flowerPick.isFaceOrDpad()) {
            for (Map.Entry<Action, Input> entry : map.entrySet()) {
                if (entry.getKey().pad == Action.Pad.DRIVER && entry.getValue() == flowerPick) {
                    entry.setValue(DEFAULT_FLOWER_PICK);
                }
            }
            map.put(Action.FLOWER_PICK, flowerPick);
        }
        return map;
    }
}
