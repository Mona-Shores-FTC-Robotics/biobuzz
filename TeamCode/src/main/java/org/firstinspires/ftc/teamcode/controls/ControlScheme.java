package org.firstinspires.ftc.teamcode.controls;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Which input runs which {@link Action}: the only place a button is chosen. One layout, shared by
 * both teams; the drive team changes it here, and the Driver Station's Controls page and the
 * printed cheat sheet follow.
 *
 * <p>Both people can run the intake (R2) and unjam (L2), the same input on each pad. The operator's
 * R2 toggles the intake; the driver's R2 and either L2 are holds over that toggle, and the driver
 * wins when both press at once ({@link IntakeArbiter}).
 *
 * <p>A layout leaves out what does nothing on this robot, so the Controls page and the cheat
 * sheet show only buttons that work: {@link #NOT_BOUND} always, and {@link #TURRET_ACTIONS} while
 * the turret is locked forward ({@link #layout(Input, boolean)}). Chosen from 93 minutes of Basic
 * Drive on 20245 (1 to 2 Nov 2026): the turret controls never pressed on a locked turret,
 * auto-park pressed five times with nothing built behind it.
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
     * Actions no layout binds: auto-park, which is not built. Its code stays, so binding it again
     * is one line here once it does something.
     */
    public static final Set<Action> NOT_BOUND = Collections.unmodifiableSet(EnumSet.of(Action.AUTO_PARK));

    /**
     * Bound only while the turret is locked forward: the launch trim reset, on D-pad down, the
     * turret's trim reset's button, so the habit carries over (mentor, 7 Nov 2026). With the turret
     * aiming, its trim reset on D-pad down clears the launch trim too.
     */
    public static final Set<Action> LOCKED_TURRET_ONLY = Collections.unmodifiableSet(EnumSet.of(
            Action.LAUNCHER_TRIM_RESET));

    /** The operator's turret controls: left out while the turret is locked forward. */
    public static final Set<Action> TURRET_ACTIONS = Collections.unmodifiableSet(EnumSet.of(
            Action.TURRET_LOCK, Action.TRIM_LEFT, Action.TRIM_RIGHT, Action.TRIM_RESET, Action.TURRET_ZERO));

    /**
     * {@link #layout(Input)} without the turret's controls when {@code turretLocked}: what the
     * match TeleOp and the cheat sheet use, so a locked turret's buttons are not offered.
     */
    public static Map<Action, Input> layout(Input flowerPick, boolean turretLocked) {
        return layout(flowerPick, turretLocked, true);
    }

    /**
     * As {@link #layout(Input, boolean)}, also leaving out the go-to-launch-spot button unless
     * {@code launchSpot} ({@code TeleOpConfig.launchSpot}, on while the drivers try it): the
     * match TeleOp's layout.
     */
    public static Map<Action, Input> layout(Input flowerPick, boolean turretLocked, boolean launchSpot) {
        Map<Action, Input> map = layout(flowerPick);
        if (turretLocked) {
            for (Action action : TURRET_ACTIONS) map.remove(action);
            map.put(Action.LAUNCHER_TRIM_RESET, Input.DPAD_DOWN);
        }
        if (!launchSpot) map.remove(Action.LAUNCH_SPOT);
        return map;
    }

    /**
     * The inputs, with the FLOWER pick on {@code flowerPick}. The pick may be any face button or
     * D-pad direction; whatever driver action had that input moves to the pick's default
     * ({@link #DEFAULT_FLOWER_PICK}), so the two swap and nothing is lost. Anything else (a stick,
     * a trigger, null) leaves the pick on its default. Every action but {@link #NOT_BOUND} and
     * {@link #LOCKED_TURRET_ONLY}, turret controls included.
     */
    public static Map<Action, Input> layout(Input flowerPick) {
        EnumMap<Action, Input> map = new EnumMap<>(Action.class);
        map.put(Action.DRIVE, Input.LEFT_STICK);
        map.put(Action.TURN, Input.RIGHT_STICK_X);
        map.put(Action.INTAKE, Input.R2);
        map.put(Action.DRIVER_UNJAM, Input.L2);
        map.put(Action.SLOW, Input.L1);
        map.put(Action.FLOWER_PICK, DEFAULT_FLOWER_PICK);
        map.put(Action.CHASSIS_AIM, Input.CIRCLE);
        map.put(Action.GARDEN_GATHER, Input.TRIANGLE);
        // Experimental, to try against chassis aim (Circle); TeleOpConfig.launchSpot unbinds it.
        map.put(Action.LAUNCH_SPOT, Input.SQUARE);
        // Pose and heading fixes are occasional, so they sit on the D-pad and leave the face
        // buttons for play.
        map.put(Action.RESET_HEADING, Input.DPAD_UP);
        map.put(Action.RESET_LEFT_CORNER, Input.DPAD_LEFT);
        map.put(Action.RESET_RIGHT_CORNER, Input.DPAD_RIGHT);
        map.put(Action.CAMERA_RELOCALIZE, Input.DPAD_DOWN);

        map.put(Action.OPERATOR_INTAKE, Input.R2);
        map.put(Action.UNJAM, Input.L2);
        map.put(Action.LAUNCHER_TOGGLE, Input.CROSS);
        map.put(Action.LAUNCHER_TRIM_DOWN, Input.L1);
        map.put(Action.LAUNCHER_TRIM_UP, Input.R1);
        map.put(Action.FIRE, Input.CIRCLE);
        map.put(Action.TURRET_LOCK, Input.TRIANGLE);
        map.put(Action.FLOWER_STICK, Input.SQUARE);
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
