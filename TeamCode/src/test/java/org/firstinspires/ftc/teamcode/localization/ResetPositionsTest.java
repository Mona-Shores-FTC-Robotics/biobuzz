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
        assertNull(ResetPositions.of(Alliance.UNKNOWN));
    }

    /** A spot is either unmeasured (no reset) or a whole pose on the field: never half measured. */
    @Test
    public void eachSpotIsWhollyMeasuredOrNotAtAll() {
        for (Alliance alliance : new Alliance[]{Alliance.RED, Alliance.BLUE}) {
            Pose raw = alliance == Alliance.RED ? ResetPositions.RED : ResetPositions.BLUE;
            boolean anyNaN = Double.isNaN(raw.x()) || Double.isNaN(raw.y()) || Double.isNaN(raw.heading());
            boolean allNaN = Double.isNaN(raw.x()) && Double.isNaN(raw.y()) && Double.isNaN(raw.heading());
            assertTrue(alliance + " is half measured", !anyNaN || allNaN);
            Pose spot = ResetPositions.of(alliance);
            if (spot != null) {
                assertTrue(alliance + " is off the field", spot.x() >= 0 && spot.x() <= FieldFrame.FIELD_SIZE_INCHES
                        && spot.y() >= 0 && spot.y() <= FieldFrame.FIELD_SIZE_INCHES);
            }
        }
    }
}
