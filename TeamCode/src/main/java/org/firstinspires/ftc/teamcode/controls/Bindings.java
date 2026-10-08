package org.firstinspires.ftc.teamcode.controls;

import com.pedropathing.ivy.Command;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * One gamepad's button bindings. Every binding carries a label, and the Controls page on the
 * Driver Station is generated from those labels — so the help the drivers see is the bindings
 * that actually run, and cannot drift from them.
 *
 * <pre>{@code
 * driver.note("Left stick", "Drive");
 * driver.when("Y", "Reset heading", () -> gamepad1.y).onPress(robot.drive::resetHeading);
 * operator.when("A", "Fire (hold)", () -> gamepad2.a).whileHeld(robot.launcher.fire());
 * }</pre>
 *
 * <p>Start is reserved for pairing a gamepad on the Driver Station: a press made while it is held
 * fires nothing ({@link PairingGuard}), and Start itself is never bound.
 *
 * <p>Bind in {@code onInit()}. {@code RobotOpMode} polls every binding once per loop after PLAY —
 * never during INIT, when the robot is not allowed to move.
 *
 * <p>Ported from DECODE's {@code GamepadBindings}; the change is the label. DECODE kept a separate
 * hand-written {@code controlsSummary()} list, which nothing kept in step with the real bindings.
 */
public final class Bindings {

    private final String title;
    private final PairingGuard pairing;
    private final List<Trigger> triggers = new ArrayList<>();
    private final List<String> labels = new ArrayList<>();

    /** Bindings with no pairing guard: for tests and rigs. */
    public Bindings(String title) {
        this(title, () -> false);
    }

    /**
     * {@code start}: whether this gamepad's Start is held. A press made while it is held, pairing
     * the gamepad (Start + A, Start + B), fires nothing ({@link PairingGuard}). Never bind Start.
     */
    public Bindings(String title, BooleanSupplier start) {
        this.title = title;
        this.pairing = new PairingGuard(start);
    }

    /** Bind {@code condition}, shown on the Controls page as "{@code input} — {@code action}". */
    public Trigger when(String input, String action, BooleanSupplier condition) {
        Trigger trigger = new Trigger(pairing.guard(condition));
        triggers.add(trigger);
        labels.add(input + " — " + action);
        return trigger;
    }

    /**
     * Document an input the OpMode reads itself — a stick, a held modifier — so it still appears
     * on the Controls page. Adds nothing to poll.
     */
    public void note(String input, String action) {
        labels.add(input + " — " + action);
    }

    /** Called once per loop by {@code RobotOpMode}. Index loop: no iterator per loop. */
    public void update() {
        for (int i = 0; i < triggers.size(); i++) {
            triggers.get(i).poll();
        }
    }

    /**
     * Treat every button as already in its current state, so one held through PLAY — say B, still
     * down from choosing Red in INIT — does not count as a fresh press. Called at PLAY.
     */
    public void prime() {
        for (int i = 0; i < triggers.size(); i++) {
            triggers.get(i).prime();
        }
    }

    public String title() {
        return title;
    }

    /** Every label, in the order bound. */
    public List<String> labels() {
        return labels;
    }

    /** One polled condition and what it does on its edges. */
    public static final class Trigger {
        private final BooleanSupplier condition;
        private boolean last;
        private final List<Runnable> onRise = new ArrayList<>();
        private final List<Runnable> onFall = new ArrayList<>();
        private final List<Command> held = new ArrayList<>();

        Trigger(BooleanSupplier condition) {
            this.condition = condition;
        }

        /**
         * Run {@code action} once when pressed. For one-shot calls on a subsystem
         * ({@code robot.drive::resetHeading}) — no command, so nothing competes with the
         * subsystem's own update.
         */
        public Trigger onPress(Runnable action) {
            onRise.add(action);
            return this;
        }

        /** Schedule {@code command} when pressed; it runs to its own end. */
        public Trigger onTrue(Command command) {
            onRise.add(command::schedule);
            return this;
        }

        /** Run {@code command} while held; cancel it on release. */
        public Trigger whileHeld(Command command) {
            held.add(command);
            onFall.add(command::cancel);
            return this;
        }

        void prime() {
            last = condition.getAsBoolean();
        }

        void poll() {
            boolean now = condition.getAsBoolean();
            if (now && !last) {
                for (int i = 0; i < onRise.size(); i++) onRise.get(i).run();
            }
            if (!now && last) {
                for (int i = 0; i < onFall.size(); i++) onFall.get(i).run();
            }
            if (now) {
                for (int i = 0; i < held.size(); i++) {
                    Command command = held.get(i);
                    if (!command.isScheduled()) command.schedule();
                }
            }
            last = now;
        }
    }
}
