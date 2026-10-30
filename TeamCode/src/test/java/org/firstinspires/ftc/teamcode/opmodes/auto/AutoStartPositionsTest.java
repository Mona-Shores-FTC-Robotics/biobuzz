package org.firstinspires.ftc.teamcode.opmodes.auto;

import static org.junit.Assert.assertEquals;

import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.localization.StartPositions;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RLaunchParkAuto;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RQualsAuto;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RQualsPollenOnlyAuto;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.RSimpleAuto;
import org.firstinspires.ftc.teamcode.opmodes.auto.generated.SisterRightFixedAuto;
import org.junit.Test;

/**
 * Every Auto drawn from the red audience start begins exactly where {@link StartPositions}
 * says the drive team places the robot. The Visualizer's START is typed by hand, the measured
 * pose comes from the field, and a path that starts somewhere else begins with a lurch onto the
 * drawing — so when one moves, the other moves in the same PR (change the {@code .pp}, export
 * again, and update {@code StartPositions} from a new measurement).
 */
public class AutoStartPositionsTest {

    private static final double TOLERANCE_IN = 0.01;
    private static final double TOLERANCE_RAD = Math.toRadians(0.1);

    @Test
    public void rSimpleStartsAtTheRedAudienceStart() {
        assertStartsAt(StartPositions.RED_AUDIENCE.pose, RSimpleAuto.startPose(false));
    }

    @Test
    public void rQualsStartsAtTheRedAudienceStart() {
        assertStartsAt(StartPositions.RED_AUDIENCE.pose, RQualsAuto.startPose(false));
    }

    @Test
    public void sisterRightFixedStartsAtTheRedAudienceStart() {
        assertStartsAt(StartPositions.RED_AUDIENCE.pose, SisterRightFixedAuto.startPose(false));
    }

    @Test
    public void rLaunchParkStartsAtTheRedAudienceStart() {
        assertStartsAt(StartPositions.RED_AUDIENCE.pose, RLaunchParkAuto.startPose(false));
    }

    @Test
    public void rQualsPollenOnlyStartsAtTheRedAudienceStart() {
        assertStartsAt(StartPositions.RED_AUDIENCE.pose, RQualsPollenOnlyAuto.startPose(false));
    }

    private static void assertStartsAt(Pose measured, Pose drawn) {
        assertEquals("x", measured.x(), drawn.x(), TOLERANCE_IN);
        assertEquals("y", measured.y(), drawn.y(), TOLERANCE_IN);
        assertEquals("heading", measured.heading(), drawn.heading(), TOLERANCE_RAD);
    }
}
