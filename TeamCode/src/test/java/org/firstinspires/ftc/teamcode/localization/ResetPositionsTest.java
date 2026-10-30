package org.firstinspires.ftc.teamcode.localization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.firstinspires.ftc.teamcode.util.FieldFrame;
import org.junit.Test;

public class ResetPositionsTest {

    @Test
    public void anUnknownAllianceHasNoSpot() {
        for (ResetPositions.Corner corner : ResetPositions.Corner.values()) {
            assertNull(ResetPositions.of(Alliance.UNKNOWN, corner));
        }
    }

    /** A spot is either unmeasured (no reset) or a whole pose on the field: never half measured. */
    @Test
    public void eachSpotIsWhollyMeasuredOrNotAtAll() {
        for (Alliance alliance : new Alliance[]{Alliance.RED, Alliance.BLUE}) {
            for (ResetPositions.Corner corner : ResetPositions.Corner.values()) {
                check(alliance, corner);
            }
        }
    }

    private static void check(Alliance alliance, ResetPositions.Corner corner) {
        String which = alliance + " " + corner;
        Pose raw = ResetPositions.raw(alliance, corner);
        boolean anyNaN = Double.isNaN(raw.x()) || Double.isNaN(raw.y()) || Double.isNaN(raw.heading());
        boolean allNaN = Double.isNaN(raw.x()) && Double.isNaN(raw.y()) && Double.isNaN(raw.heading());
        assertTrue(which + " is half measured", !anyNaN || allNaN);
        Pose spot = ResetPositions.of(alliance, corner);
        if (spot != null) {
            assertTrue(which + " is off the field", spot.x() >= 0 && spot.x() <= FieldFrame.FIELD_SIZE_INCHES
                    && spot.y() >= 0 && spot.y() <= FieldFrame.FIELD_SIZE_INCHES);
        }
    }

    /** Blue's corners are red's turned half a turn about the field centre, as the field is. */
    @Test
    public void blueCornersAreRedsTurnedHalfATurn() {
        for (ResetPositions.Corner corner : ResetPositions.Corner.values()) {
            Pose red = ResetPositions.of(Alliance.RED, corner);
            Pose blue = ResetPositions.of(Alliance.BLUE, corner);
            double c = FieldFrame.FIELD_CENTRE_INCHES;
            assertEquals(corner + " x", 2 * c - red.x(), blue.x(), 1e-9);
            assertEquals(corner + " y", 2 * c - red.y(), blue.y(), 1e-9);
            assertEquals(corner + " heading", 0.0,
                    Math.IEEEremainder(blue.heading() - red.heading() - Math.PI, 2 * Math.PI), 1e-9);
        }
    }
}
