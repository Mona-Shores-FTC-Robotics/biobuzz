package org.firstinspires.ftc.teamcode.autokit;

import com.pedropathing.ivy.Command;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * The robot's named commands and triggers: the only names an Auto built in the editor can use.
 *
 * <p>A command is a factory, because a command runs once and an Auto may use the same one several
 * times. It carries its <em>typical</em> time, which the editor's preview uses; on the robot it
 * runs until it finishes or its step's timeout. A trigger is a plain true/false that reads state a
 * subsystem already keeps current; it is only called while a step waits on it, so it must be
 * cheap and must not read hardware itself. A trigger registered with {@link #triggerSince} is
 * about something that <em>happens</em> ("a TIP started"): each wait starts watching it afresh,
 * so it means "since this wait began".
 *
 * <pre>
 * registry.command("LaunchAll", 3.0, () -&gt; robot.launcher.launchAll())
 *         .trigger("IntakeFull", robot.intake::isFull);
 * </pre>
 *
 * <p>{@link #describe()} writes the list for the editor, so nobody types names by hand.
 */
public final class AutoRegistry {

    private final Map<String, Supplier<Command>> commands = new LinkedHashMap<>();
    private final Map<String, Double> typicalSeconds = new LinkedHashMap<>();
    private final Map<String, Supplier<BooleanSupplier>> triggers = new LinkedHashMap<>();

    /**
     * Registers a command.
     *
     * @param typicalS how long it usually takes, in seconds: what the editor's preview shows
     */
    public AutoRegistry command(String name, double typicalS, Supplier<Command> factory) {
        if (factory == null) throw new IllegalArgumentException("Command " + name + " has no factory");
        if (!(typicalS >= 0)) throw new IllegalArgumentException("Command " + name + " needs a typical time >= 0");
        if (commands.put(name, factory) != null) {
            throw new IllegalArgumentException("Command " + name + " is registered twice");
        }
        typicalSeconds.put(name, typicalS);
        return this;
    }

    /** Registers a trigger that is true while {@code check} is. */
    public AutoRegistry trigger(String name, BooleanSupplier check) {
        if (check == null) throw new IllegalArgumentException("Trigger " + name + " has no check");
        return triggerSince(name, () -> check);
    }

    /**
     * Registers a trigger about something that happens. When a wait starts, {@code startWatching}
     * is called once and returns the check that wait uses: it can note where things stand now and
     * say "true" once they have moved on. Called when a wait starts, never per loop.
     */
    public AutoRegistry triggerSince(String name, Supplier<BooleanSupplier> startWatching) {
        if (startWatching == null) throw new IllegalArgumentException("Trigger " + name + " has no check");
        if (triggers.put(name, startWatching) != null) {
            throw new IllegalArgumentException("Trigger " + name + " is registered twice");
        }
        return this;
    }

    /** A new instance of the command; throws, naming what is registered, if it is unknown. */
    public Command command(String name) {
        Supplier<Command> factory = commands.get(name);
        if (factory == null) {
            throw new IllegalArgumentException("No command named " + name + ". Registered: " + commands.keySet());
        }
        return factory.get();
    }

    /**
     * The trigger's check for a wait that starts now; throws, naming what is registered, if it is
     * unknown. Call it when the wait starts, and {@link #requireTrigger} when the Auto is built.
     */
    public BooleanSupplier watch(String name) {
        return startWatching(name).get();
    }

    /** Throws, naming what is registered, if there is no trigger {@code name}. */
    public void requireTrigger(String name) {
        startWatching(name);
    }

    private Supplier<BooleanSupplier> startWatching(String name) {
        Supplier<BooleanSupplier> factory = triggers.get(name);
        if (factory == null) {
            throw new IllegalArgumentException("No trigger named " + name + ". Registered: " + triggers.keySet());
        }
        return factory;
    }

    /**
     * Fails, naming every missing name at once, if an Auto uses anything not registered. Call it at
     * init with a generated Auto's {@code COMMANDS} and {@code TRIGGERS}, so a typo stops the
     * OpMode before the match rather than halfway through it.
     */
    public void requireAll(String[] commandNames, String[] triggerNames) {
        List<String> missing = new ArrayList<>();
        for (String name : commandNames) if (!commands.containsKey(name)) missing.add("command " + name);
        for (String name : triggerNames) if (!triggers.containsKey(name)) missing.add("trigger " + name);
        if (!missing.isEmpty()) {
            throw new IllegalStateException("This Auto uses names the robot has not registered: "
                    + String.join(", ", missing));
        }
    }

    /**
     * The list for the editor, as JSON:
     * {@code {"commands": [{"name": "LaunchAll", "typicalS": 3.0}], "triggers": ["IntakeFull"]}}.
     * Names keep their registration order.
     */
    public String describe() {
        StringBuilder out = new StringBuilder("{\n  \"commands\": [");
        String sep = "\n";
        for (Map.Entry<String, Double> entry : typicalSeconds.entrySet()) {
            out.append(sep).append(String.format(Locale.US, "    {\"name\": %s, \"typicalS\": %s}",
                    quote(entry.getKey()), number(entry.getValue())));
            sep = ",\n";
        }
        out.append(commands.isEmpty() ? "],\n" : "\n  ],\n");
        out.append("  \"triggers\": [");
        sep = "\n";
        for (String name : triggers.keySet()) {
            out.append(sep).append("    ").append(quote(name));
            sep = ",\n";
        }
        out.append(triggers.isEmpty() ? "]\n}\n" : "\n  ]\n}\n");
        return out.toString();
    }

    private static String number(double value) {
        return value == Math.rint(value) ? String.format(Locale.US, "%.1f", value) : Double.toString(value);
    }

    private static String quote(String text) {
        return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
