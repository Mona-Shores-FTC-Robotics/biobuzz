package org.firstinspires.ftc.teamcode.opmodes.auto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.vision.CellBaseline;
import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.HiveCellState;
import org.junit.Before;
import org.junit.Test;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

/**
 * The {@code Tip} trigger against the match-start seed (31 Oct 2026: on blue, BLUE AUDIENCE read
 * UNKNOWN at PLAY and L-Simple's first wait never fired). The camera is a map of what it says this
 * loop; the baseline is the vision subsystem's, fed the way its {@code update()} feeds it.
 */
public class TipTriggerTest {

    private final Map<HiveCell, HiveCellState> camera = new EnumMap<>(HiveCell.class);
    private final CellBaseline baseline = new CellBaseline();

    @Before
    public void unseenAtPlay() {
        for (HiveCell cell : HiveCell.values()) camera.put(cell, HiveCellState.UNKNOWN);
        observe();
        baseline.seedMatchStart();
    }

    private void observe() {
        for (HiveCell cell : HiveCell.values()) baseline.observe(cell, camera.get(cell));
    }

    private BooleanSupplier tipWait(Alliance alliance) {
        return AutoRegistration.tipSince(alliance, baseline::known, camera::get);
    }

    /** The camera says {@code state} for {@code cell} this loop. */
    private void see(HiveCell cell, HiveCellState state) {
        camera.put(cell, state);
        observe();
    }

    @Test
    public void theSeedIsTheMatchStart() {
        assertEquals(HiveCellState.DOWN, baseline.known(HiveCell.BLUE_AUDIENCE));
        assertEquals(HiveCellState.UP, baseline.known(HiveCell.BLUE_SCORING));
        assertEquals(HiveCellState.DOWN, baseline.known(HiveCell.RED_SCORING));
        assertEquals(HiveCellState.UP, baseline.known(HiveCell.RED_AUDIENCE));
    }

    @Test
    public void unknownAtPlayThenTheLeftCellSettlesUpIsATip() {
        BooleanSupplier tip = tipWait(Alliance.BLUE);
        assertFalse(tip.getAsBoolean());
        see(HiveCell.BLUE_AUDIENCE, HiveCellState.UP);
        assertTrue(tip.getAsBoolean());
    }

    @Test
    public void settlingWhereItStartedIsNoTip() {
        BooleanSupplier tip = tipWait(Alliance.BLUE);
        see(HiveCell.BLUE_AUDIENCE, HiveCellState.DOWN);
        see(HiveCell.BLUE_SCORING, HiveCellState.UP);
        assertFalse(tip.getAsBoolean());
    }

    @Test
    public void theRightCellGoingDownIsATip() {
        BooleanSupplier tip = tipWait(Alliance.BLUE);
        see(HiveCell.BLUE_SCORING, HiveCellState.DOWN);
        assertTrue(tip.getAsBoolean());
    }

    @Test
    public void theOtherAllianceTippingIsNoTip() {
        BooleanSupplier tip = tipWait(Alliance.BLUE);
        see(HiveCell.RED_SCORING, HiveCellState.UP);
        see(HiveCell.RED_AUDIENCE, HiveCellState.DOWN);
        assertFalse(tip.getAsBoolean());
    }

    @Test
    public void aLaterWaitStartsWhereTheLastTipLeftThings() {
        see(HiveCell.BLUE_AUDIENCE, HiveCellState.UP);
        see(HiveCell.BLUE_SCORING, HiveCellState.DOWN);
        // The camera loses the HIVE; the next wait still starts from LEFT UP, RIGHT DOWN.
        see(HiveCell.BLUE_AUDIENCE, HiveCellState.UNKNOWN);
        see(HiveCell.BLUE_SCORING, HiveCellState.UNKNOWN);
        BooleanSupplier tip = tipWait(Alliance.BLUE);
        see(HiveCell.BLUE_AUDIENCE, HiveCellState.UP);
        assertFalse(tip.getAsBoolean());
        see(HiveCell.BLUE_AUDIENCE, HiveCellState.DOWN);
        assertTrue(tip.getAsBoolean());
    }

    @Test
    public void unseededAndUnseenNeverTips() {
        baseline.clear();
        BooleanSupplier tip = tipWait(Alliance.BLUE);
        see(HiveCell.BLUE_AUDIENCE, HiveCellState.UP);
        assertFalse(tip.getAsBoolean());
    }

    @Test
    public void noAllianceNeverTips() {
        BooleanSupplier tip = tipWait(Alliance.UNKNOWN);
        see(HiveCell.BLUE_AUDIENCE, HiveCellState.UP);
        see(HiveCell.RED_SCORING, HiveCellState.UP);
        assertFalse(tip.getAsBoolean());
    }
}
