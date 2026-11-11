package org.firstinspires.ftc.teamcode.opmodes;

import org.firstinspires.ftc.teamcode.opmodes.auto.LaunchCards;
import org.firstinspires.ftc.teamcode.opmodes.auto.PathEndSettle;

/**
 * The volley the go-to-launch-spot button fires when {@code TeleOpConfig.goToSpotAlsoFires}
 * is on ({@link LaunchSpotDrive}). It waits as an Auto's {@code LaunchAll} card does
 * ({@link LaunchCards}), counted from the robot's arrival at the spot, and then feeds for as long
 * as the button is held, as the operator's FIRE does:
 * <ul>
 *   <li>Wait for the launcher at speed, at most {@link LaunchCards#maxSpinUpWaitS} (then feed
 *       anyway: a slightly slow shot beats none), and for the robot settled
 *       ({@link PathEndSettle#settled}: still, at the path's end heading), at most
 *       {@link PathEndSettle#maxSettleWaitS} (then feed anyway).</li>
 *   <li>Feed until the button is let go ({@link #stop}): no set time. The driver holding the
 *       button is the signal; a fixed time (it was {@link LaunchCards#launchAllFeedS}, an Auto's,
 *       until 6 Nov 2026) could only strand a slow piece and leave the robot sitting at the spot
 *       with the gate shut while the button was still held.</li>
 *   <li>The operator wins: their FIRE held at any point before the volley ends gives the shot to
 *       them for the rest of the press ({@link Phase#YIELDED}). Basic Drive no longer asks for
 *       this since 7 Nov 2026: it holds the operator's FIRE back while the volley runs
 *       (see {@link LauncherOwner}), so an early press cannot feed before the spot, aim and speed.</li>
 * </ul>
 *
 * <p>There is no "lane empty" check: the robot cannot know it is empty (no piece sensor; the lane
 * tracker is data only, never decided from), so the feed runs while held, and an empty lane just
 * turns the feeder wheels until the driver lets go.
 *
 * <p>Pure: times in seconds, inputs as booleans, so a test drives it. Once the approach-opened
 * gate lands in {@code LaunchCards}/{@code PathEndSettle} (branch {@code claude/launch-on-approach}),
 * {@link #update}'s wait is where it plugs in.
 */
final class SpotVolley {

    enum Phase { OFF, DRIVING, WAITING, FEEDING, YIELDED }

    private Phase phase = Phase.OFF;
    private double arrivedS = Double.NaN;
    private double feedStartS = Double.NaN;
    private double feedEndS = Double.NaN;
    private String why = "";

    /** A press starts: the drive begins and the launcher spins up. */
    void start() {
        phase = Phase.DRIVING;
        arrivedS = Double.NaN;
        feedStartS = Double.NaN;
        feedEndS = Double.NaN;
        why = "";
    }

    /** The press ended (let go, sticks, timeout): no more feed, whatever it was doing. */
    void stop(double nowS) {
        if (phase == Phase.FEEDING) feedEndS = nowS;
        phase = Phase.OFF;
    }

    /**
     * One step; returns whether the button asks the feeder to feed this loop.
     *
     * @param arrived        the drive has reached the spot (Pedro's path end)
     * @param atSpeed        the launcher is at launch speed
     * @param settled        the robot is still and at the path's end heading
     * @param operatorFiring the operator's FIRE is held
     */
    boolean update(double nowS, boolean arrived, boolean atSpeed, boolean settled, boolean operatorFiring) {
        return update(nowS, arrived, atSpeed, settled, operatorFiring, false);
    }

    /**
     * As {@link #update(double, boolean, boolean, boolean, boolean)}, and with {@code onApproach}
     * the feed may start before the robot arrives: while it is still driving, at launch speed, and
     * close, aimed and hardly turning ({@code PathEndSettle.readyOnApproach}, as an Auto's
     * {@code LaunchAll} does), so the first piece reaches the wheels about as the robot stops.
     * {@code TeleOpConfig.spotFeedsOnApproach}.
     */
    boolean update(double nowS, boolean arrived, boolean atSpeed, boolean settled, boolean operatorFiring,
                   boolean onApproach) {
        if (phase == Phase.OFF || phase == Phase.YIELDED) return false;
        if (operatorFiring) {
            if (phase == Phase.FEEDING) feedEndS = nowS;
            phase = Phase.YIELDED;
            return false;
        }
        if (phase == Phase.DRIVING) {
            if (!arrived) {
                if (!(onApproach && atSpeed)) return false;
                why = "on the approach, at speed";
                feedStartS = nowS;
                phase = Phase.FEEDING;
                return true;
            }
            arrivedS = nowS;
            phase = Phase.WAITING;
        }
        if (phase == Phase.WAITING) {
            double waited = nowS - arrivedS;
            if (!atSpeed && waited < nonNegative(LaunchCards.maxSpinUpWaitS)) return false;
            if (atSpeed && !settled && waited < nonNegative(PathEndSettle.maxSettleWaitS)) return false;
            why = !atSpeed ? "below launch speed (spin-up wait over)"
                    : !settled ? "before settled (settle wait over)" : "at speed, settled";
            feedStartS = nowS;
            phase = Phase.FEEDING;
        }
        return true; // FEEDING: until the button is let go (stop) or the operator fires
    }

    Phase phase() {
        return phase;
    }

    /** When the feed started, seconds on the caller's clock; NaN if it never did. */
    double feedStartS() {
        return feedStartS;
    }

    /** When the feed ended; NaN while feeding or if it never started. */
    double feedEndS() {
        return feedEndS;
    }

    /** Why the feed started when it did ("at speed, settled", or which wait ran out); empty before. */
    String why() {
        return why;
    }

    /** True once the button's feed has started this press (it fired, or is firing). */
    boolean fired() {
        return !Double.isNaN(feedStartS);
    }

    /** A Panels time clamped to [0, ∞), NaN as 0: a typo makes a short wait, never a stuck one. */
    static double nonNegative(double seconds) {
        return seconds > 0.0 ? seconds : 0.0;
    }
}
