package org.firstinspires.ftc.teamcode.logging;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public final class NectarEntryTimingTest {
    @Test public void eachEarnedRollUsesItsOwnDelayEvenWhenShorterThanTheDefault() {
        boolean aims = AutoSim.humanAims;
        String[] rolls = AutoSim.humanAimRolls;
        try {
            AutoSim.humanAims = true;
            AutoSim.humanAimRolls = new String[] {"RIGHT_AUTO_OFFSET|5.5,95|21||0.5", "keep", "RIGHT_AUTO_OFFSET"};
            assertEquals(0.5, AutoSim.entryDelayS(1), 0);
            assertEquals(AutoSim.HUMAN_ROLL_DELAY_S, AutoSim.entryDelayS(2), 0);
            assertEquals(AutoSim.HUMAN_ROLL_DELAY_S, AutoSim.entryDelayS(3), 0);
        } finally {
            AutoSim.humanAims = aims;
            AutoSim.humanAimRolls = rolls;
        }
    }

    @Test public void aPieceCanBreakThePlaneBeforeItsCentreCrosses() {
        FieldSim field = new FieldSim(Collections.emptyList(), 1);
        field.pieces.clear();
        FieldSim.Piece pollen = new FieldSim.Piece(FieldSim.Kind.POLLEN, FieldSim.Where.FIELD,
                FieldSim.CENTRE_IN - 5, 25, 5);
        field.pieces.add(pollen);
        field.watchPieceCrossings(Alliance.RED, 38);
        pollen.x = FieldSim.CENTRE_IN - pollen.kind.radius / 2;
        field.step(0.001);
        assertEquals(1, field.piecesAcrossEdge);
        assertEquals(0, field.piecesAcrossCentre);
        pollen.x = FieldSim.CENTRE_IN + 1;
        field.step(0.001);
        assertEquals(1, field.piecesAcrossEdge);
        assertEquals(1, field.piecesAcrossCentre);
    }
}
