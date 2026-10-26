package org.firstinspires.ftc.teamcode.opmodes.auto;

import static org.junit.Assert.assertEquals;

import org.firstinspires.ftc.teamcode.subsystems.IntakeSubsystem.State;
import org.junit.Test;

/**
 * The rule {@link BuiltAuto} runs the intake by: on whenever the robot has room, as the simulator
 * plays the Autos, and off at the legal limit.
 */
public class BuiltAutoTest {

    @Test
    public void intakeRunsWhileThereIsRoom() {
        assertEquals(State.IN, BuiltAuto.intakeRequest(0));
        assertEquals(State.IN, BuiltAuto.intakeRequest(BuiltAuto.MAX_HELD - 1));
    }

    @Test
    public void intakeStopsAtTheLegalLimit() {
        assertEquals(4, BuiltAuto.MAX_HELD); // G407: never hold more than four
        assertEquals(State.IDLE, BuiltAuto.intakeRequest(BuiltAuto.MAX_HELD));
        assertEquals(State.IDLE, BuiltAuto.intakeRequest(BuiltAuto.MAX_HELD + 1));
    }

    @Test
    public void anUncountedIntakeCountsAsHavingRoom() {
        // Until #181 the OpMode passes 0: the roller runs the whole Auto.
        assertEquals(State.IN, BuiltAuto.intakeRequest(0));
    }
}
