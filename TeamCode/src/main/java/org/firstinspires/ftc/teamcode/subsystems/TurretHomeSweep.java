package org.firstinspires.ftc.teamcode.subsystems;

/**
 * An Autonomous's INIT, on a turret with a home switch: turn the turret to find its mark, and
 * find it precisely (19429; {@code TurretSubsystem.homeSweep}). Pure: it says where the turret
 * should be now and how fast that moves, and {@code TurretSubsystem} holds it there with its own
 * loop, the rate fed forward. Only an Autonomous's INIT runs it (R103.B, through
 * {@code Robot.holdForAutoInit()}); a TeleOp's INIT moves nothing.
 *
 * <ol>
 *   <li><b>Search</b> ({@link State#SEARCH}): counter-clockwise at the search speed for up to a full
 *       turn and a bit ({@link Limits#searchRangeDeg}). The turret has no stops and no cables, so a
 *       full turn always crosses the magnet. As soon as the turret leaves the switch's band (a pass,
 *       or a turret that started on the magnet leaving it), it goes on past.</li>
 *   <li><b>Past and back, twice</b> ({@link State#PAST}, {@link State#FINE_BACK}, {@link State#FINE_ON}):
 *       on {@link Limits#overshootDeg} beyond where it left the band, then back across it slowly
 *       (a pass clockwise), on past again, and across once more counter-clockwise (a pass
 *       counter-clockwise).</li>
 *   <li><b>The mark</b> ({@link State#FOUND}) is the average of the two slow passes. A Hall switch
 *       turns on a little late one way and off a little late the other, and the loop places each
 *       edge a little behind the turret; both errors go with the direction of travel, so two
 *       passes in opposite directions cancel them.</li>
 * </ol>
 *
 * <p>No pass from the search is taken as the mark: the zero moves once, at the end. A search that
 * finds nothing in its range, or the time limit, is {@link State#NOT_FOUND}; slow passes that do not
 * come after a fast one did are {@link State#ROUGH}, the mark then the fast pass's.
 */
public final class TurretHomeSweep {

    public enum State {
        /** Not asked for this OpMode. */
        IDLE("no sweep"),
        SEARCH("searching for the home switch"),
        PAST("going past the home switch"),
        FINE_BACK("crossing the home switch slowly, clockwise"),
        FINE_ON("crossing the home switch slowly, counter-clockwise"),
        FOUND("the sweep found the mark"),
        /** Only the fast search's pass: the mark is that pass's. */
        ROUGH("the sweep found the mark on its fast pass only"),
        NOT_FOUND("the sweep did not find the magnet"),
        /** Ended by something else: PLAY, a zero set by hand, or the hold dropped. */
        STOPPED("sweep stopped");

        public final String text;

        State(String text) {
            this.text = text;
        }
    }

    /** The sweep's settings, the subsystem's {@code @Configurable} values copied in each loop. */
    public static final class Limits {
        /** Search speed, deg/s: under the switch's pass limit. */
        public double searchDps = 60.0;
        /** The slow passes' speed, deg/s: over the servo's floor. */
        public double fineDps = 15.0;
        /** How far the search goes, degrees: a full turn and the band's width. */
        public double searchRangeDeg = 370.0;
        /** How far past the band it goes before it turns back, degrees. */
        public double overshootDeg = 10.0;
        /** The widest band the switch can have, degrees: a slow pass not seen within twice the overshoot and this has failed. */
        public double maxBandDeg = 30.0;
        /** Give up after this long, seconds. */
        public double timeoutS = 20.0;
    }

    private State state = State.IDLE;
    private double originDeg;
    private double targetDeg;
    private int direction;
    private double speedDps;
    private boolean sawMagnet;
    /** Where the turret turns back, degrees (the PAST legs). */
    private double turnAtDeg;
    /** Where the current slow leg started, degrees. */
    private double legStartDeg;
    private long startNs;
    private long lastNs;
    private double searchMarkCounts;
    private double backMarkCounts;
    private double markCounts;
    /** The leg the PAST leads into. */
    private State next;

    /** Back to {@link State#IDLE}: a new OpMode. */
    public void reset() {
        state = State.IDLE;
        markCounts = Double.NaN;
    }

    /** Start from {@code startDeg}, the turret's angle now, in the frame the hold uses. */
    public void start(long nowNs, double startDeg, Limits limits) {
        state = State.SEARCH;
        originDeg = startDeg;
        targetDeg = startDeg;
        direction = 1;
        speedDps = limits.searchDps;
        sawMagnet = false;
        startNs = nowNs;
        lastNs = nowNs;
        searchMarkCounts = Double.NaN;
        backMarkCounts = Double.NaN;
        markCounts = Double.NaN;
    }

    /**
     * One loop.
     *
     * @param angleDeg  the turret's angle now, the hold's frame
     * @param magnet    the switch sees the magnet now
     * @param pass      this loop's result from {@link TurretHomeSwitch}
     * @param passDir   that pass's direction, +1 counter-clockwise
     * @param passMark  that pass's mark, hub counts
     * @return whether the state changed
     */
    public boolean step(long nowNs, double angleDeg, boolean magnet, TurretHomeSwitch.Result pass, int passDir,
                        double passMark, Limits limits) {
        if (!sweeping()) return false;
        if ((nowNs - startNs) / 1e9 > limits.timeoutS) {
            return fail();
        }
        double dt = Math.max(0.0, Math.min(0.1, (nowNs - lastNs) / 1e9));   // a long loop is not a jump
        lastNs = nowNs;
        targetDeg += direction * speedDps * dt;
        boolean took = pass == TurretHomeSwitch.Result.PASS;
        boolean left = sawMagnet && !magnet;
        if (magnet) sawMagnet = true;
        switch (state) {
            case SEARCH:
                if (took) searchMarkCounts = passMark;
                if (took || left) {
                    goPast(angleDeg, limits, State.FINE_BACK);
                    return true;
                }
                if (targetDeg - originDeg >= limits.searchRangeDeg) {
                    return fail();
                }
                return false;
            case PAST:
                if (direction * (targetDeg - turnAtDeg) >= 0.0) {
                    targetDeg = turnAtDeg;
                    direction = -direction;
                    speedDps = limits.fineDps;
                    legStartDeg = targetDeg;
                    state = next;
                    return true;
                }
                return false;
            case FINE_BACK:
            case FINE_ON:
                int want = state == State.FINE_BACK ? -1 : 1;
                if (took && passDir == want) {
                    if (state == State.FINE_BACK) {
                        backMarkCounts = passMark;
                        goPast(angleDeg, limits, State.FINE_ON);
                    } else {
                        markCounts = (backMarkCounts + passMark) / 2.0;
                        state = State.FOUND;
                    }
                    return true;
                }
                if (Math.abs(targetDeg - legStartDeg) > 2.0 * limits.overshootDeg + limits.maxBandDeg) {
                    return fail();
                }
                return false;
            default:
                return false;
        }
    }

    /** On past where the turret left the band, the same way, at the speed it is going. */
    private void goPast(double angleDeg, Limits limits, State then) {
        turnAtDeg = (Double.isNaN(angleDeg) ? targetDeg : angleDeg) + direction * limits.overshootDeg;
        sawMagnet = false;
        next = then;
        state = State.PAST;
    }

    /** Out of range or time: the best mark it has, else none. */
    private boolean fail() {
        if (!Double.isNaN(backMarkCounts) || !Double.isNaN(searchMarkCounts)) {
            // A slow pass beats the fast one.
            markCounts = !Double.isNaN(backMarkCounts) ? backMarkCounts : searchMarkCounts;
            state = State.ROUGH;
        } else {
            state = State.NOT_FOUND;
        }
        return true;
    }

    /** End it early ({@link State#STOPPED}); nothing if it is not running. */
    public boolean stop() {
        if (!sweeping()) return false;
        state = State.STOPPED;
        return true;
    }

    /** Whether it is turning the turret. */
    public boolean sweeping() {
        return state == State.SEARCH || state == State.PAST || state == State.FINE_BACK || state == State.FINE_ON;
    }

    public State state() {
        return state;
    }

    /** The mark it found, hub counts: {@link State#FOUND} or {@link State#ROUGH}; NaN otherwise. */
    public double markCounts() {
        return markCounts;
    }

    /** Where the turret should be now, degrees, the hold's frame. */
    public double targetDeg() {
        return targetDeg;
    }

    /** How fast that moves, degrees per second counter-clockwise; 0 when not sweeping. */
    public double rateDps() {
        return sweeping() ? direction * speedDps : 0.0;
    }
}
