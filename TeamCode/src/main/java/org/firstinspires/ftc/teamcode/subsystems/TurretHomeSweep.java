package org.firstinspires.ftc.teamcode.subsystems;

/**
 * An Autonomous's INIT, on a turret with a home switch: turn the turret slowly to find its mark
 * (19429; {@code TurretSubsystem.homeSweep}). Pure: it says where the turret should be now and how
 * fast that moves, and {@code TurretSubsystem} holds it there with its own loop, the rate fed
 * forward. Only an Autonomous's INIT runs it (R103.B, through {@code Robot.holdForAutoInit()}); a
 * TeleOp's INIT moves nothing.
 *
 * <p>The sweep starts where the turret was assumed on its mark and goes counter-clockwise up to the
 * range, then back clockwise to the range the other side. It turns back early once the switch has
 * seen the magnet and lost it: a turret that started in the band, or crossed it in a pass that was
 * not taken, then crosses the band whole on the way back. It ends as soon as a pass puts the zero on
 * the mark ({@link State#FOUND}), and gives up at the far end or the time limit
 * ({@link State#NOT_FOUND}), the zero then where it stood.
 */
public final class TurretHomeSweep {

    public enum State {
        /** Not asked for this OpMode. */
        IDLE("no sweep"),
        OUT("sweeping counter-clockwise for the mark"),
        BACK("sweeping clockwise for the mark"),
        FOUND("the sweep found the mark"),
        NOT_FOUND("the sweep did not find the magnet"),
        /** Ended by something else: PLAY, a zero set by hand, or the hold dropped. */
        STOPPED("sweep stopped");

        public final String text;

        State(String text) {
            this.text = text;
        }
    }

    private State state = State.IDLE;
    private double originDeg;
    private double targetDeg;
    private int direction;
    private boolean sawMagnet;
    private long startNs;
    private long lastNs;

    /** Back to {@link State#IDLE}: a new OpMode. */
    public void reset() {
        state = State.IDLE;
    }

    /** Start from {@code startDeg}, the turret's angle now, in the frame the hold uses. */
    public void start(long nowNs, double startDeg) {
        state = State.OUT;
        originDeg = startDeg;
        targetDeg = startDeg;
        direction = 1;
        sawMagnet = false;
        startNs = nowNs;
        lastNs = nowNs;
    }

    /**
     * One loop.
     *
     * @param magnet   the switch sees the magnet now
     * @param found    a pass has put the zero on the mark
     * @param rangeDeg how far either side of the start to look, degrees
     * @param speedDps how fast the target moves, degrees per second
     * @param timeoutS give up after this long, seconds
     * @return whether the state changed
     */
    public boolean step(long nowNs, boolean magnet, boolean found, double rangeDeg, double speedDps, double timeoutS) {
        if (!sweeping()) return false;
        if (found) {
            state = State.FOUND;
            return true;
        }
        if ((nowNs - startNs) / 1e9 > timeoutS) {
            state = State.NOT_FOUND;
            return true;
        }
        double dt = Math.max(0.0, Math.min(0.1, (nowNs - lastNs) / 1e9));   // a long loop is not a jump
        lastNs = nowNs;
        targetDeg += direction * speedDps * dt;
        if (magnet) sawMagnet = true;
        if (state == State.OUT) {
            if (targetDeg - originDeg >= rangeDeg || (sawMagnet && !magnet)) {
                targetDeg = Math.min(targetDeg, originDeg + rangeDeg);
                direction = -1;
                state = State.BACK;
                return true;
            }
        } else if (originDeg - targetDeg >= rangeDeg) {
            targetDeg = originDeg - rangeDeg;
            state = State.NOT_FOUND;
            return true;
        }
        return false;
    }

    /** End it early ({@link State#STOPPED}); nothing if it is not running. */
    public boolean stop() {
        if (!sweeping()) return false;
        state = State.STOPPED;
        return true;
    }

    /** Whether it is turning the turret. */
    public boolean sweeping() {
        return state == State.OUT || state == State.BACK;
    }

    public State state() {
        return state;
    }

    /** Where the turret should be now, degrees, the hold's frame. */
    public double targetDeg() {
        return targetDeg;
    }

    /** How fast that moves, degrees per second counter-clockwise; 0 when not sweeping. */
    public double rateDps(double speedDps) {
        return sweeping() ? direction * speedDps : 0.0;
    }
}
