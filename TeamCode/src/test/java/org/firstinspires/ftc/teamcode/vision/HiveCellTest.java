package org.firstinspires.ftc.teamcode.vision;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Covers {@link HiveCell}'s own contract, as opposed to its agreement with the SDK.
 *
 * <p>{@code BiobuzzTagsTest} proves the {@code clusterName} strings still match the
 * FTC SDK's BIOBUZZ library. It says nothing about {@link HiveCell#belongsTo}, which
 * had no coverage at all — and that method is the filter behind
 * {@code LimelightVisionSubsystem.bestSightingFor}. Getting it wrong does not throw;
 * it aims the robot at the opposing alliance's cell.
 *
 * <p>The {@code UNKNOWN} and {@code null} cases matter most. An alliance is not
 * chosen until someone picks one, so "not decided yet" reaches this method routinely
 * during init, and it has to mean "matches nothing" rather than "matches whatever is
 * in front of me".
 */
public class HiveCellTest {

    @Test
    public void eachCellBelongsToItsOwnAllianceAndNoOther() {
        for (HiveCell cell : HiveCell.values()) {
            Alliance own = cell.alliance();
            Alliance other = own == Alliance.RED ? Alliance.BLUE : Alliance.RED;

            assertTrue(cell + " should belong to " + own, cell.belongsTo(own));
            assertFalse(cell + " should not belong to " + other, cell.belongsTo(other));
        }
    }

    /**
     * {@code UNKNOWN} is the pre-match state, not a wildcard. If it matched, the
     * first sighting of any cell would be treated as this alliance's target before
     * anyone had chosen one.
     */
    @Test
    public void noCellBelongsToTheUnknownAlliance() {
        for (HiveCell cell : HiveCell.values()) {
            assertFalse(cell + " must not match Alliance.UNKNOWN",
                    cell.belongsTo(Alliance.UNKNOWN));
        }
    }

    @Test
    public void noCellBelongsToANullAlliance() {
        for (HiveCell cell : HiveCell.values()) {
            assertFalse(cell + " must not match null", cell.belongsTo(null));
        }
    }

    @Test
    public void everyCellResolvesFromItsOwnClusterName() {
        for (HiveCell cell : HiveCell.values()) {
            assertSame(cell, HiveCell.forClusterName(cell.clusterName()));
        }
    }

    /**
     * Matching is exact. A fiducial belonging to a cluster we do not recognise must
     * fall out of the pipeline, not land on the nearest cell — {@code
     * LimelightVisionSubsystem.poll} skips on a null return, which is the intended
     * path for a tag from another game's library.
     */
    @Test
    public void anUnrecognisedClusterNameResolvesToNull() {
        for (String unknown : new String[] {
                "", " ", "RED", "SCORING", "RED  SCORING", "Red Scoring", "red scoring",
                "RED SCORING ", " RED SCORING", "GREEN SCORING", "Obelisk"}) {
            assertNull("\"" + unknown + "\" must not resolve to a cell",
                    HiveCell.forClusterName(unknown));
        }
    }

    @Test
    public void nullClusterNameResolvesToNull() {
        assertNull(HiveCell.forClusterName(null));
    }

    @Test
    public void clusterNamesAreUnique() {
        Set<String> seen = new TreeSet<>();
        for (HiveCell cell : HiveCell.values()) {
            if (!seen.add(cell.clusterName())) {
                fail("Two cells share the cluster name \"" + cell.clusterName()
                        + "\", so forClusterName can only ever return one of them.");
            }
        }
        assertEquals(HiveCell.values().length, seen.size());
    }

    /**
     * The four cells are exactly the cross product of the two alliances and the two
     * field ends — two HIVES, each a rocker with a CELL at either end.
     *
     * <p>Asserted rather than assumed because {@code bestSightingFor} picks among an
     * alliance's cells, and code downstream will reasonably expect each alliance to
     * have one at each end. A fifth constant, or two cells at the same end of the
     * field for one alliance, would break that quietly.
     */
    @Test
    public void theCellsAreTheFullAllianceBySideCrossProduct() {
        Map<Alliance, Set<HiveCell.Side>> sidesByAlliance = new EnumMap<>(Alliance.class);

        for (HiveCell cell : HiveCell.values()) {
            Set<HiveCell.Side> sides = sidesByAlliance.get(cell.alliance());
            if (sides == null) {
                sides = new TreeSet<>();
                sidesByAlliance.put(cell.alliance(), sides);
            }
            if (!sides.add(cell.side())) {
                fail("Two cells are on the " + cell.side() + " side for "
                        + cell.alliance() + ".");
            }
        }

        assertEquals("Expected cells for exactly RED and BLUE",
                2, sidesByAlliance.size());
        for (Alliance alliance : new Alliance[] {Alliance.RED, Alliance.BLUE}) {
            assertEquals(alliance + " should have one cell at each end of the field",
                    2, sidesByAlliance.get(alliance).size());
        }
    }

    /** No cell may be attributed to the placeholder alliance. */
    @Test
    public void noCellIsOwnedByTheUnknownAlliance() {
        for (HiveCell cell : HiveCell.values()) {
            assertFalse(cell + " has alliance UNKNOWN", cell.alliance() == Alliance.UNKNOWN);
        }
    }
}
