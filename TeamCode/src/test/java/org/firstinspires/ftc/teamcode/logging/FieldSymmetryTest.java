package org.firstinspires.ftc.teamcode.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

/**
 * The field is a half turn between the alliances (Event Field Setup Guide §8.3), so the simulator
 * must be too: the same shots at the blue HIVE from the turned-around spot give the turned-around
 * run, through the TIP and its spill. With the variety on, a seed's tile slopes, spill kicks and
 * roll scales are drawn in the field frame (one field, shared by both alliances), so the same seed
 * is a different run for blue; with it off, nothing else may differ (8 Oct 2026: blue's human
 * NECTAR stepped +y, red's way, and the alliances' runs parted at the second NECTAR).
 */
public class FieldSymmetryTest {

    private static final double S = FieldSim.FIELD_SIZE_IN, LOOP = 0.02;
    /** The field model's own asymmetry: its staged pieces are half-turn images to 0.007 in. */
    private static final double TOLERANCE_IN = 0.02;

    @Test
    public void shotsTipsAndSpillsAreAHalfTurnBetweenTheAlliances() throws Exception {
        double variety = FieldSim.spillVariety, scatter = FieldSim.bounceScatter;
        FieldSim.spillVariety = 0;
        FieldSim.bounceScatter = 0;
        try {
            FieldSim red = new FieldSim(HiveAssets.committedStagedPieces(), 1);
            FieldSim blue = new FieldSim(HiveAssets.committedStagedPieces(), 1);
            double yRed = red.red.openingCentre()[1] - SimDriver.SHOT_DISTANCE_IN;
            for (int shot = 0; shot < 8; shot++) {
                fire(red, red.red, red.red.centreX, yRed, Math.PI / 2);
                fire(blue, blue.blue, S - red.red.centreX, S - yRed, -Math.PI / 2);
                for (int i = 0; i < 60; i++) {
                    red.step(LOOP);
                    blue.step(LOOP);
                }
                assertHalfTurn(red, blue, "after shot " + shot);
            }
            for (int i = 0; i < 300; i++) {
                red.step(LOOP);
                blue.step(LOOP);
            }
            assertHalfTurn(red, blue, "settled");
            assertEquals("both tipped", 1, red.red.tips);
            assertEquals(red.red.tips, blue.blue.tips);
            assertEquals(red.drainEvents().toString().replace("RED", "X").replace("red", "x").replace("AUDIENCE", "A").replace("SCORING", "B"),
                    blue.drainEvents().toString().replace("BLUE", "X").replace("blue", "x").replace("SCORING", "A").replace("AUDIENCE", "B"));
        } finally {
            FieldSim.spillVariety = variety;
            FieldSim.bounceScatter = scatter;
        }
    }

    @Test
    public void theHumanPlayersNectarLandsInTheTurnedAroundSpot() throws Exception {
        double variety = FieldSim.spillVariety, scatter = FieldSim.bounceScatter;
        FieldSim.spillVariety = 0;
        FieldSim.bounceScatter = 0;
        try {
            FieldSim red = new FieldSim(HiveAssets.committedStagedPieces(), 1);
            FieldSim blue = new FieldSim(HiveAssets.committedStagedPieces(), 1);
            for (int n = 0; n < 5; n++) {
                assertTrue(red.enterNectar(Alliance.RED));
                assertTrue(blue.enterNectar(Alliance.BLUE));
                for (int i = 0; i < 50; i++) {
                    red.step(LOOP);
                    blue.step(LOOP);
                }
                assertHalfTurn(red, blue, "NECTAR " + (n + 1));
            }
        } finally {
            FieldSim.spillVariety = variety;
            FieldSim.bounceScatter = scatter;
        }
    }

    private static void fire(FieldSim sim, FieldSim.Rocker at, double x, double y, double heading) {
        FieldSim.Piece p = new FieldSim.Piece(FieldSim.Kind.POLLEN, FieldSim.Where.ROBOT, 0, 0, 0);
        sim.pieces.add(p);
        sim.stored.add(p);
        sim.setRobot(x, y, heading, 0, 0, 0, false);
        assertTrue(sim.launch(at.aimPoint()) != null);
    }

    /** Every piece on red's field has a piece on blue's at its half-turn image, and the rockers mirror. */
    private static void assertHalfTurn(FieldSim red, FieldSim blue, String when) {
        assertEquals(when, -red.red.angle, blue.blue.angle, 1e-9);
        assertEquals(when, -red.blue.angle, blue.red.angle, 1e-9);
        List<FieldSim.Piece> theirs = new ArrayList<>();
        for (FieldSim.Piece b : blue.pieces) if (b.where == FieldSim.Where.FIELD) theirs.add(b);
        for (FieldSim.Piece a : red.pieces) {
            if (a.where != FieldSim.Where.FIELD) continue;
            double best = Double.MAX_VALUE;
            for (FieldSim.Piece b : theirs) {
                best = Math.min(best, Math.max(Math.abs(a.x - (S - b.x)), Math.max(Math.abs(a.y - (S - b.y)), Math.abs(a.z - b.z))));
            }
            assertTrue(String.format(java.util.Locale.ROOT, "%s: red %s at (%.2f, %.2f, %.2f) has no blue image (nearest %.3f in off)",
                    when, a.kind, a.x, a.y, a.z, best), best < TOLERANCE_IN);
        }
    }
}
