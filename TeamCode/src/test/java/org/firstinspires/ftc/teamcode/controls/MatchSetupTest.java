package org.firstinspires.ftc.teamcode.controls;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.util.Alliance;
import org.junit.Test;

public class MatchSetupTest {

    private final MatchSetup setup = new MatchSetup();

    @Test
    public void nothingChosenMeansUnknownNeverAGuess() {
        assertEquals(Alliance.UNKNOWN, setup.alliance());
        assertEquals(MatchSetup.Source.NONE, setup.source());

        setup.lock();
        assertEquals(Alliance.UNKNOWN, setup.alliance());
    }

    @Test
    public void visionAloneIsUsed() {
        setup.offerVision(Alliance.RED, "sees RED_SCORING");

        assertEquals(Alliance.RED, setup.alliance());
        assertEquals(MatchSetup.Source.VISION, setup.source());
    }

    @Test
    public void visionFollowsTheCameraUntilSomeoneChooses() {
        setup.offerVision(Alliance.RED, "");
        setup.offerVision(Alliance.BLUE, "");

        assertEquals(Alliance.BLUE, setup.alliance());
    }

    @Test
    public void autoBeatsVisionAndTheDisagreementIsVisible() {
        setup.inheritFromAuto(Alliance.BLUE);
        setup.offerVision(Alliance.RED, "");

        assertEquals(Alliance.BLUE, setup.alliance());
        assertEquals(MatchSetup.Source.AUTO, setup.source());
        assertTrue(setup.hasDisagreement());
    }

    @Test
    public void aManualChoiceBeatsEverythingAndSticks() {
        setup.inheritFromAuto(Alliance.RED);
        setup.chooseManually(Alliance.BLUE);
        setup.offerVision(Alliance.RED, "");

        assertEquals(Alliance.BLUE, setup.alliance());
        assertEquals(MatchSetup.Source.MANUAL, setup.source());
        assertTrue(setup.hasDisagreement());
    }

    @Test
    public void agreeingSourcesAreNotADisagreement() {
        setup.inheritFromAuto(Alliance.RED);
        setup.offerVision(Alliance.RED, "");
        setup.chooseManually(Alliance.RED);

        assertFalse(setup.hasDisagreement());
    }

    @Test
    public void choosingUnknownDoesNotClearAChoice() {
        setup.chooseManually(Alliance.RED);
        setup.chooseManually(Alliance.UNKNOWN);

        assertEquals(Alliance.RED, setup.alliance());
    }

    @Test
    public void lockFreezesEverySource() {
        setup.offerVision(Alliance.RED, "");
        setup.lock();

        setup.chooseManually(Alliance.BLUE);
        setup.inheritFromAuto(Alliance.BLUE);
        setup.offerVision(Alliance.BLUE, "");

        assertEquals(Alliance.RED, setup.alliance());
        assertTrue(setup.isLocked());
    }
}
