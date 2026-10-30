package org.firstinspires.ftc.teamcode.localization;

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
}
