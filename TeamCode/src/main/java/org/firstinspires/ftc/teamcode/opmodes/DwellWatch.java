package org.firstinspires.ftc.teamcode.opmodes;

/**
 * When a grab's dwell can end, from the intake roller's current. A piece passing the roller loads
 * it: a spike several amps over its free-running current (about 0.35 A against 5–6 A on 29 Oct).
 * So the dwell is over once the current has been quiet for {@code quietMs} after its last spike
 * (the pieces are in), or has had no spike by {@code emptyMs} into the dwell (nothing came off).
 * The grab's own {@code dwellMs} stays the cap.
 *
 * <p>The baseline is the lowest current seen since the grab started the roller, so it is learned
 * before the dwell, while the roller runs free; a spike ignores it. It cannot count pieces: spikes
 * from pieces arriving together merge. It only says "still coming" or "done".
 *
 * <p>Plain Java with no SDK types, so it is unit tested. The caller passes the time.
 */
final class DwellWatch {

    private double baselineAmps = Double.NaN;
    private double lastSpikeMs = Double.NaN;

    /** A new grab: no baseline, no spike. */
    void reset() {
        baselineAmps = Double.NaN;
        lastSpikeMs = Double.NaN;
    }

    /**
     * A current reading, from the roller's start on (a spike during the In counts too).
     *
     * @param nowMs any clock, the same one {@link #done} is given
     * @param amps  NaN when not read (it is skipped)
     */
    void observe(double nowMs, double amps, double spikeAboveAmps) {
        if (Double.isNaN(amps)) return;
        if (Double.isNaN(baselineAmps) || amps < baselineAmps) baselineAmps = amps;
        if (amps > baselineAmps + spikeAboveAmps) lastSpikeMs = nowMs;
    }

    /**
     * Whether the dwell that began at {@code dwellStartMs} may end at {@code nowMs}: with no spike
     * yet, once {@code emptyMs} have passed; after one, once {@code quietMs} have passed since the
     * last. Each is off at 0 or less, on its own: off, that case dwells the grab's full time.
     *
     * <p>Until 23 Nov 2026 either at 0 turned both off, so with {@code FlowerPicker}'s
     * {@code dwellQuietMs} 0 (the mentor's call, 1 Nov) its {@code dwellEmptyMs} 1300 did nothing:
     * at Belding and Fruitport 10 of 45 Auto FLOWER picks took nothing and dwelt the whole 2.0 s
     * cap, 7 of them with no spike at all.
     */
    boolean done(double nowMs, double dwellStartMs, double quietMs, double emptyMs) {
        if (Double.isNaN(lastSpikeMs)) return emptyMs > 0.0 && nowMs - dwellStartMs >= emptyMs;
        return quietMs > 0.0 && nowMs - lastSpikeMs >= quietMs;
    }

    /** Whether a piece came in: a spike since the roller started. */
    boolean sawSpike() {
        return !Double.isNaN(lastSpikeMs);
    }
}
