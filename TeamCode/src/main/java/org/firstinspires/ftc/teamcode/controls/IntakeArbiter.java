package org.firstinspires.ftc.teamcode.controls;

import org.firstinspires.ftc.teamcode.subsystems.IntakeSubsystem;

/**
 * Who runs the intake this loop, as one rule: <b>the driver wins</b>. The driver's intake or
 * unjam, if pressed, is what happens. Otherwise the operator's unjam, and otherwise the operator's
 * intake. Otherwise it idles. (Unjam also reverses the feeder; that is the OpMode's, not this.)
 *
 * <p>Decided every loop rather than on presses, so letting go of one person's button hands the
 * intake straight to whatever the other is holding. {@link #owner()} is logged every loop, so the
 * match log shows who ran it.
 *
 * <p>An object with fields rather than a returned pair: it runs every loop and allocates nothing.
 */
public final class IntakeArbiter {

    /** Who the intake listened to. */
    public enum Owner { NONE, DRIVER, OPERATOR }

    private IntakeSubsystem.State state = IntakeSubsystem.State.IDLE;
    private Owner owner = Owner.NONE;

    /** Decide; read the answer with {@link #state()} and {@link #owner()}. */
    public void decide(boolean driverIn, boolean driverUnjam, boolean operatorUnjam, boolean operatorIn) {
        if (driverIn || driverUnjam) {
            // Both at once is a thumb slip; unjam is the safe reading.
            state = driverUnjam ? IntakeSubsystem.State.OUT : IntakeSubsystem.State.IN;
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
