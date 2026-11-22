package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.vision.HiveCell;
import org.firstinspires.ftc.teamcode.vision.HiveState;
import org.junit.Test;

/** {@link OurCell}: our end's CELL, up, down, or not known, from the HIVE's state. */
public class OurCellTest {

    private static final HiveCell RED_LEFT = HiveCell.leftCell(Alliance.RED);
    private static final HiveCell RED_RIGHT = HiveCell.rightCell(Alliance.RED);

    @Test
    public void settledStatesReadUpOrDown() {
        assertEquals(OurCell.Reading.UP, OurCell.of(RED_RIGHT, Alliance.RED, HiveState.RIGHT_CELL_UP, HiveState.RIGHT_CELL_UP));
        assertEquals(OurCell.Reading.DOWN, OurCell.of(RED_LEFT, Alliance.RED, HiveState.RIGHT_CELL_UP, HiveState.RIGHT_CELL_UP));
        assertEquals(OurCell.Reading.UP, OurCell.of(RED_LEFT, Alliance.RED, HiveState.LEFT_CELL_UP, HiveState.LEFT_CELL_UP));
        assertEquals(OurCell.Reading.DOWN, OurCell.of(RED_RIGHT, Alliance.RED, HiveState.LEFT_CELL_UP, HiveState.LEFT_CELL_UP));
    }

    @Test
    public void aTipAwayFromOurCellReadsDownAndOneTowardItIsNotKnownYet() {
        assertEquals(OurCell.Reading.DOWN, OurCell.of(RED_RIGHT, Alliance.RED, HiveState.TRANSITION, HiveState.RIGHT_CELL_UP));
        assertEquals(OurCell.Reading.UNKNOWN, OurCell.of(RED_LEFT, Alliance.RED, HiveState.TRANSITION, HiveState.RIGHT_CELL_UP));
    }

    @Test
    public void unseenOrNoCellIsNotKnown() {
        assertEquals(OurCell.Reading.UNKNOWN, OurCell.of(RED_RIGHT, Alliance.RED, HiveState.UNSEEN, HiveState.RIGHT_CELL_UP));
        assertEquals(OurCell.Reading.UNKNOWN, OurCell.of(null, Alliance.RED, HiveState.RIGHT_CELL_UP, HiveState.RIGHT_CELL_UP));
        assertEquals(OurCell.Reading.UNKNOWN, OurCell.of(RED_RIGHT, Alliance.RED, null, null));
    }

    @Test
    public void theOtherAlliancesCellIsNotOurs() {
        assertEquals(OurCell.Reading.UNKNOWN,
                OurCell.of(HiveCell.rightCell(Alliance.BLUE), Alliance.RED, HiveState.RIGHT_CELL_UP, HiveState.RIGHT_CELL_UP));
    }

    @Test
    public void thePageKeepsTheLastReadingForAFewSecondsThenSaysItDoesNotKnow() {
        assertTrue(OurCell.line(OurCell.Reading.UP, OurCell.Reading.UP, 0.0).contains("OUR CELL UP"));
        assertTrue(OurCell.line(OurCell.Reading.UNKNOWN, OurCell.Reading.DOWN, 3.0).contains("OUR CELL DOWN? (3 s ago)"));
        assertTrue(OurCell.line(OurCell.Reading.UNKNOWN, OurCell.Reading.DOWN, OurCell.MEMORY_S + 0.1).contains("OUR CELL ?"));
        assertTrue(OurCell.line(OurCell.Reading.UNKNOWN, OurCell.Reading.UNKNOWN, Double.NaN).contains("OUR CELL ?"));
    }
}
