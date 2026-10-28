package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.opmodes.FireGates.Gate;
import org.firstinspires.ftc.teamcode.opmodes.FireGates.Verdict;
import org.junit.Test;

public class FireGatesTest {

    private final FireGates gates = new FireGates();

    @Test
    public void shadowGatesNeverBlockButSayTheyWould() {
        gates.evaluate(false, false, 10.0, 3.0);
        assertEquals(Verdict.BLOCK, gates.verdict(Gate.LAUNCHER_AT_SPEED));
        assertEquals(Verdict.BLOCK, gates.verdict(Gate.AIM_ON_TARGET));
        assertTrue(gates.anyWouldBlock());
        assertTrue(gates.enforcedOpen(false, false));
    }

    @Test
    public void anEnforcedGateBlocksOnlyWhenItIsClosed() {
        gates.evaluate(true, false, 1.0, 3.0);
        assertFalse(gates.enforcedOpen(true, false));
        assertTrue(gates.enforcedOpen(false, true));
        gates.evaluate(true, true, 1.0, 3.0);
        assertTrue(gates.enforcedOpen(true, true));
        assertFalse(gates.anyWouldBlock());
    }

    @Test
    public void atSpeedAtIdleIsNotAtLaunchSpeed() {
        gates.evaluate(false, true, 0.0, 3.0);
        assertEquals(Verdict.BLOCK, gates.verdict(Gate.LAUNCHER_AT_SPEED));
    }

    @Test
    public void anUnknownAimIsNeverABlock() {
        gates.evaluate(true, true, Double.NaN, 3.0);
        assertEquals(Verdict.UNKNOWN, gates.verdict(Gate.AIM_ON_TARGET));
        assertTrue(gates.enforcedOpen(true, true));
        assertFalse(gates.anyWouldBlock());
    }

    @Test
    public void aimToleranceIsEitherSide() {
        gates.evaluate(true, true, -2.9, 3.0);
        assertEquals(Verdict.PASS, gates.verdict(Gate.AIM_ON_TARGET));
        gates.evaluate(true, true, -3.1, 3.0);
        assertEquals(Verdict.BLOCK, gates.verdict(Gate.AIM_ON_TARGET));
    }
}
