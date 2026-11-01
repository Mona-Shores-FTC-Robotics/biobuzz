package org.firstinspires.ftc.teamcode.localization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.util.FieldFrame;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class CalibrationSpotsTest {

    @Test
    public void blueAudienceIsRedFarTurnedHalfATurn() {
        // RED_FAR (62.3, 133.8, 270 deg) about the centre (70.75, 70.75): the spot RED_FAR's note says
        // sees three or four of the red HIVE's tags.
        assertEquals(79.2, CalibrationSpots.BLUE_AUDIENCE.pose.x(), 1e-9);
        assertEquals(7.7, CalibrationSpots.BLUE_AUDIENCE.pose.y(), 1e-9);
        assertEquals(Math.toRadians(90), Math.IEEEremainder(CalibrationSpots.BLUE_AUDIENCE.pose.heading(),
                2 * Math.PI), 1e-9);
        assertEquals("Left side on C-D Seam", CalibrationSpots.BLUE_AUDIENCE.placement());
    }

    @Test
    public void everySpotIsOnTheFieldAgainstAWallWithItsOwnName() {
        Set<String> names = new HashSet<>();
        for (StartPosition spot : CalibrationSpots.ALL) {
            assertTrue(spot.name, names.add(spot.name));
            double x = spot.pose.x(), y = spot.pose.y();
            assertTrue(spot.name + " on the field", x > 0 && x < FieldFrame.FIELD_SIZE_INCHES
                    && y > 0 && y < FieldFrame.FIELD_SIZE_INCHES);
            // Back against the audience wall or the far wall: within a robot's half-length of one.
            assertTrue(spot.name + " against a wall", y < 10 || y > FieldFrame.FIELD_SIZE_INCHES - 10);
            // And facing the field, where the HIVEs are.
            double facing = Math.IEEEremainder(spot.pose.heading(), 2 * Math.PI);
            assertTrue(spot.name + " faces the field", y < 10
                    ? Math.abs(facing - Math.PI / 2) < 1e-9 : Math.abs(facing + Math.PI / 2) < 1e-9);
        }
    }
}
