package org.firstinspires.ftc.teamcode.controls;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class ControlSchemeTest {

    /** Every action but the operator's intake is in every scheme. */
    private static final EnumSet<Action> IN_EVERY_SCHEME = EnumSet.complementOf(EnumSet.of(Action.OPERATOR_INTAKE));

    @Test
    public void noInputDoesTwoThingsOnOneGamepad() {
        for (ControlScheme scheme : ControlScheme.values()) {
            for (Input pick : Input.values()) {
                assertNoClash(scheme, pick);
            }
        }
    }

    private static void assertNoClash(ControlScheme scheme, Input pick) {
        Map<String, Action> seen = new HashMap<>();
        for (Map.Entry<Action, Input> entry : scheme.layout(pick).entrySet()) {
            String key = entry.getKey().pad + "/" + entry.getValue();
            Action other = seen.put(key, entry.getKey());
            assertNull(scheme + " (pick on " + pick + "): " + key + " is both " + other + " and " + entry.getKey(), other);
        }
    }

    @Test
    public void everySchemeHasEveryActionTheOpModeNeeds() {
        for (ControlScheme scheme : ControlScheme.values()) {
            Map<Action, Input> layout = scheme.layout(ControlScheme.DEFAULT_FLOWER_PICK);
            for (Action action : IN_EVERY_SCHEME) {
                assertTrue(scheme + " is missing " + action, layout.containsKey(action));
            }
        }
    }

    @Test
    public void onlySchemeBGivesTheOperatorTheIntake() {
        assertFalse(ControlScheme.A.layout(null).containsKey(Action.OPERATOR_INTAKE));
        assertEquals(Input.L1, ControlScheme.B.layout(null).get(Action.OPERATOR_INTAKE));
    }

    @Test
    public void sticksAreOnlyForDrivingAndButtonsNeverAreSticks() {
        for (ControlScheme scheme : ControlScheme.values()) {
            for (Map.Entry<Action, Input> entry : scheme.layout(null).entrySet()) {
                boolean drive = entry.getKey() == Action.DRIVE || entry.getKey() == Action.TURN;
                assertEquals(scheme + " " + entry.getKey(), drive, entry.getValue().isStick());
            }
        }
    }

    @Test
    public void shareOptionsTouchpadAndPsCannotBeBound() {
        for (Input input : Input.values()) {
            String name = input.name() + " " + input.label;
            for (String forbidden : new String[]{"SHARE", "BACK", "OPTIONS", "START", "TOUCHPAD", "GUIDE", "PS"}) {
                assertFalse(input + " looks like " + forbidden, name.toUpperCase().matches(".*\\b" + forbidden + "\\b.*"));
            }
        }
    }

    @Test
    public void movingTheFlowerPickSwapsItWithWhatWasThere() {
        Map<Action, Input> layout = ControlScheme.A.layout(Input.SQUARE);
        assertEquals(Input.SQUARE, layout.get(Action.FLOWER_PICK));
        assertEquals(Input.CROSS, layout.get(Action.FLOWER_STICK));
        // The operator's Triangle is the operator's: untouched by moving the driver's pick.
        assertEquals(Input.TRIANGLE, ControlScheme.A.layout(Input.TRIANGLE).get(Action.TURRET_LOCK));
        assertEquals(Input.CROSS, ControlScheme.A.layout(Input.TRIANGLE).get(Action.RESET_HEADING));
    }

    @Test
    public void aFlowerPickThatIsNotAFaceButtonOrDpadStaysOnItsDefault() {
        for (Input bad : new Input[]{Input.R2, Input.L1, Input.LEFT_STICK, null}) {
            Map<Action, Input> layout = ControlScheme.A.layout(bad);
            assertEquals(ControlScheme.DEFAULT_FLOWER_PICK, layout.get(Action.FLOWER_PICK));
            assertEquals(Input.R2, layout.get(Action.INTAKE));
        }
    }
}
