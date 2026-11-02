package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;

import org.firstinspires.ftc.teamcode.subsystems.IndicatorSubsystem.Signal;
import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

/** The light shows only what the drive team acts on: the class comment's three rows. */
public class IndicatorSubsystemTest {

    private static final double EPS = 1e-9;

    @Test
    public void anInitWarningFlashesWhite() {
        assertEquals(Signal.CHECK_DRIVER_STATION, IndicatorSubsystem.choose(true, true, false, Alliance.RED));
        assertEquals("a warning outranks a full lane", Signal.CHECK_DRIVER_STATION,
                IndicatorSubsystem.choose(true, true, true, Alliance.BLUE));
        long half = (long) IndicatorSubsystem.FLASH_MS;
        assertEquals(IndicatorSubsystem.WHITE, IndicatorSubsystem.position(Signal.CHECK_DRIVER_STATION, 0), EPS);
        assertEquals(IndicatorSubsystem.OFF, IndicatorSubsystem.position(Signal.CHECK_DRIVER_STATION, half), EPS);
        assertEquals(IndicatorSubsystem.WHITE, IndicatorSubsystem.position(Signal.CHECK_DRIVER_STATION, 2 * half), EPS);
    }

    @Test
    public void warningsShowOnlyBeforePlay() {
        assertEquals(Signal.ALLIANCE_RED, IndicatorSubsystem.choose(false, true, false, Alliance.RED));
    }

    @Test
    public void aFullLaneIsGreenAfterPlay() {
        assertEquals(Signal.FULL, IndicatorSubsystem.choose(false, false, true, Alliance.RED));
        assertEquals(IndicatorSubsystem.GREEN, IndicatorSubsystem.position(Signal.FULL, 0), EPS);
        assertEquals("steady", IndicatorSubsystem.GREEN,
                IndicatorSubsystem.position(Signal.FULL, (long) IndicatorSubsystem.FLASH_MS), EPS);
    }

    @Test
    public void aFullLaneDoesNotShowInInit() {
        assertEquals("the preloads are not news", Signal.ALLIANCE_BLUE,
                IndicatorSubsystem.choose(true, false, true, Alliance.BLUE));
    }

    @Test
    public void otherwiseTheAllianceSteady() {
        assertEquals(Signal.ALLIANCE_RED, IndicatorSubsystem.choose(true, false, false, Alliance.RED));
        assertEquals(Signal.ALLIANCE_BLUE, IndicatorSubsystem.choose(false, false, false, Alliance.BLUE));
        long half = (long) IndicatorSubsystem.FLASH_MS;
        assertEquals(IndicatorSubsystem.RED, IndicatorSubsystem.position(Signal.ALLIANCE_RED, half), EPS);
        assertEquals(IndicatorSubsystem.BLUE, IndicatorSubsystem.position(Signal.ALLIANCE_BLUE, half), EPS);
    }

    @Test
    public void noAllianceIsOff() {
        assertEquals(Signal.OFF, IndicatorSubsystem.choose(false, false, false, Alliance.UNKNOWN));
        assertEquals(IndicatorSubsystem.OFF, IndicatorSubsystem.position(Signal.OFF, 0), EPS);
    }
}
