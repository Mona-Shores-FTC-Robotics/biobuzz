package org.firstinspires.ftc.teamcode.subsystems;

/**
 * How the flywheels answer a step, measured from the loop's own readings, so the velocity PIDF
 * ({@link LauncherSubsystem#velocityP} and the rest) is tuned from numbers and not by eye. Two
 * kinds of step, each measured to the moment the launcher says at speed again:
 *
 * <ul>
 *   <li><b>A spin-up</b>, from the command (spin up from idle, a new target, the end of an unjam):
 *       <i>rise</i>, command to the first loop both wheels are within tolerance; <i>overshoot</i>,
 *       the most either wheel went over its target before at speed; <i>settle</i>, command to at
 *       speed (in tolerance for {@code atSpeedHoldMs}): what the feeder actually waits.</li>
 *   <li><b>A shot's dip</b>, as {@code ShotDetector} finds it, timed from the loop the wheels left
 *       tolerance: <i>depth</i>, how far below target the slower wheel fell; <i>recover</i>, back
 *       into tolerance; <i>overshoot</i>, the most either wheel went over its target after that,
 *       until at speed; <i>resettle</i>, back to at speed. Overshoot past the tolerance after a dip
 *       is what held at-speed false for about 0.7 s on 3 and 4 Nov 2026.</li>
 * </ul>
 *
 * <p>A result is kept only when it completes; one in progress is dropped by {@link #cancel()} (a
 * spin-down, an unjam, an encoder fault). Counts tell a reader a new result has landed. Pure: the
 * caller passes the clock and the readings, so a test drives it with made-up ones.
 */
public final class LauncherResponse {

    /** A dip still settling after this long is closed with no resettle time: the wheels are hunting. */
    static final double DIP_GIVE_UP_MS = 3000.0;

    // A spin-up in progress.
    private long commandNs = -1;
    private double peakExcess;
    private double riseMs = Double.NaN;
    private double fromTps;

    // A dip in progress: 0 none, 1 down in it, 2 back in tolerance and settling.
    private int dipPhase;
    private long dipStartNs;
    private double dipPeakAfter;
    private double dipRecoverMs;
    private double dipDepth;

    private boolean wasWithin;
    private long leftBandNs = -1;
    private boolean wasInDip;

    // The last completed of each.
    private int spinUps;
    private double lastRiseMs = Double.NaN;
    private double lastSpinOvershoot = Double.NaN;
    private double lastSettleMs = Double.NaN;
    private double lastFromTps = Double.NaN;
    private int dips;
    private double lastDipDepth = Double.NaN;
    private double lastRecoverMs = Double.NaN;
    private double lastDipOvershoot = Double.NaN;
    private double lastResettleMs = Double.NaN;

    /**
     * A spin-up starts now: from rest, to a new target, or out of an unjam. {@code fromTps} is the
     * slower wheel's speed at the command, so a reader can tell a spin-up from rest from a nudge.
     * Any dip being measured is dropped (its target is gone).
     */
    public void command(long nowNs, double fromTps) {
        commandNs = nowNs;
        peakExcess = Double.NEGATIVE_INFINITY;
        riseMs = Double.NaN;
        this.fromTps = fromTps;
        dipPhase = 0;
        wasWithin = false;
        leftBandNs = -1;
        wasInDip = false;
    }

    /** The wheels stopped being asked for speed (spin-down, unjam, fault): drop whatever is in progress. */
    public void cancel() {
        commandNs = -1;
        if (dipPhase == 2) closeDip(Double.NaN);
        dipPhase = 0;
        wasWithin = false;
        leftBandNs = -1;
        wasInDip = false;
    }

    /**
     * One loop's readings while spinning toward a target.
     *
     * @param within   both wheels inside the at-speed tolerance this loop
     * @param atSpeed  the launcher's at-speed (within for the hold time)
     * @param excess   how far over its target the faster wheel is, ticks per second (negative when under)
     * @param inDip    {@code ShotDetector} is in a dip
     * @param shotDip  {@code ShotDetector} counted a shot this loop
     * @param dipDepth that shot's depth, ticks per second
     */
    public void update(long nowNs, boolean within, boolean atSpeed, double excess,
                       boolean inDip, boolean shotDip, double dipDepth) {
        if (wasWithin && !within) leftBandNs = nowNs;
        wasWithin = within;

        if (commandNs >= 0) {
            if (excess > peakExcess) peakExcess = excess;
            if (within && Double.isNaN(riseMs)) riseMs = ms(nowNs - commandNs);
            if (atSpeed) {
                spinUps++;
                lastRiseMs = Double.isNaN(riseMs) ? ms(nowNs - commandNs) : riseMs;
                lastSpinOvershoot = Math.max(0.0, peakExcess);
                lastSettleMs = ms(nowNs - commandNs);
                lastFromTps = fromTps;
                commandNs = -1;
            }
        }

        boolean dipStarted = inDip && !wasInDip;
        wasInDip = inDip;
        if (dipStarted) {
            if (dipPhase == 2) closeDip(Double.NaN);
            dipPhase = 1;
            dipStartNs = leftBandNs >= 0 ? leftBandNs : nowNs;
        }
        if (dipPhase == 1) {
            if (shotDip) {
                dipPhase = 2;
                dipRecoverMs = ms(nowNs - dipStartNs);
                this.dipDepth = dipDepth;
                dipPeakAfter = excess;
            } else if (!inDip) {
                dipPhase = 0; // the detector dropped it: not a shot
            }
        } else if (dipPhase == 2) {
            if (excess > dipPeakAfter) dipPeakAfter = excess;
            if (atSpeed) {
                closeDip(ms(nowNs - dipStartNs));
            } else if (ms(nowNs - dipStartNs) > DIP_GIVE_UP_MS) {
                closeDip(Double.NaN);
            }
        }
    }

    private void closeDip(double resettleMs) {
        dips++;
        lastDipDepth = dipDepth;
        lastRecoverMs = dipRecoverMs;
        lastDipOvershoot = Math.max(0.0, dipPeakAfter);
        lastResettleMs = resettleMs;
        dipPhase = 0;
    }

    private static double ms(long ns) {
        return ns / 1e6;
    }

    /** True while a spin-up is being measured (commanded, not yet at speed). */
    public boolean spinningUp() {
        return commandNs >= 0;
    }

    /** How long the spin-up in progress has run, ms; NaN when none is. */
    public double spinUpElapsedMs(long nowNs) {
        return commandNs >= 0 ? ms(nowNs - commandNs) : Double.NaN;
    }

    /** Completed spin-ups since this OpMode started. */
    public int spinUps() {
        return spinUps;
    }

    /** The last completed spin-up's rise: command to first within tolerance, ms. */
    public double riseMs() {
        return lastRiseMs;
    }

    /** The last completed spin-up's peak overshoot over target, ticks per second (0 when it never went over). */
    public double spinOvershootTps() {
        return lastSpinOvershoot;
    }

    /** The last completed spin-up's settle: command to at speed, ms. */
    public double settleMs() {
        return lastSettleMs;
    }

    /** The slower wheel's speed when the last completed spin-up was commanded, ticks per second. */
    public double spinFromTps() {
        return lastFromTps;
    }

    /** Completed dips since this OpMode started. */
    public int dips() {
        return dips;
    }

    /** The last dip's depth below target, ticks per second. */
    public double dipDepthTps() {
        return lastDipDepth;
    }

    /** The last dip's recovery: leaving tolerance to back in it, ms. */
    public double recoverMs() {
        return lastRecoverMs;
    }

    /** The last dip's overshoot over target after recovering, ticks per second (0 when none). */
    public double dipOvershootTps() {
        return lastDipOvershoot;
    }

    /** The last dip's resettle: leaving tolerance to at speed again, ms; NaN when it never settled. */
    public double resettleMs() {
        return lastResettleMs;
    }
}
