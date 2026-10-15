package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

/** The drive team's NECTAR rolled along the wall into the LOADING ZONE (doc/human-nectar.md). */
public class HumanNectarTest {

    private static FieldSim withOneOutside(Alliance alliance) {
        List<HiveAssets.StagedPiece> staged = new ArrayList<>();
        staged.add(new HiveAssets.StagedPiece(alliance == Alliance.BLUE ? "Blue Nectar" : "Red Nectar", "outside",
                alliance == Alliance.BLUE ? FieldSim.FIELD_SIZE_IN + 10 : -10, 70, FieldSim.NECTAR_RADIUS_IN));
        return new FieldSim(staged, 1);
    }

    @Test
    public void aGentleRollStopsInsideTheZoneAtTheHiveEnd() {
        // With the variety on, a seed's tile slopes steer the roll a little (2.6 in on blue's seed-1 tiles).
        double variety = FieldSim.spillVariety, scatter = FieldSim.bounceScatter;
        FieldSim.spillVariety = 0;
        FieldSim.bounceScatter = 0;
        try {
            for (Alliance a : Alliance.values()) rollsHome(a);
        } finally {
            FieldSim.spillVariety = variety;
            FieldSim.bounceScatter = scatter;
        }
    }

    private static void rollsHome(Alliance a) {
        {
            FieldSim sim = withOneOutside(a);
            assertTrue(sim.enterNectarRolled(a, 5, 7));  // 7 in/s rolls 16 in; 8 in/s rolls 21.5, just out of the 23.6 in zone
            FieldSim.Piece p = sim.pieces.get(sim.pieces.size() - 1);
            double y0 = p.y;
            for (int i = 0; i < 600; i++) sim.step(0.01);
            double[] zone = FieldSim.loadingZone(a);
            assertEquals(a + ": still against the wall", a == Alliance.BLUE ? FieldSim.FIELD_SIZE_IN - 5 : 5, p.x, 0.5);
            assertTrue(a + ": rolled toward the HIVE end", a == Alliance.BLUE ? p.y > y0 + 15 : p.y < y0 - 15);
            assertTrue(a + ": inside the zone", p.y > zone[2] && p.y < zone[3]);
            assertTrue(a + ": stopped", Math.hypot(p.vx, p.vy) < 0.5);
        }
    }

    @Test
    public void theTwoAlliancesEntriesAreAHalfTurnOfEachOther() {
        FieldSim red = withOneOutside(Alliance.RED), blue = withOneOutside(Alliance.BLUE);
        red.enterNectarRolled(Alliance.RED, 5, 12);
        blue.enterNectarRolled(Alliance.BLUE, 5, 12);
        FieldSim.Piece r = red.pieces.get(red.pieces.size() - 1), b = blue.pieces.get(blue.pieces.size() - 1);
        assertEquals(FieldSim.FIELD_SIZE_IN - r.x, b.x, 1e-9);
        assertEquals(FieldSim.FIELD_SIZE_IN - r.y, b.y, 1e-9);
        assertEquals(-r.vy, b.vy, 1e-9);
    }
}
