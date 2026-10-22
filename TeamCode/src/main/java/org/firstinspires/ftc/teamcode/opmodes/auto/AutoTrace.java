package org.firstinspires.ftc.teamcode.opmodes.auto;

import java.util.function.Consumer;

/**
 * Where an Auto's trace goes: every line {@link org.firstinspires.ftc.teamcode.autokit.AutoKit} reports (a card
 * starting, a wait ending and why, the endgame guard) is sent to the match log as an event, and the last few are kept
 * for the Match page.
 *
 * <p>The log lines start with {@link #EVENT_PREFIX}, as the simulator writes them, so a match log from the robot reads
 * like a simulated one and the same tools line the two up card by card (doc/auto-code-structure.md).
 *
 * <p>It runs only when a card starts or a wait ends, a few times a second, never every loop: one short string per line.
 */
public final class AutoTrace implements Consumer<String> {

    /** What every trace line starts with in the match log's events, on the robot and in the simulator. */
    public static final String EVENT_PREFIX = "auto: ";

    private final Consumer<String> events;
    private final String[] recent;
    private int count;

    /**
     * @param keep   how many of the latest lines to keep for the Match page
     * @param events where each line goes, already prefixed (the match log's {@code event})
     */
    public AutoTrace(int keep, Consumer<String> events) {
        if (keep < 1) throw new IllegalArgumentException("keep at least one line");
        this.recent = new String[keep];
        this.events = events;
    }

    @Override
    public void accept(String line) {
        recent[count % recent.length] = line;
        count++;
        events.accept(EVENT_PREFIX + line);
    }

    /** The latest lines, oldest first, to {@code out}. */
    public void forEachRecent(Consumer<String> out) {
        for (int i = Math.max(0, count - recent.length); i < count; i++) {
            out.accept(recent[i % recent.length]);
        }
    }
}
