package org.firstinspires.ftc.teamcode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.firstinspires.ftc.teamcode.hardware.RobotIdentity;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * Pins {@link RobotDifferences}: a robot's lines apply to that robot only, once per app run, and a
 * config change after a robot with lines refuses to run rather than mix the two robots' values.
 *
 * <p>The bookkeeping is tested on a fresh {@link RobotDifferences.Table} with this test's own
 * fields. The real lines are not applied here: they assign the subsystems' static fields, which
 * every other test in this JVM reads.
 */
public class RobotDifferencesTest {

    /** Stand-ins for a subsystem's {@code @Configurable} fields. */
    private static boolean reversed;
    private static double retracted;

    private static RobotDifferences.Table table() {
        reversed = true;
        retracted = 0.6;
        RobotDifferences.Table table = new RobotDifferences.Table();
        table.add(RobotIdentity.TEAM_19429, "intake motor swapped, turns the other way",
                () -> reversed = false);
        table.add(RobotIdentity.TEAM_19429, "stick servo re-splined", () -> retracted = 0.55);
        return table;
    }

    @Test
    public void aRobotsLinesApplyToThatRobot() {
        RobotDifferences.Table table = table();
        assertEquals(Arrays.asList("intake motor swapped, turns the other way", "stick servo re-splined"),
                table.apply(RobotIdentity.TEAM_19429));
        assertFalse(reversed);
        assertEquals(0.55, retracted, 0.0);
    }

    @Test
    public void theOtherRobotKeepsTheSharedValues() {
        RobotDifferences.Table table = table();
        assertEquals(Collections.emptyList(), table.apply(RobotIdentity.TEAM_20245));
        assertTrue(reversed);
        assertEquals(0.6, retracted, 0.0);
    }

    @Test
    public void appliedOncePerRunSoAPanelsEditSticks() {
        RobotDifferences.Table table = table();
        table.apply(RobotIdentity.TEAM_19429);
        retracted = 0.5;  // edited in Panels after the first OpMode
        table.apply(RobotIdentity.TEAM_19429);
        assertEquals("the next OpMode must not undo a Panels edit", 0.5, retracted, 0.0);
    }

    @Test
    public void switchingAwayFromARobotWithLinesRefuses() {
        RobotDifferences.Table table = table();
        table.apply(RobotIdentity.TEAM_19429);
        try {
            table.apply(RobotIdentity.TEAM_20245);
            fail("20245 would run with 19429's differences still loaded");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains("Restart the Robot Controller app"));
        }
    }

    @Test
    public void switchingAwayFromARobotWithoutLinesIsFine() {
        RobotDifferences.Table table = table();
        table.apply(RobotIdentity.TEAM_20245);
        assertEquals(2, table.apply(RobotIdentity.TEAM_19429).size());
        assertFalse(reversed);
    }

    @Test(expected = IllegalArgumentException.class)
    public void everyLineSaysWhy() {
        new RobotDifferences.Table().add(RobotIdentity.TEAM_19429, " ", () -> reversed = false);
    }

    @Test
    public void theRealLinesAllSayWhy() {
        // Loading the class builds the real table; a blank reason throws there.
        for (RobotIdentity robot : RobotIdentity.values()) {
            for (String why : RobotDifferences.of(robot)) {
                assertFalse(robot + " has a blank reason", why.trim().isEmpty());
            }
        }
    }
}
