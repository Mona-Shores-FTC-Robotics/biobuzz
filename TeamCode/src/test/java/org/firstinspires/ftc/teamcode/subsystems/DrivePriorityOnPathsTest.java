package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;

import com.pedropathing.algorithm.ForesightConfig;

import org.junit.Test;

/** {@link DriveSubsystem#drivePriorityOnPaths}: the two Foresight settings it writes, and Pedro's own when off. */
public class DrivePriorityOnPathsTest {

    private final ForesightConfig config = new ForesightConfig(c -> { });

    @Test
    public void offIsPedrosOwnSplit() {
        assertEquals("Pedro's default, so off changes nothing", 0.5, config.headingDriveRatio.get(), 0);
        DriveSubsystem.applyPathPriority(config, false, 25.0);
        assertEquals(0.5, config.headingDriveRatio.get(), 0);
        assertEquals(Math.toRadians(11.25), config.headingDeviationTolerance.get(), 1e-12);
    }

    @Test
    public void onPutsTheDriveAheadOfHeadingFeedbackWithinTheTolerance() {
        DriveSubsystem.applyPathPriority(config, true, 25.0);
        assertEquals(0.0, config.headingDriveRatio.get(), 0);
        assertEquals(Math.toRadians(25.0), config.headingDeviationTolerance.get(), 1e-12);
    }

    @Test
    public void turningItOffAgainPutsPedrosBack() {
        DriveSubsystem.applyPathPriority(config, true, 40.0);
        DriveSubsystem.applyPathPriority(config, false, 40.0);
        assertEquals(0.5, config.headingDriveRatio.get(), 0);
        assertEquals(Math.toRadians(11.25), config.headingDeviationTolerance.get(), 1e-12);
    }

    @Test
    public void aBadToleranceIsClampedOrPedros() {
        DriveSubsystem.applyPathPriority(config, true, 500.0);
        assertEquals(Math.toRadians(90.0), config.headingDeviationTolerance.get(), 1e-12);
        DriveSubsystem.applyPathPriority(config, true, -3.0);
        assertEquals(Math.toRadians(5.0), config.headingDeviationTolerance.get(), 1e-12);
        DriveSubsystem.applyPathPriority(config, true, Double.NaN);
        assertEquals(Math.toRadians(11.25), config.headingDeviationTolerance.get(), 1e-12);
    }
}
