package org.firstinspires.ftc.teamcode.controls;

import org.firstinspires.ftc.teamcode.subsystems.IntakeSubsystem;

/**
 * Who runs the intake this loop, as one rule: <b>the driver wins</b>. The driver's intake or
 * reverse, if pressed, is what happens. Otherwise the operator's unjam (which reverses the intake
 * with the feeder), and otherwise the operator's own intake, in a scheme that gives them one.
 * Otherwise it idles.
 *
 * <p>Decided every loop rather than on presses, so letting go of one person's button hands the
 * intake straight to whatever the other is holding. {@link #owner()} is logged every loop, so the
 * two schemes can be compared from the match log.
 *
 * <p>An object with fields rather than a returned pair: it runs every loop and allocates nothing.
 */
public final class IntakeArbiter {

    /** Who the intake listened to. */
    public enum Owner { NONE, DRIVER, OPERATOR }

    private IntakeSubsystem.State state = IntakeSubsystem.State.IDLE;
    private Owner owner = Owner.NONE;

    /** Decide; read the answer with {@link #state()} and {@link #owner()}. */
    public void decide(boolean driverIn, boolean driverOut, boolean operatorUnjam, boolean operatorIn) {
        if (driverIn || driverOut) {
            // Both at once is a thumb slip; reverse is the safe reading, as an unjam.
            state = driverOut ? IntakeSubsystem.State.OUT : IntakeSubsystem.State.IN;
            owner = Owner.DRIVER;
        } else if (operatorUnjam) {
            state = IntakeSubsystem.State.OUT;
            owner = Owner.OPERATOR;
        } else if (operatorIn) {
            state = IntakeSubsystem.State.IN;
            owner = Owner.OPERATOR;
        } else {
            state = IntakeSubsystem.State.IDLE;
            owner = Owner.NONE;
        }
    }

    public IntakeSubsystem.State state() {
        return state;
    }

    public Owner owner() {
        return owner;
    }
}
