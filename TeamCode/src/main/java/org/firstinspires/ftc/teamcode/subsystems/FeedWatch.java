package org.firstinspires.ftc.teamcode.subsystems;

import java.util.Locale;

/**
 * One feed, judged by the throat beam: still going, every ball out, the lane run dry, or stuck.
 * Started when the gate opens; asked once a loop until the feed ends. The Auto's volleys end on
 * its verdict ({@code LaunchCards}) and the TeleOp reports a stuck feed ({@code BeamBreakSubsystem}).
 *
 * <ul>
 *   <li><b>{@link Verdict#ALL_OUT}</b>: the volley's last ball has passed the throat
 *       ({@code endAfterBalls}, 4 for a {@code LaunchAll}). The moment it does: no timer.</li>
 *   <li><b>{@link Verdict#EMPTY}</b>: at least one ball went and then none for {@code quietS}, or
 *       none at all within {@code firstBallWithinS} of the gate, and the lane does not say it still
 *       holds a piece ({@link Lane}). Balls leave about 0.45 s apart (311 Auto feeds, 12 to 21 Nov
 *       2026), so a gap of twice that is the lane run out.</li>
 *   <li><b>{@link Verdict#HELD}</b>: a ball has sat in the throat for {@code jamBlockedS} (a piece
 *       passes in 120 to 250 ms, #505: two balls on top of the wheels, 20245 on 1 Nov 2026, look
 *       like this). A jam at the wheels: the feeder backing out clears it ({@code LaunchCards}).</li>
 *   <li><b>{@link Verdict#STUCK}</b>: the lane says it holds a piece and none has come for
 *       {@code stuckGapS} (none at all within {@code firstBallWithinS} of the gate, the same), the
 *       throat clear. A jam behind the gate, in the lane: the lane entry's jam clear
 *       ({@code FeedJamClear}) backs it out.</li>
 * </ul>
 *
 * <p>The caller recovers or gives up; the watch only says so. One jam has one handler: the two
 * kinds are told apart so the throat's unjam and the lane entry's never act on the same jam.
 *
 * <p>A beam behind the gate instead ({@code BeamBreakSubsystem.throatBehindGate}) is judged by
 * {@link #stepBehindGate}: it sees a ball waiting at the gate, not each ball leaving.
 *
 * <p>Pure: no SDK, no allocation per loop, so a test can drive it.
 */
public final class FeedWatch {

    /** What the feed is doing. */
    public enum Verdict { FEEDING, ALL_OUT, EMPTY, HELD, STUCK }

    /** What the lane says it holds, apart from the throat: pieces, none, or it cannot tell. */
    public enum Lane { HOLDS, EMPTY, UNKNOWN }

    private double startS = Double.NaN;
    private int passesAtStart;
    private int balls;
    private double lastBallS = Double.NaN;
    private Verdict verdict = Verdict.FEEDING;
    private String why = "";

    /** The gate opened at {@code nowS} with the beam's pass count at {@code passes}. */
    public void start(double nowS, int passes) {
        startS = nowS;
        passesAtStart = passes;
        balls = 0;
        lastBallS = Double.NaN;
        verdict = Verdict.FEEDING;
        why = "";
    }

    /**
     * The feed starts again after an unjam: the clocks start over from {@code nowS}, and the
     * balls already out still count toward the volley.
     */
    public void restart(double nowS) {
        startS = nowS;
        if (!Double.isNaN(lastBallS)) lastBallS = nowS;
        verdict = Verdict.FEEDING;
        why = "";
    }

    /**
     * One loop of the feed.
     *
     * @param passes           the throat beam's pass count now ({@link BeamBreak#passes()})
     * @param blockedS         how long the throat beam has been broken now, seconds; 0 while clear
     * @param lane             what the lane says it still holds
     * @param endAfterBalls    the volley's balls ({@code LaunchCards.launchAllEndAfterShots}); below 1
     *                         or NaN: never {@link Verdict#ALL_OUT}
     * @param quietS           a gap this long after a ball, with the lane not holding, is {@link Verdict#EMPTY}
     * @param firstBallWithinS no ball this long after the gate opened is {@link Verdict#EMPTY} or {@link Verdict#STUCK}
     * @param jamBlockedS      the beam broken this long is {@link Verdict#STUCK}
     * @param stuckGapS        a gap this long with the lane holding is {@link Verdict#STUCK}
     */
    public Verdict step(double nowS, int passes, double blockedS, Lane lane, double endAfterBalls,
                        double quietS, double firstBallWithinS, double jamBlockedS, double stuckGapS) {
        if (Double.isNaN(startS)) return Verdict.FEEDING;
        int out = passes - passesAtStart;
        if (out > balls) {
            balls = out;
            lastBallS = nowS;
        }
        verdict = Verdict.FEEDING;
        why = "";
        if (endAfterBalls >= 1 && balls >= endAfterBalls) {
            verdict = Verdict.ALL_OUT;
            why = String.format(Locale.US, "ball %d through the throat", balls);
        } else if (jamBlockedS > 0 && blockedS >= jamBlockedS) {
            verdict = Verdict.HELD;
            why = String.format(Locale.US, "a ball held in the throat %.2f s", blockedS);
        } else if (balls == 0) {
            double since = nowS - startS;
            if (firstBallWithinS > 0 && since >= firstBallWithinS) {
                verdict = lane == Lane.HOLDS ? Verdict.STUCK : Verdict.EMPTY;
                why = String.format(Locale.US, "no ball through the throat %.2f s after the gate%s", since,
                        lane == Lane.HOLDS ? ", the lane holding" : "");
            }
        } else {
            double gap = nowS - lastBallS;
            if (lane != Lane.HOLDS && quietS > 0 && gap >= quietS) {
                verdict = Verdict.EMPTY;
                why = String.format(Locale.US, "throat quiet %.2f s after ball %d", gap, balls);
            } else if (lane == Lane.HOLDS && stuckGapS > 0 && gap >= stuckGapS) {
                verdict = Verdict.STUCK;
                why = String.format(Locale.US, "no ball %.2f s after ball %d, the lane holding", gap, balls);
            }
        }
        return verdict;
    }

    /**
     * One loop of a feed judged by a beam <b>behind the gate</b>, under the ball that waits there
     * ({@code BeamBreakSubsystem.throatBehindGate}). That beam cannot count: the balls of a volley come
     * through touching, so it stays broken until the last one has gone. So there is no
     * {@link Verdict#ALL_OUT} and no {@link Verdict#HELD} (a ball waiting at the gate is not a jam), and
     * {@link #balls()} stays 0. Only the beam's clear time is judged, and only the feed's own part of it
     * (since {@link #start} or {@link #restart}):
     * <ul>
     *   <li><b>{@link Verdict#EMPTY}</b>: clear {@code emptyClearS} and the lane does not say it holds a
     *       piece: the volley's last ball has passed the gate and gone on to the wheels.</li>
     *   <li><b>{@link Verdict#STUCK}</b>: clear {@code stuckGapS} with the lane holding: nothing reaches
     *       the gate, a jam in the lane (the lane entry's to clear).</li>
     * </ul>
     *
     * @param clearS      how long the beam has been clear now, seconds; 0 while broken
     * @param emptyClearS clear this long, the lane not holding, is {@link Verdict#EMPTY}; 0 or NaN: never
     * @param stuckGapS   clear this long with the lane holding is {@link Verdict#STUCK}; 0 or NaN: never
     */
    public Verdict stepBehindGate(double nowS, double clearS, Lane lane, double emptyClearS, double stuckGapS) {
        if (Double.isNaN(startS)) return Verdict.FEEDING;
        verdict = Verdict.FEEDING;
        why = "";
        double clear = Math.min(clearS, nowS - startS);
        if (!(clear > 0)) return verdict;
        if (lane != Lane.HOLDS && emptyClearS > 0 && clear >= emptyClearS) {
            verdict = Verdict.EMPTY;
            why = String.format(Locale.US, "nothing at the gate %.2f s", clear);
        } else if (lane == Lane.HOLDS && stuckGapS > 0 && clear >= stuckGapS) {
            verdict = Verdict.STUCK;
            why = String.format(Locale.US, "nothing at the gate %.2f s, the lane holding", clear);
        }
        return verdict;
    }

    /** Balls through the throat since {@link #start}. */
    public int balls() {
        return balls;
    }

    /** The last {@link #step}'s verdict. */
    public Verdict verdict() {
        return verdict;
    }

    /** Why, for the match log: "ball 4 through the throat"; empty while feeding. */
    public String why() {
        return why;
    }

    /** Whether a feed is being watched (started and not {@link #stop}ped). */
    public boolean watching() {
        return !Double.isNaN(startS);
    }

    /** The feed has ended: nothing is watched until the next {@link #start}. */
    public void stop() {
        startS = Double.NaN;
        verdict = Verdict.FEEDING;
        why = "";
    }
}
