package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

    // ---------------------------------------------------------------- the lane, after PLAY

    private static Signal play(int lane, boolean busy, boolean fault) {
        return IndicatorSubsystem.choose(false, false, fault, false, busy, lane, 3, true, Alliance.RED);
    }

    @Test
    public void afterPlayTheLaneShowsFullThenSomeThenTheAlliance() {
        assertEquals(Signal.LANE_FULL, play(3, false, false));
        assertEquals(Signal.LANE_FULL, play(4, false, false));
        assertEquals(Signal.LANE_SOME, play(1, false, false));
        assertEquals(Signal.ALLIANCE_RED, play(0, false, false));
        assertEquals(IndicatorSubsystem.GREEN, IndicatorSubsystem.position(Signal.LANE_FULL, 0), EPS);
        assertEquals(IndicatorSubsystem.WHITE, IndicatorSubsystem.position(Signal.LANE_SOME, 0), EPS);
    }

    @Test
    public void aFaultOrAGrabOutranksTheLaneAndInitNeverShowsIt() {
        assertEquals(Signal.CAMERA_FAULT, play(3, false, true));
        assertEquals(Signal.BUSY, play(3, true, false));
        assertEquals(Signal.ALLIANCE_RED,
                IndicatorSubsystem.choose(true, false, false, false, false, 4, 3, true, Alliance.RED));
    }

    @Test
    public void aZeroFullCountNeverShowsFull() {
        assertEquals(Signal.LANE_SOME,
                IndicatorSubsystem.choose(false, false, false, false, false, 4, 0, true, Alliance.RED));
    }

    @Test
    public void atLaunchSpeedTheLaneAndAllianceFlashButFaultsDoNot() {
        assertTrue(IndicatorSubsystem.flashesWhenReady(Signal.LANE_FULL));
        assertTrue(IndicatorSubsystem.flashesWhenReady(Signal.ALLIANCE_BLUE));
        assertFalse(IndicatorSubsystem.flashesWhenReady(Signal.CAMERA_FAULT));
        assertFalse(IndicatorSubsystem.flashesWhenReady(Signal.BUSY));
        long half = (long) IndicatorSubsystem.FLASH_MS;
        assertEquals(IndicatorSubsystem.GREEN, IndicatorSubsystem.position(Signal.LANE_FULL, 0, true), EPS);
        assertEquals(IndicatorSubsystem.OFF, IndicatorSubsystem.position(Signal.LANE_FULL, half, true), EPS);
        assertEquals("steady when not ready", IndicatorSubsystem.GREEN,
                IndicatorSubsystem.position(Signal.LANE_FULL, half, false), EPS);
    }
}
