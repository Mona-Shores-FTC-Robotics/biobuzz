package org.firstinspires.ftc.teamcode.controls;

import java.util.EnumMap;
import java.util.Map;

/**
 * Which input runs which {@link Action}: the only place a button is chosen. One layout, shared by
 * both teams; the drive team changes it here, and the Driver Station's Controls page and the
 * printed cheat sheet follow.
 *
 * <p>Both people can run the intake (R2) and unjam (L2), the same input on each pad. When both
 * press the intake at once, the driver wins ({@link IntakeArbiter}).
 *
 * <p>{@code ControlSchemeTest} checks the layout: no input used twice on one gamepad, every
 * action the OpMode needs present.
 */
public final class ControlScheme {

    private ControlScheme() {
    }

    /** Where the FLOWER pick goes unless {@code TeleOpConfig.flowerPickButton} says otherwise. */
    public static final Input DEFAULT_FLOWER_PICK = Input.CROSS;

    /**
     * The inputs, with the FLOWER pick on {@code flowerPick}. The pick may be any face button or
     * D-pad direction; whatever driver action had that input moves to the pick's default
     * ({@link #DEFAULT_FLOWER_PICK}), so the two swap and nothing is lost. Anything else (a stick,
     * a trigger, null) leaves the pick on its default.
     */
    public static Map<Action, Input> layout(Input flowerPick) {
        EnumMap<Action, Input> map = new EnumMap<>(Action.class);
        map.put(Action.DRIVE, Input.LEFT_STICK);
        map.put(Action.TURN, Input.RIGHT_STICK_X);
        map.put(Action.INTAKE, Input.R2);
        map.put(Action.DRIVER_UNJAM, Input.L2);
        map.put(Action.SLOW, Input.L1);
        map.put(Action.TURBO, Input.R1);
        map.put(Action.FLOWER_PICK, DEFAULT_FLOWER_PICK);
        map.put(Action.FLOWER_STICK, Input.SQUARE);
        map.put(Action.CHASSIS_AIM, Input.CIRCLE);
        map.put(Action.RESET_HEADING, Input.TRIANGLE);
        // Pose fixes are occasional, so they sit on the D-pad and leave the face buttons for play.
        map.put(Action.CAMERA_RELOCALIZE, Input.DPAD_LEFT);
        map.put(Action.RESET_TO_SPOT, Input.DPAD_RIGHT);
        map.put(Action.AUTO_PARK, Input.DPAD_DOWN);
        map.put(Action.GARDEN_GATHER, Input.DPAD_UP);

        map.put(Action.OPERATOR_INTAKE, Input.R2);
        map.put(Action.UNJAM, Input.L2);
        map.put(Action.LAUNCHER_TOGGLE, Input.R1);
        map.put(Action.FIRE, Input.CIRCLE);
        map.put(Action.TURRET_LOCK, Input.TRIANGLE);
        map.put(Action.TRIM_LEFT, Input.DPAD_LEFT);
        map.put(Action.TRIM_RIGHT, Input.DPAD_RIGHT);
        map.put(Action.TRIM_RESET, Input.DPAD_DOWN);
        map.put(Action.TURRET_ZERO, Input.DPAD_UP);
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
