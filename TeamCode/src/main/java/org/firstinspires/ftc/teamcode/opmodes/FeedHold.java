package org.firstinspires.ftc.teamcode.opmodes;

/**
 * One TeleOp rule that may hold back the <b>start</b> of a driver's feed until something is true,
 * for at most {@code giveUpS} of the press: then the press fires anyway, so a held button always
 * means "shoot". Once a press is let through (the rule passed, or it gave up) it stays let through
 * until the button is let go, so a feed under way is never cut: the lane stops reading full the
 * moment the first ball leaves, and that must not stop the volley.
 *
 * <p>Two of them in {@link BasicDriveTeleOp}, both off by default (switches in {@link TeleOpConfig}):
 * <ul>
 *   <li>{@code TeleOpConfig.fullLoadGate}: wait for the lane to read full, so a volley carries 4,
 *       not 3. Fruitport (21 Nov 2026): a volley carried 3.1 pieces on 20245 and 2.6 on 19429, and a
 *       full load is about +0.5 TIPs a match (doc/drive-team-cycle-playbook.md).</li>
 *   <li>{@code TeleOpConfig.holdFireAtDownCell}: wait while the camera sees our end's CELL down, where
 *       a volley is wasted. An unseen CELL never holds.</li>
 * </ul>
 *
 * <p>Pure: no SDK, no allocation, so a test drives it.
 */
final class FeedHold {

    enum Verdict {
        /** The rule is switched off: nothing to say. */
        OFF,
        /** No press, or the press is let through because the rule passed (now or earlier in it). */
        PASS,
        /** The rule holds the feed's start: not passed, not yet given up. */
        WAITING,
        /** The rule had not passed by {@code giveUpS} into the press, so the press fires anyway. */
        GAVE_UP
    }

    private double heldSinceS = Double.NaN;
    private Verdict verdict = Verdict.OFF;
    private boolean letThrough;
    private boolean waited;

    /**
     * One loop.
     *
     * @param enabled  the rule's switch
     * @param fireHeld the driver's fire is held this loop
     * @param passes   the rule is satisfied this loop
     * @param nowS     a clock, seconds
     * @param giveUpS  how long into a press the rule gives up; NaN or negative: never gives up
     * @return this loop's verdict
     */
    Verdict update(boolean enabled, boolean fireHeld, boolean passes, double nowS, double giveUpS) {
        if (!fireHeld) {
            heldSinceS = Double.NaN;
            letThrough = false;
            waited = false;
            verdict = enabled ? Verdict.PASS : Verdict.OFF;
            return verdict;
        }
        if (!enabled) {
            // Switched off mid-press: let it through, and keep it through if switched back on.
            letThrough = true;
            verdict = Verdict.OFF;
            return verdict;
        }
        if (Double.isNaN(heldSinceS)) heldSinceS = nowS;
        if (letThrough) {
            if (verdict == Verdict.OFF) verdict = Verdict.PASS;
            return verdict;
        }
        if (passes) {
            verdict = Verdict.PASS;
            letThrough = true;
        } else if (!Double.isNaN(giveUpS) && giveUpS >= 0.0 && nowS - heldSinceS >= giveUpS) {
            verdict = Verdict.GAVE_UP;
            letThrough = true;
        } else {
            verdict = Verdict.WAITING;
            waited = true;
        }
        return verdict;
    }

    /** The rule holds the feed's start this loop. */
    boolean holds() {
        return verdict == Verdict.WAITING;
    }

    /** This press has been held back at least one loop. */
    boolean waited() {
        return waited;
    }

    /** Seconds since the press began, at {@code nowS}; NaN when no press. */
    double heldS(double nowS) {
        return Double.isNaN(heldSinceS) ? Double.NaN : nowS - heldSinceS;
    }

    Verdict verdict() {
        return verdict;
    }
}
