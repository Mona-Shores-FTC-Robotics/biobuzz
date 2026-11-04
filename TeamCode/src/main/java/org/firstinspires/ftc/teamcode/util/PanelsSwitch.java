package org.firstinspires.ftc.teamcode.util;

import com.bylazar.panels.core.PreferencesHandler;

/**
 * Whether Panels is switched on: the setting the Driver Station's Panels enable/disable OpMode
 * writes, read once per OpMode at INIT.
 *
 * <p><b>Why our code has to ask.</b> Disabling Panels stops its web and socket servers and nothing
 * else. Every field send and telemetry update still turns its data into JSON and builds a log line
 * before it finds there is nobody to send to (read from Panels 1.0.19's bytecode:
 * {@code Plugin.send} serialises, logs, then calls a socket with no clients). A field send costs
 * the loop 7–8 ms that way. So when Panels is off, {@link FieldView} draws nothing and
 * {@code RobotOpMode} hands out no Panels telemetry, and the loop pays nothing for it. At an event
 * Panels is off (no Wi-Fi streaming in the venue), so that is where it matters. The match log does
 * not go through Panels and is unaffected.
 *
 * <p>Read once at INIT, never per loop: the setting is a {@code SharedPreferences} lookup, and it
 * changes only when the enable/disable OpMode runs, which is never during another OpMode.
 */
public final class PanelsSwitch {

    private PanelsSwitch() {
    }

    /**
     * Whether Panels is enabled. On if the setting cannot be read (Panels not started, or a
     * different Panels version): then the caller's own Panels calls decide, as they did before.
     */
    public static boolean enabled() {
        try {
            return PreferencesHandler.INSTANCE.isEnabled();
        } catch (RuntimeException | LinkageError e) {
            return true;
        }
    }
}
