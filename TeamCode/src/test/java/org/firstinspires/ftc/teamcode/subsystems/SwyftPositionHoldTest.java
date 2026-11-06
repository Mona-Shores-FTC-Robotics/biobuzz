package org.firstinspires.ftc.teamcode.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class SwyftPositionHoldTest {
    private static final long MS = 1_000_000L;
    private double power(SwyftPositionHold c, boolean enabled, double error, double rate, long ms) {
        return c.power(enabled, error, rate, 150, 350, 1, 1, 100, ms * MS);
    }

    @Test
    public void usesMeasuredPulseThenWaitsForOffTimeAndQuietFeedback() {
        SwyftPositionHold c = new SwyftPositionHold();
        assertEquals(0, power(c, true, 5, 0, 0), 0);
        assertEquals(1, power(c, true, 5, 0, 100), 0);
        assertEquals(1, power(c, true, 4, 3, 249), 0);
        assertEquals(0, power(c, true, 4, 3, 250), 0);
        assertEquals(0, power(c, true, 3, 0, 599), 0);
        assertEquals(0, power(c, true, 3, 0, 600), 0);
        assertEquals(0, power(c, true, 3, 0, 699), 0);
        assertEquals(1, power(c, true, 3, 0, 700), 0);
    }

    @Test
    public void crossingTargetOrToleranceNeverSkipsTheSettlingWait() {
        SwyftPositionHold c = new SwyftPositionHold();
        power(c, true, 5, 0, 0);
        assertEquals(1, power(c, true, 5, 0, 100), 0);
        assertEquals(0, power(c, true, .5, 8, 200), 0);
        assertEquals(0, power(c, true, -3, 8, 201), 0);
        assertEquals(0, power(c, true, -3, 8, 550), 0);
        assertEquals(0, power(c, true, -3, 0, 600), 0);
        assertEquals(0, power(c, true, -3, 5, 650), 0); // resets quiet timer
        assertEquals(0, power(c, true, -3, 0, 700), 0);
        assertEquals(-1, power(c, true, -3, 0, 800), 0);
    }

    @Test
    public void releaseStopsImmediatelyAndRearmingKeepsCooldown() {
        SwyftPositionHold c = new SwyftPositionHold();
        power(c, true, 5, 0, 0);
        assertEquals(1, power(c, true, 5, 0, 100), 0);
        assertEquals(0, power(c, false, 5, 0, 110), 0);
        assertEquals(0, power(c, true, 5, 0, 111), 0);
        assertEquals(0, power(c, true, 5, 0, 460), 0);
        assertEquals(1, power(c, true, 5, 0, 560), 0);
    }

    @Test
    public void holdsWithoutDriveAndReacquiresAfterDisturbance() {
        SwyftPositionHold c = new SwyftPositionHold();
        assertEquals(0, power(c, true, .5, 0, 0), 0);
        assertEquals(SwyftPositionHold.Phase.HOLDING, c.phase());
        assertEquals(0, power(c, true, 4, 6, 100), 0);
        assertEquals(0, power(c, true, 4, 0, 200), 0);
        assertEquals(1, power(c, true, 4, 0, 300), 0);
    }

    @Test
    public void invalidFeedbackDisablesInsteadOfDriving() {
        SwyftPositionHold c = new SwyftPositionHold();
        power(c, true, 5, 0, 0);
        power(c, true, 5, 0, 100);
        assertEquals(0, power(c, true, Double.NaN, 0, 110), 0);
        assertEquals(SwyftPositionHold.Phase.FAULT, c.phase());
        assertEquals(0, power(c, true, 5, Double.NaN, 120), 0);
    }

    @Test
    public void reachesBandOnDelayedStepPlantAndReturnsAfterPush() {
        SwyftPositionHold c = new SwyftPositionHold();
        double angle = 0, rate = 0, previous = 0, direction = 0;
        long driveAt = -1000, offAt = -1000;
        for (long ms = 0; ms <= 16000; ms += 10) {
            if (ms == 8000) angle += 4; // external push while feedback hold remains selected
            double output = power(c, true, 5 - angle, rate, ms);
            if (output != 0 && previous == 0) { driveAt = ms; direction = output; }
            if (output == 0 && previous != 0) offAt = ms;
            // Measured behavior: delayed startup, then motion after PWM is disabled.
            boolean moving = (output != 0 && ms - driveAt >= 130)
                    || (output == 0 && ms - offAt < 110);
            rate = moving ? direction * 10 : 0;
            angle += rate * .01;
            previous = output;
            if (ms == 7900 || ms == 16000) {
                assertTrue("must reach 1-degree band, angle=" + angle, Math.abs(5 - angle) <= 1);
                assertEquals(0, output, 0);
            }
        }
    }
}
