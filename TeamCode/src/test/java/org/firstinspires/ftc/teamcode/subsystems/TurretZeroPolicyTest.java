package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;

import org.firstinspires.ftc.teamcode.subsystems.TurretZeroPolicy.Action;
import org.junit.Test;

/** When the turret zeroes itself: the whole table, so nobody has to think about it. */
public class TurretZeroPolicyTest {

    @Test
    public void aTeleOpAfterAnAutoTakesTheAutosZero() {
        assertEquals(Action.RESTORE_FROM_AUTO, TurretZeroPolicy.atInit(false, true));
    }

    @Test
    public void aTeleOpWithNoAutoOrNoZeroInTheHandoffTakesNothingAtInit() {
        assertEquals(Action.NOTHING, TurretZeroPolicy.atInit(false, false));
    }

    @Test
    public void anAutonomousZeroesAtInitAndNeverTakesAnotherAutosZero() {
        // A match starts at an Auto: the turret is assumed on its mark at INIT (19 Nov 2026), whatever
        // the last run left, and holds its home angle from there.
        assertEquals(Action.ZERO_HERE, TurretZeroPolicy.atInit(true, true));
        assertEquals(Action.ZERO_HERE, TurretZeroPolicy.atInit(true, false));
    }

    @Test
    public void atPlayAnUnzeroedTurretZeroesWhereItStands() {
        assertEquals(Action.ZERO_HERE, TurretZeroPolicy.atPlay(false));
    }

    @Test
    public void atPlayAZeroFromAPersonOrTheHandoffIsKept() {
        assertEquals(Action.NOTHING, TurretZeroPolicy.atPlay(true));
    }
}
