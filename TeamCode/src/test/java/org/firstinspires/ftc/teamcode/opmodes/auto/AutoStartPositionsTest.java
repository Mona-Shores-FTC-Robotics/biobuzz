package org.firstinspires.ftc.teamcode.opmodes.auto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.localization.StartPosition;
import org.firstinspires.ftc.teamcode.localization.StartPositions;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.LNoCrossAuto;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RNoCrossAuto;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RSoloAuto;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

/**
 * Every Auto begins exactly on a start: a team spot named …_START, which {@link StartPositions}
 * reads, so the drive team has a named place to put the robot and the start check knows it. A
 * path that starts somewhere else begins with a lurch onto the drawing. The Visualizer's start
 * buttons are those spots, so an Auto drawn there passes; one with a start typed by hand does not.
 */
public class AutoStartPositionsTest {

    @Test
    public void theAutosStartOnAStart() {
        assertOnAStart("R-Solo", RSoloAuto.startPose(false));
        assertOnAStart("R-NoCross", RNoCrossAuto.startPose(false));
        assertOnAStart("L-NoCross", LNoCrossAuto.startPose(false));
    }

    private static void assertOnAStart(String auto, Pose drawn) {
        StartPosition start = StartPositions.at(drawn);
        assertNotNull(auto + " starts at (" + drawn.x() + ", " + drawn.y() + ", "
                + Math.toDegrees(drawn.heading()) + " deg), on no team …_START spot: pick its start"
                + " in the Visualizer's start buttons", start);
        assertEquals(auto, Alliance.RED, start.alliance);
    }
}
