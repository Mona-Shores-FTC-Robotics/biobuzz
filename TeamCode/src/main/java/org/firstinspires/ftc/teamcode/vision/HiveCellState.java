package org.firstinspires.ftc.teamcode.vision;

/**
 * Which of its two resting positions a {@link HiveCell} is currently in.
 *
 * <p>The cells pivot between two mechanically-defined positions and dwell there.
 * That is what makes a field pose tractable at all: each cluster has two known
 * poses rather than a continuum, so "where is this cell" reduces to "which of the
 * two, and what are the two".
 */
public enum HiveCellState {

    /** Settled in the raised position. */
    UP,

    /** Settled in the lowered position. */
    DOWN,

    /**
     * Not confidently either — mid-tip, not yet settled long enough to trust, or
     * the classifier has not been given the measured geometry it needs.
     *
     * <p>Treat this as "do not derive a field pose from this cell right now",
     * never as a default to fall back on.
     */
    UNKNOWN
}
