package org.firstinspires.ftc.teamcode.opmodes;

import org.firstinspires.ftc.teamcode.controls.Display;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * The practice readout ({@link TeleOpConfig#practiceReadout}): every pick-up and every cycle timed
 * the moment it ends, for the drive team's cycle drill (doc/drive-team-cycle-playbook.md).
 *
 * <p>The phases are {@code tools/pull-logs/read_logs.py cycles}' ({@code cycle_rows}), so the
 * number on the Driver Station is the one the after-practice report prints:
 * <ul>
 *   <li>A <b>cycle</b> runs from the end of one feed to the end of the next; the first from PLAY.</li>
 *   <li>A <b>feed</b> is the feed gate's open window. One that opens again within {@link #MERGE_S}
 *       of closing (a press let go and pressed again) is the same feed. Its balls are the launches
 *       counted from the gate opening to {@link #SHOTS_AFTER_CLOSE_S} after it closed.</li>
 *   <li>A feed that launched nothing (an empty lane, a jam) is no volley: the cycle goes on, and
 *       it is counted on the next volley as an empty feed.</li>
 *   <li>The split: <i>drive to pieces</i>, until the first piece in; <i>pick up</i>, first piece to
 *       last; <i>drive loaded</i>, last piece to the fire press; <i>spin-up and aim</i>, the press
 *       to the gate opening; <i>feed</i>, gate open to gate shut. The press is the first after the
 *       last piece in; one before it is an early press, counted but not timed from. No press (a
 *       Square volley) times from the gate opening. Pieces that come in while the gate is open
 *       belong to neither cycle, as in the report.</li>
 * </ul>
 * The <b>pick-up</b> line is the drill's own: first piece in to the lane reading full (the light's
 * green), or, when the gate opens on a lane not yet full, first piece to last ("fired short").
 *
 * <p>Fed once a loop ({@link #update}); nothing is allocated there. The lines are built only when a
 * pick-up or a cycle ends, and each is one match-log event line ("practice: …") for
 * {@code read_logs.py practice}.
 */
public final class CycleReadout {

    /** A feed gate opening again within this long of closing is the same feed, seconds ({@code merge_s}). */
    public static final double MERGE_S = 1.0;
    /** A feed's balls are counted up to this long after the gate shuts, seconds (the report's {@code gb + 0.4}). */
    public static final double SHOTS_AFTER_CLOSE_S = 0.4;
    /** The cycle budget a volley is measured against, seconds ({@code CYCLE_BUDGET_S}). */
    public static final double BUDGET_S = 9.3;
    /** And the pieces a volley should carry ({@code CYCLE_BUDGET_PIECES}). */
    public static final int BUDGET_PIECES = 4;

    /** Piece-in and press times kept for one cycle; past this the last slot is overwritten. */
    static final int MAX_EVENTS = 64;
    /** Cycles and pick-ups kept for the session's best and median; past this the oldest is dropped. */
    static final int MAX_SESSION = 256;

    private enum Gate { SHUT, OPEN, CLOSING }

    private final Consumer<String> log;

    private double cycleStartS;
    private final double[] ins = new double[MAX_EVENTS];
    private int nIns;
    private final double[] presses = new double[MAX_EVENTS];
    private int nPresses;
    private int emptyFeeds;
    private boolean pickUpShown;
    /** A pick-up shown while the last feed was still being counted: it belongs to the next cycle if that feed was a volley. */
    private boolean pickUpWhileClosing;
    private boolean lastFull;

    private Gate gate = Gate.SHUT;
    private double openS, closeS;
    private int launchedAtOpen, launchedAtClose;
    private boolean closeCounted;

    private int cycles;
    private final double[] cycleTimes = new double[MAX_SESSION];
    private int nCycleTimes;
    private final double[] pickUpTimes = new double[MAX_SESSION];
    private int nPickUpTimes;
    private final double[] scratch = new double[MAX_SESSION];

    // The last result, for the Driver Station and the tests.
    private double lastPickUpS = Double.NaN;
    private int lastPickUpPieces;
    private boolean lastPickUpFull;
    private double lastCycleS = Double.NaN;
    private int lastBalls;
    private double toPiecesS, pickUpS, loadedS, spinAimS, feedS;
    private int lastEmptyFeeds, lastEarlyPresses;

    private String pickUpLine = "";
    private String cycleLine = "";
    private String splitLine = "";
    private String statsLine = "";

    /** @param log where each pick-up and cycle goes, one line each (the match log's events) */
    public CycleReadout(Consumer<String> log) {
        this.log = log;
        start(0.0);
    }

    /** PLAY: the first cycle starts here, and the session's numbers start over. */
    public void start(double tS) {
        cycleStartS = tS;
        nIns = nPresses = emptyFeeds = 0;
        pickUpShown = pickUpWhileClosing = lastFull = false;
        gate = Gate.SHUT;
        cycles = nCycleTimes = nPickUpTimes = 0;
        lastPickUpS = lastCycleS = Double.NaN;
        pickUpLine = cycleLine = splitLine = "";
        statsLine = "PRACTICE: fire a volley to time a cycle";
    }

    /**
     * One loop.
     *
     * @param tS         seconds since PLAY
     * @param pieceIn    a piece came into the lane ({@code LaneTracker.pieceArrived()})
     * @param laneFull   the lane reads full, the light's green ({@code colorSensor.full()})
     * @param firePress  the driver's fire went down this loop (the match log's "fire (driver's …" line)
     * @param gateOpen   the feed gate is open, feeding
     * @param launched   the launches so far ({@code launcher.launched()})
     */
    public void update(double tS, boolean pieceIn, boolean laneFull, boolean firePress, boolean gateOpen, int launched) {
        if (pieceIn) push(ins, nIns++, tS);
        if (nIns > MAX_EVENTS) nIns = MAX_EVENTS;
        if (firePress) push(presses, nPresses++, tS);
        if (nPresses > MAX_EVENTS) nPresses = MAX_EVENTS;

        // The pick-up: the lane goes green with pieces taken in since the cycle started.
        if (laneFull && !lastFull && !pickUpShown && nIns > 0 && gate != Gate.OPEN) {
            pickUp(tS - ins[0], true);
        }
        lastFull = laneFull;

        switch (gate) {
            case SHUT:
                if (gateOpen) {
                    gate = Gate.OPEN;
                    pickUpWhileClosing = false;
                    openS = tS;
                    launchedAtOpen = launched;
                    // Fired before the lane went green: the pick-up ends at the last piece in.
                    int n = countUpTo(ins, nIns, openS);
                    if (!pickUpShown && n > 0) pickUp(ins[n - 1] - ins[0], false);
                }
                break;
            case OPEN:
                if (!gateOpen) {
                    gate = Gate.CLOSING;
                    closeS = tS;
                    closeCounted = false;
                }
                break;
            case CLOSING:
                if (gateOpen) {
                    gate = Gate.OPEN; // the same feed: pressed again within MERGE_S
                    break;
                }
                if (!closeCounted && tS >= closeS + SHOTS_AFTER_CLOSE_S) {
                    launchedAtClose = launched;
                    closeCounted = true;
                }
                if (tS >= closeS + MERGE_S) {
                    if (!closeCounted) launchedAtClose = launched;
                    gate = Gate.SHUT;
                    feedEnded(laneFull);
                }
                break;
        }
    }

    private void feedEnded(boolean laneFull) {
        int balls = launchedAtClose - launchedAtOpen;
        if (balls <= 0) {
            emptyFeeds++;
            return;
        }
        int nIn = countUpTo(ins, nIns, openS);
        int nPress = countUpTo(presses, nPresses, openS);
        // The press that started this volley: the first at or after the last piece in.
        int first = 0;
        if (nIn > 0) {
            while (first < nPress && presses[first] < ins[nIn - 1]) first++;
        }
        double press = first < nPress ? presses[first] : openS;
        double firstIn = nIn > 0 ? ins[0] : press;
        double lastIn = nIn > 0 ? ins[nIn - 1] : press;
        toPiecesS = firstIn - cycleStartS;
        pickUpS = lastIn - firstIn;
        loadedS = press - lastIn;
        spinAimS = openS - press;
        feedS = closeS - openS;
        lastCycleS = closeS - cycleStartS;
        lastBalls = balls;
        lastEmptyFeeds = emptyFeeds;
        lastEarlyPresses = first;
        cycles++;
        nCycleTimes = keep(cycleTimes, nCycleTimes, lastCycleS);

        cycleLine = Display.huge(String.format(Locale.US, "CYCLE %.1f s · %d ball%s", lastCycleS, balls,
                balls == 1 ? "" : "s"), lastCycleS <= BUDGET_S && balls >= BUDGET_PIECES ? Display.Level.OK : null);
        String extra = (emptyFeeds > 0 ? String.format(Locale.US, " · +%d empty feed%s", emptyFeeds, emptyFeeds == 1 ? "" : "s") : "")
                + (first > 0 ? String.format(Locale.US, " · %d early press%s", first, first == 1 ? "" : "es") : "");
        splitLine = String.format(Locale.US, "<small>to pieces %.1f | pick %.1f | loaded %.1f | spin+aim %.1f | feed %.1f%s</small>",
                toPiecesS, pickUpS, loadedS, spinAimS, feedS, extra);
        statsLine = statsLine();
        log.accept(String.format(Locale.US,
                "practice: cycle %d %.1f s, %d balls: to pieces %.1f, pick up %.1f, drive loaded %.1f, spin-up and aim %.1f, feed %.1f s; %d empty feeds, %d early presses",
                cycles, lastCycleS, balls, toPiecesS, pickUpS, loadedS, spinAimS, feedS, emptyFeeds, first));

        // The next cycle starts at this feed's end; what came in after it is the next cycle's.
        cycleStartS = closeS;
        nIns = dropBefore(ins, nIns, closeS);
        nPresses = dropBefore(presses, nPresses, closeS);
        emptyFeeds = 0;
        pickUpShown = pickUpWhileClosing;
        // The lane was green already as this feed's count closed (left over, or filled meanwhile).
        if (!pickUpShown && laneFull && nIns > 0) pickUp(ins[nIns - 1] - ins[0], true);
    }

    private void pickUp(double s, boolean full) {
        pickUpShown = true;
        if (gate == Gate.CLOSING) pickUpWhileClosing = true;
        int pieces = countUpTo(ins, nIns, Double.POSITIVE_INFINITY);
        lastPickUpS = s;
        lastPickUpPieces = pieces;
        lastPickUpFull = full;
        // Only a full load's pick-up is a time to beat: a fired-short one (one ball, 0 s) would win
        // best and pull the median down. It still shows, amber, as the last pick-up.
        if (full) nPickUpTimes = keep(pickUpTimes, nPickUpTimes, s);
        pickUpLine = Display.huge(String.format(Locale.US, "PICK-UP %.1f s (%d in)", s, pieces),
                full ? Display.Level.OK : Display.Level.WARN);
        statsLine = statsLine();
        log.accept(String.format(Locale.US, "practice: pick-up %.1f s, %d in, %s", s, pieces,
                full ? "lane full" : "fired short"));
    }

    private String statsLine() {
        String c = nCycleTimes == 0 ? "no cycle yet" : String.format(Locale.US, "cycle best %.1f · median %.1f s (%d)",
                best(cycleTimes, nCycleTimes), median(cycleTimes, nCycleTimes), nCycleTimes);
        String p = nPickUpTimes == 0 ? "" : String.format(Locale.US, " · pick-up best %.1f · median %.1f",
                best(pickUpTimes, nPickUpTimes), median(pickUpTimes, nPickUpTimes));
        return "<small>" + c + p + "</small>";
    }

    /** Write the readout to the top of the Match page: the last pick-up, the last cycle and its split, the session's best and median. */
    public void show(Display display) {
        if (!pickUpLine.isEmpty()) display.line(pickUpLine);
        if (!cycleLine.isEmpty()) {
            display.line(cycleLine);
            display.line(splitLine);
        }
        display.line(statsLine);
    }

    // --- the last result -------------------------------------------------------------------

    public int cycles() { return cycles; }
    public double lastCycleS() { return lastCycleS; }
    public int lastBalls() { return lastBalls; }
    public double toPiecesS() { return toPiecesS; }
    public double pickUpPhaseS() { return pickUpS; }
    public double driveLoadedS() { return loadedS; }
    public double spinUpAimS() { return spinAimS; }
    public double feedS() { return feedS; }
    public int lastEmptyFeeds() { return lastEmptyFeeds; }
    public int lastEarlyPresses() { return lastEarlyPresses; }
    public double lastPickUpS() { return lastPickUpS; }
    public int lastPickUpPieces() { return lastPickUpPieces; }
    public boolean lastPickUpFull() { return lastPickUpFull; }
    public double bestCycleS() { return nCycleTimes == 0 ? Double.NaN : best(cycleTimes, nCycleTimes); }
    public double medianCycleS() { return nCycleTimes == 0 ? Double.NaN : median(cycleTimes, nCycleTimes); }
    public double bestPickUpS() { return nPickUpTimes == 0 ? Double.NaN : best(pickUpTimes, nPickUpTimes); }
    public double medianPickUpS() { return nPickUpTimes == 0 ? Double.NaN : median(pickUpTimes, nPickUpTimes); }

    // --- helpers -----------------------------------------------------------------------------

    private static void push(double[] a, int i, double v) {
        a[Math.min(i, a.length - 1)] = v;
    }

    /** How many of the first {@code n} (sorted) times are at or before {@code t}. */
    private static int countUpTo(double[] a, int n, double t) {
        int k = 0;
        while (k < n && a[k] <= t) k++;
        return k;
    }

    /** Drop the times before {@code t}; returns the new count. */
    private static int dropBefore(double[] a, int n, double t) {
        int k = 0;
        while (k < n && a[k] < t) k++;
        System.arraycopy(a, k, a, 0, n - k);
        return n - k;
    }

    private static int keep(double[] a, int n, double v) {
        if (n == a.length) {
            System.arraycopy(a, 1, a, 0, n - 1);
            n--;
        }
        a[n] = v;
        return n + 1;
    }

    private static double best(double[] a, int n) {
        double b = Double.POSITIVE_INFINITY;
        for (int i = 0; i < n; i++) b = Math.min(b, a[i]);
        return b;
    }

    /** The report's median: the sorted middle, the upper one of an even count. */
    private double median(double[] a, int n) {
        System.arraycopy(a, 0, scratch, 0, n);
        Arrays.sort(scratch, 0, n);
        return scratch[n / 2];
    }
}
