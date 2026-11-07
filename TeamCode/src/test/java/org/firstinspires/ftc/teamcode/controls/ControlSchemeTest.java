package org.firstinspires.ftc.teamcode.controls;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class ControlSchemeTest {

    @Test
    public void noInputDoesTwoThingsOnOneGamepad() {
        for (Input pick : Input.values()) {
            Map<String, Action> seen = new HashMap<>();
            for (Map.Entry<Action, Input> entry : ControlScheme.layout(pick).entrySet()) {
                String key = entry.getKey().pad + "/" + entry.getValue();
                Action other = seen.put(key, entry.getKey());
                assertNull("pick on " + pick + ": " + key + " is both " + other + " and " + entry.getKey(), other);
            }
        }
    }

    @Test
    public void everyActionIsBoundButTheOnesLeftOut() {
        Map<Action, Input> layout = ControlScheme.layout(ControlScheme.DEFAULT_FLOWER_PICK);
        for (Action action : Action.values()) {
            boolean bound = !ControlScheme.NOT_BOUND.contains(action) && !ControlScheme.LOCKED_TURRET_ONLY.contains(action);
            assertEquals(action.toString(), bound, layout.containsKey(action));
        }
    }

    @Test
    public void aLockedTurretPutsTheLaunchTrimResetOnTheWholeDpad() {
        Map<Action, Input> locked = ControlScheme.layout(ControlScheme.DEFAULT_FLOWER_PICK, true);
        assertEquals(Input.DPAD_ANY, locked.get(Action.LAUNCHER_TRIM_RESET));
        Map<Action, Input> unlocked = ControlScheme.layout(ControlScheme.DEFAULT_FLOWER_PICK, false);
        assertFalse("an aiming turret has the D-pad", unlocked.containsKey(Action.LAUNCHER_TRIM_RESET));
    }

    @Test
    public void theWholeDpadNeverSharesAPadWithOneDirection() {
        for (boolean locked : new boolean[]{true, false}) {
            for (Input pick : Input.values()) {
                Map<Action, Input> layout = ControlScheme.layout(pick, locked, true);
                for (Action.Pad pad : Action.Pad.values()) {
                    boolean any = false;
                    boolean one = false;
                    for (Map.Entry<Action, Input> entry : layout.entrySet()) {
                        if (entry.getKey().pad != pad) continue;
                        Input input = entry.getValue();
                        if (input == Input.DPAD_ANY) any = true;
                        if (input == Input.DPAD_UP || input == Input.DPAD_DOWN
                                || input == Input.DPAD_LEFT || input == Input.DPAD_RIGHT) one = true;
                    }
                    assertFalse(pad + " (turret locked " + locked + ", pick " + pick + ") binds the whole D-pad and one direction",
                            any && one);
                }
            }
        }
    }

    @Test
    public void noInputDoesTwoThingsOnOneGamepadInTheMatchLayouts() {
        for (boolean locked : new boolean[]{true, false}) {
            Map<String, Action> seen = new HashMap<>();
            for (Map.Entry<Action, Input> entry : ControlScheme.layout(ControlScheme.DEFAULT_FLOWER_PICK, locked).entrySet()) {
                String key = entry.getKey().pad + "/" + entry.getValue();
                Action other = seen.put(key, entry.getKey());
                assertNull("turret locked " + locked + ": " + key + " is both " + other + " and " + entry.getKey(), other);
            }
        }
    }

    @Test
    public void aLockedTurretsControlsAreLeftOutAndAnUnlockedOnesKept() {
        Map<Action, Input> locked = ControlScheme.layout(ControlScheme.DEFAULT_FLOWER_PICK, true);
        Map<Action, Input> unlocked = ControlScheme.layout(ControlScheme.DEFAULT_FLOWER_PICK, false);
        for (Action action : ControlScheme.TURRET_ACTIONS) {
            assertFalse(action + " offered on a locked turret", locked.containsKey(action));
            assertTrue(action + " missing on an aiming turret", unlocked.containsKey(action));
        }
        assertEquals("nothing else changes",
                unlocked.size() - ControlScheme.TURRET_ACTIONS.size() + ControlScheme.LOCKED_TURRET_ONLY.size(), locked.size());
    }

    @Test
    public void theLaunchSpotButtonIsTheDriversSquareAndItsSwitchLeavesItOut() {
        Map<Action, Input> on = ControlScheme.layout(ControlScheme.DEFAULT_FLOWER_PICK, true, true);
        Map<Action, Input> off = ControlScheme.layout(ControlScheme.DEFAULT_FLOWER_PICK, true, false);
        assertEquals(Input.SQUARE, on.get(Action.LAUNCH_SPOT));
        assertFalse(off.containsKey(Action.LAUNCH_SPOT));
        assertEquals("nothing else changes", on.size() - 1, off.size());
        // The two-switch layout (the cheat sheet's) keeps it, as the TeleOp's default does.
        assertEquals(on, ControlScheme.layout(ControlScheme.DEFAULT_FLOWER_PICK, true));
        // Moving the FLOWER pick onto Square swaps the launch spot onto Cross.
        Map<Action, Input> swapped = ControlScheme.layout(Input.SQUARE);
        assertEquals(Input.CROSS, swapped.get(Action.LAUNCH_SPOT));
    }

    @Test
    public void intakeAndUnjamAreTheSameInputOnBothPads() {
        Map<Action, Input> layout = ControlScheme.layout(null);
        assertEquals(layout.get(Action.INTAKE), layout.get(Action.OPERATOR_INTAKE));
        assertEquals(layout.get(Action.DRIVER_UNJAM), layout.get(Action.UNJAM));
    }

    @Test
    public void sticksAreOnlyForDrivingAndButtonsNeverAreSticks() {
        for (Map.Entry<Action, Input> entry : ControlScheme.layout(null).entrySet()) {
            boolean drive = entry.getKey() == Action.DRIVE || entry.getKey() == Action.TURN;
            assertEquals(entry.getKey().toString(), drive, entry.getValue().isStick());
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
        Map<Action, Input> layout = ControlScheme.layout(Input.TRIANGLE);
        assertEquals(Input.TRIANGLE, layout.get(Action.FLOWER_PICK));
        assertEquals(Input.CROSS, layout.get(Action.GARDEN_GATHER));
        // The operator's buttons are the operator's: moving the driver's pick never touches them.
        Map<Action, Input> onCircle = ControlScheme.layout(Input.CIRCLE);
        assertEquals(Input.CIRCLE, onCircle.get(Action.FIRE));
        assertEquals(Input.CROSS, onCircle.get(Action.CHASSIS_AIM));
    }

    @Test
    public void aFlowerPickThatIsNotAFaceButtonOrDpadStaysOnItsDefault() {
        for (Input bad : new Input[]{Input.R2, Input.L1, Input.LEFT_STICK, null}) {
            Map<Action, Input> layout = ControlScheme.layout(bad);
            assertEquals(ControlScheme.DEFAULT_FLOWER_PICK, layout.get(Action.FLOWER_PICK));
            assertEquals(Input.R2, layout.get(Action.INTAKE));
        }
    }
}
