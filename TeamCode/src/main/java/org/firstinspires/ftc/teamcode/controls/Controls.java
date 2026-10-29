package org.firstinspires.ftc.teamcode.controls;

import com.qualcomm.robotcore.hardware.Gamepad;

import java.util.Map;

/**
 * The TeleOp's gamepads, asked about by {@link Action}. Built once in {@code onInit()} from
 * {@link ControlScheme}; from then on the OpMode never names a button.
 *
 * <pre>{@code
 * controls.on(Action.TURRET_LOCK).onPress(this::toggleTurret);   // an edge, through Bindings
 * controls.note(Action.FIRE);                                     // on the Controls page...
 * if (controls.held(Action.FIRE)) ...                             // ...and read every loop
 * }</pre>
 *
 * <p>Both go through the OpMode's {@link Bindings}, so the Driver Station's Controls page still
 * lists exactly what runs, and the printed cheat sheet ({@link CheatSheet}) is built from the same
 * {@link ControlScheme#layout}. An action the layout leaves out reads as never held and is not
 * listed.
 */
public final class Controls {

    private final Map<Action, Input> layout;
    private final Gamepad driverPad;
    private final Gamepad operatorPad;
    private final Bindings driver;
    private final Bindings operator;

    public Controls(Input flowerPick, Gamepad driverPad, Gamepad operatorPad,
                    Bindings driver, Bindings operator) {
        this.layout = ControlScheme.layout(flowerPick);
        this.driverPad = driverPad;
        this.operatorPad = operatorPad;
        this.driver = driver;
        this.operator = operator;
    }

    /** The input bound to {@code action}, or null when the layout leaves it out. */
    public Input input(Action action) {
        return layout.get(action);
    }

    /**
     * Bind {@code action} for an edge ({@code onPress}, {@code whileHeld}...). An action the
     * layout leaves out gets a trigger that is never polled, so the call still reads the same.
     */
    public Bindings.Trigger on(Action action) {
        Input input = layout.get(action);
        if (input == null || input.isStick()) {
            return new Bindings.Trigger(() -> false);
        }
        Gamepad pad = padFor(action);
        return bindingsFor(action).when(input.label, action.description, () -> input.read(pad));
    }

    /** List {@code action} on the Controls page, for one the OpMode reads itself with {@link #held}. */
    public void note(Action action) {
        Input input = layout.get(action);
        if (input != null) {
            bindingsFor(action).note(input.label, action.description);
        }
    }

    /** Whether {@code action}'s input is pressed now. False for one the layout leaves out, or a stick. */
    public boolean held(Action action) {
        Input input = layout.get(action);
        return input != null && input.read(padFor(action));
    }

    private Gamepad padFor(Action action) {
        return action.pad == Action.Pad.DRIVER ? driverPad : operatorPad;
    }

    private Bindings bindingsFor(Action action) {
        return action.pad == Action.Pad.DRIVER ? driver : operator;
    }
}
