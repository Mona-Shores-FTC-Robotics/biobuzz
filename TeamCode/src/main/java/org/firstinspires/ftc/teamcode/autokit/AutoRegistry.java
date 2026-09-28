package org.firstinspires.ftc.teamcode.autokit;

import com.pedropathing.ivy.Command;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * The robot's named actions and conditions — the only names an Auto built in the editor can use.
 *
 * <p>An action is a factory, because a command runs once and an Auto may use the same action
 * several times. A condition is a plain true/false that reads state a subsystem already keeps
 * current; it is only called while a card that uses it is waiting, so it must be cheap and must not
 * read hardware itself.
 *
 * <pre>
 * registry.action("ShootAll", () -&gt; robot.launcher.shootAll())
 *         .condition("IntakeFull", robot.intake::isFull);
 * </pre>
 */
public final class AutoRegistry {

    private final Map<String, Supplier<Command>> actions = new LinkedHashMap<>();
    private final Map<String, BooleanSupplier> conditions = new LinkedHashMap<>();

    public AutoRegistry action(String name, Supplier<Command> factory) {
        if (factory == null) throw new IllegalArgumentException("Action " + name + " has no command");
        if (actions.put(name, factory) != null) {
            throw new IllegalArgumentException("Action " + name + " is registered twice");
        }
        return this;
    }

    public AutoRegistry condition(String name, BooleanSupplier condition) {
        if (condition == null) throw new IllegalArgumentException("Condition " + name + " has no check");
        if (conditions.put(name, condition) != null) {
            throw new IllegalArgumentException("Condition " + name + " is registered twice");
        }
        return this;
    }

    /** A new command for the action; throws, naming what is registered, if it is unknown. */
    public Command action(String name) {
        Supplier<Command> factory = actions.get(name);
        if (factory == null) {
            throw new IllegalArgumentException("No action named " + name + ". Registered: " + actions.keySet());
        }
        return factory.get();
    }

    /** The condition; throws, naming what is registered, if it is unknown. */
    public BooleanSupplier condition(String name) {
        BooleanSupplier condition = conditions.get(name);
        if (condition == null) {
            throw new IllegalArgumentException("No condition named " + name + ". Registered: " + conditions.keySet());
        }
        return condition;
    }

    /**
     * Fails, naming every missing name at once, if an Auto uses anything not registered. Call it at
     * init with a generated Auto's {@code ACTIONS} and {@code CONDITIONS}, so a typo stops the
     * OpMode before the match rather than halfway through it.
     */
    public void requireAll(String[] actionNames, String[] conditionNames) {
        List<String> missing = new ArrayList<>();
        for (String name : actionNames) if (!actions.containsKey(name)) missing.add("action " + name);
        for (String name : conditionNames) if (!conditions.containsKey(name)) missing.add("condition " + name);
        if (!missing.isEmpty()) {
            throw new IllegalStateException("This Auto uses names the robot has not registered: "
                    + String.join(", ", missing));
        }
    }
}
