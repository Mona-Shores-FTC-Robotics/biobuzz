package org.firstinspires.ftc.teamcode.logging;

import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;

public final class NectarStudyThresholdTest {
    @Test public void eightPollenWeightsAreRequiredBeforeTheRockerCanLeaveItsStop() {
        assertTrue(blocked(6, 1));
        assertFalse(blocked(7, 1));
        assertTrue(blocked(7, 0));
        assertFalse(blocked(8, 0));
        assertTrue(blocked(3, 3));
    }

    private static boolean blocked(int pollen, int nectar) {
        FieldSim field = new FieldSim(Collections.emptyList(), 2);
        for (int i = 0; i < pollen + nectar; i++) {
            FieldSim.Piece p = new FieldSim.Piece(i < pollen ? FieldSim.Kind.POLLEN : FieldSim.Kind.RED_NECTAR,
                    FieldSim.Where.FIELD, 0, 0, 0);
            p.cell = field.red.cell(field.red.raisedEnd());
            field.pieces.add(p);
        }
        assertFalse(field.belowStudyTipWeight(field.red));
        field.requireEightPollenWeights = true;
        return field.belowStudyTipWeight(field.red);
    }
}
