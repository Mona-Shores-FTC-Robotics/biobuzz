package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.controls.Action;
import org.firstinspires.ftc.teamcode.controls.ControlScheme;
import org.firstinspires.ftc.teamcode.controls.Input;
import org.junit.Test;

import java.util.Map;

/**
 * The pure parts of {@link LaunchTest}: the launch-speed trim, the gate-position nudge, and the
 * facts about the operator layout the bench leans on. The buttons themselves are read from
 * {@link ControlScheme} at runtime, so there is no second layout here to keep in step.
 */
public class LaunchTestTest {

    private static final double EPS = 1e-9;

    @Test
    public void theSetupModifierIsAFaceButtonNoNormalLayerActionUses() {
        Map<Action, Input> layout = ControlScheme.layout(ControlScheme.DEFAULT_FLOWER_PICK);
        Input modifier = layout.get(LaunchTest.SETUP_MODIFIER);
        assertTrue("the modifier must be a button a thumb can hold", modifier.isFaceOrDpad());
        for (Action action : LaunchTest.NORMAL_LAYER) {
            assertNotEquals(action + " shares the setup modifier's button", modifier, layout.get(action));
        }
    }

    @Test
    public void theSetupLayerReusesTwoDifferentFaceButtons() {
        Map<Action, Input> layout = ControlScheme.layout(ControlScheme.DEFAULT_FLOWER_PICK);
        Input gate = layout.get(LaunchTest.GATE_HOLD_BUTTON);
        Input prefeed = layout.get(LaunchTest.PREFEED_HOLD_BUTTON);
        assertTrue(gate.isFaceOrDpad());
        assertTrue(prefeed.isFaceOrDpad());
        assertNotEquals(gate, prefeed);
        assertNotEquals(gate, layout.get(LaunchTest.SETUP_MODIFIER));
        assertNotEquals(prefeed, layout.get(LaunchTest.SETUP_MODIFIER));
    }

    @Test
    public void everyNormalLayerActionIsAnOperatorButton() {
        Map<Action, Input> layout = ControlScheme.layout(ControlScheme.DEFAULT_FLOWER_PICK);
        for (Action action : LaunchTest.NORMAL_LAYER) {
            assertEquals(action + " is not an operator action", Action.Pad.OPERATOR, action.pad);
            Input input = layout.get(action);
            assertTrue(action + " must be a button, not a stick", input != null && !input.isStick());
        }
    }

    @Test
    public void theTrimStepIsGuardedLikeBasicDrives() {
        assertEquals(25.0, LaunchTest.trimStep(25.0, 200.0), EPS);
        assertEquals(200.0, LaunchTest.trimStep(5000.0, 200.0), EPS);
        assertEquals(0.0, LaunchTest.trimStep(-25.0, 200.0), EPS);
        assertEquals(0.0, LaunchTest.trimStep(Double.NaN, 200.0), EPS);
    }

    @Test
    public void aStepMovesTheTrimEitherWay() {
        assertEquals(50.0, LaunchTest.trimmed(0.0, 50.0, 1525.0), EPS);
        assertEquals(-50.0, LaunchTest.trimmed(0.0, -50.0, 1525.0), EPS);
        assertEquals(0.0, LaunchTest.trimmed(50.0, -50.0, 1525.0), EPS);
    }

    @Test
    public void theTrimNeverTakesTheLaunchSpeedBelowZero() {
        assertEquals(-1525.0, LaunchTest.trimmed(-1500.0, -50.0, 1525.0), EPS);
        assertEquals(0.0, LaunchTest.launchSpeedFor(1525.0, -1525.0), EPS);
        assertEquals(0.0, LaunchTest.trimmed(0.0, -50.0, Double.NaN), EPS);
        assertEquals(0.0, LaunchTest.trimmed(0.0, -50.0, -10.0), EPS);
    }

    @Test
    public void theLaunchSpeedIsTheConfiguredSpeedPlusTheTrim() {
        assertEquals(1575.0, LaunchTest.launchSpeedFor(1525.0, 50.0), EPS);
        assertEquals(1475.0, LaunchTest.launchSpeedFor(1525.0, -50.0), EPS);
        assertEquals(50.0, LaunchTest.launchSpeedFor(Double.NaN, 50.0), EPS);
    }

    @Test
    public void anUnmeasuredPositionStartsAtMidRangeWhateverTheDirection() {
        assertEquals(0.5, LaunchTest.nudged(Double.NaN, 0.02), EPS);
        assertEquals(0.5, LaunchTest.nudged(Double.NaN, -0.02), EPS);
    }

    @Test
    public void aMeasuredPositionMovesByTheStep() {
        assertEquals(0.52, LaunchTest.nudged(0.5, 0.02), EPS);
        assertEquals(0.495, LaunchTest.nudged(0.5, -0.005), EPS);
    }

    @Test
    public void itStaysInsideTheServosRange() {
        assertEquals(1.0, LaunchTest.nudged(0.99, 0.02), EPS);
        assertEquals(0.0, LaunchTest.nudged(0.01, -0.02), EPS);
    }
}
