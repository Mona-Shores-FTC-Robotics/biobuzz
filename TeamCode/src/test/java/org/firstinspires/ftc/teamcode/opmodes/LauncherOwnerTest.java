package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.opmodes.LauncherOwner.Effect;
import org.firstinspires.ftc.teamcode.opmodes.LauncherOwner.Owner;
import org.junit.Test;

/** {@link LauncherOwner}: who has the launcher, and so when it spins down. */
public class LauncherOwnerTest {

    @Test
    public void startsIdle() {
        LauncherOwner o = new LauncherOwner();
        assertFalse(o.spinning());
        assertEquals(Owner.NONE, o.owner());
    }

    @Test
    public void squareAloneSpinsUpAndDownWithThePress() {
        LauncherOwner o = new LauncherOwner();
        assertEquals(Effect.SPIN_UP, o.spotPressStarted());
        assertTrue(o.spinning());
        assertEquals(Effect.SPIN_DOWN, o.spotPressEnded());
        assertFalse(o.spinning());
    }

    @Test
    public void operatorTogglesUpAndDownWithNoSquare() {
        LauncherOwner o = new LauncherOwner();
        assertEquals(Effect.SPIN_UP, o.operatorToggle(false));
        assertEquals(Effect.SPIN_DOWN, o.operatorToggle(false));
        assertFalse(o.spinning());
    }

    @Test
    public void preSpunLauncherStaysOnThroughSquareAndIdlesWhenItIsLetGo() {
        LauncherOwner o = new LauncherOwner();
        o.operatorToggle(false);
        assertEquals(Effect.KEEP, o.spotPressStarted()); // already at speed: no spin-up wait
        assertTrue(o.spinning());
        assertEquals(Effect.SPIN_DOWN, o.spotPressEnded()); // let go: done shooting
        assertFalse(o.spinning());
    }

    @Test
    public void toggleDuringSquareNeverSpinsDown() {
        LauncherOwner o = new LauncherOwner();
        o.spotPressStarted();
        assertEquals(Effect.KEEP, o.operatorToggle(true));
        assertEquals(Effect.KEEP, o.operatorToggle(true));
        assertTrue(o.spinning());
        assertEquals(Effect.SPIN_DOWN, o.spotPressEnded());
        assertFalse(o.spinning());
    }

    @Test
    public void toggleDuringSquareOnAPreSpunLauncherAlsoKeepsIt() {
        LauncherOwner o = new LauncherOwner();
        o.operatorToggle(false);
        o.spotPressStarted();
        assertEquals(Effect.KEEP, o.operatorToggle(true));
        assertTrue(o.spinning());
    }

    @Test
    public void fireFromIdleSpinsUpForTheOperatorAndTheToggleIdlesIt() {
        LauncherOwner o = new LauncherOwner();
        assertEquals(Effect.SPIN_UP, o.operatorFire(false));
        assertEquals(Owner.OPERATOR, o.owner());
        assertEquals(Effect.KEEP, o.operatorFire(false));
        assertEquals(Effect.SPIN_DOWN, o.operatorToggle(false));
    }

    @Test
    public void fireWithUnjamHeldDoesNotSpinUp() {
        LauncherOwner o = new LauncherOwner();
        assertEquals(Effect.KEEP, o.operatorFire(true));
        assertFalse(o.spinning());
    }

    @Test
    public void fireDuringSquareChangesNothingAndLettingGoStillIdles() {
        LauncherOwner o = new LauncherOwner();
        o.spotPressStarted();
        assertEquals(Effect.KEEP, o.operatorFire(false));
        assertEquals(Owner.SPOT, o.owner());
        assertEquals(Effect.SPIN_DOWN, o.spotPressEnded());
    }
}
