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
    public void theLaunchTrimResetIsDpadDownLikeTheTurretsTrimReset() {
        Map<Action, Input> locked = ControlScheme.layout(ControlScheme.DEFAULT_FLOWER_PICK, true);
        Map<Action, Input> unlocked = ControlScheme.layout(ControlScheme.DEFAULT_FLOWER_PICK, false);
        assertEquals(Input.DPAD_DOWN, locked.get(Action.LAUNCHER_TRIM_RESET));
        assertEquals(Input.DPAD_DOWN, unlocked.get(Action.TRIM_RESET));
        assertFalse("an aiming turret's trim reset does both", unlocked.containsKey(Action.LAUNCHER_TRIM_RESET));
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
    public void theCloseRowIsTheDriversTriangleBehindItsOwnSwitch() {
        Map<Action, Input> both = ControlScheme.layout(ControlScheme.DEFAULT_FLOWER_PICK, true, true, true);
        Map<Action, Input> wallOnly = ControlScheme.layout(ControlScheme.DEFAULT_FLOWER_PICK, true, true, false);
        assertEquals(Input.SQUARE, both.get(Action.LAUNCH_SPOT));
        assertEquals(Input.TRIANGLE, both.get(Action.LAUNCH_SPOT_CLOSE));
        assertFalse(wallOnly.containsKey(Action.LAUNCH_SPOT_CLOSE));
        assertEquals("nothing else changes", both.size() - 1, wallOnly.size());
        // The launch-spot switch takes the close row with it.
        assertFalse(ControlScheme.layout(ControlScheme.DEFAULT_FLOWER_PICK, true, false, true)
                .containsKey(Action.LAUNCH_SPOT_CLOSE));
        // The cheat sheet's layout and the TeleOp's starting switch are the one default.
        assertEquals(ControlScheme.LAUNCH_SPOT_CLOSE_DEFAULT,
                ControlScheme.layout(ControlScheme.DEFAULT_FLOWER_PICK, true).containsKey(Action.LAUNCH_SPOT_CLOSE));
    }

    @Test
    public void gatherGardenIsOnTheDriversRightBumper() {
        assertEquals(Input.R1, ControlScheme.layout(ControlScheme.DEFAULT_FLOWER_PICK).get(Action.GARDEN_GATHER));
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
        assertEquals(Input.CROSS, layout.get(Action.LAUNCH_SPOT_CLOSE));
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
