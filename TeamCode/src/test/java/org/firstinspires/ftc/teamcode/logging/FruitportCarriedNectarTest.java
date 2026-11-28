package org.firstinspires.ftc.teamcode.logging;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public final class FruitportCarriedNectarTest {
    @Test public void threePickedPollenFitBesideTheCarriedNectar() {
        checkLoad(3);
    }

    @Test public void anEmptyFlowerNeverDiscardsTheCarriedNectar() {
        checkLoad(0);
    }

    private static void checkLoad(int drawn) {
        FieldSim.Piece carried = piece(FieldSim.Kind.RED_NECTAR);
        List<FieldSim.Piece> lane = new ArrayList<>(Arrays.asList(carried, piece(FieldSim.Kind.POLLEN),
                piece(FieldSim.Kind.POLLEN), piece(FieldSim.Kind.POLLEN)));
        assertEquals(3 - drawn, FieldSim.limitFlowerPickup(lane, Collections.singletonList(carried), drawn));
        assertEquals(1 + drawn, lane.size());
        assertSame(carried, lane.get(0));
        assertEquals(FieldSim.Where.ROBOT, carried.where);
    }

    private static FieldSim.Piece piece(FieldSim.Kind kind) {
        return new FieldSim.Piece(kind, FieldSim.Where.ROBOT, 0, 0, 0);
    }
}
