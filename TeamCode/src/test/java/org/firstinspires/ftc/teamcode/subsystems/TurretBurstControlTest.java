package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.subsystems.TurretBurstControl.Command;
import org.junit.Test;

/** {@link TurretBurstControl}: full speed, bursts and off, steered by the encoder. */
public class TurretBurstControlTest {

    private static final long MS = 1_000_000L;

    private static TurretBurstControl.Settings settings() {
        TurretBurstControl.Settings s = new TurretBurstControl.Settings();
        s.leadMs = 50.0;
        s.burstBelowDeg = 20.0;
        s.burstOnMs = 20.0;
        s.burstOffMs = 80.0;
        s.toleranceDeg = 2.0;
        s.shortestPath = true;
        return s;
    }

    @Test
    public void farAwayIsFullSpeedTowardTheTarget() {
        TurretBurstControl c = new TurretBurstControl();
        assertEquals(Command.CCW, c.step(90.0, 0.0, 0.0, settings(), 0));
        assertEquals(Command.CW, c.step(-90.0, 0.0, 0.0, settings(), 0));
    }

    @Test
    public void onTargetIsOff() {
        TurretBurstControl c = new TurretBurstControl();
        assertEquals(Command.OFF, c.step(10.0, 9.0, 0.0, settings(), 0));
    }

    @Test
    public void theCoastIsAllowedFor() {
        // 15° short, but at 300°/s a 50 ms coast carries it 15°: cut now.
        TurretBurstControl c = new TurretBurstControl();
        assertEquals(Command.OFF, c.step(15.0, 0.0, 300.0, settings(), 0));
    }

    @Test
    public void closeIsBurstsOnThenOff() {
        TurretBurstControl c = new TurretBurstControl();
        TurretBurstControl.Settings s = settings();
        assertEquals(Command.CCW, c.step(10.0, 0.0, 0.0, s, 0));
        assertEquals(Command.CCW, c.step(10.0, 0.0, 0.0, s, 10 * MS));
        assertEquals(Command.OFF, c.step(10.0, 0.0, 0.0, s, 30 * MS));
        assertEquals(Command.OFF, c.step(10.0, 0.0, 0.0, s, 90 * MS));
        assertEquals("the next burst", Command.CCW, c.step(10.0, 0.0, 0.0, s, 105 * MS));
    }

    @Test
    public void behindIsReachedTheShortWay() {
        TurretBurstControl c = new TurretBurstControl();
        // At 170°, target -170° (= 190°): 20° counter-clockwise, not 340° clockwise.
        assertEquals(Command.CCW, c.step(-170.0, 170.0, 0.0, settings(), 0));
        assertEquals(190.0, c.effectiveTargetDegrees(), 1e-9);
    }

    @Test
    public void manyTurnsAreFine() {
        TurretBurstControl c = new TurretBurstControl();
        // Three turns round, the target is still taken as the nearest equivalent.
        assertEquals(Command.OFF, c.step(0.0, 1080.5, 0.0, settings(), 0));
        TurretBurstControl.Settings literal = settings();
        literal.shortestPath = false;
        assertEquals(Command.CW, c.step(0.0, 1080.5, 0.0, literal, 0));
    }

    @Test
    public void noAngleIsOff() {
        TurretBurstControl c = new TurretBurstControl();
        assertEquals(Command.OFF, c.step(90.0, Double.NaN, 0.0, settings(), 0));
        assertTrue(Double.isNaN(c.lastErrorDegrees()));
    }

    /** A crude turret: full speed 240°/s, stops 50 ms after the pulse is cut. It settles on target. */
    @Test
    public void aSimulatedTurretSettlesOnTarget() {
        TurretBurstControl c = new TurretBurstControl();
        TurretBurstControl.Settings s = settings();
        double angle = 0.0;
        double rate = 0.0;
        long dtNs = 10 * MS;
        for (int i = 0; i < 400; i++) {
            Command cmd = c.step(135.0, angle, rate, s, i * dtNs);
            double wanted = cmd.sign * 240.0;
            // First-order response, 50 ms time constant.
            rate += (wanted - rate) * (10.0 / 50.0);
            angle += rate * 0.010;
        }
        assertEquals(135.0, angle, 3.0);
    }
}
