package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;

import org.firstinspires.ftc.teamcode.subsystems.IndicatorSubsystem.Signal;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

/** The light shows the first thing that holds, in the class comment's order. */
public class IndicatorSubsystemTest {

    private static final double EPS = 1e-9;

    private static Signal init(boolean battery, boolean fault, boolean noFrames, boolean onField) {
        return IndicatorSubsystem.choose(true, battery, fault, noFrames, false, onField, Alliance.RED);
    }

    @Test
    public void inInitTheBatteryComesFirstThenTheCamera() {
        assertEquals(Signal.BATTERY_LOW, init(true, true, true, false));
        assertEquals(Signal.CAMERA_FAULT, init(false, true, true, false));
        assertEquals(Signal.CAMERA_NO_FRAMES, init(false, false, true, true));
        assertEquals(Signal.POSE_UNREFERENCED, init(false, false, false, false));
        assertEquals(Signal.ALLIANCE_RED, init(false, false, false, true));
    }

    @Test
    public void afterPlayTheBatteryAndFirstFrameNoLongerShow() {
        assertEquals(Signal.ALLIANCE_BLUE,
                IndicatorSubsystem.choose(false, true, false, true, false, true, Alliance.BLUE));
        assertEquals("a dead camera still shows", Signal.CAMERA_FAULT,
                IndicatorSubsystem.choose(false, false, true, false, true, true, Alliance.BLUE));
    }

    @Test
    public void aGrabShowsOverAnUnreferencedPose() {
        assertEquals(Signal.BUSY, IndicatorSubsystem.choose(false, false, false, false, true, false, Alliance.RED));
    }

    @Test
    public void noAllianceIsWhite() {
        assertEquals(Signal.NO_ALLIANCE,
                IndicatorSubsystem.choose(false, false, false, false, false, true, Alliance.UNKNOWN));
        assertEquals(IndicatorSubsystem.WHITE, IndicatorSubsystem.position(Signal.NO_ALLIANCE, 0), EPS);
    }

    @Test
    public void aFlashingSignalIsOffHalfTheTime() {
        long half = (long) IndicatorSubsystem.FLASH_MS;
        assertEquals(IndicatorSubsystem.RED, IndicatorSubsystem.position(Signal.BATTERY_LOW, 0), EPS);
        assertEquals(IndicatorSubsystem.OFF, IndicatorSubsystem.position(Signal.BATTERY_LOW, half), EPS);
        assertEquals(IndicatorSubsystem.RED, IndicatorSubsystem.position(Signal.BATTERY_LOW, 2 * half), EPS);
        assertEquals("solid", IndicatorSubsystem.ORANGE, IndicatorSubsystem.position(Signal.CAMERA_FAULT, half), EPS);
    }
}
