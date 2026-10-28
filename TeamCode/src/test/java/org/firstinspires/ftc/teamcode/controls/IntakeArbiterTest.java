package org.firstinspires.ftc.teamcode.controls;

import static org.junit.Assert.assertEquals;

import org.firstinspires.ftc.teamcode.controls.IntakeArbiter.Owner;
import org.firstinspires.ftc.teamcode.subsystems.IntakeSubsystem.State;
import org.junit.Test;

public class IntakeArbiterTest {

    private final IntakeArbiter arbiter = new IntakeArbiter();

    private void expect(State state, Owner owner) {
        assertEquals(state, arbiter.state());
        assertEquals(owner, arbiter.owner());
    }

    @Test
    public void nobodyMeansIdle() {
        arbiter.decide(false, false, false, false);
        expect(State.IDLE, Owner.NONE);
    }

    @Test
    public void theDriverWinsOverEverythingTheOperatorHolds() {
        arbiter.decide(true, false, true, true);
        expect(State.IN, Owner.DRIVER);
        arbiter.decide(false, true, false, true);
        expect(State.OUT, Owner.DRIVER);
    }

    @Test
    public void bothDriverTriggersReadAsReverse() {
        arbiter.decide(true, true, false, false);
        expect(State.OUT, Owner.DRIVER);
    }

    @Test
    public void unjamReversesAndBeatsTheOperatorsOwnIntake() {
        arbiter.decide(false, false, true, true);
        expect(State.OUT, Owner.OPERATOR);
    }

    @Test
    public void theOperatorRunsItWhenTheDriverLetsGo() {
        arbiter.decide(true, false, false, true);
        expect(State.IN, Owner.DRIVER);
        arbiter.decide(false, false, false, true);
        expect(State.IN, Owner.OPERATOR);
    }
}
