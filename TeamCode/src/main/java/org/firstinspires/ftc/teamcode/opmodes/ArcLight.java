package org.firstinspires.ftc.teamcode.opmodes;

/**
 * The firing arc's part of the robot's indicator light ({@code IndicatorSubsystem}): orange while
 * the turret's shot is behind its firing arc ({@code TurretArc}). The robot's light, not the
 * gamepad's: nobody looks at the controller (mentor, 20 Nov 2026). Pure, so a test drives it.
 *
 * <ul>
 *   <li><b>{@link State#NONE}</b>: the shot is one the robot can take; the light shows what it would
 *       anyway.</li>
 *   <li><b>{@link State#ORANGE}</b>: the turret, or in AIM the aim, points behind the arc, so a
 *       Circle press now would be refused (or, with the arc not enforced, hit the robot). Steady,
 *       whether or not a fire is held, so the driver sees it before pressing (mentor, 20 Nov 2026:
 *       lit only while a fire was held, the light seemed to do nothing).</li>
 *   <li><b>{@link State#FLASH}</b>: the same, with a fire held: orange flashing.</li>
 * </ul>
 *
 * <p>Orange: the mentor's choice over purple (20 Nov 2026).
 *
 * <p>With {@code TeleOpConfig.circleTurnsIntoArc} on, the TeleOp only says "outside" here while a
 * fire is held outside the arc with nothing turning it in ({@code ArcTurn}), so the light is dark in
 * normal driving and never steady orange (mentor, 20 Nov 2026: the steady light was gaudy and broke
 * up normal driving).
 *
 * <p>Outside lights it at once; back inside, it waits {@link #CLEAR_MS} before it clears. On
 * 20245, 19 Nov 2026, an aim sitting on the arc's 90° edge turned the flash on and off 13 times in
 * 2.7 s, each a new light command, and none of it was readable.
 */
final class ArcLight {

    /**
     * {@link #PULSE}: one solid orange of {@code IndicatorSubsystem.ARC_TURN_PULSE_MS} as Circle's
     * arc turn starts ({@code ArcTurn}), telling the driver the robot is turning itself, so they let
     * it (mentor, 20 Nov 2026). It outranks a full lane's green, which comes back after it, but not
     * {@link #FLASH}.
     */
    enum State { NONE, ORANGE, FLASH, PULSE }

    /** The pulse ends here, ms on {@link #update}'s clock; {@code Long.MIN_VALUE} for none. */
    private long pulseUntilMs = Long.MIN_VALUE;

    /** Starts the arc turn's pulse at {@code nowMs}, {@code lengthMs} long (a later start restarts it). */
    void pulse(long nowMs, long lengthMs) {
        pulseUntilMs = nowMs + Math.max(0L, lengthMs);
    }

    /** Back inside the arc this long, ms, before the light clears. */
    static final long CLEAR_MS = 300;

    /** The last loop that was outside the arc, ms; {@code Long.MIN_VALUE} before any. */
    private long lastOutsideMs = Long.MIN_VALUE;

    /**
     * This loop's light.
     *
     * @param outsideArc the turret or its aim points behind the arc this loop
     * @param fireHeld   a fire is held this loop (Circle, or Square's volley asking to feed)
     * @param nowMs      a monotonic clock, ms
     */
    State update(boolean outsideArc, boolean fireHeld, long nowMs) {
        if (outsideArc) lastOutsideMs = nowMs;
        boolean lit = lastOutsideMs != Long.MIN_VALUE && nowMs - lastOutsideMs < CLEAR_MS;
        if (lit && fireHeld) return State.FLASH;
        if (pulseUntilMs != Long.MIN_VALUE && nowMs < pulseUntilMs) return State.PULSE;
        return lit ? State.ORANGE : State.NONE;
    }
}
